package ph.edu.umak.crm.app.requests;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
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

        // 1. Overview Card
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
        idText.setTextColor(0xff586b83);
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
        serviceTitle.setTextColor(0xff082d50);
        serviceTitle.setTypeface(bold);
        ScreenTypography.apply(serviceTitle, 18);
        overviewCard.addView(serviceTitle);

        addSpacing(context, overviewCard, 4);

        TextView submittedText = new TextView(context);
        submittedText.setText("Submitted " + currentItem.submittedDate);
        submittedText.setTextColor(0xff586b83);
        submittedText.setTypeface(regular);
        ScreenTypography.apply(submittedText, 13);
        overviewCard.addView(submittedText);

        if (currentItem.previewTitle != null) {
            addSpacing(context, overviewCard, 10);
            addSectionHeader(overviewCard, currentItem.previewTitle);
            TextView previewDetails = new TextView(context);
            previewDetails.setText(currentItem.previewDetails);
            previewDetails.setTextColor(0xff082d50);
            previewDetails.setTypeface(regular);
            ScreenTypography.apply(previewDetails, 13);
            overviewCard.addView(previewDetails);
        }
        layout.addView(overviewCard);

        // 2. Action required card (3.7c For correction)
        if ("For correction".equalsIgnoreCase(currentItem.status)) {
            addSpacing(context, layout, 10);
            LinearLayout actionCard = new LinearLayout(context);
            actionCard.setOrientation(LinearLayout.VERTICAL);
            actionCard.setBackground(UiTheme.shape(context, 0xfffffbeb, 10, 0xfffde68a));
            actionCard.setPadding(pad, pad, pad, pad);

            TextView actionTitle = new TextView(context);
            actionTitle.setText("Action required");
            actionTitle.setTextColor(0xffb45309);
            actionTitle.setTypeface(bold);
            ScreenTypography.apply(actionTitle, 15);
            actionCard.addView(actionTitle);

            addSpacing(context, actionCard, 4);

            TextView actionDesc = new TextView(context);
            actionDesc.setText(currentItem.actionRequiredNotice != null
                    ? currentItem.actionRequiredNotice
                    : "Correct the missing items below and resubmit by Nov 4, 2026, 5:00 PM.");
            actionDesc.setTextColor(0xff92400e);
            actionDesc.setTypeface(regular);
            ScreenTypography.apply(actionDesc, 13);
            actionCard.addView(actionDesc);

            addSpacing(context, actionCard, 12);

            Button correctBtn = new Button(context);
            correctBtn.setText("Add or Replace Attachments");
            correctBtn.setTextColor(0xffffffff);
            correctBtn.setTypeface(bold);
            ScreenTypography.apply(correctBtn, 14);
            correctBtn.setBackground(UiTheme.shape(context, 0xff131c4b, 8, 0));
            correctBtn.setOnClickListener(v -> {
                if (listener != null) listener.onCorrectRequest(currentItem);
            });
            actionCard.addView(correctBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 44)));

            layout.addView(actionCard);
        }

        // 3. Linked Study Card
        if (currentItem.linkedStudyCode != null) {
            LinearLayout linkedCard = addDetailsCard(pad);
            addSectionHeader(linkedCard, "Linked study");

            TextView studyTitle = new TextView(context);
            studyTitle.setText(currentItem.linkedStudyCode + " · " + currentItem.linkedStudyTitle);
            studyTitle.setTextColor(0xff082d50);
            studyTitle.setTypeface(bold);
            ScreenTypography.apply(studyTitle, 14);
            linkedCard.addView(studyTitle);

            if (currentItem.linkedStudyStage != null) {
                addSpacing(context, linkedCard, 6);
                TextView stagePill = new TextView(context);
                stagePill.setText("Stage " + currentItem.linkedStudyStage);
                stagePill.setTextColor(0xff586b83);
                stagePill.setTypeface(regular);
                ScreenTypography.apply(stagePill, 12);
                stagePill.setBackground(UiTheme.shape(context, 0xfff1f5f9, 4, 0));
                int px = UiTheme.dp(context, 6);
                int py = UiTheme.dp(context, 2);
                stagePill.setPadding(px, py, px, py);
                LinearLayout.LayoutParams spLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linkedCard.addView(stagePill, spLp);
            }
        }

        // 4. Processing Step Card
        LinearLayout stepCard = addDetailsCard(pad);
        addSectionHeader(stepCard, "Processing step");
        TextView stepVal = new TextView(context);
        stepVal.setText(currentItem.processingStep != null ? currentItem.processingStep : "No processing step recorded");
        stepVal.setTextColor(0xff082d50);
        stepVal.setTypeface(regular);
        ScreenTypography.apply(stepVal, 13);
        stepCard.addView(stepVal);

        // 5. Submitted Files Card
        LinearLayout filesCard = addDetailsCard(pad);
        addSectionHeader(filesCard, "Submitted files");
        if (currentItem.submittedFiles != null && !currentItem.submittedFiles.isEmpty()) {
            for (RequestData.FileItem file : currentItem.submittedFiles) {
                LinearLayout fileRow = new LinearLayout(context);
                fileRow.setOrientation(LinearLayout.HORIZONTAL);
                fileRow.setGravity(Gravity.CENTER_VERTICAL);
                fileRow.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

                ImageView docIcon = new ImageView(context);
                docIcon.setImageResource(R.drawable.ic_crm_doc);
                docIcon.setColorFilter(0xff586b83);
                int icon16 = UiTheme.dp(context, 16);
                fileRow.addView(docIcon, new LinearLayout.LayoutParams(icon16, icon16));

                TextView fName = new TextView(context);
                fName.setText(file.name + " (" + file.size + ")");
                fName.setTextColor(0xff082d50);
                fName.setTypeface(regular);
                ScreenTypography.apply(fName, 13);
                LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                fLp.setMarginStart(UiTheme.dp(context, 8));
                fileRow.addView(fName, fLp);

                LinearLayout attachedBadge = new LinearLayout(context);
                attachedBadge.setOrientation(LinearLayout.HORIZONTAL);
                attachedBadge.setGravity(Gravity.CENTER_VERTICAL);
                attachedBadge.setBackground(UiTheme.shape(context, 0xffdcfce7, 4, 0));
                int px = UiTheme.dp(context, 6);
                int py = UiTheme.dp(context, 2);
                attachedBadge.setPadding(px, py, px, py);

                ImageView checkIcon = new ImageView(context);
                checkIcon.setImageResource(R.drawable.ic_crm_check_small);
                checkIcon.setColorFilter(0xff16a34a);
                int cSize = UiTheme.dp(context, 12);
                attachedBadge.addView(checkIcon, new LinearLayout.LayoutParams(cSize, cSize));

                TextView fBadge = new TextView(context);
                fBadge.setText(" Attached");
                fBadge.setTextColor(0xff16a34a);
                fBadge.setTypeface(bold);
                ScreenTypography.apply(fBadge, 11);
                attachedBadge.addView(fBadge);

                fileRow.addView(attachedBadge);
                filesCard.addView(fileRow);
            }
        } else {
            TextView noFiles = new TextView(context);
            noFiles.setText("No submitted files");
            noFiles.setTextColor(0xff586b83);
            noFiles.setTypeface(regular);
            ScreenTypography.apply(noFiles, 13);
            filesCard.addView(noFiles);
        }

        // 6. Feedback from CRM Card
        LinearLayout feedbackCard = addDetailsCard(pad);
        addSectionHeader(feedbackCard, "Feedback from CRM");
        TextView feedbackVal = new TextView(context);
        feedbackVal.setText(currentItem.feedbackFromCrm != null ? currentItem.feedbackFromCrm : "No feedback yet");
        feedbackVal.setTextColor(0xff082d50);
        feedbackVal.setTypeface(regular);
        ScreenTypography.apply(feedbackVal, 13);
        if ("For correction".equalsIgnoreCase(currentItem.status) && currentItem.feedbackFromCrm != null) {
            feedbackVal.setBackground(UiTheme.shape(context, 0xfff8fafc, 6, UiTheme.BORDER));
            int fPad = UiTheme.dp(context, 10);
            feedbackVal.setPadding(fPad, fPad, fPad, fPad);
        }
        feedbackCard.addView(feedbackVal);

        // 7. Missing Items Card
        LinearLayout missingCard = addDetailsCard(pad);
        addSectionHeader(missingCard, "Missing items");
        if (currentItem.missingItems != null && !currentItem.missingItems.isEmpty()) {
            for (String missing : currentItem.missingItems) {
                TextView itemText = new TextView(context);
                itemText.setText("• " + missing);
                itemText.setTextColor(0xffb45309);
                itemText.setTypeface(regular);
                ScreenTypography.apply(itemText, 13);
                itemText.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 2));
                missingCard.addView(itemText);
            }
        } else {
            TextView noMissing = new TextView(context);
            noMissing.setText("No missing items");
            noMissing.setTextColor(0xff586b83);
            noMissing.setTypeface(regular);
            ScreenTypography.apply(noMissing, 13);
            missingCard.addView(noMissing);
        }

        // 8. Next Action Card
        LinearLayout nextCard = addDetailsCard(pad);
        addSectionHeader(nextCard, "Next action");
        TextView nextVal = new TextView(context);
        nextVal.setText(currentItem.nextAction != null ? currentItem.nextAction : "No next action recorded");
        nextVal.setTextColor(0xff082d50);
        nextVal.setTypeface(regular);
        ScreenTypography.apply(nextVal, 13);
        nextCard.addView(nextVal);

        if (currentItem.deadline != null) {
            addSpacing(context, nextCard, 6);
            TextView deadlineText = new TextView(context);
            deadlineText.setText("Deadline: " + currentItem.deadline);
            deadlineText.setTextColor(0xffdc2626);
            deadlineText.setTypeface(bold);
            ScreenTypography.apply(deadlineText, 13);
            nextCard.addView(deadlineText);
        }

        // 9. Review History Card
        LinearLayout historyCard = addDetailsCard(pad);
        addSectionHeader(historyCard, "Review history");
        if (currentItem.reviewHistory != null && !currentItem.reviewHistory.isEmpty()) {
            for (String hist : currentItem.reviewHistory) {
                TextView hText = new TextView(context);
                hText.setText("• " + hist);
                hText.setTextColor(0xff586b83);
                hText.setTypeface(regular);
                ScreenTypography.apply(hText, 13);
                hText.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 2));
                historyCard.addView(hText);
            }
        } else {
            TextView noHist = new TextView(context);
            noHist.setText("No earlier reviews");
            noHist.setTextColor(0xff586b83);
            noHist.setTypeface(regular);
            ScreenTypography.apply(noHist, 13);
            historyCard.addView(noHist);
        }
    }

    private LinearLayout addDetailsCard(int padding) {
        addSpacing(context, layout, 10);
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
        hdr.setTextColor(0xff586b83);
        hdr.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(hdr, 12);
        hdr.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        parent.addView(hdr);
    }

    private static TextView createStatusBadge(Context context, String status) {
        TextView badge = new TextView(context);
        badge.setText(status);
        badge.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(badge, 12);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        badge.setPadding(px, py, px, py);

        int bg;
        int fg;
        switch (status) {
            case "In review":
                bg = 0xffede9fe;
                fg = 0xff4338ca;
                break;
            case "For correction":
                bg = 0xfffef3c7;
                fg = 0xffb45309;
                break;
            case "Completed":
                bg = 0xffdcfce7;
                fg = 0xff16a34a;
                break;
            case "Submitted":
            default:
                bg = 0xffe5edf7;
                fg = 0xff185d9d;
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
