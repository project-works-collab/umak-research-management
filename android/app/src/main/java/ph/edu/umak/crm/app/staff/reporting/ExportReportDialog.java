package ph.edu.umak.crm.app.staff.reporting;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.dialogs.BottomSheet;
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

    private static final ExecutorService EXPORTS = Executors.newSingleThreadExecutor();
    private static final String XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final Context context;
    private final ReportExportData report;
    private final Handler main = new Handler(Looper.getMainLooper());
    private File exportedFile;
    private Dialog dialog;
    private LinearLayout container;
    private ExportState currentState = ExportState.FORMAT_PICKER;
    private String selectedFormat = "XLSX"; // Default selected format

    ExportReportDialog(Context context, ReportExportData report) {
        this.context = context;
        this.report = report;
    }

    public static void showLiveReport(Context context, org.json.JSONObject report) {
        try {
            new ExportReportDialog(context, ReportExportData.fromLive(report)).show(ExportState.FORMAT_PICKER);
        } catch (org.json.JSONException invalid) {
            Toast.makeText(context, "Unable to export. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void showDirectory(Context context,
                                     java.util.List<ph.edu.umak.crm.app.staff.registry.StaffRegistryData.RegistryRecordItem> records,
                                     String college, String period) {
        new ExportReportDialog(context, ReportExportData.directory(records, college, period))
                .show(ExportState.FORMAT_PICKER);
    }

    public void show(ExportState initialMode) {
        boolean generate = initialMode == ExportState.PENDING || initialMode == ExportState.SUCCESS;
        currentState = generate ? ExportState.PENDING : initialMode;

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        container.setPadding(pad, pad, pad, pad);

        renderCurrentState();

        dialog = new Dialog(context);
        BottomSheet.show(dialog, container);
        if (generate) startExport();
    }

    private void renderCurrentState() {
        container.removeAllViews();

        // 1. Title
        TextView title = new TextView(context);
        title.setText("Export report");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(UiTheme.headingFont(context));
        container.addView(title);

        // 2. Subtitle 1
        TextView sub1 = new TextView(context);
        sub1.setText("research-directory".equals(report.type)
                ? "Exports the research records in the current directory results."
                : "Uses the same filters and metric definitions as this report.");
        sub1.setTextColor(UiTheme.MUTED);
        sub1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams sub1Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sub1Params.topMargin = dp(4);
        container.addView(sub1, sub1Params);

        if (report.timestamp != null) {
            TextView timestamp = new TextView(context);
            timestamp.setText(report.timestamp);
            timestamp.setTextColor(UiTheme.MUTED);
            timestamp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            container.addView(timestamp);
        }
        sub1Params.bottomMargin = dp(8);

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
        pdfBtn.setEnabled(currentState == ExportState.FORMAT_PICKER);
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
        xlsxBtn.setEnabled(currentState == ExportState.FORMAT_PICKER);
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
        label.setText("Preparing export …");
        label.setTextColor(UiTheme.NAVY);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        progressContainer.addView(label);

        ProgressBar bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        bar.setIndeterminate(true);
        bar.setIndeterminateTintList(ColorStateList.valueOf(UiTheme.ACCENT));
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
        fileName.setText(exportedFile.getName());
        fileName.setTextColor(UiTheme.NAVY);
        fileName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        fileName.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams fParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fParams.topMargin = dp(4);
        successCard.addView(fileName, fParams);

        TextView fileSize = new TextView(context);
        fileSize.setText(Formatter.formatShortFileSize(context, exportedFile.length()));
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
        switch (currentState) {
            case FORMAT_PICKER:
                container.addView(UiButton.primary(context, "Export", this::startExport));
                break;
            case PENDING:
                Button pending = UiButton.primary(context, "Exporting …", null);
                pending.setEnabled(false);
                pending.setAlpha(0.6f);
                container.addView(pending);
                break;
            case SUCCESS:
                LinearLayout actions = new LinearLayout(context);
                actions.setOrientation(LinearLayout.HORIZONTAL);
                Button open = UiButton.primary(context, "Open", () -> openOrShare(false));
                actions.addView(open, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                Button share = UiButton.outline(context, "Share", UiTheme.NAVY, UiTheme.NAVY,
                        () -> openOrShare(true));
                LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                shareParams.setMarginStart(dp(8));
                actions.addView(share, shareParams);
                container.addView(actions);
                break;
            case FAILURE:
                container.addView(UiButton.primary(context, "Retry", this::startExport));
                break;
        }
    }

    private void startExport() {
        currentState = ExportState.PENDING;
        renderCurrentState();
        boolean pdf = "PDF".equals(selectedFormat);
        File cache = context.getCacheDir();
        EXPORTS.execute(() -> {
            File result = null;
            try {
                result = ReportExportWriter.write(cache, report, pdf);
            } catch (IOException | RuntimeException failure) {
                Log.w("ReportExport", "Could not generate " + report.type, failure);
            }
            File file = result;
            main.post(() -> {
                if (!dialog.isShowing()) {
                    if (file != null) {
                        file.delete();
                        file.getParentFile().delete();
                    }
                    return;
                }
                exportedFile = file;
                currentState = file == null ? ExportState.FAILURE : ExportState.SUCCESS;
                renderCurrentState();
            });
        });
    }

    private void openOrShare(boolean share) {
        if (!exportedFile.isFile()) {
            currentState = ExportState.FAILURE;
            renderCurrentState();
            return;
        }
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".report-exports", exportedFile);
        String mimeType = exportedFile.getName().endsWith(".pdf") ? "application/pdf" : XLSX_MIME;
        Intent intent = new Intent(share ? Intent.ACTION_SEND : Intent.ACTION_VIEW);
        if (share) {
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        } else {
            intent.setDataAndType(uri, mimeType);
        }
        intent.setClipData(ClipData.newRawUri(exportedFile.getName(), uri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(share ? Intent.createChooser(intent, "Share report") : intent);
        } catch (ActivityNotFoundException noViewer) {
            Toast.makeText(context, "No app can open this format. You can share the report instead.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }
}
