package com.fluxaj;

import com.fluxaj.repository.UsuarioRepository;
import com.fluxaj.service.GmailScannerService; // <-- Asegúrate de que la importación sea correcta
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FluxajApplication {

    public static void main(String[] args) {
        SpringApplication.run(FluxajApplication.class, args);
    }

    @Bean
    CommandLineRunner testearEscaneo(UsuarioRepository userRepo, GmailScannerService scannerService) {
        return args -> {
            System.out.println("\n=========================================================");
            System.out.println("   🧪 INICIANDO TEST FORZADO DE ESCANEO DE GMAIL 🧪   ");
            System.out.println("=========================================================");

            // Buscamos el primer usuario que haya iniciado sesión en tu DB
            userRepo.findAll().stream().findFirst().ifPresentOrElse(usuario -> {
                
                System.out.println("👤 Usuario encontrado para la prueba: " + usuario.getEmail());
                System.out.println("⚙️ Llamando al servicio de escaneo...");

                // ¡AQUÍ ESTÁ LA MAGIA! Llamamos al método que arreglamos
                try {
                    Integer correosEncontrados = scannerService.escanearCorreos(usuario.getId());
                    System.out.println("🏁 Test finalizado con éxito. Se guardaron: " + correosEncontrados + " suscripciones.");
                } catch (Exception e) {
                    System.err.println("❌ El test falló con una excepción: " + e.getMessage());
                }

            }, () -> {
                System.out.println("⚠️ ERROR: No hay usuarios en la base de datos.");
                System.out.println("💡 Solución: Ve a tu navegador, inicia sesión con Google en tu app y vuelve a reiniciar este servidor.");
            });

            System.out.println("=========================================================\n");
        };
    }
}