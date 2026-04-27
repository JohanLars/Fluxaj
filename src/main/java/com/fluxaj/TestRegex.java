package com.fluxaj;

import java.util.regex.Matcher;
import com.fluxaj.service.SubscriptionRegexPatterns;

public class TestRegex {
    public static void main(String[] args) {
        // Simulación de asuntos de correos reales
        String[] pruebas = {
            "Tu factura de Netflix: $39.900",
            "Receipt from Apple Services - USD 9.99",
            "Confirmación de suscripción a Disney+",
            "Your Spotify Premium receipt: $15,50",
            "Factura de Microsoft 365"
        };

        System.out.println("=== PROBANDO DETECCIÓN DE RECIBOS Y PROVEEDORES ===\n");

        for (String texto : pruebas) {
            boolean esRecibo = SubscriptionRegexPatterns.ES_RECIBO.matcher(texto).find();
            
            Matcher mProveedor = SubscriptionRegexPatterns.PROVEEDOR.matcher(texto);
            String proveedor = mProveedor.find() ? mProveedor.group(0) : "No encontrado";

            Matcher mMonto = SubscriptionRegexPatterns.MONTO.matcher(texto);
            String monto = mMonto.find() ? mMonto.group(1) : "0.00";

            System.out.println("Texto: " + texto);
            System.out.println("  > ¿Es recibo?: " + (esRecibo ? "✅ SÍ" : "❌ NO"));
            System.out.println("  > Proveedor: " + proveedor);
            System.out.println("  > Monto detectado: " + monto);
            System.out.println("------------------------------------------------");
        }
    }
}