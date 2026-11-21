package ph.edu.umak.crm.app.common.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class CenteredActionDialog {
    private CenteredActionDialog() {}

    public static void show(Context context, String title, String message, String action,
                            String cancelLabel, Runnable onAction, Runnable onCancel) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = UiTheme.dp(context, 24);
        content.setPadding(padding, padding, padding, padding);
        content.setBackground(UiTheme.shape(context, UiTheme.WHITE, 12, 0));

        TextView heading = new TextView(context);
        heading.setText(title);
        heading.setTextColor(0xff082d50);
        heading.setTypeface(UiTheme.headingFont(context));
        heading.setGravity(Gravity.CENTER);
        ScreenTypography.apply(heading, 18);
        ScreenTypography.heading(heading);
        content.addView(heading);

        if (message == null) {
            heading.setPadding(0, 0, 0, UiTheme.dp(context, 16));
        } else {
            TextView copy = new TextView(context);
            copy.setText(message);
            copy.setTextColor(0xff586b83);
            copy.setTypeface(UiTheme.bodyFont(context));
            copy.setGravity(Gravity.CENTER);
            ScreenTypography.apply(copy, 13);
            copy.setPadding(0, UiTheme.dp(context, 10), 0, UiTheme.dp(context, 20));
            content.addView(copy);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(content)
                .setCancelable(cancelLabel != null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button actionBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        actionBtn.setText(action);
        actionBtn.setAllCaps(false);
        actionBtn.setTextColor(0xffffffff);
        actionBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(actionBtn, 15);
        actionBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        actionBtn.setMinHeight(UiTheme.dp(context, 44));
        actionBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (onAction != null) onAction.run();
        });
        content.addView(actionBtn);

        if (cancelLabel != null) {
            Button cancelBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
            cancelBtn.setText(cancelLabel);
            cancelBtn.setAllCaps(false);
            cancelBtn.setTextColor(0xff082d50);
            cancelBtn.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(cancelBtn, 15);
            cancelBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xffe2e8f0, 8, 0), 0x20082d50));
            cancelBtn.setMinHeight(UiTheme.dp(context, 44));
            cancelBtn.setOnClickListener(v -> dialog.cancel());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = UiTheme.dp(context, 10);
            content.addView(cancelBtn, params);

            dialog.setOnCancelListener(ignored -> {
                if (onCancel != null) onCancel.run();
            });
        }
        dialog.show();
    }
}
