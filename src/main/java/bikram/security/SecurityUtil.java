package bikram.security;


import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.security.SecureRandom;
import java.util.UUID;

public class SecurityUtil {

    // 🔹 Secure UUID generator
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    // 🔹 Hash password using Argon2id (recommended)
    public static String hashPassword(String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
            int iterations = 3;   // time cost
            int memory = 65536;   // 64 MB
            int parallelism = 1;  // threads
            return argon2.hash(iterations, memory, parallelism, password.toCharArray());
        } finally {
            argon2.wipeArray(password.toCharArray()); // clear sensitive data
        }
    }

    // 🔹 Verify password safely (constant-time comparison)
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
            return argon2.verify(hashedPassword, rawPassword.toCharArray());
        } finally {
            argon2.wipeArray(rawPassword.toCharArray());
        }
    }

    // 🔹 Generate secure random password (optional)
    public static String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#&$!";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }
}
