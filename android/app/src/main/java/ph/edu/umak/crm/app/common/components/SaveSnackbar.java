package ph.edu.umak.crm.app.common.components;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class SaveSnackbar extends FrameLayout {
    private static final long DISPLAY_MILLIS = 5000;

    private final TextView message;
    private final Runnable clearMessage = this::clear;

    public SaveSnackbar(Context context, View content) {
        super(context);
        addView(content, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        message = new TextView(context);
        message.setTextColor(UiTheme.WHITE);
        message.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(message, 13);
        message.setBackground(UiTheme.shape(context, UiTheme.NAVY, 24, 0));
        message.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 10),
                UiTheme.dp(context, 16), UiTheme.dp(context, 10));
        message.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        message.setElevation(UiTheme.dp(context, 4));
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        int margin = UiTheme.dp(context, 12);
        params.setMargins(margin, 0, margin, margin);
        addView(message, params);
        clear();
    }

    public void show(String text) {
        removeCallbacks(clearMessage);
        message.setText(text);
        message.setVisibility(View.VISIBLE);
        postDelayed(clearMessage, DISPLAY_MILLIS);
    }

    public void clear() {
        removeCallbacks(clearMessage);
        message.setVisibility(View.GONE);
    }
}
