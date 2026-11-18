package ph.edu.umak.crm.app.services;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class ExternalSubmissionView {
    private final LinearLayout layout;

    public ExternalSubmissionView(Context context, Runnable onBack) {
        Typeface heading = UiTheme.headingFont(context);
        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 24);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 32));

        TextView institution = new TextView(context);
        institution.setText("Center for Research Management");
        institution.setTextColor(UiTheme.MUTED);
        institution.setTypeface(bold);
        ScreenTypography.apply(institution, 12);
        institution.setPadding(0, 0, 0, UiTheme.dp(context, 4));
        layout.addView(institution);

        TextView title = new TextView(context);
        title.setText("Submission Instructions");
        title.setTextColor(UiTheme.INK);
        title.setTypeface(heading);
        ScreenTypography.apply(title, 26);
        ScreenTypography.heading(title);
        layout.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("UNIVERSITY Journal Endorsement · SVC-04");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(body);
        ScreenTypography.apply(subtitle, 14);
        subtitle.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 20));
        layout.addView(subtitle);

        // Section: Submission channel
        addSectionHeader(context, layout, "SUBMISSION CHANNEL", bold);

        LinearLayout channelBox = new LinearLayout(context);
        channelBox.setOrientation(LinearLayout.VERTICAL);
        channelBox.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        channelBox.setPadding(pad, pad, pad, pad);

        TextView channelName = new TextView(context);
        channelName.setText("UNIVERSITY Editorial Office – email submission");
        channelName.setTextColor(UiTheme.INK);
        channelName.setTypeface(bold);
        ScreenTypography.apply(channelName, 15);
        channelBox.addView(channelName);

        TextView channelEmail = new TextView(context);
        channelEmail.setText("research@umak.edu.ph");
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

        String[] steps = {
                "Complete CRM Form 011 – Journal Endorsement Request.",
                "Combine your manuscript and required documents into one email.",
                "Use the subject \"UNIVERSITY Endorsement – <Study title>\".",
                "Send the email to the UNIVERSITY Editorial Office.",
                "Keep the editorial office's reply as your proof of submission."
        };

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

        // Section: Contact
        TextView contactSpace = new TextView(context);
        contactSpace.setHeight(UiTheme.dp(context, 20));
        layout.addView(contactSpace);
        addSectionHeader(context, layout, "CONTACT", bold);

        LinearLayout contactCard = new LinearLayout(context);
        contactCard.setOrientation(LinearLayout.VERTICAL);
        contactCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        contactCard.setPadding(pad, pad, pad, pad);

        TextView contactTitle = new TextView(context);
        contactTitle.setText("UNIVERSITY Editorial Office · Center for Research Management");
        contactTitle.setTextColor(UiTheme.INK);
        contactTitle.setTypeface(bold);
        ScreenTypography.apply(contactTitle, 14);
        contactCard.addView(contactTitle);

        TextView contactAddress = new TextView(context);
        contactAddress.setText("8th Floor, Health and Physical Science Building, University of Makati, JP Rizal Extn., West Rembo, Makati City");
        contactAddress.setTextColor(UiTheme.MUTED);
        contactAddress.setTypeface(body);
        ScreenTypography.apply(contactAddress, 13);
        contactAddress.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        contactCard.addView(contactAddress);

        layout.addView(contactCard);

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
        return layout;
    }
}
