package cn.ac.sitp.infrared.web;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.security.AccountAuthenticationException;
import cn.ac.sitp.infrared.security.SessionAccountHelper;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.util.Util;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static final String ATTR_AUDIT_ACTION = "audit.action";
    public static final String ATTR_AUDIT_DESCRIPTION = "audit.description";

    @Autowired
    private AuditLogService logService;

    @Autowired
    private HttpServletRequest request;

    @ExceptionHandler(AccountAuthenticationException.class)
    public Map<String, Object> handleAuthenticationException(AccountAuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        saveFailureAuditLog(e.getMessage());
        return Util.err(null, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid request parameters", e);
        saveFailureAuditLog(e.getMessage());
        return Util.err(null, e.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFileNotFound(FileNotFoundException e) {
        log.warn("File not found: {}", e.getMessage());
        saveFailureAuditLog(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Util.err(null, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleGenericException(Exception e) {
        String stackTrace = Util.getStackTrace(e);
        log.error("Unexpected error", e);
        saveFailureAuditLog(stackTrace);
        return Util.err(null, Util.GENERIC_ERROR_MESSAGE);
    }

    private void saveFailureAuditLog(String exception) {
        LogActionEnum action = (LogActionEnum) request.getAttribute(ATTR_AUDIT_ACTION);
        if (action == null) {
            return;
        }
        String description = (String) request.getAttribute(ATTR_AUDIT_DESCRIPTION);
        String ip = Util.getUserIpAddr(request);
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        logService.saveAuditLog(ip, action, Util.STATUS_FAILURE, new Date(), exception, description, user);
    }

    /**
     * Set audit context on the request so the exception handler can log failures.
     * Call this at the beginning of controller methods.
     */
    public static void setAuditContext(HttpServletRequest request, LogActionEnum action, String description) {
        request.setAttribute(ATTR_AUDIT_ACTION, action);
        request.setAttribute(ATTR_AUDIT_DESCRIPTION, description);
    }
}
