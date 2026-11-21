package ph.edu.umak.crm.app.research;

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

public final class RegistrationConfirmationView {

    public interface Listener {
        void onViewProgress();
        void onBackToDashboard();
    }

    private final Context context;
    private final LinearLayout layout;

    public RegistrationConfirmationView(Context context, String researchId, String title, String registeredDate, Listener listener) {
        this.context = context;

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        addSpacing(context, layout, 28);

        // Success Checkmark Badge (Mint Green Circle with Green Checkmark)
        FrameLayout badgeBox = new FrameLayout(context);
        int badgeSize = UiTheme.dp(context, 56);
        badgeBox.setBackground(UiTheme.shape(context, 0xffdcfce7, badgeSize / 2, 0));

        ImageView checkIcon = new ImageView(context);
        checkIcon.setImageResource(R.drawable.ic_crm_check_small);
        checkIcon.setColorFilter(0xff16a34a);
        int iconSize = UiTheme.dp(context, 26);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(iconSize, iconSize, Gravity.CENTER);
        badgeBox.addView(checkIcon, iconParams);

        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(badgeSize, badgeSize);
        badgeParams.gravity = Gravity.CENTER_HORIZONTAL;
        layout.addView(badgeBox, badgeParams);

        addSpacing(context, layout, 16);

        // Title: "Research registered"
        TextView screenTitle = new TextView(context);
        screenTitle.setText("Research registered");
        screenTitle.setTextColor(0xff082d50);
        screenTitle.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(screenTitle, 24);
        ScreenTypography.heading(screenTitle);
        screenTitle.setGravity(Gravity.CENTER);
        layout.addView(screenTitle);

        addSpacing(context, layout, 24);

        // Receipt Card
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        card.setPadding(pad, pad, pad, pad);

        String regId = researchId != null ? researchId : "REG-2026-089";
        String regTitle = title != null ? title : "Impact of AI on Student Engagement";
        String regDateDisplay = registeredDate != null ? "Registered on " + registeredDate : "Registered on Oct 29, 2026";

        addReceiptRow(card, "Research ID", regId, 0xff185d9d, bold);
        addReceiptRow(card, "Title", regTitle, 0xff082d50, bold);
        addReceiptRow(card, "Registered", regDateDisplay, 0xff082d50, bold);

        // Stage Row with badge
        LinearLayout stageRow = new LinearLayout(context);
        stageRow.setOrientation(LinearLayout.HORIZONTAL);
        stageRow.setGravity(Gravity.CENTER_VERTICAL);
        stageRow.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

        TextView stageLabel = new TextView(context);
        stageLabel.setText("Stage");
        stageLabel.setTextColor(0xff586b83);
        stageLabel.setTypeface(regular);
        ScreenTypography.apply(stageLabel, 13);
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        stageRow.addView(stageLabel, lblLp);

        TextView badge = new TextView(context);
        badge.setText("Registered");
        badge.setTextColor(0xff185d9d);
        badge.setTypeface(bold);
        ScreenTypography.apply(badge, 12);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        badge.setPadding(px, py, px, py);
        badge.setBackground(UiTheme.shape(context, 0xffe5edf7, 4, 0));
        stageRow.addView(badge);
        card.addView(stageRow);

        layout.addView(card);

        addSpacing(context, layout, 24);

        // Primary Action Button: View Research Progress (Dark Navy)
        Button viewProgressBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        viewProgressBtn.setText("View Research Progress");
        viewProgressBtn.setAllCaps(false);
        viewProgressBtn.setTypeface(bold);
        viewProgressBtn.setTextColor(0xffffffff);
        ScreenTypography.apply(viewProgressBtn, 15);
        viewProgressBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        viewProgressBtn.setMinHeight(UiTheme.dp(context, 46));
        viewProgressBtn.setOnClickListener(v -> {
            if (listener != null) listener.onViewProgress();
        });
        layout.addView(viewProgressBtn);

        addSpacing(context, layout, 12);

        // Secondary Action Button: Back to Dashboard (White outline)
        Button backBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        backBtn.setText("Back to Dashboard");
        backBtn.setAllCaps(false);
        backBtn.setTypeface(bold);
        backBtn.setTextColor(0xff082d50);
        ScreenTypography.apply(backBtn, 15);
        backBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        backBtn.setMinHeight(UiTheme.dp(context, 46));
        backBtn.setOnClickListener(v -> {
            if (listener != null) listener.onBackToDashboard();
        });
        layout.addView(backBtn);
    }

    private void addReceiptRow(LinearLayout parent, String label, String value, int valueColor, Typeface valTypeface) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(0xff586b83);
        lbl.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(lbl, 13);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(lbl, lp);

        TextView val = new TextView(context);
        val.setText(value);
        val.setTextColor(valueColor);
        val.setTypeface(valTypeface);
        ScreenTypography.apply(val, 13);
        row.addView(val);

        parent.addView(row);
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
        return layout;
    }
}
