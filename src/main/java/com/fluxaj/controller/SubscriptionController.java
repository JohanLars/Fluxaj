package com.fluxaj.controller;

import com.fluxaj.model.Subscription;
import com.fluxaj.model.Usuario;
import com.fluxaj.repository.SubscriptionRepository;
import com.fluxaj.repository.UsuarioRepository;
import com.fluxaj.service.EmailService;
import com.fluxaj.service.GmailScannerService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suscripciones")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    @Autowired
    private SubscriptionRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/me")
    public ResponseEntity<Usuario> obtenerUsuarioActual(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            return ResponseEntity.ok(usuarioRepository.findByEmail(oidcUser.getEmail()).orElse(null));
        }
        return ResponseEntity.ok(usuarioRepository.findById(1L).orElse(null));
    }

    @GetMapping
    public List<Subscription> listarMisSuscripciones(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            Usuario usuario = usuarioRepository.findByEmail(oidcUser.getEmail()).orElse(null);
            if (usuario != null) {
                return repository.findByUsuarioId(usuario.getId());
            }
        }
        return repository.findByUsuarioId(1L);
    }

    // Módulo de Auditoría: Trae todas las suscripciones a la base de datos
    @GetMapping("/admin/all")
    public ResponseEntity<List<Subscription>> listarTodoGlobal(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            Usuario admin = usuarioRepository.findByEmail(oidcUser.getEmail()).orElse(null);
            if (admin != null && "ADMIN".equals(admin.getRol())) {
                return ResponseEntity.ok(repository.findAll());
            }
        }
        return ResponseEntity.status(403).build();
    }

    // CREAR: Envío de correo notificador
    @PostMapping
    public Subscription guardar(@Valid @RequestBody Subscription suscripcion,
            @AuthenticationPrincipal OidcUser oidcUser) {
        Usuario usuarioAsignado = null;

        if (oidcUser != null) {
            usuarioAsignado = usuarioRepository.findByEmail(oidcUser.getEmail()).orElse(null);
        } else {
            usuarioAsignado = usuarioRepository.findById(1L).orElse(null);
        }

        suscripcion.setUsuario(usuarioAsignado);
        Subscription guardada = repository.save(suscripcion);

        if (usuarioAsignado != null && usuarioAsignado.getEmail() != null) {
            emailService.enviarCorreoNuevaSuscripcion(
                    usuarioAsignado.getEmail(),
                    guardada.getProveedor(),
                    guardada.getMonto(),
                    guardada.getFechaCobro().toString());
        }

        return guardada;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Subscription detalles) {
        return repository.findById(id).map(suscripcion -> {
            suscripcion.setProveedor(detalles.getProveedor());
            suscripcion.setMonto(detalles.getMonto());
            suscripcion.setFechaCobro(detalles.getFechaCobro());
            repository.save(suscripcion);
            return ResponseEntity.ok(suscripcion);
        }).orElse(ResponseEntity.status(404).body(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id, @AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            Usuario usuario = usuarioRepository.findByEmail(oidcUser.getEmail()).orElse(null);

            if (usuario != null && "ADMIN".equals(usuario.getRol())) {
                repository.deleteById(id);
                return ResponseEntity.ok("Suscripción eliminada");
            } else {
                return ResponseEntity.status(403).body("Acceso denegado: Solo los administradores pueden eliminar.");
            }
        }
        return ResponseEntity.status(401).body("Debes iniciar sesión.");
    }

    @Autowired
    private GmailScannerService gmailScannerService;

    // El usuario hace clic en "Escanear mi Gmail"
    @PostMapping("/escanear")
    public ResponseEntity<String> escanearGmail(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null)
            return ResponseEntity.status(401).body("No autenticado");

        Usuario usuario = usuarioRepository.findByEmail(oidcUser.getEmail()).orElse(null);
        if (usuario == null)
            return ResponseEntity.status(404).body("Usuario no encontrado");

        // Lanza el escaneo en hilo separado y responde inmediatamente
        gmailScannerService.escanearCorreos(usuario.getId());

        return ResponseEntity.ok("Escaneo iniciado. Las suscripciones aparecerán en tu panel en unos segundos.");
    }
}