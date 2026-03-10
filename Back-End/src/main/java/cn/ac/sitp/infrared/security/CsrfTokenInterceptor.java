package cn.ac.sitp.infrared.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

@Component
public class CsrfTokenInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CsrfTokenInterceptor.class);
    private static final String CSRF_SESSION_KEY = "infrared.csrfToken";
    private static final String CSRF_HEADER_NAME = "X-CSRF-Token";
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private static final Set<String> CSRF_EXEMPT_SUFFIXES = Set.of("/login", "/register");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod().toUpperCase();

        if (SAFE_METHODS.contains(method)) {
            ensureCsrfToken(request, response);
            return true;
        }

        // Check if this path is exempt from CSRF
        String uri = request.getRequestURI();
        for (String suffix : CSRF_EXEMPT_SUFFIXES) {
            if (uri.endsWith(suffix)) {
                return true;
            }
        }

        // Validate CSRF token for state-changing requests
        HttpSession session = request.getSession(false);
        String sessionToken = (session != null) ? (String) session.getAttribute(CSRF_SESSION_KEY) : null;
        String headerToken = request.getHeader(CSRF_HEADER_NAME);

        if (sessionToken == null || !sessionToken.equals(headerToken)) {
            log.warn("CSRF token mismatch for {} {}", method, request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token validation failed");
            return false;
        }

        return true;
    }

    private void ensureCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(CSRF_SESSION_KEY);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(CSRF_SESSION_KEY, token);
        }
        Cookie cookie = new Cookie(CSRF_COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
    }
}
