package ph.edu.umak.crm.app.staff.content;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
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
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 24));

        // 1. Service Selector Card
        TextView servLabel = new TextView(context);
        servLabel.setText("Service");
        servLabel.setTextColor(0xff082d50);
        servLabel.setTypeface(bold);
        ScreenTypography.apply(servLabel, 13);
        layout.addView(servLabel);

        addSpacing(context, layout, 6);

        FrameLayout serviceBox = new FrameLayout(context);
        serviceBox.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int boxPadH = UiTheme.dp(context, 14);
        int boxPadV = UiTheme.dp(context, 12);
        serviceBox.setPadding(boxPadH, boxPadV, boxPadH, boxPadV);

        TextView servVal = new TextView(context);
        servVal.setText(formData.serviceId + " · " + formData.serviceName);
        servVal.setTextColor(0xff082d50);
        servVal.setTypeface(bold);
        ScreenTypography.apply(servVal, 14);
        FrameLayout.LayoutParams sValLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sValLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        serviceBox.addView(servVal, sValLp);

        ImageView chevron = new ImageView(context);
        chevron.setImageResource(R.drawable.ic_crm_chevron_down);
        chevron.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams cLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 20), UiTheme.dp(context, 20));
        cLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        serviceBox.addView(chevron, cLp);

        layout.addView(serviceBox);

        addSpacing(context, layout, 20);

        // 2. Current Version Section
        LinearLayout curHeaderRow = new LinearLayout(context);
        curHeaderRow.setOrientation(LinearLayout.HORIZONTAL);
        curHeaderRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView curHeader = new TextView(context);
        curHeader.setText("Current version");
        curHeader.setTextColor(0xff082d50);
        curHeader.setTypeface(bold);
        ScreenTypography.apply(curHeader, 16);
        curHeaderRow.addView(curHeader, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView currentBadge = new TextView(context);
        currentBadge.setText(formData.live?formData.state:"Current");
        currentBadge.setTypeface(bold);
        ScreenTypography.apply(currentBadge, 11);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        currentBadge.setPadding(px, py, px, py);
        currentBadge.setBackground(UiTheme.shape(context, 0xffdcfce7, 4, 0));
        currentBadge.setTextColor(0xff16a34a);
        curHeaderRow.addView(currentBadge);

        layout.addView(curHeaderRow);

        addSpacing(context, layout, 8);

        // Current Version Card Box
        LinearLayout curCard = new LinearLayout(context);
        curCard.setOrientation(LinearLayout.VERTICAL);
        curCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        curCard.setPadding(pad, pad, pad, pad);

        LinearLayout docRow = new LinearLayout(context);
        docRow.setOrientation(LinearLayout.HORIZONTAL);
        docRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView docIcon = new ImageView(context);
        docIcon.setImageResource(R.drawable.ic_crm_doc);
        docIcon.setColorFilter(0xff185d9d);
        int dSize = UiTheme.dp(context, 24);
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(dSize, dSize);
        dLp.rightMargin = UiTheme.dp(context, 12);
        docRow.addView(docIcon, dLp);

        LinearLayout docTextCol = new LinearLayout(context);
        docTextCol.setOrientation(LinearLayout.VERTICAL);

        TextView fTitle = new TextView(context);
        fTitle.setText(formData.formTitle);
        fTitle.setTextColor(0xff082d50);
        fTitle.setTypeface(bold);
        ScreenTypography.apply(fTitle, 15);
        docTextCol.addView(fTitle);

        TextView fVer = new TextView(context);
        fVer.setText(formData.currentVersion);
        fVer.setTextColor(0xff586b83);
        fVer.setTypeface(regular);
        ScreenTypography.apply(fVer, 12);
        fVer.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        docTextCol.addView(fVer);

        docRow.addView(docTextCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        curCard.addView(docRow);

        addSpacing(context, curCard, 14);

        // Action Buttons Row: Replace Version & Withdraw
        LinearLayout actRow = new LinearLayout(context);
        actRow.setOrientation(LinearLayout.HORIZONTAL);

        Button replaceBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        replaceBtn.setText("Replace Version");
        replaceBtn.setAllCaps(false);
        replaceBtn.setTextColor(0xff082d50);
        replaceBtn.setTypeface(bold);
        ScreenTypography.apply(replaceBtn, 13);
        replaceBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        replaceBtn.setMinHeight(UiTheme.dp(context, 40));
        replaceBtn.setPadding(UiTheme.dp(context, 16), 0, UiTheme.dp(context, 16), 0);
        replaceBtn.setOnClickListener(v -> {
            if (listener != null) listener.onReplaceVersion();
        });
        actRow.addView(replaceBtn);
        if(formData.live)curCard.addView(UiButton.outline(context,"Download form",UiTheme.BORDER,UiTheme.NAVY,()->listener.onDownload()));

        addHorizontalSpacing(context, actRow, 10);

        Button withdrawBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        withdrawBtn.setText("Withdraw");
        withdrawBtn.setAllCaps(false);
        withdrawBtn.setTextColor(0xffdc2626);
        withdrawBtn.setTypeface(bold);
        ScreenTypography.apply(withdrawBtn, 13);
        withdrawBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xfffca5a5), 0x20dc2626));
        withdrawBtn.setMinHeight(UiTheme.dp(context, 40));
        withdrawBtn.setPadding(UiTheme.dp(context, 16), 0, UiTheme.dp(context, 16), 0);
        withdrawBtn.setOnClickListener(v -> {
            if (listener != null) listener.onWithdraw();
        });
        if(!formData.live||"published".equals(formData.state))actRow.addView(withdrawBtn);

        curCard.addView(actRow);
        layout.addView(curCard);

        addSpacing(context, layout, 22);

        // 3. Superseded versions Section
        LinearLayout supHeaderRow = new LinearLayout(context);
        supHeaderRow.setOrientation(LinearLayout.HORIZONTAL);
        supHeaderRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView supHeader = new TextView(context);
        supHeader.setText("Superseded versions");
        supHeader.setTextColor(0xff082d50);
        supHeader.setTypeface(bold);
        ScreenTypography.apply(supHeader, 16);
        supHeaderRow.addView(supHeader, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView staffOnly = new TextView(context);
        staffOnly.setText("🔒 Staff only");
        staffOnly.setTypeface(bold);
        ScreenTypography.apply(staffOnly, 11);
        staffOnly.setPadding(px, py, px, py);
        staffOnly.setBackground(UiTheme.shape(context, 0xfff1f5f9, 4, 0));
        staffOnly.setTextColor(0xff64748b);
        supHeaderRow.addView(staffOnly);

        layout.addView(supHeaderRow);

        addSpacing(context, layout, 8);

        // Add each superseded version card
        addSupersededCard("Version 2", "Updated Jan 2026 · Superseded Sep 2026");
        addSupersededCard("Version 1", "Updated Aug 2025 · Superseded Jan 2026");
    }

    private void addSupersededCard(String versionTitle, String versionMeta) {
        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        LinearLayout sCard = new LinearLayout(context);
        sCard.setOrientation(LinearLayout.HORIZONTAL);
        sCard.setGravity(Gravity.CENTER_VERTICAL);
        sCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int sPad = UiTheme.dp(context, 12);
        sCard.setPadding(sPad, sPad, sPad, sPad);

        ImageView docIcon = new ImageView(context);
        docIcon.setImageResource(R.drawable.ic_crm_doc);
        docIcon.setColorFilter(0xff64748b);
        int dSize = UiTheme.dp(context, 20);
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(dSize, dSize);
        dLp.rightMargin = UiTheme.dp(context, 12);
        sCard.addView(docIcon, dLp);

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(context);
        title.setText(versionTitle);
        title.setTextColor(0xff082d50);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 14);
        textCol.addView(title);

        TextView meta = new TextView(context);
        meta.setText(versionMeta);
        meta.setTextColor(0xff586b83);
        meta.setTypeface(regular);
        ScreenTypography.apply(meta, 12);
        meta.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        textCol.addView(meta);

        sCard.addView(textCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        ImageView eyeIcon = new ImageView(context);
        eyeIcon.setImageResource(R.drawable.ic_crm_eye);
        eyeIcon.setColorFilter(0xff64748b);
        int eSize = UiTheme.dp(context, 20);
        sCard.addView(eyeIcon, new LinearLayout.LayoutParams(eSize, eSize));

        layout.addView(sCard);
        addSpacing(context, layout, 8);
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

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        // Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(context);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = UiTheme.dp(context, 16);
        band.setPadding(pad, pad, pad, pad);

        TextView heading = new TextView(context);
        heading.setText("Approved Forms & Resources");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        TextView copy = new TextView(context);
        copy.setText("Researchers only see the current version.");
        copy.setTextColor(UiTheme.PALE);
        copy.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(copy, 13);
        band.addView(copy);

        root.addView(band);
        root.addView(layout);
        return root;
    }
}
