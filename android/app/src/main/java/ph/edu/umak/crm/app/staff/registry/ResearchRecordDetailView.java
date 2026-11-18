package ph.edu.umak.crm.app.staff.registry;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Research Record Detail View (Wireframe 7.2).
 * Shows record details, team members, documents, linked requests, and edit/correction history actions.
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class ResearchRecordDetailView {

    public interface Callbacks {
        void onBackToRegistry();
        void onEditRecord(StaffRegistryData.RegistryRecordItem record);
        void onCorrectionHistory(StaffRegistryData.RegistryRecordItem record);
        void onManageDocuments(StaffRegistryData.RegistryRecordItem record);
        void onOpenLinkedRequest(String requestId);
        void onMilestonesAndOutcomes(StaffRegistryData.RegistryRecordItem record);
    }

    private final Activity activity;
    private final Callbacks callbacks;
    private StaffRegistryData.RegistryRecordItem record;
    private LinearLayout rootLayout;

    public ResearchRecordDetailView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        this.record = StaffRegistryData.getMockRecords().get(0); // Default REQ-2026-089
        buildUi();
    }

    public void setRecord(StaffRegistryData.RegistryRecordItem record) {
        if (record != null) {
            this.record = record;
            buildUi();
        }
    }

    public ViewGroup getContentLayout() {
        if (rootLayout != null && rootLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) rootLayout.getParent()).removeView(rootLayout);
        }
        return PageTitle.wrap(rootLayout, "Research Record", null, false);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    private void buildUi() {
        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));

        // 1. Back button
        TextView backBtn = new TextView(activity);
        backBtn.setText("< Back to Registry");
        backBtn.setTextColor(UiTheme.ACCENT);
        backBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        backBtn.setTypeface(Typeface.DEFAULT_BOLD);
        backBtn.setPadding(0, 0, dp(16), dp(12));
        backBtn.setClickable(true);
        backBtn.setFocusable(true);
        backBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onBackToRegistry();
            }
        });
        rootLayout.addView(backBtn);

        // 2. Header


        // 3. Record Summary Card (ID, Title, Stage, Meta)
        rootLayout.addView(createRecordHeaderCard());

        // 4. Team Members Card
        rootLayout.addView(createTeamMembersCard());

        // 5. Metadata Card (Adviser, College, Academic Period, Registrant Category)
        rootLayout.addView(createMetadataCard());

        // 6. Documents Card
        rootLayout.addView(createDocumentsCard());

        // 7. Linked Service Requests Card
        rootLayout.addView(createLinkedRequestsCard());

        // 8. Action Buttons
        Button milestonesBtn = new Button(activity);
        milestonesBtn.setText("Milestones & Outcomes");
        milestonesBtn.setTextColor(UiTheme.WHITE);
        milestonesBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        milestonesBtn.setTypeface(Typeface.DEFAULT_BOLD);
        milestonesBtn.setAllCaps(false);
        GradientDrawable mBg = new GradientDrawable();
        mBg.setColor(UiTheme.ACCENT);
        mBg.setCornerRadius(dp(8));
        milestonesBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), mBg, null));
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        mParams.topMargin = dp(16);
        mParams.bottomMargin = dp(10);
        rootLayout.addView(milestonesBtn, mParams);

        milestonesBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onMilestonesAndOutcomes(record);
            }
        });

        Button editBtn = new Button(activity);
        editBtn.setText("Edit Record");
        editBtn.setTextColor(UiTheme.WHITE);
        editBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        editBtn.setTypeface(Typeface.DEFAULT_BOLD);
        editBtn.setAllCaps(false);
        GradientDrawable editBg = new GradientDrawable();
        editBg.setColor(UiTheme.NAVY);
        editBg.setCornerRadius(dp(8));
        editBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), editBg, null));
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        editParams.bottomMargin = dp(12);
        rootLayout.addView(editBtn, editParams);

        editBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onEditRecord(record);
            }
        });

        Button historyBtn = new Button(activity);
        historyBtn.setText("Correction History");
        historyBtn.setTextColor(UiTheme.NAVY);
        historyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        historyBtn.setTypeface(Typeface.DEFAULT_BOLD);
        historyBtn.setAllCaps(false);
        GradientDrawable histBg = new GradientDrawable();
        histBg.setColor(UiTheme.WHITE);
        histBg.setCornerRadius(dp(8));
        histBg.setStroke(dp(1), UiTheme.BORDER);
        historyBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22131c4b), histBg, null));
        LinearLayout.LayoutParams histParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        rootLayout.addView(historyBtn, histParams);

        historyBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onCorrectionHistory(record);
            }
        });
    }

    private View createRecordHeaderCard() {
        LinearLayout card = createBaseCard();

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView idText = new TextView(activity);
        idText.setText(record.id);
        idText.setTextColor(UiTheme.NAVY);
        idText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        idText.setTypeface(Typeface.DEFAULT_BOLD);
        topRow.addView(idText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView stageBadge = new TextView(activity);
        stageBadge.setText("Stage " + record.stage);
        stageBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        stageBadge.setTypeface(Typeface.DEFAULT_BOLD);
        stageBadge.setPadding(dp(10), dp(4), dp(10), dp(4));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(UiTheme.STATUS_IN_REVIEW_BG);
        badgeBg.setCornerRadius(dp(12));
        stageBadge.setTextColor(UiTheme.STATUS_IN_REVIEW_TEXT);
        stageBadge.setBackground(badgeBg);
        topRow.addView(stageBadge);

        card.addView(topRow);

        TextView titleText = new TextView(activity);
        titleText.setText(record.title);
        titleText.setTextColor(UiTheme.NAVY);
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleText.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(8);
        titleParams.bottomMargin = dp(8);
        card.addView(titleText, titleParams);

        TextView metaText = new TextView(activity);
        metaText.setText("Registered " + record.registeredDate + " · Last edit " + record.lastEditDate);
        metaText.setTextColor(UiTheme.MUTED);
        metaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        card.addView(metaText);

        return card;
    }

    private View createTeamMembersCard() {
        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Team members");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, 0, 0, dp(12));
        card.addView(title);

        for (int i = 0; i < record.teamMembers.size(); i++) {
            StaffRegistryData.TeamMember member = record.teamMembers.get(i);
            card.addView(createMemberRow(member));
            if (i < record.teamMembers.size() - 1) {
                View divider = new View(activity);
                divider.setBackgroundColor(UiTheme.BORDER);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                divParams.topMargin = dp(8);
                divParams.bottomMargin = dp(8);
                card.addView(divider, divParams);
            }
        }

        return card;
    }

    private View createMemberRow(StaffRegistryData.TeamMember member) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(4), 0, dp(4));

        // Initials Avatar
        TextView avatar = new TextView(activity);
        avatar.setText(member.initials);
        avatar.setTextColor(UiTheme.WHITE);
        avatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(UiTheme.ACCENT);
        avatar.setBackground(avatarBg);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(36), dp(36));
        avatarParams.rightMargin = dp(12);
        row.addView(avatar, avatarParams);

        // Name & Role
        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView nameText = new TextView(activity);
        nameText.setText(member.name);
        nameText.setTextColor(UiTheme.NAVY);
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        nameText.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(nameText);

        TextView roleText = new TextView(activity);
        roleText.setText(member.role);
        roleText.setTextColor(UiTheme.MUTED);
        roleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        textCol.addView(roleText);

        row.addView(textCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        // Category Tag (Student / Faculty)
        TextView catTag = new TextView(activity);
        catTag.setText(member.category);
        catTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        catTag.setTypeface(Typeface.DEFAULT_BOLD);
        catTag.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable tagBg = new GradientDrawable();
        tagBg.setColor(UiTheme.TAG_BG);
        tagBg.setCornerRadius(dp(6));
        catTag.setTextColor(UiTheme.TAG_TEXT);
        catTag.setBackground(tagBg);
        row.addView(catTag);

        return row;
    }

    private View createMetadataCard() {
        LinearLayout card = createBaseCard();

        addMetaItem(card, "Adviser", record.facultyAdviser != null ? record.facultyAdviser : "None", false);
        addMetaItem(card, "College", record.college, false);
        addMetaItem(card, "Academic period", record.academicPeriod, false);
        addMetaItem(card, "Registrant category", record.registrantCategory + " (Retained from registration)", true);

        return card;
    }

    private void addMetaItem(LinearLayout card, String label, String value, boolean isLast) {
        TextView labelView = new TextView(activity);
        labelView.setText(label);
        labelView.setTextColor(UiTheme.MUTED);
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        card.addView(labelView);

        TextView valView = new TextView(activity);
        valView.setText(value);
        valView.setTextColor(UiTheme.NAVY);
        valView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        valView.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams valParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valParams.topMargin = dp(2);
        valParams.bottomMargin = isLast ? 0 : dp(12);
        card.addView(valView, valParams);
    }

    private View createDocumentsCard() {
        LinearLayout card = createBaseCard();

        LinearLayout headerRow = new LinearLayout(activity);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, 0, 0, dp(12));

        TextView title = new TextView(activity);
        title.setText("Documents");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        headerRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView manageLink = new TextView(activity);
        manageLink.setText("Manage");
        manageLink.setTextColor(UiTheme.ACCENT);
        manageLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        manageLink.setTypeface(Typeface.DEFAULT_BOLD);
        manageLink.setClickable(true);
        manageLink.setFocusable(true);
        manageLink.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onManageDocuments(record);
            }
        });
        headerRow.addView(manageLink);

        card.addView(headerRow);

        for (int i = 0; i < record.documents.size(); i++) {
            StaffRegistryData.DocumentVersion doc = record.documents.get(i);
            LinearLayout docRow = new LinearLayout(activity);
            docRow.setOrientation(LinearLayout.HORIZONTAL);
            docRow.setGravity(Gravity.CENTER_VERTICAL);
            docRow.setPadding(0, dp(6), 0, dp(6));

            LinearLayout nameCol = new LinearLayout(activity);
            nameCol.setOrientation(LinearLayout.VERTICAL);

            TextView nameText = new TextView(activity);
            nameText.setText(doc.fileName);
            nameText.setTextColor(UiTheme.NAVY);
            nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            nameText.setTypeface(Typeface.DEFAULT_BOLD);
            nameCol.addView(nameText);

            TextView sizeText = new TextView(activity);
            sizeText.setText(doc.fileSize);
            sizeText.setTextColor(UiTheme.MUTED);
            sizeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            nameCol.addView(sizeText);

            docRow.addView(nameCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView badge = new TextView(activity);
            badge.setText(doc.isCurrent ? "Current" : "Earlier version");
            badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            badge.setTypeface(Typeface.DEFAULT_BOLD);
            badge.setPadding(dp(8), dp(3), dp(8), dp(3));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(10));
            if (doc.isCurrent) {
                bg.setColor(UiTheme.STATUS_COMPLETED_BG);
                badge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
            } else {
                bg.setColor(UiTheme.TAG_BG);
                badge.setTextColor(UiTheme.TAG_TEXT);
            }
            badge.setBackground(bg);
            docRow.addView(badge);

            card.addView(docRow);

            if (i < record.documents.size() - 1) {
                View divider = new View(activity);
                divider.setBackgroundColor(UiTheme.BORDER);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                divParams.topMargin = dp(6);
                divParams.bottomMargin = dp(6);
                card.addView(divider, divParams);
            }
        }

        return card;
    }

    private View createLinkedRequestsCard() {
        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Linked service requests");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(title);

        TextView note = new TextView(activity);
        note.setText("Each service request links to at most one research record.");
        note.setTextColor(UiTheme.MUTED);
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = dp(2);
        noteParams.bottomMargin = dp(12);
        card.addView(note, noteParams);

        for (int i = 0; i < record.linkedRequests.size(); i++) {
            StaffRegistryData.LinkedRequest req = record.linkedRequests.get(i);
            LinearLayout reqRow = new LinearLayout(activity);
            reqRow.setOrientation(LinearLayout.HORIZONTAL);
            reqRow.setGravity(Gravity.CENTER_VERTICAL);
            reqRow.setPadding(0, dp(6), 0, dp(6));

            LinearLayout infoCol = new LinearLayout(activity);
            infoCol.setOrientation(LinearLayout.VERTICAL);

            TextView reqId = new TextView(activity);
            reqId.setText(req.requestId);
            reqId.setTextColor(UiTheme.NAVY);
            reqId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            reqId.setTypeface(Typeface.DEFAULT_BOLD);
            infoCol.addView(reqId);

            TextView serviceText = new TextView(activity);
            serviceText.setText(req.serviceTitle);
            serviceText.setTextColor(UiTheme.MUTED);
            serviceText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            infoCol.addView(serviceText);

            reqRow.addView(infoCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView statusBadge = new TextView(activity);
            statusBadge.setText(req.status);
            statusBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            statusBadge.setTypeface(Typeface.DEFAULT_BOLD);
            statusBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setCornerRadius(dp(10));
            if ("For correction".equalsIgnoreCase(req.status)) {
                badgeBg.setColor(UiTheme.STATUS_CORRECTION_BG);
                statusBadge.setTextColor(UiTheme.STATUS_CORRECTION_TEXT);
            } else if ("Submitted".equalsIgnoreCase(req.status)) {
                badgeBg.setColor(UiTheme.STATUS_SUBMITTED_BG);
                statusBadge.setTextColor(UiTheme.STATUS_SUBMITTED_TEXT);
            } else {
                badgeBg.setColor(UiTheme.STATUS_COMPLETED_BG);
                statusBadge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
            }
            statusBadge.setBackground(badgeBg);
            reqRow.addView(statusBadge);

            reqRow.setClickable(true);
            reqRow.setFocusable(true);
            reqRow.setOnClickListener(v -> {
                if (callbacks != null) {
                    callbacks.onOpenLinkedRequest(req.requestId);
                }
            });

            card.addView(reqRow);

            if (i < record.linkedRequests.size() - 1) {
                View divider = new View(activity);
                divider.setBackgroundColor(UiTheme.BORDER);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                divParams.topMargin = dp(6);
                divParams.bottomMargin = dp(6);
                card.addView(divider, divParams);
            }
        }

        return card;
    }

    private LinearLayout createBaseCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), UiTheme.BORDER);
        card.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(12);
        card.setLayoutParams(params);
        return card;
    }
}
