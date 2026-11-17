package ph.edu.umak.crm.app.requests;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class RequestDetailView {

    public interface Listener {
        void onBack();
        void onCorrectRequest(RequestData.RequestItem item);
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;
    private RequestData.RequestItem currentItem;

    public RequestDetailView(Context context, RequestData.RequestItem item, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.currentItem = item != null ? item : RequestData.createReq089("For correction");

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, pad);

        renderContent();
    }

    public void setPreviewState(String status) {
        this.currentItem = RequestData.createReq089(status);
        renderContent();
    }

    private void renderContent() {
        layout.removeAllViews();

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 14);

        // Header with Back button
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        Button backBtn = UiButton.text(context, "Back", UiTheme.NAVY, () -> {
            if (listener != null) listener.onBack();
        });
        backBtn.setPadding(0, 0, UiTheme.dp(context, 12), 0);
        headerRow.addView(backBtn);

        LinearLayout titleCol = new LinearLayout(context);
        titleCol.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(context);
        title.setText("Request Details");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 20);
        titleCol.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Track your request with CRM.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 13);
        titleCol.addView(subtitle);

        headerRow.addView(titleCol);
        layout.addView(headerRow);

        addSpacing(context, layout, 16);

        // Action required card (3.7c For correction)
        if ("For correction".equalsIgnoreCase(currentItem.status)) {
            LinearLayout actionCard = new LinearLayout(context);
            actionCard.setOrientation(LinearLayout.VERTICAL);
            actionCard.setBackground(UiTheme.shape(context, UiTheme.AMBER_BG, 10, UiTheme.AMBER_BORDER));
            actionCard.setPadding(pad, pad, pad, pad);

            TextView actionTitle = new TextView(context);
            actionTitle.setText("Action required");
            actionTitle.setTextColor(UiTheme.AMBER_TEXT);
            actionTitle.setTypeface(bold);
            ScreenTypography.apply(actionTitle, 15);
            actionCard.addView(actionTitle);

            addSpacing(context, actionCard, 4);

            TextView actionDesc = new TextView(context);
            actionDesc.setText(currentItem.actionRequiredNotice != null
                    ? currentItem.actionRequiredNotice
                    : "Correct the missing items below and resubmit by Nov 4, 2026, 5:00 PM.");
            actionDesc.setTextColor(UiTheme.AMBER_TEXT);
            actionDesc.setTypeface(regular);
            ScreenTypography.apply(actionDesc, 13);
            actionCard.addView(actionDesc);

            addSpacing(context, actionCard, 12);

            Button correctBtn = UiButton.primary(context, "Add or Replace Attachments", () -> {
                if (listener != null) listener.onCorrectRequest(currentItem);
            });
            actionCard.addView(correctBtn);

            layout.addView(actionCard);
            addSpacing(context, layout, 16);
        }

        // Overview Card
        LinearLayout overviewCard = new LinearLayout(context);
        overviewCard.setOrientation(LinearLayout.VERTICAL);
        overviewCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        overviewCard.setPadding(pad, pad, pad, pad);

        // Top Row: ID + Status Badge
        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView idText = new TextView(context);
        idText.setText(currentItem.id);
        idText.setTextColor(UiTheme.MUTED);
        idText.setTypeface(bold);
        ScreenTypography.apply(idText, 14);
        LinearLayout.LayoutParams idLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        topRow.addView(idText, idLp);

        TextView badge = createStatusBadge(context, currentItem.status);
        topRow.addView(badge);
        overviewCard.addView(topRow);

        addSpacing(context, overviewCard, 8);

        TextView serviceTitle = new TextView(context);
        serviceTitle.setText(currentItem.service);
        serviceTitle.setTextColor(UiTheme.NAVY);
        serviceTitle.setTypeface(bold);
        ScreenTypography.apply(serviceTitle, 18);
        overviewCard.addView(serviceTitle);

        addSpacing(context, overviewCard, 4);

        TextView submittedText = new TextView(context);
        submittedText.setText("Submitted " + currentItem.submittedDate);
        submittedText.setTextColor(UiTheme.MUTED);
        submittedText.setTypeface(regular);
        ScreenTypography.apply(submittedText, 13);
        overviewCard.addView(submittedText);

        addSpacing(context, overviewCard, 12);

        // Linked Study Row
        if (currentItem.linkedStudyCode != null) {
            LinearLayout linkedCard = new LinearLayout(context);
            linkedCard.setOrientation(LinearLayout.VERTICAL);
            linkedCard.setBackground(UiTheme.shape(context, 0xFFF8FAFC, 8, UiTheme.BORDER));
            int lPad = UiTheme.dp(context, 10);
            linkedCard.setPadding(lPad, lPad, lPad, lPad);

            TextView linkedLabel = new TextView(context);
            linkedLabel.setText("Linked study");
            linkedLabel.setTextColor(UiTheme.MUTED);
            linkedLabel.setTypeface(regular);
            ScreenTypography.apply(linkedLabel, 11);
            linkedCard.addView(linkedLabel);

            TextView studyTitle = new TextView(context);
            studyTitle.setText(currentItem.linkedStudyCode + " · " + currentItem.linkedStudyTitle);
            studyTitle.setTextColor(UiTheme.NAVY);
            studyTitle.setTypeface(bold);
            ScreenTypography.apply(studyTitle, 13);
            linkedCard.addView(studyTitle);

            if (currentItem.linkedStudyStage != null) {
                addSpacing(context, linkedCard, 4);
                TextView stagePill = new TextView(context);
                stagePill.setText("Stage " + currentItem.linkedStudyStage);
                stagePill.setTextColor(UiTheme.MUTED);
                stagePill.setTypeface(regular);
                ScreenTypography.apply(stagePill, 11);
                linkedCard.addView(stagePill);
            }

            overviewCard.addView(linkedCard);
        }

        layout.addView(overviewCard);

        addSpacing(context, layout, 16);

        // Details & Progress Card
        LinearLayout detailsCard = new LinearLayout(context);
        detailsCard.setOrientation(LinearLayout.VERTICAL);
        detailsCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        detailsCard.setPadding(pad, pad, pad, pad);

        // Processing Step
        addSectionHeader(detailsCard, "Processing step");
        TextView stepVal = new TextView(context);
        stepVal.setText(currentItem.processingStep != null ? currentItem.processingStep : "No processing step recorded");
        stepVal.setTextColor(UiTheme.NAVY);
        stepVal.setTypeface(regular);
        ScreenTypography.apply(stepVal, 13);
        detailsCard.addView(stepVal);

        addSpacing(context, detailsCard, 14);

        // Submitted files
        addSectionHeader(detailsCard, "Submitted files");
        if (currentItem.submittedFiles != null && !currentItem.submittedFiles.isEmpty()) {
            for (RequestData.FileItem file : currentItem.submittedFiles) {
                LinearLayout fileRow = new LinearLayout(context);
                fileRow.setOrientation(LinearLayout.HORIZONTAL);
                fileRow.setGravity(Gravity.CENTER_VERTICAL);
                fileRow.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));

                TextView fName = new TextView(context);
                fName.setText(file.name + " (" + file.size + ")");
                fName.setTextColor(UiTheme.NAVY);
                fName.setTypeface(regular);
                ScreenTypography.apply(fName, 13);
                LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                fileRow.addView(fName, fLp);

                TextView fBadge = new TextView(context);
                fBadge.setText("Attached");
                fBadge.setTextColor(UiTheme.SUCCESS_GREEN);
                fBadge.setTypeface(bold);
                ScreenTypography.apply(fBadge, 11);
                fBadge.setBackground(UiTheme.shape(context, UiTheme.SUCCESS_BG, 4, 0));
                int px = UiTheme.dp(context, 6);
                int py = UiTheme.dp(context, 2);
                fBadge.setPadding(px, py, px, py);
                fileRow.addView(fBadge);

                detailsCard.addView(fileRow);
            }
        } else {
            TextView noFiles = new TextView(context);
            noFiles.setText("No submitted files");
            noFiles.setTextColor(UiTheme.MUTED);
            noFiles.setTypeface(regular);
            ScreenTypography.apply(noFiles, 13);
            detailsCard.addView(noFiles);
        }

        addSpacing(context, detailsCard, 14);

        // Feedback from CRM
        addSectionHeader(detailsCard, "Feedback from CRM");
        TextView feedbackVal = new TextView(context);
        feedbackVal.setText(currentItem.feedbackFromCrm != null ? currentItem.feedbackFromCrm : "No feedback yet");
        feedbackVal.setTextColor(UiTheme.NAVY);
        feedbackVal.setTypeface(regular);
        ScreenTypography.apply(feedbackVal, 13);
        if ("For correction".equalsIgnoreCase(currentItem.status)) {
            feedbackVal.setBackground(UiTheme.shape(context, 0xFFF8FAFC, 6, UiTheme.BORDER));
            int fPad = UiTheme.dp(context, 10);
            feedbackVal.setPadding(fPad, fPad, fPad, fPad);
        }
        detailsCard.addView(feedbackVal);

        addSpacing(context, detailsCard, 14);

        // Missing Items
        addSectionHeader(detailsCard, "Missing items");
        if (currentItem.missingItems != null && !currentItem.missingItems.isEmpty()) {
            for (String missing : currentItem.missingItems) {
                TextView itemText = new TextView(context);
                itemText.setText("• " + missing);
                itemText.setTextColor(UiTheme.PEACH_TEXT);
                itemText.setTypeface(regular);
                ScreenTypography.apply(itemText, 13);
                itemText.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 2));
                detailsCard.addView(itemText);
            }
        } else {
            TextView noMissing = new TextView(context);
            noMissing.setText("No missing items");
            noMissing.setTextColor(UiTheme.MUTED);
            noMissing.setTypeface(regular);
            ScreenTypography.apply(noMissing, 13);
            detailsCard.addView(noMissing);
        }

        addSpacing(context, detailsCard, 14);

        // Next Action
        addSectionHeader(detailsCard, "Next action");
        TextView nextVal = new TextView(context);
        nextVal.setText(currentItem.nextAction != null ? currentItem.nextAction : "No next action recorded");
        nextVal.setTextColor(UiTheme.NAVY);
        nextVal.setTypeface(regular);
        ScreenTypography.apply(nextVal, 13);
        detailsCard.addView(nextVal);

        if (currentItem.deadline != null) {
            addSpacing(context, detailsCard, 4);
            TextView deadlineText = new TextView(context);
            deadlineText.setText("Deadline: " + currentItem.deadline);
            deadlineText.setTextColor(UiTheme.ERROR_RED);
            deadlineText.setTypeface(bold);
            ScreenTypography.apply(deadlineText, 13);
            detailsCard.addView(deadlineText);
        }

        addSpacing(context, detailsCard, 14);

        // Review History
        addSectionHeader(detailsCard, "Review history");
        if (currentItem.reviewHistory != null && !currentItem.reviewHistory.isEmpty()) {
            for (String hist : currentItem.reviewHistory) {
                TextView hText = new TextView(context);
                hText.setText("• " + hist);
                hText.setTextColor(UiTheme.MUTED);
                hText.setTypeface(regular);
                ScreenTypography.apply(hText, 13);
                hText.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 2));
                detailsCard.addView(hText);
            }
        } else {
            TextView noHist = new TextView(context);
            noHist.setText("No earlier reviews");
            noHist.setTextColor(UiTheme.MUTED);
            noHist.setTypeface(regular);
            ScreenTypography.apply(noHist, 13);
            detailsCard.addView(noHist);
        }

        layout.addView(detailsCard);
    }

    private void addSectionHeader(LinearLayout parent, String title) {
        TextView hdr = new TextView(context);
        hdr.setText(title);
        hdr.setTextColor(UiTheme.MUTED);
        hdr.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(hdr, 12);
        hdr.setPadding(0, 0, 0, UiTheme.dp(context, 4));
        parent.addView(hdr);
    }

    private static TextView createStatusBadge(Context context, String status) {
        TextView badge = new TextView(context);
        badge.setText(status);
        badge.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(badge, 12);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 4);
        badge.setPadding(px, py, px, py);

        int bg;
        int fg;
        switch (status) {
            case "In review":
                bg = UiTheme.STATUS_IN_REVIEW_BG;
                fg = UiTheme.STATUS_IN_REVIEW_TEXT;
                break;
            case "For correction":
                bg = UiTheme.STATUS_CORRECTION_BG;
                fg = UiTheme.STATUS_CORRECTION_TEXT;
                break;
            case "Completed":
                bg = UiTheme.STATUS_COMPLETED_BG;
                fg = UiTheme.STATUS_COMPLETED_TEXT;
                break;
            case "Submitted":
            default:
                bg = UiTheme.STATUS_SUBMITTED_BG;
                fg = UiTheme.STATUS_SUBMITTED_TEXT;
                break;
        }

        badge.setBackground(UiTheme.shape(context, bg, 4, 0));
        badge.setTextColor(fg);
        return badge;
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
