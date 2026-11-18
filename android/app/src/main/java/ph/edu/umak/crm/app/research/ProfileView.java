package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class ProfileView {

    public interface Listener {
        void onEditProfile();
        void onOpenStudyProgress(String studyState);
        void onSignOut();
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;
    private final ResearchData.UserProfile userProfile;

    public ProfileView(Context context, ResearchData.UserProfile profile, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.userProfile = profile != null ? profile : ResearchData.getDefaultProfile();

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        addSpacing(context, layout, 12);

        // Profile Header Card
        LinearLayout profileCard = new LinearLayout(context);
        profileCard.setOrientation(LinearLayout.VERTICAL);
        profileCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 12, UiTheme.BORDER));
        profileCard.setPadding(pad, pad, pad, pad);

        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        FrameLayout identity = new FrameLayout(context);
        View strip = new View(context);
        strip.setBackground(UiTheme.shape(context, UiTheme.BLUE, 8, 0));
        identity.addView(strip, new FrameLayout.LayoutParams(-1, UiTheme.dp(context, 54)));
        FrameLayout.LayoutParams identityRow = new FrameLayout.LayoutParams(-1, -2);
        identityRow.topMargin = UiTheme.dp(context, 30);
        identityRow.setMarginStart(UiTheme.dp(context, 8));
        identity.addView(topRow, identityRow);

        // Avatar circle
        TextView avatar = new TextView(context);
        StringBuilder initials = new StringBuilder();
        String name = userProfile.name == null ? "" : userProfile.name.trim();
        for (String word : name.split("\\s+")) {
            if (!word.isEmpty() && initials.length() < 2) initials.appendCodePoint(word.codePointAt(0));
        }
        avatar.setText(initials.length() == 0 ? "?" : initials.toString().toUpperCase(Locale.ROOT));
        avatar.setTextColor(UiTheme.NAVY);
        avatar.setTypeface(bold);
        avatar.setGravity(Gravity.CENTER);
        ScreenTypography.apply(avatar, 18);
        int aSize = UiTheme.dp(context, 48);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(aSize, aSize);
        aLp.rightMargin = UiTheme.dp(context, 12);
        avatar.setLayoutParams(aLp);
        avatar.setBackground(UiTheme.shape(context, UiTheme.YELLOW, 24, 0));
        topRow.addView(avatar);

        LinearLayout nameCol = new LinearLayout(context);
        nameCol.setOrientation(LinearLayout.VERTICAL);

        TextView nameText = new TextView(context);
        nameText.setText(userProfile.name);
        nameText.setTextColor(UiTheme.NAVY);
        nameText.setTypeface(bold);
        ScreenTypography.apply(nameText, 18);
        nameCol.addView(nameText);

        TextView roleText = new TextView(context);
        roleText.setText(userProfile.role);
        roleText.setTextColor(UiTheme.MUTED);
        roleText.setTypeface(regular);
        ScreenTypography.apply(roleText, 13);
        nameCol.addView(roleText);

        profileCard.addView(identity);
        addSpacing(context, profileCard, 4);
        profileCard.addView(nameCol);

        addSpacing(context, profileCard, 12);

        // Details grid / list
        if (!userProfile.program.isEmpty()) addDetailRow(profileCard, "Program", userProfile.program);
        if (!userProfile.yearLevel.isEmpty()) addDetailRow(profileCard, "Year level", userProfile.yearLevel);
        addDetailRow(profileCard, "Researcher category", userProfile.category);
        addDetailRow(profileCard, "College", userProfile.college.isEmpty() ? "—" : userProfile.college);
        addDetailRow(profileCard, "Institutional email", userProfile.email);
        addDetailRow(profileCard, "Account identifier", userProfile.accountId);

        addSpacing(context, profileCard, 12);

        Button editProfileBtn = UiButton.outline(context, "Edit Profile", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (listener != null) listener.onEditProfile();
        });
        profileCard.addView(editProfileBtn);

        layout.addView(profileCard);

        addSpacing(context, layout, 16);

        // Stats Grid (2 rows x 2 cols)
        LinearLayout statsRow1 = new LinearLayout(context);
        statsRow1.setOrientation(LinearLayout.HORIZONTAL);

        statsRow1.addView(createStatCard("2", "Total Studies", "Registered with CRM"),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        addHorizontalSpacing(context, statsRow1, 10);
        statsRow1.addView(createStatCard("1", "Active Studies", "In progress"),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        layout.addView(statsRow1);

        addSpacing(context, layout, 10);

        LinearLayout statsRow2 = new LinearLayout(context);
        statsRow2.setOrientation(LinearLayout.HORIZONTAL);

        statsRow2.addView(createStatCard("4", "Service Requests", "1 for correction"),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        addHorizontalSpacing(context, statsRow2, 10);
        statsRow2.addView(createStatCard("1", "Publications", "UNIVERSITY"),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        layout.addView(statsRow2);

        addSpacing(context, layout, 20);

        // My Research Studies Section
        TextView sectionTitle = new TextView(context);
        sectionTitle.setText("My Research Studies");
        sectionTitle.setTextColor(UiTheme.NAVY);
        sectionTitle.setTypeface(bold);
        ScreenTypography.apply(sectionTitle, 16);
        layout.addView(sectionTitle);

        addSpacing(context, layout, 10);

        // Study 1: REG-2026-089 (In progress)
        View studyCard1 = createStudyCard("REG-2026-089", "Impact of AI on Student Engagement", "In progress",
                () -> {
                    if (listener != null) listener.onOpenStudyProgress("4.6c");
                });
        layout.addView(studyCard1);

        addSpacing(context, layout, 10);

        // Study 2: REG-2025-041 (Completed)
        View studyCard2 = createStudyCard("REG-2025-041", "Gamified Learning Modules for Introductory Programming", "Completed",
                () -> {
                    if (listener != null) listener.onOpenStudyProgress("4.6a");
                });
        layout.addView(studyCard2);

        addSpacing(context, layout, 20);

        // Account Settings Card
        LinearLayout settingsCard = new LinearLayout(context);
        settingsCard.setOrientation(LinearLayout.VERTICAL);
        settingsCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        settingsCard.setPadding(pad, pad, pad, pad);

        TextView setHeader = new TextView(context);
        setHeader.setText("Account Settings");
        setHeader.setTextColor(UiTheme.NAVY);
        setHeader.setTypeface(bold);
        ScreenTypography.apply(setHeader, 15);
        settingsCard.addView(setHeader);

        addSpacing(context, settingsCard, 10);

        addDetailRow(settingsCard, "Email address", userProfile.email);
        addDetailRow(settingsCard, "Account status", userProfile.accountStatus);
        addDetailRow(settingsCard, "Last login", userProfile.lastLogin);

        addSpacing(context, settingsCard, 14);

        Button signOutBtn = UiButton.outline(context, "Sign Out", UiTheme.BORDER, UiTheme.ERROR_RED, () -> {
            if (listener != null) listener.onSignOut();
        });
        settingsCard.addView(signOutBtn);

        layout.addView(settingsCard);
    }

    private void addDetailRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));

        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(UiTheme.MUTED);
        lbl.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(lbl, 13);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(lbl, lp);

        TextView val = new TextView(context);
        val.setText(value != null ? value : "—");
        val.setTextColor(UiTheme.NAVY);
        val.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(val, 13);
        row.addView(val);

        parent.addView(row);
    }

    private View createStatCard(String count, String label, String subtext) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 12);
        card.setPadding(pad, pad, pad, pad);

        TextView countText = new TextView(context);
        countText.setText(count);
        countText.setTextColor(UiTheme.NAVY);
        countText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(countText, 22);
        card.addView(countText);

        TextView lblText = new TextView(context);
        lblText.setText(label);
        lblText.setTextColor(UiTheme.NAVY);
        lblText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(lblText, 13);
        card.addView(lblText);

        TextView subText = new TextView(context);
        subText.setText(subtext);
        subText.setTextColor(UiTheme.MUTED);
        subText.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(subText, 11);
        card.addView(subText);

        return card;
    }

    private View createStudyCard(String id, String title, String stage, Runnable onClick) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 14);
        card.setPadding(pad, pad, pad, pad);

        TextView idText = new TextView(context);
        idText.setText(id);
        idText.setTextColor(UiTheme.MUTED);
        idText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(idText, 12);
        card.addView(idText);

        addSpacing(context, card, 4);

        TextView titleText = new TextView(context);
        titleText.setText(title);
        titleText.setTextColor(UiTheme.NAVY);
        titleText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(titleText, 14);
        card.addView(titleText);

        addSpacing(context, card, 6);

        TextView stageBadge = new TextView(context);
        stageBadge.setText("Stage " + stage);
        stageBadge.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(stageBadge, 11);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        stageBadge.setPadding(px, py, px, py);
        if ("Completed".equalsIgnoreCase(stage)) {
            stageBadge.setBackground(UiTheme.shape(context, UiTheme.STATUS_COMPLETED_BG, 4, 0));
            stageBadge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
        } else {
            stageBadge.setBackground(UiTheme.shape(context, UiTheme.STATUS_IN_REVIEW_BG, 4, 0));
            stageBadge.setTextColor(UiTheme.STATUS_IN_REVIEW_TEXT);
        }
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stageBadge.setLayoutParams(bLp);
        card.addView(stageBadge);

        card.setOnClickListener(v -> {
            if (onClick != null) onClick.run();
        });

        return card;
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
