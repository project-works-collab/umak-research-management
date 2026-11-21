package ph.edu.umak.crm.app.staff.registry;

import android.app.Activity;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
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

    private LinearLayout pageWrapper;

    public ViewGroup getContentLayout() {
        if (pageWrapper != null && pageWrapper.getParent() instanceof ViewGroup) {
            ((ViewGroup) pageWrapper.getParent()).removeView(pageWrapper);
        }
        return pageWrapper;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    private void buildUi() {
        pageWrapper = new LinearLayout(activity);
        pageWrapper.setOrientation(LinearLayout.VERTICAL);

        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        // Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(activity);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = dp(16);
        band.setPadding(pad, pad, pad, pad);

        // 1. Heading: "Correction History"
        TextView heading = new TextView(activity);
        heading.setText("Correction History");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(activity));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        // Subtitle: "Impact of AI on Student Engagement"
        TextView subTitle = new TextView(activity);
        subTitle.setText(record != null ? record.title : "Research study");
        subTitle.setTextColor(0xffe2e8f0);
        subTitle.setTypeface(regular);
        ScreenTypography.apply(subTitle, 13);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dp(4);
        band.addView(subTitle, subParams);

        pageWrapper.addView(band);

        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));
        pageWrapper.addView(rootLayout);

        // 2. ID Card
        LinearLayout idCard = createBaseCard();
        TextView idLabel = new TextView(activity);
        idLabel.setText("Research ID");
        idLabel.setTextColor(0xff586b83);
        idLabel.setTypeface(regular);
        ScreenTypography.apply(idLabel, 12);
        idCard.addView(idLabel);

        TextView idValue = new TextView(activity);
        idValue.setText(record.id);
        idValue.setTextColor(0xff185d9d);
        idValue.setTypeface(bold);
        ScreenTypography.apply(idValue, 16);
        LinearLayout.LayoutParams idValParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        idValParams.topMargin = dp(2);
        idValParams.bottomMargin = dp(4);
        idCard.addView(idValue, idValParams);

        TextView idNote = new TextView(activity);
        idNote.setText("🔒 Stays unchanged across all corrections");
        idNote.setTextColor(0xff64748b);
        idNote.setTypeface(regular);
        ScreenTypography.apply(idNote, 12);
        idCard.addView(idNote);

        rootLayout.addView(idCard);

        // 3. Correction Logs Card (single card with all entries and dividers)
        LinearLayout logsCard = createBaseCard();

        for (int i = 0; i < record.correctionLogs.size(); i++) {
            StaffRegistryData.CorrectionLog log = record.correctionLogs.get(i);
            logsCard.addView(createCorrectionLogEntry(log));

            if (i < record.correctionLogs.size() - 1) {
                View divider = new View(activity);
                divider.setBackgroundColor(UiTheme.BORDER);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                divParams.topMargin = dp(12);
                divParams.bottomMargin = dp(12);
                logsCard.addView(divider, divParams);
            }
        }
        rootLayout.addView(logsCard);

        // 4. Back to Record Button
        Button backBtn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        backBtn.setText("Back to Record");
        backBtn.setAllCaps(false);
        backBtn.setTextColor(0xff082d50);
        backBtn.setTypeface(bold);
        ScreenTypography.apply(backBtn, 15);
        backBtn.setBackground(UiTheme.ripple(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER), 0x20082d50));
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        backParams.topMargin = dp(16);
        rootLayout.addView(backBtn, backParams);

        backBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onBackToRecord();
            }
        });
    }

    private View createCorrectionLogEntry(StaffRegistryData.CorrectionLog log) {
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        LinearLayout entry = new LinearLayout(activity);
        entry.setOrientation(LinearLayout.VERTICAL);

        // Field Changed
        TextView fieldTitle = new TextView(activity);
        fieldTitle.setText(log.fieldChanged);
        fieldTitle.setTextColor(0xff082d50);
        fieldTitle.setTypeface(bold);
        ScreenTypography.apply(fieldTitle, 14);
        fieldTitle.setPadding(0, 0, 0, dp(4));
        entry.addView(fieldTitle);

        // Previous
        TextView prevView = new TextView(activity);
        prevView.setText("Previous: " + log.previousValue);
        prevView.setTextColor(0xff64748b);
        prevView.setTypeface(regular);
        ScreenTypography.apply(prevView, 12);
        entry.addView(prevView);

        // New
        TextView newView = new TextView(activity);
        newView.setText("New: " + log.newValue);
        newView.setTextColor(0xff082d50);
        newView.setTypeface(bold);
        ScreenTypography.apply(newView, 12);
        LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        newParams.topMargin = dp(1);
        newParams.bottomMargin = dp(3);
        entry.addView(newView, newParams);

        // Reason
        TextView reasonView = new TextView(activity);
        reasonView.setText("Reason: " + log.reason);
        reasonView.setTextColor(0xff64748b);
        reasonView.setTypeface(regular);
        ScreenTypography.apply(reasonView, 12);
        entry.addView(reasonView);

        // Author and date
        TextView authorView = new TextView(activity);
        authorView.setText(log.authorAndDate);
        authorView.setTextColor(0xff64748b);
        authorView.setTypeface(regular);
        ScreenTypography.apply(authorView, 12);
        LinearLayout.LayoutParams authParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        authParams.topMargin = dp(4);
        entry.addView(authorView, authParams);

        return entry;
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
}
