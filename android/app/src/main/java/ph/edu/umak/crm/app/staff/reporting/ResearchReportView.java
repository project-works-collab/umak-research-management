package ph.edu.umak.crm.app.staff.reporting;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Research Report View (Wireframes 9.1 Result & 9.3 Empty).
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class ResearchReportView {

    public interface Callbacks {
        void onBackToFilters();
        void onOpenRecord(String recordId);
    }

    private final Activity activity;
    private final Callbacks callbacks;
    private StaffReportingData.ResearchReportModel model;
    private ScrollView scrollView;
    private LinearLayout rootLayout;

    public ResearchReportView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        this.model = StaffReportingData.getResearchReportResult();
    }

    public void setModel(StaffReportingData.ResearchReportModel model) {
        this.model = model;
        if (rootLayout != null) {
            buildLayout();
        }
    }

    public View getContentLayout() {
        if (scrollView != null && scrollView.getParent() instanceof ViewGroup) {
            ((ViewGroup) scrollView.getParent()).removeView(scrollView);
        }

        scrollView = new ScrollView(activity);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(UiTheme.PAPER);

        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));

        scrollView.addView(PageTitle.wrap(rootLayout, "Research Report", null, false));
        buildLayout();

        return scrollView;
    }

    private void buildLayout() {
        rootLayout.removeAllViews();

        // 2. Applied Filters Card
        renderAppliedFilters();

        // 3. Metrics Section
        renderMetrics();

        // 4. Metric Definitions Card
        renderMetricDefinitions();

        // 5. Underlying Records
        renderUnderlyingRecords();

        // 6. Action Buttons
        renderActionButtons();
    }

    private void renderAppliedFilters() {
        LinearLayout filtersCard = new LinearLayout(activity);
        filtersCard.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(14);
        filtersCard.setPadding(pad, pad, pad, pad);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(UiTheme.WHITE);
        cardBg.setCornerRadius(dp(8));
        cardBg.setStroke(dp(1), UiTheme.BORDER);
        filtersCard.setBackground(cardBg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(16);
        rootLayout.addView(filtersCard, params);

        TextView header = new TextView(activity);
        header.setText("Applied filters");
        header.setTextColor(UiTheme.NAVY);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        filtersCard.addView(header);

        ReportFilterChips.addRow(filtersCard, model.collegeFilter, model.registrantCategoryFilter);
        ReportFilterChips.addRow(filtersCard, model.stageFilter, model.linkedServicesFilter);
        ReportFilterChips.addRow(filtersCard, model.academicPeriodFilter, model.outcomeFilter);

        if (model.generatedTimestamp != null) {
            TextView ts = new TextView(activity);
            ts.setText(model.generatedTimestamp);
            ts.setTextColor(UiTheme.MUTED);
            ts.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            LinearLayout.LayoutParams tsParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tsParams.topMargin = dp(6);
            filtersCard.addView(ts, tsParams);
        }
    }

    private void renderMetrics() {
        // Distinct studies and completion
        LinearLayout row1 = new LinearLayout(activity);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams r1Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        r1Params.bottomMargin = dp(8);
        rootLayout.addView(row1, r1Params);

        row1.addView(createMetricTile(String.valueOf(model.distinctStudies), "Distinct studies", 0));
        row1.addView(createMetricTile(String.valueOf(model.completed), "Completed", dp(8)));

        // Presentation and publication
        LinearLayout row2 = new LinearLayout(activity);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams r2Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        r2Params.bottomMargin = dp(8);
        rootLayout.addView(row2, r2Params);

        row2.addView(createMetricTile(String.valueOf(model.presented), "Presented", 0));
        row2.addView(createMetricTile(String.valueOf(model.published), "Published", dp(8)));

        // Linked researchers spans the full width
        LinearLayout row3 = new LinearLayout(activity);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams r3Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        r3Params.bottomMargin = dp(16);
        rootLayout.addView(row3, r3Params);

        row3.addView(createMetricTile(String.valueOf(model.distinctLinkedResearchers),
                "Distinct linked researchers", 0));
    }

    private View createMetricTile(String value, String label, int leftMargin) {
        LinearLayout tile = new LinearLayout(activity);
        tile.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(14);
        tile.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), UiTheme.BORDER);
        tile.setBackground(bg);

        TextView valTv = new TextView(activity);
        valTv.setText(value);
        valTv.setTextColor(UiTheme.NAVY);
        valTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        valTv.setTypeface(Typeface.DEFAULT_BOLD);
        tile.addView(valTv);

        TextView lblTv = new TextView(activity);
        lblTv.setText(label);
        lblTv.setTextColor(UiTheme.MUTED);
        lblTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams lblParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lblParams.topMargin = dp(2);
        tile.addView(lblTv, lblParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        params.leftMargin = leftMargin;
        tile.setLayoutParams(params);
        return tile;
    }

    private void renderMetricDefinitions() {
        LinearLayout defCard = new LinearLayout(activity);
        defCard.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(14);
        defCard.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), UiTheme.BORDER);
        defCard.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(16);
        rootLayout.addView(defCard, params);

        TextView title = new TextView(activity);
        title.setText("Metric definitions");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        defCard.addView(title);

        defCard.addView(createDefinitionItem("Distinct studies", "Each research record is counted once."));
        defCard.addView(createDefinitionItem("Completed · Presented · Published", "Independent outcome counts. One study can count in more than one."));
        defCard.addView(createDefinitionItem("Distinct linked researchers", "Each linked researcher or adviser is counted once across all studies."));
    }

    private View createDefinitionItem(String heading, String description) {
        LinearLayout item = new LinearLayout(activity);
        item.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        item.setLayoutParams(params);

        TextView hTv = new TextView(activity);
        hTv.setText(heading);
        hTv.setTextColor(UiTheme.NAVY);
        hTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        hTv.setTypeface(Typeface.DEFAULT_BOLD);
        item.addView(hTv);

        TextView dTv = new TextView(activity);
        dTv.setText(description);
        dTv.setTextColor(UiTheme.MUTED);
        dTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        dTv.setLineSpacing(dp(2), 1.0f);
        item.addView(dTv);

        return item;
    }

    private void renderUnderlyingRecords() {
        LinearLayout recordsSection = new LinearLayout(activity);
        recordsSection.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(14);
        recordsSection.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), UiTheme.BORDER);
        recordsSection.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(20);
        rootLayout.addView(recordsSection, params);

        TextView title = new TextView(activity);
        title.setText("Underlying records");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        recordsSection.addView(title);

        List<StaffReportingData.RecordSummary> records = model.underlyingRecords;
        if (records.isEmpty()) {
            TextView emptyTv = new TextView(activity);
            emptyTv.setText("No matching records");
            emptyTv.setTextColor(UiTheme.MUTED);
            emptyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            emptyTv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams eParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            eParams.topMargin = dp(16);
            eParams.bottomMargin = dp(12);
            recordsSection.addView(emptyTv, eParams);
        } else {
            for (int i = 0; i < records.size(); i++) {
                StaffReportingData.RecordSummary rec = records.get(i);
                View recRow = createRecordRow(rec);
                recordsSection.addView(recRow);

                if (i < records.size() - 1) {
                    View divider = new View(activity);
                    divider.setBackgroundColor(UiTheme.BORDER);
                    LinearLayout.LayoutParams dParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                    dParams.topMargin = dp(10);
                    dParams.bottomMargin = dp(10);
                    recordsSection.addView(divider, dParams);
                }
            }
        }
    }

    private View createRecordRow(StaffReportingData.RecordSummary rec) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(10);
        row.setLayoutParams(params);

        LinearLayout topLine = new LinearLayout(activity);
        topLine.setOrientation(LinearLayout.HORIZONTAL);
        topLine.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(topLine);

        TextView idTv = new TextView(activity);
        idTv.setText(rec.id);
        idTv.setTextColor(UiTheme.ACCENT);
        idTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        idTv.setTypeface(Typeface.DEFAULT_BOLD);
        topLine.addView(idTv);

        View spacer = new View(activity);
        topLine.addView(spacer, new LinearLayout.LayoutParams(0, 0, 1.0f));

        TextView badge = new TextView(activity);
        badge.setText(rec.status);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        int bPadH = dp(8);
        int bPadV = dp(3);
        badge.setPadding(bPadH, bPadV, bPadH, bPadV);

        GradientDrawable bBg = new GradientDrawable();
        bBg.setCornerRadius(dp(4));
        if ("Completed".equalsIgnoreCase(rec.status)) {
            bBg.setColor(UiTheme.STATUS_COMPLETED_BG);
            badge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
        } else {
            bBg.setColor(UiTheme.AMBER_BG);
            badge.setTextColor(UiTheme.AMBER_TEXT);
        }
        badge.setBackground(bBg);
        topLine.addView(badge);

        TextView titleTv = new TextView(activity);
        titleTv.setText(rec.title);
        titleTv.setTextColor(UiTheme.NAVY);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tParams.topMargin = dp(4);
        row.addView(titleTv, tParams);

        row.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onOpenRecord(rec.id);
        });

        return row;
    }

    public void showExport(ExportReportDialog.ExportState state) {
        new ExportReportDialog(activity, ReportExportData.from(model)).show(state);
    }

    private void renderActionButtons() {
        LinearLayout btnCol = new LinearLayout(activity);
        btnCol.setOrientation(LinearLayout.VERTICAL);
        rootLayout.addView(btnCol);

        // Export Button
        Button exportBtn = new Button(activity);
        exportBtn.setText("Export");
        exportBtn.setTextColor(UiTheme.WHITE);
        exportBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        exportBtn.setTypeface(Typeface.DEFAULT_BOLD);

        GradientDrawable expBg = new GradientDrawable();
        expBg.setColor(UiTheme.NAVY);
        expBg.setCornerRadius(dp(8));
        exportBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), expBg, null));

        exportBtn.setOnClickListener(v -> {
            showExport(ExportReportDialog.ExportState.FORMAT_PICKER);
        });
        LinearLayout.LayoutParams expParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        btnCol.addView(exportBtn, expParams);

        // Back to Filters Button
        Button backBtn = new Button(activity);
        backBtn.setText("Back to Filters");
        backBtn.setTextColor(UiTheme.NAVY);
        backBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        backBtn.setTypeface(Typeface.DEFAULT);

        GradientDrawable backBg = new GradientDrawable();
        backBg.setColor(UiTheme.WHITE);
        backBg.setStroke(dp(1), UiTheme.BORDER);
        backBg.setCornerRadius(dp(8));
        backBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22000000), backBg, null));

        backBtn.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onBackToFilters();
        });
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        backParams.topMargin = dp(8);
        btnCol.addView(backBtn, backParams);
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, activity.getResources().getDisplayMetrics());
    }
}
