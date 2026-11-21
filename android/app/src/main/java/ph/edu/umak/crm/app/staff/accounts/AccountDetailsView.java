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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
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
    private final LinearLayout contentLayout;

    private StaffData.StaffAccountItem currentAccount;
    private boolean isStudentSelected = true;

    public AccountDetailsView(Context context, Callbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setBackgroundColor(UiTheme.PAPER);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));

        // Default to Ana M. Reyes (5.10a)
        setAccount(findDefaultStaffAccount());
    }

    public View getContentLayout() {
        if (contentLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) contentLayout.getParent()).removeView(contentLayout);
        }

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        // Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(context);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = UiTheme.dp(context, 16);
        band.setPadding(pad, pad, pad, pad);

        TextView heading = new TextView(context);
        heading.setText("Account Details");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        root.addView(band);
        root.addView(contentLayout);
        return root;
    }

    public void setAccount(StaffData.StaffAccountItem account) {
        this.currentAccount = account;
        this.isStudentSelected = account == null || !"Faculty".equalsIgnoreCase(account.category);
        buildView();
    }

    public interface LiveActions {
        void permissions(org.json.JSONArray selected);void category(String selected);void state(boolean active);void recovery();void history();void back();
    }
    public void bindLive(StaffData.StaffAccountItem account,LiveActions actions){
        currentAccount=account;contentLayout.removeAllViews();
        contentLayout.addView(UiButton.text(context,"Back to Accounts",UiTheme.NAVY,actions::back));
        contentLayout.addView(createProfileSummaryCard());
        var page=new ph.edu.umak.crm.app.staff.common.StaffDesignPage(context,"Account access",null,null);
        var card=page.section("CRM Staff".equals(account.role)?"Permissions":"Researcher category");
        if("CRM Staff".equals(account.role)){
            java.util.List<Switch> toggles=new java.util.ArrayList<>();
            for(var entry:account.permissions.entrySet()){
                Switch toggle=new Switch(context);toggle.setText(entry.getKey().replace('_',' '));toggle.setTag(entry.getKey());toggle.setChecked(Boolean.TRUE.equals(entry.getValue()));toggle.setMinHeight(UiTheme.dp(context,48));card.addView(toggle);toggles.add(toggle);
            }
            card.addView(UiButton.primary(context,"Save permissions",()->{org.json.JSONArray selected=new org.json.JSONArray();for(Switch toggle:toggles)if(toggle.isChecked())selected.put(toggle.getTag().toString());actions.permissions(selected);}));
        }else{
            RadioGroup category=new RadioGroup(context);category.setOrientation(LinearLayout.VERTICAL);
            String[] selected={"Faculty".equals(account.category)?"faculty":"student"};
            for(String value:new String[]{"student","faculty"}){RadioButton choice=new RadioButton(context);choice.setId(View.generateViewId());choice.setText(value.equals("student")?"Student":"Faculty");category.addView(choice);choice.setChecked(value.equals(selected[0]));choice.setOnCheckedChangeListener((button,checked)->{if(checked)selected[0]=value;});}
            card.addView(category);card.addView(UiButton.primary(context,"Save Category Correction",()->actions.category(selected[0])));
        }
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.detach(page.content);contentLayout.addView(page.content);
        contentLayout.addView(UiButton.outline(context,"Active".equals(account.status)?"Deactivate Account":"Activate Account",UiTheme.BORDER,UiTheme.NAVY,()->actions.state(!"Active".equals(account.status))));
        contentLayout.addView(UiButton.outline(context,"Issue Temporary Credential",UiTheme.BORDER,UiTheme.NAVY,actions::recovery));
        contentLayout.addView(UiButton.text(context,"View Account Audit History",UiTheme.NAVY,actions::history));
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
            StaffDialogs.showTemporaryCredentialIssuedDialog(context, "TMP-0000-0000");
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

        // 1. Account Profile Card
        contentLayout.addView(createProfileSummaryCard());

        addSpacing(16);

        // 2. Conditional Section: Staff Permissions vs Researcher Category Correction
        boolean isStaff = "CRM Staff".equalsIgnoreCase(currentAccount.role);
        if (isStaff) {
            contentLayout.addView(createPermissionsSection());
        } else {
            // Notice: Researcher accounts cannot receive CRM Staff permissions.
            contentLayout.addView(createResearcherNoticeBanner());
            addSpacing(16);
            contentLayout.addView(createResearcherCategorySection());
        }

        addSpacing(20);

        // 3. Action Buttons: Issue Temporary Credential
        Button tempCredBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        tempCredBtn.setText("🗝  Issue Temporary Credential");
        tempCredBtn.setAllCaps(false);
        tempCredBtn.setTextColor(0xff082d50);
        tempCredBtn.setTypeface(bold);
        ScreenTypography.apply(tempCredBtn, 14);
        tempCredBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        tempCredBtn.setMinHeight(UiTheme.dp(context, 44));
        tempCredBtn.setOnClickListener(v -> {
            StaffDialogs.showTemporaryCredentialIssuedDialog(context, "TMP-0000-0000");
        });
        contentLayout.addView(tempCredBtn);

        addSpacing(10);

        // 4. Action Button: Deactivate / Activate Account
        boolean isActive = "Active".equalsIgnoreCase(currentAccount.status);
        String deactTitle = isActive ? "Deactivate Account" : "Activate Account";
        Button deactBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        deactBtn.setText(deactTitle);
        deactBtn.setAllCaps(false);
        deactBtn.setTextColor(isActive ? 0xffdc2626 : 0xff16a34a);
        deactBtn.setTypeface(bold);
        ScreenTypography.apply(deactBtn, 14);
        deactBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, isActive ? 0xfffca5a5 : 0xff86efac), 0x20dc2626));
        deactBtn.setMinHeight(UiTheme.dp(context, 44));
        deactBtn.setOnClickListener(v -> {
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

        addSpacing(14);

        // 5. Action Button: View account audit history (Link)
        TextView auditBtn = new TextView(context);
        auditBtn.setText("View account audit history");
        auditBtn.setTextColor(0xff185d9d);
        auditBtn.setTypeface(bold);
        auditBtn.setGravity(Gravity.CENTER);
        ScreenTypography.apply(auditBtn, 13);
        auditBtn.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 4));
        auditBtn.setOnClickListener(v -> {
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

        // Header Row: Avatar, Name, Badges row
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = new TextView(context);
        avatar.setText(currentAccount.initials);
        avatar.setTextColor(0xff185d9d);
        avatar.setTypeface(bold);
        ScreenTypography.apply(avatar, 15);
        avatar.setGravity(Gravity.CENTER);
        int avatarSize = UiTheme.dp(context, 46);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        aLp.setMarginEnd(UiTheme.dp(context, 12));
        avatar.setLayoutParams(aLp);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#E0F2FE"));
        avatar.setBackground(avatarBg);
        headerRow.addView(avatar);

        LinearLayout col = new LinearLayout(context);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView name = new TextView(context);
        name.setText(currentAccount.name);
        name.setTextColor(0xff082d50);
        name.setTypeface(bold);
        ScreenTypography.apply(name, 17);
        col.addView(name);

        // Badges row
        LinearLayout badgesRow = new LinearLayout(context);
        badgesRow.setOrientation(LinearLayout.HORIZONTAL);
        badgesRow.setGravity(Gravity.CENTER_VERTICAL);
        badgesRow.setPadding(0, UiTheme.dp(context, 4), 0, 0);

        TextView roleBadge = new TextView(context);
        roleBadge.setText(currentAccount.role);
        roleBadge.setTypeface(bold);
        ScreenTypography.apply(roleBadge, 11);
        int px = UiTheme.dp(context, 7);
        int py = UiTheme.dp(context, 2);
        roleBadge.setPadding(px, py, px, py);
        roleBadge.setBackground(UiTheme.shape(context, 0xffe0f2fe, 4, 0));
        roleBadge.setTextColor(0xff0284c7);
        badgesRow.addView(roleBadge);

        TextView statusBadge = new TextView(context);
        statusBadge.setText(currentAccount.status);
        statusBadge.setTypeface(bold);
        ScreenTypography.apply(statusBadge, 11);
        statusBadge.setPadding(px, py, px, py);
        LinearLayout.LayoutParams statLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statLp.leftMargin = UiTheme.dp(context, 6);
        statusBadge.setLayoutParams(statLp);

        GradientDrawable sBg = new GradientDrawable();
        sBg.setCornerRadius(UiTheme.dp(context, 4));
        if ("Active".equalsIgnoreCase(currentAccount.status)) {
            sBg.setColor(Color.parseColor("#DCFCE7"));
            statusBadge.setTextColor(Color.parseColor("#16A34A"));
        } else {
            sBg.setColor(Color.parseColor("#F1F5F9"));
            statusBadge.setTextColor(Color.parseColor("#64748B"));
        }
        statusBadge.setBackground(sBg);
        badgesRow.addView(statusBadge);

        col.addView(badgesRow);
        headerRow.addView(col, colLp);
        card.addView(headerRow);

        addSpacingInCard(card, 16);

        // 1. Institutional email
        card.addView(createFieldContainer("Institutional email", currentAccount.email));
        addSpacingInCard(card, 10);

        // 2. Account identifier
        card.addView(createFieldContainer("Account identifier", currentAccount.identifier));
        addSpacingInCard(card, 10);

        // 3. Role
        card.addView(createFieldContainer("Role", currentAccount.role));
        addSpacingInCard(card, 8);

        // Note: 🔒 Existing passwords are never displayed.
        TextView pwNote = new TextView(context);
        pwNote.setText("🔒 Existing passwords are never displayed.");
        pwNote.setTextColor(0xff64748b);
        pwNote.setTypeface(regular);
        ScreenTypography.apply(pwNote, 12);
        card.addView(pwNote);

        return card;
    }

    private View createFieldContainer(String label, String value) {
        Typeface regular = UiTheme.bodyFont(context);

        LinearLayout group = new LinearLayout(context);
        group.setOrientation(LinearLayout.VERTICAL);

        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(0xff586b83);
        lbl.setTypeface(regular);
        ScreenTypography.apply(lbl, 12);
        lbl.setPadding(0, 0, 0, UiTheme.dp(context, 4));
        group.addView(lbl);

        FrameLayout box = new FrameLayout(context);
        box.setBackground(UiTheme.shape(context, 0xfff8fafc, 8, UiTheme.BORDER));
        int padH = UiTheme.dp(context, 12);
        int padV = UiTheme.dp(context, 10);
        box.setPadding(padH, padV, padH, padV);
        box.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 44)));

        TextView val = new TextView(context);
        val.setText(value != null ? value : "—");
        val.setTextColor(0xff082d50);
        val.setTypeface(regular);
        ScreenTypography.apply(val, 14);
        FrameLayout.LayoutParams vLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        box.addView(val, vLp);

        ImageView lockIcon = new ImageView(context);
        lockIcon.setImageResource(R.drawable.ic_crm_lock);
        lockIcon.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams lkLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 18), UiTheme.dp(context, 18));
        lkLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        box.addView(lockIcon, lkLp);

        group.addView(box);
        return group;
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
        sectionTitle.setTextColor(0xff082d50);
        sectionTitle.setTypeface(bold);
        ScreenTypography.apply(sectionTitle, 16);
        sectionTitle.setPadding(0, 0, 0, UiTheme.dp(context, 12));
        card.addView(sectionTitle);

        Map<String, Boolean> perms = currentAccount.permissions;
        for (Map.Entry<String, Boolean> entry : perms.entrySet()) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 8));

            TextView permName = new TextView(context);
            permName.setText(entry.getKey());
            permName.setTextColor(0xff082d50);
            permName.setTypeface(regular);
            ScreenTypography.apply(permName, 13);
            LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(permName, pLp);

            boolean isOn = Boolean.TRUE.equals(entry.getValue());

            TextView toggleLabel = new TextView(context);
            toggleLabel.setText(isOn ? "On" : "Off");
            toggleLabel.setTextColor(isOn ? 0xff082d50 : 0xff64748b);
            toggleLabel.setTypeface(bold);
            ScreenTypography.apply(toggleLabel, 12);
            toggleLabel.setPadding(0, 0, UiTheme.dp(context, 8), 0);
            row.addView(toggleLabel);

            // Toggle switch pill visual
            FrameLayout switchPill = new FrameLayout(context);
            int pillWidth = UiTheme.dp(context, 36);
            int pillHeight = UiTheme.dp(context, 20);
            switchPill.setLayoutParams(new LinearLayout.LayoutParams(pillWidth, pillHeight));

            View pillBg = new View(context);
            pillBg.setBackground(UiTheme.shape(context, isOn ? 0xff082d50 : 0xffcbd5e1, pillHeight / 2, 0));
            switchPill.addView(pillBg, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            View thumb = new View(context);
            int thumbSize = UiTheme.dp(context, 16);
            thumb.setBackground(UiTheme.shape(context, 0xffffffff, thumbSize / 2, 0));
            FrameLayout.LayoutParams tLp = new FrameLayout.LayoutParams(thumbSize, thumbSize);
            tLp.gravity = (isOn ? Gravity.END : Gravity.START) | Gravity.CENTER_VERTICAL;
            int margin = UiTheme.dp(context, 2);
            tLp.setMargins(margin, margin, margin, margin);
            switchPill.addView(thumb, tLp);

            row.addView(switchPill);

            card.addView(row);
        }

        return card;
    }

    private View createResearcherNoticeBanner() {
        LinearLayout banner = new LinearLayout(context);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setBackground(UiTheme.shape(context, 0xffeff6ff, 8, 0xffbfdbfe));
        int pad = UiTheme.dp(context, 12);
        banner.setPadding(pad, pad, pad, pad);

        ImageView infoIcon = new ImageView(context);
        infoIcon.setImageResource(R.drawable.ic_crm_info);
        int iSize = UiTheme.dp(context, 18);
        LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(iSize, iSize);
        iLp.rightMargin = UiTheme.dp(context, 8);
        banner.addView(infoIcon, iLp);

        TextView text = new TextView(context);
        text.setText("Researcher accounts cannot receive CRM Staff permissions.");
        text.setTextColor(0xff1e40af);
        text.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(text, 12);
        banner.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        return banner;
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
        sectionTitle.setTextColor(0xff082d50);
        sectionTitle.setTypeface(bold);
        ScreenTypography.apply(sectionTitle, 16);
        card.addView(sectionTitle);

        addSpacingInCard(card, 10);

        TextView catLabel = new TextView(context);
        catLabel.setText("Researcher category");
        catLabel.setTextColor(0xff586b83);
        catLabel.setTypeface(regular);
        ScreenTypography.apply(catLabel, 12);
        card.addView(catLabel);

        addSpacingInCard(card, 6);

        // Segmented pill toggle: Student vs Faculty
        LinearLayout toggleContainer = new LinearLayout(context);
        toggleContainer.setOrientation(LinearLayout.HORIZONTAL);
        toggleContainer.setBackground(UiTheme.shape(context, 0xfff1f5f9, 6, 0));
        int tPad = UiTheme.dp(context, 2);
        toggleContainer.setPadding(tPad, tPad, tPad, tPad);

        Button studentBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        Button facultyBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);

        updateCategoryButtons(studentBtn, facultyBtn);

        studentBtn.setOnClickListener(v -> {
            isStudentSelected = true;
            updateCategoryButtons(studentBtn, facultyBtn);
        });

        facultyBtn.setOnClickListener(v -> {
            isStudentSelected = false;
            updateCategoryButtons(studentBtn, facultyBtn);
        });

        toggleContainer.addView(studentBtn, new LinearLayout.LayoutParams(0, UiTheme.dp(context, 36), 1f));
        toggleContainer.addView(facultyBtn, new LinearLayout.LayoutParams(0, UiTheme.dp(context, 36), 1f));
        card.addView(toggleContainer);

        addSpacingInCard(card, 14);

        // Reason for correction *
        TextView reasonLabel = new TextView(context);
        reasonLabel.setText("Reason for correction *");
        reasonLabel.setTextColor(0xff082d50);
        reasonLabel.setTypeface(bold);
        ScreenTypography.apply(reasonLabel, 13);
        card.addView(reasonLabel);

        addSpacingInCard(card, 6);

        EditText reasonInput = new EditText(context);
        reasonInput.setHint("Explain why the category is being corrected");
        reasonInput.setHintTextColor(0xff94a3b8);
        reasonInput.setTextColor(0xff082d50);
        reasonInput.setTypeface(regular);
        ScreenTypography.apply(reasonInput, 14);
        int padH = UiTheme.dp(context, 12);
        int padV = UiTheme.dp(context, 10);
        reasonInput.setPadding(padH, padV, padH, padV);
        reasonInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        reasonInput.setMinLines(3);
        reasonInput.setGravity(Gravity.TOP | Gravity.START);
        card.addView(reasonInput);

        addSpacingInCard(card, 16);

        Button saveCorrectionBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        saveCorrectionBtn.setText("Save Category Correction");
        saveCorrectionBtn.setAllCaps(false);
        saveCorrectionBtn.setTextColor(0xff082d50);
        saveCorrectionBtn.setTypeface(bold);
        ScreenTypography.apply(saveCorrectionBtn, 14);
        saveCorrectionBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xff082d50), 0x20082d50));
        saveCorrectionBtn.setMinHeight(UiTheme.dp(context, 44));
        saveCorrectionBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onViewAuditHistory(currentAccount.identifier, currentAccount.name);
            }
        });
        card.addView(saveCorrectionBtn);

        return card;
    }

    private void updateCategoryButtons(Button studentBtn, Button facultyBtn) {
        studentBtn.setAllCaps(false);
        studentBtn.setText(isStudentSelected ? "✓ Student" : "Student");
        studentBtn.setTypeface(isStudentSelected ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        studentBtn.setTextColor(isStudentSelected ? 0xff082d50 : 0xff586b83);
        ScreenTypography.apply(studentBtn, 13);
        studentBtn.setBackground(isStudentSelected ? UiTheme.shape(context, UiTheme.WHITE, 6, 0xff082d50) : null);

        facultyBtn.setAllCaps(false);
        facultyBtn.setText(!isStudentSelected ? "✓ Faculty" : "Faculty");
        facultyBtn.setTypeface(!isStudentSelected ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        facultyBtn.setTextColor(!isStudentSelected ? 0xff082d50 : 0xff586b83);
        ScreenTypography.apply(facultyBtn, 13);
        facultyBtn.setBackground(!isStudentSelected ? UiTheme.shape(context, UiTheme.WHITE, 6, 0xff082d50) : null);
    }

    private void addSpacingInCard(LinearLayout parent, int dp) {
        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, dp)));
        parent.addView(spacer);
    }

    private void addSpacing(int dp) {
        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, dp)));
        contentLayout.addView(spacer);
    }
}
