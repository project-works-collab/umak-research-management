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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class ServiceAuthoringStep1View {

    public interface Listener {
        void onBackToCatalog();
        void onContinue();
        void onSaveDraft();
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;

    private final EditText nameInput;
    private final EditText categoryInput;
    private final EditText descInput;
    private final EditText purposeInput;
    private final CheckBox studentCb;
    private final CheckBox facultyCb;
    private final EditText unitInput;
    private final EditText emailInput;

    private final RadioButton inAppRadio;
    private final RadioButton externalRadio;
    private final LinearLayout externalInstructionCard;
    private final EditText externalInstructionInput;

    private boolean isExternal = false;

    public ServiceAuthoringStep1View(Context context, Listener listener) {
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
            if (listener != null) listener.onBackToCatalog();
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
        subtitle.setText("Create a new research service catalog entry and configure its dynamic research form.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 13);
        layout.addView(subtitle);

        addSpacing(context, layout, 8);

        // Step indicator
        TextView stepIndicator = new TextView(context);
        stepIndicator.setText("Step 1 of 3: Service Details");
        stepIndicator.setTextColor(UiTheme.NAVY);
        stepIndicator.setTypeface(bold);
        ScreenTypography.apply(stepIndicator, 14);
        layout.addView(stepIndicator);

        addSpacing(context, layout, 16);

        // Card 1: Service Details
        LinearLayout card1 = new LinearLayout(context);
        card1.setOrientation(LinearLayout.VERTICAL);
        card1.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        card1.setPadding(pad, pad, pad, pad);

        TextView c1Title = new TextView(context);
        c1Title.setText("Service Details");
        c1Title.setTextColor(UiTheme.NAVY);
        c1Title.setTypeface(bold);
        ScreenTypography.apply(c1Title, 16);
        card1.addView(c1Title);

        addSpacing(context, card1, 12);

        // Name
        nameInput = addFormField(card1, "Service Name / Title *", "Plagiarism Checker", false);
        // Category
        categoryInput = addFormField(card1, "Service Category", "Ethics Clearance", false);
        // Description
        descInput = addFormField(card1, "Service Description", "Similarity screening of manuscripts before CRM clearance or journal submission.", true);
        // Purpose
        purposeInput = addFormField(card1, "Purpose *", "Help researchers confirm their manuscript meets UMak originality standards before submission.", true);

        // Applicant categories
        TextView catLabel = new TextView(context);
        catLabel.setText("Applicant categories");
        catLabel.setTextColor(UiTheme.NAVY);
        catLabel.setTypeface(bold);
        ScreenTypography.apply(catLabel, 13);
        card1.addView(catLabel);

        addSpacing(context, card1, 6);

        LinearLayout cbRow = new LinearLayout(context);
        cbRow.setOrientation(LinearLayout.HORIZONTAL);

        studentCb = new CheckBox(context);
        studentCb.setText("Student");
        studentCb.setChecked(true);
        studentCb.setTextColor(UiTheme.NAVY);
        studentCb.setTypeface(regular);
        cbRow.addView(studentCb);

        addHorizontalSpacing(context, cbRow, 16);

        facultyCb = new CheckBox(context);
        facultyCb.setText("Faculty");
        facultyCb.setChecked(true);
        facultyCb.setTextColor(UiTheme.NAVY);
        facultyCb.setTypeface(regular);
        cbRow.addView(facultyCb);

        card1.addView(cbRow);

        addSpacing(context, card1, 12);

        // Responsible contact
        TextView contactHeader = new TextView(context);
        contactHeader.setText("Responsible contact");
        contactHeader.setTextColor(UiTheme.NAVY);
        contactHeader.setTypeface(bold);
        ScreenTypography.apply(contactHeader, 14);
        card1.addView(contactHeader);

        addSpacing(context, card1, 8);

        unitInput = addFormField(card1, "Office or unit *", "Center for Research Management", false);
        emailInput = addFormField(card1, "Contact email *", "research@umak.edu.ph", false);

        layout.addView(card1);

        addSpacing(context, layout, 16);

        // Card 2: Submission channel & instructions
        LinearLayout card2 = new LinearLayout(context);
        card2.setOrientation(LinearLayout.VERTICAL);
        card2.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        card2.setPadding(pad, pad, pad, pad);

        TextView c2Title = new TextView(context);
        c2Title.setText("Submission channel");
        c2Title.setTextColor(UiTheme.NAVY);
        c2Title.setTypeface(bold);
        ScreenTypography.apply(c2Title, 16);
        card2.addView(c2Title);

        addSpacing(context, card2, 8);

        TextView subLabel = new TextView(context);
        subLabel.setText("Researchers submit");
        subLabel.setTextColor(UiTheme.MUTED);
        subLabel.setTypeface(regular);
        ScreenTypography.apply(subLabel, 13);
        card2.addView(subLabel);

        addSpacing(context, card2, 6);

        RadioGroup radioGroup = new RadioGroup(context);
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);

        inAppRadio = new RadioButton(context);
        inAppRadio.setText("In app");
        inAppRadio.setChecked(!isExternal);
        inAppRadio.setTextColor(UiTheme.NAVY);
        inAppRadio.setTypeface(regular);
        radioGroup.addView(inAppRadio);

        addHorizontalSpacing(context, radioGroup, 20);

        externalRadio = new RadioButton(context);
        externalRadio.setText("External channel");
        externalRadio.setChecked(isExternal);
        externalRadio.setTextColor(UiTheme.NAVY);
        externalRadio.setTypeface(regular);
        radioGroup.addView(externalRadio);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isExternal = externalRadio.isChecked();
            updateChannelVisibility();
        });

        card2.addView(radioGroup);

        addSpacing(context, card2, 12);

        // External instructions card container (visible only if external)
        externalInstructionCard = new LinearLayout(context);
        externalInstructionCard.setOrientation(LinearLayout.VERTICAL);
        externalInstructionCard.setVisibility(isExternal ? View.VISIBLE : View.GONE);

        TextView extLabel = new TextView(context);
        extLabel.setText("External submission instructions *");
        extLabel.setTextColor(UiTheme.NAVY);
        extLabel.setTypeface(bold);
        ScreenTypography.apply(extLabel, 13);
        externalInstructionCard.addView(extLabel);

        addSpacing(context, externalInstructionCard, 6);

        externalInstructionInput = new EditText(context);
        externalInstructionInput.setText("Email the manuscript and endorsement letter to research@umak.edu.ph with the subject “Plagiarism Check – <Study title>”.");
        externalInstructionInput.setTextColor(UiTheme.NAVY);
        externalInstructionInput.setTypeface(regular);
        ScreenTypography.apply(externalInstructionInput, 13);
        int ePad = UiTheme.dp(context, 10);
        externalInstructionInput.setPadding(ePad, ePad, ePad, ePad);
        externalInstructionInput.setBackground(UiTheme.shape(context, 0xFFF8FAFC, 8, UiTheme.BORDER));
        externalInstructionCard.addView(externalInstructionInput);

        addSpacing(context, externalInstructionCard, 4);

        TextView extNote = new TextView(context);
        extNote.setText("Shown to researchers instead of the in-app form.");
        extNote.setTextColor(UiTheme.MUTED);
        extNote.setTypeface(regular);
        ScreenTypography.apply(extNote, 12);
        externalInstructionCard.addView(extNote);

        addSpacing(context, externalInstructionCard, 12);
        card2.addView(externalInstructionCard);

        // Instructions Section
        TextView instHeader = new TextView(context);
        instHeader.setText("Instructions");
        instHeader.setTextColor(UiTheme.NAVY);
        instHeader.setTypeface(bold);
        ScreenTypography.apply(instHeader, 14);
        card2.addView(instHeader);

        addSpacing(context, card2, 6);

        addInstructionStep(card2, "1", "Sign in to your Researcher account.");
        addInstructionStep(card2, "2", "Download and complete the approved form.");
        addInstructionStep(card2, "3", "Prepare the required documents.");
        addInstructionStep(card2, "4", "Submit the request and attach files.");

        addSpacing(context, card2, 8);

        Button addStepBtn = UiButton.outline(context, "Add step", UiTheme.BORDER, UiTheme.NAVY, null);
        card2.addView(addStepBtn);

        addSpacing(context, card2, 4);

        TextView reorderNote = new TextView(context);
        reorderNote.setText("Drag the handle to reorder. Numbers update automatically.");
        reorderNote.setTextColor(UiTheme.MUTED);
        reorderNote.setTypeface(regular);
        ScreenTypography.apply(reorderNote, 12);
        card2.addView(reorderNote);

        addSpacing(context, card2, 16);

        // Requirements Section
        TextView reqHeader = new TextView(context);
        reqHeader.setText("Requirements");
        reqHeader.setTextColor(UiTheme.NAVY);
        reqHeader.setTypeface(bold);
        ScreenTypography.apply(reqHeader, 14);
        card2.addView(reqHeader);

        TextView reqSubtitle = new TextView(context);
        reqSubtitle.setText("These requirements apply to this service only.");
        reqSubtitle.setTextColor(UiTheme.MUTED);
        reqSubtitle.setTypeface(regular);
        ScreenTypography.apply(reqSubtitle, 12);
        card2.addView(reqSubtitle);

        addSpacing(context, card2, 8);

        addRequirementRow(card2, "Manuscript (PDF or DOCX)", true);
        addRequirementRow(card2, "Adviser endorsement letter", true);
        addRequirementRow(card2, "Similarity report from adviser", false);

        addSpacing(context, card2, 8);

        Button addReqBtn = UiButton.outline(context, "Add requirement", UiTheme.BORDER, UiTheme.NAVY, null);
        card2.addView(addReqBtn);

        addSpacing(context, card2, 16);

        // Form Fields Section
        TextView formHeader = new TextView(context);
        formHeader.setText("Form Fields");
        formHeader.setTextColor(UiTheme.NAVY);
        formHeader.setTypeface(bold);
        ScreenTypography.apply(formHeader, 14);
        card2.addView(formHeader);

        addSpacing(context, card2, 8);

        addFormFieldItem(card2, "Input: Applicant Name", "Required");
        addFormFieldItem(card2, "Dropdown: College / Department", "Required");
        addFormFieldItem(card2, "File Uploaded: Manuscript", "Required");

        layout.addView(card2);

        addSpacing(context, layout, 24);

        // Actions: Continue and Save as Draft
        Button continueBtn = UiButton.primary(context, "Continue", () -> {
            if (listener != null) listener.onContinue();
        });
        layout.addView(continueBtn);

        addSpacing(context, layout, 12);

        Button draftBtn = UiButton.outline(context, "Save as Draft", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (listener != null) listener.onSaveDraft();
        });
        layout.addView(draftBtn);
    }

    private void updateChannelVisibility() {
        externalInstructionCard.setVisibility(isExternal ? View.VISIBLE : View.GONE);
    }

    private EditText addFormField(LinearLayout parent, String label, String value, boolean multiline) {
        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(UiTheme.NAVY);
        lbl.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(lbl, 13);
        parent.addView(lbl);

        addSpacing(context, parent, 4);

        EditText field = new EditText(context);
        field.setText(value);
        field.setTextColor(UiTheme.NAVY);
        field.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(field, 13);
        int pad = UiTheme.dp(context, 10);
        field.setPadding(pad, pad, pad, pad);
        field.setBackground(UiTheme.shape(context, 0xFFF8FAFC, 8, UiTheme.BORDER));
        if (multiline) {
            field.setMinLines(3);
            field.setGravity(Gravity.TOP | Gravity.START);
        } else {
            field.setSingleLine(true);
        }
        parent.addView(field);

        addSpacing(context, parent, 12);
        return field;
    }

    private void addInstructionStep(LinearLayout parent, String num, String text) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));

        TextView numView = new TextView(context);
        numView.setText(num);
        numView.setTextColor(UiTheme.WHITE);
        numView.setTypeface(UiTheme.boldFont(context));
        numView.setGravity(Gravity.CENTER);
        ScreenTypography.apply(numView, 11);
        int size = UiTheme.dp(context, 20);
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(size, size);
        nLp.rightMargin = UiTheme.dp(context, 10);
        numView.setLayoutParams(nLp);
        numView.setBackground(UiTheme.shape(context, UiTheme.NAVY, 10, 0));
        row.addView(numView);

        TextView tView = new TextView(context);
        tView.setText(text);
        tView.setTextColor(UiTheme.NAVY);
        tView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(tView, 13);
        row.addView(tView);

        parent.addView(row);
    }

    private void addRequirementRow(LinearLayout parent, String name, boolean isRequired) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

        TextView nameView = new TextView(context);
        nameView.setText(name);
        nameView.setTextColor(UiTheme.NAVY);
        nameView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(nameView, 13);
        row.addView(nameView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView reqTag = new TextView(context);
        reqTag.setText(isRequired ? "Required" : "Optional");
        reqTag.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(reqTag, 11);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        reqTag.setPadding(px, py, px, py);
        if (isRequired) {
            reqTag.setBackground(UiTheme.shape(context, UiTheme.PILL_REQUIRED_BG, 4, 0));
            reqTag.setTextColor(UiTheme.PILL_REQUIRED_TEXT);
        } else {
            reqTag.setBackground(UiTheme.shape(context, UiTheme.PILL_OPTIONAL_BG, 4, 0));
            reqTag.setTextColor(UiTheme.PILL_OPTIONAL_TEXT);
        }
        row.addView(reqTag);

        parent.addView(row);
    }

    private void addFormFieldItem(LinearLayout parent, String title, String badge) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

        TextView tView = new TextView(context);
        tView.setText(title);
        tView.setTextColor(UiTheme.NAVY);
        tView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(tView, 13);
        row.addView(tView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView bView = new TextView(context);
        bView.setText(badge);
        bView.setTextColor(UiTheme.PILL_REQUIRED_TEXT);
        bView.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(bView, 11);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        bView.setPadding(px, py, px, py);
        bView.setBackground(UiTheme.shape(context, UiTheme.PILL_REQUIRED_BG, 4, 0));
        row.addView(bView);

        parent.addView(row);
    }

    public void setPreviewState(String state) {
        if ("external".equalsIgnoreCase(state) || "5.3_external".equalsIgnoreCase(state)) {
            isExternal = true;
            externalRadio.setChecked(true);
            inAppRadio.setChecked(false);
        } else {
            isExternal = false;
            inAppRadio.setChecked(true);
            externalRadio.setChecked(false);
        }
        updateChannelVisibility();
    }

    private static void addSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(space);
    }

    private static void addHorizontalSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, dp), ViewGroup.LayoutParams.MATCH_PARENT));
        parent.addView(space);
    }

    public View getContentLayout() {
        if (layout.getParent() instanceof ViewGroup) {
            ((ViewGroup) layout.getParent()).removeView(layout);
        }
        return layout;
    }
}
