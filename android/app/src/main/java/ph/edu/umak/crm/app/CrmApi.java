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
        final String code;
        final String detail;
        JSONObject fieldErrors = new JSONObject();

        HttpError(int status, String code) {
            this(status, code, "");
        }

        HttpError(int status, String code, String detail) {
            super("HTTP " + status);
            this.status = status;
            this.code = code;
            this.detail = detail;
        }
    }

    static JSONObject post(String origin, String path, JSONObject body, String token) throws Exception {
        return new JSONObject(request(origin, path, body, token));
    }

    static String send(String origin, String path, JSONObject body, String token) throws IOException {
        return request(origin, path, body, token);
    }

    static String search(String origin, String keyword) throws Exception {
        JSONObject query = new JSONObject().put("keyword", keyword).put("limit", 15);
        return request(origin, "/api/v1/research/search", query, null);
    }

    static String get(String origin, String path, String token) throws IOException {
        return request(origin, path, null, token);
    }

    static void downloadForm(String origin, String path, String token, String mime, java.io.File destination)
            throws IOException {
        URL url = new URL(origin + path);
        if (!"https".equals(url.getProtocol())) throw new IOException("HTTPS required");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        if (token != null) connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        try {
            int status = connection.getResponseCode();
            if (status != 200) throw new HttpError(status, "");
            String type = connection.getContentType();
            if (type == null || !mime.equalsIgnoreCase(type.split(";", 2)[0].trim())) {
                throw new IOException("Unexpected form type");
            }
            try (InputStream input = connection.getInputStream();
                    OutputStream output = new java.io.FileOutputStream(destination)) {
                byte[] buffer = new byte[8192];
                int total = 0;
                int count;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > 10_000_000) throw new IOException("Form is too large");
                    output.write(buffer, 0, count);
                }
                if (total == 0) throw new IOException("Empty form");
            }
        } finally {
            connection.disconnect();
        }
    }

    static void uploadForm(String origin, int revision, String token, String filename, byte[] bytes)
            throws IOException {
        URL url = new URL(origin + "/api/v1/staff/services/" + revision + "/form");
        if (!"https".equals(url.getProtocol())) throw new IOException("HTTPS required");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(bytes.length);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setRequestProperty("X-Form-Name", filename);
        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bytes);
            }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) throw new HttpError(status, "");
        } finally {
            connection.disconnect();
        }
    }

    static String uploadRequestFile(String origin, String token, String filename, int requirement,
            String attempt, byte[] bytes) throws IOException {
        String path = "/api/v1/service-requests/files?filename="
                + java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8.name())
                + "&requirement=" + requirement + "&attempt_token=" + attempt;
        return uploadBinary(origin, path, token, bytes);
    }

    static String uploadBinary(String origin, String path, String token, byte[] bytes) throws IOException {
        URL url = new URL(origin + path);
        if (!"https".equals(url.getProtocol())) throw new IOException("HTTPS required");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(bytes.length);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bytes);
            }
            if (connection.getResponseCode() != 201) throw new HttpError(connection.getResponseCode(), "");
            try (InputStream input = connection.getInputStream()) {
                ByteArrayOutputStream response = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (response.size() + count > 4096) throw new IOException("Invalid upload response");
                    response.write(buffer, 0, count);
                }
                return response.toString(StandardCharsets.UTF_8.name());
            }
        } finally {
            connection.disconnect();
        }
    }

    private static String request(String origin, String path, JSONObject body, String token)
            throws IOException {
        URL url = new URL(origin + path);
        if (!"https".equals(url.getProtocol())) throw new IOException("HTTPS required");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(60000);
        connection.setReadTimeout(60000);
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
            if (status < 200 || status >= 300) {
                String code = "";
                String detail = "";
                JSONObject fields = new JSONObject();
                try (InputStream error = connection.getErrorStream()) {
                    if (error != null) {
                        ByteArrayOutputStream message = new ByteArrayOutputStream();
                        byte[] bytes = new byte[1024];
                        int count;
                        while (message.size() < 4096 && (count = error.read(bytes)) != -1) {
                            message.write(bytes, 0, count);
                        }
                        JSONObject failure = new JSONObject(message.toString(StandardCharsets.UTF_8.name()));
                        code = failure.optString("code");
                        detail = failure.optString("error");
                        if (failure.optJSONObject("field_errors") != null) fields = failure.getJSONObject("field_errors");
                    }
                } catch (IOException | org.json.JSONException ignored) {
                    // Keep the HTTP status even if the server does not return a structured error.
                }
                HttpError rejected = new HttpError(status, code, detail);
                rejected.fieldErrors = fields;
                throw rejected;
            }
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
