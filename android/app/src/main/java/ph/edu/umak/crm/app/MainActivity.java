package ph.edu.umak.crm.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;
import java.io.IOException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;
import ph.edu.umak.crm.cache.PublicDatabase;
import ph.edu.umak.crm.cache.PublicItem;
import ph.edu.umak.crm.cache.PublicSync;
import ph.edu.umak.crm.security.SessionVault;

/** Launchable integration for sign-in, Keystore sessions, and public offline resources. */
public class MainActivity extends Activity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable expire = this::showSessionExpired;
    private ScrollView publicScreen;
    private Button staffShortcut;
    private StaffScreen staffScreen;
    private volatile int accountGeneration;
    private String staffTab = "Dashboard";
    private LinearLayout layout;
    private TextView status;
    private LinearLayout resources;
    private SharedAccess access;
    private String page = "welcome";
    private String emailAddress = "";
    private ResearcherScreen researcherScreen;
    private String researcherTab = "Dashboard";
    private TextView publicTitle;
    private Button publicAccess;
    private boolean authenticating;
    private LiveData<List<WorkInfo>> refreshData;
    private Observer<List<WorkInfo>> refreshObserver;
    private SessionVault vault;
    private SharedPreferences preferences;

    private void message(String text) {
        runOnUiThread(() -> {
            if (!isDestroyed()) {
                status.setText(text);
                if ("sign-in".equals(page) || "activation".equals(page)) access.feedback(text);
            }
        });
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        vault = new SessionVault(this);
        preferences = getSharedPreferences("crm-settings", MODE_PRIVATE);
        access = new SharedAccess(this);
        buildPublicScreen();
        loadCache();
        if (state != null) {
            researcherTab = state.getString("researcher_tab", "Dashboard");
            staffTab = state.getString("staff_tab", "Dashboard");
            emailAddress = state.getString("email_address", "");
        }
        switch (state == null ? "welcome" : state.getString("access_page", "welcome")) {
            case "sign-in": showSignIn(); break;
            case "support": displayAccess("support", ResearcherScreen.support(this)); break;
            case "activation": showActivation(); break;
            case "updated": showPasswordUpdated(); break;
            case "public": showGuest(); break;
            case "account": startAccess(); break;
            default: showWelcome();
        }
        String origin = preferences.getString("server", "");
        if (!origin.isEmpty()) PublicSync.schedule(this, origin);
    }

    private void buildPublicScreen() {
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        publicScreen = new ScrollView(this);
        publicScreen.setBackgroundColor(0xfff5f6f8);
        publicScreen.addView(layout);
        applySystemInsets(publicScreen);
        publicTitle = new TextView(this);
        publicTitle.setTypeface(getResources().getFont(R.font.marcellus_regular));
        ScreenTypography.apply(publicTitle, 28);
        ScreenTypography.heading(publicTitle);
        layout.addView(BackControl.create(this, "Back to workspace", 0xff141e4d, this::returnFromPublic));
        layout.addView(publicTitle);
        status = new TextView(this);
        status.setTypeface(getResources().getFont(R.font.metropolis_regular));
        ScreenTypography.apply(status, 14);
        status.setPadding(0, padding / 3, 0, padding / 2);
        layout.addView(status);
        staffShortcut = button("Staff workspace", this::showStaff);
        staffShortcut.setVisibility(View.GONE);
        publicAccess = button("Sign In", this::showSignIn);
        button("Refresh public resources", this::refresh);
        resources = new LinearLayout(this);
        resources.setOrientation(LinearLayout.VERTICAL);
        layout.addView(resources);
    }

    private void showWelcome() {
        displayAccess("welcome", access.welcome());
    }

    void startAccess() {
        showSignIn();
        int generation = accountGeneration;
        executor.execute(() -> {
            try {
                JSONObject saved = session();
                if (saved != null) openAccount(saved, generation);
            } catch (IOException error) {
                runOnUiThread(() -> {
                    if (!isDestroyed() && generation == accountGeneration && "sign-in".equals(page)) {
                        access.feedback("Unable to restore the saved session. Check your connection.");
                    }
                });
            } catch (Exception error) {
                message("Saved session unavailable. Sign in again.");
            }
        });
    }

    void showSignIn() {
        displayAccess("sign-in", access.signIn(emailAddress));
    }

    private void showActivation() {
        displayAccess("activation", access.activate());
    }

    private void showPasswordUpdated() {
        displayAccess("updated", access.passwordUpdated());
    }

    private void displayAccess(String destination, View view) {
        accountGeneration++;
        page = destination;
        applySystemInsets(view);
        setContentView(view);
        setLightSystemBars(!"welcome".equals(destination));
        view.requestApplyInsets();
    }

    void showAccessHelp() {
        java.util.List<String> options = new java.util.ArrayList<>();
        options.add("Set up an invited account");
        options.add("CRM server settings");
        options.add("Contact CRM");
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            options.add("Open staff design preview");
            options.add("Open researcher design preview");
        }
        new AlertDialog.Builder(this).setTitle("Need Help?")
                .setItems(options.toArray(new String[0]), (dialog, option) -> {
                    switch (option) {
                        case 0: showActivation(); break;
                        case 1: configureServer(null); break;
                        case 2:
                            displayAccess("support", ResearcherScreen.support(this));
                            break;
                        case 3: startActivity(new Intent().setClassName(this,
                                "ph.edu.umak.crm.app.PrototypeActivity")); break;
                        case 4: startActivity(new Intent().setClassName(this,
                                "ph.edu.umak.crm.app.ResearcherPreviewActivity")); break;
                        default: throw new IllegalStateException("Unknown help action");
                    }
                }).setNegativeButton("Close", null).show();
    }

    private void configureServer(Runnable onSaved) {
        EditText address = new EditText(this);
        address.setSingleLine(true);
        address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        address.setHint("https://");
        address.setText(preferences.getString("server", ""));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("CRM Server")
                .setMessage("Enter the HTTPS address provided by CRM.")
                .setView(address).setNegativeButton("Cancel", null).setPositiveButton("Save", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    try {
                        String base = origin(address.getText().toString().trim());
                        String previous = preferences.getString("server", "");
                        if (!previous.isEmpty() && !previous.equals(base)) {
                            address.setError("Use the saved server address for this installation.");
                            return;
                        }
                        preferences.edit().putString("server", base).apply();
                        dialog.dismiss();
                        if (onSaved != null) onSaved.run();
                    } catch (Exception error) {
                        address.setError("Enter a valid HTTPS server address.");
                    }
                }));
        dialog.show();
    }

    @SuppressWarnings("deprecation")
    private void setLightSystemBars(boolean light) {
        int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            android.view.WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                int mask = android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(light ? mask : 0, mask);
            }
        } else {
            View decor = getWindow().getDecorView();
            int visibility = decor.getSystemUiVisibility() & ~flags;
            decor.setSystemUiVisibility(light ? visibility | flags : visibility);
        }
    }

    @SuppressWarnings("deprecation")
    final void applySystemInsets(View view) {
        view.setOnApplyWindowInsetsListener((target, insets) -> {
            target.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
    }

    private Button button(String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTypeface(getResources().getFont(R.font.metropolis_semibold));
        button.setOnClickListener(view -> action.run());
        layout.addView(button);
        return button;
    }

    private String origin(String address) throws Exception {
        URL url = new URL(address);
        if (!"https".equals(url.getProtocol()) || url.getHost().isEmpty() || url.getUserInfo() != null
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

    void signIn(String email, EditText password) {
        if (authenticating) return;
        emailAddress = email;
        if (email.isEmpty() || password.length() == 0) {
            access.feedback("Enter your institutional email and password.");
            return;
        }
        String base = preferences.getString("server", "");
        if (base.isEmpty()) {
            configureServer(() -> signIn(email, password));
            return;
        }
        hideKeyboard(password.getWindowToken());
        int generation = ++accountGeneration;
        authenticating = true;
        access.feedback("Signing in…");
        String secret = password.getText().toString();
        executor.execute(() -> {
            try {
                JSONObject credentials = new JSONObject().put("email", email).put("password", secret);
                JSONObject response = CrmApi.post(base, "/api/v1/auth/login", credentials, null);
                long lifetime = response.getLong("expires_in");
                if (lifetime < 1 || lifetime > 900) throw new IllegalArgumentException("Invalid session lifetime");
                if (generation != accountGeneration) return;
                JSONObject saved = new JSONObject().put("token", response.getString("access_token"))
                        .put("server", base).put("expires_at", System.currentTimeMillis() + lifetime * 1000);
                vault.save(saved.toString());
                PublicSync.schedule(this, base);
                openAccount(saved, generation);
                runOnUiThread(() -> password.getText().clear());
            } catch (CrmApi.HttpError error) {
                showAuthHttpError(generation, error, "Sign-in failed. Check your institutional email and password.");
            } catch (IOException error) {
                showNoInternet(generation, () -> signIn(email, password));
            } catch (Exception error) {
                runAccessCallback(generation, () -> access.feedback("Unable to sign in. Try again."));
            } finally {
                runOnUiThread(() -> authenticating = false);
            }
        });
    }

    void activate(EditText code, EditText password, EditText confirm) {
        if (authenticating) return;
        String token = code.getText().toString().trim();
        String replacement = password.getText().toString();
        int length = replacement.codePointCount(0, replacement.length());
        if (!token.matches("[a-f0-9]{64}") || length < 12 || length > 128
                || !replacement.contentEquals(confirm.getText())) {
            access.feedback("Enter a valid activation code and matching passwords of 12 to 128 characters.");
            return;
        }
        String base = preferences.getString("server", "");
        if (base.isEmpty()) {
            configureServer(() -> activate(code, password, confirm));
            return;
        }
        hideKeyboard(confirm.getWindowToken());
        int generation = ++accountGeneration;
        authenticating = true;
        access.feedback("Updating password…");
        executor.execute(() -> {
            try {
                JSONObject request = new JSONObject().put("token", token).put("password", replacement);
                CrmApi.post(base, "/api/v1/auth/activate", request, null);
                runAccessCallback(generation, () -> {
                    code.getText().clear();
                    password.getText().clear();
                    confirm.getText().clear();
                    showPasswordUpdated();
                });
            } catch (CrmApi.HttpError error) {
                showAuthHttpError(generation, error, "The activation code is invalid or expired. Contact CRM for a new code.");
            } catch (IOException error) {
                showNoInternet(generation, () -> activate(code, password, confirm));
            } catch (Exception error) {
                runAccessCallback(generation, () -> access.feedback("Unable to update your password. Try again."));
            } finally {
                runOnUiThread(() -> authenticating = false);
            }
        });
    }

    private void hideKeyboard(IBinder token) {
        InputMethodManager keyboard = getSystemService(InputMethodManager.class);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(token, 0);
    }

    private void runAccessCallback(int generation, Runnable callback) {
        runOnUiThread(() -> {
            if (isDestroyed() || generation != accountGeneration) return;
            if ("sign-in".equals(page) || "activation".equals(page)) callback.run();
        });
    }

    private void showAuthHttpError(int generation, CrmApi.HttpError error, String fallback) {
        runAccessCallback(generation, () -> {
            if (error.status == 403) {
                access.feedback("");
                showAccessDenied();
            } else if (error.status == 429) {
                access.feedback("Too many attempts. Wait before trying again.");
            } else if (error.status >= 500) {
                access.feedback("The CRM server is temporarily unavailable. Try again later.");
            } else {
                access.feedback(fallback);
            }
        });
    }

    private void showNoInternet(int generation, Runnable retry) {
        runAccessCallback(generation, () -> {
            authenticating = false;
            access.feedback("");
            access.dialog("No Internet", "Failed to connect. Check your internet connection and try again.",
                    "Retry", retry, "Cancel");
        });
    }

    private void showAccessDenied() {
        access.dialog("Access Denied",
                "Your account does not have permission to do this action. Please contact CRM for help.",
                "Go Back", null, null);
    }

    private void openAccount(JSONObject saved, int generation) throws Exception {
        String base = saved.getString("server");
        if (!base.equals(preferences.getString("server", ""))) {
            vault.clear();
            message("Sign in using the saved server address.");
            return;
        }
        long expiresAt = saved.getLong("expires_at");
        JSONObject account;
        try {
            account = new JSONObject(CrmApi.get(base, "/api/v1/users/me", saved.getString("token")));
        } catch (CrmApi.HttpError error) {
            if (error.status != 401) throw error;
            vault.clear();
            runOnUiThread(() -> expireAccount(generation));
            return;
        }
        runOnUiThread(() -> {
            if (isDestroyed() || generation != accountGeneration) return;
            long remaining = expiresAt - System.currentTimeMillis();
            if (remaining <= 0) {
                showSessionExpired();
                return;
            }
            if (!displayAccount(account)) return;
            handler.removeCallbacks(expire);
            handler.postDelayed(expire, remaining);
        });
    }

    boolean displayAccount(JSONObject account) {
        String role = account.optString("role");
        if ("staff".equals(role)) {
            page = "account";
            staffScreen = new StaffScreen(this, account.optString("email"), staffTab);
            applySystemInsets(staffScreen);
            staffScreen.loadInitialData();
            setContentView(staffScreen);
            staffScreen.requestApplyInsets();
        } else if ("student".equals(role) || "faculty".equals(role)) {
            showResearcher(account);
        } else {
            showSignIn();
            showAccessDenied();
            return false;
        }
        return true;
    }

    void loadConsultations(StaffScreen screen) {
        int generation = accountGeneration;
        executor.execute(() -> {
            try {
                JSONObject saved = session();
                if (saved == null) {
                    runOnUiThread(() -> expireAccount(generation));
                    return;
                }
                JSONArray tickets = new JSONArray(CrmApi.get(saved.getString("server"),
                        "/api/v1/consultations", saved.getString("token")));
                runOnUiThread(() -> {
                    if (!isDestroyed() && staffScreen == screen) screen.showConsultations(tickets, null);
                });
            } catch (CrmApi.HttpError error) {
                if (error.status == 401) {
                    runOnUiThread(() -> expireAccount(generation));
                } else {
                    showQueueError(screen, "Unable to load consultations.");
                    if (error.status == 403) runOnUiThread(() -> {
                        if (!isDestroyed() && staffScreen == screen && "account".equals(page)) showAccessDenied();
                    });
                }
            } catch (Exception error) {
                showQueueError(screen, "Unable to load consultations. Check your connection.");
            }
        });
    }

    private void expireAccount(int generation) {
        if (!isDestroyed() && accountGeneration == generation) expire.run();
    }

    private void showQueueError(StaffScreen screen, String message) {
        runOnUiThread(() -> {
            if (!isDestroyed() && staffScreen == screen) screen.showConsultations(null, message);
        });
    }

    void searchResearch(StaffScreen screen, String keyword, int queryId) {
        String base = preferences.getString("server", "");
        executor.execute(() -> {
            try {
                JSONArray records = new JSONArray(CrmApi.search(base, keyword));
                runOnUiThread(() -> {
                    if (!isDestroyed() && staffScreen == screen) {
                        screen.showResearch(queryId, records, null);
                    }
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (!isDestroyed() && staffScreen == screen) {
                        screen.showResearch(queryId, null, "Unable to search. Check your connection.");
                    }
                });
            }
        });
    }

    final void showResearcher(JSONObject account) {
        researcherScreen = new ResearcherScreen(this, account, researcherTab);
        applySystemInsets(researcherScreen);
        displayResearcher();
        researcherScreen.openInitialTab();
    }

    private void displayResearcher() {
        page = researcherScreen.isSignedIn() ? "researcher" : "guest";
        setLightSystemBars(true);
        if (researcherScreen.getParent() == null) setContentView(researcherScreen);
        researcherScreen.requestApplyInsets();
    }

    void showSavedPublicResources() {
        showPublicScreen();
    }

    private static final int RESEARCHER_DOCUMENT = 24;
    private ResearcherScreen pickingScreen;
    private String pickingField;

    void pickResearcherDocument(ResearcherScreen screen, String field, String[] types) {
        if (researcherScreen != screen || !"researcher".equals(page)) return;
        pickingScreen = screen;
        pickingField = field;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(types.length == 1 ? types[0] : "*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, types);
        try {
            startActivityForResult(intent, RESEARCHER_DOCUMENT);
        } catch (android.content.ActivityNotFoundException unavailable) {
            screen.showDocumentName(field, "No document picker available.");
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent intent) {
        super.onActivityResult(request, result, intent);
        if (request != RESEARCHER_DOCUMENT) return;
        ResearcherScreen screen = pickingScreen;
        String field = pickingField;
        pickingScreen = null;
        pickingField = null;
        if (screen == null || screen != researcherScreen || result != RESULT_OK || intent == null
                || intent.getData() == null || !"researcher".equals(page)) return;
        String name = "Document selected";
        try (android.database.Cursor row = getContentResolver().query(intent.getData(),
                new String[]{android.provider.OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (row != null && row.moveToFirst() && !row.isNull(0)) name = row.getString(0);
        } catch (SecurityException unavailable) {
            name = "Document could not be read";
        }
        screen.showDocumentName(field, name);
    }

    void researcherRequest(ResearcherScreen screen, String path, JSONObject body, boolean authenticated,
            java.util.function.BiConsumer<String, String> result) {
        int generation = accountGeneration;
        executor.execute(() -> {
            try {
                JSONObject saved = authenticated ? session() : null;
                if (authenticated && saved == null) {
                    runOnUiThread(() -> expireAccount(generation));
                    return;
                }
                String base = authenticated ? saved.getString("server") : preferences.getString("server", "");
                if (base.isEmpty()) {
                    deliverResearcher(screen, generation, result, null, "Set the CRM server from shared sign-in to connect.");
                    return;
                }
                String token = authenticated ? saved.getString("token") : null;
                String data = body == null ? CrmApi.get(base, path, token) : CrmApi.send(base, path, body, token);
                deliverResearcher(screen, generation, result, data, null);
            } catch (CrmApi.HttpError error) {
                if (authenticated && error.status == 401) {
                    try {
                        vault.clear();
                    } catch (Exception unavailable) {
                        // The rejected token cannot authorize further requests.
                    }
                    runOnUiThread(() -> expireAccount(generation));
                } else {
                    deliverResearcher(screen, generation, result, null, error.status == 403
                            ? "Access denied." : "CRM returned an error (" + error.status + ").");
                }
            } catch (Exception error) {
                deliverResearcher(screen, generation, result, null, "Unable to reach CRM. Check your connection.");
            }
        });
    }

    private void deliverResearcher(ResearcherScreen screen, int generation,
            java.util.function.BiConsumer<String, String> result, String data, String error) {
        runOnUiThread(() -> {
            if (!isDestroyed() && researcherScreen == screen && accountGeneration == generation) result.accept(data, error);
        });
    }

    void showPublicResources() {
        if (staffScreen == null) return;
        staffTab = staffScreen.selectedTab();
        showPublicScreen();
    }

    void showGuest() {
        accountGeneration++;
        if (researcherScreen != null && !researcherScreen.isSignedIn()) displayResearcher();
        else showResearcher(null);
    }

    private void showPublicScreen() {
        page = "public";
        setLightSystemBars(true);
        publicTitle.setText("Public Resources");
        status.setText("Public resources are available offline.");
        publicAccess.setVisibility(staffScreen == null ? View.VISIBLE : View.GONE);
        staffShortcut.setVisibility(staffScreen == null ? View.GONE : View.VISIBLE);
        if (publicScreen.getParent() == null) setContentView(publicScreen);
        publicScreen.requestApplyInsets();
    }

    void showStaff() {
        if (staffScreen == null || staffScreen.getParent() != null) return;
        page = "account";
        setContentView(staffScreen);
        staffScreen.requestApplyInsets();
    }

    private void leaveAccount(String message) {
        handler.removeCallbacks(expire);
        staffScreen = null;
        researcherScreen = null;
        pickingScreen = null;
        pickingField = null;
        researcherTab = "Dashboard";
        staffTab = "Dashboard";
        showSignIn();
        access.feedback(message);
    }

    private void showSessionExpired() {
        leaveAccount("");
        access.dialog("Session Expired", "Your session has ended. Please sign in again to continue.",
                "Sign In", null, null);
    }

    boolean isResearcherPreview() { return false; }

    void backFromSignIn() {
        if (researcherScreen != null && !researcherScreen.isSignedIn()) displayResearcher();
        else showWelcome();
    }

    void leaveGuestPortal() {
        researcherScreen = null;
        showSignIn();
    }

    private void returnFromPublic() {
        if (staffScreen != null) showStaff();
        else if (researcherScreen != null) displayResearcher();
        else showSignIn();
    }

    @Override
    public void onBackPressed() {
        switch (page) {
            case "support": showSignIn(); break;
            case "activation":
            case "updated": showSignIn(); break;
            case "public": returnFromPublic(); break;
            case "researcher":
            case "guest":
                if (!researcherScreen.goBack()) {
                    if ("guest".equals(page)) leaveGuestPortal();
                    else super.onBackPressed();
                }
                break;
            case "sign-in": backFromSignIn(); break;
            default: super.onBackPressed();
        }
    }

    void signOut() {
        accountGeneration++;
        leaveAccount("Signing out…");
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
                    CrmApi.post(saved.getString("server"), "/api/v1/auth/logout", new JSONObject(), saved.getString("token"));
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
        String base = preferences.getString("server", "");
        if (base.isEmpty()) {
            configureServer(this::refresh);
            return;
        }
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
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("staff_tab", staffScreen == null ? staffTab : staffScreen.selectedTab());
        state.putString("researcher_tab", researcherScreen == null ? researcherTab : researcherScreen.selectedTab());
        boolean signedInResearcher = researcherScreen != null && researcherScreen.isSignedIn();
        state.putString("access_page", signedInResearcher ? "account" : "guest".equals(page) ? "public" : page);
        state.putString("email_address", emailAddress);
    }

    @Override
    public void onDestroy() {
        if (refreshObserver != null && refreshData != null) refreshData.removeObserver(refreshObserver);
        handler.removeCallbacks(expire);
        executor.shutdown();
        super.onDestroy();
    }
}
