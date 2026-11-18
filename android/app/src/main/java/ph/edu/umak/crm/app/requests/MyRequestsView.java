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
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class MyRequestsView {

    public interface Listener {
        void onRequestSelected(RequestData.RequestItem item);
        void onSubmitNewRequest();
    }

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

    public MyRequestsView(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

        allRequests.addAll(RequestData.getInitialRequests());

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, pad);

        // Institution Header
        TextView institution = new TextView(context);
        institution.setText("Center for Research Management");
        institution.setTextColor(UiTheme.MUTED);
        institution.setTypeface(regular);
        ScreenTypography.apply(institution, 12);
        layout.addView(institution);

        // Screen Title
        TextView title = new TextView(context);
        title.setText("My Requests");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 22);
        title.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));
        layout.addView(title);

        // Subtitle
        TextView subtitle = new TextView(context);
        subtitle.setText("Track your requests with CRM.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 14);
        layout.addView(subtitle);

        addSpacing(context, layout, 16);

        // Search & Filter Row
        LinearLayout searchRow = new LinearLayout(context);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);

        searchInput = new EditText(context);
        searchInput.setHint("Search by request ID or service");
        searchInput.setHintTextColor(UiTheme.MUTED);
        searchInput.setTextColor(UiTheme.NAVY);
        searchInput.setTypeface(regular);
        ScreenTypography.apply(searchInput, 14);
        searchInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int sPad = UiTheme.dp(context, 10);
        searchInput.setPadding(sPad, sPad, sPad, sPad);
        searchInput.setSingleLine(true);
        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        searchLp.rightMargin = UiTheme.dp(context, 8);
        searchRow.addView(searchInput, searchLp);

        Button filterBtn = UiButton.outline(context, "Filter", UiTheme.BORDER, UiTheme.NAVY, this::openFilterDialog);
        filterBtn.setPadding(UiTheme.dp(context, 12), 0, UiTheme.dp(context, 12), 0);
        searchRow.addView(filterBtn);

        layout.addView(searchRow);

        addSpacing(context, layout, 8);

        // Active filters bar (for 3.6b)
        activeFiltersContainer = new LinearLayout(context);
        activeFiltersContainer.setOrientation(LinearLayout.HORIZONTAL);
        activeFiltersContainer.setGravity(Gravity.CENTER_VERTICAL);
        activeFiltersContainer.setVisibility(View.GONE);
        layout.addView(activeFiltersContainer);

        addSpacing(context, layout, 8);

        // Request Counter (e.g. "4 requests")
        counterText = new TextView(context);
        counterText.setTextColor(UiTheme.MUTED);
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
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        renderList();
    }

    private void openFilterDialog() {
        RequestFilterDialog dialog = new RequestFilterDialog(context, new RequestFilterDialog.FilterListener() {
            @Override
            public void onApplyFilters(String status, String fromDate, String toDate) {
                currentStatusFilter = status;
                currentFromDate = fromDate;
                currentToDate = toDate;
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

        if (!"All".equalsIgnoreCase(currentStatusFilter)) {
            hasFilter = true;
            TextView chip = createFilterChip("Status: " + currentStatusFilter, () -> {
                currentStatusFilter = "All";
                applyFilters();
            });
            activeFiltersContainer.addView(chip);
        }

        if (!currentFromDate.isEmpty() || !currentToDate.isEmpty()) {
            hasFilter = true;
            String dateLabel = (currentFromDate.isEmpty() ? "Any" : currentFromDate) + " - " + (currentToDate.isEmpty() ? "Any" : currentToDate);
            TextView chip = createFilterChip(dateLabel, () -> {
                currentFromDate = "";
                currentToDate = "";
                applyFilters();
            });
            activeFiltersContainer.addView(chip);
        }

        if (hasFilter) {
            Button clearBtn = UiButton.text(context, "Clear filters", UiTheme.ACCENT, this::clearAllFilters);
            activeFiltersContainer.addView(clearBtn);
            activeFiltersContainer.setVisibility(View.VISIBLE);
        } else {
            activeFiltersContainer.setVisibility(View.GONE);
        }

        renderList();
    }

    private TextView createFilterChip(String label, Runnable onRemove) {
        TextView chip = new TextView(context);
        chip.setText(label + " [x]");
        chip.setTextColor(UiTheme.NAVY);
        chip.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(chip, 12);
        chip.setBackground(UiTheme.shape(context, UiTheme.TAG_BG, 12, 0));
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 4);
        chip.setPadding(px, py, px, py);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = UiTheme.dp(context, 6);
        chip.setLayoutParams(lp);
        chip.setOnClickListener(v -> onRemove.run());
        return chip;
    }

    private void renderList() {
        listContainer.removeAllViews();
        emptyContainer.removeAllViews();

        if (isAccountEmptyPreview) {
            counterText.setText("0 requests");
            renderEmptyAccount();
            listContainer.setVisibility(View.GONE);
            emptyContainer.setVisibility(View.VISIBLE);
            return;
        }

        List<RequestData.RequestItem> filtered = new ArrayList<>();
        for (RequestData.RequestItem item : allRequests) {
            boolean matchesStatus = "All".equalsIgnoreCase(currentStatusFilter) || item.status.equalsIgnoreCase(currentStatusFilter);
            boolean matchesSearch = currentSearchQuery.isEmpty()
                    || item.id.toLowerCase().contains(currentSearchQuery.toLowerCase())
                    || item.service.toLowerCase().contains(currentSearchQuery.toLowerCase());
            if (matchesStatus && matchesSearch) {
                filtered.add(item);
            }
        }

        counterText.setText(filtered.size() + (filtered.size() == 1 ? " request" : " requests"));

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
        idText.setTextColor(UiTheme.MUTED);
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
        serviceTitle.setTextColor(UiTheme.NAVY);
        serviceTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(serviceTitle, 16);
        card.addView(serviceTitle);

        addSpacing(context, card, 4);

        // Submitted Date
        TextView dateText = new TextView(context);
        dateText.setText("Submitted " + item.submittedDate);
        dateText.setTextColor(UiTheme.MUTED);
        dateText.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(dateText, 13);
        card.addView(dateText);

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
        int py = UiTheme.dp(context, 4);
        badge.setPadding(px, py, px, py);

        int bg;
        int fg;
        switch (status) {
            case "In review":
                bg = UiTheme.STATUS_IN_REVIEW_BG;
                fg = UiTheme.STATUS_IN_REVIEW_TEXT;
                break;
            case "For correction":
                bg = UiTheme.STATUS_CORRECTION_BG;
                fg = UiTheme.STATUS_CORRECTION_TEXT;
                break;
            case "Completed":
                bg = UiTheme.STATUS_COMPLETED_BG;
                fg = UiTheme.STATUS_COMPLETED_TEXT;
                break;
            case "Submitted":
            default:
                bg = UiTheme.STATUS_SUBMITTED_BG;
                fg = UiTheme.STATUS_SUBMITTED_TEXT;
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
        emptyTitle.setTextColor(UiTheme.NAVY);
        emptyTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(emptyTitle, 18);
        emptyTitle.setGravity(Gravity.CENTER);
        emptyContainer.addView(emptyTitle);

        addSpacing(context, emptyContainer, 6);

        TextView emptySubtitle = new TextView(context);
        emptySubtitle.setText("Start a request to track clearance, ethics, and consultation requests.");
        emptySubtitle.setTextColor(UiTheme.MUTED);
        emptySubtitle.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(emptySubtitle, 14);
        emptySubtitle.setGravity(Gravity.CENTER);
        emptyContainer.addView(emptySubtitle);

        addSpacing(context, emptyContainer, 16);

        Button submitBtn = UiButton.primary(context, "Submit request", () -> {
            if (listener != null) listener.onSubmitNewRequest();
        });
        emptyContainer.addView(submitBtn);
    }

    private void renderEmptySearch() {
        emptyContainer.setPadding(0, UiTheme.dp(context, 32), 0, UiTheme.dp(context, 32));

        TextView emptyTitle = new TextView(context);
        emptyTitle.setText("No matching requests");
        emptyTitle.setTextColor(UiTheme.NAVY);
        emptyTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(emptyTitle, 18);
        emptyTitle.setGravity(Gravity.CENTER);
        emptyContainer.addView(emptyTitle);

        addSpacing(context, emptyContainer, 6);

        TextView emptySubtitle = new TextView(context);
        emptySubtitle.setText("Try adjusting your search terms or filters.");
        emptySubtitle.setTextColor(UiTheme.MUTED);
        emptySubtitle.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(emptySubtitle, 14);
        emptySubtitle.setGravity(Gravity.CENTER);
        emptyContainer.addView(emptySubtitle);

        addSpacing(context, emptyContainer, 16);

        Button clearBtn = UiButton.outline(context, "Clear filters", UiTheme.BORDER, UiTheme.NAVY, this::clearAllFilters);
        emptyContainer.addView(clearBtn);
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
        return layout;
    }
}
