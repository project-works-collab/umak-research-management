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
import android.widget.ScrollView;
import android.widget.TextView;

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
    private ScrollView scrollView;
    private LinearLayout rootLayout;

    public AccessDeniedReportingView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
    }

    public View getContentLayout() {
        if (scrollView != null && scrollView.getParent() instanceof ViewGroup) {
            ((ViewGroup) scrollView.getParent()).removeView(scrollView);
        }

        scrollView = new ScrollView(activity);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(UiTheme.PAPER);

        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(20), dp(24), dp(20), dp(32));

        scrollView.addView(rootLayout);
        buildLayout();

        return scrollView;
    }

    private void buildLayout() {
        rootLayout.removeAllViews();

        // 1. Institution
        TextView instTv = new TextView(activity);
        instTv.setText("Center for Research Management");
        instTv.setTextColor(UiTheme.MUTED);
        instTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        instTv.setTypeface(Typeface.DEFAULT);
        rootLayout.addView(instTv);

        // 2. Category
        TextView catTv = new TextView(activity);
        catTv.setText("Account access");
        catTv.setTextColor(UiTheme.NAVY);
        catTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        catTv.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        catParams.topMargin = dp(2);
        rootLayout.addView(catTv, catParams);

        // 3. Card Container
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        card.setPadding(pad, pad, pad, pad);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(UiTheme.WHITE);
        cardBg.setCornerRadius(dp(8));
        cardBg.setStroke(dp(1), UiTheme.BORDER);
        card.setBackground(cardBg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(20);
        rootLayout.addView(card, cardParams);

        // Branding
        TextView brandTv = new TextView(activity);
        brandTv.setText("UMak Research Management");
        brandTv.setTextColor(UiTheme.MUTED);
        brandTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        card.addView(brandTv);

        // Title: Access denied
        TextView titleTv = new TextView(activity);
        titleTv.setText("Access denied");
        titleTv.setTextColor(UiTheme.PEACH_TEXT);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tParams.topMargin = dp(12);
        tParams.bottomMargin = dp(8);
        card.addView(titleTv, tParams);

        // Main description
        TextView descTv = new TextView(activity);
        descTv.setText("Your account does not have permission to view this page or perform this action.");
        descTv.setTextColor(UiTheme.NAVY);
        descTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        descTv.setLineSpacing(dp(2), 1.0f);
        card.addView(descTv);

        // Instructions
        TextView instrTv = new TextView(activity);
        instrTv.setText("Go back to your previous screen. If you need access, contact CRM.");
        instrTv.setTextColor(UiTheme.MUTED);
        instrTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        instrTv.setLineSpacing(dp(2), 1.0f);
        LinearLayout.LayoutParams iParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iParams.topMargin = dp(10);
        iParams.bottomMargin = dp(20);
        card.addView(instrTv, iParams);

        // Action button: Go Back
        Button goBackBtn = new Button(activity);
        goBackBtn.setText("Go Back");
        goBackBtn.setTextColor(UiTheme.WHITE);
        goBackBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        goBackBtn.setTypeface(Typeface.DEFAULT_BOLD);

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(UiTheme.NAVY);
        btnBg.setCornerRadius(dp(8));
        goBackBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), btnBg, null));

        goBackBtn.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onGoBack();
        });
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        card.addView(goBackBtn, btnParams);
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, activity.getResources().getDisplayMetrics());
    }
}
