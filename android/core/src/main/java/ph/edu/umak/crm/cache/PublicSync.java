package ph.edu.umak.crm.cache;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;

/** Public data only. No manuscripts, reviews, passwords or tokens enter Room. */
public final class PublicSync extends Worker {
    public PublicSync(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    private static void require(boolean condition) {
        if (!condition) throw new IllegalArgumentException("Invalid public content");
    }

    private byte[] download(URL url, int maximum) throws Exception {
        require("https".equals(url.getProtocol()));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        try {
            require(connection.getResponseCode() == 200);
            require(connection.getContentLengthLong() <= maximum);
            try (InputStream stream = connection.getInputStream();
                    ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = stream.read(buffer)) != -1) {
                    require(output.size() + count <= maximum);
                    output.write(buffer, 0, count);
                }
                return output.toByteArray();
            }
        } finally {
            connection.disconnect();
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        String base = getInputData().getString("base_url");
        if (base == null) return Result.failure();
        PublicDatabase database = Room.databaseBuilder(
                getApplicationContext(), PublicDatabase.class, "crm-public.db").build();
        try {
            URL origin = new URL(base);
            require("https".equals(origin.getProtocol()) && origin.getUserInfo() == null);
            JSONObject body = new JSONObject(new String(download(
                    new URL(origin, "/api/v1/public/content"), 1_000_000), StandardCharsets.UTF_8));
            String version = body.getString("version");
            require(version.length() <= 100);
            JSONArray items = body.getJSONArray("items");
            require(items.length() <= 100);
            File directory = new File(getApplicationContext().getFilesDir(), "crm-public-templates");
            directory.mkdirs();
            List<PublicItem> rows = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            int totalBytes = 0;
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String id = item.getString("id");
                require(id.matches("[A-Za-z0-9_-]{1,64}"));
                String kind = item.getString("kind");
                require("directory".equals(kind) || "requirements".equals(kind)
                        || "instructions".equals(kind) || "template".equals(kind));
                String path = null;
                if ("template".equals(kind)) {
                    String relative = item.getString("url");
                    require(relative.matches("/api/v1/public/templates/[A-Za-z0-9_.-]+"));
                    URL url = new URL(origin, relative);
                    require(url.getHost().equals(origin.getHost()) && url.getPort() == origin.getPort());
                    String expected = item.getString("sha256");
                    require(expected.matches("[a-f0-9]{64}"));
                    byte[] bytes = download(url, 25_000_000);
                    totalBytes += bytes.length;
                    require(totalBytes <= 100_000_000);
                    byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
                    StringBuilder hash = new StringBuilder(64);
                    for (byte value : digest) {
                        hash.append(Character.forDigit((value & 0xff) >>> 4, 16));
                        hash.append(Character.forDigit(value & 0x0f, 16));
                    }
                    require(expected.contentEquals(hash));
                    File target = new File(directory, expected);
                    if (!target.exists()) {
                        File temporary = File.createTempFile("download-", ".tmp", directory);
                        try {
                            try (FileOutputStream output = new FileOutputStream(temporary)) {
                                output.write(bytes);
                            }
                            require(temporary.renameTo(target));
                        } finally {
                            temporary.delete();
                        }
                    }
                    path = target.getAbsolutePath();
                }
                rows.add(new PublicItem(id, kind, item.getString("title"), item.getString("text"),
                        version, System.currentTimeMillis(), path));
                require(ids.add(id));
            }
            // Only publish after all templates pass validation.
            database.content().replace(rows);
            return Result.success();
        } catch (Exception error) {
            return getRunAttemptCount() < 3 ? Result.retry() : Result.failure();
        } finally {
            database.close();
        }
    }

    public static void schedule(Context context, String baseUrl) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(PublicSync.class, 12, TimeUnit.HOURS)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("base_url", baseUrl).build()).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "crm-public-sync", ExistingPeriodicWorkPolicy.UPDATE, request);
    }
}
