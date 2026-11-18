package ph.edu.umak.crm.app.services;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class AppNavigationGuidanceView {
    private final LinearLayout layout;

    public AppNavigationGuidanceView(Context context, Runnable onBack) {
        Typeface heading = UiTheme.headingFont(context);
        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 24);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 32));

        TextView navSectionLabel = new TextView(context);
        navSectionLabel.setText("BOTTOM NAVIGATION");
        navSectionLabel.setTextColor(UiTheme.MUTED);
        navSectionLabel.setTypeface(bold);
        ScreenTypography.apply(navSectionLabel, 11);
        navSectionLabel.setPadding(0, 0, 0, UiTheme.dp(context, 10));
        layout.addView(navSectionLabel);

        addTabGuide(context, layout, "Dashboard", "See your active studies, requests and next actions.", bold, body);
        addTabGuide(context, layout, "Services", "Browse CRM services, requirements and approved forms.", bold, body);
        addTabGuide(context, layout, "Requests", "Submit service requests and track their status.", bold, body);
        addTabGuide(context, layout, "Research", "Search the Research Archive and the UNIVERSITY journal.", bold, body);
        addTabGuide(context, layout, "Profile", "View your account details, settings and sign out.", bold, body);

        Button backBtn = UiButton.primary(context, "Back to Service", onBack);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(-1, -2);
        btnParams.topMargin = UiTheme.dp(context, 24);
        layout.addView(backBtn, btnParams);
    }

    private void addTabGuide(Context context, LinearLayout parent, String tabName, String tabDesc, Typeface bold, Typeface body) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 14);
        card.setPadding(pad, pad, pad, pad);

        TextView name = new TextView(context);
        name.setText(tabName);
        name.setTextColor(UiTheme.INK);
        name.setTypeface(bold);
        ScreenTypography.apply(name, 15);
        card.addView(name);

        TextView desc = new TextView(context);
        desc.setText(tabDesc);
        desc.setTextColor(UiTheme.MUTED);
        desc.setTypeface(body);
        ScreenTypography.apply(desc, 13);
        desc.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        card.addView(desc);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = UiTheme.dp(context, 10);
        parent.addView(card, lp);
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
