package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PendingStatus;
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
    private final FrameLayout periodContainer;
    private final Spinner periodSpinner;
    private final TextView periodError;
    private final TextView adviserLabel;
    private final EditText adviserInput;
    private final TextView adviserNote;
    private final TextView adviserError;

    private final FrameLayout collegeContainer;
    private final Spinner collegeSpinner;
    private final TextView collegeError;
    private final TextView leadAuthorText;
    private final LinearLayout coAuthorsContainer;

    private boolean isFacultyMode;
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

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        // 1. Project Details Card
        LinearLayout projectCard = new LinearLayout(context);
        projectCard.setOrientation(LinearLayout.VERTICAL);
        projectCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        projectCard.setPadding(pad, pad, pad, pad);

        TextView pTitle = new TextView(context);
        pTitle.setText("Project Details");
        pTitle.setTextColor(0xff082d50);
        pTitle.setTypeface(bold);
        ScreenTypography.apply(pTitle, 16);
        projectCard.addView(pTitle);

        addSpacing(context, projectCard, 14);

        // Title Input
        TextView tLabel = new TextView(context);
        tLabel.setText("Research Title *");
        tLabel.setTextColor(0xff082d50);
        tLabel.setTypeface(bold);
        ScreenTypography.apply(tLabel, 13);
        projectCard.addView(tLabel);

        addSpacing(context, projectCard, 6);

        titleInput = new EditText(context);
        titleInput.setText(initialData != null ? initialData.getOrDefault("Research Title", "") : "");
        titleInput.setHint("Enter the complete research title");
        titleInput.setHintTextColor(0xff8898aa);
        titleInput.setTextColor(0xff082d50);
        titleInput.setTypeface(regular);
        ScreenTypography.apply(titleInput, 14);
        titleInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int inpPadH = UiTheme.dp(context, 12);
        int inpPadV = UiTheme.dp(context, 10);
        titleInput.setPadding(inpPadH, inpPadV, inpPadH, inpPadV);
        titleInput.setMinHeight(UiTheme.dp(context, 46));
        projectCard.addView(titleInput);

        titleError = new TextView(context);
        titleError.setTextColor(0xffdc2626);
        titleError.setTypeface(regular);
        ScreenTypography.apply(titleError, 12);
        titleError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        titleError.setVisibility(View.GONE);
        projectCard.addView(titleError);

        addSpacing(context, projectCard, 14);

        // Research Type Dropdown with chevron
        TextView typeLabel = new TextView(context);
        typeLabel.setText("Research Type");
        typeLabel.setTextColor(0xff082d50);
        typeLabel.setTypeface(bold);
        ScreenTypography.apply(typeLabel, 13);
        projectCard.addView(typeLabel);

        addSpacing(context, projectCard, 6);

        FrameLayout typeContainer = new FrameLayout(context);
        typeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        typeSpinner = new Spinner(context);
        typeSpinner.setBackground(null);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, TYPES);
        typeSpinner.setAdapter(typeAdapter);
        FrameLayout.LayoutParams typeSpinLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 46));
        typeContainer.addView(typeSpinner, typeSpinLp);

        ImageView typeChevron = new ImageView(context);
        typeChevron.setImageResource(R.drawable.ic_crm_chevron_down);
        typeChevron.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams typeChevLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 20), UiTheme.dp(context, 20));
        typeChevLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        typeChevLp.rightMargin = UiTheme.dp(context, 12);
        typeContainer.addView(typeChevron, typeChevLp);

        projectCard.addView(typeContainer);

        addSpacing(context, projectCard, 14);

        // Target Completion Date with calendar icon
        TextView dateLabel = new TextView(context);
        dateLabel.setText("Target Completion Date");
        dateLabel.setTextColor(0xff082d50);
        dateLabel.setTypeface(bold);
        ScreenTypography.apply(dateLabel, 13);
        projectCard.addView(dateLabel);

        addSpacing(context, projectCard, 6);

        LinearLayout dateContainer = new LinearLayout(context);
        dateContainer.setOrientation(LinearLayout.HORIZONTAL);
        dateContainer.setGravity(Gravity.CENTER_VERTICAL);
        dateContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        dateContainer.setMinimumHeight(UiTheme.dp(context, 46));

        dateInput = new EditText(context);
        dateInput.setText(initialData != null ? initialData.getOrDefault("Target Completion Date", "") : "");
        dateInput.setHint("YYYY-MM-DD");
        dateInput.setHintTextColor(0xff8898aa);
        dateInput.setTextColor(0xff082d50);
        dateInput.setTypeface(regular);
        ScreenTypography.apply(dateInput, 14);
        dateInput.setBackground(null);
        dateInput.setPadding(inpPadH, inpPadV, inpPadH, inpPadV);
        dateInput.setSingleLine(true);
        LinearLayout.LayoutParams dateInputLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        dateContainer.addView(dateInput, dateInputLp);

        ImageView calIcon = new ImageView(context);
        calIcon.setImageResource(R.drawable.ic_crm_calendar);
        int iconSize = UiTheme.dp(context, 20);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconLp.rightMargin = UiTheme.dp(context, 12);
        dateContainer.addView(calIcon, iconLp);

        projectCard.addView(dateContainer);

        addSpacing(context, projectCard, 14);

        // Academic period Dropdown with chevron
        TextView periodLabel = new TextView(context);
        periodLabel.setText("Academic period *");
        periodLabel.setTextColor(0xff082d50);
        periodLabel.setTypeface(bold);
        ScreenTypography.apply(periodLabel, 13);
        projectCard.addView(periodLabel);

        addSpacing(context, projectCard, 6);

        periodContainer = new FrameLayout(context);
        periodContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        periodSpinner = new Spinner(context);
        periodSpinner.setBackground(null);
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, PERIODS) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTypeface(UiTheme.bodyFont(context));
                    ScreenTypography.apply(tv, 14);
                    tv.setTextColor(position == 0 ? 0xff8898aa : 0xff082d50);
                    tv.setPadding(UiTheme.dp(context, 12), 0, UiTheme.dp(context, 32), 0);
                }
                return v;
            }
        };
        periodSpinner.setAdapter(periodAdapter);
        FrameLayout.LayoutParams periodSpinLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 46));
        periodContainer.addView(periodSpinner, periodSpinLp);

        ImageView periodChevron = new ImageView(context);
        periodChevron.setImageResource(R.drawable.ic_crm_chevron_down);
        periodChevron.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams periodChevLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 20), UiTheme.dp(context, 20));
        periodChevLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        periodChevLp.rightMargin = UiTheme.dp(context, 12);
        periodContainer.addView(periodChevron, periodChevLp);

        periodSpinner.setSelection(0); // Default: "Select academic period" in 4.2a
        projectCard.addView(periodContainer);

        periodError = new TextView(context);
        periodError.setTextColor(0xffdc2626);
        periodError.setTypeface(regular);
        ScreenTypography.apply(periodError, 12);
        periodError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        periodError.setVisibility(View.GONE);
        projectCard.addView(periodError);

        addSpacing(context, projectCard, 14);

        // Faculty adviser
        adviserLabel = new TextView(context);
        adviserLabel.setText(isFacultyMode ? "Faculty adviser" : "Faculty adviser *");
        adviserLabel.setTextColor(0xff082d50);
        adviserLabel.setTypeface(bold);
        ScreenTypography.apply(adviserLabel, 13);
        projectCard.addView(adviserLabel);

        addSpacing(context, projectCard, 6);

        adviserInput = new EditText(context);
        adviserInput.setText(initialData != null ? initialData.getOrDefault("Faculty adviser", "") : "");
        adviserInput.setHint("Enter adviser name");
        adviserInput.setHintTextColor(0xff8898aa);
        adviserInput.setTextColor(0xff082d50);
        adviserInput.setTypeface(regular);
        ScreenTypography.apply(adviserInput, 14);
        adviserInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        adviserInput.setPadding(inpPadH, inpPadV, inpPadH, inpPadV);
        adviserInput.setMinHeight(UiTheme.dp(context, 46));
        adviserInput.setSingleLine(true);
        projectCard.addView(adviserInput);

        adviserNote = new TextView(context);
        adviserNote.setText(isFacultyMode ? "Optional for Faculty" : "Required for Student category. Optional for Faculty.");
        adviserNote.setTextColor(0xff586b83);
        adviserNote.setTypeface(regular);
        ScreenTypography.apply(adviserNote, 12);
        adviserNote.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        projectCard.addView(adviserNote);

        adviserError = new TextView(context);
        adviserError.setTextColor(0xffdc2626);
        adviserError.setTypeface(regular);
        ScreenTypography.apply(adviserError, 12);
        adviserError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        adviserError.setVisibility(View.GONE);
        projectCard.addView(adviserError);

        layout.addView(projectCard);

        addSpacing(context, layout, 16);

        // 2. College & Research Team Card
        LinearLayout teamCard = new LinearLayout(context);
        teamCard.setOrientation(LinearLayout.VERTICAL);
        teamCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        teamCard.setPadding(pad, pad, pad, pad);

        TextView teamTitle = new TextView(context);
        teamTitle.setText("College & research team");
        teamTitle.setTextColor(0xff082d50);
        teamTitle.setTypeface(bold);
        ScreenTypography.apply(teamTitle, 16);
        teamCard.addView(teamTitle);

        addSpacing(context, teamCard, 14);

        // Department / College Dropdown with chevron
        TextView collegeLbl = new TextView(context);
        collegeLbl.setText("Department / College *");
        collegeLbl.setTextColor(0xff082d50);
        collegeLbl.setTypeface(bold);
        ScreenTypography.apply(collegeLbl, 13);
        teamCard.addView(collegeLbl);

        addSpacing(context, teamCard, 6);

        collegeContainer = new FrameLayout(context);
        collegeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        collegeSpinner = new Spinner(context);
        collegeSpinner.setBackground(null);
        List<String> colleges = new ArrayList<>(java.util.Arrays.asList(COLLEGES));
        String savedCollege = initialData == null ? userProfile.college
                : initialData.getOrDefault("Department / College", userProfile.college);
        if (savedCollege != null && !savedCollege.isEmpty() && !colleges.contains(savedCollege)) colleges.add(savedCollege);
        ArrayAdapter<String> collegeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, colleges) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTypeface(UiTheme.bodyFont(context));
                    ScreenTypography.apply(tv, 14);
                    tv.setTextColor(position == 0 ? 0xff8898aa : 0xff082d50);
                    tv.setPadding(UiTheme.dp(context, 12), 0, UiTheme.dp(context, 32), 0);
                }
                return v;
            }
        };
        collegeSpinner.setAdapter(collegeAdapter);
        FrameLayout.LayoutParams colSpinLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 46));
        collegeContainer.addView(collegeSpinner, colSpinLp);

        ImageView colChevron = new ImageView(context);
        colChevron.setImageResource(R.drawable.ic_crm_chevron_down);
        colChevron.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams colChevLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 20), UiTheme.dp(context, 20));
        colChevLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        colChevLp.rightMargin = UiTheme.dp(context, 12);
        collegeContainer.addView(colChevron, colChevLp);

        int colIdx = colleges.indexOf(userProfile.college);
        collegeSpinner.setSelection(colIdx >= 0 ? colIdx : 1);
        teamCard.addView(collegeContainer);

        TextView collegeNote = new TextView(context);
        collegeNote.setText("Prefilled from your profile.");
        collegeNote.setTextColor(0xff586b83);
        collegeNote.setTypeface(regular);
        ScreenTypography.apply(collegeNote, 12);
        collegeNote.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        teamCard.addView(collegeNote);

        collegeError = new TextView(context);
        collegeError.setTextColor(0xffdc2626);
        collegeError.setTypeface(regular);
        ScreenTypography.apply(collegeError, 12);
        collegeError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        collegeError.setVisibility(View.GONE);
        teamCard.addView(collegeError);

        addSpacing(context, teamCard, 14);

        // Lead Researcher / Author
        TextView leadLabel = new TextView(context);
        leadLabel.setText("Lead Researcher / Author");
        leadLabel.setTextColor(0xff082d50);
        leadLabel.setTypeface(bold);
        ScreenTypography.apply(leadLabel, 13);
        teamCard.addView(leadLabel);

        addSpacing(context, teamCard, 6);

        leadAuthorText = new TextView(context);
        leadAuthorText.setText(userProfile.name + " · " + (isFacultyMode ? "Faculty Researcher" : "Student Researcher"));
        leadAuthorText.setTextColor(0xff082d50);
        leadAuthorText.setTypeface(bold);
        ScreenTypography.apply(leadAuthorText, 14);
        leadAuthorText.setBackground(UiTheme.shape(context, 0xfff8fafc, 8, UiTheme.BORDER));
        leadAuthorText.setPadding(inpPadH, inpPadV, inpPadH, inpPadV);
        leadAuthorText.setMinHeight(UiTheme.dp(context, 46));
        leadAuthorText.setGravity(Gravity.CENTER_VERTICAL);
        teamCard.addView(leadAuthorText);

        TextView leadNote = new TextView(context);
        leadNote.setText("Prefilled from your profile.");
        leadNote.setTextColor(0xff586b83);
        leadNote.setTypeface(regular);
        ScreenTypography.apply(leadNote, 12);
        leadNote.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        teamCard.addView(leadNote);

        addSpacing(context, teamCard, 14);

        // Add Co-Author / Collaborator
        coAuthorsContainer = new LinearLayout(context);
        coAuthorsContainer.setOrientation(LinearLayout.VERTICAL);
        teamCard.addView(coAuthorsContainer);

        Button addCoAuthorBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        addCoAuthorBtn.setText("+ Add Co-Author / Collaborator");
        addCoAuthorBtn.setAllCaps(false);
        addCoAuthorBtn.setTypeface(bold);
        addCoAuthorBtn.setTextColor(0xff185d9d);
        ScreenTypography.apply(addCoAuthorBtn, 14);
        addCoAuthorBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xff185d9d), 0x20185d9d));
        addCoAuthorBtn.setMinHeight(UiTheme.dp(context, 44));
        addCoAuthorBtn.setOnClickListener(v -> openAddCoAuthorDialog());
        teamCard.addView(addCoAuthorBtn);

        addSpacing(context, teamCard, 6);

        TextView coAuthorSubtitle = new TextView(context);
        coAuthorSubtitle.setText("Search existing researcher accounts to add members. Duplicate selections are not added.");
        coAuthorSubtitle.setTextColor(0xff586b83);
        coAuthorSubtitle.setTypeface(regular);
        ScreenTypography.apply(coAuthorSubtitle, 12);
        teamCard.addView(coAuthorSubtitle);

        layout.addView(teamCard);

        addSpacing(context, layout, 24);

        // Action Buttons: Continue (Yellow CTA) & Save as Draft (White outline)
        Button continueBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        continueBtn.setText("Continue");
        continueBtn.setAllCaps(false);
        continueBtn.setTypeface(bold);
        continueBtn.setTextColor(0xff082d50);
        ScreenTypography.apply(continueBtn, 15);
        continueBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xfffbbf24, 8, 0), 0x20082d50));
        continueBtn.setMinHeight(UiTheme.dp(context, 46));
        continueBtn.setOnClickListener(v -> handleContinue());
        layout.addView(continueBtn);

        addSpacing(context, layout, 12);

        draftBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        draftBtn.setText("Save as Draft");
        draftBtn.setAllCaps(false);
        draftBtn.setTypeface(bold);
        draftBtn.setTextColor(0xff082d50);
        ScreenTypography.apply(draftBtn, 15);
        draftBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        draftBtn.setMinHeight(UiTheme.dp(context, 46));
        draftBtn.setOnClickListener(v -> handleSaveDraft());
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
            dateInput.setText(initialData.getOrDefault("Target Completion Date", ""));
            adviserInput.setText(initialData.getOrDefault("Faculty adviser", ""));
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
            row.setBackground(UiTheme.shape(context, 0xfff8fafc, 6, UiTheme.BORDER));
            int pad = UiTheme.dp(context, 8);
            row.setPadding(pad, pad, pad, pad);

            TextView info = new TextView(context);
            info.setText(item.name + " · " + item.roleAndCollege);
            info.setTextColor(0xff082d50);
            info.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(info, 13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(info, lp);

            Button removeBtn = UiButton.text(context, "Remove", 0xffdc2626, () -> {
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
            titleInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            titleError.setText("⚠ Please enter the research title.");
            titleError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            titleInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
            titleError.setVisibility(View.GONE);
        }

        if (periodPos <= 0) {
            periodContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            periodError.setText("⚠ Please select the academic period.");
            periodError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            periodContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
            periodError.setVisibility(View.GONE);
        }

        if (collegePos <= 0) {
            collegeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            collegeError.setText("⚠ Please select your college or institute.");
            collegeError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            collegeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
            collegeError.setVisibility(View.GONE);
        }

        if (!isFacultyMode && adviser.isEmpty()) {
            adviserInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            adviserError.setText("⚠ Please enter the adviser name.");
            adviserError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            adviserInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
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
        titleInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        titleError.setVisibility(View.GONE);
        periodContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        periodError.setVisibility(View.GONE);
        collegeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        collegeError.setVisibility(View.GONE);
        adviserInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        adviserError.setVisibility(View.GONE);

        switch (state) {
            case "4.2b_error_title":
                titleInput.setText("");
                titleInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
                titleError.setText("⚠ Please enter the research title.");
                titleError.setVisibility(View.VISIBLE);
                dateInput.setText("2027-03-15");
                periodSpinner.setSelection(1);
                collegeSpinner.setSelection(1);
                adviserInput.setText("Dr. Maria A. Santos");
                break;
            case "4.2c_error_academic_period":
                titleInput.setText("Impact of AI on Student Engagement");
                dateInput.setText("2027-03-15");
                periodSpinner.setSelection(0);
                periodContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
                periodError.setText("⚠ Please select the academic period.");
                periodError.setVisibility(View.VISIBLE);
                collegeSpinner.setSelection(1);
                adviserInput.setText("Dr. Maria A. Santos");
                break;
            case "4.2d_error_college":
                titleInput.setText("Impact of AI on Student Engagement");
                dateInput.setText("2027-03-15");
                periodSpinner.setSelection(1);
                collegeSpinner.setSelection(0);
                collegeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
                collegeError.setText("⚠ Please select your college or institute.");
                collegeError.setVisibility(View.VISIBLE);
                adviserInput.setText("Dr. Maria A. Santos");
                break;
            case "4.2e_error_adviser":
                titleInput.setText("Impact of AI on Student Engagement");
                dateInput.setText("2027-03-15");
                periodSpinner.setSelection(1);
                collegeSpinner.setSelection(1);
                adviserInput.setText("");
                adviserInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
                adviserError.setText("⚠ Please enter the adviser name.");
                adviserError.setVisibility(View.VISIBLE);
                break;
            case "4.2f_faculty":
                isFacultyMode = true;
                adviserLabel.setText("Faculty adviser");
                adviserNote.setText("Optional for Faculty");
                leadAuthorText.setText(userProfile.name + " · Faculty Researcher");
                titleInput.setText("Impact of AI on Student Engagement");
                dateInput.setText("2027-03-15");
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
                titleInput.setText("");
                dateInput.setText("");
                adviserInput.setText("");
                periodSpinner.setSelection(0);
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

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        // Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(context);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = UiTheme.dp(context, 16);
        band.setPadding(pad, pad, pad, pad);

        // Heading: Research Registration
        TextView heading = new TextView(context);
        heading.setText("Research Registration");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        // Subtitle: Register your ongoing or proposed research project.
        TextView copy = new TextView(context);
        copy.setText("Register your ongoing or proposed research project.");
        copy.setTextColor(UiTheme.PALE);
        copy.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(copy, 13);
        band.addView(copy);

        addSpacing(context, band, 14);

        // Step Indicator Title
        TextView stepIndicator = new TextView(context);
        stepIndicator.setText("Step 1 of 3: General Information");
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
