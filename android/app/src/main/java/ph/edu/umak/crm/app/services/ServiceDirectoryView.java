package ph.edu.umak.crm.app.services;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
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
    private final Button retryBtn;
    private final LinearLayout guideBox;
    private List<ServiceData.ServiceItem> services = List.of();
    private String loadStatus = "Loading services…";
    private final LinearLayout cardsContainer;
    private final LinearLayout emptyStateContainer;
    private final LinearLayout guestFooterCard;

    public ServiceDirectoryView(Context context, boolean isSignedIn, Listener listener) {
        this.context = context;
        this.isSignedIn = isSignedIn;
        this.listener = listener;

        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 32));

        // 1. Search Bar with Search icon on left and Clear (x) on right
        LinearLayout searchBox = new LinearLayout(context);
        searchBox.setOrientation(LinearLayout.HORIZONTAL);
        searchBox.setGravity(Gravity.CENTER_VERTICAL);
        searchBox.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        searchBox.setPadding(UiTheme.dp(context, 12), 0, UiTheme.dp(context, 6), 0);

        ImageView searchIcon = new ImageView(context);
        searchIcon.setImageResource(R.drawable.ic_researcher_search);
        searchIcon.setColorFilter(0xff586b83);
        int iconSize = UiTheme.dp(context, 18);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconLp.rightMargin = UiTheme.dp(context, 8);
        searchBox.addView(searchIcon, iconLp);

        searchInput = new EditText(context);
        searchInput.setHint("Search services by name...");
        searchInput.setHintTextColor(0xff8898aa);
        searchInput.setTextColor(UiTheme.INK);
        searchInput.setTypeface(body);
        ScreenTypography.apply(searchInput, 14);
        searchInput.setSingleLine(true);
        searchInput.setBackgroundColor(Color.TRANSPARENT);
        searchInput.setMinHeight(UiTheme.dp(context, 46));
        searchBox.addView(searchInput, new LinearLayout.LayoutParams(0, -2, 1));

        clearBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        clearBtn.setText("×");
        clearBtn.setTextColor(0xff586b83);
        clearBtn.setTextSize(18);
        clearBtn.setTypeface(bold);
        clearBtn.setBackgroundColor(Color.TRANSPARENT);
        clearBtn.setVisibility(View.GONE);
        clearBtn.setOnClickListener(v -> {
            searchInput.setText("");
            filterServices("");
        });
        searchBox.addView(clearBtn, new LinearLayout.LayoutParams(UiTheme.dp(context, 36), UiTheme.dp(context, 36)));

        layout.addView(searchBox);

        // 2. Status text (e.g. "8 services", "1 service found for \"Ethics\"", "0 services found for \"ZZZ\"")
        statusText = new TextView(context);
        statusText.setTextColor(0xff586b83);
        statusText.setTypeface(bold);
        ScreenTypography.apply(statusText, 13);
        statusText.setPadding(0, UiTheme.dp(context, 10), 0, UiTheme.dp(context, 8));
        layout.addView(statusText);

        retryBtn = UiButton.outline(context, "Retry", UiTheme.BORDER, UiTheme.NAVY, null);
        retryBtn.setVisibility(View.GONE);
        layout.addView(retryBtn);

        // 3. Numbered Steps Guide Box (1 to 5) - displayed in default state only
        guideBox = createStepsGuideBox(context);
        layout.addView(guideBox);

        // 4. Cards container
        cardsContainer = new LinearLayout(context);
        cardsContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(cardsContainer);

        // 5. Empty state container (Column 3)
        emptyStateContainer = new LinearLayout(context);
        emptyStateContainer.setOrientation(LinearLayout.VERTICAL);
        emptyStateContainer.setGravity(Gravity.CENTER);
        emptyStateContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        emptyStateContainer.setPadding(pad, UiTheme.dp(context, 32), pad, UiTheme.dp(context, 32));
        emptyStateContainer.setVisibility(View.GONE);

        FrameLayout circleIconBox = new FrameLayout(context);
        circleIconBox.setBackground(UiTheme.shape(context, 0xffe5edf7, 28, 0));
        ImageView emptyIcon = new ImageView(context);
        emptyIcon.setImageResource(R.drawable.ic_researcher_search);
        emptyIcon.setColorFilter(0xff185d9d);
        int emptyIconSize = UiTheme.dp(context, 24);
        FrameLayout.LayoutParams emptyIconLp = new FrameLayout.LayoutParams(emptyIconSize, emptyIconSize, Gravity.CENTER);
        circleIconBox.addView(emptyIcon, emptyIconLp);
        int circleSize = UiTheme.dp(context, 56);
        emptyStateContainer.addView(circleIconBox, new LinearLayout.LayoutParams(circleSize, circleSize));

        TextView emptyTitle = new TextView(context);
        emptyTitle.setText("No matching services");
        emptyTitle.setTextColor(0xff082d50);
        emptyTitle.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(emptyTitle, 18);
        LinearLayout.LayoutParams emptyTitleLp = new LinearLayout.LayoutParams(-2, -2);
        emptyTitleLp.topMargin = UiTheme.dp(context, 16);
        emptyStateContainer.addView(emptyTitle, emptyTitleLp);

        Button emptyClearBtn = UiButton.text(context, "Clear search", 0xff185d9d, () -> {
            searchInput.setText("");
            filterServices("");
        });
        emptyClearBtn.setTypeface(bold);
        ScreenTypography.apply(emptyClearBtn, 14);
        LinearLayout.LayoutParams clearLp = new LinearLayout.LayoutParams(-2, -2);
        clearLp.topMargin = UiTheme.dp(context, 6);
        emptyStateContainer.addView(emptyClearBtn, clearLp);

        layout.addView(emptyStateContainer);

        // 6. Guest footer card (when user is visitor / guest - Column 1)
        guestFooterCard = new LinearLayout(context);
        guestFooterCard.setOrientation(LinearLayout.VERTICAL);
        guestFooterCard.setBackground(UiTheme.shape(context, 0xff082d50, 8, 0));
        guestFooterCard.setPadding(pad, UiTheme.dp(context, 20), pad, UiTheme.dp(context, 20));
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(-1, -2);
        footerParams.topMargin = UiTheme.dp(context, 16);

        TextView footerTitle = new TextView(context);
        footerTitle.setText("Submit a Service Request Today");
        footerTitle.setTextColor(0xffffffff);
        footerTitle.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(footerTitle, 18);
        guestFooterCard.addView(footerTitle);

        TextView footerDesc = new TextView(context);
        footerDesc.setText("Log in with your UMak credentials to access all CRM Services.");
        footerDesc.setTextColor(0xffe5edf7);
        footerDesc.setTypeface(body);
        ScreenTypography.apply(footerDesc, 13);
        footerDesc.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 16));
        guestFooterCard.addView(footerDesc);

        Button signInBtn = UiButton.primary(context, "Sign In", () -> {
            if (listener != null) listener.onSignInClicked();
        });
        signInBtn.setBackground(UiTheme.shape(context, 0xfff8eb37, 6, 0));
        signInBtn.setTextColor(0xff082d50);
        signInBtn.setTypeface(bold);
        ScreenTypography.apply(signInBtn, 14);
        guestFooterCard.addView(signInBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 44)));

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

    private LinearLayout createStepsGuideBox(Context ctx) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(UiTheme.shape(ctx, 0xff185d9d, 8, 0));
        int pad = UiTheme.dp(ctx, 14);
        box.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = UiTheme.dp(ctx, 14);
        box.setLayoutParams(lp);

        String[] stepTexts = {
                "Log in to your account",
                "Navigate to Service Requests",
                "Select & fill out the service form",
                "Upload required documents",
                "Track status in real time"
        };

        for (int i = 0; i < stepTexts.length; i++) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 0, 0, i == stepTexts.length - 1 ? 0 : UiTheme.dp(ctx, 8));

            TextView badge = new TextView(ctx);
            badge.setText(String.valueOf(i + 1));
            badge.setTextColor(0xffffffff);
            badge.setTypeface(UiTheme.boldFont(ctx));
            ScreenTypography.apply(badge, 11);
            badge.setGravity(Gravity.CENTER);
            badge.setBackground(UiTheme.shape(ctx, 0xffe67e22, 11, 0));
            int badgeSize = UiTheme.dp(ctx, 22);
            row.addView(badge, new LinearLayout.LayoutParams(badgeSize, badgeSize));

            TextView text = new TextView(ctx);
            text.setText(stepTexts[i]);
            text.setTextColor(0xffffffff);
            text.setTypeface(UiTheme.bodyFont(ctx));
            ScreenTypography.apply(text, 13);
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, -2, 1);
            textLp.setMarginStart(UiTheme.dp(ctx, 10));
            row.addView(text, textLp);

            box.addView(row);
        }
        return box;
    }

    public void showServices(List<ServiceData.ServiceItem> items) {
        services = List.copyOf(items);
        loadStatus = null;
        retryBtn.setVisibility(View.GONE);
        filterServices(searchInput.getText().toString().trim());
    }

    public void showLoading() {
        loadStatus = "Loading services…";
        retryBtn.setVisibility(View.GONE);
        filterServices("");
    }

    public void showFailure(Runnable retry) {
        loadStatus = "Unable to load services. Try again.";
        retryBtn.setVisibility(View.VISIBLE);
        retryBtn.setOnClickListener(view -> retry.run());
        filterServices("");
    }

    public void filterServices(String query) {
        cardsContainer.removeAllViews();
        if (loadStatus != null) {
            statusText.setText(loadStatus);
            guideBox.setVisibility(View.GONE);
            cardsContainer.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.GONE);
            return;
        }
        List<ServiceData.ServiceItem> matches = new ArrayList<>();
        for (ServiceData.ServiceItem item : services) {
            if (query.isEmpty() || item.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                    || item.code.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                matches.add(item);
            }
        }

        if (query.isEmpty()) {
            statusText.setText(matches.size() + " services");
            guideBox.setVisibility(View.VISIBLE);
        } else {
            statusText.setText(matches.size() + (matches.size() == 1 ? " service found for \"" : " services found for \"") + query + "\"");
            guideBox.setVisibility(View.GONE);
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
        int pad = UiTheme.dp(ctx, 14);
        card.setPadding(pad, pad, pad, pad);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> {
            if (listener != null) listener.onSelectService(item);
        });

        // Top Row: Document icon container + Title column
        LinearLayout headerRow = new LinearLayout(ctx);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        FrameLayout iconFrame = new FrameLayout(ctx);
        iconFrame.setBackground(UiTheme.shape(ctx, 0xffe5edf7, 6, 0));
        ImageView docIcon = new ImageView(ctx);
        docIcon.setImageResource(R.drawable.ic_crm_doc);
        docIcon.setColorFilter(0xff185d9d);
        int docSize = UiTheme.dp(ctx, 18);
        iconFrame.addView(docIcon, new FrameLayout.LayoutParams(docSize, docSize, Gravity.CENTER));
        int frameSize = UiTheme.dp(ctx, 36);
        headerRow.addView(iconFrame, new LinearLayout.LayoutParams(frameSize, frameSize));

        LinearLayout titleCol = new LinearLayout(ctx);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleColLp = new LinearLayout.LayoutParams(0, -2, 1);
        titleColLp.setMarginStart(UiTheme.dp(ctx, 10));

        TextView codeView = new TextView(ctx);
        codeView.setText(item.code);
        codeView.setTextColor(0xff586b83);
        codeView.setTypeface(UiTheme.boldFont(ctx));
        ScreenTypography.apply(codeView, 11);
        titleCol.addView(codeView);

        TextView nameView = new TextView(ctx);
        nameView.setText(item.name);
        nameView.setTextColor(0xff082d50);
        nameView.setTypeface(UiTheme.boldFont(ctx));
        ScreenTypography.apply(nameView, 16);
        titleCol.addView(nameView);

        headerRow.addView(titleCol, titleColLp);
        card.addView(headerRow);

        // Tags row directly below title (exclude redundant Student/Faculty tags)
        LinearLayout tagsRow = new LinearLayout(ctx);
        tagsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tagsRowLp = new LinearLayout.LayoutParams(-1, -2);
        tagsRowLp.topMargin = UiTheme.dp(ctx, 6);
        int tagCount = 0;
        for (String tag : item.tags) {
            if ("Student".equalsIgnoreCase(tag) || "Faculty".equalsIgnoreCase(tag)) {
                continue;
            }
            tagCount++;
            TextView tagPill = new TextView(ctx);
            tagPill.setText(tag);
            tagPill.setTextColor(0xff185d9d);
            tagPill.setTypeface(UiTheme.boldFont(ctx));
            ScreenTypography.apply(tagPill, 10);
            tagPill.setBackground(UiTheme.shape(ctx, 0xffe5edf7, 4, 0));
            int tp = UiTheme.dp(ctx, 6);
            tagPill.setPadding(tp, UiTheme.dp(ctx, 2), tp, UiTheme.dp(ctx, 2));
            LinearLayout.LayoutParams tagLp = new LinearLayout.LayoutParams(-2, -2);
            tagLp.rightMargin = UiTheme.dp(ctx, 6);
            tagsRow.addView(tagPill, tagLp);
        }
        if (tagCount > 0) {
            card.addView(tagsRow, tagsRowLp);
        }

        // Description
        TextView descView = new TextView(ctx);
        descView.setText(item.cardDescription);
        descView.setTextColor(0xff586b83);
        descView.setTypeface(UiTheme.bodyFont(ctx));
        ScreenTypography.apply(descView, 13);
        descView.setLineSpacing(0, 1.15f);
        descView.setPadding(0, UiTheme.dp(ctx, 8), 0, UiTheme.dp(ctx, 10));
        card.addView(descView);

        // Requirements heading
        TextView reqHeading = new TextView(ctx);
        reqHeading.setText("REQUIREMENTS");
        reqHeading.setTextColor(0xff586b83);
        reqHeading.setTypeface(UiTheme.boldFont(ctx));
        ScreenTypography.apply(reqHeading, 10);
        reqHeading.setLetterSpacing(0.04f);
        card.addView(reqHeading);

        // Requirements list with orange bullet dots
        for (String req : item.cardRequirements) {
            LinearLayout reqRow = new LinearLayout(ctx);
            reqRow.setOrientation(LinearLayout.HORIZONTAL);
            reqRow.setPadding(0, UiTheme.dp(ctx, 2), 0, UiTheme.dp(ctx, 1));

            TextView dot = new TextView(ctx);
            dot.setText("•");
            dot.setTextColor(0xffe67e22);
            dot.setTypeface(UiTheme.boldFont(ctx));
            ScreenTypography.apply(dot, 13);
            reqRow.addView(dot, new LinearLayout.LayoutParams(-2, -2));

            TextView reqItem = new TextView(ctx);
            reqItem.setText(" " + req);
            reqItem.setTextColor(0xff586b83);
            reqItem.setTypeface(UiTheme.bodyFont(ctx));
            ScreenTypography.apply(reqItem, 12);
            reqRow.addView(reqItem, new LinearLayout.LayoutParams(0, -2, 1));

            card.addView(reqRow);
        }

        // Bottom Turnaround Row with Chevron
        LinearLayout bottomRow = new LinearLayout(ctx);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bottomLp = new LinearLayout.LayoutParams(-1, -2);
        bottomLp.topMargin = UiTheme.dp(ctx, 12);

        String prefix = "TURNAROUND ";
        String fullTurnaround = prefix + item.turnaround;
        SpannableString span = new SpannableString(fullTurnaround);
        span.setSpan(new ForegroundColorSpan(0xff718096), 0, prefix.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(0xff082d50), prefix.length(), fullTurnaround.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new StyleSpan(Typeface.BOLD), prefix.length(), fullTurnaround.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView turnaround = new TextView(ctx);
        turnaround.setText(span);
        ScreenTypography.apply(turnaround, 11);
        bottomRow.addView(turnaround, new LinearLayout.LayoutParams(0, -2, 1));

        TextView chevron = new TextView(ctx);
        chevron.setText("›");
        chevron.setTextColor(0xff185d9d);
        chevron.setTypeface(UiTheme.boldFont(ctx));
        chevron.setTextSize(16);
        bottomRow.addView(chevron);

        card.addView(bottomRow, bottomLp);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = UiTheme.dp(ctx, 12);
        card.setLayoutParams(cardParams);
        return card;
    }

    public void configurePreviewState(String state) {
        showServices(ServiceData.ALL_SERVICES);
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
        return PageTitle.wrap(layout, "Research Services &\nProcedures", "The Center for Research Management provides eight core services to support the research journey of every UMak student and faculty member — from initial registration through publication.", true);
    }
}
