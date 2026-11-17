package ph.edu.umak.crm.app.requests;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class NotificationsView {

    public interface Listener {
        void onBack();
        void onNotificationClick(RequestData.NotificationItem item);
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;

    private final List<RequestData.NotificationItem> items = new ArrayList<>();
    private final TextView subtitle;
    private final LinearLayout listContainer;
    private final LinearLayout emptyContainer;
    private boolean isEmptyPreview = false;

    public NotificationsView(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

        items.addAll(RequestData.getInitialNotifications());

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        // Header Row: Back button on left
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        Button backBtn = UiButton.text(context, "Back", UiTheme.NAVY, () -> {
            if (listener != null) listener.onBack();
        });
        backBtn.setPadding(0, 0, UiTheme.dp(context, 12), 0);
        headerRow.addView(backBtn);

        LinearLayout titleCol = new LinearLayout(context);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView title = new TextView(context);
        title.setText("Notifications");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 20);
        titleCol.addView(title);

        subtitle = new TextView(context);
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 13);
        titleCol.addView(subtitle);

        headerRow.addView(titleCol, tLp);

        Button markReadBtn = UiButton.text(context, "Mark read", UiTheme.ACCENT, this::markAllRead);
        headerRow.addView(markReadBtn);

        layout.addView(headerRow);

        addSpacing(context, layout, 16);

        // List container
        listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(listContainer);

        // Empty container
        emptyContainer = new LinearLayout(context);
        emptyContainer.setOrientation(LinearLayout.VERTICAL);
        emptyContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyContainer.setVisibility(View.GONE);
        layout.addView(emptyContainer);

        renderList();
    }

    private void markAllRead() {
        for (RequestData.NotificationItem item : items) {
            item.unread = false;
        }
        renderList();
    }

    public void setPreviewState(String state) {
        if ("3.9_empty".equalsIgnoreCase(state)) {
            isEmptyPreview = true;
        } else {
            isEmptyPreview = false;
            items.clear();
            items.addAll(RequestData.getInitialNotifications());
        }
        renderList();
    }

    private void renderList() {
        listContainer.removeAllViews();
        emptyContainer.removeAllViews();

        if (isEmptyPreview) {
            subtitle.setText("0 unread");
            listContainer.setVisibility(View.GONE);
            emptyContainer.setVisibility(View.VISIBLE);
            renderEmpty();
            return;
        }

        int unreadCount = 0;
        for (RequestData.NotificationItem it : items) {
            if (it.unread) unreadCount++;
        }
        subtitle.setText(unreadCount + " unread");

        listContainer.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);

        for (RequestData.NotificationItem item : items) {
            View row = createNotificationCard(item);
            listContainer.addView(row);
            addSpacing(context, listContainer, 10);
        }
    }

    private View createNotificationCard(RequestData.NotificationItem item) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 14);
        card.setPadding(pad, pad, pad, pad);

        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(context);
        titleView.setText(item.title);
        titleView.setTextColor(UiTheme.NAVY);
        titleView.setTypeface(item.unread ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        ScreenTypography.apply(titleView, 14);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        topRow.addView(titleView, tLp);

        if (item.unread) {
            TextView unreadDot = new TextView(context);
            unreadDot.setText("New");
            unreadDot.setTextColor(UiTheme.WHITE);
            unreadDot.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(unreadDot, 10);
            unreadDot.setBackground(UiTheme.shape(context, UiTheme.ACCENT, 10, 0));
            unreadDot.setPadding(UiTheme.dp(context, 6), UiTheme.dp(context, 2), UiTheme.dp(context, 6), UiTheme.dp(context, 2));
            topRow.addView(unreadDot);
        }

        card.addView(topRow);

        addSpacing(context, card, 4);

        TextView reqCode = new TextView(context);
        reqCode.setText("Request: " + item.requestCode);
        reqCode.setTextColor(UiTheme.MUTED);
        reqCode.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(reqCode, 12);
        card.addView(reqCode);

        addSpacing(context, card, 2);

        TextView dateView = new TextView(context);
        dateView.setText(item.date);
        dateView.setTextColor(UiTheme.MUTED);
        dateView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(dateView, 11);
        card.addView(dateView);

        card.setOnClickListener(v -> {
            item.unread = false;
            renderList();
            if (listener != null) listener.onNotificationClick(item);
        });

        return card;
    }

    private void renderEmpty() {
        emptyContainer.setPadding(0, UiTheme.dp(context, 48), 0, UiTheme.dp(context, 48));

        TextView emptyTitle = new TextView(context);
        emptyTitle.setText("No notifications yet");
        emptyTitle.setTextColor(UiTheme.NAVY);
        emptyTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(emptyTitle, 18);
        emptyTitle.setGravity(Gravity.CENTER);
        emptyContainer.addView(emptyTitle);

        addSpacing(context, emptyContainer, 6);

        TextView emptySubtitle = new TextView(context);
        emptySubtitle.setText("Updates about your requests, approvals, and deadlines will appear here.");
        emptySubtitle.setTextColor(UiTheme.MUTED);
        emptySubtitle.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(emptySubtitle, 14);
        emptySubtitle.setGravity(Gravity.CENTER);
        emptyContainer.addView(emptySubtitle);
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
