package ph.edu.umak.crm.app.requests;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class RequestReceiptView {

    public interface Listener {
        void onViewRequest(String requestId);
        void onBackToRequests();
    }

    private final Context context;
    private final LinearLayout layout;

    public RequestReceiptView(Context context, Listener listener, boolean isCorrection, RequestData.RequestItem item) {
        this.context = context;

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, pad);

        // Institution Header
        TextView institution = new TextView(context);
        institution.setText("Center for Research Management");
        institution.setTextColor(UiTheme.MUTED);
        institution.setTypeface(regular);
        ScreenTypography.apply(institution, 12);
        layout.addView(institution);

        // Screen Title
        TextView title = new TextView(context);
        title.setText(isCorrection ? "Correction submitted" : "Request submitted");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 22);
        title.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));
        layout.addView(title);

        // Subtitle
        TextView subtitle = new TextView(context);
        subtitle.setText(isCorrection
                ? "Your updated documents have been submitted to CRM."
                : "Your request has been submitted to CRM. We will review your request and post updates here.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 14);
        layout.addView(subtitle);

        addSpacing(context, layout, 16);

        // Receipt Card
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        card.setPadding(pad, pad, pad, pad);

        String reqId = item != null ? item.id : "REQ-2026-089";
        String service = item != null ? item.service : "Ethics Clearance";
        String timeStr = isCorrection ? "Nov 3, 2026, 2:15 PM" : (item != null ? item.submittedDate : "Oct 29, 2026, 10:42 AM");
        String status = "Submitted";

        addReceiptRow(card, "Request ID", reqId, bold);
        addReceiptRow(card, "Service", service, bold);
        addReceiptRow(card, isCorrection ? "Resubmitted" : "Submitted", timeStr, regular);

        // Status row with badge
        LinearLayout statusRow = new LinearLayout(context);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);
        statusRow.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

        TextView statusLabel = new TextView(context);
        statusLabel.setText("Status");
        statusLabel.setTextColor(UiTheme.MUTED);
        statusLabel.setTypeface(regular);
        ScreenTypography.apply(statusLabel, 14);
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        statusRow.addView(statusLabel, lblLp);

        TextView badge = createStatusBadge(context, status, UiTheme.STATUS_SUBMITTED_BG, UiTheme.STATUS_SUBMITTED_TEXT);
        statusRow.addView(badge);
        card.addView(statusRow);

        addSpacing(context, card, 12);

        // Explanatory note
        TextView note = new TextView(context);
        note.setText(isCorrection
                ? "Earlier reviews stay in your request history. CRM will notify you when review continues."
                : "Save your request ID for reference. You can view progress and upload more documents at any time from My Requests.");
        note.setTextColor(UiTheme.MUTED);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 13);
        note.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 10), UiTheme.dp(context, 10), UiTheme.dp(context, 10));
        note.setBackground(UiTheme.shape(context, 0xFFF1F5F9, 6, 0));
        card.addView(note);

        layout.addView(card);

        addSpacing(context, layout, 24);

        // Action Buttons
        Button viewRequestBtn = UiButton.primary(context, "View Request", () -> {
            if (listener != null) listener.onViewRequest(reqId);
        });
        layout.addView(viewRequestBtn);

        addSpacing(context, layout, 12);

        Button backBtn = UiButton.outline(context, "Back to Requests", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (listener != null) listener.onBackToRequests();
        });
        layout.addView(backBtn);
    }

    private void addReceiptRow(LinearLayout parent, String label, String value, Typeface valTypeface) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(UiTheme.MUTED);
        lbl.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(lbl, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(lbl, lp);

        TextView val = new TextView(context);
        val.setText(value);
        val.setTextColor(UiTheme.NAVY);
        val.setTypeface(valTypeface);
        ScreenTypography.apply(val, 14);
        row.addView(val);

        parent.addView(row);
    }

    private static TextView createStatusBadge(Context context, String text, int bg, int fg) {
        TextView badge = new TextView(context);
        badge.setText(text);
        badge.setTextColor(fg);
        badge.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(badge, 12);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 4);
        badge.setPadding(px, py, px, py);
        badge.setBackground(UiTheme.shape(context, bg, 4, 0));
        return badge;
    }

    private static void addSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(space);
    }

    public View getContentLayout() {
        if (layout.getParent() instanceof ViewGroup) {
            ((ViewGroup) layout.getParent()).removeView(layout);
        }
        return layout;
    }
}
