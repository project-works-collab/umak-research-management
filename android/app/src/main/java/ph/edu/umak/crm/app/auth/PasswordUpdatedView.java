package ph.edu.umak.crm.app.auth;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class PasswordUpdatedView {
    private final View root;

    public PasswordUpdatedView(Context context, AuthCallbacks callbacks) {
        Typeface heading = UiTheme.headingFont(context);
        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        LinearLayout form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 24);
        form.setPadding(pad, pad, pad, UiTheme.dp(context, 32));
        form.setFocusableInTouchMode(true);

        TextView headline = new TextView(context);
        headline.setText("Password Update Successful");
        headline.setTextColor(UiTheme.INK);
        headline.setTypeface(heading);
        ScreenTypography.apply(headline, 32);
        ScreenTypography.heading(headline);
        form.addView(headline);

        TextView description = new TextView(context);
        description.setText("Your password has been updated. You may now sign in to the UMak Research Management and Lifecycle Information System.");
        description.setTextColor(UiTheme.MUTED);
        description.setTypeface(body);
        ScreenTypography.apply(description, 14);
        description.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 24));
        form.addView(description);

        View space = new View(context);
        form.addView(space, new LinearLayout.LayoutParams(-1, 0, 1));

        Button continueBtn = UiButton.create(context, "Continue", UiTheme.NAVY, UiTheme.WHITE, bold, () -> {
            if (callbacks != null) callbacks.onShowSignIn();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = UiTheme.dp(context, 8);
        form.addView(continueBtn, params);

        ScrollView scroll = new ScrollView(context);
        scroll.setBackgroundColor(UiTheme.PAPER);
        scroll.setFillViewport(true);
        scroll.addView(form);

        root = scroll;
    }

    public View getView() {
        return root;
    }
}
