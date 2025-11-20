package bikram.encryption;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class AESEncryptor {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int SALT_LENGTH = 16;

    private final SecretKey secretKey;

    public AESEncryptor(char[] masterPassword) throws Exception {
        this.secretKey = deriveKey(masterPassword);
    }

    private SecretKey deriveKey(char[] password) throws Exception {
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        PBEKeySpec spec = new PBEKeySpec(password, salt, 65536, KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) throws Exception {
        if (plaintext == null) return null;

        byte[] iv = generateRandomBytes(IV_LENGTH);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null) return null;

        byte[] combined = Base64.getDecoder().decode(encryptedText);
        byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}