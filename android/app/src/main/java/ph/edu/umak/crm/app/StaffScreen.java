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

import ph.edu.umak.crm.app.common.dialogs.CenteredActionDialog;
import ph.edu.umak.crm.app.common.components.SaveSnackbar;
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
import ph.edu.umak.crm.app.staff.lifecycle.CompletionCorrectionView;
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
    private final SaveSnackbar snackbar;
    private String tab;
    private int tabSlideDirection = 0;
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
    private ph.edu.umak.crm.app.staff.queue.LiveStaffRequestsView liveRequestsView;
    private boolean previewRequests;
    private boolean previewManage,previewRegistry;
    private ph.edu.umak.crm.app.staff.registry.LiveRegistryRecordView liveRegistryView;
    private ph.edu.umak.crm.app.staff.registry.LiveRegistryDirectoryView liveRegistryDirectory;
    private ph.edu.umak.crm.app.staff.lifecycle.LiveResearchProgressEditor liveProgressView;
    private ph.edu.umak.crm.app.staff.content.LiveServiceCatalogView liveCatalogView;
    private ph.edu.umak.crm.app.staff.accounts.LiveAccountsView liveAccountsView;
    private ph.edu.umak.crm.app.staff.reporting.LiveReportsView liveReportsView;

    // Registry Sub-Pages (Part 7, C-03 & Part 8, C-04)
    private String registrySubPage = "list";
    private ResearchRegistryView researchRegistryView;
    private ResearchRecordDetailView researchRecordDetailView;
    private AddResearchEntryView addResearchEntryView;
    private RegistryDocumentsView registryDocumentsView;
    private RegistryCorrectionHistoryView registryCorrectionHistoryView;
    private View completionCorrectionView;
    private StaffRegistryData.RegistryRecordItem selectedRegistryRecord;

    // Lifecycle Sub-Pages (Part 8, C-04)
    private MilestonesAndOutcomesView milestonesAndOutcomesView;
    private StageHistoryView stageHistoryView;
    private StaffLifecycleData.ResearchLifecycleState lifecycleState;

    // Reporting Sub-Pages (Part 9, C-05)
    private ResearchReportView researchReportView;
    private ServiceUtilizationView serviceUtilizationView;
    private AccessDeniedReportingView accessDeniedReportingView;

    private final boolean liveWorkspace;
    private final java.util.Set<String> permissions = new java.util.HashSet<>();

    StaffScreen(MainActivity activity, String email, String initialTab) {
        this(activity, email, initialTab, false);
    }

    StaffScreen(MainActivity activity, String email, String initialTab, boolean liveWorkspace) {
        this(activity, email, initialTab, liveWorkspace, null);
    }

    StaffScreen(MainActivity activity, String email, String initialTab, boolean liveWorkspace, JSONArray permissions) {
        super(activity);
        this.activity = activity;
        this.email = email;
        this.liveWorkspace = liveWorkspace;
        if (permissions != null) for (int i = 0; i < permissions.length(); i++) this.permissions.add(permissions.optString(i));
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
        scroll.setFillViewport(true);
        content = new LinearLayout(activity);
        content.setOrientation(VERTICAL);
        scroll.addView(content);
        snackbar = new SaveSnackbar(activity, scroll);
        addView(snackbar, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));

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

    boolean hasPendingWrite(){return liveRequestsView!=null&&liveRequestsView.isSaving()
        ||liveCatalogView!=null&&liveCatalogView.isSaving()||liveAccountsView!=null&&liveAccountsView.isSaving()
        ||liveRegistryView!=null&&liveRegistryView.isSaving()||liveProgressView!=null&&liveProgressView.isSaving();}

    private int tabIndex(String name) {
        for (int i = 0; i < TABS.length; i++) {
            if (TABS[i].equals(name)) return i;
        }
        return -1;
    }

    private void navigateFromNavBar(String destination) {
        int prev = tabIndex(tab);
        int next = tabIndex(destination);
        if (next > prev) {
            tabSlideDirection = 1;
        } else if (next < prev) {
            tabSlideDirection = -1;
        } else {
            tabSlideDirection = 0;
        }
        navigate(destination);
    }

    private void navigate(String destination) {
        if (hasPendingWrite()) return;
        if(previewManage){manageSubPage="hub";previewManage=false;}
        if(previewRegistry){registrySubPage="list";previewRegistry=false;}
        previewRequests = false;
        if ("Requests".equals(destination) && reviewQueueView != null) reviewQueueView.endPreview();
        select(destination);
        loadTabData();
    }

    private void loadTabData() {
        if (!liveWorkspace && hasPermission("request_processing") && ("Requests".equals(tab) || "Dashboard".equals(tab))) {
            activity.loadConsultations(this);
        }
    }

    void updatePermissions(JSONArray current) {
        java.util.Set<String> updated = new java.util.HashSet<>();
        for (int i = 0; i < current.length(); i++) updated.add(current.optString(i));
        if (permissions.equals(updated)) return;
        permissions.clear(); permissions.addAll(updated);
        if (!permissions.contains("request_processing")) liveRequestsView = null;
        if (!permissions.contains("content_management")) liveCatalogView=null;
        if (!permissions.contains("account_administration")) liveAccountsView=null;
        if (!permissions.contains("registry_management")){liveRegistryView=null;liveRegistryDirectory=null;}
        if (!permissions.contains("progress_updates")) liveProgressView=null;
        if (!permissions.contains("reporting")) liveReportsView=null;
        previewRequests = false;
        select("Dashboard");
    }

    private boolean hasPermission(String permission) { return !liveWorkspace || permissions.contains(permission); }

    private void select(String destination) {
        snackbar.clear();
        if (!"Registry".equals(destination) && "completion_correction".equals(registrySubPage)) {
            registrySubPage = "lifecycle";
            completionCorrectionView = null;
        }
        tab = destination;
        navigation.removeAllViews();
        for (int index = 0; index < TABS.length; index++) {
            String name = TABS[index];
            if (liveWorkspace && "Requests".equals(name) && !hasPermission("request_processing")) continue;
            if (liveWorkspace && "Registry".equals(name) && !hasPermission("registry_management") && !hasPermission("progress_updates")) continue;
            boolean selected = name.equals(tab);
            Button item = button(name, () -> navigateFromNavBar(name));
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
        if (tabSlideDirection != 0) {
            ph.edu.umak.crm.app.common.animation.UiMotion.slideHorizontal(content, tabSlideDirection);
            tabSlideDirection = 0;
        } else {
            ph.edu.umak.crm.app.common.animation.UiMotion.animateEntrance(content);
        }
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
        if (liveWorkspace) {
            content.removeAllViews();
            LinearLayout panel = card("Staff workspace");
            panel.addView(text("Open an authorized workspace to view current CRM records.", 14, MUTED, bodyFont));
            if (hasPermission("request_processing")) action(panel, "Service requests", () -> navigate("Requests"));
            if (hasPermission("registry_management")) action(panel, "Research registry", () -> openRegistrySubPage("registered_live"));
            if (hasPermission("progress_updates")) action(panel, "Research progress", () -> openRegistrySubPage("progress_live"));
            if (hasPermission("reporting")) action(panel, "Reports", () -> openManageSubPage("live_reports"));
            action(panel, "Manage authorized workspaces", () -> navigate("Manage"));
            return;
        }
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
        if (hasPermission("registry_management") || hasPermission("progress_updates"))
            action(links, "Research Registry", () -> navigate("Registry"));
        action(links, "Public resources", activity::showPublicResources);
        LinearLayout settings = card("Account Settings");
        settings.addView(text("Email", 12, MUTED, actionFont));
        settings.addView(text(email, 15, NAVY, bodyFont));
        styledAction(settings, "Sign Out", activity::signOut, WHITE, 0xffa62a35, true);
    }

    private void showRequestQueue() {
        content.removeAllViews();
        if (liveWorkspace && !previewRequests) {
            if (!hasPermission("request_processing")) {
                card("Access denied").addView(text("Request processing permission is required.", 14, MUTED, bodyFont));
                return;
            }
            if (liveRequestsView == null) liveRequestsView = new ph.edu.umak.crm.app.staff.queue.LiveStaffRequestsView(activity,
                    new ph.edu.umak.crm.app.staff.queue.LiveStaffRequestsView.Actions() {
                        @Override
                        public void request(String path, org.json.JSONObject body,
                                java.util.function.BiConsumer<String, String> result) {
                            activity.staffRequest(StaffScreen.this, "/api/v1/staff/service-requests" + path, body, result);
                        }

                        @Override
                        public void download(String path, String filename, String mime, Runnable failure) {
                            activity.downloadRequestAttachment("/api/v1/staff/service-requests" + path, filename, mime,
                                    () -> "Requests".equals(tab) && !previewRequests && liveRequestsView != null && liveRequestsView.isAttachedToWindow(),
                                    failure);
                        }

                        @Override
                        public void back() { if (liveWorkspace) navigate("Dashboard"); else openRequestsSubPage("queue"); }
                    });
            if (liveRequestsView.getParent() instanceof android.view.ViewGroup parent) parent.removeView(liveRequestsView);
            content.addView(liveRequestsView);
            liveRequestsView.load();
        } else if ("workspace".equals(requestsSubPage)) {
            ensureRequestWorkspaceView();
            content.addView(requestWorkspaceView.getContentLayout());
        } else {
            ensureReviewQueueView();
            if (consultationsLoading) reviewQueueView.showLoading();
            content.addView(reviewQueueView.getContentLayout());
        }
        scroll.scrollTo(0, 0);
    }

    void openRequestsSubPage(String subPage) {
        snackbar.clear();
        this.requestsSubPage = subPage;
        if (!"Requests".equals(tab)) {
            tab = "Requests";
            navigation.removeAllViews();
            for (int index = 0; index < TABS.length; index++) {
                String name = TABS[index];
            if (liveWorkspace && "Requests".equals(name) && !hasPermission("request_processing")) continue;
            if (liveWorkspace && "Registry".equals(name) && !hasPermission("registry_management") && !hasPermission("progress_updates")) continue;
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
                    if (request.id.startsWith("#")) {
                        ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(activity, request.id,
                                request.serviceType + "\n" + request.status + "\nSubmitted " + request.submittedAt);
                        return;
                    }
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
                public void onSessionDraftKept() {
                    openRequestsSubPage("queue");
                    snackbar.show("Draft kept for this session.");
                }
            });
        }
    }

    private boolean consultationsLoading;

    boolean beginConsultationsRefresh() {
        if (consultationsLoading) return false;
        consultationsLoading = true;
        if ("Requests".equals(tab) && "queue".equals(requestsSubPage)) {
            ensureReviewQueueView();
            reviewQueueView.showLoading();
        }
        return true;
    }

    void showConsultations(JSONArray tickets, String message) {
        consultationsLoading = false;
        List<StaffQueueData.QueueRequestItem> rows = new ArrayList<>();
        if (tickets != null) {
            try {
                for (int i = 0; i < tickets.length(); i++) {
                    org.json.JSONObject ticket = tickets.getJSONObject(i);
                    var request = ph.edu.umak.crm.app.requests.RequestData.fromConsultation(ticket);
                    if ("Cancelled".equals(request.status)) continue;
                    rows.add(new StaffQueueData.QueueRequestItem(request.id, request.status,
                            request.service, "", request.submittedDate,
                            "", "", "", "", "", null, null, null));
                }
                consultations = tickets;
            } catch (org.json.JSONException invalid) {
                message = "CRM returned an invalid review queue. Try refreshing again.";
            }
        }
        consultationMessage = message;
        if (reviewQueueView != null) reviewQueueView.showLiveRequests(rows, message);
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
            case "progress_live":
                showLiveResearchProgress();
                break;
            case "registered_live":
                showRegisteredResearch();
                break;
            case "registered_live_create":
                liveRegistryRecord().create();
                break;
            case "registered_live_detail":
                showRegisteredResearchDetail();
                break;
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
                addResearchEntryView.setEditRecord(selectedRegistryRecord);
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
            case "completion_correction":
                content.addView(completionCorrectionView);
                break;
            case "stage_history":
                ensureStageHistoryView();
                content.addView(stageHistoryView.getContentLayout());
                break;
            case "list":
            default:
                if (liveWorkspace) {
                    if (hasPermission("registry_management")) content.addView(button("Registered research", () -> openRegistrySubPage("registered_live")));
                    if (hasPermission("progress_updates")) content.addView(button("Research progress", () -> openRegistrySubPage("progress_live")));
                    break;
                }
                ensureResearchRegistryView();
                content.addView(researchRegistryView.getContentLayout());
                break;
        }
        scroll.scrollTo(0, 0);
    }

    private void showLiveResearchProgress() {
        if(liveProgressView==null)liveProgressView = new ph.edu.umak.crm.app.staff.lifecycle.LiveResearchProgressEditor(activity,
                new ph.edu.umak.crm.app.staff.lifecycle.LiveResearchProgressEditor.Actions() {
                    @Override
                    public void request(String path, JSONObject body,
                            java.util.function.BiConsumer<String, String> result) {
                        activity.staffRequest(StaffScreen.this, "/api/v1/staff/research-progress" + path, body, result);
                    }

                    @Override
                    public void back() { openRegistrySubPage("list"); }
                });
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.detach(liveProgressView);
        content.addView(liveProgressView);
        liveProgressView.ensureLoaded();
    }

    private static final String REGISTRY_API = "/api/v1/staff/research-registrations";
    private String registryKeyword = "";
    private String registryCollege = "";
    private String registryPeriod = "";
    private int registeredResearch;

    private void showRegisteredResearch(){
        if(liveRegistryDirectory==null)liveRegistryDirectory=new ph.edu.umak.crm.app.staff.registry.LiveRegistryDirectoryView(activity,new ph.edu.umak.crm.app.staff.registry.LiveRegistryDirectoryView.Actions(){
            public void request(String query,java.util.function.BiConsumer<String,String> result){activity.staffRequest(StaffScreen.this,REGISTRY_API+query,null,result);}
            public void open(int id){registeredResearch=id;openRegistrySubPage("registered_live_detail");}
            public void create(){openRegistrySubPage("registered_live_create");}
            public void back(){openRegistrySubPage("list");}
        });
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.detach(liveRegistryDirectory);content.addView(liveRegistryDirectory);liveRegistryDirectory.load();
    }

    private void showRegisteredResearchDetail() {
        liveRegistryRecord().showRecord(registeredResearch);
    }

    private ph.edu.umak.crm.app.staff.registry.LiveRegistryRecordView liveRegistryRecord() {
        if(liveRegistryView==null)liveRegistryView = new ph.edu.umak.crm.app.staff.registry.LiveRegistryRecordView(activity,
                new ph.edu.umak.crm.app.staff.registry.LiveRegistryRecordView.Actions() {
            public void reviewDuplicates(JSONObject body, java.util.function.BiConsumer<String, String> result) {
                activity.staffRequest(StaffScreen.this, "/api/v1/research/duplicate-review", body, result);
            }
            public void request(String path, JSONObject body, java.util.function.BiConsumer<String, String> result) {
                activity.staffRequest(StaffScreen.this, "/api/v1/staff/registry" + path, body, result);
            }
            public void choose(Runnable checking, java.util.function.BiConsumer<MainActivity.SelectedAttachment, String> result) {
                activity.chooseRequestAttachment(checking, result);
            }
            public void upload(MainActivity.SelectedAttachment file, int study, String previous, String token,
                    java.util.function.BiConsumer<String, String> result) {
                activity.uploadRegistryAttachment(StaffScreen.this, file, study, previous, token, result);
            }
            public void download(String path, String name, String mime, Runnable failure) {
                activity.downloadRequestAttachment("/api/v1/staff/registry" + path, name, mime, () -> isAttachedToWindow(), failure);
            }
            public void back() { if(liveRegistryDirectory!=null)liveRegistryDirectory.refresh();openRegistrySubPage("registered_live"); }
            public boolean canProgress(){return hasPermission("progress_updates");}
            public void openProgress(int id){if(hasPermission("progress_updates")){openRegistrySubPage("progress_live");liveProgressView.openRecord(id);}else ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(activity,"Access denied","Research progress permission is required.");}
            public void openRequest(String id){openLiveRequest(id);}
        });
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.detach(liveRegistryView);
        content.addView(liveRegistryView);
        return liveRegistryView;
    }

    void openRegistrySubPage(String subPage) {
        snackbar.clear();
        this.registrySubPage = subPage;
        if (!"Registry".equals(tab)) {
            tab = "Registry";
            navigation.removeAllViews();
            for (int index = 0; index < TABS.length; index++) {
                String name = TABS[index];
            if (liveWorkspace && "Requests".equals(name) && !hasPermission("request_processing")) continue;
            if (liveWorkspace && "Registry".equals(name) && !hasPermission("registry_management") && !hasPermission("progress_updates")) continue;
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
                public void onReviewDuplicates(String title, String abstractText, java.util.function.BiConsumer<String,String> result) {
                    try {
                        JSONObject body = new JSONObject();
                        body.put("title", title);
                        body.put("abstract", abstractText);
                        activity.staffRequest(StaffScreen.this, "/api/v1/research/duplicate-review", body, result);
                    } catch (org.json.JSONException error) {
                        result.accept(null, "Unable to prepare duplicate review");
                    }
                }
                @Override
                public void onSaveEntry(StaffRegistryData.RegistryRecordItem record) {
                    selectedRegistryRecord = record;
                    openRegistrySubPage("detail");
                }

                @Override
                public void onCancel() {
                    openRegistrySubPage("edit_entry".equals(registrySubPage) ? "detail" : "list");
                }

            });
        }
    }

    private void ensureRegistryDocumentsView() {
        if (registryDocumentsView == null) {
            registryDocumentsView = new RegistryDocumentsView(activity, new RegistryDocumentsView.Callbacks() {
                @Override
                public void onBackToRecord() {
                    openRegistrySubPage("detail");
                }

                @Override
                public void onChooseAttachment(Runnable onChecking, java.util.function.BiConsumer<MainActivity.SelectedAttachment, String> result) {
                    activity.chooseRequestAttachment(onChecking, result);
                }
            });
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
                public void onCorrectCompletion(String recordId, String title, String date,
                                                CompletionCorrectionView.Callbacks onSaved) {
                    openCompletionCorrection(recordId, title, date, onSaved);
                }

                @Override
                public void onStageSaved() {
                    snackbar.show("Stage updated.");
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
            case "live_reports":
                showLiveReports();
                break;
            case "live_accounts":
                showLiveAccounts();
                break;
            case "live_catalog":
                showLiveCatalog();
                break;
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
                serviceUtilizationView.setModel(StaffReportingData.getServiceUtilizationResult(UTILIZATION_STATUSES[utilizationStatusFilter]));
                content.addView(serviceUtilizationView.getContentLayout());
                break;
            case "access_denied":
                ensureAccessDeniedReportingView();
                content.addView(accessDeniedReportingView.getContentLayout(),
                        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                break;
            case "hub":
            default:
                if (liveWorkspace) showLiveManage();
                else {
                    ensureManageHubView();
                    content.addView(manageHubView.getContentLayout());
                }
                break;
        }
        scroll.scrollTo(0, 0);
    }

    void openManageSubPage(String subPage) {
        snackbar.clear();
        this.manageSubPage = subPage;
        if (!"Manage".equals(tab)) {
            tab = "Manage";
            navigation.removeAllViews();
            for (int index = 0; index < TABS.length; index++) {
                String name = TABS[index];
            if (liveWorkspace && "Requests".equals(name) && !hasPermission("request_processing")) continue;
            if (liveWorkspace && "Registry".equals(name) && !hasPermission("registry_management") && !hasPermission("progress_updates")) continue;
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

    private void openLiveRequest(String id){
        if(!hasPermission("request_processing")){ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(activity,"Access denied","Request processing permission is required.");return;}
        navigate("Requests");liveRequestsView.openRecord(Integer.parseInt(id.substring(id.lastIndexOf('-')+1)));
    }

    private void showLiveManage() {
        ManageHubView design=new ManageHubView(activity,new ManageHubView.Listener(){
            public void onOpenServiceCatalog(){openManageSubPage("live_catalog");}
            public void onOpenApprovedForms(){openManageSubPage("live_catalog");liveCatalogView.openFormsDirectory();}
            public void onOpenUserAccounts(){openManageSubPage("live_accounts");}
            public void onOpenAuditHistory(){
                new android.app.AlertDialog.Builder(activity).setTitle("Audit History")
                    .setItems(hasPermission("content_management")&&hasPermission("account_administration")?new String[]{"Publication history","Account audit history"}:hasPermission("content_management")?new String[]{"Publication history"}:new String[]{"Account audit history"},(dialog,index)->{
                        if(hasPermission("content_management")&&(index==0))openManageSubPage("live_catalog");else openManageSubPage("live_accounts");
                    }).setNegativeButton("Cancel",null).show();
            }
            public void onOpenAnalyticsReporting(){openManageSubPage("live_reports");}
        });
        java.util.Set<String> visible=new java.util.HashSet<>();
        if(hasPermission("content_management")){visible.add("Service Catalog");visible.add("Approved Forms & Resources");visible.add("Audit History");}
        if(hasPermission("account_administration")){visible.add("User Accounts");visible.add("Audit History");}
        if(hasPermission("reporting"))visible.add("Analytics & Reporting");
        design.setVisibleActions(visible);content.addView(design.getContentLayout());
        if(visible.isEmpty())content.addView(text("No staff management permissions assigned.",14,MUTED,bodyFont));
    }

    private void showLiveReports() {
        if(liveReportsView==null)liveReportsView = new ph.edu.umak.crm.app.staff.reporting.LiveReportsView(activity,
                new ph.edu.umak.crm.app.staff.reporting.LiveReportsView.Actions() {
                    @Override public void request(JSONObject filters, java.util.function.BiConsumer<String, String> result) {
                        activity.staffRequest(StaffScreen.this, "/api/v1/staff/reports", filters, result);
                    }
                    @Override public void back() { openManageSubPage("hub"); }
                    @Override public void openRequest(String id){openLiveRequest(id);}
                    @Override public void openRecord(String id){
                        if(!hasPermission("registry_management")){ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(activity,"Access denied","Registry management permission is required.");return;}
                        registeredResearch=Integer.parseInt(id.substring(id.lastIndexOf('-')+1));openRegistrySubPage("registered_live_detail");
                    }
                });
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.detach(liveReportsView);
        content.addView(liveReportsView); if(liveReportsView.getChildCount()==0)liveReportsView.load();
    }

    private void showLiveAccounts() {
        if(liveAccountsView==null)liveAccountsView = new ph.edu.umak.crm.app.staff.accounts.LiveAccountsView(activity,
                new ph.edu.umak.crm.app.staff.accounts.LiveAccountsView.Actions() {
                    @Override
                    public void request(String path, JSONObject body, java.util.function.BiConsumer<String, String> result) {
                        activity.staffRequest(StaffScreen.this, "/api/v1/staff/accounts" + path, body, result);
                    }
                    @Override public void back() { openManageSubPage("hub"); }
                });
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.detach(liveAccountsView);
        content.addView(liveAccountsView);if(liveAccountsView.getChildCount()==0)liveAccountsView.load();
    }

    private void showLiveCatalog() {
        if(liveCatalogView==null)liveCatalogView = new ph.edu.umak.crm.app.staff.content.LiveServiceCatalogView(activity,
                new ph.edu.umak.crm.app.staff.content.LiveServiceCatalogView.Actions() {
                    @Override
                    public void request(String path, org.json.JSONObject body,
                            java.util.function.BiConsumer<String, String> complete) {
                        activity.staffRequest(StaffScreen.this, "/api/v1/staff/services" + path, body, complete);
                    }

                    @Override
                    public void replaceForm(int id, Runnable complete, java.util.function.Consumer<String> failure) {
                        activity.chooseCatalogForm(StaffScreen.this, id, complete, failure);
                    }

                    @Override
                    public void downloadForm(int id, String name, String mime, Runnable failure) {
                        activity.downloadCatalogForm(id, name, mime,
                                () -> StaffScreen.this.isAttachedToWindow() && "Manage".equals(tab)
                                        && "live_catalog".equals(manageSubPage), failure);
                    }

                    @Override
                    public void back() { openManageSubPage("hub"); }
                });
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.detach(liveCatalogView.view());
        content.addView(liveCatalogView.view());if(liveCatalogView.view().getChildCount()==0)liveCatalogView.load();
    }

    private void ensureManageHubView() {
        if (manageHubView == null) {
            manageHubView = new ManageHubView(activity, new ManageHubView.Listener() {
                @Override
                public void onOpenServiceCatalog() {
                    openManageSubPage(liveWorkspace ? "live_catalog" : "catalog");
                }

                @Override
                public void onOpenApprovedForms() {
                    openManageSubPage(liveWorkspace ? "live_catalog" : "forms");
                }

                @Override
                public void onOpenUserAccounts() {
                    openManageSubPage("accounts");
                }

                @Override
                public void onOpenAuditHistory() {
                    openManageSubPage(liveWorkspace ? "live_catalog" : "history");
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
                    showCatalogSaveUnavailable();
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
                    showCatalogSaveUnavailable();
                }

                @Override
                public void onSaveDraft() {
                    showCatalogSaveUnavailable();
                }

                @Override
                public void onEditStep1() {
                    openManageSubPage("authoring_step1");
                }
            });
        }
    }

    private void showCatalogSaveUnavailable() {
        ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(activity, "Catalog changes can’t be saved",
                "This app can’t save or publish service catalog changes to CRM yet. Your entries remain on this screen.");
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
        previewManage=true;
        openManageSubPage("hub");
    }

    void showPreview5_2_ServiceCatalog() {
        previewManage=true;
        openManageSubPage("catalog");
    }

    void showPreview5_3_AuthoringStep1InApp() {
        previewManage=true;
        ensureServiceAuthoringStep1View();
        serviceAuthoringStep1View.setPreviewState("in_app");
        openManageSubPage("authoring_step1");
    }

    void showPreview5_3_AuthoringStep1External() {
        previewManage=true;
        ensureServiceAuthoringStep1View();
        serviceAuthoringStep1View.setPreviewState("external");
        openManageSubPage("authoring_step1");
    }

    void showPreview5_4_ApprovedForms() {
        previewManage=true;
        openManageSubPage("forms");
    }

    void showPreview5_5_AuthoringStep3Error() {
        previewManage=true;
        ensureServiceAuthoringStep3View();
        serviceAuthoringStep3View.setPreviewState("error");
        openManageSubPage("authoring_step3");
    }

    void showPreview5_5_AuthoringStep3Draft() {
        previewManage=true;
        ensureServiceAuthoringStep3View();
        serviceAuthoringStep3View.setPreviewState("draft");
        openManageSubPage("authoring_step3");
    }

    void showPreview5_6a_WithdrawServiceDialog() {
        previewManage=true;
        openManageSubPage("catalog");
        StaffDialogs.showWithdrawServiceDialog(activity, "SVC-02", "Ethics Review & Clearance", () -> {
            openManageSubPage("history");
        });
    }

    void showPreview5_6b_ReplaceFormVersionDialog() {
        previewManage=true;
        openManageSubPage("forms");
        StaffDialogs.showReplaceFormVersionDialog(activity, () -> {
            openManageSubPage("history");
        });
    }

    void showPreview5_7_PublicationHistory() {
        previewManage=true;
        openManageSubPage("history");
    }

    void showPreview5_8_UserAccounts() {
        previewManage=true;
        openManageSubPage("accounts");
    }

    void showPreview5_9_CreateStaffDefault() {
        previewManage=true;
        ensureCreateStaffAccountView();
        createStaffAccountView.setPreviewState("5.9_default");
        openManageSubPage("create_staff");
    }

    void showPreview5_9_CreateStaffMissingError() {
        previewManage=true;
        ensureCreateStaffAccountView();
        createStaffAccountView.setPreviewState("5.9_error_missing");
        openManageSubPage("create_staff");
    }

    void showPreview5_9_CreateStaffDuplicateError() {
        previewManage=true;
        ensureCreateStaffAccountView();
        createStaffAccountView.setPreviewState("5.9_error_duplicate");
        openManageSubPage("create_staff");
    }

    void showPreview5_10a_AccountDetailsStaff() {
        previewManage=true;
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.10a");
        openManageSubPage("account_details");
    }

    void showPreview5_10b_AccountDetailsResearcher() {
        previewManage=true;
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.10b");
        openManageSubPage("account_details");
    }

    void showPreview5_11a_DeactivateDialog() {
        previewManage=true;
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.11a");
        openManageSubPage("account_details");
    }

    void showPreview5_11b_ActivateDialog() {
        previewManage=true;
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.11b");
        openManageSubPage("account_details");
    }

    void showPreview5_11c_OwnAccountErrorDialog() {
        previewManage=true;
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.11c");
        openManageSubPage("account_details");
    }

    void showPreview5_11d_LastAdminErrorDialog() {
        previewManage=true;
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.11d");
        openManageSubPage("account_details");
    }

    void showPreview5_12_TemporaryCredentialIssuedDialog() {
        previewManage=true;
        ensureAccountDetailsView();
        accountDetailsView.setPreviewState("5.12");
        openManageSubPage("account_details");
    }

    void showPreview5_13_AccountAuditHistory() {
        previewManage=true;
        ensureAccountAuditHistoryView();
        accountAuditHistoryView.setAccount("LeBron R. James", "A1234");
        openManageSubPage("account_audit");
    }

    // --- Part 6 Wireframe Previews ---

    void showPreview6_1_InReviewTab() {
        previewRequests = true;
        ensureReviewQueueView();
        reviewQueueView.setPreviewState("6.1_in_review");
        openRequestsSubPage("queue");
    }

    void showPreview6_1_CompletedTab() {
        previewRequests = true;
        ensureReviewQueueView();
        reviewQueueView.setPreviewState("6.1_completed");
        openRequestsSubPage("queue");
    }

    void showPreview6_2_FilterSheet() {
        previewRequests = true;
        ensureReviewQueueView();
        reviewQueueView.setPreviewState("6.2_filter_sheet");
        openRequestsSubPage("queue");
    }

    void showPreview6_2_NoMatchingRequests() {
        previewRequests = true;
        ensureReviewQueueView();
        reviewQueueView.setPreviewState("6.2_no_matches");
        openRequestsSubPage("queue");
    }

    void showPreview6_3a_RequestWorkspaceRequesterAndChecklist() {
        previewRequests = true;
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.3a_workspace");
        openRequestsSubPage("workspace");
    }

    void showPreview6_3b_RequestWorkspaceDecision() {
        previewRequests = true;
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.3b_decision");
        openRequestsSubPage("workspace");
    }

    void showPreview6_4a_ErrorMissingItemsAndNextAction() {
        previewRequests = true;
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.4a_error_missing");
        openRequestsSubPage("workspace");
    }

    void showPreview6_4b_ErrorStatusChangeNotAllowed() {
        previewRequests = true;
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.4b_error_status_not_allowed");
        openRequestsSubPage("workspace");
    }

    void showPreview6_4c_StaleDataConflict() {
        previewRequests = true;
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.4c_stale_conflict");
        openRequestsSubPage("workspace");
    }

    void showPreview6_5_CompletedRequestCorrection() {
        previewRequests = true;
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.5_completed_correction");
        openRequestsSubPage("workspace");
    }

    void showPreview6_6_ReviewSaved() {
        previewRequests = true;
        ensureRequestWorkspaceView();
        requestWorkspaceView.setPreviewState("6.6_review_saved");
        openRequestsSubPage("workspace");
        snackbar.show("Review saved.");
    }

    // --- Part 7 Wireframe Previews (C-03) ---

    void showPreview7_1a_Results() {
        previewRegistry=true;
        ensureResearchRegistryView();
        researchRegistryView.showDefaultState();
        openRegistrySubPage("list");
    }

    void showPreview7_1b_FiltersApplied() {
        previewRegistry=true;
        ensureResearchRegistryView();
        researchRegistryView.showFiltersAppliedState();
        openRegistrySubPage("list");
    }

    void showPreview7_1c_EmptyState() {
        previewRegistry=true;
        ensureResearchRegistryView();
        researchRegistryView.showEmptyState();
        openRegistrySubPage("list");
    }

    void showPreview7_2_RecordDetail() {
        previewRegistry=true;
        selectedRegistryRecord = StaffRegistryData.getMockRecords().get(0);
        ensureResearchRecordDetailView();
        researchRecordDetailView.setRecord(selectedRegistryRecord);
        openRegistrySubPage("detail");
    }

    void showPreview7_3_TeamPicker() {
        previewRegistry=true;
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_3_TeamPicker();
        openRegistrySubPage("add_entry");
    }

    void showPreview7_3_ErrorAlreadyMember() {
        previewRegistry=true;
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_3_ErrorAlreadyMember();
        openRegistrySubPage("add_entry");
    }

    void showPreview7_3_ErrorNoSuchAccount() {
        previewRegistry=true;
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_3_ErrorNoSuchAccount();
        openRegistrySubPage("add_entry");
    }

    void showPreview7_4_PossibleDuplicate() {
        previewRegistry=true;
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_4_PossibleDuplicate();
        openRegistrySubPage("add_entry");
    }

    void showPreview7_4_ErrorReasonMissing() {
        previewRegistry=true;
        ensureAddResearchEntryView();
        addResearchEntryView.showPreview7_4_ErrorReasonMissing();
        openRegistrySubPage("add_entry");
    }

    void showPreview7_5_StaleDataConflict() {
        previewRegistry=true;
        selectedRegistryRecord = StaffRegistryData.getMockRecords().get(0);
        openRegistrySubPage("edit_entry");
        addResearchEntryView.showPreview7_5_StaleDataConflict();
    }

    void showPreview7_6_DocumentsVersionList() {
        previewRegistry=true;
        selectedRegistryRecord = StaffRegistryData.getMockRecords().get(0);
        ensureRegistryDocumentsView();
        registryDocumentsView.setRecord(selectedRegistryRecord);
        openRegistrySubPage("documents");
    }

    void showPreview7_7_CorrectionHistory() {
        previewRegistry=true;
        selectedRegistryRecord = StaffRegistryData.getMockRecords().get(0);
        ensureRegistryCorrectionHistoryView();
        registryCorrectionHistoryView.setRecord(selectedRegistryRecord);
        openRegistrySubPage("corrections");
    }

    // --- Part 8 Wireframe Previews (C-04) ---

    void showPreview8_1_Default() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1_Default();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1a_ErrorStageNotAllowed() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1a_ErrorStageNotAllowed();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1b_ErrorEffectiveDate() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1b_ErrorEffectiveDate();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1c_ErrorSupportingDetails() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1c_ErrorSupportingDetails();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1d_ErrorNextAction() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1d_ErrorNextAction();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1e_ErrorCompletionDate() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1e_ErrorCompletionDate();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1f_ErrorPresentation() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1f_ErrorPresentation();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_1g_ErrorPublication() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_1g_ErrorPublication();
        openRegistrySubPage("lifecycle");
    }

    void showPreview8_2_MilestoneEditor() {
        previewRegistry=true;
        showPreview8_1_Default();
        StaffLifecycleData.MilestoneItem item = new StaffLifecycleData.MilestoneItem(
                "Data Collection & Analysis",
                "In progress",
                "Target · Nov 15, 2026",
                "2026-11-15",
                "Field logbook, survey batches 1 – 3"
        );
        MilestoneEditorDialog.show(activity, item, updated -> {
            milestonesAndOutcomesView.showPreview8_1_Default();
        });
    }

    void showPreview8_3_StageHistory() {
        previewRegistry=true;
        ensureStageHistoryView();
        openRegistrySubPage("stage_history");
    }

    void showPreview8_4_CompletionCorrection() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_5b_OutcomeRecorded();
        milestonesAndOutcomesView.requestCompletionCorrection();
    }

    private void openCompletionCorrection(String recordId, String title, String date,
                                          CompletionCorrectionView.Callbacks onSaved) {
        completionCorrectionView = CompletionCorrectionView.create(activity, recordId, title, date,
                (correctedDate, reason) -> {
                    onSaved.onCompletionCorrectionSaved(correctedDate, reason);
                    ensureStageHistoryView();
                    stageHistoryView.setState(lifecycleState);
                    openRegistrySubPage("stage_history");
                    completionCorrectionView = null;
                }, this::closeCompletionCorrection);
        openRegistrySubPage("completion_correction");
    }

    private void closeCompletionCorrection() {
        openRegistrySubPage("lifecycle");
        completionCorrectionView = null;
    }

    boolean handleBack() {
        if(liveWorkspace && "Manage".equals(tab)) {
            if("live_catalog".equals(manageSubPage)&&liveCatalogView!=null)return liveCatalogView.handleBack();
            if("live_accounts".equals(manageSubPage)&&liveAccountsView!=null)return liveAccountsView.handleBack();
            if("live_reports".equals(manageSubPage)&&liveReportsView!=null){if(!liveReportsView.handleBack())openManageSubPage("hub");return true;}
            if(!"hub".equals(manageSubPage)){openManageSubPage("hub");return true;}
        }
        if(liveWorkspace && "Registry".equals(tab)) {
            if(("registered_live_detail".equals(registrySubPage)||"registered_live_create".equals(registrySubPage))&&liveRegistryView!=null)return liveRegistryView.handleBack();
            if("progress_live".equals(registrySubPage)&&liveProgressView!=null)return liveProgressView.handleBack();
            if(!"list".equals(registrySubPage)){openRegistrySubPage("list");return true;}
        }
        if ("Requests".equals(tab)) {
            if (liveWorkspace && !previewRequests) {
                if (liveRequestsView != null && liveRequestsView.handleBack()) return true;
                navigate("Dashboard");
            } else if ("workspace".equals(requestsSubPage)) openRequestsSubPage("queue");
            else navigate("Dashboard");
            return true;
        }
        if (!"Registry".equals(tab) || !"completion_correction".equals(registrySubPage)) return false;
        closeCompletionCorrection();
        return true;
    }

    void showPreview8_5a_StageUpdated() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_5a_StageUpdated();
        openRegistrySubPage("lifecycle");
        snackbar.show("Stage updated.");
    }

    void showPreview8_5b_OutcomeRecorded() {
        previewRegistry=true;
        ensureMilestonesAndOutcomesView();
        milestonesAndOutcomesView.showPreview8_5b_OutcomeRecorded();
        openRegistrySubPage("lifecycle");
        snackbar.show("Outcome recorded.");
    }

    // --- Part 9 Views & Previews (C-05) ---

    private void ensureResearchReportView() {
        if (researchReportView == null) {
            researchReportView = new ResearchReportView(activity, new ResearchReportView.Callbacks() {
                @Override
                public void onBackToFilters() {
                    showResearchFilters();
                }

                @Override
                public void onOpenRecord(String recordId) {
                    for (StaffRegistryData.RegistryRecordItem record : StaffRegistryData.getMockRecords()) {
                        if (record.id.equals(recordId)) {
                            selectedRegistryRecord = record;
                            openRegistrySubPage("detail");
                            return;
                        }
                    }
                    ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(activity, "Record unavailable",
                            "The registry has no details for " + recordId + ".");
                }
            });
        }
    }

    private void ensureServiceUtilizationView() {
        if (serviceUtilizationView == null) {
            serviceUtilizationView = new ServiceUtilizationView(activity, new ServiceUtilizationView.Callbacks() {
                @Override
                public void onBackToFilters() {
                    showUtilizationFilters();
                }

                @Override
                public void onOpenRequest(StaffReportingData.RequestSummary summary) {
                    for (StaffQueueData.QueueRequestItem request : StaffQueueData.getInitialQueue()) {
                        if (request.id.equals(summary.id)) {
                            ensureRequestWorkspaceView();
                            requestWorkspaceView.setRequest(request);
                            openRequestsSubPage("workspace");
                            return;
                        }
                    }
                    ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(activity, summary.id,
                            summary.serviceName + " · " + summary.status + "\n\nFull request details are unavailable for this report row.");
                }
            });
        }
    }

    private void showResearchFilters() {
        boolean empty = "research_report_empty".equals(manageSubPage);
        String[] filters = {
                "College: CCIS · Students · All stages",
                "College of Health Sciences · AY 2023–2024 · Published"
        };
        int[] selected = {empty ? 1 : 0};
        new android.app.AlertDialog.Builder(activity)
                .setTitle("Research Report filters")
                .setSingleChoiceItems(filters, selected[0], (dialog, which) -> selected[0] = which)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply Filters", (dialog, which) ->
                        openManageSubPage(selected[0] == 0 ? "research_report" : "research_report_empty"))
                .show();
    }

    private static final String[] UTILIZATION_STATUSES = {"All statuses", "Completed", "In review"};
    private int utilizationStatusFilter;

    private void showUtilizationFilters() {
        int[] selected = {utilizationStatusFilter};
        new android.app.AlertDialog.Builder(activity)
                .setTitle("Service Utilization · Request status")
                .setSingleChoiceItems(UTILIZATION_STATUSES, selected[0], (dialog, which) -> selected[0] = which)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply Filters", (dialog, which) -> {
                    utilizationStatusFilter = selected[0];
                    openManageSubPage("service_utilization");
                })
                .show();
    }

    private void ensureAccessDeniedReportingView() {
        if (accessDeniedReportingView == null) {
            accessDeniedReportingView = new AccessDeniedReportingView(activity, () -> {
                openManageSubPage("hub");
            });
        }
    }

    void showPreview9_1_ResearchReportResult() {
        previewManage=true;
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportResult());
        openManageSubPage("research_report");
    }

    void showPreview9_2_ServiceUtilizationResult() {
        previewManage=true;
        ensureServiceUtilizationView();
        utilizationStatusFilter = 0;
        openManageSubPage("service_utilization");
    }

    void showPreview9_3_ResearchReportEmpty() {
        previewManage=true;
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportEmpty());
        openManageSubPage("research_report_empty");
    }

    void showPreview9_4a_ExportFormatPicker() {
        previewManage=true;
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportResult());
        openManageSubPage("research_report");
        researchReportView.showExport(ExportReportDialog.ExportState.FORMAT_PICKER);
    }

    void showPreview9_4b_ExportPending() {
        previewManage=true;
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportResult());
        openManageSubPage("research_report");
        researchReportView.showExport(ExportReportDialog.ExportState.PENDING);
    }

    void showPreview9_4c_ExportSuccess() {
        previewManage=true;
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportResult());
        openManageSubPage("research_report");
        researchReportView.showExport(ExportReportDialog.ExportState.SUCCESS);
    }

    void showPreview9_4d_ExportFailure() {
        previewManage=true;
        ensureResearchReportView();
        researchReportView.setModel(StaffReportingData.getResearchReportResult());
        openManageSubPage("research_report");
        researchReportView.showExport(ExportReportDialog.ExportState.FAILURE);
    }

    void showPreview9_5_AccessDenied() {
        previewManage=true;
        ensureAccessDeniedReportingView();
        openManageSubPage("access_denied");
    }
}
