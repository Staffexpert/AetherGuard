package com.aetherguard.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/**
 * 🛡️ AetherGuard Encryption Manager
 * 
 * Encrypts sensitive data and check results
 * Prevents tampering with detection data
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class EncryptionManager {
    
    private SecretKey secretKey;
    private final String ALGORITHM = "AES";
    
    public EncryptionManager() {
        initializeKey();
    }
    
    private void initializeKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256);
            this.secretKey = keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption", e);
        }
    }
    
    /**
     * Encrypt data
     */
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            return data;
        }
    }
    
    /**
     * Decrypt data
     */
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData);
        } catch (Exception e) {
            return encryptedData;
        }
    }
    
    /**
     * Hash sensitive data
     */
    public String hash(String data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return data;
        }
    }
}
