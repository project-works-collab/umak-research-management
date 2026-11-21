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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
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

        // + New Service Button (Dark Navy)
        Button newServiceBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        newServiceBtn.setText("+ New Service");
        newServiceBtn.setAllCaps(false);
        newServiceBtn.setTypeface(bold);
        newServiceBtn.setTextColor(0xffffffff);
        ScreenTypography.apply(newServiceBtn, 15);
        newServiceBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        newServiceBtn.setMinHeight(UiTheme.dp(context, 46));
        newServiceBtn.setOnClickListener(v -> {
            if (listener != null) listener.onNewService();
        });
        layout.addView(newServiceBtn);

        addSpacing(context, layout, 14);

        // Search Bar with search icon
        LinearLayout searchBox = new LinearLayout(context);
        searchBox.setOrientation(LinearLayout.HORIZONTAL);
        searchBox.setGravity(Gravity.CENTER_VERTICAL);
        searchBox.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        searchBox.setMinimumHeight(UiTheme.dp(context, 46));
        int sPad = UiTheme.dp(context, 12);
        searchBox.setPadding(sPad, 0, sPad, 0);

        ImageView searchIcon = new ImageView(context);
        searchIcon.setImageResource(R.drawable.ic_researcher_search);
        searchIcon.setColorFilter(0xff64748b);
        int iconSize = UiTheme.dp(context, 18);
        searchBox.addView(searchIcon, new LinearLayout.LayoutParams(iconSize, iconSize));

        searchInput = new EditText(context);
        searchInput.setHint("Search services by name or ID");
        searchInput.setHintTextColor(0xff8898aa);
        searchInput.setTextColor(0xff082d50);
        searchInput.setTypeface(regular);
        ScreenTypography.apply(searchInput, 14);
        searchInput.setBackground(null);
        searchInput.setPadding(UiTheme.dp(context, 8), sPad, UiTheme.dp(context, 8), sPad);
        searchInput.setSingleLine(true);
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        searchBox.addView(searchInput, inputLp);

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
        layout.addView(searchBox);

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
        countText.setTextColor(0xff586b83);
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
            cView.setText(isSelected ? "✓ " + chip : chip);
            cView.setTypeface(isSelected ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
            ScreenTypography.apply(cView, 12);
            int px = UiTheme.dp(context, 12);
            int py = UiTheme.dp(context, 6);
            cView.setPadding(px, py, px, py);

            if (isSelected) {
                cView.setBackground(UiTheme.shape(context, UiTheme.WHITE, 16, 0xff082d50));
                cView.setTextColor(0xff082d50);
            } else {
                cView.setBackground(UiTheme.shape(context, UiTheme.WHITE, 16, UiTheme.BORDER));
                cView.setTextColor(0xff586b83);
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
            idText.setTextColor(0xff185d9d);
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
                badge.setBackground(UiTheme.shape(context, 0xffdcfce7, 4, 0));
                badge.setTextColor(0xff16a34a);
            } else if ("Draft".equalsIgnoreCase(item.status)) {
                badge.setBackground(UiTheme.shape(context, 0xfff1f5f9, 4, 0xffcbd5e1));
                badge.setTextColor(0xff586b83);
            } else {
                badge.setBackground(UiTheme.shape(context, 0xfffef2f2, 4, 0xfffee2e2));
                badge.setTextColor(0xffdc2626);
            }
            topRow.addView(badge);
            card.addView(topRow);

            addSpacing(context, card, 4);

            // Title
            TextView tView = new TextView(context);
            tView.setText(item.title);
            tView.setTextColor(0xff082d50);
            tView.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(tView, 16);
            card.addView(tView);

            addSpacing(context, card, 2);

            // Version and updated date
            TextView vView = new TextView(context);
            vView.setText(item.version + " · " + item.updatedDate);
            vView.setTextColor(0xff586b83);
            vView.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(vView, 12);
            card.addView(vView);

            // Visibility note if draft: Eye off icon + text
            if (item.visibilityNote != null && !item.visibilityNote.isEmpty()) {
                addSpacing(context, card, 4);
                LinearLayout noteRow = new LinearLayout(context);
                noteRow.setOrientation(LinearLayout.HORIZONTAL);
                noteRow.setGravity(Gravity.CENTER_VERTICAL);

                ImageView eyeOff = new ImageView(context);
                eyeOff.setImageResource(R.drawable.ic_crm_eye_off);
                eyeOff.setColorFilter(0xff586b83);
                int eyeSize = UiTheme.dp(context, 14);
                LinearLayout.LayoutParams eyeLp = new LinearLayout.LayoutParams(eyeSize, eyeSize);
                eyeLp.rightMargin = UiTheme.dp(context, 6);
                noteRow.addView(eyeOff, eyeLp);

                TextView nView = new TextView(context);
                nView.setText(item.visibilityNote);
                nView.setTextColor(0xff586b83);
                nView.setTypeface(UiTheme.bodyFont(context));
                ScreenTypography.apply(nView, 11);
                noteRow.addView(nView);

                card.addView(noteRow);
            }

            addSpacing(context, card, 12);

            // Action buttons row: Edit, and Withdraw if SVC-02
            if ("SVC-02".equalsIgnoreCase(item.id) && "Published".equalsIgnoreCase(item.status)) {
                LinearLayout btnRow = new LinearLayout(context);
                btnRow.setOrientation(LinearLayout.HORIZONTAL);

                Button editBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
                editBtn.setText("Edit");
                editBtn.setAllCaps(false);
                editBtn.setTextColor(0xff082d50);
                editBtn.setTypeface(UiTheme.boldFont(context));
                ScreenTypography.apply(editBtn, 14);
                editBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
                editBtn.setMinHeight(UiTheme.dp(context, 40));
                editBtn.setOnClickListener(v -> {
                    if (listener != null) listener.onEditService(item);
                });
                LinearLayout.LayoutParams eLp = new LinearLayout.LayoutParams(0, UiTheme.dp(context, 40), 1f);
                eLp.rightMargin = UiTheme.dp(context, 8);
                btnRow.addView(editBtn, eLp);

                Button withdrawBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
                withdrawBtn.setText("Withdraw");
                withdrawBtn.setAllCaps(false);
                withdrawBtn.setTextColor(0xffdc2626);
                withdrawBtn.setTypeface(UiTheme.boldFont(context));
                ScreenTypography.apply(withdrawBtn, 14);
                withdrawBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xfffca5a5), 0x20dc2626));
                withdrawBtn.setMinHeight(UiTheme.dp(context, 40));
                withdrawBtn.setOnClickListener(v -> {
                    if (listener != null) listener.onWithdrawService(item);
                });
                LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(0, UiTheme.dp(context, 40), 1f);
                btnRow.addView(withdrawBtn, wLp);

                card.addView(btnRow);
            } else {
                Button editBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
                editBtn.setText("Edit");
                editBtn.setAllCaps(false);
                editBtn.setTextColor(0xff082d50);
                editBtn.setTypeface(UiTheme.boldFont(context));
                ScreenTypography.apply(editBtn, 14);
                editBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
                editBtn.setMinHeight(UiTheme.dp(context, 40));
                editBtn.setOnClickListener(v -> {
                    if (listener != null) listener.onEditService(item);
                });
                card.addView(editBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 40)));
            }

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
        return PageTitle.wrap(layout, "Service Catalog", "Manage the services researchers can browse.", false);
    }
}
