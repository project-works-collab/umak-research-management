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

        // 1. Summary Card (Title, ID, Current overall stage badge, Unsaved/Saved notice, View stage history)
        rootLayout.addView(createSummaryCard());

        // 2. Section: Update overall stage Card
        rootLayout.addView(createStageUpdateCard());

        // 3. Section: Research Milestones Card
        rootLayout.addView(createMilestonesCard());

        // 4. Section: Completion Outcome Card
        rootLayout.addView(createCompletionCard());

        // 5. Section: Presentation Outcome Card
        rootLayout.addView(createPresentationCard());

        // 6. Section: Publication Outcome Card
        rootLayout.addView(createPublicationCard());

        // Helper instruction
        TextView outcomeNote = new TextView(activity);
        outcomeNote.setText("Record each outcome independently. Saving one does not mark the others as achieved.");
        outcomeNote.setTextColor(0xff64748b);
        outcomeNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams oParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        oParams.bottomMargin = dp(16);
        rootLayout.addView(outcomeNote, oParams);

        // 7. Actions (Save & Close)
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

        // Confirmation Banner (8.5a Stage updated, 8.5b Outcome recorded)
        if (state.savedBannerMessage != null) {
            LinearLayout banner = new LinearLayout(activity);
            banner.setOrientation(LinearLayout.HORIZONTAL);
            banner.setGravity(Gravity.CENTER);
            banner.setPadding(dp(18), dp(10), dp(18), dp(10));
            GradientDrawable bBg = new GradientDrawable();
            bBg.setColor(0xff082d50);
            bBg.setCornerRadius(dp(22));
            banner.setBackground(bBg);

            TextView bText = new TextView(activity);
            bText.setText("✓ " + state.savedBannerMessage);
            bText.setTextColor(UiTheme.WHITE);
            bText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            bText.setTypeface(Typeface.DEFAULT_BOLD);
            banner.addView(bText);

            LinearLayout.LayoutParams bParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
            bParams.gravity = Gravity.CENTER_HORIZONTAL;
            bParams.topMargin = dp(12);
            bParams.bottomMargin = dp(4);
            rootLayout.addView(banner, bParams);
        }
    }

    private View createSummaryCard() {
        LinearLayout card = createBaseCard();

        TextView studyTitle = new TextView(activity);
        studyTitle.setText(state.title);
        studyTitle.setTextColor(UiTheme.NAVY);
        studyTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        studyTitle.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(studyTitle);

        TextView idText = new TextView(activity);
        idText.setText(state.recordId);
        idText.setTextColor(0xff64748b);
        idText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams idParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        idParams.topMargin = dp(2);
        idParams.bottomMargin = dp(8);
        card.addView(idText, idParams);

        LinearLayout stageRow = new LinearLayout(activity);
        stageRow.setOrientation(LinearLayout.HORIZONTAL);
        stageRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams stageRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stageRow.setLayoutParams(stageRowParams);

        TextView stageLabel = new TextView(activity);
        stageLabel.setText("Current overall stage:   ");
        stageLabel.setTextColor(UiTheme.NAVY);
        stageLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        stageLabel.setTypeface(Typeface.DEFAULT_BOLD);
        stageRow.addView(stageLabel);

        TextView stageBadge = new TextView(activity);
        stageBadge.setText(state.currentStage);
        stageBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        stageBadge.setTypeface(Typeface.DEFAULT_BOLD);
        stageBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(dp(12));
        if ("Completed".equalsIgnoreCase(state.currentStage)) {
            stageBadge.setTextColor(0xff059669);
            badgeBg.setColor(0xffecfdf5);
            badgeBg.setStroke(dp(1), 0xffa7f3d0);
        } else if ("For revision".equalsIgnoreCase(state.currentStage)) {
            stageBadge.setTextColor(0xffb45309);
            badgeBg.setColor(0xfffffbeb);
            badgeBg.setStroke(dp(1), 0xfffde68a);
        } else {
            stageBadge.setTextColor(0xff1d4ed8);
            badgeBg.setColor(0xffeff6ff);
            badgeBg.setStroke(dp(1), 0xffbfdbfe);
        }
        stageBadge.setBackground(badgeBg);
        stageRow.addView(stageBadge);

        card.addView(stageRow);

        if (!"none".equals(previewErrorMode)) {
            TextView notSavedText = new TextView(activity);
            notSavedText.setText("Not saved. The saved record is unchanged.");
            notSavedText.setTextColor(0xff64748b);
            notSavedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            LinearLayout.LayoutParams nsParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            nsParams.topMargin = dp(4);
            nsParams.bottomMargin = dp(2);
            card.addView(notSavedText, nsParams);
        } else if (state.savedTimestamp != null) {
            TextView savedMeta = new TextView(activity);
            savedMeta.setText("✓ " + state.savedTimestamp);
            savedMeta.setTextColor(0xff16a34a);
            savedMeta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            savedMeta.setTypeface(Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams smParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            smParams.topMargin = dp(4);
            smParams.bottomMargin = dp(2);
            card.addView(savedMeta, smParams);
        }

        TextView historyLink = new TextView(activity);
        historyLink.setText("View stage history →");
        historyLink.setTextColor(UiTheme.ACCENT);
        historyLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        historyLink.setTypeface(Typeface.DEFAULT_BOLD);
        historyLink.setClickable(true);
        historyLink.setFocusable(true);
        LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linkParams.topMargin = dp(6);
        historyLink.setLayoutParams(linkParams);
        historyLink.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onViewStageHistory();
            }
        });
        card.addView(historyLink);

        return card;
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
        boolean isStageError = "8.1a".equals(previewErrorMode);
        styleSpinner(stageSpinner, isStageError);

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
        stageGuidance.setTextColor(0xff64748b);
        stageGuidance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams gParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gParams.bottomMargin = dp(12);
        card.addView(stageGuidance, gParams);

        // Effective date *
        TextView dateLabel = new TextView(activity);
        dateLabel.setText("Effective date *");
        dateLabel.setTextColor(UiTheme.NAVY);
        dateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        dateLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(dateLabel);

        boolean isDateError = "8.1b".equals(previewErrorMode);
        effectiveDateInput = createStyledInput(
                isDateError ? "" :
                        (state.effectiveDate != null && !state.effectiveDate.isEmpty() ? state.effectiveDate : ""),
                "YYYY-MM-DD",
                isDateError
        );
        card.addView(effectiveDateInput);

        if (isDateError) {
            TextView errDate = createErrorTextView("Please enter an effective date.");
            card.addView(errDate);
        }

        // Supporting details or reference *
        TextView refLabel = new TextView(activity);
        refLabel.setText("Supporting details or reference *");
        refLabel.setTextColor(UiTheme.NAVY);
        refLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        refLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams refLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        refLabelParams.topMargin = dp(8);
        card.addView(refLabel, refLabelParams);

        boolean isRefError = "8.1c".equals(previewErrorMode);
        supportingRefInput = createStyledInput(
                isRefError ? "" :
                        (state.supportingRef != null && !state.supportingRef.isEmpty() ? state.supportingRef : ""),
                "Enter the supporting reference",
                isRefError
        );
        card.addView(supportingRefInput);

        if (isRefError) {
            TextView errRef = createErrorTextView("Provide supporting details or a reference.");
            card.addView(errRef);
        }

        // Next action
        TextView nextLabel = new TextView(activity);
        nextLabel.setText("Next action");
        nextLabel.setTextColor(UiTheme.NAVY);
        nextLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        nextLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams nextLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nextLabelParams.topMargin = dp(8);
        card.addView(nextLabel, nextLabelParams);

        boolean isNextError = "8.1d".equals(previewErrorMode);
        nextActionInput = createStyledInput(
                isNextError ? "" :
                        (state.nextAction != null && !state.nextAction.isEmpty() ? state.nextAction : ""),
                "Enter the next action",
                isNextError
        );
        card.addView(nextActionInput);

        if (isNextError) {
            TextView errNext = createErrorTextView("Enter a next action or select No next action recorded.");
            card.addView(errNext);
        }

        // Checkbox: No next action recorded
        noNextActionCheckBox = new CheckBox(activity);
        noNextActionCheckBox.setText("No next action recorded");
        noNextActionCheckBox.setTextColor(UiTheme.NAVY);
        noNextActionCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        noNextActionCheckBox.setChecked(state.noNextActionRecorded);
        if (isNextError) {
            noNextActionCheckBox.setButtonTintList(ColorStateList.valueOf(0xffea580c));
        } else {
            noNextActionCheckBox.setButtonTintList(ColorStateList.valueOf(0xff131c4b));
        }
        card.addView(noNextActionCheckBox);

        TextView note = new TextView(activity);
        note.setText("Completion requires a completion date. Invalid transitions leave the saved record unchanged.");
        note.setTextColor(0xffb45309);
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
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
        sub1.setTextColor(0xff64748b);
        sub1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams s1Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        s1Params.topMargin = dp(2);
        s1Params.bottomMargin = dp(12);
        card.addView(sub1, s1Params);

        for (int i = 0; i < state.milestones.size(); i++) {
            StaffLifecycleData.MilestoneItem item = state.milestones.get(i);
            card.addView(createMilestoneRow(item));
        }

        TextView sub2 = new TextView(activity);
        sub2.setText("Status options: Pending · In progress · Completed · Not applicable");
        sub2.setTextColor(0xff64748b);
        sub2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        LinearLayout.LayoutParams s2Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        s2Params.topMargin = dp(4);
        s2Params.bottomMargin = dp(2);
        card.addView(sub2, s2Params);

        return card;
    }

    private View createMilestoneRow(StaffLifecycleData.MilestoneItem item) {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), 0xffe2e8f0);
        box.setBackground(bg);

        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boxParams.bottomMargin = dp(8);
        box.setLayoutParams(boxParams);

        // Row 1: Name (left) and Status badge (right)
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
        GradientDrawable bBg = new GradientDrawable();
        bBg.setCornerRadius(dp(10));
        if ("Completed".equalsIgnoreCase(item.status)) {
            bBg.setColor(0xffecfdf5);
            badge.setTextColor(0xff059669);
        } else if ("In progress".equalsIgnoreCase(item.status)) {
            bBg.setColor(0xfffffbeb);
            badge.setTextColor(0xffd97706);
        } else {
            bBg.setColor(0xfff3f4f6);
            badge.setTextColor(0xff4b5563);
        }
        badge.setBackground(bBg);
        topRow.addView(badge);
        box.addView(topRow);

        // Row 2: Subtitle (meta) on left, chevron › on right
        LinearLayout bottomRow = new LinearLayout(activity);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bRowParams.topMargin = dp(4);
        bottomRow.setLayoutParams(bRowParams);

        TextView metaText = new TextView(activity);
        metaText.setText(item.meta);
        metaText.setTextColor(0xff64748b);
        metaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        bottomRow.addView(metaText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView chevron = new TextView(activity);
        chevron.setText("›");
        chevron.setTextColor(0xff94a3b8);
        chevron.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        chevron.setTypeface(Typeface.DEFAULT_BOLD);
        bottomRow.addView(chevron);

        box.addView(bottomRow);

        box.setClickable(true);
        box.setFocusable(true);
        box.setOnClickListener(v -> MilestoneEditorDialog.show(
                activity,
                item,
                updated -> buildUi()
        ));

        return box;
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
            savedBadge.setTextColor(0xff059669);
            savedBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            savedBadge.setTypeface(Typeface.DEFAULT_BOLD);
            savedBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
            GradientDrawable sBg = new GradientDrawable();
            sBg.setColor(0xffecfdf5);
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
        styleSpinner(completionSpinner, false);
        boolean isCompleted = "Completed".equalsIgnoreCase(state.completion.status) || "8.1e".equals(previewErrorMode);
        completionSpinner.setSelection(isCompleted ? 1 : 0);
        card.addView(completionSpinner);

        TextView dateLabel = new TextView(activity);
        dateLabel.setText(isCompleted ? "Completion date *" : "Completion date");
        dateLabel.setTextColor(UiTheme.NAVY);
        dateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        dateLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(dateLabel);

        boolean isDateError = "8.1e".equals(previewErrorMode);
        completionDateInput = createStyledInput(
                isDateError ? "" :
                        (state.completion.completionDate != null && !state.completion.completionDate.isEmpty() ? state.completion.completionDate : ""),
                "YYYY-MM-DD",
                isDateError
        );
        card.addView(completionDateInput);

        if (isDateError) {
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
        styleSpinner(presentationSpinner, false);
        boolean isPresented = "Presented".equalsIgnoreCase(state.presentation.status) || "8.1f".equals(previewErrorMode);
        presentationSpinner.setSelection(isPresented ? 1 : 0);
        card.addView(presentationSpinner);

        TextView dateLabel = new TextView(activity);
        dateLabel.setText("Presentation date");
        dateLabel.setTextColor(UiTheme.NAVY);
        dateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        dateLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(dateLabel);

        boolean isPresError = "8.1f".equals(previewErrorMode);
        presentationDateInput = createStyledInput(
                isPresError ? "" : state.presentation.presentationDate,
                "YYYY-MM-DD",
                isPresError
        );
        card.addView(presentationDateInput);

        if (isPresError) {
            TextView errPres = createErrorTextView("Enter the presentation date and event.");
            card.addView(errPres);
        }

        TextView eventLabel = new TextView(activity);
        eventLabel.setText("Conference or event");
        eventLabel.setTextColor(UiTheme.NAVY);
        eventLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        eventLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(eventLabel);

        presentationEventInput = createStyledInput(
                isPresError ? "" : state.presentation.eventName,
                "Enter event name",
                isPresError
        );
        card.addView(presentationEventInput);

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
        styleSpinner(publicationSpinner, false);
        boolean isPublished = "Published".equalsIgnoreCase(state.publication.status) || "8.1g".equals(previewErrorMode);
        publicationSpinner.setSelection(isPublished ? 2 : 0);
        card.addView(publicationSpinner);

        TextView dateLabel = new TextView(activity);
        dateLabel.setText("Publication date");
        dateLabel.setTextColor(UiTheme.NAVY);
        dateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        dateLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(dateLabel);

        boolean isPubError = "8.1g".equals(previewErrorMode);
        publicationDateInput = createStyledInput(
                isPubError ? "" : state.publication.publicationDate,
                isPubError ? "YYYY-MM-DD" : "Exact date not recorded",
                isPubError
        );
        card.addView(publicationDateInput);

        if (isPubError) {
            TextView errPub = createErrorTextView("Enter the publication date and citation.");
            card.addView(errPub);
        }

        TextView citeLabel = new TextView(activity);
        citeLabel.setText("Journal or citation");
        citeLabel.setTextColor(UiTheme.NAVY);
        citeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        citeLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(citeLabel);

        publicationCitationInput = createStyledInput(
                isPubError ? "" : state.publication.journalCitation,
                isPubError ? "Enter journal or citation" : "Citation not recorded",
                isPubError
        );
        card.addView(publicationCitationInput);

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

        // Completion validation if Completed is selected in stage or completion
        int compPos = completionSpinner.getSelectedItemPosition();
        String compStatus = COMPLETION_OPTIONS[compPos];
        String compDate = completionDateInput.getText().toString().trim();
        if (("Completed".equalsIgnoreCase(selectedStage) || "Completed".equalsIgnoreCase(compStatus))
                && (TextUtils.isEmpty(compDate) || "YYYY-MM-DD".equalsIgnoreCase(compDate))) {
            previewErrorMode = "8.1e";
            buildUi();
            return;
        }

        // Presentation validation
        int presPos = presentationSpinner.getSelectedItemPosition();
        String presStatus = PRESENTATION_OPTIONS[presPos];
        String presDate = presentationDateInput.getText().toString().trim();
        String presEvent = presentationEventInput.getText().toString().trim();
        if ("Presented".equalsIgnoreCase(presStatus)
                && (TextUtils.isEmpty(presDate) || "YYYY-MM-DD".equalsIgnoreCase(presDate) || TextUtils.isEmpty(presEvent))) {
            previewErrorMode = "8.1f";
            buildUi();
            return;
        }

        // Publication validation
        int pubPos = publicationSpinner.getSelectedItemPosition();
        String pubStatus = PUBLICATION_OPTIONS[pubPos];
        String pubDate = publicationDateInput.getText().toString().trim();
        String pubCite = publicationCitationInput.getText().toString().trim();
        if ("Published".equalsIgnoreCase(pubStatus)
                && (TextUtils.isEmpty(pubDate) || "YYYY-MM-DD".equalsIgnoreCase(pubDate) || TextUtils.isEmpty(pubCite))) {
            previewErrorMode = "8.1g";
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
        if ("Completed".equalsIgnoreCase(compStatus)) {
            state.completion.status = "Completed";
            state.completion.completionDate = compDate;
            state.completion.isSaved = true;
        }
        if ("Presented".equalsIgnoreCase(presStatus)) {
            state.presentation.status = "Presented";
            state.presentation.presentationDate = presDate;
            state.presentation.eventName = presEvent;
        }
        if ("Published".equalsIgnoreCase(pubStatus)) {
            state.publication.status = "Published";
            state.publication.publicationDate = pubDate;
            state.publication.journalCitation = pubCite;
        }

        buildUi();
        if (callbacks != null) callbacks.onStageSaved();
        if (callbacks != null) {
            callbacks.onStageUpdated(state);
        }
    }

    private TextView createErrorTextView(String message) {
        TextView err = new TextView(activity);
        err.setText(message.startsWith("⚠") ? message : "⚠ " + message);
        err.setTextColor(0xffc2410c);
        err.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        err.setTypeface(Typeface.DEFAULT_BOLD);
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
        input.setHintTextColor(0xff94a3b8);
        input.setTextColor(UiTheme.INK);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(isError ? 2 : 1), isError ? 0xffea580c : UiTheme.BORDER);
        input.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(4);
        params.bottomMargin = dp(8);
        input.setLayoutParams(params);
        return input;
    }

    private void styleSpinner(Spinner spinner, boolean isError) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(isError ? 2 : 1), isError ? 0xffea580c : UiTheme.BORDER);
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
        this.state.newStage = "";
        this.state.effectiveDate = "";
        this.state.supportingRef = "";
        this.state.nextAction = "";
        this.state.noNextActionRecorded = true;
        buildUi();
    }

    public void showPreview8_1a_ErrorStageNotAllowed() {
        this.previewErrorMode = "8.1a";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "Registered";
        this.state.effectiveDate = "2026-11-06";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        this.state.noNextActionRecorded = false;
        buildUi();
    }

    public void showPreview8_1b_ErrorEffectiveDate() {
        this.previewErrorMode = "8.1b";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "For revision";
        this.state.effectiveDate = "";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        this.state.noNextActionRecorded = false;
        buildUi();
    }

    public void showPreview8_1c_ErrorSupportingDetails() {
        this.previewErrorMode = "8.1c";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "For revision";
        this.state.effectiveDate = "2026-11-06";
        this.state.supportingRef = "";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        this.state.noNextActionRecorded = false;
        buildUi();
    }

    public void showPreview8_1d_ErrorNextAction() {
        this.previewErrorMode = "8.1d";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "For revision";
        this.state.effectiveDate = "2026-11-06";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.nextAction = "";
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
        this.state.noNextActionRecorded = false;
        this.state.completion.status = "Completed";
        this.state.completion.completionDate = "";
        this.state.completion.isSaved = false;
        buildUi();
    }

    public void showPreview8_1f_ErrorPresentation() {
        this.previewErrorMode = "8.1f";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "For revision";
        this.state.effectiveDate = "2026-11-06";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        this.state.noNextActionRecorded = false;
        this.state.presentation.status = "Presented";
        this.state.presentation.presentationDate = "";
        this.state.presentation.eventName = "";
        buildUi();
    }

    public void showPreview8_1g_ErrorPublication() {
        this.previewErrorMode = "8.1g";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.newStage = "For revision";
        this.state.effectiveDate = "2026-11-06";
        this.state.supportingRef = "Panel evaluation memo 2026-131";
        this.state.nextAction = "Revise Chapter 3 methodology per panel memo.";
        this.state.noNextActionRecorded = false;
        this.state.publication.status = "Published";
        this.state.publication.publicationDate = "";
        this.state.publication.journalCitation = "";
        buildUi();
    }

    public void showPreview8_5a_StageUpdated() {
        this.previewErrorMode = "none";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.currentStage = "For revision";
        this.state.savedBannerMessage = "Stage updated.";
        this.state.savedTimestamp = "Saved Nov 6, 2026, 2:40 PM by LeBron James";
        this.state.newStage = "";
        this.state.effectiveDate = "";
        this.state.supportingRef = "";
        this.state.nextAction = "";
        this.state.noNextActionRecorded = true;
        buildUi();
    }

    public void showPreview8_5b_OutcomeRecorded() {
        this.previewErrorMode = "none";
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        this.state.currentStage = "Completed";
        this.state.savedBannerMessage = "Outcome recorded.";
        this.state.savedTimestamp = "Saved Nov 6, 2026, 2:40 PM by LeBron James";
        this.state.newStage = "Completed";
        this.state.effectiveDate = "2027-03-15";
        this.state.supportingRef = "Final defense result, CRM memo 2027-022";
        this.state.nextAction = "";
        this.state.noNextActionRecorded = true;
        this.state.completion.status = "Completed";
        this.state.completion.completionDate = "2027-03-15";
        this.state.completion.isSaved = true;
        this.state.presentation.status = "Not recorded";
        this.state.publication.status = "Not recorded";
        this.state.publication.publicationDate = "";
        this.state.publication.journalCitation = "";
        buildUi();
    }
}
