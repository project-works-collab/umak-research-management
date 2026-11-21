package ph.edu.umak.crm.app.staff.queue;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.common.dialogs.BottomSheet;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * 6.2 Filter Sheet Dialog for Staff Review Queue
 */
public final class QueueFilterDialog {

    public interface FilterCallback {
        void onApply(String service, String status, String fromDate, String toDate);
        void onClear();
    }

    private QueueFilterDialog() {}

    public static void show(Context context, String currentService, String currentStatus,
                            String currentFrom, String currentTo, FilterCallback callback) {
        show(context, currentService, currentStatus, currentFrom, currentTo,
                java.util.List.of("Ethics Clearance", "Research Consultation", "Research Registration",
                        "CRM Clearance Processing", "Statistical Report Generation", "UNIVERSITY Journal Endorsement"), callback);
    }

    public static void show(Context context, String currentService, String currentStatus,
                            String currentFrom, String currentTo, List<String> availableServices, FilterCallback callback) {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        ScrollView scrollView = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        int pad = UiTheme.dp(context, 20);
        layout.setPadding(pad, UiTheme.dp(context, 12), pad, pad);
        scrollView.addView(layout);

        View handle = new View(context);
        handle.setBackground(UiTheme.shape(context, 0xffcbd5e1, 2, 0));
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(UiTheme.dp(context, 36), UiTheme.dp(context, 4));
        hLp.gravity = Gravity.CENTER_HORIZONTAL;
        hLp.bottomMargin = UiTheme.dp(context, 14);
        handle.setLayoutParams(hLp);
        layout.addView(handle);

        // Header Title
        TextView title = new TextView(context);
        title.setText("Filter queue");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 20);
        title.setPadding(0, 0, 0, UiTheme.dp(context, 16));
        layout.addView(title);

        // Service Dropdown
        TextView serviceLabel = new TextView(context);
        serviceLabel.setText("Service");
        serviceLabel.setTextColor(UiTheme.NAVY);
        serviceLabel.setTypeface(bold);
        ScreenTypography.apply(serviceLabel, 13);
        serviceLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        layout.addView(serviceLabel);

        Spinner serviceSpinner = new Spinner(context);
        List<String> services = new ArrayList<>();
        services.add("All Services");
        for (String service : availableServices) if (!services.contains(service)) services.add(service);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, services);
        serviceSpinner.setAdapter(adapter);

        if (currentService != null) {
            int idx = services.indexOf(currentService);
            if (idx >= 0) serviceSpinner.setSelection(idx);
        }

        GradientDrawable spinnerBg = new GradientDrawable();
        spinnerBg.setColor(UiTheme.WHITE);
        spinnerBg.setCornerRadius(UiTheme.dp(context, 8));
        spinnerBg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        serviceSpinner.setBackground(spinnerBg);
        serviceSpinner.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 12),
                UiTheme.dp(context, 12), UiTheme.dp(context, 12));
        layout.addView(serviceSpinner);

        addSpacing(context, layout, 16);

        // Status Selector
        TextView statusLabel = new TextView(context);
        statusLabel.setText("Status");
        statusLabel.setTextColor(UiTheme.NAVY);
        statusLabel.setTypeface(bold);
        ScreenTypography.apply(statusLabel, 13);
        statusLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        layout.addView(statusLabel);

        HorizontalScrollView statusScroll = new HorizontalScrollView(context);
        statusScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout statusRow = new LinearLayout(context);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);

        String[] statusOptions = {"All", "Submitted", "In review", "For correction", "Completed"};
        final String[] selectedStatus = {currentStatus != null ? currentStatus : "All"};
        final List<TextView> statusChips = new ArrayList<>();

        for (String opt : statusOptions) {
            TextView chip = new TextView(context);
            chip.setText(opt);
            chip.setTypeface(bold);
            ScreenTypography.apply(chip, 12);
            chip.setPadding(UiTheme.dp(context, 14), UiTheme.dp(context, 8),
                    UiTheme.dp(context, 14), UiTheme.dp(context, 8));
            updateChipAppearance(context, chip, opt.equalsIgnoreCase(selectedStatus[0]));

            chip.setOnClickListener(v -> {
                selectedStatus[0] = opt;
                for (int i = 0; i < statusOptions.length; i++) {
                    updateChipAppearance(context, statusChips.get(i), statusOptions[i].equalsIgnoreCase(selectedStatus[0]));
                }
            });

            statusChips.add(chip);
            statusRow.addView(chip);

            View s = new View(context);
            s.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 8), 1));
            statusRow.addView(s);
        }
        statusScroll.addView(statusRow);
        layout.addView(statusScroll);

        addSpacing(context, layout, 16);

        // Date Range
        TextView dateLabel = new TextView(context);
        dateLabel.setText("Date");
        dateLabel.setTextColor(UiTheme.NAVY);
        dateLabel.setTypeface(bold);
        ScreenTypography.apply(dateLabel, 13);
        dateLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        layout.addView(dateLabel);

        LinearLayout dateRow = new LinearLayout(context);
        dateRow.setOrientation(LinearLayout.HORIZONTAL);

        // From Date
        LinearLayout fromCol = new LinearLayout(context);
        fromCol.setOrientation(LinearLayout.VERTICAL);
        fromCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView fromLbl = new TextView(context);
        fromLbl.setText("From");
        fromLbl.setTextColor(UiTheme.MUTED);
        fromLbl.setTypeface(regular);
        ScreenTypography.apply(fromLbl, 12);
        fromCol.addView(fromLbl);

        EditText fromInput = new EditText(context);
        fromInput.setText(currentFrom != null ? currentFrom : "");
        fromInput.setHint("YYYY-MM-DD");
        fromInput.setContentDescription("Submitted from date");
        fromInput.setTextColor(UiTheme.NAVY);
        fromInput.setTypeface(regular);
        ScreenTypography.apply(fromInput, 14);
        fromInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 10),
                UiTheme.dp(context, 10), UiTheme.dp(context, 10));
        fromInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        fromCol.addView(fromInput);
        dateRow.addView(fromCol);

        View dSpacer = new View(context);
        dSpacer.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 12), 1));
        dateRow.addView(dSpacer);

        // To Date
        LinearLayout toCol = new LinearLayout(context);
        toCol.setOrientation(LinearLayout.VERTICAL);
        toCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView toLbl = new TextView(context);
        toLbl.setText("To");
        toLbl.setTextColor(UiTheme.MUTED);
        toLbl.setTypeface(regular);
        ScreenTypography.apply(toLbl, 12);
        toCol.addView(toLbl);

        EditText toInput = new EditText(context);
        toInput.setText(currentTo != null ? currentTo : "");
        toInput.setHint("YYYY-MM-DD");
        toInput.setContentDescription("Submitted to date");
        toInput.setTextColor(UiTheme.NAVY);
        toInput.setTypeface(regular);
        ScreenTypography.apply(toInput, 14);
        toInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 10),
                UiTheme.dp(context, 10), UiTheme.dp(context, 10));
        toInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        toCol.addView(toInput);
        dateRow.addView(toCol);

        layout.addView(dateRow);

        addSpacing(context, layout, 24);

        Dialog dialog = new Dialog(context);

        // Action Buttons
        Button applyBtn = UiButton.primary(context, "Apply Filters", () -> {
            String from = fromInput.getText().toString().trim(), to = toInput.getText().toString().trim();
            try {
                if (!from.isEmpty()) java.time.LocalDate.parse(from);
                if (!to.isEmpty()) java.time.LocalDate.parse(to);
            } catch (java.time.DateTimeException invalid) {
                fromInput.setError("Use dates in YYYY-MM-DD format.");
                return;
            }
            if (!from.isEmpty() && !to.isEmpty() && from.compareTo(to) > 0) {
                toInput.setError("End date must be on or after the start date.");
                return;
            }
            dialog.dismiss();
            if (callback != null) {
                String selSvc = serviceSpinner.getSelectedItem() != null ? serviceSpinner.getSelectedItem().toString() : "All Services";
                callback.onApply(selSvc, selectedStatus[0], from, to);
            }
        });
        layout.addView(applyBtn);

        addSpacing(context, layout, 10);

        Button clearBtn = UiButton.outline(context, "Clear filters", UiTheme.BORDER, UiTheme.NAVY, () -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onClear();
            }
        });
        layout.addView(clearBtn);

        BottomSheet.show(dialog, scrollView);
    }

    private static void updateChipAppearance(Context context, TextView chip, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(UiTheme.dp(context, 16));
        if (selected) {
            bg.setColor(UiTheme.NAVY);
            chip.setTextColor(UiTheme.WHITE);
        } else {
            bg.setColor(UiTheme.WHITE);
            bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
            chip.setTextColor(UiTheme.NAVY);
        }
        chip.setBackground(bg);
    }

    private static void addSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(space);
    }
}
