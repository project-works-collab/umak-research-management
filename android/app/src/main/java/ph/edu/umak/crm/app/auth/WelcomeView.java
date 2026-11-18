package ph.edu.umak.crm.app.auth;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class WelcomeView {
    private WelcomeView() {}

    public static View create(Context context, AuthCallbacks callbacks) {
        Typeface heading = UiTheme.headingFont(context);
        Typeface bold = UiTheme.boldFont(context);

        FrameLayout screen = new FrameLayout(context);
        screen.setBackgroundColor(UiTheme.DARK_NAVY);

        LinearLayout brand = new LinearLayout(context);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setGravity(Gravity.CENTER);
        brand.setTranslationY(-UiTheme.dp(context, 34));
        brand.setPadding(UiTheme.dp(context, 16), 0, UiTheme.dp(context, 16), 0);

        ImageView emblems = new ImageView(context);
        emblems.setImageResource(R.drawable.shared_access_emblems);
        emblems.setContentDescription("University of Makati and Center for Research Management emblems");
        emblems.setScaleType(ImageView.ScaleType.FIT_CENTER);
        brand.addView(emblems, new LinearLayout.LayoutParams(UiTheme.dp(context, 188), UiTheme.dp(context, 91)));

        TextView title = new TextView(context);
        title.setText("UNIVERSITY OF MAKATI");
        title.setTextColor(UiTheme.WHITE);
        title.setTypeface(heading);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UiTheme.dp(context, 7), 0, 0);
        ScreenTypography.apply(title, 19);
        ScreenTypography.heading(title);
        brand.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(context);
        subtitle.setText("Center for Research Management - RMLIS");
        subtitle.setTextColor(UiTheme.WHITE);
        subtitle.setTypeface(bold);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, UiTheme.dp(context, 7), 0, 0);
        ScreenTypography.apply(subtitle, 13);
        brand.addView(subtitle, new LinearLayout.LayoutParams(-1, -2));

        screen.addView(brand, new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER));

        Button start = UiButton.create(context, "Get Started", UiTheme.YELLOW, UiTheme.NAVY, bold, () -> {
            if (callbacks != null) callbacks.onStartAccess();
        });
        FrameLayout.LayoutParams startParams = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        startParams.setMargins(UiTheme.dp(context, 46), 0, UiTheme.dp(context, 46), UiTheme.dp(context, 52));
        screen.addView(start, startParams);

        return screen;
    }
}
