package ph.edu.umak.crm.app.common.dialogs;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import ph.edu.umak.crm.app.common.util.UiTheme;

public final class BottomSheet {
    private BottomSheet() {}

    public static void show(Dialog dialog, View content) {
        LinearLayout panel = new LinearLayout(content.getContext());
        panel.setOrientation(LinearLayout.VERTICAL);
        float radius = UiTheme.dp(content.getContext(), 16);
        GradientDrawable surface = new GradientDrawable();
        surface.setColor(UiTheme.WHITE);
        surface.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
        panel.setBackground(surface);
        panel.setClipToOutline(true);

        View handle = new View(content.getContext());
        handle.setBackground(UiTheme.shape(content.getContext(), UiTheme.BORDER, 2, 0));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(
                UiTheme.dp(content.getContext(), 32), UiTheme.dp(content.getContext(), 4));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.topMargin = UiTheme.dp(content.getContext(), 8);
        panel.addView(handle, handleParams);

        if (content instanceof ScrollView) {
            panel.addView(content);
        } else {
            ScrollView scroll = new ScrollView(content.getContext());
            scroll.addView(content);
            panel.addView(scroll);
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(panel);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(android.R.style.Animation_InputMethod);
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
        ph.edu.umak.crm.app.common.animation.UiMotion.animateEntrance(panel, 220, 24);
    }
}
