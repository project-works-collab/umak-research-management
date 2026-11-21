package ph.edu.umak.crm.app.staff.queue;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * 6.3a - 6.6 Request Workspace View
 * Handles review checklist, evaluation, decision making, validation error states,
 * stale data conflicts, completed corrections, and review saved confirmations.
 */
public class RequestWorkspaceView {
    public interface LiveActions {
        void save(org.json.JSONObject body);
        void download(StaffQueueData.AttachmentItem attachment);
        void reload();
    }
    private final LiveActions live;
    private ph.edu.umak.crm.app.common.components.PendingStatus liveStatus;
    private boolean saving;
    private boolean stale;
    private Button liveSave;
    private String originalDraft = "";


    public interface Callbacks {
        void onBackToQueue();
        void onSessionDraftKept();
    }

    private final Context context;
    private final Callbacks callbacks;
    private final LinearLayout contentLayout;

    private StaffQueueData.QueueRequestItem requestItem;
    private String previewState = "6.3a_workspace";
    private final java.util.Map<String, StaffQueueData.QueueRequestItem> sessionDrafts = new java.util.HashMap<>();

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
        this(context, callbacks, null);
    }

    public RequestWorkspaceView(Context context, Callbacks callbacks, LiveActions live) {
        this.context = context;
        this.callbacks = callbacks;
        this.live = live;

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setBackgroundColor(UiTheme.PAPER);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));

        if (live == null) setRequest(StaffQueueData.getInitialQueue().get(0));
    }

    public View getContentLayout() {
        if (contentLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) contentLayout.getParent()).removeView(contentLayout);
        }
        return PageTitle.wrap(contentLayout, "Request Workspace", "Review and process service requests.", false);
    }

    public void setRequest(StaffQueueData.QueueRequestItem request) {
        previewState = live == null ? "6.3a_workspace" : "live";
        stale = false;
        saving = false;
        var draft = request == null ? null : sessionDrafts.get(request.id);
        if (draft != null && live != null && draft.version != request.version) stale = true;
        bindRequest(draft == null ? request : draft);
        if (stale) showResult("This request was updated. Your session draft is retained. Reload before saving.", true, true);
        originalDraft = draftFingerprint();
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
            // Find REQ-2026-074
            for (StaffQueueData.QueueRequestItem item : StaffQueueData.getInitialQueue()) {
                if ("REQ-2026-074".equalsIgnoreCase(item.id)) {
                    bindRequest(item);
                    return;
                }
            }
        } else {
            // REQ-2026-089
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
        contentLayout.removeAllViews();
        liveSave = null;
        feedbackInput = nextActionInput = deadlineDateInput = deadlineTimeInput = internalRemarksInput = null;
        checkpointNameInput = checkpointDateInput = checkpointRefInput = correctionReasonInput = null;
        if (requestItem == null) return;
        if (live != null) {
            liveStatus = new ph.edu.umak.crm.app.common.components.PendingStatus(context);
            contentLayout.addView(liveStatus);
        }

        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        // Back button row
        if (callbacks != null) {
            TextView backBtn = new TextView(context);
            backBtn.setText("< Back to Queue");
            backBtn.setMinHeight(UiTheme.dp(context, 48));
            backBtn.setGravity(Gravity.CENTER_VERTICAL);
            backBtn.setTextColor(UiTheme.ACCENT);
            backBtn.setTypeface(bold);
            ScreenTypography.apply(backBtn, 14);
            backBtn.setPadding(0, 0, 0, UiTheme.dp(context, 12));
            backBtn.setOnClickListener(v -> callbacks.onBackToQueue());
            contentLayout.addView(backBtn);
        }

        // Request Summary Banner Card
        contentLayout.addView(createRequestHeaderCard());

        addSpacing(14);

        boolean isCompleted = "Completed".equalsIgnoreCase(requestItem.status)
                || "6.5_completed_correction".equalsIgnoreCase(previewState);

        if (isCompleted && live == null) {
            // 6.5 Completed Request Correction Mode
            contentLayout.addView(createCompletedCorrectionSection());
        } else {
            // Standard Review Workspace (6.3a, 6.3b, 6.4a-c, 6.6)
            // Part 1: Requester Profile
            contentLayout.addView(createRequesterProfileCard());
            addSpacing(14);

            // Attachments
            if (!requestItem.attachments.isEmpty()) {
                contentLayout.addView(createAttachmentsCard());
                addSpacing(14);
            }

            // Evaluation Checklist
            contentLayout.addView(createChecklistCard());
            addSpacing(14);

            // Earlier Reviews
            contentLayout.addView(createEarlierReviewsCard());
            addSpacing(20);

            // Part 2: Decision Section Header & Content
            if (live != null && "For correction".equals(requestItem.status)) {
                contentLayout.addView(createErrorBanner("Waiting for the researcher to resubmit. This request cannot be reviewed yet."));
            } else if (isCompleted) {
                contentLayout.addView(createCompletedCorrectionSection());
            } else contentLayout.addView(createDecisionSection());
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
        idView.setTextColor(UiTheme.NAVY);
        idView.setTypeface(bold);
        ScreenTypography.apply(idView, 15);
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
        sView.setTextColor(UiTheme.ACCENT);
        sView.setTypeface(bold);
        ScreenTypography.apply(sView, 14);
        card.addView(sView);

        // Study Title
        TextView tView = new TextView(context);
        tView.setText(requestItem.studyTitle);
        tView.setTextColor(UiTheme.NAVY);
        tView.setTypeface(bold);
        ScreenTypography.apply(tView, 16);
        tView.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 4));
        card.addView(tView);

        // Submitted info
        TextView dView = new TextView(context);
        dView.setText("Submitted " + requestItem.submittedAt);
        dView.setTextColor(UiTheme.MUTED);
        dView.setTypeface(regular);
        ScreenTypography.apply(dView, 12);
        card.addView(dView);
        if (live != null && !requestItem.summary.isEmpty()) {
            TextView description = new TextView(context);
            description.setText(requestItem.summary);
            description.setTypeface(regular);
            description.setTextColor(UiTheme.MUTED);
            ScreenTypography.apply(description, 14);
            card.addView(description);
        }

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
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 10);

        // Avatar Row
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = new TextView(context);
        String[] names = requestItem.requesterName.trim().split("\\s+");
        String initials = requestItem.requesterName.isBlank() ? "?"
                : names[0].substring(0, 1) + (names.length > 1 ? names[names.length - 1].substring(0, 1) : "");
        avatar.setText(live == null ? "LJ" : initials.toUpperCase(java.util.Locale.ROOT));
        avatar.setTextColor(UiTheme.NAVY);
        avatar.setTypeface(bold);
        ScreenTypography.apply(avatar, 13);
        avatar.setGravity(Gravity.CENTER);
        int aSize = UiTheme.dp(context, 38);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(aSize, aSize);
        aLp.setMarginEnd(UiTheme.dp(context, 10));
        avatar.setLayoutParams(aLp);

        GradientDrawable aBg = new GradientDrawable();
        aBg.setShape(GradientDrawable.OVAL);
        aBg.setColor(Color.parseColor("#E5EDF6"));
        avatar.setBackground(aBg);
        row.addView(avatar);

        LinearLayout col = new LinearLayout(context);
        col.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(context);
        name.setText(requestItem.requesterName);
        name.setTextColor(UiTheme.NAVY);
        name.setTypeface(bold);
        ScreenTypography.apply(name, 14);
        col.addView(name);

        TextView role = new TextView(context);
        role.setText(requestItem.requesterRole);
        role.setTextColor(UiTheme.MUTED);
        role.setTypeface(regular);
        ScreenTypography.apply(role, 12);
        col.addView(role);

        row.addView(col);
        card.addView(row);

        addSpacing(card, 10);

        // College
        TextView cLbl = new TextView(context);
        cLbl.setText("College");
        cLbl.setTextColor(UiTheme.MUTED);
        cLbl.setTypeface(regular);
        ScreenTypography.apply(cLbl, 12);
        card.addView(cLbl);

        TextView cVal = new TextView(context);
        cVal.setText(requestItem.requesterCollege);
        cVal.setTextColor(UiTheme.NAVY);
        cVal.setTypeface(bold);
        ScreenTypography.apply(cVal, 13);
        card.addView(cVal);

        addSpacing(card, 8);

        // Email
        TextView eLbl = new TextView(context);
        eLbl.setText("Email");
        eLbl.setTextColor(UiTheme.MUTED);
        eLbl.setTypeface(regular);
        ScreenTypography.apply(eLbl, 12);
        card.addView(eLbl);

        TextView eVal = new TextView(context);
        eVal.setText(requestItem.requesterEmail);
        eVal.setTextColor(UiTheme.NAVY);
        eVal.setTypeface(bold);
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
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 10);

        for (StaffQueueData.AttachmentItem att : requestItem.attachments) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

            TextView fName = new TextView(context);
            fName.setText(att.fileName);
            fName.setTextColor(UiTheme.NAVY);
            fName.setTypeface(regular);
            ScreenTypography.apply(fName, 13);
            LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(fName, fLp);

            TextView fSize = new TextView(context);
            fSize.setText(att.fileSize);
            fSize.setTextColor(UiTheme.MUTED);
            fSize.setTypeface(regular);
            ScreenTypography.apply(fSize, 12);
            row.addView(fSize);

            if (live != null) {
                Button open = UiButton.outline(context, "Open " + att.fileName, UiTheme.BORDER, UiTheme.NAVY,
                        () -> live.download(att));
                card.addView(open);
            } else card.addView(row);
        }

        return card;
    }

    private View createChecklistCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Evaluation Checklist");
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 12);

        if (requestItem.checklist.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText("This service has no document requirements.");
            empty.setTextColor(UiTheme.MUTED);
            empty.setTypeface(regular);
            ScreenTypography.apply(empty, 13);
            card.addView(empty);
        }
        String[] options = {"Pending", "Verified", "Needs correction", "Not applicable"};

        for (StaffQueueData.ChecklistItem item : requestItem.checklist) {
            TextView itemTitle = new TextView(context);
            itemTitle.setText(item.title);
            itemTitle.setTextColor(UiTheme.NAVY);
            itemTitle.setTypeface(bold);
            ScreenTypography.apply(itemTitle, 13);
            card.addView(itemTitle);

            addSpacing(card, 6);

            HorizontalScrollView hsv = new HorizontalScrollView(context);
            hsv.setHorizontalScrollBarEnabled(false);
            LinearLayout chipRow = new LinearLayout(context);
            chipRow.setOrientation(LinearLayout.HORIZONTAL);

            List<TextView> chipViews = new ArrayList<>();
            for (String opt : options) {
                TextView chip = new TextView(context);
                chip.setText(opt);
                if (live != null) { chip.setMinHeight(UiTheme.dp(context, 48)); chip.setGravity(Gravity.CENTER); }
                chip.setTypeface(regular);
                ScreenTypography.apply(chip, 11);
                chip.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 5),
                        UiTheme.dp(context, 10), UiTheme.dp(context, 5));

                updateChecklistChip(chip, opt.equalsIgnoreCase(item.status));

                chip.setOnClickListener(v -> {
                    if (live != null && (saving || "For correction".equals(requestItem.status))) return;
                    item.status = opt;
                    for (int i = 0; i < options.length; i++) {
                        updateChecklistChip(chipViews.get(i), options[i].equalsIgnoreCase(item.status));
                    }
                });

                chipViews.add(chip);
                chipRow.addView(chip);

                View sp = new View(context);
                sp.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 6), 1));
                chipRow.addView(sp);
            }
            hsv.addView(chipRow);
            card.addView(hsv);

            addSpacing(card, 12);
        }

        return card;
    }

    private void updateChecklistChip(TextView chip, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(UiTheme.dp(context, 14));
        if (selected) {
            bg.setColor(UiTheme.NAVY);
            chip.setTextColor(UiTheme.WHITE);
            chip.setTypeface(UiTheme.boldFont(context));
        } else {
            bg.setColor(UiTheme.WHITE);
            bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
            chip.setTextColor(UiTheme.MUTED);
            chip.setTypeface(UiTheme.bodyFont(context));
        }
        chip.setBackground(bg);
    }

    private View createEarlierReviewsCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        int count = "6.6_review_saved".equalsIgnoreCase(previewState) ? 3 : requestItem.earlierReviews.size();
        TextView secTitle = new TextView(context);
        secTitle.setText("Earlier reviews (" + count + " entries)");
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 10);

        if ("6.6_review_saved".equalsIgnoreCase(previewState)) {
            // New saved entry at top
            card.addView(createReviewLogEntry("For correction", true, "Lebron James · CRM Staff", "Nov 1, 2026, 3:20 PM"));
            addSpacing(card, 8);
        }

        for (StaffQueueData.ReviewLogItem rev : requestItem.earlierReviews) {
            card.addView(createReviewLogEntry(rev.status, rev.isNew, rev.author, rev.timestamp));
            if (live != null && !rev.details.isEmpty()) {
                TextView details = new TextView(context);
                details.setText(rev.details);
                details.setTextColor(UiTheme.MUTED);
                details.setTypeface(regular);
                ScreenTypography.apply(details, 13);
                card.addView(details);
            }
            addSpacing(card, 8);
        }

        return card;
    }

    private View createReviewLogEntry(String status, boolean isNew, String author, String date) {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);

        LinearLayout top = new LinearLayout(context);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView sBadge = createStatusBadge(status);
        top.addView(sBadge);

        if (isNew) {
            View sp = new View(context);
            sp.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 6), 1));
            top.addView(sp);

            TextView newBadge = new TextView(context);
            newBadge.setText("New");
            newBadge.setTextColor(Color.WHITE);
            newBadge.setTypeface(bold);
            ScreenTypography.apply(newBadge, 10);
            newBadge.setPadding(UiTheme.dp(context, 6), UiTheme.dp(context, 2),
                    UiTheme.dp(context, 6), UiTheme.dp(context, 2));
            newBadge.setBackground(UiTheme.shape(context, UiTheme.ACCENT, 4, 0));
            top.addView(newBadge);
        }

        row.addView(top);

        TextView authText = new TextView(context);
        authText.setText(author + " · " + date);
        authText.setTextColor(UiTheme.MUTED);
        authText.setTypeface(regular);
        ScreenTypography.apply(authText, 12);
        authText.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        row.addView(authText);

        return row;
    }

    private View createDecisionSection() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout section = new LinearLayout(context);
        section.setOrientation(LinearLayout.VERTICAL);

        // Section Title
        TextView title = new TextView(context);
        title.setText("Request Workspace · Decision");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 18);
        title.setPadding(0, 0, 0, UiTheme.dp(context, 12));
        section.addView(title);

        // Stale Data Conflict Modal / Banner (6.4c)
        if ("6.4c_stale_conflict".equalsIgnoreCase(previewState)) {
            section.addView(createStaleConflictBanner());
            addSpacing(section, 14);
        }

        // Top Error Banner (6.4a or 6.4b)
        if ("6.4a_error_missing".equalsIgnoreCase(previewState)) {
            section.addView(createErrorBanner("Enter the missing items and next action."));
            addSpacing(section, 14);
        } else if ("6.4b_error_status_not_allowed".equalsIgnoreCase(previewState)) {
            section.addView(createErrorBanner("This status change is not allowed."));
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

        // 5. Internal remarks Card
        section.addView(createInternalRemarksCard());
        addSpacing(section, 14);

        // 6. Processing checkpoint Card
        section.addView(createCheckpointCard());
        addSpacing(section, 20);

        // Save & Action Buttons
        Button saveBtn = UiButton.primary(context, "Save Review", () -> {
            if (live != null) { saveLive(); return; }
            if ("6.4a_error_missing".equalsIgnoreCase(previewState) || "6.4b_error_status_not_allowed".equalsIgnoreCase(previewState)) {
                return;
            }
            if ("6.4c_stale_conflict".equalsIgnoreCase(previewState)) {
                showStaleDialog();
                return;
            }
            ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(context, "Review not sent",
                    "This workspace can’t submit a review to CRM yet. You can keep these changes for this session.");
        });
        if (live != null) liveSave = saveBtn;
        section.addView(saveBtn);

        addSpacing(section, 10);

        Button draftBtn = UiButton.outline(context, "Keep draft for this session", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (saving) return;
            if (live != null) {
                captureDraft();
                sessionDrafts.put(requestItem.id, requestItem);
                if (callbacks != null) callbacks.onSessionDraftKept();
                return;
            }
            requestItem.targetStatus = selectedNextStatus;
            requestItem.feedbackToResearcher = feedbackInput.getText().toString();
            requestItem.missingItems = new ArrayList<>(missingItemsList);
            requestItem.nextAction = nextActionInput.getText().toString();
            requestItem.deadlineDate = deadlineDateInput.getText().toString();
            requestItem.deadlineTime = deadlineTimeInput.getText().toString();
            requestItem.internalRemarks = internalRemarksInput.getText().toString();
            requestItem.checkpointName = checkpointNameInput.getText().toString();
            requestItem.checkpointDate = checkpointDateInput.getText().toString();
            requestItem.checkpointRef = checkpointRefInput.getText().toString();
            sessionDrafts.put(requestItem.id, requestItem);
            if (callbacks != null) callbacks.onSessionDraftKept();
        });
        section.addView(draftBtn);

        return section;
    }

    private View createStatusChangeCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Status change");
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 8);

        TextView curStatus = new TextView(context);
        curStatus.setText("Current status: " + requestItem.status);
        curStatus.setTextColor(UiTheme.MUTED);
        curStatus.setTypeface(regular);
        ScreenTypography.apply(curStatus, 13);
        card.addView(curStatus);

        addSpacing(card, 10);

        TextView moveLabel = new TextView(context);
        moveLabel.setText("Move to");
        moveLabel.setTextColor(UiTheme.NAVY);
        moveLabel.setTypeface(bold);
        ScreenTypography.apply(moveLabel, 13);
        card.addView(moveLabel);

        RadioGroup rg = new RadioGroup(context);
        rg.setOrientation(live == null ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        java.util.List<String> statuses = live == null ? java.util.List.of("For correction", "Completed")
                : StaffRequestData.allowedStatuses(requestItem.status);
        for (String status : statuses) {
            RadioButton choice = new RadioButton(context);
            choice.setId(View.generateViewId());
            choice.setText(status);
            choice.setTextColor(UiTheme.NAVY);
            choice.setMinHeight(UiTheme.dp(context, 48));
            rg.addView(choice);
            choice.setChecked(status.equals(selectedNextStatus));
            choice.setOnCheckedChangeListener((button, checked) -> {
                if (checked) selectedNextStatus = status;
            });
        }
        card.addView(rg);

        // Error for status change if 6.4b
        if ("6.4b_error_status_not_allowed".equalsIgnoreCase(previewState)) {
            TextView err = new TextView(context);
            err.setText("This status change is not allowed.");
            err.setTextColor(UiTheme.ERROR_RED);
            err.setTypeface(regular);
            ScreenTypography.apply(err, 12);
            err.setPadding(0, UiTheme.dp(context, 4), 0, 0);
            card.addView(err);
        }

        addSpacing(card, 6);

        TextView note = new TextView(context);
        note.setText("Only allowed next statuses are shown. Submitted -> In review; In review -> For correction or Completed.");
        note.setTextColor(UiTheme.MUTED);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 12);
        card.addView(note);

        return card;
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
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 8);

        TextView fLabel = new TextView(context);
        fLabel.setText("Feedback to researcher");
        fLabel.setTextColor(UiTheme.MUTED);
        fLabel.setTypeface(regular);
        ScreenTypography.apply(fLabel, 12);
        card.addView(fLabel);

        addSpacing(card, 4);

        feedbackInput = new EditText(context);
        feedbackInput.setContentDescription("Researcher-visible feedback");
        feedbackInput.setText(requestItem.feedbackToResearcher != null ? requestItem.feedbackToResearcher : "");
        feedbackInput.setTextColor(UiTheme.NAVY);
        feedbackInput.setTypeface(regular);
        ScreenTypography.apply(feedbackInput, 13);
        feedbackInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                UiTheme.dp(context, 12), UiTheme.dp(context, 10));
        feedbackInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        card.addView(feedbackInput);

        addSpacing(card, 4);

        TextView note = new TextView(context);
        note.setText("The researcher sees this text in Request Details.");
        note.setTextColor(UiTheme.MUTED);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 12);
        card.addView(note);

        return card;
    }

    private View createMissingItemsCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Missing items");
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 10);

        if (live != null) {
            for (int i = 0; i < requestItem.checklist.size(); i++) {
                final int index = i;
                android.widget.CheckBox requirement = new android.widget.CheckBox(context);
                requirement.setText(requestItem.checklist.get(i).title);
                requirement.setTextColor(UiTheme.NAVY);
                requirement.setTypeface(regular);
                requirement.setMinHeight(UiTheme.dp(context, 48));
                requirement.setChecked(requestItem.missingRequirementIndices.contains(index));
                requirement.setOnCheckedChangeListener((view, checked) -> {
                    requestItem.missingRequirementIndices.remove(Integer.valueOf(index));
                    if (checked) requestItem.missingRequirementIndices.add(index);
                });
                card.addView(requirement);
            }
            if (requestItem.checklist.isEmpty()) {
                TextView empty = new TextView(context);
                empty.setText("This service has no document requirements.");
                empty.setTextColor(UiTheme.MUTED);
                card.addView(empty);
            }
            return card;
        }

        missingItemsContainer = new LinearLayout(context);
        missingItemsContainer.setOrientation(LinearLayout.VERTICAL);
        renderMissingItems();
        card.addView(missingItemsContainer);

        addSpacing(card, 8);

        Button addBtn = UiButton.outline(context, "Add item", UiTheme.BORDER, UiTheme.NAVY, () -> {
            missingItemsList.add("");
            renderMissingItems();
        });
        addBtn.setMinHeight(UiTheme.dp(context, 38));
        card.addView(addBtn);

        return card;
    }

    private void renderMissingItems() {
        missingItemsContainer.removeAllViews();
        Typeface regular = UiTheme.bodyFont(context);
        boolean isErrorMissing = "6.4a_error_missing".equalsIgnoreCase(previewState);

        if (missingItemsList.isEmpty() && isErrorMissing) {
            missingItemsList.add("");
        }

        for (int i = 0; i < missingItemsList.size(); i++) {
            final int index = i;
            String val = missingItemsList.get(i);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));

            EditText input = new EditText(context);
            input.setText(val);
            input.setHint("Describe a missing item");
            input.setHintTextColor(UiTheme.MUTED);
            input.setTextColor(UiTheme.NAVY);
            input.setTypeface(regular);
            ScreenTypography.apply(input, 13);
            input.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                    UiTheme.dp(context, 10), UiTheme.dp(context, 8));

            int borderStroke = (isErrorMissing && val.trim().isEmpty()) ? UiTheme.ERROR_RED : UiTheme.BORDER;
            input.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, borderStroke));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(input, lp);

            Button deleteBtn = UiButton.text(context, "Remove", UiTheme.ERROR_RED, () -> {
                missingItemsList.remove(index);
                renderMissingItems();
            });
            deleteBtn.setPadding(UiTheme.dp(context, 6), 0, 0, 0);
            row.addView(deleteBtn);

            missingItemsContainer.addView(row);

            if (isErrorMissing && val.trim().isEmpty()) {
                TextView err = new TextView(context);
                err.setText("Required for For correction");
                err.setTextColor(UiTheme.ERROR_RED);
                err.setTypeface(regular);
                ScreenTypography.apply(err, 11);
                err.setPadding(0, 0, 0, UiTheme.dp(context, 4));
                missingItemsContainer.addView(err);
            }
        }
    }

    private View createNextActionCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Next action");
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 8);

        TextView naLabel = new TextView(context);
        naLabel.setText("Next action for researcher *");
        naLabel.setTextColor(UiTheme.NAVY);
        naLabel.setTypeface(bold);
        ScreenTypography.apply(naLabel, 13);
        card.addView(naLabel);

        addSpacing(card, 4);

        nextActionInput = new EditText(context);
        nextActionInput.setContentDescription("Next action");
        boolean isErrorMissing = "6.4a_error_missing".equalsIgnoreCase(previewState);
        nextActionInput.setText(isErrorMissing ? "" : (requestItem.nextAction != null ? requestItem.nextAction : ""));
        nextActionInput.setHint("Describe what the researcher should do next");
        nextActionInput.setHintTextColor(UiTheme.MUTED);
        nextActionInput.setTextColor(UiTheme.NAVY);
        nextActionInput.setTypeface(regular);
        ScreenTypography.apply(nextActionInput, 13);
        nextActionInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                UiTheme.dp(context, 12), UiTheme.dp(context, 10));

        int borderStroke = isErrorMissing ? UiTheme.ERROR_RED : UiTheme.BORDER;
        nextActionInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, borderStroke));
        card.addView(nextActionInput);

        if (isErrorMissing) {
            TextView err = new TextView(context);
            err.setText("Required for For correction");
            err.setTextColor(UiTheme.ERROR_RED);
            err.setTypeface(regular);
            ScreenTypography.apply(err, 12);
            err.setPadding(0, UiTheme.dp(context, 4), 0, 0);
            card.addView(err);
        }

        addSpacing(card, 12);

        TextView dlLabel = new TextView(context);
        dlLabel.setText("Deadline (optional)");
        dlLabel.setTextColor(UiTheme.NAVY);
        dlLabel.setTypeface(bold);
        ScreenTypography.apply(dlLabel, 13);
        card.addView(dlLabel);

        addSpacing(card, 4);

        LinearLayout dtRow = new LinearLayout(context);
        dtRow.setOrientation(LinearLayout.HORIZONTAL);

        deadlineDateInput = new EditText(context);
        deadlineDateInput.setContentDescription("Deadline date (YYYY-MM-DD)");
        deadlineDateInput.setText(isErrorMissing ? "" : (requestItem.deadlineDate != null ? requestItem.deadlineDate : (live == null ? "2026-11-04" : "")));
        deadlineDateInput.setHint("YYYY-MM-DD");
        deadlineDateInput.setHintTextColor(UiTheme.MUTED);
        deadlineDateInput.setTextColor(UiTheme.NAVY);
        deadlineDateInput.setTypeface(regular);
        ScreenTypography.apply(deadlineDateInput, 13);
        deadlineDateInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        deadlineDateInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        dtRow.addView(deadlineDateInput, dLp);

        View dtSp = new View(context);
        dtSp.setLayoutParams(new LinearLayout.LayoutParams(UiTheme.dp(context, 8), 1));
        dtRow.addView(dtSp);

        deadlineTimeInput = new EditText(context);
        deadlineTimeInput.setContentDescription("Deadline time (HH:mm, Philippine time)");
        if (live != null) deadlineTimeInput.setHint("HH:mm · Philippine time");
        deadlineTimeInput.setText(isErrorMissing ? "" : (requestItem.deadlineTime != null ? requestItem.deadlineTime : (live == null ? "5:00 PM" : "")));
        deadlineTimeInput.setHint("HH:MM");
        deadlineTimeInput.setHintTextColor(UiTheme.MUTED);
        deadlineTimeInput.setTextColor(UiTheme.NAVY);
        deadlineTimeInput.setTypeface(regular);
        ScreenTypography.apply(deadlineTimeInput, 13);
        deadlineTimeInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        deadlineTimeInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        dtRow.addView(deadlineTimeInput, tLp);

        card.addView(dtRow);

        addSpacing(card, 4);

        TextView dlNote = new TextView(context);
        dlNote.setText("A reminder is scheduled 24 hours before the deadline.");
        dlNote.setTextColor(UiTheme.MUTED);
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
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Internal remarks");
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 4);

        TextView note = new TextView(context);
        note.setText("Staff only. Not visible to the researcher.");
        note.setTextColor(UiTheme.MUTED);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 12);
        card.addView(note);

        addSpacing(card, 8);

        internalRemarksInput = new EditText(context);
        internalRemarksInput.setContentDescription("Internal staff remarks");
        internalRemarksInput.setText(requestItem.internalRemarks != null ? requestItem.internalRemarks : "");
        internalRemarksInput.setTextColor(UiTheme.NAVY);
        internalRemarksInput.setTypeface(regular);
        ScreenTypography.apply(internalRemarksInput, 13);
        internalRemarksInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                UiTheme.dp(context, 12), UiTheme.dp(context, 10));
        internalRemarksInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
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
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 15);
        card.addView(secTitle);

        addSpacing(card, 8);

        TextView cpLbl = new TextView(context);
        cpLbl.setText("Checkpoint name");
        cpLbl.setTextColor(UiTheme.MUTED);
        cpLbl.setTypeface(regular);
        ScreenTypography.apply(cpLbl, 12);
        card.addView(cpLbl);

        checkpointNameInput = new EditText(context);
        checkpointNameInput.setContentDescription("Processing checkpoint");
        checkpointNameInput.setText(requestItem.checkpointName != null ? requestItem.checkpointName : (live == null ? "Committee review completed" : ""));
        checkpointNameInput.setTextColor(UiTheme.NAVY);
        checkpointNameInput.setTypeface(regular);
        ScreenTypography.apply(checkpointNameInput, 13);
        checkpointNameInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        checkpointNameInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        card.addView(checkpointNameInput);

        addSpacing(card, 8);

        TextView dtLbl = new TextView(context);
        dtLbl.setText("Date");
        dtLbl.setTextColor(UiTheme.MUTED);
        dtLbl.setTypeface(regular);
        ScreenTypography.apply(dtLbl, 12);
        card.addView(dtLbl);

        checkpointDateInput = new EditText(context);
        checkpointDateInput.setContentDescription("Checkpoint date (YYYY-MM-DD)");
        checkpointDateInput.setText(requestItem.checkpointDate != null ? requestItem.checkpointDate : (live == null ? "2026-11-01" : ""));
        checkpointDateInput.setTextColor(UiTheme.NAVY);
        checkpointDateInput.setTypeface(regular);
        ScreenTypography.apply(checkpointDateInput, 13);
        checkpointDateInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        checkpointDateInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        card.addView(checkpointDateInput);

        addSpacing(card, 8);

        TextView refLbl = new TextView(context);
        refLbl.setText("Supporting reference");
        refLbl.setTextColor(UiTheme.MUTED);
        refLbl.setTypeface(regular);
        ScreenTypography.apply(refLbl, 12);
        card.addView(refLbl);

        checkpointRefInput = new EditText(context);
        checkpointRefInput.setContentDescription("Supporting reference");
        checkpointRefInput.setText(requestItem.checkpointRef != null ? requestItem.checkpointRef : (live == null ? "UREC minutes no. 2026-41" : ""));
        checkpointRefInput.setTextColor(UiTheme.NAVY);
        checkpointRefInput.setTypeface(regular);
        ScreenTypography.apply(checkpointRefInput, 13);
        checkpointRefInput.setPadding(UiTheme.dp(context, 10), UiTheme.dp(context, 8),
                UiTheme.dp(context, 10), UiTheme.dp(context, 8));
        checkpointRefInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 6, UiTheme.BORDER));
        card.addView(checkpointRefInput);

        addSpacing(card, 6);

        TextView cpNote = new TextView(context);
        cpNote.setText("Recording a checkpoint does not issue an endorsement, signature or approval.");
        cpNote.setTextColor(UiTheme.MUTED);
        cpNote.setTypeface(regular);
        ScreenTypography.apply(cpNote, 12);
        card.addView(cpNote);

        return card;
    }

    private View createCompletedCorrectionSection() {
        if (live != null) return createLiveCompletedCorrection();
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        TextView secTitle = new TextView(context);
        secTitle.setText("Authorized correction");
        secTitle.setTextColor(UiTheme.NAVY);
        secTitle.setTypeface(bold);
        ScreenTypography.apply(secTitle, 16);
        card.addView(secTitle);

        addSpacing(card, 6);

        TextView note = new TextView(context);
        note.setText("This request is completed. Changes are saved as a correction to the existing record.");
        note.setTextColor(UiTheme.MUTED);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 13);
        card.addView(note);

        addSpacing(card, 12);

        TextView reasonLbl = new TextView(context);
        reasonLbl.setText("Reason for correction *");
        reasonLbl.setTextColor(UiTheme.NAVY);
        reasonLbl.setTypeface(bold);
        ScreenTypography.apply(reasonLbl, 13);
        card.addView(reasonLbl);

        addSpacing(card, 4);

        correctionReasonInput = new EditText(context);
        correctionReasonInput.setContentDescription("Reason for correcting this completed request");
        correctionReasonInput.setHint("Explain why this completed request is being corrected");
        correctionReasonInput.setHintTextColor(UiTheme.MUTED);
        correctionReasonInput.setTextColor(UiTheme.NAVY);
        correctionReasonInput.setTypeface(regular);
        ScreenTypography.apply(correctionReasonInput, 13);
        correctionReasonInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                UiTheme.dp(context, 12), UiTheme.dp(context, 10));
        correctionReasonInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        card.addView(correctionReasonInput);

        addSpacing(card, 4);

        TextView rNote = new TextView(context);
        rNote.setText("Reason and audit history are retained.");
        rNote.setTextColor(UiTheme.MUTED);
        rNote.setTypeface(regular);
        ScreenTypography.apply(rNote, 12);
        card.addView(rNote);

        addSpacing(card, 16);

        // Researcher-visible feedback
        TextView fbLbl = new TextView(context);
        fbLbl.setText("Feedback to researcher");
        fbLbl.setTextColor(UiTheme.MUTED);
        fbLbl.setTypeface(regular);
        ScreenTypography.apply(fbLbl, 12);
        card.addView(fbLbl);

        EditText compFb = new EditText(context);
        compFb.setText("Registration recorded under AY 2026-2027. Research ID: REG-2026-074.");
        compFb.setTextColor(UiTheme.NAVY);
        compFb.setTypeface(regular);
        ScreenTypography.apply(compFb, 13);
        compFb.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                UiTheme.dp(context, 12), UiTheme.dp(context, 10));
        compFb.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        card.addView(compFb);

        addSpacing(card, 16);

        // Checkpoint
        TextView cpLbl = new TextView(context);
        cpLbl.setText("Processing checkpoint");
        cpLbl.setTextColor(UiTheme.NAVY);
        cpLbl.setTypeface(bold);
        ScreenTypography.apply(cpLbl, 14);
        card.addView(cpLbl);

        addSpacing(card, 4);

        TextView cpNameLbl = new TextView(context);
        cpNameLbl.setText("Checkpoint name: Registration recorded");
        cpNameLbl.setTextColor(UiTheme.NAVY);
        cpNameLbl.setTypeface(regular);
        ScreenTypography.apply(cpNameLbl, 13);
        card.addView(cpNameLbl);

        TextView cpDtLbl = new TextView(context);
        cpDtLbl.setText("Date: 2026-10-14");
        cpDtLbl.setTextColor(UiTheme.MUTED);
        cpDtLbl.setTypeface(regular);
        ScreenTypography.apply(cpDtLbl, 12);
        card.addView(cpDtLbl);

        TextView cpRefLbl = new TextView(context);
        cpRefLbl.setText("Supporting reference: CRM log 2026-10-118");
        cpRefLbl.setTextColor(UiTheme.MUTED);
        cpRefLbl.setTypeface(regular);
        ScreenTypography.apply(cpRefLbl, 12);
        card.addView(cpRefLbl);

        addSpacing(card, 20);

        Button saveCorrBtn = UiButton.primary(context, "Save Correction", () ->
                ph.edu.umak.crm.app.common.dialogs.UiDialog.alert(context, "Correction not saved",
                        "This workspace can’t send review corrections to CRM yet."));
        card.addView(saveCorrBtn);

        addSpacing(card, 10);

        Button cancelBtn = UiButton.outline(context, "Cancel", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (callbacks != null) {
                callbacks.onBackToQueue();
            }
        });
        card.addView(cancelBtn);

        return card;
    }

    private View createErrorBanner(String message) {
        Typeface bold = UiTheme.boldFont(context);
        LinearLayout banner = new LinearLayout(context);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setPadding(UiTheme.dp(context, 14), UiTheme.dp(context, 12),
                UiTheme.dp(context, 14), UiTheme.dp(context, 12));
        banner.setBackground(UiTheme.shape(context, UiTheme.PEACH_BG, 8, UiTheme.PEACH_BORDER));

        TextView text = new TextView(context);
        text.setText(message);
        text.setTextColor(UiTheme.PEACH_TEXT);
        text.setTypeface(bold);
        ScreenTypography.apply(text, 13);
        banner.addView(text);

        return banner;
    }

    private View createStaleConflictBanner() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));
        card.setBackground(UiTheme.shape(context, UiTheme.AMBER_BG, 8, UiTheme.AMBER_BORDER));

        TextView title = new TextView(context);
        title.setText("This request was updated. Reload before saving");
        title.setTextColor(UiTheme.AMBER_TEXT);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 14);
        card.addView(title);

        addSpacing(card, 6);

        TextView sub = new TextView(context);
        sub.setText("Your changes have not been saved. Reload the latest version, then review it before making changes.");
        sub.setTextColor(UiTheme.AMBER_TEXT);
        sub.setTypeface(regular);
        ScreenTypography.apply(sub, 12);
        card.addView(sub);

        addSpacing(card, 12);

        Button reloadBtn = UiButton.primary(context, "Reload", () -> {
            setPreviewState("6.3a_workspace");
        });
        reloadBtn.setMinHeight(UiTheme.dp(context, 38));
        card.addView(reloadBtn);

        return card;
    }

    private void showStaleDialog() {
        new AlertDialog.Builder(context)
                .setTitle("This request was updated. Reload before saving")
                .setMessage("Your changes have not been saved. Reload the latest version, then review it before making changes.")
                .setPositiveButton("Reload", (dialog, which) -> {
                    if (live != null) { discardSessionDraft(); live.reload(); }
                    else setPreviewState("6.3a_workspace");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private View createLiveCompletedCorrection() {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.addView(createErrorBanner("Authorized correction to a completed request. A reason is required and retained in the audit history."));
        correctionReasonInput = new EditText(context);
        correctionReasonInput.setHint("Reason for correction *");
        correctionReasonInput.setContentDescription("Reason for correcting this completed request");
        correctionReasonInput.setText(requestItem.correctionReason == null ? "" : requestItem.correctionReason);
        card.addView(correctionReasonInput);
        card.addView(createFeedbackCard());
        card.addView(createNextActionCard());
        card.addView(createInternalRemarksCard());
        card.addView(createCheckpointCard());
        liveSave = UiButton.primary(context, "Save Correction", this::saveLive);
        card.addView(liveSave);
        return card;
    }

    private void captureDraft() {
        if (requestItem == null) return;
        requestItem.targetStatus = selectedNextStatus;
        if (feedbackInput != null) requestItem.feedbackToResearcher = feedbackInput.getText().toString();
        if (nextActionInput != null) requestItem.nextAction = nextActionInput.getText().toString();
        if (deadlineDateInput != null) requestItem.deadlineDate = deadlineDateInput.getText().toString();
        if (deadlineTimeInput != null) requestItem.deadlineTime = deadlineTimeInput.getText().toString();
        if (internalRemarksInput != null) requestItem.internalRemarks = internalRemarksInput.getText().toString();
        if (checkpointNameInput != null) requestItem.checkpointName = checkpointNameInput.getText().toString();
        if (checkpointDateInput != null) requestItem.checkpointDate = checkpointDateInput.getText().toString();
        if (checkpointRefInput != null) requestItem.checkpointRef = checkpointRefInput.getText().toString();
        if (correctionReasonInput != null) requestItem.correctionReason = correctionReasonInput.getText().toString();
    }

    private String draftFingerprint() {
        captureDraft();
        if (requestItem == null) return "";
        org.json.JSONArray state = new org.json.JSONArray();
        for (String value : new String[]{requestItem.targetStatus, requestItem.feedbackToResearcher,
                requestItem.nextAction, requestItem.deadlineDate, requestItem.deadlineTime, requestItem.internalRemarks,
                requestItem.checkpointName, requestItem.checkpointDate, requestItem.checkpointRef, requestItem.correctionReason}) state.put(value);
        for (var item : requestItem.checklist) state.put(item.status);
        state.put(new org.json.JSONArray(requestItem.missingRequirementIndices));
        return state.toString();
    }

    private void saveLive() {
        if (saving || stale) return;
        captureDraft();
        try { live.save(StaffRequestData.review(requestItem)); }
        catch (IllegalArgumentException | org.json.JSONException error) { showResult(error.getMessage(), true, false); }
    }

    public void setSaving(boolean value) {
        saving = value;
        enable(contentLayout, !value);
        if (liveSave != null) liveSave.setEnabled(!value && !stale);
        if (value) liveStatus.working("Saving review to CRM…");
    }

    private void enable(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) enable(group.getChildAt(i), enabled);
    }

    public void showResult(String message, boolean error, boolean conflict) {
        if (liveStatus == null) return;
        stale = stale || conflict;
        liveStatus.result(message, error);
        if (liveSave != null) liveSave.setEnabled(!saving && !stale);
        if (conflict) {
            Button reload = UiButton.outline(context, "Reload request", UiTheme.BORDER, UiTheme.NAVY, this::showStaleDialog);
            contentLayout.addView(reload, 1);
        }
    }

    public void discardSessionDraft() {
        if (requestItem != null) sessionDrafts.remove(requestItem.id);
    }

    public void confirmLeave(Runnable leave) {
        if (saving) return;
        if (live == null || originalDraft.equals(draftFingerprint())) { leave.run(); return; }
        new AlertDialog.Builder(context).setTitle("Leave this review?")
                .setMessage("Keep your edits for this session, or discard them. They have not been sent to CRM.")
                .setPositiveButton("Keep draft", (dialog, which) -> {
                    captureDraft(); sessionDrafts.put(requestItem.id, requestItem); leave.run();
                })
                .setNegativeButton("Discard", (dialog, which) -> { discardSessionDraft(); leave.run(); })
                .setNeutralButton("Cancel", null).show();
    }

    private TextView createStatusBadge(String status) {
        Typeface bold = UiTheme.boldFont(context);
        TextView badge = new TextView(context);
        badge.setText(status);
        badge.setTypeface(bold);
        ScreenTypography.apply(badge, 11);
        badge.setPadding(UiTheme.dp(context, 8), UiTheme.dp(context, 3),
                UiTheme.dp(context, 8), UiTheme.dp(context, 3));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(UiTheme.dp(context, 4));

        if ("In review".equalsIgnoreCase(status)) {
            bg.setColor(Color.parseColor("#EBF3FA"));
            badge.setTextColor(Color.parseColor("#1B4D7E"));
        } else if ("Completed".equalsIgnoreCase(status)) {
            bg.setColor(Color.parseColor("#E6F4EA"));
            badge.setTextColor(Color.parseColor("#137333"));
        } else if ("For correction".equalsIgnoreCase(status)) {
            bg.setColor(UiTheme.AMBER_BG);
            badge.setTextColor(UiTheme.AMBER_TEXT);
        } else {
            bg.setColor(UiTheme.TAG_BG);
            badge.setTextColor(UiTheme.TAG_TEXT);
        }

        badge.setBackground(bg);
        return badge;
    }

    private void addSpacing(int dp) {
        View s = new View(context);
        s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        contentLayout.addView(s);
    }

    private void addSpacing(LinearLayout parent, int dp) {
        View s = new View(context);
        s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(s);
    }
}
