package ph.edu.umak.crm.app.staff.accounts;

import android.content.Context;
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

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * 5.9 Create Staff Account Screen
 * Allows creation of CRM Staff accounts with validation for missing details and duplicate emails.
 */
public class CreateStaffAccountView {

    public interface Callbacks {
        void onBack();
        void onAccountCreated(String name, String college, String email);
    }

    private final Context context;
    private final Callbacks callbacks;
    private final LinearLayout contentLayout;

    private LinearLayout warningBanner;
    private EditText nameInput;
    private TextView nameError;
    private FrameLayout collegeBox;
    private TextView collegeText;
    private TextView collegeError;
    private EditText emailInput;
    private TextView emailError;

    private String currentState = "5.9_default";

    public CreateStaffAccountView(Context context, Callbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setBackgroundColor(UiTheme.PAPER);
        contentLayout.setPadding(UiTheme.dp(context, 16), UiTheme.dp(context, 16),
                UiTheme.dp(context, 16), UiTheme.dp(context, 24));

        buildView();
        applyState(currentState);
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
        heading.setText("Create Staff Account");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        TextView copy = new TextView(context);
        copy.setText("Accounts created here always receive the CRM Staff role.");
        copy.setTextColor(UiTheme.PALE);
        copy.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(copy, 13);
        band.addView(copy);

        root.addView(band);
        root.addView(contentLayout);
        return root;
    }

    public void setPreviewState(String state) {
        this.currentState = state;
        applyState(state);
    }

    private void buildView() {
        contentLayout.removeAllViews();
        Typeface bold = UiTheme.boldFont(context);
        Typeface regular = UiTheme.bodyFont(context);

        // Top Warning Banner (for missing error state)
        warningBanner = new LinearLayout(context);
        warningBanner.setOrientation(LinearLayout.HORIZONTAL);
        warningBanner.setGravity(Gravity.CENTER_VERTICAL);
        warningBanner.setBackground(UiTheme.shape(context, 0xfffff7ed, 8, 0xfffed7aa));
        int wPad = UiTheme.dp(context, 12);
        warningBanner.setPadding(wPad, wPad, wPad, wPad);

        TextView warnIcon = new TextView(context);
        warnIcon.setText("⚠");
        warnIcon.setTextColor(0xffc2410c);
        warnIcon.setTypeface(bold);
        ScreenTypography.apply(warnIcon, 14);
        warnIcon.setPadding(0, 0, UiTheme.dp(context, 8), 0);
        warningBanner.addView(warnIcon);

        TextView warnText = new TextView(context);
        warnText.setText("Complete the highlighted account details.");
        warnText.setTextColor(0xffc2410c);
        warnText.setTypeface(regular);
        ScreenTypography.apply(warnText, 13);
        warningBanner.addView(warnText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        warningBanner.setVisibility(View.GONE);
        contentLayout.addView(warningBanner);

        addSpacing(14);

        // 1. Name Field
        TextView nameLabel = new TextView(context);
        nameLabel.setText("Name *");
        nameLabel.setTextColor(0xff082d50);
        nameLabel.setTypeface(bold);
        ScreenTypography.apply(nameLabel, 13);
        nameLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        contentLayout.addView(nameLabel);

        nameInput = new EditText(context);
        nameInput.setHint("Enter full name");
        nameInput.setHintTextColor(0xff94a3b8);
        nameInput.setTextColor(0xff082d50);
        nameInput.setTypeface(regular);
        ScreenTypography.apply(nameInput, 14);
        int padH = UiTheme.dp(context, 12);
        int padV = UiTheme.dp(context, 10);
        nameInput.setPadding(padH, padV, padH, padV);
        nameInput.setSingleLine(true);
        nameInput.setMinHeight(UiTheme.dp(context, 46));
        contentLayout.addView(nameInput);

        nameError = new TextView(context);
        nameError.setText("⚠ Required");
        nameError.setTextColor(0xffdc2626);
        nameError.setTypeface(regular);
        ScreenTypography.apply(nameError, 12);
        nameError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        nameError.setVisibility(View.GONE);
        contentLayout.addView(nameError);

        addSpacing(14);

        // 2. College / Unit Field
        TextView collegeLabel = new TextView(context);
        collegeLabel.setText("College / Unit *");
        collegeLabel.setTextColor(0xff082d50);
        collegeLabel.setTypeface(bold);
        ScreenTypography.apply(collegeLabel, 13);
        collegeLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        contentLayout.addView(collegeLabel);

        collegeBox = new FrameLayout(context);
        collegeBox.setPadding(padH, padV, padH, padV);
        collegeBox.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 46)));

        collegeText = new TextView(context);
        collegeText.setText("Center for Research Management");
        collegeText.setTextColor(0xff082d50);
        collegeText.setTypeface(regular);
        ScreenTypography.apply(collegeText, 14);
        FrameLayout.LayoutParams ctLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ctLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        collegeBox.addView(collegeText, ctLp);

        ImageView chevron = new ImageView(context);
        chevron.setImageResource(R.drawable.ic_crm_chevron_down);
        chevron.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams cLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 20), UiTheme.dp(context, 20));
        cLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        collegeBox.addView(chevron, cLp);

        contentLayout.addView(collegeBox);

        collegeError = new TextView(context);
        collegeError.setText("⚠ Required");
        collegeError.setTextColor(0xffdc2626);
        collegeError.setTypeface(regular);
        ScreenTypography.apply(collegeError, 12);
        collegeError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        collegeError.setVisibility(View.GONE);
        contentLayout.addView(collegeError);

        addSpacing(14);

        // 3. Institutional email Field
        TextView emailLabel = new TextView(context);
        emailLabel.setText("Institutional email *");
        emailLabel.setTextColor(0xff082d50);
        emailLabel.setTypeface(bold);
        ScreenTypography.apply(emailLabel, 13);
        emailLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        contentLayout.addView(emailLabel);

        emailInput = new EditText(context);
        emailInput.setHint("areyes@umak.edu.ph");
        emailInput.setHintTextColor(0xff94a3b8);
        emailInput.setTextColor(0xff082d50);
        emailInput.setTypeface(regular);
        ScreenTypography.apply(emailInput, 14);
        emailInput.setPadding(padH, padV, padH, padV);
        emailInput.setSingleLine(true);
        emailInput.setMinHeight(UiTheme.dp(context, 46));
        contentLayout.addView(emailInput);

        emailError = new TextView(context);
        emailError.setText("⚠ This institutional email is already registered.");
        emailError.setTextColor(0xffdc2626);
        emailError.setTypeface(regular);
        ScreenTypography.apply(emailError, 12);
        emailError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        emailError.setVisibility(View.GONE);
        contentLayout.addView(emailError);

        addSpacing(14);

        // 4. Role Field (Fixed with lock icon)
        TextView roleLabel = new TextView(context);
        roleLabel.setText("Role");
        roleLabel.setTextColor(0xff082d50);
        roleLabel.setTypeface(bold);
        ScreenTypography.apply(roleLabel, 13);
        roleLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        contentLayout.addView(roleLabel);

        FrameLayout roleBox = new FrameLayout(context);
        roleBox.setBackground(UiTheme.shape(context, 0xfff8fafc, 8, UiTheme.BORDER));
        roleBox.setPadding(padH, padV, padH, padV);
        roleBox.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 46)));

        TextView roleText = new TextView(context);
        roleText.setText("CRM Staff");
        roleText.setTextColor(0xff082d50);
        roleText.setTypeface(regular);
        ScreenTypography.apply(roleText, 14);
        FrameLayout.LayoutParams rtLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rtLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        roleBox.addView(roleText, rtLp);

        ImageView lockIcon = new ImageView(context);
        lockIcon.setImageResource(R.drawable.ic_crm_lock);
        lockIcon.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams lkLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 18), UiTheme.dp(context, 18));
        lkLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        roleBox.addView(lockIcon, lkLp);

        contentLayout.addView(roleBox);

        TextView roleHelp = new TextView(context);
        roleHelp.setText("Role is fixed for accounts created here.");
        roleHelp.setTextColor(0xff586b83);
        roleHelp.setTypeface(regular);
        ScreenTypography.apply(roleHelp, 12);
        roleHelp.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        contentLayout.addView(roleHelp);

        addSpacing(24);

        // 5. Buttons: Create Account & Cancel
        Button createBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        createBtn.setText("Create Account");
        createBtn.setAllCaps(false);
        createBtn.setTextColor(0xffffffff);
        createBtn.setTypeface(bold);
        ScreenTypography.apply(createBtn, 15);
        createBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        createBtn.setMinHeight(UiTheme.dp(context, 46));
        createBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onAccountCreated(
                        nameInput.getText().toString(),
                        collegeText.getText().toString(),
                        emailInput.getText().toString()
                );
            }
        });
        contentLayout.addView(createBtn);

        addSpacing(10);

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
                callbacks.onBack();
            }
        });
        contentLayout.addView(cancelBtn);
    }

    private void applyState(String state) {
        if ("5.9_error_missing".equals(state)) {
            warningBanner.setVisibility(View.VISIBLE);

            nameInput.setText("");
            nameInput.setBackground(createFieldBg(true));
            nameError.setVisibility(View.VISIBLE);

            collegeText.setText("Select college or unit");
            collegeText.setTextColor(0xff94a3b8);
            collegeBox.setBackground(createFieldBg(true));
            collegeError.setVisibility(View.VISIBLE);

            emailInput.setText("areyes@umak.edu.ph");
            emailInput.setBackground(createFieldBg(false));
            emailError.setVisibility(View.GONE);

        } else if ("5.9_error_duplicate".equals(state)) {
            warningBanner.setVisibility(View.GONE);

            nameInput.setText("Ana M. Reyes");
            nameInput.setBackground(createFieldBg(false));
            nameError.setVisibility(View.GONE);

            collegeText.setText("Center for Research Management");
            collegeText.setTextColor(0xff082d50);
            collegeBox.setBackground(createFieldBg(false));
            collegeError.setVisibility(View.GONE);

            emailInput.setText("areyes@umak.edu.ph");
            emailInput.setBackground(createFieldBg(true));
            emailError.setVisibility(View.VISIBLE);

        } else {
            // Default (5.9_default)
            warningBanner.setVisibility(View.GONE);

            nameInput.setText("Ana M. Reyes");
            nameInput.setBackground(createFieldBg(false));
            nameError.setVisibility(View.GONE);

            collegeText.setText("Center for Research Management");
            collegeText.setTextColor(0xff082d50);
            collegeBox.setBackground(createFieldBg(false));
            collegeError.setVisibility(View.GONE);

            emailInput.setText("areyes@umak.edu.ph");
            emailInput.setBackground(createFieldBg(false));
            emailError.setVisibility(View.GONE);
        }
    }

    private GradientDrawable createFieldBg(boolean hasError) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(UiTheme.dp(context, 8));
        if (hasError) {
            bg.setStroke(UiTheme.dp(context, 1), 0xfff87171);
        } else {
            bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        }
        return bg;
    }

    private void addSpacing(int dp) {
        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, dp)));
        contentLayout.addView(spacer);
    }
}
