package ph.edu.umak.crm.app.staff.lifecycle;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Milestones and Outcomes View (Wireframes 8.1, 8.1a-g, 8.5a, 8.5b).
 * Supports stage updates, individual milestones, outcomes (completion, presentation, publication),
 * validation error states, and confirmation banners.
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class MilestonesAndOutcomesView {

    public interface Callbacks {
        void onViewStageHistory();
        void onClose();
        void onCorrectCompletion(String recordId, String title, String date, CompletionCorrectionView.Callbacks onSaved);
        void onStageSaved();
        void onStageUpdated(StaffLifecycleData.ResearchLifecycleState state);
    }

    private final Activity activity;
    private final Callbacks callbacks;
    private StaffLifecycleData.ResearchLifecycleState state;
    private LinearLayout rootLayout;

    // Error state mode
    private String previewErrorMode = "none"; // "none", "8.1a", "8.1b", "8.1c", "8.1d", "8.1e", "8.1f", "8.1g"

    // Form inputs
    private Spinner stageSpinner;
    private EditText effectiveDateInput;
    private EditText supportingRefInput;
    private EditText nextActionInput;
    private CheckBox noNextActionCheckBox;

    private Spinner completionSpinner;
    private EditText completionDateInput;

    private Spinner presentationSpinner;
    private EditText presentationDateInput;
    private EditText presentationEventInput;

    private Spinner publicationSpinner;
    private EditText publicationDateInput;
    private EditText publicationCitationInput;

    private static final String[] STAGE_OPTIONS = {
            "Select In progress, For revision or Completed",
            "In progress",
            "For revision",
            "Completed",
            "Registered"
    };

    private static final String[] COMPLETION_OPTIONS = {
            "Not completed",
            "Completed"
    };

    private static final String[] PRESENTATION_OPTIONS = {
            "Not recorded",
            "Presented"
    };

    private static final String[] PUBLICATION_OPTIONS = {
            "Recorded month: Oct 2026",
            "Not recorded",
            "Published"
    };

    public MilestonesAndOutcomesView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        buildUi();
    }

    public void setState(StaffLifecycleData.ResearchLifecycleState state) {
        if (state != null) {
            this.state = state;
            buildUi();
        }
    }

    public interface LiveActions {
        void stage(); void milestones(); void milestone(int index); void presentation(); void publication(); void completion(); void history(); void close();
    }
    public void bindLive(org.json.JSONObject record, LiveActions actions) throws org.json.JSONException {
        var page=new ph.edu.umak.crm.app.staff.common.StaffDesignPage(activity,"Milestone and Outcomes",null,null);
        rootLayout=page.content;
        var identity=page.section(record.getString("title"));
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(identity,"REG-"+record.getInt("id"),false);
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(identity,"Current overall stage: "+ph.edu.umak.crm.app.research.RegisteredProgressView.stageLabel(record.getString("stage")),true);
        identity.addView(ph.edu.umak.crm.app.common.components.UiButton.text(activity,"View stage history",UiTheme.ACCENT,actions::history));
        var stage=page.section("Overall stage");
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(stage,record.optString("next_action","").isEmpty()?"No next action recorded":record.optString("next_action"),false);
        if(!record.isNull("deadline"))ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(stage,"Deadline: "+record.optString("deadline"),false);
        if(!"Completed".equals(record.getString("stage")))stage.addView(ph.edu.umak.crm.app.common.components.UiButton.primary(activity,"Update overall stage",actions::stage));
        var milestones=page.section("Research milestones");
        org.json.JSONArray rows=record.getJSONArray("milestones");
        for(int i=0;i<rows.length();i++) {
            final int index=i;org.json.JSONObject item=rows.getJSONObject(i);
            milestones.addView(ph.edu.umak.crm.app.common.components.UiButton.outline(activity,item.getString("title")+" · "+item.getString("status"),UiTheme.BORDER,UiTheme.NAVY,()->actions.milestone(index)));
            if(!item.isNull("effective_date"))ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(milestones,item.optString("effective_date"),false);
        }
        milestones.addView(ph.edu.umak.crm.app.common.components.UiButton.outline(activity,"Edit milestones",UiTheme.BORDER,UiTheme.NAVY,actions::milestones));
        for(String kind:new String[]{"completion","presentation","publication"}) {
            var section=page.section(kind.substring(0,1).toUpperCase(java.util.Locale.ROOT)+kind.substring(1));
            org.json.JSONObject outcome=record.optJSONObject(kind);
            boolean saved=outcome!=null&&!outcome.isNull("date");
            ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(section,saved?outcome.optString("date"):"Not recorded",false);
            if(saved)ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(section,outcome.optString(kind.equals("publication")?"citation":kind.equals("presentation")?"event":"details",""),false);
            if(kind.equals("completion")){if(saved)section.addView(ph.edu.umak.crm.app.common.components.UiButton.outline(activity,"Correct completion",UiTheme.BORDER,UiTheme.NAVY,actions::completion));}
            else section.addView(ph.edu.umak.crm.app.common.components.UiButton.outline(activity,kind.equals("presentation")?"Record presentation":"Record publication",UiTheme.BORDER,UiTheme.NAVY,kind.equals("presentation")?actions::presentation:actions::publication));
        }
        rootLayout.addView(ph.edu.umak.crm.app.common.components.UiButton.outline(activity,"Back to progress list",UiTheme.BORDER,UiTheme.NAVY,actions::close));
    }

    public ViewGroup getContentLayout() {
        if (rootLayout != null && rootLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) rootLayout.getParent()).removeView(rootLayout);
        }
        return PageTitle.wrap(rootLayout, "Milestone and Outcomes", null, false);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    private void buildUi() {
        if (rootLayout == null) {
            rootLayout = new LinearLayout(activity);
            rootLayout.setOrientation(LinearLayout.VERTICAL);
        } else {
            rootLayout.removeAllViews();
        }
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));

        // 1. Top Banners (Error or Confirmation)
        if (!"none".equals(previewErrorMode)) {
            rootLayout.addView(createTopErrorBanner());

        }

        // 3. Study title, ID, stage & View stage history link
        TextView studyTitle = new TextView(activity);
        studyTitle.setText(state.title);
        studyTitle.setTextColor(UiTheme.NAVY);
        studyTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        studyTitle.setTypeface(Typeface.DEFAULT_BOLD);
        rootLayout.addView(studyTitle);

        TextView idText = new TextView(activity);
        idText.setText(state.recordId);
        idText.setTextColor(UiTheme.MUTED);
        idText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        rootLayout.addView(idText);

        LinearLayout stageRow = new LinearLayout(activity);
        stageRow.setOrientation(LinearLayout.HORIZONTAL);
        stageRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams stageRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stageRowParams.topMargin = dp(2);
        stageRowParams.bottomMargin = dp(4);
        stageRow.setLayoutParams(stageRowParams);

        TextView stageText = new TextView(activity);
        stageText.setText("Current overall stage: " + state.currentStage);
        stageText.setTextColor(UiTheme.NAVY);
        stageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        stageText.setTypeface(Typeface.DEFAULT_BOLD);
        stageRow.addView(stageText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView historyLink = new TextView(activity);
        historyLink.setText("View stage history →");
        historyLink.setTextColor(UiTheme.ACCENT);
        historyLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        historyLink.setTypeface(Typeface.DEFAULT_BOLD);
        historyLink.setClickable(true);
        historyLink.setFocusable(true);
        historyLink.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onViewStageHistory();
            }
        });
        stageRow.addView(historyLink);
        rootLayout.addView(stageRow);

        if (state.savedTimestamp != null) {
            TextView savedMeta = new TextView(activity);
            savedMeta.setText(state.savedTimestamp);
            savedMeta.setTextColor(UiTheme.MUTED);
            savedMeta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            LinearLayout.LayoutParams smParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            smParams.bottomMargin = dp(14);
            rootLayout.addView(savedMeta, smParams);
        } else {
            View spacing = new View(activity);
            rootLayout.addView(spacing, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(10)));
        }

        // 4. Section: Update overall stage Card
        rootLayout.addView(createStageUpdateCard());

        // 5. Section: Research Milestones Card
        rootLayout.addView(createMilestonesCard());

        // 6. Section: Completion Outcome Card
        rootLayout.addView(createCompletionCard());

        // 7. Section: Presentation Outcome Card
        rootLayout.addView(createPresentationCard());

        // 8. Section: Publication Outcome Card
        rootLayout.addView(createPublicationCard());

        // Helper instruction
        TextView outcomeNote = new TextView(activity);
        outcomeNote.setText("Record each outcome independently. Saving one does not mark the others as achieved.");
        outcomeNote.setTextColor(UiTheme.MUTED);
        outcomeNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams oParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        oParams.bottomMargin = dp(16);
        rootLayout.addView(outcomeNote, oParams);

        // 9. Actions (Save & Close)
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
        saveParams.bottomMargin = dp(12);
        rootLayout.addView(saveBtn, saveParams);

        saveBtn.setOnClickListener(v -> handleSave());

        Button closeBtn = new Button(activity);
        closeBtn.setText("Close");
        closeBtn.setTextColor(UiTheme.NAVY);
        closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        closeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        closeBtn.setAllCaps(false);
        GradientDrawable closeBg = new GradientDrawable();
        closeBg.setColor(UiTheme.WHITE);
        closeBg.setCornerRadius(dp(8));
        closeBg.setStroke(dp(1), UiTheme.BORDER);
        closeBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22131c4b), closeBg, null));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        rootLayout.addView(closeBtn, closeParams);

        closeBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onClose();
            }
        });
    }

    private View createTopErrorBanner() {
        LinearLayout banner = new LinearLayout(activity);
        banner.setOrientation(LinearLayout.VERTICAL);
        banner.setPadding(dp(14), dp(10), dp(14), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.PEACH_BG);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), UiTheme.PEACH_BORDER);
        banner.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(14);
        banner.setLayoutParams(params);

        TextView text = new TextView(activity);
        text.setText("Not saved. The saved record is unchanged.");
        text.setTextColor(UiTheme.PEACH_TEXT);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        banner.addView(text);
        return banner;
    }

    private View createStageUpdateCard() {
        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Update overall stage");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, 0, 0, dp(12));
        card.addView(title);

        // New overall stage
        TextView stageLabel = new TextView(activity);
        stageLabel.setText("New overall stage");
        stageLabel.setTextColor(UiTheme.NAVY);
        stageLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        stageLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(stageLabel);

        stageSpinner = new Spinner(activity);
        ArrayAdapter<String> stageAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, STAGE_OPTIONS);
        stageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stageSpinner.setAdapter(stageAdapter);
        styleSpinner(stageSpinner);

        if ("8.1a".equals(previewErrorMode)) {
            stageSpinner.setSelection(4); // "Registered" -> triggers not allowed
        } else if ("For revision".equalsIgnoreCase(state.newStage)) {
            stageSpinner.setSelection(2);
        } else if ("Completed".equalsIgnoreCase(state.newStage)) {
            stageSpinner.setSelection(3);
        } else {
            stageSpinner.setSelection(0);
        }
        card.addView(stageSpinner);

        // Error 8.1a: stage change not allowed
        if ("8.1a".equals(previewErrorMode)) {
            TextView errStage = createErrorTextView("This research stage change is not allowed.");
            card.addView(errStage);
        }

        TextView stageGuidance = new TextView(activity);
        stageGuidance.setText("From In progress, you may request For revision or Completed. Registered can advance to In progress; For revision can return to In progress.");
        stageGuidance.setTextColor(UiTheme.MUTED);
        stageGuidance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams gParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gParams.bottomMargin = dp(12);
        card.addView(stageGuidance, gParams);

        // Effective date *
        TextView dateLabel = new TextView(activity);
        dateLabel.setText("Effective date *");
        dateLabel.setTextColor("8.1b".equals(previewErrorMode) ? UiTheme.ERROR_RED : UiTheme.NAVY);
        dateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        dateLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(dateLabel);

        effectiveDateInput = createStyledInput(
                "8.1b".equals(previewErrorMode) ? "" :
                        (state.effectiveDate != null && !state.effectiveDate.isEmpty() ? state.effectiveDate : "2026-11-06"),
                "YYYY-MM-DD",
                "8.1b".equals(previewErrorMode)
        );
        card.addView(effectiveDateInput);

        if ("8.1b".equals(previewErrorMode)) {
            TextView errDate = createErrorTextView("Please enter an effective date.");
            card.addView(errDate);
        }

        // Supporting details or reference *
        TextView refLabel = new TextView(activity);
        refLabel.setText("Supporting details or reference *");
        refLabel.setTextColor("8.1c".equals(previewErrorMode) ? UiTheme.ERROR_RED : UiTheme.NAVY);
        refLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        refLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams refLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        refLabelParams.topMargin = dp(8);
        card.addView(refLabel, refLabelParams);

        supportingRefInput = createStyledInput(
                "8.1c".equals(previewErrorMode) ? "" :
                        (state.supportingRef != null && !state.supportingRef.isEmpty() ? state.supportingRef : "Panel evaluation memo 2026-131"),
                "Enter the supporting reference",
                "8.1c".equals(previewErrorMode)
        );
        card.addView(supportingRefInput);

        if ("8.1c".equals(previewErrorMode)) {
            TextView errRef = createErrorTextView("Provide supporting details or a reference.");
            card.addView(errRef);
        }

        // Next action
        TextView nextLabel = new TextView(activity);
        nextLabel.setText("Next action");
        nextLabel.setTextColor("8.1d".equals(previewErrorMode) ? UiTheme.ERROR_RED : UiTheme.NAVY);
        nextLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        nextLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams nextLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nextLabelParams.topMargin = dp(8);
        card.addView(nextLabel, nextLabelParams);

        nextActionInput = createStyledInput(
                "8.1d".equals(previewErrorMode) ? "" :
                        (state.nextAction != null && !state.nextAction.isEmpty() ? state.nextAction : "Revise Chapter 3 methodology per panel memo."),
                "Enter the next action",
                "8.1d".equals(previewErrorMode)
        );
        card.addView(nextActionInput);

        if ("8.1d".equals(previewErrorMode)) {
            TextView errNext = createErrorTextView("Enter a next action or select No next action recorded.");
            card.addView(errNext);
        }

        // Checkbox: No next action recorded
        noNextActionCheckBox = new CheckBox(activity);
        noNextActionCheckBox.setText("No next action recorded");
        noNextActionCheckBox.setTextColor(UiTheme.NAVY);
        noNextActionCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        noNextActionCheckBox.setChecked(state.noNextActionRecorded);
        card.addView(noNextActionCheckBox);

        TextView note = new TextView(activity);
        note.setText("Completion requires a completion date. Invalid transitions leave the saved record unchanged.");
        note.setTextColor(UiTheme.MUTED);
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = dp(6);
        card.addView(note, noteParams);

        return card;
    }

    private View createMilestonesCard() {
        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Research milestones");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(title);

        TextView sub1 = new TextView(activity);
        sub1.setText("Milestones are separate from the overall stage. CRM confirms which milestones apply.");
        sub1.setTextColor(UiTheme.MUTED);
        sub1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams s1Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        s1Params.topMargin = dp(2);
        card.addView(sub1, s1Params);

        TextView sub2 = new TextView(activity);
        sub2.setText("Status options: Pending · In progress · Completed · Not applicable");
        sub2.setTextColor(UiTheme.MUTED);
        sub2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams s2Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        s2Params.topMargin = dp(2);
        s2Params.bottomMargin = dp(12);
        card.addView(sub2, s2Params);

        for (int i = 0; i < state.milestones.size(); i++) {
            StaffLifecycleData.MilestoneItem item = state.milestones.get(i);
            card.addView(createMilestoneRow(item));

            if (i < state.milestones.size() - 1) {
                View div = new View(activity);
                div.setBackgroundColor(UiTheme.BORDER);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                divParams.topMargin = dp(8);
                divParams.bottomMargin = dp(8);
                card.addView(div, divParams);
            }
        }

        return card;
    }

    private View createMilestoneRow(StaffLifecycleData.MilestoneItem item) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(4), 0, dp(4));

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView nameText = new TextView(activity);
        nameText.setText(item.name);
        nameText.setTextColor(UiTheme.NAVY);
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        nameText.setTypeface(Typeface.DEFAULT_BOLD);
        topRow.addView(nameText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView badge = new TextView(activity);
        badge.setText(item.status);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(10));
        if ("Completed".equalsIgnoreCase(item.status)) {
            bg.setColor(UiTheme.STATUS_COMPLETED_BG);
            badge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
        } else if ("In progress".equalsIgnoreCase(item.status)) {
            bg.setColor(UiTheme.STATUS_IN_REVIEW_BG);
            badge.setTextColor(UiTheme.STATUS_IN_REVIEW_TEXT);
        } else {
            bg.setColor(UiTheme.TAG_BG);
            badge.setTextColor(UiTheme.TAG_TEXT);
        }
        badge.setBackground(bg);
        topRow.addView(badge);
        row.addView(topRow);

        TextView metaText = new TextView(activity);
        metaText.setText(item.meta);
        metaText.setTextColor(UiTheme.MUTED);
        metaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        metaParams.topMargin = dp(2);
        row.addView(metaText, metaParams);

        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> MilestoneEditorDialog.show(
                activity,
                item,
                updated -> buildUi()
        ));

        return row;
    }

    private View createCompletionCard() {
        LinearLayout card = createBaseCard();

        LinearLayout headerRow = new LinearLayout(activity);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(activity);
        title.setText("Completion");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        headerRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        if (state.completion.isSaved) {
            TextView savedBadge = new TextView(activity);
            savedBadge.setText("Saved");
            savedBadge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
            savedBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            savedBadge.setTypeface(Typeface.DEFAULT_BOLD);
            savedBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
            GradientDrawable sBg = new GradientDrawable();
            sBg.setColor(UiTheme.STATUS_COMPLETED_BG);
            sBg.setCornerRadius(dp(8));
            savedBadge.setBackground(sBg);
            headerRow.addView(savedBadge);
        }
        card.addView(headerRow);

        TextView stLabel = new TextView(activity);
        stLabel.setText("Completion status");
        stLabel.setTextColor(UiTheme.NAVY);
        stLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        stLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams stParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stParams.topMargin = dp(8);
        card.addView(stLabel, stParams);

        completionSpinner = new Spinner(activity);
        ArrayAdapter<String> cAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, COMPLETION_OPTIONS);
        cAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        completionSpinner.setAdapter(cAdapter);
        styleSpinner(completionSpinner);
        completionSpinner.setSelection("Completed".equalsIgnoreCase(state.completion.status) || "8.1e".equals(previewErrorMode) ? 1 : 0);
        card.addView(completionSpinner);

        TextView dateLabel = new TextView(activity);
        dateLabel.setText("Completion date *");
        dateLabel.setTextColor("8.1e".equals(previewErrorMode) ? UiTheme.ERROR_RED : UiTheme.NAVY);
        dateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        dateLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(dateLabel);

        completionDateInput = createStyledInput(
                "8.1e".equals(previewErrorMode) ? "" :
                        (state.completion.completionDate != null && !state.completion.completionDate.isEmpty() ? state.completion.completionDate : "YYYY-MM-DD"),
                "YYYY-MM-DD",
                "8.1e".equals(previewErrorMode)
        );
        card.addView(completionDateInput);

        if ("8.1e".equals(previewErrorMode)) {
            TextView errDate = createErrorTextView("Please enter the completion date.");
            card.addView(errDate);
        }

        if (state.completion.isSaved && callbacks != null) {
            TextView corrLink = new TextView(activity);
            corrLink.setText("Correct completion record →");
            corrLink.setTextColor(UiTheme.ACCENT);
            corrLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            corrLink.setTypeface(Typeface.DEFAULT_BOLD);
            corrLink.setClickable(true);
            corrLink.setFocusable(true);
            corrLink.setPadding(0, dp(6), 0, dp(4));
            corrLink.setOnClickListener(v -> requestCompletionCorrection());
            card.addView(corrLink);
        }

        return card;
    }

    public void requestCompletionCorrection() {
        if (callbacks == null || !state.completion.isSaved) return;

        callbacks.onCorrectCompletion(state.recordId, state.title, state.completion.completionDate,
                (correctedDate, reason) -> {
                    state.completion.completionDate = correctedDate;
                    state.stageHistory.add(0, new StaffLifecycleData.StageHistoryEntry(
                            "Completed (Date corrected)",
                            correctedDate,
                            "Correction: " + reason,
                            "Corrected completion date from earlier entry.",
                            "LeBron James (CRM Staff) · Recorded Nov 6, 2026, 3:00 PM",
                            true
                    ));
                    buildUi();
                    callbacks.onStageUpdated(state);
                });
    }

    private View createPresentationCard() {
        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Presentation");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(title);

        TextView stLabel = new TextView(activity);
        stLabel.setText("Presentation status");
        stLabel.setTextColor(UiTheme.NAVY);
        stLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        stLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams stParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stParams.topMargin = dp(8);
        card.addView(stLabel, stParams);

        presentationSpinner = new Spinner(activity);
        ArrayAdapter<String> pAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, PRESENTATION_OPTIONS);
        pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        presentationSpinner.setAdapter(pAdapter);
        styleSpinner(presentationSpinner);
        presentationSpinner.setSelection("8.1f".equals(previewErrorMode) ? 1 : 0);
        card.addView(presentationSpinner);

        TextView dateLabel = new TextView(activity);
        dateLabel.setText("Presentation date");
        dateLabel.setTextColor("8.1f".equals(previewErrorMode) ? UiTheme.ERROR_RED : UiTheme.NAVY);
        dateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        dateLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(dateLabel);

        presentationDateInput = createStyledInput(
                "8.1f".equals(previewErrorMode) ? "" : state.presentation.presentationDate,
                "YYYY-MM-DD",
                "8.1f".equals(previewErrorMode)
        );
        card.addView(presentationDateInput);

        TextView eventLabel = new TextView(activity);
        eventLabel.setText("Conference or event");
        eventLabel.setTextColor("8.1f".equals(previewErrorMode) ? UiTheme.ERROR_RED : UiTheme.NAVY);
        eventLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        eventLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(eventLabel);

        presentationEventInput = createStyledInput(
                "8.1f".equals(previewErrorMode) ? "" : state.presentation.eventName,
                "Enter event name",
                "8.1f".equals(previewErrorMode)
        );
        card.addView(presentationEventInput);

        if ("8.1f".equals(previewErrorMode)) {
            TextView errPres = createErrorTextView("Enter the presentation date and event.");
            card.addView(errPres);
        }

        return card;
    }

    private View createPublicationCard() {
        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Publication");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(title);

        TextView stLabel = new TextView(activity);
        stLabel.setText("Publication status");
        stLabel.setTextColor(UiTheme.NAVY);
        stLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        stLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams stParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stParams.topMargin = dp(8);
        card.addView(stLabel, stParams);

        publicationSpinner = new Spinner(activity);
        ArrayAdapter<String> pubAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, PUBLICATION_OPTIONS);
        pubAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        publicationSpinner.setAdapter(pubAdapter);
        styleSpinner(publicationSpinner);
        publicationSpinner.setSelection("8.1g".equals(previewErrorMode) ? 2 : 0);
        card.addView(publicationSpinner);

        TextView dateLabel = new TextView(activity);
        dateLabel.setText("Publication date");
        dateLabel.setTextColor("8.1g".equals(previewErrorMode) ? UiTheme.ERROR_RED : UiTheme.NAVY);
        dateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        dateLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(dateLabel);

        publicationDateInput = createStyledInput(
                "8.1g".equals(previewErrorMode) ? "" : state.publication.publicationDate,
                "Exact date not recorded",
                "8.1g".equals(previewErrorMode)
        );
        card.addView(publicationDateInput);

        TextView citeLabel = new TextView(activity);
        citeLabel.setText("Journal or citation");
        citeLabel.setTextColor("8.1g".equals(previewErrorMode) ? UiTheme.ERROR_RED : UiTheme.NAVY);
        citeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        citeLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(citeLabel);

        publicationCitationInput = createStyledInput(
                "8.1g".equals(previewErrorMode) ? "" : state.publication.journalCitation,
                "Citation not recorded",
                "8.1g".equals(previewErrorMode)
        );
        card.addView(publicationCitationInput);

        if ("8.1g".equals(previewErrorMode)) {
            TextView errPub = createErrorTextView("Enter the publication date and citation.");
            card.addView(errPub);
        }

        return card;
    }

    private void handleSave() {
        int selectedStagePos = stageSpinner.getSelectedItemPosition();
        String selectedStage = STAGE_OPTIONS[selectedStagePos];

        if ("Registered".equalsIgnoreCase(selectedStage)) {
            previewErrorMode = "8.1a";
            buildUi();
            return;
        }

        String effDate = effectiveDateInput.getText().toString().trim();
        if (TextUtils.isEmpty(effDate) || "YYYY-MM-DD".equalsIgnoreCase(effDate)) {
            previewErrorMode = "8.1b";
            buildUi();
            return;
        }

        String suppRef = supportingRefInput.getText().toString().trim();
        if (TextUtils.isEmpty(suppRef)) {
            previewErrorMode = "8.1c";
            buildUi();
            return;
        }

        String nextAct = nextActionInput.getText().toString().trim();
        if (TextUtils.isEmpty(nextAct) && !noNextActionCheckBox.isChecked()) {
            previewErrorMode = "8.1d";
            buildUi();
            return;
        }

        // Save successfully
        previewErrorMode = "none";
        state.savedBannerMessage = "Stage updated.";
        state.savedTimestamp = "Saved Nov 6, 2026, 2:40 PM by LeBron James";
        state.currentStage = selectedStagePos > 0 ? selectedStage : "For revision";
        state.effectiveDate = effDate;
        state.supportingRef = suppRef;
        state.nextAction = nextAct;
        state.noNextActionRecorded = noNextActionCheckBox.isChecked();

        buildUi();
        if (callbacks != null) callbacks.onStageSaved();
        if (callbacks != null) {
            callbacks.onStageUpdated(state);
        }
    }

    private TextView createErrorTextView(String message) {
        TextView err = new TextView(activity);
        err.setText(message);
        err.setTextColor(UiTheme.ERROR_RED);
        err.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(2);
        params.bottomMargin = dp(8);
        err.setLayoutParams(params);
        return err;
    }

    private EditText createStyledInput(String text, String hint, boolean isError) {
        EditText input = new EditText(activity);
        input.setText(text);
        input.setHint(hint);
        input.setHintTextColor(UiTheme.MUTED);
        input.setTextColor(UiTheme.INK);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(isError ? 2 : 1), isError ? UiTheme.ERROR_RED : UiTheme.BORDER);
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

    // Public Preview Setters for Part 8 States
    public void showPreview8_1_Default() {
        this.previewErrorMode = "none";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.savedBannerMessage = null;
        this.state.savedTimestamp = null;
        buildUi();
    }

    public void showPreview8_1a_ErrorStageNotAllowed() {
        this.previewErrorMode = "8.1a";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.effectiveDate = "2026-11-06";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        buildUi();
    }

    public void showPreview8_1b_ErrorEffectiveDate() {
        this.previewErrorMode = "8.1b";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "For revision";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        buildUi();
    }

    public void showPreview8_1c_ErrorSupportingDetails() {
        this.previewErrorMode = "8.1c";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "For revision";
        this.state.effectiveDate = "2026-11-06";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        buildUi();
    }

    public void showPreview8_1d_ErrorNextAction() {
        this.previewErrorMode = "8.1d";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "For revision";
        this.state.effectiveDate = "2026-11-06";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.noNextActionRecorded = false;
        buildUi();
    }

    public void showPreview8_1e_ErrorCompletionDate() {
        this.previewErrorMode = "8.1e";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "Completed";
        this.state.effectiveDate = "2026-11-06";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        buildUi();
    }

    public void showPreview8_1f_ErrorPresentation() {
        this.previewErrorMode = "8.1f";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "For revision";
        this.state.effectiveDate = "2026-11-06";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        buildUi();
    }

    public void showPreview8_1g_ErrorPublication() {
        this.previewErrorMode = "8.1g";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "For revision";
        this.state.effectiveDate = "2026-11-06";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        buildUi();
    }

    public void showPreview8_5a_StageUpdated() {
        this.previewErrorMode = "none";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.currentStage = "For revision";
        this.state.savedBannerMessage = "Stage updated.";
        this.state.savedTimestamp = "Saved Nov 6, 2026, 2:40 PM by LeBron James";
        buildUi();
    }

    public void showPreview8_5b_OutcomeRecorded() {
        this.previewErrorMode = "none";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.currentStage = "Completed";
        this.state.savedBannerMessage = "Outcome recorded.";
        this.state.savedTimestamp = "Saved Nov 6, 2026, 2:40 PM by LeBron James";
        this.state.completion.status = "Completed";
        this.state.completion.completionDate = "2027-03-15";
        this.state.completion.isSaved = true;
        this.state.effectiveDate = "2027-03-15";
        this.state.supportingRef = "Final defense result, CRM memo 2027-022";
        buildUi();
    }
}
