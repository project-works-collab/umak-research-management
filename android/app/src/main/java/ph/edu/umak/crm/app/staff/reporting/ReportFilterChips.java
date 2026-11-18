package ph.edu.umak.crm.app.staff.reporting;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

final class ReportFilterChips {
    private ReportFilterChips() {}

    static void addRow(LinearLayout parent, String... labels) {
        Context context = parent.getContext();
        LinearLayout row = new LinearLayout(context);
        row.setBaselineAligned(false);
        for (String label : labels) {
            if (label == null || label.isEmpty()) continue;

            TextView chip = new TextView(context);
            chip.setText(label);
            chip.setTextColor(UiTheme.ACCENT);
            chip.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(chip, 12);
            chip.setBackground(UiTheme.shape(context, UiTheme.BADGE_BLUE_BG, 12, 0));
            int horizontal = UiTheme.dp(context, 8);
            int vertical = UiTheme.dp(context, 3);
            chip.setPadding(horizontal, vertical, horizontal, vertical);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMarginEnd(UiTheme.dp(context, 4));
            row.addView(chip, params);
        }
        if (row.getChildCount() > 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = UiTheme.dp(context, 4);
            parent.addView(row, params);
        }
    }
}
