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
import android.widget.ScrollView;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
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
    private final ScrollView rootScrollView;
    private final LinearLayout contentLayout;

    private EditText nameInput;
    private TextView nameError;
    private EditText collegeInput;
    private TextView collegeError;
    private EditText emailInput;
    private TextView emailError;
    private LinearLayout bottomErrorBanner;

    private String currentState = "5.9_default";

    public CreateStaffAccountView(Context context, Callbacks callbacks) {
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

        buildView();
        applyState(currentState);
    }

    public View getContentLayout() {
        if (contentLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) contentLayout.getParent()).removeView(contentLayout);
        }
        return contentLayout;
    }

    public void setPreviewState(String state) {
        this.currentState = state;
        applyState(state);
    }

    private void buildView() {
        contentLayout.removeAllViews();
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
        title.setText("Create Staff Account");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 22);
        title.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 2));
        contentLayout.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Accounts created here always receive the CRM Staff role.");
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(regular);
        ScreenTypography.apply(subtitle, 13);
        subtitle.setPadding(0, 0, 0, UiTheme.dp(context, 16));
        contentLayout.addView(subtitle);

        // Name Field
        TextView nameLabel = new TextView(context);
        nameLabel.setText("Name *");
        nameLabel.setTextColor(UiTheme.NAVY);
        nameLabel.setTypeface(bold);
        ScreenTypography.apply(nameLabel, 13);
        nameLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        contentLayout.addView(nameLabel);

        nameInput = new EditText(context);
        nameInput.setHint("Enter full name");
        nameInput.setHintTextColor(UiTheme.MUTED);
        nameInput.setTextColor(UiTheme.NAVY);
        nameInput.setTypeface(regular);
        ScreenTypography.apply(nameInput, 14);
        nameInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 12),
                UiTheme.dp(context, 12), UiTheme.dp(context, 12));
        contentLayout.addView(nameInput);

        nameError = new TextView(context);
        nameError.setText("Required");
        nameError.setTextColor(UiTheme.ERROR_RED);
        nameError.setTypeface(regular);
        ScreenTypography.apply(nameError, 12);
        nameError.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 8));
        nameError.setVisibility(View.GONE);
        contentLayout.addView(nameError);

        View sp1 = new View(context);
        sp1.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, 12)));
        contentLayout.addView(sp1);

        // College / Unit Field
        TextView collegeLabel = new TextView(context);
        collegeLabel.setText("College / Unit *");
        collegeLabel.setTextColor(UiTheme.NAVY);
        collegeLabel.setTypeface(bold);
        ScreenTypography.apply(collegeLabel, 13);
        collegeLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        contentLayout.addView(collegeLabel);

        collegeInput = new EditText(context);
        collegeInput.setHint("Select college or unit");
        collegeInput.setHintTextColor(UiTheme.MUTED);
        collegeInput.setTextColor(UiTheme.NAVY);
        collegeInput.setTypeface(regular);
        ScreenTypography.apply(collegeInput, 14);
        collegeInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 12),
                UiTheme.dp(context, 12), UiTheme.dp(context, 12));
        contentLayout.addView(collegeInput);

        collegeError = new TextView(context);
        collegeError.setText("Required");
        collegeError.setTextColor(UiTheme.ERROR_RED);
        collegeError.setTypeface(regular);
        ScreenTypography.apply(collegeError, 12);
        collegeError.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 8));
        collegeError.setVisibility(View.GONE);
        contentLayout.addView(collegeError);

        View sp2 = new View(context);
        sp2.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, 12)));
        contentLayout.addView(sp2);

        // Institutional email Field
        TextView emailLabel = new TextView(context);
        emailLabel.setText("Institutional email *");
        emailLabel.setTextColor(UiTheme.NAVY);
        emailLabel.setTypeface(bold);
        ScreenTypography.apply(emailLabel, 13);
        emailLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        contentLayout.addView(emailLabel);

        emailInput = new EditText(context);
        emailInput.setHint("areyes@umak.edu.ph");
        emailInput.setHintTextColor(UiTheme.MUTED);
        emailInput.setTextColor(UiTheme.NAVY);
        emailInput.setTypeface(regular);
        ScreenTypography.apply(emailInput, 14);
        emailInput.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 12),
                UiTheme.dp(context, 12), UiTheme.dp(context, 12));
        contentLayout.addView(emailInput);

        emailError = new TextView(context);
        emailError.setText("This institutional email is already registered.");
        emailError.setTextColor(UiTheme.ERROR_RED);
        emailError.setTypeface(regular);
        ScreenTypography.apply(emailError, 12);
        emailError.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 8));
        emailError.setVisibility(View.GONE);
        contentLayout.addView(emailError);

        View sp3 = new View(context);
        sp3.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, 12)));
        contentLayout.addView(sp3);

        // Role Field (Fixed)
        TextView roleLabel = new TextView(context);
        roleLabel.setText("Role");
        roleLabel.setTextColor(UiTheme.NAVY);
        roleLabel.setTypeface(bold);
        ScreenTypography.apply(roleLabel, 13);
        roleLabel.setPadding(0, 0, 0, UiTheme.dp(context, 6));
        contentLayout.addView(roleLabel);

        TextView roleBox = new TextView(context);
        roleBox.setText("CRM Staff");
        roleBox.setTextColor(UiTheme.NAVY);
        roleBox.setTypeface(bold);
        ScreenTypography.apply(roleBox, 14);
        roleBox.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 12),
                UiTheme.dp(context, 12), UiTheme.dp(context, 12));

        GradientDrawable roleBg = new GradientDrawable();
        roleBg.setColor(Color.parseColor("#EEF2F6"));
        roleBg.setCornerRadius(UiTheme.dp(context, 8));
        roleBg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        roleBox.setBackground(roleBg);
        contentLayout.addView(roleBox);

        TextView roleHelp = new TextView(context);
        roleHelp.setText("Role is fixed for accounts created here.");
        roleHelp.setTextColor(UiTheme.MUTED);
        roleHelp.setTypeface(regular);
        ScreenTypography.apply(roleHelp, 12);
        roleHelp.setPadding(0, UiTheme.dp(context, 4), 0, UiTheme.dp(context, 16));
        contentLayout.addView(roleHelp);

        // Bottom Error Banner (Hidden by default)
        bottomErrorBanner = new LinearLayout(context);
        bottomErrorBanner.setOrientation(LinearLayout.HORIZONTAL);
        bottomErrorBanner.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 12),
                UiTheme.dp(context, 12), UiTheme.dp(context, 12));

        GradientDrawable errBannerBg = new GradientDrawable();
        errBannerBg.setColor(UiTheme.PEACH_BG);
        errBannerBg.setCornerRadius(UiTheme.dp(context, 8));
        errBannerBg.setStroke(UiTheme.dp(context, 1), UiTheme.PEACH_BORDER);
        bottomErrorBanner.setBackground(errBannerBg);

        TextView errBannerText = new TextView(context);
        errBannerText.setText("Complete the highlighted account details.");
        errBannerText.setTextColor(UiTheme.PEACH_TEXT);
        errBannerText.setTypeface(bold);
        ScreenTypography.apply(errBannerText, 13);
        bottomErrorBanner.addView(errBannerText);
        bottomErrorBanner.setVisibility(View.GONE);
        contentLayout.addView(bottomErrorBanner);

        View sp4 = new View(context);
        sp4.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, 20)));
        contentLayout.addView(sp4);

        // Buttons
        Button createBtn = UiButton.primary(context, "Create Account", () -> {
            if ("5.9_error_missing".equals(currentState) || "5.9_error_duplicate".equals(currentState)) {
                return;
            }
            if (callbacks != null) {
                callbacks.onAccountCreated(
                        nameInput.getText().toString(),
                        collegeInput.getText().toString(),
                        emailInput.getText().toString()
                );
            }
        });
        contentLayout.addView(createBtn);

        View sp5 = new View(context);
        sp5.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, 10)));
        contentLayout.addView(sp5);

        Button cancelBtn = UiButton.outline(context, "Cancel", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (callbacks != null) {
                callbacks.onBack();
            }
        });
        contentLayout.addView(cancelBtn);
    }

    private void applyState(String state) {
        if ("5.9_error_missing".equals(state)) {
            nameInput.setText("");
            nameInput.setBackground(createFieldBg(true));
            nameError.setVisibility(View.VISIBLE);

            collegeInput.setText("");
            collegeInput.setBackground(createFieldBg(true));
            collegeError.setVisibility(View.VISIBLE);

            emailInput.setText("areyes@umak.edu.ph");
            emailInput.setBackground(createFieldBg(false));
            emailError.setVisibility(View.GONE);

            bottomErrorBanner.setVisibility(View.VISIBLE);
        } else if ("5.9_error_duplicate".equals(state)) {
            nameInput.setText("Ana M. Reyes");
            nameInput.setBackground(createFieldBg(false));
            nameError.setVisibility(View.GONE);

            collegeInput.setText("Center for Research Management");
            collegeInput.setBackground(createFieldBg(false));
            collegeError.setVisibility(View.GONE);

            emailInput.setText("areyes@umak.edu.ph");
            emailInput.setBackground(createFieldBg(true));
            emailError.setVisibility(View.VISIBLE);

            bottomErrorBanner.setVisibility(View.GONE);
        } else {
            // Default (5.9_default)
            nameInput.setText("Ana M. Reyes");
            nameInput.setBackground(createFieldBg(false));
            nameError.setVisibility(View.GONE);

            collegeInput.setText("Center for Research Management");
            collegeInput.setBackground(createFieldBg(false));
            collegeError.setVisibility(View.GONE);

            emailInput.setText("areyes@umak.edu.ph");
            emailInput.setBackground(createFieldBg(false));
            emailError.setVisibility(View.GONE);

            bottomErrorBanner.setVisibility(View.GONE);
        }
    }

    private GradientDrawable createFieldBg(boolean hasError) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(UiTheme.WHITE);
        bg.setCornerRadius(UiTheme.dp(context, 8));
        if (hasError) {
            bg.setStroke(UiTheme.dp(context, 1), UiTheme.ERROR_RED);
        } else {
            bg.setStroke(UiTheme.dp(context, 1), UiTheme.BORDER);
        }
        return bg;
    }
}
