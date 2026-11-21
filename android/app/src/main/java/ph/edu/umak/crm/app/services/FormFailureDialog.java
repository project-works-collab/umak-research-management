package ph.edu.umak.crm.app.services;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class FormFailureDialog {
    private FormFailureDialog() {}

    public static void show(Context context, Runnable onRetry, Runnable onCancel) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setBackground(UiTheme.shape(context, UiTheme.WHITE, 16, 0));
        int padH = UiTheme.dp(context, 24);
        int padV = UiTheme.dp(context, 28);
        content.setPadding(padH, padV, padH, padV);

        // Warning Icon
        ImageView alertIcon = new ImageView(context);
        alertIcon.setImageResource(R.drawable.ic_crm_alert);
        alertIcon.setColorFilter(0xffe67e22);
        int iconSize = UiTheme.dp(context, 38);
        content.addView(alertIcon, new LinearLayout.LayoutParams(iconSize, iconSize));

        // Title
        TextView titleView = new TextView(context);
        titleView.setText("Unable to open the form. Try\nagain.");
        titleView.setTextColor(0xff082d50);
        titleView.setTypeface(UiTheme.headingFont(context));
        titleView.setGravity(Gravity.CENTER);
        ScreenTypography.apply(titleView, 18);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = UiTheme.dp(context, 14);
        titleLp.bottomMargin = UiTheme.dp(context, 20);
        content.addView(titleView, titleLp);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(content)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Retry Button with Refresh Icon
        LinearLayout retryBtn = new LinearLayout(context);
        retryBtn.setOrientation(LinearLayout.HORIZONTAL);
        retryBtn.setGravity(Gravity.CENTER);
        retryBtn.setBackground(UiTheme.shape(context, 0xff131c4b, 8, 0));
        retryBtn.setClickable(true);
        retryBtn.setFocusable(true);
        retryBtn.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 12), UiTheme.dp(context, 16), UiTheme.dp(context, 12));

        ImageView refreshIcon = new ImageView(context);
        refreshIcon.setImageResource(R.drawable.ic_crm_refresh);
        refreshIcon.setColorFilter(0xffffffff);
        int rfSize = UiTheme.dp(context, 16);
        retryBtn.addView(refreshIcon, new LinearLayout.LayoutParams(rfSize, rfSize));

        TextView retryText = new TextView(context);
        retryText.setText("Retry");
        retryText.setTextColor(0xffffffff);
        retryText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(retryText, 14);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(-2, -2);
        textLp.setMarginStart(UiTheme.dp(context, 8));
        retryBtn.addView(retryText, textLp);

        retryBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (onRetry != null) onRetry.run();
        });
        content.addView(retryBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));

        // Cancel Text Button
        Button cancelBtn = UiButton.text(context, "Cancel", 0xff082d50, () -> {
            dialog.cancel();
        });
        cancelBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(cancelBtn, 14);
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(-2, -2);
        cancelLp.topMargin = UiTheme.dp(context, 10);
        content.addView(cancelBtn, cancelLp);

        dialog.setOnCancelListener(d -> {
            if (onCancel != null) onCancel.run();
        });

        dialog.show();
    }
}
