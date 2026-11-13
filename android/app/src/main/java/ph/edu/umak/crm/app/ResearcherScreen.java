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

    ResearcherScreen(MainActivity activity, JSONObject account, String initialTab) {
        super(activity);
        this.activity = activity;
        this.account = account;
        tab = java.util.Arrays.asList(TABS).contains(initialTab) ? initialTab : "Dashboard";
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
        bell.setContentDescription("Announcements");
        bell.setOnClickListener(view -> openDetail("announcements"));
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
        if ("guidelines".equals(destination)) tab = "Services";
        else if (!"announcements".equals(destination)) tab = "Requests";
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
            case "request": serviceRequest(); return;
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
        TextView signIn = paragraph(card, "Sign In →", 14, ACCENT, bold, 0);
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
        action(journal, "Learn More →", YELLOW, NAVY, () -> select("Research"));
    }

    private void services() {
        hero("Research Services &\nProcedures", "The Center for Research Management provides eight core services to support the research journey of every UMAk student and faculty member.", true);
        LinearLayout steps = section(ACCENT);
        String[] instructions = {"Log in to your account", "Navigate to Service Requests", "Select and fill out the service form", "Upload required documents", "Track status in real time"};
        for (int index = 0; index < instructions.length; index++) {
            paragraph(steps, (index + 1) + "   " + instructions[index], 13, WHITE, bold);
        }
        LinearLayout directory = section(PAPER);
        for (int index = 0; index < ResearcherCatalog.SERVICES.length; index++) {
            String[] service = ResearcherCatalog.SERVICES[index];
            LinearLayout card = card(directory);
            paragraph(card, String.format(java.util.Locale.ROOT, "SVC-%02d   ·   %s", index + 1, service[1]), 10, MUTED, bold);
            heading(card, service[0]);
            paragraph(card, service[2], 13, MUTED, regular);
            paragraph(card, "REQUIREMENTS", 10, ACCENT, bold);
            paragraph(card, service[3], 12, MUTED, regular);
        }
        outline(directory, "View Guidelines", () -> openDetail("guidelines"));
        LinearLayout footer = hero("Submit a Service\nRequest Today", "Log in with your UMAk credentials to access CRM services.", true);
        action(footer, isSignedIn() ? "Submit Service Request" : "Sign In", YELLOW, NAVY,
                isSignedIn() ? () -> openDetail("request") : activity::startAccess);
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
        hero("Service Requests", "View your requests and start a new submission.", true);
        LinearLayout content = section(PAPER);
        action(content, "Submit Service Request", NAVY, WHITE, () -> openDetail("request"));
        outline(content, "Research Registration", () -> openDetail("registration"));
        outline(content, "Upload Manuscript", () -> openDetail("manuscript"));
        LinearLayout history = card(content);
        heading(history, "Personal Service Request History");
        paragraph(history, "Consultation requests", 13, MUTED, regular);
        TextView state = paragraph(history, "Loading requests…", 13, MUTED, regular);
        activity.researcherRequest(this, "/api/v1/consultations", null, true, (data, error) -> {
            if (error != null) {
                state.setText(error);
                return;
            }
            try {
                consultations = new JSONArray(data);
                if (consultations.length() == 0) state.setText("No consultation requests yet.");
                else history.removeView(state);
                for (int index = 0; index < consultations.length(); index++) {
                    JSONObject ticket = consultations.getJSONObject(index);
                    paragraph(history, ticket.optString("service_type") + " · #" + ticket.optInt("id"),
                            14, BLUE, bold);
                    paragraph(history, ticket.optString("status"), 12, MUTED, regular);
                }
            } catch (JSONException invalid) {
                state.setText("CRM requests could not be read.");
            }
        });
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
        if (registrationStep == 0) {
            LinearLayout project = card(content);
            heading(project, "Project Details");
            draftField(project, fields, "Research Title", "Enter the full title of your study", false);
            draftField(project, fields, "Research Type", "Applied Research", false);
            draftField(project, fields, "Target Completion Date", "YYYY-MM-DD", false);
            LinearLayout team = card(content);
            heading(team, "Funding & Collaborators");
            draftField(team, fields, "Department / College", "College name", false);
            draftField(team, fields, "Lead Researcher / Author", "Your name, student/faculty ID", false);
            if (registration.containsKey("Co-Author / Collaborator")) {
                draftField(team, fields, "Co-Author / Collaborator", "Name", false);
            } else {
                outline(team, "Add Co-Author / Collaborator", () -> {
                    collect(fields);
                    registration.put("Co-Author / Collaborator", "");
                    render();
                });
            }
        } else if (registrationStep == 1) {
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
            action(content, "Submit", NAVY, WHITE,
                    () -> feedback.setText("Final registration submission is currently unavailable."));
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
            feedback.setText("Enter the title, abstract and department before saving to CRM.");
            return;
        }
        JSONObject draft = json("title", title);
        put(draft, "abstract", summary);
        put(draft, "department", department);
        put(draft, "research_year", java.time.LocalDate.now().getYear());
        save.setEnabled(false);
        activity.researcherRequest(this, "/api/v1/researcher/drafts" + (draftId == 0 ? "" : "/" + draftId),
                draft, true, (data, error) -> {
                    save.setEnabled(true);
                    if (error != null) {
                        feedback.setText(error);
                        return;
                    }
                    try {
                        draftId = new JSONObject(data).getInt("id");
                        feedback.setText("Core research details saved as a draft.");
                        loadStudies();
                    } catch (JSONException invalid) {
                        feedback.setText("CRM draft response could not be read.");
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
        LinearLayout content = section(PAPER);
        LinearLayout identity = card(content);
        TextView strip = paragraph(identity, name().isEmpty() ? "?" : name().substring(0, 1).toUpperCase(java.util.Locale.ROOT),
                27, YELLOW, bold);
        strip.setBackground(shape(NAVY, 8, 0));
        strip.setPadding(dp(12), dp(14), dp(12), dp(14));
        paragraph(identity, name(), 25, ACCENT, bold);
        paragraph(identity, "faculty".equals(account.optString("role")) ? "Faculty Researcher" : "Student Researcher", 12, ACCENT, bold);
        paragraph(identity, "EMAIL", 10, MUTED, bold);
        paragraph(identity, account.optString("email"), 13, BLUE, regular);
        LinearLayout row = new LinearLayout(activity);
        LinearLayout left = column();
        LinearLayout right = column();
        LayoutParams leftParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        leftParams.setMarginEnd(dp(8));
        row.addView(left, leftParams);
        row.addView(right, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        content.addView(row);
        metric(left, R.drawable.ic_staff_profile, studies == null ? "—" : String.valueOf(studies.length()), "Total Studies", "Registered with CRM");
        metric(right, R.drawable.ic_staff_registry, countStudies("In-Progress"), "Active Studies", "In progress");
        metric(left, R.drawable.ic_staff_requests, consultations == null ? "—" : String.valueOf(consultations.length()), "Service Requests", "Consultations");
        metric(right, R.drawable.ic_researcher_services, countStudies("Published"), "Publications", "Peer-reviewed");
        LinearLayout myStudies = card(content);
        heading(myStudies, "My Research Studies");
        if (studies == null || studies.length() == 0) paragraph(myStudies,
                studyError != null ? studyError : studies == null ? "Loading research studies…" : "No studies registered yet.", 13, MUTED, regular);
        else for (int index = 0; index < studies.length(); index++) {
            JSONObject study = studies.optJSONObject(index);
            if (study == null) continue;
            if ("Draft".equals(study.optString("status")) && "Registered".equals(study.optString("lifecycle_state"))) {
                outline(myStudies, study.optString("title") + " · Draft", () -> openDraft(study));
            } else {
                outline(myStudies, study.optString("title") + " · " + study.optString("lifecycle_state"), () -> {
                    reviewStudy = study;
                    openDetail("review");
                });
            }
        }
        outline(myStudies, "Research Registration", () -> openDetail("registration"));
        LinearLayout settings = card(content);
        heading(settings, "Account Settings");
        paragraph(settings, "EMAIL ADDRESS", 10, MUTED, bold);
        paragraph(settings, account.optString("email"), 13, BLUE, regular);
        paragraph(settings, "ACCOUNT STATUS", 10, MUTED, bold);
        paragraph(settings, "Active", 13, BLUE, regular);
        paragraph(settings, "LAST LOGIN", 10, MUTED, bold);
        paragraph(settings, "This session", 13, BLUE, regular);
        action(content, "Sign Out", WHITE, 0xffb52934, activity::signOut);
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
