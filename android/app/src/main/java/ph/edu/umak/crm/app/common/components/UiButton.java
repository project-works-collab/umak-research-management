package ph.edu.umak.crm.app.common.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.Button;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class UiButton {
    private UiButton() {}

    public static Button create(Context context, String title, int fill, int textColor, Typeface font, Runnable callback) {
        return create(context, title, fill, textColor, 0, font, callback);
    }

    public static Button create(Context context, String title, int fill, int textColor, int strokeColor, Typeface font, Runnable callback) {
        Button button = new Button(context, null, android.R.attr.borderlessButtonStyle);
        button.setText(title);
        button.setAllCaps(false);
        button.setTypeface(font != null ? font : UiTheme.boldFont(context));
        button.setTextColor(textColor);
        button.setMinHeight(UiTheme.dp(context, 48));
        button.setBackground(UiTheme.ripple(UiTheme.shape(context, fill, 8, strokeColor), 0x20395072));
        button.setOnClickListener(view -> {
            if (callback != null) callback.run();
        });
        return button;
    }

    public static Button primary(Context context, String title, Runnable callback) {
        return create(context, title, UiTheme.NAVY, UiTheme.WHITE, 0, UiTheme.boldFont(context), callback);
    }

    public static Button secondary(Context context, String title, Runnable callback) {
        return create(context, title, UiTheme.PALE, UiTheme.NAVY, 0, UiTheme.boldFont(context), callback);
    }

    public static Button outline(Context context, String title, int strokeColor, int textColor, Runnable callback) {
        return create(context, title, UiTheme.WHITE, textColor, strokeColor, UiTheme.boldFont(context), callback);
    }

    public static Button text(Context context, String title, int textColor, Runnable callback) {
        Button button = new Button(context, null, android.R.attr.borderlessButtonStyle);
        button.setText(title);
        button.setAllCaps(false);
        button.setTypeface(UiTheme.boldFont(context));
        button.setTextColor(textColor);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setOnClickListener(view -> {
            if (callback != null) callback.run();
        });
        return button;
    }
}
