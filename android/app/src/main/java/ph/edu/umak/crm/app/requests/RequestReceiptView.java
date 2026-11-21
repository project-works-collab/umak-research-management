package ph.edu.umak.crm.app.requests;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
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

        // 3.5 Top Success Badge: Mint green circle with green checkmark
        FrameLayout iconBadge = new FrameLayout(context);
        int badgeSize = UiTheme.dp(context, 54);
        iconBadge.setBackground(UiTheme.shape(context, 0xffdcfce7, badgeSize / 2, 0));
        ImageView checkIcon = new ImageView(context);
        checkIcon.setImageResource(R.drawable.ic_crm_check_small);
        checkIcon.setColorFilter(0xff16a34a);
        int checkSize = UiTheme.dp(context, 26);
        FrameLayout.LayoutParams checkLp = new FrameLayout.LayoutParams(checkSize, checkSize, Gravity.CENTER);
        iconBadge.addView(checkIcon, checkLp);

        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(badgeSize, badgeSize);
        badgeParams.gravity = Gravity.CENTER_HORIZONTAL;
        badgeParams.topMargin = UiTheme.dp(context, 20);
        badgeParams.bottomMargin = UiTheme.dp(context, 12);
        layout.addView(iconBadge, badgeParams);

        // Screen Title
        TextView title = new TextView(context);
        if (item != null && item.isPreviewSubmission()) title.setText("Preview request created");
        else title.setText(isCorrection ? "Correction submitted" : "Request submitted");
        title.setTextColor(0xff082d50);
        title.setTypeface(UiTheme.headingFont(context));
        title.setGravity(Gravity.CENTER);
        ScreenTypography.heading(title);
        ScreenTypography.apply(title, 22);
        title.setPadding(0, 0, 0, UiTheme.dp(context, 20));
        layout.addView(title);

        // Receipt Card
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        card.setPadding(pad, pad, pad, pad);

        String reqId = item != null ? item.id : "REQ-2026-089";
        String service = item != null ? item.service : "Ethics Clearance";
        String timeStr = isCorrection && (item == null || item.serviceRequestId == 0)
                ? "Nov 3, 2026, 2:15 PM" : (item != null ? item.submittedDate : "Oct 29, 2026, 10:42 AM");
        String status = "Submitted";

        addReceiptRow(card, "Request ID", reqId, bold, 0xff185d9d);
        addReceiptRow(card, "Service", service, bold, 0xff082d50);
        addReceiptRow(card, isCorrection ? "Resubmitted" : "Submitted", timeStr, bold, 0xff082d50);

        // Status row with badge
        LinearLayout statusRow = new LinearLayout(context);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);
        statusRow.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 4));

        TextView statusLabel = new TextView(context);
        statusLabel.setText("Status");
        statusLabel.setTextColor(0xff586b83);
        statusLabel.setTypeface(regular);
        ScreenTypography.apply(statusLabel, 14);
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        statusRow.addView(statusLabel, lblLp);

        TextView badge = createStatusBadge(context, status, 0xffe5edf7, 0xff185d9d);
        statusRow.addView(badge);
        card.addView(statusRow);

        layout.addView(card);

        if (isCorrection) {
            addSpacing(context, layout, 12);
            TextView note = new TextView(context);
            note.setText("Earlier reviews stay in your request history.");
            note.setTextColor(0xff586b83);
            note.setTypeface(regular);
            ScreenTypography.apply(note, 13);
            layout.addView(note);
        }
        addSpacing(context, layout, 24);

        // Action Buttons
        Button viewRequestBtn = new Button(context);
        viewRequestBtn.setText("View Request");
        viewRequestBtn.setTextColor(0xffffffff);
        viewRequestBtn.setTypeface(bold);
        ScreenTypography.apply(viewRequestBtn, 15);
        viewRequestBtn.setBackground(UiTheme.shape(context, 0xff131c4b, 8, 0));
        viewRequestBtn.setOnClickListener(v -> {
            if (listener != null) listener.onViewRequest(reqId);
        });
        layout.addView(viewRequestBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));

        addSpacing(context, layout, 12);

        Button backBtn = new Button(context);
        backBtn.setText("Back to Requests");
        backBtn.setTextColor(0xff082d50);
        backBtn.setTypeface(bold);
        ScreenTypography.apply(backBtn, 15);
        backBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1));
        backBtn.setOnClickListener(v -> {
            if (listener != null) listener.onBackToRequests();
        });
        layout.addView(backBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));
    }

    private void addReceiptRow(LinearLayout parent, String label, String value, Typeface valTypeface, int valColor) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 8));

        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(0xff586b83);
        lbl.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(lbl, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(lbl, lp);

        TextView val = new TextView(context);
        val.setText(value);
        val.setTextColor(valColor);
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
        int px = UiTheme.dp(context, 10);
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
