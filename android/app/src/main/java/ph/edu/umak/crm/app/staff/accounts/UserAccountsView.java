package ph.edu.umak.crm.app.staff.accounts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.staff.StaffData;

/**
 * 5.8 User Accounts Screen
 * Allows staff to manage staff and researcher access, filter accounts, search, and navigate to details.
 */
public class UserAccountsView {

    public interface Callbacks {
        void onBack();
        void onCreateStaffAccount();
        void onSelectAccount(StaffData.StaffAccountItem account);
    }

    private final Context context;
    private final Callbacks callbacks;
    private final ScrollView rootScrollView;
    private final LinearLayout contentLayout;
    private final LinearLayout accountsListLayout;
    private final TextView countView;
    private final EditText searchInput;

    private final List<StaffData.StaffAccountItem> allAccounts;
    private final Set<String> activeFilters = new HashSet<>();
    private String currentSearchQuery = "";

    public UserAccountsView(Context context, Callbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
        this.allAccounts = StaffData.getUserAccounts();

        rootScrollView = new ScrollView(context);
        rootScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        rootScrollView.setBackgroundColor(UiTheme.PAPER);

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));
        rootScrollView.addView(contentLayout);

        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        // Back button row
        if (callbacks != null) {
            TextView backBtn = new TextView(context);
            backBtn.setText("< Back to Manage");
            backBtn.setTextColor(UiTheme.ACCENT);
            backBtn.setTypeface(bold);
            ScreenTypography.apply(backBtn, 14);
            backBtn.setPadding(0, 0, 0, UiTheme.dp(context, 12));
            backBtn.setOnClickListener(v -> callbacks.onBack());
            contentLayout.addView(backBtn);
        }

        contentLayout.addView(UiButton.primary(context, "Create Staff Account", () -> {
            if (callbacks != null) callbacks.onCreateStaffAccount();
        }));
        // Separate the create action from search.
        View spacing = new View(context);
        contentLayout.addView(spacing, new LinearLayout.LayoutParams(1, UiTheme.dp(context, 8)));

        // Search bar
        searchInput = new EditText(context);
        searchInput.setHint("Search by name or institutional email");
        searchInput.setHintTextColor(UiTheme.MUTED);
        searchInput.setTextColor(UiTheme.NAVY);
        searchInput.setTypeface(regular);
        ScreenTypography.apply(searchInput, 14);
        searchInput.setPadding(UiTheme.dp(context, 14), UiTheme.dp(context, 12),
                UiTheme.dp(context, 14), UiTheme.dp(context, 12));

        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(UiTheme.WHITE);
        searchBg.setCornerRadius(UiTheme.dp(context, 8));
        searchBg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        searchInput.setBackground(searchBg);
        contentLayout.addView(searchInput);

        // Filter chips row
        HorizontalScrollView chipScroll = new HorizontalScrollView(context);
        chipScroll.setHorizontalScrollBarEnabled(false);
        chipScroll.setPadding(0, UiTheme.dp(context, 12), 0, UiTheme.dp(context, 12));

        LinearLayout chipLayout = new LinearLayout(context);
        chipLayout.setOrientation(LinearLayout.HORIZONTAL);

        String[] filters = {"Staff", "Researcher", "Active", "Inactive"};
        for (String filter : filters) {
            chipLayout.addView(createFilterChip(filter));
            View spacer = new View(context);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 8), 1));
            chipLayout.addView(spacer);
        }
        chipScroll.addView(chipLayout);
        contentLayout.addView(chipScroll);

        // Count label
        countView = new TextView(context);
        countView.setTextColor(UiTheme.MUTED);
        countView.setTypeface(bold);
        ScreenTypography.apply(countView, 13);
        countView.setPadding(0, 0, 0, UiTheme.dp(context, 10));
        contentLayout.addView(countView);

        // Accounts list container
        accountsListLayout = new LinearLayout(context);
        accountsListLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.addView(accountsListLayout);

        // Search listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim() : "";
                applyFilterAndRender();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        applyFilterAndRender();
    }

    public View getContentLayout() {
        if (contentLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) contentLayout.getParent()).removeView(contentLayout);
        }
        return PageTitle.wrap(contentLayout, "User Accounts", "Staff and researcher access.", false);
    }

    private View createFilterChip(String filterName) {
        Typeface bold = UiTheme.boldFont(context);
        TextView chip = new TextView(context);
        chip.setText(filterName);
        chip.setTypeface(bold);
        ScreenTypography.apply(chip, 12);
        chip.setPadding(UiTheme.dp(context, 14), UiTheme.dp(context, 6),
                UiTheme.dp(context, 14), UiTheme.dp(context, 6));

        updateChipState(chip, filterName);

        chip.setOnClickListener(v -> {
            if (activeFilters.contains(filterName)) {
                activeFilters.remove(filterName);
            } else {
                activeFilters.add(filterName);
            }
            updateChipState(chip, filterName);
            applyFilterAndRender();
        });

        return chip;
    }

    private void updateChipState(TextView chip, String filterName) {
        boolean selected = activeFilters.contains(filterName);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(UiTheme.dp(context, 16));

        if (selected) {
            bg.setColor(UiTheme.NAVY);
            chip.setTextColor(UiTheme.WHITE);
            bg.setStroke(0, Color.TRANSPARENT);
        } else {
            bg.setColor(UiTheme.WHITE);
            chip.setTextColor(UiTheme.NAVY);
            bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        }
        chip.setBackground(bg);
    }

    private void applyFilterAndRender() {
        accountsListLayout.removeAllViews();
        List<StaffData.StaffAccountItem> filtered = new ArrayList<>();

        String query = currentSearchQuery.toLowerCase(Locale.ROOT);

        for (StaffData.StaffAccountItem item : allAccounts) {
            // Check text match
            boolean matchesSearch = query.isEmpty()
                    || item.name.toLowerCase(Locale.ROOT).contains(query)
                    || item.email.toLowerCase(Locale.ROOT).contains(query)
                    || (item.identifier != null && item.identifier.toLowerCase(Locale.ROOT).contains(query));

            if (!matchesSearch) continue;

            // Check chip filter
            boolean matchesFilter = true;
            if (!activeFilters.isEmpty()) {
                boolean matchesRole = true;
                if (activeFilters.contains("Staff") || activeFilters.contains("Researcher")) {
                    matchesRole = (activeFilters.contains("Staff") && "CRM Staff".equalsIgnoreCase(item.role))
                            || (activeFilters.contains("Researcher") && "Researcher".equalsIgnoreCase(item.role));
                }

                boolean matchesStatus = true;
                if (activeFilters.contains("Active") || activeFilters.contains("Inactive")) {
                    matchesStatus = (activeFilters.contains("Active") && "Active".equalsIgnoreCase(item.status))
                            || (activeFilters.contains("Inactive") && "Inactive".equalsIgnoreCase(item.status));
                }

                matchesFilter = matchesRole && matchesStatus;
            }

            if (matchesFilter) {
                filtered.add(item);
            }
        }

        countView.setText(filtered.size() + " accounts");

        for (StaffData.StaffAccountItem item : filtered) {
            accountsListLayout.addView(createAccountCard(item));
            View spacer = new View(context);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 10)));
            accountsListLayout.addView(spacer);
        }
    }

    private View createAccountCard(StaffData.StaffAccountItem item) {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 12),
                UiTheme.dp(context, 12), UiTheme.dp(context, 12));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(UiTheme.dp(context, 8));
        bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        card.setBackground(bg);

        // Avatar Initials Circle
        TextView avatar = new TextView(context);
        avatar.setText(item.initials);
        avatar.setTextColor(UiTheme.NAVY);
        avatar.setTypeface(bold);
        ScreenTypography.apply(avatar, 13);
        avatar.setGravity(Gravity.CENTER);

        int avatarSize = UiTheme.dp(context, 38);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        aLp.setMarginEnd(UiTheme.dp(context, 12));
        avatar.setLayoutParams(aLp);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#E5EDF6"));
        avatar.setBackground(avatarBg);
        card.addView(avatar);

        // Name and Role column
        LinearLayout col = new LinearLayout(context);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

        TextView nameView = new TextView(context);
        nameView.setText(item.name);
        nameView.setTextColor(UiTheme.NAVY);
        nameView.setTypeface(bold);
        ScreenTypography.apply(nameView, 14);
        col.addView(nameView);

        String subtitleText = item.role;
        if (item.category != null && !item.category.isEmpty()) {
            subtitleText += " · " + item.category;
        }

        TextView subView = new TextView(context);
        subView.setText(subtitleText);
        subView.setTextColor(UiTheme.MUTED);
        subView.setTypeface(regular);
        ScreenTypography.apply(subView, 12);
        subView.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        col.addView(subView);

        card.addView(col, colLp);

        // Status Badge
        TextView statusBadge = new TextView(context);
        statusBadge.setText(item.status);
        statusBadge.setTypeface(bold);
        ScreenTypography.apply(statusBadge, 11);
        statusBadge.setPadding(UiTheme.dp(context, 8), UiTheme.dp(context, 3),
                UiTheme.dp(context, 8), UiTheme.dp(context, 3));

        GradientDrawable sBg = new GradientDrawable();
        sBg.setCornerRadius(UiTheme.dp(context, 4));
        if ("Active".equalsIgnoreCase(item.status)) {
            sBg.setColor(Color.parseColor("#E6F4EA"));
            statusBadge.setTextColor(Color.parseColor("#137333"));
        } else {
            sBg.setColor(Color.parseColor("#F1F3F4"));
            statusBadge.setTextColor(Color.parseColor("#5F6368"));
        }
        statusBadge.setBackground(sBg);
        card.addView(statusBadge);

        card.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onSelectAccount(item);
            }
        });

        return card;
    }
}
