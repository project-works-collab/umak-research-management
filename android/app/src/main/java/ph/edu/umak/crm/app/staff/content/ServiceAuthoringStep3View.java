package ph.edu.umak.crm.app.staff.content;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
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
    private final CheckBox announceCb;
    private final LinearLayout missingCard;

    public ServiceAuthoringStep3View(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

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
            if (listener != null) listener.onBack();
        });
        topHeader.addView(backBtn);
        layout.addView(topHeader);

        addSpacing(context, layout, 4);

        // Institution Header
        TextView institution = new TextView(context);
        institution.setText("Center for Research Management");
        institution.setTextColor(UiTheme.MUTED);
        institution.setTypeface(regular);
        ScreenTypography.apply(institution, 12);
        layout.addView(institution);

        // Screen Title
        TextView screenTitle = new TextView(context);
        screenTitle.setText("Service Authoring");
        screenTitle.setTextColor(UiTheme.NAVY);
        screenTitle.setTypeface(bold);
        ScreenTypography.apply(screenTitle, 22);
        screenTitle.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));
        layout.addView(screenTitle);

        // Subtitle
        TextView subtitle = new TextView(context);
        subtitle.setText("Set visibility, configure role-based access, and publish your research service catalog form.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 13);
        layout.addView(subtitle);

        addSpacing(context, layout, 8);

        // Step indicator
        TextView stepIndicator = new TextView(context);
        stepIndicator.setText("Step 3 of 3: Access & Publishing");
        stepIndicator.setTextColor(UiTheme.NAVY);
        stepIndicator.setTypeface(bold);
        ScreenTypography.apply(stepIndicator, 14);
        layout.addView(stepIndicator);

        addSpacing(context, layout, 16);

        // Access Control Card
        LinearLayout accessCard = new LinearLayout(context);
        accessCard.setOrientation(LinearLayout.VERTICAL);
        accessCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        accessCard.setPadding(pad, pad, pad, pad);

        TextView accHeader = new TextView(context);
        accHeader.setText("Access Control");
        accHeader.setTextColor(UiTheme.NAVY);
        accHeader.setTypeface(bold);
        ScreenTypography.apply(accHeader, 16);
        accessCard.addView(accHeader);

        addSpacing(context, accessCard, 10);

        TextView eligLabel = new TextView(context);
        eligLabel.setText("Applicant eligibility");
        eligLabel.setTextColor(UiTheme.NAVY);
        eligLabel.setTypeface(bold);
        ScreenTypography.apply(eligLabel, 13);
        accessCard.addView(eligLabel);

        addSpacing(context, accessCard, 6);

        studentCb = new CheckBox(context);
        studentCb.setText("Student Researchers");
        studentCb.setChecked(false);
        studentCb.setTextColor(UiTheme.NAVY);
        studentCb.setTypeface(regular);
        accessCard.addView(studentCb);

        facultyCb = new CheckBox(context);
        facultyCb.setText("Faculty Researchers");
        facultyCb.setChecked(false);
        facultyCb.setTextColor(UiTheme.NAVY);
        facultyCb.setTypeface(regular);
        accessCard.addView(facultyCb);

        eligibilityError = new TextView(context);
        eligibilityError.setText("Select at least one applicant category.");
        eligibilityError.setTextColor(UiTheme.ERROR_RED);
        eligibilityError.setTypeface(regular);
        ScreenTypography.apply(eligibilityError, 12);
        eligibilityError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        accessCard.addView(eligibilityError);

        layout.addView(accessCard);

        addSpacing(context, layout, 16);

        // Missing Information Card (Peach background)
        missingCard = new LinearLayout(context);
        missingCard.setOrientation(LinearLayout.VERTICAL);
        missingCard.setBackground(UiTheme.shape(context, UiTheme.PEACH_BG, 10, UiTheme.PEACH_BORDER));
        missingCard.setPadding(pad, pad, pad, pad);

        TextView missTitle = new TextView(context);
        missTitle.setText("Missing information");
        missTitle.setTextColor(UiTheme.PEACH_TEXT);
        missTitle.setTypeface(bold);
        ScreenTypography.apply(missTitle, 15);
        missingCard.addView(missTitle);

        addSpacing(context, missingCard, 8);

        addMissingRow(missingCard, "Purpose", "Step 1 Edit", () -> {
            if (listener != null) listener.onEditStep1();
        });
        addMissingRow(missingCard, "Responsible contact email", "Step 1 Edit", () -> {
            if (listener != null) listener.onEditStep1();
        });
        addMissingRow(missingCard, "Applicant eligibility", "Step 3", null);

        addSpacing(context, missingCard, 8);

        TextView missNote = new TextView(context);
        missNote.setText("Complete the highlighted service information before publishing.");
        missNote.setTextColor(UiTheme.PEACH_TEXT);
        missNote.setTypeface(regular);
        ScreenTypography.apply(missNote, 12);
        missingCard.addView(missNote);

        layout.addView(missingCard);

        addSpacing(context, layout, 16);

        // Launch Status Card
        LinearLayout launchCard = new LinearLayout(context);
        launchCard.setOrientation(LinearLayout.VERTICAL);
        launchCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        launchCard.setPadding(pad, pad, pad, pad);

        TextView launchHeader = new TextView(context);
        launchHeader.setText("Launch status");
        launchHeader.setTextColor(UiTheme.NAVY);
        launchHeader.setTypeface(bold);
        ScreenTypography.apply(launchHeader, 16);
        launchCard.addView(launchHeader);

        addSpacing(context, launchCard, 8);

        TextView statLabel = new TextView(context);
        statLabel.setText("Service status");
        statLabel.setTextColor(UiTheme.MUTED);
        statLabel.setTypeface(regular);
        ScreenTypography.apply(statLabel, 13);
        launchCard.addView(statLabel);

        addSpacing(context, launchCard, 4);

        TextView statBadge = new TextView(context);
        statBadge.setText("Draft");
        statBadge.setTypeface(bold);
        ScreenTypography.apply(statBadge, 12);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        statBadge.setPadding(px, py, px, py);
        statBadge.setBackground(UiTheme.shape(context, UiTheme.AMBER_BG, 4, UiTheme.AMBER_BORDER));
        statBadge.setTextColor(UiTheme.AMBER_TEXT);
        statBadge.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        launchCard.addView(statBadge);

        addSpacing(context, launchCard, 6);

        TextView draftNote = new TextView(context);
        draftNote.setText("Drafts are not visible to researchers or the public. Publish requires confirmation.");
        draftNote.setTextColor(UiTheme.MUTED);
        draftNote.setTypeface(regular);
        ScreenTypography.apply(draftNote, 12);
        launchCard.addView(draftNote);

        addSpacing(context, launchCard, 8);

        TextView savedText = new TextView(context);
        savedText.setText("Draft saved · Oct 29, 2026, 3:12 PM");
        savedText.setTextColor(UiTheme.MUTED);
        savedText.setTypeface(regular);
        ScreenTypography.apply(savedText, 12);
        launchCard.addView(savedText);

        addSpacing(context, launchCard, 12);

        TextView noticeHeader = new TextView(context);
        noticeHeader.setText("Publish Notice (Optional)");
        noticeHeader.setTextColor(UiTheme.NAVY);
        noticeHeader.setTypeface(bold);
        ScreenTypography.apply(noticeHeader, 13);
        launchCard.addView(noticeHeader);

        announceCb = new CheckBox(context);
        announceCb.setText("Announce on the dashboard");
        announceCb.setChecked(false);
        announceCb.setTextColor(UiTheme.NAVY);
        announceCb.setTypeface(regular);
        launchCard.addView(announceCb);

        layout.addView(launchCard);

        addSpacing(context, layout, 24);

        // Actions: Publish Service, Save as Draft, Back
        Button publishBtn = UiButton.primary(context, "Publish Service", () -> {
            if (listener != null) listener.onPublish();
        });
        layout.addView(publishBtn);

        addSpacing(context, layout, 12);

        Button draftBtn = UiButton.outline(context, "Save as Draft", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (listener != null) listener.onSaveDraft();
        });
        layout.addView(draftBtn);

        addSpacing(context, layout, 12);

        Button backActionBtn = UiButton.text(context, "Back", UiTheme.NAVY, () -> {
            if (listener != null) listener.onBack();
        });
        layout.addView(backActionBtn);
    }

    private void addMissingRow(LinearLayout parent, String item, String actionText, Runnable onClick) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));

        TextView iView = new TextView(context);
        iView.setText(item);
        iView.setTextColor(UiTheme.PEACH_TEXT);
        iView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(iView, 13);
        row.addView(iView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        if (onClick != null) {
            Button btn = UiButton.text(context, actionText, UiTheme.PEACH_TEXT, onClick);
            ScreenTypography.apply(btn, 12);
            row.addView(btn);
        } else {
            TextView tView = new TextView(context);
            tView.setText(actionText);
            tView.setTextColor(UiTheme.PEACH_TEXT);
            tView.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(tView, 12);
            row.addView(tView);
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
        } else {
            // error state (default in wireframe 5.5)
            studentCb.setChecked(false);
            facultyCb.setChecked(false);
            eligibilityError.setVisibility(View.VISIBLE);
            missingCard.setVisibility(View.VISIBLE);
        }
    }

    public View getContentLayout() {
        if (layout.getParent() instanceof ViewGroup) {
            ((ViewGroup) layout.getParent()).removeView(layout);
        }
        return layout;
    }
}
