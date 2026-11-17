package ph.edu.umak.crm.app.staff.accounts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Map;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.staff.StaffData;
import ph.edu.umak.crm.app.staff.StaffDialogs;

/**
 * 5.10a / 5.10b Account Details Screen
 * Displays staff permissions or researcher category correction, credentials, activation/deactivation, and audit link.
 */
public class AccountDetailsView {

    public interface Callbacks {
        void onBack();
        void onViewAuditHistory(String identifier, String name);
        void onAccountUpdated();
    }

    private final Context context;
    private final Callbacks callbacks;
    private final ScrollView rootScrollView;
    private final LinearLayout contentLayout;

    private StaffData.StaffAccountItem currentAccount;

    public AccountDetailsView(Context context, Callbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;

        rootScrollView = new ScrollView(context);
        rootScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        rootScrollView.setBackgroundColor(UiTheme.PAPER);

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));
        rootScrollView.addView(contentLayout);

        // Default to Ana M. Reyes (5.10a)
        setAccount(findDefaultStaffAccount());
    }

    public View getContentLayout() {
        if (contentLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) contentLayout.getParent()).removeView(contentLayout);
        }
        return contentLayout;
    }

    public void setAccount(StaffData.StaffAccountItem account) {
        this.currentAccount = account;
        buildView();
    }

    public void setPreviewState(String state) {
        if ("5.10a".equals(state)) {
            setAccount(findStaffAccount("STF-0042")); // Ana M. Reyes
        } else if ("5.10b".equals(state)) {
            setAccount(findStaffAccount("A1234")); // LeBron R. James
        } else if ("5.11a".equals(state)) {
            setAccount(findStaffAccount("STF-0042"));
            StaffDialogs.showDeactivateAccountDialog(context, currentAccount.name, currentAccount.role, () -> {
                if (callbacks != null) callbacks.onViewAuditHistory(currentAccount.identifier, currentAccount.name);
            });
        } else if ("5.11b".equals(state)) {
            setAccount(findStaffAccount("K2210")); // Jonard L. Wala
            StaffDialogs.showActivateAccountDialog(context, currentAccount.name, currentAccount.role, () -> {
                if (callbacks != null) callbacks.onViewAuditHistory(currentAccount.identifier, currentAccount.name);
            });
        } else if ("5.11c".equals(state)) {
            setAccount(findStaffAccount("STF-0007")); // Lebron James (you)
            StaffDialogs.showCantDeactivateOwnAccountDialog(context);
        } else if ("5.11d".equals(state)) {
            setAccount(findStaffAccount("STF-0042"));
            StaffDialogs.showCantDeactivateLastAdminDialog(context);
        } else if ("5.12".equals(state)) {
            setAccount(findStaffAccount("A1234"));
            StaffDialogs.showTemporaryCredentialIssuedDialog(context, "TMP-7Q4K-92LX");
        }
    }

    private StaffData.StaffAccountItem findDefaultStaffAccount() {
        return findStaffAccount("STF-0042");
    }

    private StaffData.StaffAccountItem findStaffAccount(String identifier) {
        for (StaffData.StaffAccountItem item : StaffData.getUserAccounts()) {
            if (identifier.equalsIgnoreCase(item.identifier)) {
                return item;
            }
        }
        return StaffData.getUserAccounts().get(1); // fallback Ana M. Reyes
    }

    private void buildView() {
        contentLayout.removeAllViews();
        if (currentAccount == null) return;

        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        // Back button row
        if (callbacks != null) {
            TextView backBtn = new TextView(context);
            backBtn.setText("< Back to Accounts");
            backBtn.setTextColor(UiTheme.ACCENT);
            backBtn.setTypeface(bold);
            ScreenTypography.apply(backBtn, 14);
            backBtn.setPadding(0, 0, 0, UiTheme.dp(context, 12));
            backBtn.setOnClickListener(v -> callbacks.onBack());
            contentLayout.addView(backBtn);
        }

        // Header Title
        TextView deptText = new TextView(context);
        deptText.setText("Center for Research Management");
        deptText.setTextColor(UiTheme.MUTED);
        deptText.setTypeface(regular);
        ScreenTypography.apply(deptText, 12);
        contentLayout.addView(deptText);

        TextView title = new TextView(context);
        title.setText("Account Details");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 22);
        title.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 16));
        contentLayout.addView(title);

        // Account Profile Card
        contentLayout.addView(createProfileSummaryCard());

        addSpacing(16);

        // Conditional Section: Staff Permissions vs Researcher Category Correction
        boolean isStaff = "CRM Staff".equalsIgnoreCase(currentAccount.role);
        if (isStaff) {
            contentLayout.addView(createPermissionsSection());
        } else {
            contentLayout.addView(createResearcherCategorySection());
        }

        addSpacing(20);

        // Action Buttons
        Button tempCredBtn = UiButton.outline(context, "Issue Temporary Credential", UiTheme.BORDER, UiTheme.NAVY, () -> {
            StaffDialogs.showTemporaryCredentialIssuedDialog(context, "TMP-7Q4K-92LX");
        });
        contentLayout.addView(tempCredBtn);

        addSpacing(10);

        boolean isActive = "Active".equalsIgnoreCase(currentAccount.status);
        String deactTitle = isActive ? "Deactivate Account" : "Activate Account";
        Button deactBtn = UiButton.outline(context, deactTitle, UiTheme.BORDER,
                isActive ? UiTheme.ERROR_RED : UiTheme.SUCCESS_GREEN, () -> {
                    if ("STF-0007".equalsIgnoreCase(currentAccount.identifier)) {
                        // Own account error (5.11c)
                        StaffDialogs.showCantDeactivateOwnAccountDialog(context);
                    } else if (isActive) {
                        // Deactivate confirmation (5.11a)
                        StaffDialogs.showDeactivateAccountDialog(context, currentAccount.name, currentAccount.role, () -> {
                            if (callbacks != null) {
                                callbacks.onViewAuditHistory(currentAccount.identifier, currentAccount.name);
                            }
                        });
                    } else {
                        // Activate confirmation (5.11b)
                        StaffDialogs.showActivateAccountDialog(context, currentAccount.name, currentAccount.role, () -> {
                            if (callbacks != null) {
                                callbacks.onViewAuditHistory(currentAccount.identifier, currentAccount.name);
                            }
                        });
                    }
                });
        contentLayout.addView(deactBtn);

        addSpacing(10);

        Button auditBtn = UiButton.outline(context, "View account audit history", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (callbacks != null) {
                callbacks.onViewAuditHistory(currentAccount.identifier, currentAccount.name);
            }
        });
        contentLayout.addView(auditBtn);
    }

    private View createProfileSummaryCard() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(UiTheme.dp(context, 8));
        bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        card.setBackground(bg);

        // Header Row: Avatar, Name, Status Badge
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = new TextView(context);
        avatar.setText(currentAccount.initials);
        avatar.setTextColor(UiTheme.NAVY);
        avatar.setTypeface(bold);
        ScreenTypography.apply(avatar, 14);
        avatar.setGravity(Gravity.CENTER);
        int avatarSize = UiTheme.dp(context, 42);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        aLp.setMarginEnd(UiTheme.dp(context, 12));
        avatar.setLayoutParams(aLp);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#E5EDF6"));
        avatar.setBackground(avatarBg);
        headerRow.addView(avatar);

        LinearLayout col = new LinearLayout(context);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView name = new TextView(context);
        name.setText(currentAccount.name);
        name.setTextColor(UiTheme.NAVY);
        name.setTypeface(bold);
        ScreenTypography.apply(name, 16);
        col.addView(name);

        String sub = currentAccount.role;
        if (currentAccount.category != null && !currentAccount.category.isEmpty()) {
            sub += " · " + currentAccount.category;
        }
        TextView subView = new TextView(context);
        subView.setText(sub);
        subView.setTextColor(UiTheme.MUTED);
        subView.setTypeface(regular);
        ScreenTypography.apply(subView, 12);
        col.addView(subView);
        headerRow.addView(col, colLp);

        // Status Badge
        TextView statusBadge = new TextView(context);
        statusBadge.setText(currentAccount.status);
        statusBadge.setTypeface(bold);
        ScreenTypography.apply(statusBadge, 11);
        statusBadge.setPadding(UiTheme.dp(context, 8), UiTheme.dp(context, 3),
                UiTheme.dp(context, 8), UiTheme.dp(context, 3));

        GradientDrawable sBg = new GradientDrawable();
        sBg.setCornerRadius(UiTheme.dp(context, 4));
        if ("Active".equalsIgnoreCase(currentAccount.status)) {
            sBg.setColor(Color.parseColor("#E6F4EA"));
            statusBadge.setTextColor(Color.parseColor("#137333"));
        } else {
            sBg.setColor(Color.parseColor("#F1F3F4"));
            statusBadge.setTextColor(Color.parseColor("#5F6368"));
        }
        statusBadge.setBackground(sBg);
        headerRow.addView(statusBadge);

        card.addView(headerRow);

        addDivider(card);

        // Institutional email
        card.addView(createFieldRow("Institutional email", currentAccount.email));
        addDivider(card);

        // Account identifier
        card.addView(createFieldRow("Account identifier", currentAccount.identifier));
        addDivider(card);

        // Role
        card.addView(createFieldRow("Role", currentAccount.role));

        // Note: Existing passwords are never displayed.
        TextView pwNote = new TextView(context);
        pwNote.setText("Existing passwords are never displayed.");
        pwNote.setTextColor(UiTheme.MUTED);
        pwNote.setTypeface(regular);
        ScreenTypography.apply(pwNote, 12);
        pwNote.setPadding(0, UiTheme.dp(context, 8), 0, 0);
        card.addView(pwNote);

        return card;
    }

    private View createPermissionsSection() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(UiTheme.dp(context, 8));
        bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        card.setBackground(bg);

        TextView sectionTitle = new TextView(context);
        sectionTitle.setText("Permissions");
        sectionTitle.setTextColor(UiTheme.NAVY);
        sectionTitle.setTypeface(bold);
        ScreenTypography.apply(sectionTitle, 15);
        sectionTitle.setPadding(0, 0, 0, UiTheme.dp(context, 12));
        card.addView(sectionTitle);

        Map<String, Boolean> perms = currentAccount.permissions;
        for (Map.Entry<String, Boolean> entry : perms.entrySet()) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

            TextView permName = new TextView(context);
            permName.setText(entry.getKey());
            permName.setTextColor(UiTheme.NAVY);
            permName.setTypeface(regular);
            ScreenTypography.apply(permName, 13);
            LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(permName, pLp);

            TextView toggleLabel = new TextView(context);
            boolean isOn = Boolean.TRUE.equals(entry.getValue());
            toggleLabel.setText(isOn ? "On" : "Off");
            toggleLabel.setTextColor(isOn ? UiTheme.NAVY : UiTheme.MUTED);
            toggleLabel.setTypeface(bold);
            ScreenTypography.apply(toggleLabel, 13);
            row.addView(toggleLabel);

            card.addView(row);
        }

        return card;
    }

    private View createResearcherCategorySection() {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(UiTheme.dp(context, 8));
        bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        card.setBackground(bg);

        TextView sectionTitle = new TextView(context);
        sectionTitle.setText("Correct researcher category");
        sectionTitle.setTextColor(UiTheme.NAVY);
        sectionTitle.setTypeface(bold);
        ScreenTypography.apply(sectionTitle, 15);
        card.addView(sectionTitle);

        TextView catLabel = new TextView(context);
        catLabel.setText("Researcher category");
        catLabel.setTextColor(UiTheme.MUTED);
        catLabel.setTypeface(regular);
        ScreenTypography.apply(catLabel, 12);
        catLabel.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 6));
        card.addView(catLabel);

        // Radio group
        RadioGroup radioGroup = new RadioGroup(context);
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);

        RadioButton studentRadio = new RadioButton(context);
        studentRadio.setText("Student");
        studentRadio.setTextColor(UiTheme.NAVY);
        studentRadio.setChecked(!"Faculty".equalsIgnoreCase(currentAccount.category));
        radioGroup.addView(studentRadio);

        RadioButton facultyRadio = new RadioButton(context);
        facultyRadio.setText("Faculty");
        facultyRadio.setTextColor(UiTheme.NAVY);
        facultyRadio.setChecked("Faculty".equalsIgnoreCase(currentAccount.category));
        radioGroup.addView(facultyRadio);

        card.addView(radioGroup);

        // Reason for correction
        TextView reasonLabel = new TextView(context);
        reasonLabel.setText("Reason for correction *");
        reasonLabel.setTextColor(UiTheme.NAVY);
        reasonLabel.setTypeface(bold);
        ScreenTypography.apply(reasonLabel, 13);
        reasonLabel.setPadding(0, UiTheme.dp(context, 12), 0, UiTheme.dp(context, 6));
        card.addView(reasonLabel);

        EditText reasonInput = new EditText(context);
        reasonInput.setHint("Explain why the category is being corrected");
        reasonInput.setHintTextColor(UiTheme.MUTED);
        reasonInput.setTextColor(UiTheme.NAVY);
        reasonInput.setTypeface(regular);
        ScreenTypography.apply(reasonInput, 14);
        reasonInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 12),
                UiTheme.dp(context, 12), UiTheme.dp(context, 12));
        GradientDrawable rBg = new GradientDrawable();
        rBg.setColor(UiTheme.WHITE);
        rBg.setCornerRadius(UiTheme.dp(context, 8));
        rBg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        reasonInput.setBackground(rBg);
        card.addView(reasonInput);

        // Note: Researcher accounts cannot receive CRM Staff permissions.
        TextView permNote = new TextView(context);
        permNote.setText("Researcher accounts cannot receive CRM Staff permissions.");
        permNote.setTextColor(UiTheme.MUTED);
        permNote.setTypeface(regular);
        ScreenTypography.apply(permNote, 12);
        permNote.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 14));
        card.addView(permNote);

        Button saveCorrectionBtn = UiButton.primary(context, "Save Category Correction", () -> {
            if (callbacks != null) {
                callbacks.onViewAuditHistory(currentAccount.identifier, currentAccount.name);
            }
        });
        card.addView(saveCorrectionBtn);

        return card;
    }

    private View createFieldRow(String label, String value) {
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 8));

        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(UiTheme.MUTED);
        lbl.setTypeface(regular);
        ScreenTypography.apply(lbl, 12);
        row.addView(lbl);

        TextView val = new TextView(context);
        val.setText(value != null ? value : "—");
        val.setTextColor(UiTheme.NAVY);
        val.setTypeface(bold);
        ScreenTypography.apply(val, 14);
        val.setPadding(0, UiTheme.dp(context, 2), 0, 0);
        row.addView(val);

        return row;
    }

    private void addDivider(LinearLayout parent) {
        View divider = new View(context);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 1)));
        divider.setBackgroundColor(UiTheme.BORDER);
        parent.addView(divider);
    }

    private void addSpacing(int dp) {
        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, dp)));
        contentLayout.addView(spacer);
    }
}
