package cn.ac.sitp.infrared.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * AES Encryption utility for login credentials.
 * Uses AES/CBC/PKCS5Padding with externalized key.
 */
@Component
public class AESLoginUtil {

    // Default key for backward compatibility (should be overridden via configuration)
    private static String DEFAULT_KEY = "SITP0123456789AB";
    private static final String IV_STRING = "A-16-Byte-String";
    private static final String CHARSET = "UTF-8";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    // Externalized key from configuration
    private static String externalKey = null;

    /**
     * Set the AES key from configuration.
     * This should be called during application startup.
     *
     * @param key the AES key (must be 16, 24, or 32 bytes)
     */
    @Value("${app.security.aes.key:}")
    public void setExternalKey(String key) {
        if (key != null && !key.trim().isEmpty()) {
            externalKey = key;
        }
    }

    /**
     * Get the current AES key.
     * Returns external key if set, otherwise returns default key.
     */
    private static String getKey() {
        return externalKey != null ? externalKey : DEFAULT_KEY;
    }

    /**
     * Encrypt a string using AES/CBC/PKCS5Padding.
     *
     * @param content the content to encrypt
     * @param key     the encryption key (if null, uses configured key)
     * @return Base64 encoded encrypted string
     */
    public static String aesEncryptString(String content, String key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        String actualKey = (key != null) ? key : getKey();
        byte[] contentBytes = content.getBytes(CHARSET);
        byte[] keyBytes = actualKey.getBytes(CHARSET);
        byte[] encryptedBytes = aesEncryptBytes(contentBytes, keyBytes);
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(encryptedBytes);
    }

    /**
     * Decrypt a string using AES/CBC/PKCS5Padding.
     *
     * @param content the Base64 encoded encrypted content
     * @param key     the decryption key (if null, uses configured key)
     * @return decrypted string
     */
    public static String aesDecryptString(String content, String key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        String actualKey = (key != null) ? key : getKey();
        Decoder decoder = Base64.getDecoder();
        byte[] encryptedBytes = decoder.decode(content);
        byte[] keyBytes = actualKey.getBytes(CHARSET);
        byte[] decryptedBytes = aesDecryptBytes(encryptedBytes, keyBytes);
        return new String(decryptedBytes, CHARSET);
    }

    public static byte[] aesEncryptBytes(byte[] contentBytes, byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return cipherOperation(contentBytes, keyBytes, Cipher.ENCRYPT_MODE);
    }

    public static byte[] aesDecryptBytes(byte[] contentBytes, byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return cipherOperation(contentBytes, keyBytes, Cipher.DECRYPT_MODE);
    }

    private static byte[] cipherOperation(byte[] contentBytes, byte[] keyBytes, int mode) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

        byte[] initParam = IV_STRING.getBytes(CHARSET);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);

        // FIXED: Changed from ECB to CBC mode with IV
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode, secretKey, ivParameterSpec);

        return cipher.doFinal(contentBytes);
    }

    /**
     * Generate a new random AES key.
     * Use this to generate a secure key for production.
     *
     * @param keyLength key length in bits (128, 192, or 256)
     * @return Base64 encoded key
     */
    public static String generateKey(int keyLength) {
        int bytes = keyLength / 8;
        byte[] keyBytes = new byte[bytes];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    // Legacy key constant for backward compatibility during migration
    // This should be used only for decrypting old data
    public static final String KEY = DEFAULT_KEY;

    public static void main(String[] args) {
        try {
            // Test encryption/decryption
            String testData = "testPassword123";
            String encrypted = aesEncryptString(testData, null);
            System.out.println("Encrypted: " + encrypted);
            String decrypted = aesDecryptString(encrypted, null);
            System.out.println("Decrypted: " + decrypted);
            System.out.println("Match: " + testData.equals(decrypted));
            
            // Generate a new key example
            System.out.println("\nExample of generating a new 128-bit key: ");
            System.out.println(generateKey(128));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
