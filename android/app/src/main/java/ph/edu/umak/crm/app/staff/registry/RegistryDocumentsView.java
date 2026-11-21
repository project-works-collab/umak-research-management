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
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Research Documents & Version Management View (Wireframe 7.6).
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class RegistryDocumentsView {

    public interface Callbacks {
        void onBackToRecord();
        void onChooseAttachment(Runnable onChecking, java.util.function.BiConsumer<ph.edu.umak.crm.app.MainActivity.SelectedAttachment, String> result);
    }

    private final Activity activity;
    private final Callbacks callbacks;
    private StaffRegistryData.RegistryRecordItem record;
    private LinearLayout rootLayout;

    public RegistryDocumentsView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        this.record = StaffRegistryData.getMockRecords().get(0);
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

        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        // Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(activity);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = dp(16);
        band.setPadding(pad, pad, pad, pad);

        // 1. Heading: "Documents"
        TextView heading = new TextView(activity);
        heading.setText("Documents");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(activity));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        // Subtitle: "REG-2026-089 · Impact of AI on Student Engagement"
        TextView subTitle = new TextView(activity);
        subTitle.setText((record != null ? record.id : "REG-2026-089") + " · " + (record != null ? record.title : "Research study"));
        subTitle.setTextColor(0xffe2e8f0);
        subTitle.setTypeface(regular);
        ScreenTypography.apply(subTitle, 13);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dp(4);
        band.addView(subTitle, subParams);

        pageWrapper.addView(band);

        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));
        pageWrapper.addView(rootLayout);

        // 2. Privacy / Security Notice Card
        LinearLayout privacyCard = new LinearLayout(activity);
        privacyCard.setOrientation(LinearLayout.HORIZONTAL);
        privacyCard.setGravity(Gravity.CENTER_VERTICAL);
        privacyCard.setPadding(dp(12), dp(10), dp(12), dp(10));
        privacyCard.setBackground(UiTheme.shape(activity, 0xfff8fafc, 8, 0xffe2e8f0));
        LinearLayout.LayoutParams privParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        privParams.bottomMargin = dp(14);
        privacyCard.setLayoutParams(privParams);

        TextView privText = new TextView(activity);
        privText.setText("🔒 Visible to linked researchers and authorized staff only.");
        privText.setTextColor(0xff586b83);
        privText.setTypeface(regular);
        ScreenTypography.apply(privText, 12);
        privacyCard.addView(privText);
        rootLayout.addView(privacyCard);

        // 3. Section: Attach a new version Card
        LinearLayout attachCard = createBaseCard();

        TextView attachSectionTitle = new TextView(activity);
        attachSectionTitle.setText("Attach a new version");
        attachSectionTitle.setTextColor(0xff082d50);
        attachSectionTitle.setTypeface(bold);
        ScreenTypography.apply(attachSectionTitle, 15);
        attachCard.addView(attachSectionTitle);

        // Dashed upload box / dropzone
        LinearLayout dropzone = new LinearLayout(activity);
        dropzone.setOrientation(LinearLayout.VERTICAL);
        dropzone.setGravity(Gravity.CENTER);
        dropzone.setPadding(dp(16), dp(20), dp(16), dp(20));
        dropzone.setBackground(UiTheme.shape(activity, 0xfff8fafc, 8, UiTheme.BORDER));
        LinearLayout.LayoutParams dropParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dropParams.topMargin = dp(10);
        dropParams.bottomMargin = dp(6);
        dropzone.setLayoutParams(dropParams);

        TextView uploadIcon = new TextView(activity);
        uploadIcon.setText("⇪");
        uploadIcon.setTextColor(0xff185d9d);
        uploadIcon.setTypeface(bold);
        ScreenTypography.apply(uploadIcon, 22);
        uploadIcon.setGravity(Gravity.CENTER);
        dropzone.addView(uploadIcon);

        TextView attachBtnText = new TextView(activity);
        attachBtnText.setText("Attach file");
        attachBtnText.setTextColor(0xff185d9d);
        attachBtnText.setTypeface(bold);
        ScreenTypography.apply(attachBtnText, 14);
        attachBtnText.setGravity(Gravity.CENTER);
        dropzone.addView(attachBtnText);

        TextView formatNote = new TextView(activity);
        formatNote.setText("PDF or DOCX, up to 10 MB each");
        formatNote.setTextColor(0xff64748b);
        formatNote.setTypeface(regular);
        ScreenTypography.apply(formatNote, 12);
        formatNote.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fnParams.topMargin = dp(2);
        dropzone.addView(formatNote, fnParams);

        attachCard.addView(dropzone);

        PendingStatus attachmentFeedback = new PendingStatus(activity);
        attachCard.addView(attachmentFeedback);

        dropzone.setOnClickListener(v -> {
            if (callbacks == null) return;
            callbacks.onChooseAttachment(() -> {
                attachBtnText.setText("Checking file…");
                attachmentFeedback.working("Checking the selected file…");
            }, (file, error) -> {
                if (!attachmentFeedback.isAttachedToWindow()) return;
                attachBtnText.setText("Attach file");
                attachmentFeedback.result(error != null ? error
                        : "Selected " + file.name() + ". This registry record does not support uploads to CRM.", error != null);
            });
        });

        TextView instructionNote = new TextView(activity);
        instructionNote.setText("Uploading keeps the research ID. The new file becomes the current version.");
        instructionNote.setTextColor(0xff64748b);
        instructionNote.setTypeface(regular);
        ScreenTypography.apply(instructionNote, 12);
        LinearLayout.LayoutParams instParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        instParams.topMargin = dp(6);
        attachCard.addView(instructionNote, instParams);

        rootLayout.addView(attachCard);

        // 4. Section: Version list Card
        LinearLayout versionCard = createBaseCard();

        TextView versionSectionTitle = new TextView(activity);
        versionSectionTitle.setText("Version list");
        versionSectionTitle.setTextColor(0xff082d50);
        versionSectionTitle.setTypeface(bold);
        ScreenTypography.apply(versionSectionTitle, 15);
        versionSectionTitle.setPadding(0, 0, 0, dp(12));
        versionCard.addView(versionSectionTitle);

        for (int i = 0; i < record.documents.size(); i++) {
            StaffRegistryData.DocumentVersion doc = record.documents.get(i);
            LinearLayout docRow = new LinearLayout(activity);
            docRow.setOrientation(LinearLayout.HORIZONTAL);
            docRow.setGravity(Gravity.CENTER_VERTICAL);
            docRow.setPadding(0, dp(8), 0, dp(8));

            ImageView docIcon = new ImageView(activity);
            docIcon.setImageResource(R.drawable.ic_crm_doc);
            docIcon.setColorFilter(0xff185d9d);
            LinearLayout.LayoutParams dParams = new LinearLayout.LayoutParams(dp(18), dp(18));
            dParams.rightMargin = dp(10);
            docRow.addView(docIcon, dParams);

            LinearLayout infoCol = new LinearLayout(activity);
            infoCol.setOrientation(LinearLayout.VERTICAL);

            TextView nameText = new TextView(activity);
            nameText.setText(doc.fileName);
            nameText.setTextColor(0xff082d50);
            nameText.setTypeface(bold);
            ScreenTypography.apply(nameText, 14);
            infoCol.addView(nameText);

            TextView metaText = new TextView(activity);
            metaText.setText(doc.fileSize + " · " + doc.date + " · " + doc.uploader);
            metaText.setTextColor(0xff64748b);
            metaText.setTypeface(regular);
            ScreenTypography.apply(metaText, 12);
            infoCol.addView(metaText);

            docRow.addView(infoCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView badge = new TextView(activity);
            badge.setText(doc.isCurrent ? "Current" : "Earlier version");
            ScreenTypography.apply(badge, 11);
            badge.setTypeface(bold);
            badge.setPadding(dp(8), dp(3), dp(8), dp(3));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setCornerRadius(dp(4));
            if (doc.isCurrent) {
                badgeBg.setColor(Color.parseColor("#DCFCE7"));
                badge.setTextColor(Color.parseColor("#16A34A"));
            } else {
                badgeBg.setColor(Color.parseColor("#F1F5F9"));
                badge.setTextColor(Color.parseColor("#64748B"));
            }
            badge.setBackground(badgeBg);
            docRow.addView(badge);

            versionCard.addView(docRow);

            if (i < record.documents.size() - 1) {
                View divider = new View(activity);
                divider.setBackgroundColor(UiTheme.BORDER);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                divParams.topMargin = dp(8);
                divParams.bottomMargin = dp(8);
                versionCard.addView(divider, divParams);
            }
        }

        rootLayout.addView(versionCard);

        // 5. Back to Record Button
        Button backBtn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        backBtn.setText("Back to Record");
        backBtn.setAllCaps(false);
        backBtn.setTextColor(0xff082d50);
        backBtn.setTypeface(bold);
        ScreenTypography.apply(backBtn, 15);
        backBtn.setBackground(UiTheme.ripple(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER), 0x20082d50));
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        backParams.topMargin = dp(16);
        rootLayout.addView(backBtn, backParams);

        backBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onBackToRecord();
            }
        });
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
}
