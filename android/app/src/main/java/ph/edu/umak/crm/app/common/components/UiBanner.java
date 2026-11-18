package ph.edu.umak.crm.app.common.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class UiBanner {
    private UiBanner() {}

    public static View create(Context context, String message, String actionText, Runnable actionClick) {
        return createCustom(context, message, actionText, actionClick, UiTheme.PEACH_BG, UiTheme.PEACH_BORDER, UiTheme.PEACH_TEXT);
    }

    public static View amber(Context context, String message) {
        return createCustom(context, message, null, null, UiTheme.AMBER_BG, UiTheme.AMBER_BORDER, UiTheme.AMBER_TEXT);
    }

    public static View createCustom(Context context, String message, String actionText, Runnable actionClick,
            int bg, int border, int textColor) {
        LinearLayout banner = new LinearLayout(context);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        int padH = UiTheme.dp(context, 12);
        int padV = UiTheme.dp(context, 10);
        banner.setPadding(padH, padV, padH, padV);
        banner.setBackground(UiTheme.shape(context, bg, 8, border));

        ImageView icon = new ImageView(context);
        Drawable warning = context.getDrawable(R.drawable.ic_crm_alert);
        if (warning != null) {
            warning = warning.mutate();
            warning.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
            icon.setImageDrawable(warning);
        }
        int iconSize = UiTheme.dp(context, 18);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconParams.rightMargin = UiTheme.dp(context, 10);
        banner.addView(icon, iconParams);

        TextView text = new TextView(context);
        text.setTextColor(textColor);
        text.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(text, 13);

        if (actionText != null && !actionText.isEmpty() && actionClick != null) {
            String full = message + " " + actionText;
            SpannableString ss = new SpannableString(full);
            int start = message.length() + 1;
            int end = full.length();
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    actionClick.run();
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setText(ss);
            text.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            text.setText(message);
        }

        banner.addView(text, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return banner;
    }
}
