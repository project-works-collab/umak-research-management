package ph.edu.umak.crm.app;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.junit.Test;
import ph.edu.umak.crm.security.SessionVault;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public final class CatalogHttpsTest {
    @Test
    public void guestDownloadsPublishedFormOverHttps() throws Exception {
        withSyntheticApi(scenario -> {
            scenario.onActivity(activity -> {
                click(activity, "Get Started");
                click(activity, "Continue as Guest");
                click(activity, "Services");
            });
            waitFor(scenario, "Demo Service A");
            scenario.onActivity(activity -> {
                assertNull(find(activity.getWindow().getDecorView(), "Demo Draft Service"));
                click(activity, "Demo Service A");
                assertNotNull(find(activity.getWindow().getDecorView(), "demo-form.pdf"));
                assertNotNull(find(activity.getWindow().getDecorView(), "Optional"));
                click(activity, "Download demo-form.pdf");
            });
            var ui = InstrumentationRegistry.getInstrumentation().getUiAutomation();
            boolean opened = false;
            for (int attempt = 0; attempt < 35 && !opened; attempt++) {
                var root = ui.getRootInActiveWindow();
                opened = root != null && String.valueOf(root.getPackageName()).contains("documentsui");
                if (!opened) SystemClock.sleep(300);
            }
            assertTrue("The real form download opened Android's save picker", opened);
        });
    }

    @Test
    public void contentManagerSavesAnUnfinishedDraftFromManage() throws Exception {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File credentials = new File(context.getFilesDir(), "catalog-test-password");
        assumeTrue("Requires the generated synthetic staff password", credentials.isFile());
        String password = new String(Files.readAllBytes(credentials.toPath()),
                java.nio.charset.StandardCharsets.UTF_8).trim();
        SessionVault vault = new SessionVault(context);
        String originalSession = vault.read();
        try {
            withSyntheticApi(scenario -> {
                scenario.onActivity(activity -> signIn(activity, "demo.content@example.test", password));
                waitFor(scenario, "Manage");
                scenario.onActivity(activity -> {
                    click(activity, "Manage");
                    click(activity, "Service Catalog");
                });
                waitFor(scenario, "New Service");
                String code = "SVC-ANDROID-" + Long.toString(System.nanoTime(), 36).toUpperCase(java.util.Locale.ROOT);
                scenario.onActivity(activity -> {
                    click(activity, "New Service");
                    View screen = activity.getWindow().getDecorView();
                    EditText serviceCode = findEditor(screen, "Service code");
                    assertNotNull(serviceCode);
                    serviceCode.setText(code);
                    click(activity, "Save as Draft");
                });
                waitFor(scenario, "Draft saved to CRM.");
            });
        } finally {
            vault.clear();
            if (originalSession != null) vault.save(originalSession);
        }
    }

    @Test public void contentManagerPublishesDesignedServiceWizard() throws Exception {
        String code="SVC-WIZARD-"+Long.toString(System.nanoTime(),36).toUpperCase(java.util.Locale.ROOT);
        withIdentity("demo.content@example.test",scenario->{
            scenario.onActivity(activity->{click(activity,"Manage");click(activity,"Service Catalog");});
            waitFor(scenario,"New Service");
            scenario.onActivity(activity->{
                click(activity,"New Service");View root=activity.getWindow().getDecorView();
                String[][] fields={{"Service code",code},{"Service name","Live wizard service"},{"Brief description","Android wizard integration"},{"Purpose","Review real research"},{"Who can apply","Registered students"},{"Responsible contact","CRM Office"},{"Contact email","crm@example.test"},{"Instructions, one step per line","Submit your protocol"}};
                for(String[] field:fields){EditText control=findEditor(root,field[0]);assertNotNull(field[0],control);control.setText(field[1]);}
                ((android.widget.CheckBox)find(root,"Student")).setChecked(true);click(activity,"Continue");
            });
            waitFor(scenario,"Continue to Access & Publishing");
            captureLiveScreen("part5-live-resources");
            scenario.onActivity(activity->{click(activity,"Continue to Access & Publishing");click(activity,"Publish Service");click(activity,"Confirm");});
            waitFor(scenario,"Service published.");
            captureLiveScreen("part5-live-catalog");
            String origin=InstrumentationRegistry.getArguments().getString("catalogOrigin");
            org.json.JSONArray catalog=new org.json.JSONArray(CrmApi.get(origin,"/api/v1/public/services",null));
            boolean found=false;for(int i=0;i<catalog.length();i++)if(code.equals(catalog.getJSONObject(i).getString("code")))found=true;
            assertTrue("The wizard published a real service revision",found);
        });
    }

    @Test
    public void researcherSubmitsRequestFromPublishedService() throws Exception {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File credentials = new File(context.getFilesDir(), "catalog-test-password");
        assumeTrue("Requires the generated synthetic password", credentials.isFile());
        String password = new String(Files.readAllBytes(credentials.toPath()),
                java.nio.charset.StandardCharsets.UTF_8).trim();
        SessionVault vault = new SessionVault(context);
        String originalSession = vault.read();
        try {
            withSyntheticApi(scenario -> {
                scenario.onActivity(activity -> signIn(activity, "demo.student@example.test", password));
                waitFor(scenario, "Services");
                scenario.onActivity(activity -> click(activity, "Services"));
                waitFor(scenario, "Demo Simple Service");
                scenario.onActivity(activity -> {
                    click(activity, "Demo Simple Service");
                    click(activity, "Submit Request");
                    EditText subject = findEditor(activity.getWindow().getDecorView(), "Request subject");
                    assertNotNull(subject);
                    subject.setText("Synthetic request created by Android");
                    click(activity, "Submit request");
                });
                waitFor(scenario, "Request submitted");
            });
        } finally {
            vault.clear();
            if (originalSession != null) vault.save(originalSession);
        }
    }

    @Test
    public void researcherRegistersStudyFromProfile() throws Exception {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File credentials = new File(context.getFilesDir(), "catalog-test-password");
        assumeTrue("Requires the generated synthetic password", credentials.isFile());
        String password = new String(Files.readAllBytes(credentials.toPath()),
                java.nio.charset.StandardCharsets.UTF_8).trim();
        SessionVault vault = new SessionVault(context);
        String originalSession = vault.read();
        SharedPreferences drafts = context.getSharedPreferences("crm-registration-drafts", 0);
        String key = "4"; // The fixture seeds the demo student after the three baseline accounts.
        String originalDraft = drafts.getString(key, null);
        drafts.edit().remove(key).commit();
        try {
            withSyntheticApi(scenario -> {
                scenario.onActivity(activity -> signIn(activity, "demo.student@example.test", password));
                waitFor(scenario, "Profile");
                scenario.onActivity(activity -> {
                    click(activity, "Profile");
                    click(activity, "Start a draft");
                });
                scenario.onActivity(activity -> {
                    View screen = activity.getWindow().getDecorView();
                    EditText title = findEditor(screen, "Enter the complete research title");
                    EditText adviser = findEditor(screen, "Enter adviser name");
                    assertNotNull(title);
                    assertNotNull(adviser);
                    title.setText("Synthetic Android Registration");
                    adviser.setText("Synthetic Adviser");
                    assertTrue(selectSpinnerOption(screen, "AY 2026-2027"));
                    click(activity, "Continue");
                    click(activity, "Continue");
                    click(activity, "Continue");
                });
                waitFor(scenario, "Review Registration");
                scenario.onActivity(activity -> click(activity, "Confirm registration"));
                waitFor(scenario, "Research registered");
            });
        } finally {
            if (originalDraft == null) drafts.edit().remove(key).commit();
            else drafts.edit().putString(key, originalDraft).commit();
            vault.clear();
            if (originalSession != null) vault.save(originalSession);
        }
    }

    @Test
    public void staffUpdatesStageAndResearcherSeesSavedProgress() throws Exception {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File credentials = new File(context.getFilesDir(), "catalog-test-password");
        assumeTrue("Requires the generated synthetic password", credentials.isFile());
        String password = new String(Files.readAllBytes(credentials.toPath()),
                java.nio.charset.StandardCharsets.UTF_8).trim();
        SessionVault vault = new SessionVault(context);
        String originalSession = vault.read();
        try {
            String title = "Synthetic progress " + Long.toString(System.nanoTime(), 36);
            withSyntheticApi(scenario -> {
                String origin = InstrumentationRegistry.getArguments().getString("catalogOrigin");
                org.json.JSONObject login = CrmApi.post(origin, "/api/v1/auth/login",
                        new org.json.JSONObject().put("email", "demo.student@example.test").put("password", password), null);
                String token = login.getString("access_token");
                try {
                    CrmApi.post(origin, "/api/v1/researcher/registrations",
                            new org.json.JSONObject().put("submission_token", java.util.UUID.randomUUID().toString())
                                    .put("title", title).put("college", "Demo College")
                                    .put("academic_period", "AY 2026-2027").put("adviser", "Demo Adviser"), token);
                } finally {
                    CrmApi.post(origin, "/api/v1/auth/logout", new org.json.JSONObject(), token);
                }
                scenario.onActivity(activity -> signIn(activity, "demo.progress@example.test", password));
                waitFor(scenario, "Registry");
                scenario.onActivity(activity -> {
                    click(activity, "Registry");
                    click(activity, "Research progress");
                });
                waitForSuffix(scenario, " · " + title);
                scenario.onActivity(activity -> clickSuffix(activity, " · " + title));
                waitFor(scenario, "Update overall stage");
                scenario.onActivity(activity -> {
                    click(activity, "Update overall stage");
                    View root = activity.getWindow().getDecorView();
                    findEditor(root, "Effective date (YYYY-MM-DD)").setText("2026-11-01");
                    findEditor(root, "Supporting reference").setText("DEMO-STAGE-ANDROID");
                    findEditor(root, "Next action").setText("Prepare final manuscript");
                    click(activity, "Save stage update");
                });
                waitFor(scenario, "Current overall stage: In progress");
                captureLiveScreen("part8-live-progress");
                waitFor(scenario, "Configure research workflow");
                scenario.onActivity(activity -> {
                    click(activity, "Configure research workflow");
                    View root = activity.getWindow().getDecorView();
                    String[] codes = {"proposal", "ethics", "collection", "manuscript", "output"};
                    for (int i = 0; i < codes.length; i++) {
                        findEditor(root, "Estimated work minutes · " + codes[i]).setText("30");
                        if (i > 0) findEditor(root, "Prerequisite milestone codes, comma separated · " + codes[i])
                                .setText(codes[i - 1]);
                    }
                    findEditor(root, "Evidence gates (code:received or code:verified), comma separated · ethics")
                            .setText("ethics:verified");
                    click(activity, "Save workflow");
                });
                waitFor(scenario, "Estimated remaining work: 150 minutes");
                scenario.onActivity(activity -> {
                    click(activity, "Edit milestones");
                    click(activity, "Proposal submission and defense · Pending");
                    View root = activeRoot(activity);
                    assertTrue(selectSpinnerOption(root, "Completed"));
                    click(activity, "Save milestone");
                });
                waitFor(scenario, "Estimated remaining work: 120 minutes");
                waitFor(scenario, "Record verified evidence for Ethics and consent clearance.");
                scenario.onActivity(activity -> {
                    click(activity, "Edit milestones");
                    click(activity, "Ethics and consent clearance · Pending");
                    View root = activeRoot(activity);
                    findEditor(root, "Evidence reference").setText("DEMO-ETHICS-ANDROID");
                    assertTrue(selectSpinnerOption(root, "Verified"));
                    click(activity, "Save milestone");
                });
                waitFor(scenario, "Ready to start");
            });
            vault.clear();
            withSyntheticApi(scenario -> {
                scenario.onActivity(activity -> signIn(activity, "demo.student@example.test", password));
                waitFor(scenario, "Profile");
                scenario.onActivity(activity -> {
                    click(activity, "Profile");
                    click(activity, "Registered research");
                });
                waitForSuffix(scenario, " · " + title);
                scenario.onActivity(activity -> clickSuffix(activity, " · " + title));
                waitFor(scenario, "Next action: Prepare final manuscript");
                waitFor(scenario, "Estimated remaining work: 120 minutes");
                waitFor(scenario, "Ready to start");
            });
        } finally {
            vault.clear();
            if (originalSession != null) vault.save(originalSession);
        }
    }

    private static View activeRoot(MainActivity activity){
        if(android.os.Build.VERSION.SDK_INT>=29){var windows=android.view.inspector.WindowInspector.getGlobalWindowViews();for(int i=windows.size()-1;i>=0;i--)if(windows.get(i).isShown())return windows.get(i);}
        return activity.getWindow().getDecorView();
    }

    private static boolean selectSpinnerOption(View root, String option) {
        if (root instanceof android.widget.Spinner spinner && spinner.getAdapter() != null) {
            for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
                if (option.equals(String.valueOf(spinner.getAdapter().getItem(i)))) {
                    spinner.setSelection(i);
                    return true;
                }
            }
        }
        if (root instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                if (selectSpinnerOption(group.getChildAt(i), option)) return true;
            }
        }
        return false;
    }

    private static void signIn(MainActivity activity, String username, String password) {
        click(activity, "Get Started");
        View screen = activity.getWindow().getDecorView();
        EditText email = findEditor(screen, "e.g. jdelacruz.K1234@umak.edu.ph");
        EditText secret = findEditor(screen, "Enter your password");
        assertNotNull(email);
        assertNotNull(secret);
        email.setText(username);
        secret.setText(password);
        View submit = find(screen, "Sign In", true);
        assertNotNull(submit);
        assertTrue(submit.performClick());
    }

    @Test
    public void reportingStaffGeneratesStoredReportFromManage() throws Exception {
        withIdentity("demo.reporting@example.test", scenario -> {
            scenario.onActivity(activity -> { click(activity, "Manage"); click(activity, "Analytics & Reporting"); });
            waitFor(scenario, "Generate report");
            scenario.onActivity(activity -> click(activity, "Generate report"));
            waitFor(scenario, "Underlying records");
            captureLiveScreen("part9-live-research-report");
            scenario.onActivity(activity -> {
                assertNotNull(find(activity.getWindow().getDecorView(), "Export"));
                assertNull(find(activity.getWindow().getDecorView(), "User Accounts"));
                click(activity,"Back to Filters");click(activity,"Service utilization report");click(activity,"Generate report");
            });
            waitFor(scenario,"Underlying requests");captureLiveScreen("part9-live-service-report");
        });
    }

    @Test public void registryStaffCreatesAndCorrectsRealDesignedRecord() throws Exception {
        String title="Staff encoded "+Long.toString(System.nanoTime(),36);
        withIdentity("demo.registry@example.test",scenario->{
            scenario.onActivity(activity->{click(activity,"Registry");click(activity,"Registered research");});
            waitFor(scenario,"Add Research Entry");
            scenario.onActivity(activity->{click(activity,"Add Research Entry");View root=activity.getWindow().getDecorView();findEditor(root,"Research title").setText(title);findEditor(root,"College or institute").setText("Demo College");findEditor(root,"Adviser").setText("Verified adviser");findEditor(root,"Academic period (AY 2026-2027)").setText("AY 2026-2027");findEditor(root,"Correction or entry reason").setText("Staff verified intake");click(activity,"Add existing researcher");});
            scenario.onActivity(activity->{findEditor(activeRoot(activity),"Institutional email").setText("demo.student@example.test");click(activity,"Find");});
            waitFor(scenario,"Demo Student · Remove");
            scenario.onActivity(activity->click(activity,"Create record"));waitFor(scenario,title);
            scenario.onActivity(activity->{click(activity,"Edit Record");View root=activity.getWindow().getDecorView();findEditor(root,"Research title").setText(title+" corrected");findEditor(root,"Correction or entry reason").setText("Corrected title against signed source");click(activity,"Save correction");});
            waitFor(scenario,title+" corrected");captureLiveScreen("part7-live-corrected-record");
        });
    }

    @Test
    public void registryStaffLoadsStoredResearchRecord() throws Exception {
        withIdentity("demo.registry@example.test", scenario -> {
            scenario.onActivity(activity -> { click(activity, "Registry"); click(activity, "Registered research"); });
            waitFor(scenario,"Demo Research A");
            scenario.onActivity(activity -> click(activity,"Demo Research A"));
            waitFor(scenario,"Edit Record");
            captureLiveScreen("part7-live-record");
            scenario.onActivity(activity -> click(activity,"Manage"));
            waitFor(scenario,"Attach file");
        });
    }

    @Test
    public void administratorLoadsActualAccountDirectory() throws Exception {
        withIdentity("demo.admin@example.test", scenario -> {
            scenario.onActivity(activity -> { click(activity, "Manage"); click(activity, "User Accounts"); });
            waitFor(scenario, "Create Staff Account");
            scenario.onActivity(activity -> click(activity, "Demo Student"));
            waitFor(scenario, "demo.student@example.test");
            captureLiveScreen("part5-live-account");
        });
    }

    @Test
    public void processingStaffSavesReviewFromNormalRequestsTab() throws Exception {
        withIdentity("demo.processing@example.test", scenario -> {
            String origin = InstrumentationRegistry.getArguments().getString("catalogOrigin");
            var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            String password = new String(Files.readAllBytes(new File(context.getFilesDir(), "catalog-test-password").toPath()),
                    java.nio.charset.StandardCharsets.UTF_8).trim();
            String token = CrmApi.post(origin, "/api/v1/auth/login", new org.json.JSONObject()
                    .put("email", "demo.student@example.test").put("password", password), null).getString("access_token");
            int requestId;
            String serviceName = "";
            try {
                org.json.JSONArray catalog = new org.json.JSONArray(CrmApi.get(origin, "/api/v1/public/services", null));
                org.json.JSONObject service = null;
                for (int i = 0; i < catalog.length(); i++) if ("SVC-DEMO-SIMPLE".equals(catalog.getJSONObject(i).getString("code"))) service = catalog.getJSONObject(i);
                assertNotNull(service);
                serviceName = service.getJSONObject("details").getString("name");
                requestId = CrmApi.post(origin, "/api/v1/service-requests", new org.json.JSONObject()
                        .put("revision_id", service.getInt("id")).put("subject", "Android staff review")
                        .put("submission_token", java.util.UUID.randomUUID().toString()), token).getInt("id");
            } finally { CrmApi.post(origin, "/api/v1/auth/logout", new org.json.JSONObject(), token); }
            scenario.onActivity(activity -> click(activity, "Requests"));
            waitFor(scenario, "Review Queue");
            waitFor(scenario, "Review queue updated.");
            captureLiveScreen("part6-live-queue");
            scenario.onActivity(activity -> {
                View root = activity.getWindow().getDecorView();
                // The designed queue keeps separate status tabs; the new request is Submitted.
                View submitted = findPrefix(root, "Submitted (");
                assertNotNull(submitted);
                submitted.performClick();
                click(activity, "REQ-" + requestId);
            });
            waitFor(scenario, "Save Review");
            scenario.onActivity(activity -> {
                assertNotNull(find(activity.getWindow().getDecorView(), "Request Workspace"));
                findEditor(activity.getWindow().getDecorView(), "Researcher-visible feedback").setText("Review started from Android");
                click(activity, "Save Review");
            });
            waitFor(scenario, "Review saved.");
            waitFor(scenario, "Feedback: Review started from Android");
            captureLiveScreen("part6-live-workspace");
            scenario.onActivity(activity -> {
                assertNotNull(find(activity.getWindow().getDecorView(), "Current status: In review"));
                View completed = find(activity.getWindow().getDecorView(), "Completed");
                assertNotNull(completed);
                completed.performClick();
                assertTrue(((android.widget.RadioButton) completed).isChecked());
                click(activity, "Save Review");
            });
            waitFor(scenario, "Review saved. The request is completed.");
            waitFor(scenario, "Save Correction");
            scenario.onActivity(activity -> {
                View root = activity.getWindow().getDecorView();
                findEditor(root, "Reason for correcting this completed request").setText("Correct the final response wording");
                findEditor(root, "Researcher-visible feedback").setText("Final response corrected from Android");
                click(activity, "Save Correction");
            });
            waitFor(scenario, "Review saved. The request is completed.");
            scenario.onActivity(activity -> {
                assertNotNull(findPrefix(activity.getWindow().getDecorView(), "Feedback: Final response corrected from Android"));
            });
            JSONObjectSessionCheck.verifyLatestReview(origin, context, requestId);
            captureLiveScreen("part6-live-completed-correction");
            scenario.onActivity(activity -> {
                activity.onBackPressed();
                assertNotNull(find(activity.getWindow().getDecorView(), "Review Queue"));
            });
        });
    }

    private static void captureLiveScreen(String name) throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(250);
        try (java.io.InputStream stream = new android.os.ParcelFileDescriptor.AutoCloseInputStream(
                InstrumentationRegistry.getInstrumentation().getUiAutomation()
                        .executeShellCommand("screencap -p /data/local/tmp/" + name + ".png"))) {
            byte[] buffer = new byte[1024];
            while (stream.read(buffer) != -1) { /* Wait for screenshot completion. */ }
        }
    }

    private static final class JSONObjectSessionCheck {
        static void verifyLatestReview(String origin, android.content.Context context, int requestId) throws Exception {
            org.json.JSONObject session = new org.json.JSONObject(new SessionVault(context).read());
            org.json.JSONObject detail = new org.json.JSONObject(CrmApi.get(origin,
                    "/api/v1/staff/service-requests/" + requestId, session.getString("token")));
            org.json.JSONObject latest = detail.getJSONArray("reviews").getJSONObject(0);
            org.junit.Assert.assertEquals("Completed", detail.getString("status"));
            org.junit.Assert.assertEquals("Final response corrected from Android", latest.getString("feedback"));
            org.junit.Assert.assertEquals("Correct the final response wording", latest.getString("correction_reason"));
        }
    }

    private static View findPrefix(View view, String prefix) {
        if (view instanceof android.widget.TextView text && text.getText().toString().startsWith(prefix)) return view;
        if (view instanceof android.view.ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) {
            View match = findPrefix(group.getChildAt(i), prefix); if (match != null) return match;
        }
        return null;
    }

    private static void withIdentity(String email, Journey journey) throws Exception {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File credentials = new File(context.getFilesDir(), "catalog-test-password");
        assumeTrue("Requires the generated synthetic password", credentials.isFile());
        String password = new String(Files.readAllBytes(credentials.toPath()), java.nio.charset.StandardCharsets.UTF_8).trim();
        SessionVault vault = new SessionVault(context);
        String previous = vault.read();
        try {
            withSyntheticApi(scenario -> {
                scenario.onActivity(activity -> signIn(activity, email, password));
                waitFor(scenario, "Manage");
                journey.run(scenario);
            });
        } finally { vault.clear(); if (previous != null) vault.save(previous); }
    }

    @Test
    public void hostedDemoSupportsSignupVerificationAndSignIn() throws Exception {
        String origin = InstrumentationRegistry.getArguments().getString("hostedOrigin");
        assumeTrue("Requires the deployed hosted demo", origin != null);
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File credentials = new File(context.getFilesDir(), "hosted-test-password");
        assumeTrue("Requires the private hosted demo credential", credentials.isFile());
        String password = new String(Files.readAllBytes(credentials.toPath()), java.nio.charset.StandardCharsets.UTF_8).trim();
        String email = "student.demo" + System.currentTimeMillis() + "@umak.edu.ph";
        CrmApi.post(origin, "/api/v1/auth/researchers/register", new org.json.JSONObject()
                .put("email", email).put("password", password).put("confirm_password", password)
                .put("display_name", "Hosted Android Demo").put("college", "Demo College").put("role", "student"), null);
        var login = (java.net.HttpURLConnection) new java.net.URL(origin + "/demo/inbox/login").openConnection();
        login.setInstanceFollowRedirects(false); login.setRequestMethod("POST"); login.setDoOutput(true);
        login.setConnectTimeout(60000); login.setReadTimeout(60000);
        login.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (var output = login.getOutputStream()) {
            output.write(("password=" + java.net.URLEncoder.encode(password, java.nio.charset.StandardCharsets.UTF_8)).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        org.junit.Assert.assertEquals(303, login.getResponseCode()); String cookie = login.getHeaderField("Set-Cookie").split(";", 2)[0]; login.disconnect();
        var inbox = (java.net.HttpURLConnection) new java.net.URL(origin + "/demo/inbox").openConnection();
        inbox.setConnectTimeout(60000); inbox.setReadTimeout(60000); inbox.setRequestProperty("Cookie", cookie);
        String html;
        try (var input = inbox.getInputStream()) { html = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8); }
        finally { inbox.disconnect(); }
        String ownMessage = html.split(java.util.regex.Pattern.quote("<h2>" + email + "</h2>"), 2)[1];
        var match = java.util.regex.Pattern.compile("verification code is ([0-9]{6})").matcher(ownMessage);
        assertTrue(match.find());
        CrmApi.post(origin, "/api/v1/auth/researchers/verify", new org.json.JSONObject().put("email", email).put("code", match.group(1)), null);
        SharedPreferences preferences = context.getSharedPreferences("crm-settings", 0);
        String previousOrigin = preferences.getString("server", "");
        SessionVault vault = new SessionVault(context); String previousSession = vault.read(); vault.clear();
        preferences.edit().putString("server", origin).commit();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> signIn(activity, email, password));
            waitFor(scenario, "Services");
            scenario.onActivity(activity -> click(activity, "Services"));
            waitFor(scenario, "Demo Service A");
        } finally {
            vault.clear(); if (previousSession != null) vault.save(previousSession);
            preferences.edit().putString("server", previousOrigin).commit();
        }
    }

    @Test
    public void hostedSeededStudentCanSignIn() throws Exception {
        String origin = InstrumentationRegistry.getArguments().getString("hostedOrigin");
        assumeTrue("Requires the hosted Render demo build", origin != null && BuildConfig.HOSTED_DEMO);
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File credentials = new File(context.getFilesDir(), "hosted-test-password");
        assumeTrue("Requires the private hosted demo credential", credentials.isFile());
        String password = new String(Files.readAllBytes(credentials.toPath()), java.nio.charset.StandardCharsets.UTF_8).trim();
        SharedPreferences settings = context.getSharedPreferences("crm-settings", 0);
        settings.edit().putString("server", "https://127.0.0.1:1").commit();
        new SessionVault(context).clear();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            org.junit.Assert.assertEquals(origin, settings.getString("server", ""));
            scenario.onActivity(activity -> signIn(activity, "demo.student@umak.edu.ph", password));
            waitFor(scenario, "Services");
            scenario.onActivity(activity -> {
                assertNull(find(activity.getWindow().getDecorView(), "P"));
                click(activity, "Services");
            });
            waitFor(scenario, "Demo Service A");
        } finally { new SessionVault(context).clear(); }
    }

    private interface Journey {
        void run(ActivityScenario<MainActivity> scenario) throws Exception;
    }

    private static void withSyntheticApi(Journey journey) throws Exception {
        var args = InstrumentationRegistry.getArguments();
        assumeTrue("Requires the synthetic HTTPS API and its certificate",
                args.containsKey("catalogOrigin") && args.containsKey("catalogCert"));
        var cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(
                android.util.Base64.decode(args.getString("catalogCert"), android.util.Base64.DEFAULT)));
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null);
        store.setCertificateEntry("catalog-fixture", cert);
        var trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trust.init(store);
        var ssl = SSLContext.getInstance("TLS");
        ssl.init(null, trust.getTrustManagers(), null);
        var previous = HttpsURLConnection.getDefaultSSLSocketFactory();
        HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());
        SharedPreferences preferences = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getSharedPreferences("crm-settings", 0);
        String originalOrigin = preferences.getString("server", "");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> preferences.edit()
                    .putString("server", args.getString("catalogOrigin")).commit());
            journey.run(scenario);
        } finally {
            preferences.edit().putString("server", originalOrigin).commit();
            HttpsURLConnection.setDefaultSSLSocketFactory(previous);
        }
    }

    private static void waitForSuffix(ActivityScenario<MainActivity> scenario, String suffix) {
        boolean[] found = {false};
        for (int attempt = 0; attempt < 35; attempt++) {
            scenario.onActivity(activity -> found[0] = findSuffix(activity.getWindow().getDecorView(), suffix) != null);
            if (found[0]) return;
            SystemClock.sleep(300);
        }
        assertTrue("Expected record ending with " + suffix, false);
    }

    private static void clickSuffix(MainActivity activity, String suffix) {
        View view = findSuffix(activity.getWindow().getDecorView(), suffix);
        assertNotNull(suffix, view);
        while (!view.isClickable() && view.getParent() instanceof View parent) view = parent;
        assertTrue(view.performClick());
    }

    private static View findSuffix(View root, String suffix) {
        if (root instanceof TextView text && text.getText().toString().endsWith(suffix)) return root;
        if (root instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = findSuffix(group.getChildAt(i), suffix);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void waitFor(ActivityScenario<MainActivity> scenario, String label) {
        boolean[] found = {false};
        for (int attempt = 0; attempt < 35; attempt++) {
            scenario.onActivity(activity -> found[0] = find(activity.getWindow().getDecorView(), label) != null);
            if (found[0]) return;
            SystemClock.sleep(300);
        }
        assertTrue("Expected " + label, false);
    }

    private static EditText findEditor(View root, String hint) {
        if (root instanceof EditText edit && (hint.equals(String.valueOf(edit.getHint()))
                || hint.equals(String.valueOf(edit.getContentDescription())))) return edit;
        if (root instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                EditText child = findEditor(group.getChildAt(i), hint);
                if (child != null) return child;
            }
        }
        return null;
    }

    private static void click(MainActivity activity, String label) {
        View view = find(activeRoot(activity), label);
        if(view==null)view=find(activity.getWindow().getDecorView(),label);
        assertNotNull(label, view);
        while (!view.isClickable() && view.getParent() instanceof View parent) view = parent;
        assertTrue(label, view.performClick());
    }

    private static View find(View root, String label) {
        return find(root, label, false);
    }

    private static View find(View root, String label, boolean clickable) {
        if (!clickable || root.isClickable()) {
            if (label.equals(root.getContentDescription())) return root;
            if (root instanceof TextView text && label.contentEquals(text.getText())) return root;
        }
        if (root instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                View result = find(group.getChildAt(i), label, clickable);
                if (result != null) return result;
            }
        }
        return null;
    }
}
