package ph.edu.umak.crm.app.staff.registry;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Research Record Correction History View (Wireframe 7.7).
 * Displays immutable audit trail for research title, co-authors, and academic period changes.
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class RegistryCorrectionHistoryView {

    public interface Callbacks {
        void onBackToRecord();
    }

    private final Activity activity;
    private final Callbacks callbacks;
    private StaffRegistryData.RegistryRecordItem record;
    private LinearLayout rootLayout;

    public RegistryCorrectionHistoryView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        this.record = StaffRegistryData.getMockRecords().get(0);
        buildUi();
    }

    public void setRecord(StaffRegistryData.RegistryRecordItem record) {
        if (record != null) {
            this.record = record;
            buildUi();
        }
    }

    public ViewGroup getContentLayout() {
        if (rootLayout != null && rootLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) rootLayout.getParent()).removeView(rootLayout);
        }
        return PageTitle.wrap(rootLayout, "Correction History", record.title, false);
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


        // 2. ID Card
        LinearLayout idCard = createBaseCard();
        TextView idLabel = new TextView(activity);
        idLabel.setText("Research ID");
        idLabel.setTextColor(UiTheme.MUTED);
        idLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        idCard.addView(idLabel);

        TextView idValue = new TextView(activity);
        idValue.setText(record.id);
        idValue.setTextColor(UiTheme.NAVY);
        idValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        idValue.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams idValParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        idValParams.topMargin = dp(2);
        idValParams.bottomMargin = dp(4);
        idCard.addView(idValue, idValParams);

        TextView idNote = new TextView(activity);
        idNote.setText("Stays unchanged across all corrections");
        idNote.setTextColor(UiTheme.MUTED);
        idNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        idCard.addView(idNote);

        rootLayout.addView(idCard);

        // 3. Correction Logs Cards
        for (int i = 0; i < record.correctionLogs.size(); i++) {
            StaffRegistryData.CorrectionLog log = record.correctionLogs.get(i);
            rootLayout.addView(createCorrectionLogCard(log));
        }

        // 4. Back to Record Button
        Button backBtn = new Button(activity);
        backBtn.setText("Back to Record");
        backBtn.setTextColor(UiTheme.NAVY);
        backBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        backBtn.setTypeface(Typeface.DEFAULT_BOLD);
        backBtn.setAllCaps(false);
        GradientDrawable backBg = new GradientDrawable();
        backBg.setColor(UiTheme.WHITE);
        backBg.setCornerRadius(dp(8));
        backBg.setStroke(dp(1), UiTheme.BORDER);
        backBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22131c4b), backBg, null));
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        backParams.topMargin = dp(16);
        rootLayout.addView(backBtn, backParams);

        backBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onBackToRecord();
            }
        });
    }

    private View createCorrectionLogCard(StaffRegistryData.CorrectionLog log) {
        LinearLayout card = createBaseCard();

        // Field Changed
        TextView fieldTitle = new TextView(activity);
        fieldTitle.setText(log.fieldChanged);
        fieldTitle.setTextColor(UiTheme.NAVY);
        fieldTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        fieldTitle.setTypeface(Typeface.DEFAULT_BOLD);
        fieldTitle.setPadding(0, 0, 0, dp(6));
        card.addView(fieldTitle);

        // Previous
        TextView prevView = new TextView(activity);
        prevView.setText("Previous: " + log.previousValue);
        prevView.setTextColor(UiTheme.MUTED);
        prevView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        card.addView(prevView);

        // New
        TextView newView = new TextView(activity);
        newView.setText("New: " + log.newValue);
        newView.setTextColor(UiTheme.INK);
        newView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        newView.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        newParams.topMargin = dp(2);
        newParams.bottomMargin = dp(4);
        card.addView(newView, newParams);

        // Reason
        TextView reasonView = new TextView(activity);
        reasonView.setText("Reason: " + log.reason);
        reasonView.setTextColor(UiTheme.INK);
        reasonView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        card.addView(reasonView);

        // Author and date
        TextView authorView = new TextView(activity);
        authorView.setText(log.authorAndDate);
        authorView.setTextColor(UiTheme.MUTED);
        authorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams authParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        authParams.topMargin = dp(6);
        card.addView(authorView, authParams);

        return card;
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
}
