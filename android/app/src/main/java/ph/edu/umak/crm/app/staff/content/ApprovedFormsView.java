package ph.edu.umak.crm.app.staff.content;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.staff.StaffData;

public final class ApprovedFormsView {

    public interface Listener {
        void onBackToManage();
        void onReplaceVersion();
        void onWithdraw();
        default void onDownload() {}
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;
    private final StaffData.ApprovedFormItem formData;

    public ApprovedFormsView(Context context, Listener listener) {
        this(context,listener,StaffData.getApprovedForms());
    }
    public ApprovedFormsView(Context context,Listener listener,StaffData.ApprovedFormItem data) {
        this.context = context;
        this.listener = listener;
        this.formData = data;

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

        addSpacing(context, layout, 16);

        // Service selector card
        LinearLayout serviceCard = new LinearLayout(context);
        serviceCard.setOrientation(LinearLayout.VERTICAL);
        serviceCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        serviceCard.setPadding(pad, pad, pad, pad);

        TextView servLabel = new TextView(context);
        servLabel.setText("Service");
        servLabel.setTextColor(UiTheme.MUTED);
        servLabel.setTypeface(regular);
        ScreenTypography.apply(servLabel, 12);
        serviceCard.addView(servLabel);

        addSpacing(context, serviceCard, 4);

        TextView servVal = new TextView(context);
        servVal.setText(formData.serviceId + " · " + formData.serviceName);
        servVal.setTextColor(UiTheme.NAVY);
        servVal.setTypeface(bold);
        ScreenTypography.apply(servVal, 15);
        serviceCard.addView(servVal);

        layout.addView(serviceCard);

        addSpacing(context, layout, 20);

        // Current Version Section
        TextView curHeader = new TextView(context);
        curHeader.setText("Current version");
        curHeader.setTextColor(UiTheme.NAVY);
        curHeader.setTypeface(bold);
        ScreenTypography.apply(curHeader, 16);
        layout.addView(curHeader);

        addSpacing(context, layout, 10);

        LinearLayout curCard = new LinearLayout(context);
        curCard.setOrientation(LinearLayout.VERTICAL);
        curCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        curCard.setPadding(pad, pad, pad, pad);

        // Badges row
        LinearLayout badgeRow = new LinearLayout(context);
        badgeRow.setOrientation(LinearLayout.HORIZONTAL);
        badgeRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView staffOnly = new TextView(context);
        staffOnly.setText("Staff only");
        staffOnly.setTypeface(bold);
        ScreenTypography.apply(staffOnly, 11);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        staffOnly.setPadding(px, py, px, py);
        staffOnly.setBackground(UiTheme.shape(context, 0xFFF1F5F9, 4, 0));
        staffOnly.setTextColor(UiTheme.MUTED);
        badgeRow.addView(staffOnly);

        addHorizontalSpacing(context, badgeRow, 8);

        TextView currentBadge = new TextView(context);
        currentBadge.setText(formData.live?formData.state:"Current");
        currentBadge.setTypeface(bold);
        ScreenTypography.apply(currentBadge, 11);
        currentBadge.setPadding(px, py, px, py);
        currentBadge.setBackground(UiTheme.shape(context, UiTheme.STATUS_COMPLETED_BG, 4, 0));
        currentBadge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
        badgeRow.addView(currentBadge);

        curCard.addView(badgeRow);

        addSpacing(context, curCard, 8);

        TextView fTitle = new TextView(context);
        fTitle.setText(formData.formTitle);
        fTitle.setTextColor(UiTheme.NAVY);
        fTitle.setTypeface(bold);
        ScreenTypography.apply(fTitle, 16);
        curCard.addView(fTitle);

        addSpacing(context, curCard, 4);

        TextView fVer = new TextView(context);
        fVer.setText(formData.currentVersion);
        fVer.setTextColor(UiTheme.MUTED);
        fVer.setTypeface(regular);
        ScreenTypography.apply(fVer, 13);
        curCard.addView(fVer);

        addSpacing(context, curCard, 14);

        // Actions: Replace Version & Withdraw
        LinearLayout actRow = new LinearLayout(context);
        actRow.setOrientation(LinearLayout.HORIZONTAL);

        Button replaceBtn = UiButton.primary(context, "Replace Version", () -> {
            if (listener != null) listener.onReplaceVersion();
        });
        actRow.addView(replaceBtn);
        if(formData.live)curCard.addView(UiButton.outline(context,"Download form",UiTheme.BORDER,UiTheme.NAVY,()->listener.onDownload()));

        addHorizontalSpacing(context, actRow, 10);

        Button withdrawBtn = UiButton.outline(context, "Withdraw", UiTheme.BORDER, UiTheme.ERROR_RED, () -> {
            if (listener != null) listener.onWithdraw();
        });
        if(!formData.live||"published".equals(formData.state))actRow.addView(withdrawBtn);

        curCard.addView(actRow);
        layout.addView(curCard);

        addSpacing(context, layout, 24);

        // Superseded versions Section
        TextView supHeader = new TextView(context);
        supHeader.setText("Superseded versions");
        supHeader.setTextColor(UiTheme.NAVY);
        supHeader.setTypeface(bold);
        ScreenTypography.apply(supHeader, 16);
        layout.addView(supHeader);

        addSpacing(context, layout, 10);

        for (String sup : formData.supersededVersions) {
            LinearLayout sCard = new LinearLayout(context);
            sCard.setOrientation(LinearLayout.VERTICAL);
            sCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
            int sPad = UiTheme.dp(context, 12);
            sCard.setPadding(sPad, sPad, sPad, sPad);

            TextView sText = new TextView(context);
            sText.setText(sup);
            sText.setTextColor(UiTheme.MUTED);
            sText.setTypeface(regular);
            ScreenTypography.apply(sText, 13);
            sCard.addView(sText);

            layout.addView(sCard);
            addSpacing(context, layout, 8);
        }
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
        return PageTitle.wrap(layout, "Approved Forms & Resources", "Researchers only see the current version.", false);
    }
}
