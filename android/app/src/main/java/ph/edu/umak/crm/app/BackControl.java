package ph.edu.umak.crm.app;

import android.content.Context;
import android.view.Gravity;
import android.widget.Button;

final class BackControl {
    private BackControl() {}

    static Button create(Context context, String label, int color, Runnable action) {
        Button button = new Button(context, null, android.R.attr.borderlessButtonStyle);
        int unit = Math.round(8 * context.getResources().getDisplayMetrics().density);
        button.setText("‹  " + label);
        button.setContentDescription(label);
        button.setAllCaps(false);
        button.setTextColor(color);
        button.setTypeface(context.getResources().getFont(R.font.metropolis_semibold));
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setMinHeight(unit * 6);
        button.setPadding(unit * 2, unit, unit * 2, unit);
        ScreenTypography.apply(button, 14);
        button.setOnClickListener(view -> action.run());
        return button;
    }
}
