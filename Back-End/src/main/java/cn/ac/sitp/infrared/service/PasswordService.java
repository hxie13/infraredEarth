package cn.ac.sitp.infrared.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for password hashing and verification.
 * Supports migration from MD5 to BCrypt.
 */
@Service
public class PasswordService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public PasswordService() {
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Hash a raw password using BCrypt.
     *
     * @param rawPassword the raw password
     * @return BCrypt hashed password
     */
    public String hashPassword(String rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }

    /**
     * Verify a password against a stored hash.
     * Supports both BCrypt and MD5 hashes for migration.
     *
     * @param rawPassword    the raw password to verify
     * @param storedHash     the stored password hash
     * @param isLegacyMd5    true if the stored hash is MD5
     * @return true if password matches
     */
    public boolean verifyPassword(String rawPassword, String storedHash, boolean isLegacyMd5) {
        if (isLegacyMd5) {
            // Legacy MD5 verification
            String md5Hash = DigestUtils.md5Hex(rawPassword);
            return md5Hash.equalsIgnoreCase(storedHash);
        }
        // BCrypt verification
        return bCryptPasswordEncoder.matches(rawPassword, storedHash);
    }

    /**
     * Check if a hash is BCrypt format.
     *
     * @param hash the password hash
     * @return true if it's a BCrypt hash
     */
    public boolean isBCryptHash(String hash) {
        return hash != null && hash.startsWith("$2a$");
    }
}
