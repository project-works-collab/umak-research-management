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


import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Research Stage History View (Wireframe 8.3).
 * Chronological stage transitions and immutable corrections log timeline.
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
        return PageTitle.wrap(rootLayout, "Stage History", state.title, false);
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

        // 1. Top Summary Card
        rootLayout.addView(createSummaryCard());

        // 2. Timeline Card
        rootLayout.addView(createTimelineCard());

        // 3. Back to Milestone and Outcomes Button
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
        backParams.topMargin = dp(8);
        rootLayout.addView(backBtn, backParams);

        backBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onBackToMilestones();
            }
        });
    }

    private View createSummaryCard() {
        LinearLayout card = createBaseCard();

        TextView idText = new TextView(activity);
        idText.setText(state.recordId);
        idText.setTextColor(0xff0284c7);
        idText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        idText.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(idText);

        LinearLayout stageRow = new LinearLayout(activity);
        stageRow.setOrientation(LinearLayout.HORIZONTAL);
        stageRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams stageRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stageRowParams.topMargin = dp(4);
        stageRowParams.bottomMargin = dp(6);
        stageRow.setLayoutParams(stageRowParams);

        TextView stageLabel = new TextView(activity);
        stageLabel.setText("Current overall stage:   ");
        stageLabel.setTextColor(UiTheme.NAVY);
        stageLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        stageLabel.setTypeface(Typeface.DEFAULT_BOLD);
        stageRow.addView(stageLabel);

        TextView stageBadge = new TextView(activity);
        stageBadge.setText(state.currentStage);
        stageBadge.setTextColor(0xff1d4ed8);
        stageBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        stageBadge.setTypeface(Typeface.DEFAULT_BOLD);
        stageBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(0xffeff6ff);
        badgeBg.setCornerRadius(dp(12));
        badgeBg.setStroke(dp(1), 0xffbfdbfe);
        stageBadge.setBackground(badgeBg);
        stageRow.addView(stageBadge);

        card.addView(stageRow);

        TextView infoText = new TextView(activity);
        infoText.setText("Corrections are added as new entries. Earlier entries are never removed.");
        infoText.setTextColor(0xff64748b);
        infoText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        infoParams.topMargin = dp(2);
        card.addView(infoText, infoParams);

        return card;
    }

    private View createTimelineCard() {
        LinearLayout card = createBaseCard();

        for (int i = 0; i < state.stageHistory.size(); i++) {
            StaffLifecycleData.StageHistoryEntry entry = state.stageHistory.get(i);
            boolean isLast = (i == state.stageHistory.size() - 1);
            card.addView(createTimelineRow(entry, isLast));
        }

        return card;
    }

    private View createTimelineRow(StaffLifecycleData.StageHistoryEntry entry, boolean isLast) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);

        // Left: Indicator column (dot + connector line)
        LinearLayout indicatorCol = new LinearLayout(activity);
        indicatorCol.setOrientation(LinearLayout.VERTICAL);
        indicatorCol.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams indParams = new LinearLayout.LayoutParams(dp(24), ViewGroup.LayoutParams.MATCH_PARENT);
        indParams.setMarginEnd(dp(12));
        indicatorCol.setLayoutParams(indParams);

        View dot = new View(activity);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        if (entry.isCorrection) {
            dotBg.setColor(UiTheme.WHITE);
            dotBg.setStroke(dp(3), 0xfff59e0b);
        } else {
            dotBg.setColor(0xff2563eb);
        }
        dot.setBackground(dotBg);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(12), dp(12));
        dotParams.topMargin = dp(4);
        indicatorCol.addView(dot, dotParams);

        if (!isLast) {
            View line = new View(activity);
            line.setBackgroundColor(0xffcbd5e1);
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(dp(2), 0, 1);
            lineParams.topMargin = dp(4);
            lineParams.bottomMargin = dp(4);
            indicatorCol.addView(line, lineParams);
        }

        row.addView(indicatorCol);

        // Right: Content column
        LinearLayout contentCol = new LinearLayout(activity);
        contentCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        contentParams.bottomMargin = isLast ? dp(4) : dp(20);
        contentCol.setLayoutParams(contentParams);

        if (entry.isCorrection) {
            TextView corrBadge = new TextView(activity);
            corrBadge.setText("Correction");
            corrBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            corrBadge.setTypeface(Typeface.DEFAULT_BOLD);
            corrBadge.setTextColor(0xffb45309);
            corrBadge.setPadding(dp(8), dp(2), dp(8), dp(2));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(0xfffef3c7);
            badgeBg.setCornerRadius(dp(8));
            corrBadge.setBackground(badgeBg);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            badgeParams.bottomMargin = dp(4);
            contentCol.addView(corrBadge, badgeParams);
        }

        TextView transView = new TextView(activity);
        transView.setText(entry.transition);
        transView.setTextColor(UiTheme.NAVY);
        transView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        transView.setTypeface(Typeface.DEFAULT_BOLD);
        contentCol.addView(transView);

        TextView dateView = new TextView(activity);
        dateView.setText("Effective: " + entry.effectiveDate);
        dateView.setTextColor(0xff64748b);
        dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = dp(2);
        contentCol.addView(dateView, dateParams);

        TextView detailsView = new TextView(activity);
        detailsView.setText("Supporting details: " + entry.supportingDetails);
        detailsView.setTextColor(UiTheme.INK);
        detailsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams detParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        detParams.topMargin = dp(2);
        contentCol.addView(detailsView, detParams);

        if (entry.explanation != null && !entry.explanation.isEmpty()) {
            TextView explView = new TextView(activity);
            explView.setText(entry.explanation);
            explView.setTextColor(0xffc2410c);
            explView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            LinearLayout.LayoutParams explParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            explParams.topMargin = dp(2);
            contentCol.addView(explView, explParams);
        }

        TextView authorView = new TextView(activity);
        authorView.setText(entry.authorAndTimestamp);
        authorView.setTextColor(0xff64748b);
        authorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        LinearLayout.LayoutParams authParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        authParams.topMargin = dp(4);
        contentCol.addView(authorView, authParams);

        row.addView(contentCol);
        return row;
    }

    private LinearLayout createBaseCard() {
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
        return card;
    }
}
