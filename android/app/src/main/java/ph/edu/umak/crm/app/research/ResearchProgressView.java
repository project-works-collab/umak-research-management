package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
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

        addSpacing(context, layout, 12);

        // Check if empty state (4.6d)
        if (progressData == null) {
            renderEmptyState();
            return;
        }

        // 1. Overview Card
        LinearLayout overviewCard = new LinearLayout(context);
        overviewCard.setOrientation(LinearLayout.VERTICAL);
        overviewCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        overviewCard.setPadding(pad, pad, pad, pad);

        TextView idText = new TextView(context);
        idText.setText(progressData.id);
        idText.setTextColor(0xff185d9d);
        idText.setTypeface(bold);
        ScreenTypography.apply(idText, 12);
        overviewCard.addView(idText);

        addSpacing(context, overviewCard, 4);

        TextView titleText = new TextView(context);
        titleText.setText(progressData.title);
        titleText.setTextColor(0xff082d50);
        titleText.setTypeface(bold);
        ScreenTypography.apply(titleText, 18);
        overviewCard.addView(titleText);

        addSpacing(context, overviewCard, 10);

        LinearLayout stageRow = new LinearLayout(context);
        stageRow.setOrientation(LinearLayout.HORIZONTAL);
        stageRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView stageLabel = new TextView(context);
        stageLabel.setText("Latest stage:  ");
        stageLabel.setTextColor(0xff082d50);
        stageLabel.setTypeface(bold);
        ScreenTypography.apply(stageLabel, 13);
        stageRow.addView(stageLabel);

        TextView stageBadge = new TextView(context);
        stageBadge.setText(progressData.latestStage);
        stageBadge.setTypeface(bold);
        ScreenTypography.apply(stageBadge, 12);
        int px = UiTheme.dp(context, 8);
        int py = UiTheme.dp(context, 3);
        stageBadge.setPadding(px, py, px, py);
        if ("Completed".equalsIgnoreCase(progressData.latestStage)) {
            stageBadge.setBackground(UiTheme.shape(context, 0xffdcfce7, 4, 0));
            stageBadge.setTextColor(0xff16a34a);
        } else {
            stageBadge.setBackground(UiTheme.shape(context, 0xffe5edf7, 4, 0));
            stageBadge.setTextColor(0xff185d9d);
        }
        stageRow.addView(stageBadge);
        overviewCard.addView(stageRow);

        layout.addView(overviewCard);

        addSpacing(context, layout, 12);

        // 2. Interactive Numbered Lifecycle Timeline Card (1 to 4 with clickable stages)
        View timelineCard = RegisteredProgressView.createLifecycleTimeline(context, progressData);
        layout.addView(timelineCard);

        addSpacing(context, layout, 12);

        // 3. Requirements Card
        LinearLayout reqCard = new LinearLayout(context);
        reqCard.setOrientation(LinearLayout.VERTICAL);
        reqCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        reqCard.setPadding(pad, pad, pad, pad);

        TextView reqHdr = new TextView(context);
        reqHdr.setText("Requirements");
        reqHdr.setTextColor(0xff082d50);
        reqHdr.setTypeface(bold);
        ScreenTypography.apply(reqHdr, 16);
        reqCard.addView(reqHdr);

        addSpacing(context, reqCard, 10);

        for (ResearchData.RequirementItem item : progressData.requirements) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

            boolean isCompleted = "Completed".equalsIgnoreCase(item.status);

            if (isCompleted) {
                ImageView check = new ImageView(context);
                check.setImageResource(R.drawable.ic_crm_check_small);
                check.setColorFilter(0xff16a34a);
                int icon16 = UiTheme.dp(context, 16);
                row.addView(check, new LinearLayout.LayoutParams(icon16, icon16));
            } else {
                View hollow = new View(context);
                int circleSize = UiTheme.dp(context, 14);
                hollow.setBackground(UiTheme.shape(context, UiTheme.WHITE, circleSize / 2, 0xff94a3b8));
                LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(circleSize, circleSize);
                hLp.setMarginEnd(UiTheme.dp(context, 2));
                row.addView(hollow, hLp);
            }

            TextView rText = new TextView(context);
            rText.setText(item.title);
            rText.setTextColor(0xff082d50);
            rText.setTypeface(regular);
            ScreenTypography.apply(rText, 13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMarginStart(UiTheme.dp(context, 8));
            row.addView(rText, lp);

            TextView statusText = new TextView(context);
            statusText.setText(item.status);
            statusText.setTypeface(isCompleted ? bold : regular);
            ScreenTypography.apply(statusText, 12);
            statusText.setTextColor(isCompleted ? 0xff16a34a : 0xff586b83);
            row.addView(statusText);

            reqCard.addView(row);
        }

        layout.addView(reqCard);

        addSpacing(context, layout, 12);

        // 4. Next Action Card
        LinearLayout actionCard = new LinearLayout(context);
        actionCard.setOrientation(LinearLayout.VERTICAL);
        actionCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        actionCard.setPadding(pad, pad, pad, pad);

        TextView actHdr = new TextView(context);
        actHdr.setText("Next action");
        actHdr.setTextColor(0xff082d50);
        actHdr.setTypeface(bold);
        ScreenTypography.apply(actHdr, 16);
        actionCard.addView(actHdr);

        addSpacing(context, actionCard, 6);

        TextView actDesc = new TextView(context);
        actDesc.setText(progressData.nextAction != null ? progressData.nextAction : "No next action recorded");
        actDesc.setTextColor("No next action recorded".equalsIgnoreCase(progressData.nextAction) ? 0xffb45309 : 0xff082d50);
        actDesc.setTypeface(regular);
        ScreenTypography.apply(actDesc, 13);
        actionCard.addView(actDesc);

        addSpacing(context, actionCard, 8);

        TextView contactHdr = new TextView(context);
        contactHdr.setText("Responsible contact");
        contactHdr.setTextColor(0xff586b83);
        contactHdr.setTypeface(regular);
        ScreenTypography.apply(contactHdr, 12);
        actionCard.addView(contactHdr);

        TextView contactVal = new TextView(context);
        contactVal.setText(progressData.responsibleContact != null ? progressData.responsibleContact : "Center for Research Management · research@umak.edu.ph");
        contactVal.setTextColor(0xff082d50);
        contactVal.setTypeface(regular);
        ScreenTypography.apply(contactVal, 13);
        actionCard.addView(contactVal);

        if (progressData.deadline != null) {
            addSpacing(context, actionCard, 8);
            TextView dlHdr = new TextView(context);
            dlHdr.setText("Deadline");
            dlHdr.setTextColor(0xff586b83);
            dlHdr.setTypeface(regular);
            ScreenTypography.apply(dlHdr, 12);
            actionCard.addView(dlHdr);

            TextView dlVal = new TextView(context);
            dlVal.setText(progressData.deadline);
            dlVal.setTextColor(0xff082d50);
            dlVal.setTypeface(bold);
            ScreenTypography.apply(dlVal, 13);
            actionCard.addView(dlVal);
        }

        layout.addView(actionCard);

        addSpacing(context, layout, 12);

        // 5. Outcome Cards (Completion, Presentation, Publication)
        for (ResearchData.OutcomeItem outcome : progressData.outcomes) {
            View oCard = createOutcomeCard(outcome);
            layout.addView(oCard);
            addSpacing(context, layout, 10);
        }

        // Section note below outcomes
        TextView outcomeNote = new TextView(context);
        outcomeNote.setText("Each outcome is recorded separately by CRM.");
        outcomeNote.setTextColor(0xff586b83);
        outcomeNote.setTypeface(regular);
        ScreenTypography.apply(outcomeNote, 12);
        outcomeNote.setGravity(Gravity.CENTER);
        outcomeNote.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 16));
        layout.addView(outcomeNote);

        // Back to Profile Link / Button
        Button backToProfileBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        backToProfileBtn.setText("← Back to Profile");
        backToProfileBtn.setAllCaps(false);
        backToProfileBtn.setTextColor(0xff082d50);
        backToProfileBtn.setTypeface(bold);
        ScreenTypography.apply(backToProfileBtn, 14);
        backToProfileBtn.setBackgroundColor(0);
        backToProfileBtn.setOnClickListener(v -> {
            if (listener != null) listener.onBack();
        });
        layout.addView(backToProfileBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 44)));
    }

    private View createOutcomeCard(ResearchData.OutcomeItem outcome) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 14);
        card.setPadding(pad, pad, pad, pad);

        TextView catTitle = new TextView(context);
        catTitle.setText(outcome.category);
        catTitle.setTextColor(0xff082d50);
        catTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(catTitle, 16);
        card.addView(catTitle);

        addSpacing(context, card, 8);

        addOutcomeRow(card, "Status", outcome.status);
        addOutcomeRow(card, "Date", outcome.date);
        addOutcomeRow(card, outcome.detailLabel, outcome.detailValue);

        return card;
    }

    private void addOutcomeRow(LinearLayout parent, String label, String value) {
        boolean isMultiLine = value != null && value.length() > 25;

        LinearLayout row = new LinearLayout(context);
        row.setPadding(0, UiTheme.dp(context, 3), 0, UiTheme.dp(context, 3));

        if (isMultiLine) {
            row.setOrientation(LinearLayout.VERTICAL);

            TextView lbl = new TextView(context);
            lbl.setText(label);
            lbl.setTextColor(0xff586b83);
            lbl.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(lbl, 13);
            lbl.setPadding(0, 0, 0, UiTheme.dp(context, 2));
            row.addView(lbl);

            TextView val = new TextView(context);
            val.setText(value);
            val.setTextColor(0xff082d50);
            val.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(val, 13);
            row.addView(val);
        } else {
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView lbl = new TextView(context);
            lbl.setText(label);
            lbl.setTextColor(0xff586b83);
            lbl.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(lbl, 13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(lbl, lp);

            TextView val = new TextView(context);
            val.setText(value != null ? value : "—");
            val.setTextColor("Not recorded".equalsIgnoreCase(value) ? 0xff586b83 : 0xff082d50);
            val.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(val, 13);
            row.addView(val);
        }

        parent.addView(row);
    }

    private void renderEmptyState() {
        LinearLayout emptyCard = new LinearLayout(context);
        emptyCard.setOrientation(LinearLayout.VERTICAL);
        emptyCard.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 32);
        emptyCard.setPadding(pad, pad, pad, pad);

        FrameLayout iconBox = new FrameLayout(context);
        int boxSize = UiTheme.dp(context, 52);
        iconBox.setBackground(UiTheme.shape(context, 0xfff1f5f9, boxSize / 2, 0));
        ImageView folderIcon = new ImageView(context);
        folderIcon.setImageResource(R.drawable.ic_crm_doc);
        folderIcon.setColorFilter(0xff586b83);
        int icon24 = UiTheme.dp(context, 24);
        FrameLayout.LayoutParams fLp = new FrameLayout.LayoutParams(icon24, icon24, Gravity.CENTER);
        iconBox.addView(folderIcon, fLp);

        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(boxSize, boxSize);
        boxParams.bottomMargin = UiTheme.dp(context, 16);
        emptyCard.addView(iconBox, boxParams);

        TextView emptyTitle = new TextView(context);
        emptyTitle.setText("No research registered");
        emptyTitle.setTextColor(0xff082d50);
        emptyTitle.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(emptyTitle, 18);
        emptyCard.addView(emptyTitle);

        addSpacing(context, emptyCard, 20);

        Button registerBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        registerBtn.setText("Register research");
        registerBtn.setAllCaps(false);
        registerBtn.setTextColor(0xffffffff);
        registerBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(registerBtn, 15);
        registerBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        registerBtn.setMinHeight(UiTheme.dp(context, 46));
        registerBtn.setOnClickListener(v -> {
            if (listener != null) listener.onRegisterResearch();
        });
        emptyCard.addView(registerBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));

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
        return PageTitle.wrap(layout, "Research Progress", null, false);
    }
}
