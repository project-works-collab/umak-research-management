package ph.edu.umak.crm.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;
import ph.edu.umak.crm.cache.PublicDatabase;
import ph.edu.umak.crm.cache.PublicItem;
import ph.edu.umak.crm.cache.PublicSync;
import ph.edu.umak.crm.security.SessionVault;

/** Launchable integration for sign-in, Keystore sessions, and public offline resources. */
public final class MainActivity extends Activity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinearLayout layout;
    private TextView status;
    private LinearLayout resources;
    private EditText server;
    private EditText email;
    private EditText password;
    private LiveData<List<WorkInfo>> refreshData;
    private Observer<List<WorkInfo>> refreshObserver;
    private SessionVault vault;
    private SharedPreferences preferences;

    private void message(String text) {
        runOnUiThread(() -> {
            if (!isDestroyed()) status.setText(text);
        });
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        vault = new SessionVault(this);
        preferences = getSharedPreferences("crm-settings", MODE_PRIVATE);
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);
        setContentView(scroll);
        TextView title = new TextView(this);
        title.setText("UMak Research Management");
        title.setTextSize(24f);
        layout.addView(title);
        server = field("CRM server (https://…)");
        server.setText(preferences.getString("server", ""));
        email = field("Email");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        password = field("Password");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setSaveEnabled(false);
        status = new TextView(this);
        layout.addView(status);
        button("Sign in", this::signIn);
        button("Sign out", this::signOut);
        button("Refresh public resources", this::refresh);
        resources = new LinearLayout(this);
        resources.setOrientation(LinearLayout.VERTICAL);
        layout.addView(resources);
        loadCache();
        executor.execute(() -> {
            try {
                JSONObject saved = session();
                message(saved == null
                        ? "Sign in to access your account. Public resources remain available offline."
                        : "Saved session available; expires " + new Date(saved.getLong("expires_at")) + ".");
            } catch (Exception error) {
                message("Saved session unavailable. Sign in again.");
            }
        });
        String origin = preferences.getString("server", "");
        if (!origin.isEmpty()) PublicSync.schedule(this, origin);
    }

    private EditText field(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        layout.addView(input);
        return input;
    }

    private void button(String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setOnClickListener(view -> action.run());
        layout.addView(button);
    }

    private String origin() throws Exception {
        URL url = new URL(server.getText().toString().trim());
        if (!"https".equals(url.getProtocol()) || url.getUserInfo() != null
                || url.getQuery() != null || url.getRef() != null
                || !(url.getPath().isEmpty() || "/".equals(url.getPath()))) {
            throw new IllegalArgumentException("Invalid HTTPS origin");
        }
        return url.toExternalForm().replaceAll("/+$", "");
    }

    private JSONObject session() throws Exception {
        try {
            String secret = vault.read();
            JSONObject saved = new JSONObject(secret == null ? "{}" : secret);
            if (saved.getLong("expires_at") <= System.currentTimeMillis()) {
                vault.clear();
                return null;
            }
            return saved;
        } catch (Exception error) {
            vault.clear();
            return null;
        }
    }

    private JSONObject request(String base, String path, JSONObject body, String token) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(base + path).openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        if (token != null) connection.setRequestProperty("Authorization", "Bearer " + token);
        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
            int code = connection.getResponseCode();
            if (code < 200 || code > 299) throw new IllegalStateException("Request failed");
            if (code == 204) return new JSONObject();
            try (InputStream input = connection.getInputStream();
                    ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (output.size() + count > 16384) throw new IllegalArgumentException("Response too large");
                    output.write(buffer, 0, count);
                }
                return new JSONObject(new String(output.toByteArray(), StandardCharsets.UTF_8));
            }
        } finally {
            connection.disconnect();
        }
    }

    private void signIn() {
        final String base;
        try {
            base = origin();
        } catch (Exception error) {
            message("Enter a valid HTTPS server address.");
            return;
        }
        String previous = preferences.getString("server", "");
        if (!previous.isEmpty() && !previous.equals(base)) {
            message("Use the saved server address for this installation.");
            return;
        }
        final JSONObject credentials;
        try {
            credentials = new JSONObject().put("email", email.getText().toString().trim())
                    .put("password", password.getText().toString());
        } catch (Exception error) {
            message("Unable to prepare sign-in.");
            return;
        }
        password.getText().clear();
        preferences.edit().putString("server", base).apply();
        message("Signing in…");
        executor.execute(() -> {
            try {
                JSONObject response = request(base, "/api/v1/auth/login", credentials, null);
                long lifetime = response.getLong("expires_in");
                if (lifetime < 1 || lifetime > 900) throw new IllegalArgumentException("Invalid session lifetime");
                vault.save(new JSONObject().put("token", response.getString("access_token"))
                        .put("server", base).put("expires_at", System.currentTimeMillis() + lifetime * 1000).toString());
                preferences.edit().putString("server", base).apply();
                PublicSync.schedule(this, base);
                message("Signed in. Session expires in " + lifetime / 60 + " minutes.");
            } catch (Exception error) {
                message("Sign-in failed. Check your credentials, connection, and server address.");
            }
        });
    }

    private void signOut() {
        executor.execute(() -> {
            try {
                JSONObject saved = session();
                // Queue behind any in-flight sign-in so logout clears the latest token.
                vault.clear();
                if (saved == null) {
                    message("Signed out.");
                    return;
                }
                try {
                    request(saved.getString("server"), "/api/v1/auth/logout", new JSONObject(), saved.getString("token"));
                    message("Signed out.");
                } catch (Exception error) {
                    message("Signed out on this device. Server revocation unavailable; session expires within 15 minutes.");
                }
            } catch (Exception error) {
                message("Unable to clear the saved session. Try signing out again.");
            }
        });
    }

    private void refresh() {
        final String base;
        try {
            base = origin();
        } catch (Exception error) {
            message("Enter a valid HTTPS server address.");
            return;
        }
        // Cached content and credentials are bound to one server origin.
        String previous = preferences.getString("server", "");
        if (!previous.isEmpty() && !previous.equals(base)) {
            message("Use the saved server for cached resources.");
            return;
        }
        preferences.edit().putString("server", base).apply();
        PublicSync.schedule(this, base);
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(PublicSync.class)
                .setInputData(new Data.Builder().putString("base_url", base).build())
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build();
        WorkManager manager = WorkManager.getInstance(this);
        manager.enqueueUniqueWork("crm-public-refresh", ExistingWorkPolicy.REPLACE, work);
        message("Refresh queued. Saved resources remain available offline.");
        if (refreshObserver != null && refreshData != null) refreshData.removeObserver(refreshObserver);
        LiveData<List<WorkInfo>> data = manager.getWorkInfosForUniqueWorkLiveData("crm-public-refresh");
        Observer<List<WorkInfo>> observer = new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> value) {
                if (value == null) return;
                for (WorkInfo info : value) {
                    if (info.getId().equals(work.getId()) && info.getState().isFinished()) {
                        data.removeObserver(this);
                        if (!isDestroyed()) loadCache();
                        break;
                    }
                }
            }
        };
        refreshData = data;
        refreshObserver = observer;
        data.observeForever(observer);
    }

    private void loadCache() {
        executor.execute(() -> {
            PublicDatabase database = Room.databaseBuilder(
                    getApplicationContext(), PublicDatabase.class, "crm-public.db").build();
            try {
                List<PublicItem> rows = database.content().list();
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    resources.removeAllViews();
                    TextView state = new TextView(this);
                    if (rows.isEmpty()) {
                        state.setText("No saved public resources. Connect and refresh first.");
                    } else {
                        long oldest = Long.MAX_VALUE;
                        for (PublicItem row : rows) oldest = Math.min(oldest, row.syncedAt);
                        state.setText("Saved public resources • last synced " + new Date(oldest)
                                + ". May be outdated; available offline.");
                    }
                    resources.addView(state);
                    for (PublicItem row : rows) {
                        TextView text = new TextView(this);
                        text.setText("\n" + row.title + "\n" + row.text);
                        resources.addView(text);
                        if (row.templatePath != null) {
                            Button open = new Button(this);
                            open.setText("Open saved blank form");
                            open.setOnClickListener(view -> openTemplate(row));
                            resources.addView(open);
                        }
                    }
                });
            } finally {
                database.close();
            }
        });
    }

    private void openTemplate(PublicItem row) {
        executor.execute(() -> {
            try {
                File file = new File(row.templatePath);
                if (file.length() > 25_000_000) throw new IllegalArgumentException("Saved form too large");
                // Current server publishes a plain-text blank template.
                String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                runOnUiThread(() -> {
                    if (!isDestroyed()) new AlertDialog.Builder(this).setTitle(row.title)
                            .setMessage(text).setPositiveButton("Close", null).show();
                });
            } catch (Exception error) {
                message("Saved form unavailable. Refresh public resources.");
            }
        });
    }

    @Override
    public void onDestroy() {
        if (refreshObserver != null && refreshData != null) refreshData.removeObserver(refreshObserver);
        executor.shutdown();
        super.onDestroy();
    }
}
