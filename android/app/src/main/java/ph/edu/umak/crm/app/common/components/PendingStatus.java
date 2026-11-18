package ph.edu.umak.crm.app.common.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

/** Inline status for work without a measurable percentage. */
public final class PendingStatus extends LinearLayout {
    private final ProgressBar spinner;
    private final TextView label;

    public PendingStatus(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        int unit = UiTheme.dp(context, 8);
        setPadding(0, unit, 0, unit);

        spinner = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
        spinner.setIndeterminate(true);
        spinner.setIndeterminateTintList(ColorStateList.valueOf(UiTheme.ACCENT));
        int size = UiTheme.dp(context, 20);
        LayoutParams icon = new LayoutParams(size, size);
        icon.setMarginEnd(unit);
        addView(spinner, icon);

        label = new TextView(context);
        label.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(label, 13);
        label.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
        addView(label, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        clear();
    }

    public void working(String message) {
        display(message, true, false);
    }

    public void result(String message, boolean failed) {
        display(message, false, failed);
    }

    public void clear() {
        display("", false, false);
    }

    private void display(String message, boolean busy, boolean failed) {
        spinner.setVisibility(busy ? VISIBLE : GONE);
        label.setTextColor(failed ? UiTheme.ERROR_RED : UiTheme.MUTED);
        label.setText(message);
        setVisibility(message.isEmpty() ? GONE : VISIBLE);
    }
}
