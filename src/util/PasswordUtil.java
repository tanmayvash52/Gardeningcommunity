
package util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    private static final String PREFIX = "PBKDF2";
    private static final int ITERATIONS = 210000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hashPassword(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        byte[] hash = deriveKey(password.toCharArray(), salt, ITERATIONS);

        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean isHashed(String storedPassword) {
        return storedPassword != null
                && storedPassword.startsWith(PREFIX + "$");
    }

    public static boolean verifyPassword(
            String password, String storedPassword) {

        try {
            String[] parts = storedPassword.split("\\$", -1);

            if (parts.length != 4 || !parts[0].equals(PREFIX)) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);

            if (iterations < 1 || iterations > 1000000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            if (salt.length < 16 || expectedHash.length != KEY_LENGTH / 8) {
                return false;
            }

            byte[] actualHash = deriveKey(
                    password.toCharArray(), salt, iterations);

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] deriveKey(
            char[] password, byte[] salt, int iterations) {

        PBEKeySpec spec = new PBEKeySpec(
                password, salt, iterations, KEY_LENGTH);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Password hashing is unavailable.", e);

        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(password, '\0');
        }
    }
}