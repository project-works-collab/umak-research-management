package ph.edu.umak.crm.app;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.FrameLayout;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import android.text.InputFilter;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

final class StaffScreen extends LinearLayout {
    private static final int NAVY = 0xff141e4d;
    private static final int BLUE = 0xff092d4d;
    private static final int MUTED = 0xff51627b;
    private static final int BACKGROUND = 0xfff5f6f8;
    private static final int WHITE = 0xffffffff;
    private static final int ACCENT = 0xff195c99;
    private static final int BORDER = 0xffe0e3e8;
    private static final int PALE_BLUE = 0xffe5edf6;
    private static final int YELLOW = 0xfff7eb37;
    private static final int[] TAB_ICONS = {
            R.drawable.ic_staff_dashboard, R.drawable.ic_staff_requests, R.drawable.ic_staff_registry,
            R.drawable.ic_staff_manage, R.drawable.ic_staff_profile
    };
    private static final String[] REQUEST_LABELS = {"Pending", "Under Review", "Completed"};
    private static final String[] REQUEST_STATES = {"queued", "claimed", "resolved"};
    private static final String[] TABS = {"Dashboard", "Requests", "Registry", "Manage", "Profile"};

    private final MainActivity activity;
    private final String email;
    private final Typeface headingFont;
    private final Typeface bodyFont;
    private final Typeface actionFont;
    private final LinearLayout content;
    private final LinearLayout navigation;
    private final ScrollView scroll;
    private String tab;
    private JSONArray consultations;
    private String consultationMessage;
    private LinearLayout requestResults;
    private int requestFilter;
    private String requestQuery = "";
    private String searchKeyword = "";
    private int queryId;

    StaffScreen(MainActivity activity, String email, String initialTab) {
        super(activity);
        this.activity = activity;
        this.email = email;
        headingFont = getResources().getFont(R.font.marcellus_regular);
        bodyFont = getResources().getFont(R.font.metropolis_regular);
        actionFont = getResources().getFont(R.font.metropolis_semibold);
        setOrientation(VERTICAL);
        setFocusableInTouchMode(true);
        setBackgroundColor(BACKGROUND);
        TextView header = text("Center for Research Management", 18, WHITE, headingFont);
        header.setPadding(dp(16), dp(14), dp(16), dp(14));
        header.setBackgroundColor(NAVY);
        addView(header);

        scroll = new ScrollView(activity);
        content = new LinearLayout(activity);
        content.setOrientation(VERTICAL);
        scroll.addView(content);
        addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));

        navigation = new LinearLayout(activity);
        navigation.setBackgroundColor(WHITE);
        navigation.setPadding(dp(4), dp(4), dp(4), dp(4));
        content.setPadding(0, 0, 0, dp(24));
        View divider = new View(activity);
        divider.setBackgroundColor(BORDER);
        addView(divider, new LayoutParams(LayoutParams.MATCH_PARENT, dp(1)));
        addView(navigation);
        select(initialTab);
    }

    String selectedTab() {
        return tab;
    }

    void loadInitialData() {
        loadTabData();
    }

    private void navigate(String destination) {
        select(destination);
        loadTabData();
    }

    private void loadTabData() {
        if ("Requests".equals(tab) || "Dashboard".equals(tab)) {
            activity.loadConsultations(this);
        }
    }

    private void select(String destination) {
        tab = destination;
        navigation.removeAllViews();
        for (int index = 0; index < TABS.length; index++) {
            String name = TABS[index];
            boolean selected = name.equals(tab);
            Button item = button(name, () -> navigate(name));
            ScreenTypography.apply(item, 11);
            item.setMinWidth(0);
            item.setMinimumWidth(0);
            item.setTextColor(selected ? NAVY : MUTED);
            item.setSelected(selected);
            item.setPadding(0, dp(8), 0, dp(8));
            Drawable icon = getResources().getDrawable(TAB_ICONS[index], null).mutate();
            icon.setTint(selected ? NAVY : MUTED);
            item.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
            item.setCompoundDrawablePadding(dp(4));
            item.setBackground(ripple(selected ? rounded(PALE_BLUE) : rounded(WHITE)));
            navigation.addView(item, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        }

        content.removeAllViews();
        if (!"Profile".equals(tab)) addBanner();
        switch (tab) {
            case "Profile":
                showProfile();
                break;
            case "Dashboard":
                showDashboard();
                break;
            case "Requests":
                showRequestQueue();
                break;
            case "Registry":
                showRegistry();
                break;
            case "Manage":
                card("Service Authoring").addView(text("Service publishing is unavailable.", 14, MUTED, bodyFont));
                break;
            default:
                throw new IllegalArgumentException("Unknown staff tab");
        }
        scroll.scrollTo(0, 0);
    }

    private void addBanner() {
        LinearLayout banner = new LinearLayout(activity);
        banner.setOrientation(VERTICAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setPadding(dp(24), dp(24), dp(24), dp(24));

        banner.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{BLUE, 0xff13354d}));
        if ("Dashboard".equals(tab)) {
            banner.addView(text("Welcome back,", 21, WHITE, headingFont));
            banner.addView(text(email, 26, WHITE, headingFont));
            badge(banner, "CRM Staff", PALE_BLUE, NAVY);
        } else {
            banner.addView(text(title(), 30, WHITE, headingFont));
        }
        content.addView(banner);
    }

    private void showDashboard() {
        clearAfter(1);
        LinearLayout metrics = new LinearLayout(activity);
        LayoutParams metricParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        metricParams.setMargins(dp(16), dp(16), dp(16), 0);
        content.addView(metrics, metricParams);
        metric(metrics, "Pending Requests", "queued", 0xffad2534);
        metric(metrics, "In Processing", "claimed", ACCENT);

        LinearLayout queue = card("Service Request Queue");
        queue.addView(text("Recent consultations", 13, MUTED, bodyFont));
        if (consultations == null) {
            queue.addView(text(consultationMessage == null ? "Loading queue…" : consultationMessage,
                    14, MUTED, bodyFont));
        } else if (consultations.length() == 0) {
            queue.addView(text("No consultations.", 14, MUTED, bodyFont));
        } else {
            for (int index = 0; index < Math.min(5, consultations.length()); index++) {
                JSONObject ticket = consultations.optJSONObject(index);
                if (ticket == null) continue;
                TextView row = text("#" + ticket.optString("id") + "  •  "
                        + ticket.optString("service_type"), 14, NAVY, actionFont);
                row.setPadding(0, dp(10), 0, dp(10));
                queue.addView(row);
            }
        }
        styledAction(queue, "View Review Queue", () -> navigate("Requests"), NAVY, WHITE, false);
        LinearLayout actions = card("Staff Actions");
        action(actions, "View profile", () -> navigate("Profile"));
        action(actions, "Public resources", activity::showPublicResources);
    }

    private void metric(LinearLayout row, String label, String state, int color) {
        LinearLayout tile = panel();
        LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
        if (row.getChildCount() > 0) params.leftMargin = dp(12);
        row.addView(tile, params);
        int count = 0;
        if (consultations != null) {
            for (int index = 0; index < consultations.length(); index++) {
                JSONObject ticket = consultations.optJSONObject(index);
                if (ticket != null && state.equals(ticket.optString("status"))) count++;
            }
        }
        tile.addView(text(consultations == null ? "—" : Integer.toString(count), 28, color, actionFont));
        tile.addView(text(label, 13, NAVY, actionFont));
        tile.addView(text("Recent consultations", 11, MUTED, bodyFont));
    }

    private void showProfile() {
        LinearLayout profile = panel();
        profile.setPadding(0, 0, 0, 0);
        addPanel(profile);
        FrameLayout identity = new FrameLayout(activity);
        View cover = new View(activity);
        cover.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{NAVY, 0xff2d3450}));
        identity.addView(cover, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(72)));
        String initial = email.isEmpty() ? "?" : email.substring(0, 1).toUpperCase(Locale.ROOT);
        TextView avatar = text(initial, 24, ACCENT, actionFont);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable avatarBackground = new GradientDrawable();
        avatarBackground.setShape(GradientDrawable.OVAL);
        avatarBackground.setColor(YELLOW);
        avatarBackground.setStroke(dp(2), WHITE);
        avatar.setBackground(avatarBackground);
        FrameLayout.LayoutParams avatarParams = new FrameLayout.LayoutParams(dp(60), dp(60));
        avatarParams.setMargins(dp(16), dp(42), 0, 0);
        identity.addView(avatar, avatarParams);
        profile.addView(identity);
        profile.setClipToOutline(true);

        LinearLayout details = new LinearLayout(activity);
        details.setOrientation(VERTICAL);
        details.setPadding(dp(16), dp(8), dp(16), dp(16));
        profile.addView(details);
        details.addView(text(email, 19, ACCENT, actionFont));
        badge(details, "Staff", PALE_BLUE, NAVY);

        LinearLayout links = card("Navigation");
        action(links, "Staff Dashboard", () -> navigate("Dashboard"));
        action(links, "Research Registry", () -> navigate("Registry"));
        action(links, "Public resources", activity::showPublicResources);
        LinearLayout settings = card("Account Settings");
        settings.addView(text("Email", 12, MUTED, actionFont));
        settings.addView(text(email, 15, NAVY, bodyFont));
        styledAction(settings, "Sign Out", activity::signOut, WHITE, 0xffa62a35, true);
    }

    private void showRequestQueue() {
        LinearLayout filters = new LinearLayout(activity);
        filters.setBackgroundColor(WHITE);
        content.addView(filters);
        for (int index = 0; index < REQUEST_LABELS.length; index++) {
            int filter = index;
            boolean selected = index == requestFilter;
            Button filterButton = button(REQUEST_LABELS[index], () -> {
                requestFilter = filter;
                select("Requests");
            });
            filterButton.setSelected(selected);
            filterButton.setTextSize(13);
            filterButton.setTextColor(selected ? NAVY : MUTED);
            LinearLayout filterTab = new LinearLayout(activity);
            filterTab.setOrientation(VERTICAL);
            filterTab.addView(filterButton, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            View indicator = new View(activity);
            indicator.setBackgroundColor(selected ? YELLOW : BORDER);
            filterTab.addView(indicator, new LayoutParams(LayoutParams.MATCH_PARENT, dp(2)));
            filters.addView(filterTab, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        }

        EditText search = searchField("Search consultations");
        search.setText(requestQuery);
        addPanel(search);
        requestResults = new LinearLayout(activity);
        requestResults.setOrientation(VERTICAL);
        content.addView(requestResults);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                requestQuery = s.toString();
                renderRequests();
            }
            @Override public void afterTextChanged(Editable value) {}
        });
        renderRequests();

        Button refresh = button("Refresh", () -> {
            consultations = null;
            consultationMessage = null;
            renderRequests();
            activity.loadConsultations(this);
        });
        refresh.setTextColor(NAVY);
        refresh.setBackground(ripple(outlined(WHITE)));
        addPanel(refresh);
    }

    void showConsultations(JSONArray tickets, String message) {
        consultations = tickets;
        consultationMessage = message;
        if ("Requests".equals(tab)) renderRequests();
        if ("Dashboard".equals(tab)) showDashboard();
    }

    private void renderRequests() {
        requestResults.removeAllViews();
        if (consultations != null) {
            String keyword = requestQuery.trim().toLowerCase(Locale.ROOT);
            for (JSONObject ticket : matchingTickets(keyword)) {
                String service = ticket.optString("service_type");
                LinearLayout row = panel();
                row.addView(text(service, 17, ACCENT, actionFont));
                row.addView(text("Request #" + ticket.optString("id"), 14, NAVY, bodyFont));
                row.addView(text("Submitted " + date(ticket.optString("submitted_at")), 13, MUTED, bodyFont));
                if (!ticket.isNull("deadline_at")) {
                    row.addView(text("Deadline " + date(ticket.optString("deadline_at")), 13, MUTED, bodyFont));
                }
                badge(row, REQUEST_LABELS[requestFilter],
                        requestFilter == 2 ? 0xffe8f5ec : 0xfffff6df,
                        requestFilter == 2 ? 0xff216b37 : 0xff815000);
                addPanel(requestResults, row);
            }
        }
        if (requestResults.getChildCount() == 0) {
            TextView state = text(consultationMessage != null ? consultationMessage
                    : consultations == null ? "Loading consultations…" : "No matching consultations.",
                    14, MUTED, bodyFont);
            state.setPadding(dp(16), dp(12), dp(16), dp(12));
            addPanel(requestResults, state);
        }
    }

    private List<JSONObject> matchingTickets(String keyword) {
        List<JSONObject> matches = new ArrayList<>();
        for (int index = 0; index < consultations.length(); index++) {
            JSONObject ticket = consultations.optJSONObject(index);
            if (ticket == null || !REQUEST_STATES[requestFilter].equals(ticket.optString("status"))) continue;
            String service = ticket.optString("service_type").toLowerCase(Locale.ROOT);
            if (service.contains(keyword) || ticket.optString("id").contains(keyword)) matches.add(ticket);
        }
        if (requestFilter == 0) {
            matches.sort(Comparator
                    .comparing((JSONObject ticket) -> ticket.isNull("deadline_at"))
                    .thenComparing(ticket -> ticket.optString("deadline_at"))
                    .thenComparing(ticket -> ticket.optString("submitted_at"))
                    .thenComparingInt(ticket -> ticket.optInt("id")));
        }
        return matches;
    }

    private String date(String value) {
        try {
            return DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.getDefault())
                    .withZone(ZoneId.systemDefault()).format(Instant.parse(value));
        } catch (java.time.DateTimeException invalidDate) {
            return value;
        }
    }

    private EditText searchField(String hint) {
        EditText field = new EditText(activity);
        field.setHint(hint);
        field.setTypeface(bodyFont);
        field.setTextSize(14);
        field.setSingleLine();
        field.setMinHeight(dp(48));
        field.setPadding(dp(16), dp(8), dp(16), dp(8));
        field.setBackground(rounded(0xffeef2f7));
        return field;
    }

    private void showRegistry() {
        LinearLayout search = new LinearLayout(activity);
        search.setOrientation(HORIZONTAL);
        addPanel(search);
        EditText keyword = searchField("Search approved research");
        keyword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        keyword.setText(searchKeyword);
        search.addView(keyword, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        Button submit = button("Search", () -> {
            searchKeyword = keyword.getText().toString().trim();
            int currentQuery = ++queryId;
            showResearch(currentQuery, null, "Searching approved records…");
            activity.searchResearch(this, searchKeyword, currentQuery);
        });
        submit.setBackground(ripple(rounded(YELLOW)));
        LayoutParams submitParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        submitParams.leftMargin = dp(8);
        search.addView(submit, submitParams);
        int currentQuery = ++queryId;
        showResearch(currentQuery, null, "Loading approved records…");
        activity.searchResearch(this, searchKeyword, currentQuery);
    }

    void showResearch(int currentQuery, JSONArray records, String message) {
        if (!"Registry".equals(tab) || currentQuery != queryId) return;
        clearAfter(2);
        if (records == null || records.length() == 0) {
            card("Search results").addView(text(message == null ? "No matching records." : message,
                    14, MUTED, bodyFont));
            return;
        }
        for (int index = 0; index < records.length(); index++) {
            JSONObject record = records.optJSONObject(index);
            if (record == null) continue;
            LinearLayout row = card(record.optString("title"));
            if (!record.isNull("department")) {
                row.addView(text(record.optString("department"), 14, MUTED, bodyFont));
            }
            if (!record.isNull("research_year")) {
                row.addView(text(record.optString("research_year"), 14, MUTED, bodyFont));
            }
        }
    }

    private String title() {
        switch (tab) {
            case "Dashboard": return "Staff Dashboard";
            case "Requests": return "Review Queue";
            case "Registry": return "Research Registry";
            case "Manage": return "Manage";
            default: return "Profile";
        }
    }

    private void clearAfter(int count) {
        while (content.getChildCount() > count) content.removeViewAt(count);
    }

    private LinearLayout panel() {
        LinearLayout panel = new LinearLayout(activity);
        panel.setOrientation(VERTICAL);
        panel.setPadding(dp(16), dp(16), dp(16), dp(16));
        panel.setBackground(outlined(WHITE));
        return panel;
    }

    private void addPanel(View panel) {
        addPanel(content, panel);
    }

    private void addPanel(LinearLayout parent, View panel) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(16), dp(12), dp(16), 0);
        parent.addView(panel, params);
    }

    private LinearLayout card(String title) {
        LinearLayout card = panel();
        addPanel(card);
        TextView heading = text(title, 18, ACCENT, actionFont);
        ScreenTypography.heading(heading);
        heading.setPadding(0, 0, 0, dp(8));
        card.addView(heading);
        return card;
    }

    private void badge(LinearLayout parent, String label, int fill, int color) {
        TextView badge = text(label, 12, color, actionFont);
        badge.setBackground(rounded(fill));
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        parent.addView(badge, params);
    }

    private Button button(String label, Runnable run) {
        Button button = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextColor(NAVY);
        button.setTypeface(actionFont);
        ScreenTypography.apply(button, 14);
        button.setMinHeight(dp(48));
        button.setOnClickListener(view -> run.run());
        return button;
    }

    private void action(LinearLayout card, String label, Runnable run) {
        Button button = button(label, run);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        card.addView(button);
    }

    private void styledAction(LinearLayout card, String label, Runnable run, int fill, int color,
            boolean outline) {
        Button button = button(label, run);
        button.setTextColor(color);
        button.setBackground(ripple(outline ? outlined(fill) : rounded(fill)));
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        card.addView(button, params);
    }

    private RippleDrawable ripple(Drawable background) {
        return new RippleDrawable(ColorStateList.valueOf(0x22141e4d), background, null);
    }

    private GradientDrawable outlined(int color) {
        GradientDrawable shape = rounded(color);
        shape.setStroke(dp(1), BORDER);
        return shape;
    }

    private TextView text(String value, int size, int color, Typeface font) {
        TextView text = new TextView(activity);
        text.setText(value);
        text.setTextColor(color);
        text.setTypeface(font);
        ScreenTypography.apply(text, size);
        return text;
    }

    private GradientDrawable rounded(int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(12));
        return background;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
