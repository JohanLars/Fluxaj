package com.fluxaj.service;

import com.fluxaj.model.Subscription;
import com.fluxaj.model.Usuario;
import com.fluxaj.repository.SubscriptionRepository;
import com.fluxaj.repository.UsuarioRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;

@Service
public class GmailScannerService {

    @Autowired
    private TokenEncryptionService tokenEncryptionService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ELIMINAMOS @Async y CompletableFuture para forzar a la app a esperar el escaneo
    public Integer escanearCorreos(Long usuarioId) {
        System.out.println(" [SCANNER] Iniciando proceso para usuario ID: " + usuarioId);
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la DB"));

            if (usuario.getAccessTokenCifrado() == null || usuario.getAccessTokenCifrado().isEmpty()) {
                System.out.println(" [SCANNER] El usuario no tiene un Token OAuth2 guardado.");
                return 0;
            }

            // Descifrar el token
            String accessToken = tokenEncryptionService.decrypt(usuario.getAccessTokenCifrado());
            System.out.println(" [SCANNER] Token descifrado correctamente.");

            Gmail gmailService = buildGmailService(accessToken);
            int suscripcionesEncontradas = procesarCorreos(gmailService, usuario);

            System.out.println(" [SCANNER] Éxito. Suscripciones nuevas guardadas: " + suscripcionesEncontradas);
            return suscripcionesEncontradas;

        } catch (Exception e) {
            System.err.println(" [SCANNER] ERROR CRÍTICO: " + e.getMessage());
            e.printStackTrace(); 
            return 0; // Devolvemos 0 en vez de fallar silenciosamente
        }
    }

    private int procesarCorreos(Gmail gmail, Usuario usuario) throws Exception {
        int contador = 0;
        System.out.println(" [SCANNER] Buscando recibos en Gmail...");

        ListMessagesResponse response = gmail.users().messages()
            .list("me")
            .setQ("subject:(invoice OR receipt OR factura OR recibo OR pago OR suscripción) newer_than:90d")
            .setMaxResults(50L) // Limitamos a 50 para pruebas
            .execute();

        if (response.getMessages() == null) {
            System.out.println(" [SCANNER] No se encontraron correos que coincidan con la búsqueda.");
            return 0;
        }

        System.out.println(" [SCANNER] Se encontraron " + response.getMessages().size() + " correos potenciales. Analizando...");

        for (Message msgRef : response.getMessages()) {
            Message msg = gmail.users().messages()
                .get("me", msgRef.getId())
                .setFormat("metadata")
                .setMetadataHeaders(List.of("Subject", "From", "Date"))
                .execute();

            String asunto = getHeader(msg, "Subject");
            String remitente = getHeader(msg, "From");

            if (!esRecibo(asunto, remitente)) continue;

            ResultadoExtraccion resultado = extraerDatos(asunto, remitente);
            if (resultado == null) continue;

            // Evitar duplicados por nombre de proveedor para este usuario
            boolean yaExiste = subscriptionRepository
                .findByUsuarioId(usuario.getId())
                .stream()
                .anyMatch(s -> s.getProveedor().equalsIgnoreCase(resultado.proveedor));

            if (!yaExiste) {
                Subscription sub = new Subscription();
                sub.setProveedor(resultado.proveedor);
                sub.setMonto(resultado.monto);
                sub.setFechaCobro(resultado.fecha);
                sub.setCategoria(resultado.categoria);
                sub.setActiva(true);
                sub.setUsuario(usuario);
                sub.setOrigenEscaneo(true);
                
                subscriptionRepository.save(sub);
                System.out.println(" [SCANNER] Guardada suscripción detectada: " + resultado.proveedor);
                contador++;
            }
        }
        return contador;
    }

    private boolean esRecibo(String asunto, String remitente) {
        if (asunto == null) return false;
        return SubscriptionRegexPatterns.ES_RECIBO.matcher(asunto).find()
            || (remitente != null && SubscriptionRegexPatterns.PROVEEDOR.matcher(remitente).find());
    }

    private ResultadoExtraccion extraerDatos(String asunto, String remitente) {
        try {
            String textoCompleto = (asunto != null ? asunto : "") + " " + (remitente != null ? remitente : "");

            // 1. Detectar proveedor
            Matcher mProveedor = SubscriptionRegexPatterns.PROVEEDOR.matcher(textoCompleto);
            String proveedor = null;
            if (mProveedor.find()) {
                proveedor = capitalizar(mProveedor.group(1));
            } else {
                proveedor = extraerDominioRemitente(remitente);
            }

            if (proveedor == null || proveedor.isEmpty()) return null;

            // 2. Detectar monto con limpieza profunda
            Matcher mMonto = SubscriptionRegexPatterns.MONTO.matcher(asunto);
            double montoFinal = 0.0;
            if (mMonto.find()) {
                try {
                    String montoRaw = mMonto.group(1).replace(".", "").replace(",", ".");
                    montoRaw = montoRaw.replaceAll("[^0-9.]", "");
                    montoFinal = Double.parseDouble(montoRaw);
                } catch (Exception e) {
                    System.out.println(" [SCANNER] No se pudo procesar el monto de: " + proveedor);
                }
            }

            // 3. Categorizar y Fecha
            String categoria = categorizarProveedor(proveedor);
            LocalDate fechaCobro = LocalDate.now().plusDays(30);

            return new ResultadoExtraccion(proveedor, montoFinal, fechaCobro, categoria);
        } catch (Exception e) {
            return null;
        }
    }

    private String categorizarProveedor(String proveedor) {
        String p = proveedor.toLowerCase();
        if (SubscriptionRegexPatterns.STREAMING.matcher(p).find()) return "Streaming";
        if (SubscriptionRegexPatterns.TRABAJO.matcher(p).find())   return "Trabajo";
        if (SubscriptionRegexPatterns.SALUD.matcher(p).find())     return "Salud";
        if (SubscriptionRegexPatterns.GAMING.matcher(p).find())    return "Gaming";
        return "Otros";
    }

    private String extraerDominioRemitente(String remitente) {
        if (remitente == null) return null;
        Matcher m = java.util.regex.Pattern.compile("@([a-zA-Z0-9-]+)\\.").matcher(remitente);
        return m.find() ? capitalizar(m.group(1)) : null;
    }

    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String getHeader(Message msg, String nombre) {
        if (msg.getPayload() == null || msg.getPayload().getHeaders() == null) return "";
        return msg.getPayload().getHeaders().stream()
            .filter(h -> h.getName().equalsIgnoreCase(nombre))
            .map(MessagePartHeader::getValue)
            .findFirst().orElse("");
    }

    private Gmail buildGmailService(String accessToken) throws Exception {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
        return new Gmail.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials)
        ).setApplicationName("Fluxaj").build();
    }

    private static class ResultadoExtraccion {
        String proveedor;
        double monto;
        LocalDate fecha;
        String categoria;

        ResultadoExtraccion(String proveedor, double monto, LocalDate fecha, String categoria) {
            this.proveedor = proveedor;
            this.monto = monto;
            this.fecha = fecha;
            this.categoria = categoria;
        }
    }
}