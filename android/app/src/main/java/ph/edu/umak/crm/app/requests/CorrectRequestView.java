package ph.edu.umak.crm.app.requests;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class CorrectRequestView {

    public interface Listener {
        void onChooseAttachment(Runnable onChecking, java.util.function.BiConsumer<ph.edu.umak.crm.app.MainActivity.SelectedAttachment, String> result);
        void onSubmitCorrection(RequestData.RequestItem item);
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

        // Initial revised files (3.7e)
        revisedFiles.add(new RequestData.FileItem("INFORMED_CONSENT_FORM_v2.docx", "512 KB", "Attached", false));
        revisedFiles.add(new RequestData.FileItem("DATA_PRIVACY_NOTICE.pdf", "220 KB", "Attached", false));

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        addSpacing(context, layout, 12);

        // Error Banner (initially hidden)
        errorBanner = new TextView(context);
        errorBanner.setTextColor(0xff991b1b);
        errorBanner.setTypeface(bold);
        ScreenTypography.apply(errorBanner, 13);
        errorBanner.setBackground(UiTheme.shape(context, 0xfffee2e2, 8, 0xfffca5a5));
        int bPad = UiTheme.dp(context, 10);
        errorBanner.setPadding(bPad, bPad, bPad, bPad);
        errorBanner.setVisibility(View.GONE);
        layout.addView(errorBanner);

        // 1. Request Summary Card
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
        sService.setTextColor(0xff082d50);
        sService.setTypeface(bold);
        ScreenTypography.apply(sService, 16);
        sCol.addView(sService);

        TextView sMeta = new TextView(context);
        sMeta.setText(requestItem.id + " · Submitted " + requestItem.submittedDate);
        sMeta.setTextColor(0xff586b83);
        sMeta.setTypeface(regular);
        ScreenTypography.apply(sMeta, 13);
        sCol.addView(sMeta);

        sRow.addView(sCol, sColLp);

        TextView statusBadge = new TextView(context);
        statusBadge.setText("For correction");
        statusBadge.setTextColor(0xffb45309);
        statusBadge.setTypeface(bold);
        ScreenTypography.apply(statusBadge, 12);
        statusBadge.setBackground(UiTheme.shape(context, 0xfffef3c7, 4, 0));
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 4);
        statusBadge.setPadding(px, py, px, py);
        sRow.addView(statusBadge);

        summaryCard.addView(sRow);
        layout.addView(summaryCard);

        addSpacing(context, layout, 12);

        // 2. Feedback from CRM Card
        LinearLayout feedbackCard = new LinearLayout(context);
        feedbackCard.setOrientation(LinearLayout.VERTICAL);
        feedbackCard.setBackground(UiTheme.shape(context, 0xfffffbeb, 10, 0xfffde68a));
        feedbackCard.setPadding(pad, pad, pad, pad);

        TextView fbHeader = new TextView(context);
        fbHeader.setText("Feedback from CRM");
        fbHeader.setTextColor(0xffb45309);
        fbHeader.setTypeface(bold);
        ScreenTypography.apply(fbHeader, 15);
        feedbackCard.addView(fbHeader);

        addSpacing(context, feedbackCard, 6);

        TextView fbQuote = new TextView(context);
        fbQuote.setText(requestItem.feedbackFromCrm != null
                ? requestItem.feedbackFromCrm
                : "Please revise Section 3.2 (Consent Form) to specify participant data retention policies.");
        fbQuote.setTextColor(0xff92400e);
        fbQuote.setTypeface(regular);
        ScreenTypography.apply(fbQuote, 13);
        feedbackCard.addView(fbQuote);

        addSpacing(context, feedbackCard, 10);

        TextView missingLabel = new TextView(context);
        missingLabel.setText("Missing items:");
        missingLabel.setTextColor(0xffb45309);
        missingLabel.setTypeface(bold);
        ScreenTypography.apply(missingLabel, 12);
        feedbackCard.addView(missingLabel);

        if (requestItem.missingItems != null) {
            for (String itemStr : requestItem.missingItems) {
                TextView mItem = new TextView(context);
                mItem.setText("• " + itemStr);
                mItem.setTextColor(0xff92400e);
                mItem.setTypeface(regular);
                ScreenTypography.apply(mItem, 12);
                mItem.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 2));
                feedbackCard.addView(mItem);
            }
        }

        addSpacing(context, feedbackCard, 8);

        TextView deadlineNote = new TextView(context);
        deadlineNote.setText("Resubmit by " + (requestItem.deadline != null ? requestItem.deadline : "Nov 4, 2026, 5:00 PM"));
        deadlineNote.setTextColor(0xffdc2626);
        deadlineNote.setTypeface(bold);
        ScreenTypography.apply(deadlineNote, 13);
        feedbackCard.addView(deadlineNote);

        layout.addView(feedbackCard);

        addSpacing(context, layout, 12);

        // 3. Resubmission Form Card
        LinearLayout formCard = new LinearLayout(context);
        formCard.setOrientation(LinearLayout.VERTICAL);
        formCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        formCard.setPadding(pad, pad, pad, pad);

        // Response to feedback input
        TextView respLabel = new TextView(context);
        respLabel.setText("Response to feedback (optional)");
        respLabel.setTextColor(0xff082d50);
        respLabel.setTypeface(bold);
        ScreenTypography.apply(respLabel, 14);
        formCard.addView(respLabel);

        addSpacing(context, formCard, 6);

        responseInput = new EditText(context);
        responseInput.setText("Updated Section 3.2 of the consent form with data retention details and attached signed data privacy notice.");
        responseInput.setTextColor(0xff082d50);
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
        repLabel.setTextColor(0xff082d50);
        repLabel.setTypeface(bold);
        ScreenTypography.apply(repLabel, 14);
        formCard.addView(repLabel);

        addSpacing(context, formCard, 6);

        LinearLayout repBox = new LinearLayout(context);
        repBox.setOrientation(LinearLayout.HORIZONTAL);
        repBox.setGravity(Gravity.CENTER_VERTICAL);
        repBox.setBackground(UiTheme.shape(context, 0xfff8fafc, 6, UiTheme.BORDER));
        int rPad = UiTheme.dp(context, 8);
        repBox.setPadding(rPad, rPad, rPad, rPad);

        ImageView repDocIcon = new ImageView(context);
        repDocIcon.setImageResource(R.drawable.ic_crm_doc);
        repDocIcon.setColorFilter(0xff586b83);
        int icon16 = UiTheme.dp(context, 16);
        repBox.addView(repDocIcon, new LinearLayout.LayoutParams(icon16, icon16));

        TextView repName = new TextView(context);
        repName.setText("INFORMED_CONSENT_FORM.docx (480 KB)");
        repName.setTextColor(0xff586b83);
        repName.setTypeface(regular);
        ScreenTypography.apply(repName, 12);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        rLp.setMarginStart(UiTheme.dp(context, 8));
        repBox.addView(repName, rLp);

        TextView repTag = new TextView(context);
        repTag.setText("Replaced");
        repTag.setTextColor(0xff586b83);
        repTag.setTypeface(bold);
        ScreenTypography.apply(repTag, 11);
        repTag.setBackground(UiTheme.shape(context, 0xffe2e8f0, 4, 0));
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
        revLabel.setTextColor(0xff082d50);
        revLabel.setTypeface(bold);
        ScreenTypography.apply(revLabel, 14);
        LinearLayout.LayoutParams revLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        attachHdrRow.addView(revLabel, revLp);

        revisedCounter = new TextView(context);
        revisedCounter.setTextColor(0xff586b83);
        revisedCounter.setTypeface(regular);
        ScreenTypography.apply(revisedCounter, 12);
        attachHdrRow.addView(revisedCounter);

        formCard.addView(attachHdrRow);

        addSpacing(context, formCard, 8);

        // Attach Button / Zone
        PendingStatus attachmentStatus = new PendingStatus(context);
        Button attachBtn = new Button(context);
        attachBtn.setText("Attach file");
        attachBtn.setTextColor(0xff082d50);
        attachBtn.setTypeface(bold);
        ScreenTypography.apply(attachBtn, 14);
        attachBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1));
        attachBtn.setOnClickListener(v -> {
            if (listener == null || revisedFiles.size() >= 5) return;
            listener.onChooseAttachment(() -> {
                attachBtn.setEnabled(false);
                attachBtn.setText("Checking file…");
                attachmentStatus.working("Checking the selected file…");
            }, (file, error) -> {
                if (!layout.isAttachedToWindow()) return;
                attachBtn.setEnabled(true);
                attachBtn.setText("Attach file");
                attachmentStatus.clear();
                if (error != null) {
                    attachmentStatus.result(error, true);
                    return;
                }
                if (revisedFiles.size() >= 5) return;
                String size = android.text.format.Formatter.formatShortFileSize(context, file.size());
                revisedFiles.add(new RequestData.FileItem(file.name(), size, "Attached", false));
                errorBanner.setVisibility(View.GONE);
                renderRevisedList();
            });
        });
        formCard.addView(attachBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 44)));
        formCard.addView(attachmentStatus);

        addSpacing(context, formCard, 10);

        revisedFilesContainer = new LinearLayout(context);
        revisedFilesContainer.setOrientation(LinearLayout.VERTICAL);
        formCard.addView(revisedFilesContainer);

        addSpacing(context, formCard, 14);

        // Confirmation Checkbox
        confirmBox = new CheckBox(context);
        confirmBox.setText("I confirm that the updated documents address the CRM feedback.");
        confirmBox.setTextColor(0xff082d50);
        confirmBox.setTypeface(regular);
        ScreenTypography.apply(confirmBox, 13);
        confirmBox.setChecked(true);
        formCard.addView(confirmBox);

        layout.addView(formCard);

        addSpacing(context, layout, 20);

        // Submit Button
        Button submitBtn = new Button(context);
        submitBtn.setText("Submit");
        submitBtn.setTextColor(0xffffffff);
        submitBtn.setTypeface(bold);
        ScreenTypography.apply(submitBtn, 15);
        submitBtn.setBackground(UiTheme.shape(context, 0xff131c4b, 8, 0));
        submitBtn.setOnClickListener(v -> handleSubmit());
        layout.addView(submitBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));

        addSpacing(context, layout, 10);

        // Cancel Button
        Button cancelBtn = new Button(context);
        cancelBtn.setText("Cancel");
        cancelBtn.setTextColor(0xff082d50);
        cancelBtn.setTypeface(bold);
        ScreenTypography.apply(cancelBtn, 15);
        cancelBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1));
        cancelBtn.setOnClickListener(v -> {
            if (listener != null) listener.onCancel();
        });
        layout.addView(cancelBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));

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
            row.setBackground(UiTheme.shape(context, 0xfff8fafc, 6, UiTheme.BORDER));
            int pad = UiTheme.dp(context, 8);
            row.setPadding(pad, pad, pad, pad);

            ImageView docIcon = new ImageView(context);
            docIcon.setImageResource(R.drawable.ic_crm_doc);
            docIcon.setColorFilter(0xff586b83);
            int icon16 = UiTheme.dp(context, 16);
            row.addView(docIcon, new LinearLayout.LayoutParams(icon16, icon16));

            TextView name = new TextView(context);
            name.setText(file.name + " (" + file.size + ")");
            name.setTextColor(0xff082d50);
            name.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(name, 12);
            LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            nLp.setMarginStart(UiTheme.dp(context, 8));
            row.addView(name, nLp);

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

            TextView statusBadge = new TextView(context);
            statusBadge.setText(" Attached");
            statusBadge.setTextColor(0xff16a34a);
            statusBadge.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(statusBadge, 11);
            attachedBadge.addView(statusBadge);
            row.addView(attachedBadge);

            TextView removeBtn = new TextView(context);
            removeBtn.setText("×");
            removeBtn.setTextColor(0xff586b83);
            removeBtn.setTypeface(UiTheme.boldFont(context));
            removeBtn.setTextSize(18);
            removeBtn.setPadding(UiTheme.dp(context, 8), 0, UiTheme.dp(context, 4), 0);
            removeBtn.setOnClickListener(v -> {
                revisedFiles.remove(index);
                renderRevisedList();
            });
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
        return PageTitle.wrap(layout, "Correct Request", "Review the feedback and resubmit updated documents.", true);
    }
}
