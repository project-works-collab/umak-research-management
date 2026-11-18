package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
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

        // Institution Header
        TextView institution = new TextView(context);
        institution.setText("Center for Research Management");
        institution.setTextColor(UiTheme.MUTED);
        institution.setTypeface(regular);
        ScreenTypography.apply(institution, 12);
        layout.addView(institution);

        // Screen Title
        TextView screenTitle = new TextView(context);
        screenTitle.setText("Research registered");
        screenTitle.setTextColor(UiTheme.NAVY);
        screenTitle.setTypeface(bold);
        ScreenTypography.apply(screenTitle, 22);
        screenTitle.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));
        layout.addView(screenTitle);

        // Subtitle
        TextView subtitle = new TextView(context);
        subtitle.setText("Your research project has been registered with the Center for Research Management.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 14);
        layout.addView(subtitle);

        addSpacing(context, layout, 16);

        // Receipt Card
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        card.setPadding(pad, pad, pad, pad);

        String regId = researchId != null ? researchId : "REG-2026-089";
        String regTitle = title != null ? title : "Impact of AI on Student Engagement";

        addReceiptRow(card, "Research ID", regId, bold);
        addReceiptRow(card, "Title", regTitle, bold);
        addReceiptRow(card, "Registered on", registeredDate, regular);

        // Stage Row with badge
        LinearLayout stageRow = new LinearLayout(context);
        stageRow.setOrientation(LinearLayout.HORIZONTAL);
        stageRow.setGravity(Gravity.CENTER_VERTICAL);
        stageRow.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

        TextView stageLabel = new TextView(context);
        stageLabel.setText("Stage");
        stageLabel.setTextColor(UiTheme.MUTED);
        stageLabel.setTypeface(regular);
        ScreenTypography.apply(stageLabel, 14);
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        stageRow.addView(stageLabel, lblLp);

        TextView badge = new TextView(context);
        badge.setText("Registered");
        badge.setTextColor(UiTheme.STATUS_SUBMITTED_TEXT);
        badge.setTypeface(bold);
        ScreenTypography.apply(badge, 12);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 4);
        badge.setPadding(px, py, px, py);
        badge.setBackground(UiTheme.shape(context, UiTheme.STATUS_SUBMITTED_BG, 4, 0));
        stageRow.addView(badge);
        card.addView(stageRow);

        layout.addView(card);

        addSpacing(context, layout, 24);

        // Actions
        Button viewProgressBtn = UiButton.primary(context, "View Research Progress", () -> {
            if (listener != null) listener.onViewProgress();
        });
        layout.addView(viewProgressBtn);

        addSpacing(context, layout, 12);

        Button backBtn = UiButton.outline(context, "Back to Dashboard", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (listener != null) listener.onBackToDashboard();
        });
        layout.addView(backBtn);
    }

    private void addReceiptRow(LinearLayout parent, String label, String value, Typeface valTypeface) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(UiTheme.MUTED);
        lbl.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(lbl, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(lbl, lp);

        TextView val = new TextView(context);
        val.setText(value);
        val.setTextColor(UiTheme.NAVY);
        val.setTypeface(valTypeface);
        ScreenTypography.apply(val, 14);
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
