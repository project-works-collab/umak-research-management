package ph.edu.umak.crm.app;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.common.dialogs.SignInGateDialog;
import ph.edu.umak.crm.app.requests.CorrectRequestView;
import ph.edu.umak.crm.app.requests.MyRequestsView;
import ph.edu.umak.crm.app.requests.NotificationsView;
import ph.edu.umak.crm.app.requests.RequestData;
import ph.edu.umak.crm.app.requests.RequestDetailView;
import ph.edu.umak.crm.app.requests.RequestFilterDialog;
import ph.edu.umak.crm.app.requests.RequestReceiptView;
import ph.edu.umak.crm.app.requests.SubmitRequestView;
import ph.edu.umak.crm.app.research.AddCoAuthorDialog;
import ph.edu.umak.crm.app.research.EditProfileView;
import ph.edu.umak.crm.app.research.ProfileView;
import ph.edu.umak.crm.app.research.RegistrationConfirmationView;
import ph.edu.umak.crm.app.research.RegistrationStep1View;
import ph.edu.umak.crm.app.research.ResearchData;
import ph.edu.umak.crm.app.research.ResearchProgressView;
import ph.edu.umak.crm.app.services.AppNavigationGuidanceView;
import ph.edu.umak.crm.app.services.ExternalSubmissionView;
import ph.edu.umak.crm.app.services.FormFailureDialog;
import ph.edu.umak.crm.app.services.ServiceData;
import ph.edu.umak.crm.app.services.ServiceDetailView;
import ph.edu.umak.crm.app.services.ServiceDirectoryView;

final class ResearcherScreen extends LinearLayout {
    private static final int NAVY = 0xff131c4b;
    private static final int BLUE = 0xff082d50;
    private static final int ACCENT = 0xff185d9d;
    private static final int MUTED = 0xff586b83;
    private static final int PAPER = 0xfff5f6f7;
    private static final int WHITE = 0xffffffff;
    private static final int BORDER = 0xffe4e8ee;
    private static final int PALE = 0xffe5edf7;
    private static final int YELLOW = 0xfff8eb37;
    private static final String[] TABS = {"Dashboard", "Services", "Requests", "Research", "Profile"};
    private static final int[] ICONS = {R.drawable.ic_staff_dashboard, R.drawable.ic_researcher_services,
            R.drawable.ic_staff_requests, R.drawable.ic_researcher_search, R.drawable.ic_staff_profile};

    private final MainActivity activity;
    private final JSONObject account;
    private final Typeface serif;
    private final Typeface regular;
    private final Typeface bold;
    private final LinearLayout body;
    private final LinearLayout navigation;
    private final Button back;
    private Runnable previousRegistrationStep;
    private final ScrollView scroll;
    private String tab;
    private String detail = "";
    private String detailParent = "";
    private JSONArray studies;
    private JSONArray consultations;
    private String studyError;
    private final java.util.Map<String, String> registration = new java.util.HashMap<>();
    private int registrationStep;
    private int searchGeneration;
    private JSONObject reviewStudy;

    private ServiceDirectoryView serviceDirectoryView;
    private ServiceDetailView serviceDetailView;
    private ServiceData.ServiceItem selectedServiceItem;
    private boolean serviceFormUnavailable;

    private MyRequestsView myRequestsView;
    private SubmitRequestView submitRequestView;
    private RequestDetailView requestDetailView;
    private RequestReceiptView requestReceiptView;
    private CorrectRequestView correctRequestView;
    private NotificationsView notificationsView;
    private RequestData.RequestItem selectedRequestItem;
    private boolean receiptIsCorrection = false;

    private ProfileView profileView;
    private EditProfileView editProfileView;
    private RegistrationStep1View registrationStep1View;
    private RegistrationConfirmationView registrationConfirmationView;
    private ResearchProgressView researchProgressView;
    private ResearchData.UserProfile userProfile;
    private String selectedProgressState = "4.6c";
    private boolean profileUpdatedSnackbar = false;

    ResearcherScreen(MainActivity activity, JSONObject account, String initialTab) {
        super(activity);
        this.activity = activity;
        this.account = account;
        tab = java.util.Arrays.asList(TABS).contains(initialTab) ? initialTab : "Dashboard";

        String accountName = name();
        String accountEmail = account != null ? account.optString("email", "ljames.A1234@umak.edu.ph") : "ljames.A1234@umak.edu.ph";
        String accountRole = account != null && "faculty".equals(account.optString("role")) ? "Faculty Researcher" : "Student Researcher";
        String accountCategory = account != null && "faculty".equals(account.optString("role")) ? "Faculty" : "Student";
        userProfile = new ResearchData.UserProfile(
                accountName.isEmpty() ? "LeBron R. James" : accountName,
                accountRole,
                accountCategory,
                "BS CS",
                "3rd Year",
                "College of Computing and Information Sciences",
                accountEmail.isEmpty() ? "ljames.A1234@umak.edu.ph" : accountEmail,
                "A1234",
                "Active",
                "This session"
        );
        serif = getResources().getFont(R.font.marcellus_regular);
        regular = getResources().getFont(R.font.metropolis_regular);
        bold = getResources().getFont(R.font.metropolis_bold);
        setOrientation(VERTICAL);
        setBackgroundColor(PAPER);
        LinearLayout header = new LinearLayout(activity);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(NAVY);
        header.setPadding(dp(16), dp(4), dp(8), dp(4));
        TextView title = text("Center for Research Management", 17, WHITE, serif);
        header.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        ImageView bell = new ImageView(activity);
        bell.setImageResource(R.drawable.ic_researcher_bell);
        bell.setScaleType(ImageView.ScaleType.CENTER);
        bell.setContentDescription("Notifications");
        bell.setOnClickListener(view -> openDetail("notifications"));
        header.addView(bell, new LayoutParams(dp(48), dp(48)));
        addView(header);
        back = BackControl.create(activity, "Back", NAVY, this::navigateBack);
        back.setBackgroundColor(WHITE);
        addView(back);
        scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        body = column();
        scroll.addView(body);
        addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        View line = new View(activity);
        line.setBackgroundColor(BORDER);
        addView(line, new LayoutParams(LayoutParams.MATCH_PARENT, dp(1)));
        navigation = new LinearLayout(activity);
        navigation.setBackgroundColor(WHITE);
        addView(navigation);
    }

    static View support(MainActivity activity) {
        LinearLayout root = new LinearLayout(activity);
        root.setBackgroundColor(PAPER);
        ScrollView scroll = new ScrollView(activity);
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(VERTICAL);
        int margin = Math.round(24 * activity.getResources().getDisplayMetrics().density);
        content.setPadding(margin, margin * 3, margin, margin);
        scroll.addView(content);
        root.addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView title = new TextView(activity);
        title.setTypeface(activity.getResources().getFont(R.font.marcellus_regular));
        title.setTextSize(30);
        title.setTextColor(NAVY);
        title.setText("Account Support &\nOffline Access");
        content.addView(title);
        TextView copy = new TextView(activity);
        copy.setTextColor(MUTED);
        copy.setPadding(0, margin, 0, margin * 2);
        copy.setText("Please contact the Center for Research Management (CRM) via email or visit the office within office hours.\n\n"
                + "Email Address\nresearch@umak.edu.ph\n\nOffice Location\n9th Floor, Health and Physical Science Building, University of Makati, JP Rizal Ext., West Rembo, Makati City");
        content.addView(copy);
        Button back = new Button(activity);
        back.setText("Go Back to Sign-In");
        back.setAllCaps(false);
        back.setOnClickListener(view -> activity.showSignIn());
        content.addView(back);
        return root;
    }

    boolean isSignedIn() { return account != null; }
    String selectedTab() { return tab; }

    void openInitialTab() {
        render();
        if (isSignedIn()) loadStudies();
    }

    private void navigateBack() {
        if (!goBack()) {
            if (!isSignedIn()) activity.leaveGuestPortal();
            else if (activity.isResearcherPreview()) activity.finish();
        }
    }

    boolean goBack() {
        if ("registration".equals(detail) && previousRegistrationStep != null) {
            previousRegistrationStep.run();
            return true;
        }
        if ("correct_request".equals(detail)) {
            detail = "request_detail";
            render();
            return true;
        }
        if ("edit_profile".equals(detail) || "research_progress".equals(detail)) {
            detail = "";
            tab = "Profile";
            render();
            return true;
        }
        if ("registration_confirmation".equals(detail)) {
            detail = "";
            tab = "Dashboard";
            render();
            return true;
        }
        if ("external_submission".equals(detail) || "app_navigation".equals(detail)) {
            detail = "service_detail";
            render();
            return true;
        }
        if (!detail.isEmpty()) {
            tab = detailParent;
            documents.remove("revision");
            detail = "";
        } else if (!"Dashboard".equals(tab)) {
            tab = "Dashboard";
        } else {
            return false;
        }
        render();
        return true;
    }

    private void select(String selected) {
        if (!isSignedIn() && ("Requests".equals(selected) || "Profile".equals(selected))) {
            activity.showSignIn();
            return;
        }
        tab = selected;
        documents.remove("revision");
        detail = "";
        render();
    }

    private void openDetail(String destination) {
        if (detail.isEmpty()) detailParent = tab;
        detail = destination;
        if ("guidelines".equals(destination) || "service_detail".equals(destination)
                || "external_submission".equals(destination) || "app_navigation".equals(destination)) {
            tab = "Services";
        } else if ("edit_profile".equals(destination) || "research_progress".equals(destination)) {
            tab = "Profile";
        } else if ("registration".equals(destination) || "registration_confirmation".equals(destination)
                || "review".equals(destination) || "manuscript".equals(destination)) {
            tab = "Research";
        } else if (!"announcements".equals(destination) && !"notifications".equals(destination)) {
            tab = "Requests";
        }
        render();
    }

    private void render() {
        previousRegistrationStep = null;
        back.setVisibility(!detail.isEmpty() || ((!isSignedIn() || activity.isResearcherPreview()) && "Dashboard".equals(tab)) ? VISIBLE : GONE);
        String backLabel = "registration".equals(detail) && registrationStep > 0 ? "Previous step"
                : detail.isEmpty() ? (isSignedIn() ? "Exit researcher preview" : "Back to sign-in")
                : "Back to " + detailParent;
        back.setText("‹  " + backLabel);
        back.setContentDescription(backLabel);
        body.removeAllViews();
        documentLabels.clear();
        drawNavigation();
        scroll.scrollTo(0, 0);
        switch (detail) {
            case "announcements": announcements(); return;
            case "guidelines": guidelines(); return;
            case "service_detail": renderServiceDetail(); return;
            case "external_submission": renderExternalSubmission(); return;
            case "app_navigation": renderAppNavigation(); return;
            case "request": renderSubmitRequest(); return;
            case "request_detail": renderRequestDetail(); return;
            case "request_receipt": renderRequestReceipt(); return;
            case "correct_request": renderCorrectRequest(); return;
            case "notifications": renderNotifications(); return;
            case "edit_profile": renderEditProfile(); return;
            case "registration_confirmation": renderRegistrationConfirmation(); return;
            case "research_progress": renderResearchProgress(); return;
            case "manuscript": manuscript(); return;
            case "registration": registration(); return;
            case "review": reviewManuscript(); return;
            case "consultation": consultation(); return;
            default: break;
        }
        switch (tab) {
            case "Services": services(); break;
            case "Requests": requests(); break;
            case "Research": archive(); break;
            case "Profile": profile(); break;
            default:
                if (isSignedIn()) dashboard();
                else landing();
        }
    }

    private void drawNavigation() {
        navigation.removeAllViews();
        for (int index = 0; index < TABS.length; index++) {
            String name = TABS[index];
            boolean selected = name.equals(tab);
            LinearLayout item = column();
            item.setGravity(Gravity.CENTER);
            item.setMinimumHeight(dp(64));
            item.setContentDescription(name);
            item.setSelected(selected);
            item.setOnClickListener(view -> select(name));
            ImageView icon = new ImageView(activity);
            icon.setImageResource(ICONS[index]);
            icon.setColorFilter(selected ? NAVY : MUTED);
            icon.setScaleType(ImageView.ScaleType.CENTER);
            if (selected) icon.setBackground(shape(PALE, 30, 0));
            item.addView(icon, new LayoutParams(dp(52), dp(29)));
            TextView label = text(name, 10, selected ? NAVY : MUTED, bold);
            label.setGravity(Gravity.CENTER);
            label.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            item.addView(label);
            navigation.addView(item, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
        }
    }

    private void landing() {
        LinearLayout intro = hero("Research Management\n& Lifecycle Information\nSystem",
                "A centralized platform that streamlines research registration, service requests, lifecycle tracking, and institutional reporting across all UMAk colleges.", false);
        TextView headline = (TextView) intro.getChildAt(0);
        tintText(headline, "Lifecycle Information\nSystem", YELLOW);
        Button portal = action(intro, "Researcher Portal", YELLOW, NAVY, activity::startAccess);
        portal.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
        ((LayoutParams) portal.getLayoutParams()).topMargin = dp(16);
        LinearLayout stats = section(ACCENT);
        landingMetrics(stats);
        LinearLayout roles = section(WHITE);
        roles.setPadding(dp(16), dp(32), dp(16), dp(24));
        TextView eyebrow = paragraph(roles, "SYSTEM ROLES", 11, ACCENT, bold, 8);
        eyebrow.setLetterSpacing(0.06f);
        ScreenTypography.heading(paragraph(roles, "Built for Every Stakeholder in the Research Process",
                24, BLUE, serif, 16));
        role(roles, "Researcher", "Submit research proposals\nTrack lifecycle timeline\nRequest CRM services\nAccess repository");
        role(roles, "Faculty Adviser", "Monitor advisee progress\nReview manuscripts digitally\nSubmit endorsements");
        role(roles, "CRM Staff", "Manage service queue\nEvaluate submissions\nVerify compliance\nUpdate milestones");
        LinearLayout lifecycle = section(BLUE);
        paragraph(lifecycle, "RESEARCH LIFECYCLE", 10, YELLOW, bold);
        paragraph(lifecycle, "From Registration to Publication", 25, WHITE, serif);
        String[] stages = {"Registration", "Proposal Review", "Ethics Review", "Data Gathering", "Presentation", "Publication"};
        for (int index = 0; index < stages.length; index += 2) {
            LinearLayout row = new LinearLayout(activity);
            for (int cell = index; cell < index + 2; cell++) {
                TextView label = text(String.format(java.util.Locale.ROOT, "%02d\n%s", cell + 1, stages[cell]), 13, WHITE, bold);
                label.setPadding(dp(10), dp(12), dp(10), dp(12));
                label.setBackground(shape(0xff143959, 8, 0));
                LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
                params.setMargins(0, dp(8), dp(5), 0);
                row.addView(label, params);
            }
            lifecycle.addView(row);
        }
        LinearLayout latest = section(WHITE);
        paragraph(latest, "CRM UPDATES", 10, ACCENT, bold);
        paragraph(latest, "Latest Announcements", 24, BLUE, serif);
        publicItems(latest, "announcement");
        LinearLayout bottom = hero("Ready to Start Your\nResearch Journey?",
                "Sign in to the CRM system to register your study, submit service requests, and track progress from proposal to publication.", true);
        action(bottom, "Researcher Portal", YELLOW, NAVY, activity::startAccess);
        action(bottom, "View CRM Services", PALE, NAVY, () -> select("Services"));
    }

    private void landingMetrics(LinearLayout parent) {
        String[] labels = {"Registered Studies", "Active Researchers", "UNIVERSITY Volumes", "CRM Services"};
        String[] values = {"1,240+", "320+", "14", String.valueOf(ResearcherCatalog.SERVICES.length)};
        for (int index = 0; index < labels.length; index += 2) {
            LinearLayout row = new LinearLayout(activity);
            LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dp(16);
            parent.addView(row, rowParams);
            for (int cell = index; cell < index + 2; cell++) {
                LinearLayout metric = column();
                metric.setPadding(dp(4), 0, dp(4), 0);
                row.addView(metric, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
                paragraph(metric, values[cell], 24, WHITE, bold, 4).setGravity(Gravity.CENTER);
                paragraph(metric, labels[cell], 12, 0xffe5edf7, regular, 0).setGravity(Gravity.CENTER);
            }
        }
        paragraph(parent, "Sample figures", 11, 0xffe5edf7, regular, 0).setGravity(Gravity.CENTER);
    }

    private static void tintText(TextView view, String phrase, int color) {
        String original = view.getText().toString();
        int start = original.indexOf(phrase);
        if (start < 0) return;
        android.text.SpannableString text = new android.text.SpannableString(original);
        text.setSpan(new android.text.style.ForegroundColorSpan(color), start, start + phrase.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(text);
    }

    private void role(LinearLayout parent, String title, String description) {
        LinearLayout card = card(parent);
        ScreenTypography.heading(paragraph(card, title, 18, BLUE, bold));
        paragraph(card, "• " + description.replace("\n", "\n• "), 14, MUTED, regular);
        TextView signIn = paragraph(card, "Sign In", 14, ACCENT, bold, 0);
        signIn.setMinimumHeight(dp(48));
        signIn.setGravity(Gravity.CENTER_VERTICAL);
        signIn.setOnClickListener(view -> activity.startAccess());
    }

    private void dashboard() {
        LinearLayout welcome = hero("Welcome back,\n" + name(), "", false);
        TextView role = text("Researcher", 11, NAVY, bold);
        role.setBackground(shape(PALE, 18, 0));
        role.setPadding(dp(10), dp(5), dp(10), dp(5));
        welcome.addView(role, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        LinearLayout content = section(PAPER);
        metric(content, R.drawable.ic_staff_profile, countStudies("In-Progress"), "Active Studies", "In progress");
        metric(content, R.drawable.ic_staff_requests, consultations == null ? "—" : String.valueOf(consultations.length()),
                "Service Requests", "Consultations");
        metric(content, R.drawable.ic_staff_registry, "—", "Clearances", "Ethics + Registration");
        JSONObject study = activeStudy();
        metric(content, R.drawable.ic_staff_manage, study == null ? "—" : study.optString("lifecycle_state", "—"),
                "Current Stage", "");
        LinearLayout tracker = card(content);
        heading(tracker, "Research Progress Tracker");
        paragraph(tracker, study == null ? (studyError != null ? studyError : studies == null
                ? "Loading research studies…" : "No studies registered yet.") : study.optString("title"), 13, MUTED, regular);
        if (study != null) {
            LinearLayout stages = new LinearLayout(activity);
            tracker.addView(stages);
            for (String stage : new String[]{"Registered", "In Progress", "Under Review", "Completed"}) {
                TextView milestone = text(("In Progress".equals(stage) || "Registered".equals(stage) ? "●" : "○")
                        + "\n" + stage, 11, ACCENT, bold);
                milestone.setGravity(Gravity.CENTER);
                stages.addView(milestone, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            }
        }
        LinearLayout quick = card(content);
        heading(quick, "Quick Actions");
        outline(quick, "Submit Service Request", () -> openDetail("request"));
        outline(quick, "Upload Manuscript", () -> openDetail("manuscript"));
        outline(quick, "View Guidelines", () -> openDetail("guidelines"));
        outline(quick, "Access Repository", () -> select("Research"));
        outline(quick, "Book Consultation", () -> openDetail("consultation"));
        LinearLayout journal = card(content);
        journal.setBackground(shape(ACCENT, 13, 0));
        paragraph(journal, "UNIVERSITY", 11, YELLOW, bold);
        paragraph(journal, "The UMak Research Journal", 16, WHITE, bold);
        action(journal, "Learn More", YELLOW, NAVY, () -> select("Research"));
    }

    private void services() {
        serviceDirectoryView = new ServiceDirectoryView(activity, isSignedIn(), new ServiceDirectoryView.Listener() {
            @Override
            public void onSelectService(ServiceData.ServiceItem item) {
                if ("SVC-01".equals(item.code) && !isSignedIn()) {
                    SignInGateDialog.show(activity, activity::showSignIn, activity::showCreateAccount);
                    return;
                }
                openServiceDetail(item, false);
            }

            @Override
            public void onSignInClicked() {
                activity.startAccess();
            }
        });
        body.addView(serviceDirectoryView.getContentLayout());
    }

    public void openServiceDetail(ServiceData.ServiceItem item, boolean formUnavailable) {
        this.selectedServiceItem = item != null ? item : ServiceData.getById(2);
        this.serviceFormUnavailable = formUnavailable;
        openDetail("service_detail");
    }

    private void renderServiceDetail() {
        if (selectedServiceItem == null) selectedServiceItem = ServiceData.getById(2);
        serviceDetailView = new ServiceDetailView(activity, selectedServiceItem, new ServiceDetailView.Listener() {
            @Override
            public void onBack() {
                goBack();
            }

            @Override
            public void onSubmitRequest(ServiceData.ServiceItem item) {
                if (!isSignedIn()) {
                    SignInGateDialog.show(activity, activity::showSignIn, activity::showCreateAccount);
                } else {
                    openDetail("request");
                }
            }

            @Override
            public void onViewSubmissionInstructions(ServiceData.ServiceItem item) {
                openDetail("external_submission");
            }

            @Override
            public void onOpenNavigationGuidance() {
                openDetail("app_navigation");
            }

            @Override
            public void onContactCrm() {
                openDetail("guidelines");
            }

            @Override
            public void onOpenForm(ServiceData.ServiceItem item, boolean simulateFailure) {
                if (simulateFailure || serviceFormUnavailable) {
                    FormFailureDialog.show(activity, () -> {
                        serviceFormUnavailable = false;
                        if (serviceDetailView != null) serviceDetailView.setFormUnavailable(false);
                    }, () -> {
                        serviceFormUnavailable = true;
                        if (serviceDetailView != null) serviceDetailView.setFormUnavailable(true);
                    });
                } else {
                    ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(activity, item.formTitle, "Opening approved form template for download.");
                }
            }
        });
        serviceDetailView.setFormUnavailable(serviceFormUnavailable);
        body.addView(serviceDetailView.getContentLayout());
    }

    private void renderExternalSubmission() {
        ExternalSubmissionView view = new ExternalSubmissionView(activity, this::goBack);
        body.addView(view.getContentLayout());
    }

    private void renderAppNavigation() {
        AppNavigationGuidanceView view = new AppNavigationGuidanceView(activity, this::goBack);
        body.addView(view.getContentLayout());
    }

    public void previewServiceDirectory(String previewState) {
        detail = "";
        tab = "Services";
        render();
        if (serviceDirectoryView != null) {
            serviceDirectoryView.configurePreviewState(previewState);
        }
    }

    public void previewServiceDetail(String state) {
        detailParent = "Services";
        if ("2.2a".equals(state)) {
            openServiceDetail(ServiceData.getById(2), false);
        } else if ("2.2b".equals(state)) {
            openServiceDetail(ServiceData.getById(4), false);
        } else if ("2.2c".equals(state)) {
            openServiceDetail(ServiceData.getById(2), true);
        }
    }

    public void previewExternalSubmission() {
        detailParent = "Services";
        selectedServiceItem = ServiceData.getById(4);
        openDetail("external_submission");
    }

    public void previewAppNavigation() {
        detailParent = "Services";
        openDetail("app_navigation");
    }

    public void previewFormFailureModal() {
        FormFailureDialog.show(activity, () -> {
            serviceFormUnavailable = false;
            if (serviceDetailView != null) serviceDetailView.setFormUnavailable(false);
        }, () -> {
            serviceFormUnavailable = true;
            if (serviceDetailView != null) serviceDetailView.setFormUnavailable(true);
        });
    }

    private void guidelines() {
        hero("Guidelines", "Review current CRM policies, templates, and submission requirements.", true);
        LinearLayout content = section(PAPER);
        LinearLayout library = card(content);
        heading(library, "Guideline library");
        LinearLayout policy = card(content);
        showPolicy(policy, ResearcherCatalog.GUIDELINES.length - 1);
        for (int index = 0; index < ResearcherCatalog.GUIDELINES.length; index++) {
            int selected = index;
            outline(library, ResearcherCatalog.GUIDELINES[index][0], () -> showPolicy(policy, selected));
        }
        outline(content, "Open saved public resources", activity::showSavedPublicResources);
    }

    private void showPolicy(LinearLayout card, int selected) {
        card.removeAllViews();
        paragraph(card, "POLICY", 11, ACCENT, bold);
        heading(card, ResearcherCatalog.GUIDELINES[selected][0]);
        paragraph(card, ResearcherCatalog.GUIDELINES[selected][1], 13, MUTED, regular);
    }

    private void announcements() {
        hero("Latest Announcements", "Center for Research Management", true);
        publicItems(section(PAPER), "announcement");
    }

    private void publicItems(LinearLayout parent, String kind) {
        TextView state = paragraph(parent, "Loading CRM updates…", 13, MUTED, regular);
        activity.researcherRequest(this, "/api/v1/public/content", null, false, (data, error) -> {
            if (error != null) {
                state.setText(error);
                return;
            }
            try {
                JSONArray items = new JSONObject(data).getJSONArray("items");
                int matched = 0;
                for (int index = 0; index < items.length(); index++) {
                    JSONObject item = items.getJSONObject(index);
                    if (!kind.equals(item.optString("kind"))) continue;
                    LinearLayout card = card(parent);
                    heading(card, item.optString("title"));
                    paragraph(card, item.optString("text"), 13, MUTED, regular);
                    matched++;
                }
                if (matched == 0) state.setText("No announcements available.");
                else parent.removeView(state);
            } catch (JSONException invalid) {
                state.setText("CRM updates could not be read.");
            }
        });
    }

    private void requests() {
        myRequestsView = new MyRequestsView(activity, new MyRequestsView.Listener() {
            @Override
            public void onRequestSelected(RequestData.RequestItem item) {
                openRequestDetail(item);
            }

            @Override
            public void onSubmitNewRequest() {
                openDetail("request");
            }
        });
        body.addView(myRequestsView.getContentLayout());
    }

    public void openRequestDetail(RequestData.RequestItem item) {
        this.selectedRequestItem = item;
        openDetail("request_detail");
    }

    private void renderSubmitRequest() {
        submitRequestView = new SubmitRequestView(activity, new SubmitRequestView.Listener() {
            @Override
            public void onSubmitSuccess(RequestData.RequestItem request) {
                selectedRequestItem = request;
                receiptIsCorrection = false;
                openDetail("request_receipt");
            }

            @Override
            public void onBack() {
                goBack();
            }
        });
        body.addView(submitRequestView.getContentLayout());
    }

    private void renderRequestDetail() {
        if (selectedRequestItem == null) {
            selectedRequestItem = RequestData.createReq089("For correction");
        }
        requestDetailView = new RequestDetailView(activity, selectedRequestItem, new RequestDetailView.Listener() {
            @Override
            public void onBack() {
                goBack();
            }

            @Override
            public void onCorrectRequest(RequestData.RequestItem item) {
                selectedRequestItem = item;
                openDetail("correct_request");
            }
        });
        body.addView(requestDetailView.getContentLayout());
    }

    private void renderCorrectRequest() {
        if (selectedRequestItem == null) {
            selectedRequestItem = RequestData.createReq089("For correction");
        }
        correctRequestView = new CorrectRequestView(activity, selectedRequestItem, new CorrectRequestView.Listener() {
            @Override
            public void onSubmitCorrection(RequestData.RequestItem updatedItem) {
                selectedRequestItem = updatedItem;
                receiptIsCorrection = true;
                openDetail("request_receipt");
            }

            @Override
            public void onCancel() {
                goBack();
            }
        });
        body.addView(correctRequestView.getContentLayout());
    }

    private void renderRequestReceipt() {
        if (selectedRequestItem == null) {
            selectedRequestItem = RequestData.createReq089("Submitted");
        }
        requestReceiptView = new RequestReceiptView(activity, new RequestReceiptView.Listener() {
            @Override
            public void onViewRequest(String requestId) {
                openRequestDetail(selectedRequestItem);
            }

            @Override
            public void onBackToRequests() {
                detail = "";
                tab = "Requests";
                render();
            }
        }, receiptIsCorrection, selectedRequestItem);
        body.addView(requestReceiptView.getContentLayout());
    }

    private void renderNotifications() {
        notificationsView = new NotificationsView(activity, new NotificationsView.Listener() {
            @Override
            public void onBack() {
                goBack();
            }

            @Override
            public void onNotificationClick(RequestData.NotificationItem item) {
                RequestData.RequestItem req;
                if ("REQ-2026-089".equals(item.requestCode)) {
                    if (item.title != null && item.title.contains("Correction")) {
                        req = RequestData.createReq089("For correction");
                    } else if (item.title != null && item.title.contains("Review")) {
                        req = RequestData.createReq089("In review");
                    } else {
                        req = RequestData.createReq089("Submitted");
                    }
                } else if ("REQ-2026-081".equals(item.requestCode)) {
                    req = RequestData.getInitialRequests().get(2);
                } else if ("REQ-2026-074".equals(item.requestCode)) {
                    req = RequestData.getInitialRequests().get(3);
                } else {
                    req = RequestData.createReq089("Submitted");
                }
                openRequestDetail(req);
            }
        });
        body.addView(notificationsView.getContentLayout());
    }

    public void previewSubmitRequest(String state) {
        detailParent = "Requests";
        openDetail("request");
        if (submitRequestView != null) {
            submitRequestView.setPreviewState(state);
        }
    }

    public void previewMyRequests(String state) {
        detail = "";
        tab = "Requests";
        render();
        if (myRequestsView != null) {
            myRequestsView.setPreviewState(state);
        }
    }

    public void previewRequestDetail(String status) {
        detailParent = "Requests";
        this.selectedRequestItem = RequestData.createReq089(status);
        openDetail("request_detail");
    }

    public void previewCorrectRequest() {
        detailParent = "Requests";
        this.selectedRequestItem = RequestData.createReq089("For correction");
        openDetail("correct_request");
    }

    public void previewRequestReceipt(boolean isCorrection) {
        detailParent = "Requests";
        this.selectedRequestItem = RequestData.createReq089("Submitted");
        this.receiptIsCorrection = isCorrection;
        openDetail("request_receipt");
    }

    public void previewNotifications(String state) {
        detailParent = tab;
        openDetail("notifications");
        if (notificationsView != null) {
            notificationsView.setPreviewState(state);
        }
    }

    public void previewRequestFilterDialog() {
        detail = "";
        tab = "Requests";
        render();
        new RequestFilterDialog(activity, new RequestFilterDialog.FilterListener() {
            @Override
            public void onApplyFilters(String status, String fromDate, String toDate) {
                if (myRequestsView != null) {
                    myRequestsView.setPreviewState("3.6b_filtered");
                }
            }

            @Override
            public void onClearFilters() {
                if (myRequestsView != null) {
                    myRequestsView.clearAllFilters();
                }
            }
        }).show("All", "2026-10-01", "2026-10-31");
    }

    private void renderEditProfile() {
        editProfileView = new EditProfileView(activity, userProfile, new EditProfileView.Listener() {
            @Override
            public void onSave(String name, String college) {
                userProfile.name = name;
                userProfile.college = college;
                profileUpdatedSnackbar = true;
                goBack();
            }

            @Override
            public void onCancel() {
                goBack();
            }
        });
        body.addView(editProfileView.getContentLayout());
    }

    private void renderRegistrationConfirmation() {
        String regTitle = registration.getOrDefault("Research Title", "Impact of AI on Student Engagement");
        registrationConfirmationView = new RegistrationConfirmationView(activity, "REG-2026-089", regTitle, new RegistrationConfirmationView.Listener() {
            @Override
            public void onViewProgress() {
                selectedProgressState = "4.6c";
                openDetail("research_progress");
            }

            @Override
            public void onBackToDashboard() {
                detail = "";
                tab = "Dashboard";
                render();
            }
        });
        body.addView(registrationConfirmationView.getContentLayout());
    }

    private void renderResearchProgress() {
        researchProgressView = new ResearchProgressView(activity, ResearchData.getStudyProgress(selectedProgressState), new ResearchProgressView.Listener() {
            @Override
            public void onBack() {
                goBack();
            }

            @Override
            public void onRegisterResearch() {
                registrationStep = 0;
                openDetail("registration");
            }
        });
        body.addView(researchProgressView.getContentLayout());
    }

    public void previewEditProfile(String state) {
        detailParent = "Profile";
        openDetail("edit_profile");
        if (editProfileView != null) {
            editProfileView.setPreviewState(state);
        }
    }

    public void previewProfile(String state) {
        detail = "";
        tab = "Profile";
        profileUpdatedSnackbar = "4.1_success".equals(state) || "success_snackbar".equals(state);
        render();
        if (profileView != null) {
            profileView.setPreviewState(state);
        }
    }

    public void previewRegistrationStep1(String state) {
        detailParent = "Research";
        registrationStep = 0;
        openDetail("registration");
        if (registrationStep1View != null) {
            registrationStep1View.setPreviewState(state);
        }
    }

    public void previewAddCoAuthorDialog() {
        new AddCoAuthorDialog(activity, new java.util.ArrayList<>(), new AddCoAuthorDialog.Listener() {
            @Override
            public void onTeamUpdated(java.util.List<ResearchData.CoAuthorItem> currentTeam) {}

            @Override
            public void onDismiss() {}
        }).show();
    }

    public void previewRegistrationConfirmation() {
        detailParent = "Research";
        openDetail("registration_confirmation");
    }

    public void previewResearchProgress(String state) {
        detailParent = "Profile";
        selectedProgressState = state;
        openDetail("research_progress");
        if (researchProgressView != null) {
            researchProgressView.setPreviewState(state);
        }
    }

    private void consultation() {
        hero("Book Consultation", "Request a consultation with the Center for Research Management.", true);
        LinearLayout form = card(section(PAPER));
        heading(form, "Consultation details");
        EditText topic = input(form, "Consultation topic", "Research consultation", false);
        topic.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        TextView feedback = paragraph(form, "", 13, MUTED, regular);
        Button submit = action(form, "Submit", NAVY, WHITE, null);
        submit.setOnClickListener(view -> {
            String subject = topic.getText().toString().trim();
            if (subject.isEmpty()) {
                topic.setError("Enter a consultation topic.");
                return;
            }
            submit.setEnabled(false);
            JSONObject payload = json("service_type", subject);
            activity.researcherRequest(this, "/api/v1/consultations", payload, true, (data, error) -> {
                submit.setEnabled(true);
                if (error != null) {
                    feedback.setText(error);
                    return;
                }
                select("Requests");
            });
        });
    }

    private void serviceRequest() {
        hero("Submit Service Request", "Start a new request with the Center for Research Management.", true);
        LinearLayout form = card(section(PAPER));
        heading(form, "Request details");
        paragraph(form, "Fields marked required must be completed before submission.", 13, MUTED, regular);
        String[] services = new String[ResearcherCatalog.SERVICES.length + 1];
        services[0] = "Select a CRM service";
        for (int index = 0; index < ResearcherCatalog.SERVICES.length; index++) {
            services[index + 1] = ResearcherCatalog.SERVICES[index][0];
        }
        Spinner type = choices(form, "Service type", services);
        choices(form, "Research stage", new String[]{"Proposal development", "Ethics review", "Data gathering", "Presentation", "Publication"});
        input(form, "Study title", "Enter the complete research title", false);
        input(form, "Request details", "Describe the assistance you need", true);
        paragraph(form, "Include specific questions, deadlines, or documents CRM should review.", 12, MUTED, regular);
        check(form, "I confirm that the information provided is complete and accurate.");
        TextView feedback = paragraph(form, "Consultation booking is available.", 12, MUTED, regular);
        action(form, "Submit", NAVY, WHITE, () -> {
            if ("Research Consultation".equals(type.getSelectedItem())) openDetail("consultation");
            else feedback.setText("Online submission for this service is currently unavailable.");
        });
    }

    private void manuscript() {
        hero("Upload Manuscript", "Submit a manuscript and supporting files for review.", true);
        LinearLayout form = card(section(PAPER));
        heading(form, "Manuscript submission");
        paragraph(form, "Prepare an anonymized manuscript before uploading.", 13, MUTED, regular);
        choices(form, "Submission type", new String[]{"Original Research Article", "Review Article"});
        input(form, "Manuscript title", "Enter manuscript title", false);
        outline(form, "Choose manuscript file", () -> activity.pickResearcherDocument(this, "manuscript",
                new String[]{"application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"}));
        TextView filename = paragraph(form, documents.optString("manuscript", "PDF or DOCX"), 12, MUTED, regular);
        documentLabels.put("manuscript", filename);
        input(form, "Cover note", "Add a note for the editorial team (optional)", true);
        check(form, "This work is original and has been approved by all listed authors.");
        TextView feedback = paragraph(form, "", 12, MUTED, regular);
        action(form, "Upload", NAVY, WHITE, () -> feedback.setText("Online manuscript submission is currently unavailable."));
    }

    private void reviewManuscript() {
        hero("Review Manuscript", "Review Manuscript and Resubmit updated Documents.", true);
        LinearLayout content = section(PAPER);
        LinearLayout study = card(content);
        paragraph(study, reviewStudy.optString("title"), 19, BLUE, serif);
        paragraph(study, "Study #" + reviewStudy.optInt("id") + " · " + reviewStudy.optString("lifecycle_state"),
                12, MUTED, regular);
        LinearLayout history = card(content);
        paragraph(history, "REVIEW HISTORY & FEEDBACK", 16, BLUE, serif);
        TextView feedback = paragraph(history, "Loading researcher-visible feedback…", 13, MUTED, regular);
        activity.researcherRequest(this, "/api/v1/research/" + reviewStudy.optInt("id") + "/feedback", null, true,
                (data, error) -> {
                    if (error != null) {
                        feedback.setText(error);
                        return;
                    }
                    try {
                        String message = new JSONObject(data).optString("researcher_visible_feedback");
                        feedback.setText(message.isEmpty() ? "No reviewer feedback yet." : message);
                    } catch (JSONException invalid) {
                        feedback.setText("CRM feedback could not be read.");
                    }
                });
        LinearLayout correction = card(content);
        paragraph(correction, "Correction Resubmission", 19, BLUE, serif);
        outline(correction, "Upload Revised Manuscript", () -> activity.pickResearcherDocument(
                this, "revision", new String[]{"application/pdf"}));
        documentLabels.put("revision", paragraph(correction, documents.optString("revision", "PDF Format"),
                12, MUTED, regular));
        LinearLayout response = card(content);
        input(response, "Response to feedback", "Describe the changes made to the document.", true);
        TextView status = paragraph(content, "", 12, MUTED, regular);
        action(content, "Submit", NAVY, WHITE, () -> status.setText("Online resubmission is currently unavailable."));
        outline(content, "Cancel", this::goBack);
    }

    private final JSONObject documents = new JSONObject();
    private final java.util.Map<String, TextView> documentLabels = new java.util.HashMap<>();

    void showDocumentName(String field, String name) {
        put(documents, field, name);
        TextView target = documentLabels.get(field);
        if (target != null && target.isAttachedToWindow()) target.setText(name);
    }

    private void registration() {
        if (registrationStep == 0) {
            registrationStep1View = new RegistrationStep1View(activity, userProfile, registration, new RegistrationStep1View.Listener() {
                @Override
                public void onContinue(java.util.Map<String, String> data) {
                    registration.putAll(data);
                    registrationStep++;
                    render();
                }

                @Override
                public void onSaveDraft(java.util.Map<String, String> data) {
                    registration.putAll(data);
                    saveRegistrationDraft(null, null);
                }
            });
            body.addView(registrationStep1View.getContentLayout());
            return;
        }
        String[] steps = {"General Information", "Abstract & Methodology", "Document Upload & Compliance", "Final Review"};
        LinearLayout intro = hero(registrationStep == 3 ? "Review Registration" : "Research Registration",
                registrationStep == 3 ? "Please check the submitted details before submitting."
                        : "Register your ongoing or proposed research project.", false);
        android.widget.ProgressBar progress = new android.widget.ProgressBar(activity, null,
                android.R.attr.progressBarStyleHorizontal);
        progress.setMax(3);
        progress.setProgress(Math.min(registrationStep + 1, 3));
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(WHITE));
        intro.addView(progress);
        paragraph(intro, registrationStep == 3 ? steps[3] : "Step " + (registrationStep + 1) + " of 3: " + steps[registrationStep],
                11, WHITE, bold);
        LinearLayout content = section(PAPER);
        java.util.Map<String, EditText> fields = new java.util.LinkedHashMap<>();
        if (registrationStep == 1) {
            LinearLayout abstractCard = card(content);
            heading(abstractCard, "Abstract & Keywords");
            draftField(abstractCard, fields, "Research Abstract / Executive Summary", "Type or paste your abstract here.", true);
            draftField(abstractCard, fields, "Keywords", "Type a keyword…", false);
            LinearLayout methodology = card(content);
            heading(methodology, "Methodology & Expected Outcomes");
            draftField(methodology, fields, "Research Methodology", "Quantitative Method", false);
            draftField(methodology, fields, "Expected Outcomes & Significance", "Describe the expected contributions.", true);
        } else if (registrationStep == 2) {
            LinearLayout uploads = card(content);
            heading(uploads, "Required Documents");
            registrationFile(uploads, "Research Proposal");
            registrationFile(uploads, "Ethical Clearance");
            declarations(card(content));
        } else {
            LinearLayout general = card(content);
            heading(general, "General Details");
            review(general, "Research Title", "Research Type", "Department / College", "Lead Researcher / Author");
            LinearLayout abstractCard = card(content);
            heading(abstractCard, "Abstract & Methodology");
            review(abstractCard, "Research Methodology", "Research Abstract / Executive Summary");
            LinearLayout uploads = card(content);
            heading(uploads, "Documents Uploaded");
            paragraph(uploads, documents.optString("Research Proposal", "Proposal pending"), 13, MUTED, regular);
            paragraph(uploads, documents.optString("Ethical Clearance", "Ethical clearance pending"), 13, MUTED, regular);
            declarations(card(content));
        }
        TextView feedback = paragraph(content, "", 12, MUTED, regular);
        if (registrationStep < 3) {
            action(content, "Continue", YELLOW, NAVY, () -> {
                collect(fields);
                registrationStep++;
                render();
            });
        } else {
            action(content, "Submit", NAVY, WHITE, () -> {
                openDetail("registration_confirmation");
            });
        }
        if (registrationStep > 0) {
            previousRegistrationStep = () -> {
                collect(fields);
                registrationStep--;
                render();
            };
        }
        Button save = action(content, "Save as Draft", WHITE, NAVY, null);
        save.setOnClickListener(view -> {
            collect(fields);
            saveRegistrationDraft(save, feedback);
        });
    }

    private int draftId;

    private void saveRegistrationDraft(Button save, TextView feedback) {
        String title = registration.getOrDefault("Research Title", "").trim();
        String summary = registration.getOrDefault("Research Abstract / Executive Summary", "").trim();
        String department = registration.getOrDefault("Department / College", "").trim();
        if (title.isEmpty() || summary.isEmpty() || department.isEmpty()) {
            if (feedback != null) feedback.setText("Enter the title, abstract and department before saving to CRM.");
            else android.widget.Toast.makeText(activity, "Enter the title, abstract and department before saving to CRM.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject draft = json("title", title);
        put(draft, "abstract", summary);
        put(draft, "department", department);
        put(draft, "research_year", java.time.LocalDate.now().getYear());
        if (save != null) save.setEnabled(false);
        activity.researcherRequest(this, "/api/v1/researcher/drafts" + (draftId == 0 ? "" : "/" + draftId),
                draft, true, (data, error) -> {
                    if (save != null) save.setEnabled(true);
                    if (error != null) {
                        if (feedback != null) feedback.setText(error);
                        else android.widget.Toast.makeText(activity, error, android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        draftId = new JSONObject(data).getInt("id");
                        if (feedback != null) feedback.setText("Core research details saved as a draft.");
                        else android.widget.Toast.makeText(activity, "Core research details saved as a draft.", android.widget.Toast.LENGTH_SHORT).show();
                        loadStudies();
                    } catch (JSONException invalid) {
                        if (feedback != null) feedback.setText("CRM draft response could not be read.");
                    }
                });
    }

    private void draftField(LinearLayout parent, java.util.Map<String, EditText> fields,
            String label, String hint, boolean multiline) {
        EditText field = input(parent, label, hint, multiline);
        field.setText(registration.getOrDefault(label, ""));
        fields.put(label, field);
    }

    private void collect(java.util.Map<String, EditText> fields) {
        fields.forEach((key, field) -> registration.put(key, field.getText().toString()));
    }

    private void review(LinearLayout parent, String... keys) {
        for (String key : keys) paragraph(parent, key + "  " + registration.getOrDefault(key, ""), 12, BLUE, regular);
    }

    private void registrationFile(LinearLayout parent, String field) {
        outline(parent, "Attach " + field, () -> activity.pickResearcherDocument(this, field, new String[]{"application/pdf"}));
        documentLabels.put(field, paragraph(parent, documents.optString(field, "PDF"), 12, MUTED, regular));
    }

    private final java.util.Set<String> declarations = new java.util.HashSet<>();

    private void declarations(LinearLayout parent) {
        heading(parent, "Declaration & Compliance");
        for (String label : new String[]{"I certify that this study is an original work and not plagiarized or duplicative.",
                "I agree to comply with the UMAK CRM research data policies and ethical guidelines.",
                "I declare that all co-authors have consented to their registration."}) {
            CheckBox box = check(parent, label);
            box.setChecked(declarations.contains(label));
            box.setOnCheckedChangeListener((view, checked) -> {
                if (checked) declarations.add(label);
                else declarations.remove(label);
            });
        }
    }

    private void archive() {
        LinearLayout intro = hero("Research Archive", "Browse research studies from across all UMak colleges. All works are archived by the Center for Research Management.", true);
        tintText((TextView) intro.getChildAt(0), "Archive", YELLOW);
        LinearLayout filters = section(WHITE);
        EditText query = input(filters, "Search", "Search by title", false);
        query.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        EditText department = input(filters, "College", "All Colleges", false);
        department.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        EditText year = input(filters, "Year", "All Years", false);
        year.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        year.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        LinearLayout results = column();
        Button search = action(filters, "Search", NAVY, WHITE, null);
        search.setOnClickListener(view -> {
            String yearText = year.getText().toString().trim();
            int selectedYear = yearText.isEmpty() ? 0 : Integer.parseInt(yearText);
            if (!yearText.isEmpty() && selectedYear < 1900) {
                year.setError("Enter a year from 1900 onward.");
                return;
            }
            searchArchive(results, query.getText().toString(), department.getText().toString().trim(), selectedYear);
        });
        LinearLayout area = section(PAPER);
        area.addView(results);
        searchArchive(results, "", "", 0);
        LinearLayout journal = hero("UNIVERSITY", "The UMak Research Journal", true);
        paragraph(journal, "The official peer-reviewed academic journal of the University of Makati.", 13, WHITE, regular);
    }

    private void searchArchive(LinearLayout results, String query, String department, int year) {
        int generation = ++searchGeneration;
        results.removeAllViews();
        TextView state = paragraph(results, "Searching the archive…", 13, MUTED, regular);
        JSONObject filters = json("keyword", query);
        put(filters, "limit", 50);
        if (!department.isEmpty()) put(filters, "department", department);
        if (year != 0) put(filters, "year", year);
        activity.researcherRequest(this, "/api/v1/research/search", filters, false, (data, error) -> {
            if (generation != searchGeneration) return;
            if (error != null) {
                state.setText(error);
                return;
            }
            try {
                JSONArray records = new JSONArray(data);
                state.setText(records.length() == 0 ? "No results found." : records.length() + " results shown");
                for (int index = 0; index < records.length(); index++) {
                    JSONObject record = records.getJSONObject(index);
                    LinearLayout item = card(results);
                    paragraph(item, record.optString("department") + "  ·  AY " + record.optString("research_year"), 11, MUTED, bold);
                    paragraph(item, record.optString("status"), 11, NAVY, bold);
                    paragraph(item, record.optString("title"), 18, ACCENT, serif);
                    paragraph(item, record.optString("abstract"), 12, MUTED, regular);
                }
            } catch (JSONException invalid) {
                state.setText("CRM results could not be read.");
            }
        });
    }

    private void profile() {
        profileView = new ProfileView(activity, userProfile, new ProfileView.Listener() {
            @Override
            public void onEditProfile() {
                openDetail("edit_profile");
            }

            @Override
            public void onOpenStudyProgress(String studyState) {
                selectedProgressState = studyState;
                openDetail("research_progress");
            }

            @Override
            public void onSignOut() {
                activity.signOut();
            }
        });
        if (profileUpdatedSnackbar) {
            profileView.setPreviewState("4.1_success");
            profileUpdatedSnackbar = false;
        }
        body.addView(profileView.getContentLayout());
    }

    private void openDraft(JSONObject study) {
        registration.clear();
        declarations.clear();
        documents.remove("Research Proposal");
        documents.remove("Ethical Clearance");
        draftId = study.optInt("id");
        registrationStep = 0;
        registration.put("Research Title", study.optString("title"));
        registration.put("Research Abstract / Executive Summary", study.optString("abstract"));
        registration.put("Department / College", study.optString("department"));
        openDetail("registration");
    }

    private void loadStudies() {
        activity.researcherRequest(this, "/api/v1/researcher/studies", null, true, (data, error) -> {
            studyError = error;
            if (error == null) {
                try {
                    studies = new JSONArray(data);
                } catch (JSONException invalid) {
                    studyError = "CRM studies could not be read.";
                }
            }
            if (detail.isEmpty() && ("Dashboard".equals(tab) || "Profile".equals(tab))) render();
        });
        if (consultations == null) activity.researcherRequest(this, "/api/v1/consultations", null, true, (data, error) -> {
            if (error != null) return;
            try {
                consultations = new JSONArray(data);
                if (detail.isEmpty() && ("Dashboard".equals(tab) || "Profile".equals(tab))) render();
            } catch (JSONException ignored) {
                // Keep the count unavailable when the server returns an invalid list.
            }
        });
    }

    private String name() {
        if (account == null) return "LeBron R. James";
        String email = account.optString("email");
        return account.optString("display_name", email.isEmpty() ? "Researcher" : email);
    }

    private JSONObject activeStudy() {
        if (studies == null) return null;
        for (int index = 0; index < studies.length(); index++) {
            JSONObject study = studies.optJSONObject(index);
            if (study != null && "In-Progress".equals(study.optString("lifecycle_state"))) return study;
        }
        return null;
    }

    private String countStudies(String state) {
        if (studies == null) return "—";
        int count = 0;
        for (int index = 0; index < studies.length(); index++) {
            JSONObject study = studies.optJSONObject(index);
            if (study == null) continue;
            if ("Published".equals(state) ? !study.isNull("published_at")
                    : state.equals(study.optString("lifecycle_state"))) count++;
        }
        return String.valueOf(count);
    }

    private void metric(LinearLayout parent, int icon, String value, String label, String note) {
        LinearLayout tile = card(parent);
        tile.setMinimumHeight(dp(120));
        ImageView image = new ImageView(activity);
        image.setImageResource(icon);
        tile.addView(image, new LayoutParams(dp(20), dp(20)));
        paragraph(tile, value, 23, ACCENT, bold);
        paragraph(tile, label, 13, BLUE, bold);
        if (!note.isEmpty()) paragraph(tile, note, 11, MUTED, regular);
    }

    private LinearLayout hero(String title, String subtitle, boolean centered) {
        LinearLayout hero = section(BLUE);
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xff06294d, 0xff15354b, 0xff06294d});
        hero.setBackground(gradient);
        hero.setPadding(dp(16), dp(32), dp(16), dp(24));
        hero.setGravity(Gravity.CENTER_VERTICAL | (centered ? Gravity.CENTER_HORIZONTAL : Gravity.START));
        TextView heading = paragraph(hero, title, 28, WHITE, serif);
        ScreenTypography.heading(heading);
        if (centered) heading.setGravity(Gravity.CENTER);
        if (!subtitle.isEmpty()) {
            TextView copy = paragraph(hero, subtitle, 14, 0xffbdcadd, regular, 0);
            if (centered) copy.setGravity(Gravity.CENTER);
        }
        return hero;
    }

    private LinearLayout section(int color) {
        LinearLayout section = column();
        section.setPadding(dp(16), dp(24), dp(16), dp(24));
        section.setBackgroundColor(color);
        body.addView(section);
        return section;
    }

    private LinearLayout card(LinearLayout parent) {
        LinearLayout card = column();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(shape(WHITE, 12, BORDER));
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(12));
        parent.addView(card, params);
        return card;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(VERTICAL);
        return layout;
    }

    private TextView text(String value, int size, int color, Typeface font) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextColor(color);
        view.setTypeface(font);
        ScreenTypography.apply(view, size);
        return view;
    }

    private TextView paragraph(LinearLayout parent, String value, int size, int color, Typeface font) {
        return paragraph(parent, value, size, color, font, 8);
    }

    private TextView paragraph(LinearLayout parent, String value, int size, int color, Typeface font,
            int bottomMargin) {
        TextView view = text(value, size, color, font);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(bottomMargin);
        parent.addView(view, params);
        return view;
    }

    private void heading(LinearLayout parent, String value) {
        ScreenTypography.heading(paragraph(parent, value, 20, ACCENT, bold));
    }

    private GradientDrawable shape(int fill, int radius, int stroke) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(dp(radius));
        if (stroke != 0) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private Button action(LinearLayout parent, String label, int fill, int ink, Runnable clicked) {
        Button button = new Button(activity);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(ink);

        button.setTypeface(bold);
        ScreenTypography.apply(button, 14);
        button.setMinHeight(dp(48));
        button.setPadding(dp(16), dp(8), dp(16), dp(8));
        button.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(0x22000000),
                shape(fill, 7, fill == WHITE ? BORDER : 0), null));
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        parent.addView(button, params);
        if (clicked != null) button.setOnClickListener(view -> clicked.run());
        return button;
    }

    private Button outline(LinearLayout parent, String label, Runnable clicked) {
        return action(parent, label, WHITE, NAVY, clicked);
    }

    private EditText input(LinearLayout parent, String label, String hint, boolean multiline) {
        TextView caption = paragraph(parent, label, 12, BLUE, bold);
        EditText field = new EditText(activity);
        field.setId(View.generateViewId());
        caption.setLabelFor(field.getId());
        field.setTypeface(regular);
        field.setTextColor(BLUE);
        field.setHintTextColor(MUTED);
        ScreenTypography.apply(field, 16);
        field.setHint(hint);
        field.setSingleLine(!multiline);
        field.setMinHeight(dp(multiline ? 96 : 48));
        if (multiline) field.setGravity(Gravity.TOP);
        field.setPadding(dp(12), dp(8), dp(12), dp(8));
        field.setBackground(shape(WHITE, 6, BORDER));
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(16);
        parent.addView(field, params);
        return field;
    }

    private Spinner choices(LinearLayout parent, String label, String[] values) {
        TextView caption = paragraph(parent, label, 12, BLUE, bold);
        Spinner field = new Spinner(activity);
        field.setId(View.generateViewId());
        caption.setLabelFor(field.getId());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        field.setAdapter(adapter);
        field.setPadding(dp(10), 0, dp(10), 0);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        field.setMinimumHeight(dp(48));
        params.bottomMargin = dp(16);
        parent.addView(field, params);
        return field;
    }

    private CheckBox check(LinearLayout parent, String label) {
        CheckBox box = new CheckBox(activity);
        box.setText(label);
        box.setTypeface(regular);
        ScreenTypography.apply(box, 14);
        box.setMinHeight(dp(48));
        box.setTextColor(MUTED);
        box.setButtonTintList(android.content.res.ColorStateList.valueOf(NAVY));
        parent.addView(box);
        return box;
    }

    private int dp(int pixels) {
        return Math.round(pixels * getResources().getDisplayMetrics().density);
    }

    private static JSONObject json(String key, Object value) {
        JSONObject object = new JSONObject();
        put(object, key, value);
        return object;
    }

    private static void put(JSONObject target, String key, Object value) {
        try {
            target.put(key, value);
        } catch (JSONException invalid) {
            throw new IllegalArgumentException(key, invalid);
        }
    }
}
