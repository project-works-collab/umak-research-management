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

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class RequestDetailView {

    public interface Listener {
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

        if (currentItem.previewTitle != null) {
            addSectionHeader(overviewCard, currentItem.previewTitle);
            TextView previewDetails = new TextView(context);
            previewDetails.setText(currentItem.previewDetails);
            previewDetails.setTextColor(UiTheme.NAVY);
            previewDetails.setTypeface(regular);
            ScreenTypography.apply(previewDetails, 13);
            overviewCard.addView(previewDetails);
        }
        layout.addView(overviewCard);

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

            addSpacing(context, layout, 8);
            layout.addView(actionCard);
        }

        LinearLayout detailsCard = addDetailsCard(pad);

        // Processing Step
        addSectionHeader(detailsCard, "Processing step");
        TextView stepVal = new TextView(context);
        stepVal.setText(currentItem.processingStep != null ? currentItem.processingStep : "No processing step recorded");
        stepVal.setTextColor(UiTheme.NAVY);
        stepVal.setTypeface(regular);
        ScreenTypography.apply(stepVal, 13);
        detailsCard.addView(stepVal);

        detailsCard = addDetailsCard(pad);

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

        detailsCard = addDetailsCard(pad);

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

        detailsCard = addDetailsCard(pad);

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

        detailsCard = addDetailsCard(pad);

        // Next Action
        addSectionHeader(detailsCard, "Next action");
        TextView nextVal = new TextView(context);
        nextVal.setText(currentItem.nextAction != null ? currentItem.nextAction : "No next action recorded");
        nextVal.setTextColor(UiTheme.NAVY);
        nextVal.setTypeface(regular);
        ScreenTypography.apply(nextVal, 13);
        detailsCard.addView(nextVal);

        if (currentItem.deadline != null) {
            detailsCard = addDetailsCard(pad);
            addSectionHeader(detailsCard, "Deadline");
            TextView deadlineText = new TextView(context);
            deadlineText.setText(currentItem.deadline);
            deadlineText.setTextColor(UiTheme.ERROR_RED);
            deadlineText.setTypeface(bold);
            ScreenTypography.apply(deadlineText, 13);
            detailsCard.addView(deadlineText);
        }

        detailsCard = addDetailsCard(pad);

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

        // Linked Study Row
        if (currentItem.linkedStudyCode != null) {
            LinearLayout linkedCard = addDetailsCard(pad);

            addSectionHeader(linkedCard, "Linked study");

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

        }
    }

    private LinearLayout addDetailsCard(int padding) {
        addSpacing(context, layout, 8);
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        card.setPadding(padding, padding, padding, padding);
        layout.addView(card);
        return card;
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
        return PageTitle.wrap(layout, "Request Details", "Track your request with CRM.", false);
    }
}
