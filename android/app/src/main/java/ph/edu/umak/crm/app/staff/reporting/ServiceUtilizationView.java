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

import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Service Utilization View (Wireframe 9.2 Result).
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class ServiceUtilizationView {

    public interface Callbacks {
        void onBackToFilters();
        void onExport();
        void onOpenRequest(String requestId);
    }

    private final Activity activity;
    private final Callbacks callbacks;
    private StaffReportingData.ServiceUtilizationModel model;
    private ScrollView scrollView;
    private LinearLayout rootLayout;

    public ServiceUtilizationView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        this.model = StaffReportingData.getServiceUtilizationResult();
    }

    public void setModel(StaffReportingData.ServiceUtilizationModel model) {
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

        scrollView.addView(rootLayout);
        buildLayout();

        return scrollView;
    }

    private void buildLayout() {
        rootLayout.removeAllViews();

        // 1. Top Header
        TextView orgLabel = new TextView(activity);
        orgLabel.setText("Center for Research Management");
        orgLabel.setTextColor(UiTheme.NAVY);
        orgLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        orgLabel.setTypeface(Typeface.DEFAULT_BOLD);
        rootLayout.addView(orgLabel);

        TextView screenTitle = new TextView(activity);
        screenTitle.setText("Service Utilization");
        screenTitle.setTextColor(UiTheme.NAVY);
        screenTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        screenTitle.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(4);
        titleParams.bottomMargin = dp(12);
        rootLayout.addView(screenTitle, titleParams);

        // 2. Applied Filters Card
        renderAppliedFilters();

        // 3. Metrics Section
        renderMetrics();

        // 4. Requests per Service Card
        renderRequestsPerService();

        // 5. Metric Definitions / Notes Card
        renderMetricNotes();

        // 6. Underlying Requests
        renderUnderlyingRequests();

        // 7. Action Buttons
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

        if (model.requesterCollegeFilter != null || model.submittedPeriodFilter != null) {
            String line1 = (model.requesterCollegeFilter != null ? model.requesterCollegeFilter : "") +
                    (model.submittedPeriodFilter != null ? "    " + model.submittedPeriodFilter : "");
            filtersCard.addView(createFilterText(line1));
        }

        if (model.serviceFilter != null || model.statusFilter != null) {
            String line2 = (model.serviceFilter != null ? model.serviceFilter : "") +
                    (model.statusFilter != null ? "    " + model.statusFilter : "");
            filtersCard.addView(createFilterText(line2));
        }

        if (model.requesterCategoryFilter != null) {
            filtersCard.addView(createFilterText(model.requesterCategoryFilter));
        }

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

    private TextView createFilterText(String text) {
        TextView tv = new TextView(activity);
        tv.setText(text);
        tv.setTextColor(UiTheme.NAVY);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(4);
        tv.setLayoutParams(p);
        return tv;
    }

    private void renderMetrics() {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rParams.bottomMargin = dp(16);
        rootLayout.addView(row, rParams);

        // Tile 1: 2 Distinct requests
        row.addView(createMetricTile(String.valueOf(model.distinctRequests), "Distinct requests", null, 0));

        // Tile 2: — Pending
        row.addView(createMetricTile(model.pendingValue, "Pending", model.pendingNote, dp(8)));

        // Tile 3: 1 Completed
        row.addView(createMetricTile(String.valueOf(model.completed), "Completed", null, dp(8)));
    }

    private View createMetricTile(String value, String label, String subNote, int leftMargin) {
        LinearLayout tile = new LinearLayout(activity);
        tile.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(12);
        tile.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), UiTheme.BORDER);
        tile.setBackground(bg);

        TextView valTv = new TextView(activity);
        valTv.setText(value);
        valTv.setTextColor(UiTheme.NAVY);
        valTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
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

        if (subNote != null) {
            TextView noteTv = new TextView(activity);
            noteTv.setText(subNote);
            noteTv.setTextColor(UiTheme.MUTED);
            noteTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            noteTv.setLineSpacing(dp(1), 1.0f);
            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            noteParams.topMargin = dp(2);
            tile.addView(noteTv, noteParams);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        params.leftMargin = leftMargin;
        tile.setLayoutParams(params);
        return tile;
    }

    private void renderRequestsPerService() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(14);
        card.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), UiTheme.BORDER);
        card.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(16);
        rootLayout.addView(card, params);

        TextView title = new TextView(activity);
        title.setText("Requests per service");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(title);

        for (StaffReportingData.ServiceCountItem item : model.requestsPerService) {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rParams.topMargin = dp(10);
            row.setLayoutParams(rParams);

            TextView nameTv = new TextView(activity);
            nameTv.setText(item.serviceName);
            nameTv.setTextColor(UiTheme.NAVY);
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            nameTv.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(nameTv);

            View spacer = new View(activity);
            row.addView(spacer, new LinearLayout.LayoutParams(0, 0, 1.0f));

            TextView countTv = new TextView(activity);
            countTv.setText(item.requestCount + " requests");
            countTv.setTextColor(UiTheme.MUTED);
            countTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            row.addView(countTv);

            card.addView(row);
        }
    }

    private void renderMetricNotes() {
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

        TextView note = new TextView(activity);
        note.setText("Pending shown as — until definition confirmed");
        note.setTextColor(UiTheme.MUTED);
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams nParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nParams.topMargin = dp(6);
        defCard.addView(note, nParams);
    }

    private void renderUnderlyingRequests() {
        LinearLayout requestsSection = new LinearLayout(activity);
        requestsSection.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(14);
        requestsSection.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), UiTheme.BORDER);
        requestsSection.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(20);
        rootLayout.addView(requestsSection, params);

        TextView title = new TextView(activity);
        title.setText("Underlying requests");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        requestsSection.addView(title);

        List<StaffReportingData.RequestSummary> requests = model.underlyingRequests;
        if (requests.isEmpty()) {
            TextView emptyTv = new TextView(activity);
            emptyTv.setText("No matching requests");
            emptyTv.setTextColor(UiTheme.MUTED);
            emptyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            emptyTv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams eParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            eParams.topMargin = dp(16);
            eParams.bottomMargin = dp(12);
            requestsSection.addView(emptyTv, eParams);
        } else {
            for (int i = 0; i < requests.size(); i++) {
                StaffReportingData.RequestSummary req = requests.get(i);
                View reqRow = createRequestRow(req);
                requestsSection.addView(reqRow);

                if (i < requests.size() - 1) {
                    View divider = new View(activity);
                    divider.setBackgroundColor(UiTheme.BORDER);
                    LinearLayout.LayoutParams dParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                    dParams.topMargin = dp(10);
                    dParams.bottomMargin = dp(10);
                    requestsSection.addView(divider, dParams);
                }
            }
        }
    }

    private View createRequestRow(StaffReportingData.RequestSummary req) {
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
        idTv.setText(req.id);
        idTv.setTextColor(UiTheme.ACCENT);
        idTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        idTv.setTypeface(Typeface.DEFAULT_BOLD);
        topLine.addView(idTv);

        View spacer = new View(activity);
        topLine.addView(spacer, new LinearLayout.LayoutParams(0, 0, 1.0f));

        TextView badge = new TextView(activity);
        badge.setText(req.status);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        int bPadH = dp(8);
        int bPadV = dp(3);
        badge.setPadding(bPadH, bPadV, bPadH, bPadV);

        GradientDrawable bBg = new GradientDrawable();
        bBg.setCornerRadius(dp(4));
        if ("Completed".equalsIgnoreCase(req.status)) {
            bBg.setColor(UiTheme.STATUS_COMPLETED_BG);
            badge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
        } else {
            bBg.setColor(UiTheme.STATUS_IN_REVIEW_BG);
            badge.setTextColor(UiTheme.STATUS_IN_REVIEW_TEXT);
        }
        badge.setBackground(bBg);
        topLine.addView(badge);

        TextView srvTv = new TextView(activity);
        srvTv.setText(req.serviceName);
        srvTv.setTextColor(UiTheme.NAVY);
        srvTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tParams.topMargin = dp(4);
        row.addView(srvTv, tParams);

        row.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onOpenRequest(req.id);
        });

        return row;
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
            if (callbacks != null) callbacks.onExport();
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
