package ph.edu.umak.crm.app.staff.reporting;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        content.setBackgroundColor(UiTheme.WHITE);
        content.setPadding(dp(24), dp(48), dp(24), dp(48));
        content.setGravity(Gravity.CENTER_HORIZONTAL);

        // Circular lock icon
        TextView iconTv = new TextView(activity);
        iconTv.setText("🔒");
        iconTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        iconTv.setGravity(Gravity.CENTER);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(0xfffef3c7);
        iconBg.setStroke(dp(1), 0xfffde68a);
        iconTv.setBackground(iconBg);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(56), dp(56));
        iconParams.bottomMargin = dp(16);
        content.addView(iconTv, iconParams);

        // Title: Access denied
        TextView titleTv = new TextView(activity);
        titleTv.setText("Access denied");
        titleTv.setTextColor(UiTheme.NAVY);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        titleTv.setTypeface(UiTheme.headingFont(activity));
        titleTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tParams.bottomMargin = dp(10);
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
        instrTv.setTextColor(0xff64748b);
        instrTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        instrTv.setGravity(Gravity.CENTER);
        instrTv.setTypeface(UiTheme.bodyFont(activity));
        instrTv.setLineSpacing(dp(2), 1.0f);
        LinearLayout.LayoutParams iParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iParams.topMargin = dp(10);
        iParams.bottomMargin = dp(24);
        content.addView(instrTv, iParams);

        // Go Back button
        Button goBackBtn = new Button(activity);
        goBackBtn.setText("← Go Back");
        goBackBtn.setTextColor(UiTheme.WHITE);
        goBackBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        goBackBtn.setTypeface(Typeface.DEFAULT_BOLD);
        goBackBtn.setAllCaps(false);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(UiTheme.NAVY);
        btnBg.setCornerRadius(dp(8));
        goBackBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), btnBg, null));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        content.addView(goBackBtn, btnParams);

        goBackBtn.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onGoBack();
        });

        LinearLayout page = PageTitle.wrap(content, "Account access", "UMak Research Management", false);
        content.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        return page;
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, activity.getResources().getDisplayMetrics());
    }
}
