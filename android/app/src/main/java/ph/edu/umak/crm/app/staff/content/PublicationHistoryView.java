package ph.edu.umak.crm.app.staff.content;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
    private final LinearLayout contentLayout;

    public PublicationHistoryView(Context context, Callbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setBackgroundColor(UiTheme.PAPER);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));

        buildView();
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
        heading.setText("Publication History");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        TextView copy = new TextView(context);
        copy.setText("Services and approved form versions.");
        copy.setTextColor(UiTheme.PALE);
        copy.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(copy, 13);
        band.addView(copy);

        root.addView(band);
        root.addView(contentLayout);
        return root;
    }

    private void buildView() {
        contentLayout.removeAllViews();
        Typeface bold = UiTheme.boldFont(context);

        // Top Row: Right-aligned "Account audit history ->"
        FrameLayout topLinkContainer = new FrameLayout(context);
        topLinkContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView auditLink = new TextView(context);
        auditLink.setText("Account audit history →");
        auditLink.setTextColor(0xff185d9d);
        auditLink.setTypeface(bold);
        ScreenTypography.apply(auditLink, 13);
        auditLink.setPadding(0, 0, 0, UiTheme.dp(context, 12));
        auditLink.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onNavigateToAccountAudit();
            }
        });
        FrameLayout.LayoutParams aLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        aLp.gravity = Gravity.END;
        topLinkContainer.addView(auditLink, aLp);

        contentLayout.addView(topLinkContainer);

        // Publication history cards
        List<StaffData.PublicationHistoryItem> list = StaffData.getPublicationHistory();
        for (StaffData.PublicationHistoryItem item : list) {
            contentLayout.addView(createHistoryCard(item));
            View spacer = new View(context);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 10)));
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
        titleView.setText(item.serviceId.toLowerCase() + " · " + item.title + " · " + item.change);
        titleView.setTextColor(0xff082d50);
        titleView.setTypeface(bold);
        ScreenTypography.apply(titleView, 14);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        titleLp.rightMargin = UiTheme.dp(context, 8);
        topRow.addView(titleView, titleLp);

        TextView badge = createStatusBadge(item.badge);
        topRow.addView(badge);
        card.addView(topRow);

        // Subtitle / Actor & Date
        TextView subView = new TextView(context);
        subView.setText(item.author + " · " + item.timestamp);
        subView.setTextColor(0xff586b83);
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
            bg.setColor(Color.parseColor("#E0F2FE")); // Light blue
            badge.setTextColor(Color.parseColor("#0284C7"));
        } else if ("Published".equalsIgnoreCase(actionType)) {
            bg.setColor(Color.parseColor("#DCFCE7")); // Light green
            badge.setTextColor(Color.parseColor("#16A34A"));
        } else if ("Withdrawn".equalsIgnoreCase(actionType)) {
            bg.setColor(Color.parseColor("#FEE2E2")); // Light red
            badge.setTextColor(Color.parseColor("#DC2626"));
        } else {
            bg.setColor(UiTheme.PAPER);
            badge.setTextColor(0xff082d50);
        }

        badge.setBackground(bg);
        return badge;
    }
}
