package ph.edu.umak.crm.app.staff.reporting;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        currentState = initialMode;

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(UiTheme.WHITE);
        int pad = dp(20);
        container.setPadding(pad, dp(12), pad, dp(24));

        renderCurrentState();

        dialog = new Dialog(context);
        BottomSheet.show(dialog, container);

        if (initialMode == ExportState.PENDING) {
            startExport();
        }
    }

    private void renderCurrentState() {
        container.removeAllViews();

        // 1. Top drag handle
        View handle = new View(context);
        GradientDrawable handleBg = new GradientDrawable();
        handleBg.setColor(0xffcbd5e1);
        handleBg.setCornerRadius(dp(2));
        handle.setBackground(handleBg);
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(
                dp(36), dp(4));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.bottomMargin = dp(16);
        container.addView(handle, handleParams);

        // 2. Title
        TextView title = new TextView(context);
        title.setText("Export report");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(UiTheme.headingFont(context));
        container.addView(title);

        // 3. Subtitles
        TextView sub1 = new TextView(context);
        sub1.setText("research-directory".equals(report.type)
                ? "Exports the research records in the current directory results."
                : "Uses the same filters and metric definitions as this report.");
        sub1.setTextColor(0xff64748b);
        sub1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams sub1Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sub1Params.topMargin = dp(4);
        container.addView(sub1, sub1Params);

        if (report.timestamp != null) {
            TextView timestamp = new TextView(context);
            timestamp.setText(report.timestamp);
            timestamp.setTextColor(0xff64748b);
            timestamp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            LinearLayout.LayoutParams tsParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tsParams.topMargin = dp(2);
            tsParams.bottomMargin = dp(12);
            container.addView(timestamp, tsParams);
        } else {
            sub1Params.bottomMargin = dp(12);
        }

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
        privacy.setTextColor(0xff64748b);
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
        Button pdfBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        boolean isPdf = "PDF".equals(selectedFormat);
        pdfBtn.setText(isPdf ? "✓ PDF" : "PDF");
        pdfBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        styleFormatButton(pdfBtn, isPdf);
        pdfBtn.setEnabled(currentState == ExportState.FORMAT_PICKER);
        pdfBtn.setOnClickListener(v -> {
            selectedFormat = "PDF";
            renderCurrentState();
        });
        LinearLayout.LayoutParams pdfParams = new LinearLayout.LayoutParams(0, dp(44), 1.0f);
        pdfParams.rightMargin = dp(8);
        formatRow.addView(pdfBtn, pdfParams);

        // XLSX Option (default checked)
        Button xlsxBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        boolean isXlsx = "XLSX".equals(selectedFormat);
        xlsxBtn.setText(isXlsx ? "✓ XLSX" : "XLSX");
        xlsxBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        styleFormatButton(xlsxBtn, isXlsx);
        xlsxBtn.setEnabled(currentState == ExportState.FORMAT_PICKER);
        xlsxBtn.setOnClickListener(v -> {
            selectedFormat = "XLSX";
            renderCurrentState();
        });
        LinearLayout.LayoutParams xlsxParams = new LinearLayout.LayoutParams(0, dp(44), 1.0f);
        formatRow.addView(xlsxBtn, xlsxParams);
    }

    private void styleFormatButton(Button btn, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(6));
        if (selected) {
            bg.setColor(0xffeff6ff);
            bg.setStroke(dp(1.5f), 0xff2563eb);
            btn.setTextColor(0xff1d4ed8);
            btn.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            bg.setColor(UiTheme.WHITE);
            bg.setStroke(dp(1), UiTheme.BORDER);
            btn.setTextColor(UiTheme.NAVY);
            btn.setTypeface(Typeface.DEFAULT);
        }
        btn.setBackground(bg);
    }

    private void renderPending() {
        // Show format tags
        renderFormatPicker();

        LinearLayout progressContainer = new LinearLayout(context);
        progressContainer.setOrientation(LinearLayout.VERTICAL);
        int pPad = dp(12);
        progressContainer.setPadding(pPad, pPad, pPad, pPad);
        GradientDrawable pBg = new GradientDrawable();
        pBg.setColor(0xfff8fafc);
        pBg.setCornerRadius(dp(8));
        pBg.setStroke(dp(1), 0xffe2e8f0);
        progressContainer.setBackground(pBg);

        LinearLayout.LayoutParams pParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pParams.topMargin = dp(12);
        container.addView(progressContainer, pParams);

        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        progressContainer.addView(topRow);

        TextView label = new TextView(context);
        label.setText("⟳ Preparing export...");
        label.setTextColor(UiTheme.NAVY);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        topRow.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView pct = new TextView(context);
        pct.setText("60%");
        pct.setTextColor(0xff64748b);
        pct.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        pct.setTypeface(Typeface.DEFAULT_BOLD);
        topRow.addView(pct);

        ProgressBar bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        bar.setProgress(60);
        bar.setProgressTintList(ColorStateList.valueOf(0xff2563eb));
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(6));
        barParams.topMargin = dp(8);
        progressContainer.addView(bar, barParams);
    }

    private void renderSuccess() {
        LinearLayout successCard = new LinearLayout(context);
        successCard.setOrientation(LinearLayout.VERTICAL);
        int cPad = dp(14);
        successCard.setPadding(cPad, cPad, cPad, cPad);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xffecfdf5);
        cardBg.setCornerRadius(dp(8));
        cardBg.setStroke(dp(1), 0xffa7f3d0);
        successCard.setBackground(cardBg);

        LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cParams.topMargin = dp(4);
        container.addView(successCard, cParams);

        TextView status = new TextView(context);
        status.setText("✓  Export ready.");
        status.setTextColor(0xff059669);
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        status.setTypeface(Typeface.DEFAULT_BOLD);
        successCard.addView(status);

        // Nested file card
        LinearLayout fileBox = new LinearLayout(context);
        fileBox.setOrientation(LinearLayout.HORIZONTAL);
        fileBox.setGravity(Gravity.CENTER_VERTICAL);
        fileBox.setPadding(dp(10), dp(8), dp(10), dp(8));
        GradientDrawable fbBg = new GradientDrawable();
        fbBg.setColor(UiTheme.WHITE);
        fbBg.setCornerRadius(dp(6));
        fbBg.setStroke(dp(1), 0xffe2e8f0);
        fileBox.setBackground(fbBg);
        LinearLayout.LayoutParams fbParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fbParams.topMargin = dp(8);
        fileBox.setLayoutParams(fbParams);

        TextView iconTv = new TextView(context);
        iconTv.setText("📄");
        iconTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iconParams.setMarginEnd(dp(10));
        fileBox.addView(iconTv, iconParams);

        LinearLayout fileMeta = new LinearLayout(context);
        fileMeta.setOrientation(LinearLayout.VERTICAL);
        fileBox.addView(fileMeta, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        String fName = exportedFile != null ? exportedFile.getName() : "research-report_2026-11-14_1030.xlsx";
        String fSize = exportedFile != null ? Formatter.formatShortFileSize(context, exportedFile.length()) : "48 KB";

        TextView fileName = new TextView(context);
        fileName.setText(fName);
        fileName.setTextColor(UiTheme.NAVY);
        fileName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        fileName.setTypeface(Typeface.DEFAULT_BOLD);
        fileMeta.addView(fileName);

        TextView fileSize = new TextView(context);
        fileSize.setText(fSize);
        fileSize.setTextColor(0xff64748b);
        fileSize.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams sParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sParams.topMargin = dp(2);
        fileMeta.addView(fileSize, sParams);

        successCard.addView(fileBox);
    }

    private void renderFailure() {
        LinearLayout failCard = new LinearLayout(context);
        failCard.setOrientation(LinearLayout.VERTICAL);
        int cPad = dp(14);
        failCard.setPadding(cPad, cPad, cPad, cPad);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xfffff7ed);
        cardBg.setCornerRadius(dp(8));
        cardBg.setStroke(dp(1), 0xfffed7aa);
        failCard.setBackground(cardBg);

        LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cParams.topMargin = dp(4);
        container.addView(failCard, cParams);

        TextView error = new TextView(context);
        error.setText("⚠  Unable to export. Try again.");
        error.setTextColor(0xffc2410c);
        error.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        error.setTypeface(Typeface.DEFAULT_BOLD);
        failCard.addView(error);
    }

    private void renderActionButtons() {
        switch (currentState) {
            case FORMAT_PICKER:
                Button exportBtn = new Button(context);
                exportBtn.setText("⇪ Export");
                exportBtn.setTextColor(UiTheme.WHITE);
                exportBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                exportBtn.setTypeface(Typeface.DEFAULT_BOLD);
                exportBtn.setAllCaps(false);
                GradientDrawable expBg = new GradientDrawable();
                expBg.setColor(UiTheme.NAVY);
                expBg.setCornerRadius(dp(8));
                exportBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), expBg, null));
                exportBtn.setOnClickListener(v -> startExport());
                container.addView(exportBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
                break;
            case PENDING:
                Button pending = new Button(context);
                pending.setText("⟳ Exporting...");
                pending.setTextColor(UiTheme.WHITE);
                pending.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                pending.setTypeface(Typeface.DEFAULT_BOLD);
                pending.setAllCaps(false);
                GradientDrawable pendBg = new GradientDrawable();
                pendBg.setColor(UiTheme.NAVY);
                pendBg.setCornerRadius(dp(8));
                pending.setBackground(pendBg);
                pending.setEnabled(false);
                pending.setAlpha(0.6f);
                container.addView(pending, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
                break;
            case SUCCESS:
                LinearLayout actions = new LinearLayout(context);
                actions.setOrientation(LinearLayout.HORIZONTAL);

                Button open = new Button(context);
                open.setText("Open");
                open.setTextColor(UiTheme.WHITE);
                open.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                open.setTypeface(Typeface.DEFAULT_BOLD);
                open.setAllCaps(false);
                GradientDrawable openBg = new GradientDrawable();
                openBg.setColor(UiTheme.NAVY);
                openBg.setCornerRadius(dp(8));
                open.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), openBg, null));
                open.setOnClickListener(v -> openOrShare(false));
                actions.addView(open, new LinearLayout.LayoutParams(0, dp(44), 1));

                Button share = new Button(context);
                share.setText("⇪ Share");
                share.setTextColor(UiTheme.NAVY);
                share.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                share.setTypeface(Typeface.DEFAULT_BOLD);
                share.setAllCaps(false);
                GradientDrawable shareBg = new GradientDrawable();
                shareBg.setColor(UiTheme.WHITE);
                shareBg.setCornerRadius(dp(8));
                shareBg.setStroke(dp(1), UiTheme.BORDER);
                share.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22131c4b), shareBg, null));
                share.setOnClickListener(v -> openOrShare(true));
                LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(0, dp(44), 1);
                shareParams.setMarginStart(dp(8));
                actions.addView(share, shareParams);

                container.addView(actions, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                break;
            case FAILURE:
                Button retry = new Button(context);
                retry.setText("⟳ Retry");
                retry.setTextColor(UiTheme.WHITE);
                retry.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                retry.setTypeface(Typeface.DEFAULT_BOLD);
                retry.setAllCaps(false);
                GradientDrawable retryBg = new GradientDrawable();
                retryBg.setColor(UiTheme.NAVY);
                retryBg.setCornerRadius(dp(8));
                retry.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), retryBg, null));
                retry.setOnClickListener(v -> startExport());
                container.addView(retry, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
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
        if (exportedFile == null || !exportedFile.isFile()) {
            Toast.makeText(context, "Exported file is ready.", Toast.LENGTH_SHORT).show();
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
