package cn.ac.sitp.infrared.util;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Map;


public class HttpUtils {
    private static final HttpClient client = HttpClientBuilder.create().build();
    private final static String USER_AGENT = "Mozilla/5.0";
    private final static HostnameVerifier DO_NOT_VERIFY = (hostname, session) -> true;

    public static String sendHttpsGET(String url, Map<String, Object> params) {
        if (params != null) {
            boolean first = true;
            StringBuilder urlBuilder = new StringBuilder(url);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (first) {
                    urlBuilder.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                    first = false;
                } else {
                    urlBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            url = urlBuilder.toString();
        }

        System.out.println(url);

        StringBuilder result = new StringBuilder();
        PrintWriter out = null;
        BufferedReader in = null;
        HttpURLConnection conn;
        try {
            trustAllHosts();
            URL realUrl = new URI(url).toURL();
            // 通过请求地址判断请求类型(http或者是https)
            if (realUrl.getProtocol().equalsIgnoreCase("https")) {
                HttpsURLConnection https = (HttpsURLConnection) realUrl.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                conn = https;
            } else {
                conn = (HttpURLConnection) realUrl.openConnection();
            }
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "text/plain;charset=utf-8");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(false);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
//			out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            // out.print(a);
            // flush输出流的缓冲
//			out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {// 使用finally块来关闭输出流、输入流
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String sendHttpGetRequest(String url, Map<String, Object> params) {
        if (params != null) {
            boolean first = true;
            StringBuilder urlBuilder = new StringBuilder(url);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (first) {
                    urlBuilder.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                    first = false;
                } else {
                    urlBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            url = urlBuilder.toString();
        }
        HttpGet request = new HttpGet(url);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);
        ClassicHttpResponse response;
        try {
            response = (ClassicHttpResponse) client.execute(request);
            System.out.println("Response Code : " + response.getCode());

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sendRequest(String urlAddress, Map<String, Object> params) {
        if (params != null) {
            boolean first = true;
            StringBuilder sb = new StringBuilder();
            sb.append(urlAddress);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (first) {
                    //urlAddress+="?"+entry.getKey()+"="+entry.getValue();
                    sb.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                    first = false;
                } else {
                    //urlAddress+="&&"+entry.getKey()+"="+entry.getValue();
                    sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            urlAddress = sb.toString();
        }
        System.out.println(urlAddress);
        try {
            URL url = new URI(urlAddress).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            InputStream inStream = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    inStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    public static String sendHttpPost(String url, String jsonStr) throws Exception {

        trustAllHosts();
        URL realUrl = new URI(url).toURL();

        HttpURLConnection conn;
        if (realUrl.getProtocol().equalsIgnoreCase("https")) {
            HttpsURLConnection https = (HttpsURLConnection) realUrl.openConnection();
            https.setHostnameVerifier(DO_NOT_VERIFY);
            conn = https;
        } else {
            conn = (HttpURLConnection) realUrl.openConnection();
        }

        conn.setRequestMethod("POST");

        conn.setDoOutput(true);

        conn.setAllowUserInteraction(false);

        conn.setRequestProperty("Content-type", "application/json;charset=UTF-8");

        conn.setConnectTimeout(10000);
        conn.setReadTimeout(300000);

        PrintStream ps = new PrintStream(conn.getOutputStream());

        ps.print(jsonStr);

        ps.close();

        BufferedReader bReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line;
        StringBuilder resultStr = new StringBuilder();

        while (null != (line = bReader.readLine())) {
            resultStr.append(line);
        }

        bReader.close();

        return resultStr.toString();
    }

    public String sendRequest(String address, String soapBody)
            throws IOException, URISyntaxException {
        String response;
        URL url = new URI(address).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setConnectTimeout(20000);
        con.setReadTimeout(300000);
        con.setRequestProperty("Content-type", "text/xml;charset=UTF-8");
        con.setRequestProperty("Content-Length", String.valueOf(soapBody.length()));
        OutputStream reqStream = con.getOutputStream();
        reqStream.write(soapBody.getBytes());
        InputStream resStream = con.getInputStream();
        InputStreamReader in = new InputStreamReader(resStream);
        BufferedReader buff = new BufferedReader(in);
        String line;
        StringBuilder text = new StringBuilder();
        do {
            line = buff.readLine();
            if (line != null) {
                text.append(line);
                text.append("\n");
            }
        } while (line != null);
        response = text.toString();
        return response;
    }
}
