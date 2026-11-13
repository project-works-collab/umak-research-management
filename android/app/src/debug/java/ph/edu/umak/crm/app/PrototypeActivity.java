package ph.edu.umak.crm.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.LinkedHashMap;
import java.util.Map;

/** Interactive design reference using sample data, available only in debug builds. */
public final class PrototypeActivity extends Activity {
    private static final int NAVY = 0xff131c4b;
    private static final int BLUE = 0xff082e50;
    private static final int ACCENT = 0xff195e9d;
    private static final int MUTED = 0xff536881;
    private static final int BORDER = 0xffdce4ee;
    private static final int BACK = 0xfff5f6f8;
    private static final int PALE = 0xffe5edf6;
    private static final int YELLOW = 0xfff8eb37;
    private static final int WHITE = 0xffffffff;
    private static final String PROJECT = "Impact of AI on Student Engagement";
    private static final String ATTACHMENT = "RESEARCH_PROPOSAL_v1 (13 MB)";
    private static final String[] TABS = {"Dashboard", "Requests", "Registry", "Manage", "Profile"};
    private static final int[] ICONS = {R.drawable.ic_staff_dashboard, R.drawable.ic_staff_requests,
            R.drawable.ic_staff_registry, R.drawable.ic_staff_manage, R.drawable.ic_staff_profile};
    private final Map<String, String> values = new LinkedHashMap<>();
    private final Map<String, Boolean> checks = new LinkedHashMap<>();
    private LinearLayout root;
    private LinearLayout content;
    private ScrollView scroll;
    private Typeface heading;
    private Typeface body;
    private Typeface bold;
    private int tab;
    private String screen;
    private int serviceStep;
    private int reviewFilter;
    private int completedRequests = 1 << 2;
    private final String[] requestServices = {"Ethics Review", "Plagiarism Check", "Research Registration"};
    private final String[] requestTitles = {PROJECT, "Machine Learning on Agriculture", "Quantum Computing Foundations"};
    private final String[] requestAuthors = {"LeBron R. James", "Juan L. Iniwan", "Jonard L. Wala"};
    private int requestIndex;
    private int extraFields;
    private int extraTasks;
    private int extraOutcomes;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        heading = getResources().getFont(R.font.marcellus_regular);
        body = getResources().getFont(R.font.metropolis_regular);
        bold = getResources().getFont(R.font.metropolis_semibold);
        if (state != null) {
            tab = state.getInt("tab");
            serviceStep = state.getInt("serviceStep");
            reviewFilter = state.getInt("reviewFilter");
            requestIndex = state.getInt("requestIndex");
            completedRequests = state.getInt("completedRequests", 1 << 2);
            extraFields = state.getInt("extraFields");
            extraTasks = state.getInt("extraTasks");
            extraOutcomes = state.getInt("extraOutcomes");
            Bundle input = state.getBundle("values");
            Bundle flags = state.getBundle("checks");
            if (input != null) for (String key : input.keySet()) values.put(key, input.getString(key));
            if (flags != null) for (String key : flags.keySet()) checks.put(key, flags.getBoolean(key));
        }
        render(state == null ? "dashboard" : state.getString("screen", "dashboard"));
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("tab", tab);
        state.putInt("serviceStep", serviceStep);
        state.putInt("reviewFilter", reviewFilter);
        state.putInt("requestIndex", requestIndex);
        state.putInt("completedRequests", completedRequests);
        state.putInt("extraFields", extraFields);
        state.putInt("extraTasks", extraTasks);
        state.putInt("extraOutcomes", extraOutcomes);
        state.putString("screen", screen);
        Bundle input = new Bundle();
        values.forEach(input::putString);
        state.putBundle("values", input);
        Bundle flags = new Bundle();
        checks.forEach(flags::putBoolean);
        state.putBundle("checks", flags);
    }

    @Override
    public void onBackPressed() {
        switch (screen) {
            case "workspace": render("requests"); break;
            case "analytics": render("dashboard"); break;
            case "registration":
            case "editor": render("registry"); break;
            case "milestones": render("editor"); break;
            default:
                if (tab == 0) super.onBackPressed();
                else selectTab(0);
        }
    }

    private void selectTab(int index) {
        tab = index;
        render(new String[]{"dashboard", "requests", "registry", "authoring", "profile"}[index]);
    }

    private void render(String destination) {
        screen = destination;
        root = column();
        root.setBackgroundColor(NAVY);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            applyInsets(view, insets);
            return insets;
        });
        root.setFocusableInTouchMode(true);
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), 0, dp(8), 0);
        TextView brand = text("Center for Research Management", 17, WHITE, heading);
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        Button notifications = button("", NAVY, WHITE, () -> dialog("Notifications", "You have no new notifications."));
        notifications.setContentDescription("Notifications");
        notifications.setMinWidth(dp(44));
        notifications.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_prototype_bell, 0, 0, 0);
        header.addView(notifications, new LinearLayout.LayoutParams(dp(44), dp(48)));
        root.addView(header);

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACK);
        content = column();
        content.setPadding(0, 0, 0, dp(24));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        switch (screen) {
            case "dashboard": dashboard(); break;
            case "requests": requests(); break;
            case "workspace": workspace(); break;
            case "registry": registry(); break;
            case "editor": editor(); break;
            case "registration": registration(); break;
            case "milestones": milestones(); break;
            case "analytics": analytics(); break;
            case "authoring": authoring(); break;
            case "profile": profile(); break;
            default: throw new IllegalArgumentException(screen);
        }

        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(dp(4), dp(4), dp(4), dp(4));
        nav.setBackgroundColor(WHITE);
        for (int index = 0; index < TABS.length; index++) {
            int target = index;
            Button item = button(TABS[index], tab == index ? PALE : WHITE, tab == index ? NAVY : MUTED,
                    () -> selectTab(target));
            item.setTextSize(11);
            item.setMinWidth(0);
            item.setMinimumWidth(0);
            item.setPadding(0, dp(6), 0, dp(6));
            item.setCompoundDrawablesWithIntrinsicBounds(0, ICONS[index], 0, 0);
            item.setCompoundDrawablePadding(dp(4));
            item.setSelected(tab == index);
            item.setBackground(shape(tab == index ? PALE : WHITE, 10, 0));
            nav.addView(item, new LinearLayout.LayoutParams(0, -2, 1));
        }
        root.addView(nav);
        setContentView(root);
        root.requestApplyInsets();
    }

    @SuppressWarnings("deprecation")
    private void applyInsets(View view, WindowInsets insets) {
        view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
    }

    private void dashboard() {
        LinearLayout hero = banner("Welcome back,\nLebron James");
        chip(hero, "CRM Staff", PALE, NAVY);
        metrics(new String[]{"5", "2", "38", "95", "8"},
                new String[]{"Pending Requests", "In Processing", "Approved This Month", "Active Studies", "UNIVERSITY Queue"},
                "Requires action", "Active evaluations", "Sep 2026", "All colleges", "Pending review");
        LinearLayout queue = card("Service Request Queue");
        line(queue, "As of Oct 9, 2026", MUTED);
        tableRow(queue, "REF NO.", "RESEARCHER", "SERVICE", "PRIORITY");
        tableRow(queue, "SR-2026-0601", "Cruz, Patricia L.", "Ethics Review", "High");
        tableRow(queue, "SR-2026-0598", "Bautista, Leon R.", "CRM Clearance", "Normal");
        tableRow(queue, "SR-2026-0589", "Santos, Maria A.", "CRM Consultation", "Normal");
        tableRow(queue, "SR-2026-0580", "Mendoza, Roy K.", "University Endorsement", "High");
        tableRow(queue, "SR-2026-0571", "Lim, Anne T.", "Manuscript Revision", "Normal");
        action(queue, "Process Selected", ACCENT, WHITE, () -> selectTab(1));
        action(queue, "Export Queue", WHITE, ACCENT, () -> local("Queue export"));
        LinearLayout colleges = card("Active Studies by College");
        bar(colleges, "CCSS", 38, 45);
        bar(colleges, "CET", 24, 45);
        bar(colleges, "CBA", 19, 45);
        bar(colleges, "CHS", 14, 45);
        LinearLayout actions = card("Staff Actions");
        action(actions, "Generate Monthly Report", WHITE, ACCENT, () -> render("analytics"));
        action(actions, "Manage Users & Roles", WHITE, ACCENT, () -> selectTab(4));
        action(actions, "UNIVERSITY Pipeline", WHITE, ACCENT, () -> selectTab(2));
        action(actions, "Archive Study Records", WHITE, ACCENT, () -> selectTab(2));
        action(actions, "System Settings", WHITE, ACCENT, () -> selectTab(3));
    }

    private void requests() {
        banner("Review Queue");
        LinearLayout tabs = new LinearLayout(this);
        tabs.setBackgroundColor(WHITE);
        String[] labels = {"Pending", "Under Review", "Completed"};
        for (int index = 0; index < labels.length; index++) {
            int filter = index;
            LinearLayout item = column();
            item.addView(button(labels[index], WHITE, index == reviewFilter ? NAVY : MUTED, () -> {
                reviewFilter = filter;
                render("requests");
            }));
            View indicator = new View(this);
            indicator.setBackgroundColor(index == reviewFilter ? YELLOW : BORDER);
            item.addView(indicator, new LinearLayout.LayoutParams(-1, dp(2)));
            tabs.addView(item, new LinearLayout.LayoutParams(0, -2, 1));
        }
        content.addView(tabs);
        LinearLayout search = padded();
        EditText query = field(search, "queueSearch", "Search papers, DOI, or authors…", "");
        LinearLayout results = column();
        content.addView(results);
        Runnable refresh = () -> {
            results.removeAllViews();
            String term = query.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            for (int index = 0; index < requestServices.length; index++) {
                String rowText = requestServices[index] + " " + requestTitles[index] + " " + requestAuthors[index];
                int status = isCompleted(index) ? 2 : index == 1 ? 1 : 0;
                if (status != reviewFilter || !rowText.toLowerCase(java.util.Locale.ROOT).contains(term)) continue;
                requestCard(results, index, labels[status]);
            }
            if (results.getChildCount() == 0) card(results, "No matching requests");
        };
        query.addTextChangedListener(new TextChange(refresh));
        refresh.run();
        LinearLayout actions = padded();
        action(actions, "View Assigned Request", YELLOW, NAVY, () -> openWorkspace(0));
        action(actions, "Refresh", WHITE, NAVY, refresh);
    }

    private void requestCard(LinearLayout parent, int index, String status) {
        LinearLayout row = card(parent, requestServices[index]);
        line(row, requestTitles[index], ACCENT);
        line(row, "Submitted Oct 26, 2026\n" + requestAuthors[index] + " (Student)", MUTED);
        chip(row, status, "Completed".equals(status) ? 0xffe8f5ec : 0xfffff6de,
                "Completed".equals(status) ? 0xff22743b : 0xff8c5900);
        row.setOnClickListener(view -> openWorkspace(index));
    }

    private boolean isCompleted(int index) {
        return (completedRequests & (1 << index)) != 0;
    }

    private void openWorkspace(int index) {
        requestIndex = index;
        render("workspace");
    }

    private void workspace() {
        banner("Request Workspace");
        LinearLayout request = card("Request Workspace");
        line(request, requestServices[requestIndex] + " · " + requestTitles[requestIndex], ACCENT);
        line(request, "Submitted Oct 29, 2026 | " + requestAuthors[requestIndex] + " (Student)", MUTED);
        attachment(request);
        LinearLayout checklist = card("Evaluation Checklist");
        check(checklist, "Informed consent form included", true);
        check(checklist, "Research methodology is clear", true);
        check(checklist, "Participant data privacy addressed", false);
        check(checklist, "Signature of lead & co-authors", false);
        LinearLayout feedback = card("Feedback & Action");
        field(feedback, "feedback" + requestIndex, "Evaluation notes / feedback", "");
        choices(feedback, "Recommendation", "Needs Revision", "Recommend Approval", "Return to Applicant");
        LinearLayout actions = padded();
        action(actions, "Submit", NAVY, WHITE, () -> {
            if (isCompleted(requestIndex)) {
                new AlertDialog.Builder(this).setTitle("Stale Data Conflict")
                        .setMessage("This request was updated. Reload before saving.")
                        .setPositiveButton("Confirm", (dialog, which) -> render("requests")).show();
            } else {
                completedRequests |= 1 << requestIndex;
                reviewFilter = 2;
                render("requests");
            }
        });
        action(actions, "Save as Draft", WHITE, NAVY, () -> local("Review draft saved for this preview session"));
    }

    private void registry() {
        banner("Research Registry");
        LinearLayout search = padded();
        EditText query = field(search, "registrySearch", "Search papers, DOI, or authors…", "");
        LinearLayout results = column();
        content.addView(results);
        Runnable refresh = () -> {
            results.removeAllViews();
            String term = query.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            for (int index = 0; index < requestTitles.length; index++) {
                String record = requestTitles[index] + " " + requestAuthors[index];
                if (!record.toLowerCase(java.util.Locale.ROOT).contains(term)) continue;
                int selected = index;
                LinearLayout row = card(results, "Research Project");
                line(row, requestTitles[index], ACCENT);
                line(row, "Registration ID: REG-2026-0" + (89 + index), MUTED);
                line(row, "Publication date: Oct 2026\nAuthors: " + requestAuthors[index], MUTED);
                if (index == 0) attachment(row);
                action(row, "Open Record", WHITE, ACCENT, () -> {
                    requestIndex = selected;
                    render("editor");
                });
            }
            if (results.getChildCount() == 0) card(results, "No matching records");
        };
        query.addTextChangedListener(new TextChange(refresh));
        refresh.run();
        LinearLayout actions = padded();
        action(actions, "Submit New Registration", NAVY, WHITE, () -> render("registration"));
        action(actions, "Export Directory", WHITE, NAVY, () -> local("Directory export"));
    }

    private void editor() {
        banner("Record Editor");
        LinearLayout project = card("Research Project");
        line(project, requestTitles[requestIndex], ACCENT);
        line(project, "Registration ID: REC-2026-0" + (89 + requestIndex), MUTED);
        line(project, "Last edit: Oct 2026 by Staff", MUTED);
        if (requestIndex == 0) {
            LinearLayout duplicate = card("Potential Duplicate");
            duplicate.setBackground(shape(WHITE, 12, 0xffdf4056));
            line(duplicate, "A historical entry titled “AI Tools in Student Engagement” has a 92% title match.", NAVY);
            action(duplicate, "Compare Records", WHITE, NAVY, () -> dialog("Compare Records",
                    PROJECT + "\n\nAI Tools in Student Engagement\nREG-2024-012 · 2024"));
        }
        LinearLayout record = card("Request Workspace");
        line(record, "Title: " + requestTitles[requestIndex], NAVY);
        label(record, "Historical Entry Check");
        check(record, "Research methodology is clear", true);
        label(record, "Principal Author");
        field(record, "author" + requestIndex, "Principal Author", requestAuthors[requestIndex]);
        label(record, "Co-Authors");
        field(record, "coauthors" + requestIndex, "Co-Authors", "Juan D. Iniwan, Peter Parker");
        label(record, "Historical Field: Source Institution");
        field(record, "institution" + requestIndex, "Source Institution", "University of Makati");
        LinearLayout history = card("Historical Submissions & Revisions");
        line(history, "• Revision v2 Uploaded · Oct 29, 2026\n• Duplication Flag Triggered · Oct 28, 2026\n• Historical Record Imported · Oct 15, 2026", NAVY);
        LinearLayout actions = padded();
        action(actions, "Milestones and Outcomes", WHITE, NAVY, () -> render("milestones"));
        action(actions, "Save", NAVY, WHITE, () -> render("registry"));
        action(actions, "Cancel", WHITE, NAVY, () -> render("registry"));
    }

    private void registration() {
        banner("New Registration");
        LinearLayout record = card("Research Project");
        field(record, "registrationTitle", "Research title", "");
        field(record, "registrationAuthor", "Principal Author", "");
        field(record, "registrationInstitution", "Source Institution", "");
        action(record, "Save Draft", NAVY, WHITE, () -> render("registry"));
        action(record, "Cancel", WHITE, NAVY, () -> render("registry"));
    }

    private void milestones() {
        banner("Milestone and\nOutcomes");
        LinearLayout timeline = card("Research Project");
        line(timeline, requestTitles[requestIndex], ACCENT);
        line(timeline, "Overall Completion: 65%\nActive Stage 3 of 5", MUTED);
        stage(timeline, "✓", "Proposal Submission & Defense", "Approved · Jun 15, 2026");
        stage(timeline, "✓", "Ethics & Consent Clearance", "Cleared · Jul 22, 2026");
        stage(timeline, "●", "Data Collection & Analysis", "Target: Nov 15, 2026 · Completion 50%");
        chip(timeline, "In Progress", 0xffffc460, NAVY);
        check(timeline, "Surveys Administered", true);
        check(timeline, "Data Analytics Modeling", false);
        for (int index = 0; index < extraTasks; index++) field(timeline, "task" + index, "Subtask", "");
        action(timeline, "Add Sub-Task", WHITE, ACCENT, () -> {
            extraTasks++;
            render("milestones");
        });
        stage(timeline, "○", "Manuscript Drafting & Peer Review", "Locked · Pending Stage 3");
        stage(timeline, "○", "Final Output & Registration", "Locked");
        LinearLayout deliverables = card("Target Deliverable & Outcomes");
        field(deliverables, "outcome0", "Deliverable", "Peer-Reviewed Journal Publication");
        field(deliverables, "outcome1", "Deliverable", "Conference Presentation");
        field(deliverables, "outcome2", "Deliverable", "Final Research Manuscript");
        for (int index = 0; index < extraOutcomes; index++) {
            field(deliverables, "newOutcome" + index, "Deliverable / outcome", "");
        }
        action(deliverables, "Add deliverable / outcome", WHITE, ACCENT, () -> {
            extraOutcomes++;
            render("milestones");
        });
        LinearLayout actions = padded();
        action(actions, "Save", NAVY, WHITE, () -> render("editor"));
        action(actions, "Close", WHITE, NAVY, () -> render("editor"));
    }

    private void authoring() {
        LinearLayout hero = banner("Service Authoring");
        String[] steps = {"Service Details", "Advanced Form Rules", "Access & Publishing"};
        String[] summaries = {"Create a research service catalog entry and configure its request form.",
                "Configure validation rules, file attachments and submission constraints.",
                "Set visibility, reviewer access and publishing options."};
        line(hero, summaries[serviceStep], 0xffc2d3e4);
        View progress = new View(this);
        progress.setBackgroundColor(0xffccdaec);
        hero.addView(progress, new LinearLayout.LayoutParams(dp(80 * (serviceStep + 1)), dp(3)));
        line(hero, "Step " + (serviceStep + 1) + " of 3: " + steps[serviceStep], WHITE);
        if (serviceStep == 0) {
            LinearLayout details = card("Service Details");
            label(details, "Service Name / Title");
            field(details, "serviceTitle", "e.g., Plagiarism Checker", "");
            choices(details, "Service Category", "Ethics Clearance", "Plagiarism Check", "Research Registration");
            label(details, "Service Description");
            field(details, "serviceDescription", "Describe the service, prerequisites and outcomes", "");
            LinearLayout form = card("Form Fields");
            formField(form, "Input: Applicant Name");
            formField(form, "Dropdown: College / Department");
            formField(form, "File Upload: Research Proposal");
            for (int index = 0; index < extraFields; index++) field(form, "formField" + index, "Field name", "");
            action(form, "Add Form Field", WHITE, ACCENT, () -> {
                extraFields++;
                render("authoring");
            });
        } else if (serviceStep == 1) {
            LinearLayout validation = card("Attachment Validation");
            line(validation, "Rule for File Upload: Research Proposal", NAVY);
            field(validation, "formats", "Allowed file formats", "PDF, DOCX");
            choices(validation, "Maximum File Size Limit", "20 MB", "10 MB", "5 MB");
            check(validation, "Require Digital Signature", false);
            LinearLayout rules = card("Workflow & Rules");
            choices(rules, "Default Assignee Group", "Ethics Review Committee", "CRM Staff");
            choices(rules, "Prerequisite Service Required (Optional)", "No prerequisite", "Research Registration");
            check(rules, "Allow Multiple Ongoing Submissions per user", false);
            check(rules, "Enforce turn-around-time countdown alert", true);
            check(rules, "Enable email notification to applicant", true);
        } else {
            LinearLayout access = card("Access Control");
            label(access, "Target Audience Visibility");
            check(access, "Visible to Student Researchers", true);
            check(access, "Visible to Faculty Researchers", true);
            check(access, "Visible to External Applicants", false);
            check(access, "Staff Internal use only", false);
            choices(access, "Authorized Reviewer Role", "Ethics Review Committee", "CRM Staff");
            LinearLayout launch = card("Launch Status");
            choices(launch, "Service Status", "Active", "Draft");
            choices(launch, "Publish Date", "Immediate", "Scheduled");
            label(launch, "Publish Notices (Optional)");
            field(launch, "publishNotice", "Announce on the dashboard", "");
        }
        LinearLayout actions = padded();
        action(actions, "Continue", YELLOW, NAVY, () -> {
            if (serviceStep < 2) {
                serviceStep++;
                render("authoring");
            } else {
                dialog("Service Authoring", "Publishing is simulated in this design preview.");
            }
        });
        if (serviceStep > 0) action(actions, "Back", PALE, NAVY, () -> {
            serviceStep--;
            render("authoring");
        });
        action(actions, "Save as Draft", WHITE, NAVY, () -> local("Draft kept for this preview session"));
    }

    private void profile() {
        LinearLayout account = card("");
        View cover = new View(this);
        cover.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{NAVY, 0xff2d3450}));
        account.addView(cover, new LinearLayout.LayoutParams(-1, dp(58)));
        TextView avatar = text("JA", 20, ACCENT, bold);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(shape(YELLOW, 40, WHITE));
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(52), dp(52));
        avatarParams.setMargins(dp(12), -dp(27), 0, 0);
        account.addView(avatar, avatarParams);
        account.addView(text("Lebron James", 23, ACCENT, bold));
        chip(account, "Staff", PALE, NAVY);
        profileValue(account, "POSITION", "Staff");
        profileValue(account, "OFFICE", "CRM");
        profileValue(account, "EMPLOYEE ID", "K12345678");
        profileValue(account, "EMAIL", "l.james@example.test");
        metrics(new String[]{"0", "0", "0", "0"},
                new String[]{"Total Studies", "Active Studies", "Service Requests", "Publications"});
        LinearLayout nav = card("Navigation");
        action(nav, "Staff Dashboard", WHITE, ACCENT, () -> selectTab(0));
        action(nav, "CRM Services", WHITE, ACCENT, () -> selectTab(3));
        action(nav, "Repository", WHITE, ACCENT, () -> selectTab(2));
        metrics(new String[]{"284", "38", "95", "8"},
                new String[]{"Requests Processed", "This Month", "Studies Monitored", "UNIVERSITY"});
        LinearLayout permissions = card("Role & System Permissions");
        for (String permission : new String[]{"Service Queue Management", "Manuscript Evaluation",
                "Document Compliance Verification", "Statistical Report Generation"}) {
            chip(permissions, "•  " + permission, 0xffe9f2dc, 0xff416b32);
        }
        chip(permissions, "•  User Role Management", 0xffefeff1, MUTED);
        chip(permissions, "•  UNIVERSITY Pipeline Admin", 0xffefeff1, MUTED);
        LinearLayout settings = card("Account Settings");
        profileValue(settings, "EMAIL", "l.james@example.test");
        profileValue(settings, "ACCOUNT STATUS", "Active");
        profileValue(settings, "LAST LOGIN", "This session");
        profileValue(settings, "PASSWORD", "••••••••");
        action(settings, "Sign Out", WHITE, 0xffb33042, () -> selectTab(0));
        action(settings, "Connect to CRM Server", WHITE, NAVY, () -> startActivity(new Intent(this, MainActivity.class)));
    }

    private void analytics() {
        banner("Analytics");
        LinearLayout analytics = card("Utilization Analytics");
        line(analytics, "Active Projects: 124\nCompleted: 67", NAVY);
        choices(analytics, "Date Range", "Jan – Oct 2026", "Jul – Oct 2026", "Oct 2026");
        LinearLayout chart = column();
        chart.setPadding(dp(14), dp(12), dp(14), dp(12));
        chart.setBackground(shape(BLUE, 10, 0));
        analytics.addView(chart);
        line(chart, "Submission Volume", WHITE);
        chart.addView(new SubmissionChart(), new LinearLayout.LayoutParams(-1, dp(110)));
        tableRow(chart, "Jan", "Apr", "Jul", "Oct").setBackgroundColor(PALE);
        line(chart, "172 Total Submissions     34 Days Avg. Turnaround", WHITE);
        LinearLayout colleges = card("Submission by College");
        bar(colleges, "Computer Science", 58, 60);
        bar(colleges, "Allied Health", 34, 60);
        bar(colleges, "Business Administration", 30, 60);
        LinearLayout export = card("Export Report");
        LinearLayout formats = new LinearLayout(this);
        export.addView(formats);
        check(formats, "PDF", true);
        check(formats, "EXCEL", false);
        check(formats, "CSV", false);
        label(export, "Recipient’s Email Address");
        field(export, "recipient", "sample@umak.edu.ph", "");
        LinearLayout actions = padded();
        action(actions, "Generate Report", NAVY, WHITE, () -> local("Report generation is simulated"));
        action(actions, "Cancel", WHITE, NAVY, () -> render("dashboard"));
    }

    private void attachment(LinearLayout group) {
        action(group, "▤  " + ATTACHMENT, WHITE, ACCENT,
                () -> dialog("Research Proposal", "Sample attachment shown in the prototype."));
    }

    private void stage(LinearLayout group, String marker, String name, String detail) {
        line(group, marker + "   " + name, NAVY);
        line(group, "     " + detail, MUTED);
    }

    private void formField(LinearLayout group, String name) {
        line(group, name, NAVY);
        chip(group, "Required", 0xffd8f254, NAVY);
    }

    private void profileValue(LinearLayout group, String name, String value) {
        label(group, name);
        line(group, value, ACCENT);
    }

    private void metrics(String[] numbers, String[] labels, String... descriptions) {
        int[] colors = {0xffbd2536, ACCENT, 0xff4a7334, 0xff8a6319, 0xff6845a9};
        int[] metricIcons = {R.drawable.ic_staff_requests, R.drawable.ic_staff_manage,
                R.drawable.ic_staff_requests, R.drawable.ic_staff_registry, R.drawable.ic_staff_registry};
        for (int index = 0; index < numbers.length; index += 2) {
            LinearLayout row = new LinearLayout(this);
            add(content, row, 16, 12);
            for (int column = 0; column < 2; column++) {
                int item = index + column;
                LinearLayout tile = column();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1);
                if (column > 0) params.leftMargin = dp(10);
                row.addView(tile, params);
                if (item == numbers.length) break;
                tile.setPadding(dp(14), dp(14), dp(12), dp(12));
                tile.setBackground(shape(WHITE, 12, BORDER));
                android.widget.ImageView icon = new android.widget.ImageView(this);
                icon.setImageResource(metricIcons[item % metricIcons.length]);
                icon.setPadding(dp(5), dp(5), dp(5), dp(5));
                icon.setBackground(shape(0xffedf1f6, 7, 0));
                tile.addView(icon, new LinearLayout.LayoutParams(dp(30), dp(30)));
                TextView amount = text(numbers[item], 29, colors[item % colors.length], bold);
                amount.setPadding(0, dp(5), 0, dp(2));
                tile.addView(amount);
                tile.addView(text(labels[item], 13, NAVY, bold));
                if (item < descriptions.length) line(tile, descriptions[item], MUTED);
            }
        }
    }

    private LinearLayout tableRow(LinearLayout group, String... columns) {
        LinearLayout row = new LinearLayout(this);
        row.setPadding(0, dp(8), 0, dp(8));
        group.addView(row);
        for (String value : columns) {
            TextView text = text(value, 10, NAVY, bold);
            text.setPadding(dp(2), 0, dp(2), 0);
            row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        }
        return row;
    }

    private void bar(LinearLayout group, String name, int value, int total) {
        line(group, name + "  ·  " + value, ACCENT);
        LinearLayout track = new LinearLayout(this);
        track.setBackground(shape(PALE, 8, 0));
        group.addView(track, new LinearLayout.LayoutParams(-1, dp(5)));
        View fill = new View(this);
        fill.setBackground(shape(0xffeda132, 8, 0));
        track.addView(fill, new LinearLayout.LayoutParams(0, -1, value));
        track.addView(new View(this), new LinearLayout.LayoutParams(0, -1, total - value));
    }

    private LinearLayout banner(String title) {
        LinearLayout banner = column();
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setMinimumHeight(dp(140));
        banner.setPadding(dp(24), dp(24), dp(24), dp(24));
        banner.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{BLUE, 0xff13364e}));
        banner.addView(text(title, 31, WHITE, heading));
        content.addView(banner);
        return banner;
    }

    private LinearLayout card(String title) {
        return card(content, title);
    }

    private LinearLayout card(LinearLayout parent, String title) {
        LinearLayout card = column();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(shape(WHITE, 12, BORDER));
        if (!title.isEmpty()) {
            TextView label = text(title, 18, ACCENT, bold);
            label.setPadding(0, 0, 0, dp(6));
            card.addView(label);
        }
        add(parent, card, 16, 12);
        return card;
    }

    private LinearLayout padded() {
        LinearLayout group = column();
        add(content, group, 16, 12);
        return group;
    }

    private void label(LinearLayout group, String value) {
        TextView label = text(value, 12, MUTED, bold);
        label.setPadding(0, dp(10), 0, dp(2));
        group.addView(label);
    }

    private void line(LinearLayout group, String value, int color) {
        TextView line = text(value, 13, color, body);
        line.setPadding(0, dp(4), 0, dp(4));
        group.addView(line);
    }

    private void chip(LinearLayout group, String label, int background, int color) {
        TextView chip = text(label, 12, color, bold);
        chip.setPadding(dp(10), dp(5), dp(10), dp(5));
        chip.setBackground(shape(background, 20, 0));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.topMargin = dp(6);
        group.addView(chip, params);
    }

    private EditText field(LinearLayout group, String key, String hint, String initial) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(values.getOrDefault(key, initial));
        input.setTypeface(body);
        input.setTextSize(14);
        input.setTextColor(NAVY);
        input.setHintTextColor(MUTED);
        input.setMinHeight(dp(48));
        input.setPadding(dp(12), dp(8), dp(12), dp(8));
        input.setBackground(shape(WHITE, 8, BORDER));
        input.addTextChangedListener(new TextChange(() -> values.put(key, input.getText().toString())));
        add(group, input, 0, 6);
        return input;
    }

    private void choices(LinearLayout group, String name, String... options) {
        label(group, name);
        Spinner spinner = new Spinner(this, Spinner.MODE_DROPDOWN);
        spinner.setPadding(dp(12), 0, dp(8), 0);
        spinner.setMinimumHeight(dp(48));
        spinner.setBackground(shape(WHITE, 8, BORDER));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, options) {
            @Override public View getView(int position, View recycled, android.view.ViewGroup parent) {
                TextView value = (TextView) super.getView(position, recycled, parent);
                value.setTypeface(body);
                value.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
                value.setTextColor(NAVY);
                value.setTextSize(14);
                return value;
            }
        };
        spinner.setAdapter(adapter);
        int selected = java.util.Arrays.asList(options).indexOf(values.get(name));
        if (selected >= 0) spinner.setSelection(selected);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int index, long id) {
                values.put(name, options[index]);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        add(group, spinner, 0, 6);
    }

    private void check(LinearLayout group, String label, boolean selected) {
        CheckBox box = new CheckBox(this);
        box.setText(label);
        box.setTypeface(body);
        box.setTextColor(NAVY);
        box.setButtonTintList(android.content.res.ColorStateList.valueOf(NAVY));
        box.setChecked(checks.getOrDefault(label, selected));
        box.setOnCheckedChangeListener((view, value) -> checks.put(label, value));
        group.addView(box);
    }

    private void action(LinearLayout group, String label, int background, int foreground, Runnable run) {
        add(group, button(label, background, foreground, run), 0, 8);
    }

    private Button button(String label, int background, int foreground, Runnable run) {
        Button button = new Button(this, null, android.R.attr.borderlessButtonStyle);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(foreground);
        button.setTypeface(bold);
        button.setMinHeight(dp(46));
        button.setPadding(dp(10), dp(8), dp(10), dp(8));
        button.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(0x26131c4b),
                shape(background, 8, background == WHITE ? BORDER : 0), null));
        button.setOnClickListener(view -> run.run());
        return button;
    }

    private TextView text(String value, int size, int color, Typeface font) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTypeface(font);
        text.setTextColor(color);
        return text;
    }

    private LinearLayout column() {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        return group;
    }

    private void add(LinearLayout group, View child, int sides, int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dp(sides), dp(top), dp(sides), 0);
        group.addView(child, params);
    }

    private GradientDrawable shape(int fill, int radius, int outline) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        if (outline != 0) drawable.setStroke(dp(1), outline);
        return drawable;
    }

    private void local(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void dialog(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("Close", null).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class TextChange implements android.text.TextWatcher {
        private final Runnable action;
        TextChange(Runnable action) { this.action = action; }
        @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence text, int start, int before, int count) { action.run(); }
        @Override public void afterTextChanged(android.text.Editable text) {}
    }

    private final class SubmissionChart extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final int[] samples = {9, 28, 34, 25, 44, 61, 58, 46, 56, 81, 68, 72, 84};
        SubmissionChart() {
            super(PrototypeActivity.this);
            setContentDescription("Submission volume rises from 9 in January to 84 at the end of the period.");
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setColor(0xff305572);
            paint.setStrokeWidth(dp(1));
            for (int row = 1; row <= 4; row++) {
                float y = row * getHeight() / 5f;
                canvas.drawLine(0, y, getWidth(), y, paint);
            }
            path.reset();
            for (int index = 0; index < samples.length; index++) {
                float x = index * getWidth() / (samples.length - 1f);
                float y = getHeight() * (1f - samples[index] / 100f);
                if (index == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }
            paint.setColor(YELLOW);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            canvas.drawPath(path, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}
