package ph.edu.umak.crm.app.staff.content;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.staff.StaffData;

/**
 * 5.7 Publication History Screen
 * Displays version publication, replacement, and withdrawal history for services and forms.
 */
public class PublicationHistoryView {

    public interface Callbacks {
        void onBack();
        void onNavigateToAccountAudit();
    }

    private final Context context;
    private final Callbacks callbacks;
    private final ScrollView rootScrollView;
    private final LinearLayout contentLayout;

    public PublicationHistoryView(Context context, Callbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;

        rootScrollView = new ScrollView(context);
        rootScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        rootScrollView.setBackgroundColor(UiTheme.PAPER);

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));
        rootScrollView.addView(contentLayout);

        buildView();
    }

    public View getContentLayout() {
        if (contentLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) contentLayout.getParent()).removeView(contentLayout);
        }
        return contentLayout;
    }

    private void buildView() {
        contentLayout.removeAllViews();
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        // Back button row
        if (callbacks != null) {
            TextView backBtn = new TextView(context);
            backBtn.setText("< Back to Manage");
            backBtn.setTextColor(UiTheme.ACCENT);
            backBtn.setTypeface(bold);
            ScreenTypography.apply(backBtn, 14);
            backBtn.setPadding(0, 0, 0, UiTheme.dp(context, 12));
            backBtn.setOnClickListener(v -> callbacks.onBack());
            contentLayout.addView(backBtn);
        }

        // Header Title
        TextView deptText = new TextView(context);
        deptText.setText("Center for Research Management");
        deptText.setTextColor(UiTheme.MUTED);
        deptText.setTypeface(regular);
        ScreenTypography.apply(deptText, 12);
        contentLayout.addView(deptText);

        TextView title = new TextView(context);
        title.setText("Publication History");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 22);
        title.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 2));
        contentLayout.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Services and approved form versions.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 13);
        subtitle.setPadding(0, 0, 0, UiTheme.dp(context, 12));
        contentLayout.addView(subtitle);

        // Account audit history link
        TextView auditLink = new TextView(context);
        auditLink.setText("Account audit history ->");
        auditLink.setTextColor(UiTheme.ACCENT);
        auditLink.setTypeface(bold);
        ScreenTypography.apply(auditLink, 14);
        auditLink.setPadding(0, 0, 0, UiTheme.dp(context, 16));
        auditLink.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onNavigateToAccountAudit();
            }
        });
        contentLayout.addView(auditLink);

        // Publication history cards
        List<StaffData.PublicationHistoryItem> list = StaffData.getPublicationHistory();
        for (StaffData.PublicationHistoryItem item : list) {
            contentLayout.addView(createHistoryCard(item));
            View spacer = new View(context);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 12)));
            contentLayout.addView(spacer);
        }
    }

    private View createHistoryCard(StaffData.PublicationHistoryItem item) {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 14), UiTheme.dp(context, 14),
                UiTheme.dp(context, 14), UiTheme.dp(context, 14));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(UiTheme.dp(context, 8));
        bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        card.setBackground(bg);

        // Top row: Title and Badge
        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(context);
        titleView.setText(item.serviceId + " · " + item.title + " · " + item.change);
        titleView.setTextColor(UiTheme.NAVY);
        titleView.setTypeface(bold);
        ScreenTypography.apply(titleView, 14);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        topRow.addView(titleView, titleLp);

        TextView badge = createStatusBadge(item.badge);
        topRow.addView(badge);
        card.addView(topRow);

        // Subtitle / Actor & Date
        TextView subView = new TextView(context);
        subView.setText(item.author + " · " + item.timestamp);
        subView.setTextColor(UiTheme.MUTED);
        subView.setTypeface(regular);
        ScreenTypography.apply(subView, 12);
        subView.setPadding(0, UiTheme.dp(context, 6), 0, 0);
        card.addView(subView);

        return card;
    }

    private TextView createStatusBadge(String actionType) {
        Typeface bold = UiTheme.boldFont(context);
        TextView badge = new TextView(context);
        badge.setText(actionType);
        badge.setTypeface(bold);
        ScreenTypography.apply(badge, 11);
        badge.setPadding(UiTheme.dp(context, 8), UiTheme.dp(context, 3),
                UiTheme.dp(context, 8), UiTheme.dp(context, 3));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(UiTheme.dp(context, 4));

        if ("Replaced".equalsIgnoreCase(actionType)) {
            bg.setColor(Color.parseColor("#EBF3FA")); // Light blue
            badge.setTextColor(Color.parseColor("#1B4D7E"));
        } else if ("Published".equalsIgnoreCase(actionType)) {
            bg.setColor(Color.parseColor("#E6F4EA")); // Light green
            badge.setTextColor(Color.parseColor("#137333"));
        } else if ("Withdrawn".equalsIgnoreCase(actionType)) {
            bg.setColor(Color.parseColor("#F1F3F4")); // Light gray
            badge.setTextColor(Color.parseColor("#5F6368"));
        } else {
            bg.setColor(UiTheme.PAPER);
            badge.setTextColor(UiTheme.NAVY);
        }

        badge.setBackground(bg);
        return badge;
    }
}
