package ph.edu.umak.crm.app.staff.registry;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Add Research Entry & Record Editor View (Wireframes 7.3, 7.4, 7.5).
 * Supports team picker, validation errors, duplicate detection modal banner, and stale data conflict.
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class AddResearchEntryView {

    public interface Callbacks {
        void onSaveEntry(StaffRegistryData.RegistryRecordItem record);
        void onCancel();
        void onReviewDuplicates(String title, String abstractText, java.util.function.BiConsumer<String,String> result);
    }

    private final Activity activity;
    private final Callbacks callbacks;

    private boolean isEditMode = false;
    private boolean isStaleConflict = false;
    private boolean showDuplicateWarning = false;
    private boolean duplicateReasonError = false;
    private boolean reviewInProgress = false;
    private String teamErrorType = "none"; // "none", "already_member", "no_such_account"

    private FrameLayout rootContainer;
    private LinearLayout rootLayout;
    private EditText titleInput;
    private EditText abstractInput;
    private Spinner collegeSpinner;
    private Spinner academicYearSpinner;
    private EditText adviserInput;
    private Spinner categorySpinner;

    private LinearLayout teamListContainer;
    private EditText teamSearchInput;

    private CheckBox duplicateConfirmCheckBox;
    private EditText duplicateReasonInput;
    private TextView duplicateReasonErrorText;

    private final List<StaffRegistryData.TeamMember> currentTeam = new ArrayList<>();
    private StaffRegistryData.RegistryRecordItem editingRecord;

    private static final String[] COLLEGES = {
            "College of Computing and Information Sciences",
            "College of Engineering and Technology",
            "College of Health Sciences"
    };

    private static final String[] ACADEMIC_YEARS = {
            "AY 2026-2027",
            "AY 2025-2026",
            "AY 2024-2025",
            "AY 2023-2024"
    };

    private static final String[] CATEGORIES = {
            "Student",
            "Faculty"
    };

    private static final String[] ENTRY_TYPES = {
            "Current registration",
            "Archival backfile",
            "Transferred project"
    };

    public AddResearchEntryView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        rootContainer = new FrameLayout(activity);
        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));
        rootContainer.addView(rootLayout);

        resetToDefaultAdd();
        buildUi();
    }

    public void setEditRecord(StaffRegistryData.RegistryRecordItem record) {
        this.isEditMode = true;
        this.isStaleConflict = false;
        this.editingRecord = record;
        this.showDuplicateWarning = false;
        this.duplicateReasonError = false;
        this.teamErrorType = "none";

        currentTeam.clear();
        if (record != null) {
            currentTeam.addAll(record.teamMembers);
        }
        buildUi();
    }

    public void resetToDefaultAdd() {
        this.isEditMode = false;
        this.isStaleConflict = false;
        this.editingRecord = null;
        this.showDuplicateWarning = false;
        this.duplicateReasonError = false;
        this.teamErrorType = "none";

        currentTeam.clear();
        currentTeam.add(new StaffRegistryData.TeamMember("LeBron R. James", "Principal author", "Student", "LJ", "CCIS"));
        currentTeam.add(new StaffRegistryData.TeamMember("Juan D. Inlwan", "Co-author", "Student", "JI", "CCIS"));
    }

    public ViewGroup getContentLayout() {
        if (rootContainer != null && rootContainer.getParent() instanceof ViewGroup) {
            ((ViewGroup) rootContainer.getParent()).removeView(rootContainer);
        }
        buildUi();
        return rootContainer;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    private void buildUi() {
        rootContainer.removeAllViews();
        rootLayout.removeAllViews();

        Typeface bold = UiTheme.boldFont(activity);

        // Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(activity);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = dp(16);
        band.setPadding(pad, pad, pad, pad);

        // 1. Heading: "Add Research Entry" / "Record Editor"
        TextView heading = new TextView(activity);
        heading.setText(isEditMode ? "Record Editor" : "Add Research Entry");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(activity));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        if (isEditMode && editingRecord != null) {
            TextView subTitle = new TextView(activity);
            subTitle.setText(editingRecord.title);
            subTitle.setTextColor(0xffe2e8f0);
            subTitle.setTypeface(bold);
            ScreenTypography.apply(subTitle, 15);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            subParams.topMargin = dp(4);
            band.addView(subTitle, subParams);

            TextView meta = new TextView(activity);
            meta.setText(editingRecord.id + " · Last edit: " + editingRecord.lastEditDate);
            meta.setTextColor(0xff94a3b8);
            meta.setTypeface(UiTheme.bodyFont(activity));
            ScreenTypography.apply(meta, 12);
            LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            metaParams.topMargin = dp(2);
            band.addView(meta, metaParams);
        }

        LinearLayout pageWrapper = new LinearLayout(activity);
        pageWrapper.setOrientation(LinearLayout.VERTICAL);
        pageWrapper.addView(band);
        pageWrapper.addView(rootLayout);
        rootContainer.addView(pageWrapper);

        // 2. Wireframe 7.4: Possible Duplicate Warning Card (if triggered)
        if (showDuplicateWarning) {
            rootLayout.addView(createDuplicateWarningCard());
        }

        // 3. Research Details Card
        rootLayout.addView(createResearchDetailsCard());

        // 4. Team Members Card (7.3)
        rootLayout.addView(createTeamMembersCard());

        // 5. Historical Entry Card
        rootLayout.addView(createHistoricalEntryCard());

        // 6. Action Buttons: Save & Cancel
        Button saveBtn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        saveBtn.setText("Save");
        saveBtn.setAllCaps(false);
        saveBtn.setTextColor(0xffffffff);
        saveBtn.setTypeface(bold);
        ScreenTypography.apply(saveBtn, 15);
        saveBtn.setBackground(UiTheme.ripple(UiTheme.shape(activity, 0xff131c4b, 8, 0), 0x20ffffff));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        saveParams.topMargin = dp(16);
        saveParams.bottomMargin = dp(10);
        rootLayout.addView(saveBtn, saveParams);

        saveBtn.setOnClickListener(v -> handleSave());

        Button cancelBtn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        cancelBtn.setText("Cancel");
        cancelBtn.setAllCaps(false);
        cancelBtn.setTextColor(0xff082d50);
        cancelBtn.setTypeface(bold);
        ScreenTypography.apply(cancelBtn, 15);
        cancelBtn.setBackground(UiTheme.ripple(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER), 0x20082d50));
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        rootLayout.addView(cancelBtn, cancelParams);

        cancelBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onCancel();
            }
        });

        rootContainer.addView(rootLayout);

        // 7. Wireframe 7.5: Stale Data Conflict Modal Overlay
        if (isStaleConflict) {
            View scrim = new View(activity);
            scrim.setBackgroundColor(0x99000000);
            scrim.setClickable(true);
            rootContainer.addView(scrim, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            View modal = createStaleModalCard();
            FrameLayout.LayoutParams mLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mLp.gravity = Gravity.CENTER;
            mLp.leftMargin = dp(24);
            mLp.rightMargin = dp(24);
            rootContainer.addView(modal, mLp);
        }
    }

    private View createStaleModalCard() {
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(24), dp(28), dp(24), dp(24));
        card.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 16, 0));

        // Amber Circle with Warning Icon
        TextView icon = new TextView(activity);
        icon.setText("⚠");
        icon.setTextColor(0xffd97706);
        icon.setTypeface(bold);
        ScreenTypography.apply(icon, 22);
        icon.setGravity(Gravity.CENTER);
        int iSize = dp(48);
        LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(iSize, iSize);
        icon.setLayoutParams(iLp);

        GradientDrawable iBg = new GradientDrawable();
        iBg.setShape(GradientDrawable.OVAL);
        iBg.setColor(Color.parseColor("#FEF3C7"));
        icon.setBackground(iBg);
        card.addView(icon);

        View sp1 = new View(activity);
        sp1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(16)));
        card.addView(sp1);

        // Title
        TextView title = new TextView(activity);
        title.setText("This research record was updated. Reload before saving.");
        title.setTextColor(0xff082d50);
        title.setTypeface(bold);
        title.setGravity(Gravity.CENTER);
        ScreenTypography.apply(title, 18);
        card.addView(title);

        View sp2 = new View(activity);
        sp2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8)));
        card.addView(sp2);

        // Body message
        TextView body = new TextView(activity);
        body.setText("Your changes have not been saved. Reload the latest version, then review it before making changes.");
        body.setTextColor(0xff586b83);
        body.setTypeface(regular);
        body.setGravity(Gravity.CENTER);
        ScreenTypography.apply(body, 13);
        card.addView(body);

        View sp3 = new View(activity);
        sp3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(20)));
        card.addView(sp3);

        // Reload button
        Button reloadBtn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        reloadBtn.setText("⟳ Reload");
        reloadBtn.setAllCaps(false);
        reloadBtn.setTextColor(0xffffffff);
        reloadBtn.setTypeface(bold);
        ScreenTypography.apply(reloadBtn, 15);
        reloadBtn.setBackground(UiTheme.ripple(UiTheme.shape(activity, 0xff131c4b, 8, 0), 0x20ffffff));
        reloadBtn.setMinHeight(dp(46));
        reloadBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
        reloadBtn.setOnClickListener(v -> {
            isStaleConflict = false;
            if (callbacks != null) {
                callbacks.onCancel(); // Reload -> 7.2 Research Record Detail
            }
        });
        card.addView(reloadBtn);

        return card;
    }

    private View createDuplicateWarningCard() {
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(UiTheme.shape(activity, 0xfffff7ed, 8, 0xfffed7aa));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(16);
        card.setLayoutParams(params);

        TextView title = new TextView(activity);
        title.setText("⚠ Possible duplicate research record.");
        title.setTextColor(0xffc2410c);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 15);
        card.addView(title);

        // Existing match card
        LinearLayout matchCard = new LinearLayout(activity);
        matchCard.setOrientation(LinearLayout.VERTICAL);
        matchCard.setPadding(dp(12), dp(10), dp(12), dp(10));
        matchCard.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER));
        LinearLayout.LayoutParams matchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        matchParams.topMargin = dp(8);
        matchParams.bottomMargin = dp(8);
        card.addView(matchCard, matchParams);

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView recId = new TextView(activity);
        recId.setText("REG-2024-012");
        recId.setTextColor(0xff185d9d);
        recId.setTypeface(bold);
        ScreenTypography.apply(recId, 13);
        topRow.addView(recId, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView stage = new TextView(activity);
        stage.setText("Completed");
        stage.setTextColor(0xff16a34a);
        stage.setTypeface(bold);
        ScreenTypography.apply(stage, 11);
        stage.setPadding(dp(8), dp(2), dp(8), dp(2));
        GradientDrawable sBg = new GradientDrawable();
        sBg.setColor(Color.parseColor("#DCFCE7"));
        sBg.setCornerRadius(dp(4));
        stage.setBackground(sBg);
        topRow.addView(stage);
        matchCard.addView(topRow);

        TextView recTitle = new TextView(activity);
        recTitle.setText("AI Tools in Student Engagement");
        recTitle.setTextColor(0xff082d50);
        recTitle.setTypeface(bold);
        ScreenTypography.apply(recTitle, 14);
        matchCard.addView(recTitle);

        TextView recMeta = new TextView(activity);
        recMeta.setText("Dr. J. Oke · AY 2024-2025 · College of Computing and Information Sciences");
        recMeta.setTextColor(0xff64748b);
        recMeta.setTypeface(regular);
        ScreenTypography.apply(recMeta, 12);
        matchCard.addView(recMeta);

        TextView compareNote = new TextView(activity);
        compareNote.setText("Compare normalized title and team members before creating a separate record.");
        compareNote.setTextColor(0xffc2410c);
        compareNote.setTypeface(regular);
        ScreenTypography.apply(compareNote, 12);
        LinearLayout.LayoutParams compareParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        compareParams.bottomMargin = dp(10);
        card.addView(compareNote, compareParams);

        // Confirmation Checkbox
        duplicateConfirmCheckBox = new CheckBox(activity);
        duplicateConfirmCheckBox.setText("I confirm this is a separate record");
        duplicateConfirmCheckBox.setTextColor(0xff082d50);
        duplicateConfirmCheckBox.setTypeface(bold);
        ScreenTypography.apply(duplicateConfirmCheckBox, 13);
        duplicateConfirmCheckBox.setChecked(duplicateReasonError); // Checked in 7.4 error, unchecked in 7.4 default
        card.addView(duplicateConfirmCheckBox);

        // Separate-record reason
        TextView reasonLabel = new TextView(activity);
        reasonLabel.setText("Separate-record reason *");
        reasonLabel.setTextColor(0xffc2410c);
        reasonLabel.setTypeface(bold);
        ScreenTypography.apply(reasonLabel, 13);
        LinearLayout.LayoutParams rLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rLabelParams.topMargin = dp(8);
        card.addView(reasonLabel, rLabelParams);

        duplicateReasonInput = new EditText(activity);
        duplicateReasonInput.setHint("Enter a reason to create a separate record.");
        duplicateReasonInput.setHintTextColor(0xff94a3b8);
        duplicateReasonInput.setTextColor(0xff082d50);
        duplicateReasonInput.setTypeface(regular);
        ScreenTypography.apply(duplicateReasonInput, 13);
        duplicateReasonInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        duplicateReasonInput.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, duplicateReasonError ? 0xffdc2626 : 0xfffed7aa));
        LinearLayout.LayoutParams reasonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        reasonParams.topMargin = dp(4);
        card.addView(duplicateReasonInput, reasonParams);

        if (duplicateReasonError) {
            duplicateReasonErrorText = new TextView(activity);
            duplicateReasonErrorText.setText("⚠ Enter a reason to create a separate record.");
            duplicateReasonErrorText.setTextColor(0xffdc2626);
            duplicateReasonErrorText.setTypeface(bold);
            ScreenTypography.apply(duplicateReasonErrorText, 12);
            LinearLayout.LayoutParams errParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            errParams.topMargin = dp(4);
            card.addView(duplicateReasonErrorText, errParams);
        }

        TextView note = new TextView(activity);
        note.setText("Creating a separate record requires explicit confirmation and a reason.");
        note.setTextColor(0xffc2410c);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 12);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = dp(4);
        card.addView(note, noteParams);

        return card;
    }

    private View createResearchDetailsCard() {
        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Research details");
        title.setTextColor(0xff082d50);
        title.setTypeface(UiTheme.boldFont(activity));
        ScreenTypography.apply(title, 16);
        title.setPadding(0, 0, 0, dp(12));
        card.addView(title);

        // Research title *
        addFormFieldLabel(card, "Research title *");
        titleInput = createStyledInput("Impact of AI on Student Engagement");
        titleInput.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(200)});
        card.addView(titleInput);
        addFormFieldLabel(card, "Abstract *");
        abstractInput = createStyledInput("Describe the study for duplicate review");
        abstractInput.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(4000)});
        abstractInput.setMinLines(3);
        card.addView(abstractInput);

        // College or institute *
        addFormFieldLabel(card, "College or institute *");
        collegeSpinner = new Spinner(activity);
        ArrayAdapter<String> collegeAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, COLLEGES);
        collegeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        collegeSpinner.setAdapter(collegeAdapter);
        styleSpinner(collegeSpinner);
        card.addView(collegeSpinner);

        // Academic Year *
        addFormFieldLabel(card, "Academic Year *");
        academicYearSpinner = new Spinner(activity);
        ArrayAdapter<String> ayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, ACADEMIC_YEARS);
        ayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        academicYearSpinner.setAdapter(ayAdapter);
        styleSpinner(academicYearSpinner);
        card.addView(academicYearSpinner);

        // Faculty adviser
        addFormFieldLabel(card, "Faculty adviser");
        adviserInput = createStyledInput("Dr. Maria A. Santos");
        card.addView(adviserInput);

        TextView adviserNote = new TextView(activity);
        adviserNote.setText("Required for Student records; optional for Faculty.");
        adviserNote.setTextColor(0xff64748b);
        adviserNote.setTypeface(UiTheme.bodyFont(activity));
        ScreenTypography.apply(adviserNote, 12);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = dp(2);
        noteParams.bottomMargin = dp(12);
        card.addView(adviserNote, noteParams);

        // Registrant category
        addFormFieldLabel(card, "Registrant category");
        categorySpinner = new Spinner(activity);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(catAdapter);
        styleSpinner(categorySpinner);
        card.addView(categorySpinner);

        return card;
    }

    private View createTeamMembersCard() {
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Team members");
        title.setTextColor(0xff082d50);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 16);
        card.addView(title);

        TextView subtitle = new TextView(activity);
        subtitle.setText("Select existing Researcher accounts only.");
        subtitle.setTextColor(0xff64748b);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 12);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dp(2);
        subParams.bottomMargin = dp(12);
        card.addView(subtitle, subParams);

        teamListContainer = new LinearLayout(activity);
        teamListContainer.setOrientation(LinearLayout.VERTICAL);
        renderTeamMembersList();
        card.addView(teamListContainer);

        // Add team member section
        TextView addLabel = new TextView(activity);
        addLabel.setText("Add team member");
        addLabel.setTextColor(0xff082d50);
        addLabel.setTypeface(bold);
        ScreenTypography.apply(addLabel, 13);
        LinearLayout.LayoutParams addLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addLabelParams.topMargin = dp(12);
        card.addView(addLabel, addLabelParams);

        boolean isAlreadyMember = "already_member".equals(teamErrorType);
        boolean isNoSuchAccount = "no_such_account".equals(teamErrorType);
        boolean hasError = isAlreadyMember || isNoSuchAccount;

        // Search Input Box with Icon
        FrameLayout searchContainer = new FrameLayout(activity);
        searchContainer.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, hasError ? 0xffef4444 : UiTheme.BORDER));
        LinearLayout.LayoutParams searchBoxParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        searchBoxParams.topMargin = dp(4);
        searchContainer.setLayoutParams(searchBoxParams);

        ImageView searchIcon = new ImageView(activity);
        searchIcon.setImageResource(R.drawable.ic_researcher_search);
        searchIcon.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams sIconLp = new FrameLayout.LayoutParams(dp(18), dp(18));
        sIconLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        sIconLp.leftMargin = dp(12);
        searchContainer.addView(searchIcon, sIconLp);

        teamSearchInput = new EditText(activity);
        if (isAlreadyMember) {
            teamSearchInput.setText("Juan");
        } else if (isNoSuchAccount) {
            teamSearchInput.setText("Mark Villanueva");
        } else {
            teamSearchInput.setText("Pet");
        }
        teamSearchInput.setTextColor(0xff082d50);
        teamSearchInput.setTypeface(regular);
        teamSearchInput.setBackground(null);
        ScreenTypography.apply(teamSearchInput, 13);
        teamSearchInput.setPadding(dp(38), 0, dp(12), 0);
        teamSearchInput.setSingleLine(true);
        FrameLayout.LayoutParams sInputLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        searchContainer.addView(teamSearchInput, sInputLp);

        card.addView(searchContainer);

        // Suggestions / Error Container
        if (isAlreadyMember) {
            // Error: already member
            LinearLayout sugBox = new LinearLayout(activity);
            sugBox.setOrientation(LinearLayout.VERTICAL);
            sugBox.setBackground(UiTheme.shape(activity, 0xfff8fafc, 8, 0xffe2e8f0));
            sugBox.setPadding(dp(10), dp(8), dp(10), dp(8));
            LinearLayout.LayoutParams sugParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            sugParams.topMargin = dp(6);
            sugBox.setLayoutParams(sugParams);

            LinearLayout itemRow = new LinearLayout(activity);
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView avatar = new TextView(activity);
            avatar.setText("JI");
            avatar.setTextColor(0xff0284c7);
            avatar.setTypeface(bold);
            ScreenTypography.apply(avatar, 12);
            avatar.setGravity(Gravity.CENTER);
            GradientDrawable avBg = new GradientDrawable();
            avBg.setShape(GradientDrawable.OVAL);
            avBg.setColor(Color.parseColor("#E0F2FE"));
            avatar.setBackground(avBg);
            LinearLayout.LayoutParams avParams = new LinearLayout.LayoutParams(dp(32), dp(32));
            avParams.rightMargin = dp(10);
            itemRow.addView(avatar, avParams);

            LinearLayout textCol = new LinearLayout(activity);
            textCol.setOrientation(LinearLayout.VERTICAL);

            TextView name = new TextView(activity);
            name.setText("Juan D. Inlwan");
            name.setTextColor(0xff082d50);
            name.setTypeface(bold);
            ScreenTypography.apply(name, 13);
            textCol.addView(name);

            TextView sub = new TextView(activity);
            sub.setText("Student · CCIS · Already added");
            sub.setTextColor(0xff64748b);
            sub.setTypeface(regular);
            ScreenTypography.apply(sub, 12);
            textCol.addView(sub);

            itemRow.addView(textCol);
            sugBox.addView(itemRow);
            card.addView(sugBox);

            TextView err = new TextView(activity);
            err.setText("⚠ This researcher is already a team member.");
            err.setTextColor(0xffc2410c);
            err.setTypeface(bold);
            ScreenTypography.apply(err, 12);
            LinearLayout.LayoutParams errParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            errParams.topMargin = dp(4);
            card.addView(err, errParams);
        } else if (isNoSuchAccount) {
            // Error: no such account
            TextView err = new TextView(activity);
            err.setText("⚠ Select an existing Researcher account.");
            err.setTextColor(0xffc2410c);
            err.setTypeface(bold);
            ScreenTypography.apply(err, 12);
            LinearLayout.LayoutParams errParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            errParams.topMargin = dp(4);
            card.addView(err, errParams);
        } else {
            // Team Picker (Peter Parker suggestion)
            LinearLayout sugBox = new LinearLayout(activity);
            sugBox.setOrientation(LinearLayout.VERTICAL);
            sugBox.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER));
            sugBox.setPadding(dp(10), dp(8), dp(10), dp(8));
            LinearLayout.LayoutParams sugParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            sugParams.topMargin = dp(6);
            sugBox.setLayoutParams(sugParams);

            LinearLayout itemRow = new LinearLayout(activity);
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView avatar = new TextView(activity);
            avatar.setText("PP");
            avatar.setTextColor(0xff0284c7);
            avatar.setTypeface(bold);
            ScreenTypography.apply(avatar, 12);
            avatar.setGravity(Gravity.CENTER);
            GradientDrawable avBg = new GradientDrawable();
            avBg.setShape(GradientDrawable.OVAL);
            avBg.setColor(Color.parseColor("#E0F2FE"));
            avatar.setBackground(avBg);
            LinearLayout.LayoutParams avParams = new LinearLayout.LayoutParams(dp(32), dp(32));
            avParams.rightMargin = dp(10);
            itemRow.addView(avatar, avParams);

            LinearLayout textCol = new LinearLayout(activity);
            textCol.setOrientation(LinearLayout.VERTICAL);

            TextView name = new TextView(activity);
            name.setText("Peter Parker");
            name.setTextColor(0xff082d50);
            name.setTypeface(bold);
            ScreenTypography.apply(name, 13);
            textCol.addView(name);

            TextView sub = new TextView(activity);
            sub.setText("Faculty · CCIS");
            sub.setTextColor(0xff64748b);
            sub.setTypeface(regular);
            ScreenTypography.apply(sub, 12);
            textCol.addView(sub);

            itemRow.addView(textCol);
            sugBox.addView(itemRow);

            sugBox.setOnClickListener(v -> {
                currentTeam.add(new StaffRegistryData.TeamMember("Peter Parker", "Co-author", "Faculty", "PP", "CCIS"));
                teamSearchInput.setText("");
                renderTeamMembersList();
                Toast.makeText(activity, "Peter Parker added as Co-author.", Toast.LENGTH_SHORT).show();
            });

            card.addView(sugBox);
        }

        return card;
    }

    private void renderTeamMembersList() {
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        teamListContainer.removeAllViews();
        for (int i = 0; i < currentTeam.size(); i++) {
            StaffRegistryData.TeamMember member = currentTeam.get(i);
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));

            TextView avatar = new TextView(activity);
            avatar.setText(member.initials);
            avatar.setTextColor(0xff0284c7);
            avatar.setTypeface(bold);
            ScreenTypography.apply(avatar, 12);
            avatar.setGravity(Gravity.CENTER);
            GradientDrawable avBg = new GradientDrawable();
            avBg.setShape(GradientDrawable.OVAL);
            avBg.setColor(Color.parseColor("#E0F2FE"));
            avatar.setBackground(avBg);
            LinearLayout.LayoutParams avParams = new LinearLayout.LayoutParams(dp(36), dp(36));
            avParams.rightMargin = dp(10);
            row.addView(avatar, avParams);

            LinearLayout textCol = new LinearLayout(activity);
            textCol.setOrientation(LinearLayout.VERTICAL);

            TextView nameView = new TextView(activity);
            nameView.setText(member.name);
            nameView.setTextColor(0xff082d50);
            nameView.setTypeface(bold);
            ScreenTypography.apply(nameView, 14);
            textCol.addView(nameView);

            TextView subView = new TextView(activity);
            subView.setText(member.category + " · " + member.role);
            subView.setTextColor(0xff64748b);
            subView.setTypeface(regular);
            ScreenTypography.apply(subView, 12);
            textCol.addView(subView);

            row.addView(textCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView removeBtn = new TextView(activity);
            removeBtn.setText("✕");
            removeBtn.setTextColor(0xff64748b);
            removeBtn.setTypeface(bold);
            ScreenTypography.apply(removeBtn, 14);
            removeBtn.setPadding(dp(8), dp(4), dp(8), dp(4));
            removeBtn.setClickable(true);
            removeBtn.setFocusable(true);
            final int idx = i;
            removeBtn.setOnClickListener(v -> {
                currentTeam.remove(idx);
                renderTeamMembersList();
            });
            row.addView(removeBtn);

            teamListContainer.addView(row);
        }
    }

    private View createHistoricalEntryCard() {
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Historical entry");
        title.setTextColor(0xff082d50);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 16);
        title.setPadding(0, 0, 0, dp(12));
        card.addView(title);

        addFormFieldLabel(card, "Entry type");
        Spinner entryTypeSpinner = new Spinner(activity);
        ArrayAdapter<String> etAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, ENTRY_TYPES);
        etAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entryTypeSpinner.setAdapter(etAdapter);
        styleSpinner(entryTypeSpinner);
        card.addView(entryTypeSpinner);

        addFormFieldLabel(card, "Source reference");
        EditText sourceRefInput = createStyledInput("");
        sourceRefInput.setHint("Enter the archival source reference");
        sourceRefInput.setHintTextColor(0xff94a3b8);
        card.addView(sourceRefInput);

        addFormFieldLabel(card, "Source Institution");
        EditText sourceInstInput = createStyledInput("University of Makati");
        card.addView(sourceInstInput);

        addFormFieldLabel(card, "Original registration date");
        EditText origDateInput = createStyledInput("Unknown — not recorded");
        card.addView(origDateInput);

        TextView note = new TextView(activity);
        note.setText("Keep unknown historical values unknown. Do not substitute the encoding date.");
        note.setTextColor(0xff64748b);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 12);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = dp(2);
        card.addView(note, noteParams);

        return card;
    }

    private void addFormFieldLabel(LinearLayout parent, String label) {
        TextView view = new TextView(activity);
        view.setText(label);
        view.setTextColor(0xff082d50);
        view.setTypeface(UiTheme.boldFont(activity));
        ScreenTypography.apply(view, 13);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        parent.addView(view, params);
    }

    private EditText createStyledInput(String defaultText) {
        EditText input = new EditText(activity);
        input.setText(defaultText);
        input.setTextColor(0xff082d50);
        input.setTypeface(UiTheme.bodyFont(activity));
        ScreenTypography.apply(input, 13);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(4);
        params.bottomMargin = dp(8);
        input.setLayoutParams(params);
        return input;
    }

    private void styleSpinner(Spinner spinner) {
        spinner.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER));
        spinner.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(4);
        params.bottomMargin = dp(8);
        spinner.setLayoutParams(params);
    }

    private LinearLayout createBaseCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(12);
        card.setLayoutParams(params);
        return card;
    }

    private void handleSave() {
        if (showDuplicateWarning) {
            String reason = duplicateReasonInput != null ? duplicateReasonInput.getText().toString().trim() : "";
            if (TextUtils.isEmpty(reason)) {
                showPreview7_4_ErrorReasonMissing();
                return;
            }
        } else if (!isEditMode) {
            String title = titleInput.getText().toString().trim();
            String abstractText = abstractInput.getText().toString().trim();
            if (title.isEmpty() || abstractText.isEmpty()) {
                new android.app.AlertDialog.Builder(activity).setMessage("Enter a title and abstract before duplicate review.").setPositiveButton("OK", null).show();
                return;
            }
            if (reviewInProgress || callbacks == null) return;
            reviewInProgress = true;
            callbacks.onReviewDuplicates(title, abstractText, (data, error) -> {
                reviewInProgress = false;
                if (error != null || data == null) {
                    new android.app.AlertDialog.Builder(activity).setMessage("Duplicate review is unavailable. Please try again.").setPositiveButton("OK", null).show();
                    return;
                }
                try {
                    org.json.JSONArray matches = new org.json.JSONObject(data).getJSONArray("matches");
                    StringBuilder message = new StringBuilder("Possible matches for staff review:\n\n");
                    if (matches.length() == 0) message.append("No matches found in the current shortlist.");
                    for (int i = 0; i < matches.length(); i++) {
                        org.json.JSONObject match = matches.getJSONObject(i);
                        message.append(i + 1).append(". ").append(match.optString("title"))
                                .append(" (score ").append(String.format(java.util.Locale.ROOT, "%.3f", match.getDouble("tfidf_score"))).append(")");
                        if (match.optBoolean("alert")) message.append(" — review suggested");
                        message.append("\n");
                    }
                    new android.app.AlertDialog.Builder(activity).setTitle("Duplicate review")
                            .setMessage(message.toString()).setNegativeButton("Back", null)
                            .setPositiveButton("Continue", (dialog, which) -> saveEntry()).show();
                } catch (org.json.JSONException invalid) {
                    new android.app.AlertDialog.Builder(activity).setMessage("Duplicate review is unavailable. Please try again.").setPositiveButton("OK", null).show();
                }
            });
            return;
        }

        saveEntry();
    }

    private void saveEntry() {
        if (callbacks != null) {
            StaffRegistryData.RegistryRecordItem item = editingRecord != null ? editingRecord :
                    new StaffRegistryData.RegistryRecordItem(
                            "REG-2026-095",
                            titleInput.getText().toString(),
                            "Registered",
                            "Nov 18, 2026",
                            ACADEMIC_YEARS[academicYearSpinner.getSelectedItemPosition()],
                            "LeBron R. James, et al.",
                            COLLEGES[collegeSpinner.getSelectedItemPosition()],
                            adviserInput.getText().toString(),
                            CATEGORIES[categorySpinner.getSelectedItemPosition()],
                            "Nov 18, 2026 by Staff",
                            currentTeam,
                            new ArrayList<>(),
                            new ArrayList<>(),
                            null,
                            new ArrayList<>()
                    );
            callbacks.onSaveEntry(item);
        }
    }

    // Public preview setters for Wireframes 7.3, 7.4, 7.5
    public void showPreview7_3_TeamPicker() {
        resetToDefaultAdd();
        this.teamErrorType = "none";
        buildUi();
    }

    public void showPreview7_3_ErrorAlreadyMember() {
        resetToDefaultAdd();
        this.teamErrorType = "already_member";
        buildUi();
    }

    public void showPreview7_3_ErrorNoSuchAccount() {
        resetToDefaultAdd();
        this.teamErrorType = "no_such_account";
        buildUi();
    }

    public void showPreview7_4_PossibleDuplicate() {
        resetToDefaultAdd();
        this.showDuplicateWarning = true;
        this.duplicateReasonError = false;
        buildUi();
    }

    public void showPreview7_4_ErrorReasonMissing() {
        this.showDuplicateWarning = true;
        this.duplicateReasonError = true;
        buildUi();
    }

    public void showPreview7_5_StaleDataConflict() {
        this.isEditMode = true;
        this.isStaleConflict = true;
        this.showDuplicateWarning = false;
        this.duplicateReasonError = false;
        this.teamErrorType = "none";
        buildUi();
    }
}
