package ph.edu.umak.crm.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

final class CrmApi {
    private static final int MAX_RESPONSE_BYTES = 512 * 1024;
    private CrmApi() {}

    static final class HttpError extends IOException {
        final int status;

        HttpError(int status) {
            super("HTTP " + status);
            this.status = status;
        }
    }

    static JSONObject post(String origin, String path, JSONObject body, String token) throws Exception {
        return new JSONObject(request(origin, path, body, token));
    }

    static String search(String origin, String keyword) throws Exception {
        JSONObject query = new JSONObject().put("keyword", keyword).put("limit", 15);
        return request(origin, "/api/v1/research/search", query, null);
    }

    static String get(String origin, String path, String token) throws IOException {
        return request(origin, path, null, token);
    }

    private static String request(String origin, String path, JSONObject body, String token)
            throws IOException {
        URL url = new URL(origin + path);
        if (!"https".equals(url.getProtocol())) throw new IOException("HTTPS required");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        if (token != null) connection.setRequestProperty("Authorization", "Bearer " + token);
        try {
            if (body != null) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) throw new HttpError(status);
            if (status == 204) return "{}";
            try (InputStream input = connection.getInputStream();
                    ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (output.size() + count > MAX_RESPONSE_BYTES) throw new IOException("Response too large");
                    output.write(buffer, 0, count);
                }
                return new String(output.toByteArray(), StandardCharsets.UTF_8);
            }
        } finally {
            connection.disconnect();
        }
    }
}
