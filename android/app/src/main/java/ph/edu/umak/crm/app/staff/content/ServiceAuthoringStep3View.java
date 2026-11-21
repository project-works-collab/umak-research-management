package ph.edu.umak.crm.app.staff.content;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class ServiceAuthoringStep3View {

    public interface Listener {
        void onBack();
        void onEditStep1();
        void onPublish();
        void onSaveDraft();
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;

    private final CheckBox studentCb;
    private final CheckBox facultyCb;
    private final TextView eligibilityError;
    private final EditText publishNoticeInput;
    private final LinearLayout missingCard;
    private final LinearLayout warningBanner;

    public ServiceAuthoringStep3View(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 24));

        // 1. Top Warning Banner: "Complete the highlighted service information before publishing."
        warningBanner = new LinearLayout(context);
        warningBanner.setOrientation(LinearLayout.HORIZONTAL);
        warningBanner.setGravity(Gravity.CENTER_VERTICAL);
        warningBanner.setBackground(UiTheme.shape(context, 0xfffff7ed, 8, 0xfffed7aa));
        int wPad = UiTheme.dp(context, 12);
        warningBanner.setPadding(wPad, wPad, wPad, wPad);

        TextView warnIcon = new TextView(context);
        warnIcon.setText("⚠");
        warnIcon.setTextColor(0xffc2410c);
        warnIcon.setTypeface(bold);
        ScreenTypography.apply(warnIcon, 14);
        warnIcon.setPadding(0, 0, UiTheme.dp(context, 8), 0);
        warningBanner.addView(warnIcon);

        TextView warnText = new TextView(context);
        warnText.setText("Complete the highlighted service information before publishing.");
        warnText.setTextColor(0xffc2410c);
        warnText.setTypeface(regular);
        ScreenTypography.apply(warnText, 13);
        warningBanner.addView(warnText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        layout.addView(warningBanner);

        addSpacing(context, layout, 14);

        // 2. Missing Information Card (Red border)
        missingCard = new LinearLayout(context);
        missingCard.setOrientation(LinearLayout.VERTICAL);
        missingCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, 0xfffca5a5));
        missingCard.setPadding(pad, pad, pad, pad);

        TextView missTitle = new TextView(context);
        missTitle.setText("Missing information");
        missTitle.setTextColor(0xffb91c1c);
        missTitle.setTypeface(bold);
        ScreenTypography.apply(missTitle, 15);
        missingCard.addView(missTitle);

        addSpacing(context, missingCard, 10);

        addMissingRow(missingCard, "Purpose", "Step 1", "Edit", () -> {
            if (listener != null) listener.onEditStep1();
        });
        addMissingRow(missingCard, "Responsible contact email", "Step 1", "Edit", () -> {
            if (listener != null) listener.onEditStep1();
        });
        addMissingRow(missingCard, "Applicant eligibility", "Step 3", null, null);

        layout.addView(missingCard);

        addSpacing(context, layout, 16);

        // 3. Access Control Card
        LinearLayout accessCard = new LinearLayout(context);
        accessCard.setOrientation(LinearLayout.VERTICAL);
        accessCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        accessCard.setPadding(pad, pad, pad, pad);

        TextView accHeader = new TextView(context);
        accHeader.setText("Access Control");
        accHeader.setTextColor(0xff082d50);
        accHeader.setTypeface(bold);
        ScreenTypography.apply(accHeader, 16);
        accessCard.addView(accHeader);

        addSpacing(context, accessCard, 10);

        TextView eligLabel = new TextView(context);
        eligLabel.setText("Applicant eligibility");
        eligLabel.setTextColor(0xff082d50);
        eligLabel.setTypeface(bold);
        ScreenTypography.apply(eligLabel, 13);
        accessCard.addView(eligLabel);

        addSpacing(context, accessCard, 6);

        studentCb = new CheckBox(context);
        studentCb.setText("Student Researchers");
        studentCb.setChecked(false);
        studentCb.setTextColor(0xff082d50);
        studentCb.setTypeface(regular);
        accessCard.addView(studentCb);

        facultyCb = new CheckBox(context);
        facultyCb.setText("Faculty Researchers");
        facultyCb.setChecked(false);
        facultyCb.setTextColor(0xff082d50);
        facultyCb.setTypeface(regular);
        accessCard.addView(facultyCb);

        addSpacing(context, accessCard, 4);

        eligibilityError = new TextView(context);
        eligibilityError.setText("⚠ Select at least one applicant category.");
        eligibilityError.setTextColor(0xffdc2626);
        eligibilityError.setTypeface(regular);
        ScreenTypography.apply(eligibilityError, 12);
        accessCard.addView(eligibilityError);

        layout.addView(accessCard);

        addSpacing(context, layout, 16);

        // 4. Launch Status Card
        LinearLayout launchCard = new LinearLayout(context);
        launchCard.setOrientation(LinearLayout.VERTICAL);
        launchCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        launchCard.setPadding(pad, pad, pad, pad);

        TextView launchHeader = new TextView(context);
        launchHeader.setText("Launch status");
        launchHeader.setTextColor(0xff082d50);
        launchHeader.setTypeface(bold);
        ScreenTypography.apply(launchHeader, 16);
        launchCard.addView(launchHeader);

        addSpacing(context, launchCard, 8);

        TextView statLabel = new TextView(context);
        statLabel.setText("Service status");
        statLabel.setTextColor(0xff586b83);
        statLabel.setTypeface(regular);
        ScreenTypography.apply(statLabel, 12);
        launchCard.addView(statLabel);

        addSpacing(context, launchCard, 4);

        TextView statBadge = new TextView(context);
        statBadge.setText("Draft");
        statBadge.setTypeface(bold);
        ScreenTypography.apply(statBadge, 11);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        statBadge.setPadding(px, py, px, py);
        statBadge.setBackground(UiTheme.shape(context, 0xfff1f5f9, 4, 0));
        statBadge.setTextColor(0xff475569);
        statBadge.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        launchCard.addView(statBadge);

        addSpacing(context, launchCard, 6);

        TextView draftNote = new TextView(context);
        draftNote.setText("Drafts are not visible to researchers or the public. Publish requires confirmation.");
        draftNote.setTextColor(0xff586b83);
        draftNote.setTypeface(regular);
        ScreenTypography.apply(draftNote, 12);
        launchCard.addView(draftNote);

        addSpacing(context, launchCard, 10);

        TextView savedText = new TextView(context);
        savedText.setText("✓ Draft saved · Oct 29, 2026, 3:12 PM");
        savedText.setTextColor(0xff16a34a);
        savedText.setTypeface(regular);
        ScreenTypography.apply(savedText, 12);
        launchCard.addView(savedText);

        addSpacing(context, launchCard, 14);

        TextView noticeHeader = new TextView(context);
        noticeHeader.setText("Publish Notice (Optional)");
        noticeHeader.setTextColor(0xff082d50);
        noticeHeader.setTypeface(bold);
        ScreenTypography.apply(noticeHeader, 13);
        launchCard.addView(noticeHeader);

        addSpacing(context, launchCard, 6);

        publishNoticeInput = new EditText(context);
        publishNoticeInput.setHint("Announce on the dashboard");
        publishNoticeInput.setHintTextColor(0xff94a3b8);
        publishNoticeInput.setTextColor(0xff082d50);
        publishNoticeInput.setTypeface(regular);
        ScreenTypography.apply(publishNoticeInput, 14);
        int ipH = UiTheme.dp(context, 12);
        int ipV = UiTheme.dp(context, 10);
        publishNoticeInput.setPadding(ipH, ipV, ipH, ipV);
        publishNoticeInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        publishNoticeInput.setSingleLine(true);
        publishNoticeInput.setMinHeight(UiTheme.dp(context, 46));
        launchCard.addView(publishNoticeInput);

        layout.addView(launchCard);

        addSpacing(context, layout, 24);

        // 5. Action Buttons: Publish Service (Yellow), then Back & Save as Draft row
        Button publishBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        publishBtn.setText("Publish Service");
        publishBtn.setAllCaps(false);
        publishBtn.setTextColor(0xff082d50);
        publishBtn.setTypeface(bold);
        ScreenTypography.apply(publishBtn, 15);
        publishBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xfffbbf24, 8, 0), 0x20082d50));
        publishBtn.setMinHeight(UiTheme.dp(context, 46));
        publishBtn.setOnClickListener(v -> {
            if (listener != null) listener.onPublish();
        });
        layout.addView(publishBtn);

        addSpacing(context, layout, 12);

        LinearLayout rowButtons = new LinearLayout(context);
        rowButtons.setOrientation(LinearLayout.HORIZONTAL);

        Button backBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        backBtn.setText("Back");
        backBtn.setAllCaps(false);
        backBtn.setTextColor(0xff082d50);
        backBtn.setTypeface(bold);
        ScreenTypography.apply(backBtn, 14);
        backBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        backBtn.setMinHeight(UiTheme.dp(context, 44));
        backBtn.setOnClickListener(v -> {
            if (listener != null) listener.onBack();
        });
        LinearLayout.LayoutParams backLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        backLp.rightMargin = UiTheme.dp(context, 8);
        rowButtons.addView(backBtn, backLp);

        Button draftBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        draftBtn.setText("Save as Draft");
        draftBtn.setAllCaps(false);
        draftBtn.setTextColor(0xff082d50);
        draftBtn.setTypeface(bold);
        ScreenTypography.apply(draftBtn, 14);
        draftBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        draftBtn.setMinHeight(UiTheme.dp(context, 44));
        draftBtn.setOnClickListener(v -> {
            if (listener != null) listener.onSaveDraft();
        });
        LinearLayout.LayoutParams draftLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        draftLp.leftMargin = UiTheme.dp(context, 8);
        rowButtons.addView(draftBtn, draftLp);

        layout.addView(rowButtons);
    }

    private void addMissingRow(LinearLayout parent, String item, String stepText, String actionText, Runnable onClick) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));

        TextView iView = new TextView(context);
        iView.setText(item);
        iView.setTextColor(0xffc2410c);
        iView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(iView, 13);
        row.addView(iView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView sView = new TextView(context);
        sView.setText(stepText);
        sView.setTextColor(0xff64748b);
        sView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(sView, 13);
        row.addView(sView);

        if (actionText != null && onClick != null) {
            TextView aView = new TextView(context);
            aView.setText(actionText);
            aView.setTextColor(0xff185d9d);
            aView.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(aView, 13);
            aView.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));
            aView.setOnClickListener(v -> onClick.run());
            row.addView(aView);
        }

        parent.addView(row);
    }

    private static void addSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(space);
    }

    public void setPreviewState(String state) {
        if ("draft".equalsIgnoreCase(state) || "5.5_draft".equalsIgnoreCase(state)) {
            studentCb.setChecked(true);
            facultyCb.setChecked(false);
            eligibilityError.setVisibility(View.GONE);
            missingCard.setVisibility(View.GONE);
            warningBanner.setVisibility(View.GONE);
        } else {
            // error state (default in wireframe 5.5)
            studentCb.setChecked(false);
            facultyCb.setChecked(false);
            eligibilityError.setVisibility(View.VISIBLE);
            missingCard.setVisibility(View.VISIBLE);
            warningBanner.setVisibility(View.VISIBLE);
        }
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

        // Heading: Service Authoring
        TextView heading = new TextView(context);
        heading.setText("Service Authoring");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        // Subtitle: Set visibility, configure role-based access, and publish your research service catalog form.
        TextView copy = new TextView(context);
        copy.setText("Set visibility, configure role-based access, and publish your research service catalog form.");
        copy.setTextColor(UiTheme.PALE);
        copy.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(copy, 13);
        band.addView(copy);

        addSpacing(context, band, 14);

        // Step Indicator Title
        TextView stepIndicator = new TextView(context);
        stepIndicator.setText("Step 3 of 3: Access & Publishing");
        stepIndicator.setTextColor(0xffffffff);
        stepIndicator.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(stepIndicator, 13);
        band.addView(stepIndicator);

        addSpacing(context, band, 8);

        // 3-Segment Progress Indicator Line inside the Dark Blue Band (All 3 segments active/white!)
        LinearLayout stepBar = new LinearLayout(context);
        stepBar.setOrientation(LinearLayout.HORIZONTAL);
        stepBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 4)));

        View seg1 = new View(context);
        seg1.setBackground(UiTheme.shape(context, 0xffffffff, 2, 0));
        LinearLayout.LayoutParams seg1Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        seg1Params.rightMargin = UiTheme.dp(context, 6);
        stepBar.addView(seg1, seg1Params);

        View seg2 = new View(context);
        seg2.setBackground(UiTheme.shape(context, 0xffffffff, 2, 0));
        LinearLayout.LayoutParams seg2Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        seg2Params.rightMargin = UiTheme.dp(context, 6);
        stepBar.addView(seg2, seg2Params);

        View seg3 = new View(context);
        seg3.setBackground(UiTheme.shape(context, 0xffffffff, 2, 0));
        LinearLayout.LayoutParams seg3Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        stepBar.addView(seg3, seg3Params);

        band.addView(stepBar);

        root.addView(band);
        root.addView(layout);
        return root;
    }
}
