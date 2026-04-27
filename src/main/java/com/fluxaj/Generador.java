package com.fluxaj;

import java.security.SecureRandom;
import java.util.Base64;

public class Generador {
    public static void main(String[] args) {
        // Creamos un array de 32 bytes (256 bits)
        byte[] key = new byte[32]; 
        
        // Llenamos el array con bytes aleatorios seguros
        new SecureRandom().nextBytes(key);
        
        // Convertimos a Base64 para que sea legible
        String llaveBase64 = Base64.getEncoder().encodeToString(key);
        
        System.out.println("\n--- TU LLAVE SECRETA GENERADA ---");
        System.out.println(llaveBase64);
        System.out.println("---------------------------------\n");
    }
}