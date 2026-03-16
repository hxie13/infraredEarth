package cn.ac.sitp.infrared.util;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    public static final String FORMAT_LONG = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_SHORT = "yyyy-MM-dd";
    public static final String GENERIC_ERROR_MESSAGE = "Internal server error";

    private static final String RESPONSE_ERROR_CODE = "errorCode";
    private static final String RESPONSE_STATUS = "status";
    private static final String RESPONSE_RESULT_SUCCESS = "Success";
    private static final String RESPONSE_RESULT_FAILURE = "Failure";
    private static final String RESPONSE_RESULT_LOGGED_OUT = "Logged_out";

    private Util() {
    }

    public static Map<String, Object> noLogin() {
        Map<String, Object> contents = new HashMap<>();
        contents.put(RESPONSE_STATUS, RESPONSE_RESULT_LOGGED_OUT);
        return contents;
    }

    public static Map<String, Object> suc(Map<String, Object> contents) {
        if (contents == null) {
            contents = new HashMap<>();
        }
        contents.put(RESPONSE_STATUS, RESPONSE_RESULT_SUCCESS);
        return contents;
    }

    public static Map<String, Object> err(Map<String, Object> contents, String errorCode) {
        if (contents == null) {
            contents = new HashMap<>();
        }
        contents.put(RESPONSE_STATUS, RESPONSE_RESULT_FAILURE);
        contents.put(RESPONSE_ERROR_CODE, errorCode);
        return contents;
    }

    public static Date strToDate(String str, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public static String getUserIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X_Real_IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-forwarded-for");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    ip = inetAddress.getHostAddress();
                } catch (UnknownHostException e) {
                    log.error("Failed to resolve local host", e);
                }
            }
        }
        if (ip != null && ip.length() > 15 && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

    public static void buildPageModel(int pageSize, int totalAmount, Map<String, Object> contents) {
        contents.put("totalRecords", totalAmount);
        contents.put("pageCount", pageSize > 0 ? (totalAmount / pageSize + (totalAmount % pageSize > 0 ? 1 : 0)) : 1);
    }

    public static String escapeLikePattern(String input) {
        if (input == null) return null;
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
