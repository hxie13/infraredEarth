package cn.ac.sitp.infrared.security;

public class AccountAuthenticationException extends RuntimeException {

    public AccountAuthenticationException(String message) {
        super(message);
    }
}
