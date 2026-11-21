package ph.edu.umak.crm.app.staff.queue;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * 6.1 Review Queue View (In review, Completed, Submitted, For correction tabs, search, filter, and cards).
 */
public class ReviewQueueView {

    public interface Callbacks {
        void onOpenRequest(StaffQueueData.QueueRequestItem request);
        void onRefreshQueue();
        default void onLoadMore() {}
    }

    private final Context context;
    private final Callbacks callbacks;
    private final LinearLayout contentLayout;
    private final LinearLayout requestsListLayout;
    private final EditText searchInput;
    private final LinearLayout activeFilterBanner;
    private final TextView activeFilterText;

    private final List<StaffQueueData.QueueRequestItem> allRequests = new ArrayList<>();
    private final Button refreshBtn;
    private final PendingStatus refreshStatus;
    private boolean loading;
    private boolean loaded;
    private boolean preview;
    private String selectedTab = "In review";
    private String searchQuery = "";
    private String filterService = null;
    private String filterStatus = null;
    private String filterFrom = "";
    private String filterTo = "";
    private final Button moreButton;
    private final TextView pageSummary;
    private boolean forceEmptyState = false;

    private final String[] statusTabs = {"In review", "Submitted", "For correction", "Completed"};
    private final LinearLayout tabsContainer;
    private final List<TextView> tabViews = new ArrayList<>();

    public ReviewQueueView(Context context, Callbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;

        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setBackgroundColor(UiTheme.PAPER);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));

        refreshStatus = new PendingStatus(context);

        // Status Tabs Row
        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout tabRow = null;
        for (int tabIndex = 0; tabIndex < statusTabs.length; tabIndex++) {
            String tabName = statusTabs[tabIndex];
            if (tabIndex % 2 == 0) {
                tabRow = new LinearLayout(context);
                tabRow.setOrientation(LinearLayout.HORIZONTAL);
                tabsContainer.addView(tabRow);
            }
            TextView tabView = new TextView(context);
            tabView.setTypeface(bold);
            ScreenTypography.apply(tabView, 12);
            tabView.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 8),
                    UiTheme.dp(context, 12), UiTheme.dp(context, 8));

            tabView.setMinHeight(UiTheme.dp(context, 48));
            tabView.setGravity(Gravity.CENTER);
            tabView.setOnClickListener(v -> {
                selectedTab = tabName;
                forceEmptyState = false;
                filterStatus = null;
                updateTabAppearance();
                renderRequests();
            });

            tabViews.add(tabView);
            LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            tabParams.setMargins(0, 0, UiTheme.dp(context, 8), UiTheme.dp(context, 8));
            tabRow.addView(tabView, tabParams);
        }
        contentLayout.addView(tabsContainer);

        addSpacing(14);

        // Search & Filter Row
        LinearLayout searchRow = new LinearLayout(context);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);

        searchInput = new EditText(context);
        searchInput.setHint("Search by request ID, title or requester");
        searchInput.setMinHeight(UiTheme.dp(context, 48));
        searchInput.setHintTextColor(UiTheme.MUTED);
        searchInput.setTextColor(UiTheme.NAVY);
        searchInput.setTypeface(regular);
        ScreenTypography.apply(searchInput, 13);
        searchInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                UiTheme.dp(context, 12), UiTheme.dp(context, 10));

        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(UiTheme.WHITE);
        searchBg.setCornerRadius(UiTheme.dp(context, 8));
        searchBg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        searchInput.setBackground(searchBg);

        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        searchRow.addView(searchInput, searchLp);

        View btnSpace = new View(context);
        btnSpace.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 8), 1));
        searchRow.addView(btnSpace);

        Button filterBtn = UiButton.outline(context, "Filter", UiTheme.BORDER, UiTheme.NAVY, () -> {
            QueueFilterDialog.show(context, filterService, filterStatus, filterFrom, filterTo,
                    preview ? java.util.List.of("Ethics Clearance", "Research Consultation", "Research Registration", "CRM Clearance Processing", "Statistical Report Generation", "UNIVERSITY Journal Endorsement")
                            : availableServices(), new QueueFilterDialog.FilterCallback() {
                @Override
                public void onApply(String service, String status, String fromDate, String toDate) {
                    applyFilters(service, status, fromDate, toDate);
                }

                @Override
                public void onClear() {
                    clearFilters();
                }
            });
        });
        filterBtn.setMinHeight(UiTheme.dp(context, 48));
        searchRow.addView(filterBtn);

        contentLayout.addView(searchRow);

        // Active Filter Banner (for 6.2 no matches state or applied filters)
        activeFilterBanner = new LinearLayout(context);
        activeFilterBanner.setOrientation(LinearLayout.HORIZONTAL);
        activeFilterBanner.setGravity(Gravity.CENTER_VERTICAL);
        activeFilterBanner.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 8),
                UiTheme.dp(context, 12), UiTheme.dp(context, 8));
        activeFilterBanner.setBackground(UiTheme.shape(context, UiTheme.PALE, 6, UiTheme.BORDER));

        activeFilterText = new TextView(context);
        activeFilterText.setTextColor(UiTheme.NAVY);
        activeFilterText.setTypeface(regular);
        ScreenTypography.apply(activeFilterText, 12);
        LinearLayout.LayoutParams afLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        activeFilterBanner.addView(activeFilterText, afLp);

        Button clearSmallBtn = UiButton.outline(context, "Clear", UiTheme.BORDER, UiTheme.NAVY, this::clearFilters);
        clearSmallBtn.setMinHeight(UiTheme.dp(context, 48));
        activeFilterBanner.addView(clearSmallBtn);

        activeFilterBanner.setVisibility(View.GONE);
        contentLayout.addView(activeFilterBanner);

        addSpacing(14);

        // Requests List Layout
        requestsListLayout = new LinearLayout(context);
        requestsListLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.addView(requestsListLayout);

        addSpacing(16);

        // Refresh Button
        refreshBtn = UiButton.outline(context, "Refresh", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (preview) {
                refreshStatus.result("Sample queue. Open the staff workspace to refresh CRM requests.", false);
            } else if (callbacks != null && !loading) {
                callbacks.onRefreshQueue();
            }
        });
        contentLayout.addView(refreshBtn);
        contentLayout.addView(refreshStatus);
        pageSummary = new TextView(context);
        pageSummary.setTextColor(UiTheme.MUTED);
        pageSummary.setTypeface(regular);
        ScreenTypography.apply(pageSummary, 12);
        pageSummary.setVisibility(View.GONE);
        contentLayout.addView(pageSummary);
        moreButton = UiButton.outline(context, "Load more requests", UiTheme.BORDER, UiTheme.NAVY,
                () -> { if (!loading && callbacks != null) callbacks.onLoadMore(); });
        moreButton.setVisibility(View.GONE);
        contentLayout.addView(moreButton);

        // Search text watcher
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString().trim() : "";
                renderRequests();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        updateTabAppearance();
        renderRequests();
    }

    public View getContentLayout() {
        if (contentLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) contentLayout.getParent()).removeView(contentLayout);
        }
        return PageTitle.wrap(contentLayout, "Review Queue", null, false);
    }

    public void setPreviewState(String state) {
        preview = true;
        loading = false;
        refreshBtn.setText("Refresh");
        refreshBtn.setEnabled(true);
        refreshStatus.clear();
        allRequests.clear();
        allRequests.addAll(StaffQueueData.getInitialQueue());
        if ("6.1_completed".equalsIgnoreCase(state)) {
            selectedTab = "Completed";
            forceEmptyState = false;
            filterService = null;
            filterStatus = null;
        } else if ("6.2_no_matches".equalsIgnoreCase(state)) {
            selectedTab = "In review";
            filterService = "Statistical Report Generation";
            filterStatus = "In review";
            forceEmptyState = true;
        } else if ("6.2_filter_sheet".equalsIgnoreCase(state)) {
            selectedTab = "In review";
            forceEmptyState = false;
            filterService = null;
            filterStatus = null;
            QueueFilterDialog.show(context, "Ethics Clearance", "In review", "2026-10-01", "2026-10-31", new QueueFilterDialog.FilterCallback() {
                @Override
                public void onApply(String service, String status, String fromDate, String toDate) {
                    filterFrom = fromDate;
                    filterTo = toDate;
                    filterService = "All Services".equalsIgnoreCase(service) ? null : service;
                    filterStatus = "All".equalsIgnoreCase(status) ? null : status;
                    if (filterStatus != null) selectedTab = filterStatus;
                    updateTabAppearance();
                    renderRequests();
                }

                @Override
                public void onClear() {
                    clearFilters();
                }
            });
        } else {
            // Default 6.1_in_review
            selectedTab = "In review";
            forceEmptyState = false;
            filterService = null;
            filterStatus = null;
        }
        updateTabAppearance();
        renderRequests();
    }

    public void endPreview() {
        if (!preview) return;
        preview = false;
        loaded = false;
        allRequests.clear();
        refreshStatus.clear();
        clearFilters();
    }

    public void showLoading() {
        if (preview) return;
        loading = true;
        refreshBtn.setText("Refreshing…");
        refreshBtn.setEnabled(false);
        refreshStatus.working(loaded ? "Refreshing the review queue…" : "Loading the review queue…");
        renderRequests();
    }

    public void showLiveRequests(List<StaffQueueData.QueueRequestItem> tickets, String error) {
        if (preview) return;
        loading = false;
        refreshBtn.setText("Refresh");
        refreshBtn.setEnabled(true);
        if (error != null) {
            refreshStatus.result(loaded ? error + " Showing the last loaded requests." : error, true);
        } else {
            allRequests.clear();
            allRequests.addAll(tickets);
            if (!loaded && getCountForTab(selectedTab) == 0 && getCountForTab("Submitted") > 0) {
                selectedTab = "Submitted";
            }
            loaded = true;
            refreshStatus.result("Review queue updated.", false);
            updateTabAppearance();
        }
        renderRequests();
    }

    public void applyFilters(String service, String status, String fromDate, String toDate) {
        filterFrom = fromDate == null ? "" : fromDate;
        filterTo = toDate == null ? "" : toDate;
        filterService = service == null || "All Services".equalsIgnoreCase(service) ? null : service;
        filterStatus = status == null || "All".equalsIgnoreCase(status) ? null : status;
        if (filterStatus != null) selectedTab = filterStatus;
        forceEmptyState = false;
        updateTabAppearance();
        renderRequests();
    }

    private java.util.List<String> availableServices() {
        java.util.Set<String> services = new java.util.TreeSet<>();
        for (var request : allRequests) services.add(request.serviceType);
        if (filterService != null) services.add(filterService);
        return new java.util.ArrayList<>(services);
    }

    public void showPagination(boolean more, int loadedCount, boolean busy) {
        moreButton.setVisibility(more ? View.VISIBLE : View.GONE);
        moreButton.setEnabled(!busy);
        pageSummary.setVisibility(View.VISIBLE);
        pageSummary.setText(more ? "Status counts, search and filters cover " + loadedCount + " loaded requests. Load more to include older requests."
                : loadedCount + " requests loaded.");
    }

    public void showMessage(String message, boolean error) { refreshStatus.result(message, error); }

    private void clearFilters() {
        filterService = null;
        filterStatus = null;
        filterFrom = filterTo = "";
        forceEmptyState = false;
        searchQuery = "";
        searchInput.setText("");
        activeFilterBanner.setVisibility(View.GONE);
        updateTabAppearance();
        renderRequests();
    }

    private void updateTabAppearance() {
        for (int i = 0; i < statusTabs.length; i++) {
            String tabName = statusTabs[i];
            TextView tv = tabViews.get(i);
            int count = getCountForTab(tabName);
            tv.setText(tabName + " (" + count + ")");

            boolean isSelected = tabName.equalsIgnoreCase(selectedTab);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(UiTheme.dp(context, 16));

            if (isSelected) {
                bg.setColor(UiTheme.NAVY);
                tv.setTextColor(UiTheme.WHITE);
            } else {
                bg.setColor(UiTheme.WHITE);
                bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
                tv.setTextColor(UiTheme.NAVY);
            }
            tv.setBackground(bg);
        }
    }

    private int getCountForTab(String tabName) {
        if (!preview) {
            int count = 0;
            for (StaffQueueData.QueueRequestItem request : allRequests) {
                if (tabName.equalsIgnoreCase(request.status)) count++;
            }
            return count;
        }
        if ("Completed".equalsIgnoreCase(tabName)) return 12; // Matching wireframe 6.1
        if ("In review".equalsIgnoreCase(tabName)) return 2;
        if ("Submitted".equalsIgnoreCase(tabName)) return 3;
        if ("For correction".equalsIgnoreCase(tabName)) return 1;
        return 0;
    }

    private void renderRequests() {
        requestsListLayout.removeAllViews();

        if (filterService != null || filterStatus != null || !filterFrom.isEmpty() || !filterTo.isEmpty()) {
            activeFilterBanner.setVisibility(View.VISIBLE);
            String banner = "";
            if (filterService != null) banner += "Service: " + filterService;
            if (filterStatus != null) {
                if (!banner.isEmpty()) banner += " · ";
                banner += "Status: " + filterStatus;
            }
            if (!filterFrom.isEmpty()) banner += " · From: " + filterFrom;
            if (!filterTo.isEmpty()) banner += " · To: " + filterTo;
            activeFilterText.setText(banner);
        } else {
            activeFilterBanner.setVisibility(View.GONE);
        }

        if (!preview && !loaded) return;

        if (forceEmptyState) {
            renderEmptyState();
            return;
        }

        List<StaffQueueData.QueueRequestItem> filtered = new ArrayList<>();
        String query = searchQuery.toLowerCase(Locale.ROOT);

        for (StaffQueueData.QueueRequestItem req : allRequests) {
            boolean matchesTab = req.status.equalsIgnoreCase(selectedTab);
            if (!matchesTab) continue;

            if (filterService != null && !req.serviceType.equalsIgnoreCase(filterService)) {
                continue;
            }

            if (!filterFrom.isEmpty() && req.submittedDate.compareTo(filterFrom) < 0) continue;
            if (!filterTo.isEmpty() && req.submittedDate.compareTo(filterTo) > 0) continue;

            if (!query.isEmpty()) {
                boolean match = req.id.toLowerCase(Locale.ROOT).contains(query)
                        || req.studyTitle.toLowerCase(Locale.ROOT).contains(query)
                        || req.requesterName.toLowerCase(Locale.ROOT).contains(query)
                        || req.requesterEmail.toLowerCase(Locale.ROOT).contains(query)
                        || req.serviceType.toLowerCase(Locale.ROOT).contains(query);
                if (!match) continue;
            }

            filtered.add(req);
        }

        if (filtered.isEmpty()) {
            renderEmptyState();
            return;
        }

        for (StaffQueueData.QueueRequestItem item : filtered) {
            requestsListLayout.addView(createRequestCard(item));
            View s = new View(context);
            s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 12)));
            requestsListLayout.addView(s);
        }
    }

    private void renderEmptyState() {
        LinearLayout emptyCard = new LinearLayout(context);
        emptyCard.setOrientation(LinearLayout.VERTICAL);
        emptyCard.setGravity(Gravity.CENTER);
        emptyCard.setPadding(UiTheme.dp(context, 24), UiTheme.dp(context, 32),
                UiTheme.dp(context, 24), UiTheme.dp(context, 32));
        emptyCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView emptyText = new TextView(context);
        emptyText.setText("No matching requests");
        emptyText.setTextColor(UiTheme.NAVY);
        emptyText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(emptyText, 16);
        emptyCard.addView(emptyText);

        View sp = new View(context);
        sp.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, 14)));
        emptyCard.addView(sp);

        Button clearBtn = UiButton.outline(context, "Clear filters", UiTheme.BORDER, UiTheme.NAVY, this::clearFilters);
        emptyCard.addView(clearBtn);

        requestsListLayout.addView(emptyCard);
    }

    private View createRequestCard(StaffQueueData.QueueRequestItem item) {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 14), UiTheme.dp(context, 14),
                UiTheme.dp(context, 14), UiTheme.dp(context, 14));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        // Header: ID and Status Badge
        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView idView = new TextView(context);
        idView.setText(item.id);
        idView.setContentDescription("Open request " + item.id);
        idView.setOnClickListener(view -> { if (callbacks != null) callbacks.onOpenRequest(item); });
        idView.setTextColor(UiTheme.NAVY);
        idView.setTypeface(bold);
        ScreenTypography.apply(idView, 14);
        LinearLayout.LayoutParams idLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        topRow.addView(idView, idLp);

        TextView badge = createStatusBadge(item.status);
        topRow.addView(badge);
        card.addView(topRow);

        addSpacing(card, 6);

        // Service Type
        TextView serviceView = new TextView(context);
        serviceView.setText(item.serviceType);
        serviceView.setTextColor(UiTheme.ACCENT);
        serviceView.setTypeface(bold);
        ScreenTypography.apply(serviceView, 13);
        card.addView(serviceView);

        if (!item.studyTitle.isEmpty()) {
            // Study Title
            TextView titleView = new TextView(context);
            titleView.setText(item.studyTitle);
            titleView.setTextColor(UiTheme.NAVY);
            titleView.setTypeface(bold);
            ScreenTypography.apply(titleView, 15);
            titleView.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 4));
            card.addView(titleView);
        }

        // Submitted info
        TextView subView = new TextView(context);
        subView.setText("Submitted " + item.submittedAt);
        subView.setTextColor(UiTheme.MUTED);
        subView.setTypeface(regular);
        ScreenTypography.apply(subView, 12);
        card.addView(subView);

        if (!item.requesterName.isEmpty()) {
            // Requester name and role
            TextView reqView = new TextView(context);
            reqView.setText(item.requesterName + " (" + item.requesterRole + ")");
            reqView.setTextColor(UiTheme.MUTED);
            reqView.setTypeface(regular);
            ScreenTypography.apply(reqView, 12);
            card.addView(reqView);
        }

        // Summary snippet
        if (item.summary != null && !item.summary.isEmpty()) {
            addSpacing(card, 6);
            TextView sumView = new TextView(context);
            sumView.setText(item.summary);
            sumView.setTextColor(UiTheme.MUTED);
            sumView.setTypeface(regular);
            ScreenTypography.apply(sumView, 13);
            card.addView(sumView);
        }

        addSpacing(card, 12);

        // Action Button: View Assigned Request or View Completed Request
        boolean isCompleted = "Completed".equalsIgnoreCase(item.status);
        String actionTitle = isCompleted ? "View Completed Request" : "View Assigned Request";
        Button actionBtn = UiButton.primary(context, actionTitle, () -> {
            if (callbacks != null) {
                callbacks.onOpenRequest(item);
            }
        });
        actionBtn.setMinHeight(UiTheme.dp(context, 48));
        card.addView(actionBtn);

        return card;
    }

    private TextView createStatusBadge(String status) {
        Typeface bold = UiTheme.boldFont(context);
        TextView badge = new TextView(context);
        badge.setText(status);
        badge.setTypeface(bold);
        ScreenTypography.apply(badge, 11);
        badge.setPadding(UiTheme.dp(context, 8), UiTheme.dp(context, 3),
                UiTheme.dp(context, 8), UiTheme.dp(context, 3));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(UiTheme.dp(context, 4));

        if ("In review".equalsIgnoreCase(status)) {
            bg.setColor(Color.parseColor("#EBF3FA"));
            badge.setTextColor(Color.parseColor("#1B4D7E"));
        } else if ("Completed".equalsIgnoreCase(status)) {
            bg.setColor(Color.parseColor("#E6F4EA"));
            badge.setTextColor(Color.parseColor("#137333"));
        } else if ("For correction".equalsIgnoreCase(status)) {
            bg.setColor(UiTheme.AMBER_BG);
            badge.setTextColor(UiTheme.AMBER_TEXT);
        } else {
            bg.setColor(UiTheme.TAG_BG);
            badge.setTextColor(UiTheme.TAG_TEXT);
        }

        badge.setBackground(bg);
        return badge;
    }

    private void addSpacing(int dp) {
        View s = new View(context);
        s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        contentLayout.addView(s);
    }

    private void addSpacing(LinearLayout parent, int dp) {
        View s = new View(context);
        s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(s);
    }
}
