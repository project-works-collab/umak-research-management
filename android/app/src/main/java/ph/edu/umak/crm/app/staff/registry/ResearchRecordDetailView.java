package ph.edu.umak.crm.app.staff.registry;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
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

    private LinearLayout pageWrapper;

    public ViewGroup getContentLayout() {
        if (pageWrapper != null && pageWrapper.getParent() instanceof ViewGroup) {
            ((ViewGroup) pageWrapper.getParent()).removeView(pageWrapper);
        }
        return pageWrapper;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    private void buildUi() {
        pageWrapper = new LinearLayout(activity);
        pageWrapper.setOrientation(LinearLayout.VERTICAL);

        // Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(activity);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = dp(16);
        band.setPadding(pad, pad, pad, pad);

        TextView heading = new TextView(activity);
        heading.setText("Research Record");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(activity));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        pageWrapper.addView(band);

        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));
        pageWrapper.addView(rootLayout);

        Typeface bold = UiTheme.boldFont(activity);

        // 2. Record Summary Card (ID, Title, Stage, Meta)
        rootLayout.addView(createRecordHeaderCard());

        // 3. Team Members Card
        rootLayout.addView(createTeamMembersCard());

        // 4. Metadata Card (Adviser, College, Academic Period, Registrant Category)
        rootLayout.addView(createMetadataCard());

        // 5. Documents Card
        rootLayout.addView(createDocumentsCard());

        // 6. Linked Service Requests Card
        rootLayout.addView(createLinkedRequestsCard());

        // 7. Action Buttons (Edit Record & Correction History)
        Button editBtn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        editBtn.setText("Edit Record");
        editBtn.setAllCaps(false);
        editBtn.setTextColor(0xffffffff);
        editBtn.setTypeface(bold);
        ScreenTypography.apply(editBtn, 15);
        editBtn.setBackground(UiTheme.ripple(UiTheme.shape(activity, 0xff131c4b, 8, 0), 0x20ffffff));
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        editParams.topMargin = dp(16);
        editParams.bottomMargin = dp(10);
        rootLayout.addView(editBtn, editParams);

        editBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onEditRecord(record);
            }
        });

        Button historyBtn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        historyBtn.setText("Correction History");
        historyBtn.setAllCaps(false);
        historyBtn.setTextColor(0xff082d50);
        historyBtn.setTypeface(bold);
        ScreenTypography.apply(historyBtn, 15);
        historyBtn.setBackground(UiTheme.ripple(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER), 0x20082d50));
        LinearLayout.LayoutParams histParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        rootLayout.addView(historyBtn, histParams);

        historyBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onCorrectionHistory(record);
            }
        });
    }

    private View createRecordHeaderCard() {
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        LinearLayout card = createBaseCard();

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView idText = new TextView(activity);
        idText.setText(record.id);
        idText.setTextColor(0xff185d9d);
        idText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        idText.setTypeface(bold);
        topRow.addView(idText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        card.addView(topRow);

        TextView titleText = new TextView(activity);
        titleText.setText(record.title);
        titleText.setTextColor(0xff082d50);
        titleText.setTypeface(bold);
        ScreenTypography.apply(titleText, 18);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(4);
        titleParams.bottomMargin = dp(6);
        card.addView(titleText, titleParams);

        // Stage Row: Stage [In progress]
        LinearLayout stageRow = new LinearLayout(activity);
        stageRow.setOrientation(LinearLayout.HORIZONTAL);
        stageRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView stageLbl = new TextView(activity);
        stageLbl.setText("Stage ");
        stageLbl.setTextColor(0xff586b83);
        stageLbl.setTypeface(regular);
        ScreenTypography.apply(stageLbl, 12);
        stageRow.addView(stageLbl);

        TextView stageBadge = new TextView(activity);
        stageBadge.setText(record.stage);
        ScreenTypography.apply(stageBadge, 11);
        stageBadge.setTypeface(bold);
        stageBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(dp(4));
        badgeBg.setColor(Color.parseColor("#FEF3C7"));
        stageBadge.setTextColor(Color.parseColor("#D97706"));
        stageBadge.setBackground(badgeBg);
        stageRow.addView(stageBadge);

        LinearLayout.LayoutParams stageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stageParams.bottomMargin = dp(6);
        card.addView(stageRow, stageParams);

        TextView metaText = new TextView(activity);
        metaText.setText("Registered " + record.registeredDate + " · Last edit " + record.lastEditDate);
        metaText.setTextColor(0xff64748b);
        metaText.setTypeface(regular);
        ScreenTypography.apply(metaText, 12);
        card.addView(metaText);

        return card;
    }

    private View createTeamMembersCard() {
        Typeface bold = UiTheme.boldFont(activity);

        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Team members");
        title.setTextColor(0xff082d50);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 15);
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
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(4), 0, dp(4));

        // Initials Avatar
        TextView avatar = new TextView(activity);
        avatar.setText(member.initials);
        avatar.setTextColor(0xff0284c7);
        avatar.setTypeface(bold);
        ScreenTypography.apply(avatar, 13);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#E0F2FE"));
        avatar.setBackground(avatarBg);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(36), dp(36));
        avatarParams.rightMargin = dp(12);
        row.addView(avatar, avatarParams);

        // Name & Role
        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView nameText = new TextView(activity);
        nameText.setText(member.name);
        nameText.setTextColor(0xff082d50);
        nameText.setTypeface(bold);
        ScreenTypography.apply(nameText, 14);
        textCol.addView(nameText);

        TextView roleText = new TextView(activity);
        roleText.setText(member.role);
        roleText.setTextColor(0xff64748b);
        roleText.setTypeface(regular);
        ScreenTypography.apply(roleText, 12);
        textCol.addView(roleText);

        row.addView(textCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        // Category Tag (Student / Faculty)
        TextView catTag = new TextView(activity);
        catTag.setText(member.category);
        ScreenTypography.apply(catTag, 11);
        catTag.setTypeface(bold);
        catTag.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable tagBg = new GradientDrawable();
        tagBg.setCornerRadius(dp(4));
        tagBg.setColor(Color.parseColor("#E0F2FE"));
        catTag.setTextColor(0xff0284c7);
        catTag.setBackground(tagBg);
        row.addView(catTag);

        return row;
    }

    private View createMetadataCard() {
        LinearLayout card = createBaseCard();

        addMetaItem(card, "ADVISER", record.facultyAdviser != null ? record.facultyAdviser : "None", null, false);
        addMetaItem(card, "COLLEGE", record.college, null, false);
        addMetaItem(card, "ACADEMIC PERIOD", record.academicPeriod, null, false);
        addMetaItem(card, "REGISTRANT CATEGORY", record.registrantCategory, "🔒 Retained from registration", true);

        return card;
    }

    private void addMetaItem(LinearLayout card, String label, String value, String subtext, boolean isLast) {
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        TextView labelView = new TextView(activity);
        labelView.setText(label);
        labelView.setTextColor(0xff586b83);
        labelView.setTypeface(bold);
        ScreenTypography.apply(labelView, 11);
        card.addView(labelView);

        TextView valView = new TextView(activity);
        valView.setText(value);
        valView.setTextColor(0xff082d50);
        valView.setTypeface(regular);
        ScreenTypography.apply(valView, 14);
        LinearLayout.LayoutParams valParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valParams.topMargin = dp(2);
        card.addView(valView, valParams);

        if (subtext != null) {
            TextView subView = new TextView(activity);
            subView.setText(subtext);
            subView.setTextColor(0xff64748b);
            subView.setTypeface(regular);
            ScreenTypography.apply(subView, 12);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            subParams.topMargin = dp(2);
            card.addView(subView, subParams);
        }

        if (!isLast) {
            View space = new View(activity);
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(12));
            card.addView(space, sp);
        }
    }

    private View createDocumentsCard() {
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        LinearLayout card = createBaseCard();

        LinearLayout headerRow = new LinearLayout(activity);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, 0, 0, dp(12));

        TextView title = new TextView(activity);
        title.setText("Documents");
        title.setTextColor(0xff082d50);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 15);
        headerRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView manageLink = new TextView(activity);
        manageLink.setText("Manage");
        manageLink.setTextColor(0xff185d9d);
        manageLink.setTypeface(bold);
        ScreenTypography.apply(manageLink, 13);
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

            ImageView docIcon = new ImageView(activity);
            docIcon.setImageResource(R.drawable.ic_crm_doc);
            docIcon.setColorFilter(0xff185d9d);
            LinearLayout.LayoutParams dParams = new LinearLayout.LayoutParams(dp(18), dp(18));
            dParams.rightMargin = dp(10);
            docRow.addView(docIcon, dParams);

            LinearLayout nameCol = new LinearLayout(activity);
            nameCol.setOrientation(LinearLayout.VERTICAL);

            TextView nameText = new TextView(activity);
            nameText.setText(doc.fileName);
            nameText.setTextColor(0xff082d50);
            nameText.setTypeface(bold);
            ScreenTypography.apply(nameText, 13);
            nameCol.addView(nameText);

            TextView sizeText = new TextView(activity);
            sizeText.setText(doc.fileSize);
            sizeText.setTextColor(0xff64748b);
            sizeText.setTypeface(regular);
            ScreenTypography.apply(sizeText, 12);
            nameCol.addView(sizeText);

            docRow.addView(nameCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView badge = new TextView(activity);
            badge.setText(doc.isCurrent ? "Current" : "Earlier version");
            ScreenTypography.apply(badge, 11);
            badge.setTypeface(bold);
            badge.setPadding(dp(8), dp(3), dp(8), dp(3));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(4));
            if (doc.isCurrent) {
                bg.setColor(Color.parseColor("#DCFCE7"));
                badge.setTextColor(Color.parseColor("#16A34A"));
            } else {
                bg.setColor(Color.parseColor("#F1F5F9"));
                badge.setTextColor(Color.parseColor("#64748B"));
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
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        LinearLayout card = createBaseCard();

        TextView title = new TextView(activity);
        title.setText("Linked service requests");
        title.setTextColor(0xff082d50);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 15);
        card.addView(title);

        addSpacing(card, 8);

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
            reqId.setTextColor(0xff185d9d);
            reqId.setTypeface(bold);
            ScreenTypography.apply(reqId, 12);
            infoCol.addView(reqId);

            TextView serviceText = new TextView(activity);
            serviceText.setText(req.serviceTitle);
            serviceText.setTextColor(0xff082d50);
            serviceText.setTypeface(bold);
            ScreenTypography.apply(serviceText, 14);
            infoCol.addView(serviceText);

            reqRow.addView(infoCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView statusBadge = new TextView(activity);
            statusBadge.setText(req.status);
            ScreenTypography.apply(statusBadge, 11);
            statusBadge.setTypeface(bold);
            statusBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setCornerRadius(dp(4));
            if ("For correction".equalsIgnoreCase(req.status)) {
                badgeBg.setColor(Color.parseColor("#FEF3C7"));
                statusBadge.setTextColor(Color.parseColor("#D97706"));
            } else if ("Submitted".equalsIgnoreCase(req.status)) {
                badgeBg.setColor(Color.parseColor("#E0F2FE"));
                statusBadge.setTextColor(Color.parseColor("#0284c7"));
            } else {
                badgeBg.setColor(Color.parseColor("#DCFCE7"));
                statusBadge.setTextColor(Color.parseColor("#16A34A"));
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

        addSpacing(card, 6);

        TextView note = new TextView(activity);
        note.setText("Each service request links to at most one research record.");
        note.setTextColor(0xff64748b);
        note.setTypeface(regular);
        ScreenTypography.apply(note, 12);
        card.addView(note);

        return card;
    }

    private LinearLayout createBaseCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(12);
        card.setLayoutParams(params);
        return card;
    }

    private void addSpacing(LinearLayout parent, int dp) {
        View s = new View(activity);
        s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(dp)));
        parent.addView(s);
    }
}
