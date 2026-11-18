package ph.edu.umak.crm.app.staff.reporting;

import android.app.Activity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Reporting Access Denied View (Wireframe 9.5).
 * Displayed when user does not have "Reporting" permission (or toggled off in 5.10).
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class AccessDeniedReportingView {

    public interface Callbacks {
        void onGoBack();
    }

    private final Activity activity;
    private final Callbacks callbacks;

    public AccessDeniedReportingView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
    }

    public View getContentLayout() {
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(UiTheme.PAPER);
        content.setPadding(dp(20), dp(24), dp(20), dp(24));
        content.setGravity(Gravity.CENTER);

        // Title: Access denied
        TextView titleTv = new TextView(activity);
        titleTv.setText("Access denied");
        titleTv.setTextColor(UiTheme.NAVY);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        titleTv.setTypeface(UiTheme.headingFont(activity));
        titleTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tParams.topMargin = dp(12);
        tParams.bottomMargin = dp(8);
        content.addView(titleTv, tParams);

        // Main description
        TextView descTv = new TextView(activity);
        descTv.setText("Your account does not have permission to view this page or perform this action.");
        descTv.setTextColor(UiTheme.NAVY);
        descTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        descTv.setGravity(Gravity.CENTER);
        descTv.setTypeface(UiTheme.bodyFont(activity));
        descTv.setLineSpacing(dp(2), 1.0f);
        content.addView(descTv);

        // Instructions
        TextView instrTv = new TextView(activity);
        instrTv.setText("Go back to your previous screen. If you need access, contact CRM.");
        instrTv.setTextColor(UiTheme.MUTED);
        instrTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        instrTv.setGravity(Gravity.CENTER);
        instrTv.setTypeface(UiTheme.bodyFont(activity));
        instrTv.setLineSpacing(dp(2), 1.0f);
        LinearLayout.LayoutParams iParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iParams.topMargin = dp(10);
        iParams.bottomMargin = dp(20);
        content.addView(instrTv, iParams);

        content.addView(UiButton.primary(activity, "Go Back", () -> {
            if (callbacks != null) callbacks.onGoBack();
        }));

        LinearLayout page = PageTitle.wrap(content, "Account access", "UMak Research Management", false);
        content.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        return page;
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, activity.getResources().getDisplayMetrics());
    }
}
