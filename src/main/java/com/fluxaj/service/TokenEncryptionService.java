package com.fluxaj.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TokenEncryptionService {

    // Clave de 32 bytes = 256 bits

    @Value("${config.security.secret-key}")
    private String secretKeyBase64;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;   // 96 bits recomendado para GCM
    private static final int GCM_TAG_LENGTH = 128;  // bits de autenticación

    // Cifrar: base64(IV + token cifrado)
    public String encrypt(String plainToken) {
        try {
            SecretKey key = getSecretKey();

            // IV aleatorio nuevo en cada cifrado (nunca reutilizar)
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] encrypted = cipher.doFinal(plainToken.getBytes("UTF-8"));

            // Guardamos IV + cifrado juntos para poder descifrar después
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar token", e);
        }
    }

    // Descifrar: base64(IV + cifrado) 
    public String decrypt(String encryptedToken) {
        try {
            SecretKey key = getSecretKey();

            byte[] combined = Base64.getDecoder().decode(encryptedToken);

            // Separar IV y datos cifrados
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            return new String(cipher.doFinal(encrypted), "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar token", e);
        }
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
        return new SecretKeySpec(keyBytes, "AES");
    }
}