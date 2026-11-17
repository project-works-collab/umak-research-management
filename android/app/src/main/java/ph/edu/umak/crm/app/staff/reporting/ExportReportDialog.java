package ph.edu.umak.crm.app.staff.reporting;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Export Report Dialog (Wireframes 9.4a, 9.4b, 9.4c, 9.4d).
 * Zero emojis compliant.
 */
public class ExportReportDialog {

    public enum ExportState {
        FORMAT_PICKER, // 9.4a
        PENDING,       // 9.4b
        SUCCESS,       // 9.4c
        FAILURE        // 9.4d
    }

    private final Context context;
    private AlertDialog dialog;
    private LinearLayout container;
    private ExportState currentState = ExportState.FORMAT_PICKER;
    private String selectedFormat = "XLSX"; // Default selected format

    public ExportReportDialog(Context context) {
        this.context = context;
    }

    public void show() {
        show(ExportState.FORMAT_PICKER);
    }

    public void show(ExportState initialMode) {
        this.currentState = initialMode;

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(UiTheme.WHITE);
        int pad = dp(20);
        container.setPadding(pad, pad, pad, pad);

        renderCurrentState();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(container);
        dialog = builder.create();
        if (dialog.getWindow() != null) {
            GradientDrawable winBg = new GradientDrawable();
            winBg.setColor(UiTheme.WHITE);
            winBg.setCornerRadius(dp(12));
            dialog.getWindow().setBackgroundDrawable(winBg);
        }
        dialog.show();
    }

    private void renderCurrentState() {
        container.removeAllViews();

        // 1. Title
        TextView title = new TextView(context);
        title.setText("Export report");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        container.addView(title);

        // 2. Subtitle 1
        TextView sub1 = new TextView(context);
        sub1.setText("Uses the same filters and metric definitions as this report.");
        sub1.setTextColor(UiTheme.MUTED);
        sub1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams sub1Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sub1Params.topMargin = dp(4);
        container.addView(sub1, sub1Params);

        // 3. Subtitle 2
        TextView sub2 = new TextView(context);
        sub2.setText("Generated Nov 14, 2026, 10:30 AM.");
        sub2.setTextColor(UiTheme.MUTED);
        sub2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams sub2Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sub2Params.topMargin = dp(2);
        sub2Params.bottomMargin = dp(14);
        container.addView(sub2, sub2Params);

        // 4. Middle content based on current state
        switch (currentState) {
            case FORMAT_PICKER:
                renderFormatPicker();
                break;
            case PENDING:
                renderPending();
                break;
            case SUCCESS:
                renderSuccess();
                break;
            case FAILURE:
                renderFailure();
                break;
        }

        // 5. Privacy Notice
        TextView privacy = new TextView(context);
        privacy.setText("Exports exclude personal contact information, attachment contents and internal remarks.");
        privacy.setTextColor(UiTheme.MUTED);
        privacy.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        privacy.setLineSpacing(dp(2), 1.0f);
        LinearLayout.LayoutParams privParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        privParams.topMargin = dp(14);
        privParams.bottomMargin = dp(16);
        container.addView(privacy, privParams);

        // 6. Action buttons
        renderActionButtons();
    }

    private void renderFormatPicker() {
        TextView formatLabel = new TextView(context);
        formatLabel.setText("Format");
        formatLabel.setTextColor(UiTheme.NAVY);
        formatLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        formatLabel.setTypeface(Typeface.DEFAULT_BOLD);
        container.addView(formatLabel);

        LinearLayout formatRow = new LinearLayout(context);
        formatRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(8);
        container.addView(formatRow, rowParams);

        // PDF Option
        Button pdfBtn = new Button(context);
        pdfBtn.setText("PDF");
        pdfBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        boolean isPdf = "PDF".equals(selectedFormat);
        styleFormatButton(pdfBtn, isPdf);
        pdfBtn.setOnClickListener(v -> {
            selectedFormat = "PDF";
            renderCurrentState();
        });
        LinearLayout.LayoutParams pdfParams = new LinearLayout.LayoutParams(0, dp(40), 1.0f);
        pdfParams.rightMargin = dp(8);
        formatRow.addView(pdfBtn, pdfParams);

        // XLSX Option (default checked)
        Button xlsxBtn = new Button(context);
        xlsxBtn.setText(isPdf ? "XLSX" : "[X] XLSX");
        xlsxBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        boolean isXlsx = "XLSX".equals(selectedFormat);
        styleFormatButton(xlsxBtn, isXlsx);
        xlsxBtn.setOnClickListener(v -> {
            selectedFormat = "XLSX";
            renderCurrentState();
        });
        LinearLayout.LayoutParams xlsxParams = new LinearLayout.LayoutParams(0, dp(40), 1.0f);
        formatRow.addView(xlsxBtn, xlsxParams);
    }

    private void styleFormatButton(Button btn, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(6));
        if (selected) {
            bg.setColor(UiTheme.PALE);
            bg.setStroke(dp(1.5f), UiTheme.ACCENT);
            btn.setTextColor(UiTheme.NAVY);
            btn.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            bg.setColor(UiTheme.WHITE);
            bg.setStroke(dp(1), UiTheme.BORDER);
            btn.setTextColor(UiTheme.MUTED);
            btn.setTypeface(Typeface.DEFAULT);
        }
        btn.setBackground(bg);
    }

    private void renderPending() {
        // Show format tags
        renderFormatPicker();

        LinearLayout progressContainer = new LinearLayout(context);
        progressContainer.setOrientation(LinearLayout.VERTICAL);
        progressContainer.setBackgroundColor(UiTheme.PAPER);
        int pPad = dp(12);
        progressContainer.setPadding(pPad, pPad, pPad, pPad);
        GradientDrawable pBg = new GradientDrawable();
        pBg.setColor(UiTheme.PAPER);
        pBg.setCornerRadius(dp(8));
        pBg.setStroke(dp(1), UiTheme.BORDER);
        progressContainer.setBackground(pBg);

        LinearLayout.LayoutParams pParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pParams.topMargin = dp(12);
        container.addView(progressContainer, pParams);

        TextView label = new TextView(context);
        label.setText("Preparing export … 60%");
        label.setTextColor(UiTheme.NAVY);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        progressContainer.addView(label);

        ProgressBar bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        bar.setProgress(60);
        bar.setMax(100);
        bar.setProgressTintList(ColorStateList.valueOf(UiTheme.ACCENT));
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
        barParams.topMargin = dp(8);
        progressContainer.addView(bar, barParams);
    }

    private void renderSuccess() {
        LinearLayout successCard = new LinearLayout(context);
        successCard.setOrientation(LinearLayout.VERTICAL);
        int cPad = dp(14);
        successCard.setPadding(cPad, cPad, cPad, cPad);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(UiTheme.SUCCESS_BG);
        cardBg.setCornerRadius(dp(8));
        cardBg.setStroke(dp(1), UiTheme.SUCCESS_GREEN);
        successCard.setBackground(cardBg);

        LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cParams.topMargin = dp(4);
        container.addView(successCard, cParams);

        TextView status = new TextView(context);
        status.setText("Export ready.");
        status.setTextColor(UiTheme.SUCCESS_GREEN);
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        status.setTypeface(Typeface.DEFAULT_BOLD);
        successCard.addView(status);

        TextView fileName = new TextView(context);
        fileName.setText("research-report_2026-11-14_1030." + selectedFormat.toLowerCase());
        fileName.setTextColor(UiTheme.NAVY);
        fileName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        fileName.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams fParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fParams.topMargin = dp(4);
        successCard.addView(fileName, fParams);

        TextView fileSize = new TextView(context);
        fileSize.setText("48 KB");
        fileSize.setTextColor(UiTheme.MUTED);
        fileSize.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams sParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sParams.topMargin = dp(2);
        successCard.addView(fileSize, sParams);
    }

    private void renderFailure() {
        LinearLayout failCard = new LinearLayout(context);
        failCard.setOrientation(LinearLayout.VERTICAL);
        int cPad = dp(14);
        failCard.setPadding(cPad, cPad, cPad, cPad);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(UiTheme.PEACH_BG);
        cardBg.setCornerRadius(dp(8));
        cardBg.setStroke(dp(1), UiTheme.PEACH_BORDER);
        failCard.setBackground(cardBg);

        LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cParams.topMargin = dp(4);
        container.addView(failCard, cParams);

        TextView error = new TextView(context);
        error.setText("Unable to export. Try again.");
        error.setTextColor(UiTheme.PEACH_TEXT);
        error.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        error.setTypeface(Typeface.DEFAULT_BOLD);
        failCard.addView(error);
    }

    private void renderActionButtons() {
        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        container.addView(btnRow);

        switch (currentState) {
            case FORMAT_PICKER:
                Button cancelBtn = createButton("Cancel", false);
                cancelBtn.setOnClickListener(v -> dialog.dismiss());
                btnRow.addView(cancelBtn);

                Button exportBtn = createButton("Export", true);
                exportBtn.setOnClickListener(v -> {
                    currentState = ExportState.PENDING;
                    renderCurrentState();
                });
                LinearLayout.LayoutParams expParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
                expParams.leftMargin = dp(8);
                btnRow.addView(exportBtn, expParams);
                break;

            case PENDING:
                Button exportingBtn = createButton("Exporting …", true);
                exportingBtn.setEnabled(false);
                exportingBtn.setAlpha(0.6f);
                btnRow.addView(exportingBtn);
                break;

            case SUCCESS:
                Button closeBtn = createButton("Close", false);
                closeBtn.setOnClickListener(v -> dialog.dismiss());
                btnRow.addView(closeBtn);

                Button shareBtn = createButton("Share", false);
                shareBtn.setOnClickListener(v -> {
                    Toast.makeText(context, "Sharing export file...", Toast.LENGTH_SHORT).show();
                });
                LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
                shareParams.leftMargin = dp(8);
                btnRow.addView(shareBtn, shareParams);

                Button openBtn = createButton("Open", true);
                openBtn.setOnClickListener(v -> {
                    Toast.makeText(context, "Opening report...", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
                LinearLayout.LayoutParams openParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
                openParams.leftMargin = dp(8);
                btnRow.addView(openBtn, openParams);
                break;

            case FAILURE:
                Button failCancelBtn = createButton("Cancel", false);
                failCancelBtn.setOnClickListener(v -> dialog.dismiss());
                btnRow.addView(failCancelBtn);

                Button retryBtn = createButton("Retry", true);
                retryBtn.setOnClickListener(v -> {
                    currentState = ExportState.PENDING;
                    renderCurrentState();
                });
                LinearLayout.LayoutParams retParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
                retParams.leftMargin = dp(8);
                btnRow.addView(retryBtn, retParams);
                break;
        }
    }

    private Button createButton(String label, boolean isPrimary) {
        Button btn = new Button(context);
        btn.setText(label);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        int padH = dp(16);
        btn.setPadding(padH, 0, padH, 0);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(6));
        if (isPrimary) {
            bg.setColor(UiTheme.NAVY);
            btn.setTextColor(UiTheme.WHITE);
            btn.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            bg.setColor(UiTheme.WHITE);
            bg.setStroke(dp(1), UiTheme.BORDER);
            btn.setTextColor(UiTheme.NAVY);
            btn.setTypeface(Typeface.DEFAULT);
        }
        btn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22000000), bg, null));
        return btn;
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }
}
