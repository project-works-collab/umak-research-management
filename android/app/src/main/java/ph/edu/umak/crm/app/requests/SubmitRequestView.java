package ph.edu.umak.crm.app.requests;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.dialogs.BottomSheet;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class SubmitRequestView {
    public interface Listener {
        void onSubmitSuccess(RequestData.RequestItem request);
        void onBack();
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;

    private final LinearLayout topBanner;
    private final ImageView topBannerIcon;
    private final TextView topBannerText;

    private final Spinner serviceTypeSpinner;
    private final TextView serviceTypeError;
    private final Spinner stageSpinner;
    private final EditText titleInput;
    private final TextView titleError;
    private final Spinner linkedStudySpinner;
    private final TextView linkedStudyError;
    private final EditText detailsInput;
    private final TextView detailsError;

    private final TextView attachmentsCounter;
    private final LinearLayout attachmentsContainer;
    private final LinearLayout attachBox;
    private final TextView attachErrorText;
    private boolean showAttachRetry = false;

    private final CheckBox confirmBox;
    private final TextView confirmError;
    private final Button submitBtn;
    private final TextView pendingStatusText;

    private final List<RequestData.FileItem> currentFiles = new ArrayList<>();
    private final String[] serviceOptions = {
            "Select a CRM service",
            "Ethics Clearance",
            "Research Registration",
            "Research Consultation",
            "Publication Assistance",
            "Statistical Consultation",
            "Clearance Processing"
    };
    private final String[] stageOptions = {
            "Proposal development",
            "Ethics review",
            "Data gathering",
            "Presentation",
            "Publication"
    };
    private final String[] studyOptions = {
            "No linked study",
            "REG-2026-089 · Impact of AI on Student Engagement",
            "Select a linked study"
    };

    public SubmitRequestView(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 32));

        // Card 1: Request Details
        LinearLayout detailsCard = createCard(context);

        TextView cardTitle = new TextView(context);
        cardTitle.setText("Request details");
        cardTitle.setTextColor(0xff082d50);
        cardTitle.setTypeface(bold);
        ScreenTypography.apply(cardTitle, 16);
        detailsCard.addView(cardTitle);

        TextView requiredNote = new TextView(context);
        requiredNote.setText("Fields marked required must be completed before submission.");
        requiredNote.setTextColor(0xff586b83);
        requiredNote.setTypeface(body);
        ScreenTypography.apply(requiredNote, 13);
        requiredNote.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 12));
        detailsCard.addView(requiredNote);

        // Top Amber Error Banner inside Card 1
        topBanner = new LinearLayout(context);
        topBanner.setOrientation(LinearLayout.HORIZONTAL);
        topBanner.setGravity(Gravity.CENTER_VERTICAL);
        topBanner.setBackground(UiTheme.shape(context, 0xfffffbeb, 8, 0xfffde68a));
        int bPad = UiTheme.dp(context, 12);
        topBanner.setPadding(bPad, bPad, bPad, bPad);
        topBanner.setVisibility(View.GONE);

        topBannerIcon = new ImageView(context);
        topBannerIcon.setImageResource(R.drawable.ic_crm_alert);
        topBannerIcon.setColorFilter(0xffd97706);
        int icon18 = UiTheme.dp(context, 18);
        topBanner.addView(topBannerIcon, new LinearLayout.LayoutParams(icon18, icon18));

        topBannerText = new TextView(context);
        topBannerText.setTextColor(0xff92400e);
        topBannerText.setTypeface(body);
        ScreenTypography.apply(topBannerText, 13);
        LinearLayout.LayoutParams tbLp = new LinearLayout.LayoutParams(0, -2, 1);
        tbLp.setMarginStart(UiTheme.dp(context, 10));
        topBanner.addView(topBannerText, tbLp);

        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(-1, -2);
        bannerParams.bottomMargin = UiTheme.dp(context, 14);
        detailsCard.addView(topBanner, bannerParams);

        // Field 1: Service type *
        addLabel(context, detailsCard, "Service type *", bold);
        serviceTypeSpinner = createSpinner(context, serviceOptions);
        serviceTypeSpinner.setSelection(1); // Default: Ethics Clearance
        detailsCard.addView(serviceTypeSpinner);
        serviceTypeError = addFieldError(context, detailsCard);

        addSpacing(context, detailsCard, 14);

        // Field 2: Research stage
        addLabel(context, detailsCard, "Research stage", bold);
        stageSpinner = createSpinner(context, stageOptions);
        stageSpinner.setSelection(0); // Default: Proposal development
        detailsCard.addView(stageSpinner);

        addSpacing(context, detailsCard, 14);

        // Field 3: Study title *
        addLabel(context, detailsCard, "Study title *", bold);
        titleInput = createInput(context, "Impact of AI on Student Engagement", body);
        detailsCard.addView(titleInput);
        titleError = addFieldError(context, detailsCard);

        addSpacing(context, detailsCard, 14);

        // Field 4: Linked study (optional)
        addLabel(context, detailsCard, "Linked study (optional)", bold);
        linkedStudySpinner = createSpinner(context, studyOptions);
        linkedStudySpinner.setSelection(1); // Default: REG-2026-089
        detailsCard.addView(linkedStudySpinner);

        TextView linkedHelper = new TextView(context);
        linkedHelper.setText("Only studies you are linked to are listed.");
        linkedHelper.setTextColor(0xff586b83);
        linkedHelper.setTypeface(body);
        ScreenTypography.apply(linkedHelper, 12);
        linkedHelper.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        detailsCard.addView(linkedHelper);
        linkedStudyError = addFieldError(context, detailsCard);

        addSpacing(context, detailsCard, 14);

        // Field 5: Request details *
        addLabel(context, detailsCard, "Request details *", bold);
        detailsInput = createInput(context, "Please review the consent form and research protocol before data collection starts on Nov 15, 2026.", body);
        detailsInput.setSingleLine(false);
        detailsInput.setMinLines(3);
        detailsInput.setGravity(Gravity.TOP | Gravity.START);
        detailsCard.addView(detailsInput);

        TextView detailsHelper = new TextView(context);
        detailsHelper.setText("Include specific questions, deadlines, or documents CRM should review.");
        detailsHelper.setTextColor(0xff586b83);
        detailsHelper.setTypeface(body);
        ScreenTypography.apply(detailsHelper, 12);
        detailsHelper.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        detailsCard.addView(detailsHelper);
        detailsError = addFieldError(context, detailsCard);

        layout.addView(detailsCard);
        addSpacing(context, layout, 16);

        // Card 2: Attachments
        LinearLayout attachCard = createCard(context);

        LinearLayout attachHeaderRow = new LinearLayout(context);
        attachHeaderRow.setOrientation(LinearLayout.HORIZONTAL);
        attachHeaderRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView attachHeader = new TextView(context);
        attachHeader.setText("Attachments");
        attachHeader.setTextColor(0xff082d50);
        attachHeader.setTypeface(bold);
        ScreenTypography.apply(attachHeader, 16);
        attachHeaderRow.addView(attachHeader, new LinearLayout.LayoutParams(0, -2, 1));

        attachmentsCounter = new TextView(context);
        attachmentsCounter.setText("2 of 5 files");
        attachmentsCounter.setTextColor(0xff586b83);
        attachmentsCounter.setTypeface(bold);
        ScreenTypography.apply(attachmentsCounter, 12);
        attachHeaderRow.addView(attachmentsCounter);

        attachCard.addView(attachHeaderRow);

        addSpacing(context, attachCard, 12);

        // Upload Box
        attachBox = new LinearLayout(context);
        attachBox.setOrientation(LinearLayout.VERTICAL);
        attachBox.setGravity(Gravity.CENTER);
        attachBox.setBackground(UiTheme.shape(context, 0xfff8fafc, 8, UiTheme.BORDER));
        int boxPad = UiTheme.dp(context, 16);
        attachBox.setPadding(boxPad, boxPad, boxPad, boxPad);
        attachBox.setClickable(true);
        attachBox.setFocusable(true);

        ImageView uploadIcon = new ImageView(context);
        uploadIcon.setImageResource(R.drawable.ic_crm_upload);
        uploadIcon.setColorFilter(0xff185d9d);
        int upSize = UiTheme.dp(context, 22);
        attachBox.addView(uploadIcon, new LinearLayout.LayoutParams(upSize, upSize));

        TextView attachBoxTitle = new TextView(context);
        attachBoxTitle.setText("Attach file");
        attachBoxTitle.setTextColor(0xff082d50);
        attachBoxTitle.setTypeface(bold);
        ScreenTypography.apply(attachBoxTitle, 14);
        attachBoxTitle.setPadding(0, UiTheme.dp(context, 6), 0, 0);
        attachBox.addView(attachBoxTitle);

        TextView attachBoxSub = new TextView(context);
        attachBoxSub.setText("PDF or DOCX, up to 10 MB each, up to 5 files");
        attachBoxSub.setTextColor(0xff586b83);
        attachBoxSub.setTypeface(body);
        ScreenTypography.apply(attachBoxSub, 12);
        attachBoxSub.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        attachBox.addView(attachBoxSub);

        attachCard.addView(attachBox);

        addSpacing(context, attachCard, 12);

        // Files container
        attachmentsContainer = new LinearLayout(context);
        attachmentsContainer.setOrientation(LinearLayout.VERTICAL);
        attachCard.addView(attachmentsContainer);

        // Attachments global error text
        attachErrorText = new TextView(context);
        attachErrorText.setTextColor(0xffdc2626);
        attachErrorText.setTypeface(body);
        ScreenTypography.apply(attachErrorText, 12);
        attachErrorText.setPadding(0, UiTheme.dp(context, 6), 0, 0);
        attachErrorText.setVisibility(View.GONE);
        attachCard.addView(attachErrorText);

        layout.addView(attachCard);
        addSpacing(context, layout, 16);

        // Confirmation Checkbox
        confirmBox = new CheckBox(context);
        confirmBox.setText("I confirm that the information provided is complete and accurate.");
        confirmBox.setTextColor(0xff1f2937);
        confirmBox.setTypeface(body);
        ScreenTypography.apply(confirmBox, 13);
        confirmBox.setChecked(true);
        layout.addView(confirmBox);

        confirmError = addFieldError(context, layout);

        TextView ackNote = new TextView(context);
        ackNote.setText("Requests are usually acknowledged within two working days.");
        ackNote.setTextColor(0xff586b83);
        ackNote.setTypeface(body);
        ScreenTypography.apply(ackNote, 12);
        ackNote.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 14));
        layout.addView(ackNote);

        // Submit Button & Pending state message
        pendingStatusText = new TextView(context);
        pendingStatusText.setText("Sending your request and files to CRM…");
        pendingStatusText.setTextColor(0xff586b83);
        pendingStatusText.setTypeface(body);
        ScreenTypography.apply(pendingStatusText, 13);
        pendingStatusText.setGravity(Gravity.CENTER);
        pendingStatusText.setPadding(0, 0, 0, UiTheme.dp(context, 8));
        pendingStatusText.setVisibility(View.GONE);
        layout.addView(pendingStatusText);

        submitBtn = new Button(context);
        submitBtn.setText("Submit");
        submitBtn.setTextColor(0xffffffff);
        submitBtn.setTypeface(bold);
        ScreenTypography.apply(submitBtn, 15);
        submitBtn.setBackground(UiTheme.shape(context, 0xff131c4b, 8, 0));
        submitBtn.setHeight(UiTheme.dp(context, 46));
        submitBtn.setOnClickListener(v -> handleSubmit());
        layout.addView(submitBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));

        // Initial default files (3.1)
        currentFiles.add(new RequestData.FileItem("RESEARCH_PROTOCOL_v2.pdf", "2.4 MB", "Attached", false));
        currentFiles.add(new RequestData.FileItem("INFORMED_CONSENT_FORM.docx", "480 KB", "Attached", false));
        renderFileList();
    }

    private void renderFileList() {
        attachmentsContainer.removeAllViews();
        int attachedCount = 0;
        for (int i = 0; i < currentFiles.size(); i++) {
            final int index = i;
            RequestData.FileItem file = currentFiles.get(i);
            if (!file.isError) attachedCount++;

            LinearLayout itemWrapper = new LinearLayout(context);
            itemWrapper.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(-1, -2);
            wrapParams.bottomMargin = UiTheme.dp(context, 8);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            if (file.isError) {
                row.setBackground(UiTheme.shape(context, 0xfffffbeb, 8, 0xfffca5a5));
            } else {
                row.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
            }
            int pad = UiTheme.dp(context, 10);
            row.setPadding(pad, pad, pad, pad);

            // Document Icon
            ImageView docIcon = new ImageView(context);
            docIcon.setImageResource(R.drawable.ic_crm_doc);
            docIcon.setColorFilter(file.isError ? 0xffdc2626 : 0xff586b83);
            int icon18 = UiTheme.dp(context, 18);
            row.addView(docIcon, new LinearLayout.LayoutParams(icon18, icon18));

            // Name + Size/Status Column
            LinearLayout info = new LinearLayout(context);
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, -2, 1);
            infoLp.setMarginStart(UiTheme.dp(context, 10));

            TextView name = new TextView(context);
            name.setText(file.name);
            name.setTextColor(file.isError ? 0xffb91c1c : 0xff082d50);
            name.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(name, 13);
            info.addView(name);

            LinearLayout statusRow = new LinearLayout(context);
            statusRow.setOrientation(LinearLayout.HORIZONTAL);
            statusRow.setGravity(Gravity.CENTER_VERTICAL);
            statusRow.setPadding(0, UiTheme.dp(context, 2), 0, 0);

            TextView size = new TextView(context);
            size.setText(file.size);
            size.setTextColor(0xff586b83);
            size.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(size, 12);
            statusRow.addView(size);

            if (file.isError) {
                TextView notAttached = new TextView(context);
                notAttached.setText(" · Not attached");
                notAttached.setTextColor(0xffdc2626);
                notAttached.setTypeface(UiTheme.boldFont(context));
                ScreenTypography.apply(notAttached, 12);
                statusRow.addView(notAttached);
            } else {
                ImageView check = new ImageView(context);
                check.setImageResource(R.drawable.ic_crm_check_small);
                check.setColorFilter(0xff16a34a);
                int checkSize = UiTheme.dp(context, 14);
                LinearLayout.LayoutParams checkLp = new LinearLayout.LayoutParams(checkSize, checkSize);
                checkLp.setMarginStart(UiTheme.dp(context, 8));
                statusRow.addView(check, checkLp);

                TextView attached = new TextView(context);
                attached.setText("Attached");
                attached.setTextColor(0xff16a34a);
                attached.setTypeface(UiTheme.boldFont(context));
                ScreenTypography.apply(attached, 12);
                LinearLayout.LayoutParams attLp = new LinearLayout.LayoutParams(-2, -2);
                attLp.setMarginStart(UiTheme.dp(context, 3));
                statusRow.addView(attached, attLp);
            }
            info.addView(statusRow);
            row.addView(info, infoLp);

            // Remove Button
            TextView removeBtn = new TextView(context);
            removeBtn.setText("×");
            removeBtn.setTextColor(0xff586b83);
            removeBtn.setTypeface(UiTheme.boldFont(context));
            removeBtn.setTextSize(18);
            removeBtn.setPadding(UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6), 0);
            removeBtn.setOnClickListener(v -> {
                currentFiles.remove(index);
                renderFileList();
            });
            row.addView(removeBtn);

            itemWrapper.addView(row);

            // Error subtext directly below the item card if this file has an error
            if (file.isError && attachErrorText.getVisibility() == View.VISIBLE) {
                TextView fileErr = new TextView(context);
                fileErr.setText(attachErrorText.getText());
                fileErr.setTextColor(0xffdc2626);
                fileErr.setTypeface(UiTheme.bodyFont(context));
                ScreenTypography.apply(fileErr, 12);
                fileErr.setPadding(0, UiTheme.dp(context, 4), 0, 0);
                itemWrapper.addView(fileErr);

                if (showAttachRetry) {
                    Button retryFileBtn = new Button(context);
                    retryFileBtn.setText("↻  Retry");
                    retryFileBtn.setTextColor(0xff082d50);
                    retryFileBtn.setTypeface(UiTheme.boldFont(context));
                    ScreenTypography.apply(retryFileBtn, 13);
                    retryFileBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, 0xffcbd5e1));
                    int padH = UiTheme.dp(context, 14);
                    int padV = UiTheme.dp(context, 6);
                    retryFileBtn.setPadding(padH, padV, padH, padV);
                    LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, UiTheme.dp(context, 36));
                    rLp.topMargin = UiTheme.dp(context, 6);
                    retryFileBtn.setLayoutParams(rLp);
                    retryFileBtn.setOnClickListener(v -> {
                        showAttachRetry = false;
                        attachErrorText.setVisibility(View.GONE);
                        currentFiles.set(index, new RequestData.FileItem(file.name, file.size, "Attached", false));
                        renderFileList();
                    });
                    itemWrapper.addView(retryFileBtn);
                }
            }

            attachmentsContainer.addView(itemWrapper, wrapParams);
        }
        attachmentsCounter.setText(attachedCount + " of 5 files");
        attachBox.setEnabled(attachedCount < 5);
        attachBox.setAlpha(attachedCount >= 5 ? 0.5f : 1.0f);
    }

    private void setInputsEnabled(boolean enabled) {
        serviceTypeSpinner.setEnabled(enabled);
        serviceTypeSpinner.setBackground(UiTheme.shape(context, enabled ? UiTheme.WHITE : 0xfff1f5f9, 8, UiTheme.BORDER));
        stageSpinner.setEnabled(enabled);
        stageSpinner.setBackground(UiTheme.shape(context, enabled ? UiTheme.WHITE : 0xfff1f5f9, 8, UiTheme.BORDER));
        titleInput.setEnabled(enabled);
        titleInput.setBackground(UiTheme.shape(context, enabled ? UiTheme.WHITE : 0xfff1f5f9, 8, UiTheme.BORDER));
        linkedStudySpinner.setEnabled(enabled);
        linkedStudySpinner.setBackground(UiTheme.shape(context, enabled ? UiTheme.WHITE : 0xfff1f5f9, 8, UiTheme.BORDER));
        detailsInput.setEnabled(enabled);
        detailsInput.setBackground(UiTheme.shape(context, enabled ? UiTheme.WHITE : 0xfff1f5f9, 8, UiTheme.BORDER));
        attachBox.setEnabled(enabled);
        attachBox.setAlpha(enabled ? 1.0f : 0.5f);
        confirmBox.setEnabled(enabled);
    }

    private void handleSubmit() {
        if (serviceTypeSpinner.getSelectedItemPosition() == 0) {
            configurePreviewState("3.2a");
            return;
        }
        if (titleInput.getText().toString().trim().length() < 10) {
            configurePreviewState("3.2b");
            return;
        }
        if (!confirmBox.isChecked()) {
            confirmError.setVisibility(View.VISIBLE);
            return;
        }
        if (listener != null) {
            listener.onSubmitSuccess(RequestData.previewSubmission(
                    String.valueOf(serviceTypeSpinner.getSelectedItem()), titleInput.getText().toString().trim(),
                    detailsInput.getText().toString().trim(), currentFiles));
        }
    }

    public void setPreviewState(String state) {
        configurePreviewState(state);
    }

    public void configurePreviewState(String state) {
        clearErrors();
        if ("3.1".equals(state)) {
            serviceTypeSpinner.setSelection(1); // Ethics Clearance
            stageSpinner.setSelection(0);
            titleInput.setText("Impact of AI on Student Engagement");
            linkedStudySpinner.setSelection(1);
            detailsInput.setText("Please review the consent form and research protocol before data collection starts on Nov 15, 2026.");
            confirmBox.setChecked(true);
            currentFiles.clear();
            currentFiles.add(new RequestData.FileItem("RESEARCH_PROTOCOL_v2.pdf", "2.4 MB", "Attached", false));
            currentFiles.add(new RequestData.FileItem("INFORMED_CONSENT_FORM.docx", "480 KB", "Attached", false));
            renderFileList();
        } else if ("3.1-linked".equals(state)) {
            configurePreviewState("3.1");
            showLinkedStudySelectionSheet();
        } else if ("3.2a".equals(state)) {
            // Error required fields
            showTopBanner("Complete the highlighted required fields.");
            serviceTypeSpinner.setSelection(0);
            serviceTypeSpinner.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            serviceTypeError.setVisibility(View.VISIBLE);

            titleInput.setText("");
            titleInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            titleError.setText("⚠ Required");
            titleError.setVisibility(View.VISIBLE);

            detailsInput.setText("");
            detailsInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            detailsError.setText("⚠ Required");
            detailsError.setVisibility(View.VISIBLE);

            confirmBox.setChecked(false);
            confirmError.setText("⚠ Required");
            confirmError.setVisibility(View.VISIBLE);
        } else if ("3.2b".equals(state)) {
            // Error invalid information
            showTopBanner("Correct the highlighted information.");
            serviceTypeSpinner.setSelection(1);
            titleInput.setText("AI");
            titleInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            titleError.setText("⚠ Enter the complete research title (at least 10 characters).");
            titleError.setVisibility(View.VISIBLE);
        } else if ("3.2c".equals(state)) {
            // Error unauthorized study
            linkedStudySpinner.setSelection(2); // Select a linked study
            linkedStudySpinner.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            linkedStudyError.setText("⚠ Select a study you are linked to.");
            linkedStudyError.setVisibility(View.VISIBLE);
        } else if ("3.3a".equals(state)) {
            // File type error
            configurePreviewState("3.1");
            attachErrorText.setText("⚠ Upload a PDF or DOCX file.");
            attachErrorText.setVisibility(View.VISIBLE);
            currentFiles.add(new RequestData.FileItem("photo.png", "1.2 MB", "Not attached", true));
            renderFileList();
        } else if ("3.3b".equals(state)) {
            // File too large error
            configurePreviewState("3.1");
            attachErrorText.setText("⚠ Each file must be 10 MB or smaller.");
            attachErrorText.setVisibility(View.VISIBLE);
            currentFiles.add(new RequestData.FileItem("large.pdf", "11 MB", "Not attached", true));
            renderFileList();
        } else if ("3.3c".equals(state)) {
            // Sixth file error
            currentFiles.clear();
            currentFiles.add(new RequestData.FileItem("RESEARCH_PROTOCOL_v2.pdf", "2.4 MB", "Attached", false));
            currentFiles.add(new RequestData.FileItem("INFORMED_CONSENT_FORM.docx", "480 KB", "Attached", false));
            currentFiles.add(new RequestData.FileItem("DATA_COLLECTION_TOOL.docx", "310 KB", "Attached", false));
            currentFiles.add(new RequestData.FileItem("ADVISER_ENDORSEMENT.pdf", "820 KB", "Attached", false));
            currentFiles.add(new RequestData.FileItem("ETHICS_FORM_004.pdf", "1.8 MB", "Attached", false));
            currentFiles.add(new RequestData.FileItem("SURVEY_RESULTS.pdf", "640 KB", "Not attached", true));
            attachErrorText.setText("⚠ You can attach up to 5 files.");
            attachErrorText.setVisibility(View.VISIBLE);
            renderFileList();
        } else if ("3.3d".equals(state)) {
            // Upload failed with retry
            configurePreviewState("3.1");
            attachErrorText.setText("⚠ Upload failed. Try again.");
            attachErrorText.setVisibility(View.VISIBLE);
            showAttachRetry = true;
            currentFiles.add(new RequestData.FileItem("ETHICS_FORM_004.pdf", "1.8 MB", "Not attached", true));
            renderFileList();
        } else if ("3.4".equals(state)) {
            // Pending state
            configurePreviewState("3.1");
            setInputsEnabled(false);
            submitBtn.setText("Submitting…");
            submitBtn.setEnabled(false);
            submitBtn.setBackground(UiTheme.shape(context, 0xffcbd5e1, 8, 0));
            pendingStatusText.setVisibility(View.VISIBLE);
        } else if ("3.4b".equals(state)) {
            // Unable to connect sheet
            configurePreviewState("3.1");
            showConnectionFailure("Unable to connect. Try again", "Check your connection and retry. Your entered information is still here.");
        }
    }

    private void showLinkedStudySelectionSheet() {
        Dialog dialog = new Dialog(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackground(UiTheme.shape(context, UiTheme.WHITE, 12, 0));
        int pad = UiTheme.dp(context, 16);
        content.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(context);
        title.setText("Select a study you are linked to");
        title.setTextColor(0xff082d50);
        title.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(title, 14);
        title.setPadding(0, 0, 0, UiTheme.dp(context, 12));
        content.addView(title);

        // Option 1: No linked study
        TextView opt1 = new TextView(context);
        opt1.setText("No linked study");
        opt1.setTextColor(0xff1f2937);
        opt1.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(opt1, 13);
        int itemPad = UiTheme.dp(context, 12);
        opt1.setPadding(itemPad, itemPad, itemPad, itemPad);
        opt1.setOnClickListener(v -> {
            linkedStudySpinner.setSelection(0);
            dialog.dismiss();
        });
        content.addView(opt1);

        // Option 2: Active REG-2026-089 item with light blue background and checkmark
        LinearLayout opt2 = new LinearLayout(context);
        opt2.setOrientation(LinearLayout.HORIZONTAL);
        opt2.setGravity(Gravity.CENTER_VERTICAL);
        opt2.setBackground(UiTheme.shape(context, 0xffe5edf7, 6, 0));
        opt2.setPadding(itemPad, itemPad, itemPad, itemPad);

        ImageView check = new ImageView(context);
        check.setImageResource(R.drawable.ic_crm_check_small);
        check.setColorFilter(0xff185d9d);
        int chSize = UiTheme.dp(context, 16);
        opt2.addView(check, new LinearLayout.LayoutParams(chSize, chSize));

        TextView opt2Text = new TextView(context);
        opt2Text.setText("REG-2026-089 · Impact of AI on Student Engagement");
        opt2Text.setTextColor(0xff185d9d);
        opt2Text.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(opt2Text, 13);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, -2, 1);
        tLp.setMarginStart(UiTheme.dp(context, 8));
        opt2.addView(opt2Text, tLp);

        opt2.setOnClickListener(v -> {
            linkedStudySpinner.setSelection(1);
            dialog.dismiss();
        });
        content.addView(opt2);

        BottomSheet.show(dialog, content);
    }

    private void showTopBanner(String message) {
        topBannerText.setText(message);
        topBanner.setVisibility(View.VISIBLE);
    }

    private void showConnectionFailure(String title, String description) {
        Dialog dialog = new Dialog(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = UiTheme.dp(context, 20);
        content.setPadding(padding, padding, padding, padding);
        content.setBackground(UiTheme.shape(context, UiTheme.WHITE, 16, 0));

        FrameLayout iconContainer = new FrameLayout(context);
        int iconBoxSize = UiTheme.dp(context, 44);
        iconContainer.setBackground(UiTheme.shape(context, 0xfff1f5f9, iconBoxSize / 2, 0));
        ImageView wifiOff = new ImageView(context);
        wifiOff.setImageResource(R.drawable.ic_crm_wifi_off);
        wifiOff.setColorFilter(0xff586b83);
        int iconSize = UiTheme.dp(context, 24);
        FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(iconSize, iconSize, Gravity.CENTER);
        iconContainer.addView(wifiOff, iconLp);

        LinearLayout.LayoutParams containerLp = new LinearLayout.LayoutParams(iconBoxSize, iconBoxSize);
        containerLp.bottomMargin = UiTheme.dp(context, 14);
        content.addView(iconContainer, containerLp);

        TextView heading = new TextView(context);
        heading.setText(title);
        heading.setTextColor(0xff082d50);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 20);
        ScreenTypography.heading(heading);
        content.addView(heading);

        TextView copy = new TextView(context);
        copy.setText(description);
        copy.setTextColor(0xff586b83);
        copy.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(copy, 13);
        copy.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 20));
        content.addView(copy);

        Button retryBtn = new Button(context);
        retryBtn.setText("↻  Retry");
        retryBtn.setTextColor(0xffffffff);
        retryBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(retryBtn, 14);
        retryBtn.setBackground(UiTheme.shape(context, 0xff131c4b, 8, 0));
        retryBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onSubmitSuccess(RequestData.previewSubmission(
                        String.valueOf(serviceTypeSpinner.getSelectedItem()),
                        titleInput.getText().toString().trim(),
                        detailsInput.getText().toString().trim(),
                        currentFiles));
            }
        });
        content.addView(retryBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));

        BottomSheet.show(dialog, content);
    }

    private void clearErrors() {
        setInputsEnabled(true);
        showAttachRetry = false;
        topBanner.setVisibility(View.GONE);
        serviceTypeSpinner.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        serviceTypeError.setVisibility(View.GONE);

        titleInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        titleError.setVisibility(View.GONE);

        linkedStudySpinner.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        linkedStudyError.setVisibility(View.GONE);

        detailsInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        detailsError.setVisibility(View.GONE);

        confirmError.setVisibility(View.GONE);
        attachErrorText.setVisibility(View.GONE);
        pendingStatusText.setVisibility(View.GONE);

        submitBtn.setText("Submit");
        submitBtn.setEnabled(true);
        submitBtn.setBackground(UiTheme.shape(context, 0xff131c4b, 8, 0));
    }

    private LinearLayout createCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 16);
        card.setPadding(pad, pad, pad, pad);
        return card;
    }

    private void addLabel(Context context, LinearLayout parent, String label, Typeface font) {
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextColor(0xff082d50);
        tv.setTypeface(font);
        ScreenTypography.apply(tv, 13);
        tv.setPadding(0, 0, 0, UiTheme.dp(context, 4));
        parent.addView(tv);
    }

    private TextView addFieldError(Context context, LinearLayout parent) {
        TextView err = new TextView(context);
        err.setText("⚠ Required");
        err.setTextColor(0xffdc2626);
        err.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(err, 12);
        err.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        err.setVisibility(View.GONE);
        parent.addView(err);
        return err;
    }

    private EditText createInput(Context context, String text, Typeface font) {
        EditText input = new EditText(context);
        input.setText(text);
        input.setTextColor(0xff1f2937);
        input.setTypeface(font);
        ScreenTypography.apply(input, 14);
        input.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int padH = UiTheme.dp(context, 12);
        int padV = UiTheme.dp(context, 10);
        input.setPadding(padH, padV, padH, padV);
        input.setMinHeight(UiTheme.dp(context, 46));
        return input;
    }

    private Spinner createSpinner(Context context, String[] items) {
        Spinner spinner = new Spinner(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
        spinner.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        spinner.setMinimumHeight(UiTheme.dp(context, 46));
        return spinner;
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
        if (layout.getParent() instanceof ViewGroup) {
            ((ViewGroup) layout.getParent()).removeView(layout);
        }
        return PageTitle.wrap(layout, "Submit Service Request", "Start a new request with the Center for Research Management.", true);
    }
}
