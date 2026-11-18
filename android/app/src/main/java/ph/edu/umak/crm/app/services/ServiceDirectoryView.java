package ph.edu.umak.crm.app.services;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class ServiceDirectoryView {
    public interface Listener {
        void onSelectService(ServiceData.ServiceItem item);
        void onSignInClicked();
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;
    private final boolean isSignedIn;

    private final EditText searchInput;
    private final Button clearBtn;
    private final TextView statusText;
    private final LinearLayout cardsContainer;
    private final LinearLayout emptyStateContainer;
    private final LinearLayout guestFooterCard;

    public ServiceDirectoryView(Context context, boolean isSignedIn, Listener listener) {
        this.context = context;
        this.isSignedIn = isSignedIn;
        this.listener = listener;

        Typeface heading = UiTheme.headingFont(context);
        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 24);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 32));

        TextView institution = new TextView(context);
        institution.setText("Center for Research Management");
        institution.setTextColor(UiTheme.MUTED);
        institution.setTypeface(bold);
        ScreenTypography.apply(institution, 12);
        institution.setPadding(0, 0, 0, UiTheme.dp(context, 4));
        layout.addView(institution);

        TextView title = new TextView(context);
        title.setText("Research Services &\nProcedures");
        title.setTextColor(UiTheme.INK);
        title.setTypeface(heading);
        ScreenTypography.apply(title, 26);
        ScreenTypography.heading(title);
        layout.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("The Center for Research Management provides eight core services to support the research journey of every UMak student and faculty member — from initial registration through publication.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(body);
        ScreenTypography.apply(subtitle, 14);
        subtitle.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 16));
        layout.addView(subtitle);

        // Search Bar with Clear button
        LinearLayout searchBox = new LinearLayout(context);
        searchBox.setOrientation(LinearLayout.HORIZONTAL);
        searchBox.setGravity(Gravity.CENTER_VERTICAL);
        searchBox.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        searchBox.setPadding(UiTheme.dp(context, 12), 0, UiTheme.dp(context, 6), 0);

        searchInput = new EditText(context);
        searchInput.setHint("Search services by name…");
        searchInput.setHintTextColor(UiTheme.MUTED);
        searchInput.setTextColor(UiTheme.INK);
        searchInput.setTypeface(body);
        ScreenTypography.apply(searchInput, 14);
        searchInput.setSingleLine(true);
        searchInput.setBackgroundColor(Color.TRANSPARENT);
        searchInput.setMinHeight(UiTheme.dp(context, 46));
        searchBox.addView(searchInput, new LinearLayout.LayoutParams(0, -2, 1));

        clearBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        clearBtn.setText("X");
        clearBtn.setTextColor(UiTheme.MUTED);
        clearBtn.setTextSize(16);
        clearBtn.setTypeface(UiTheme.boldFont(context));
        clearBtn.setBackgroundColor(Color.TRANSPARENT);
        clearBtn.setVisibility(View.GONE);
        clearBtn.setOnClickListener(v -> {
            searchInput.setText("");
            filterServices("");
        });
        searchBox.addView(clearBtn, new LinearLayout.LayoutParams(UiTheme.dp(context, 36), UiTheme.dp(context, 36)));

        layout.addView(searchBox);

        // Status text (e.g. "8 services" or "1 service found for \"Ethics\"")
        statusText = new TextView(context);
        statusText.setTextColor(UiTheme.MUTED);
        statusText.setTypeface(bold);
        ScreenTypography.apply(statusText, 13);
        statusText.setPadding(0, UiTheme.dp(context, 12), 0, UiTheme.dp(context, 8));
        layout.addView(statusText);

        // Cards container
        cardsContainer = new LinearLayout(context);
        cardsContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(cardsContainer);

        // Empty state container
        emptyStateContainer = new LinearLayout(context);
        emptyStateContainer.setOrientation(LinearLayout.VERTICAL);
        emptyStateContainer.setGravity(Gravity.CENTER);
        emptyStateContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        emptyStateContainer.setPadding(pad, pad * 2, pad, pad * 2);
        emptyStateContainer.setVisibility(View.GONE);

        TextView emptyTitle = new TextView(context);
        emptyTitle.setText("No matching services");
        emptyTitle.setTextColor(UiTheme.MUTED);
        emptyTitle.setTypeface(bold);
        ScreenTypography.apply(emptyTitle, 16);
        emptyStateContainer.addView(emptyTitle);

        Button emptyClearBtn = UiButton.text(context, "Clear search", UiTheme.NAVY, () -> {
            searchInput.setText("");
            filterServices("");
        });
        LinearLayout.LayoutParams clearLp = new LinearLayout.LayoutParams(-2, -2);
        clearLp.topMargin = UiTheme.dp(context, 10);
        emptyStateContainer.addView(emptyClearBtn, clearLp);

        layout.addView(emptyStateContainer);

        // Guest footer card (when user is visitor / guest)
        guestFooterCard = new LinearLayout(context);
        guestFooterCard.setOrientation(LinearLayout.VERTICAL);
        guestFooterCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        guestFooterCard.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(-1, -2);
        footerParams.topMargin = UiTheme.dp(context, 16);

        TextView footerTitle = new TextView(context);
        footerTitle.setText("Submit a Service Request Today");
        footerTitle.setTextColor(UiTheme.INK);
        footerTitle.setTypeface(bold);
        ScreenTypography.apply(footerTitle, 16);
        guestFooterCard.addView(footerTitle);

        TextView footerDesc = new TextView(context);
        footerDesc.setText("Log in with your UMak credentials to access all CRM Services.");
        footerDesc.setTextColor(UiTheme.MUTED);
        footerDesc.setTypeface(body);
        ScreenTypography.apply(footerDesc, 13);
        footerDesc.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 14));
        guestFooterCard.addView(footerDesc);

        Button signInBtn = UiButton.primary(context, "Sign In", () -> {
            if (listener != null) listener.onSignInClicked();
        });
        guestFooterCard.addView(signInBtn, new LinearLayout.LayoutParams(-1, -2));

        if (!isSignedIn) {
            layout.addView(guestFooterCard, footerParams);
        }

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim() : "";
                clearBtn.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                filterServices(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        filterServices("");
    }

    public void filterServices(String query) {
        cardsContainer.removeAllViews();
        List<ServiceData.ServiceItem> matches = new ArrayList<>();
        for (ServiceData.ServiceItem item : ServiceData.ALL_SERVICES) {
            if (query.isEmpty() || item.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                    || item.code.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                matches.add(item);
            }
        }

        if (query.isEmpty()) {
            statusText.setText(matches.size() + " services");
        } else {
            statusText.setText(matches.size() + (matches.size() == 1 ? " service found for \"" : " services found for \"") + query + "\"");
        }

        if (matches.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            cardsContainer.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            cardsContainer.setVisibility(View.VISIBLE);
            for (ServiceData.ServiceItem item : matches) {
                cardsContainer.addView(createServiceCard(item));
            }
        }
    }

    private View createServiceCard(ServiceData.ServiceItem item) {
        Context ctx = context;
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.ripple(UiTheme.shape(ctx, UiTheme.WHITE, 8, UiTheme.BORDER), 0x20395072));
        int pad = UiTheme.dp(ctx, 16);
        card.setPadding(pad, pad, pad, pad);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> {
            if (listener != null) listener.onSelectService(item);
        });

        // Top Row: Tags on left, turnaround on right
        LinearLayout topRow = new LinearLayout(ctx);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout tagsRow = new LinearLayout(ctx);
        tagsRow.setOrientation(LinearLayout.HORIZONTAL);
        for (String tag : item.tags) {
            TextView tagPill = new TextView(ctx);
            tagPill.setText(tag);
            tagPill.setTextColor(UiTheme.TAG_TEXT);
            tagPill.setTypeface(UiTheme.boldFont(ctx));
            ScreenTypography.apply(tagPill, 10);
            tagPill.setBackground(UiTheme.shape(ctx, UiTheme.TAG_BG, 4, 0));
            int tp = UiTheme.dp(ctx, 6);
            tagPill.setPadding(tp, UiTheme.dp(ctx, 2), tp, UiTheme.dp(ctx, 2));
            LinearLayout.LayoutParams tagLp = new LinearLayout.LayoutParams(-2, -2);
            tagLp.rightMargin = UiTheme.dp(ctx, 6);
            tagsRow.addView(tagPill, tagLp);
        }
        topRow.addView(tagsRow, new LinearLayout.LayoutParams(0, -2, 1));

        TextView turnaround = new TextView(ctx);
        turnaround.setText("TURNAROUND " + item.turnaround);
        turnaround.setTextColor(UiTheme.MUTED);
        turnaround.setTypeface(UiTheme.boldFont(ctx));
        ScreenTypography.apply(turnaround, 10);
        topRow.addView(turnaround);

        card.addView(topRow);

        // Code
        TextView codeView = new TextView(ctx);
        codeView.setText(item.code);
        codeView.setTextColor(UiTheme.MUTED);
        codeView.setTypeface(UiTheme.boldFont(ctx));
        ScreenTypography.apply(codeView, 11);
        codeView.setPadding(0, UiTheme.dp(ctx, 8), 0, UiTheme.dp(ctx, 2));
        card.addView(codeView);

        // Name
        TextView nameView = new TextView(ctx);
        nameView.setText(item.name);
        nameView.setTextColor(UiTheme.INK);
        nameView.setTypeface(UiTheme.boldFont(ctx));
        ScreenTypography.apply(nameView, 16);
        card.addView(nameView);

        // Description
        TextView descView = new TextView(ctx);
        descView.setText(item.cardDescription);
        descView.setTextColor(UiTheme.MUTED);
        descView.setTypeface(UiTheme.bodyFont(ctx));
        ScreenTypography.apply(descView, 13);
        descView.setPadding(0, UiTheme.dp(ctx, 4), 0, UiTheme.dp(ctx, 10));
        card.addView(descView);

        // Requirements heading
        TextView reqHeading = new TextView(ctx);
        reqHeading.setText("Requirements");
        reqHeading.setTextColor(UiTheme.INK);
        reqHeading.setTypeface(UiTheme.boldFont(ctx));
        ScreenTypography.apply(reqHeading, 11);
        card.addView(reqHeading);

        // Requirements list
        for (String req : item.cardRequirements) {
            TextView reqItem = new TextView(ctx);
            reqItem.setText("• " + req);
            reqItem.setTextColor(UiTheme.MUTED);
            reqItem.setTypeface(UiTheme.bodyFont(ctx));
            ScreenTypography.apply(reqItem, 12);
            reqItem.setPadding(UiTheme.dp(ctx, 4), UiTheme.dp(ctx, 1), 0, 0);
            card.addView(reqItem);
        }

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = UiTheme.dp(ctx, 12);
        card.setLayoutParams(cardParams);
        return card;
    }

    public void configurePreviewState(String state) {
        if ("2.1".equals(state)) {
            searchInput.setText("");
            filterServices("");
        } else if ("2.1-ethics".equals(state)) {
            searchInput.setText("Ethics");
            filterServices("Ethics");
        } else if ("2.1-empty".equals(state)) {
            searchInput.setText("ZZZ");
            filterServices("ZZZ");
        }
    }

    public View getView() {
        return getContentLayout();
    }

    public LinearLayout getContentLayout() {
        if (layout.getParent() instanceof android.view.ViewGroup) {
            ((android.view.ViewGroup) layout.getParent()).removeView(layout);
        }
        return layout;
    }
}
