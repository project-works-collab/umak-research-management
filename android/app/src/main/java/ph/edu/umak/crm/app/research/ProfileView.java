package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
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
    private boolean showSnackbar = false;

    public ProfileView(Context context, ResearchData.UserProfile profile, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.userProfile = profile != null ? profile : ResearchData.getDefaultProfile();

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, pad);

        renderContent();
    }

    public void setShowSnackbar(boolean show) {
        this.showSnackbar = show;
        renderContent();
    }

    private void renderContent() {
        layout.removeAllViews();

        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        addSpacing(context, layout, 12);

        // 1. Profile Header Card (Dark Navy Card #082d50)
        LinearLayout profileCard = new LinearLayout(context);
        profileCard.setOrientation(LinearLayout.VERTICAL);
        profileCard.setBackground(UiTheme.shape(context, 0xff082d50, 12, 0));
        profileCard.setPadding(pad, pad, pad, pad);

        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        // Yellow Avatar Circle "JA"
        TextView avatar = new TextView(context);
        String name = userProfile.name == null ? "" : userProfile.name.trim();
        String initials;
        if (!"LeBron R. James".equalsIgnoreCase(name)) {
            StringBuilder sb = new StringBuilder();
            for (String word : name.split("\\s+")) {
                if (!word.isEmpty() && sb.length() < 2) sb.appendCodePoint(word.codePointAt(0));
            }
            initials = sb.length() > 0 ? sb.toString().toUpperCase(Locale.ROOT) : "JA";
        } else {
            initials = "JA";
        }
        avatar.setText(initials);
        avatar.setTextColor(0xff082d50);
        avatar.setTypeface(bold);
        avatar.setGravity(Gravity.CENTER);
        ScreenTypography.apply(avatar, 18);
        int aSize = UiTheme.dp(context, 48);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(aSize, aSize);
        aLp.rightMargin = UiTheme.dp(context, 14);
        avatar.setLayoutParams(aLp);
        avatar.setBackground(UiTheme.shape(context, 0xfffbbf24, aSize / 2, 0));
        topRow.addView(avatar);

        LinearLayout nameCol = new LinearLayout(context);
        nameCol.setOrientation(LinearLayout.VERTICAL);

        TextView nameText = new TextView(context);
        nameText.setText(userProfile.name);
        nameText.setTextColor(0xffffffff);
        nameText.setTypeface(bold);
        ScreenTypography.apply(nameText, 20);
        nameCol.addView(nameText);

        addSpacing(context, nameCol, 4);

        TextView roleBadge = new TextView(context);
        roleBadge.setText(userProfile.role != null ? userProfile.role : "Student Researcher");
        roleBadge.setTextColor(0xff3730a3);
        roleBadge.setTypeface(bold);
        ScreenTypography.apply(roleBadge, 11);
        roleBadge.setBackground(UiTheme.shape(context, 0xffe0e7ff, 10, 0));
        int rpx = UiTheme.dp(context, 8);
        int rpy = UiTheme.dp(context, 2);
        roleBadge.setPadding(rpx, rpy, rpx, rpy);
        LinearLayout.LayoutParams rbLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameCol.addView(roleBadge, rbLp);

        topRow.addView(nameCol);
        profileCard.addView(topRow);

        addSpacing(context, profileCard, 14);

        // Details grid inside header card
        addHeaderDetailRow(profileCard, "PROGRAM", userProfile.program);
        addHeaderDetailRow(profileCard, "YEAR LEVEL", userProfile.yearLevel);
        addHeaderDetailRow(profileCard, "COLLEGE", userProfile.college.isEmpty() ? "—" : userProfile.college);
        addHeaderDetailRow(profileCard, "EMAIL", userProfile.email);
        addHeaderDetailRow(profileCard, "STUDENT ID", userProfile.accountId);

        addSpacing(context, profileCard, 14);

        Button editProfileBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        editProfileBtn.setText("Edit Profile");
        editProfileBtn.setAllCaps(false);
        editProfileBtn.setTextColor(0xff082d50);
        editProfileBtn.setTypeface(bold);
        ScreenTypography.apply(editProfileBtn, 14);
        editProfileBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        editProfileBtn.setMinHeight(UiTheme.dp(context, 44));
        editProfileBtn.setOnClickListener(v -> {
            if (listener != null) listener.onEditProfile();
        });
        profileCard.addView(editProfileBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 44)));

        layout.addView(profileCard);

        addSpacing(context, layout, 14);

        // 2. Stats Grid (2 rows x 2 cols)
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

        addSpacing(context, layout, 14);

        // 3. My Research Studies Card
        LinearLayout studiesCard = new LinearLayout(context);
        studiesCard.setOrientation(LinearLayout.VERTICAL);
        studiesCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        int sPad = UiTheme.dp(context, 14);
        studiesCard.setPadding(sPad, sPad, sPad, sPad);

        TextView sectionTitle = new TextView(context);
        sectionTitle.setText("My Research Studies");
        sectionTitle.setTextColor(0xff082d50);
        sectionTitle.setTypeface(bold);
        ScreenTypography.apply(sectionTitle, 16);
        studiesCard.addView(sectionTitle);

        addSpacing(context, studiesCard, 10);

        // Study 1: REG-2026-089 (In progress -> routes to 4.6c)
        View studyCard1 = createStudyCard("REG-2026-089", "Impact of AI on Student Engagement", "In progress",
                () -> {
                    if (listener != null) listener.onOpenStudyProgress("4.6c");
                });
        studiesCard.addView(studyCard1);

        addSpacing(context, studiesCard, 10);

        // Study 2: REG-2025-041 (Completed -> routes to 4.6a)
        View studyCard2 = createStudyCard("REG-2025-041", "Gamified Learning Modules for Introductory Programming", "Completed",
                () -> {
                    if (listener != null) listener.onOpenStudyProgress("4.6a");
                });
        studiesCard.addView(studyCard2);

        layout.addView(studiesCard);

        addSpacing(context, layout, 14);

        // 4. Account Settings Card
        LinearLayout settingsCard = new LinearLayout(context);
        settingsCard.setOrientation(LinearLayout.VERTICAL);
        settingsCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        settingsCard.setPadding(sPad, sPad, sPad, sPad);

        TextView setHeader = new TextView(context);
        setHeader.setText("Account Settings");
        setHeader.setTextColor(0xff082d50);
        setHeader.setTypeface(bold);
        ScreenTypography.apply(setHeader, 16);
        settingsCard.addView(setHeader);

        addSpacing(context, settingsCard, 12);

        addSettingBox(settingsCard, "Email address", userProfile.email);
        addSettingBox(settingsCard, "Account status", userProfile.accountStatus);
        addSettingBox(settingsCard, "Last login", userProfile.lastLogin);

        layout.addView(settingsCard);

        // 5. Bottom Section: Success Snackbar OR Sign Out Button
        if (showSnackbar) {
            addSpacing(context, layout, 14);
            LinearLayout snackbar = new LinearLayout(context);
            snackbar.setOrientation(LinearLayout.HORIZONTAL);
            snackbar.setGravity(Gravity.CENTER_VERTICAL);
            snackbar.setBackground(UiTheme.shape(context, 0xff082d50, 8, 0));
            int snPad = UiTheme.dp(context, 12);
            snackbar.setPadding(snPad, 0, snPad, 0);

            ImageView check = new ImageView(context);
            check.setImageResource(R.drawable.ic_crm_check_small);
            check.setColorFilter(0xff38bdf8);
            int cSize = UiTheme.dp(context, 18);
            snackbar.addView(check, new LinearLayout.LayoutParams(cSize, cSize));

            TextView snText = new TextView(context);
            snText.setText("Profile updated.");
            snText.setTextColor(0xffffffff);
            snText.setTypeface(bold);
            ScreenTypography.apply(snText, 14);
            LinearLayout.LayoutParams snLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            snLp.setMarginStart(UiTheme.dp(context, 10));
            snackbar.addView(snText, snLp);

            layout.addView(snackbar, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));
        } else {
            addSpacing(context, layout, 14);
            Button signOutBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
            signOutBtn.setText("Sign Out");
            signOutBtn.setAllCaps(false);
            signOutBtn.setTextColor(0xffdc2626);
            signOutBtn.setTypeface(bold);
            ScreenTypography.apply(signOutBtn, 15);
            signOutBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xfffca5a5), 0x20dc2626));
            signOutBtn.setMinHeight(UiTheme.dp(context, 46));
            signOutBtn.setOnClickListener(v -> {
                if (listener != null) listener.onSignOut();
            });
            layout.addView(signOutBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));
        }
    }

    private void addHeaderDetailRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 3), 0, UiTheme.dp(context, 3));

        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(0xff94a3b8);
        lbl.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(lbl, 11);
        row.addView(lbl);

        TextView val = new TextView(context);
        val.setText(value != null && !value.isEmpty() ? value : "—");
        val.setTextColor(0xffffffff);
        val.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(val, 13);
        row.addView(val);

        parent.addView(row);
    }

    private void addSettingBox(LinearLayout parent, String label, String value) {
        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(0xff082d50);
        lbl.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(lbl, 13);
        lbl.setPadding(0, 0, 0, UiTheme.dp(context, 4));
        parent.addView(lbl);

        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setBackground(UiTheme.shape(context, 0xfff8fafc, 6, 0xffe2e8f0));
        int padH = UiTheme.dp(context, 12);
        box.setPadding(padH, 0, padH, 0);

        TextView val = new TextView(context);
        val.setText(value != null ? value : "—");
        val.setTextColor(0xff082d50);
        val.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(val, 13);
        box.addView(val);

        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 44));
        boxLp.bottomMargin = UiTheme.dp(context, 10);
        parent.addView(box, boxLp);
    }

    private View createStatCard(String count, String label, String subtext) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 12);
        card.setPadding(pad, pad, pad, pad);

        TextView countText = new TextView(context);
        countText.setText(count);
        countText.setTextColor(0xff185d9d);
        countText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(countText, 24);
        card.addView(countText);

        TextView lblText = new TextView(context);
        lblText.setText(label);
        lblText.setTextColor(0xff082d50);
        lblText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(lblText, 13);
        card.addView(lblText);

        TextView subText = new TextView(context);
        subText.setText(subtext);
        subText.setTextColor(0xff586b83);
        subText.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(subText, 11);
        card.addView(subText);

        return card;
    }

    private View createStudyCard(String id, String title, String stage, Runnable onClick) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 12);
        card.setPadding(pad, pad, pad, pad);

        TextView idText = new TextView(context);
        idText.setText(id);
        idText.setTextColor(0xff185d9d);
        idText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(idText, 12);
        card.addView(idText);

        addSpacing(context, card, 4);

        TextView titleText = new TextView(context);
        titleText.setText(title);
        titleText.setTextColor(0xff082d50);
        titleText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(titleText, 15);
        card.addView(titleText);

        addSpacing(context, card, 6);

        LinearLayout stageRow = new LinearLayout(context);
        stageRow.setOrientation(LinearLayout.HORIZONTAL);
        stageRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView stageLbl = new TextView(context);
        stageLbl.setText("Stage  ");
        stageLbl.setTextColor(0xff586b83);
        stageLbl.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(stageLbl, 12);
        stageRow.addView(stageLbl);

        TextView stageBadge = new TextView(context);
        stageBadge.setText(stage);
        stageBadge.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(stageBadge, 11);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        stageBadge.setPadding(px, py, px, py);
        if ("Completed".equalsIgnoreCase(stage)) {
            stageBadge.setBackground(UiTheme.shape(context, 0xffdcfce7, 4, 0));
            stageBadge.setTextColor(0xff16a34a);
        } else {
            stageBadge.setBackground(UiTheme.shape(context, 0xffe5edf7, 4, 0));
            stageBadge.setTextColor(0xff185d9d);
        }
        stageRow.addView(stageBadge);
        card.addView(stageRow);

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
