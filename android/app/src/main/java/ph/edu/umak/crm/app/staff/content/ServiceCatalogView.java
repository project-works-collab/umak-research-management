package ph.edu.umak.crm.app.staff.content;

import android.content.Context;
import android.graphics.Typeface;
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

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.staff.StaffData;

public final class ServiceCatalogView {

    public interface Listener {
        void onBackToManage();
        void onNewService();
        void onEditService(StaffData.ServiceCatalogItem item);
        void onWithdrawService(StaffData.ServiceCatalogItem item);
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;
    private final List<StaffData.ServiceCatalogItem> allServices;
    private final List<StaffData.ServiceCatalogItem> filteredServices = new ArrayList<>();

    private final EditText searchInput;
    private final LinearLayout chipContainer;
    private final TextView countText;
    private final LinearLayout cardsContainer;

    private String selectedFilter = "All";
    private String currentSearch = "";

    public ServiceCatalogView(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.allServices = StaffData.getServiceCatalog();
        this.filteredServices.addAll(allServices);

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        // Header with Back button
        LinearLayout topHeader = new LinearLayout(context);
        topHeader.setOrientation(LinearLayout.HORIZONTAL);
        topHeader.setGravity(Gravity.CENTER_VERTICAL);

        Button backBtn = UiButton.text(context, "Back", UiTheme.NAVY, () -> {
            if (listener != null) listener.onBackToManage();
        });
        topHeader.addView(backBtn);

        layout.addView(topHeader);
        addSpacing(context, layout, 4);

        // Institution Header
        TextView institution = new TextView(context);
        institution.setText("Center for Research Management");
        institution.setTextColor(UiTheme.MUTED);
        institution.setTypeface(regular);
        ScreenTypography.apply(institution, 12);
        layout.addView(institution);

        // Title Row with "New Service" button
        LinearLayout titleRow = new LinearLayout(context);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleCol = new LinearLayout(context);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView title = new TextView(context);
        title.setText("Service Catalog");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 22);
        titleCol.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Manage the services researchers can browse.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 13);
        titleCol.addView(subtitle);

        titleRow.addView(titleCol, tLp);

        Button newServiceBtn = UiButton.primary(context, "New Service", () -> {
            if (listener != null) listener.onNewService();
        });
        titleRow.addView(newServiceBtn);

        layout.addView(titleRow);

        addSpacing(context, layout, 16);

        // Search bar
        searchInput = new EditText(context);
        searchInput.setHint("Search services by name or ID");
        searchInput.setSingleLine(true);
        searchInput.setTextColor(UiTheme.NAVY);
        searchInput.setHintTextColor(UiTheme.MUTED);
        searchInput.setTypeface(regular);
        ScreenTypography.apply(searchInput, 14);
        int inpPad = UiTheme.dp(context, 12);
        searchInput.setPadding(inpPad, inpPad, inpPad, inpPad);
        searchInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s != null ? s.toString().trim() : "";
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        layout.addView(searchInput);

        addSpacing(context, layout, 12);

        // Filter chips horizontal scroll
        HorizontalScrollView hScroll = new HorizontalScrollView(context);
        hScroll.setHorizontalScrollBarEnabled(false);
        chipContainer = new LinearLayout(context);
        chipContainer.setOrientation(LinearLayout.HORIZONTAL);
        hScroll.addView(chipContainer);
        layout.addView(hScroll);

        addSpacing(context, layout, 12);

        // Count text
        countText = new TextView(context);
        countText.setTextColor(UiTheme.MUTED);
        countText.setTypeface(regular);
        ScreenTypography.apply(countText, 13);
        layout.addView(countText);

        addSpacing(context, layout, 10);

        // Cards container
        cardsContainer = new LinearLayout(context);
        cardsContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(cardsContainer);

        renderChips();
        applyFilters();
    }

    private void renderChips() {
        chipContainer.removeAllViews();
        String[] chips = {"All", "Draft", "Published", "Withdrawn"};
        for (String chip : chips) {
            final String filterName = chip;
            boolean isSelected = filterName.equalsIgnoreCase(selectedFilter);

            TextView cView = new TextView(context);
            cView.setText(chip);
            cView.setTypeface(isSelected ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
            ScreenTypography.apply(cView, 12);
            int px = UiTheme.dp(context, 12);
            int py = UiTheme.dp(context, 6);
            cView.setPadding(px, py, px, py);

            if (isSelected) {
                cView.setBackground(UiTheme.shape(context, UiTheme.NAVY, 16, 0));
                cView.setTextColor(UiTheme.WHITE);
            } else {
                cView.setBackground(UiTheme.shape(context, UiTheme.WHITE, 16, UiTheme.BORDER));
                cView.setTextColor(UiTheme.MUTED);
            }

            cView.setClickable(true);
            cView.setFocusable(true);
            cView.setOnClickListener(v -> {
                selectedFilter = filterName;
                renderChips();
                applyFilters();
            });

            chipContainer.addView(cView);
            addHorizontalSpacing(context, chipContainer, 8);
        }
    }

    private void applyFilters() {
        filteredServices.clear();
        String q = currentSearch.toLowerCase(Locale.ROOT);
        for (StaffData.ServiceCatalogItem item : allServices) {
            boolean matchesFilter = "All".equalsIgnoreCase(selectedFilter) || item.status.equalsIgnoreCase(selectedFilter);
            boolean matchesSearch = q.isEmpty() || item.id.toLowerCase(Locale.ROOT).contains(q) || item.title.toLowerCase(Locale.ROOT).contains(q);
            if (matchesFilter && matchesSearch) {
                filteredServices.add(item);
            }
        }

        countText.setText(filteredServices.size() + (filteredServices.size() == 1 ? " service" : " services"));
        renderCards();
    }

    private void renderCards() {
        cardsContainer.removeAllViews();
        int pad = UiTheme.dp(context, 14);

        for (StaffData.ServiceCatalogItem item : filteredServices) {
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
            card.setPadding(pad, pad, pad, pad);

            // Top row: ID and Status badge
            LinearLayout topRow = new LinearLayout(context);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView idText = new TextView(context);
            idText.setText(item.id);
            idText.setTextColor(UiTheme.MUTED);
            idText.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(idText, 12);
            topRow.addView(idText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView badge = new TextView(context);
            badge.setText(item.status);
            badge.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(badge, 11);
            int px = UiTheme.dp(context, 8);
            int py = UiTheme.dp(context, 3);
            badge.setPadding(px, py, px, py);

            if ("Published".equalsIgnoreCase(item.status)) {
                badge.setBackground(UiTheme.shape(context, UiTheme.STATUS_COMPLETED_BG, 4, 0));
                badge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
            } else if ("Draft".equalsIgnoreCase(item.status)) {
                badge.setBackground(UiTheme.shape(context, UiTheme.AMBER_BG, 4, UiTheme.AMBER_BORDER));
                badge.setTextColor(UiTheme.AMBER_TEXT);
            } else {
                badge.setBackground(UiTheme.shape(context, 0xFFF1F5F9, 4, 0));
                badge.setTextColor(UiTheme.MUTED);
            }
            topRow.addView(badge);
            card.addView(topRow);

            addSpacing(context, card, 4);

            // Title
            TextView tView = new TextView(context);
            tView.setText(item.title);
            tView.setTextColor(UiTheme.NAVY);
            tView.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(tView, 16);
            card.addView(tView);

            addSpacing(context, card, 2);

            // Version and updated date
            TextView vView = new TextView(context);
            vView.setText(item.version + " · " + item.updatedDate);
            vView.setTextColor(UiTheme.MUTED);
            vView.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(vView, 12);
            card.addView(vView);

            // Visibility note if draft
            if (item.visibilityNote != null && !item.visibilityNote.isEmpty()) {
                addSpacing(context, card, 4);
                TextView nView = new TextView(context);
                nView.setText(item.visibilityNote);
                nView.setTextColor(UiTheme.AMBER_TEXT);
                nView.setTypeface(UiTheme.bodyFont(context));
                ScreenTypography.apply(nView, 11);
                card.addView(nView);
            }

            addSpacing(context, card, 10);

            // Action buttons row: Edit, and Withdraw if SVC-02
            LinearLayout btnRow = new LinearLayout(context);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setGravity(Gravity.END);

            if ("SVC-02".equalsIgnoreCase(item.id) && "Published".equalsIgnoreCase(item.status)) {
                Button withdrawBtn = UiButton.outline(context, "Withdraw", UiTheme.BORDER, UiTheme.ERROR_RED, () -> {
                    if (listener != null) listener.onWithdrawService(item);
                });
                btnRow.addView(withdrawBtn);
                addHorizontalSpacing(context, btnRow, 8);
            }

            Button editBtn = UiButton.outline(context, "Edit", UiTheme.BORDER, UiTheme.NAVY, () -> {
                if (listener != null) listener.onEditService(item);
            });
            btnRow.addView(editBtn);

            card.addView(btnRow);

            cardsContainer.addView(card);
            addSpacing(context, cardsContainer, 10);
        }
    }

    public void setPreviewState(String state) {
        if ("withdrawn_filter".equalsIgnoreCase(state)) {
            selectedFilter = "Withdrawn";
        } else if ("draft_filter".equalsIgnoreCase(state)) {
            selectedFilter = "Draft";
        } else if ("published_filter".equalsIgnoreCase(state)) {
            selectedFilter = "Published";
        } else {
            selectedFilter = "All";
        }
        renderChips();
        applyFilters();
    }

    private static void addSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(space);
    }

    private static void addHorizontalSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, dp), ViewGroup.LayoutParams.MATCH_PARENT));
        parent.addView(space);
    }

    public View getContentLayout() {
        if (layout.getParent() instanceof ViewGroup) {
            ((ViewGroup) layout.getParent()).removeView(layout);
        }
        return layout;
    }
}
