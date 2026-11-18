package ph.edu.umak.crm.app.common.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class SignInGateDialog {
    private SignInGateDialog() {}

    public static void show(Context context, Runnable onSignIn, Runnable onCreateAccount) {
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

        // Blue circle badge with sign-in icon
        LinearLayout badge = new LinearLayout(context);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(UiTheme.shape(context, UiTheme.BADGE_BLUE_BG, 28, 0));
        int badgeSize = UiTheme.dp(context, 56);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(badgeSize, badgeSize);
        badgeParams.bottomMargin = UiTheme.dp(context, 16);

        ImageView icon = new ImageView(context);
        Drawable loginDrawable = context.getDrawable(R.drawable.ic_crm_signin);
        if (loginDrawable != null) {
            loginDrawable = loginDrawable.mutate();
            loginDrawable.setColorFilter(UiTheme.BADGE_BLUE_ICON, PorterDuff.Mode.SRC_IN);
            icon.setImageDrawable(loginDrawable);
        }
        int iconSize = UiTheme.dp(context, 28);
        badge.addView(icon, new LinearLayout.LayoutParams(iconSize, iconSize));
        card.addView(badge, badgeParams);

        TextView title = new TextView(context);
        title.setText("Sign in to submit");
        title.setTextColor(UiTheme.INK);
        title.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(title, 22);
        title.setGravity(Gravity.START);
        title.setPadding(0, 0, 0, UiTheme.dp(context, 8));
        card.addView(title);

        TextView body = new TextView(context);
        body.setText("You need an active Researcher account to submit this request. You can keep browsing services and forms without signing in.");
        body.setTextColor(UiTheme.MUTED);
        body.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(body, 14);
        body.setGravity(Gravity.START);
        body.setPadding(0, 0, 0, UiTheme.dp(context, 24));
        card.addView(body);

        Button signInBtn = UiButton.primary(context, "Sign In", () -> {
            dialog.dismiss();
            if (onSignIn != null) onSignIn.run();
        });
        card.addView(signInBtn, new LinearLayout.LayoutParams(-1, -2));

        Button createBtn = UiButton.outline(context, "+ Create Researcher Account", UiTheme.NAVY, UiTheme.NAVY, () -> {
            dialog.dismiss();
            if (onCreateAccount != null) onCreateAccount.run();
        });
        LinearLayout.LayoutParams createParams = new LinearLayout.LayoutParams(-1, -2);
        createParams.topMargin = UiTheme.dp(context, 10);
        card.addView(createBtn, createParams);

        Button notNowBtn = UiButton.text(context, "Not now", UiTheme.MUTED, dialog::dismiss);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(-1, -2);
        cancelParams.topMargin = UiTheme.dp(context, 8);
        card.addView(notNowBtn, cancelParams);

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
