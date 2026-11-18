package ph.edu.umak.crm.cache;

import android.content.Context;
import android.content.SharedPreferences;
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
    private static final Object CACHE_LOCK = new Object();
    private static final String PERIODIC_WORK = "crm-public-sync";

    public PublicSync(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    private static void require(boolean condition) {
        if (!condition) throw new IllegalArgumentException("Invalid public content");
    }

    private static SharedPreferences validators(Context context) {
        return context.getSharedPreferences("crm-public-validators", Context.MODE_PRIVATE);
    }

    private static File templateDirectory(Context context) {
        return new File(context.getFilesDir(), "crm-public-templates");
    }

    private byte[] download(URL url, int maximum, String etag, String[] receivedEtag) throws Exception {
        require("https".equals(url.getProtocol()));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        try {
            if (etag != null) connection.setRequestProperty("If-None-Match", etag);
            int status = connection.getResponseCode();
            if (status == 304 && etag != null) return null;
            require(status == 200);
            if (receivedEtag != null) receivedEtag[0] = connection.getHeaderField("ETag");
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
        synchronized (CACHE_LOCK) {
            return refreshCache();
        }
    }

    private Result refreshCache() {
        String base = getInputData().getString("base_url");
        if (base == null) return Result.failure();
        PublicDatabase database = Room.databaseBuilder(
                getApplicationContext(), PublicDatabase.class, "crm-public.db").build();
        try {
            URL origin = new URL(base);
            require("https".equals(origin.getProtocol()) && origin.getUserInfo() == null);
            SharedPreferences validators = validators(getApplicationContext());
            List<PublicItem> cached = database.content().list();
            boolean complete = !cached.isEmpty() && cached.stream().allMatch(item ->
                    item.templatePath == null || new File(item.templatePath).isFile());
            String etag = complete ? validators.getString(base, null) : null;
            String[] receivedEtag = new String[1];
            byte[] manifest = download(new URL(origin, "/api/v1/public/content"), 1_000_000, etag, receivedEtag);
            if (manifest == null) {
                database.content().markSynced(System.currentTimeMillis());
                return Result.success();
            }
            JSONObject body = new JSONObject(new String(manifest, StandardCharsets.UTF_8));
            String version = body.getString("version");
            require(version.length() <= 100);
            JSONArray items = body.getJSONArray("items");
            require(items.length() <= 100);
            File directory = templateDirectory(getApplicationContext());
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
                    File target = new File(directory, expected);
                    if (!target.isFile()) {
                        byte[] bytes = download(url, 25_000_000, null, null);
                        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
                        StringBuilder hash = new StringBuilder(64);
                        for (byte value : digest) {
                            hash.append(Character.forDigit((value & 0xff) >>> 4, 16));
                            hash.append(Character.forDigit(value & 0x0f, 16));
                        }
                        require(expected.contentEquals(hash));
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
                    require(target.length() <= 25_000_000);
                    totalBytes += (int) target.length();
                    require(totalBytes <= 100_000_000);
                    path = target.getAbsolutePath();
                }
                rows.add(new PublicItem(id, kind, item.getString("title"), item.getString("text"),
                        version, System.currentTimeMillis(), path));
                require(ids.add(id));
            }
            // Only publish after all templates pass validation.
            if (isStopped()) return Result.failure();
            // Invalidate before replacing the snapshot so an interrupted update cannot pair an old tag with new rows.
            require(validators.edit().clear().commit());
            database.content().replace(rows);
            if (receivedEtag[0] != null) validators.edit().putString(base, receivedEtag[0]).apply();
            return Result.success();
        } catch (Exception error) {
            return getRunAttemptCount() < 3 ? Result.retry() : Result.failure();
        } finally {
            database.close();
        }
    }

    public static boolean clearCache(Context context) {
        synchronized (CACHE_LOCK) {
            PublicDatabase database = null;
            try {
                database = Room.databaseBuilder(context.getApplicationContext(),
                        PublicDatabase.class, "crm-public.db").build();
                validators(context).edit().clear().apply();
                database.content().clear();
                File[] templates = templateDirectory(context).listFiles();
                if (templates == null) return true;
                boolean cleared = true;
                for (File template : templates) {
                    if (!template.delete()) cleared = false;
                }
                return cleared;
            } catch (RuntimeException failure) {
                return false;
            } finally {
                if (database != null) database.close();
            }
        }
    }

    public static void cancelSchedule(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK);
    }

    public static void schedule(Context context, String baseUrl) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(PublicSync.class, 12, TimeUnit.HOURS)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build())
                .setInputData(new Data.Builder().putString("base_url", baseUrl).build()).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK, ExistingPeriodicWorkPolicy.UPDATE, request);
    }
}
