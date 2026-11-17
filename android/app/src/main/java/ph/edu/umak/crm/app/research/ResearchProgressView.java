package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class ResearchProgressView {

    public interface Listener {
        void onBack();
        void onRegisterResearch();
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;
    private ResearchData.StudyProgress progressData;

    public ResearchProgressView(Context context, ResearchData.StudyProgress progress, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.progressData = progress != null ? progress : ResearchData.getStudyProgress("4.6c");

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, pad);

        renderContent();
    }

    public void setPreviewState(String state) {
        this.progressData = ResearchData.getStudyProgress(state);
        renderContent();
    }

    private void renderContent() {
        layout.removeAllViews();

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 14);

        // Header Row: Back button on left
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        Button backBtn = UiButton.text(context, "Back", UiTheme.NAVY, () -> {
            if (listener != null) listener.onBack();
        });
        backBtn.setPadding(0, 0, UiTheme.dp(context, 12), 0);
        headerRow.addView(backBtn);

        LinearLayout titleCol = new LinearLayout(context);
        titleCol.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(context);
        title.setText("Research Progress");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 20);
        titleCol.addView(title);

        headerRow.addView(titleCol);
        layout.addView(headerRow);

        addSpacing(context, layout, 16);

        // Check if empty state (4.6d)
        if (progressData == null) {
            renderEmptyState();
            return;
        }

        // Overview Card
        LinearLayout overviewCard = new LinearLayout(context);
        overviewCard.setOrientation(LinearLayout.VERTICAL);
        overviewCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        overviewCard.setPadding(pad, pad, pad, pad);

        TextView idText = new TextView(context);
        idText.setText(progressData.id);
        idText.setTextColor(UiTheme.MUTED);
        idText.setTypeface(bold);
        ScreenTypography.apply(idText, 13);
        overviewCard.addView(idText);

        addSpacing(context, overviewCard, 4);

        TextView titleText = new TextView(context);
        titleText.setText(progressData.title);
        titleText.setTextColor(UiTheme.NAVY);
        titleText.setTypeface(bold);
        ScreenTypography.apply(titleText, 17);
        overviewCard.addView(titleText);

        addSpacing(context, overviewCard, 8);

        TextView stageBadge = new TextView(context);
        stageBadge.setText("Latest stage: " + progressData.latestStage);
        stageBadge.setTypeface(bold);
        ScreenTypography.apply(stageBadge, 12);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 4);
        stageBadge.setPadding(px, py, px, py);
        if ("Completed".equalsIgnoreCase(progressData.latestStage)) {
            stageBadge.setBackground(UiTheme.shape(context, UiTheme.STATUS_COMPLETED_BG, 4, 0));
            stageBadge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
        } else {
            stageBadge.setBackground(UiTheme.shape(context, UiTheme.STATUS_IN_REVIEW_BG, 4, 0));
            stageBadge.setTextColor(UiTheme.STATUS_IN_REVIEW_TEXT);
        }
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stageBadge.setLayoutParams(bLp);
        overviewCard.addView(stageBadge);

        layout.addView(overviewCard);

        addSpacing(context, layout, 14);

        // Stage History Card
        LinearLayout historyCard = new LinearLayout(context);
        historyCard.setOrientation(LinearLayout.VERTICAL);
        historyCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        historyCard.setPadding(pad, pad, pad, pad);

        TextView histHdr = new TextView(context);
        histHdr.setText("Stage history");
        histHdr.setTextColor(UiTheme.NAVY);
        histHdr.setTypeface(bold);
        ScreenTypography.apply(histHdr, 15);
        historyCard.addView(histHdr);

        addSpacing(context, historyCard, 8);

        for (ResearchData.StageHistoryItem item : progressData.stageHistory) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, UiTheme.dp(context, 3), 0, UiTheme.dp(context, 3));

            TextView sText = new TextView(context);
            sText.setText(item.stage);
            sText.setTextColor(UiTheme.NAVY);
            sText.setTypeface(regular);
            ScreenTypography.apply(sText, 13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(sText, lp);

            TextView dText = new TextView(context);
            dText.setText(item.date);
            dText.setTextColor(UiTheme.MUTED);
            dText.setTypeface(regular);
            ScreenTypography.apply(dText, 13);
            row.addView(dText);

            historyCard.addView(row);
        }

        layout.addView(historyCard);

        addSpacing(context, layout, 14);

        // Requirements Card
        LinearLayout reqCard = new LinearLayout(context);
        reqCard.setOrientation(LinearLayout.VERTICAL);
        reqCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        reqCard.setPadding(pad, pad, pad, pad);

        TextView reqHdr = new TextView(context);
        reqHdr.setText("Requirements");
        reqHdr.setTextColor(UiTheme.NAVY);
        reqHdr.setTypeface(bold);
        ScreenTypography.apply(reqHdr, 15);
        reqCard.addView(reqHdr);

        addSpacing(context, reqCard, 8);

        for (ResearchData.RequirementItem item : progressData.requirements) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));

            TextView rText = new TextView(context);
            rText.setText(item.title);
            rText.setTextColor(UiTheme.NAVY);
            rText.setTypeface(regular);
            ScreenTypography.apply(rText, 13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(rText, lp);

            TextView badge = new TextView(context);
            badge.setText(item.status);
            badge.setTypeface(bold);
            ScreenTypography.apply(badge, 11);
            int rpx = UiTheme.dp(context, 6);
            int rpy = UiTheme.dp(context, 2);
            badge.setPadding(rpx, rpy, rpx, rpy);
            if ("Completed".equalsIgnoreCase(item.status)) {
                badge.setBackground(UiTheme.shape(context, UiTheme.SUCCESS_BG, 4, 0));
                badge.setTextColor(UiTheme.SUCCESS_GREEN);
            } else {
                badge.setBackground(UiTheme.shape(context, 0xFFF1F5F9, 4, 0));
                badge.setTextColor(UiTheme.MUTED);
            }
            row.addView(badge);

            reqCard.addView(row);
        }

        layout.addView(reqCard);

        addSpacing(context, layout, 14);

        // Next Action Card
        LinearLayout actionCard = new LinearLayout(context);
        actionCard.setOrientation(LinearLayout.VERTICAL);
        actionCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        actionCard.setPadding(pad, pad, pad, pad);

        TextView actHdr = new TextView(context);
        actHdr.setText("Next action");
        actHdr.setTextColor(UiTheme.NAVY);
        actHdr.setTypeface(bold);
        ScreenTypography.apply(actHdr, 15);
        actionCard.addView(actHdr);

        addSpacing(context, actionCard, 6);

        TextView actDesc = new TextView(context);
        actDesc.setText(progressData.nextAction != null ? progressData.nextAction : "No next action recorded");
        actDesc.setTextColor(UiTheme.NAVY);
        actDesc.setTypeface(regular);
        ScreenTypography.apply(actDesc, 13);
        actionCard.addView(actDesc);

        addSpacing(context, actionCard, 8);

        TextView contactHdr = new TextView(context);
        contactHdr.setText("Responsible contact");
        contactHdr.setTextColor(UiTheme.MUTED);
        contactHdr.setTypeface(bold);
        ScreenTypography.apply(contactHdr, 12);
        actionCard.addView(contactHdr);

        TextView contactVal = new TextView(context);
        contactVal.setText(progressData.responsibleContact != null ? progressData.responsibleContact : "Center for Research Management");
        contactVal.setTextColor(UiTheme.NAVY);
        contactVal.setTypeface(regular);
        ScreenTypography.apply(contactVal, 13);
        actionCard.addView(contactVal);

        if (progressData.deadline != null) {
            addSpacing(context, actionCard, 8);
            TextView dlHdr = new TextView(context);
            dlHdr.setText("Deadline");
            dlHdr.setTextColor(UiTheme.ERROR_RED);
            dlHdr.setTypeface(bold);
            ScreenTypography.apply(dlHdr, 12);
            actionCard.addView(dlHdr);

            TextView dlVal = new TextView(context);
            dlVal.setText(progressData.deadline);
            dlVal.setTextColor(UiTheme.ERROR_RED);
            dlVal.setTypeface(bold);
            ScreenTypography.apply(dlVal, 13);
            actionCard.addView(dlVal);
        }

        layout.addView(actionCard);

        addSpacing(context, layout, 14);

        // Outcomes section note
        TextView outcomeNote = new TextView(context);
        outcomeNote.setText("Each outcome is recorded separately by CRM.");
        outcomeNote.setTextColor(UiTheme.MUTED);
        outcomeNote.setTypeface(regular);
        ScreenTypography.apply(outcomeNote, 12);
        layout.addView(outcomeNote);

        addSpacing(context, layout, 8);

        // Outcome Cards (Completion, Presentation, Publication)
        for (ResearchData.OutcomeItem outcome : progressData.outcomes) {
            View oCard = createOutcomeCard(outcome);
            layout.addView(oCard);
            addSpacing(context, layout, 10);
        }

        addSpacing(context, layout, 14);

        // Back to Profile Button
        Button backToProfileBtn = UiButton.outline(context, "Back to Profile", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (listener != null) listener.onBack();
        });
        layout.addView(backToProfileBtn);
    }

    private View createOutcomeCard(ResearchData.OutcomeItem outcome) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 12);
        card.setPadding(pad, pad, pad, pad);

        TextView catTitle = new TextView(context);
        catTitle.setText(outcome.category);
        catTitle.setTextColor(UiTheme.NAVY);
        catTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(catTitle, 14);
        card.addView(catTitle);

        addSpacing(context, card, 6);

        addOutcomeRow(card, "Status", outcome.status);
        addOutcomeRow(card, "Date", outcome.date);
        addOutcomeRow(card, outcome.detailLabel, outcome.detailValue);

        return card;
    }

    private void addOutcomeRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 2));

        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(UiTheme.MUTED);
        lbl.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(lbl, 12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(lbl, lp);

        TextView val = new TextView(context);
        val.setText(value);
        val.setTextColor("Not recorded".equalsIgnoreCase(value) ? UiTheme.MUTED : UiTheme.NAVY);
        val.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(val, 12);
        row.addView(val);

        parent.addView(row);
    }

    private void renderEmptyState() {
        LinearLayout emptyCard = new LinearLayout(context);
        emptyCard.setOrientation(LinearLayout.VERTICAL);
        emptyCard.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 32);
        emptyCard.setPadding(pad, pad, pad, pad);

        TextView emptyTitle = new TextView(context);
        emptyTitle.setText("No research registered");
        emptyTitle.setTextColor(UiTheme.NAVY);
        emptyTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(emptyTitle, 18);
        emptyCard.addView(emptyTitle);

        addSpacing(context, emptyCard, 16);

        Button registerBtn = UiButton.primary(context, "Register research", () -> {
            if (listener != null) listener.onRegisterResearch();
        });
        emptyCard.addView(registerBtn);

        layout.addView(emptyCard);
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
        return layout;
    }
}
