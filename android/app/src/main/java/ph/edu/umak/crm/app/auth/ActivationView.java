package ph.edu.umak.crm.app.auth;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import ph.edu.umak.crm.app.BackControl;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiBanner;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.components.UiInput;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class ActivationView {
    private final Context context;
    private final View root;
    private final LinearLayout bannerContainer;
    private final UiInput.FormField tempPasswordField;
    private final UiInput.FormField newPasswordField;
    private final UiInput.FormField confirmPasswordField;
    private final AuthCallbacks callbacks;

    public ActivationView(Context context, AuthCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;

        Typeface heading = UiTheme.headingFont(context);
        Typeface body = UiTheme.bodyFont(context);

        LinearLayout form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 24);
        form.setPadding(pad, pad, pad, UiTheme.dp(context, 32));
        form.setFocusableInTouchMode(true);

        TextView headline = new TextView(context);
        headline.setText("Change Temporary Password");
        headline.setTextColor(UiTheme.INK);
        headline.setTypeface(heading);
        ScreenTypography.apply(headline, 28);
        ScreenTypography.heading(headline);
        form.addView(headline);

        TextView description = new TextView(context);
        description.setText("For security purposes, please set a new password before continuing.");
        description.setTextColor(UiTheme.MUTED);
        description.setTypeface(body);
        ScreenTypography.apply(description, 14);
        description.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 16));
        form.addView(description);

        bannerContainer = new LinearLayout(context);
        bannerContainer.setOrientation(LinearLayout.VERTICAL);
        bannerContainer.setVisibility(View.GONE);
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(-1, -2);
        bannerParams.bottomMargin = UiTheme.dp(context, 16);
        form.addView(bannerContainer, bannerParams);

        tempPasswordField = UiInput.field(context, form, "Temporary Password", "Enter temporary password", true);
        newPasswordField = UiInput.field(context, form, "New Password", "Enter your new password", true);
        newPasswordField.setHelper("At least 12 characters");
        confirmPasswordField = UiInput.field(context, form, "Confirm Password", "Re-enter your new password", true);

        Button updateBtn = UiButton.primary(context, "Update Password", this::attemptUpdate);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = UiTheme.dp(context, 16);
        form.addView(updateBtn, params);

        Button backBtn = UiButton.text(context, "Go back", UiTheme.INK, () -> {
            if (callbacks != null) callbacks.onShowSignIn();
        });
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(-1, -2);
        backParams.topMargin = UiTheme.dp(context, 8);
        form.addView(backBtn, backParams);

        ScrollView scroll = new ScrollView(context);
        scroll.setBackgroundColor(UiTheme.PAPER);
        scroll.setFillViewport(true);
        scroll.addView(form);

        LinearLayout screen = new LinearLayout(context);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(UiTheme.PAPER);
        screen.addView(BackControl.create(context, "Back to sign-in", UiTheme.NAVY, () -> {
            if (callbacks != null) callbacks.onShowSignIn();
        }));
        screen.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        root = screen;
    }

    private void attemptUpdate() {
        clearErrors();
        String temp = tempPasswordField.getInput().getText().toString();
        String newPass = newPasswordField.getInput().getText().toString();
        String confirm = confirmPasswordField.getInput().getText().toString();

        if (temp.isEmpty()) {
            tempPasswordField.setError("Enter your temporary password or activation code.");
            return;
        }

        if (newPass.length() < 12) {
            newPasswordField.setError("Password must be at least 12 characters.");
            return;
        }

        if (newPass.equals(temp)) {
            newPasswordField.setError("Choose a password different from your temporary password.");
            return;
        }

        if (!newPass.equals(confirm)) {
            confirmPasswordField.setError("Passwords do not match.");
            return;
        }

        if (callbacks != null) {
            callbacks.onActivate(tempPasswordField.getInput(), newPasswordField.getInput(), confirmPasswordField.getInput());
        }
    }

    public void clearErrors() {
        bannerContainer.removeAllViews();
        bannerContainer.setVisibility(View.GONE);
        tempPasswordField.clearError();
        newPasswordField.clearError();
        newPasswordField.setHelper("At least 12 characters");
        confirmPasswordField.clearError();
    }

    public void setFeedback(String message) {
        if (message == null || message.isEmpty()) {
            clearErrors();
            return;
        }
        bannerContainer.removeAllViews();
        bannerContainer.addView(UiBanner.create(context, message, null, null));
        bannerContainer.setVisibility(View.VISIBLE);
    }

    public void configurePreviewState(String state) {
        clearErrors();
        if ("1.8a".equals(state)) {
            tempPasswordField.getInput().setText("tempSecret1234");
            newPasswordField.getInput().setText("short123");
            confirmPasswordField.getInput().setText("short123");
            newPasswordField.setError("Password must be at least 12 characters.");
        } else if ("1.8b".equals(state)) {
            tempPasswordField.getInput().setText("tempSecret1234");
            newPasswordField.getInput().setText("SecurePass123456!");
            confirmPasswordField.getInput().setText("MismatchedPass!");
            confirmPasswordField.setError("Passwords do not match.");
        } else if ("1.8c".equals(state)) {
            tempPasswordField.getInput().setText("tempSecret1234");
            newPasswordField.getInput().setText("tempSecret1234");
            confirmPasswordField.getInput().setText("tempSecret1234");
            newPasswordField.setError("Choose a password different from your temporary password.");
        } else {
            tempPasswordField.getInput().setText("");
            newPasswordField.getInput().setText("");
            confirmPasswordField.getInput().setText("");
        }
    }

    public View getView() {
        return root;
    }

    public EditText getCodeInput() {
        return tempPasswordField.getInput();
    }

    public EditText getPasswordInput() {
        return newPasswordField.getInput();
    }

    public EditText getConfirmInput() {
        return confirmPasswordField.getInput();
    }
}
