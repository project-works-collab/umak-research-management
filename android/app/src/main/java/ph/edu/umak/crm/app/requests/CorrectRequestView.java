package ph.edu.umak.crm.app.requests;

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

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class CorrectRequestView {

    public interface Listener {
        void onSubmitCorrection(RequestData.RequestItem updatedItem);
        void onCancel();
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;
    private final RequestData.RequestItem requestItem;

    private final List<RequestData.FileItem> revisedFiles = new ArrayList<>();
    private final LinearLayout revisedFilesContainer;
    private final TextView revisedCounter;
    private final EditText responseInput;
    private final CheckBox confirmBox;
    private final TextView errorBanner;

    public CorrectRequestView(Context context, RequestData.RequestItem item, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.requestItem = item != null ? item : RequestData.createReq089("For correction");

        // Initial revised files
        revisedFiles.add(new RequestData.FileItem("INFORMED_CONSENT_FORM_v2.docx", "512 KB", "Attached", false));
        revisedFiles.add(new RequestData.FileItem("DATA_PRIVACY_NOTICE.pdf", "220 KB", "Attached", false));

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        // Header Row: Cancel on left or Title
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        Button cancelTopBtn = UiButton.text(context, "Cancel", UiTheme.NAVY, () -> {
            if (listener != null) listener.onCancel();
        });
        cancelTopBtn.setPadding(0, 0, UiTheme.dp(context, 12), 0);
        headerRow.addView(cancelTopBtn);

        LinearLayout titleCol = new LinearLayout(context);
        titleCol.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(context);
        title.setText("Correct Request");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 20);
        titleCol.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Review the feedback and resubmit updated documents.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 13);
        titleCol.addView(subtitle);

        headerRow.addView(titleCol);
        layout.addView(headerRow);

        addSpacing(context, layout, 14);

        // Error Banner (initially hidden)
        errorBanner = new TextView(context);
        errorBanner.setTextColor(UiTheme.PEACH_TEXT);
        errorBanner.setTypeface(bold);
        ScreenTypography.apply(errorBanner, 13);
        errorBanner.setBackground(UiTheme.shape(context, UiTheme.PEACH_BG, 8, UiTheme.PEACH_BORDER));
        int bPad = UiTheme.dp(context, 10);
        errorBanner.setPadding(bPad, bPad, bPad, bPad);
        errorBanner.setVisibility(View.GONE);
        layout.addView(errorBanner);

        // Request Summary Card
        LinearLayout summaryCard = new LinearLayout(context);
        summaryCard.setOrientation(LinearLayout.VERTICAL);
        summaryCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        summaryCard.setPadding(pad, pad, pad, pad);

        LinearLayout sRow = new LinearLayout(context);
        sRow.setOrientation(LinearLayout.HORIZONTAL);
        sRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout sCol = new LinearLayout(context);
        sCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams sColLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView sService = new TextView(context);
        sService.setText(requestItem.service);
        sService.setTextColor(UiTheme.NAVY);
        sService.setTypeface(bold);
        ScreenTypography.apply(sService, 16);
        sCol.addView(sService);

        TextView sMeta = new TextView(context);
        sMeta.setText(requestItem.id + " · Submitted " + requestItem.submittedDate);
        sMeta.setTextColor(UiTheme.MUTED);
        sMeta.setTypeface(regular);
        ScreenTypography.apply(sMeta, 13);
        sCol.addView(sMeta);

        sRow.addView(sCol, sColLp);

        TextView statusBadge = new TextView(context);
        statusBadge.setText("For correction");
        statusBadge.setTextColor(UiTheme.STATUS_CORRECTION_TEXT);
        statusBadge.setTypeface(bold);
        ScreenTypography.apply(statusBadge, 12);
        statusBadge.setBackground(UiTheme.shape(context, UiTheme.STATUS_CORRECTION_BG, 4, 0));
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 4);
        statusBadge.setPadding(px, py, px, py);
        sRow.addView(statusBadge);

        summaryCard.addView(sRow);
        layout.addView(summaryCard);

        addSpacing(context, layout, 14);

        // Feedback from CRM Card
        LinearLayout feedbackCard = new LinearLayout(context);
        feedbackCard.setOrientation(LinearLayout.VERTICAL);
        feedbackCard.setBackground(UiTheme.shape(context, UiTheme.AMBER_BG, 10, UiTheme.AMBER_BORDER));
        feedbackCard.setPadding(pad, pad, pad, pad);

        TextView fbHeader = new TextView(context);
        fbHeader.setText("Feedback from CRM");
        fbHeader.setTextColor(UiTheme.AMBER_TEXT);
        fbHeader.setTypeface(bold);
        ScreenTypography.apply(fbHeader, 15);
        feedbackCard.addView(fbHeader);

        addSpacing(context, feedbackCard, 6);

        TextView fbQuote = new TextView(context);
        fbQuote.setText(requestItem.feedbackFromCrm != null
                ? requestItem.feedbackFromCrm
                : "Please revise Section 3.2 (Consent Form) to specify participant data retention policies.");
        fbQuote.setTextColor(UiTheme.AMBER_TEXT);
        fbQuote.setTypeface(regular);
        ScreenTypography.apply(fbQuote, 13);
        feedbackCard.addView(fbQuote);

        addSpacing(context, feedbackCard, 10);

        TextView missingLabel = new TextView(context);
        missingLabel.setText("Missing items:");
        missingLabel.setTextColor(UiTheme.AMBER_TEXT);
        missingLabel.setTypeface(bold);
        ScreenTypography.apply(missingLabel, 12);
        feedbackCard.addView(missingLabel);

        if (requestItem.missingItems != null) {
            for (String itemStr : requestItem.missingItems) {
                TextView mItem = new TextView(context);
                mItem.setText("• " + itemStr);
                mItem.setTextColor(UiTheme.AMBER_TEXT);
                mItem.setTypeface(regular);
                ScreenTypography.apply(mItem, 12);
                mItem.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 2));
                feedbackCard.addView(mItem);
            }
        }

        addSpacing(context, feedbackCard, 8);

        TextView deadlineNote = new TextView(context);
        deadlineNote.setText("Resubmit by " + (requestItem.deadline != null ? requestItem.deadline : "Nov 4, 2026, 5:00 PM"));
        deadlineNote.setTextColor(UiTheme.ERROR_RED);
        deadlineNote.setTypeface(bold);
        ScreenTypography.apply(deadlineNote, 13);
        feedbackCard.addView(deadlineNote);

        layout.addView(feedbackCard);

        addSpacing(context, layout, 14);

        // Resubmission Form Card
        LinearLayout formCard = new LinearLayout(context);
        formCard.setOrientation(LinearLayout.VERTICAL);
        formCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        formCard.setPadding(pad, pad, pad, pad);

        // Response to feedback input
        TextView respLabel = new TextView(context);
        respLabel.setText("Response to feedback (optional)");
        respLabel.setTextColor(UiTheme.NAVY);
        respLabel.setTypeface(bold);
        ScreenTypography.apply(respLabel, 14);
        formCard.addView(respLabel);

        addSpacing(context, formCard, 6);

        responseInput = new EditText(context);
        responseInput.setText("Updated Section 3.2 of the consent form with data retention details and attached signed data privacy notice.");
        responseInput.setTextColor(UiTheme.NAVY);
        responseInput.setTypeface(regular);
        ScreenTypography.apply(responseInput, 13);
        responseInput.setGravity(Gravity.TOP | Gravity.START);
        responseInput.setMinLines(3);
        responseInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int inpPad = UiTheme.dp(context, 10);
        responseInput.setPadding(inpPad, inpPad, inpPad, inpPad);
        formCard.addView(responseInput);

        addSpacing(context, formCard, 14);

        // Replaced file section
        TextView repLabel = new TextView(context);
        repLabel.setText("Replaced file");
        repLabel.setTextColor(UiTheme.NAVY);
        repLabel.setTypeface(bold);
        ScreenTypography.apply(repLabel, 14);
        formCard.addView(repLabel);

        addSpacing(context, formCard, 6);

        LinearLayout repBox = new LinearLayout(context);
        repBox.setOrientation(LinearLayout.HORIZONTAL);
        repBox.setGravity(Gravity.CENTER_VERTICAL);
        repBox.setBackground(UiTheme.shape(context, 0xFFF8FAFC, 6, UiTheme.BORDER));
        int rPad = UiTheme.dp(context, 8);
        repBox.setPadding(rPad, rPad, rPad, rPad);

        TextView repName = new TextView(context);
        repName.setText("INFORMED_CONSENT_FORM.docx (480 KB)");
        repName.setTextColor(UiTheme.MUTED);
        repName.setTypeface(regular);
        ScreenTypography.apply(repName, 12);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        repBox.addView(repName, rLp);

        TextView repTag = new TextView(context);
        repTag.setText("Replaced");
        repTag.setTextColor(UiTheme.MUTED);
        repTag.setTypeface(bold);
        ScreenTypography.apply(repTag, 11);
        repTag.setBackground(UiTheme.shape(context, UiTheme.TAG_BG, 4, 0));
        repTag.setPadding(UiTheme.dp(context, 6), UiTheme.dp(context, 2), UiTheme.dp(context, 6), UiTheme.dp(context, 2));
        repBox.addView(repTag);

        formCard.addView(repBox);

        addSpacing(context, formCard, 14);

        // Revised Attachments header
        LinearLayout attachHdrRow = new LinearLayout(context);
        attachHdrRow.setOrientation(LinearLayout.HORIZONTAL);
        attachHdrRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView revLabel = new TextView(context);
        revLabel.setText("Attached revised files");
        revLabel.setTextColor(UiTheme.NAVY);
        revLabel.setTypeface(bold);
        ScreenTypography.apply(revLabel, 14);
        LinearLayout.LayoutParams revLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        attachHdrRow.addView(revLabel, revLp);

        revisedCounter = new TextView(context);
        revisedCounter.setTextColor(UiTheme.MUTED);
        revisedCounter.setTypeface(regular);
        ScreenTypography.apply(revisedCounter, 12);
        attachHdrRow.addView(revisedCounter);

        formCard.addView(attachHdrRow);

        addSpacing(context, formCard, 8);

        // Attach Button / Zone
        Button attachBtn = UiButton.outline(context, "Attach file", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (revisedFiles.size() < 5) {
                revisedFiles.add(new RequestData.FileItem("REVISED_ADDITIONAL_DOC_" + (revisedFiles.size() + 1) + ".pdf", "320 KB", "Attached", false));
                renderRevisedList();
            }
        });
        formCard.addView(attachBtn);

        addSpacing(context, formCard, 8);

        revisedFilesContainer = new LinearLayout(context);
        revisedFilesContainer.setOrientation(LinearLayout.VERTICAL);
        formCard.addView(revisedFilesContainer);

        addSpacing(context, formCard, 14);

        // Confirmation Checkbox
        confirmBox = new CheckBox(context);
        confirmBox.setText("I confirm that the updated documents address the CRM feedback.");
        confirmBox.setTextColor(UiTheme.NAVY);
        confirmBox.setTypeface(regular);
        ScreenTypography.apply(confirmBox, 13);
        confirmBox.setChecked(true);
        formCard.addView(confirmBox);

        layout.addView(formCard);

        addSpacing(context, layout, 20);

        // Submit Button
        Button submitBtn = UiButton.primary(context, "Submit", this::handleSubmit);
        layout.addView(submitBtn);

        addSpacing(context, layout, 10);

        // Cancel Button
        Button cancelBtn = UiButton.outline(context, "Cancel", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (listener != null) listener.onCancel();
        });
        layout.addView(cancelBtn);

        renderRevisedList();
    }

    private void renderRevisedList() {
        revisedFilesContainer.removeAllViews();
        revisedCounter.setText(revisedFiles.size() + " of 5 files");

        for (int i = 0; i < revisedFiles.size(); i++) {
            final int index = i;
            RequestData.FileItem file = revisedFiles.get(i);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(UiTheme.shape(context, 0xFFF8FAFC, 6, UiTheme.BORDER));
            int pad = UiTheme.dp(context, 8);
            row.setPadding(pad, pad, pad, pad);

            TextView name = new TextView(context);
            name.setText(file.name + " (" + file.size + ")");
            name.setTextColor(UiTheme.NAVY);
            name.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(name, 12);
            LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(name, nLp);

            TextView statusBadge = new TextView(context);
            statusBadge.setText("Attached");
            statusBadge.setTextColor(UiTheme.SUCCESS_GREEN);
            statusBadge.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(statusBadge, 11);
            statusBadge.setBackground(UiTheme.shape(context, UiTheme.SUCCESS_BG, 4, 0));
            statusBadge.setPadding(UiTheme.dp(context, 6), UiTheme.dp(context, 2), UiTheme.dp(context, 6), UiTheme.dp(context, 2));
            row.addView(statusBadge);

            Button removeBtn = UiButton.text(context, "Remove", UiTheme.ERROR_RED, () -> {
                revisedFiles.remove(index);
                renderRevisedList();
            });
            removeBtn.setPadding(UiTheme.dp(context, 8), 0, 0, 0);
            row.addView(removeBtn);

            revisedFilesContainer.addView(row);
            addSpacing(context, revisedFilesContainer, 6);
        }
    }

    private void handleSubmit() {
        if (!confirmBox.isChecked()) {
            errorBanner.setText("Please confirm that the updated documents address the feedback.");
            errorBanner.setVisibility(View.VISIBLE);
            return;
        }
        if (revisedFiles.isEmpty()) {
            errorBanner.setText("Please attach at least one revised document.");
            errorBanner.setVisibility(View.VISIBLE);
            return;
        }

        errorBanner.setVisibility(View.GONE);
        // Resubmitted state
        requestItem.status = "Submitted";
        requestItem.submittedFiles = new ArrayList<>(revisedFiles);
        requestItem.processingStep = "Correction submitted · Nov 3, 2026";
        requestItem.actionRequiredNotice = null;

        if (listener != null) {
            listener.onSubmitCorrection(requestItem);
        }
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
