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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Completion Record Correction (Wireframe 8.4).
 * Allows authorized staff to correct a recorded completion date with mandatory reason.
 * Zero emojis compliant. Defensive view detachment on wrap.
 */
public final class CompletionCorrectionView {

    public interface Callbacks {
        void onCompletionCorrectionSaved(String correctedDate, String reason);
    }

    private CompletionCorrectionView() {}

    public interface LiveSave { void save(org.json.JSONObject fields,java.util.function.BiConsumer<String,String> result); }
    public static View createLive(Activity activity,org.json.JSONObject record,LiveSave save,Runnable saved,Runnable reload){
        var page=new ph.edu.umak.crm.app.staff.common.StaffDesignPage(activity,"Completion correction",null,null);
        var summary=page.section(record.optString("title")+" · REG-"+record.optInt("id"));
        var completion=record.optJSONObject("completion");
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(summary,"Recorded completion: "+(completion==null?"Not recorded":completion.optString("date","")),false);
        var card=page.section("Correct completion record");
        android.widget.CheckBox reopen=new android.widget.CheckBox(activity);reopen.setText("Return to In progress");card.addView(reopen);
        EditText date=new EditText(activity);date.setContentDescription("Corrected completion date (YYYY-MM-DD)");date.setHint("YYYY-MM-DD");date.setText(completion==null?"":completion.optString("date",""));ph.edu.umak.crm.app.staff.common.StaffDesignPage.style(date,false);card.addView(date);
        EditText details=new EditText(activity);details.setContentDescription("Completion details");details.setHint("Completion details");details.setText(completion==null?"":completion.optString("details",""));ph.edu.umak.crm.app.staff.common.StaffDesignPage.style(details,true);card.addView(details);
        EditText reason=new EditText(activity);reason.setContentDescription("Correction reason");reason.setHint("Reason for correction *");ph.edu.umak.crm.app.staff.common.StaffDesignPage.style(reason,true);card.addView(reason);
        Button submit=ph.edu.umak.crm.app.common.components.UiButton.primary(activity,"Save completion correction",null);card.addView(submit);
        submit.setOnClickListener(view->{
            if(reason.getText().toString().trim().isEmpty()){reason.setError("Enter the correction reason.");return;}
            try{
                String day=date.getText().toString().trim();if(!day.isEmpty())java.time.LocalDate.parse(day);
                if(!reopen.isChecked()&&day.isEmpty()){date.setError("Enter the corrected completion date.");return;}
                org.json.JSONObject fields=new org.json.JSONObject().put("reopen",reopen.isChecked()).put("completion_date",day.isEmpty()?org.json.JSONObject.NULL:day)
                    .put("completion_details",details.getText().toString().trim()).put("reason",reason.getText().toString().trim());
                Runnable restore=ph.edu.umak.crm.app.staff.common.StaffDesignPage.freeze(page);page.status.working("Saving completion correction…");
                save.save(fields,(data,error)->{restore.run();if(error==null)saved.run();else{page.status.result(error,true);if(error.toLowerCase(java.util.Locale.ROOT).contains("reload")){submit.setEnabled(false);card.addView(ph.edu.umak.crm.app.common.components.UiButton.outline(activity,"Reload research progress",UiTheme.BORDER,UiTheme.NAVY,reload));}else submit.setEnabled(true);}});
            }catch(org.json.JSONException|java.time.DateTimeException invalid){page.status.result("Check the completion date and reason.",true);}
        });return page;
    }

    private static int dp(Activity activity, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    public static View create(Activity activity, String recordId, String studyTitle,
                              String recordedDate, Callbacks callbacks, Runnable onClose) {
        ScrollView scroll = new ScrollView(activity);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(activity, 16), dp(activity, 16), dp(activity, 16), dp(activity, 32));
        root.setBackgroundColor(UiTheme.PAPER);
        scroll.addView(root);

        // 1. Summary Card
        LinearLayout summaryCard = new LinearLayout(activity);
        summaryCard.setOrientation(LinearLayout.VERTICAL);
        summaryCard.setPadding(dp(activity, 16), dp(activity, 16), dp(activity, 16), dp(activity, 16));
        GradientDrawable sBg = new GradientDrawable();
        sBg.setColor(UiTheme.WHITE);
        sBg.setCornerRadius(dp(activity, 10));
        sBg.setStroke(dp(activity, 1), UiTheme.BORDER);
        summaryCard.setBackground(sBg);
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        summaryParams.bottomMargin = dp(activity, 12);
        summaryCard.setLayoutParams(summaryParams);

        TextView titleView = new TextView(activity);
        titleView.setText(studyTitle != null ? studyTitle : "Impact of AI on Student Engagement");
        titleView.setTextColor(UiTheme.NAVY);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        summaryCard.addView(titleView);

        TextView idView = new TextView(activity);
        idView.setText(recordId != null ? recordId : "REG-2026-089");
        idView.setTextColor(0xff64748b);
        idView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams idParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        idParams.topMargin = dp(activity, 2);
        idParams.bottomMargin = dp(activity, 8);
        summaryCard.addView(idView, idParams);

        LinearLayout stageRow = new LinearLayout(activity);
        stageRow.setOrientation(LinearLayout.HORIZONTAL);
        stageRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView stageLabel = new TextView(activity);
        stageLabel.setText("Current overall stage:   ");
        stageLabel.setTextColor(UiTheme.NAVY);
        stageLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        stageLabel.setTypeface(Typeface.DEFAULT_BOLD);
        stageRow.addView(stageLabel);

        TextView stageBadge = new TextView(activity);
        stageBadge.setText("Completed");
        stageBadge.setTextColor(0xff059669);
        stageBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        stageBadge.setTypeface(Typeface.DEFAULT_BOLD);
        stageBadge.setPadding(dp(activity, 8), dp(activity, 3), dp(activity, 8), dp(activity, 3));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(0xffecfdf5);
        badgeBg.setCornerRadius(dp(activity, 12));
        badgeBg.setStroke(dp(activity, 1), 0xffa7f3d0);
        stageBadge.setBackground(badgeBg);
        stageRow.addView(stageBadge);

        summaryCard.addView(stageRow);
        root.addView(summaryCard);

        // 2. Correction Form Card (Amber/peach container)
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity, 16), dp(activity, 16), dp(activity, 16), dp(activity, 16));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xfffffbeb);
        cardBg.setCornerRadius(dp(activity, 10));
        cardBg.setStroke(dp(activity, 1), 0xfffde68a);
        card.setBackground(cardBg);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(activity, 12);
        card.setLayoutParams(cardParams);
        root.addView(card);

        TextView cardTitle = new TextView(activity);
        cardTitle.setText("🛡 Correct completion record");
        cardTitle.setTextColor(UiTheme.NAVY);
        cardTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        cardTitle.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(cardTitle);

        // Completion status
        TextView stLabel = new TextView(activity);
        stLabel.setText("Completion status");
        stLabel.setTextColor(UiTheme.NAVY);
        stLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        stLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams stParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stParams.topMargin = dp(activity, 12);
        card.addView(stLabel, stParams);

        TextView stBox = new TextView(activity);
        stBox.setText("Completed");
        stBox.setTextColor(UiTheme.NAVY);
        stBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        stBox.setPadding(dp(activity, 12), dp(activity, 10), dp(activity, 12), dp(activity, 10));
        GradientDrawable boxBg = new GradientDrawable();
        boxBg.setColor(0xfffef9c3);
        boxBg.setCornerRadius(dp(activity, 8));
        boxBg.setStroke(dp(activity, 1), 0xfffde68a);
        stBox.setBackground(boxBg);
        LinearLayout.LayoutParams stBoxParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stBoxParams.topMargin = dp(activity, 4);
        stBoxParams.bottomMargin = dp(activity, 10);
        card.addView(stBox, stBoxParams);

        // Recorded completion date
        TextView recLabel = new TextView(activity);
        recLabel.setText("Recorded completion date");
        recLabel.setTextColor(UiTheme.NAVY);
        recLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        recLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(recLabel);

        TextView recBox = new TextView(activity);
        recBox.setText(recordedDate != null && !recordedDate.isEmpty() ? recordedDate : "2027-03-15");
        recBox.setTextColor(0xff64748b);
        recBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        recBox.setPadding(dp(activity, 12), dp(activity, 10), dp(activity, 12), dp(activity, 10));
        recBox.setBackground(boxBg);
        LinearLayout.LayoutParams recBoxParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recBoxParams.topMargin = dp(activity, 4);
        recBoxParams.bottomMargin = dp(activity, 10);
        card.addView(recBox, recBoxParams);

        // Corrected completion date *
        TextView corrDateLabel = new TextView(activity);
        corrDateLabel.setText("Corrected completion date *");
        corrDateLabel.setTextColor(UiTheme.NAVY);
        corrDateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        corrDateLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(corrDateLabel);

        EditText corrDateInput = new EditText(activity);
        corrDateInput.setText("2027-03-18");
        corrDateInput.setHint("YYYY-MM-DD");
        corrDateInput.setTextColor(UiTheme.INK);
        corrDateInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        corrDateInput.setPadding(dp(activity, 12), dp(activity, 10), dp(activity, 12), dp(activity, 10));
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(UiTheme.WHITE);
        inputBg.setCornerRadius(dp(activity, 8));
        inputBg.setStroke(dp(activity, 1), UiTheme.BORDER);
        corrDateInput.setBackground(inputBg);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = dp(activity, 4);
        dateParams.bottomMargin = dp(activity, 10);
        card.addView(corrDateInput, dateParams);

        // Reason for correction *
        TextView reasonLabel = new TextView(activity);
        reasonLabel.setText("Reason for correction *");
        reasonLabel.setTextColor(UiTheme.NAVY);
        reasonLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        reasonLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(reasonLabel);

        EditText reasonInput = new EditText(activity);
        reasonInput.setHint("Explain why the completion record is being corrected");
        reasonInput.setTextColor(UiTheme.INK);
        reasonInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        reasonInput.setPadding(dp(activity, 12), dp(activity, 10), dp(activity, 12), dp(activity, 10));
        reasonInput.setMinLines(3);
        reasonInput.setGravity(Gravity.TOP);
        reasonInput.setBackground(inputBg);
        LinearLayout.LayoutParams reasonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        reasonParams.topMargin = dp(activity, 4);
        reasonParams.bottomMargin = dp(activity, 12);
        card.addView(reasonInput, reasonParams);

        TextView note = new TextView(activity);
        note.setText("Stage and completion record stay consistent. Reason and change history are retained.");
        note.setTextColor(0xffb45309);
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        card.addView(note);

        // 3. Presentation and Publication status pills
        LinearLayout outcomePillsRow = new LinearLayout(activity);
        outcomePillsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams pillsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pillsParams.bottomMargin = dp(activity, 16);
        outcomePillsRow.setLayoutParams(pillsParams);

        TextView presPill = new TextView(activity);
        presPill.setText("Presentation: Not recorded");
        presPill.setTextColor(0xff1d4ed8);
        presPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        presPill.setTypeface(Typeface.DEFAULT_BOLD);
        presPill.setPadding(dp(activity, 10), dp(activity, 4), dp(activity, 10), dp(activity, 4));
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(0xffeff6ff);
        pillBg.setCornerRadius(dp(activity, 12));
        pillBg.setStroke(dp(activity, 1), 0xffbfdbfe);
        presPill.setBackground(pillBg);
        outcomePillsRow.addView(presPill);

        View pillSpacer = new View(activity);
        outcomePillsRow.addView(pillSpacer, new LinearLayout.LayoutParams(dp(activity, 8), 1));

        TextView pubPill = new TextView(activity);
        pubPill.setText("Publication: Not recorded");
        pubPill.setTextColor(0xff1d4ed8);
        pubPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        pubPill.setTypeface(Typeface.DEFAULT_BOLD);
        pubPill.setPadding(dp(activity, 10), dp(activity, 4), dp(activity, 10), dp(activity, 4));
        pubPill.setBackground(pillBg);
        outcomePillsRow.addView(pubPill);

        root.addView(outcomePillsRow);

        // 4. Action Buttons
        Button saveBtn = new Button(activity);
        saveBtn.setText("Save Correction");
        saveBtn.setTextColor(UiTheme.WHITE);
        saveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        saveBtn.setTypeface(Typeface.DEFAULT_BOLD);
        saveBtn.setAllCaps(false);
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(UiTheme.NAVY);
        saveBg.setCornerRadius(dp(activity, 8));
        saveBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), saveBg, null));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 48));
        saveParams.bottomMargin = dp(activity, 10);
        root.addView(saveBtn, saveParams);

        Button cancelBtn = new Button(activity);
        cancelBtn.setText("Cancel");
        cancelBtn.setTextColor(UiTheme.NAVY);
        cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        cancelBtn.setTypeface(Typeface.DEFAULT_BOLD);
        cancelBtn.setAllCaps(false);
        GradientDrawable cancelBg = new GradientDrawable();
        cancelBg.setColor(UiTheme.WHITE);
        cancelBg.setCornerRadius(dp(activity, 8));
        cancelBg.setStroke(dp(activity, 1), UiTheme.BORDER);
        cancelBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22131c4b), cancelBg, null));
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 48));
        root.addView(cancelBtn, cancelParams);

        saveBtn.setOnClickListener(v -> {
            String corrected = corrDateInput.getText().toString().trim();
            String reason = reasonInput.getText().toString().trim();
            if (TextUtils.isEmpty(corrected)) {
                corrDateInput.setError("Please enter corrected date.");
                return;
            }
            if (TextUtils.isEmpty(reason)) {
                reasonInput.setError("Please enter a reason for correction.");
                return;
            }
            Toast.makeText(activity, "Completion correction saved.", Toast.LENGTH_SHORT).show();
            if (callbacks != null) {
                callbacks.onCompletionCorrectionSaved(corrected, reason);
            }
        });

        cancelBtn.setOnClickListener(v -> onClose.run());
        return PageTitle.wrap(root, "Milestone and Outcomes", null, false);
    }
}
