package ph.edu.umak.crm.app.common.components;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class PageTitle {
    private PageTitle() {}

    public static LinearLayout wrap(LinearLayout content, String title, String subtitle, boolean centered) {
        TextView heading = new TextView(content.getContext());
        heading.setText(title);
        return wrap(content, heading, subtitle, centered);
    }

    public static LinearLayout wrap(LinearLayout content, TextView heading, String subtitle, boolean centered) {
        Context context = content.getContext();
        LinearLayout page = new LinearLayout(context);
        page.setOrientation(LinearLayout.VERTICAL);
        LinearLayout band = new LinearLayout(context);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(UiTheme.BLUE);
        int padding = UiTheme.dp(context, 16);
        band.setPadding(padding, padding, padding, padding);

        if (heading.getParent() instanceof LinearLayout) {
            ((LinearLayout) heading.getParent()).removeView(heading);
        }
        heading.setTextColor(UiTheme.WHITE);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        if (centered) heading.setGravity(Gravity.CENTER);
        band.addView(heading);

        if (subtitle != null) {
            TextView copy = new TextView(context);
            copy.setText(subtitle);
            copy.setTextColor(UiTheme.PALE);
            copy.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(copy, 13);
            if (centered) copy.setGravity(Gravity.CENTER);
            band.addView(copy);
        }
        page.addView(band);
        page.addView(content);
        return page;
    }
}
