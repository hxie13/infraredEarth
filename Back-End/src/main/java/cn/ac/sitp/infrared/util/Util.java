package cn.ac.sitp.infrared.util;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Util {

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    public static final String FORMAT_LONG = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_SHORT = "yyyy-MM-dd";
    public static final String FORMAT_ARRANGE = "yyyy-MM-dd HH:mm";
    private static final String RESPONSE_ERROR_CODE = "errorCode";
    private static final String RESPONSE_STATUS = "status";
    private static final String RESPONSE_RESULT_SUCCESS = "Success";
    private static final String RESPONSE_RESULT_FAILURE = "Failure";
    private static final String RESPONSE_RESULT_LOGGED_OUT = "Logged_out";
    private static ExecutorService fixedThreadPool;

    public static ExecutorService getThreadPool() {
        if (fixedThreadPool == null) {
            fixedThreadPool = Executors.newFixedThreadPool(10);
            return fixedThreadPool;
        } else {
            return fixedThreadPool;
        }
    }

    public static Map<String, Object> noLogin() {
        Map<String, Object> contents = new HashMap<>();
        contents.put(RESPONSE_STATUS, RESPONSE_RESULT_LOGGED_OUT);
        return contents;
    }

    public static String dateToStringLong(Date date, String pattern) {
        if (date != null) {
            return new SimpleDateFormat(pattern).format(date);
        }
        return null;
    }

    public static Date strToDate(String str, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static String generateArray(String[] list) {
        if (list == null || list.length < 1) {
            return "array['0']";
        }
        StringBuilder result = new StringBuilder("array[");
        for (String str : list) {
            result.append("'").append(str).append("',");
        }
        result = new StringBuilder(result.substring(0, result.length() - 1));
        result.append("]");
        return result.toString();
    }

    public static Boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static long toLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return 0L;
        }
    }

    public static int toInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

    public static Boolean isDate(String str) {
        if (str == null) {
            return false;
        } else if (str.length() == 10) {
            try {
                new SimpleDateFormat(FORMAT_SHORT).parse(str);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else if (str.length() == 19) {
            try {
                new SimpleDateFormat(FORMAT_LONG).parse(str);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 将Unicode编码转换为中文字符串
     * @param str Unicode编码
     */
    public static String unicodeToCN(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }

    public static Map<String, Object> suc(Map<String, Object> contents) {
        if (contents == null) {
            contents = new HashMap<String, Object>();
        }
        contents.put(RESPONSE_STATUS, RESPONSE_RESULT_SUCCESS);
        return contents;
    }

    public static Map<String, Object> err(Map<String, Object> contents, String errorCode) {
        if (contents == null) {
            contents = new HashMap<String, Object>();
        }
        contents.put(RESPONSE_STATUS, RESPONSE_RESULT_FAILURE);
        contents.put(RESPONSE_ERROR_CODE, errorCode);
        return contents;
    }

    public static String base64encode(String str) {
        if (str == null) {
            return "";
        } else {
            try {
                return new String(Base64.encodeBase64(str.getBytes("utf-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new String(Base64.encodeBase64(str.getBytes()));
            }
        }
    }

    public static String base64decode(String str) {
        if (str == null) {
            return "";
        } else {
            try {
                return new String(Base64.decodeBase64(str.getBytes()), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new String(Base64.decodeBase64(str.getBytes()));
            }
        }
    }

    public static String UIDGenerator(String hospitalid, Date report_date, long order_no) {
        return "1.2.840." + hospitalid + "." + dateToStringLong(report_date, "yyyy.MM.dd.HH.mm.ss") + "."
                + System.currentTimeMillis() + "." + order_no;
    }

    public static String getLocalIP() {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            return ia.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
            // 从本地访问时根据网卡取本机配置的IP
            if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ip = inetAddress.getHostAddress();
            }
        }
        // 通过多个代理转发的情况，第一个IP为客户端真实IP，多个IP会按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }

    public static File transferToFile(MultipartFile multipartFile) {
//        选择用缓冲区来实现这个转换即使用java 创建的临时文件 使用 MultipartFile.transferto()方法 。
        File file = null;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            file = File.createTempFile(originalFilename.substring(0, originalFilename.lastIndexOf(".")),
                    originalFilename.substring(originalFilename.lastIndexOf(".")));
            multipartFile.transferTo(file);
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void buildPageModel(int pageSize, int totalAmount, Map<String, Object> contents) {
        contents.put("totalRecords", totalAmount);
        contents.put("pageCount", pageSize > 0 ? (totalAmount / pageSize + (totalAmount % pageSize > 0 ? 1 : 0)) : 1);
    }

    public static List<Long> StringToLongList(String strings) {
        List<Long> longList = new ArrayList<>();
        if (strings != null && !strings.isEmpty()) {
            String[] stringArr = strings.split(",");
            for (String s : stringArr) {
                Long parseLong = Long.parseLong(s);
                longList.add(parseLong);
            }
        }
        return longList;
    }

    public static String[] generateNumberArray(int number) {
        if (number == 0) number = 1;
        String[] numberArray = new String[number];
        for (int i = 0; i < number; i++) {
            numberArray[i] = String.valueOf(i + 1);
        }
        return numberArray;
    }

//    public static String toPinyin(String chinese) throws BadHanyuPinyinOutputFormatCombination {
//        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
//        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//
//        StringBuilder pinyinBuilder = new StringBuilder();
//        char[] chars = chinese.toCharArray();
//        for (char c : chars) {
//            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
//            if (pinyinArray != null) {
//                pinyinBuilder.append(pinyinArray[0]);
//            } else {
//                pinyinBuilder.append(c);
//            }
//        }
//        return pinyinBuilder.toString();
//    }

    public static boolean genderEqual(String risGender, String dicomGender) {
        return (Arrays.asList("男", "M").contains(risGender)) && (Arrays.asList("男", "M").contains(dicomGender))
                || (Arrays.asList("女", "F").contains(risGender)) && (Arrays.asList("女", "F").contains(dicomGender))
                || (Arrays.asList("未知", "O").contains(risGender)) && (Arrays.asList("未知", "O").contains(dicomGender));
    }

    public static void main(String[] args) {
        String month = "2023-02";
        String startDate = Util.dateToStringLong(DateUtil.addMonths(Util.strToDate(month + "-01", Util.FORMAT_SHORT), -3), Util.FORMAT_SHORT);
        System.out.println(startDate);
    }


}
