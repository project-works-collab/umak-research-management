package ph.edu.umak.crm.app.services;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiBanner;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class ServiceDetailView {
    public interface Listener {
        void onBack();
        void onSubmitRequest(ServiceData.ServiceItem item);
        void onViewSubmissionInstructions(ServiceData.ServiceItem item);
        void onOpenNavigationGuidance();
        void onContactCrm();
        void onOpenForm(ServiceData.ServiceItem item, boolean simulateFailure);
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;
    private ServiceData.ServiceItem currentItem;

    private final LinearLayout formUnavailableBanner;
    private final TextView serviceCodeBadge;
    private final TextView serviceChannelBadge;
    private final TextView titleView;
    private final TextView purposeText;
    private final LinearLayout whoCanApplyTagsContainer;
    private final TextView whoCanApplyText;
    private final TextView responsibleContactText;
    private final TextView responsibleEmailText;
    private final LinearLayout instructionsContainer;
    private final LinearLayout documentsContainer;
    private final LinearLayout approvedFormBox;
    private final TextView formTitleText;
    private final TextView formVersionText;
    private final LinearLayout formUnavailableRow;
    private final Button actionBtn;

    public ServiceDetailView(Context context, ServiceData.ServiceItem service, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.currentItem = service != null ? service : ServiceData.getById(2);

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

        titleView = new TextView(context);
        titleView.setText(currentItem.name);
        titleView.setTextColor(UiTheme.INK);
        titleView.setTypeface(heading);
        ScreenTypography.apply(titleView, 26);
        ScreenTypography.heading(titleView);
        layout.addView(titleView);

        // Badges row: Code badge + Channel badge
        LinearLayout badgesRow = new LinearLayout(context);
        badgesRow.setOrientation(LinearLayout.HORIZONTAL);
        badgesRow.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 16));

        serviceCodeBadge = createBadge(context, currentItem.code, UiTheme.NAVY, UiTheme.WHITE, bold);
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(-2, -2);
        codeParams.rightMargin = UiTheme.dp(context, 8);
        badgesRow.addView(serviceCodeBadge, codeParams);

        serviceChannelBadge = createBadge(context, currentItem.channel, UiTheme.PALE, UiTheme.NAVY, bold);
        badgesRow.addView(serviceChannelBadge);
        layout.addView(badgesRow);

        // Section: Purpose
        addSectionHeader(context, layout, "PURPOSE", bold);
        LinearLayout purposeCard = createCard(context);
        purposeText = new TextView(context);
        purposeText.setText(currentItem.purpose);
        purposeText.setTextColor(UiTheme.INK);
        purposeText.setTypeface(body);
        ScreenTypography.apply(purposeText, 14);
        purposeCard.addView(purposeText);
        layout.addView(purposeCard);

        addSpacing(context, layout, 16);

        // Section: Who can apply
        addSectionHeader(context, layout, "WHO CAN APPLY", bold);
        LinearLayout applyCard = createCard(context);
        whoCanApplyTagsContainer = new LinearLayout(context);
        whoCanApplyTagsContainer.setOrientation(LinearLayout.HORIZONTAL);
        whoCanApplyTagsContainer.setPadding(0, 0, 0, UiTheme.dp(context, 8));
        applyCard.addView(whoCanApplyTagsContainer);

        whoCanApplyText = new TextView(context);
        whoCanApplyText.setText(currentItem.whoCanApplyText);
        whoCanApplyText.setTextColor(UiTheme.MUTED);
        whoCanApplyText.setTypeface(body);
        ScreenTypography.apply(whoCanApplyText, 13);
        applyCard.addView(whoCanApplyText);
        layout.addView(applyCard);

        addSpacing(context, layout, 16);

        // Section: Responsible contact
        addSectionHeader(context, layout, "RESPONSIBLE CONTACT", bold);
        LinearLayout contactCard = createCard(context);
        responsibleContactText = new TextView(context);
        responsibleContactText.setText(currentItem.responsibleContact);
        responsibleContactText.setTextColor(UiTheme.INK);
        responsibleContactText.setTypeface(bold);
        ScreenTypography.apply(responsibleContactText, 14);
        contactCard.addView(responsibleContactText);

        responsibleEmailText = new TextView(context);
        responsibleEmailText.setText(currentItem.contactEmail);
        responsibleEmailText.setTextColor(UiTheme.ACCENT);
        responsibleEmailText.setTypeface(body);
        ScreenTypography.apply(responsibleEmailText, 13);
        responsibleEmailText.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        contactCard.addView(responsibleEmailText);
        layout.addView(contactCard);

        addSpacing(context, layout, 16);

        // Section: Instructions
        addSectionHeader(context, layout, "INSTRUCTIONS", bold);
        instructionsContainer = createCard(context);
        layout.addView(instructionsContainer);

        addSpacing(context, layout, 16);

        // Section: Documents to prepare
        addSectionHeader(context, layout, "DOCUMENTS TO PREPARE", bold);
        TextView docNote = new TextView(context);
        docNote.setText("PDF or DOCX · up to 10 MB each · up to 5 files");
        docNote.setTextColor(UiTheme.MUTED);
        docNote.setTypeface(body);
        ScreenTypography.apply(docNote, 12);
        docNote.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        layout.addView(docNote);

        documentsContainer = createCard(context);
        layout.addView(documentsContainer);

        addSpacing(context, layout, 16);

        // Section: Approved forms
        addSectionHeader(context, layout, "APPROVED FORMS", bold);

        formUnavailableBanner = new LinearLayout(context);
        formUnavailableBanner.setOrientation(LinearLayout.VERTICAL);
        formUnavailableBanner.setVisibility(View.GONE);
        View amberAlert = UiBanner.amber(context,
                "The approved form could not be loaded. You can still read the instructions and requirements, or contact CRM for a copy.");
        formUnavailableBanner.addView(amberAlert);
        LinearLayout.LayoutParams bannerLp = new LinearLayout.LayoutParams(-1, -2);
        bannerLp.bottomMargin = UiTheme.dp(context, 10);
        layout.addView(formUnavailableBanner, bannerLp);

        approvedFormBox = createCard(context);
        formTitleText = new TextView(context);
        formTitleText.setText(currentItem.formTitle);
        formTitleText.setTextColor(UiTheme.INK);
        formTitleText.setTypeface(bold);
        ScreenTypography.apply(formTitleText, 14);
        approvedFormBox.addView(formTitleText);

        formVersionText = new TextView(context);
        formVersionText.setText(currentItem.formVersion);
        formVersionText.setTextColor(UiTheme.MUTED);
        formVersionText.setTypeface(body);
        ScreenTypography.apply(formVersionText, 12);
        formVersionText.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        approvedFormBox.addView(formVersionText);

        formUnavailableRow = new LinearLayout(context);
        formUnavailableRow.setOrientation(LinearLayout.HORIZONTAL);
        formUnavailableRow.setGravity(Gravity.CENTER_VERTICAL);
        formUnavailableRow.setPadding(0, UiTheme.dp(context, 8), 0, 0);
        formUnavailableRow.setVisibility(View.GONE);

        TextView formErrorMsg = new TextView(context);
        formErrorMsg.setText("Unable to open the form");
        formErrorMsg.setTextColor(UiTheme.ERROR_RED);
        formErrorMsg.setTypeface(body);
        ScreenTypography.apply(formErrorMsg, 12);
        formUnavailableRow.addView(formErrorMsg, new LinearLayout.LayoutParams(0, -2, 1));

        Button retryFormBtn = UiButton.text(context, "Retry", UiTheme.NAVY, () -> {
            if (listener != null) listener.onOpenForm(currentItem, false);
        });
        formUnavailableRow.addView(retryFormBtn);
        approvedFormBox.addView(formUnavailableRow);

        approvedFormBox.setOnClickListener(v -> {
            if (listener != null) listener.onOpenForm(currentItem, formUnavailableBanner.getVisibility() == View.VISIBLE);
        });
        layout.addView(approvedFormBox);

        addSpacing(context, layout, 20);

        // Navigation links
        Button navGuidanceBtn = UiButton.text(context, "App navigation guidance", UiTheme.NAVY, () -> {
            if (listener != null) listener.onOpenNavigationGuidance();
        });
        navGuidanceBtn.setGravity(Gravity.CENTER);
        layout.addView(navGuidanceBtn, new LinearLayout.LayoutParams(-1, -2));

        Button contactCrmBtn = UiButton.text(context, "Contact CRM", UiTheme.MUTED, () -> {
            if (listener != null) listener.onContactCrm();
        });
        contactCrmBtn.setGravity(Gravity.CENTER);
        layout.addView(contactCrmBtn, new LinearLayout.LayoutParams(-1, -2));

        // Primary Action Button (Submit Request or View Submission Instructions)
        actionBtn = UiButton.primary(context, "Submit Request", this::onPrimaryAction);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(-1, -2);
        actionParams.topMargin = UiTheme.dp(context, 16);
        layout.addView(actionBtn, actionParams);

        bindService(this.currentItem, false);
    }

    public void bindService(ServiceData.ServiceItem item, boolean formUnavailable) {
        this.currentItem = item;
        titleView.setText(item.name);
        serviceCodeBadge.setText(item.code);
        serviceChannelBadge.setText(item.channel);
        purposeText.setText(item.purpose);

        // Who can apply tags
        whoCanApplyTagsContainer.removeAllViews();
        for (String tag : item.whoCanApplyTags) {
            TextView tagPill = createBadge(context, tag, UiTheme.TAG_BG, UiTheme.TAG_TEXT, UiTheme.boldFont(context));
            LinearLayout.LayoutParams tagLp = new LinearLayout.LayoutParams(-2, -2);
            tagLp.rightMargin = UiTheme.dp(context, 6);
            whoCanApplyTagsContainer.addView(tagPill, tagLp);
        }
        whoCanApplyText.setText(item.whoCanApplyText);

        responsibleContactText.setText(item.responsibleContact);
        responsibleEmailText.setText(item.contactEmail);

        // Instructions steps
        instructionsContainer.removeAllViews();
        for (int i = 0; i < item.instructions.length; i++) {
            LinearLayout stepRow = new LinearLayout(context);
            stepRow.setOrientation(LinearLayout.HORIZONTAL);
            stepRow.setPadding(0, 0, 0, i == item.instructions.length - 1 ? 0 : UiTheme.dp(context, 10));

            TextView num = new TextView(context);
            num.setText(String.valueOf(i + 1));
            num.setTextColor(UiTheme.NAVY);
            num.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(num, 13);
            num.setBackground(UiTheme.shape(context, UiTheme.PALE, 10, 0));
            num.setGravity(Gravity.CENTER);
            int numSize = UiTheme.dp(context, 22);
            LinearLayout.LayoutParams numLp = new LinearLayout.LayoutParams(numSize, numSize);
            numLp.rightMargin = UiTheme.dp(context, 10);
            stepRow.addView(num, numLp);

            TextView stepText = new TextView(context);
            stepText.setText(item.instructions[i]);
            stepText.setTextColor(UiTheme.INK);
            stepText.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(stepText, 13);
            stepRow.addView(stepText, new LinearLayout.LayoutParams(0, -2, 1));

            instructionsContainer.addView(stepRow);
        }

        // Documents list
        documentsContainer.removeAllViews();
        for (int i = 0; i < item.documents.length; i++) {
            ServiceData.DocumentItem doc = item.documents[i];
            LinearLayout docRow = new LinearLayout(context);
            docRow.setOrientation(LinearLayout.HORIZONTAL);
            docRow.setGravity(Gravity.CENTER_VERTICAL);
            docRow.setPadding(0, 0, 0, i == item.documents.length - 1 ? 0 : UiTheme.dp(context, 10));

            TextView docTitle = new TextView(context);
            docTitle.setText(doc.title);
            docTitle.setTextColor(UiTheme.INK);
            docTitle.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(docTitle, 13);
            docRow.addView(docTitle, new LinearLayout.LayoutParams(0, -2, 1));

            TextView pill = createBadge(context, doc.required ? "Required" : "Optional",
                    doc.required ? UiTheme.PILL_REQUIRED_BG : UiTheme.PILL_OPTIONAL_BG,
                    doc.required ? UiTheme.PILL_REQUIRED_TEXT : UiTheme.PILL_OPTIONAL_TEXT,
                    UiTheme.boldFont(context));
            docRow.addView(pill);

            documentsContainer.addView(docRow);
        }

        // Approved form
        formTitleText.setText(item.formTitle);
        formVersionText.setText(item.formVersion);

        setFormUnavailable(formUnavailable);

        // Action button text
        if ("External channel".equalsIgnoreCase(item.channel)) {
            actionBtn.setText("View Submission Instructions");
        } else {
            actionBtn.setText("Submit Request");
        }
    }

    public void setFormUnavailable(boolean unavailable) {
        if (unavailable) {
            formUnavailableBanner.setVisibility(View.VISIBLE);
            formUnavailableRow.setVisibility(View.VISIBLE);
            formVersionText.setVisibility(View.GONE);
        } else {
            formUnavailableBanner.setVisibility(View.GONE);
            formUnavailableRow.setVisibility(View.GONE);
            formVersionText.setVisibility(View.VISIBLE);
        }
    }

    private void onPrimaryAction() {
        if ("External channel".equalsIgnoreCase(currentItem.channel)) {
            if (listener != null) listener.onViewSubmissionInstructions(currentItem);
        } else {
            if (listener != null) listener.onSubmitRequest(currentItem);
        }
    }

    public void configurePreviewState(String state) {
        if ("2.2a".equals(state)) {
            bindService(ServiceData.getById(2), false); // SVC-02 in-app normal
        } else if ("2.2b".equals(state)) {
            bindService(ServiceData.getById(4), false); // SVC-04 external channel
        } else if ("2.2c".equals(state)) {
            bindService(ServiceData.getById(2), true);  // SVC-02 form unavailable
        }
    }

    private TextView createBadge(Context context, String text, int bg, int textColor, Typeface font) {
        TextView badge = new TextView(context);
        badge.setText(text);
        badge.setTextColor(textColor);
        badge.setTypeface(font);
        ScreenTypography.apply(badge, 11);
        int padH = UiTheme.dp(context, 8);
        int padV = UiTheme.dp(context, 3);
        badge.setPadding(padH, padV, padH, padV);
        badge.setBackground(UiTheme.shape(context, bg, 6, 0));
        return badge;
    }

    private LinearLayout createCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 14);
        card.setPadding(pad, pad, pad, pad);
        return card;
    }

    private void addSectionHeader(Context context, LinearLayout parent, String label, Typeface bold) {
        TextView header = new TextView(context);
        header.setText(label);
        header.setTextColor(UiTheme.MUTED);
        header.setTypeface(bold);
        ScreenTypography.apply(header, 11);
        header.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        parent.addView(header);
    }

    private void addSpacing(Context context, LinearLayout parent, int dp) {
        TextView space = new TextView(context);
        space.setHeight(UiTheme.dp(context, dp));
        parent.addView(space);
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
