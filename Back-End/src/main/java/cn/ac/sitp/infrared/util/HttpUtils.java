package cn.ac.sitp.infrared.util;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;


public class HttpUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
    private static final HttpClient client = HttpClientBuilder.create().build();
    private static final String USER_AGENT = "Mozilla/5.0";

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

        log.debug("Sending HTTPS GET: {}", url);

        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            URL realUrl = new URI(url).toURL();
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", USER_AGENT);
            conn.setRequestProperty("Content-Type", "text/plain;charset=utf-8");
            conn.setDoOutput(false);
            conn.setDoInput(true);
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            log.error("Failed to send HTTPS GET request to {}", url, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                log.error("Failed to close input stream", ex);
            }
        }
        return result.toString();
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

        request.addHeader("User-Agent", USER_AGENT);
        ClassicHttpResponse response;
        try {
            response = (ClassicHttpResponse) client.execute(request);
            log.debug("Response Code: {}", response.getCode());

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            log.error("Failed to send HTTP GET request to {}", url, e);
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
                    sb.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                    first = false;
                } else {
                    sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            urlAddress = sb.toString();
        }
        log.debug("Sending request: {}", urlAddress);
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
            log.error("Failed to send request to {}", urlAddress, e);
        }
        return "ERROR";
    }

    public static String sendHttpPost(String url, String jsonStr) throws Exception {
        URL realUrl = new URI(url).toURL();
        HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();

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
