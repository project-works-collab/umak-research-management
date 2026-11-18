package ph.edu.umak.crm.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import java.io.IOException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.dialogs.EmailVerificationDialog;
import ph.edu.umak.crm.app.common.dialogs.SignInGateDialog;
import ph.edu.umak.crm.app.common.util.UiTheme;
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
    private Button refreshButton;
    private PendingStatus publicRefreshStatus;
    private PendingStatus publicCacheStatus;
    private UUID refreshId;
    private LiveData<WorkInfo> refreshData;
    private Observer<WorkInfo> refreshObserver;
    private SessionVault vault;
    private SharedPreferences preferences;

    void message(String text) {
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
        if ((BuildConfig.HOSTED_DEMO || preferences.getString("server", "").isEmpty())
                && BuildConfig.DEFAULT_SERVER_URL != null
                && !BuildConfig.DEFAULT_SERVER_URL.isEmpty()) {
            preferences.edit().putString("server", BuildConfig.DEFAULT_SERVER_URL).apply();
        }
        access = new SharedAccess(this);
        buildPublicScreen();
        loadCache(null);
        if (state != null) {
            String pendingFormName = state.getString("pending_service_form", "");
            if (pendingFormName.matches("service-form-[0-9]+\\.download")) {
                java.io.File pending = new java.io.File(getCacheDir(), pendingFormName);
                if (pending.isFile()) pendingServiceForm = pending;
            }
            String savedRefresh = state.getString("public_refresh_id");
            if (savedRefresh != null) observeRefresh(UUID.fromString(savedRefresh));
            researcherTab = state.getString("researcher_tab", "Dashboard");
            staffTab = state.getString("staff_tab", "Dashboard");
            emailAddress = state.getString("email_address", "");
        }
        switch (state == null ? "welcome" : state.getString("access_page", "welcome")) {
            case "sign-in": showSignIn(); break;
            case "create-account": showCreateAccount(); break;
            case "email-verify": showEmailVerification(emailAddress); break;
            case "support": displayAccess("support", ResearcherScreen.support(this)); break;
            case "activation": showActivation(); break;
            case "updated": showPasswordUpdated(); break;
            case "public": showPublicScreen(); break;
            case "guest": showGuest(); break;
            case "account": startAccess(); break;
            default: showWelcome();
        }
        updatePublicRefreshSchedule();
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
        refreshButton = button("Refresh public resources", this::refresh);
        publicRefreshStatus = new PendingStatus(this);
        publicCacheStatus = new PendingStatus(this);
        button("Data & Sync", this::showDataSync);
        layout.addView(publicRefreshStatus);
        layout.addView(publicCacheStatus);
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

    private String passwordChallenge;

    void showSignIn() {
        passwordChallenge = null;
        displayAccess("sign-in", access.signIn(emailAddress));
    }

    void showCreateAccount() {
        displayAccess("create-account", access.createAccount());
    }

    void onAccountCreated(String email, String fullName, String college, String category,
            String password, String confirmation) {
        var view = access.getCurrentCreateAccount();
        if (view == null || !"create-account".equals(page)) return;
        if (preferences.getString("server", "").isEmpty()) {
            configureServer(() -> onAccountCreated(email, fullName, college, category, password, confirmation));
            return;
        }
        view.setSubmitting(true);
        JSONObject body = accountRequestBody(email);
        try {
            body.put("display_name", fullName).put("college", college).put("role", category)
                    .put("password", password).put("confirm_password", confirmation);
        } catch (JSONException invalid) {
            view.setSubmitting(false);
            view.showBanner("Unable to prepare registration. Try again.");
            return;
        }
        publicAuthRequest("/api/v1/auth/researchers/register", body, (result, status) -> {
            view.setSubmitting(false);
            if (result != null) {
                view.clearPasswords();
                showEmailVerification(email);
                access.getCurrentEmailVerify().startCountdown(result.optInt("resend_after_seconds", 45));
            } else if (status == 409) {
                view.showEmailError("This institutional email is already registered.");
            } else if (status == 422) {
                view.showEmailError("Enter an accepted institutional email.");
            } else {
                view.showBanner(verificationRequestError(status));
            }
        });
    }

    void showEmailVerification(String email) {
        this.emailAddress = email;
        displayAccess("email-verify", access.emailVerify(email));
    }

    void verifyEmail(String email, String code) {
        var view = access.getCurrentEmailVerify();
        if (view == null || !"email-verify".equals(page) || email == null || email.isBlank()) return;
        view.setSubmitting(true, "Verifying email…");
        JSONObject body = accountRequestBody(email);
        try {
            body.put("code", code);
        } catch (JSONException invalid) {
            view.setSubmitting(false, "");
            view.showError("Enter the complete 6-digit verification code.");
            return;
        }
        publicAuthRequest("/api/v1/auth/researchers/verify", body, (result, status) -> {
            view.setSubmitting(false, "");
            if (result != null) view.showSuccess();
            else if (status == 400) view.showError("This verification link or code is invalid, expired or already used.");
            else view.showError(verificationRequestError(status));
        });
    }

    void resendVerificationCode(String email) {
        var view = access.getCurrentEmailVerify();
        if (view == null || !"email-verify".equals(page) || email == null || email.isBlank()) return;
        view.setSubmitting(true, "Sending verification email…");
        publicAuthRequest("/api/v1/auth/researchers/resend", accountRequestBody(email), (result, status) -> {
            view.setSubmitting(false, "");
            if (result != null) view.showResent(result.optInt("resend_after_seconds", 45));
            else if (status == 400) view.showError("Verification is unavailable for this account.");
            else view.showError(verificationRequestError(status));
        });
    }

    private JSONObject accountRequestBody(String email) {
        JSONObject body = new JSONObject();
        try {
            return body.put("email", email);
        } catch (JSONException invalid) {
            throw new IllegalArgumentException("Unable to encode email address", invalid);
        }
    }

    private String verificationRequestError(int status) {
        if (status == 429) return "Too many attempts. Wait before trying again.";
        if (status == 400) return "Review the account details and try again.";
        return "Unable to connect or send verification email. Try again.";
    }

    private void publicAuthRequest(String path, JSONObject body,
            java.util.function.BiConsumer<JSONObject, Integer> callback) {
        int generation = accountGeneration;
        String base = preferences.getString("server", "");
        executor.execute(() -> {
            JSONObject result = null;
            int status = 0;
            try {
                result = CrmApi.post(base, path, body, null);
            } catch (CrmApi.HttpError error) {
                status = error.status;
            } catch (Exception unavailable) {
                // The current form remains available for retry.
            }
            JSONObject response = result;
            int httpStatus = status;
            runOnUiThread(() -> {
                if (!isDestroyed() && generation == accountGeneration) callback.accept(response, httpStatus);
            });
        });
    }

    void onEmailVerified(String email) {
        this.emailAddress = email;
        showSignIn();
    }

    private void showActivation() {
        displayAccess("activation", access.activate());
    }

    private void showPasswordUpdated() {
        displayAccess("updated", access.passwordUpdated());
    }

    private float savedFabX = -1f;
    private float savedFabY = -1f;

    private View wrapWithFloatingPreview(View content) {
        if (content.getParent() instanceof ViewGroup) {
            ((ViewGroup) content.getParent()).removeView(content);
        }
        if (BuildConfig.HOSTED_DEMO) return content;
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(content, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Floating Circular Preview FAB Button
        Button fab = new Button(this, null, android.R.attr.borderlessButtonStyle);
        fab.setText("P");
        fab.setTextColor(UiTheme.YELLOW);
        fab.setTextSize(18);
        fab.setTypeface(UiTheme.boldFont(this));
        fab.setBackground(UiTheme.ripple(UiTheme.shape(this, UiTheme.NAVY, 28, 0), 0x30ffffff));
        fab.setElevation(UiTheme.dp(this, 6));

        int fabSize = UiTheme.dp(this, 56);
        FrameLayout.LayoutParams fabParams = new FrameLayout.LayoutParams(fabSize, fabSize);
        fabParams.gravity = Gravity.TOP | Gravity.START;
        fab.setLayoutParams(fabParams);
        root.addView(fab);

        final int innerPadding = UiTheme.dp(this, 20);

        // Position restoring and bounds clamping
        root.post(() -> {
            int rootW = root.getWidth();
            int rootH = root.getHeight();
            if (rootW <= 0 || rootH <= 0) return;

            int minX = innerPadding;
            int maxX = rootW - fab.getWidth() - innerPadding;
            int minY = innerPadding;
            int maxY = rootH - fab.getHeight() - innerPadding;

            if (savedFabX < 0 || savedFabY < 0) {
                savedFabX = maxX;
                savedFabY = maxY;
            } else {
                if (maxX > minX) savedFabX = Math.max(minX, Math.min(savedFabX, maxX));
                if (maxY > minY) savedFabY = Math.max(minY, Math.min(savedFabY, maxY));
            }
            fab.setX(savedFabX);
            fab.setY(savedFabY);
        });

        fab.setOnClickListener(v -> showPreviewMenu());

        final int touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        fab.setOnTouchListener(new View.OnTouchListener() {
            private float downRawX, downRawY;
            private float dX, dY;
            private boolean isDragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downRawX = event.getRawX();
                        downRawY = event.getRawY();
                        dX = v.getX() - downRawX;
                        dY = v.getY() - downRawY;
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float diffX = event.getRawX() - downRawX;
                        float diffY = event.getRawY() - downRawY;
                        if (!isDragging && Math.hypot(diffX, diffY) > touchSlop) {
                            isDragging = true;
                        }
                        if (isDragging) {
                            float newX = event.getRawX() + dX;
                            float newY = event.getRawY() + dY;

                            int minX = innerPadding;
                            int maxX = root.getWidth() - v.getWidth() - innerPadding;
                            int minY = innerPadding;
                            int maxY = root.getHeight() - v.getHeight() - innerPadding;

                            if (maxX > minX) newX = Math.max(minX, Math.min(newX, maxX));
                            if (maxY > minY) newY = Math.max(minY, Math.min(newY, maxY));

                            v.setX(newX);
                            v.setY(newY);
                            savedFabX = newX;
                            savedFabY = newY;
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            v.performClick();
                        }
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        return true;
                }
                return false;
            }
        });

        return root;
    }

    private void displayAccess(String destination, View view) {
        accountGeneration++;
        page = destination;
        applySystemInsets(view);
        View wrapped = wrapWithFloatingPreview(view);
        setContentView(wrapped);
        setLightSystemBars(!"welcome".equals(destination));
        view.requestApplyInsets();
    }

    void showPreviewMenu() {
        java.util.List<String> options = new java.util.ArrayList<>();
        options.add("Part 1: Access & Account Previews");
        options.add("Part 2: Service Directory Previews");
        options.add("Part 3: Requests & Notifications Previews");
        options.add("Part 4: Profile, Registration & Progress Previews");
        options.add("Part 5: Staff & Administration Previews");
        options.add("Part 6: Requests Review & Processing Previews");
        options.add("Part 7: Research Registry & Corrections Previews");
        options.add("Part 8: Stages, Milestones & Outcomes Previews");
        options.add("Part 9: Staff Reporting & Analytics Previews");
        options.add("Modal: 1.6 Verify Email First (Service Request)");
        options.add("Modal: 1.7 Sign In to Submit (Visitor)");
        options.add("Modal: 2.4 Form Retrieval Failure");
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            options.add("Open staff design preview");
            options.add("Open researcher design preview");
        }
        options.add("Set up an invited account");
        options.add("CRM server settings");

        new AlertDialog.Builder(this)
                .setTitle("Design & Wireframe Previews")
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    String selected = options.get(which);
                    if (selected.contains("Part 1")) {
                        showPart1WireframeSelector();
                    } else if (selected.contains("Part 2")) {
                        showPart2WireframeSelector();
                    } else if (selected.contains("Part 3")) {
                        showPart3WireframeSelector();
                    } else if (selected.contains("Part 4")) {
                        showPart4WireframeSelector();
                    } else if (selected.contains("Part 5")) {
                        showPart5WireframeSelector();
                    } else if (selected.contains("Part 6")) {
                        showPart6WireframeSelector();
                    } else if (selected.contains("Part 7")) {
                        showPart7WireframeSelector();
                    } else if (selected.contains("Part 8")) {
                        showPart8WireframeSelector();
                    } else if (selected.contains("Part 9")) {
                        showPart9WireframeSelector();
                    } else if (selected.contains("1.6")) {
                        EmailVerificationDialog.show(this,
                                () -> showEmailVerification(emailAddress),
                                () -> resendVerificationCode(emailAddress));
                    } else if (selected.contains("1.7")) {
                        SignInGateDialog.show(this,
                                this::showSignIn,
                                this::showCreateAccount);
                    } else if (selected.contains("2.4")) {
                        ensureResearcherScreen();
                        researcherScreen.previewFormFailureModal();
                    } else if (selected.contains("staff design preview")) {
                        startActivity(new Intent().setClassName(this, "ph.edu.umak.crm.app.PrototypeActivity"));
                    } else if (selected.contains("researcher design preview")) {
                        startActivity(new Intent().setClassName(this, "ph.edu.umak.crm.app.ResearcherPreviewActivity"));
                    } else if (selected.contains("Set up an invited account")) {
                        showActivation();
                    } else if (selected.contains("CRM server settings")) {
                        configureServer(null);
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showPart1WireframeSelector() {
        final String[] items = new String[]{
                "1.1 Sign In (Default)",
                "1.2a Sign In (Blank Username Error)",
                "1.2b Sign In (Blank Password Error)",
                "1.2c Sign In (Incorrect Credentials Error)",
                "1.2d Sign In (Inactive Account Error)",
                "1.3 Sign In (Pending / Checking Account)",
                "1.4 Create Account (Default)",
                "1.4 Create Account (Missing Required Error)",
                "1.4 Create Account (Duplicate Email Error)",
                "1.4 Create Account (Short Password Error)",
                "1.4 Create Account (Password Mismatch Error)",
                "1.5 Verify Email (Default with Countdown)",
                "1.5 Verify Email (Invalid / Expired Code Error)",
                "1.5 Verify Email (Success State)",
                "1.6 Modal: Verify Email First (Service Request)",
                "1.7 Modal: Sign In to Submit (Visitor)",
                "1.8a Change Temp Password (Short Password Error)",
                "1.8b Change Temp Password (Password Mismatch Error)",
                "1.8c Change Temp Password (Same as Temp Password Error)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Part 1 State to Preview")
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showSignIn();
                            if (access.getCurrentSignIn() != null) access.getCurrentSignIn().configurePreviewState("1.1");
                            break;
                        case 1:
                            showSignIn();
                            if (access.getCurrentSignIn() != null) access.getCurrentSignIn().configurePreviewState("1.2a");
                            break;
                        case 2:
                            showSignIn();
                            if (access.getCurrentSignIn() != null) access.getCurrentSignIn().configurePreviewState("1.2b");
                            break;
                        case 3:
                            showSignIn();
                            if (access.getCurrentSignIn() != null) access.getCurrentSignIn().configurePreviewState("1.2c");
                            break;
                        case 4:
                            showSignIn();
                            if (access.getCurrentSignIn() != null) access.getCurrentSignIn().configurePreviewState("1.2d");
                            break;
                        case 5:
                            showSignIn();
                            if (access.getCurrentSignIn() != null) access.getCurrentSignIn().configurePreviewState("1.3");
                            break;
                        case 6:
                            showCreateAccount();
                            if (access.getCurrentCreateAccount() != null) access.getCurrentCreateAccount().configurePreviewState("1.4");
                            break;
                        case 7:
                            showCreateAccount();
                            if (access.getCurrentCreateAccount() != null) access.getCurrentCreateAccount().configurePreviewState("1.4-required");
                            break;
                        case 8:
                            showCreateAccount();
                            if (access.getCurrentCreateAccount() != null) access.getCurrentCreateAccount().configurePreviewState("1.4-duplicate");
                            break;
                        case 9:
                            showCreateAccount();
                            if (access.getCurrentCreateAccount() != null) access.getCurrentCreateAccount().configurePreviewState("1.4-short-pass");
                            break;
                        case 10:
                            showCreateAccount();
                            if (access.getCurrentCreateAccount() != null) access.getCurrentCreateAccount().configurePreviewState("1.4-mismatch");
                            break;
                        case 11:
                            showEmailVerification(emailAddress);
                            if (access.getCurrentEmailVerify() != null) access.getCurrentEmailVerify().configurePreviewState("1.5");
                            break;
                        case 12:
                            showEmailVerification(emailAddress);
                            if (access.getCurrentEmailVerify() != null) access.getCurrentEmailVerify().configurePreviewState("1.5-error");
                            break;
                        case 13:
                            showEmailVerification(emailAddress);
                            if (access.getCurrentEmailVerify() != null) access.getCurrentEmailVerify().configurePreviewState("1.5-success");
                            break;
                        case 14:
                            EmailVerificationDialog.show(this,
                                    () -> showEmailVerification(emailAddress),
                                    () -> resendVerificationCode(emailAddress));
                            break;
                        case 15:
                            SignInGateDialog.show(this,
                                    this::showSignIn,
                                    this::showCreateAccount);
                            break;
                        case 16:
                            showActivation();
                            if (access.getCurrentActivation() != null) access.getCurrentActivation().configurePreviewState("1.8a");
                            break;
                        case 17:
                            showActivation();
                            if (access.getCurrentActivation() != null) access.getCurrentActivation().configurePreviewState("1.8b");
                            break;
                        case 18:
                            showActivation();
                            if (access.getCurrentActivation() != null) access.getCurrentActivation().configurePreviewState("1.8c");
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void ensureResearcherScreen() {
        if (researcherScreen == null) {
            showGuest();
        } else if (!"researcher".equals(page) && !"guest".equals(page)) {
            displayResearcher();
        }
    }

    private void showPart2WireframeSelector() {
        final String[] items = new String[]{
                "2.1 Services (Default - All 8 Services)",
                "2.1 Services (Results - Search 'Ethics')",
                "2.1 Services (Empty - Search 'ZZZ')",
                "2.2a Service Details (In-app - SVC-02)",
                "2.2b Service Details (External channel - SVC-04)",
                "2.2c Service Details (Form unavailable - SVC-02)",
                "2.3 External Submission Instructions (SVC-04)",
                "2.4 Modal: Form Retrieval Failure",
                "2.5 App Navigation Guidance"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Part 2 State to Preview")
                .setItems(items, (dialog, which) -> {
                    ensureResearcherScreen();
                    switch (which) {
                        case 0:
                            researcherScreen.previewServiceDirectory("2.1");
                            break;
                        case 1:
                            researcherScreen.previewServiceDirectory("2.1-ethics");
                            break;
                        case 2:
                            researcherScreen.previewServiceDirectory("2.1-empty");
                            break;
                        case 3:
                            researcherScreen.previewServiceDetail("2.2a");
                            break;
                        case 4:
                            researcherScreen.previewServiceDetail("2.2b");
                            break;
                        case 5:
                            researcherScreen.previewServiceDetail("2.2c");
                            break;
                        case 6:
                            researcherScreen.previewExternalSubmission();
                            break;
                        case 7:
                            researcherScreen.previewFormFailureModal();
                            break;
                        case 8:
                            researcherScreen.previewAppNavigation();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPart3WireframeSelector() {
        final String[] items = new String[]{
                "3.1 Submit Service Request (Default)",
                "3.1 Submit Service Request (Linked Study Open)",
                "3.2a Submit Error (Required Fields Missing)",
                "3.2b Submit Error (Study Title Too Short)",
                "3.2c Submit Error (Invalid Linked Study)",
                "3.3a Attachments Error (Invalid File Type - PNG)",
                "3.3b Attachments Error (File Exceeds 10MB)",
                "3.3c Attachments Error (Max 5 Files Limit)",
                "3.3d Attachments Error (Upload Failed / Retry)",
                "3.4 Submitting… (In Flight State)",
                "3.4b Unable to Connect (Retry State)",
                "3.5 Request Receipt (Initial Submission Success)",
                "3.6a My Requests (Default / 4 Populated Requests)",
                "3.6 Filter Sheet (Filter by Status & Date)",
                "3.6b My Requests (Filtered State - In Review)",
                "3.6c My Requests (Empty Account)",
                "3.6d My Requests (Empty Search - No Matching)",
                "3.7a Request Details (Submitted Lifecycle)",
                "3.7b Request Details (In Review Lifecycle)",
                "3.7c Request Details (For Correction Lifecycle)",
                "3.7d Request Details (Completed Lifecycle)",
                "3.7e Correct Request (Feedback & Resubmit Form)",
                "3.8 Correction Submitted Receipt (Resubmission Success)",
                "3.9 Notifications (Default - 2 Unread)",
                "3.9 Notifications (Empty - No Notifications)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Part 3 State to Preview")
                .setItems(items, (dialog, which) -> {
                    ensureResearcherScreen();
                    switch (which) {
                        case 0:
                            researcherScreen.previewSubmitRequest("3.1");
                            break;
                        case 1:
                            researcherScreen.previewSubmitRequest("3.1-linked");
                            break;
                        case 2:
                            researcherScreen.previewSubmitRequest("3.2a");
                            break;
                        case 3:
                            researcherScreen.previewSubmitRequest("3.2b");
                            break;
                        case 4:
                            researcherScreen.previewSubmitRequest("3.2c");
                            break;
                        case 5:
                            researcherScreen.previewSubmitRequest("3.3a");
                            break;
                        case 6:
                            researcherScreen.previewSubmitRequest("3.3b");
                            break;
                        case 7:
                            researcherScreen.previewSubmitRequest("3.3c");
                            break;
                        case 8:
                            researcherScreen.previewSubmitRequest("3.3d");
                            break;
                        case 9:
                            researcherScreen.previewSubmitRequest("3.4");
                            break;
                        case 10:
                            researcherScreen.previewSubmitRequest("3.4b");
                            break;
                        case 11:
                            researcherScreen.previewRequestReceipt(false);
                            break;
                        case 12:
                            researcherScreen.previewMyRequests("3.6a_default");
                            break;
                        case 13:
                            researcherScreen.previewRequestFilterDialog();
                            break;
                        case 14:
                            researcherScreen.previewMyRequests("3.6b_filtered");
                            break;
                        case 15:
                            researcherScreen.previewMyRequests("3.6c_empty_account");
                            break;
                        case 16:
                            researcherScreen.previewMyRequests("3.6d_empty_search");
                            break;
                        case 17:
                            researcherScreen.previewRequestDetail("Submitted");
                            break;
                        case 18:
                            researcherScreen.previewRequestDetail("In review");
                            break;
                        case 19:
                            researcherScreen.previewRequestDetail("For correction");
                            break;
                        case 20:
                            researcherScreen.previewRequestDetail("Completed");
                            break;
                        case 21:
                            researcherScreen.previewCorrectRequest();
                            break;
                        case 22:
                            researcherScreen.previewRequestReceipt(true);
                            break;
                        case 23:
                            researcherScreen.previewNotifications("3.9_default");
                            break;
                        case 24:
                            researcherScreen.previewNotifications("3.9_empty");
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPart4WireframeSelector() {
        final String[] items = new String[]{
                "4.1 Edit Profile (Default)",
                "4.1 Edit Profile (Error - Name Blank)",
                "4.1 Edit Profile (Error - College Unselected)",
                "4.1 Profile (Success Snackbar - Profile updated)",
                "4.2a Registration Step 1 (Prefilled)",
                "4.2b Registration Step 1 (Error - Blank Title)",
                "4.2c Registration Step 1 (Error - Academic Period Unselected)",
                "4.2d Registration Step 1 (Error - College Unselected)",
                "4.2e Registration Step 1 (Error - Adviser Blank for Student)",
                "4.2f Registration Step 1 (Faculty - Adviser Optional)",
                "4.3 Add Co-Author / Collaborator Sheet",
                "4.4 Registration Confirmation (Receipt Success)",
                "4.5 Profile (Default Populated)",
                "4.6a Research Progress (All Outcomes Recorded)",
                "4.6b Research Progress (Completed with Deadline)",
                "4.6c Research Progress (In Progress / No Next Action)",
                "4.6d Research Progress (Empty State)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Part 4 State to Preview")
                .setItems(items, (dialog, which) -> {
                    ensureResearcherScreen();
                    switch (which) {
                        case 0:
                            researcherScreen.previewEditProfile("4.1_default");
                            break;
                        case 1:
                            researcherScreen.previewEditProfile("4.1_error_name");
                            break;
                        case 2:
                            researcherScreen.previewEditProfile("4.1_error_college");
                            break;
                        case 3:
                            researcherScreen.previewProfile("4.1_success");
                            break;
                        case 4:
                            researcherScreen.previewRegistrationStep1("4.2a_prefilled");
                            break;
                        case 5:
                            researcherScreen.previewRegistrationStep1("4.2b_error_title");
                            break;
                        case 6:
                            researcherScreen.previewRegistrationStep1("4.2c_error_academic_period");
                            break;
                        case 7:
                            researcherScreen.previewRegistrationStep1("4.2d_error_college");
                            break;
                        case 8:
                            researcherScreen.previewRegistrationStep1("4.2e_error_adviser");
                            break;
                        case 9:
                            researcherScreen.previewRegistrationStep1("4.2f_faculty");
                            break;
                        case 10:
                            researcherScreen.previewAddCoAuthorDialog();
                            break;
                        case 11:
                            researcherScreen.previewRegistrationConfirmation();
                            break;
                        case 12:
                            researcherScreen.previewProfile("4.5_default");
                            break;
                        case 13:
                            researcherScreen.previewResearchProgress("4.6a");
                            break;
                        case 14:
                            researcherScreen.previewResearchProgress("4.6b");
                            break;
                        case 15:
                            researcherScreen.previewResearchProgress("4.6c");
                            break;
                        case 16:
                            researcherScreen.previewResearchProgress("4.6d");
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void ensureStaffScreen() {
        if (staffScreen == null) {
            page = "account";
            staffScreen = new StaffScreen(this, "areyes@umak.edu.ph", "Manage");
            applySystemInsets(staffScreen);
            setContentView(wrapWithFloatingPreview(staffScreen));
            staffScreen.requestApplyInsets();
        } else if (!"account".equals(page)) {
            showStaff();
        }
    }

    private void showPart5WireframeSelector() {
        final String[] items = new String[]{
                "5.1 Manage Hub (Default)",
                "5.2 Service Catalog (Default)",
                "5.3 Service Details Step 1 (In app)",
                "5.3 Service Details Step 1 (External channel)",
                "5.4 Approved Forms & Resources (SVC-02)",
                "5.5 Step 3 Access & Publishing (Missing Info Error)",
                "5.5 Step 3 Access & Publishing (Draft Launch Status)",
                "5.6a Dialog: Withdraw Service?",
                "5.6b Dialog: Replace Form Version?",
                "5.7 Publication History (Default)",
                "5.8 User Accounts (Default)",
                "5.9 Create Staff Account (Default)",
                "5.9 Create Staff Account (Error - Missing Details)",
                "5.9 Create Staff Account (Error - Duplicate Email)",
                "5.10a Account Details: Staff (Ana M. Reyes)",
                "5.10b Account Details: Researcher (LeBron R. James)",
                "5.11a Dialog: Deactivate Account Confirmation",
                "5.11b Dialog: Activate Account Confirmation",
                "5.11c Dialog: Can't Deactivate Own Account",
                "5.11d Dialog: Can't Deactivate Last Administrator",
                "5.12 Dialog: Temporary Credential Issued",
                "5.13 Account Audit History (LeBron R. James)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Part 5 State to Preview")
                .setItems(items, (dialog, which) -> {
                    ensureStaffScreen();
                    switch (which) {
                        case 0: staffScreen.showPreview5_1_ManageHub(); break;
                        case 1: staffScreen.showPreview5_2_ServiceCatalog(); break;
                        case 2: staffScreen.showPreview5_3_AuthoringStep1InApp(); break;
                        case 3: staffScreen.showPreview5_3_AuthoringStep1External(); break;
                        case 4: staffScreen.showPreview5_4_ApprovedForms(); break;
                        case 5: staffScreen.showPreview5_5_AuthoringStep3Error(); break;
                        case 6: staffScreen.showPreview5_5_AuthoringStep3Draft(); break;
                        case 7: staffScreen.showPreview5_6a_WithdrawServiceDialog(); break;
                        case 8: staffScreen.showPreview5_6b_ReplaceFormVersionDialog(); break;
                        case 9: staffScreen.showPreview5_7_PublicationHistory(); break;
                        case 10: staffScreen.showPreview5_8_UserAccounts(); break;
                        case 11: staffScreen.showPreview5_9_CreateStaffDefault(); break;
                        case 12: staffScreen.showPreview5_9_CreateStaffMissingError(); break;
                        case 13: staffScreen.showPreview5_9_CreateStaffDuplicateError(); break;
                        case 14: staffScreen.showPreview5_10a_AccountDetailsStaff(); break;
                        case 15: staffScreen.showPreview5_10b_AccountDetailsResearcher(); break;
                        case 16: staffScreen.showPreview5_11a_DeactivateDialog(); break;
                        case 17: staffScreen.showPreview5_11b_ActivateDialog(); break;
                        case 18: staffScreen.showPreview5_11c_OwnAccountErrorDialog(); break;
                        case 19: staffScreen.showPreview5_11d_LastAdminErrorDialog(); break;
                        case 20: staffScreen.showPreview5_12_TemporaryCredentialIssuedDialog(); break;
                        case 21: staffScreen.showPreview5_13_AccountAuditHistory(); break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPart6WireframeSelector() {
        final String[] items = new String[]{
                "6.1 Review Queue (In review tab)",
                "6.1 Review Queue (Completed tab)",
                "6.2 Filter Queue Sheet",
                "6.2 Review Queue (No matching requests)",
                "6.3a Request Workspace (Part 1 - Requester & Checklist)",
                "6.3b Request Workspace (Part 2 - Decision)",
                "6.4a Request Workspace (Error - Missing Items & Next Action)",
                "6.4b Request Workspace (Error - Status Change Not Allowed)",
                "6.4c Request Workspace (Stale Data Conflict)",
                "6.5 Request Workspace (Completed - Authorized Correction)",
                "6.6 Request Workspace (Review Saved Confirmation)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Part 6 State to Preview")
                .setItems(items, (dialog, which) -> {
                    ensureStaffScreen();
                    switch (which) {
                        case 0: staffScreen.showPreview6_1_InReviewTab(); break;
                        case 1: staffScreen.showPreview6_1_CompletedTab(); break;
                        case 2: staffScreen.showPreview6_2_FilterSheet(); break;
                        case 3: staffScreen.showPreview6_2_NoMatchingRequests(); break;
                        case 4: staffScreen.showPreview6_3a_RequestWorkspaceRequesterAndChecklist(); break;
                        case 5: staffScreen.showPreview6_3b_RequestWorkspaceDecision(); break;
                        case 6: staffScreen.showPreview6_4a_ErrorMissingItemsAndNextAction(); break;
                        case 7: staffScreen.showPreview6_4b_ErrorStatusChangeNotAllowed(); break;
                        case 8: staffScreen.showPreview6_4c_StaleDataConflict(); break;
                        case 9: staffScreen.showPreview6_5_CompletedRequestCorrection(); break;
                        case 10: staffScreen.showPreview6_6_ReviewSaved(); break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPart7WireframeSelector() {
        final String[] items = new String[]{
                "7.1a Research Registry (All records default)",
                "7.1b Research Registry (Filters applied: CCIS, AY 2026-2027)",
                "7.1c Research Registry (Empty State: Health Sciences, AY 2023-2024)",
                "7.2 Research Record Detail (REG-2026-089, Team, Docs, Linked reqs)",
                "7.3 Add Research Entry (Team picker, Peter Parker)",
                "7.3 Add Research Entry (Error: Already a team member)",
                "7.3 Add Research Entry (Error: No such account)",
                "7.4 Add Research Entry (Possible duplicate REG-2024-012)",
                "7.4 Add Research Entry (Error: Duplicate reason missing)",
                "7.5 Record Editor (Stale data conflict warning & reload)",
                "7.6 Documents (Version list, manuscript-v2, manuscript-v1)",
                "7.7 Correction History (Title, Co-authors, Academic period audit)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Part 7 State to Preview")
                .setItems(items, (dialog, which) -> {
                    ensureStaffScreen();
                    switch (which) {
                        case 0: staffScreen.showPreview7_1a_Results(); break;
                        case 1: staffScreen.showPreview7_1b_FiltersApplied(); break;
                        case 2: staffScreen.showPreview7_1c_EmptyState(); break;
                        case 3: staffScreen.showPreview7_2_RecordDetail(); break;
                        case 4: staffScreen.showPreview7_3_TeamPicker(); break;
                        case 5: staffScreen.showPreview7_3_ErrorAlreadyMember(); break;
                        case 6: staffScreen.showPreview7_3_ErrorNoSuchAccount(); break;
                        case 7: staffScreen.showPreview7_4_PossibleDuplicate(); break;
                        case 8: staffScreen.showPreview7_4_ErrorReasonMissing(); break;
                        case 9: staffScreen.showPreview7_5_StaleDataConflict(); break;
                        case 10: staffScreen.showPreview7_6_DocumentsVersionList(); break;
                        case 11: staffScreen.showPreview7_7_CorrectionHistory(); break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPart8WireframeSelector() {
        final String[] items = new String[]{
                "8.1 Milestone and Outcomes (Default with No next action checkbox)",
                "8.1a Error: stage change not allowed",
                "8.1b Error: effective date missing",
                "8.1c Error: supporting details missing",
                "8.1d Error: next action missing",
                "8.1e Error: completion date missing",
                "8.1f Error: presentation date and event missing",
                "8.1g Error: publication date and citation missing",
                "8.2 Milestone editor sheet (Data Collection & Analysis)",
                "8.3 Stage History timeline",
                "8.4 Completion correction page",
                "8.5a Stage updated confirmation",
                "8.5b Outcome recorded confirmation (Completion only)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Part 8 State to Preview")
                .setItems(items, (dialog, which) -> {
                    ensureStaffScreen();
                    switch (which) {
                        case 0: staffScreen.showPreview8_1_Default(); break;
                        case 1: staffScreen.showPreview8_1a_ErrorStageNotAllowed(); break;
                        case 2: staffScreen.showPreview8_1b_ErrorEffectiveDate(); break;
                        case 3: staffScreen.showPreview8_1c_ErrorSupportingDetails(); break;
                        case 4: staffScreen.showPreview8_1d_ErrorNextAction(); break;
                        case 5: staffScreen.showPreview8_1e_ErrorCompletionDate(); break;
                        case 6: staffScreen.showPreview8_1f_ErrorPresentation(); break;
                        case 7: staffScreen.showPreview8_1g_ErrorPublication(); break;
                        case 8: staffScreen.showPreview8_2_MilestoneEditor(); break;
                        case 9: staffScreen.showPreview8_3_StageHistory(); break;
                        case 10: staffScreen.showPreview8_4_CompletionCorrection(); break;
                        case 11: staffScreen.showPreview8_5a_StageUpdated(); break;
                        case 12: staffScreen.showPreview8_5b_OutcomeRecorded(); break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPart9WireframeSelector() {
        final String[] items = new String[]{
                "9.1 Research Report · Result",
                "9.2 Service Utilization · Result",
                "9.3 Research Report · Empty",
                "9.4a Export · Format picker dialog",
                "9.4b Export · Pending progress dialog",
                "9.4c Export · Success ready dialog",
                "9.4d Export · Failure retry dialog",
                "9.5 Access denied · No Reporting permission"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Part 9 State to Preview")
                .setItems(items, (dialog, which) -> {
                    ensureStaffScreen();
                    switch (which) {
                        case 0: staffScreen.showPreview9_1_ResearchReportResult(); break;
                        case 1: staffScreen.showPreview9_2_ServiceUtilizationResult(); break;
                        case 2: staffScreen.showPreview9_3_ResearchReportEmpty(); break;
                        case 3: staffScreen.showPreview9_4a_ExportFormatPicker(); break;
                        case 4: staffScreen.showPreview9_4b_ExportPending(); break;
                        case 5: staffScreen.showPreview9_4c_ExportSuccess(); break;
                        case 6: staffScreen.showPreview9_4d_ExportFailure(); break;
                        case 7: staffScreen.showPreview9_5_AccessDenied(); break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    void showAccessHelp() {
        displayAccess("support", ResearcherScreen.support(this));
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
                        updatePublicRefreshSchedule();
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

    private long lastUserActivity;
    private long lastSessionRenewal;
    private boolean renewingSession;

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (!"account".equals(page) && !"researcher".equals(page)) return;
        long now = android.os.SystemClock.elapsedRealtime();
        if (lastUserActivity == 0 || now - lastUserActivity >= 30 * 60_000L) {
            if (lastUserActivity != 0) showSessionExpired();
            return;
        }
        lastUserActivity = now;
        handler.removeCallbacks(expire);
        handler.postDelayed(expire, 30 * 60_000L);
        if (renewingSession || now - lastSessionRenewal < 30_000L) return;
        renewingSession = true;
        lastSessionRenewal = now;
        int generation = accountGeneration;
        executor.execute(() -> {
            try {
                JSONObject saved = session();
                if (saved == null) {
                    runOnUiThread(() -> expireAccount(generation));
                    return;
                }
                JSONObject response = CrmApi.post(saved.getString("server"), "/api/v1/auth/activity",
                        new JSONObject(), saved.getString("token"));
                if (generation == accountGeneration) {
                    saved.put("token", response.getString("access_token"))
                            .put("expires_at", System.currentTimeMillis() + response.getLong("expires_in") * 1000);
                    vault.save(saved.toString());
                }
            } catch (CrmApi.HttpError error) {
                if (error.status == 401) runOnUiThread(() -> expireAccount(generation));
            } catch (Exception unavailable) {
                // A later interaction retries renewal. Protected calls still enforce server expiry.
            } finally {
                runOnUiThread(() -> renewingSession = false);
            }
        });
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
        var signInView = access.getCurrentSignIn();
        if (signInView != null) signInView.setLoading(true);
        String secret = password.getText().toString();
        executor.execute(() -> {
            try {
                JSONObject credentials = new JSONObject().put("email", email).put("password", secret);
                JSONObject response = CrmApi.post(base, "/api/v1/auth/login", credentials, null);
                if (response.optBoolean("requires_password_change")) {
                    String challenge = response.getString("challenge");
                    runAccessCallback(generation, () -> {
                        passwordChallenge = challenge;
                        showActivation();
                        access.getCurrentActivation().setTemporaryPassword(secret);
                        password.getText().clear();
                    });
                    return;
                }
                long lifetime = response.getLong("expires_in");
                if (lifetime < 1 || lifetime > 1800) throw new IllegalArgumentException("Invalid session lifetime");
                if (generation != accountGeneration) return;
                JSONObject saved = new JSONObject().put("token", response.getString("access_token"))
                        .put("server", base).put("expires_at", System.currentTimeMillis() + lifetime * 1000);
                vault.save(saved.toString());
                openAccount(saved, generation);
                runOnUiThread(() -> password.getText().clear());
            } catch (CrmApi.HttpError error) {
                showAuthHttpError(generation, error, "Incorrect username or password.");
            } catch (IOException error) {
                showNoInternet(generation, () -> signIn(email, password));
            } catch (Exception error) {
                runAccessCallback(generation, () -> access.feedback("Unable to sign in. Try again."));
            } finally {
                runOnUiThread(() -> {
                    authenticating = false;
                    if (signInView != null) signInView.setLoading(false);
                });
            }
        });
    }

    void activate(EditText code, EditText password, EditText confirm) {
        if (authenticating) return;
        if (passwordChallenge != null) {
            replaceTemporaryPassword(password, confirm);
            return;
        }
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
        var activation = access.getCurrentActivation();
        if (activation != null) activation.setUpdating(true);
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
                runOnUiThread(() -> {
                    authenticating = false;
                    if (activation != null) activation.setUpdating(false);
                });
            }
        });
    }

    private void replaceTemporaryPassword(EditText password, EditText confirm) {
        var view = access.getCurrentActivation();
        if (view == null) return;
        String secret = password.getText().toString();
        JSONObject body = new JSONObject();
        try { body.put("challenge", passwordChallenge).put("password", secret).put("confirmation", confirm.getText().toString()); }
        catch (JSONException invalid) { view.setFeedback("Check the replacement password."); return; }
        authenticating = true;
        view.setUpdating(true);
        publicAuthRequest("/api/v1/auth/replace-temporary-password", body, (response, status) -> {
            authenticating = false;
            view.setUpdating(false);
            if (response == null) {
                view.setFeedback(status == 429 ? "Too many attempts. Wait before trying again."
                        : "Unable to replace the password. Check the details or sign in again.");
                return;
            }
            password.getText().clear();
            confirm.getText().clear();
            showSignIn();
            var signIn = access.getCurrentSignIn();
            signIn.getPasswordInput().setText(secret);
            signIn(emailAddress, signIn.getPasswordInput());
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
            if ("email_unverified".equals(error.code)) {
                showEmailVerification(emailAddress);
            } else if ("account_unavailable".equals(error.code)) {
                access.feedback("Account unavailable. Contact CRM");
            } else if (error.status == 403) {
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
        lastUserActivity = android.os.SystemClock.elapsedRealtime();
        lastSessionRenewal = lastUserActivity;
        String role = account.optString("role");
        if ("staff".equals(role)) {
            page = "account";
            staffScreen = new StaffScreen(this, account.optString("email"), staffTab, true, account.optJSONArray("permissions"));
            applySystemInsets(staffScreen);
            staffScreen.loadInitialData();
            setContentView(wrapWithFloatingPreview(staffScreen));
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
        if (!screen.beginConsultationsRefresh()) return;
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
        setContentView(wrapWithFloatingPreview(researcherScreen));
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

    private static final int REQUEST_SAVE_SERVICE_FORM = 26;
    private java.io.File pendingServiceForm;
    private boolean downloadingServiceForm;

    void downloadServiceForm(ph.edu.umak.crm.app.services.ServiceData.ServiceItem item,
            java.util.function.BooleanSupplier visible, Runnable started, Runnable ready, Runnable failure) {
        downloadCatalogDocument("/api/v1/public/services/" + item.revisionId + "/form",
                item.formTitle, item.formMime, false, visible, started, ready, failure);
    }

    void downloadCatalogForm(int id, String name, String mime,
            java.util.function.BooleanSupplier visible, Runnable failure) {
        downloadCatalogDocument("/api/v1/staff/services/" + id + "/form", name, mime, true,
                visible, () -> {}, () -> {}, failure);
    }

    void downloadRequestAttachment(String path, String filename, String mime,
            java.util.function.BooleanSupplier visible, Runnable failure) {
        downloadCatalogDocument(path, filename, mime, true, visible, () -> {}, () -> {}, failure);
    }

    private void downloadCatalogDocument(String path, String name, String mime, boolean authenticated,
            java.util.function.BooleanSupplier visible, Runnable started, Runnable ready, Runnable failure) {
        if (downloadingServiceForm || pendingServiceForm != null) return;
        String origin = preferences.getString("server", "");
        int generation = accountGeneration;
        downloadingServiceForm = true;
        started.run();
        executor.execute(() -> {
            java.io.File downloaded = null;
            try {
                downloaded = java.io.File.createTempFile("service-form-", ".download", getCacheDir());
                JSONObject saved = authenticated ? session() : null;
                if (authenticated && saved == null) throw new IOException("Sign in again");
                CrmApi.downloadForm(origin, path, saved == null ? null : saved.getString("token"), mime, downloaded);
            } catch (Exception unavailable) {
                if (downloaded != null) downloaded.delete();
                downloaded = null;
            }
            java.io.File file = downloaded;
            runOnUiThread(() -> {
                downloadingServiceForm = false;
                if (isDestroyed() || generation != accountGeneration || !visible.getAsBoolean()) {
                    if (file != null) file.delete();
                    return;
                }
                if (file == null) {
                    failure.run();
                    return;
                }
                pendingServiceForm = file;
                ready.run();
                Intent picker = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType(mime)
                        .putExtra(Intent.EXTRA_TITLE, name);
                try {
                    startActivityForResult(picker, REQUEST_SAVE_SERVICE_FORM);
                } catch (android.content.ActivityNotFoundException unavailable) {
                    pendingServiceForm = null;
                    file.delete();
                    failure.run();
                }
            });
        });
    }

    private void saveServiceForm(int result, Intent intent) {
        java.io.File file = pendingServiceForm;
        pendingServiceForm = null;
        if (file == null) return;
        if (result != RESULT_OK || intent == null || intent.getData() == null) {
            file.delete();
            return;
        }
        android.net.Uri destination = intent.getData();
        executor.execute(() -> {
            try (var input = new java.io.FileInputStream(file);
                    var output = getContentResolver().openOutputStream(destination)) {
                if (output == null) throw new IOException("Unable to open save location");
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                output.flush();
                serviceFormNotice("Form saved.");
            } catch (IOException | SecurityException unavailable) {
                serviceFormNotice("Unable to save the form. Try again.");
            } finally {
                file.delete();
            }
        });
    }

    private void serviceFormNotice(String notice) {
        runOnUiThread(() -> {
            if (!isDestroyed()) Toast.makeText(this, notice, Toast.LENGTH_SHORT).show();
        });
    }

    private record CatalogUpload(StaffScreen screen, int revision, int generation, Runnable complete,
            java.util.function.Consumer<String> failure) {}
    private static final int REQUEST_CATALOG_FORM = 27;
    private CatalogUpload catalogUpload;

    void staffRequest(StaffScreen screen, String path, JSONObject body,
            java.util.function.BiConsumer<String, String> callback) {
        int generation = accountGeneration;
        executor.execute(() -> {
            String data = null;
            String error = null;
            JSONArray refreshedPermissions = null;
            try {
                JSONObject saved = session();
                if (saved == null) {
                    runOnUiThread(() -> expireAccount(generation));
                    return;
                }
                String origin = saved.getString("server");
                String token = saved.getString("token");
                data = body == null ? CrmApi.get(origin, path, token) : CrmApi.send(origin, path, body, token);
                if (body != null && path.startsWith("/api/v1/staff/accounts/")) {
                    refreshedPermissions = new JSONObject(CrmApi.get(origin, "/api/v1/users/me", token)).getJSONArray("permissions");
                }
            } catch (CrmApi.HttpError rejected) {
                if (rejected.status == 401) {
                    runOnUiThread(() -> expireAccount(generation));
                    return;
                }
                error = switch (rejected.status) {
                    case 400, 409 -> rejected.detail.isEmpty() ? "Check the information and try again." : rejected.detail;
                    case 403 -> "Access denied.";
                    default -> "CRM could not save or load this information. Try again.";
                };
            } catch (Exception unavailable) {
                error = "Unable to connect. Try again.";
            }
            JSONArray currentPermissions = refreshedPermissions;
            String response = data;
            String message = error;
            runOnUiThread(() -> {
                if (!isDestroyed() && staffScreen == screen && generation == accountGeneration) {
                    callback.accept(response, message);
                    if (currentPermissions != null) screen.updatePermissions(currentPermissions);
                }
            });
        });
    }

    void chooseCatalogForm(StaffScreen screen, int revision, Runnable complete,
            java.util.function.Consumer<String> failure) {
        if (catalogUpload != null) return;
        catalogUpload = new CatalogUpload(screen, revision, accountGeneration, complete, failure);
        Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/pdf",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"});
        try {
            startActivityForResult(picker, REQUEST_CATALOG_FORM);
        } catch (android.content.ActivityNotFoundException unavailable) {
            catalogUpload = null;
            failure.accept("No file picker is available.");
        }
    }

    private void uploadSelectedCatalogForm(int result, Intent intent) {
        CatalogUpload upload = catalogUpload;
        catalogUpload = null;
        if (upload == null || result != RESULT_OK || intent == null || intent.getData() == null) return;
        android.net.Uri uri = intent.getData();
        executor.execute(() -> {
            String error = null;
            try {
                JSONObject saved = session();
                if (saved == null) {
                    runOnUiThread(() -> expireAccount(upload.generation()));
                    return;
                }
                String extension = switch (String.valueOf(getContentResolver().getType(uri))) {
                    case "application/pdf" -> ".pdf";
                    case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
                    default -> "";
                };
                if (extension.isEmpty()) throw new IOException("Invalid file type");
                try (var input = getContentResolver().openInputStream(uri);
                        var output = new java.io.ByteArrayOutputStream()) {
                    if (input == null) throw new IOException("File unavailable");
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        if (output.size() + count > 10_000_000) throw new IOException("File too large");
                        output.write(buffer, 0, count);
                    }
                    CrmApi.uploadForm(saved.getString("server"), upload.revision(), saved.getString("token"),
                            "approved-form" + extension, output.toByteArray());
                }
            } catch (CrmApi.HttpError rejected) {
                error = rejected.status == 403 ? "Access denied." : "CRM could not upload this form. Try again.";
            } catch (Exception unavailable) {
                error = "Unable to upload the form. Select a PDF or DOCX up to 10 MB and try again.";
            }
            String message = error;
            runOnUiThread(() -> {
                if (!isDestroyed() && upload.generation() == accountGeneration && staffScreen == upload.screen()) {
                    if (message == null) upload.complete().run();
                    else upload.failure().accept(message);
                }
            });
        });
    }

    private static final int REQUEST_ATTACHMENT = 25;
    private static final long ATTACHMENT_LIMIT_BYTES = 10_000_000;
    private java.util.function.BiConsumer<SelectedAttachment, String> attachmentResult;
    private int attachmentGeneration;
    private Runnable attachmentChecking;

    public record SelectedAttachment(android.net.Uri uri, String name, long size) {}

    public void chooseRequestAttachment(Runnable onChecking, java.util.function.BiConsumer<SelectedAttachment, String> result) {
        if (attachmentResult != null) return;
        attachmentResult = result;
        attachmentChecking = onChecking;
        attachmentGeneration = accountGeneration;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        });
        try {
            startActivityForResult(intent, REQUEST_ATTACHMENT);
        } catch (android.content.ActivityNotFoundException unavailable) {
            attachmentResult = null;
            attachmentChecking = null;
            result.accept(null, "No document picker is available.");
        }
    }

    private void readRequestAttachment(Intent intent,
                                       java.util.function.BiConsumer<SelectedAttachment, String> callback,
                                       int generation) {
        android.net.Uri uri = intent.getData();
        executor.execute(() -> {
            SelectedAttachment selected = null;
            String error;
            try (android.database.Cursor row = getContentResolver().query(uri,
                    new String[]{android.provider.OpenableColumns.DISPLAY_NAME, android.provider.OpenableColumns.SIZE},
                    null, null, null)) {
                if (row == null || !row.moveToFirst() || row.isNull(0) || row.isNull(1)) {
                    error = "Could not read the document name or size. Choose another file.";
                } else {
                    String name = row.getString(0);
                    String extension = name.toLowerCase(java.util.Locale.ROOT);
                    long size = row.getLong(1);
                    if (!extension.endsWith(".pdf") && !extension.endsWith(".docx")) {
                        error = "Upload a PDF or DOCX file.";
                    } else if (size <= 0 || size > ATTACHMENT_LIMIT_BYTES) {
                        error = size > ATTACHMENT_LIMIT_BYTES ? "Each file must be 10 MB or smaller." : "Choose a non-empty file.";
                    } else {
                        selected = new SelectedAttachment(uri, name, size);
                        error = null;
                    }
                }
            } catch (RuntimeException unavailable) {
                error = "Could not read the selected file.";
            }
            SelectedAttachment attachment = selected;
            String message = error;
            runOnUiThread(() -> {
                if (!isDestroyed() && accountGeneration == generation) callback.accept(attachment, message);
            });
        });
    }

    @Override
    protected void onActivityResult(int request, int result, Intent intent) {
        super.onActivityResult(request, result, intent);
        if (request == REQUEST_CATALOG_FORM) {
            uploadSelectedCatalogForm(result, intent);
            return;
        }
        if (request == REQUEST_SAVE_SERVICE_FORM) {
            saveServiceForm(result, intent);
            return;
        }
        if (request == REQUEST_ATTACHMENT) {
            var callback = attachmentResult;
            Runnable onChecking = attachmentChecking;
            attachmentResult = null;
            attachmentChecking = null;
            if (callback != null && result == RESULT_OK && intent != null && intent.getData() != null
                    && attachmentGeneration == accountGeneration) {
                onChecking.run();
                readRequestAttachment(intent, callback, attachmentGeneration);
            }
            return;
        }
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

    void uploadServiceRequestAttachment(ResearcherScreen screen, SelectedAttachment attachment,
            int requirement, String attempt, java.util.function.BiConsumer<String, String> callback) {
        String query = new android.net.Uri.Builder().appendQueryParameter("filename", attachment.name())
                .appendQueryParameter("requirement", String.valueOf(requirement)).appendQueryParameter("attempt_token", attempt)
                .build().toString();
        uploadPrivateAttachment(attachment, "/api/v1/service-requests/files" + query,
                () -> researcherScreen == screen, callback);
    }

    void uploadRegistryAttachment(StaffScreen screen, SelectedAttachment attachment, int study, String previous,
            String attempt, java.util.function.BiConsumer<String, String> callback) {
        android.net.Uri.Builder query = new android.net.Uri.Builder().appendQueryParameter("filename", attachment.name())
                .appendQueryParameter("upload_token", attempt);
        if (previous != null) query.appendQueryParameter("previous_id", previous);
        uploadPrivateAttachment(attachment, "/api/v1/staff/registry/" + study + "/documents" + query.build(),
                () -> staffScreen == screen, callback);
    }

    private void uploadPrivateAttachment(SelectedAttachment attachment, String path,
            java.util.function.BooleanSupplier current, java.util.function.BiConsumer<String, String> callback) {
        int generation = accountGeneration;
        executor.execute(() -> {
            String result = null;
            String error = null;
            try {
                JSONObject saved = session();
                if (saved == null) {
                    runOnUiThread(() -> expireAccount(generation));
                    return;
                }
                try (var input = getContentResolver().openInputStream(attachment.uri());
                        var output = new java.io.ByteArrayOutputStream()) {
                    if (input == null) throw new IOException("File unavailable");
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        if (output.size() + count > ATTACHMENT_LIMIT_BYTES) throw new IOException("File too large");
                        output.write(buffer, 0, count);
                    }
                    result = CrmApi.uploadBinary(saved.getString("server"), path, saved.getString("token"), output.toByteArray());
                }
            } catch (CrmApi.HttpError rejected) {
                if (rejected.status == 401) {
                    runOnUiThread(() -> expireAccount(generation));
                    return;
                }
                error = rejected.status == 415 ? "Upload a PDF or DOCX file."
                        : rejected.status == 413 ? "Each file must be 10 MB or smaller." : "Upload failed. Try again.";
            } catch (Exception unavailable) {
                error = "Upload failed. Try again.";
            }
            String response = result;
            String message = error;
            runOnUiThread(() -> {
                if (!isDestroyed() && generation == accountGeneration && current.getAsBoolean()) callback.accept(response, message);
            });
        });
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
                    String detail = (path.startsWith("/api/v1/service-requests")
                            || path.startsWith("/api/v1/researcher/registrations"))
                            && (error.status == 400 || error.status == 404 || error.status == 409) && !error.detail.isEmpty()
                            ? error.detail : error.status == 403 ? "Access denied."
                            : "CRM returned an error (" + error.status + ").";
                    deliverResearcher(screen, generation, result, null, detail);
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
        if (publicScreen.getParent() instanceof ViewGroup) {
            ((ViewGroup) publicScreen.getParent()).removeView(publicScreen);
        }
        setContentView(wrapWithFloatingPreview(publicScreen));
        publicScreen.requestApplyInsets();
    }

    void showStaff() {
        if (staffScreen == null) return;
        page = "account";
        if (staffScreen.getParent() instanceof ViewGroup) {
            ((ViewGroup) staffScreen.getParent()).removeView(staffScreen);
        }
        setContentView(wrapWithFloatingPreview(staffScreen));
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
            case "create-account":
            case "email-verify":
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
            case "account":
                if (staffScreen == null || !staffScreen.handleBack()) super.onBackPressed();
                break;
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
                    message("Signed out on this device. Server revocation unavailable; session expires within 30 minutes.");
                }
            } catch (Exception error) {
                message("Unable to clear the saved session. Try signing out again.");
            }
        });
    }

    private void updatePublicRefreshSchedule() {
        String base = preferences.getString("server", "");
        if (!base.isEmpty() && preferences.getBoolean("public_auto_refresh", false)) {
            PublicSync.schedule(this, base);
        } else {
            PublicSync.cancelSchedule(this);
        }
    }

    private void showDataSync() {
        boolean automatic = preferences.getBoolean("public_auto_refresh", false);
        new AlertDialog.Builder(this)
                .setTitle("Data & Sync")
                .setSingleChoiceItems(new String[]{"Refresh public resources on demand",
                        "Auto-refresh on unmetered networks"}, automatic ? 1 : 0, (dialog, choice) -> {
                    preferences.edit().putBoolean("public_auto_refresh", choice == 1).apply();
                    updatePublicRefreshSchedule();
                    dialog.dismiss();
                    if (choice == 1 && preferences.getString("server", "").isEmpty()) configureServer(null);
                })
                .setNeutralButton("Clear saved resources", (dialog, which) -> confirmClearPublicResources())
                .setNegativeButton("Close", null).show();
    }

    private boolean clearingPublicResources;

    private void confirmClearPublicResources() {
        if (refreshId != null || clearingPublicResources) {
            message("Wait for the current resource operation to finish.");
            return;
        }
        new AlertDialog.Builder(this).setTitle("Clear saved public resources?")
                .setMessage("Downloaded forms and instructions will be removed from this device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    clearingPublicResources = true;
                    refreshButton.setEnabled(false);
                    publicCacheStatus.working("Clearing saved public resources…");
                    executor.execute(() -> {
                        boolean cleared = PublicSync.clearCache(this);
                        runOnUiThread(() -> {
                            if (isDestroyed()) return;
                            clearingPublicResources = false;
                            refreshButton.setEnabled(true);
                            if (cleared) {
                                publicRefreshStatus.clear();
                                loadCache(null);
                            } else {
                                publicCacheStatus.result("Saved resources could not be cleared. Try again.", true);
                            }
                        });
                    });
                }).show();
    }

    private void refresh() {
        String base = preferences.getString("server", "");
        if (refreshId != null || clearingPublicResources) return;
        if (base.isEmpty()) {
            configureServer(this::refresh);
            return;
        }
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(PublicSync.class)
                .setInputData(new Data.Builder().putString("base_url", base).build())
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build();
        WorkManager manager = WorkManager.getInstance(this);
        var enqueue = manager.enqueueUniqueWork("crm-public-refresh", ExistingWorkPolicy.REPLACE, work).getResult();
        observeRefresh(work.getId());
        enqueue.addListener(() -> {
            try {
                enqueue.get();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                if (!isDestroyed() && work.getId().equals(refreshId)) finishRefresh("Refresh was interrupted. Try again.", true);
            } catch (java.util.concurrent.ExecutionException | java.util.concurrent.CancellationException failure) {
                if (!isDestroyed() && work.getId().equals(refreshId)) finishRefresh("Refresh could not start. Try again.", true);
            }
        }, this::runOnUiThread);
    }

    private void observeRefresh(UUID id) {
        stopObservingRefresh();
        refreshId = id;
        refreshButton.setEnabled(false);
        refreshButton.setText("Refresh pending…");
        publicRefreshStatus.working("Waiting to refresh. Saved resources remain available.");
        refreshData = WorkManager.getInstance(this).getWorkInfoByIdLiveData(id);
        refreshObserver = info -> {
            if (info == null || !id.equals(refreshId)) return;
            switch (info.getState()) {
                case RUNNING:
                    refreshButton.setText("Refreshing…");
                    publicRefreshStatus.working("Refreshing public resources…");
                    break;
                case SUCCEEDED:
                    stopObservingRefresh();
                    publicRefreshStatus.working("Opening refreshed resources…");
                    loadCache(loaded -> finishRefresh(loaded ? "Public resources are up to date."
                            : "Refresh finished, but the saved resources could not be opened.", !loaded));
                    break;
                case FAILED:
                case CANCELLED:
                    finishRefresh("Refresh did not finish. Saved resources remain available. Try again.", true);
                    break;
                default:
                    refreshButton.setText("Refresh pending…");
                    publicRefreshStatus.working(info.getRunAttemptCount() > 0
                            ? "Waiting to retry the refresh. Saved resources remain available."
                            : "Waiting for a connection or for refresh to start. Saved resources remain available.");
                    break;
            }
        };
        refreshData.observeForever(refreshObserver);
    }

    private void stopObservingRefresh() {
        if (refreshObserver != null && refreshData != null) refreshData.removeObserver(refreshObserver);
        refreshObserver = null;
        refreshData = null;
    }

    private void finishRefresh(String message, boolean failed) {
        stopObservingRefresh();
        refreshId = null;
        refreshButton.setEnabled(true);
        refreshButton.setText("Refresh public resources");
        publicRefreshStatus.result(message, failed);
    }

    private void loadCache(java.util.function.Consumer<Boolean> onLoaded) {
        if (onLoaded == null) publicCacheStatus.working("Loading saved resources…");
        executor.execute(() -> {
            PublicDatabase database = null;
            try {
                database = Room.databaseBuilder(getApplicationContext(), PublicDatabase.class, "crm-public.db").build();
                List<PublicItem> rows = database.content().list();
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    publicCacheStatus.clear();
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
                            resources.addView(open);
                            PendingStatus opening = new PendingStatus(this);
                            resources.addView(opening);
                            open.setOnClickListener(view -> openTemplate(row, open, opening));
                        }
                    }
                    if (onLoaded != null) onLoaded.accept(true);
                });
            } catch (RuntimeException failure) {
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    if (onLoaded == null) publicCacheStatus.result("Could not open saved public resources.", true);
                    else onLoaded.accept(false);
                });
            } finally {
                if (database != null) database.close();
            }
        });
    }

    private void openTemplate(PublicItem row, Button button, PendingStatus opening) {
        button.setEnabled(false);
        button.setText("Opening form…");
        opening.working("Opening the saved form…");
        executor.execute(() -> {
            String text = null;
            try {
                File file = new File(row.templatePath);
                if (file.length() > 25_000_000) throw new IllegalArgumentException("Saved form too large");
                text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            } catch (IOException | RuntimeException unavailable) {
                // Keep the resource list available so the user can refresh or try another form.
            }
            String template = text;
            runOnUiThread(() -> {
                if (isDestroyed()) return;
                button.setEnabled(true);
                button.setText("Open saved blank form");
                if (template == null) {
                    opening.result("Saved form unavailable. Refresh public resources and try again.", true);
                } else {
                    opening.clear();
                    if ("public".equals(page) && opening.isAttachedToWindow()) {
                        new AlertDialog.Builder(this).setTitle(row.title)
                                .setMessage(template).setPositiveButton("Close", null).show();
                    }
                }
            });
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("staff_tab", staffScreen == null ? staffTab : staffScreen.selectedTab());
        state.putString("researcher_tab", researcherScreen == null ? researcherTab : researcherScreen.selectedTab());
        boolean signedInResearcher = researcherScreen != null && researcherScreen.isSignedIn();
        state.putString("access_page", signedInResearcher ? "account" : page);
        state.putString("email_address", emailAddress);
        if (pendingServiceForm != null) state.putString("pending_service_form", pendingServiceForm.getName());
        if (refreshId != null) state.putString("public_refresh_id", refreshId.toString());
    }

    @Override
    public void onDestroy() {
        stopObservingRefresh();
        handler.removeCallbacks(expire);
        executor.shutdown();
        super.onDestroy();
    }
}
