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
        Typeface bold = UiTheme.boldFont(context);

        FrameLayout screen = new FrameLayout(context);
        screen.setBackgroundColor(UiTheme.DARK_NAVY);

        LinearLayout brand = new LinearLayout(context);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setGravity(Gravity.CENTER);
        brand.setTranslationY(-UiTheme.dp(context, 34));
        brand.setPadding(UiTheme.dp(context, 16), 0, UiTheme.dp(context, 16), 0);

        LinearLayout emblemsRow = new LinearLayout(context);
        emblemsRow.setOrientation(LinearLayout.HORIZONTAL);
        emblemsRow.setGravity(Gravity.CENTER);

        ImageView umakLogo = new ImageView(context);
        umakLogo.setImageResource(R.drawable.umak_logo_white);
        umakLogo.setContentDescription("University of Makati emblem");
        umakLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        emblemsRow.addView(umakLogo, new LinearLayout.LayoutParams(UiTheme.dp(context, 86), UiTheme.dp(context, 86)));

        View emblemGap = new View(context);
        emblemsRow.addView(emblemGap, new LinearLayout.LayoutParams(UiTheme.dp(context, 16), 1));

        ImageView crmLogo = new ImageView(context);
        crmLogo.setImageResource(R.drawable.crm_logo);
        crmLogo.setContentDescription("Center for Research Management emblem");
        crmLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        emblemsRow.addView(crmLogo, new LinearLayout.LayoutParams(UiTheme.dp(context, 86), UiTheme.dp(context, 86)));

        brand.addView(emblemsRow, new LinearLayout.LayoutParams(-2, -2));

        ImageView title = new ImageView(context);
        title.setImageResource(R.drawable.umak_type_white);
        title.setContentDescription("UNIVERSITY OF MAKATI");
        title.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(UiTheme.dp(context, 230), UiTheme.dp(context, 38));
        titleParams.topMargin = UiTheme.dp(context, 10);
        brand.addView(title, titleParams);

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
