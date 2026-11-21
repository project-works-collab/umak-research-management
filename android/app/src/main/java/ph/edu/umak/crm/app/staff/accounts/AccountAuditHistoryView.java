package ph.edu.umak.crm.app.staff.accounts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ph.edu.umak.crm.app.R;
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
    private final LinearLayout contentLayout;

    private java.util.List<StaffData.AccountAuditLogItem> liveLogs;
    public void bindLive(String name,String identifier,java.util.List<StaffData.AccountAuditLogItem> logs){liveLogs=logs;setAccount(name,identifier);}
    private String userDisplayName = "LeBron R. James";
    private String userIdentifier = "A1234";

    public AccountAuditHistoryView(Context context, Callbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setBackgroundColor(UiTheme.PAPER);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));

        buildView();
    }

    public Callbacks getCallbacks() {
        return callbacks;
    }

    public View getContentLayout() {
        if (contentLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) contentLayout.getParent()).removeView(contentLayout);
        }

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        // Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(context);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = UiTheme.dp(context, 16);
        band.setPadding(pad, pad, pad, pad);

        TextView heading = new TextView(context);
        heading.setText("Account Audit History");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        TextView copy = new TextView(context);
        copy.setText(userDisplayName + " · " + userIdentifier);
        copy.setTextColor(UiTheme.PALE);
        copy.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(copy, 13);
        band.addView(copy);

        root.addView(band);
        root.addView(contentLayout);
        return root;
    }

    public void setAccount(String name, String identifier) {
        if (name != null) this.userDisplayName = name;
        if (identifier != null) this.userIdentifier = identifier;
        buildView();
    }

    private void buildView() {
        contentLayout.removeAllViews();

        // Logs
        List<StaffData.AccountAuditLogItem> logs = liveLogs==null?StaffData.getAccountAuditLogs():liveLogs;
        for (StaffData.AccountAuditLogItem log : logs) {
            contentLayout.addView(createLogCard(log));
            View spacer = new View(context);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 10)));
            contentLayout.addView(spacer);
        }
    }

    private View createLogCard(StaffData.AccountAuditLogItem log) {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.TOP);
        card.setPadding(UiTheme.dp(context, 14), UiTheme.dp(context, 14),
                UiTheme.dp(context, 14), UiTheme.dp(context, 14));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(UiTheme.dp(context, 8));
        bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        card.setBackground(bg);

        // Circular Icon Avatar on Left
        ImageView iconView = new ImageView(context);
        int iconRes;
        int iconColor;
        if (log.action.contains("category")) {
            iconRes = R.drawable.ic_crm_user_single;
            iconColor = 0xff082d50;
        } else if (log.action.contains("credential")) {
            iconRes = R.drawable.ic_crm_key;
            iconColor = 0xff082d50;
        } else if (log.action.contains("activated") && !log.action.contains("deactivated")) {
            iconRes = R.drawable.ic_crm_check_small;
            iconColor = 0xff16a34a;
        } else {
            iconRes = R.drawable.ic_crm_lock;
            iconColor = 0xff64748b;
        }
        iconView.setImageResource(iconRes);
        iconView.setColorFilter(iconColor);
        int iconPad = UiTheme.dp(context, 8);
        iconView.setPadding(iconPad, iconPad, iconPad, iconPad);

        int aSize = UiTheme.dp(context, 36);
        LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(aSize, aSize);
        iLp.rightMargin = UiTheme.dp(context, 12);
        iconView.setLayoutParams(iLp);

        GradientDrawable aBg = new GradientDrawable();
        aBg.setShape(GradientDrawable.OVAL);
        aBg.setColor(Color.parseColor("#F1F5F9"));
        iconView.setBackground(aBg);
        card.addView(iconView);

        // Content Column on Right
        LinearLayout col = new LinearLayout(context);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

        // Action title
        TextView actionView = new TextView(context);
        actionView.setText(log.action);
        actionView.setTextColor(0xff082d50);
        actionView.setTypeface(bold);
        ScreenTypography.apply(actionView, 14);
        col.addView(actionView);

        // Previous -> New
        TextView diffView = new TextView(context);
        SpannableStringBuilder diffText = new SpannableStringBuilder();
        diffText.append("Previous: ");
        int prevStart = diffText.length();
        diffText.append(log.previous);
        diffText.setSpan(new StyleSpan(Typeface.BOLD), prevStart, diffText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        diffText.append(" → New: ");
        int nextStart = diffText.length();
        diffText.append(log.next);
        diffText.setSpan(new StyleSpan(Typeface.BOLD), nextStart, diffText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        diffView.setText(diffText);
        diffView.setTextColor(0xff082d50);
        diffView.setTypeface(regular);
        ScreenTypography.apply(diffView, 13);
        diffView.setPadding(0, UiTheme.dp(context, 3), 0, 0);
        col.addView(diffView);

        // Reason (if present)
        if (log.reason != null && !log.reason.isEmpty()) {
            TextView reasonView = new TextView(context);
            reasonView.setText("Reason: " + log.reason);
            reasonView.setTextColor(0xff586b83);
            reasonView.setTypeface(regular);
            ScreenTypography.apply(reasonView, 12);
            reasonView.setPadding(0, UiTheme.dp(context, 3), 0, 0);
            col.addView(reasonView);
        }

        // Actor and Timestamp
        TextView actorView = new TextView(context);
        actorView.setText(log.actor + " · " + log.timestamp);
        actorView.setTextColor(0xff64748b);
        actorView.setTypeface(regular);
        ScreenTypography.apply(actorView, 12);
        actorView.setPadding(0, UiTheme.dp(context, 5), 0, 0);
        col.addView(actorView);

        card.addView(col, colLp);
        return card;
    }
}
