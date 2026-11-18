package ph.edu.umak.crm.app.staff;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class ManageHubView {

    public interface Listener {
        void onOpenServiceCatalog();
        void onOpenApprovedForms();
        void onOpenUserAccounts();
        void onOpenAuditHistory();
        void onOpenAnalyticsReporting();
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;

    public ManageHubView(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        addSpacing(context, layout, 20);

        // Staff Actions Header
        TextView sectionTitle = new TextView(context);
        sectionTitle.setText("Staff Actions");
        sectionTitle.setTextColor(UiTheme.NAVY);
        sectionTitle.setTypeface(bold);
        ScreenTypography.apply(sectionTitle, 16);
        layout.addView(sectionTitle);

        addSpacing(context, layout, 12);

        // 1. Service Catalog Card
        layout.addView(createActionCard(
                "Service Catalog",
                "Create, edit, publish or withdraw services.",
                () -> {
                    if (listener != null) listener.onOpenServiceCatalog();
                }
        ));

        addSpacing(context, layout, 10);

        // 2. Approved Forms & Resources Card
        layout.addView(createActionCard(
                "Approved Forms & Resources",
                "Manage current and superseded form versions.",
                () -> {
                    if (listener != null) listener.onOpenApprovedForms();
                }
        ));

        addSpacing(context, layout, 10);

        // 3. User Accounts Card
        layout.addView(createActionCard(
                "User Accounts",
                "Create staff accounts and manage access.",
                () -> {
                    if (listener != null) listener.onOpenUserAccounts();
                }
        ));

        addSpacing(context, layout, 10);

        // 4. Audit History Card
        layout.addView(createActionCard(
                "Audit History",
                "Publication and account change records.",
                () -> {
                    if (listener != null) listener.onOpenAuditHistory();
                }
        ));

        addSpacing(context, layout, 10);

        // 5. Analytics & Reporting Card (Part 9, C-05)
        layout.addView(createActionCard(
                "Analytics & Reporting",
                "Research reports and service utilization.",
                () -> {
                    if (listener != null) listener.onOpenAnalyticsReporting();
                }
        ));
    }

    private View createActionCard(String title, String description, Runnable onClick) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 16);
        card.setPadding(pad, pad, pad, pad);
        card.setClickable(true);
        card.setFocusable(true);

        TextView tView = new TextView(context);
        tView.setText(title);
        tView.setTextColor(UiTheme.NAVY);
        tView.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(tView, 16);
        card.addView(tView);

        addSpacing(context, card, 4);

        TextView dView = new TextView(context);
        dView.setText(description);
        dView.setTextColor(UiTheme.MUTED);
        dView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(dView, 13);
        card.addView(dView);

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

    public View getContentLayout() {
        if (layout.getParent() instanceof ViewGroup) {
            ((ViewGroup) layout.getParent()).removeView(layout);
        }
        return PageTitle.wrap(layout, "Manage", "Services, forms, accounts, and audit records.", false);
    }
}
