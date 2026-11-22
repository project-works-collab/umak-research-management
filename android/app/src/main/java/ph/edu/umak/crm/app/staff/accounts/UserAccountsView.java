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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
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

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setBackgroundColor(UiTheme.PAPER);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));

        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        // 1. + Create Staff Account Button
        Button createBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        createBtn.setText("+ Create Staff Account");
        createBtn.setContentDescription("Create Staff Account");
        createBtn.setAllCaps(false);
        createBtn.setTextColor(0xffffffff);
        createBtn.setTypeface(bold);
        ScreenTypography.apply(createBtn, 15);
        createBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        createBtn.setMinHeight(UiTheme.dp(context, 46));
        createBtn.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onCreateStaffAccount();
        });
        contentLayout.addView(createBtn);

        addSpacing(14);

        // 2. Search bar with search icon
        FrameLayout searchContainer = new FrameLayout(context);
        searchContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        searchContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 46)));

        ImageView searchIcon = new ImageView(context);
        searchIcon.setImageResource(R.drawable.ic_researcher_search);
        searchIcon.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams sIconLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 20), UiTheme.dp(context, 20));
        sIconLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        sIconLp.leftMargin = UiTheme.dp(context, 14);
        searchContainer.addView(searchIcon, sIconLp);

        searchInput = new EditText(context);
        searchInput.setHint("Search by name or institutional email");
        searchInput.setHintTextColor(0xff94a3b8);
        searchInput.setTextColor(0xff082d50);
        searchInput.setTypeface(regular);
        searchInput.setBackground(null);
        ScreenTypography.apply(searchInput, 14);
        int padL = UiTheme.dp(context, 44);
        int padR = UiTheme.dp(context, 14);
        searchInput.setPadding(padL, 0, padR, 0);
        searchInput.setSingleLine(true);
        FrameLayout.LayoutParams sInputLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        searchContainer.addView(searchInput, sInputLp);

        contentLayout.addView(searchContainer);

        addSpacing(12);

        // 3. Filter chips row (horizontal)
        HorizontalScrollView chipScroll = new HorizontalScrollView(context);
        chipScroll.setHorizontalScrollBarEnabled(false);

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

        addSpacing(12);

        // 4. Count label (e.g. "6 accounts")
        countView = new TextView(context);
        countView.setTextColor(0xff586b83);
        countView.setTypeface(regular);
        ScreenTypography.apply(countView, 13);
        countView.setPadding(0, 0, 0, UiTheme.dp(context, 8));
        contentLayout.addView(countView);

        // 5. Accounts list container
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

    public void bindLive(java.util.List<StaffData.StaffAccountItem> accounts){allAccounts.clear();allAccounts.addAll(accounts);applyFilterAndRender();}
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
        heading.setText("User Accounts");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        TextView copy = new TextView(context);
        copy.setText("Staff and researcher access.");
        copy.setTextColor(UiTheme.PALE);
        copy.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(copy, 13);
        band.addView(copy);

        root.addView(band);
        root.addView(contentLayout);
        return root;
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
            bg.setColor(0xff082d50);
            chip.setTextColor(0xffffffff);
            bg.setStroke(0, Color.TRANSPARENT);
        } else {
            bg.setColor(UiTheme.WHITE);
            chip.setTextColor(0xff082d50);
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
                    LinearLayout.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 8)));
            accountsListLayout.addView(spacer);
        }
    }

    private View createAccountCard(StaffData.StaffAccountItem item) {
        Typeface bold = UiTheme.boldFont(context);

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
        avatar.setTextColor(0xff185d9d);
        avatar.setTypeface(bold);
        ScreenTypography.apply(avatar, 13);
        avatar.setGravity(Gravity.CENTER);

        int avatarSize = UiTheme.dp(context, 38);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        aLp.setMarginEnd(UiTheme.dp(context, 12));
        avatar.setLayoutParams(aLp);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#E0F2FE"));
        avatar.setBackground(avatarBg);
        card.addView(avatar);

        // Middle: Name and Badges row
        LinearLayout col = new LinearLayout(context);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

        // Name
        TextView nameView = new TextView(context);
        nameView.setText(item.name);
        nameView.setTextColor(0xff082d50);
        nameView.setTypeface(bold);
        ScreenTypography.apply(nameView, 14);
        col.addView(nameView);

        // Badges row
        LinearLayout badgesRow = new LinearLayout(context);
        badgesRow.setOrientation(LinearLayout.HORIZONTAL);
        badgesRow.setGravity(Gravity.CENTER_VERTICAL);
        badgesRow.setPadding(0, UiTheme.dp(context, 4), 0, 0);

        // Role / Category Badge
        String roleText = "CRM Staff".equalsIgnoreCase(item.role)
                ? "CRM Staff"
                : (item.category != null ? "Researcher · " + item.category : "Researcher");

        TextView roleBadge = new TextView(context);
        roleBadge.setText(roleText);
        roleBadge.setTypeface(bold);
        ScreenTypography.apply(roleBadge, 11);
        int px = UiTheme.dp(context, 7);
        int py = UiTheme.dp(context, 2);
        roleBadge.setPadding(px, py, px, py);
        roleBadge.setBackground(UiTheme.shape(context, 0xffe0f2fe, 4, 0));
        roleBadge.setTextColor(0xff0284c7);
        badgesRow.addView(roleBadge);

        // Status Badge
        TextView statusBadge = new TextView(context);
        statusBadge.setText(item.status);
        statusBadge.setTypeface(bold);
        ScreenTypography.apply(statusBadge, 11);
        statusBadge.setPadding(px, py, px, py);

        LinearLayout.LayoutParams statLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statLp.leftMargin = UiTheme.dp(context, 6);
        statusBadge.setLayoutParams(statLp);

        GradientDrawable sBg = new GradientDrawable();
        sBg.setCornerRadius(UiTheme.dp(context, 4));
        if ("Active".equalsIgnoreCase(item.status)) {
            sBg.setColor(Color.parseColor("#DCFCE7"));
            statusBadge.setTextColor(Color.parseColor("#16A34A"));
        } else {
            sBg.setColor(Color.parseColor("#F1F5F9"));
            statusBadge.setTextColor(Color.parseColor("#64748B"));
        }
        statusBadge.setBackground(sBg);
        badgesRow.addView(statusBadge);

        col.addView(badgesRow);
        card.addView(col, colLp);

        // Right: Chevron right >
        ImageView chevron = new ImageView(context);
        chevron.setImageResource(R.drawable.ic_crm_chevron_right);
        chevron.setColorFilter(0xff94a3b8);
        int cSize = UiTheme.dp(context, 18);
        card.addView(chevron, new LinearLayout.LayoutParams(cSize, cSize));

        card.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onSelectAccount(item);
            }
        });

        return card;
    }

    private void addSpacing(int dp) {
        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, dp)));
        contentLayout.addView(spacer);
    }
}
