package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class RegistrationStep1View {

    public interface Listener {
        default void onChanged(Map<String, String> data) {}
        default boolean pickLiveCoAuthor(java.util.function.Consumer<ResearchData.CoAuthorItem> selected) {
            return false;
        }
        void onContinue(Map<String, String> data);
        void onSaveDraft(Map<String, String> data, Button button, PendingStatus feedback);
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;
    private final Button draftBtn;
    private final PendingStatus draftStatus;
    private final ResearchData.UserProfile userProfile;

    private final EditText titleInput;
    private final TextView titleError;
    private final Spinner typeSpinner;
    private final EditText dateInput;
    private final Spinner periodSpinner;
    private final TextView periodError;
    private final TextView adviserLabel;
    private final EditText adviserInput;
    private final TextView adviserNote;
    private final TextView adviserError;

    private final Spinner collegeSpinner;
    private final TextView collegeError;
    private final TextView leadAuthorText;
    private final LinearLayout coAuthorsContainer;

    private boolean isFacultyMode = false;
    private final List<ResearchData.CoAuthorItem> teamMembers = new ArrayList<>();

    private static final String[] TYPES = {"Applied Research", "Basic Research", "Action Research"};
    private static final String[] PERIODS = {"Select academic period", "AY 2026-2027", "AY 2025-2026"};
    private static final String[] COLLEGES = {
            "Select your college or institute",
            "College of Computing and Information Sciences",
            "College of Arts and Letters",
            "College of Science",
            "College of Business and Financial Science",
            "College of Education",
            "College of Technology",
            "College of Tourism and Hospitality Management",
            "Center for Governance and Public Policy",
            "Institute of Imaging Health Sciences",
            "Institute of Nursing",
            "Institute of Pharmacy",
            "School of Law"
    };

    public RegistrationStep1View(Context context, ResearchData.UserProfile profile,
            Map<String, String> initialData, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.userProfile = profile != null ? profile : ResearchData.getDefaultProfile();
        this.isFacultyMode = "Faculty".equalsIgnoreCase(userProfile.category);

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        // Prepopulate sample team members if available
        teamMembers.add(new ResearchData.CoAuthorItem("1", "Juan D. Inlwan", "Student · CCIS", "jinlwan@umak.edu.ph"));

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        addSpacing(context, layout, 8);

        // Step Indicator Pill / Text
        TextView stepIndicator = new TextView(context);
        stepIndicator.setText("Step 1 of 3: General Information");
        stepIndicator.setTextColor(UiTheme.ACCENT);
        stepIndicator.setTypeface(bold);
        ScreenTypography.apply(stepIndicator, 13);
        layout.addView(stepIndicator);

        addSpacing(context, layout, 16);

        // Project Details Card
        LinearLayout projectCard = new LinearLayout(context);
        projectCard.setOrientation(LinearLayout.VERTICAL);
        projectCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        projectCard.setPadding(pad, pad, pad, pad);

        TextView pTitle = new TextView(context);
        pTitle.setText("Project Details");
        pTitle.setTextColor(UiTheme.NAVY);
        pTitle.setTypeface(bold);
        ScreenTypography.apply(pTitle, 16);
        projectCard.addView(pTitle);

        addSpacing(context, projectCard, 12);

        // Title Input
        TextView tLabel = new TextView(context);
        tLabel.setText("Research Title *");
        tLabel.setTextColor(UiTheme.NAVY);
        tLabel.setTypeface(bold);
        ScreenTypography.apply(tLabel, 14);
        projectCard.addView(tLabel);

        addSpacing(context, projectCard, 6);

        titleInput = new EditText(context);
        titleInput.setText(initialData != null ? initialData.getOrDefault("Research Title", "Impact of AI on Student Engagement") : "Impact of AI on Student Engagement");
        titleInput.setHint("Enter the complete research title");
        titleInput.setTextColor(UiTheme.NAVY);
        titleInput.setTypeface(regular);
        ScreenTypography.apply(titleInput, 14);
        titleInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int inpPad = UiTheme.dp(context, 10);
        titleInput.setPadding(inpPad, inpPad, inpPad, inpPad);
        projectCard.addView(titleInput);

        titleError = new TextView(context);
        titleError.setTextColor(UiTheme.ERROR_RED);
        titleError.setTypeface(regular);
        ScreenTypography.apply(titleError, 12);
        titleError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        titleError.setVisibility(View.GONE);
        projectCard.addView(titleError);

        addSpacing(context, projectCard, 12);

        // Research Type
        TextView typeLabel = new TextView(context);
        typeLabel.setText("Research Type");
        typeLabel.setTextColor(UiTheme.NAVY);
        typeLabel.setTypeface(bold);
        ScreenTypography.apply(typeLabel, 14);
        projectCard.addView(typeLabel);

        addSpacing(context, projectCard, 6);

        typeSpinner = new Spinner(context);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, TYPES);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        projectCard.addView(typeSpinner);

        addSpacing(context, projectCard, 12);

        // Target Completion Date
        TextView dateLabel = new TextView(context);
        dateLabel.setText("Target Completion Date");
        dateLabel.setTextColor(UiTheme.NAVY);
        dateLabel.setTypeface(bold);
        ScreenTypography.apply(dateLabel, 14);
        projectCard.addView(dateLabel);

        addSpacing(context, projectCard, 6);

        dateInput = new EditText(context);
        dateInput.setText("2027-03-15");
        dateInput.setHint("YYYY-MM-DD");
        dateInput.setTextColor(UiTheme.NAVY);
        dateInput.setTypeface(regular);
        ScreenTypography.apply(dateInput, 14);
        dateInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        dateInput.setPadding(inpPad, inpPad, inpPad, inpPad);
        dateInput.setSingleLine(true);
        projectCard.addView(dateInput);

        addSpacing(context, projectCard, 12);

        // Academic period
        TextView periodLabel = new TextView(context);
        periodLabel.setText("Academic period *");
        periodLabel.setTextColor(UiTheme.NAVY);
        periodLabel.setTypeface(bold);
        ScreenTypography.apply(periodLabel, 14);
        projectCard.addView(periodLabel);

        addSpacing(context, projectCard, 6);

        periodSpinner = new Spinner(context);
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, PERIODS);
        periodSpinner.setAdapter(periodAdapter);
        periodSpinner.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        periodSpinner.setSelection(1); // AY 2026-2027
        projectCard.addView(periodSpinner);

        periodError = new TextView(context);
        periodError.setTextColor(UiTheme.ERROR_RED);
        periodError.setTypeface(regular);
        ScreenTypography.apply(periodError, 12);
        periodError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        periodError.setVisibility(View.GONE);
        projectCard.addView(periodError);

        addSpacing(context, projectCard, 12);

        // Faculty adviser
        adviserLabel = new TextView(context);
        adviserLabel.setText(isFacultyMode ? "Faculty adviser" : "Faculty adviser *");
        adviserLabel.setTextColor(UiTheme.NAVY);
        adviserLabel.setTypeface(bold);
        ScreenTypography.apply(adviserLabel, 14);
        projectCard.addView(adviserLabel);

        addSpacing(context, projectCard, 6);

        adviserInput = new EditText(context);
        adviserInput.setText("Dr. Maria A. Santos");
        adviserInput.setHint("Enter adviser name");
        adviserInput.setTextColor(UiTheme.NAVY);
        adviserInput.setTypeface(regular);
        ScreenTypography.apply(adviserInput, 14);
        adviserInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        adviserInput.setPadding(inpPad, inpPad, inpPad, inpPad);
        adviserInput.setSingleLine(true);
        projectCard.addView(adviserInput);

        adviserNote = new TextView(context);
        adviserNote.setText(isFacultyMode ? "Optional for Faculty" : "Required for Student category. Optional for Faculty.");
        adviserNote.setTextColor(UiTheme.MUTED);
        adviserNote.setTypeface(regular);
        ScreenTypography.apply(adviserNote, 12);
        adviserNote.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        projectCard.addView(adviserNote);

        adviserError = new TextView(context);
        adviserError.setTextColor(UiTheme.ERROR_RED);
        adviserError.setTypeface(regular);
        ScreenTypography.apply(adviserError, 12);
        adviserError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        adviserError.setVisibility(View.GONE);
        projectCard.addView(adviserError);

        layout.addView(projectCard);

        addSpacing(context, layout, 16);

        // College & Research Team Card
        LinearLayout teamCard = new LinearLayout(context);
        teamCard.setOrientation(LinearLayout.VERTICAL);
        teamCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        teamCard.setPadding(pad, pad, pad, pad);

        TextView teamTitle = new TextView(context);
        teamTitle.setText("College & research team");
        teamTitle.setTextColor(UiTheme.NAVY);
        teamTitle.setTypeface(bold);
        ScreenTypography.apply(teamTitle, 16);
        teamCard.addView(teamTitle);

        addSpacing(context, teamCard, 12);

        // Department / College
        TextView collegeLbl = new TextView(context);
        collegeLbl.setText("Department / College *");
        collegeLbl.setTextColor(UiTheme.NAVY);
        collegeLbl.setTypeface(bold);
        ScreenTypography.apply(collegeLbl, 14);
        teamCard.addView(collegeLbl);

        addSpacing(context, teamCard, 6);

        collegeSpinner = new Spinner(context);
        List<String> colleges = new ArrayList<>(java.util.Arrays.asList(COLLEGES));
        String savedCollege = initialData == null ? userProfile.college
                : initialData.getOrDefault("Department / College", userProfile.college);
        if (savedCollege != null && !savedCollege.isEmpty() && !colleges.contains(savedCollege)) colleges.add(savedCollege);
        ArrayAdapter<String> collegeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, colleges);
        collegeSpinner.setAdapter(collegeAdapter);
        collegeSpinner.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        collegeSpinner.setSelection(1);
        teamCard.addView(collegeSpinner);

        TextView collegeNote = new TextView(context);
        collegeNote.setText("Prefilled from your profile.");
        collegeNote.setTextColor(UiTheme.MUTED);
        collegeNote.setTypeface(regular);
        ScreenTypography.apply(collegeNote, 12);
        collegeNote.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        teamCard.addView(collegeNote);

        collegeError = new TextView(context);
        collegeError.setTextColor(UiTheme.ERROR_RED);
        collegeError.setTypeface(regular);
        ScreenTypography.apply(collegeError, 12);
        collegeError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        collegeError.setVisibility(View.GONE);
        teamCard.addView(collegeError);

        addSpacing(context, teamCard, 12);

        // Lead Researcher / Author
        TextView leadLabel = new TextView(context);
        leadLabel.setText("Lead Researcher / Author");
        leadLabel.setTextColor(UiTheme.NAVY);
        leadLabel.setTypeface(bold);
        ScreenTypography.apply(leadLabel, 14);
        teamCard.addView(leadLabel);

        addSpacing(context, teamCard, 6);

        leadAuthorText = new TextView(context);
        leadAuthorText.setText(userProfile.name + " · " + (isFacultyMode ? "Faculty Researcher" : "Student Researcher"));
        leadAuthorText.setTextColor(UiTheme.NAVY);
        leadAuthorText.setTypeface(bold);
        ScreenTypography.apply(leadAuthorText, 14);
        leadAuthorText.setBackground(UiTheme.shape(context, 0xFFF8FAFC, 8, UiTheme.BORDER));
        leadAuthorText.setPadding(inpPad, inpPad, inpPad, inpPad);
        teamCard.addView(leadAuthorText);

        TextView leadNote = new TextView(context);
        leadNote.setText("Prefilled from your profile.");
        leadNote.setTextColor(UiTheme.MUTED);
        leadNote.setTypeface(regular);
        ScreenTypography.apply(leadNote, 12);
        leadNote.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        teamCard.addView(leadNote);

        addSpacing(context, teamCard, 14);

        // Add Co-Author / Collaborator
        TextView coAuthorTitle = new TextView(context);
        coAuthorTitle.setText("Add Co-Author / Collaborator");
        coAuthorTitle.setTextColor(UiTheme.NAVY);
        coAuthorTitle.setTypeface(bold);
        ScreenTypography.apply(coAuthorTitle, 14);
        teamCard.addView(coAuthorTitle);

        addSpacing(context, teamCard, 4);

        TextView coAuthorSubtitle = new TextView(context);
        coAuthorSubtitle.setText("Search existing researcher accounts to add members. Duplicate selections are not added.");
        coAuthorSubtitle.setTextColor(UiTheme.MUTED);
        coAuthorSubtitle.setTypeface(regular);
        ScreenTypography.apply(coAuthorSubtitle, 12);
        teamCard.addView(coAuthorSubtitle);

        addSpacing(context, teamCard, 8);

        coAuthorsContainer = new LinearLayout(context);
        coAuthorsContainer.setOrientation(LinearLayout.VERTICAL);
        teamCard.addView(coAuthorsContainer);

        addSpacing(context, teamCard, 8);

        Button addCoAuthorBtn = UiButton.outline(context, "Add Co-Author / Collaborator", UiTheme.BORDER, UiTheme.NAVY, this::openAddCoAuthorDialog);
        teamCard.addView(addCoAuthorBtn);

        layout.addView(teamCard);

        addSpacing(context, layout, 24);

        // Action Buttons: Continue & Save as Draft
        Button continueBtn = UiButton.primary(context, "Continue", this::handleContinue);
        layout.addView(continueBtn);

        addSpacing(context, layout, 12);

        draftBtn = UiButton.outline(context, "Save to CRM", UiTheme.BORDER, UiTheme.NAVY, this::handleSaveDraft);
        layout.addView(draftBtn);
        draftStatus = new PendingStatus(context);
        layout.addView(draftStatus);

        if (initialData != null && initialData.containsKey("Co-authors")) {
            teamMembers.clear();
            try {
                org.json.JSONArray saved = new org.json.JSONArray(initialData.get("Co-authors"));
                for (int i = 0; i < saved.length(); i++) {
                    org.json.JSONArray member = saved.getJSONArray(i);
                    teamMembers.add(new ResearchData.CoAuthorItem(member.getString(0), member.getString(1),
                            member.getString(2), member.getString(3)));
                }
            } catch (org.json.JSONException invalid) {
                teamMembers.clear();
            }
        }
        renderTeamList();
        if (initialData != null) {
            dateInput.setText(initialData.getOrDefault("Target Completion Date", "2027-03-15"));
            adviserInput.setText(initialData.getOrDefault("Faculty adviser", "Dr. Maria A. Santos"));
            restoreSelection(typeSpinner, TYPES, initialData.get("Research Type"));
            restoreSelection(periodSpinner, PERIODS, initialData.get("Academic period"));
            restoreSelection(collegeSpinner, colleges.toArray(new String[0]), savedCollege);
        }
    }

    private static void restoreSelection(Spinner spinner, String[] options, String value) {
        int index = java.util.Arrays.asList(options).indexOf(value);
        if (index >= 0) spinner.setSelection(index);
    }

    private Map<String, String> lastSnapshot;

    private void notifyChanged() {
        Map<String, String> snapshot = collectFormData();
        if (!snapshot.equals(lastSnapshot)) {
            lastSnapshot = snapshot;
            listener.onChanged(snapshot);
        }
    }

    public void watchChanges() {
        lastSnapshot = collectFormData();
        for (EditText field : new EditText[]{titleInput, dateInput, adviserInput}) {
            field.addTextChangedListener(new DraftTextWatcher(this::notifyChanged));
        }
        for (Spinner spinner : new Spinner[]{typeSpinner, periodSpinner, collegeSpinner}) {
            spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    notifyChanged();
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }
    }

    public Button getDraftButton() {
        return draftBtn;
    }

    public PendingStatus getDraftStatus() {
        return draftStatus;
    }

    public void openAddCoAuthorDialog() {
        if (listener != null && listener.pickLiveCoAuthor(this::addLiveCoAuthor)) return;
        openPreviewCoAuthorDialog();
    }

    public void openPreviewCoAuthorDialog() {
        AddCoAuthorDialog dialog = new AddCoAuthorDialog(context, teamMembers, new AddCoAuthorDialog.Listener() {
            @Override
            public void onTeamUpdated(List<ResearchData.CoAuthorItem> currentTeam) {
                teamMembers.clear();
                teamMembers.addAll(currentTeam);
                renderTeamList();
                if (lastSnapshot != null) notifyChanged();
            }

            @Override
            public void onDismiss() {}
        });
        dialog.show();
    }

    private void addLiveCoAuthor(ResearchData.CoAuthorItem candidate) {
        for (ResearchData.CoAuthorItem teammate : teamMembers) {
            if (teammate.id.equals(candidate.id)) {
                ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(context,
                        "This researcher is already a team member.", "");
                return;
            }
        }
        teamMembers.add(candidate);
        renderTeamList();
        if (lastSnapshot != null) notifyChanged();
    }

    private void renderTeamList() {
        coAuthorsContainer.removeAllViews();
        for (int i = 0; i < teamMembers.size(); i++) {
            final int index = i;
            ResearchData.CoAuthorItem item = teamMembers.get(i);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(UiTheme.shape(context, 0xFFF8FAFC, 6, UiTheme.BORDER));
            int pad = UiTheme.dp(context, 8);
            row.setPadding(pad, pad, pad, pad);

            TextView info = new TextView(context);
            info.setText(item.name + " · " + item.roleAndCollege);
            info.setTextColor(UiTheme.NAVY);
            info.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(info, 13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(info, lp);

            Button removeBtn = UiButton.text(context, "Remove", UiTheme.ERROR_RED, () -> {
                teamMembers.remove(index);
                renderTeamList();
                if (lastSnapshot != null) notifyChanged();
            });
            row.addView(removeBtn);

            coAuthorsContainer.addView(row);
            addSpacing(context, coAuthorsContainer, 6);
        }
    }

    private void handleContinue() {
        boolean valid = validateFields();
        if (valid && listener != null) {
            listener.onContinue(collectFormData());
        }
    }

    private void handleSaveDraft() {
        if (listener != null) {
            listener.onSaveDraft(collectFormData(), draftBtn, draftStatus);
        }
    }

    private boolean validateFields() {
        String title = titleInput.getText().toString().trim();
        int periodPos = periodSpinner.getSelectedItemPosition();
        int collegePos = collegeSpinner.getSelectedItemPosition();
        String adviser = adviserInput.getText().toString().trim();

        boolean hasError = false;

        if (title.isEmpty()) {
            titleError.setText("Please enter the research title.");
            titleError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            titleError.setVisibility(View.GONE);
        }

        if (periodPos <= 0) {
            periodError.setText("Please select the academic period.");
            periodError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            periodError.setVisibility(View.GONE);
        }

        if (collegePos <= 0) {
            collegeError.setText("Please select your college or institute.");
            collegeError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            collegeError.setVisibility(View.GONE);
        }

        if (!isFacultyMode && adviser.isEmpty()) {
            adviserError.setText("Please enter the adviser name.");
            adviserError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            adviserError.setVisibility(View.GONE);
        }

        return !hasError;
    }

    public Map<String, String> collectFormData() {
        Map<String, String> map = new java.util.LinkedHashMap<>();
        map.put("Research Title", titleInput.getText().toString().trim());
        map.put("Research Type", (String) typeSpinner.getSelectedItem());
        map.put("Target Completion Date", dateInput.getText().toString().trim());
        map.put("Academic period", (String) periodSpinner.getSelectedItem());
        map.put("Faculty adviser", adviserInput.getText().toString().trim());
        map.put("Department / College", (String) collegeSpinner.getSelectedItem());
        map.put("Lead Researcher / Author", leadAuthorText.getText().toString());
        org.json.JSONArray members = new org.json.JSONArray();
        for (ResearchData.CoAuthorItem member : teamMembers) {
            members.put(new org.json.JSONArray().put(member.id).put(member.name)
                    .put(member.roleAndCollege).put(member.email));
        }
        map.put("Co-authors", members.toString());
        return map;
    }

    public void setPreviewState(String state) {
        titleError.setVisibility(View.GONE);
        periodError.setVisibility(View.GONE);
        collegeError.setVisibility(View.GONE);
        adviserError.setVisibility(View.GONE);

        switch (state) {
            case "4.2b_error_title":
                titleInput.setText("");
                titleError.setText("Please enter the research title.");
                titleError.setVisibility(View.VISIBLE);
                periodSpinner.setSelection(1);
                collegeSpinner.setSelection(1);
                adviserInput.setText("Dr. Maria A. Santos");
                break;
            case "4.2c_error_academic_period":
                titleInput.setText("Impact of AI on Student Engagement");
                periodSpinner.setSelection(0);
                periodError.setText("Please select the academic period.");
                periodError.setVisibility(View.VISIBLE);
                collegeSpinner.setSelection(1);
                adviserInput.setText("Dr. Maria A. Santos");
                break;
            case "4.2d_error_college":
                titleInput.setText("Impact of AI on Student Engagement");
                periodSpinner.setSelection(1);
                collegeSpinner.setSelection(0);
                collegeError.setText("Please select your college or institute.");
                collegeError.setVisibility(View.VISIBLE);
                adviserInput.setText("Dr. Maria A. Santos");
                break;
            case "4.2e_error_adviser":
                titleInput.setText("Impact of AI on Student Engagement");
                periodSpinner.setSelection(1);
                collegeSpinner.setSelection(1);
                adviserInput.setText("");
                adviserError.setText("Please enter the adviser name.");
                adviserError.setVisibility(View.VISIBLE);
                break;
            case "4.2f_faculty":
                isFacultyMode = true;
                adviserLabel.setText("Faculty adviser");
                adviserNote.setText("Optional for Faculty");
                leadAuthorText.setText(userProfile.name + " · Faculty Researcher");
                titleInput.setText("Impact of AI on Student Engagement");
                adviserInput.setText("");
                periodSpinner.setSelection(1);
                collegeSpinner.setSelection(1);
                break;
            case "4.2a_prefilled":
            default:
                isFacultyMode = false;
                adviserLabel.setText("Faculty adviser *");
                adviserNote.setText("Required for Student category. Optional for Faculty.");
                leadAuthorText.setText(userProfile.name + " · Student Researcher");
                titleInput.setText("Impact of AI on Student Engagement");
                adviserInput.setText("Dr. Maria A. Santos");
                periodSpinner.setSelection(1);
                collegeSpinner.setSelection(1);
                break;
        }
    }

    private static void addSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(space);
    }

    public LinearLayout getContentLayout() {
        if (layout.getParent() instanceof ViewGroup) {
            ((ViewGroup) layout.getParent()).removeView(layout);
        }
        return PageTitle.wrap(layout, "Research Registration", "Register your ongoing or proposed research project.", false);
    }
}
