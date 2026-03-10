package cn.ac.sitp.infrared.util;

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;

    public static String validate(String password, String username) {
        if (password == null || password.length() < MIN_LENGTH) {
            return "Password must be at least " + MIN_LENGTH + " characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return "Password must contain at least one special character";
        }
        if (username != null && password.equalsIgnoreCase(username)) {
            return "Password must not be the same as username";
        }
        return null;
    }
}
