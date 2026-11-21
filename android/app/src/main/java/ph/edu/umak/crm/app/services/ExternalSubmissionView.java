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

public final class ExternalSubmissionView {
    private final LinearLayout layout;
    private final String serviceLabel;

    public ExternalSubmissionView(Context context, ServiceData.ServiceItem service, Runnable onBack) {
        serviceLabel = service.name + " · " + service.code;
        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 32));

        // 1. Top Info Banner: Light blue panel with Info icon
        LinearLayout infoPanel = new LinearLayout(context);
        infoPanel.setOrientation(LinearLayout.HORIZONTAL);
        infoPanel.setGravity(Gravity.CENTER_VERTICAL);
        infoPanel.setBackground(UiTheme.shape(context, 0xffeff6ff, 8, 0xffbfdbfe));
        int infoPad = UiTheme.dp(context, 12);
        infoPanel.setPadding(infoPad, infoPad, infoPad, infoPad);

        ImageView infoIcon = new ImageView(context);
        infoIcon.setImageResource(R.drawable.ic_crm_info);
        infoIcon.setColorFilter(0xff185d9d);
        int iconSize = UiTheme.dp(context, 18);
        infoPanel.addView(infoIcon, new LinearLayout.LayoutParams(iconSize, iconSize));

        TextView infoText = new TextView(context);
        infoText.setText("This service is submitted outside the app. No request is created here.");
        infoText.setTextColor(0xff1e40af);
        infoText.setTypeface(body);
        ScreenTypography.apply(infoText, 13);
        LinearLayout.LayoutParams infoTextLp = new LinearLayout.LayoutParams(0, -2, 1);
        infoTextLp.setMarginStart(UiTheme.dp(context, 10));
        infoPanel.addView(infoText, infoTextLp);

        LinearLayout.LayoutParams infoPanelLp = new LinearLayout.LayoutParams(-1, -2);
        infoPanelLp.bottomMargin = UiTheme.dp(context, 14);
        layout.addView(infoPanel, infoPanelLp);

        // 2. Section: Submission channel
        LinearLayout channelCard = createCard(context);
        addCardHeader(context, channelCard, "Submission channel", bold);

        LinearLayout channelRow = new LinearLayout(context);
        channelRow.setOrientation(LinearLayout.HORIZONTAL);
        channelRow.setGravity(Gravity.CENTER_VERTICAL);

        FrameLayout extIconFrame = new FrameLayout(context);
        extIconFrame.setBackground(UiTheme.shape(context, 0xffe5edf7, 6, 0));
        ImageView extIcon = new ImageView(context);
        extIcon.setImageResource(R.drawable.ic_crm_external);
        extIcon.setColorFilter(0xff185d9d);
        int extIconSize = UiTheme.dp(context, 18);
        extIconFrame.addView(extIcon, new FrameLayout.LayoutParams(extIconSize, extIconSize, Gravity.CENTER));
        int frameSize = UiTheme.dp(context, 36);
        channelRow.addView(extIconFrame, new LinearLayout.LayoutParams(frameSize, frameSize));

        LinearLayout channelCol = new LinearLayout(context);
        channelCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, -2, 1);
        colLp.setMarginStart(UiTheme.dp(context, 10));

        TextView channelTitle = new TextView(context);
        channelTitle.setText("UNIVERSITY Editorial Office – email submission");
        channelTitle.setTextColor(0xff082d50);
        channelTitle.setTypeface(bold);
        ScreenTypography.apply(channelTitle, 14);
        channelCol.addView(channelTitle);

        TextView channelEmail = new TextView(context);
        channelEmail.setText(service.contactEmail != null && !service.contactEmail.isEmpty() ? service.contactEmail : "research@umak.edu.ph");
        channelEmail.setTextColor(0xff185d9d);
        channelEmail.setTypeface(body);
        ScreenTypography.apply(channelEmail, 13);
        channelEmail.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        channelCol.addView(channelEmail);

        channelRow.addView(channelCol, colLp);
        channelCard.addView(channelRow);
        layout.addView(channelCard);

        addSpacing(context, layout, 12);

        // 3. Section: Steps (1 to 5 numbered steps)
        LinearLayout stepsCard = createCard(context);
        addCardHeader(context, stepsCard, "Steps", bold);

        String[] steps = {
                "Complete CRM Form 011 – Journal Endorsement Request.",
                "Combine your manuscript and required documents into one email.",
                "Use the subject \"UNIVERSITY Endorsement – <Study title>\".",
                "Send the email to the UNIVERSITY Editorial Office.",
                "Keep the editorial office's reply as your proof of submission."
        };

        for (int i = 0; i < steps.length; i++) {
            LinearLayout stepRow = new LinearLayout(context);
            stepRow.setOrientation(LinearLayout.HORIZONTAL);
            stepRow.setPadding(0, 0, 0, i == steps.length - 1 ? 0 : UiTheme.dp(context, 10));

            TextView num = new TextView(context);
            num.setText(String.valueOf(i + 1));
            num.setTextColor(0xffffffff);
            num.setTypeface(bold);
            ScreenTypography.apply(num, 11);
            num.setBackground(UiTheme.shape(context, 0xffe67e22, 11, 0));
            num.setGravity(Gravity.CENTER);
            int numSize = UiTheme.dp(context, 22);
            LinearLayout.LayoutParams numLp = new LinearLayout.LayoutParams(numSize, numSize);
            numLp.rightMargin = UiTheme.dp(context, 10);
            stepRow.addView(num, numLp);

            TextView stepText = new TextView(context);
            stepText.setText(steps[i]);
            stepText.setTextColor(0xff1f2937);
            stepText.setTypeface(body);
            ScreenTypography.apply(stepText, 13);
            stepRow.addView(stepText, new LinearLayout.LayoutParams(0, -2, 1));

            stepsCard.addView(stepRow);
        }
        layout.addView(stepsCard);

        addSpacing(context, layout, 12);

        // 4. Section: Contact (with building and location pin icons)
        LinearLayout contactCard = createCard(context);
        addCardHeader(context, contactCard, "Contact", bold);

        LinearLayout bldgRow = new LinearLayout(context);
        bldgRow.setOrientation(LinearLayout.HORIZONTAL);
        bldgRow.setGravity(Gravity.CENTER_VERTICAL);
        ImageView bldgIcon = new ImageView(context);
        bldgIcon.setImageResource(R.drawable.ic_crm_building);
        bldgIcon.setColorFilter(0xff586b83);
        bldgRow.addView(bldgIcon, new LinearLayout.LayoutParams(iconSize, iconSize));

        TextView bldgText = new TextView(context);
        bldgText.setText(service.responsibleContact != null ? service.responsibleContact : "UNIVERSITY Editorial Office · Center for Research Management");
        bldgText.setTextColor(0xff082d50);
        bldgText.setTypeface(bold);
        ScreenTypography.apply(bldgText, 13);
        LinearLayout.LayoutParams bldgLp = new LinearLayout.LayoutParams(0, -2, 1);
        bldgLp.setMarginStart(UiTheme.dp(context, 8));
        bldgRow.addView(bldgText, bldgLp);
        contactCard.addView(bldgRow);

        LinearLayout locRow = new LinearLayout(context);
        locRow.setOrientation(LinearLayout.HORIZONTAL);
        locRow.setGravity(Gravity.TOP);
        locRow.setPadding(0, UiTheme.dp(context, 8), 0, 0);
        ImageView locIcon = new ImageView(context);
        locIcon.setImageResource(R.drawable.ic_crm_location);
        locIcon.setColorFilter(0xff586b83);
        locRow.addView(locIcon, new LinearLayout.LayoutParams(iconSize, iconSize));

        TextView locText = new TextView(context);
        locText.setText("8th Floor, Health and Physical Science Building, University of Makati, JP Rizal Extn., West Rembo, Makati City");
        locText.setTextColor(0xff586b83);
        locText.setTypeface(body);
        ScreenTypography.apply(locText, 13);
        locText.setLineSpacing(0, 1.15f);
        LinearLayout.LayoutParams locLp = new LinearLayout.LayoutParams(0, -2, 1);
        locLp.setMarginStart(UiTheme.dp(context, 8));
        locRow.addView(locText, locLp);
        contactCard.addView(locRow);

        layout.addView(contactCard);

        addSpacing(context, layout, 18);

        // 5. Back to Service Button (light blue/gray)
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
        return PageTitle.wrap(layout, "Submission Instructions", serviceLabel, false);
    }
}
