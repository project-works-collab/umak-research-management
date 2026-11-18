package ph.edu.umak.crm.app.staff.lifecycle;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Completion Record Correction (Wireframe 8.4).
 * Allows authorized staff to correct a recorded completion date with mandatory reason.
 * Zero emojis compliant.
 */
public final class CompletionCorrectionView {

    public interface Callbacks {
        void onCompletionCorrectionSaved(String correctedDate, String reason);
    }

    private CompletionCorrectionView() {}

    private static int dp(Activity activity, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    public static View create(Activity activity, String recordId, String studyTitle,
                              String recordedDate, Callbacks callbacks, Runnable onClose) {
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(activity, 20), dp(activity, 20), dp(activity, 20), dp(activity, 24));
        root.setBackgroundColor(UiTheme.PAPER);

        TextView subContext = new TextView(activity);
        subContext.setText(studyTitle + "\n" + recordId + "\nCurrent overall stage: Completed");
        subContext.setTextColor(UiTheme.NAVY);
        int summaryPadding = dp(activity, 12);
        subContext.setPadding(summaryPadding, summaryPadding, summaryPadding, summaryPadding);
        subContext.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER));
        subContext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subParams.bottomMargin = dp(activity, 16);
        root.addView(subContext, subParams);

        // Card
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity, 16), dp(activity, 16), dp(activity, 16), dp(activity, 16));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(UiTheme.AMBER_BG);
        cardBg.setCornerRadius(dp(activity, 10));
        cardBg.setStroke(dp(activity, 1), UiTheme.AMBER_BORDER);
        card.setBackground(cardBg);
        root.addView(card);

        TextView cardTitle = new TextView(activity);
        cardTitle.setText("Correct completion record");
        cardTitle.setTextColor(UiTheme.NAVY);
        cardTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        cardTitle.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(cardTitle);

        TextView banner = new TextView(activity);
        banner.setText("Stage and completion record stay consistent. Reason and change history are retained.");
        banner.setTextColor(UiTheme.MUTED);
        banner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bannerParams.topMargin = dp(activity, 6);
        bannerParams.bottomMargin = dp(activity, 14);
        card.addView(banner, bannerParams);

        // Completion status
        TextView stLabel = new TextView(activity);
        stLabel.setText("Completion status");
        stLabel.setTextColor(UiTheme.MUTED);
        stLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        card.addView(stLabel);

        TextView stVal = new TextView(activity);
        stVal.setText("Completed");
        stVal.setTextColor(UiTheme.NAVY);
        stVal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        stVal.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams stValParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stValParams.topMargin = dp(activity, 2);
        stValParams.bottomMargin = dp(activity, 12);
        card.addView(stVal, stValParams);

        // Recorded completion date
        TextView recLabel = new TextView(activity);
        recLabel.setText("Recorded completion date");
        recLabel.setTextColor(UiTheme.MUTED);
        recLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        card.addView(recLabel);

        TextView recVal = new TextView(activity);
        recVal.setText(recordedDate != null && !recordedDate.isEmpty() ? recordedDate : "2027-03-15");
        recVal.setTextColor(UiTheme.MUTED);
        recVal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams recValParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recValParams.topMargin = dp(activity, 2);
        recValParams.bottomMargin = dp(activity, 12);
        card.addView(recVal, recValParams);

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
        dateParams.bottomMargin = dp(activity, 12);
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
        reasonInput.setBackground(inputBg);
        LinearLayout.LayoutParams reasonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        reasonParams.topMargin = dp(activity, 4);
        reasonParams.bottomMargin = dp(activity, 10);
        card.addView(reasonInput, reasonParams);

        TextView note = new TextView(activity);
        note.setText("Presentation: Not recorded  Publication: Not recorded");
        note.setTextColor(UiTheme.MUTED);
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        card.addView(note);

        // Actions
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
        saveParams.topMargin = dp(activity, 16);
        saveParams.bottomMargin = dp(activity, 8);
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
                reasonInput.setError("Please explain reason for correction.");
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
