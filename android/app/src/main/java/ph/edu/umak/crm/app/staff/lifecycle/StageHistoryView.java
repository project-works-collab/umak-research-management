package ph.edu.umak.crm.app.staff.lifecycle;

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

import java.util.List;

import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Research Stage History View (Wireframe 8.3).
 * Chronological stage transitions and immutable corrections log.
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class StageHistoryView {

    public interface Callbacks {
        void onBackToMilestones();
    }

    private final Activity activity;
    private final Callbacks callbacks;
    private StaffLifecycleData.ResearchLifecycleState state;
    private LinearLayout rootLayout;

    public StageHistoryView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        this.state = StaffLifecycleData.getDefaultLifecycleState();
        buildUi();
    }

    public void setState(StaffLifecycleData.ResearchLifecycleState state) {
        if (state != null) {
            this.state = state;
            buildUi();
        }
    }

    public ViewGroup getContentLayout() {
        if (rootLayout != null && rootLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) rootLayout.getParent()).removeView(rootLayout);
        }
        return rootLayout;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    private void buildUi() {
        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));

        // 1. Header
        TextView topHeader = new TextView(activity);
        topHeader.setText("Center for Research Management");
        topHeader.setTextColor(UiTheme.NAVY);
        topHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        topHeader.setTypeface(Typeface.DEFAULT_BOLD);
        rootLayout.addView(topHeader);

        TextView title = new TextView(activity);
        title.setText("Stage History");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(4);
        titleParams.bottomMargin = dp(4);
        rootLayout.addView(title, titleParams);

        TextView subtitle = new TextView(activity);
        subtitle.setText(state.title);
        subtitle.setTextColor(UiTheme.NAVY);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        subtitle.setTypeface(Typeface.DEFAULT_BOLD);
        rootLayout.addView(subtitle);

        TextView metaText = new TextView(activity);
        metaText.setText(state.recordId + " · Current overall stage " + state.currentStage);
        metaText.setTextColor(UiTheme.MUTED);
        metaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        metaParams.topMargin = dp(2);
        metaParams.bottomMargin = dp(14);
        rootLayout.addView(metaText, metaParams);

        // 2. Info Banner
        LinearLayout infoCard = new LinearLayout(activity);
        infoCard.setOrientation(LinearLayout.HORIZONTAL);
        infoCard.setGravity(Gravity.CENTER_VERTICAL);
        infoCard.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable infoBg = new GradientDrawable();
        infoBg.setColor(UiTheme.BADGE_BLUE_BG);
        infoBg.setCornerRadius(dp(8));
        infoBg.setStroke(dp(1), UiTheme.BORDER);
        infoCard.setBackground(infoBg);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        infoParams.bottomMargin = dp(16);
        infoCard.setLayoutParams(infoParams);

        TextView infoText = new TextView(activity);
        infoText.setText("Corrections are added as new entries. Earlier entries are never removed.");
        infoText.setTextColor(UiTheme.NAVY);
        infoText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        infoText.setTypeface(Typeface.DEFAULT_BOLD);
        infoCard.addView(infoText);
        rootLayout.addView(infoCard);

        // 3. Stage History Cards
        for (int i = 0; i < state.stageHistory.size(); i++) {
            StaffLifecycleData.StageHistoryEntry entry = state.stageHistory.get(i);
            rootLayout.addView(createHistoryCard(entry));
        }

        // 4. Back to Milestone and Outcomes Button
        Button backBtn = new Button(activity);
        backBtn.setText("Back to Milestone and Outcomes");
        backBtn.setTextColor(UiTheme.NAVY);
        backBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        backBtn.setTypeface(Typeface.DEFAULT_BOLD);
        backBtn.setAllCaps(false);
        GradientDrawable backBg = new GradientDrawable();
        backBg.setColor(UiTheme.WHITE);
        backBg.setCornerRadius(dp(8));
        backBg.setStroke(dp(1), UiTheme.BORDER);
        backBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22131c4b), backBg, null));
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        backParams.topMargin = dp(16);
        rootLayout.addView(backBtn, backParams);

        backBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onBackToMilestones();
            }
        });
    }

    private View createHistoryCard(StaffLifecycleData.StageHistoryEntry entry) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), UiTheme.BORDER);
        card.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(12);
        card.setLayoutParams(params);

        if (entry.isCorrection) {
            TextView corrBadge = new TextView(activity);
            corrBadge.setText("Correction");
            corrBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            corrBadge.setTypeface(Typeface.DEFAULT_BOLD);
            corrBadge.setTextColor(UiTheme.STATUS_CORRECTION_TEXT);
            corrBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(UiTheme.STATUS_CORRECTION_BG);
            badgeBg.setCornerRadius(dp(8));
            corrBadge.setBackground(badgeBg);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            badgeParams.bottomMargin = dp(6);
            card.addView(corrBadge, badgeParams);
        }

        TextView transView = new TextView(activity);
        transView.setText(entry.transition);
        transView.setTextColor(UiTheme.NAVY);
        transView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        transView.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(transView);

        TextView dateView = new TextView(activity);
        dateView.setText("Effective: " + entry.effectiveDate);
        dateView.setTextColor(UiTheme.MUTED);
        dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = dp(4);
        card.addView(dateView, dateParams);

        TextView detailsView = new TextView(activity);
        detailsView.setText("Supporting details: " + entry.supportingDetails);
        detailsView.setTextColor(UiTheme.INK);
        detailsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams detParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        detParams.topMargin = dp(2);
        card.addView(detailsView, detParams);

        if (entry.explanation != null && !entry.explanation.isEmpty()) {
            TextView explView = new TextView(activity);
            explView.setText(entry.explanation);
            explView.setTextColor(UiTheme.INK);
            explView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            LinearLayout.LayoutParams explParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            explParams.topMargin = dp(4);
            card.addView(explView, explParams);
        }

        TextView authorView = new TextView(activity);
        authorView.setText(entry.authorAndTimestamp);
        authorView.setTextColor(UiTheme.MUTED);
        authorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams authParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        authParams.topMargin = dp(6);
        card.addView(authorView, authParams);

        return card;
    }
}
