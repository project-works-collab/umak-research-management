package ph.edu.umak.crm.app.requests;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
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
    private final Button markReadBtn;
    private final LinearLayout listContainer;
    private final LinearLayout emptyContainer;
    private boolean isEmptyPreview = false;

    public NotificationsView(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

        items.addAll(RequestData.getInitialNotifications());

        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        // Header Actions Row (Unread count + Mark read button)
        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        subtitle = new TextView(context);
        subtitle.setTextColor(0xff586b83);
        subtitle.setTypeface(bold);
        ScreenTypography.apply(subtitle, 13);
        actions.addView(subtitle, new LinearLayout.LayoutParams(0, -2, 1));

        markReadBtn = new Button(context);
        markReadBtn.setText("Mark read");
        markReadBtn.setTextColor(0xff185d9d);
        markReadBtn.setTypeface(bold);
        ScreenTypography.apply(markReadBtn, 13);
        markReadBtn.setBackgroundColor(0);
        markReadBtn.setPadding(UiTheme.dp(context, 8), 0, UiTheme.dp(context, 8), 0);
        markReadBtn.setOnClickListener(v -> markAllRead());
        actions.addView(markReadBtn);

        layout.addView(actions);

        addSpacing(context, layout, 12);

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
            markReadBtn.setVisibility(View.GONE);
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
        markReadBtn.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);

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
        titleView.setTextColor(0xff082d50);
        titleView.setTypeface(item.unread ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        ScreenTypography.apply(titleView, 14);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        topRow.addView(titleView, tLp);

        if (item.unread) {
            TextView unreadDot = new TextView(context);
            unreadDot.setText("New");
            unreadDot.setTextColor(0xffffffff);
            unreadDot.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(unreadDot, 10);
            unreadDot.setBackground(UiTheme.shape(context, 0xff185d9d, 10, 0));
            unreadDot.setPadding(UiTheme.dp(context, 6), UiTheme.dp(context, 2), UiTheme.dp(context, 6), UiTheme.dp(context, 2));
            topRow.addView(unreadDot);
        }

        card.addView(topRow);

        addSpacing(context, card, 4);

        TextView reqCode = new TextView(context);
        reqCode.setText("Request: " + item.requestCode);
        reqCode.setTextColor(0xff586b83);
        reqCode.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(reqCode, 12);
        card.addView(reqCode);

        addSpacing(context, card, 2);

        TextView dateView = new TextView(context);
        dateView.setText(item.date);
        dateView.setTextColor(0xff586b83);
        dateView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(dateView, 11);
        card.addView(dateView);

        card.setFocusable(true);
        card.setOnClickListener(v -> {
            item.unread = false;
            renderList();
            if (listener != null) listener.onNotificationClick(item);
        });

        return card;
    }

    private void renderEmpty() {
        emptyContainer.setPadding(0, UiTheme.dp(context, 48), 0, UiTheme.dp(context, 48));

        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.ic_crm_info);
        icon.setColorFilter(0xff586b83);
        int icon32 = UiTheme.dp(context, 32);
        emptyContainer.addView(icon, new LinearLayout.LayoutParams(icon32, icon32));

        addSpacing(context, emptyContainer, 12);

        TextView emptyTitle = new TextView(context);
        emptyTitle.setText("No notifications yet");
        emptyTitle.setTextColor(0xff082d50);
        emptyTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(emptyTitle, 18);
        emptyTitle.setGravity(Gravity.CENTER);
        emptyContainer.addView(emptyTitle);

        addSpacing(context, emptyContainer, 6);

        TextView emptySubtitle = new TextView(context);
        emptySubtitle.setText("Updates about your requests, approvals, and deadlines will appear here.");
        emptySubtitle.setTextColor(0xff586b83);
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
        return PageTitle.wrap(layout, "Notifications", null, false);
    }
}
