package ph.edu.umak.crm.app.staff.content;

import android.content.Context;
import android.graphics.Typeface;
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

import java.util.List;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
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
    private final Spinner categorySpinner;
    private final EditText descInput;
    private final EditText purposeInput;
    private final CheckBox studentCb;
    private final CheckBox facultyCb;
    private final EditText unitInput;
    private final EditText emailInput;

    private final Button inAppBtn;
    private final Button externalBtn;
    private final LinearLayout externalInstructionCard;
    private final EditText externalInstructionInput;

    private boolean isExternal = false;

    private static final String[] CATEGORIES = {
            "Ethics Clearance",
            "Research Registration",
            "Clearance Processing",
            "Publication",
            "Data Analysis"
    };

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

        // Card 1: Service Details
        LinearLayout card1 = new LinearLayout(context);
        card1.setOrientation(LinearLayout.VERTICAL);
        card1.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        card1.setPadding(pad, pad, pad, pad);

        TextView c1Title = new TextView(context);
        c1Title.setText("Service Details");
        c1Title.setTextColor(0xff082d50);
        c1Title.setTypeface(bold);
        ScreenTypography.apply(c1Title, 16);
        card1.addView(c1Title);

        addSpacing(context, card1, 14);

        // Name
        nameInput = addFormField(card1, "Service Name / Title *", "Plagiarism Checker", false);

        // Category Dropdown with chevron
        TextView catLabel = new TextView(context);
        catLabel.setText("Service Category");
        catLabel.setTextColor(0xff082d50);
        catLabel.setTypeface(bold);
        ScreenTypography.apply(catLabel, 13);
        card1.addView(catLabel);

        addSpacing(context, card1, 6);

        FrameLayout catContainer = new FrameLayout(context);
        catContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        categorySpinner = new Spinner(context);
        categorySpinner.setBackground(null);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
        categorySpinner.setAdapter(catAdapter);
        FrameLayout.LayoutParams spinLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 46));
        catContainer.addView(categorySpinner, spinLp);

        ImageView chevron = new ImageView(context);
        chevron.setImageResource(R.drawable.ic_crm_chevron_down);
        chevron.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams chevLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 20), UiTheme.dp(context, 20));
        chevLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        chevLp.rightMargin = UiTheme.dp(context, 12);
        catContainer.addView(chevron, chevLp);

        card1.addView(catContainer);

        addSpacing(context, card1, 14);

        // Description
        descInput = addFormField(card1, "Service Description", "Similarity screening of manuscripts before CRM clearance or journal submission.", true);

        // Purpose
        purposeInput = addFormField(card1, "Purpose *", "Help researchers confirm their manuscript meets UMak originality standards before submission.", true);

        // Applicant categories
        TextView appCatLabel = new TextView(context);
        appCatLabel.setText("Applicant categories");
        appCatLabel.setTextColor(0xff082d50);
        appCatLabel.setTypeface(bold);
        ScreenTypography.apply(appCatLabel, 13);
        card1.addView(appCatLabel);

        addSpacing(context, card1, 6);

        studentCb = new CheckBox(context);
        studentCb.setText("Student");
        studentCb.setChecked(true);
        studentCb.setTextColor(0xff082d50);
        studentCb.setTypeface(regular);
        card1.addView(studentCb);

        facultyCb = new CheckBox(context);
        facultyCb.setText("Faculty");
        facultyCb.setChecked(true);
        facultyCb.setTextColor(0xff082d50);
        facultyCb.setTypeface(regular);
        card1.addView(facultyCb);

        addSpacing(context, card1, 14);

        // Responsible contact
        TextView contactHeader = new TextView(context);
        contactHeader.setText("Responsible contact");
        contactHeader.setTextColor(0xff082d50);
        contactHeader.setTypeface(bold);
        ScreenTypography.apply(contactHeader, 14);
        card1.addView(contactHeader);

        addSpacing(context, card1, 10);

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
        c2Title.setTextColor(0xff082d50);
        c2Title.setTypeface(bold);
        ScreenTypography.apply(c2Title, 16);
        card2.addView(c2Title);

        addSpacing(context, card2, 8);

        TextView subLabel = new TextView(context);
        subLabel.setText("Researchers submit");
        subLabel.setTextColor(0xff586b83);
        subLabel.setTypeface(regular);
        ScreenTypography.apply(subLabel, 13);
        card2.addView(subLabel);

        addSpacing(context, card2, 8);

        // Segmented pill buttons for submission channel
        LinearLayout channelToggle = new LinearLayout(context);
        channelToggle.setOrientation(LinearLayout.HORIZONTAL);
        channelToggle.setBackground(UiTheme.shape(context, 0xfff1f5f9, 8, UiTheme.BORDER));
        int tPad = UiTheme.dp(context, 3);
        channelToggle.setPadding(tPad, tPad, tPad, tPad);

        inAppBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        inAppBtn.setText("✓ In app");
        inAppBtn.setAllCaps(false);
        inAppBtn.setTypeface(bold);
        inAppBtn.setTextColor(0xff082d50);
        ScreenTypography.apply(inAppBtn, 13);
        inAppBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, 0xff082d50));
        inAppBtn.setMinHeight(UiTheme.dp(context, 38));
        inAppBtn.setOnClickListener(v -> {
            isExternal = false;
            updateChannelButtons();
        });
        channelToggle.addView(inAppBtn, new LinearLayout.LayoutParams(0, UiTheme.dp(context, 38), 1f));

        externalBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        externalBtn.setText("External channel");
        externalBtn.setAllCaps(false);
        externalBtn.setTypeface(regular);
        externalBtn.setTextColor(0xff586b83);
        ScreenTypography.apply(externalBtn, 13);
        externalBtn.setBackground(null);
        externalBtn.setMinHeight(UiTheme.dp(context, 38));
        externalBtn.setOnClickListener(v -> {
            isExternal = true;
            updateChannelButtons();
        });
        channelToggle.addView(externalBtn, new LinearLayout.LayoutParams(0, UiTheme.dp(context, 38), 1f));

        card2.addView(channelToggle);

        addSpacing(context, card2, 12);

        // External instructions card container (visible only if external)
        externalInstructionCard = new LinearLayout(context);
        externalInstructionCard.setOrientation(LinearLayout.VERTICAL);
        externalInstructionCard.setVisibility(isExternal ? View.VISIBLE : View.GONE);

        TextView extLabel = new TextView(context);
        extLabel.setText("External submission instructions *");
        extLabel.setTextColor(0xff082d50);
        extLabel.setTypeface(bold);
        ScreenTypography.apply(extLabel, 13);
        externalInstructionCard.addView(extLabel);

        addSpacing(context, externalInstructionCard, 6);

        externalInstructionInput = new EditText(context);
        externalInstructionInput.setText("Email the manuscript and endorsement letter to research@umak.edu.ph with the subject “Plagiarism Check – <Study title>”.");
        externalInstructionInput.setTextColor(0xff082d50);
        externalInstructionInput.setTypeface(regular);
        ScreenTypography.apply(externalInstructionInput, 13);
        int ePad = UiTheme.dp(context, 10);
        externalInstructionInput.setPadding(ePad, ePad, ePad, ePad);
        externalInstructionInput.setBackground(UiTheme.shape(context, 0xffffffff, 8, UiTheme.BORDER));
        externalInstructionInput.setMinLines(3);
        externalInstructionInput.setGravity(Gravity.TOP | Gravity.START);
        externalInstructionCard.addView(externalInstructionInput);

        addSpacing(context, externalInstructionCard, 4);

        TextView extNote = new TextView(context);
        extNote.setText("Shown to researchers instead of the in-app form.");
        extNote.setTextColor(0xff586b83);
        extNote.setTypeface(regular);
        ScreenTypography.apply(extNote, 12);
        externalInstructionCard.addView(extNote);

        addSpacing(context, externalInstructionCard, 12);
        card2.addView(externalInstructionCard);

        // Instructions Section
        TextView instHeader = new TextView(context);
        instHeader.setText("Instructions");
        instHeader.setTextColor(0xff082d50);
        instHeader.setTypeface(bold);
        ScreenTypography.apply(instHeader, 14);
        card2.addView(instHeader);

        addSpacing(context, card2, 8);

        addInstructionStep(card2, "1", "Sign in to your Researcher account.");
        addInstructionStep(card2, "2", "Download and complete the approved form.");
        addInstructionStep(card2, "3", "Prepare the required documents.");
        addInstructionStep(card2, "4", "Submit the request and attach files.");

        addSpacing(context, card2, 10);

        Button addStepBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        addStepBtn.setText("+ Add step");
        addStepBtn.setAllCaps(false);
        addStepBtn.setTextColor(0xff185d9d);
        addStepBtn.setTypeface(bold);
        ScreenTypography.apply(addStepBtn, 14);
        addStepBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xff185d9d), 0x20185d9d));
        addStepBtn.setMinHeight(UiTheme.dp(context, 40));
        card2.addView(addStepBtn);

        addSpacing(context, card2, 6);

        TextView reorderNote = new TextView(context);
        reorderNote.setText("Drag the handle to reorder. Numbers update automatically.");
        reorderNote.setTextColor(0xff586b83);
        reorderNote.setTypeface(regular);
        ScreenTypography.apply(reorderNote, 12);
        card2.addView(reorderNote);

        addSpacing(context, card2, 18);

        // Requirements Section
        TextView reqHeader = new TextView(context);
        reqHeader.setText("Requirements");
        reqHeader.setTextColor(0xff082d50);
        reqHeader.setTypeface(bold);
        ScreenTypography.apply(reqHeader, 14);
        card2.addView(reqHeader);

        TextView reqSubtitle = new TextView(context);
        reqSubtitle.setText("These requirements apply to this service only.");
        reqSubtitle.setTextColor(0xff586b83);
        reqSubtitle.setTypeface(regular);
        ScreenTypography.apply(reqSubtitle, 12);
        card2.addView(reqSubtitle);

        addSpacing(context, card2, 10);

        addRequirementCard(card2, "Manuscript (PDF or DOCX)", true);
        addRequirementCard(card2, "Adviser endorsement letter", true);
        addRequirementCard(card2, "Similarity report from adviser", false);

        addSpacing(context, card2, 10);

        Button addReqBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        addReqBtn.setText("+ Add requirement");
        addReqBtn.setAllCaps(false);
        addReqBtn.setTextColor(0xff185d9d);
        addReqBtn.setTypeface(bold);
        ScreenTypography.apply(addReqBtn, 14);
        addReqBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xff185d9d), 0x20185d9d));
        addReqBtn.setMinHeight(UiTheme.dp(context, 40));
        card2.addView(addReqBtn);

        addSpacing(context, card2, 18);

        // Form Fields Section
        TextView formHeader = new TextView(context);
        formHeader.setText("Form Fields");
        formHeader.setTextColor(0xff082d50);
        formHeader.setTypeface(bold);
        ScreenTypography.apply(formHeader, 14);
        card2.addView(formHeader);

        addSpacing(context, card2, 8);

        addFormFieldItem(card2, "Input: Applicant Name", "Required");
        addFormFieldItem(card2, "Dropdown: College / Department", "Required");
        addFormFieldItem(card2, "File Uploaded: Manuscript", "Required");

        layout.addView(card2);

        addSpacing(context, layout, 24);

        // Actions: Continue (Yellow CTA) and Save as Draft (White outline)
        Button continueBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        continueBtn.setText("Continue");
        continueBtn.setAllCaps(false);
        continueBtn.setTypeface(bold);
        continueBtn.setTextColor(0xff082d50);
        ScreenTypography.apply(continueBtn, 15);
        continueBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xfffbbf24, 8, 0), 0x20082d50));
        continueBtn.setMinHeight(UiTheme.dp(context, 46));
        continueBtn.setOnClickListener(v -> {
            if (listener != null) listener.onContinue();
        });
        layout.addView(continueBtn);

        addSpacing(context, layout, 12);

        Button draftBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        draftBtn.setText("Save as Draft");
        draftBtn.setAllCaps(false);
        draftBtn.setTextColor(0xff082d50);
        draftBtn.setTypeface(bold);
        ScreenTypography.apply(draftBtn, 15);
        draftBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        draftBtn.setMinHeight(UiTheme.dp(context, 46));
        draftBtn.setOnClickListener(v -> {
            if (listener != null) listener.onSaveDraft();
        });
        layout.addView(draftBtn);
    }

    private void updateChannelButtons() {
        if (isExternal) {
            externalBtn.setText("✓ External channel");
            externalBtn.setTypeface(UiTheme.boldFont(context));
            externalBtn.setTextColor(0xff082d50);
            externalBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, 0xff082d50));

            inAppBtn.setText("In app");
            inAppBtn.setTypeface(UiTheme.bodyFont(context));
            inAppBtn.setTextColor(0xff586b83);
            inAppBtn.setBackground(null);

            externalInstructionCard.setVisibility(View.VISIBLE);
        } else {
            inAppBtn.setText("✓ In app");
            inAppBtn.setTypeface(UiTheme.boldFont(context));
            inAppBtn.setTextColor(0xff082d50);
            inAppBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, 0xff082d50));

            externalBtn.setText("External channel");
            externalBtn.setTypeface(UiTheme.bodyFont(context));
            externalBtn.setTextColor(0xff586b83);
            externalBtn.setBackground(null);

            externalInstructionCard.setVisibility(View.GONE);
        }
    }

    private EditText addFormField(LinearLayout parent, String label, String value, boolean multiline) {
        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(0xff082d50);
        lbl.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(lbl, 13);
        parent.addView(lbl);

        addSpacing(context, parent, 6);

        EditText field = new EditText(context);
        field.setText(value);
        field.setTextColor(0xff082d50);
        field.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(field, 14);
        int padH = UiTheme.dp(context, 12);
        int padV = UiTheme.dp(context, 10);
        field.setPadding(padH, padV, padH, padV);
        field.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        if (multiline) {
            field.setMinLines(3);
            field.setGravity(Gravity.TOP | Gravity.START);
        } else {
            field.setSingleLine(true);
            field.setMinHeight(UiTheme.dp(context, 46));
        }
        parent.addView(field);

        addSpacing(context, parent, 14);
        return field;
    }

    private void addInstructionStep(LinearLayout parent, String num, String text) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(UiTheme.shape(context, 0xffffffff, 6, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 10);
        row.setPadding(pad, pad, pad, pad);

        // Amber step badge
        TextView numView = new TextView(context);
        numView.setText(num);
        numView.setTextColor(0xffffffff);
        numView.setTypeface(UiTheme.boldFont(context));
        numView.setGravity(Gravity.CENTER);
        ScreenTypography.apply(numView, 12);
        int size = UiTheme.dp(context, 22);
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(size, size);
        nLp.rightMargin = UiTheme.dp(context, 12);
        numView.setLayoutParams(nLp);
        numView.setBackground(UiTheme.shape(context, 0xfff59e0b, size / 2, 0));
        row.addView(numView);

        TextView tView = new TextView(context);
        tView.setText(text);
        tView.setTextColor(0xff082d50);
        tView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(tView, 13);
        row.addView(tView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // Drag handle icon ⋮⋮
        ImageView dragIcon = new ImageView(context);
        dragIcon.setImageResource(R.drawable.ic_crm_drag_handle);
        int iconSize = UiTheme.dp(context, 20);
        row.addView(dragIcon, new LinearLayout.LayoutParams(iconSize, iconSize));

        parent.addView(row);
        addSpacing(context, parent, 6);
    }

    private void addRequirementCard(LinearLayout parent, String name, boolean defaultRequired) {
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(UiTheme.shape(context, 0xffffffff, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 10);
        box.setPadding(pad, pad, pad, pad);

        // Header row with Name and Remove ×
        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView nameView = new TextView(context);
        nameView.setText(name);
        nameView.setTextColor(0xff082d50);
        nameView.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(nameView, 13);
        topRow.addView(nameView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView removeBtn = new TextView(context);
        removeBtn.setText("×");
        removeBtn.setTextColor(0xff586b83);
        removeBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(removeBtn, 16);
        removeBtn.setPadding(UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4), 0);
        topRow.addView(removeBtn);

        box.addView(topRow);

        addSpacing(context, box, 8);

        // Segmented pill toggle: Required vs Optional
        LinearLayout toggle = new LinearLayout(context);
        toggle.setOrientation(LinearLayout.HORIZONTAL);
        toggle.setBackground(UiTheme.shape(context, 0xfff1f5f9, 6, 0));
        int tPad = UiTheme.dp(context, 2);
        toggle.setPadding(tPad, tPad, tPad, tPad);

        Button reqBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        Button optBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);

        reqBtn.setAllCaps(false);
        reqBtn.setText(defaultRequired ? "✓ Required" : "Required");
        reqBtn.setTypeface(defaultRequired ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        reqBtn.setTextColor(defaultRequired ? 0xff082d50 : 0xff586b83);
        ScreenTypography.apply(reqBtn, 12);
        reqBtn.setBackground(defaultRequired ? UiTheme.shape(context, UiTheme.WHITE, 6, 0xff082d50) : null);
        reqBtn.setMinHeight(UiTheme.dp(context, 32));

        optBtn.setAllCaps(false);
        optBtn.setText(!defaultRequired ? "✓ Optional" : "Optional");
        optBtn.setTypeface(!defaultRequired ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        optBtn.setTextColor(!defaultRequired ? 0xff082d50 : 0xff586b83);
        ScreenTypography.apply(optBtn, 12);
        optBtn.setBackground(!defaultRequired ? UiTheme.shape(context, UiTheme.WHITE, 6, 0xff082d50) : null);
        optBtn.setMinHeight(UiTheme.dp(context, 32));

        reqBtn.setOnClickListener(v -> {
            reqBtn.setText("✓ Required");
            reqBtn.setTypeface(UiTheme.boldFont(context));
            reqBtn.setTextColor(0xff082d50);
            reqBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, 0xff082d50));

            optBtn.setText("Optional");
            optBtn.setTypeface(UiTheme.bodyFont(context));
            optBtn.setTextColor(0xff586b83);
            optBtn.setBackground(null);
        });

        optBtn.setOnClickListener(v -> {
            optBtn.setText("✓ Optional");
            optBtn.setTypeface(UiTheme.boldFont(context));
            optBtn.setTextColor(0xff082d50);
            optBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, 0xff082d50));

            reqBtn.setText("Required");
            reqBtn.setTypeface(UiTheme.bodyFont(context));
            reqBtn.setTextColor(0xff586b83);
            reqBtn.setBackground(null);
        });

        toggle.addView(reqBtn, new LinearLayout.LayoutParams(0, UiTheme.dp(context, 32), 1f));
        toggle.addView(optBtn, new LinearLayout.LayoutParams(0, UiTheme.dp(context, 32), 1f));

        box.addView(toggle);

        parent.addView(box);
        addSpacing(context, parent, 8);
    }

    private void addFormFieldItem(LinearLayout parent, String title, String badge) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(UiTheme.shape(context, 0xfff8fafc, 6, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 10);
        row.setPadding(pad, pad, pad, pad);

        TextView tView = new TextView(context);
        tView.setText(title);
        tView.setTextColor(0xff082d50);
        tView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(tView, 13);
        row.addView(tView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView bView = new TextView(context);
        bView.setText(badge);
        bView.setTextColor(0xff16a34a);
        bView.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(bView, 11);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        bView.setPadding(px, py, px, py);
        bView.setBackground(UiTheme.shape(context, 0xffdcfce7, 4, 0));
        row.addView(bView);

        parent.addView(row);
        addSpacing(context, parent, 6);
    }

    public void setPreviewState(String state) {
        isExternal = "external".equalsIgnoreCase(state) || "5.3_external".equalsIgnoreCase(state);
        updateChannelButtons();
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

        // Subtitle: Create a new research service catalog entry and configure its dynamic research form.
        TextView copy = new TextView(context);
        copy.setText("Create a new research service catalog entry and configure its dynamic research form.");
        copy.setTextColor(UiTheme.PALE);
        copy.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(copy, 13);
        band.addView(copy);

        addSpacing(context, band, 14);

        // Step Indicator Title
        TextView stepIndicator = new TextView(context);
        stepIndicator.setText("Step 1 of 3: Service Details");
        stepIndicator.setTextColor(0xffffffff);
        stepIndicator.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(stepIndicator, 13);
        band.addView(stepIndicator);

        addSpacing(context, band, 8);

        // 3-Segment Progress Indicator Line inside the Dark Blue Band
        LinearLayout stepBar = new LinearLayout(context);
        stepBar.setOrientation(LinearLayout.HORIZONTAL);
        stepBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 4)));

        View seg1 = new View(context);
        seg1.setBackground(UiTheme.shape(context, 0xffffffff, 2, 0));
        LinearLayout.LayoutParams seg1Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        seg1Params.rightMargin = UiTheme.dp(context, 6);
        stepBar.addView(seg1, seg1Params);

        View seg2 = new View(context);
        seg2.setBackground(UiTheme.shape(context, 0x40ffffff, 2, 0));
        LinearLayout.LayoutParams seg2Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        seg2Params.rightMargin = UiTheme.dp(context, 6);
        stepBar.addView(seg2, seg2Params);

        View seg3 = new View(context);
        seg3.setBackground(UiTheme.shape(context, 0x40ffffff, 2, 0));
        LinearLayout.LayoutParams seg3Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        stepBar.addView(seg3, seg3Params);

        band.addView(stepBar);

        root.addView(band);
        root.addView(layout);
        return root;
    }
}
