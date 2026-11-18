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
import android.widget.Toast;

import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Research Documents & Version Management View (Wireframe 7.6).
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class RegistryDocumentsView {

    public interface Callbacks {
        void onBackToRecord();
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

    public ViewGroup getContentLayout() {
        if (rootLayout != null && rootLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) rootLayout.getParent()).removeView(rootLayout);
        }
        return rootLayout;
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

        // 1. Header
        TextView topHeader = new TextView(activity);
        topHeader.setText("Center for Research Management");
        topHeader.setTextColor(UiTheme.NAVY);
        topHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        topHeader.setTypeface(Typeface.DEFAULT_BOLD);
        rootLayout.addView(topHeader);

        TextView title = new TextView(activity);
        title.setText("Documents");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(4);
        titleParams.bottomMargin = dp(4);
        rootLayout.addView(title, titleParams);

        TextView contextText = new TextView(activity);
        contextText.setText(record.id + " · " + record.title);
        contextText.setTextColor(UiTheme.NAVY);
        contextText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        contextText.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams ctxParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ctxParams.bottomMargin = dp(12);
        rootLayout.addView(contextText, ctxParams);

        // 2. Privacy Banner
        LinearLayout privacyCard = new LinearLayout(activity);
        privacyCard.setOrientation(LinearLayout.HORIZONTAL);
        privacyCard.setGravity(Gravity.CENTER_VERTICAL);
        privacyCard.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable privBg = new GradientDrawable();
        privBg.setColor(UiTheme.BADGE_BLUE_BG);
        privBg.setCornerRadius(dp(8));
        privBg.setStroke(dp(1), UiTheme.BORDER);
        privacyCard.setBackground(privBg);
        LinearLayout.LayoutParams privParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        privParams.bottomMargin = dp(16);
        privacyCard.setLayoutParams(privParams);

        TextView privText = new TextView(activity);
        privText.setText("Visible to linked researchers and authorized staff only.");
        privText.setTextColor(UiTheme.NAVY);
        privText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        privText.setTypeface(Typeface.DEFAULT_BOLD);
        privacyCard.addView(privText);
        rootLayout.addView(privacyCard);

        // 3. Section: Attach a new version
        LinearLayout attachCard = createBaseCard();

        TextView attachSectionTitle = new TextView(activity);
        attachSectionTitle.setText("Attach a new version");
        attachSectionTitle.setTextColor(UiTheme.NAVY);
        attachSectionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        attachSectionTitle.setTypeface(Typeface.DEFAULT_BOLD);
        attachCard.addView(attachSectionTitle);

        Button attachBtn = new Button(activity);
        attachBtn.setText("Attach file");
        attachBtn.setTextColor(UiTheme.WHITE);
        attachBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        attachBtn.setTypeface(Typeface.DEFAULT_BOLD);
        attachBtn.setAllCaps(false);
        GradientDrawable attachBtnBg = new GradientDrawable();
        attachBtnBg.setColor(UiTheme.NAVY);
        attachBtnBg.setCornerRadius(dp(8));
        attachBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), attachBtnBg, null));
        LinearLayout.LayoutParams attachBtnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(42));
        attachBtnParams.topMargin = dp(10);
        attachBtnParams.bottomMargin = dp(8);
        attachCard.addView(attachBtn, attachBtnParams);

        attachBtn.setOnClickListener(v ->
                Toast.makeText(activity, "File picker opened. Select PDF or DOCX file.", Toast.LENGTH_SHORT).show()
        );

        TextView formatNote = new TextView(activity);
        formatNote.setText("PDF or DOCX, up to 10 MB each");
        formatNote.setTextColor(UiTheme.MUTED);
        formatNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        attachCard.addView(formatNote);

        TextView instructionNote = new TextView(activity);
        instructionNote.setText("Uploading keeps the research ID. The new file becomes the current version.");
        instructionNote.setTextColor(UiTheme.INK);
        instructionNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams instParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        instParams.topMargin = dp(6);
        attachCard.addView(instructionNote, instParams);

        rootLayout.addView(attachCard);

        // 4. Section: Version list
        LinearLayout versionCard = createBaseCard();

        TextView versionSectionTitle = new TextView(activity);
        versionSectionTitle.setText("Version list");
        versionSectionTitle.setTextColor(UiTheme.NAVY);
        versionSectionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        versionSectionTitle.setTypeface(Typeface.DEFAULT_BOLD);
        versionSectionTitle.setPadding(0, 0, 0, dp(12));
        versionCard.addView(versionSectionTitle);

        for (int i = 0; i < record.documents.size(); i++) {
            StaffRegistryData.DocumentVersion doc = record.documents.get(i);
            LinearLayout docRow = new LinearLayout(activity);
            docRow.setOrientation(LinearLayout.HORIZONTAL);
            docRow.setGravity(Gravity.CENTER_VERTICAL);
            docRow.setPadding(0, dp(8), 0, dp(8));

            LinearLayout infoCol = new LinearLayout(activity);
            infoCol.setOrientation(LinearLayout.VERTICAL);

            TextView nameText = new TextView(activity);
            nameText.setText(doc.fileName);
            nameText.setTextColor(UiTheme.NAVY);
            nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            nameText.setTypeface(Typeface.DEFAULT_BOLD);
            infoCol.addView(nameText);

            TextView metaText = new TextView(activity);
            metaText.setText(doc.fileSize + " · " + doc.date + " · " + doc.uploader);
            metaText.setTextColor(UiTheme.MUTED);
            metaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            infoCol.addView(metaText);

            docRow.addView(infoCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView badge = new TextView(activity);
            badge.setText(doc.isCurrent ? "Current" : "Earlier version");
            badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            badge.setTypeface(Typeface.DEFAULT_BOLD);
            badge.setPadding(dp(8), dp(3), dp(8), dp(3));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setCornerRadius(dp(10));
            if (doc.isCurrent) {
                badgeBg.setColor(UiTheme.STATUS_COMPLETED_BG);
                badge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
            } else {
                badgeBg.setColor(UiTheme.TAG_BG);
                badge.setTextColor(UiTheme.TAG_TEXT);
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
        Button backBtn = new Button(activity);
        backBtn.setText("Back to Record");
        backBtn.setTextColor(UiTheme.NAVY);
        backBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        backBtn.setTypeface(Typeface.DEFAULT_BOLD);
        backBtn.setAllCaps(false);
        GradientDrawable backBg = new GradientDrawable();
        backBg.setColor(UiTheme.WHITE);
        backBg.setCornerRadius(dp(8));
        backBg.setStroke(dp(1), UiTheme.BORDER);
        backBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22131c4b), backBg, null));
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
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
