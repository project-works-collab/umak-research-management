package ph.edu.umak.crm.app.services;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class FormFailureDialog {
    private FormFailureDialog() {}

    public static void show(Context context, Runnable onRetry, Runnable onCancel) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 16, 0));
        int pad = UiTheme.dp(context, 24);
        card.setPadding(pad, UiTheme.dp(context, 16), pad, pad);

        // Drag handle
        View handle = new View(context);
        handle.setBackground(UiTheme.shape(context, 0xffd0d7de, 3, 0));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(UiTheme.dp(context, 36), UiTheme.dp(context, 4));
        handleParams.bottomMargin = UiTheme.dp(context, 20);
        card.addView(handle, handleParams);

        TextView message = new TextView(context);
        message.setText("Unable to open the form. Try again.");
        message.setTextColor(UiTheme.INK);
        message.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(message, 16);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 0, 0, UiTheme.dp(context, 24));
        card.addView(message);

        Button retryBtn = UiButton.primary(context, "Retry", () -> {
            dialog.dismiss();
            if (onRetry != null) onRetry.run();
        });
        card.addView(retryBtn, new LinearLayout.LayoutParams(-1, -2));

        Button cancelBtn = UiButton.outline(context, "Cancel", UiTheme.BORDER, UiTheme.INK, () -> {
            dialog.dismiss();
            if (onCancel != null) onCancel.run();
        });
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(-1, -2);
        cancelParams.topMargin = UiTheme.dp(context, 10);
        card.addView(cancelBtn, cancelParams);

        dialog.setContentView(card);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(wlp);
        }
        dialog.show();
    }
}
