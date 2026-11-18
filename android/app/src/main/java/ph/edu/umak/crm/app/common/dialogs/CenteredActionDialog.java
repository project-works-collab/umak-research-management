package ph.edu.umak.crm.app.common.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class CenteredActionDialog {
    private CenteredActionDialog() {}

    public static void show(Context context, String title, String message, String action,
                            String cancelLabel, Runnable onAction, Runnable onCancel) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = UiTheme.dp(context, 20);
        content.setPadding(padding, padding, padding, padding);

        TextView heading = new TextView(context);
        heading.setText(title);
        heading.setTextColor(UiTheme.NAVY);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 20);
        ScreenTypography.heading(heading);
        content.addView(heading);

        if (message == null) {
            heading.setPadding(0, 0, 0, UiTheme.dp(context, 16));
        } else {
            TextView copy = new TextView(context);
            copy.setText(message);
            copy.setTextColor(UiTheme.MUTED);
            copy.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(copy, 13);
            copy.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 16));
            content.addView(copy);
        }

        ScrollView scroll = new ScrollView(context);
        scroll.addView(content);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(scroll)
                .setCancelable(cancelLabel != null)
                .create();
        content.addView(UiButton.primary(context, action, () -> {
            dialog.dismiss();
            if (onAction != null) onAction.run();
        }));
        if (cancelLabel != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.topMargin = UiTheme.dp(context, 8);
            content.addView(UiButton.secondary(context, cancelLabel, dialog::cancel), params);
            dialog.setOnCancelListener(ignored -> {
                if (onCancel != null) onCancel.run();
            });
        }
        dialog.show();
    }
}
