package ph.edu.umak.crm.app.requests;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class MyRequestsView {

    public interface Listener {
        void onRequestSelected(RequestData.RequestItem item);
        void onSubmitNewRequest();
    }

    public record Filters(String query, String status, String fromDate, String toDate) {}

    private static final DateTimeFormatter SUBMITTED_DATE =
            DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.ENGLISH);

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;

    private final EditText searchInput;
    private final LinearLayout activeFiltersContainer;
    private final TextView counterText;
    private final LinearLayout listContainer;
    private final LinearLayout emptyContainer;

    private final List<RequestData.RequestItem> allRequests = new ArrayList<>();
    private String currentStatusFilter = "All";
    private String currentFromDate = "";
    private String currentToDate = "";
    private String currentSearchQuery = "";
    private boolean isAccountEmptyPreview = false;
    private String loadMessage;
    private String currentScope = "All"; // "Active", "History", "All"
    private Button activeTabBtn;
    private Button historyTabBtn;
    private Button allTabBtn;

    public MyRequestsView(Context context, Listener listener, boolean preview) {
        this.context = context;
        this.listener = listener;

        if (preview) allRequests.addAll(RequestData.getInitialRequests());

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, pad);

        // Segmented Scope Bar: Active | History (Completed only) | All
        LinearLayout scopeRow = new LinearLayout(context);
        scopeRow.setOrientation(LinearLayout.HORIZONTAL);
        scopeRow.setBackground(UiTheme.shape(context, 0xffe2e8f0, 8, 0));
        int sPad = UiTheme.dp(context, 3);
        scopeRow.setPadding(sPad, sPad, sPad, sPad);
        LinearLayout.LayoutParams scopeLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 48));
        scopeLp.bottomMargin = UiTheme.dp(context, 10);

        activeTabBtn = createScopeTabButton("Active Requests", "Active", () -> setScope("Active"));
        historyTabBtn = createScopeTabButton("Request History", "History", () -> setScope("History"));
        allTabBtn = createScopeTabButton("All", "All", () -> setScope("All"));

        scopeRow.addView(activeTabBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        scopeRow.addView(historyTabBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.1f));
        scopeRow.addView(allTabBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.7f));
        layout.addView(scopeRow, scopeLp);
        updateScopeButtons();

        addSpacing(context, layout, 4);

        // Search & Filter Row
        LinearLayout searchRow = new LinearLayout(context);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);

        // Search Input Box with Search Icon and Clear button
        LinearLayout searchBox = new LinearLayout(context);
        searchBox.setOrientation(LinearLayout.HORIZONTAL);
        searchBox.setGravity(Gravity.CENTER_VERTICAL);
        searchBox.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        searchBox.setPadding(UiTheme.dp(context, 12), 0, UiTheme.dp(context, 8), 0);
        searchBox.setMinimumHeight(UiTheme.dp(context, 46));

        ImageView searchIcon = new ImageView(context);
        searchIcon.setImageResource(R.drawable.ic_researcher_search);
        searchIcon.setColorFilter(0xff586b83);
        int icon18 = UiTheme.dp(context, 18);
        searchBox.addView(searchIcon, new LinearLayout.LayoutParams(icon18, icon18));

        searchInput = new EditText(context);
        searchInput.setHint("Search by request ID or service");
        searchInput.setHintTextColor(0xff8898aa);
        searchInput.setTextColor(0xff082d50);
        searchInput.setTypeface(regular);
        ScreenTypography.apply(searchInput, 14);
        searchInput.setBackgroundColor(0);
        searchInput.setSingleLine(true);
        searchInput.setPadding(UiTheme.dp(context, 8), 0, UiTheme.dp(context, 8), 0);
        searchBox.addView(searchInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView clearSearchBtn = new TextView(context);
        clearSearchBtn.setText("×");
        clearSearchBtn.setTextColor(0xff586b83);
        clearSearchBtn.setTypeface(bold);
        clearSearchBtn.setTextSize(18);
        clearSearchBtn.setPadding(UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6), 0);
        clearSearchBtn.setVisibility(View.GONE);
        clearSearchBtn.setOnClickListener(v -> searchInput.setText(""));
        searchBox.addView(clearSearchBtn);

        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        searchLp.rightMargin = UiTheme.dp(context, 8);
        searchRow.addView(searchBox, searchLp);

        Button filterBtn = new Button(context);
        filterBtn.setText("Filter");
        filterBtn.setTextColor(0xff082d50);
        filterBtn.setTypeface(bold);
        ScreenTypography.apply(filterBtn, 14);
        filterBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1));
        filterBtn.setPadding(UiTheme.dp(context, 16), 0, UiTheme.dp(context, 16), 0);
        filterBtn.setHeight(UiTheme.dp(context, 46));
        filterBtn.setOnClickListener(v -> openFilterDialog());
        searchRow.addView(filterBtn);

        layout.addView(searchRow);

        addSpacing(context, layout, 8);

        // Active filters bar (for 3.6b)
        activeFiltersContainer = new LinearLayout(context);
        activeFiltersContainer.setOrientation(LinearLayout.VERTICAL);
        activeFiltersContainer.setGravity(Gravity.START);
        activeFiltersContainer.setVisibility(View.GONE);
        layout.addView(activeFiltersContainer);

        addSpacing(context, layout, 8);

        // Request Counter (e.g. "4 requests")
        counterText = new TextView(context);
        counterText.setTextColor(0xff586b83);
        counterText.setTypeface(bold);
        ScreenTypography.apply(counterText, 13);
        layout.addView(counterText);

        addSpacing(context, layout, 12);

        // List Container for cards
        listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(listContainer);

        // Empty state container
        emptyContainer = new LinearLayout(context);
        emptyContainer.setOrientation(LinearLayout.VERTICAL);
        emptyContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyContainer.setVisibility(View.GONE);
        layout.addView(emptyContainer);

        // Search text watcher
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim() : "";
                clearSearchBtn.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        renderList();
    }

    private Button createScopeTabButton(String label, String scopeKey, Runnable onClick) {
        Button btn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        btn.setText(label);
        btn.setContentDescription(label);
        btn.setAllCaps(false);
        ScreenTypography.apply(btn, 13);
        btn.setOnClickListener(v -> onClick.run());
        return btn;
    }

    private void updateScopeButtons() {
        styleScopeBtn(activeTabBtn, "Active".equalsIgnoreCase(currentScope));
        styleScopeBtn(historyTabBtn, "History".equalsIgnoreCase(currentScope));
        styleScopeBtn(allTabBtn, "All".equalsIgnoreCase(currentScope));
    }

    private void styleScopeBtn(Button btn, boolean selected) {
        if (btn == null) return;
        btn.setTypeface(selected ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        btn.setTextColor(selected ? 0xff082d50 : 0xff586b83);
        btn.setBackground(selected ? UiTheme.shape(context, UiTheme.WHITE, 6, 0) : null);
    }

    public void setScope(String scope) {
        this.currentScope = scope;
        updateScopeButtons();
        applyFilters();
    }

    public Filters filters() {
        return new Filters(searchInput.getText().toString(), currentStatusFilter,
                currentFromDate, currentToDate);
    }

    public void restoreFilters(Filters filters) {
        currentStatusFilter = filters.status();
        currentFromDate = filters.fromDate();
        currentToDate = filters.toDate();
        if ("Completed".equalsIgnoreCase(currentStatusFilter)) {
            currentScope = "History";
        }
        updateScopeButtons();
        searchInput.setText(filters.query());
    }

    public void showRequests(List<RequestData.RequestItem> requests) {
        loadMessage = null;
        allRequests.clear();
        allRequests.addAll(requests);
        applyFilters();
    }

    public void addPreviewRequests(List<RequestData.RequestItem> requests) {
        allRequests.addAll(0, requests);
        applyFilters();
    }

    public void showLoadMessage(String message) {
        loadMessage = message;
        renderList();
    }

    private void openFilterDialog() {
        RequestFilterDialog dialog = new RequestFilterDialog(context, new RequestFilterDialog.FilterListener() {
            @Override
            public void onApplyFilters(String status, String fromDate, String toDate) {
                currentStatusFilter = status;
                currentFromDate = fromDate;
                currentToDate = toDate;
                if ("Completed".equalsIgnoreCase(status)) {
                    currentScope = "History";
                    updateScopeButtons();
                }
                applyFilters();
            }

            @Override
            public void onClearFilters() {
                clearAllFilters();
            }
        });
        dialog.show(currentStatusFilter, currentFromDate, currentToDate);
    }

    public void clearAllFilters() {
        currentScope = "All";
        updateScopeButtons();
        currentStatusFilter = "All";
        currentFromDate = "";
        currentToDate = "";
        currentSearchQuery = "";
        isAccountEmptyPreview = false;
        searchInput.setText("");
        applyFilters();
    }

    private void applyFilters() {
        activeFiltersContainer.removeAllViews();
        boolean hasFilter = false;

        LinearLayout chipsRow = new LinearLayout(context);
        chipsRow.setOrientation(LinearLayout.HORIZONTAL);
        chipsRow.setGravity(Gravity.CENTER_VERTICAL);

        if (!"All".equalsIgnoreCase(currentStatusFilter)) {
            hasFilter = true;
            View chip = createFilterChip("Status: " + currentStatusFilter, () -> {
                currentStatusFilter = "All";
                applyFilters();
            });
            chipsRow.addView(chip);
        }

        if (!currentFromDate.isEmpty() || !currentToDate.isEmpty()) {
            hasFilter = true;
            String dateLabel = (currentFromDate.isEmpty() ? "Any" : currentFromDate) + " - " + (currentToDate.isEmpty() ? "Any" : currentToDate);
            View chip = createFilterChip(dateLabel, () -> {
                currentFromDate = "";
                currentToDate = "";
                applyFilters();
            });
            chipsRow.addView(chip);
        }

        if (hasFilter) {
            Button clearBtn = new Button(context);
            clearBtn.setText("Clear filters");
            clearBtn.setTextColor(0xff185d9d);
            clearBtn.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(clearBtn, 12);
            clearBtn.setBackgroundColor(0);
            clearBtn.setPadding(UiTheme.dp(context, 8), 0, UiTheme.dp(context, 8), 0);
            clearBtn.setOnClickListener(v -> clearAllFilters());
            chipsRow.addView(clearBtn);

            activeFiltersContainer.addView(chipsRow);
            activeFiltersContainer.setVisibility(View.VISIBLE);
        } else {
            activeFiltersContainer.setVisibility(View.GONE);
        }

        renderList();
    }

    private View createFilterChip(String label, Runnable onRemove) {
        LinearLayout chip = new LinearLayout(context);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setBackground(UiTheme.shape(context, 0xffe5edf7, 12, 0));
        int px = UiTheme.dp(context, 10);
        int py = UiTheme.dp(context, 4);
        chip.setPadding(px, py, px, py);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = UiTheme.dp(context, 8);
        chip.setLayoutParams(lp);

        TextView text = new TextView(context);
        text.setText(label);
        text.setTextColor(0xff185d9d);
        text.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(text, 12);
        chip.addView(text);

        TextView close = new TextView(context);
        close.setText(" ×");
        close.setTextColor(0xff185d9d);
        close.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(close, 14);
        close.setPadding(UiTheme.dp(context, 2), 0, 0, 0);
        chip.addView(close);

        chip.setOnClickListener(v -> onRemove.run());
        return chip;
    }

    private void renderList() {
        listContainer.removeAllViews();
        emptyContainer.removeAllViews();

        if (loadMessage != null) {
            counterText.setText(loadMessage);
            listContainer.setVisibility(View.GONE);
            emptyContainer.setVisibility(View.GONE);
            return;
        }

        if (isAccountEmptyPreview || allRequests.isEmpty()) {
            counterText.setText("0 requests");
            renderEmptyAccount();
            listContainer.setVisibility(View.GONE);
            emptyContainer.setVisibility(View.VISIBLE);
            return;
        }

        List<RequestData.RequestItem> filtered = new ArrayList<>();
        String query = currentSearchQuery.toLowerCase(Locale.ROOT);
        for (RequestData.RequestItem item : allRequests) {
            boolean isCompleted = "Completed".equalsIgnoreCase(item.status);
            if ("History".equalsIgnoreCase(currentScope) && !isCompleted) {
                continue;
            }
            if ("Active".equalsIgnoreCase(currentScope) && isCompleted) {
                continue;
            }
            boolean matchesStatus = "All".equalsIgnoreCase(currentStatusFilter) || item.status.equalsIgnoreCase(currentStatusFilter);
            boolean matchesSearch = query.isEmpty()
                    || item.id.toLowerCase(Locale.ROOT).contains(query)
                    || item.service.toLowerCase(Locale.ROOT).contains(query);
            if (matchesStatus && matchesSearch && matchesDate(item.submittedDate)) {
                filtered.add(item);
            }
        }

        String countSuffix = "History".equalsIgnoreCase(currentScope)
                ? (filtered.size() == 1 ? " completed request in history" : " completed requests in history")
                : (filtered.size() == 1 ? " request" : " requests");
        counterText.setText(filtered.size() + countSuffix);

        if (filtered.isEmpty()) {
            renderEmptySearch();
            listContainer.setVisibility(View.GONE);
            emptyContainer.setVisibility(View.VISIBLE);
        } else {
            listContainer.setVisibility(View.VISIBLE);
            emptyContainer.setVisibility(View.GONE);
            for (RequestData.RequestItem item : filtered) {
                View card = createRequestCard(item);
                listContainer.addView(card);
                addSpacing(context, listContainer, 12);
            }
        }
    }

    private boolean matchesDate(String submitted) {
        if (currentFromDate.isEmpty() && currentToDate.isEmpty()) return true;
        try {
            int firstComma = submitted.indexOf(',');
            int timeSeparator = firstComma < 0 ? -1 : submitted.indexOf(',', firstComma + 1);
            String date = timeSeparator < 0 ? submitted : submitted.substring(0, timeSeparator);
            String day = LocalDate.parse(date, SUBMITTED_DATE).toString();
            return (currentFromDate.isEmpty() || day.compareTo(currentFromDate) >= 0)
                    && (currentToDate.isEmpty() || day.compareTo(currentToDate) <= 0);
        } catch (DateTimeParseException invalid) {
            return false;
        }
    }

    private View createRequestCard(RequestData.RequestItem item) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 14);
        card.setPadding(pad, pad, pad, pad);

        // Top row: ID and Status badge
        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView idText = new TextView(context);
        idText.setText(item.id);
        idText.setTextColor(0xff586b83);
        idText.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(idText, 13);
        LinearLayout.LayoutParams idLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        topRow.addView(idText, idLp);

        TextView badge = createStatusBadge(context, item.status);
        topRow.addView(badge);
        card.addView(topRow);

        addSpacing(context, card, 6);

        // Service Title
        TextView serviceTitle = new TextView(context);
        serviceTitle.setText(item.service);
        serviceTitle.setTextColor(0xff082d50);
        serviceTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(serviceTitle, 16);
        card.addView(serviceTitle);

        addSpacing(context, card, 4);

        // Submitted Date
        TextView dateText = new TextView(context);
        dateText.setText("Submitted " + item.submittedDate);
        dateText.setTextColor(0xff586b83);
        dateText.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(dateText, 13);
        card.addView(dateText);

        card.setFocusable(true);
        card.setOnClickListener(v -> {
            if (listener != null) listener.onRequestSelected(item);
        });

        return card;
    }

    private static TextView createStatusBadge(Context context, String status) {
        TextView badge = new TextView(context);
        badge.setText(status);
        badge.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(badge, 12);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        badge.setPadding(px, py, px, py);

        int bg;
        int fg;
        switch (status) {
            case "In review":
                bg = 0xffede9fe;
                fg = 0xff4338ca;
                break;
            case "For correction":
                bg = 0xfffef3c7;
                fg = 0xffb45309;
                break;
            case "Completed":
                bg = 0xffdcfce7;
                fg = 0xff16a34a;
                break;
            case "Submitted":
            default:
                bg = 0xffe5edf7;
                fg = 0xff185d9d;
                break;
        }

        badge.setBackground(UiTheme.shape(context, bg, 4, 0));
        badge.setTextColor(fg);
        return badge;
    }

    private void renderEmptyAccount() {
        emptyContainer.setPadding(0, UiTheme.dp(context, 32), 0, UiTheme.dp(context, 32));

        TextView emptyTitle = new TextView(context);
        emptyTitle.setText("No requests yet");
        emptyTitle.setTextColor(0xff082d50);
        emptyTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(emptyTitle, 18);
        emptyTitle.setGravity(Gravity.CENTER);
        emptyContainer.addView(emptyTitle);

        addSpacing(context, emptyContainer, 6);

        TextView emptySubtitle = new TextView(context);
        emptySubtitle.setText("Start a request to track clearance, ethics, and consultation requests.");
        emptySubtitle.setTextColor(0xff586b83);
        emptySubtitle.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(emptySubtitle, 14);
        emptySubtitle.setGravity(Gravity.CENTER);
        emptyContainer.addView(emptySubtitle);

        addSpacing(context, emptyContainer, 16);

        Button submitBtn = new Button(context);
        submitBtn.setText("Submit request");
        submitBtn.setTextColor(0xffffffff);
        submitBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(submitBtn, 15);
        submitBtn.setBackground(UiTheme.shape(context, 0xff131c4b, 8, 0));
        submitBtn.setOnClickListener(v -> {
            if (listener != null) listener.onSubmitNewRequest();
        });
        emptyContainer.addView(submitBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));
    }

    private void renderEmptySearch() {
        emptyContainer.setPadding(0, UiTheme.dp(context, 32), 0, UiTheme.dp(context, 32));

        TextView emptyTitle = new TextView(context);
        emptyTitle.setTextColor(0xff082d50);
        emptyTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(emptyTitle, 18);
        emptyTitle.setGravity(Gravity.CENTER);

        TextView emptySubtitle = new TextView(context);
        emptySubtitle.setTextColor(0xff586b83);
        emptySubtitle.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(emptySubtitle, 14);
        emptySubtitle.setGravity(Gravity.CENTER);

        if ("History".equalsIgnoreCase(currentScope)) {
            emptyTitle.setText("No completed requests in history");
            emptySubtitle.setText("Only past completed requests are recorded here.");
        } else {
            emptyTitle.setText("No matching requests");
            emptySubtitle.setText("Try adjusting your search terms or filters.");
        }

        emptyContainer.addView(emptyTitle);
        addSpacing(context, emptyContainer, 6);
        emptyContainer.addView(emptySubtitle);
        addSpacing(context, emptyContainer, 16);

        Button clearBtn = new Button(context);
        clearBtn.setText("Clear filters");
        clearBtn.setTextColor(0xff082d50);
        clearBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(clearBtn, 14);
        clearBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1));
        clearBtn.setOnClickListener(v -> clearAllFilters());
        emptyContainer.addView(clearBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));
    }

    public void setPreviewState(String state) {
        switch (state) {
            case "3.6b_filtered":
                isAccountEmptyPreview = false;
                currentStatusFilter = "In review";
                currentFromDate = "2026-10-01";
                currentToDate = "2026-10-31";
                applyFilters();
                break;
            case "3.6c_empty_account":
                isAccountEmptyPreview = true;
                applyFilters();
                break;
            case "3.6d_empty_search":
                isAccountEmptyPreview = false;
                searchInput.setText("REQ-2025-999");
                break;
            case "3.6a_default":
            default:
                clearAllFilters();
                break;
        }
    }

    private static void addSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(space);
    }

    public View getContentLayout() {
        if (layout.getParent() instanceof ViewGroup) {
            ((ViewGroup) layout.getParent()).removeView(layout);
        }
        return PageTitle.wrap(layout, "My Requests", "Track your requests with CRM.", false);
    }
}
