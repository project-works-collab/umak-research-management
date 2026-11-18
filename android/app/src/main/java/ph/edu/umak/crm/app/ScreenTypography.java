package ph.edu.umak.crm.app;

import android.os.Build;
import android.util.TypedValue;
import android.widget.TextView;

public final class ScreenTypography {
    private ScreenTypography() {}

    public static void apply(TextView view, int sizeSp) {
        view.setTextSize(sizeSp);
        int lineSp = sizeSp >= 24 ? sizeSp + 8 : sizeSp + 6;
        if (Build.VERSION.SDK_INT >= 34) {
            view.setLineHeight(TypedValue.COMPLEX_UNIT_SP, lineSp);
        } else {
            int pixels = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    lineSp, view.getResources().getDisplayMetrics()));
            view.setLineSpacing(pixels - view.getPaint().getFontMetricsInt(null), 1f);
        }
    }

    public static void heading(TextView view) {
        if (Build.VERSION.SDK_INT >= 28) view.setAccessibilityHeading(true);
    }
}
