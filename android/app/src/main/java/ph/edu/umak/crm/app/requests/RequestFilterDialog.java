package ph.edu.umak.crm.app.requests;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class RequestFilterDialog {

    public interface FilterListener {
        void onApplyFilters(String status, String fromDate, String toDate);
        void onClearFilters();
    }

    private final Context context;
    private final FilterListener listener;
    private String selectedStatus = "All";
    private final List<TextView> chipViews = new ArrayList<>();

    public RequestFilterDialog(Context context, FilterListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show(String currentStatus, String currentFrom, String currentTo) {
        if (currentStatus != null && !currentStatus.isEmpty()) {
            this.selectedStatus = currentStatus;
        }

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackground(UiTheme.shape(context, UiTheme.WHITE, 16, 0));
        int pad = UiTheme.dp(context, 20);
        layout.setPadding(pad, pad, pad, pad);

        // Header
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleCol = new LinearLayout(context);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView title = new TextView(context);
        title.setText("Filter Requests");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 18);
        titleCol.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Narrow your requests by status and date.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 13);
        titleCol.addView(subtitle);

        header.addView(titleCol, tLp);

        TextView closeBtn = new TextView(context);
        closeBtn.setText("Close");
        closeBtn.setTextColor(UiTheme.MUTED);
        closeBtn.setTypeface(bold);
        ScreenTypography.apply(closeBtn, 14);
        closeBtn.setPadding(UiTheme.dp(context, 8), UiTheme.dp(context, 4), UiTheme.dp(context, 8), UiTheme.dp(context, 4));
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        header.addView(closeBtn);

        layout.addView(header);

        addSpacing(context, layout, 16);

        // Status Label
        TextView statusLabel = new TextView(context);
        statusLabel.setText("Status");
        statusLabel.setTextColor(UiTheme.NAVY);
        statusLabel.setTypeface(bold);
        ScreenTypography.apply(statusLabel, 14);
        layout.addView(statusLabel);

        addSpacing(context, layout, 8);

        // Status Chips Container (2 rows or flow)
        LinearLayout chipRow1 = new LinearLayout(context);
        chipRow1.setOrientation(LinearLayout.HORIZONTAL);
        chipViews.clear();

        String[] statuses1 = {"All", "Submitted", "In review"};
        for (String s : statuses1) {
            TextView chip = createChip(s);
            chipRow1.addView(chip);
        }
        layout.addView(chipRow1);

        addSpacing(context, layout, 6);

        LinearLayout chipRow2 = new LinearLayout(context);
        chipRow2.setOrientation(LinearLayout.HORIZONTAL);
        String[] statuses2 = {"For correction", "Completed"};
        for (String s : statuses2) {
            TextView chip = createChip(s);
            chipRow2.addView(chip);
        }
        layout.addView(chipRow2);

        updateChipSelections();

        addSpacing(context, layout, 16);

        // Date Range Label
        TextView dateLabel = new TextView(context);
        dateLabel.setText("Date range");
        dateLabel.setTextColor(UiTheme.NAVY);
        dateLabel.setTypeface(bold);
        ScreenTypography.apply(dateLabel, 14);
        layout.addView(dateLabel);

        addSpacing(context, layout, 8);

        // From Date Input
        TextView fromLabel = new TextView(context);
        fromLabel.setText("From");
        fromLabel.setTextColor(UiTheme.MUTED);
        fromLabel.setTypeface(regular);
        ScreenTypography.apply(fromLabel, 12);
        layout.addView(fromLabel);

        EditText fromInput = createDateInput(context, currentFrom != null ? currentFrom : "2026-10-01");
        layout.addView(fromInput);

        addSpacing(context, layout, 8);

        // To Date Input
        TextView toLabel = new TextView(context);
        toLabel.setText("To");
        toLabel.setTextColor(UiTheme.MUTED);
        toLabel.setTypeface(regular);
        ScreenTypography.apply(toLabel, 12);
        layout.addView(toLabel);

        EditText toInput = createDateInput(context, currentTo != null ? currentTo : "2026-10-31");
        layout.addView(toInput);

        addSpacing(context, layout, 20);

        // Buttons: Apply Filters and Clear Filters
        Button applyBtn = UiButton.primary(context, "Apply Filters", () -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onApplyFilters(selectedStatus, fromInput.getText().toString().trim(), toInput.getText().toString().trim());
            }
        });
        layout.addView(applyBtn);

        addSpacing(context, layout, 8);

        Button clearBtn = UiButton.outline(context, "Clear filters", UiTheme.BORDER, UiTheme.NAVY, () -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onClearFilters();
            }
        });
        layout.addView(clearBtn);

        dialog.setContentView(layout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();
    }

    private TextView createChip(String text) {
        TextView chip = new TextView(context);
        chip.setText(text);
        chip.setTag(text);
        ScreenTypography.apply(chip, 13);
        int px = UiTheme.dp(context, 12);
        int py = UiTheme.dp(context, 6);
        chip.setPadding(px, py, px, py);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = UiTheme.dp(context, 8);
        chip.setLayoutParams(lp);

        chip.setOnClickListener(v -> {
            selectedStatus = text;
            updateChipSelections();
        });

        chipViews.add(chip);
        return chip;
    }

    private void updateChipSelections() {
        for (TextView chip : chipViews) {
            String text = (String) chip.getTag();
            boolean isSelected = text.equalsIgnoreCase(selectedStatus);
            if (isSelected) {
                chip.setBackground(UiTheme.shape(context, UiTheme.NAVY, 16, 0));
                chip.setTextColor(UiTheme.WHITE);
                chip.setTypeface(UiTheme.boldFont(context));
            } else {
                chip.setBackground(UiTheme.shape(context, UiTheme.WHITE, 16, UiTheme.BORDER));
                chip.setTextColor(UiTheme.NAVY);
                chip.setTypeface(UiTheme.bodyFont(context));
            }
        }
    }

    private static EditText createDateInput(Context context, String defaultValue) {
        EditText input = new EditText(context);
        input.setText(defaultValue);
        input.setTextColor(UiTheme.NAVY);
        input.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(input, 14);
        int pad = UiTheme.dp(context, 10);
        input.setPadding(pad, pad, pad, pad);
        input.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        input.setSingleLine(true);
        return input;
    }

    private static void addSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(space);
    }
}
