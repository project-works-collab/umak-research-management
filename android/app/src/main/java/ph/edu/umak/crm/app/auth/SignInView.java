package ph.edu.umak.crm.app.auth;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
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

public final class SignInView {
    private final Context context;
    private final View root;
    private final LinearLayout form;
    private final LinearLayout bannerContainer;
    private final UiInput.FormField usernameField;
    private final UiInput.FormField passwordField;
    private final Button forgotBtn;
    private final Button signInBtn;
    private final Button createAccountBtn;
    private final Button contactBtn;
    private final Button guestBtn;
    private final TextView pendingSubtext;
    private final AuthCallbacks callbacks;

    public SignInView(Context context, String initialEmail, AuthCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
        Typeface heading = UiTheme.headingFont(context);
        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 24);
        form.setPadding(pad, pad, pad, UiTheme.dp(context, 32));
        form.setFocusableInTouchMode(true);

        TextView headline = new TextView(context);
        headline.setText("Sign In");
        headline.setTextColor(UiTheme.INK);
        headline.setTypeface(heading);
        ScreenTypography.apply(headline, 32);
        ScreenTypography.heading(headline);
        form.addView(headline);

        TextView description = new TextView(context);
        description.setText(ph.edu.umak.crm.app.BuildConfig.HOSTED_DEMO
                ? "Render demo · umak-crm-api.onrender.com\nUse your demo account email and password."
                : "Enter your given institutional email and password to access all CRM services.");
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

        usernameField = UiInput.field(context, form, "Username or Institutional Email", "e.g. jdelacruz.K1234@umak.edu.ph", false);
        usernameField.getInput().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        if (initialEmail != null && !initialEmail.isEmpty()) {
            usernameField.getInput().setText(initialEmail);
        }

        passwordField = UiInput.field(context, form, "Password", "Enter your password", true);

        forgotBtn = UiButton.text(context, "Forgot password?", UiTheme.MUTED, () -> {
            if (callbacks != null) callbacks.onShowForgotPassword();
        });
        forgotBtn.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams forgotParams = new LinearLayout.LayoutParams(-1, -2);
        forgotParams.topMargin = UiTheme.dp(context, 2);
        forgotParams.bottomMargin = UiTheme.dp(context, 8);
        form.addView(forgotBtn, forgotParams);

        signInBtn = UiButton.primary(context, "Sign In", this::attemptSignIn);
        LinearLayout.LayoutParams signInParams = new LinearLayout.LayoutParams(-1, -2);
        signInParams.topMargin = UiTheme.dp(context, 4);
        form.addView(signInBtn, signInParams);

        pendingSubtext = new TextView(context);
        pendingSubtext.setText("Checking your account details...");
        pendingSubtext.setTextColor(UiTheme.MUTED);
        pendingSubtext.setTypeface(body);
        ScreenTypography.apply(pendingSubtext, 12);
        pendingSubtext.setGravity(Gravity.CENTER);
        pendingSubtext.setPadding(0, UiTheme.dp(context, 8), 0, 0);
        pendingSubtext.setVisibility(View.GONE);
        form.addView(pendingSubtext);

        createAccountBtn = UiButton.outline(context, "+ Create Researcher Account", UiTheme.NAVY, UiTheme.NAVY, () -> {
            if (callbacks != null) callbacks.onShowCreateAccount();
        });
        LinearLayout.LayoutParams createParams = new LinearLayout.LayoutParams(-1, -2);
        createParams.topMargin = UiTheme.dp(context, 12);
        form.addView(createAccountBtn, createParams);

        contactBtn = UiButton.text(context, "Contact CRM for assistance", UiTheme.MUTED, () -> {
            if (callbacks != null) callbacks.onShowAccessHelp();
        });
        LinearLayout.LayoutParams contactParams = new LinearLayout.LayoutParams(-1, -2);
        contactParams.topMargin = UiTheme.dp(context, 8);
        form.addView(contactBtn, contactParams);

        guestBtn = UiButton.text(context, "Continue as Guest", UiTheme.INK, () -> {
            if (callbacks != null) callbacks.onContinueAsGuest();
        });
        LinearLayout.LayoutParams guestParams = new LinearLayout.LayoutParams(-1, -2);
        guestParams.topMargin = UiTheme.dp(context, 4);
        form.addView(guestBtn, guestParams);

        ScrollView scroll = new ScrollView(context);
        scroll.setBackgroundColor(UiTheme.PAPER);
        scroll.setFillViewport(true);
        scroll.addView(form);

        LinearLayout screen = new LinearLayout(context);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(UiTheme.PAPER);
        screen.addView(BackControl.create(context, "Back", UiTheme.NAVY, () -> {
            if (callbacks != null) callbacks.onBackFromSignIn();
        }));
        screen.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        root = screen;
    }

    private void attemptSignIn() {
        clearErrors();
        String username = usernameField.getInput().getText().toString().trim();
        String password = passwordField.getInput().getText().toString();

        boolean hasError = false;
        if (username.isEmpty()) {
            usernameField.setError("Please enter your username or institutional email.");
            hasError = true;
        }
        if (password.isEmpty()) {
            passwordField.setError("Please enter your password.");
            hasError = true;
        }
        if (hasError) return;

        if (callbacks != null) {
            callbacks.onSignIn(username, passwordField.getInput());
        }
    }

    public void clearErrors() {
        bannerContainer.removeAllViews();
        bannerContainer.setVisibility(View.GONE);
        usernameField.clearError();
        passwordField.clearError();
    }

    public void showBanner(String message, String actionText, Runnable actionClick) {
        bannerContainer.removeAllViews();
        bannerContainer.addView(UiBanner.create(context, message, actionText, actionClick));
        bannerContainer.setVisibility(View.VISIBLE);
    }

    public void setLoading(boolean loading) {
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        forgotBtn.setEnabled(!loading);
        createAccountBtn.setEnabled(!loading);
        contactBtn.setEnabled(!loading);
        guestBtn.setEnabled(!loading);

        if (loading) {
            signInBtn.setText("Signing in...");
            signInBtn.setBackground(UiTheme.shape(context, UiTheme.DISABLED_BTN_BG, 8, 0));
            signInBtn.setTextColor(UiTheme.DISABLED_BTN_TEXT);
            signInBtn.setEnabled(false);
            pendingSubtext.setVisibility(View.VISIBLE);
        } else {
            signInBtn.setText("Sign In");
            signInBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.NAVY, 8, 0), 0x20395072));
            signInBtn.setTextColor(UiTheme.WHITE);
            signInBtn.setEnabled(true);
            pendingSubtext.setVisibility(View.GONE);
        }
    }

    public void configurePreviewState(String state) {
        clearErrors();
        setLoading(false);
        if ("1.1".equals(state)) {
            // Default blank state
            usernameField.getInput().setText("");
            passwordField.getInput().setText("");
        } else if ("1.2a".equals(state)) {
            // Blank username error
            usernameField.getInput().setText("");
            passwordField.getInput().setText("secret123");
            usernameField.setError("Please enter your username or institutional email.");
        } else if ("1.2b".equals(state)) {
            // Blank password error
            usernameField.getInput().setText("jdelacruz.K1234@umak.edu.ph");
            passwordField.getInput().setText("");
            passwordField.setError("Please enter your password.");
        } else if ("1.2c".equals(state)) {
            // Wrong credentials error
            usernameField.getInput().setText("jdelacruz.K1234@umak.edu.ph");
            passwordField.getInput().setText("wrongpassword");
            showBanner("Incorrect username or password", null, null);
        } else if ("1.2d".equals(state)) {
            // Inactive account error
            usernameField.getInput().setText("jdelacruz.K1234@umak.edu.ph");
            passwordField.getInput().setText("validpassword123");
            showBanner("Account unavailable.", "Contact CRM", () -> {
                if (callbacks != null) callbacks.onShowAccessHelp();
            });
        } else if ("1.3".equals(state)) {
            // Pending sign in state
            usernameField.getInput().setText("jdelacruz.K1234@umak.edu.ph");
            passwordField.getInput().setText("validpassword123");
            setLoading(true);
        }
    }

    public View getView() {
        return root;
    }

    public void setFeedback(String message) {
        if (message == null || message.isEmpty()) {
            clearErrors();
            setLoading(false);
            return;
        }
        if (message.contains("Signing in")) {
            setLoading(true);
        } else {
            setLoading(false);
            showBanner(message, null, null);
        }
    }

    public EditText getEmailInput() {
        return usernameField.getInput();
    }

    public EditText getPasswordInput() {
        return passwordField.getInput();
    }
}
