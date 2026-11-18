package ph.edu.umak.crm.app.staff.accounts;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
 * 5.13 Account Audit History Screen
 * Displays immutable audit log for account updates, category changes, credential issuance, and status changes.
 */
public class AccountAuditHistoryView {

    public interface Callbacks {
        void onBack();
    }

    private final Context context;
    private final Callbacks callbacks;
    private final ScrollView rootScrollView;
    private final LinearLayout contentLayout;

    private String userDisplayName = "LeBron R. James";
    private String userIdentifier = "A1234";

    public AccountAuditHistoryView(Context context, Callbacks callbacks) {
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

    public void setAccount(String name, String identifier) {
        if (name != null) this.userDisplayName = name;
        if (identifier != null) this.userIdentifier = identifier;
        buildView();
    }

    private void buildView() {
        contentLayout.removeAllViews();
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        // Back button row
        if (callbacks != null) {
            TextView backBtn = new TextView(context);
            backBtn.setText("< Back to Account");
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
        title.setText("Account Audit History");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 22);
        title.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 2));
        contentLayout.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText(userDisplayName + " · " + userIdentifier);
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(bold);
        ScreenTypography.apply(subtitle, 14);
        subtitle.setPadding(0, 0, 0, UiTheme.dp(context, 16));
        contentLayout.addView(subtitle);

        // Logs
        List<StaffData.AccountAuditLogItem> logs = StaffData.getAccountAuditLogs();
        for (StaffData.AccountAuditLogItem log : logs) {
            contentLayout.addView(createLogCard(log));
            View spacer = new View(context);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, 12)));
            contentLayout.addView(spacer);
        }
    }

    private View createLogCard(StaffData.AccountAuditLogItem log) {
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

        // Action title
        TextView actionView = new TextView(context);
        actionView.setText(log.action);
        actionView.setTextColor(UiTheme.NAVY);
        actionView.setTypeface(bold);
        ScreenTypography.apply(actionView, 14);
        card.addView(actionView);

        // Previous -> New
        TextView diffView = new TextView(context);
        diffView.setText("Previous: " + log.previous + " -> New: " + log.next);
        diffView.setTextColor(UiTheme.NAVY);
        diffView.setTypeface(regular);
        ScreenTypography.apply(diffView, 13);
        diffView.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        card.addView(diffView);

        // Reason (if present)
        if (log.reason != null && !log.reason.isEmpty()) {
            TextView reasonView = new TextView(context);
            reasonView.setText("Reason: " + log.reason);
            reasonView.setTextColor(UiTheme.MUTED);
            reasonView.setTypeface(regular);
            ScreenTypography.apply(reasonView, 12);
            reasonView.setPadding(0, UiTheme.dp(context, 2), 0, 0);
            card.addView(reasonView);
        }

        // Actor and Timestamp
        TextView actorView = new TextView(context);
        actorView.setText(log.actor + " · " + log.timestamp);
        actorView.setTextColor(UiTheme.MUTED);
        actorView.setTypeface(regular);
        ScreenTypography.apply(actorView, 12);
        actorView.setPadding(0, UiTheme.dp(context, 6), 0, 0);
        card.addView(actorView);

        return card;
    }
}
