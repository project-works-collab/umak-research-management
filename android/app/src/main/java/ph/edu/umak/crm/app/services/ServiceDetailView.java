package ph.edu.umak.crm.app.services;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
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

    private final TextView serviceCodeBadge;
    private final LinearLayout serviceChannelBadgeContainer;
    private final ImageView serviceChannelIcon;
    private final TextView serviceChannelBadgeText;
    private final TextView titleView;
    private final TextView purposeText;
    private final LinearLayout whoCanApplyTagsContainer;
    private final TextView whoCanApplyText;
    private final TextView responsibleContactText;
    private final TextView responsibleEmailText;
    private final LinearLayout instructionsContainer;
    private final LinearLayout documentsContainer;

    // Approved Form components
    private final LinearLayout approvedFormBox;
    private final FrameLayout docFrame;
    private final ImageView formDocIcon;
    private final TextView formTitleText;
    private final TextView formVersionText;
    private final TextView formErrorMsg;
    private final FrameLayout downloadFrame;
    private final ImageView dlIcon;
    private final LinearLayout formUnavailableBanner;

    // Primary CTA Button
    private final LinearLayout actionBtn;
    private final ImageView actionBtnIcon;
    private final TextView actionBtnText;

    public ServiceDetailView(Context context, ServiceData.ServiceItem service, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.currentItem = service != null ? service : ServiceData.getById(2);

        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, UiTheme.dp(context, 32));

        titleView = new TextView(context);
        titleView.setText(currentItem.name);

        // 1. Badges row: Code badge + Channel badge
        LinearLayout badgesRow = new LinearLayout(context);
        badgesRow.setOrientation(LinearLayout.HORIZONTAL);
        badgesRow.setGravity(Gravity.CENTER_VERTICAL);
        badgesRow.setPadding(0, 0, 0, UiTheme.dp(context, 14));

        serviceCodeBadge = createBadge(context, currentItem.code, 0xff131c4b, 0xffffffff, bold);
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(-2, -2);
        codeParams.rightMargin = UiTheme.dp(context, 8);
        badgesRow.addView(serviceCodeBadge, codeParams);

        // Channel badge container with optional external link icon
        serviceChannelBadgeContainer = new LinearLayout(context);
        serviceChannelBadgeContainer.setOrientation(LinearLayout.HORIZONTAL);
        serviceChannelBadgeContainer.setGravity(Gravity.CENTER_VERTICAL);
        serviceChannelBadgeContainer.setPadding(UiTheme.dp(context, 8), UiTheme.dp(context, 3), UiTheme.dp(context, 8), UiTheme.dp(context, 3));

        serviceChannelIcon = new ImageView(context);
        serviceChannelIcon.setImageResource(R.drawable.ic_crm_external);
        int chIconSize = UiTheme.dp(context, 12);
        LinearLayout.LayoutParams chIconLp = new LinearLayout.LayoutParams(chIconSize, chIconSize);
        chIconLp.rightMargin = UiTheme.dp(context, 4);
        serviceChannelBadgeContainer.addView(serviceChannelIcon, chIconLp);

        serviceChannelBadgeText = new TextView(context);
        serviceChannelBadgeText.setTypeface(bold);
        ScreenTypography.apply(serviceChannelBadgeText, 11);
        serviceChannelBadgeContainer.addView(serviceChannelBadgeText);

        badgesRow.addView(serviceChannelBadgeContainer);
        layout.addView(badgesRow);

        // 2. Section: Purpose
        LinearLayout purposeCard = createCard(context);
        addCardHeader(context, purposeCard, "Purpose", bold);
        purposeText = new TextView(context);
        purposeText.setText(currentItem.purpose);
        purposeText.setTextColor(0xff1f2937);
        purposeText.setTypeface(body);
        ScreenTypography.apply(purposeText, 13);
        purposeText.setLineSpacing(0, 1.2f);
        purposeCard.addView(purposeText);
        layout.addView(purposeCard);

        addSpacing(context, layout, 12);

        // 3. Section: Who can apply
        LinearLayout applyCard = createCard(context);
        addCardHeader(context, applyCard, "Who can apply", bold);
        whoCanApplyTagsContainer = new LinearLayout(context);
        whoCanApplyTagsContainer.setOrientation(LinearLayout.HORIZONTAL);
        whoCanApplyTagsContainer.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        applyCard.addView(whoCanApplyTagsContainer);

        whoCanApplyText = new TextView(context);
        whoCanApplyText.setText(currentItem.whoCanApplyText);
        whoCanApplyText.setTextColor(0xff586b83);
        whoCanApplyText.setTypeface(body);
        ScreenTypography.apply(whoCanApplyText, 13);
        applyCard.addView(whoCanApplyText);
        layout.addView(applyCard);

        addSpacing(context, layout, 12);

        // 4. Section: Responsible contact
        LinearLayout contactCard = createCard(context);
        addCardHeader(context, contactCard, "Responsible contact", bold);

        LinearLayout row1 = new LinearLayout(context);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);
        ImageView bldgIcon = new ImageView(context);
        bldgIcon.setImageResource(R.drawable.ic_crm_building);
        bldgIcon.setColorFilter(0xff586b83);
        int icon18 = UiTheme.dp(context, 18);
        row1.addView(bldgIcon, new LinearLayout.LayoutParams(icon18, icon18));

        responsibleContactText = new TextView(context);
        responsibleContactText.setText(currentItem.responsibleContact);
        responsibleContactText.setTextColor(0xff082d50);
        responsibleContactText.setTypeface(bold);
        ScreenTypography.apply(responsibleContactText, 13);
        LinearLayout.LayoutParams rcLp = new LinearLayout.LayoutParams(0, -2, 1);
        rcLp.setMarginStart(UiTheme.dp(context, 8));
        row1.addView(responsibleContactText, rcLp);
        contactCard.addView(row1);

        LinearLayout row2 = new LinearLayout(context);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(Gravity.CENTER_VERTICAL);
        row2.setPadding(0, UiTheme.dp(context, 8), 0, 0);
        ImageView mailIcon = new ImageView(context);
        mailIcon.setImageResource(R.drawable.ic_crm_email);
        mailIcon.setColorFilter(0xff185d9d);
        row2.addView(mailIcon, new LinearLayout.LayoutParams(icon18, icon18));

        responsibleEmailText = new TextView(context);
        responsibleEmailText.setText(currentItem.contactEmail);
        responsibleEmailText.setTextColor(0xff185d9d);
        responsibleEmailText.setTypeface(body);
        ScreenTypography.apply(responsibleEmailText, 13);
        LinearLayout.LayoutParams reLp = new LinearLayout.LayoutParams(0, -2, 1);
        reLp.setMarginStart(UiTheme.dp(context, 8));
        row2.addView(responsibleEmailText, reLp);
        contactCard.addView(row2);

        layout.addView(contactCard);

        addSpacing(context, layout, 12);

        // 5. Section: Instructions
        LinearLayout instCard = createCard(context);
        addCardHeader(context, instCard, "Instructions", bold);
        instructionsContainer = new LinearLayout(context);
        instructionsContainer.setOrientation(LinearLayout.VERTICAL);
        instCard.addView(instructionsContainer);
        layout.addView(instCard);

        addSpacing(context, layout, 12);

        // 6. Section: Documents to prepare
        LinearLayout docsCard = createCard(context);
        addCardHeader(context, docsCard, "Documents to prepare", bold);
        documentsContainer = new LinearLayout(context);
        documentsContainer.setOrientation(LinearLayout.VERTICAL);
        docsCard.addView(documentsContainer);

        TextView docNote = new TextView(context);
        docNote.setText("PDF or DOCX · up to 10 MB each · up to 5 files");
        docNote.setTextColor(0xff586b83);
        docNote.setTypeface(body);
        ScreenTypography.apply(docNote, 12);
        docNote.setPadding(0, UiTheme.dp(context, 10), 0, 0);
        docsCard.addView(docNote);
        layout.addView(docsCard);

        addSpacing(context, layout, 14);

        // 7. Section: Approved forms
        addCardHeader(context, layout, "Approved forms", bold);

        approvedFormBox = createCard(context);
        LinearLayout formRow = new LinearLayout(context);
        formRow.setOrientation(LinearLayout.HORIZONTAL);
        formRow.setGravity(Gravity.CENTER_VERTICAL);

        docFrame = new FrameLayout(context);
        docFrame.setBackground(UiTheme.shape(context, 0xffe5edf7, 6, 0));
        formDocIcon = new ImageView(context);
        formDocIcon.setImageResource(R.drawable.ic_crm_doc);
        formDocIcon.setColorFilter(0xff185d9d);
        int docIconSize = UiTheme.dp(context, 20);
        docFrame.addView(formDocIcon, new FrameLayout.LayoutParams(docIconSize, docIconSize, Gravity.CENTER));
        int docFrameSize = UiTheme.dp(context, 38);
        formRow.addView(docFrame, new LinearLayout.LayoutParams(docFrameSize, docFrameSize));

        LinearLayout formCol = new LinearLayout(context);
        formCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams formColLp = new LinearLayout.LayoutParams(0, -2, 1);
        formColLp.setMarginStart(UiTheme.dp(context, 10));

        formTitleText = new TextView(context);
        formTitleText.setText(currentItem.formTitle);
        formTitleText.setTextColor(0xff082d50);
        formTitleText.setTypeface(bold);
        ScreenTypography.apply(formTitleText, 14);
        formCol.addView(formTitleText);

        formVersionText = new TextView(context);
        formVersionText.setText(currentItem.formVersion);
        formVersionText.setTextColor(0xff586b83);
        formVersionText.setTypeface(body);
        ScreenTypography.apply(formVersionText, 12);
        formVersionText.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        formCol.addView(formVersionText);

        formErrorMsg = new TextView(context);
        formErrorMsg.setText("Unable to open the form");
        formErrorMsg.setTextColor(0xffb45309);
        formErrorMsg.setTypeface(body);
        ScreenTypography.apply(formErrorMsg, 12);
        formErrorMsg.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        formErrorMsg.setVisibility(View.GONE);
        formCol.addView(formErrorMsg);

        formRow.addView(formCol, formColLp);

        downloadFrame = new FrameLayout(context);
        downloadFrame.setBackground(UiTheme.shape(context, 0xffe5edf7, 6, 0));
        dlIcon = new ImageView(context);
        dlIcon.setImageResource(R.drawable.ic_crm_download);
        dlIcon.setColorFilter(0xff185d9d);
        int dlIconSize = UiTheme.dp(context, 18);
        downloadFrame.addView(dlIcon, new FrameLayout.LayoutParams(dlIconSize, dlIconSize, Gravity.CENTER));
        formRow.addView(downloadFrame, new LinearLayout.LayoutParams(docFrameSize, docFrameSize));

        approvedFormBox.addView(formRow);
        layout.addView(approvedFormBox);

        // Form unavailable amber alert banner (placed directly below approvedFormBox)
        formUnavailableBanner = new LinearLayout(context);
        formUnavailableBanner.setOrientation(LinearLayout.HORIZONTAL);
        formUnavailableBanner.setGravity(Gravity.CENTER_VERTICAL);
        formUnavailableBanner.setBackground(UiTheme.shape(context, 0xfffffbeb, 8, 0xfffde68a));
        int bannerPad = UiTheme.dp(context, 12);
        formUnavailableBanner.setPadding(bannerPad, bannerPad, bannerPad, bannerPad);
        formUnavailableBanner.setVisibility(View.GONE);

        ImageView alertIcon = new ImageView(context);
        alertIcon.setImageResource(R.drawable.ic_crm_alert);
        alertIcon.setColorFilter(0xffd97706);
        formUnavailableBanner.addView(alertIcon, new LinearLayout.LayoutParams(icon18, icon18));

        TextView alertText = new TextView(context);
        alertText.setText("The approved form could not be loaded. You can still read the instructions and requirements, or contact CRM for a copy.");
        alertText.setTextColor(0xff92400e);
        alertText.setTypeface(body);
        ScreenTypography.apply(alertText, 12);
        alertText.setLineSpacing(0, 1.15f);
        LinearLayout.LayoutParams alertLp = new LinearLayout.LayoutParams(0, -2, 1);
        alertLp.setMarginStart(UiTheme.dp(context, 10));
        formUnavailableBanner.addView(alertText, alertLp);

        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(-1, -2);
        bannerParams.topMargin = UiTheme.dp(context, 8);
        layout.addView(formUnavailableBanner, bannerParams);

        addSpacing(context, layout, 14);

        // 8. Navigation & Contact Links (Rows with icon, text and chevron)
        LinearLayout navLinkCard = createLinkCard(context, R.drawable.ic_crm_compass, "App navigation guidance", () -> {
            if (listener != null) listener.onOpenNavigationGuidance();
        });
        layout.addView(navLinkCard);

        addSpacing(context, layout, 8);

        LinearLayout contactLinkCard = createLinkCard(context, R.drawable.ic_crm_email, "Contact CRM", () -> {
            if (listener != null) listener.onContactCrm();
        });
        layout.addView(contactLinkCard);

        addSpacing(context, layout, 16);

        // 9. Primary Action Button (Yellow for in-app, Dark Navy with ↗ for external)
        actionBtn = new LinearLayout(context);
        actionBtn.setOrientation(LinearLayout.HORIZONTAL);
        actionBtn.setGravity(Gravity.CENTER);
        actionBtn.setClickable(true);
        actionBtn.setFocusable(true);
        actionBtn.setOnClickListener(v -> onPrimaryAction());

        actionBtnIcon = new ImageView(context);
        actionBtnIcon.setImageResource(R.drawable.ic_crm_external);
        actionBtnIcon.setColorFilter(0xffffffff);
        actionBtnIcon.setVisibility(View.GONE);
        LinearLayout.LayoutParams btnIconLp = new LinearLayout.LayoutParams(UiTheme.dp(context, 18), UiTheme.dp(context, 18));
        btnIconLp.rightMargin = UiTheme.dp(context, 8);
        actionBtn.addView(actionBtnIcon, btnIconLp);

        actionBtnText = new TextView(context);
        actionBtnText.setTypeface(bold);
        ScreenTypography.apply(actionBtnText, 15);
        actionBtn.addView(actionBtnText);

        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 48));
        layout.addView(actionBtn, actionParams);

        bindService(this.currentItem, false);
    }

    private LinearLayout createLinkCard(Context ctx, int iconRes, String title, Runnable onClick) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(UiTheme.ripple(UiTheme.shape(ctx, UiTheme.WHITE, 8, UiTheme.BORDER), 0x20395072));
        int pad = UiTheme.dp(ctx, 12);
        row.setPadding(pad, pad, pad, pad);
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> onClick.run());

        ImageView icon = new ImageView(ctx);
        icon.setImageResource(iconRes);
        icon.setColorFilter(0xff185d9d);
        int iconSize = UiTheme.dp(ctx, 18);
        row.addView(icon, new LinearLayout.LayoutParams(iconSize, iconSize));

        TextView text = new TextView(ctx);
        text.setText(title);
        text.setTextColor(0xff185d9d);
        text.setTypeface(UiTheme.boldFont(ctx));
        ScreenTypography.apply(text, 13);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, -2, 1);
        textLp.setMarginStart(UiTheme.dp(ctx, 10));
        row.addView(text, textLp);

        TextView chevron = new TextView(ctx);
        chevron.setText("›");
        chevron.setTextColor(0xff586b83);
        chevron.setTextSize(16);
        row.addView(chevron);

        return row;
    }

    public void bindService(ServiceData.ServiceItem item, boolean formUnavailable) {
        this.currentItem = item;
        titleView.setText(item.name);
        serviceCodeBadge.setText(item.code);

        boolean isExternal = "External channel".equalsIgnoreCase(item.channel);
        if (isExternal) {
            serviceChannelBadgeContainer.setBackground(UiTheme.shape(context, 0xfffffbeb, 6, 0xfffde68a));
            serviceChannelIcon.setVisibility(View.VISIBLE);
            serviceChannelIcon.setColorFilter(0xffb45309);
            serviceChannelBadgeText.setText("External channel");
            serviceChannelBadgeText.setTextColor(0xffb45309);
        } else {
            serviceChannelBadgeContainer.setBackground(UiTheme.shape(context, 0xffe5edf7, 6, 0));
            serviceChannelIcon.setVisibility(View.GONE);
            serviceChannelBadgeText.setText("Submit in app");
            serviceChannelBadgeText.setTextColor(0xff185d9d);
        }

        purposeText.setText(item.purpose);

        // Who can apply tags (exclude redundant Student/Faculty tags)
        whoCanApplyTagsContainer.removeAllViews();
        for (String tag : item.whoCanApplyTags) {
            if ("Student".equalsIgnoreCase(tag) || "Faculty".equalsIgnoreCase(tag)) {
                continue;
            }
            TextView tagPill = createBadge(context, tag, 0xffe5edf7, 0xff185d9d, UiTheme.boldFont(context));
            LinearLayout.LayoutParams tagLp = new LinearLayout.LayoutParams(-2, -2);
            tagLp.rightMargin = UiTheme.dp(context, 6);
            whoCanApplyTagsContainer.addView(tagPill, tagLp);
        }
        whoCanApplyTagsContainer.setVisibility(whoCanApplyTagsContainer.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        whoCanApplyText.setText(item.whoCanApplyText);

        responsibleContactText.setText(item.responsibleContact);
        responsibleEmailText.setText(item.contactEmail);

        // Instructions steps (1 to 5 with orange badges)
        instructionsContainer.removeAllViews();
        for (int i = 0; i < item.instructions.length; i++) {
            LinearLayout stepRow = new LinearLayout(context);
            stepRow.setOrientation(LinearLayout.HORIZONTAL);
            stepRow.setPadding(0, 0, 0, i == item.instructions.length - 1 ? 0 : UiTheme.dp(context, 10));

            TextView num = new TextView(context);
            num.setText(String.valueOf(i + 1));
            num.setTextColor(0xffffffff);
            num.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(num, 11);
            num.setBackground(UiTheme.shape(context, 0xffe67e22, 11, 0));
            num.setGravity(Gravity.CENTER);
            int numSize = UiTheme.dp(context, 22);
            LinearLayout.LayoutParams numLp = new LinearLayout.LayoutParams(numSize, numSize);
            numLp.rightMargin = UiTheme.dp(context, 10);
            stepRow.addView(num, numLp);

            TextView stepText = new TextView(context);
            stepText.setText(item.instructions[i]);
            stepText.setTextColor(0xff1f2937);
            stepText.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(stepText, 13);
            stepRow.addView(stepText, new LinearLayout.LayoutParams(0, -2, 1));

            instructionsContainer.addView(stepRow);
        }

        // Documents list with document icon and Required/Optional pill
        documentsContainer.removeAllViews();
        for (int i = 0; i < item.documents.length; i++) {
            ServiceData.DocumentItem doc = item.documents[i];
            LinearLayout docRow = new LinearLayout(context);
            docRow.setOrientation(LinearLayout.HORIZONTAL);
            docRow.setGravity(Gravity.CENTER_VERTICAL);
            docRow.setPadding(0, 0, 0, i == item.documents.length - 1 ? 0 : UiTheme.dp(context, 10));

            ImageView docIcon = new ImageView(context);
            docIcon.setImageResource(R.drawable.ic_crm_doc);
            docIcon.setColorFilter(0xff586b83);
            int docIconSize = UiTheme.dp(context, 16);
            docRow.addView(docIcon, new LinearLayout.LayoutParams(docIconSize, docIconSize));

            TextView docTitle = new TextView(context);
            docTitle.setText(doc.title);
            docTitle.setTextColor(0xff1f2937);
            docTitle.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(docTitle, 13);
            LinearLayout.LayoutParams docTitleLp = new LinearLayout.LayoutParams(0, -2, 1);
            docTitleLp.setMarginStart(UiTheme.dp(context, 8));
            docRow.addView(docTitle, docTitleLp);

            TextView pill = createBadge(context, doc.required ? "Required" : "Optional",
                    doc.required ? 0xfffef3c7 : 0xfff3f4f6,
                    doc.required ? 0xffb45309 : 0xff6b7280,
                    UiTheme.boldFont(context));
            docRow.addView(pill);

            documentsContainer.addView(docRow);
        }

        // Approved form
        formTitleText.setText(item.formTitle);
        formVersionText.setText(item.formVersion);
        boolean canDownload = item.revisionId == 0 || item.formMime != null;
        approvedFormBox.setClickable(canDownload);
        approvedFormBox.setFocusable(canDownload);
        approvedFormBox.setContentDescription(canDownload ? "Download " + item.formTitle : null);

        setFormUnavailable(formUnavailable);

        // Action button layout & styling
        if (isExternal) {
            actionBtn.setBackground(UiTheme.shape(context, 0xff131c4b, 8, 0));
            actionBtnIcon.setVisibility(View.VISIBLE);
            actionBtnIcon.setColorFilter(0xffffffff);
            actionBtnText.setText("View Submission Instructions");
            actionBtnText.setTextColor(0xffffffff);
        } else {
            actionBtn.setBackground(UiTheme.shape(context, 0xfff8eb37, 8, 0));
            actionBtnIcon.setVisibility(View.GONE);
            actionBtnText.setText("Submit Request");
            actionBtnText.setTextColor(0xff131c4b);
        }
    }

    public boolean isAttached() {
        return layout.isAttachedToWindow();
    }

    public void showFormLoading() {
        setFormUnavailable(false);
        approvedFormBox.setClickable(false);
        formVersionText.setText("Downloading form…");
    }

    public void setFormUnavailable(boolean unavailable) {
        approvedFormBox.setClickable(true);
        if (unavailable) {
            approvedFormBox.setBackground(UiTheme.shape(context, 0xfffffbeb, 8, 0xfffde68a));
            docFrame.setBackground(UiTheme.shape(context, 0xfffef3c7, 6, 0));
            formDocIcon.setColorFilter(0xffb45309);
            formTitleText.setTextColor(0xff92400e);
            formVersionText.setVisibility(View.GONE);
            formErrorMsg.setVisibility(View.VISIBLE);

            downloadFrame.setBackground(UiTheme.shape(context, 0xfffef3c7, 6, 0));
            dlIcon.setImageResource(R.drawable.ic_crm_refresh);
            dlIcon.setColorFilter(0xffb45309);

            formUnavailableBanner.setVisibility(View.VISIBLE);
            approvedFormBox.setOnClickListener(v -> {
                if (listener != null) listener.onOpenForm(currentItem, true);
            });
        } else {
            approvedFormBox.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
            docFrame.setBackground(UiTheme.shape(context, 0xffe5edf7, 6, 0));
            formDocIcon.setColorFilter(0xff185d9d);
            formTitleText.setTextColor(0xff082d50);
            formVersionText.setText(currentItem.formVersion);
            formVersionText.setVisibility(View.VISIBLE);
            formErrorMsg.setVisibility(View.GONE);

            downloadFrame.setBackground(UiTheme.shape(context, 0xffe5edf7, 6, 0));
            dlIcon.setImageResource(R.drawable.ic_crm_download);
            dlIcon.setColorFilter(0xff185d9d);

            formUnavailableBanner.setVisibility(View.GONE);
            approvedFormBox.setOnClickListener(v -> {
                if (listener != null) listener.onOpenForm(currentItem, false);
            });
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

    private void addCardHeader(Context context, LinearLayout parent, String title, Typeface bold) {
        TextView header = new TextView(context);
        header.setText(title);
        header.setTextColor(0xff082d50);
        header.setTypeface(bold);
        ScreenTypography.apply(header, 14);
        header.setPadding(0, 0, 0, UiTheme.dp(context, 8));
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
        return PageTitle.wrap(layout, titleView, null, false);
    }
}
