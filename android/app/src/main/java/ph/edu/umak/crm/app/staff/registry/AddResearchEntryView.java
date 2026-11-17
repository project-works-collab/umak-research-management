package ph.edu.umak.crm.app.staff.registry;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
        void onReloadRecord(StaffRegistryData.RegistryRecordItem record);
    }

    private final Activity activity;
    private final Callbacks callbacks;

    private boolean isEditMode = false;
    private boolean showStaleConflict = false;
    private boolean showDuplicateWarning = false;
    private boolean duplicateReasonError = false;
    private String teamErrorType = "none"; // "none", "already_member", "no_such_account"

    private LinearLayout rootLayout;
    private EditText titleInput;
    private Spinner collegeSpinner;
    private EditText academicYearInput;
    private EditText adviserInput;
    private Spinner categorySpinner;

    private LinearLayout teamListContainer;
    private AutoCompleteTextView teamSearchInput;
    private TextView teamErrorBanner;
    private LinearLayout alreadyMemberItemView;

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

    private static final String[] CATEGORIES = {
            "Student",
            "Faculty"
    };

    private static final String[] CANDIDATE_RESEARCHERS = {
            "Peter Parker (Faculty · CCIS)",
            "Juan D. Inlwan (Student · CCIS)",
            "Maria Santos (Faculty · CCIS)",
            "Mark Villanueva (Researcher)",
            "Alan Turing (Faculty · CET)"
    };

    public AddResearchEntryView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        resetToDefaultAdd();
        buildUi();
    }

    public void setEditRecord(StaffRegistryData.RegistryRecordItem record, boolean staleConflict) {
        this.isEditMode = true;
        this.editingRecord = record;
        this.showStaleConflict = staleConflict;
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
        this.editingRecord = null;
        this.showStaleConflict = false;
        this.showDuplicateWarning = false;
        this.duplicateReasonError = false;
        this.teamErrorType = "none";

        currentTeam.clear();
        currentTeam.add(new StaffRegistryData.TeamMember("LeBron R. James", "Principal author", "Student", "LJ", "CCIS"));
        currentTeam.add(new StaffRegistryData.TeamMember("Juan D. Inlwan", "Co-author", "Student", "JI", "CCIS"));
    }

    public ViewGroup getContentLayout() {
        if (rootLayout != null && rootLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) rootLayout.getParent()).removeView(rootLayout);
        }
        return rootLayout;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    private void buildUi() {
        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));

        // 1. Header
        TextView topHeader = new TextView(activity);
        topHeader.setText("Center for Research Management");
        topHeader.setTextColor(UiTheme.NAVY);
        topHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        topHeader.setTypeface(Typeface.DEFAULT_BOLD);
        rootLayout.addView(topHeader);

        TextView title = new TextView(activity);
        title.setText(isEditMode ? "Record Editor" : "Add Research Entry");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(4);
        titleParams.bottomMargin = isEditMode ? dp(4) : dp(16);
        rootLayout.addView(title, titleParams);

        if (isEditMode && editingRecord != null) {
            TextView subTitle = new TextView(activity);
            subTitle.setText(editingRecord.title);
            subTitle.setTextColor(UiTheme.NAVY);
            subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            subTitle.setTypeface(Typeface.DEFAULT_BOLD);
            rootLayout.addView(subTitle);

            TextView meta = new TextView(activity);
            meta.setText(editingRecord.id + " · Last edit: " + editingRecord.lastEditDate);
            meta.setTextColor(UiTheme.MUTED);
            meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            metaParams.bottomMargin = dp(16);
            rootLayout.addView(meta, metaParams);
        }

        // 2. Wireframe 7.5: Stale Data Conflict Banner
        if (showStaleConflict) {
            rootLayout.addView(createStaleConflictBanner());
        }

        // 3. Wireframe 7.4: Possible Duplicate Warning Card
        if (showDuplicateWarning) {
            rootLayout.addView(createDuplicateWarningCard());
        }

        // 4. Research Details Card
        rootLayout.addView(createResearchDetailsCard());

        // 5. Team Members Card (7.3)
        rootLayout.addView(createTeamMembersCard());

        // 6. Historical Entry Card
        rootLayout.addView(createHistoricalEntryCard());

        // 7. Actions (Save & Cancel)
        Button saveBtn = new Button(activity);
        saveBtn.setText("Save");
        saveBtn.setTextColor(UiTheme.WHITE);
        saveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        saveBtn.setTypeface(Typeface.DEFAULT_BOLD);
        saveBtn.setAllCaps(false);
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(UiTheme.NAVY);
        saveBg.setCornerRadius(dp(8));
        saveBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), saveBg, null));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        saveParams.topMargin = dp(16);
        saveParams.bottomMargin = dp(12);
        rootLayout.addView(saveBtn, saveParams);

        saveBtn.setOnClickListener(v -> handleSave());

        Button cancelBtn = new Button(activity);
        cancelBtn.setText("Cancel");
        cancelBtn.setTextColor(UiTheme.NAVY);
        cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        cancelBtn.setTypeface(Typeface.DEFAULT_BOLD);
        cancelBtn.setAllCaps(false);
        GradientDrawable cancelBg = new GradientDrawable();
        cancelBg.setColor(UiTheme.WHITE);
        cancelBg.setCornerRadius(dp(8));
        cancelBg.setStroke(dp(1), UiTheme.BORDER);
        cancelBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22131c4b), cancelBg, null));
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        rootLayout.addView(cancelBtn, cancelParams);

        cancelBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onCancel();
            }
        });
    }

    private View createStaleConflictBanner() {
        LinearLayout banner = new LinearLayout(activity);
        banner.setOrientation(LinearLayout.VERTICAL);
        banner.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.PEACH_BG);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), UiTheme.PEACH_BORDER);
        banner.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(16);
        banner.setLayoutParams(params);

        TextView title = new TextView(activity);
        title.setText("This research record was updated. Reload before saving.");
        title.setTextColor(UiTheme.PEACH_TEXT);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        banner.addView(title);

        TextView body = new TextView(activity);
        body.setText("Your changes have not been saved. Reload the latest version, then review it before making changes.");
        body.setTextColor(UiTheme.INK);
        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bodyParams.topMargin = dp(6);
        bodyParams.bottomMargin = dp(12);
        banner.addView(body, bodyParams);

        Button reloadBtn = new Button(activity);
        reloadBtn.setText("Reload");
        reloadBtn.setTextColor(UiTheme.WHITE);
        reloadBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        reloadBtn.setTypeface(Typeface.DEFAULT_BOLD);
        reloadBtn.setAllCaps(false);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(UiTheme.PEACH_TEXT);
        btnBg.setCornerRadius(dp(6));
        reloadBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), btnBg, null));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(38));
        banner.addView(reloadBtn, btnParams);

        reloadBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onReloadRecord(editingRecord);
            }
        });

        return banner;
    }

    private View createDuplicateWarningCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.AMBER_BG);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), UiTheme.AMBER_BORDER);
        card.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(16);
        card.setLayoutParams(params);

        TextView title = new TextView(activity);
        title.setText("Possible duplicate research record.");
        title.setTextColor(UiTheme.AMBER_TEXT);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(title);

        // Existing match card
        LinearLayout matchCard = new LinearLayout(activity);
        matchCard.setOrientation(LinearLayout.VERTICAL);
        matchCard.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable matchBg = new GradientDrawable();
        matchBg.setColor(UiTheme.WHITE);
        matchBg.setCornerRadius(dp(8));
        matchBg.setStroke(dp(1), UiTheme.BORDER);
        matchCard.setBackground(matchBg);
        LinearLayout.LayoutParams matchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        matchParams.topMargin = dp(8);
        matchParams.bottomMargin = dp(8);
        card.addView(matchCard, matchParams);

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView recId = new TextView(activity);
        recId.setText("REG-2024-012");
        recId.setTextColor(UiTheme.NAVY);
        recId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        recId.setTypeface(Typeface.DEFAULT_BOLD);
        topRow.addView(recId, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView stage = new TextView(activity);
        stage.setText("Stage Completed");
        stage.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
        stage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        stage.setTypeface(Typeface.DEFAULT_BOLD);
        topRow.addView(stage);
        matchCard.addView(topRow);

        TextView recTitle = new TextView(activity);
        recTitle.setText("AI Tools in Student Engagement");
        recTitle.setTextColor(UiTheme.NAVY);
        recTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        recTitle.setTypeface(Typeface.DEFAULT_BOLD);
        matchCard.addView(recTitle);

        TextView recMeta = new TextView(activity);
        recMeta.setText("Dr. J. Oke · AY 2024-2025 · College of Computing and Information Sciences");
        recMeta.setTextColor(UiTheme.MUTED);
        recMeta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        matchCard.addView(recMeta);

        TextView compareNote = new TextView(activity);
        compareNote.setText("Compare normalized title and team members before creating a separate record.");
        compareNote.setTextColor(UiTheme.INK);
        compareNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams compareParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        compareParams.bottomMargin = dp(12);
        card.addView(compareNote, compareParams);

        // Confirmation Checkbox
        duplicateConfirmCheckBox = new CheckBox(activity);
        duplicateConfirmCheckBox.setText("I confirm this is a separate record");
        duplicateConfirmCheckBox.setTextColor(UiTheme.NAVY);
        duplicateConfirmCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        duplicateConfirmCheckBox.setChecked(true);
        card.addView(duplicateConfirmCheckBox);

        // Separate-record reason
        TextView reasonLabel = new TextView(activity);
        reasonLabel.setText("Separate-record reason *");
        reasonLabel.setTextColor(duplicateReasonError ? UiTheme.ERROR_RED : UiTheme.NAVY);
        reasonLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        reasonLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams rLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rLabelParams.topMargin = dp(8);
        card.addView(reasonLabel, rLabelParams);

        duplicateReasonInput = new EditText(activity);
        duplicateReasonInput.setHint("Enter a reason to create a separate record.");
        duplicateReasonInput.setHintTextColor(UiTheme.MUTED);
        duplicateReasonInput.setTextColor(UiTheme.INK);
        duplicateReasonInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        duplicateReasonInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable reasonBg = new GradientDrawable();
        reasonBg.setColor(UiTheme.WHITE);
        reasonBg.setCornerRadius(dp(8));
        reasonBg.setStroke(dp(duplicateReasonError ? 2 : 1), duplicateReasonError ? UiTheme.ERROR_RED : UiTheme.BORDER);
        duplicateReasonInput.setBackground(reasonBg);
        LinearLayout.LayoutParams reasonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        reasonParams.topMargin = dp(4);
        card.addView(duplicateReasonInput, reasonParams);

        duplicateReasonErrorText = new TextView(activity);
        duplicateReasonErrorText.setText("Enter a reason to create a separate record.");
        duplicateReasonErrorText.setTextColor(UiTheme.ERROR_RED);
        duplicateReasonErrorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        duplicateReasonErrorText.setVisibility(duplicateReasonError ? View.VISIBLE : View.GONE);
        LinearLayout.LayoutParams errParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        errParams.topMargin = dp(4);
        card.addView(duplicateReasonErrorText, errParams);

        TextView note = new TextView(activity);
        note.setText("Creating a separate record requires explicit confirmation and a reason.");
        note.setTextColor(UiTheme.MUTED);
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
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
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, 0, 0, dp(12));
        card.addView(title);

        // Research title *
        addFormFieldLabel(card, "Research title *");
        titleInput = createStyledInput("Impact of AI on Student Engagement");
        card.addView(titleInput);

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
        academicYearInput = createStyledInput("AY 2026-2027");
        card.addView(academicYearInput);

        // Faculty adviser
        addFormFieldLabel(card, "Faculty adviser");
        adviserInput = createStyledInput("Dr. Maria A. Santos");
        card.addView(adviserInput);

        TextView adviserNote = new TextView(activity);
        adviserNote.setText("Required for Student records; optional for Faculty.");
        adviserNote.setTextColor(UiTheme.MUTED);
        adviserNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
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
        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Team members");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(title);

        TextView subtitle = new TextView(activity);
        subtitle.setText("Select existing Researcher accounts only.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
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
        addLabel.setTextColor(UiTheme.NAVY);
        addLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        addLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams addLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addLabelParams.topMargin = dp(12);
        card.addView(addLabel, addLabelParams);

        teamSearchInput = new AutoCompleteTextView(activity);
        teamSearchInput.setHint("Search researcher name...");
        teamSearchInput.setHintTextColor(UiTheme.MUTED);
        teamSearchInput.setTextColor(UiTheme.INK);
        teamSearchInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        teamSearchInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        teamSearchInput.setThreshold(1);

        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(UiTheme.WHITE);
        searchBg.setCornerRadius(dp(8));
        searchBg.setStroke(dp(1), "no_such_account".equals(teamErrorType) ? UiTheme.ERROR_RED : UiTheme.BORDER);
        teamSearchInput.setBackground(searchBg);

        ArrayAdapter<String> candidateAdapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_dropdown_item_1line, CANDIDATE_RESEARCHERS);
        teamSearchInput.setAdapter(candidateAdapter);

        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        searchParams.topMargin = dp(4);
        card.addView(teamSearchInput, searchParams);

        // Team Picker Errors (Wireframe 7.3 errors)
        teamErrorBanner = new TextView(activity);
        teamErrorBanner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        teamErrorBanner.setTextColor(UiTheme.ERROR_RED);
        teamErrorBanner.setVisibility(View.GONE);
        LinearLayout.LayoutParams errParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        errParams.topMargin = dp(4);
        card.addView(teamErrorBanner, errParams);

        if ("already_member".equals(teamErrorType)) {
            teamErrorBanner.setText("This researcher is already a team member.");
            teamErrorBanner.setVisibility(View.VISIBLE);
            teamSearchInput.setText("Juan");

            alreadyMemberItemView = createAlreadyMemberCard("Juan D. Inlwan", "Student · CCIS · Already added");
            card.addView(alreadyMemberItemView);
        } else if ("no_such_account".equals(teamErrorType)) {
            teamErrorBanner.setText("Select an existing Researcher account.");
            teamErrorBanner.setVisibility(View.VISIBLE);
            teamSearchInput.setText("Mark Villanueva");
        } else {
            teamSearchInput.setText("Pet");
        }

        teamSearchInput.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            handleCandidateSelected(selected);
        });

        return card;
    }

    private LinearLayout createAlreadyMemberCard(String name, String sub) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(8), dp(12), dp(8));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.PEACH_BG);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), UiTheme.PEACH_BORDER);
        row.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(6);
        row.setLayoutParams(params);

        TextView avatar = new TextView(activity);
        avatar.setText("JI");
        avatar.setTextColor(UiTheme.WHITE);
        avatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable avBg = new GradientDrawable();
        avBg.setShape(GradientDrawable.OVAL);
        avBg.setColor(UiTheme.ERROR_RED);
        avatar.setBackground(avBg);
        LinearLayout.LayoutParams avParams = new LinearLayout.LayoutParams(dp(32), dp(32));
        avParams.rightMargin = dp(10);
        row.addView(avatar, avParams);

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView nameView = new TextView(activity);
        nameView.setText(name);
        nameView.setTextColor(UiTheme.ERROR_RED);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(nameView);

        TextView subView = new TextView(activity);
        subView.setText(sub);
        subView.setTextColor(UiTheme.ERROR_RED);
        subView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        textCol.addView(subView);

        row.addView(textCol);
        return row;
    }

    private void handleCandidateSelected(String candidate) {
        if (candidate.contains("Peter Parker")) {
            currentTeam.add(new StaffRegistryData.TeamMember("Peter Parker", "Co-author", "Faculty", "PP", "CCIS"));
            teamSearchInput.setText("");
            renderTeamMembersList();
            Toast.makeText(activity, "Peter Parker added as Co-author.", Toast.LENGTH_SHORT).show();
        } else if (candidate.contains("Juan D. Inlwan")) {
            setTeamErrorState("already_member");
        } else if (candidate.contains("Mark Villanueva")) {
            setTeamErrorState("no_such_account");
        }
    }

    private void renderTeamMembersList() {
        teamListContainer.removeAllViews();
        for (int i = 0; i < currentTeam.size(); i++) {
            StaffRegistryData.TeamMember member = currentTeam.get(i);
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(4), 0, dp(4));

            TextView avatar = new TextView(activity);
            avatar.setText(member.initials);
            avatar.setTextColor(UiTheme.WHITE);
            avatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            avatar.setTypeface(Typeface.DEFAULT_BOLD);
            avatar.setGravity(Gravity.CENTER);
            GradientDrawable avBg = new GradientDrawable();
            avBg.setShape(GradientDrawable.OVAL);
            avBg.setColor(UiTheme.ACCENT);
            avatar.setBackground(avBg);
            LinearLayout.LayoutParams avParams = new LinearLayout.LayoutParams(dp(32), dp(32));
            avParams.rightMargin = dp(10);
            row.addView(avatar, avParams);

            LinearLayout textCol = new LinearLayout(activity);
            textCol.setOrientation(LinearLayout.VERTICAL);

            TextView nameView = new TextView(activity);
            nameView.setText(member.name);
            nameView.setTextColor(UiTheme.NAVY);
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            nameView.setTypeface(Typeface.DEFAULT_BOLD);
            textCol.addView(nameView);

            TextView subView = new TextView(activity);
            subView.setText(member.category + " · " + member.role);
            subView.setTextColor(UiTheme.MUTED);
            subView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            textCol.addView(subView);

            row.addView(textCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            // Remove member button if not principal author
            if (!"Principal author".equalsIgnoreCase(member.role)) {
                TextView removeBtn = new TextView(activity);
                removeBtn.setText("Remove");
                removeBtn.setTextColor(UiTheme.MUTED);
                removeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                removeBtn.setPadding(dp(8), dp(4), dp(8), dp(4));
                removeBtn.setClickable(true);
                removeBtn.setFocusable(true);
                final int idx = i;
                removeBtn.setOnClickListener(v -> {
                    currentTeam.remove(idx);
                    renderTeamMembersList();
                });
                row.addView(removeBtn);
            }

            teamListContainer.addView(row);
        }
    }

    private View createHistoricalEntryCard() {
        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Historical entry");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, 0, 0, dp(12));
        card.addView(title);

        addFormFieldLabel(card, "Entry type");
        EditText entryTypeInput = createStyledInput("Current registration");
        card.addView(entryTypeInput);

        addFormFieldLabel(card, "Source reference");
        EditText sourceRefInput = createStyledInput("Enter the archival source reference");
        card.addView(sourceRefInput);

        addFormFieldLabel(card, "Source Institution");
        EditText sourceInstInput = createStyledInput("University of Makati");
        card.addView(sourceInstInput);

        addFormFieldLabel(card, "Original registration date");
        EditText origDateInput = createStyledInput("Unknown — not recorded");
        card.addView(origDateInput);

        TextView note = new TextView(activity);
        note.setText("Keep unknown historical values unknown. Do not substitute the encoding date.");
        note.setTextColor(UiTheme.MUTED);
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = dp(2);
        card.addView(note, noteParams);

        return card;
    }

    private void addFormFieldLabel(LinearLayout parent, String label) {
        TextView view = new TextView(activity);
        view.setText(label);
        view.setTextColor(UiTheme.NAVY);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        parent.addView(view, params);
    }

    private EditText createStyledInput(String defaultText) {
        EditText input = new EditText(activity);
        input.setText(defaultText);
        input.setTextColor(UiTheme.INK);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), UiTheme.BORDER);
        input.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(4);
        params.bottomMargin = dp(8);
        input.setLayoutParams(params);
        return input;
    }

    private void styleSpinner(Spinner spinner) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), UiTheme.BORDER);
        spinner.setBackground(bg);
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

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), UiTheme.BORDER);
        card.setBackground(bg);

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
                duplicateReasonError = true;
                buildUi();
                return;
            }
        }

        if (callbacks != null) {
            StaffRegistryData.RegistryRecordItem item = editingRecord != null ? editingRecord :
                    new StaffRegistryData.RegistryRecordItem(
                            "REG-2026-095",
                            titleInput.getText().toString(),
                            "Registered",
                            "Nov 18, 2026",
                            academicYearInput.getText().toString(),
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
        resetToDefaultAdd();
        this.showDuplicateWarning = true;
        this.duplicateReasonError = true;
        buildUi();
    }

    public void showPreview7_5_StaleDataConflict(StaffRegistryData.RegistryRecordItem record) {
        setEditRecord(record != null ? record : StaffRegistryData.getMockRecords().get(0), true);
    }

    public void setTeamErrorState(String type) {
        this.teamErrorType = type;
        buildUi();
    }
}
