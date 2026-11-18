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

public final class ExternalSubmissionView {
    private final LinearLayout layout;
    private final String serviceLabel;

    public ExternalSubmissionView(Context context, ServiceData.ServiceItem service, Runnable onBack) {
        serviceLabel = service.name + " · " + service.code;
        Typeface heading = UiTheme.headingFont(context);
        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 24);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 32));

        // Section: Submission channel
        addSectionHeader(context, layout, "SUBMISSION CHANNEL", bold);

        LinearLayout channelBox = new LinearLayout(context);
        channelBox.setOrientation(LinearLayout.VERTICAL);
        channelBox.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        channelBox.setPadding(pad, pad, pad, pad);

        TextView channelName = new TextView(context);
        channelName.setText(service.responsibleContact);
        channelName.setTextColor(UiTheme.INK);
        channelName.setTypeface(bold);
        ScreenTypography.apply(channelName, 15);
        channelBox.addView(channelName);

        TextView channelEmail = new TextView(context);
        channelEmail.setText(service.contactEmail);
        channelEmail.setTextColor(UiTheme.ACCENT);
        channelEmail.setTypeface(body);
        ScreenTypography.apply(channelEmail, 14);
        channelEmail.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        channelBox.addView(channelEmail);

        layout.addView(channelBox);

        TextView channelNote = new TextView(context);
        channelNote.setText("This service is submitted outside the app. No request is created here.");
        channelNote.setTextColor(UiTheme.MUTED);
        channelNote.setTypeface(body);
        ScreenTypography.apply(channelNote, 13);
        channelNote.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 20));
        layout.addView(channelNote);

        // Section: Steps
        addSectionHeader(context, layout, "STEPS", bold);

        String[] steps = service.instructions;

        LinearLayout stepsContainer = new LinearLayout(context);
        stepsContainer.setOrientation(LinearLayout.VERTICAL);
        stepsContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        stepsContainer.setPadding(pad, pad, pad, pad);

        for (int i = 0; i < steps.length; i++) {
            LinearLayout stepRow = new LinearLayout(context);
            stepRow.setOrientation(LinearLayout.HORIZONTAL);
            stepRow.setPadding(0, 0, 0, i == steps.length - 1 ? 0 : UiTheme.dp(context, 12));

            TextView num = new TextView(context);
            num.setText(String.valueOf(i + 1));
            num.setTextColor(UiTheme.NAVY);
            num.setTypeface(bold);
            ScreenTypography.apply(num, 14);
            num.setBackground(UiTheme.shape(context, UiTheme.PALE, 12, 0));
            num.setGravity(android.view.Gravity.CENTER);
            int numSize = UiTheme.dp(context, 24);
            LinearLayout.LayoutParams numLp = new LinearLayout.LayoutParams(numSize, numSize);
            numLp.rightMargin = UiTheme.dp(context, 12);
            stepRow.addView(num, numLp);

            TextView stepText = new TextView(context);
            stepText.setText(steps[i]);
            stepText.setTextColor(UiTheme.INK);
            stepText.setTypeface(body);
            ScreenTypography.apply(stepText, 14);
            stepRow.addView(stepText, new LinearLayout.LayoutParams(0, -2, 1));

            stepsContainer.addView(stepRow);
        }
        layout.addView(stepsContainer);

        Button backBtn = UiButton.primary(context, "Back to Service", onBack);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(-1, -2);
        btnParams.topMargin = UiTheme.dp(context, 24);
        layout.addView(backBtn, btnParams);
    }

    private void addSectionHeader(Context context, LinearLayout parent, String label, Typeface bold) {
        TextView header = new TextView(context);
        header.setText(label);
        header.setTextColor(UiTheme.MUTED);
        header.setTypeface(bold);
        ScreenTypography.apply(header, 11);
        header.setPadding(0, 0, 0, UiTheme.dp(context, 8));
        parent.addView(header);
    }

    public View getView() {
        return getContentLayout();
    }

    public LinearLayout getContentLayout() {
        if (layout.getParent() instanceof android.view.ViewGroup) {
            ((android.view.ViewGroup) layout.getParent()).removeView(layout);
        }
        return PageTitle.wrap(layout, "Submission Instructions", serviceLabel, false);
    }
}
