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
import ph.edu.umak.crm.app.common.components.SaveSnackbar;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.dialogs.SignInGateDialog;
import ph.edu.umak.crm.app.requests.CorrectRequestView;
import ph.edu.umak.crm.app.requests.MyRequestsView;
import ph.edu.umak.crm.app.requests.NotificationsView;
import ph.edu.umak.crm.app.requests.RequestData;
import ph.edu.umak.crm.app.requests.RequestDetailView;
import ph.edu.umak.crm.app.requests.RequestFilterDialog;
import ph.edu.umak.crm.app.requests.RequestReceiptView;
import ph.edu.umak.crm.app.requests.SubmitRequestView;
import ph.edu.umak.crm.app.requests.LiveRequestForm;
import ph.edu.umak.crm.app.research.DraftTextWatcher;
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
    private final SaveSnackbar snackbar;
    private String tab;
    private int tabSlideDirection = 0;
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
    private boolean requestPreview;
    private final java.util.List<RequestData.RequestItem> previewRequests = new java.util.ArrayList<>();

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
                account == null ? "BS CS" : "",
                account == null ? "3rd Year" : "",
                account == null ? "College of Computing and Information Sciences"
                        : (account.isNull("college") ? "" : account.optString("college")),
                accountEmail.isEmpty() ? "ljames.A1234@umak.edu.ph" : accountEmail,
                account == null ? "A1234" : account.optString("id", "—"),
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
        snackbar = new SaveSnackbar(activity, scroll);
        addView(snackbar, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
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
        requestPreview = requestPreview && ("request_detail".equals(detail) || "correct_request".equals(detail));
        if ("registration".equals(detail) && previousRegistrationStep != null) {
            previousRegistrationStep.run();
            return true;
        }
        if ("correct_request".equals(detail) || "live_correction".equals(detail)) {
            detail = "request_detail";
            render();
            return true;
        }
        if ("registered_study".equals(detail)) {
            detail = "registered_studies";
            render();
            return true;
        }
        if ("registration".equals(detail) && submittingRegistration) return true;
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

    private int tabIndex(String name) {
        for (int i = 0; i < TABS.length; i++) {
            if (TABS[i].equals(name)) return i;
        }
        return -1;
    }

    private void selectFromNavBar(String selected) {
        int prevIndex = tabIndex(tab);
        int nextIndex = tabIndex(selected);
        if (nextIndex > prevIndex) {
            tabSlideDirection = 1;
        } else if (nextIndex < prevIndex) {
            tabSlideDirection = -1;
        } else {
            tabSlideDirection = 0;
        }
        select(selected);
    }

    private void select(String selected) {
        returningRequestFilters = null;
        requestPreview = false;
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
        } else if ("edit_profile".equals(destination) || "research_progress".equals(destination)
                || "registered_studies".equals(destination) || "registered_study".equals(destination)) {
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
        snackbar.clear();
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
        if (!detail.isEmpty()) {
            switch (detail) {
                case "announcements": announcements(); break;
                case "guidelines": guidelines(); break;
                case "service_detail": renderServiceDetail(); break;
                case "external_submission": renderExternalSubmission(); break;
                case "app_navigation": renderAppNavigation(); break;
                case "request": renderSubmitRequest(); break;
                case "live_service_request": renderLiveServiceRequest(); break;
                case "live_correction": renderLiveCorrection(); break;
                case "request_detail": renderRequestDetail(); break;
                case "request_receipt": renderRequestReceipt(); break;
                case "correct_request": renderCorrectRequest(); break;
                case "notifications": renderNotifications(); break;
                case "edit_profile": renderEditProfile(); break;
                case "registration_confirmation": renderRegistrationConfirmation(); break;
                case "registered_studies": renderRegisteredStudies(); break;
                case "registered_study": renderRegisteredStudy(); break;
                case "research_progress": renderResearchProgress(); break;
                case "manuscript": manuscript(); break;
                case "registration": registration(); break;
                case "review": reviewManuscript(); break;
                case "consultation": consultation(); break;
                default: break;
            }
        } else {
            switch (tab) {
                case "Services": services(); break;
                case "Requests": requests(); break;
                case "Research": archive(); break;
                case "Profile": profile(); break;
                default:
                    if (isSignedIn()) dashboard();
                    else landing();
                    break;
            }
        }
        if (tabSlideDirection != 0) {
            ph.edu.umak.crm.app.common.animation.UiMotion.slideHorizontal(body, tabSlideDirection);
            tabSlideDirection = 0;
        } else {
            ph.edu.umak.crm.app.common.animation.UiMotion.animateEntrance(body);
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
            item.setOnClickListener(view -> selectFromNavBar(name));
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

    private boolean previewingServiceDirectory;

    private void services() {
        serviceDirectoryView = new ServiceDirectoryView(activity, isSignedIn(), new ServiceDirectoryView.Listener() {
            @Override
            public void onSelectService(ServiceData.ServiceItem item) {
                if (item.revisionId == 0 && "SVC-01".equals(item.code) && !isSignedIn()) {
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
        ServiceDirectoryView view = serviceDirectoryView;
        body.addView(view.getContentLayout());
        if (!previewingServiceDirectory) loadPublishedServices(view);
    }

    private void loadPublishedServices(ServiceDirectoryView view) {
        view.showLoading();
        activity.researcherRequest(this, "/api/v1/public/services", null, false, (data, error) -> {
            if (serviceDirectoryView != view || !"Services".equals(tab) || !detail.isEmpty()
                    || previewingServiceDirectory) return;
            if (error == null) {
                try {
                    JSONArray rows = new JSONArray(data);
                    java.util.List<ServiceData.ServiceItem> published = new java.util.ArrayList<>();
                    for (int i = 0; i < rows.length(); i++) {
                        published.add(ServiceData.fromPublished(rows.getJSONObject(i)));
                    }
                    view.showServices(published);
                    return;
                } catch (JSONException invalid) {
                    // The retry control covers invalid and unavailable catalog responses.
                }
            }
            view.showFailure(() -> loadPublishedServices(view));
        });
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
                } else if (item.revisionId > 0) {
                    openDetail("live_service_request");
                } else if ("SVC-05".equals(item.code)) {
                    openDetail("consultation");
                } else {
                    ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(activity,
                            "Online submission unavailable", "Contact CRM for help submitting this service request.");
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
                ServiceData.ServiceItem item = selectedServiceItem;
                ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(activity, item.responsibleContact, item.contactEmail);
            }

            @Override
            public void onOpenForm(ServiceData.ServiceItem item, boolean simulateFailure) {
                if (item.revisionId > 0 && item.formMime != null) {
                    var view = serviceDetailView;
                    activity.downloadServiceForm(item,
                            () -> view.isAttached() && serviceDetailView == view && "service_detail".equals(detail),
                            view::showFormLoading, () -> view.setFormUnavailable(false),
                            () -> {
                        view.setFormUnavailable(true);
                        FormFailureDialog.show(activity, () -> onOpenForm(item, false), null);
                    });
                    return;
                }
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
        ExternalSubmissionView view = new ExternalSubmissionView(activity, selectedServiceItem, this::goBack);
        body.addView(view.getContentLayout());
    }

    private void renderAppNavigation() {
        AppNavigationGuidanceView view = new AppNavigationGuidanceView(activity, this::goBack);
        body.addView(view.getContentLayout());
    }

    public void previewServiceDirectory(String previewState) {
        detail = "";
        tab = "Services";
        previewingServiceDirectory = true;
        render();
        previewingServiceDirectory = false;
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

    private MyRequestsView.Filters returningRequestFilters;

    private void requests() {
        boolean preview = requestPreview || activity.isResearcherPreview();
        requestPreview = false;
        myRequestsView = new MyRequestsView(activity, new MyRequestsView.Listener() {
            @Override
            public void onRequestSelected(RequestData.RequestItem item) {
                returningRequestFilters = myRequestsView.filters();
                openRequestDetail(item);
                requestPreview = preview;
            }

            @Override
            public void onSubmitNewRequest() {
                requestPreview = preview;
                openDetail("request");
            }
        }, preview);
        MyRequestsView view = myRequestsView;
        if (returningRequestFilters != null) {
            view.restoreFilters(returningRequestFilters);
            returningRequestFilters = null;
        }
        body.addView(view.getContentLayout());
        if (preview) {
            view.addPreviewRequests(previewRequests);
            return;
        }
        if (!isSignedIn()) return;

        view.showLoadMessage("Loading requests…");
        loadRequestHistory(view, new JSONArray(), new java.util.ArrayList<>(), Integer.MAX_VALUE);
    }

    private void loadRequestHistory(MyRequestsView view, JSONArray history,
            java.util.List<RequestData.RequestItem> requests, int before) {
        String path = "/api/v1/consultations?before=" + before;
        activity.researcherRequest(this, path, null, true, (data, error) -> {
            if (myRequestsView != view || !"Requests".equals(tab) || !detail.isEmpty()) return;
            if (error != null) {
                view.showLoadMessage(error);
                return;
            }
            try {
                JSONArray page = new JSONArray(data);
                int nextBefore = before;
                for (int i = 0; i < page.length(); i++) {
                    JSONObject ticket = page.getJSONObject(i);
                    int id = ticket.getInt("id");
                    if (id >= nextBefore) throw new JSONException("Invalid request order");
                    nextBefore = id;
                    history.put(ticket);
                    requests.add(RequestData.fromConsultation(ticket));
                }
                if (page.length() == 100) {
                    loadRequestHistory(view, history, requests, nextBefore);
                } else {
                    consultations = history;
                    loadServiceRequestHistory(view, requests, Integer.MAX_VALUE, new java.util.ArrayList<>());
                }
            } catch (JSONException invalid) {
                view.showLoadMessage("CRM requests could not be read.");
            }
        });
    }

    private void loadServiceRequestHistory(MyRequestsView view, java.util.List<RequestData.RequestItem> consultations,
            int before, java.util.List<RequestData.RequestItem> serviceRequests) {
        activity.researcherRequest(this, "/api/v1/service-requests?before=" + before, null, true, (data, error) -> {
            if (myRequestsView != view || !"Requests".equals(tab) || !detail.isEmpty()) return;
            if (error != null) {
                view.showRequests(consultations);
                activity.message("Unable to load full service-request history.");
                return;
            }
            try {
                JSONArray rows = new JSONArray(data);
                int nextBefore = before;
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);
                    int id = row.getInt("id");
                    if (id >= nextBefore) throw new JSONException("Invalid request order");
                    nextBefore = id;
                    serviceRequests.add(RequestData.fromServiceRequest(row));
                }
                if (rows.length() == 100) {
                    loadServiceRequestHistory(view, consultations, nextBefore, serviceRequests);
                } else {
                    serviceRequests.addAll(consultations);
                    view.showRequests(serviceRequests);
                }
            } catch (JSONException invalid) {
                view.showLoadMessage("CRM requests could not be read.");
            }
        });
    }

    public void openRequestDetail(RequestData.RequestItem item) {
        this.selectedRequestItem = item;
        openDetail("request_detail");
    }

    private void renderLiveServiceRequest() {
        hero("Submit Service Request", selectedServiceItem.name, true);
        LiveRequestForm view = new LiveRequestForm(activity, selectedServiceItem, liveRequestActions(),
                saved -> openLiveReceipt(saved, false));
        body.addView(view.view());
        view.loadStudies();
    }

    private LiveRequestForm.Actions liveRequestActions() {
        return new LiveRequestForm.Actions() {
            @Override
            public void request(String path, JSONObject payload, java.util.function.BiConsumer<String, String> result) {
                activity.researcherRequest(ResearcherScreen.this, "/api/v1/service-requests" + path,
                        payload, true, result);
            }

            @Override
            public void choose(Runnable checking,
                    java.util.function.BiConsumer<MainActivity.SelectedAttachment, String> result) {
                activity.chooseRequestAttachment(checking, result);
            }

            @Override
            public void upload(MainActivity.SelectedAttachment attachment, int requirement, String attempt,
                    java.util.function.BiConsumer<String, String> result) {
                activity.uploadServiceRequestAttachment(ResearcherScreen.this, attachment, requirement, attempt, result);
            }
        };
    }

    private void openLiveReceipt(RequestData.RequestItem item, boolean correction) {
        selectedRequestItem = item;
        receiptIsCorrection = correction;
        openDetail("request_receipt");
    }

    private void renderLiveCorrection() {
        JSONObject request = currentLiveRequest;
        if (request == null) return;
        hero("Correct Request", "REQ-" + request.optInt("id"), true);
        try {
            JSONArray requirements = request.getJSONArray("requirements");
            JSONArray reviews = request.getJSONArray("reviews");
            JSONObject latest = reviews.optJSONObject(0);
            JSONArray missing = latest == null ? new JSONArray() : latest.getJSONArray("missing_items");
            java.util.Set<Integer> required = new java.util.HashSet<>();
            for (int i = 0; i < missing.length(); i++) required.add(missing.getInt(i));
            ServiceData.DocumentItem[] checklist = new ServiceData.DocumentItem[requirements.length()];
            for (int i = 0; i < checklist.length; i++) {
                checklist[i] = new ServiceData.DocumentItem(requirements.getJSONObject(i).getString("title"),
                        required.contains(i));
            }
            LiveRequestForm view = new LiveRequestForm(activity, request.getInt("id"), request.getInt("version"),
                    checklist, liveRequestActions(), saved -> openLiveReceipt(saved, true));
            body.addView(view.view());
        } catch (JSONException invalid) {
            paragraph(section(PAPER), "CRM returned invalid correction details.", 14, MUTED, regular);
        }
    }

    private void renderSubmitRequest() {
        if (!requestPreview && !activity.isResearcherPreview()) {
            serviceRequest();
            return;
        }
        submitRequestView = new SubmitRequestView(activity, new SubmitRequestView.Listener() {
            @Override
            public void onSubmitSuccess(RequestData.RequestItem request) {
                previewRequests.add(0, request);
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
        if (selectedRequestItem.serviceRequestId > 0) {
            renderLiveRequestDetails(selectedRequestItem.serviceRequestId);
            return;
        }
        requestDetailView = new RequestDetailView(activity, selectedRequestItem, new RequestDetailView.Listener() {
            @Override
            public void onCorrectRequest(RequestData.RequestItem item) {
                selectedRequestItem = item;
                openDetail("correct_request");
            }
        });
        body.addView(requestDetailView.getContentLayout());
    }

    private static final String SERVICE_REQUEST_API = "/api/v1/service-requests";
    private JSONObject currentLiveRequest;

    private void renderLiveRequestDetails(int id) {
        hero("Request details", "REQ-" + id, true);
        LinearLayout content = card(section(PAPER));
        PendingStatus state = new PendingStatus(activity);
        content.addView(state);
        state.working("Loading request…");
        activity.researcherRequest(this, SERVICE_REQUEST_API + "/" + id, null, true, (data, error) -> {
            if (!content.isAttachedToWindow()) return;
            if (error != null) {
                state.result(error, true);
                action(content, "Retry", WHITE, NAVY, this::render);
                return;
            }
            try {
                JSONObject request = new JSONObject(data);
                currentLiveRequest = request;
                state.clear();
                heading(content, request.getString("service"));
                paragraph(content, request.getString("status"), 15, NAVY, bold);
                paragraph(content, request.optString("subject"), 14, NAVY, bold);
                paragraph(content, request.optString("description"), 14, MUTED, regular);
                JSONArray files = request.getJSONArray("attachments");
                heading(content, "Submitted files");
                if (files.length() == 0) paragraph(content, "No files attached.", 14, MUTED, regular);
                for (int i = 0; i < files.length(); i++) {
                    JSONObject file = files.getJSONObject(i);
                    outline(content, file.getString("filename"), () -> activity.downloadRequestAttachment(
                            SERVICE_REQUEST_API + "/" + id + "/files/" + file.optString("id"),
                            file.optString("filename"), file.optString("mime"), content::isAttachedToWindow,
                            () -> state.result("Unable to open the file. Try again.", true)));
                }
                JSONArray reviews = request.getJSONArray("reviews");
                JSONArray requirements = request.getJSONArray("requirements");
                JSONObject latest = reviews.optJSONObject(0);
                JSONObject checkpoint = latest == null ? null : latest.optJSONObject("checkpoint");
                paragraph(content, checkpoint == null ? "No processing step recorded"
                        : checkpoint.optString("name") + " · " + checkpoint.optString("date"), 14, NAVY, bold);
                if (latest != null) {
                    paragraph(content, "Feedback: " + latest.optString("feedback"), 14, MUTED, regular);
                    paragraph(content, "Next action: " + latest.optString("next_action"), 14, MUTED, regular);
                    if (!latest.isNull("deadline_at")) paragraph(content, "Deadline: " + latest.optString("deadline_at"), 14, NAVY, bold);
                    JSONArray missing = latest.getJSONArray("missing_items");
                    for (int i = 0; i < missing.length(); i++) {
                        JSONObject requirement = requirements.optJSONObject(missing.getInt(i));
                        if (requirement != null) paragraph(content, "Needs correction: " + requirement.optString("title"), 14, NAVY, bold);
                    }
                    heading(content, "Review history");
                    for (int i = 0; i < reviews.length(); i++) {
                        JSONObject event = reviews.getJSONObject(i);
                        paragraph(content, event.optString("to_status") + " · " + event.optString("reviewed_at"),
                                14, NAVY, bold);
                        paragraph(content, event.optString("feedback"), 14, MUTED, regular);
                    }
                }
                if ("For correction".equals(request.optString("status"))) {
                    action(content, "Correct request", YELLOW, NAVY, () -> openDetail("live_correction"));
                }
            } catch (JSONException invalid) {
                state.result("CRM returned invalid request details. Try again.", true);
            }
        });
    }

    private void renderCorrectRequest() {
        if (selectedRequestItem == null) {
            selectedRequestItem = RequestData.createReq089("For correction");
        }
        correctRequestView = new CorrectRequestView(activity, selectedRequestItem, new CorrectRequestView.Listener() {
            @Override
            public void onChooseAttachment(Runnable onChecking, java.util.function.BiConsumer<MainActivity.SelectedAttachment, String> result) {
                activity.chooseRequestAttachment(onChecking, result);
            }

            @Override
            public void onSubmitCorrection(RequestData.RequestItem item) {
                receiptIsCorrection = true;
                selectedRequestItem = RequestData.createReq089("Submitted");
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
                requestPreview = requestPreview || selectedRequestItem.isPreviewSubmission();
                detail = "";
                tab = "Requests";
                render();
            }
        }, receiptIsCorrection, selectedRequestItem);
        body.addView(requestReceiptView.getContentLayout());
    }

    private boolean notificationPreview;

    private void renderNotifications() {
        if (!notificationPreview && !activity.isResearcherPreview()) {
            if (isSignedIn()) renderLiveNotifications();
            else {
                hero("Notifications", "Sign in to see your service request updates.", true);
                action(section(PAPER), "Sign In", YELLOW, NAVY, activity::showSignIn);
            }
            return;
        }
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

    private void renderLiveNotifications() {
        hero("Notifications", "Service request updates and deadline reminders", true);
        LinearLayout content = card(section(PAPER));
        PendingStatus state = new PendingStatus(activity);
        content.addView(state);
        state.working("Loading updates…");
        activity.researcherRequest(this, SERVICE_REQUEST_API + "/updates", null, true, (data, error) -> {
            if (!content.isAttachedToWindow()) return;
            if (error != null) {
                state.result(error, true);
                action(content, "Retry", WHITE, NAVY, this::render);
                return;
            }
            try {
                JSONArray updates = new JSONArray(data);
                state.clear();
                if (updates.length() == 0) paragraph(content, "No notifications yet.", 14, MUTED, regular);
                for (int i = 0; i < updates.length(); i++) {
                    JSONObject row = updates.getJSONObject(i);
                    int id = row.getInt("request_id");
                    String label = switch (row.getString("kind")) {
                        case "receipt" -> "Request submitted";
                        case "correction" -> "Correction requested";
                        case "completion" -> "Request completed";
                        case "deadline" -> "Deadline reminder";
                        default -> "Request reviewed";
                    };
                    outline(content, label + " · " + row.getString("service") + " · REQ-" + id, () -> {
                        selectedRequestItem = RequestData.serviceRequestReference(id, row.optString("service"));
                        openDetail("request_detail");
                    });
                }
            } catch (JSONException invalid) {
                state.result("CRM returned invalid updates. Try again.", true);
            }
        });
    }

    public void previewSubmitRequest(String state) {
        requestPreview = true;
        detailParent = "Requests";
        openDetail("request");
        if (submitRequestView != null) {
            submitRequestView.setPreviewState(state);
        }
    }

    public void previewMyRequests(String state) {
        requestPreview = true;
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
        notificationPreview = true;
        openDetail("notifications");
        notificationPreview = false;
        if (notificationsView != null) {
            notificationsView.setPreviewState(state);
        }
    }

    public void previewRequestFilterDialog() {
        requestPreview = true;
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
                if (activity.isResearcherPreview() || account == null) {
                    userProfile.name = name;
                    userProfile.college = college;
                    profileUpdatedSnackbar = true;
                    goBack();
                    return;
                }
                EditProfileView view = editProfileView;
                view.showSaving();
                JSONObject payload = json("display_name", name);
                try {
                    payload.put("college", college);
                } catch (JSONException invalid) {
                    view.showSaveError("Unable to prepare profile changes. Try again.");
                    return;
                }
                activity.researcherRequest(ResearcherScreen.this, "/api/v1/users/me/profile", payload, true,
                        (data, error) -> {
                            if (error != null) {
                                view.showSaveError(error);
                                return;
                            }
                            try {
                                JSONObject saved = new JSONObject(data);
                                String savedName = saved.getString("display_name");
                                String savedCollege = saved.getString("college");
                                userProfile.name = savedName;
                                userProfile.college = savedCollege;
                                account.put("display_name", savedName);
                                account.put("college", savedCollege);
                            } catch (JSONException invalid) {
                                view.showSaveError("CRM could not confirm the profile changes. Try again.");
                                return;
                            }
                            if (editProfileView == view && "edit_profile".equals(detail)) {
                                profileUpdatedSnackbar = true;
                                goBack();
                            }
                        });
            }

            @Override
            public void onCancel() {
                goBack();
            }
        });
        body.addView(editProfileView.getContentLayout());
    }

    private static final String REGISTRATION_API = "/api/v1/researcher/registrations";
    private JSONObject registrationReceipt;
    private int registeredStudyId;

    private void renderRegistrationConfirmation() {
        String regTitle = registration.getOrDefault("Research Title", "Impact of AI on Student Engagement");
        JSONObject receipt = registrationReceipt;
        String researchId = receipt == null ? "REG-2026-089" : receipt.optString("research_id");
        String date = receipt == null ? "Oct 29, 2026" : receipt.optString("registered_at").split("T", 2)[0];
        registrationConfirmationView = new RegistrationConfirmationView(activity, researchId,
                receipt == null ? regTitle : receipt.optString("title"), date, new RegistrationConfirmationView.Listener() {
            @Override
            public void onViewProgress() {
                if (receipt != null) {
                    registeredStudyId = receipt.optInt("id");
                    openDetail("registered_study");
                } else {
                    selectedProgressState = "4.6c";
                    openDetail("research_progress");
                }
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
                openLocalRegistration();
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

    }

    public void previewRegistrationStep1(String state) {
        detailParent = "Research";
        resetRegistration();
        openDetail("registration");
        if (registrationStep1View != null) {
            registrationStep1View.setPreviewState(state);
        }
    }

    public void previewAddCoAuthorDialog() {
        previewRegistrationStep1("4.2a_prefilled");
        if (registrationStep1View != null) registrationStep1View.openPreviewCoAuthorDialog();
    }

    public void previewRegistrationConfirmation() {
        registrationReceipt = null;
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

    private boolean submittingConsultation;
    private String consultationTopic = "";
    private String consultationAttemptTopic;
    private String consultationToken;
    private Button consultationSubmitButton;
    private EditText consultationTopicInput;
    private PendingStatus consultationStatus;

    private void consultation() {
        hero("Book Consultation", "Request a consultation with the Center for Research Management.", true);
        LinearLayout form = card(section(PAPER));
        heading(form, "Consultation details");
        EditText topic = input(form, "Consultation topic", "Research consultation", false);
        topic.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        topic.setText(consultationTopic);
        PendingStatus feedback = new PendingStatus(activity);
        form.addView(feedback);
        Button submit = action(form, "Submit", NAVY, WHITE, null);
        consultationSubmitButton = submit;
        consultationTopicInput = topic;
        consultationStatus = feedback;
        if (submittingConsultation) showConsultationPending();
        submit.setOnClickListener(view -> {
            if (submittingConsultation) return;
            String subject = topic.getText().toString().trim();
            if (subject.isEmpty()) {
                topic.setError("Enter a consultation topic.");
                return;
            }
            submittingConsultation = true;
            consultationTopic = subject;
            if (!subject.equals(consultationAttemptTopic)) {
                consultationAttemptTopic = subject;
                consultationToken = java.util.UUID.randomUUID().toString();
            }
            showConsultationPending();
            JSONObject payload = json("service_type", subject);
            try {
                payload.put("submission_token", consultationToken);
            } catch (JSONException invalid) {
                throw new IllegalStateException("Could not encode consultation token", invalid);
            }
            activity.researcherRequest(this, "/api/v1/consultations", payload, true, (data, error) -> {
                submittingConsultation = false;
                Button currentButton = consultationSubmitButton;
                EditText currentTopic = consultationTopicInput;
                PendingStatus currentStatus = consultationStatus;
                currentButton.setEnabled(true);
                currentButton.setText("Submit");
                currentTopic.setEnabled(true);
                currentStatus.clear();
                if (error != null) {
                    currentButton.setText("Retry");
                    currentStatus.result(error, true);
                    return;
                }
                try {
                    RequestData.RequestItem request = RequestData.fromConsultation(new JSONObject(data));
                    consultations = null;
                    consultationTopic = "";
                    consultationAttemptTopic = null;
                    consultationToken = null;
                    if (currentStatus.isAttachedToWindow() && "consultation".equals(detail)) {
                        selectedRequestItem = request;
                        receiptIsCorrection = false;
                        openDetail("request_receipt");
                    }
                } catch (JSONException invalid) {
                    currentButton.setText("Retry");
                    currentStatus.result("CRM could not confirm the request. Try again.", true);
                }
            });
        });
    }

    private void showConsultationPending() {
        consultationSubmitButton.setEnabled(false);
        consultationSubmitButton.setText("Submitting…");
        consultationTopicInput.setEnabled(false);
        consultationStatus.working("Submitting your consultation request to CRM…");
    }

    private void serviceRequest() {
        hero("Submit Service Request", "Choose an available way to submit a request.", true);
        LinearLayout form = card(section(PAPER));
        action(form, "Browse Services", YELLOW, NAVY, () -> select("Services"));
        heading(form, "Research consultation");
        paragraph(form, "Book a consultation online or select a service from the directory.",
                13, MUTED, regular);
        action(form, "Book Consultation", NAVY, WHITE, () -> openDetail("consultation"));
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

    private TextView localDraftStatus;

    private android.content.SharedPreferences localDrafts() {
        return activity.getSharedPreferences("crm-registration-drafts", android.content.Context.MODE_PRIVATE);
    }

    private String localDraftKey() {
        int id = account == null ? 0 : account.optInt("id");
        return id > 0 && !activity.isResearcherPreview() ? String.valueOf(id) : null;
    }

    private void autosaveRegistration() {
        String key = localDraftKey();
        if (key == null) return;
        JSONObject snapshot = json("fields", new JSONObject(registration));
        put(snapshot, "declarations", new JSONArray(declarations));
        put(snapshot, "crmId", draftId);
        put(snapshot, "submissionToken", registrationToken);
        put(snapshot, "step", registrationStep);
        localDrafts().edit().putString(key, snapshot.toString()).apply();
        if (localDraftStatus != null) localDraftStatus.setText("Saved on this device");
    }

    private void openLocalRegistration() {
        if (savingRegistrationDraft || submittingRegistration) return;
        resetRegistration();
        String key = localDraftKey();
        String saved = key == null ? null : localDrafts().getString(key, null);
        if (saved != null) {
            try {
                JSONObject snapshot = new JSONObject(saved);
                JSONObject fields = snapshot.getJSONObject("fields");
                fields.keys().forEachRemaining(field -> registration.put(field, fields.optString(field)));
                JSONArray checks = snapshot.getJSONArray("declarations");
                for (int i = 0; i < checks.length(); i++) declarations.add(checks.getString(i));
                draftId = snapshot.getInt("crmId");
                registrationToken = snapshot.optString("submissionToken", registrationToken);
                registrationStep = Math.max(0, Math.min(3, snapshot.getInt("step")));
            } catch (JSONException invalid) {
                resetRegistration();
                activity.message("The draft saved on this device could not be opened.");
            }
        }
        openDetail("registration");
    }

    private void localDraftControls(LinearLayout parent) {
        String key = localDraftKey();
        if (key == null) return;
        localDraftStatus = paragraph(parent, localDrafts().contains(key)
                ? "Saved on this device" : "Changes save on this device automatically", 12, MUTED, regular);
        outline(parent, "Delete local draft", () -> {
            if (savingRegistrationDraft || submittingRegistration) return;
            new android.app.AlertDialog.Builder(activity)
                    .setTitle("Delete draft from this device?")
                    .setMessage("Changes saved only on this device will be lost. Any copy saved to CRM will remain there.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (savingRegistrationDraft || submittingRegistration) return;
                        localDrafts().edit().remove(key).apply();
                        resetRegistration();
                        previousRegistrationStep = null;
                        goBack();
                    }).show();
        });
    }

    private void registration() {
        localDraftStatus = null;
        if (registrationStep == 0) {
            int session = registrationSession;
            registrationStep1View = new RegistrationStep1View(activity, userProfile, registration, new RegistrationStep1View.Listener() {
                @Override
                public boolean pickLiveCoAuthor(java.util.function.Consumer<ResearchData.CoAuthorItem> selected) {
                    if (localDraftKey() == null) return false;
                    showLiveCoAuthorLookup(selected);
                    return true;
                }

                @Override
                public void onChanged(java.util.Map<String, String> data) {
                    if (session != registrationSession || !"registration".equals(detail) || registrationStep != 0) return;
                    registration.putAll(data);
                    autosaveRegistration();
                }

                @Override
                public void onContinue(java.util.Map<String, String> data) {
                    registration.putAll(data);
                    registrationStep++;
                    autosaveRegistration();
                    render();
                }

                @Override
                public void onSaveDraft(java.util.Map<String, String> data, Button button, PendingStatus feedback) {
                    registration.putAll(data);
                    autosaveRegistration();
                    saveRegistrationDraft(button, feedback);
                }
            });
            LinearLayout form = registrationStep1View.getContentLayout();
            body.addView(form);
            localDraftControls(form);
            if (localDraftKey() != null) registrationStep1View.watchChanges();
            if (savingRegistrationDraft) {
                showDraftPending(registrationStep1View.getDraftButton(), registrationStep1View.getDraftStatus());
            }
            return;
        }
        String[] steps = {"General Information", "Abstract & Methodology",
                localDraftKey() == null ? "Document Upload & Compliance" : "Compliance", "Final Review"};
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
            if (localDraftKey() == null) {
                heading(uploads, "Required Documents");
                registrationFile(uploads, "Research Proposal");
                registrationFile(uploads, "Ethical Clearance");
            } else {
                heading(uploads, "Compliance declarations");
            }
            declarations(card(content));
        } else {
            LinearLayout general = card(content);
            heading(general, "General Details");
            review(general, "Research Title", "Research Type", "Department / College", "Academic period",
                    "Faculty adviser", "Lead Researcher / Author", "Target Completion Date");
            heading(general, "Team members");
            try {
                JSONArray team = new JSONArray(registration.getOrDefault("Co-authors", "[]"));
                if (team.length() == 0) paragraph(general, "No additional team members.", 14, MUTED, regular);
                for (int i = 0; i < team.length(); i++) {
                    JSONArray member = team.getJSONArray(i);
                    paragraph(general, member.optString(1) + " · " + member.optString(2), 14, NAVY, regular);
                }
            } catch (JSONException invalid) {
                paragraph(general, "Reopen the team selection to review members.", 14, MUTED, regular);
            }
            LinearLayout abstractCard = card(content);
            heading(abstractCard, "Abstract & Methodology");
            review(abstractCard, "Research Methodology", "Research Abstract / Executive Summary",
                    "Keywords", "Expected Outcomes & Significance");
            LinearLayout reviewDeclarations = card(content);
            heading(reviewDeclarations, "Selected declarations");
            if (declarations.isEmpty()) paragraph(reviewDeclarations, "None selected.", 14, MUTED, regular);
            for (String choice : declarations) paragraph(reviewDeclarations, choice, 14, NAVY, regular);
        }
        PendingStatus feedback = new PendingStatus(activity);
        content.addView(feedback);
        localDraftControls(content);
        if (registrationStep < 3) {
            action(content, "Continue", YELLOW, NAVY, () -> {
                collect(fields);
                registrationStep++;
                autosaveRegistration();
                render();
            });
        } else if (localDraftKey() != null) {
            Button confirm = action(content, "Confirm registration", YELLOW, NAVY, null);
            confirm.setEnabled(!submittingRegistration && !savingRegistrationDraft);
            confirm.setOnClickListener(view -> submitRegistration(confirm, feedback));
            outline(content, "Edit registration", () -> {
                if (submittingRegistration) return;
                registrationStep = 0;
                autosaveRegistration();
                render();
            });
        }
        if (registrationStep > 0) {
            previousRegistrationStep = () -> {
                collect(fields);
                registrationStep--;
                autosaveRegistration();
                render();
            };
        }
        Button save = action(content, "Save to CRM", WHITE, NAVY, null);
        if (savingRegistrationDraft) showDraftPending(save, feedback);
        save.setOnClickListener(view -> {
            collect(fields);
            autosaveRegistration();
            saveRegistrationDraft(save, feedback);
        });
    }

    private int draftId;
    private String registrationToken = java.util.UUID.randomUUID().toString();
    private boolean submittingRegistration;
    private boolean savingRegistrationDraft;
    private int registrationSession;

    private Button activeDraftButton;
    private PendingStatus activeDraftStatus;

    private void submitRegistration(Button button, PendingStatus feedback) {
        if (submittingRegistration || savingRegistrationDraft) return;
        String key = localDraftKey();
        if (key == null) return;
        try {
            JSONArray team = new JSONArray(registration.getOrDefault("Co-authors", "[]"));
            JSONArray memberIds = new JSONArray();
            for (int i = 0; i < team.length(); i++) memberIds.put(team.getJSONArray(i).getInt(0));
            String deadline = registration.getOrDefault("Target Completion Date", "");
            JSONObject payload = new JSONObject().put("submission_token", registrationToken)
                    .put("draft_id", draftId == 0 ? JSONObject.NULL : draftId)
                    .put("title", registration.getOrDefault("Research Title", ""))
                    .put("college", registration.getOrDefault("Department / College", ""))
                    .put("academic_period", registration.getOrDefault("Academic period", ""))
                    .put("adviser", registration.getOrDefault("Faculty adviser", ""))
                    .put("abstract", registration.getOrDefault("Research Abstract / Executive Summary", ""))
                    .put("keywords", registration.getOrDefault("Keywords", ""))
                    .put("methodology", registration.getOrDefault("Research Methodology", ""))
                    .put("expected_outcomes", registration.getOrDefault("Expected Outcomes & Significance", ""))
                    .put("research_type", registration.getOrDefault("Research Type", ""))
                    .put("target_date", deadline.isEmpty() ? JSONObject.NULL : deadline)
                    .put("member_ids", memberIds).put("declarations", new JSONArray(declarations));
            autosaveRegistration();
            int session = registrationSession;
            submittingRegistration = true;
            button.setEnabled(false);
            feedback.working("Registering your research with CRM…");
            activity.researcherRequest(this, REGISTRATION_API, payload, true, (data, error) -> {
                submittingRegistration = false;
                button.setEnabled(true);
                if (session != registrationSession) return;
                if (error != null) {
                    feedback.result(error, true);
                    return;
                }
                try {
                    JSONObject receipt = new JSONObject(data);
                    if (receipt.getInt("id") <= 0 || !receipt.has("registered_at") || !receipt.has("research_id")) {
                        throw new JSONException("Invalid registration receipt");
                    }
                    registrationReceipt = receipt;
                    localDrafts().edit().remove(key).apply();
                    draftId = 0;
                    loadStudies();
                    if ("registration".equals(detail) && registrationStep == 3) {
                        openDetail("registration_confirmation");
                    }
                } catch (JSONException invalid) {
                    feedback.result("CRM could not confirm registration. Retry the same submission.", true);
                }
            });
        } catch (JSONException invalid) {
            feedback.result("Check the selected team members and registration details.", true);
        }
    }

    private void showLiveCoAuthorLookup(java.util.function.Consumer<ResearchData.CoAuthorItem> selected) {
        EditText email = new EditText(activity);
        email.setHint("Institutional email");
        email.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        var dialog = new android.app.AlertDialog.Builder(activity).setTitle("Add co-author")
                .setView(email).setNegativeButton("Cancel", null).setPositiveButton("Find researcher", null).create();
        dialog.setOnShowListener(ignored -> {
            Button find = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            find.setOnClickListener(view -> {
                String address = email.getText().toString().trim();
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
                    email.setError("Enter the researcher's institutional email.");
                    return;
                }
                find.setEnabled(false);
                find.setText("Checking…");
                String suffix = new android.net.Uri.Builder().appendQueryParameter("email", address).build().toString();
                activity.researcherRequest(this, REGISTRATION_API + "/team-candidate" + suffix, null, true, (data, error) -> {
                    if (!dialog.isShowing()) return;
                    find.setEnabled(true);
                    find.setText("Find researcher");
                    if (error != null) {
                        email.setError(error);
                        return;
                    }
                    try {
                        JSONObject user = new JSONObject(data);
                        String category = "faculty".equals(user.getString("role")) ? "Faculty" : "Student";
                        String affiliation = user.isNull("college") ? category : category + " · " + user.getString("college");
                        ResearchData.CoAuthorItem person = new ResearchData.CoAuthorItem(user.getString("id"),
                                user.getString("display_name"), affiliation, user.getString("email"));
                        dialog.dismiss();
                        new android.app.AlertDialog.Builder(activity).setTitle("Add " + person.name + "?")
                                .setMessage(affiliation)
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Add", (confirmation, which) -> selected.accept(person)).show();
                    } catch (JSONException invalid) {
                        email.setError("CRM returned invalid account details. Try again.");
                    }
                });
            });
        });
        dialog.show();
    }

    private void renderRegisteredStudies() {
        hero("Registered research", "Research you registered or were added to.", true);
        LinearLayout results = card(section(PAPER));
        PendingStatus status = new PendingStatus(activity);
        results.addView(status);
        status.working("Loading registered studies…");
        activity.researcherRequest(this, REGISTRATION_API, null, true, (data, error) -> {
            if (!results.isAttachedToWindow()) return;
            if (error != null) {
                status.result(error, true);
                action(results, "Retry", WHITE, NAVY, this::render);
                return;
            }
            try {
                JSONArray studies = new JSONArray(data);
                status.clear();
                if (studies.length() == 0) {
                    paragraph(results, "No research registered.", 14, MUTED, regular);
                    action(results, "Register research", YELLOW, NAVY, this::openLocalRegistration);
                }
                for (int i = 0; i < studies.length(); i++) {
                    JSONObject row = studies.getJSONObject(i);
                    int id = row.getInt("id");
                    outline(results, row.getString("research_id") + " · " + row.getString("title"), () -> {
                        registeredStudyId = id;
                        openDetail("registered_study");
                    });
                }
            } catch (JSONException invalid) {
                status.result("CRM returned invalid registration data.", true);
            }
        });
    }

    private void renderRegisteredStudy() {
        hero("Registered research details", "REG-" + registeredStudyId, true);
        LinearLayout results = card(section(PAPER));
        PendingStatus status = new PendingStatus(activity);
        results.addView(status);
        status.working("Loading registration…");
        activity.researcherRequest(this, REGISTRATION_API + "/" + registeredStudyId, null, true, (data, error) -> {
            if (!results.isAttachedToWindow()) return;
            if (error != null) {
                status.result(error, true);
                action(results, "Retry", WHITE, NAVY, this::render);
                return;
            }
            try {
                JSONObject study = new JSONObject(data);
                status.clear();
                heading(results, study.getString("title"));
                paragraph(results, study.getString("stage"), 15, NAVY, bold);
                paragraph(results, study.getString("department") + " · " + study.getString("academic_period"),
                        14, NAVY, regular);
                paragraph(results, "Adviser: " + study.optString("adviser", "—"), 14, NAVY, regular);
                paragraph(results, study.optString("abstract"), 14, MUTED, regular);
                heading(results, "Research team");
                JSONArray team = study.getJSONArray("team");
                for (int i = 0; i < team.length(); i++) {
                    JSONObject person = team.getJSONObject(i);
                    paragraph(results, person.getString("name"), 14, NAVY, regular);
                }
                loadRegisteredProgress(results, study.getInt("id"));
                loadRegisteredDocuments(results, study.getInt("id"));
            } catch (JSONException invalid) {
                status.result("CRM returned an invalid registration.", true);
            }
        });
    }

    private void loadRegisteredDocuments(LinearLayout parent, int study) {
        LinearLayout files = card(parent);
        heading(files, "Research documents and versions");
        PendingStatus status = new PendingStatus(activity);
        files.addView(status);
        status.working("Loading documents…");
        String path = REGISTRATION_API + "/" + study + "/documents";
        activity.researcherRequest(this, path, null, true, (data, error) -> {
            if (!files.isAttachedToWindow()) return;
            if (error != null) { status.result(error, true); return; }
            try {
                JSONArray documents = new JSONArray(data);
                status.clear();
                if (documents.length() == 0) paragraph(files, "No research documents uploaded.", 14, MUTED, regular);
                for (int i = 0; i < documents.length(); i++) {
                    JSONObject file = documents.getJSONObject(i);
                    action(files, file.getString("filename") + " · Version " + file.getInt("version"), WHITE, NAVY,
                            () -> activity.downloadRequestAttachment(path + "/" + file.optString("id"),
                                    file.optString("filename"), file.optString("mime"), files::isAttachedToWindow,
                                    () -> status.result("Download failed. Try again.", true)));
                }
            } catch (JSONException invalid) { status.result("CRM returned invalid document details.", true); }
        });
    }

    private void loadRegisteredProgress(LinearLayout parent, int study) {
        LinearLayout progress = card(parent);
        PendingStatus status = new PendingStatus(activity);
        progress.addView(status);
        status.working("Loading saved research progress…");
        activity.researcherRequest(this, REGISTRATION_API + "/" + study + "/progress", null, true, (data, error) -> {
            if (!progress.isAttachedToWindow()) return;
            if (error != null) {
                status.result(error, true);
                action(progress, "Retry progress", WHITE, NAVY, () -> {
                    parent.removeView(progress);
                    loadRegisteredProgress(parent, study);
                });
                return;
            }
            try {
                JSONObject saved = new JSONObject(data);
                status.clear();
                ph.edu.umak.crm.app.research.RegisteredProgressView.append(progress, saved, false);
                loadResearchPlanning(progress, study);
            } catch (JSONException invalid) {
                status.result("CRM returned invalid progress. Try again.", true);
            }
        });
    }

    private void loadResearchPlanning(LinearLayout parent, int study) {
        LinearLayout planning = column();
        parent.addView(planning);
        PendingStatus status = new PendingStatus(activity);
        planning.addView(status);
        status.working("Calculating next tasks and estimated work…");
        activity.researcherRequest(this, REGISTRATION_API + "/" + study + "/planning", null, true, (data, error) -> {
            if (!planning.isAttachedToWindow()) return;
            if (error != null) {
                status.result(error, true);
                action(planning, "Retry planning", WHITE, NAVY, () -> {
                    parent.removeView(planning);
                    loadResearchPlanning(parent, study);
                });
                return;
            }
            try {
                JSONObject plan = new JSONObject(data);
                status.clear();
                ph.edu.umak.crm.app.research.RegisteredProgressView.appendPlanning(planning, plan);
            } catch (JSONException invalid) {
                status.result("CRM returned invalid planning results. Try again.", true);
            }
        });
    }

    private void showDraftPending(Button button, PendingStatus feedback) {
        activeDraftButton = button;
        activeDraftStatus = feedback;
        button.setText("Saving draft…");
        button.setEnabled(false);
        feedback.working("Saving your research draft to CRM…");
    }

    private void saveRegistrationDraft(Button save, PendingStatus feedback) {
        if (savingRegistrationDraft || submittingRegistration) return;
        String title = registration.getOrDefault("Research Title", "").trim();
        String summary = registration.getOrDefault("Research Abstract / Executive Summary", "").trim();
        String department = registration.getOrDefault("Department / College", "").trim();
        if (title.isEmpty() || summary.isEmpty() || department.isEmpty()
                || "Select your college or institute".equals(department)) {
            feedback.result("Enter the title, abstract and department before saving to CRM.", true);
            return;
        }
        JSONObject draft = json("title", title);
        put(draft, "abstract", summary);
        put(draft, "department", department);
        put(draft, "research_year", java.time.LocalDate.now().getYear());
        int session = registrationSession;
        savingRegistrationDraft = true;
        showDraftPending(save, feedback);
        activity.researcherRequest(this, "/api/v1/researcher/drafts" + (draftId == 0 ? "" : "/" + draftId),
                draft, true, (data, error) -> {
                    savingRegistrationDraft = false;
                    Button currentButton = activeDraftButton;
                    PendingStatus currentStatus = activeDraftStatus;
                    activeDraftButton = null;
                    activeDraftStatus = null;
                    currentButton.setText("Save to CRM");
                    currentButton.setEnabled(true);
                    if (session != registrationSession) {
                        currentStatus.clear();
                        if (error == null) loadStudies();
                        return;
                    }
                    if (error != null) {
                        currentStatus.result(error, true);
                        return;
                    }
                    try {
                        draftId = new JSONObject(data).getInt("id");
                        autosaveRegistration();
                        currentStatus.result("Core details saved to CRM as draft #" + draftId + ".", false);
                        loadStudies();
                    } catch (JSONException invalid) {
                        currentStatus.result("CRM draft response could not be read.", true);
                    }
                });
    }

    private void draftField(LinearLayout parent, java.util.Map<String, EditText> fields,
            String label, String hint, boolean multiline) {
        EditText field = input(parent, label, hint, multiline);
        field.setText(registration.getOrDefault(label, ""));
        fields.put(label, field);
        int session = registrationSession;
        field.addTextChangedListener(new DraftTextWatcher(() -> {
            if (session != registrationSession || !"registration".equals(detail) || registrationStep != 1) return;
            registration.put(label, field.getText().toString());
            autosaveRegistration();
        }));
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
                autosaveRegistration();
            });
        }
    }

    private void archive() {
        LinearLayout intro = hero("Research Archive", "Browse research studies from across all UMak colleges. All works are archived by the Center for Research Management.", true);
        tintText((TextView) intro.getChildAt(0), "Archive", YELLOW);
        LinearLayout filters = section(WHITE);
        EditText title = input(filters, "Title", "Search by title (e.g. Impact of AI)", false);
        title.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        EditText author = input(filters, "Author", "Search by author / proponent", false);
        author.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        EditText department = input(filters, "College", "All Colleges", false);
        department.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        EditText year = input(filters, "Year", "All Years (e.g. 2026)", false);
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
            searchArchive(results, title.getText().toString(), author.getText().toString().trim(),
                    department.getText().toString().trim(), selectedYear);
        });
        LinearLayout area = section(PAPER);
        area.addView(results);
        searchArchive(results, "", "", "", 0);
        LinearLayout journal = hero("UNIVERSITY", "The UMak Research Journal", true);
        paragraph(journal, "The official peer-reviewed academic journal of the University of Makati.", 13, WHITE, regular);
    }

    private void searchArchive(LinearLayout results, String titleQuery, String authorQuery, String department, int year) {
        int generation = ++searchGeneration;
        results.removeAllViews();
        TextView state = paragraph(results, "Searching the archive…", 13, MUTED, regular);

        // Logical search parsing (support search: title: "..." and author: "..." syntax)
        String parsedTitle = titleQuery.trim();
        String parsedAuthor = authorQuery.trim();
        int parsedYear = year;

        if (parsedTitle.toLowerCase(java.util.Locale.ROOT).contains("author:") || parsedTitle.toLowerCase(java.util.Locale.ROOT).contains("title:")) {
            java.util.regex.Matcher tm = java.util.regex.Pattern.compile("title:\\s*\"?([^\"]+?)\"?(?:\\s+author:|\\s+year:|$)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(parsedTitle);
            if (tm.find()) parsedTitle = tm.group(1).trim();
            java.util.regex.Matcher am = java.util.regex.Pattern.compile("author:\\s*\"?([^\"]+?)\"?(?:\\s+title:|\\s+year:|$)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(titleQuery);
            if (am.find() && am.group(1) != null) parsedAuthor = am.group(1).trim();
            java.util.regex.Matcher ym = java.util.regex.Pattern.compile("year:\\s*(\\d{4})", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(titleQuery);
            if (ym.find() && ym.group(1) != null) parsedYear = Integer.parseInt(ym.group(1));
        }

        JSONObject filters = json("keyword", parsedTitle);
        put(filters, "limit", 50);
        if (!department.isEmpty()) put(filters, "department", department);
        if (parsedYear != 0) put(filters, "year", parsedYear);

        final String finalAuthor = parsedAuthor.toLowerCase(java.util.Locale.ROOT);
        final int finalYear = parsedYear;
        final String finalTitle = parsedTitle.toLowerCase(java.util.Locale.ROOT);

        activity.researcherRequest(this, "/api/v1/research/search", filters, false, (data, error) -> {
            if (generation != searchGeneration) return;
            if (error != null) {
                if (activity.isResearcherPreview()) {
                    showPreviewArchiveResults(results, state, finalTitle, finalAuthor, department, finalYear);
                    return;
                }
                state.setText(error);
                return;
            }
            try {
                JSONArray records = new JSONArray(data);
                java.util.List<JSONObject> matching = new java.util.ArrayList<>();
                for (int index = 0; index < records.length(); index++) {
                    JSONObject record = records.getJSONObject(index);
                    if (!finalAuthor.isEmpty()) {
                        String authors = record.optString("authors", record.optString("author", record.optString("proponent", ""))).toLowerCase(java.util.Locale.ROOT);
                        String abstractText = record.optString("abstract", "").toLowerCase(java.util.Locale.ROOT);
                        if (!authors.contains(finalAuthor) && !abstractText.contains(finalAuthor)) {
                            continue;
                        }
                    }
                    matching.add(record);
                }
                state.setText(matching.isEmpty() ? "No results found." : matching.size() + (matching.size() == 1 ? " result shown" : " results shown"));
                for (JSONObject record : matching) {
                    LinearLayout item = card(results);
                    paragraph(item, record.optString("department") + "  ·  AY " + record.optString("research_year"), 11, MUTED, bold);
                    paragraph(item, record.optString("status"), 11, NAVY, bold);
                    paragraph(item, record.optString("title"), 18, ACCENT, serif);
                    String authorText = record.optString("authors", record.optString("author", ""));
                    if (!authorText.isEmpty()) {
                        paragraph(item, "Author: " + authorText, 12, NAVY, regular);
                    }
                    paragraph(item, record.optString("abstract"), 12, MUTED, regular);
                }
            } catch (JSONException invalid) {
                state.setText("CRM results could not be read.");
            }
        });
    }

    private void showPreviewArchiveResults(LinearLayout results, TextView state, String title, String author, String department, int year) {
        java.util.List<ph.edu.umak.crm.app.staff.registry.StaffRegistryData.RegistryRecordItem> mockRecords =
                ph.edu.umak.crm.app.staff.registry.StaffRegistryData.getMockRecords();
        java.util.List<ph.edu.umak.crm.app.staff.registry.StaffRegistryData.RegistryRecordItem> matched = new java.util.ArrayList<>();
        for (var rec : mockRecords) {
            if (!title.isEmpty() && !rec.title.toLowerCase(java.util.Locale.ROOT).contains(title)) continue;
            if (!author.isEmpty() && !rec.authorsDisplay.toLowerCase(java.util.Locale.ROOT).contains(author)
                    && (rec.facultyAdviser == null || !rec.facultyAdviser.toLowerCase(java.util.Locale.ROOT).contains(author))) continue;
            if (!department.isEmpty() && !rec.college.toLowerCase(java.util.Locale.ROOT).contains(department.toLowerCase(java.util.Locale.ROOT))) continue;
            if (year > 0 && !rec.academicPeriod.contains(String.valueOf(year))) continue;
            matched.add(rec);
        }
        state.setText(matched.isEmpty() ? "No results found." : matched.size() + (matched.size() == 1 ? " result shown" : " results shown"));
        for (var rec : matched) {
            LinearLayout item = card(results);
            paragraph(item, rec.college + "  ·  " + rec.academicPeriod, 11, MUTED, bold);
            paragraph(item, rec.stage, 11, NAVY, bold);
            paragraph(item, rec.title, 18, ACCENT, serif);
            paragraph(item, "Author: " + rec.authorsDisplay, 12, NAVY, regular);
            paragraph(item, "Registration ID: " + rec.id, 12, MUTED, regular);
        }
    }

    private void profile() {
        profileView = new ProfileView(activity, userProfile, new ProfileView.Listener() {
            @Override
            public void onEditProfile() {
                openDetail("edit_profile");
            }

            @Override
            public void onOpenStudyProgress(String studyState) {
                if (localDraftKey() != null) openDetail("registered_studies");
                else {
                    selectedProgressState = studyState;
                    openDetail("research_progress");
                }
            }

            @Override
            public void onSignOut() {
                activity.signOut();
            }
        });
        if (profileUpdatedSnackbar) {
            profileView.setShowSnackbar(true);
            profileUpdatedSnackbar = false;
        }
        body.addView(profileView.getContentLayout());
        if (localDraftKey() != null) {
            outline(section(PAPER), "Registered research", () -> openDetail("registered_studies"));
            LinearLayout draftSection = section(PAPER);
            heading(draftSection, "Research drafts");
            outline(draftSection, localDrafts().contains(localDraftKey()) ? "Resume local draft" : "Start a draft",
                    this::openLocalRegistration);
            if (studies != null) {
                for (int i = 0; i < studies.length(); i++) {
                    JSONObject study = studies.optJSONObject(i);
                    if (study != null && !study.optBoolean("is_registered") && "Draft".equals(study.optString("status"))
                            && "Registered".equals(study.optString("lifecycle_state"))) {
                        outline(draftSection, "CRM draft · " + study.optString("title"), () -> chooseCrmDraft(study));
                    }
                }
            }
        }
    }

    private void resetRegistration() {
        registrationSession++;
        registrationToken = java.util.UUID.randomUUID().toString();
        registration.clear();
        declarations.clear();
        documents.remove("Research Proposal");
        documents.remove("Ethical Clearance");
        draftId = 0;
        registrationStep = 0;
        if (localDraftKey() != null) {
            registration.put("Research Title", "");
            registration.put("Target Completion Date", "");
            registration.put("Faculty adviser", "");
            registration.put("Academic period", "Select academic period");
            registration.put("Department / College", userProfile.college.isEmpty()
                    ? "Select your college or institute" : userProfile.college);
            registration.put("Co-authors", "[]");
        }
    }

    private void chooseCrmDraft(JSONObject study) {
        if (savingRegistrationDraft || submittingRegistration) return;
        String saved = localDrafts().getString(localDraftKey(), null);
        if (saved == null) {
            openDraft(study);
            return;
        }
        try {
            if (new JSONObject(saved).optInt("crmId") == study.optInt("id")) {
                openLocalRegistration();
                return;
            }
        } catch (JSONException ignored) {
            // Allow the researcher to replace an unreadable local copy.
        }
        new android.app.AlertDialog.Builder(activity)
                .setTitle("Replace the draft on this device?")
                .setMessage("Opening this CRM draft will replace the local draft. Changes saved only on this device will be lost.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Replace", (dialog, which) -> openDraft(study)).show();
    }

    private void openDraft(JSONObject study) {
        resetRegistration();
        draftId = study.optInt("id");
        registration.put("Research Title", study.optString("title"));
        registration.put("Research Abstract / Executive Summary", study.optString("abstract"));
        registration.put("Department / College", study.optString("department"));
        autosaveRegistration();
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
        return account.isNull("display_name") ? (email.isEmpty() ? "Researcher" : email)
                : account.optString("display_name");
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
