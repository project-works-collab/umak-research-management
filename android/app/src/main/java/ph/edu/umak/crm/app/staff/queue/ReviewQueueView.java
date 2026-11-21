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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * 6.1 & 6.2 Review Queue View (In review, Completed, Submitted, For correction tabs, search, filter, and cards).
 */
public class ReviewQueueView {

    public interface Callbacks {
        void onOpenRequest(StaffQueueData.QueueRequestItem request);
        void onRefreshQueue();
    }

    private final Context context;
    private final Callbacks callbacks;
    private final LinearLayout contentLayout;
    private final LinearLayout requestsListLayout;
    private final EditText searchInput;
    private final LinearLayout activeFilterContainer;

    private final List<StaffQueueData.QueueRequestItem> allRequests = new ArrayList<>();
    private final Button refreshBtn;
    private final Button viewAssignedBtn;
    private final PendingStatus refreshStatus;
    private boolean loading;
    private boolean loaded;
    private boolean preview;
    private String selectedTab = "In review";
    private String searchQuery = "";
    private String filterService = null;
    private String filterStatus = null;
    private boolean forceEmptyState = false;

    private final String[] statusTabs = {"Submitted", "In review", "For correction", "Completed"};
    private final List<LinearLayout> tabColumns = new ArrayList<>();

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

        // 1. Status Tabs Bar (4 Columns)
        LinearLayout tabsBar = new LinearLayout(context);
        tabsBar.setOrientation(LinearLayout.HORIZONTAL);
        tabsBar.setBackground(UiTheme.shape(context, UiTheme.WHITE, 0, 0));

        for (String tabName : statusTabs) {
            LinearLayout col = new LinearLayout(context);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER);
            col.setPadding(0, UiTheme.dp(context, 8), 0, 0);

            TextView titleView = new TextView(context);
            titleView.setText(tabName);
            titleView.setTypeface(bold);
            titleView.setGravity(Gravity.CENTER);
            ScreenTypography.apply(titleView, 12);
            col.addView(titleView);

            TextView countView = new TextView(context);
            countView.setText("(" + getCountForTab(tabName) + ")");
            countView.setTypeface(regular);
            countView.setGravity(Gravity.CENTER);
            ScreenTypography.apply(countView, 11);
            col.addView(countView);

            View indicator = new View(context);
            LinearLayout.LayoutParams indLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 3));
            indLp.topMargin = UiTheme.dp(context, 6);
            indicator.setLayoutParams(indLp);
            col.addView(indicator);

            col.setOnClickListener(v -> {
                selectedTab = tabName;
                forceEmptyState = false;
                filterService = null;
                filterStatus = null;
                updateTabAppearance();
                renderRequests();
            });

            tabColumns.add(col);
            tabsBar.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        contentLayout.addView(tabsBar);

        // Divider below tab bar
        View tabDivider = new View(context);
        tabDivider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 1)));
        tabDivider.setBackgroundColor(UiTheme.BORDER);
        contentLayout.addView(tabDivider);

        addSpacing(14);

        // 2. Search & Filter Row
        LinearLayout searchRow = new LinearLayout(context);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);

        // Search Input Box with Icon
        FrameLayout searchContainer = new FrameLayout(context);
        searchContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        ImageView searchIcon = new ImageView(context);
        searchIcon.setImageResource(R.drawable.ic_researcher_search);
        searchIcon.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams sIconLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 20), UiTheme.dp(context, 20));
        sIconLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        sIconLp.leftMargin = UiTheme.dp(context, 12);
        searchContainer.addView(searchIcon, sIconLp);

        searchInput = new EditText(context);
        searchInput.setHint("Search by request ID, title or requester");
        searchInput.setHintTextColor(0xff94a3b8);
        searchInput.setTextColor(0xff082d50);
        searchInput.setTypeface(regular);
        searchInput.setBackground(null);
        ScreenTypography.apply(searchInput, 13);
        searchInput.setPadding(UiTheme.dp(context, 38), 0, UiTheme.dp(context, 12), 0);
        searchInput.setSingleLine(true);
        FrameLayout.LayoutParams sInputLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 44));
        searchContainer.addView(searchInput, sInputLp);

        searchRow.addView(searchContainer, new LinearLayout.LayoutParams(0, UiTheme.dp(context, 44), 1f));

        View btnSpace = new View(context);
        btnSpace.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 8), 1));
        searchRow.addView(btnSpace);

        // Filter Button with sliders icon
        FrameLayout filterBtn = new FrameLayout(context);
        filterBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER), 0x20082d50));
        ImageView filterIcon = new ImageView(context);
        filterIcon.setImageResource(R.drawable.ic_crm_filter);
        filterIcon.setColorFilter(0xff082d50);
        FrameLayout.LayoutParams fLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 20), UiTheme.dp(context, 20));
        fLp.gravity = Gravity.CENTER;
        filterBtn.addView(filterIcon, fLp);
        filterBtn.setOnClickListener(v -> {
            QueueFilterDialog.show(context, filterService, filterStatus, null, null, new QueueFilterDialog.FilterCallback() {
                @Override
                public void onApply(String service, String status, String fromDate, String toDate) {
                    filterService = "All Services".equalsIgnoreCase(service) ? null : service;
                    filterStatus = "All".equalsIgnoreCase(status) ? null : status;
                    if (filterStatus != null) {
                        selectedTab = filterStatus;
                    }
                    forceEmptyState = "Statistical Report Generation".equalsIgnoreCase(filterService);
                    updateTabAppearance();
                    renderRequests();
                }

                @Override
                public void onClear() {
                    clearFilters();
                }
            });
        });
        searchRow.addView(filterBtn, new LinearLayout.LayoutParams(UiTheme.dp(context, 44), UiTheme.dp(context, 44)));

        contentLayout.addView(searchRow);

        addSpacing(10);

        // 3. Active Filter Chips Row (e.g. Service: Statistical Report Generation ✕, Status: In review ✕)
        activeFilterContainer = new LinearLayout(context);
        activeFilterContainer.setOrientation(LinearLayout.HORIZONTAL);
        activeFilterContainer.setVisibility(View.GONE);
        contentLayout.addView(activeFilterContainer);

        addSpacing(10);

        // 4. Requests List Layout
        requestsListLayout = new LinearLayout(context);
        requestsListLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.addView(requestsListLayout);

        addSpacing(16);

        // 5. Bottom Action Buttons: [ View Assigned Request ] (Yellow) & [ ⟳ Refresh ] (Outline)
        viewAssignedBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        viewAssignedBtn.setText("View Assigned Request");
        viewAssignedBtn.setAllCaps(false);
        viewAssignedBtn.setTextColor(0xff082d50);
        viewAssignedBtn.setTypeface(bold);
        ScreenTypography.apply(viewAssignedBtn, 15);
        viewAssignedBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xfffbbf24, 8, 0), 0x20082d50));
        viewAssignedBtn.setMinHeight(UiTheme.dp(context, 46));
        viewAssignedBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                // Open first item or REQ-2026-089
                StaffQueueData.QueueRequestItem target = allRequests.isEmpty() ? null : allRequests.get(0);
                if (target != null) callbacks.onOpenRequest(target);
            }
        });
        contentLayout.addView(viewAssignedBtn);

        addSpacing(10);

        refreshBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        refreshBtn.setText("⟳ Refresh");
        refreshBtn.setAllCaps(false);
        refreshBtn.setTextColor(0xff082d50);
        refreshBtn.setTypeface(bold);
        ScreenTypography.apply(refreshBtn, 14);
        refreshBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        refreshBtn.setMinHeight(UiTheme.dp(context, 44));
        refreshBtn.setOnClickListener(v -> {
            if (preview) {
                refreshStatus.result("Sample queue. Open the staff workspace to refresh CRM requests.", false);
            } else if (callbacks != null && !loading) {
                callbacks.onRefreshQueue();
            }
        });
        contentLayout.addView(refreshBtn);
        contentLayout.addView(refreshStatus);

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

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        // Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(context);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = UiTheme.dp(context, 16);
        band.setPadding(pad, pad, pad, pad);

        TextView heading = new TextView(context);
        heading.setText("Review Queue");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        root.addView(band);
        root.addView(contentLayout);
        return root;
    }

    public void setPreviewState(String state) {
        preview = true;
        loading = false;
        refreshBtn.setText("⟳ Refresh");
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

    public void setSelectedTab(String tabName) {
        this.selectedTab = tabName;
        this.forceEmptyState = false;
        this.filterService = null;
        this.filterStatus = null;
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
        refreshBtn.setText("⟳ Refresh");
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

    private void clearFilters() {
        filterService = null;
        filterStatus = null;
        forceEmptyState = false;
        searchQuery = "";
        searchInput.setText("");
        activeFilterContainer.setVisibility(View.GONE);
        updateTabAppearance();
        renderRequests();
    }

    private void updateTabAppearance() {
        for (int i = 0; i < statusTabs.length; i++) {
            String tabName = statusTabs[i];
            LinearLayout col = tabColumns.get(i);
            TextView titleView = (TextView) col.getChildAt(0);
            TextView countView = (TextView) col.getChildAt(1);
            View indicator = col.getChildAt(2);

            int count = getCountForTab(tabName);
            countView.setText("(" + count + ")");

            boolean isSelected = tabName.equalsIgnoreCase(selectedTab);
            if (isSelected) {
                titleView.setTextColor(0xff082d50);
                countView.setTextColor(0xff082d50);
                indicator.setBackgroundColor(0xff082d50);
            } else {
                titleView.setTextColor(0xff64748b);
                countView.setTextColor(0xff64748b);
                indicator.setBackgroundColor(Color.TRANSPARENT);
            }
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
        activeFilterContainer.removeAllViews();

        if (filterService != null || filterStatus != null) {
            activeFilterContainer.setVisibility(View.VISIBLE);
            if (filterService != null) {
                activeFilterContainer.addView(createFilterChip("Service: " + filterService, () -> {
                    filterService = null;
                    if (filterStatus == null) clearFilters();
                    else renderRequests();
                }));
            }
            if (filterStatus != null) {
                activeFilterContainer.addView(createFilterChip("Status: " + filterStatus, () -> {
                    filterStatus = null;
                    if (filterService == null) clearFilters();
                    else renderRequests();
                }));
            }
        } else {
            activeFilterContainer.setVisibility(View.GONE);
        }

        if (!preview && !loaded) return;

        if (forceEmptyState) {
            renderEmptyState();
            viewAssignedBtn.setVisibility(View.GONE);
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

            if (!query.isEmpty()) {
                boolean match = req.id.toLowerCase(Locale.ROOT).contains(query)
                        || req.studyTitle.toLowerCase(Locale.ROOT).contains(query)
                        || req.requesterName.toLowerCase(Locale.ROOT).contains(query)
                        || req.serviceType.toLowerCase(Locale.ROOT).contains(query);
                if (!match) continue;
            }

            filtered.add(req);
        }

        if (filtered.isEmpty()) {
            renderEmptyState();
            viewAssignedBtn.setVisibility(View.GONE);
            return;
        }

        viewAssignedBtn.setVisibility(View.VISIBLE);
        for (StaffQueueData.QueueRequestItem item : filtered) {
            requestsListLayout.addView(createRequestCard(item));
            View s = new View(context);
            s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 10)));
            requestsListLayout.addView(s);
        }
    }

    private View createFilterChip(String label, Runnable onRemove) {
        LinearLayout chip = new LinearLayout(context);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setBackground(UiTheme.shape(context, 0xffe0f2fe, 14, 0));
        chip.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 4),
                UiTheme.dp(context, 10), UiTheme.dp(context, 4));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = UiTheme.dp(context, 8);
        chip.setLayoutParams(lp);

        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextColor(0xff0284c7);
        tv.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(tv, 12);
        chip.addView(tv);

        TextView xBtn = new TextView(context);
        xBtn.setText("  ✕");
        xBtn.setTextColor(0xff0284c7);
        xBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(xBtn, 12);
        xBtn.setOnClickListener(v -> onRemove.run());
        chip.addView(xBtn);

        return chip;
    }

    private void renderEmptyState() {
        LinearLayout emptyCard = new LinearLayout(context);
        emptyCard.setOrientation(LinearLayout.VERTICAL);
        emptyCard.setGravity(Gravity.CENTER);
        emptyCard.setPadding(UiTheme.dp(context, 24), UiTheme.dp(context, 32),
                UiTheme.dp(context, 24), UiTheme.dp(context, 32));
        emptyCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        // Circular Icon Avatar with Magnifying Glass
        ImageView iconView = new ImageView(context);
        iconView.setImageResource(R.drawable.ic_researcher_search);
        iconView.setColorFilter(0xff082d50);
        int aSize = UiTheme.dp(context, 48);
        int iconPad = UiTheme.dp(context, 12);
        iconView.setPadding(iconPad, iconPad, iconPad, iconPad);
        LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(aSize, aSize);
        iLp.bottomMargin = UiTheme.dp(context, 14);
        iconView.setLayoutParams(iLp);

        GradientDrawable aBg = new GradientDrawable();
        aBg.setShape(GradientDrawable.OVAL);
        aBg.setColor(Color.parseColor("#F1F5F9"));
        iconView.setBackground(aBg);
        emptyCard.addView(iconView);

        TextView emptyText = new TextView(context);
        emptyText.setText("No matching requests");
        emptyText.setTextColor(0xff082d50);
        emptyText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(emptyText, 16);
        emptyCard.addView(emptyText);

        View sp = new View(context);
        sp.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, 14)));
        emptyCard.addView(sp);

        Button clearBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        clearBtn.setText("Clear filters");
        clearBtn.setAllCaps(false);
        clearBtn.setTextColor(0xff082d50);
        clearBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(clearBtn, 14);
        clearBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xff082d50), 0x20082d50));
        clearBtn.setMinHeight(UiTheme.dp(context, 40));
        clearBtn.setPadding(UiTheme.dp(context, 20), 0, UiTheme.dp(context, 20), 0);
        clearBtn.setOnClickListener(v -> clearFilters());
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

        // Top row: ID and Status Badge
        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView idView = new TextView(context);
        idView.setText(item.id);
        idView.setTextColor(0xff185d9d);
        idView.setTypeface(bold);
        ScreenTypography.apply(idView, 12);
        LinearLayout.LayoutParams idLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        topRow.addView(idView, idLp);

        TextView badge = createStatusBadge(item.status);
        topRow.addView(badge);
        card.addView(topRow);

        addSpacing(card, 4);

        // Service Type
        TextView serviceView = new TextView(context);
        serviceView.setText(item.serviceType);
        serviceView.setTextColor(0xff082d50);
        serviceView.setTypeface(bold);
        ScreenTypography.apply(serviceView, 16);
        card.addView(serviceView);

        if (item.studyTitle != null && !item.studyTitle.isEmpty()) {
            TextView titleView = new TextView(context);
            titleView.setText(item.studyTitle);
            titleView.setTextColor(0xff64748b);
            titleView.setTypeface(regular);
            ScreenTypography.apply(titleView, 13);
            card.addView(titleView);
        }

        addSpacing(card, 6);

        // Submitted info
        TextView subView = new TextView(context);
        subView.setText("Submitted " + item.submittedAt);
        subView.setTextColor(0xff64748b);
        subView.setTypeface(regular);
        ScreenTypography.apply(subView, 12);
        card.addView(subView);

        if (item.requesterName != null && !item.requesterName.isEmpty()) {
            TextView reqView = new TextView(context);
            reqView.setText(item.requesterName + " (" + item.requesterRole + ")");
            reqView.setTextColor(0xff64748b);
            reqView.setTypeface(regular);
            ScreenTypography.apply(reqView, 12);
            card.addView(reqView);
        }

        // Summary snippet
        if (item.summary != null && !item.summary.isEmpty()) {
            addSpacing(card, 6);
            TextView sumView = new TextView(context);
            sumView.setText(item.summary);
            sumView.setTextColor(0xff082d50);
            sumView.setTypeface(regular);
            ScreenTypography.apply(sumView, 13);
            card.addView(sumView);
        }

        card.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onOpenRequest(item);
            }
        });

        return card;
    }

    private TextView createStatusBadge(String status) {
        Typeface bold = UiTheme.boldFont(context);
        TextView badge = new TextView(context);
        badge.setText(status);
        badge.setTypeface(bold);
        ScreenTypography.apply(badge, 11);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        badge.setPadding(px, py, px, py);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(UiTheme.dp(context, 4));

        if ("In review".equalsIgnoreCase(status)) {
            bg.setColor(Color.parseColor("#FEF3C7"));
            badge.setTextColor(Color.parseColor("#D97706"));
        } else if ("Completed".equalsIgnoreCase(status)) {
            bg.setColor(Color.parseColor("#DCFCE7"));
            badge.setTextColor(Color.parseColor("#16A34A"));
        } else if ("For correction".equalsIgnoreCase(status)) {
            bg.setColor(Color.parseColor("#FEE2E2"));
            badge.setTextColor(Color.parseColor("#DC2626"));
        } else {
            bg.setColor(Color.parseColor("#F1F5F9"));
            badge.setTextColor(Color.parseColor("#64748B"));
        }

        badge.setBackground(bg);
        return badge;
    }

    private void addSpacing(int dp) {
        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, dp)));
        contentLayout.addView(spacer);
    }

    private static void addSpacing(LinearLayout parent, int dp) {
        View space = new View(parent.getContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(parent.getContext(), dp)));
        parent.addView(space);
    }
}
