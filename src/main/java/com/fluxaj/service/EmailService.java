package com.fluxaj.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoNuevaSuscripcion(String destinatario, String proveedor, Double monto, String fechaCobro) {
        try {
            // MimeMessage para poder utizar HTML y CSS
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setTo(destinatario);
            helper.setSubject("Fluxaj | Nueva Suscripción Registrada: " + proveedor);

            // Plantilla HTML del correo
            String htmlBody = ""
                + "<div style='font-family: \"Segoe UI\", Helvetica, Arial, sans-serif; background-color: #1a1a1a; color: #ffffff; padding: 40px 20px; text-align: center;'>"
                + "  <div style='max-width: 500px; margin: 0 auto; background-color: #2b2b2b; padding: 30px; border-radius: 8px; border-top: 4px solid #3498db; box-shadow: 0 4px 10px rgba(0,0,0,0.3); text-align: left;'>"
                + "    <h2 style='color: #3498db; margin-top: 0; font-weight: 300; letter-spacing: 1px;'>❖ FLUXAJ</h2>"
                + "    <h3 style='color: #ecf0f1; border-bottom: 1px solid #444; padding-bottom: 10px;'>Confirmación de Registro</h3>"
                + "    <p style='color: #bdc3c7; line-height: 1.6;'>Se ha vinculado un nuevo pago recurrente a tu cuenta. Aquí tienes los detalles procesados por el sistema:</p>"
                + "    <table style='width: 100%; margin-top: 20px; border-collapse: collapse;'>"
                + "      <tr><td style='padding: 10px; border-bottom: 1px solid #444; color: #95a5a6;'>Proveedor</td><td style='padding: 10px; border-bottom: 1px solid #444; color: #fff; font-weight: bold; text-align: right;'>" + proveedor + "</td></tr>"
                + "      <tr><td style='padding: 10px; border-bottom: 1px solid #444; color: #95a5a6;'>Monto Asignado</td><td style='padding: 10px; border-bottom: 1px solid #444; color: #fff; font-weight: bold; text-align: right;'>$" + monto + "</td></tr>"
                + "      <tr><td style='padding: 10px; border-bottom: 1px solid #444; color: #95a5a6;'>Próximo Cobro</td><td style='padding: 10px; border-bottom: 1px solid #444; color: #fff; font-weight: bold; text-align: right;'>" + fechaCobro + "</td></tr>"
                + "    </table>"
                + "    <p style='margin-top: 30px; font-size: 12px; color: #7f8c8d; text-align: center;'>Este es un mensaje transaccional automático. No respondas a este correo.</p>"
                + "  </div>"
                + "</div>";

            helper.setText(htmlBody, true);

            mailSender.send(mensaje);
            System.out.println("Correo HTML enviado exitosamente a: " + destinatario);
        } catch (Exception e) {
            System.err.println("Error enviando el correo HTML: " + e.getMessage());
        }
    }
}