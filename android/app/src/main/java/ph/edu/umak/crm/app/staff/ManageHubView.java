package ph.edu.umak.crm.app.staff;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class ManageHubView {

    public interface Listener {
        void onOpenServiceCatalog();
        void onOpenApprovedForms();
        void onOpenUserAccounts();
        void onOpenAuditHistory();
        default void onOpenAnalyticsReporting() {}
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

        addSpacing(context, layout, 14);

        // Staff Actions Single Container Card
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        card.setPadding(pad, pad, pad, pad);

        TextView sectionTitle = new TextView(context);
        sectionTitle.setText("Staff Actions");
        sectionTitle.setTextColor(0xff082d50);
        sectionTitle.setTypeface(bold);
        ScreenTypography.apply(sectionTitle, 16);
        card.addView(sectionTitle);

        addSpacing(context, card, 12);

        // 1. Service Catalog Row (routes to 5.2)
        card.addView(createActionRow(
                R.drawable.ic_crm_book,
                "Service Catalog",
                "Create, edit, publish or withdraw services.",
                () -> {
                    if (listener != null) listener.onOpenServiceCatalog();
                }
        ));

        addDivider(card);

        // 2. Approved Forms & Resources Row (routes to 5.4)
        card.addView(createActionRow(
                R.drawable.ic_crm_doc,
                "Approved Forms & Resources",
                "Manage current and superseded form versions.",
                () -> {
                    if (listener != null) listener.onOpenApprovedForms();
                }
        ));

        addDivider(card);

        // 3. User Accounts Row (routes to 5.8)
        card.addView(createActionRow(
                R.drawable.ic_crm_users,
                "User Accounts",
                "Create staff accounts and manage access.",
                () -> {
                    if (listener != null) listener.onOpenUserAccounts();
                }
        ));

        addDivider(card);

        // 4. Audit History Row (routes to 5.7)
        card.addView(createActionRow(
                R.drawable.ic_crm_history,
                "Audit History",
                "Publication and account change records.",
                () -> {
                    if (listener != null) listener.onOpenAuditHistory();
                }
        ));

        layout.addView(card);
    }

    private View createActionRow(int iconRes, String title, String description, Runnable onClick) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int padV = UiTheme.dp(context, 12);
        row.setPadding(0, padV, 0, padV);
        row.setClickable(true);
        row.setFocusable(true);
        row.setBackground(UiTheme.ripple(null, 0x15082d50));

        // Left Icon
        ImageView iconView = new ImageView(context);
        iconView.setImageResource(iconRes);
        iconView.setColorFilter(0xff082d50);
        int iconSize = UiTheme.dp(context, 20);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconLp.rightMargin = UiTheme.dp(context, 14);
        row.addView(iconView, iconLp);

        // Text Column
        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView tView = new TextView(context);
        tView.setText(title);
        tView.setTextColor(0xff082d50);
        tView.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(tView, 14);
        textCol.addView(tView);

        TextView dView = new TextView(context);
        dView.setText(description);
        dView.setTextColor(0xff586b83);
        dView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(dView, 12);
        dView.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        textCol.addView(dView);

        row.addView(textCol, textLp);

        // Right Chevron >
        ImageView chevron = new ImageView(context);
        chevron.setImageResource(R.drawable.ic_crm_chevron_right);
        chevron.setColorFilter(0xff94a3b8);
        int cSize = UiTheme.dp(context, 18);
        row.addView(chevron, new LinearLayout.LayoutParams(cSize, cSize));

        row.setOnClickListener(v -> {
            if (onClick != null) onClick.run();
        });

        return row;
    }

    private void addDivider(LinearLayout parent) {
        View line = new View(context);
        line.setBackgroundColor(0xfff1f5f9);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 1));
        parent.addView(line, lp);
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
        return PageTitle.wrap(layout, "Manage", "Services, forms, accounts and audit records.", false);
    }
}
