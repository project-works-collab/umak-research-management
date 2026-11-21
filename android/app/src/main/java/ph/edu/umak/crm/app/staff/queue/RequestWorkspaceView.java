package ph.edu.umak.crm.app.staff.queue;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.dialogs.UiDialog;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * 6.3a - 6.6 Request Workspace View
 * Handles review checklist, evaluation, decision making, validation error states,
 * stale data conflicts, completed corrections, and review saved confirmations.
 */
public class RequestWorkspaceView {

    public interface Callbacks {
        void onBackToQueue();
        void onBackToQueueCompleted();
        void onSessionDraftKept();
    }

    private final Context context;
    private final Callbacks callbacks;
    private final FrameLayout rootLayout;
    private final LinearLayout mainLayout;
    private final LinearLayout contentLayout;

    private StaffQueueData.QueueRequestItem requestItem;
    private String previewState = "6.3a_workspace";
    private final Map<String, StaffQueueData.QueueRequestItem> sessionDrafts = new HashMap<>();

    // Form controls for Decision
    private String selectedNextStatus = "For correction";
    private EditText feedbackInput;
    private final List<String> missingItemsList = new ArrayList<>();
    private LinearLayout missingItemsContainer;
    private EditText nextActionInput;
    private EditText deadlineDateInput;
    private EditText deadlineTimeInput;
    private EditText internalRemarksInput;
    private EditText checkpointNameInput;
    private EditText checkpointDateInput;
    private EditText checkpointRefInput;

    // Completed correction controls
    private EditText correctionReasonInput;

    public RequestWorkspaceView(Context context, Callbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;

        rootLayout = new FrameLayout(context);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setBackgroundColor(UiTheme.PAPER);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));

        mainLayout.addView(contentLayout);
        rootLayout.addView(mainLayout);

        setRequest(StaffQueueData.getInitialQueue().get(0)); // REQ-2026-089 default
    }

    public View getContentLayout() {
        if (rootLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) rootLayout.getParent()).removeView(rootLayout);
        }
        buildView();
        return rootLayout;
    }

    private boolean isDecisionState() {
        return "6.3b_decision".equalsIgnoreCase(previewState)
                || "6.4a_error_missing".equalsIgnoreCase(previewState)
                || "6.4b_error_status_not_allowed".equalsIgnoreCase(previewState)
                || "6.4c_stale_conflict".equalsIgnoreCase(previewState);
    }

    public void setRequest(StaffQueueData.QueueRequestItem request) {
        previewState = "6.3a_workspace";
        request = request == null ? null : sessionDrafts.getOrDefault(request.id, request);
        bindRequest(request);
    }

    private void bindRequest(StaffQueueData.QueueRequestItem request) {
        this.requestItem = request;
        if (request != null) {
            this.selectedNextStatus = request.targetStatus != null ? request.targetStatus : "For correction";
            this.missingItemsList.clear();
            if (request.missingItems != null) {
                this.missingItemsList.addAll(request.missingItems);
            }
        }
        buildView();
    }

    public void setPreviewState(String state) {
        this.previewState = state;
        if ("6.5_completed_correction".equalsIgnoreCase(state)) {
            for (StaffQueueData.QueueRequestItem item : StaffQueueData.getInitialQueue()) {
                if ("REQ-2026-074".equalsIgnoreCase(item.id)) {
                    bindRequest(item);
                    return;
                }
            }
        } else {
            for (StaffQueueData.QueueRequestItem item : StaffQueueData.getInitialQueue()) {
                if ("REQ-2026-089".equalsIgnoreCase(item.id)) {
                    bindRequest(item);
                    return;
                }
            }
        }
        buildView();
    }

    private void buildView() {
        rootLayout.removeAllViews();
        mainLayout.removeAllViews();
        contentLayout.removeAllViews();
        if (requestItem == null) return;

        Typeface bold = UiTheme.boldFont(context);

        // 1. Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(context);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = UiTheme.dp(context, 16);
        band.setPadding(pad, pad, pad, pad);

        boolean isDecision = isDecisionState();

        if (isDecision) {
            TextView tag = new TextView(context);
            tag.setText(requestItem.id != null ? requestItem.id : "REQ-2026-089");
            tag.setTextColor(UiTheme.PALE);
            tag.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(tag, 12);
            band.addView(tag);

            TextView heading = new TextView(context);
            heading.setText("Request Workspace · Decision");
            heading.setTextColor(0xffffffff);
            heading.setTypeface(UiTheme.headingFont(context));
            ScreenTypography.apply(heading, 24);
            ScreenTypography.heading(heading);
            band.addView(heading);
        } else {
            TextView heading = new TextView(context);
            heading.setText("Request Workspace");
            heading.setTextColor(0xffffffff);
            heading.setTypeface(UiTheme.headingFont(context));
            ScreenTypography.apply(heading, 24);
            ScreenTypography.heading(heading);
            band.addView(heading);

            TextView copy = new TextView(context);
            copy.setText("Review and process service requests.");
            copy.setTextColor(UiTheme.PALE);
            copy.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(copy, 13);
            band.addView(copy);
        }

        mainLayout.addView(band);
        mainLayout.addView(contentLayout);
        rootLayout.addView(mainLayout);

        // 2. Build Content Cards
        boolean isCompleted = "Completed".equalsIgnoreCase(requestItem.status)
                || "6.5_completed_correction".equalsIgnoreCase(previewState);

        if (isCompleted) {
            // 6.5 Completed Request Correction Mode
            contentLayout.addView(createRequestHeaderCard());
            addSpacing(14);
            contentLayout.addView(createAuthorizedCorrectionCard());
            addSpacing(14);
            contentLayout.addView(createCompletedFeedbackCard());
            addSpacing(14);
            contentLayout.addView(createCompletedCheckpointCard());
            addSpacing(20);
            contentLayout.addView(createCompletedActionButtons());
        } else if (isDecision) {
            // 6.3b / 6.4a / 6.4b / 6.4c Decision Mode
            contentLayout.addView(createDecisionSection());
        } else {
            // 6.3a Workspace Part 1 / 6.6 Review Saved Mode
            contentLayout.addView(createRequestHeaderCard());
            addSpacing(14);

            contentLayout.addView(createRequesterProfileCard());
            addSpacing(14);

            if (!requestItem.attachments.isEmpty()) {
                contentLayout.addView(createAttachmentsCard());
                addSpacing(14);
            }

            contentLayout.addView(createChecklistCard());
            addSpacing(14);

            contentLayout.addView(createEarlierReviewsCard());

            if (!"6.6_review_saved".equalsIgnoreCase(previewState)) {
                addSpacing(20);

                // Centered Link: "Continue to review decision ↓"
                TextView continueBtn = new TextView(context);
                continueBtn.setText("Continue to review decision ↓");
                continueBtn.setTextColor(0xff185d9d);
                continueBtn.setTypeface(bold);
                continueBtn.setGravity(Gravity.CENTER);
                ScreenTypography.apply(continueBtn, 14);
                continueBtn.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 8));
                continueBtn.setOnClickListener(v -> setPreviewState("6.3b_decision"));
                contentLayout.addView(continueBtn);
            }
        }

        // 3. Overlays: 6.4c Modal or 6.6 Floating Toast
        if ("6.4c_stale_conflict".equalsIgnoreCase(previewState)) {
            // Dark scrim
            View scrim = new View(context);
            scrim.setBackgroundColor(0x99000000);
            scrim.setClickable(true);
            rootLayout.addView(scrim, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // Centered Modal Dialog Card
            View modal = createStaleModalCard();
            FrameLayout.LayoutParams mLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mLp.gravity = Gravity.CENTER;
            mLp.leftMargin = UiTheme.dp(context, 24);
            mLp.rightMargin = UiTheme.dp(context, 24);
            rootLayout.addView(modal, mLp);
        } else if ("6.6_review_saved".equalsIgnoreCase(previewState)) {
            // Floating bottom toast pill
            View toast = createReviewSavedToast();
            FrameLayout.LayoutParams tLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            tLp.bottomMargin = UiTheme.dp(context, 20);
            rootLayout.addView(toast, tLp);
        }
    }

    private View createRequestHeaderCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        // Top row: ID and Status Badge
        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView idView = new TextView(context);
        idView.setText(requestItem.id);
        idView.setTextColor(0xff185d9d);
        idView.setTypeface(bold);
        ScreenTypography.apply(idView, 12);
        LinearLayout.LayoutParams idLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        topRow.addView(idView, idLp);

        String displayStatus = "6.6_review_saved".equalsIgnoreCase(previewState) ? "For correction" : requestItem.status;
        TextView badge = createStatusBadge(displayStatus);
        topRow.addView(badge);
        card.addView(topRow);

        addSpacing(card, 6);

        // Service Type
        TextView sView = new TextView(context);
        sView.setText(requestItem.serviceType);
        sView.setTextColor(0xff082d50);
        sView.setTypeface(bold);
        ScreenTypography.apply(sView, 18);
        card.addView(sView);

        // Study Title
        TextView tView = new TextView(context);
        tView.setText(requestItem.studyTitle);
        tView.setTextColor(0xff64748b);
        tView.setTypeface(regular);
        ScreenTypography.apply(tView, 13);
        tView.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 4));
        card.addView(tView);

        // Submitted info
        TextView dView = new TextView(context);
        dView.setText("Submitted " + requestItem.submittedAt);
        dView.setTextColor(0xff64748b);
        dView.setTypeface(regular);
        ScreenTypography.apply(dView, 12);
        card.addView(dView);

        return card;
    }

    private View createRequesterProfileCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Requester profile");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 12);

        // Avatar Row
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = new TextView(context);
        avatar.setText("LJ");
        avatar.setTextColor(0xff185d9d);
        avatar.setTypeface(bold);
        ScreenTypography.apply(avatar, 14);
        avatar.setGravity(Gravity.CENTER);
        int aSize = UiTheme.dp(context, 42);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(aSize, aSize);
        aLp.setMarginEnd(UiTheme.dp(context, 12));
        avatar.setLayoutParams(aLp);

        GradientDrawable aBg = new GradientDrawable();
        aBg.setShape(GradientDrawable.OVAL);
        aBg.setColor(Color.parseColor("#E0F2FE"));
        avatar.setBackground(aBg);
        row.addView(avatar);

        LinearLayout col = new LinearLayout(context);
        col.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(context);
        name.setText(requestItem.requesterName);
        name.setTextColor(0xff082d50);
        name.setTypeface(bold);
        ScreenTypography.apply(name, 16);
        col.addView(name);

        TextView role = new TextView(context);
        role.setText(requestItem.requesterRole);
        role.setTypeface(bold);
        ScreenTypography.apply(role, 11);
        int px = UiTheme.dp(context, 7);
        int py = UiTheme.dp(context, 2);
        role.setPadding(px, py, px, py);
        role.setBackground(UiTheme.shape(context, 0xffe0f2fe, 4, 0));
        role.setTextColor(0xff0284c7);
        role.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        col.addView(role);

        row.addView(col);
        card.addView(row);

        addSpacing(card, 12);

        // College
        TextView cLbl = new TextView(context);
        cLbl.setText("College");
        cLbl.setTextColor(0xff586b83);
        cLbl.setTypeface(regular);
        ScreenTypography.apply(cLbl, 12);
        card.addView(cLbl);

        TextView cVal = new TextView(context);
        cVal.setText(requestItem.requesterCollege);
        cVal.setTextColor(0xff082d50);
        cVal.setTypeface(regular);
        ScreenTypography.apply(cVal, 13);
        card.addView(cVal);

        addSpacing(card, 8);

        // Email
        TextView eLbl = new TextView(context);
        eLbl.setText("Email");
        eLbl.setTextColor(0xff586b83);
        eLbl.setTypeface(regular);
        ScreenTypography.apply(eLbl, 12);
        card.addView(eLbl);

        TextView eVal = new TextView(context);
        eVal.setText(requestItem.requesterEmail);
        eVal.setTextColor(0xff082d50);
        eVal.setTypeface(regular);
        ScreenTypography.apply(eVal, 13);
        card.addView(eVal);

        return card;
    }

    private View createAttachmentsCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Attachments");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 10);

        for (StaffQueueData.AttachmentItem att : requestItem.attachments) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

            ImageView dIcon = new ImageView(context);
            dIcon.setImageResource(R.drawable.ic_crm_doc);
            dIcon.setColorFilter(0xff185d9d);
            int dSize = UiTheme.dp(context, 18);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(dSize, dSize);
            dLp.rightMargin = UiTheme.dp(context, 10);
            row.addView(dIcon, dLp);

            TextView fName = new TextView(context);
            fName.setText(att.fileName);
            fName.setTextColor(0xff082d50);
            fName.setTypeface(bold);
            ScreenTypography.apply(fName, 13);
            LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(fName, fLp);

            TextView fSize = new TextView(context);
            fSize.setText(att.fileSize);
            fSize.setTextColor(0xff64748b);
            fSize.setTypeface(regular);
            ScreenTypography.apply(fSize, 12);
            row.addView(fSize);

            card.addView(row);
        }

        return card;
    }

    private View createChecklistCard() {
        Typeface bold = UiTheme.boldFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Evaluation Checklist");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 12);

        String[] optionsRow1 = {"Pending", "Verified", "Needs correction"};
        String optionRow2 = "Not applicable";

        boolean isReviewSaved = "6.6_review_saved".equalsIgnoreCase(previewState);

        for (int i = 0; i < requestItem.checklist.size(); i++) {
            StaffQueueData.ChecklistItem item = requestItem.checklist.get(i);
            String initialStatus = item.status;
            if (isReviewSaved) {
                if (i == 0) initialStatus = "Verified";
                else if (i == 1) initialStatus = "Verified";
                else if (i == 2) initialStatus = "Needs correction";
                else if (i == 3) initialStatus = "Pending";
            }

            TextView itemTitle = new TextView(context);
            itemTitle.setText(item.title);
            itemTitle.setTextColor(0xff082d50);
            itemTitle.setTypeface(bold);
            ScreenTypography.apply(itemTitle, 13);
            card.addView(itemTitle);

            addSpacing(card, 6);

            // Row 1: Pending | Verified | Needs correction
            HorizontalScrollView hsv = new HorizontalScrollView(context);
            hsv.setHorizontalScrollBarEnabled(false);
            LinearLayout chipRow = new LinearLayout(context);
            chipRow.setOrientation(LinearLayout.HORIZONTAL);

            List<TextView> allItemChips = new ArrayList<>();
            final String currentStatus = initialStatus;

            for (String opt : optionsRow1) {
                TextView chip = new TextView(context);
                chip.setText(opt);
                ScreenTypography.apply(chip, 11);
                chip.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 6),
                        UiTheme.dp(context, 12), UiTheme.dp(context, 6));

                updateChecklistChip(chip, opt, opt.equalsIgnoreCase(currentStatus));

                chip.setOnClickListener(v -> {
                    item.status = opt;
                    for (TextView c : allItemChips) {
                        updateChecklistChip(c, c.getTag().toString(), c.getTag().toString().equalsIgnoreCase(item.status));
                    }
                });
                chip.setTag(opt);
                allItemChips.add(chip);
                chipRow.addView(chip);

                View sp = new View(context);
                sp.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 6), 1));
                chipRow.addView(sp);
            }
            hsv.addView(chipRow);
            card.addView(hsv);

            addSpacing(card, 4);

            // Row 2: Not applicable
            TextView naChip = new TextView(context);
            naChip.setText(optionRow2);
            ScreenTypography.apply(naChip, 11);
            naChip.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 6),
                    UiTheme.dp(context, 12), UiTheme.dp(context, 6));
            updateChecklistChip(naChip, optionRow2, optionRow2.equalsIgnoreCase(currentStatus));
            naChip.setTag(optionRow2);
            naChip.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            naChip.setOnClickListener(v -> {
                item.status = optionRow2;
                for (TextView c : allItemChips) {
                    updateChecklistChip(c, c.getTag().toString(), c.getTag().toString().equalsIgnoreCase(item.status));
                }
            });
            allItemChips.add(naChip);
            card.addView(naChip);

            addSpacing(card, 12);
        }

        return card;
    }

    private void updateChecklistChip(TextView chip, String label, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(14);
        if (selected) {
            chip.setText("✓ " + label);
            bg.setColor(0xffffffff);
            bg.setStroke(2, 0xff082d50);
            chip.setTextColor(0xff082d50);
            chip.setTypeface(UiTheme.boldFont(context));
        } else {
            chip.setText(label);
            bg.setColor(0xffffffff);
            bg.setStroke(1, Color.parseColor("#CBD5E1"));
            chip.setTextColor(0xff64748b);
            chip.setTypeface(UiTheme.bodyFont(context));
        }
        chip.setBackground(bg);
    }

    private View createEarlierReviewsCard() {
        Typeface bold = UiTheme.boldFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        boolean isReviewSaved = "6.6_review_saved".equalsIgnoreCase(previewState);

        // Header row with accordion arrow
        LinearLayout top = new LinearLayout(context);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView secTitle = new TextView(context);
        secTitle.setText("Earlier reviews");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        top.addView(secTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView countView = new TextView(context);
        countView.setText(isReviewSaved ? "3 entries  ^" : "2 entries  ^");
        countView.setTextColor(0xff64748b);
        countView.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(countView, 12);
        top.addView(countView);

        card.addView(top);
        addSpacing(card, 12);

        // If 6.6 Review saved, show new entry at top!
        if (isReviewSaved) {
            card.addView(createNewTimelineEntry("For correction", 0xfff59e0b, "Lebron James · CRM Staff", "Nov 1, 2026, 3:20 PM"));
            addSpacing(card, 10);
        }

        // Timeline item: In review
        card.addView(createTimelineEntry("In review", 0xfff59e0b, "Lebron James · CRM Staff", "Oct 31, 2026, 9:05 AM"));
        addSpacing(card, 10);

        // Timeline item: Submitted
        card.addView(createTimelineEntry("Submitted", 0xff0284c7, "LeBron R. James · Researcher", "Oct 29, 2026, 10:42 AM"));

        return card;
    }

    private View createNewTimelineEntry(String statusBadgeText, int dotColor, String author, String date) {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);

        LinearLayout top = new LinearLayout(context);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        View dot = new View(context);
        dot.setBackground(UiTheme.shape(context, dotColor, 4, 0));
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(UiTheme.dp(context, 8), UiTheme.dp(context, 8));
        dLp.rightMargin = UiTheme.dp(context, 8);
        top.addView(dot, dLp);

        TextView badge = createStatusBadge(statusBadgeText);
        top.addView(badge);

        View sp = new View(context);
        sp.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 6), 1));
        top.addView(sp);

        // Bright blue "New" badge
        TextView newBadge = new TextView(context);
        newBadge.setText("New");
        newBadge.setTextColor(0xffffffff);
        newBadge.setTypeface(bold);
        ScreenTypography.apply(newBadge, 10);
        int px = UiTheme.dp(context, 6);
        int py = UiTheme.dp(context, 2);
        newBadge.setPadding(px, py, px, py);
        newBadge.setBackground(UiTheme.shape(context, 0xff0284c7, 10, 0));
        top.addView(newBadge);

        row.addView(top);

        TextView authText = new TextView(context);
        authText.setText(author);
        authText.setTextColor(0xff082d50);
        authText.setTypeface(bold);
        ScreenTypography.apply(authText, 13);
        authText.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 4), 0, 0);
        row.addView(authText);

        TextView dateText = new TextView(context);
        dateText.setText(date);
        dateText.setTextColor(0xff64748b);
        dateText.setTypeface(regular);
        ScreenTypography.apply(dateText, 12);
        dateText.setPadding(UiTheme.dp(context, 16), 0, 0, 0);
        row.addView(dateText);

        return row;
    }

    private View createTimelineEntry(String statusBadgeText, int dotColor, String author, String date) {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);

        LinearLayout top = new LinearLayout(context);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        View dot = new View(context);
        dot.setBackground(UiTheme.shape(context, dotColor, 4, 0));
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(UiTheme.dp(context, 8), UiTheme.dp(context, 8));
        dLp.rightMargin = UiTheme.dp(context, 8);
        top.addView(dot, dLp);

        TextView badge = createStatusBadge(statusBadgeText);
        top.addView(badge);
        row.addView(top);

        TextView authText = new TextView(context);
        authText.setText(author);
        authText.setTextColor(0xff082d50);
        authText.setTypeface(bold);
        ScreenTypography.apply(authText, 13);
        authText.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 4), 0, 0);
        row.addView(authText);

        TextView dateText = new TextView(context);
        dateText.setText(date);
        dateText.setTextColor(0xff64748b);
        dateText.setTypeface(regular);
        ScreenTypography.apply(dateText, 12);
        dateText.setPadding(UiTheme.dp(context, 16), 0, 0, 0);
        row.addView(dateText);

        return row;
    }

    private View createDecisionSection() {
        LinearLayout section = new LinearLayout(context);
        section.setOrientation(LinearLayout.VERTICAL);

        // Top Warning Banner for 6.4a and 6.4b
        if ("6.4a_error_missing".equalsIgnoreCase(previewState)) {
            section.addView(createWarningBanner("⚠ Enter the missing items and next action."));
            addSpacing(section, 14);
        } else if ("6.4b_error_status_not_allowed".equalsIgnoreCase(previewState)) {
            section.addView(createWarningBanner("⚠ This status change is not allowed."));
            addSpacing(section, 14);
        }

        // 1. Status Change Card
        section.addView(createStatusChangeCard());
        addSpacing(section, 14);

        // 2. Researcher-visible feedback Card
        section.addView(createFeedbackCard());
        addSpacing(section, 14);

        // 3. Missing items Card
        section.addView(createMissingItemsCard());
        addSpacing(section, 14);

        // 4. Next action Card
        section.addView(createNextActionCard());
        addSpacing(section, 14);

        // 5. Deadline Card
        section.addView(createDeadlineCard());
        addSpacing(section, 14);

        // 6. Internal remarks Card
        section.addView(createInternalRemarksCard());
        addSpacing(section, 14);

        // 7. Processing checkpoint Card
        section.addView(createCheckpointCard());
        addSpacing(section, 20);

        // Save & Action Buttons
        Button saveBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        saveBtn.setText("Save Review");
        saveBtn.setAllCaps(false);
        saveBtn.setTextColor(0xffffffff);
        saveBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(saveBtn, 15);
        saveBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        saveBtn.setMinHeight(UiTheme.dp(context, 46));
        saveBtn.setOnClickListener(v -> {
            if ("6.4a_error_missing".equalsIgnoreCase(previewState)) {
                return;
            }
            if ("6.4b_error_status_not_allowed".equalsIgnoreCase(previewState)) {
                return;
            }
            if ("6.4c_stale_conflict".equalsIgnoreCase(previewState)) {
                return;
            }
            setPreviewState("6.6_review_saved");
        });
        section.addView(saveBtn);

        addSpacing(section, 10);

        Button draftBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        draftBtn.setText("Save as Draft");
        draftBtn.setAllCaps(false);
        draftBtn.setTextColor(0xff082d50);
        draftBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(draftBtn, 15);
        draftBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        draftBtn.setMinHeight(UiTheme.dp(context, 44));
        draftBtn.setOnClickListener(v -> {
            if (requestItem != null) {
                sessionDrafts.put(requestItem.id, requestItem);
            }
            if (callbacks != null) callbacks.onSessionDraftKept();
        });
        section.addView(draftBtn);

        return section;
    }

    private View createWarningBanner(String message) {
        Typeface bold = UiTheme.boldFont(context);
        LinearLayout banner = new LinearLayout(context);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setPadding(UiTheme.dp(context, 14), UiTheme.dp(context, 12),
                UiTheme.dp(context, 14), UiTheme.dp(context, 12));
        banner.setBackground(UiTheme.shape(context, 0xfffff7ed, 8, 0xfffed7aa));

        TextView text = new TextView(context);
        text.setText(message);
        text.setTextColor(0xffc2410c);
        text.setTypeface(bold);
        ScreenTypography.apply(text, 13);
        banner.addView(text);

        return banner;
    }

    private View createStatusChangeCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        boolean isNotAllowedError = "6.4b_error_status_not_allowed".equalsIgnoreCase(previewState);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, isNotAllowedError ? 0xffef4444 : UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Status change");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 16);
        card.addView(secTitle);

        addSpacing(card, 8);

        LinearLayout curRow = new LinearLayout(context);
        curRow.setOrientation(LinearLayout.HORIZONTAL);
        curRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView curStatus = new TextView(context);
        curStatus.setText("Current status: ");
        curStatus.setTextColor(0xff586b83);
        curStatus.setTypeface(regular);
        ScreenTypography.apply(curStatus, 13);
        curRow.addView(curStatus);

        TextView sBadge = createStatusBadge(requestItem.status);
        curRow.addView(sBadge);

        if (isNotAllowedError) {
            TextView unch = new TextView(context);
            unch.setText(" (unchanged)");
            unch.setTextColor(0xff64748b);
            unch.setTypeface(regular);
            ScreenTypography.apply(unch, 13);
            curRow.addView(unch);
        }

        card.addView(curRow);

        addSpacing(card, 10);

        TextView moveLabel = new TextView(context);
        moveLabel.setText("Move to");
        moveLabel.setTextColor(0xff586b83);
        moveLabel.setTypeface(regular);
        ScreenTypography.apply(moveLabel, 13);
        card.addView(moveLabel);

        addSpacing(card, 6);

        // Two status toggle buttons side by side
        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        Button forCorrBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        Button compBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);

        if (isNotAllowedError) {
            // Both unselected in 6.4b
            forCorrBtn.setAllCaps(false);
            forCorrBtn.setText("For correction");
            forCorrBtn.setTypeface(regular);
            forCorrBtn.setTextColor(0xff64748b);
            ScreenTypography.apply(forCorrBtn, 13);
            forCorrBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

            compBtn.setAllCaps(false);
            compBtn.setText("Completed");
            compBtn.setTypeface(regular);
            compBtn.setTextColor(0xff64748b);
            ScreenTypography.apply(compBtn, 13);
            compBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        } else {
            updateMoveButtons(forCorrBtn, compBtn);
            forCorrBtn.setOnClickListener(v -> {
                selectedNextStatus = "For correction";
                updateMoveButtons(forCorrBtn, compBtn);
            });
            compBtn.setOnClickListener(v -> {
                selectedNextStatus = "Completed";
                updateMoveButtons(forCorrBtn, compBtn);
            });
        }

        btnRow.addView(forCorrBtn, new LinearLayout.LayoutParams(0, UiTheme.dp(context, 40), 1f));
        View sp = new View(context);
        sp.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 10), 1));
        btnRow.addView(sp);
        btnRow.addView(compBtn, new LinearLayout.LayoutParams(0, UiTheme.dp(context, 40), 1f));

        card.addView(btnRow);

        addSpacing(card, 8);

        if (isNotAllowedError) {
            TextView errTxt = new TextView(context);
            errTxt.setText("⚠ This status change is not allowed.");
            errTxt.setTextColor(0xffdc2626);
            errTxt.setTypeface(bold);
            ScreenTypography.apply(errTxt, 12);
            card.addView(errTxt);
        } else {
            TextView note = new TextView(context);
            note.setText("Only allowed next statuses are shown. Submitted → In review; In review → For correction or Completed.");
            note.setTextColor(0xff64748b);
            note.setTypeface(regular);
            ScreenTypography.apply(note, 12);
            card.addView(note);
        }

        return card;
    }

    private void updateMoveButtons(Button forCorrBtn, Button compBtn) {
        boolean isForCorr = "For correction".equalsIgnoreCase(selectedNextStatus);

        forCorrBtn.setAllCaps(false);
        forCorrBtn.setText(isForCorr ? "✓ For correction" : "For correction");
        forCorrBtn.setTypeface(isForCorr ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        forCorrBtn.setTextColor(isForCorr ? 0xff082d50 : 0xff64748b);
        ScreenTypography.apply(forCorrBtn, 13);
        forCorrBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, isForCorr ? 0xff082d50 : UiTheme.BORDER));

        compBtn.setAllCaps(false);
        compBtn.setText(!isForCorr ? "✓ Completed" : "Completed");
        compBtn.setTypeface(!isForCorr ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        compBtn.setTextColor(!isForCorr ? 0xff082d50 : 0xff64748b);
        ScreenTypography.apply(compBtn, 13);
        compBtn.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, !isForCorr ? 0xff082d50 : UiTheme.BORDER));
    }

    private View createFeedbackCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Researcher-visible feedback");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 16);
        card.addView(secTitle);

        addSpacing(card, 8);

        TextView fLabel = new TextView(context);
        fLabel.setText("Feedback to researcher");
        fLabel.setTextColor(0xff586b83);
        fLabel.setTypeface(regular);
        ScreenTypography.apply(fLabel, 12);
        card.addView(fLabel);

        addSpacing(card, 4);

        feedbackInput = new EditText(context);
        feedbackInput.setText(requestItem.feedbackToResearcher != null ? requestItem.feedbackToResearcher : "Please revise Section 3.2 (Consent Form) to specify participant data retention policies, as required by Section 4.1 of the research protocol.");
        feedbackInput.setTextColor(0xff082d50);
        feedbackInput.setTypeface(regular);
        ScreenTypography.apply(feedbackInput, 13);
        int padH = UiTheme.dp(context, 12);
        int padV = UiTheme.dp(context, 10);
        feedbackInput.setPadding(padH, padV, padH, padV);
        feedbackInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        feedbackInput.setMinLines(3);
        feedbackInput.setGravity(Gravity.TOP | Gravity.START);
        card.addView(feedbackInput);

        addSpacing(card, 6);

        TextView note = new TextView(context);
        note.setText("The researcher sees this text in Request Details.");
        note.setTextColor(0xff64748b);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 12);
        card.addView(note);

        return card;
    }

    private View createMissingItemsCard() {
        Typeface bold = UiTheme.boldFont(context);

        boolean isMissingError = "6.4a_error_missing".equalsIgnoreCase(previewState);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, isMissingError ? 0xffef4444 : UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Missing items");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 16);
        card.addView(secTitle);

        addSpacing(card, 10);

        if (isMissingError) {
            // Show empty input with red border and warning
            EditText itemInput = new EditText(context);
            itemInput.setHint("Describe a missing item");
            itemInput.setHintTextColor(0xff94a3b8);
            itemInput.setTextColor(0xff082d50);
            itemInput.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(itemInput, 13);
            int pad = UiTheme.dp(context, 10);
            itemInput.setPadding(pad, pad, pad, pad);
            itemInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, 0xffef4444));
            card.addView(itemInput);

            addSpacing(card, 4);

            TextView reqWarning = new TextView(context);
            reqWarning.setText("⚠ Required for For correction");
            reqWarning.setTextColor(0xffdc2626);
            reqWarning.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(reqWarning, 12);
            card.addView(reqWarning);
        } else {
            missingItemsContainer = new LinearLayout(context);
            missingItemsContainer.setOrientation(LinearLayout.VERTICAL);
            renderMissingItems();
            card.addView(missingItemsContainer);
        }

        addSpacing(card, 8);

        Button addBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        addBtn.setText("+ Add item");
        addBtn.setAllCaps(false);
        addBtn.setTextColor(0xff185d9d);
        addBtn.setTypeface(bold);
        ScreenTypography.apply(addBtn, 13);
        addBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xff185d9d), 0x20185d9d));
        addBtn.setMinHeight(UiTheme.dp(context, 40));
        addBtn.setOnClickListener(v -> {
            if (isMissingError) {
                setPreviewState("6.3b_decision");
            } else {
                missingItemsList.add("");
                renderMissingItems();
            }
        });
        card.addView(addBtn);

        return card;
    }

    private void renderMissingItems() {
        if (missingItemsContainer == null) return;
        missingItemsContainer.removeAllViews();
        Typeface regular = UiTheme.bodyFont(context);

        for (int i = 0; i < missingItemsList.size(); i++) {
            final int index = i;
            String val = missingItemsList.get(i);

            LinearLayout box = new LinearLayout(context);
            box.setOrientation(LinearLayout.HORIZONTAL);
            box.setGravity(Gravity.CENTER_VERTICAL);
            box.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
            int pad = UiTheme.dp(context, 10);
            box.setPadding(pad, pad, pad, pad);

            TextView tv = new TextView(context);
            tv.setText(val);
            tv.setTextColor(0xff082d50);
            tv.setTypeface(regular);
            ScreenTypography.apply(tv, 13);
            box.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView removeBtn = new TextView(context);
            removeBtn.setText("✕");
            removeBtn.setTextColor(0xff64748b);
            removeBtn.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(removeBtn, 14);
            removeBtn.setPadding(UiTheme.dp(context, 8), 0, UiTheme.dp(context, 4), 0);
            removeBtn.setOnClickListener(v -> {
                missingItemsList.remove(index);
                renderMissingItems();
            });
            box.addView(removeBtn);

            missingItemsContainer.addView(box);
            addSpacing(missingItemsContainer, 6);
        }
    }

    private View createNextActionCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        boolean isMissingError = "6.4a_error_missing".equalsIgnoreCase(previewState);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Next action");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 16);
        card.addView(secTitle);

        addSpacing(card, 8);

        TextView naLabel = new TextView(context);
        naLabel.setText("Next action for researcher *");
        naLabel.setTextColor(0xff082d50);
        naLabel.setTypeface(bold);
        ScreenTypography.apply(naLabel, 13);
        card.addView(naLabel);

        addSpacing(card, 4);

        nextActionInput = new EditText(context);
        int padH = UiTheme.dp(context, 12);
        int padV = UiTheme.dp(context, 10);
        nextActionInput.setPadding(padH, padV, padH, padV);
        nextActionInput.setMinHeight(UiTheme.dp(context, 44));
        nextActionInput.setTypeface(regular);
        ScreenTypography.apply(nextActionInput, 13);

        if (isMissingError) {
            nextActionInput.setText("");
            nextActionInput.setHint("Describe what the researcher should do next");
            nextActionInput.setHintTextColor(0xff94a3b8);
            nextActionInput.setTextColor(0xff082d50);
            nextActionInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffef4444));
            card.addView(nextActionInput);

            addSpacing(card, 4);

            TextView reqWarning = new TextView(context);
            reqWarning.setText("⚠ Required for For correction");
            reqWarning.setTextColor(0xffdc2626);
            reqWarning.setTypeface(regular);
            ScreenTypography.apply(reqWarning, 12);
            card.addView(reqWarning);
        } else {
            nextActionInput.setText(requestItem.nextAction != null ? requestItem.nextAction : "Add or replace the listed attachments, then resubmit.");
            nextActionInput.setTextColor(0xff082d50);
            nextActionInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
            card.addView(nextActionInput);
        }

        return card;
    }

    private View createDeadlineCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        boolean isMissingError = "6.4a_error_missing".equalsIgnoreCase(previewState);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView dlLabel = new TextView(context);
        dlLabel.setText("Deadline (optional)");
        dlLabel.setTextColor(0xff082d50);
        dlLabel.setTypeface(bold);
        ScreenTypography.apply(dlLabel, 16);
        card.addView(dlLabel);

        addSpacing(card, 8);

        LinearLayout dtRow = new LinearLayout(context);
        dtRow.setOrientation(LinearLayout.HORIZONTAL);

        // Date Column
        LinearLayout dCol = new LinearLayout(context);
        dCol.setOrientation(LinearLayout.VERTICAL);
        TextView dLbl = new TextView(context);
        dLbl.setText("Date");
        dLbl.setTextColor(0xff586b83);
        dLbl.setTypeface(regular);
        ScreenTypography.apply(dLbl, 12);
        dCol.addView(dLbl);

        deadlineDateInput = new EditText(context);
        if (isMissingError) {
            deadlineDateInput.setText("");
            deadlineDateInput.setHint("YYYY-MM-DD");
            deadlineDateInput.setHintTextColor(0xff94a3b8);
        } else {
            deadlineDateInput.setText(requestItem.deadlineDate != null ? requestItem.deadlineDate : "2026-11-04");
        }
        deadlineDateInput.setTextColor(0xff082d50);
        deadlineDateInput.setTypeface(regular);
        ScreenTypography.apply(deadlineDateInput, 13);
        deadlineDateInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        deadlineDateInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        dCol.addView(deadlineDateInput);
        dtRow.addView(dCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        View dtSp = new View(context);
        dtSp.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 10), 1));
        dtRow.addView(dtSp);

        // Time Column
        LinearLayout tCol = new LinearLayout(context);
        tCol.setOrientation(LinearLayout.VERTICAL);
        TextView tLbl = new TextView(context);
        tLbl.setText("Time");
        tLbl.setTextColor(0xff586b83);
        tLbl.setTypeface(regular);
        ScreenTypography.apply(tLbl, 12);
        tCol.addView(tLbl);

        deadlineTimeInput = new EditText(context);
        if (isMissingError) {
            deadlineTimeInput.setText("");
            deadlineTimeInput.setHint("HH:MM");
            deadlineTimeInput.setHintTextColor(0xff94a3b8);
        } else {
            deadlineTimeInput.setText(requestItem.deadlineTime != null ? requestItem.deadlineTime : "5:00 PM");
        }
        deadlineTimeInput.setTextColor(0xff082d50);
        deadlineTimeInput.setTypeface(regular);
        ScreenTypography.apply(deadlineTimeInput, 13);
        deadlineTimeInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        deadlineTimeInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        tCol.addView(deadlineTimeInput);
        dtRow.addView(tCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        card.addView(dtRow);

        addSpacing(card, 6);

        TextView dlNote = new TextView(context);
        dlNote.setText("A reminder is scheduled 24 hours before the deadline.");
        dlNote.setTextColor(0xff64748b);
        dlNote.setTypeface(regular);
        ScreenTypography.apply(dlNote, 12);
        card.addView(dlNote);

        return card;
    }

    private View createInternalRemarksCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, 0xfffff7ed, 8, 0xfffed7aa));

        TextView secTitle = new TextView(context);
        secTitle.setText("🔒 Internal remarks");
        secTitle.setTextColor(0xffc2410c);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 2);

        TextView note = new TextView(context);
        note.setText("Staff only. Not visible to the researcher.");
        note.setTextColor(0xffc2410c);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 12);
        card.addView(note);

        addSpacing(card, 8);

        internalRemarksInput = new EditText(context);
        internalRemarksInput.setText(requestItem.internalRemarks != null ? requestItem.internalRemarks : "Second request from this adviser group this term; check against REG-2024-012 overlap.");
        internalRemarksInput.setTextColor(0xff082d50);
        internalRemarksInput.setTypeface(regular);
        ScreenTypography.apply(internalRemarksInput, 13);
        internalRemarksInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                UiTheme.dp(context, 12), UiTheme.dp(context, 10));
        internalRemarksInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xfffed7aa));
        card.addView(internalRemarksInput);

        return card;
    }

    private View createCheckpointCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Processing checkpoint");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 16);
        card.addView(secTitle);

        addSpacing(card, 8);

        TextView cpLbl = new TextView(context);
        cpLbl.setText("Checkpoint name");
        cpLbl.setTextColor(0xff586b83);
        cpLbl.setTypeface(regular);
        ScreenTypography.apply(cpLbl, 12);
        card.addView(cpLbl);

        checkpointNameInput = new EditText(context);
        checkpointNameInput.setText(requestItem.checkpointName != null ? requestItem.checkpointName : "Committee review completed");
        checkpointNameInput.setTextColor(0xff082d50);
        checkpointNameInput.setTypeface(regular);
        ScreenTypography.apply(checkpointNameInput, 13);
        checkpointNameInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        checkpointNameInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        card.addView(checkpointNameInput);

        addSpacing(card, 8);

        TextView dtLbl = new TextView(context);
        dtLbl.setText("Date");
        dtLbl.setTextColor(0xff586b83);
        dtLbl.setTypeface(regular);
        ScreenTypography.apply(dtLbl, 12);
        card.addView(dtLbl);

        checkpointDateInput = new EditText(context);
        checkpointDateInput.setText(requestItem.checkpointDate != null ? requestItem.checkpointDate : "2026-11-01");
        checkpointDateInput.setTextColor(0xff082d50);
        checkpointDateInput.setTypeface(regular);
        ScreenTypography.apply(checkpointDateInput, 13);
        checkpointDateInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        checkpointDateInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        card.addView(checkpointDateInput);

        addSpacing(card, 8);

        TextView refLbl = new TextView(context);
        refLbl.setText("Supporting reference");
        refLbl.setTextColor(0xff586b83);
        refLbl.setTypeface(regular);
        ScreenTypography.apply(refLbl, 12);
        card.addView(refLbl);

        checkpointRefInput = new EditText(context);
        checkpointRefInput.setText(requestItem.checkpointRef != null ? requestItem.checkpointRef : "UREC minutes no. 2026-41");
        checkpointRefInput.setTextColor(0xff082d50);
        checkpointRefInput.setTypeface(regular);
        ScreenTypography.apply(checkpointRefInput, 13);
        checkpointRefInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        checkpointRefInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        card.addView(checkpointRefInput);

        addSpacing(card, 6);

        TextView cpNote = new TextView(context);
        cpNote.setText("Recording a checkpoint does not issue an endorsement, signature or approval.");
        cpNote.setTextColor(0xff64748b);
        cpNote.setTypeface(regular);
        ScreenTypography.apply(cpNote, 12);
        card.addView(cpNote);

        return card;
    }

    private View createStaleModalCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(UiTheme.dp(context, 24), UiTheme.dp(context, 28),
                UiTheme.dp(context, 24), UiTheme.dp(context, 24));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 16, 0));

        // Amber Circle with Warning Icon
        TextView icon = new TextView(context);
        icon.setText("⚠");
        icon.setTextColor(0xffd97706);
        icon.setTypeface(bold);
        ScreenTypography.apply(icon, 22);
        icon.setGravity(Gravity.CENTER);
        int iSize = UiTheme.dp(context, 48);
        LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(iSize, iSize);
        icon.setLayoutParams(iLp);

        GradientDrawable iBg = new GradientDrawable();
        iBg.setShape(GradientDrawable.OVAL);
        iBg.setColor(Color.parseColor("#FEF3C7"));
        icon.setBackground(iBg);
        card.addView(icon);

        addSpacing(card, 16);

        // Title
        TextView title = new TextView(context);
        title.setText("This request was updated. Reload before saving");
        title.setTextColor(0xff082d50);
        title.setTypeface(bold);
        title.setGravity(Gravity.CENTER);
        ScreenTypography.apply(title, 18);
        card.addView(title);

        addSpacing(card, 8);

        // Body message
        TextView body = new TextView(context);
        body.setText("Your changes have not been saved. Reload the latest version, then review it before making changes.");
        body.setTextColor(0xff586b83);
        body.setTypeface(regular);
        body.setGravity(Gravity.CENTER);
        ScreenTypography.apply(body, 13);
        card.addView(body);

        addSpacing(card, 20);

        // Reload button
        Button reloadBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        reloadBtn.setText("⟳ Reload");
        reloadBtn.setAllCaps(false);
        reloadBtn.setTextColor(0xffffffff);
        reloadBtn.setTypeface(bold);
        ScreenTypography.apply(reloadBtn, 15);
        reloadBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        reloadBtn.setMinHeight(UiTheme.dp(context, 46));
        reloadBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 46)));
        reloadBtn.setOnClickListener(v -> setPreviewState("6.3a_workspace"));
        card.addView(reloadBtn);

        return card;
    }

    private View createReviewSavedToast() {
        Typeface bold = UiTheme.boldFont(context);
        LinearLayout toast = new LinearLayout(context);
        toast.setOrientation(LinearLayout.HORIZONTAL);
        toast.setGravity(Gravity.CENTER);
        toast.setBackground(UiTheme.shape(context, 0xff131c4b, 20, 0));
        int px = UiTheme.dp(context, 20);
        int py = UiTheme.dp(context, 10);
        toast.setPadding(px, py, px, py);

        TextView tv = new TextView(context);
        tv.setText("✓ Review saved.");
        tv.setTextColor(0xffffffff);
        tv.setTypeface(bold);
        ScreenTypography.apply(tv, 14);
        toast.addView(tv);

        return toast;
    }

    private View createAuthorizedCorrectionCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, 0xfffff7ed, 8, 0xfffed7aa));

        // Header Row: Icon + Authorized correction
        LinearLayout titleRow = new LinearLayout(context);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView icon = new TextView(context);
        icon.setText("✓");
        icon.setTextColor(0xffc2410c);
        icon.setTypeface(bold);
        ScreenTypography.apply(icon, 16);
        titleRow.addView(icon);

        View sp = new View(context);
        sp.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 6), 1));
        titleRow.addView(sp);

        TextView secTitle = new TextView(context);
        secTitle.setText("Authorized correction");
        secTitle.setTextColor(0xffc2410c);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 16);
        titleRow.addView(secTitle);

        card.addView(titleRow);

        addSpacing(card, 4);

        TextView note = new TextView(context);
        note.setText("This request is completed. Changes are saved as a correction to the existing record.");
        note.setTextColor(0xffc2410c);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 12);
        card.addView(note);

        addSpacing(card, 12);

        TextView reasonLbl = new TextView(context);
        reasonLbl.setText("Reason for correction *");
        reasonLbl.setTextColor(0xffc2410c);
        reasonLbl.setTypeface(bold);
        ScreenTypography.apply(reasonLbl, 13);
        card.addView(reasonLbl);

        addSpacing(card, 4);

        correctionReasonInput = new EditText(context);
        correctionReasonInput.setHint("Explain why this completed request is being corrected");
        correctionReasonInput.setHintTextColor(0xff94a3b8);
        correctionReasonInput.setTextColor(0xff082d50);
        correctionReasonInput.setTypeface(regular);
        ScreenTypography.apply(correctionReasonInput, 13);
        correctionReasonInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                UiTheme.dp(context, 12), UiTheme.dp(context, 10));
        correctionReasonInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xfffed7aa));
        card.addView(correctionReasonInput);

        addSpacing(card, 4);

        TextView rNote = new TextView(context);
        rNote.setText("Reason and audit history are retained.");
        rNote.setTextColor(0xffc2410c);
        rNote.setTypeface(regular);
        ScreenTypography.apply(rNote, 12);
        card.addView(rNote);

        return card;
    }

    private View createCompletedFeedbackCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Researcher-visible feedback");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 16);
        card.addView(secTitle);

        addSpacing(card, 8);

        TextView fbLbl = new TextView(context);
        fbLbl.setText("Feedback to researcher");
        fbLbl.setTextColor(0xff586b83);
        fbLbl.setTypeface(regular);
        ScreenTypography.apply(fbLbl, 12);
        card.addView(fbLbl);

        addSpacing(card, 4);

        EditText compFb = new EditText(context);
        compFb.setText("Registration recorded under AY 2026-2027. Research ID: REG-2026-074.");
        compFb.setTextColor(0xff082d50);
        compFb.setTypeface(regular);
        ScreenTypography.apply(compFb, 13);
        compFb.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                UiTheme.dp(context, 12), UiTheme.dp(context, 10));
        compFb.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        compFb.setMinLines(2);
        compFb.setGravity(Gravity.TOP | Gravity.START);
        card.addView(compFb);

        return card;
    }

    private View createCompletedCheckpointCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Processing checkpoint");
        secTitle.setTextColor(0xff082d50);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 16);
        card.addView(secTitle);

        addSpacing(card, 8);

        // Inner Checkpoint Container
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(UiTheme.shape(context, 0xfff8fafc, 6, 0xffe2e8f0));
        int pad = UiTheme.dp(context, 12);
        box.setPadding(pad, pad, pad, pad);

        TextView cpLbl = new TextView(context);
        cpLbl.setText("Checkpoint name");
        cpLbl.setTextColor(0xff586b83);
        cpLbl.setTypeface(regular);
        ScreenTypography.apply(cpLbl, 12);
        box.addView(cpLbl);

        EditText cpName = new EditText(context);
        cpName.setText("Registration recorded");
        cpName.setTextColor(0xff082d50);
        cpName.setTypeface(regular);
        ScreenTypography.apply(cpName, 13);
        cpName.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        cpName.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        box.addView(cpName);

        addSpacing(box, 8);

        TextView dtLbl = new TextView(context);
        dtLbl.setText("Date");
        dtLbl.setTextColor(0xff586b83);
        dtLbl.setTypeface(regular);
        ScreenTypography.apply(dtLbl, 12);
        box.addView(dtLbl);

        EditText cpDate = new EditText(context);
        cpDate.setText("2026-10-14");
        cpDate.setTextColor(0xff082d50);
        cpDate.setTypeface(regular);
        ScreenTypography.apply(cpDate, 13);
        cpDate.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        cpDate.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        box.addView(cpDate);

        addSpacing(box, 8);

        TextView refLbl = new TextView(context);
        refLbl.setText("Supporting reference");
        refLbl.setTextColor(0xff586b83);
        refLbl.setTypeface(regular);
        ScreenTypography.apply(refLbl, 12);
        box.addView(refLbl);

        EditText cpRef = new EditText(context);
        cpRef.setText("CRM log 2026-10-118");
        cpRef.setTextColor(0xff082d50);
        cpRef.setTypeface(regular);
        ScreenTypography.apply(cpRef, 13);
        cpRef.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        cpRef.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        box.addView(cpRef);

        card.addView(box);

        addSpacing(card, 6);

        TextView note = new TextView(context);
        note.setText("Recording a checkpoint does not issue an endorsement, signature or approval.");
        note.setTextColor(0xff64748b);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 12);
        card.addView(note);

        return card;
    }

    private View createCompletedActionButtons() {
        Typeface bold = UiTheme.boldFont(context);

        LinearLayout group = new LinearLayout(context);
        group.setOrientation(LinearLayout.VERTICAL);

        Button saveCorrBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        saveCorrBtn.setText("Save Correction");
        saveCorrBtn.setAllCaps(false);
        saveCorrBtn.setTextColor(0xffffffff);
        saveCorrBtn.setTypeface(bold);
        ScreenTypography.apply(saveCorrBtn, 15);
        saveCorrBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        saveCorrBtn.setMinHeight(UiTheme.dp(context, 46));
        saveCorrBtn.setOnClickListener(v -> UiDialog.alert(context, "Correction not saved",
                "This workspace can’t send review corrections to CRM yet."));
        group.addView(saveCorrBtn);

        addSpacing(group, 10);

        Button cancelBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        cancelBtn.setText("Cancel");
        cancelBtn.setAllCaps(false);
        cancelBtn.setTextColor(0xff082d50);
        cancelBtn.setTypeface(bold);
        ScreenTypography.apply(cancelBtn, 15);
        cancelBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        cancelBtn.setMinHeight(UiTheme.dp(context, 44));
        cancelBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onBackToQueueCompleted();
            }
        });
        group.addView(cancelBtn);

        return group;
    }

    private TextView createStatusBadge(String status) {
        Typeface bold = UiTheme.boldFont(context);
        TextView badge = new TextView(context);
        badge.setText(status);
        badge.setTypeface(bold);
        ScreenTypography.apply(badge, 11);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        badge.setPadding(px, py, px, py);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(UiTheme.dp(context, 4));

        if ("In review".equalsIgnoreCase(status)) {
            bg.setColor(Color.parseColor("#FEF3C7"));
            badge.setTextColor(Color.parseColor("#D97706"));
        } else if ("Completed".equalsIgnoreCase(status)) {
            bg.setColor(Color.parseColor("#DCFCE7"));
            badge.setTextColor(Color.parseColor("#16A34A"));
        } else if ("For correction".equalsIgnoreCase(status)) {
            bg.setColor(Color.parseColor("#FEF3C7"));
            badge.setTextColor(Color.parseColor("#D97706"));
        } else {
            bg.setColor(Color.parseColor("#F1F5F9"));
            badge.setTextColor(Color.parseColor("#64748B"));
        }

        badge.setBackground(bg);
        return badge;
    }

    private void addSpacing(int dp) {
        View s = new View(context);
        s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        contentLayout.addView(s);
    }

    private static void addSpacing(LinearLayout parent, int dp) {
        View s = new View(parent.getContext());
        s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(parent.getContext(), dp)));
        parent.addView(s);
    }
}
