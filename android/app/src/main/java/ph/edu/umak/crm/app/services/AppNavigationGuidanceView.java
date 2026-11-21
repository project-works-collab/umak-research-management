package ph.edu.umak.crm.app.services;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class AppNavigationGuidanceView {
    private final LinearLayout layout;

    public AppNavigationGuidanceView(Context context, Runnable onBack) {
        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 32));

        // 1. Bottom Navigation Guide Card
        LinearLayout navCard = createCard(context);
        addCardHeader(context, navCard, "Bottom navigation", bold);

        int[] icons = {
                R.drawable.ic_staff_dashboard,
                R.drawable.ic_researcher_services,
                R.drawable.ic_staff_requests,
                R.drawable.ic_researcher_search,
                R.drawable.ic_staff_profile
        };
        String[] titles = {
                "Dashboard",
                "Services",
                "Requests",
                "Research",
                "Profile"
        };
        String[] descriptions = {
                "See your active studies, requests and next actions.",
                "Browse CRM services, requirements and approved forms.",
                "Submit service requests and track their status.",
                "Search the Research Archive and the UNIVERSITY journal.",
                "View your account details, settings and sign out."
        };

        for (int i = 0; i < titles.length; i++) {
            if (i > 0) {
                View divider = new View(context);
                divider.setBackgroundColor(0xfff1f5f9);
                LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 1));
                divLp.topMargin = UiTheme.dp(context, 10);
                divLp.bottomMargin = UiTheme.dp(context, 10);
                navCard.addView(divider, divLp);
            }

            LinearLayout itemRow = new LinearLayout(context);
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setGravity(Gravity.CENTER_VERTICAL);

            FrameLayout iconFrame = new FrameLayout(context);
            iconFrame.setBackground(UiTheme.shape(context, 0xffe5edf7, 6, 0));
            ImageView icon = new ImageView(context);
            icon.setImageResource(icons[i]);
            icon.setColorFilter(0xff185d9d);
            int iconSize = UiTheme.dp(context, 20);
            iconFrame.addView(icon, new FrameLayout.LayoutParams(iconSize, iconSize, Gravity.CENTER));
            int frameSize = UiTheme.dp(context, 36);
            itemRow.addView(iconFrame, new LinearLayout.LayoutParams(frameSize, frameSize));

            LinearLayout textCol = new LinearLayout(context);
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, -2, 1);
            colLp.setMarginStart(UiTheme.dp(context, 12));

            TextView itemTitle = new TextView(context);
            itemTitle.setText(titles[i]);
            itemTitle.setTextColor(0xff082d50);
            itemTitle.setTypeface(bold);
            ScreenTypography.apply(itemTitle, 14);
            textCol.addView(itemTitle);

            TextView itemDesc = new TextView(context);
            itemDesc.setText(descriptions[i]);
            itemDesc.setTextColor(0xff586b83);
            itemDesc.setTypeface(body);
            ScreenTypography.apply(itemDesc, 13);
            itemDesc.setPadding(0, UiTheme.dp(context, 2), 0, 0);
            textCol.addView(itemDesc);

            itemRow.addView(textCol, colLp);
            navCard.addView(itemRow);
        }
        layout.addView(navCard);

        addSpacing(context, layout, 14);

        // 2. Contact CRM Card
        LinearLayout contactCard = createCard(context);
        addCardHeader(context, contactCard, "Contact CRM", bold);

        TextView introText = new TextView(context);
        introText.setText("Please contact the Center for Research Management (CRM) via email or visit the office within office hours.");
        introText.setTextColor(0xff586b83);
        introText.setTypeface(body);
        ScreenTypography.apply(introText, 13);
        introText.setLineSpacing(0, 1.2f);
        contactCard.addView(introText);

        TextView emailLabel = new TextView(context);
        emailLabel.setText("Email Address");
        emailLabel.setTextColor(0xff082d50);
        emailLabel.setTypeface(bold);
        ScreenTypography.apply(emailLabel, 12);
        LinearLayout.LayoutParams elLp = new LinearLayout.LayoutParams(-2, -2);
        elLp.topMargin = UiTheme.dp(context, 10);
        contactCard.addView(emailLabel, elLp);

        TextView emailVal = new TextView(context);
        emailVal.setText("research@umak.edu.ph");
        emailVal.setTextColor(0xff185d9d);
        emailVal.setTypeface(body);
        ScreenTypography.apply(emailVal, 13);
        emailVal.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        contactCard.addView(emailVal);

        TextView locLabel = new TextView(context);
        locLabel.setText("Office Location");
        locLabel.setTextColor(0xff082d50);
        locLabel.setTypeface(bold);
        ScreenTypography.apply(locLabel, 12);
        LinearLayout.LayoutParams llLp = new LinearLayout.LayoutParams(-2, -2);
        llLp.topMargin = UiTheme.dp(context, 10);
        contactCard.addView(locLabel, llLp);

        TextView locVal = new TextView(context);
        locVal.setText("8th Floor, Health and Physical Science Building, University of Makati, JP Rizal Extn., West Rembo, Makati City");
        locVal.setTextColor(0xff586b83);
        locVal.setTypeface(body);
        ScreenTypography.apply(locVal, 13);
        locVal.setLineSpacing(0, 1.15f);
        locVal.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        contactCard.addView(locVal);

        layout.addView(contactCard);

        addSpacing(context, layout, 18);

        // 3. Back to Service Button
        Button backBtn = new Button(context);
        backBtn.setText("Back to Service");
        backBtn.setTextColor(0xff082d50);
        backBtn.setTypeface(bold);
        ScreenTypography.apply(backBtn, 14);
        backBtn.setBackground(UiTheme.shape(context, 0xffe5edf7, 8, 0));
        backBtn.setOnClickListener(v -> {
            if (onBack != null) onBack.run();
        });
        layout.addView(backBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));
    }

    private LinearLayout createCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 14);
        card.setPadding(pad, pad, pad, pad);
        return card;
    }

    private void addCardHeader(Context context, LinearLayout parent, String title, Typeface bold) {
        TextView header = new TextView(context);
        header.setText(title);
        header.setTextColor(0xff082d50);
        header.setTypeface(bold);
        ScreenTypography.apply(header, 14);
        header.setPadding(0, 0, 0, UiTheme.dp(context, 10));
        parent.addView(header);
    }

    private void addSpacing(Context context, LinearLayout parent, int dp) {
        TextView space = new TextView(context);
        space.setHeight(UiTheme.dp(context, dp));
        parent.addView(space);
    }

    public View getView() {
        return getContentLayout();
    }

    public LinearLayout getContentLayout() {
        if (layout.getParent() instanceof android.view.ViewGroup) {
            ((android.view.ViewGroup) layout.getParent()).removeView(layout);
        }
        return PageTitle.wrap(layout, "App Navigation Guidance", "Use the bottom tabs to move around RMLIS.", false);
    }
}
