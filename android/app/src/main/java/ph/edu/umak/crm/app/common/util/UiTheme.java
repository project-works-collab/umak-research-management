package ph.edu.umak.crm.app.common.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import ph.edu.umak.crm.app.R;

public final class UiTheme {
    private UiTheme() {}

    public static final int NAVY = 0xff131c4b;
    public static final int DARK_NAVY = 0xff09154d;
    public static final int BLUE = 0xff082d50;
    public static final int ACCENT = 0xff185d9d;
    public static final int MUTED = 0xff586b83;
    public static final int INK = 0xff16191c;
    public static final int PAPER = 0xfff5f6f8;
    public static final int WHITE = 0xffffffff;
    public static final int YELLOW = 0xfff8eb37;
    public static final int PALE = 0xffe5edf6;
    public static final int BORDER = 0xffdce4ee;
    public static final int ERROR_RED = 0xffb72a39;

    public static final int PEACH_BG = 0xfffff1f0;
    public static final int PEACH_BORDER = 0xfff8d7da;
    public static final int PEACH_TEXT = 0xffad2534;

    public static final int AMBER_BG = 0xfffffbeb;
    public static final int AMBER_BORDER = 0xfffef3c7;
    public static final int AMBER_TEXT = 0xff92400e;
    public static final int AMBER_ICON = 0xffb45309;

    public static final int SUCCESS_GREEN = 0xff2e7d32;
    public static final int SUCCESS_BG = 0xffe8f5e9;

    public static final int BADGE_BLUE_BG = 0xffeaf2fa;
    public static final int BADGE_BLUE_ICON = 0xff1b5e9c;

    public static final int DISABLED_BTN_BG = 0xffcbd5e1;
    public static final int DISABLED_BTN_TEXT = 0xff64748b;

    public static final int TAG_BG = 0xffe2e8f0;
    public static final int TAG_TEXT = 0xff334155;

    public static final int PILL_REQUIRED_BG = 0xfffee2e2;
    public static final int PILL_REQUIRED_TEXT = 0xff991b1b;
    public static final int PILL_OPTIONAL_BG = 0xfff1f5f9;
    public static final int PILL_OPTIONAL_TEXT = 0xff475569;

    public static final int STATUS_SUBMITTED_BG = 0xffe0f2fe;
    public static final int STATUS_SUBMITTED_TEXT = 0xff0369a1;
    public static final int STATUS_IN_REVIEW_BG = 0xffe0e7ff;
    public static final int STATUS_IN_REVIEW_TEXT = 0xff3730a3;
    public static final int STATUS_CORRECTION_BG = 0xfffef3c7;
    public static final int STATUS_CORRECTION_TEXT = 0xff92400e;
    public static final int STATUS_COMPLETED_BG = 0xffdcfce7;
    public static final int STATUS_COMPLETED_TEXT = 0xff166534;

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static Typeface headingFont(Context context) {
        return context.getResources().getFont(R.font.marcellus_regular);
    }

    public static Typeface bodyFont(Context context) {
        return context.getResources().getFont(R.font.metropolis_regular);
    }

    public static Typeface boldFont(Context context) {
        return context.getResources().getFont(R.font.metropolis_semibold);
    }

    public static GradientDrawable shape(Context context, int fill, int radiusDp, int strokeColor) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(dp(context, radiusDp));
        if (strokeColor != 0) shape.setStroke(dp(context, 1), strokeColor);
        return shape;
    }

    public static RippleDrawable ripple(GradientDrawable content, int rippleColor) {
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), content, null);
    }
}
