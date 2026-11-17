package ph.edu.umak.crm.app;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.FrameLayout;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import android.text.InputFilter;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

import ph.edu.umak.crm.app.staff.ManageHubView;
import ph.edu.umak.crm.app.staff.StaffData;
import ph.edu.umak.crm.app.staff.StaffDialogs;
import ph.edu.umak.crm.app.staff.accounts.AccountAuditHistoryView;
import ph.edu.umak.crm.app.staff.accounts.AccountDetailsView;
import ph.edu.umak.crm.app.staff.accounts.CreateStaffAccountView;
import ph.edu.umak.crm.app.staff.accounts.UserAccountsView;
import ph.edu.umak.crm.app.staff.content.ApprovedFormsView;
import ph.edu.umak.crm.app.staff.content.PublicationHistoryView;
import ph.edu.umak.crm.app.staff.content.ServiceAuthoringStep1View;
import ph.edu.umak.crm.app.staff.content.ServiceAuthoringStep3View;
import ph.edu.umak.crm.app.staff.content.ServiceCatalogView;
import ph.edu.umak.crm.app.staff.queue.QueueFilterDialog;
import ph.edu.umak.crm.app.staff.queue.RequestWorkspaceView;
import ph.edu.umak.crm.app.staff.queue.ReviewQueueView;
import ph.edu.umak.crm.app.staff.queue.StaffQueueData;
import ph.edu.umak.crm.app.staff.registry.AddResearchEntryView;
import ph.edu.umak.crm.app.staff.registry.RegistryCorrectionHistoryView;
import ph.edu.umak.crm.app.staff.registry.RegistryDocumentsView;
import ph.edu.umak.crm.app.staff.registry.ResearchRecordDetailView;
import ph.edu.umak.crm.app.staff.registry.ResearchRegistryView;
import ph.edu.umak.crm.app.staff.registry.StaffRegistryData;
import ph.edu.umak.crm.app.staff.lifecycle.CompletionCorrectionDialog;
import ph.edu.umak.crm.app.staff.lifecycle.MilestoneEditorDialog;
import ph.edu.umak.crm.app.staff.lifecycle.MilestonesAndOutcomesView;
import ph.edu.umak.crm.app.staff.lifecycle.StageHistoryView;
import ph.edu.umak.crm.app.staff.lifecycle.StaffLifecycleData;
import ph.edu.umak.crm.app.staff.reporting.AccessDeniedReportingView;
import ph.edu.umak.crm.app.staff.reporting.ExportReportDialog;
import ph.edu.umak.crm.app.staff.reporting.ResearchReportView;
import ph.edu.umak.crm.app.staff.reporting.ServiceUtilizationView;
import ph.edu.umak.crm.app.staff.reporting.StaffReportingData;

final class StaffScreen extends LinearLayout {
    private static final int NAVY = 0xff141e4d;
    private static final int BLUE = 0xff092d4d;
    private static final int MUTED = 0xff51627b;
    private static final int BACKGROUND = 0xfff5f6f8;
    private static final int WHITE = 0xffffffff;
    private static final int ACCENT = 0xff195c99;
    private static final int BORDER = 0xffe0e3e8;
    private static final int PALE_BLUE = 0xffe5edf6;
    private static final int YELLOW = 0xfff7eb37;
    private static final int[] TAB_ICONS = {
            R.drawable.ic_staff_dashboard, R.drawable.ic_staff_requests, R.drawable.ic_staff_registry,
            R.drawable.ic_staff_manage, R.drawable.ic_staff_profile
    };
    private static final String[] REQUEST_LABELS = {"Pending", "Under Review", "Completed"};
    private static final String[] REQUEST_STATES = {"queued", "claimed", "resolved"};
    private static final String[] TABS = {"Dashboard", "Requests", "Registry", "Manage", "Profile"};

    private final MainActivity activity;
    private final String email;
    private final Typeface headingFont;
    private final Typeface bodyFont;
    private final Typeface actionFont;
    private final LinearLayout content;
    private final LinearLayout navigation;
    private final ScrollView scroll;
    private String tab;
    private JSONArray consultations;
    private String consultationMessage;
    private LinearLayout requestResults;
    private int requestFilter;
    private String requestQuery = "";
    private String searchKeyword = "";
    private int queryId;

    // Manage Sub-Pages
    private String manageSubPage = "hub";
    private ManageHubView manageHubView;
    private ServiceCatalogView serviceCatalogView;
    private ServiceAuthoringStep1View serviceAuthoringStep1View;
    private ServiceAuthoringStep3View serviceAuthoringStep3View;
    private ApprovedFormsView approvedFormsView;
    private PublicationHistoryView publicationHistoryView;
    private UserAccountsView userAccountsView;
    private CreateStaffAccountView createStaffAccountView;
    private AccountDetailsView accountDetailsView;
    private AccountAuditHistoryView accountAuditHistoryView;

    // Requests Sub-Pages
    private String requestsSubPage = "queue";
    private ReviewQueueView reviewQueueView;
    private RequestWorkspaceView requestWorkspaceView;

    // Registry Sub-Pages (Part 7, C-03 & Part 8, C-04)
    private String registrySubPage = "list";
    private ResearchRegistryView researchRegistryView;
    private ResearchRecordDetailView researchRecordDetailView;
    private AddResearchEntryView addResearchEntryView;
    private RegistryDocumentsView registryDocumentsView;
    private RegistryCorrectionHistoryView registryCorrectionHistoryView;
    private StaffRegistryData.RegistryRecordItem selectedRegistryRecord;

    // Lifecycle Sub-Pages (Part 8, C-04)
    private MilestonesAndOutcomesView milestonesAndOutcomesView;
    private StageHistoryView stageHistoryView;
    private StaffLifecycleData.ResearchLifecycleState lifecycleState;

    // Reporting Sub-Pages (Part 9, C-05)
    private ResearchReportView researchReportView;
    private ServiceUtilizationView serviceUtilizationView;
    private AccessDeniedReportingView accessDeniedReportingView;

    StaffScreen(MainActivity activity, String email, String initialTab) {
        super(activity);
        this.activity = activity;
        this.email = email;
        headingFont = getResources().getFont(R.font.marcellus_regular);
        bodyFont = getResources().getFont(R.font.metropolis_regular);
        actionFont = getResources().getFont(R.font.metropolis_semibold);
        setOrientation(VERTICAL);
        setFocusableInTouchMode(true);
        setBackgroundColor(BACKGROUND);
        TextView header = text("Center for Research Management", 18, WHITE, headingFont);
        header.setPadding(dp(16), dp(14), dp(16), dp(14));
        header.setBackgroundColor(NAVY);
        addView(header);

        scroll = new ScrollView(activity);
        content = new LinearLayout(activity);
        content.setOrientation(VERTICAL);
        scroll.addView(content);
        addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));

        navigation = new LinearLayout(activity);
        navigation.setBackgroundColor(WHITE);
        navigation.setPadding(dp(4), dp(4), dp(4), dp(4));
        content.setPadding(0, 0, 0, dp(24));
        View divider = new View(activity);
        divider.setBackgroundColor(BORDER);
        addView(divider, new LayoutParams(LayoutParams.MATCH_PARENT, dp(1)));
        addView(navigation);
        select(initialTab);
    }

    String selectedTab() {
        return tab;
    }

    void loadInitialData() {
        loadTabData();
    }

    private void navigate(String destination) {
        select(destination);
        loadTabData();
    }

    private void loadTabData() {
        if ("Requests".equals(tab) || "Dashboard".equals(tab)) {
            activity.loadConsultations(this);
        }
    }

    private void select(String destination) {
        tab = destination;
        navigation.removeAllViews();
        for (int index = 0; index < TABS.length; index++) {
            String name = TABS[index];
            boolean selected = name.equals(tab);
            Button item = button(name, () -> navigate(name));
            ScreenTypography.apply(item, 11);
            item.setMinWidth(0);
            item.setMinimumWidth(0);
            item.setTextColor(selected ? NAVY : MUTED);
            item.setSelected(selected);
            item.setPadding(0, dp(8), 0, dp(8));
            Drawable icon = getResources().getDrawable(TAB_ICONS[index], null).mutate();
            icon.setTint(selected ? NAVY : MUTED);
            item.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
            item.setCompoundDrawablePadding(dp(4));
            item.setBackground(ripple(selected ? rounded(PALE_BLUE) : rounded(WHITE)));
            navigation.addView(item, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        }

        content.removeAllViews();
        if (!"Profile".equals(tab) && !"Manage".equals(tab) && !"Requests".equals(tab)) addBanner();
        switch (tab) {
            case "Profile":
                showProfile();
                break;
            case "Dashboard":
                showDashboard();
                break;
            case "Requests":
                showRequestQueue();
                break;
            case "Registry":
                showRegistry();
                break;
            case "Manage":
                showManage();
                break;
            default:
                throw new IllegalArgumentException("Unknown staff tab");
        }
        scroll.scrollTo(0, 0);
    }

    private void addBanner() {
        LinearLayout banner = new LinearLayout(activity);
        banner.setOrientation(VERTICAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setPadding(dp(24), dp(24), dp(24), dp(24));

        banner.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{BLUE, 0xff13354d}));
        if ("Dashboard".equals(tab)) {
            banner.addView(text("Welcome back,", 21, WHITE, headingFont));
            banner.addView(text(email, 26, WHITE, headingFont));
            badge(banner, "CRM Staff", PALE_BLUE, NAVY);
        } else {
            banner.addView(text(title(), 30, WHITE, headingFont));
        }
        content.addView(banner);
    }

    private void showDashboard() {
        clearAfter(1);
        LinearLayout metrics = new LinearLayout(activity);
        LayoutParams metricParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        metricParams.setMargins(dp(16), dp(16), dp(16), 0);
        content.addView(metrics, metricParams);
        metric(metrics, "Pending Requests", "queued", 0xffad2534);
        metric(metrics, "In Processing", "claimed", ACCENT);

        LinearLayout queue = card("Service Request Queue");
        queue.addView(text("Recent consultations", 13, MUTED, bodyFont));
        if (consultations == null) {
            queue.addView(text(consultationMessage == null ? "Loading queue…" : consultationMessage,
                    14, MUTED, bodyFont));
        } else if (consultations.length() == 0) {
            queue.addView(text("No consultations.", 14, MUTED, bodyFont));
        } else {
            for (int index = 0; index < Math.min(5, consultations.length()); index++) {
                JSONObject ticket = consultations.optJSONObject(index);
                if (ticket == null) continue;
                TextView row = text("#" + ticket.optString("id") + "  •  "
                        + ticket.optString("service_type"), 14, NAVY, actionFont);
                row.setPadding(0, dp(10), 0, dp(10));
                queue.addView(row);
            }
        }
        styledAction(queue, "View Review Queue", () -> navigate("Requests"), NAVY, WHITE, false);
        LinearLayout actions = card("Staff Actions");
        action(actions, "View profile", () -> navigate("Profile"));
        action(actions, "Public resources", activity::showPublicResources);
    }

    private void metric(LinearLayout row, String label, String state, int color) {
        LinearLayout tile = panel();
        LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
        if (row.getChildCount() > 0) params.leftMargin = dp(12);
        row.addView(tile, params);
        int count = 0;
        if (consultations != null) {
            for (int index = 0; index < consultations.length(); index++) {
                JSONObject ticket = consultations.optJSONObject(index);
                if (ticket != null && state.equals(ticket.optString("status"))) count++;
            }
        }
        tile.addView(text(consultations == null ? "—" : Integer.toString(count), 28, color, actionFont));
        tile.addView(text(label, 13, NAVY, actionFont));
        tile.addView(text("Recent consultations", 11, MUTED, bodyFont));
    }

    private void showProfile() {
        LinearLayout profile = panel();
        profile.setPadding(0, 0, 0, 0);
        addPanel(profile);
        FrameLayout identity = new FrameLayout(activity);
        View cover = new View(activity);
        cover.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{NAVY, 0xff2d3450}));
        identity.addView(cover, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(72)));
        String initial = email.isEmpty() ? "?" : email.substring(0, 1).toUpperCase(Locale.ROOT);
        TextView avatar = text(initial, 24, ACCENT, actionFont);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable avatarBackground = new GradientDrawable();
        avatarBackground.setShape(GradientDrawable.OVAL);
        avatarBackground.setColor(YELLOW);
        avatarBackground.setStroke(dp(2), WHITE);
        avatar.setBackground(avatarBackground);
        FrameLayout.LayoutParams avatarParams = new FrameLayout.LayoutParams(dp(60), dp(60));
        avatarParams.setMargins(dp(16), dp(42), 0, 0);
        identity.addView(avatar, avatarParams);
        profile.addView(identity);
        profile.setClipToOutline(true);

        LinearLayout details = new LinearLayout(activity);
        details.setOrientation(VERTICAL);
        details.setPadding(dp(16), dp(8), dp(16), dp(16));
        profile.addView(details);
        details.addView(text(email, 19, ACCENT, actionFont));
        badge(details, "Staff", PALE_BLUE, NAVY);

        LinearLayout links = card("Navigation");
        action(links, "Staff Dashboard", () -> navigate("Dashboard"));
        action(links, "Research Registry", () -> navigate("Registry"));
        action(links, "Public resources", activity::showPublicResources);
        LinearLayout settings = card("Account Settings");
        settings.addView(text("Email", 12, MUTED, actionFont));
        settings.addView(text(email, 15, NAVY, bodyFont));
        styledAction(settings, "Sign Out", activity::signOut, WHITE, 0xffa62a35, true);
    }

    private void showRequestQueue() {
        content.removeAllViews();
        if ("workspace".equals(requestsSubPage)) {
            ensureRequestWorkspaceView();
            content.addView(requestWorkspaceView.getContentLayout());
        } else {
            ensureReviewQueueView();
            content.addView(reviewQueueView.getContentLayout());
        }
        scroll.scrollTo(0, 0);
    }

    void openRequestsSubPage(String subPage) {
        this.requestsSubPage = subPage;
        if (!"Requests".equals(tab)) {
            tab = "Requests";
            navigation.removeAllViews();
            for (int index = 0; index < TABS.length; index++) {
                String name = TABS[index];
                boolean selected = name.equals(tab);
                Button item = button(name, () -> navigate(name));
                ScreenTypography.apply(item, 11);
                item.setMinWidth(0);
                item.setMinimumWidth(0);
                item.setTextColor(selected ? NAVY : MUTED);
                item.setSelected(selected);
                item.setPadding(0, dp(8), 0, dp(8));
                Drawable icon = getResources().getDrawable(TAB_ICONS[index], null).mutate();
                icon.setTint(selected ? NAVY : MUTED);
                item.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                item.setCompoundDrawablePadding(dp(4));
                item.setBackground(ripple(selected ? rounded(PALE_BLUE) : rounded(WHITE)));
                navigation.addView(item, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            }
        }
        showRequestQueue();
    }

    private void ensureReviewQueueView() {
        if (reviewQueueView == null) {
            reviewQueueView = new ReviewQueueView(activity, new ReviewQueueView.Callbacks() {
                @Override
                public void onOpenRequest(StaffQueueData.QueueRequestItem request) {
                    ensureRequestWorkspaceView();
                    requestWorkspaceView.setRequest(request);
                    openRequestsSubPage("workspace");
                }

                @Override
                public void onRefreshQueue() {
                    activity.loadConsultations(StaffScreen.this);
                }
            });
        }
    }

    private void ensureRequestWorkspaceView() {
        if (requestWorkspaceView == null) {
            requestWorkspaceView = new RequestWorkspaceView(activity, new RequestWorkspaceView.Callbacks() {
                @Override
                public void onBackToQueue() {
                    openRequestsSubPage("queue");
                }

                @Override
                public void onReviewSaved(StaffQueueData.QueueRequestItem request) {
                    // Review saved
                }
            });
        }
    }

    void showConsultations(JSONArray tickets, String message) {
        consultations = tickets;
        consultationMessage = message;
        if ("Requests".equals(tab) && "queue".equals(requestsSubPage)) {
            showRequestQueue();
        }
        if ("Dashboard".equals(tab)) showDashboard();
    }

    private void renderRequests() {
        requestResults.removeAllViews();
        if (consultations != null) {
            String keyword = requestQuery.trim().toLowerCase(Locale.ROOT);
            for (JSONObject ticket : matchingTickets(keyword)) {
                String service = ticket.optString("service_type");
                LinearLayout row = panel();
                row.addView(text(service, 17, ACCENT, actionFont));
                row.addView(text("Request #" + ticket.optString("id"), 14, NAVY, bodyFont));
                row.addView(text("Submitted " + date(ticket.optString("submitted_at")), 13, MUTED, bodyFont));
                if (!ticket.isNull("deadline_at")) {
                    row.addView(text("Deadline " + date(ticket.optString("deadline_at")), 13, MUTED, bodyFont));
                }
                badge(row, REQUEST_LABELS[requestFilter],
                        requestFilter == 2 ? 0xffe8f5ec : 0xfffff6df,
                        requestFilter == 2 ? 0xff216b37 : 0xff815000);
                addPanel(requestResults, row);
            }
        }
        if (requestResults.getChildCount() == 0) {
            TextView state = text(consultationMessage != null ? consultationMessage
                    : consultations == null ? "Loading consultations…" : "No matching consultations.",
                    14, MUTED, bodyFont);
            state.setPadding(dp(16), dp(12), dp(16), dp(12));
            addPanel(requestResults, state);
        }
    }

    private List<JSONObject> matchingTickets(String keyword) {
        List<JSONObject> matches = new ArrayList<>();
        for (int index = 0; index < consultations.length(); index++) {
            JSONObject ticket = consultations.optJSONObject(index);
            if (ticket == null || !REQUEST_STATES[requestFilter].equals(ticket.optString("status"))) continue;
            String service = ticket.optString("service_type").toLowerCase(Locale.ROOT);
            if (service.contains(keyword) || ticket.optString("id").contains(keyword)) matches.add(ticket);
        }
        if (requestFilter == 0) {
            matches.sort(Comparator
                    .comparing((JSONObject ticket) -> ticket.isNull("deadline_at"))
                    .thenComparing(ticket -> ticket.optString("deadline_at"))
                    .thenComparing(ticket -> ticket.optString("submitted_at"))
                    .thenComparingInt(ticket -> ticket.optInt("id")));
        }
        return matches;
    }

    private String date(String value) {
        try {
            return DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.getDefault())
                    .withZone(ZoneId.systemDefault()).format(Instant.parse(value));
        } catch (java.time.DateTimeException invalidDate) {
            return value;
        }
    }

    private EditText searchField(String hint) {
        EditText field = new EditText(activity);
        field.setHint(hint);
        field.setTypeface(bodyFont);
        field.setTextSize(14);
        field.setSingleLine();
        field.setMinHeight(dp(48));
        field.setPadding(dp(16), dp(8), dp(16), dp(8));
        field.setBackground(rounded(0xffeef2f7));
        return field;
    }

    private void showRegistry() {
        content.removeAllViews();
        if (selectedRegistryRecord == null) {
            selectedRegistryRecord = StaffRegistryData.getMockRecords().get(0);
        }
        switch (registrySubPage) {
            case "detail":
                ensureResearchRecordDetailView();
                researchRecordDetailView.setRecord(selectedRegistryRecord);
                content.addView(researchRecordDetailView.getContentLayout());
                break;
            case "add_entry":
                ensureAddResearchEntryView();
                content.addView(addResearchEntryView.getContentLayout());
                break;
            case "edit_entry":
                ensureAddResearchEntryView();
                addResearchEntryView.setEditRecord(selectedRegistryRecord, false);
                content.addView(addResearchEntryView.getContentLayout());
                break;
            case "documents":
                ensureRegistryDocumentsView();
                registryDocumentsView.setRecord(selectedRegistryRecord);
                content.addView(registryDocumentsView.getContentLayout());
                break;
            case "corrections":
                ensureRegistryCorrectionHistoryView();
                registryCorrectionHistoryView.setRecord(selectedRegistryRecord);
                content.addView(registryCorrectionHistoryView.getContentLayout());
                break;
            case "lifecycle":
                ensureMilestonesAndOutcomesView();
                content.addView(milestonesAndOutcomesView.getContentLayout());
                break;
            case "stage_history":
                ensureStageHistoryView();
                content.addView(stageHistoryView.getContentLayout());
                break;
            case "list":
            default:
                ensureResearchRegistryView();
                content.addView(researchRegistryView.getContentLayout());
                break;
        }
        scroll.scrollTo(0, 0);
    }

    void openRegistrySubPage(String subPage) {
        this.registrySubPage = subPage;
        if (!"Registry".equals(tab)) {
            tab = "Registry";
            navigation.removeAllViews();
            for (int index = 0; index < TABS.length; index++) {
                String name = TABS[index];
                boolean selected = name.equals(tab);
                Button item = button(name, () -> navigate(name));
                ScreenTypography.apply(item, 11);
                item.setMinWidth(0);
                item.setMinimumWidth(0);
                item.setTextColor(selected ? NAVY : MUTED);
                item.setSelected(selected);
                item.setPadding(0, dp(8), 0, dp(8));
                Drawable icon = getResources().getDrawable(TAB_ICONS[index], null).mutate();
                icon.setTint(selected ? NAVY : MUTED);
                item.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                item.setCompoundDrawablePadding(dp(4));
                item.setBackground(ripple(selected ? rounded(PALE_BLUE) : rounded(WHITE)));
                navigation.addView(item, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            }
        }
        showRegistry();
    }

    private void ensureResearchRegistryView() {
        if (researchRegistryView == null) {
            researchRegistryView = new ResearchRegistryView(activity, new ResearchRegistryView.Callbacks() {
                @Override
                public void onOpenRecord(StaffRegistryData.RegistryRecordItem record) {
                    selectedRegistryRecord = record;
                    openRegistrySubPage("detail");
                }

                @Override
                public void onAddResearchEntry() {
                    ensureAddResearchEntryView();
                    addResearchEntryView.resetToDefaultAdd();
                    openRegistrySubPage("add_entry");
                }

                @Override
                public void onExportDirectory() {
                    // Exported directory toast shown in view
                }
            });
        }
    }

    private void ensureResearchRecordDetailView() {
        if (researchRecordDetailView == null) {
            researchRecordDetailView = new ResearchRecordDetailView(activity, new ResearchRecordDetailView.Callbacks() {
                @Override
                public void onBackToRegistry() {
                    openRegistrySubPage("list");
                }

                @Override
                public void onEditRecord(StaffRegistryData.RegistryRecordItem record) {
                    selectedRegistryRecord = record;
                    openRegistrySubPage("edit_entry");
                }

                @Override
                public void onCorrectionHistory(StaffRegistryData.RegistryRecordItem record) {
                    selectedRegistryRecord = record;
                    openRegistrySubPage("corrections");
                }

                @Override
                public void onManageDocuments(StaffRegistryData.RegistryRecordItem record) {
                    selectedRegistryRecord = record;
                    openRegistrySubPage("documents");
                }

                @Override
                public void onOpenLinkedRequest(String requestId) {
                    ensureRequestWorkspaceView();
                    StaffQueueData.QueueRequestItem found = null;
                    for (StaffQueueData.QueueRequestItem item : StaffQueueData.getInitialQueue()) {
                        if (item.id.equalsIgnoreCase(requestId)) {
                            found = item;
                            break;
                        }
                    }
                    if (found != null) {
                        requestWorkspaceView.setRequest(found);
                    }
                    openRequestsSubPage("workspace");
                }

                @Override
                public void onMilestonesAndOutcomes(StaffRegistryData.RegistryRecordItem record) {
                    selectedRegistryRecord = record;
                    openRegistrySubPage("lifecycle");
                }
            });
        }
    }

    private void ensureAddResearchEntryView() {
        if (addResearchEntryView == null) {
            addResearchEntryView = new AddResearchEntryView(activity, new AddResearchEntryView.Callbacks() {
                @Override
                public void onSaveEntry(StaffRegistryData.RegistryRecordItem record) {
                    selectedRegistryRecord = record;
                    openRegistrySubPage("detail");
                }

                @Override
                public void onCancel() {
                    openRegistrySubPage("edit_entry".equals(registrySubPage) ? "detail" : "list");
                }

                @Override
                public void onReloadRecord(StaffRegistryData.RegistryRecordItem record) {
                    selectedRegistryRecord = record;
                    openRegistrySubPage("detail");
                }
            });
        }
    }

    private void ensureRegistryDocumentsView() {
        if (registryDocumentsView == null) {
            registryDocumentsView = new RegistryDocumentsView(activity, () -> openRegistrySubPage("detail"));
        }
    }

    private void ensureRegistryCorrectionHistoryView() {
        if (registryCorrectionHistoryView == null) {
            registryCorrectionHistoryView = new RegistryCorrectionHistoryView(activity, () -> openRegistrySubPage("detail"));
        }
    }

    private void ensureMilestonesAndOutcomesView() {
        if (milestonesAndOutcomesView == null) {
            milestonesAndOutcomesView = new MilestonesAndOutcomesView(activity, new MilestonesAndOutcomesView.Callbacks() {
                @Override
                public void onViewStageHistory() {
                    openRegistrySubPage("stage_history");
                }

                @Override
                public void onClose() {
                    openRegistrySubPage("detail");
                }

                @Override
                public void onStageUpdated(StaffLifecycleData.ResearchLifecycleState state) {
                    lifecycleState = state;
                }
            });
        }
    }

    private void ensureStageHistoryView() {
        if (stageHistoryView == null) {
            stageHistoryView = new StageHistoryView(activity, () -> openRegistrySubPage("lifecycle"));
        }
    }

    void showResearch(int currentQuery, JSONArray records, String message) {
        // Compatibility stub for backend queries
    }

    private String title() {
        switch (tab) {
            case "Dashboard": return "Staff Dashboard";
            case "Requests": return "Review Queue";
            case "Registry": return "Research Registry";
            case "Manage": return "Manage";
            default: return "Profile";
        }
    }

    private void clearAfter(int count) {
        while (content.getChildCount() > count) content.removeViewAt(count);
    }

    private LinearLayout panel() {
        LinearLayout panel = new LinearLayout(activity);
        panel.setOrientation(VERTICAL);
        panel.setPadding(dp(16), dp(16), dp(16), dp(16));
        panel.setBackground(outlined(WHITE));
        return panel;
    }

    private void addPanel(View panel) {
        addPanel(content, panel);
    }

    private void addPanel(LinearLayout parent, View panel) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(16), dp(12), dp(16), 0);
        parent.addView(panel, params);
    }

    private LinearLayout card(String title) {
        LinearLayout card = panel();
        addPanel(card);
        TextView heading = text(title, 18, ACCENT, actionFont);
        ScreenTypography.heading(heading);
        heading.setPadding(0, 0, 0, dp(8));
        card.addView(heading);
        return card;
    }

    private void badge(LinearLayout parent, String label, int fill, int color) {
        TextView badge = text(label, 12, color, actionFont);
        badge.setBackground(rounded(fill));
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        parent.addView(badge, params);
    }

    private Button button(String label, Runnable run) {
        Button button = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextColor(NAVY);
        button.setTypeface(actionFont);
        ScreenTypography.apply(button, 14);
        button.setMinHeight(dp(48));
        button.setOnClickListener(view -> run.run());
        return button;
    }

    private void action(LinearLayout card, String label, Runnable run) {
        Button button = button(label, run);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        card.addView(button);
    }

    private void styledAction(LinearLayout card, String label, Runnable run, int fill, int color,
            boolean outline) {
        Button button = button(label, run);
        button.setTextColor(color);
        button.setBackground(ripple(outline ? outlined(fill) : rounded(fill)));
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        card.addView(button, params);
    }

    private RippleDrawable ripple(Drawable background) {
        return new RippleDrawable(ColorStateList.valueOf(0x22141e4d), background, null);
    }

    private GradientDrawable outlined(int color) {
        GradientDrawable shape = rounded(color);
        shape.setStroke(dp(1), BORDER);
        return shape;
    }

    private TextView text(String value, int size, int color, Typeface font) {
        TextView text = new TextView(activity);
        text.setText(value);
        text.setTextColor(color);
        text.setTypeface(font);
        ScreenTypography.apply(text, size);
        return text;
    }

    private GradientDrawable rounded(int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(12));
        return background;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    // --- Manage Tab Subpage Management ---

    void showManage() {
        content.removeAllViews();
        switch (manageSubPage) {
            case "catalog":
                ensureServiceCatalogView();
                content.addView(serviceCatalogView.getContentLayout());
                break;
            case "authoring_step1":
                ensureServiceAuthoringStep1View();
                content.addView(serviceAuthoringStep1View.getContentLayout());
                break;
            case "authoring_step3":
                ensureServiceAuthoringStep3View();
                content.addView(serviceAuthoringStep3View.getContentLayout());
                break;
            case "forms":
                ensureApprovedFormsView();
                content.addView(approvedFormsView.getContentLayout());
                break;
            case "history":
                ensurePublicationHistoryView();
                content.addView(publicationHistoryView.getContentLayout());
                break;
            case "accounts":
                ensureUserAccountsView();
                content.addView(userAccountsView.getContentLayout());
                break;
            case "create_staff":
                ensureCreateStaffAccountView();
                content.addView(createStaffAccountView.getContentLayout());
                break;
            case "account_details":
                ensureAccountDetailsView();
                content.addView(accountDetailsView.getContentLayout());
                break;
            case "account_audit":
                ensureAccountAuditHistoryView();
                content.addView(accountAuditHistoryView.getContentLayout());
                break;
            case "research_report":
                ensureResearchReportView();
                researchReportView.setModel(StaffReportingData.getResearchReportResult());
                content.addView(researchReportView.getContentLayout());
                break;
            case "research_report_empty":
                ensureResearchReportView();
                researchReportView.setModel(StaffReportingData.getResearchReportEmpty());
                content.addView(researchReportView.getContentLayout());
                break;
            case "service_utilization":
                ensureServiceUtilizationView();
                serviceUtilizationView.setModel(StaffReportingData.getServiceUtilizationResult());
                content.addView(serviceUtilizationView.getContentLayout());
                break;
            case "access_denied":
                ensureAccessDeniedReportingView();
                content.addView(accessDeniedReportingView.getContentLayout());
                break;
            case "hub":
            default:
                ensureManageHubView();
                content.addView(manageHubView.getContentLayout());
                break;
        }
        scroll.scrollTo(0, 0);
    }

    void openManageSubPage(String subPage) {
        this.manageSubPage = subPage;
        if (!"Manage".equals(tab)) {
            tab = "Manage";
            navigation.removeAllViews();
            for (int index = 0; index < TABS.length; index++) {
                String name = TABS[index];
                boolean selected = name.equals(tab);
                Button item = button(name, () -> navigate(name));
                ScreenTypography.apply(item, 11);
                item.setMinWidth(0);
                item.setMinimumWidth(0);
                item.setTextColor(selected ? NAVY : MUTED);
                item.setSelected(selected);
                item.setPadding(0, dp(8), 0, dp(8));
                Drawable icon = getResources().getDrawable(TAB_ICONS[index], null).mutate();
                icon.setTint(selected ? NAVY : MUTED);
                item.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                item.setCompoundDrawablePadding(dp(4));
                item.setBackground(ripple(selected ? rounded(PALE_BLUE) : rounded(WHITE)));
                navigation.addView(item, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            }
        }
        showManage();
    }

    private void ensureManageHubView() {
        if (manageHubView == null) {
            manageHubView = new ManageHubView(activity, new ManageHubView.Listener() {
                @Override
                public void onOpenServiceCatalog() {
                    openManageSubPage("catalog");
                }

                @Override
                public void onOpenApprovedForms() {
                    openManageSubPage("forms");
                }

                @Override
                public void onOpenUserAccounts() {
                    openManageSubPage("accounts");
                }

                @Override
                public void onOpenAuditHistory() {
                    openManageSubPage("history");
                }

                @Override
                public void onOpenAnalyticsReporting() {
                    openManageSubPage("research_report");
                }
            });
        }
    }

    private void ensureServiceCatalogView() {
        if (serviceCatalogView == null) {
            serviceCatalogView = new ServiceCatalogView(activity, new ServiceCatalogView.Listener() {
                @Override
                public void onBackToManage() {
                    openManageSubPage("hub");
                }

                @Override
                public void onNewService() {
                    openManageSubPage("authoring_step1");
                }

                @Override
                public void onEditService(StaffData.ServiceCatalogItem item) {
                    openManageSubPage("authoring_step1");
                }

                @Override
                public void onWithdrawService(StaffData.ServiceCatalogItem item) {
                    StaffDialogs.showWithdrawServiceDialog(activity, item.id, item.title, () -> {
                        openManageSubPage("history");
                    });
                }
            });
        }
    }

    private void ensureServiceAuthoringStep1View() {
        if (serviceAuthoringStep1View == null) {
            serviceAuthoringStep1View = new ServiceAuthoringStep1View(activity, new ServiceAuthoringStep1View.Listener() {
                @Override
                public void onBackToCatalog() {
                    openManageSubPage("catalog");
                }

                @Override
                public void onContinue() {
                    openManageSubPage("authoring_step3");
                }

                @Override
                public void onSaveDraft() {
                    openManageSubPage("catalog");
                }
            });
        }
    }

    private void ensureServiceAuthoringStep3View() {
        if (serviceAuthoringStep3View == null) {
            serviceAuthoringStep3View = new ServiceAuthoringStep3View(activity, new ServiceAuthoringStep3View.Listener() {
                @Override
                public void onBack() {
                    openManageSubPage("authoring_step1");
                }

                @Override
                public void onPublish() {
                    openManageSubPage("catalog");
                }

                @Override
                public void onSaveDraft() {
                    openManageSubPage("catalog");
                }

                @Override
                public void onEditStep1() {
                    openManageSubPage("authoring_step1");
                }
            });
        }
    }

    private void ensureApprovedFormsView() {
        if (approvedFormsView == null) {
            approvedFormsView = new ApprovedFormsView(activity, new ApprovedFormsView.Listener() {
                @Override
                public void onBackToManage() {
                    openManageSubPage("hub");
                }

                @Override
                public void onReplaceVersion() {
                    StaffDialogs.showReplaceFormVersionDialog(activity, () -> {
                        openManageSubPage("history");
                    });
                }

                @Override
                public void onWithdraw() {
                    StaffDialogs.showWithdrawServiceDialog(activity, "SVC-02", "Ethics Review & Clearance", () -> {
                        openManageSubPage("history");
                    });
                }
            });
        }
    }

    private void ensurePublicationHistoryView() {
        if (publicationHistoryView == null) {
            publicationHistoryView = new PublicationHistoryView(activity, new PublicationHistoryView.Callbacks() {
                @Override
                public void onBack() {
                    openManageSubPage("hub");
                }

                @Override
                public void onNavigateToAccountAudit() {
                    openManageSubPage("account_audit");
                }
            });
        }
    }

    private void ensureUserAccountsView() {
        if (userAccountsView == null) {
            userAccountsView = new UserAccountsView(activity, new UserAccountsView.Callbacks() {
                @Override
                public void onBack() {
                    openManageSubPage("hub");
                }

                @Override
                public void onCreateStaffAccount() {
                    openManageSubPage("create_staff");
                }

                @Override
                public void onSelectAccount(StaffData.StaffAccountItem account) {
                    ensureAccountDetailsView();
                    accountDetailsView.setAccount(account);
                    openManageSubPage("account_details");
                }
            });
        }
    }

    private void ensureCreateStaffAccountView() {
        if (createStaffAccountView == null) {
            createStaffAccountView = new CreateStaffAccountView(activity, new CreateStaffAccountView.Callbacks() {
                @Override
                public void onBack() {
                    openManageSubPage("accounts");
                }

                @Override
                public void onAccountCreated(String name, String college, String email) {
                    ensureAccountDetailsView();
                    accountDetailsView.setPreviewState("5.10a");
                    openManageSubPage("account_details");
                }
            });
        }
    }

    private void ensureAccountDetailsView() {
        if (accountDetailsView == null) {
            accountDetailsView = new AccountDetailsView(activity, new AccountDetailsView.Callbacks() {
                @Override
                public void onBack() {
                    openManageSubPage("accounts");
                }

                @Override
                public void onViewAuditHistory(String identifier, String name) {
                    ensureAccountAuditHistoryView();
                    accountAuditHistoryView.setAccount(name, identifier);
                    openManageSubPage("account_audit");
                }

                @Override
                public void onAccountUpdated() {
                    openManageSubPage("accounts");
                }
            });
        }
    }

    private void ensureAccountAuditHistoryView() {
        if (accountAuditHistoryView == null) {
            accountAuditHistoryView = new AccountAuditHistoryView(activity, () -> {
                openManageSubPage("account_details");
            });
        }
    }

    // --- Part 5 Wireframe Previews ---

    void showPreview5_1_ManageHub() {
        openManageSubPage("hub");
    }

    void showPreview5_2_ServiceCatalog() {
        openManageSubPage("catalog");
    }

    void showPreview5_3_AuthoringStep1InApp() {
        ensureServiceAuthoringStep1View();
        serviceAuthoringStep1View.setPreviewState("in_app");
        openManageSubPage("authoring_step1");
    }

    void showPreview5_3_AuthoringStep1External() {
        ensureServiceAuthoringStep1View();
        serviceAuthoringStep1View.setPreviewState("external");
        openManageSubPage("authoring_step1");
    }

    void showPreview5_4_ApprovedForms() {
        openManageSubPage("forms");
    }

    void showPreview5_5_AuthoringStep3Error() {
        ensureServiceAuthoringStep3View();
        serviceAuthoringStep3View.setPreviewState("error");
        openManageSubPage("authoring_step3");
    }

    void showPreview5_5_AuthoringStep3Draft() {
        ensureServiceAuthoringStep3View();
        serviceAuthoringStep3View.setPreviewState("draft");
        openManageSubPage("authoring_step3");
    }

    void showPreview5_6a_WithdrawServiceDialog() {
        openManageSubPage("catalog");
        StaffDialogs.showWithdrawServiceDialog(activity, "SVC-02", "Ethics Review & Clearance", () -> {
            openManageSubPage("history");
        });
    }

    void showPreview5_6b_ReplaceFormVersionDialog() {
        openManageSubPage("forms");
        StaffDialogs.showReplaceFormVersionDialog(activity, () -> {
            openManageSubPage("history");
        });
    }

    void showPreview5_7_PublicationHistory() {
        openManageSubPage("history");
    }

    void showPreview5_8_UserAccounts() {
        openManageSubPage("accounts");
    }

    void showPreview5_9_CreateStaffDefault() {
        ensureCreateStaffAccountView();
        createStaffAccountView.setPreviewState("5.9_default");
        openManageSubPage("create_staff");
    }

    void showPreview5_9_CreateStaffMissingError() {
        ensureCreateStaffAccountView();
        createStaffAccountView.setPreviewState("5.9_error_missing");
        openManageSubPage("create_staff");
    }

    void showPreview5_9_CreateStaffDuplicateError() {
        ensureCreateStaffAccountView();
        createStaffAccountView.setPreviewState("5.9_error_duplicate");
        openManageSubPage("create_staff");
    }

    void showPreview5_10a_AccountDetailsStaff() {
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.10a");
        openManageSubPage("account_details");
    }

    void showPreview5_10b_AccountDetailsResearcher() {
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.10b");
        openManageSubPage("account_details");
    }

    void showPreview5_11a_DeactivateDialog() {
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.11a");
        openManageSubPage("account_details");
    }

    void showPreview5_11b_ActivateDialog() {
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.11b");
        openManageSubPage("account_details");
    }

    void showPreview5_11c_OwnAccountErrorDialog() {
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.11c");
        openManageSubPage("account_details");
    }

    void showPreview5_11d_LastAdminErrorDialog() {
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.11d");
        openManageSubPage("account_details");
    }

    void showPreview5_12_TemporaryCredentialIssuedDialog() {
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.12");
        openManageSubPage("account_details");
    }

    void showPreview5_13_AccountAuditHistory() {
        ensureAccountAuditHistoryView();
        accountAuditHistoryView.setAccount("LeBron R. James", "A1234");
        openManageSubPage("account_audit");
    }

    // --- Part 6 Wireframe Previews ---

    void showPreview6_1_InReviewTab() {
        ensureReviewQueueView();
        reviewQueueView.setPreviewState("6.1_in_review");
        openRequestsSubPage("queue");
    }

    void showPreview6_1_CompletedTab() {
        ensureReviewQueueView();
        reviewQueueView.setPreviewState("6.1_completed");
        openRequestsSubPage("queue");
    }

    void showPreview6_2_FilterSheet() {
        ensureReviewQueueView();
        reviewQueueView.setPreviewState("6.2_filter_sheet");
        openRequestsSubPage("queue");
    }

    void showPreview6_2_NoMatchingRequests() {
        ensureReviewQueueView();
        reviewQueueView.setPreviewState("6.2_no_matches");
        openRequestsSubPage("queue");
    }

    void showPreview6_3a_RequestWorkspaceRequesterAndChecklist() {
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.3a_workspace");
        openRequestsSubPage("workspace");
    }

    void showPreview6_3b_RequestWorkspaceDecision() {
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.3b_decision");
        openRequestsSubPage("workspace");
    }

    void showPreview6_4a_ErrorMissingItemsAndNextAction() {
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.4a_error_missing");
        openRequestsSubPage("workspace");
    }

    void showPreview6_4b_ErrorStatusChangeNotAllowed() {
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.4b_error_status_not_allowed");
        openRequestsSubPage("workspace");
    }

    void showPreview6_4c_StaleDataConflict() {
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.4c_stale_conflict");
        openRequestsSubPage("workspace");
    }

    void showPreview6_5_CompletedRequestCorrection() {
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.5_completed_correction");
        openRequestsSubPage("workspace");
    }

    void showPreview6_6_ReviewSaved() {
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.6_review_saved");
        openRequestsSubPage("workspace");
    }

    // --- Part 7 Wireframe Previews (C-03) ---

    void showPreview7_1a_Results() {
        ensureResearchRegistryView();
        researchRegistryView.showDefaultState();
        openRegistrySubPage("list");
    }

    void showPreview7_1b_FiltersApplied() {
        ensureResearchRegistryView();
        researchRegistryView.showFiltersAppliedState();
        openRegistrySubPage("list");
    }

    void showPreview7_1c_EmptyState() {
        ensureResearchRegistryView();
        researchRegistryView.showEmptyState();
        openRegistrySubPage("list");
    }

    void showPreview7_2_RecordDetail() {
        selectedRegistryRecord = StaffRegistryData.getMockRecords().get(0);
        ensureResearchRecordDetailView();
        researchRecordDetailView.setRecord(selectedRegistryRecord);
        openRegistrySubPage("detail");
    }

    void showPreview7_3_TeamPicker() {
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_3_TeamPicker();
        openRegistrySubPage("add_entry");
    }

    void showPreview7_3_ErrorAlreadyMember() {
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_3_ErrorAlreadyMember();
        openRegistrySubPage("add_entry");
    }

    void showPreview7_3_ErrorNoSuchAccount() {
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_3_ErrorNoSuchAccount();
        openRegistrySubPage("add_entry");
    }

    void showPreview7_4_PossibleDuplicate() {
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_4_PossibleDuplicate();
        openRegistrySubPage("add_entry");
    }

    void showPreview7_4_ErrorReasonMissing() {
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_4_ErrorReasonMissing();
        openRegistrySubPage("add_entry");
    }

    void showPreview7_5_StaleDataConflict() {
        selectedRegistryRecord = StaffRegistryData.getMockRecords().get(0);
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_5_StaleDataConflict(selectedRegistryRecord);
        openRegistrySubPage("edit_entry");
    }

    void showPreview7_6_DocumentsVersionList() {
        selectedRegistryRecord = StaffRegistryData.getMockRecords().get(0);
        ensureRegistryDocumentsView();
        registryDocumentsView.setRecord(selectedRegistryRecord);
        openRegistrySubPage("documents");
    }

    void showPreview7_7_CorrectionHistory() {
        selectedRegistryRecord = StaffRegistryData.getMockRecords().get(0);
        ensureRegistryCorrectionHistoryView();
        registryCorrectionHistoryView.setRecord(selectedRegistryRecord);
        openRegistrySubPage("corrections");
    }

    // --- Part 8 Wireframe Previews (C-04) ---

    void showPreview8_1_Default() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1_Default();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1a_ErrorStageNotAllowed() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1a_ErrorStageNotAllowed();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1b_ErrorEffectiveDate() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1b_ErrorEffectiveDate();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1c_ErrorSupportingDetails() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1c_ErrorSupportingDetails();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1d_ErrorNextAction() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1d_ErrorNextAction();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1e_ErrorCompletionDate() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1e_ErrorCompletionDate();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1f_ErrorPresentation() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1f_ErrorPresentation();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1g_ErrorPublication() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1g_ErrorPublication();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_2_MilestoneEditor() {
        ensureMilestonesAndOutcomesView();
        openRegistrySubPage("lifecycle");
        StaffLifecycleData.MilestoneItem item = new StaffLifecycleData.MilestoneItem(
                "Data Collection & Analysis",
                "In progress",
                "Target · Nov 15, 2026",
                "2026-11-15",
                "Field logbook, survey batches 1 – 3"
        );
        MilestoneEditorDialog.show(activity, "REG-2026-089", "Impact of AI on Student Engagement", item, updated -> {
            milestonesAndOutcomesView.showPreview8_1_Default();
        });
    }

    void showPreview8_3_StageHistory() {
        ensureStageHistoryView();
        openRegistrySubPage("stage_history");
    }

    void showPreview8_4_CompletionCorrection() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_5b_OutcomeRecorded();
        openRegistrySubPage("lifecycle");
        CompletionCorrectionDialog.show(activity, "REG-2026-089", "Impact of AI on Student Engagement", "2027-03-15", (correctedDate, reason) -> {
            milestonesAndOutcomesView.showPreview8_5b_OutcomeRecorded();
        });
    }

    void showPreview8_5a_StageUpdated() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_5a_StageUpdated();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_5b_OutcomeRecorded() {
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_5b_OutcomeRecorded();
        openRegistrySubPage("lifecycle");
    }

    // --- Part 9 Views & Previews (C-05) ---

    private void ensureResearchReportView() {
        if (researchReportView == null) {
            researchReportView = new ResearchReportView(activity, new ResearchReportView.Callbacks() {
                @Override
                public void onBackToFilters() {
                    String[] options = {
                            "Research Report (Result)",
                            "Research Report (Empty)",
                            "Service Utilization",
                            "Back to Manage Hub"
                    };
                    new android.app.AlertDialog.Builder(activity)
                            .setTitle("Reporting Navigation")
                            .setItems(options, (d, which) -> {
                                switch (which) {
                                    case 0:
                                        showPreview9_1_ResearchReportResult();
                                        break;
                                    case 1:
                                        showPreview9_3_ResearchReportEmpty();
                                        break;
                                    case 2:
                                        showPreview9_2_ServiceUtilizationResult();
                                        break;
                                    case 3:
                                        openManageSubPage("hub");
                                        break;
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }

                @Override
                public void onExport() {
                    new ExportReportDialog(activity).show();
                }

                @Override
                public void onOpenRecord(String recordId) {
                    selectedRegistryRecord = StaffRegistryData.getMockRecords().get(0);
                    ensureResearchRecordDetailView();
                    researchRecordDetailView.setRecord(selectedRegistryRecord);
                    openRegistrySubPage("detail");
                }
            });
        }
    }

    private void ensureServiceUtilizationView() {
        if (serviceUtilizationView == null) {
            serviceUtilizationView = new ServiceUtilizationView(activity, new ServiceUtilizationView.Callbacks() {
                @Override
                public void onBackToFilters() {
                    String[] options = {
                            "Research Report (Result)",
                            "Research Report (Empty)",
                            "Service Utilization",
                            "Back to Manage Hub"
                    };
                    new android.app.AlertDialog.Builder(activity)
                            .setTitle("Reporting Navigation")
                            .setItems(options, (d, which) -> {
                                switch (which) {
                                    case 0:
                                        showPreview9_1_ResearchReportResult();
                                        break;
                                    case 1:
                                        showPreview9_3_ResearchReportEmpty();
                                        break;
                                    case 2:
                                        showPreview9_2_ServiceUtilizationResult();
                                        break;
                                    case 3:
                                        openManageSubPage("hub");
                                        break;
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }

                @Override
                public void onExport() {
                    new ExportReportDialog(activity).show();
                }

                @Override
                public void onOpenRequest(String requestId) {
                    navigate("Requests");
                }
            });
        }
    }

    private void ensureAccessDeniedReportingView() {
        if (accessDeniedReportingView == null) {
            accessDeniedReportingView = new AccessDeniedReportingView(activity, () -> {
                openManageSubPage("hub");
            });
        }
    }

    void showPreview9_1_ResearchReportResult() {
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportResult());
        openManageSubPage("research_report");
    }

    void showPreview9_2_ServiceUtilizationResult() {
        ensureServiceUtilizationView();
        serviceUtilizationView.setModel(StaffReportingData.getServiceUtilizationResult());
        openManageSubPage("service_utilization");
    }

    void showPreview9_3_ResearchReportEmpty() {
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportEmpty());
        openManageSubPage("research_report_empty");
    }

    void showPreview9_4a_ExportFormatPicker() {
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportResult());
        openManageSubPage("research_report");
        new ExportReportDialog(activity).show(ExportReportDialog.ExportState.FORMAT_PICKER);
    }

    void showPreview9_4b_ExportPending() {
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportResult());
        openManageSubPage("research_report");
        new ExportReportDialog(activity).show(ExportReportDialog.ExportState.PENDING);
    }

    void showPreview9_4c_ExportSuccess() {
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportResult());
        openManageSubPage("research_report");
        new ExportReportDialog(activity).show(ExportReportDialog.ExportState.SUCCESS);
    }

    void showPreview9_4d_ExportFailure() {
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportResult());
        openManageSubPage("research_report");
        new ExportReportDialog(activity).show(ExportReportDialog.ExportState.FAILURE);
    }

    void showPreview9_5_AccessDenied() {
        ensureAccessDeniedReportingView();
        openManageSubPage("access_denied");
    }
}
