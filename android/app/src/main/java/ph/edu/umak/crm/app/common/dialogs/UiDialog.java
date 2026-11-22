package ph.edu.umak.crm.app.common.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class UiDialog {
    private UiDialog() {}

    public static void show(Context context, String title, String message,
            String confirmLabel, Runnable onConfirm, String cancelLabel, Runnable onCancel) {
        Typeface heading = UiTheme.headingFont(context);
        Typeface body = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 24), UiTheme.dp(context, 24),
                UiTheme.dp(context, 24), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 14, 0));

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(UiTheme.NAVY);
        titleView.setTypeface(heading);
        ScreenTypography.apply(titleView, 23);
        ScreenTypography.heading(titleView);
        card.addView(titleView);

        TextView description = new TextView(context);
        description.setText(message);
        description.setTextColor(UiTheme.MUTED);
        description.setTypeface(body);
        ScreenTypography.apply(description, 14);
        description.setPadding(0, UiTheme.dp(context, 12), 0, UiTheme.dp(context, 10));
        card.addView(description);

        AlertDialog dialog = new AlertDialog.Builder(context).setView(card).create();

        LinearLayout actions = new LinearLayout(context);
        actions.setGravity(cancelLabel == null ? Gravity.CENTER : Gravity.END);
        card.addView(actions);

        if (cancelLabel != null) {
            Button cancel = UiButton.create(context, cancelLabel, UiTheme.WHITE, UiTheme.ERROR_RED,
                    UiTheme.boldFont(context), () -> {
                        dialog.dismiss();
                        if (onCancel != null) onCancel.run();
                    });
            actions.addView(cancel);
        }

        Button confirm = UiButton.create(context, confirmLabel, UiTheme.WHITE, UiTheme.NAVY,
                UiTheme.boldFont(context), () -> {
                    dialog.dismiss();
                    if (onConfirm != null) onConfirm.run();
                });
        actions.addView(confirm);

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        ph.edu.umak.crm.app.common.animation.UiMotion.animateDialog(card);
    }

    public static void alert(Context context, String title, String message) {
        show(context, title, message, "Close", null, null, null);
    }
}
