package ph.edu.umak.crm.app.requests;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import ph.edu.umak.crm.app.common.dialogs.BottomSheet;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiBanner;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class SubmitRequestView {
    public interface Listener {
        void onSubmitSuccess(RequestData.RequestItem request);
        void onBack();
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;

    private final LinearLayout bannerContainer;
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
    private Button attachRetryBtn;

    private final CheckBox confirmBox;
    private final TextView confirmError;
    private final Button submitBtn;
    private final TextView pendingStatusText;

    private final List<RequestData.FileItem> currentFiles = new ArrayList<>();

    public SubmitRequestView(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 24);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 32));

        // Banner container for top errors (e.g. 3.2a, 3.2b, 3.4b)
        bannerContainer = new LinearLayout(context);
        bannerContainer.setOrientation(LinearLayout.VERTICAL);
        bannerContainer.setVisibility(View.GONE);
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(-1, -2);
        bannerParams.bottomMargin = UiTheme.dp(context, 16);
        layout.addView(bannerContainer, bannerParams);

        // Card 1: Request Details
        LinearLayout detailsCard = createCard(context);

        TextView cardTitle = new TextView(context);
        cardTitle.setText("Request details");
        cardTitle.setTextColor(UiTheme.INK);
        cardTitle.setTypeface(bold);
        ScreenTypography.apply(cardTitle, 16);
        detailsCard.addView(cardTitle);

        TextView requiredNote = new TextView(context);
        requiredNote.setText("Fields marked required must be completed before submission.");
        requiredNote.setTextColor(UiTheme.MUTED);
        requiredNote.setTypeface(body);
        ScreenTypography.apply(requiredNote, 13);
        requiredNote.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 16));
        detailsCard.addView(requiredNote);

        // Service type *
        addLabel(context, detailsCard, "Service type *", bold);
        String[] services = {"Select a CRM service", "Ethics Clearance", "Research Registration", "Research Consultation", "Publication Assistance", "Statistical Consultation", "Clearance Processing"};
        serviceTypeSpinner = createSpinner(context, services);
        detailsCard.addView(serviceTypeSpinner);
        serviceTypeError = addFieldError(context, detailsCard);

        addSpacing(context, detailsCard, 14);

        // Research stage
        addLabel(context, detailsCard, "Research stage", bold);
        String[] stages = {"Proposal development", "Ethics review", "Data gathering", "Presentation", "Publication"};
        stageSpinner = createSpinner(context, stages);
        detailsCard.addView(stageSpinner);

        addSpacing(context, detailsCard, 14);

        // Study title *
        addLabel(context, detailsCard, "Study title *", bold);
        titleInput = createInput(context, "Impact of AI on Student Engagement", body);
        detailsCard.addView(titleInput);
        titleError = addFieldError(context, detailsCard);

        addSpacing(context, detailsCard, 14);

        // Linked study (optional)
        addLabel(context, detailsCard, "Linked study (optional)", bold);
        String[] studies = {"No linked study", "REG-2026-089 · Impact of AI on Student Engagement", "Select a linked study"};
        linkedStudySpinner = createSpinner(context, studies);
        linkedStudySpinner.setSelection(1);
        detailsCard.addView(linkedStudySpinner);
        TextView linkedHelper = new TextView(context);
        linkedHelper.setText("Only studies you are linked to are listed.");
        linkedHelper.setTextColor(UiTheme.MUTED);
        linkedHelper.setTypeface(body);
        ScreenTypography.apply(linkedHelper, 12);
        linkedHelper.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        detailsCard.addView(linkedHelper);
        linkedStudyError = addFieldError(context, detailsCard);

        addSpacing(context, detailsCard, 14);

        // Request details *
        addLabel(context, detailsCard, "Request details *", bold);
        detailsInput = createInput(context, "Please review the consent form and research protocol before data collection starts on Nov 15, 2026.", body);
        detailsInput.setSingleLine(false);
        detailsInput.setMinLines(3);
        detailsInput.setGravity(Gravity.TOP | Gravity.START);
        detailsCard.addView(detailsInput);
        TextView detailsHelper = new TextView(context);
        detailsHelper.setText("Include specific questions, deadlines, or documents CRM should review.");
        detailsHelper.setTextColor(UiTheme.MUTED);
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
        attachHeader.setTextColor(UiTheme.INK);
        attachHeader.setTypeface(bold);
        ScreenTypography.apply(attachHeader, 16);
        attachHeaderRow.addView(attachHeader, new LinearLayout.LayoutParams(0, -2, 1));

        attachmentsCounter = new TextView(context);
        attachmentsCounter.setText("2 of 5 files");
        attachmentsCounter.setTextColor(UiTheme.MUTED);
        attachmentsCounter.setTypeface(bold);
        ScreenTypography.apply(attachmentsCounter, 13);
        attachHeaderRow.addView(attachmentsCounter);

        attachCard.addView(attachHeaderRow);

        // Attachments error banner
        attachErrorText = new TextView(context);
        attachErrorText.setTextColor(UiTheme.ERROR_RED);
        attachErrorText.setTypeface(bold);
        ScreenTypography.apply(attachErrorText, 13);
        attachErrorText.setPadding(0, UiTheme.dp(context, 8), 0, 0);
        attachErrorText.setVisibility(View.GONE);
        attachCard.addView(attachErrorText);

        attachRetryBtn = UiButton.text(context, "Retry", UiTheme.NAVY, () -> {
            attachErrorText.setVisibility(View.GONE);
            attachRetryBtn.setVisibility(View.GONE);
            // On retry, convert failed file to attached
            for (int i = 0; i < currentFiles.size(); i++) {
                RequestData.FileItem f = currentFiles.get(i);
                if (f.isError) {
                    currentFiles.set(i, new RequestData.FileItem(f.name, f.size, "Attached", false));
                }
            }
            renderFileList();
        });
        attachRetryBtn.setVisibility(View.GONE);
        attachCard.addView(attachRetryBtn);

        addSpacing(context, attachCard, 12);

        // Upload Zone Button Box
        attachBox = new LinearLayout(context);
        attachBox.setOrientation(LinearLayout.VERTICAL);
        attachBox.setGravity(Gravity.CENTER);
        attachBox.setBackground(UiTheme.shape(context, UiTheme.BADGE_BLUE_BG, 8, UiTheme.BORDER));
        attachBox.setPadding(pad, pad, pad, pad);

        TextView attachBoxTitle = new TextView(context);
        attachBoxTitle.setText("Attach file");
        attachBoxTitle.setTextColor(UiTheme.NAVY);
        attachBoxTitle.setTypeface(bold);
        ScreenTypography.apply(attachBoxTitle, 15);
        attachBox.addView(attachBoxTitle);

        TextView attachBoxSub = new TextView(context);
        attachBoxSub.setText("PDF or DOCX, up to 10 MB each, up to 5 files");
        attachBoxSub.setTextColor(UiTheme.MUTED);
        attachBoxSub.setTypeface(body);
        ScreenTypography.apply(attachBoxSub, 13);
        attachBoxSub.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        attachBox.addView(attachBoxSub);

        attachCard.addView(attachBox);

        addSpacing(context, attachCard, 12);

        // Files container
        attachmentsContainer = new LinearLayout(context);
        attachmentsContainer.setOrientation(LinearLayout.VERTICAL);
        attachCard.addView(attachmentsContainer);

        layout.addView(attachCard);
        addSpacing(context, layout, 16);

        // Confirmation Checkbox
        confirmBox = new CheckBox(context);
        confirmBox.setText("I confirm that the information provided is complete and accurate.");
        confirmBox.setTextColor(UiTheme.INK);
        confirmBox.setTypeface(body);
        ScreenTypography.apply(confirmBox, 13);
        confirmBox.setChecked(true);
        layout.addView(confirmBox);

        confirmError = addFieldError(context, layout);

        TextView ackNote = new TextView(context);
        ackNote.setText("Requests are usually acknowledged within two working days.");
        ackNote.setTextColor(UiTheme.MUTED);
        ackNote.setTypeface(body);
        ScreenTypography.apply(ackNote, 13);
        ackNote.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 16));
        layout.addView(ackNote);

        // Submit Button & Pending state message
        pendingStatusText = new TextView(context);
        pendingStatusText.setText("Sending your request and files to CRM…");
        pendingStatusText.setTextColor(UiTheme.MUTED);
        pendingStatusText.setTypeface(body);
        ScreenTypography.apply(pendingStatusText, 13);
        pendingStatusText.setGravity(Gravity.CENTER);
        pendingStatusText.setPadding(0, 0, 0, UiTheme.dp(context, 8));
        pendingStatusText.setVisibility(View.GONE);
        layout.addView(pendingStatusText);

        submitBtn = UiButton.primary(context, "Submit", this::handleSubmit);
        layout.addView(submitBtn, new LinearLayout.LayoutParams(-1, -2));

        // Initial default files (3.1)
        currentFiles.add(new RequestData.FileItem("RESEARCH_PROTOCOL_v2.pdf", "2.4 MB", "Attached", false));
        currentFiles.add(new RequestData.FileItem("INFORMED_CONSENT_FORM.docx", "480 KB", "Attached", false));
        renderFileList();
    }

    private void renderFileList() {
        attachmentsContainer.removeAllViews();
        int attachedCount = 0;
        for (RequestData.FileItem file : currentFiles) {
            if (!file.isError) attachedCount++;
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
            int pad = UiTheme.dp(context, 10);
            row.setPadding(pad, pad, pad, pad);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
            rowParams.bottomMargin = UiTheme.dp(context, 8);

            LinearLayout info = new LinearLayout(context);
            info.setOrientation(LinearLayout.VERTICAL);

            TextView name = new TextView(context);
            name.setText(file.name);
            name.setTextColor(file.isError ? UiTheme.ERROR_RED : UiTheme.INK);
            name.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(name, 13);
            info.addView(name);

            TextView size = new TextView(context);
            size.setText(file.size);
            size.setTextColor(UiTheme.MUTED);
            size.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(size, 12);
            info.addView(size);

            row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

            TextView badge = new TextView(context);
            badge.setText(file.status);
            badge.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(badge, 11);
            int padH = UiTheme.dp(context, 8);
            int padV = UiTheme.dp(context, 3);
            badge.setPadding(padH, padV, padH, padV);
            if (file.isError) {
                badge.setTextColor(UiTheme.PILL_REQUIRED_TEXT);
                badge.setBackground(UiTheme.shape(context, UiTheme.PILL_REQUIRED_BG, 6, 0));
            } else {
                badge.setTextColor(UiTheme.TAG_TEXT);
                badge.setBackground(UiTheme.shape(context, UiTheme.TAG_BG, 6, 0));
            }
            row.addView(badge);

            attachmentsContainer.addView(row, rowParams);
        }
        attachmentsCounter.setText(attachedCount + " of 5 files");
        attachBox.setEnabled(attachedCount < 5);
        attachBox.setAlpha(attachedCount >= 5 ? 0.5f : 1.0f);
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
            linkedStudySpinner.performClick();
        } else if ("3.2a".equals(state)) {
            // Error required fields
            showTopBanner("Complete the highlighted required fields.");
            serviceTypeSpinner.setSelection(0);
            serviceTypeError.setVisibility(View.VISIBLE);
            detailsInput.setText("");
            detailsError.setVisibility(View.VISIBLE);
            confirmBox.setChecked(false);
            confirmError.setVisibility(View.VISIBLE);
        } else if ("3.2b".equals(state)) {
            // Error invalid information
            showTopBanner("Correct the highlighted information.");
            serviceTypeSpinner.setSelection(1);
            titleInput.setText("AI");
            titleError.setText("Enter the complete research title (at least 10 characters).");
            titleError.setVisibility(View.VISIBLE);
        } else if ("3.2c".equals(state)) {
            // Error unauthorized study
            linkedStudySpinner.setSelection(2); // Select a linked study
            linkedStudyError.setText("Select a study you are linked to.");
            linkedStudyError.setVisibility(View.VISIBLE);
        } else if ("3.3a".equals(state)) {
            // File type error
            configurePreviewState("3.1");
            attachErrorText.setText("Upload a PDF or DOCX file.");
            attachErrorText.setVisibility(View.VISIBLE);
            currentFiles.add(new RequestData.FileItem("photo.png", "1.2 MB", "Not attached", true));
            renderFileList();
        } else if ("3.3b".equals(state)) {
            // File too large error
            configurePreviewState("3.1");
            attachErrorText.setText("Each file must be 10 MB or smaller.");
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
            attachErrorText.setText("You can attach up to 5 files.");
            attachErrorText.setVisibility(View.VISIBLE);
            renderFileList();
        } else if ("3.3d".equals(state)) {
            // Upload failed with retry
            configurePreviewState("3.1");
            attachErrorText.setText("Upload failed. Try again.");
            attachErrorText.setVisibility(View.VISIBLE);
            attachRetryBtn.setVisibility(View.VISIBLE);
            currentFiles.add(new RequestData.FileItem("ETHICS_FORM_004.pdf", "1.8 MB", "Not attached", true));
            renderFileList();
        } else if ("3.4".equals(state)) {
            // Pending state
            configurePreviewState("3.1");
            submitBtn.setText("Submitting…");
            submitBtn.setEnabled(false);
            submitBtn.setBackground(UiTheme.shape(context, UiTheme.DISABLED_BTN_BG, 8, 0));
            pendingStatusText.setVisibility(View.VISIBLE);
        } else if ("3.4b".equals(state)) {
            // Unable to connect sheet
            configurePreviewState("3.1");
            showConnectionFailure("Unable to connect. Try again.", "Check your connection and retry. Your entered information is still here.");
        }
    }

    private void showTopBanner(String message) {
        bannerContainer.removeAllViews();
        bannerContainer.addView(UiBanner.create(context, message, null, null));
        bannerContainer.setVisibility(View.VISIBLE);
    }

    private void showConnectionFailure(String title, String description) {
        Dialog dialog = new Dialog(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = UiTheme.dp(context, 16);
        content.setPadding(padding, padding, padding, padding);

        TextView heading = new TextView(context);
        heading.setText(title);
        heading.setTextColor(UiTheme.NAVY);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 20);
        ScreenTypography.heading(heading);
        content.addView(heading);

        TextView copy = new TextView(context);
        copy.setText(description);
        copy.setTextColor(UiTheme.MUTED);
        copy.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(copy, 13);
        copy.setPadding(0, 0, 0, padding);
        content.addView(copy);

        content.addView(UiButton.primary(context, "Retry", () -> {
            dialog.dismiss();
            handleSubmit();
        }));
        BottomSheet.show(dialog, content);
    }

    private void clearErrors() {
        bannerContainer.removeAllViews();
        bannerContainer.setVisibility(View.GONE);
        serviceTypeError.setVisibility(View.GONE);
        titleError.setVisibility(View.GONE);
        linkedStudyError.setVisibility(View.GONE);
        detailsError.setVisibility(View.GONE);
        confirmError.setVisibility(View.GONE);
        attachErrorText.setVisibility(View.GONE);
        attachRetryBtn.setVisibility(View.GONE);
        pendingStatusText.setVisibility(View.GONE);
        submitBtn.setText("Submit");
        submitBtn.setEnabled(true);
        submitBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.NAVY, 8, 0), 0x20395072));
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
        tv.setTextColor(UiTheme.INK);
        tv.setTypeface(font);
        ScreenTypography.apply(tv, 13);
        tv.setPadding(0, 0, 0, UiTheme.dp(context, 4));
        parent.addView(tv);
    }

    private TextView addFieldError(Context context, LinearLayout parent) {
        TextView err = new TextView(context);
        err.setText("Required");
        err.setTextColor(UiTheme.ERROR_RED);
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
        input.setTextColor(UiTheme.INK);
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
