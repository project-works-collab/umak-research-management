package ph.edu.umak.crm.app.auth;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;
import ph.edu.umak.crm.app.BackControl;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.R;

public final class EmailVerifyView {
    private final Context context;
    private final View root;
    private final AuthCallbacks callbacks;
    private final String emailAddress;

    private final LinearLayout form;
    private final LinearLayout badge;
    private final ImageView badgeIcon;
    private final TextView headline;
    private final TextView subtitle;
    private final LinearLayout codeSection;
    private final EditText[] digitBoxes = new EditText[6];
    private final TextView feedbackText;
    private final Button verifyBtn;
    private final TextView timerText;
    private final Button resendBtn;
    private final Button successContinueBtn;
    private CountDownTimer countDownTimer;

    public EmailVerifyView(Context context, String email, AuthCallbacks callbacks) {
        this.context = context;
        this.emailAddress = (email != null && !email.isEmpty()) ? email : "jdelacruz.k1234@umak.edu.ph";
        this.callbacks = callbacks;

        Typeface heading = UiTheme.headingFont(context);
        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = UiTheme.dp(context, 24);
        form.setPadding(pad, pad, pad, UiTheme.dp(context, 32));
        form.setFocusableInTouchMode(true);

        badge = new LinearLayout(context);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(UiTheme.shape(context, UiTheme.BADGE_BLUE_BG, 32, 0));
        int badgeSize = UiTheme.dp(context, 64);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(badgeSize, badgeSize);
        badgeParams.topMargin = UiTheme.dp(context, 16);
        badgeParams.bottomMargin = UiTheme.dp(context, 20);

        badgeIcon = new ImageView(context);
        Drawable mailDrawable = context.getDrawable(R.drawable.ic_crm_email);
        if (mailDrawable != null) {
            mailDrawable = mailDrawable.mutate();
            mailDrawable.setColorFilter(UiTheme.BADGE_BLUE_ICON, PorterDuff.Mode.SRC_IN);
            badgeIcon.setImageDrawable(mailDrawable);
        }
        int iconSize = UiTheme.dp(context, 32);
        badge.addView(badgeIcon, new LinearLayout.LayoutParams(iconSize, iconSize));
        form.addView(badge, badgeParams);

        headline = new TextView(context);
        headline.setText("Verify your institutional email");
        headline.setTextColor(UiTheme.INK);
        headline.setTypeface(heading);
        ScreenTypography.apply(headline, 24);
        ScreenTypography.heading(headline);
        headline.setGravity(Gravity.CENTER);
        form.addView(headline);

        subtitle = new TextView(context);
        subtitle.setText("We sent instructions to\n" + emailAddress);
        subtitle.setTextColor(UiTheme.MUTED);
        subtitle.setTypeface(body);
        ScreenTypography.apply(subtitle, 14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 24));
        form.addView(subtitle);

        codeSection = new LinearLayout(context);
        codeSection.setOrientation(LinearLayout.VERTICAL);
        codeSection.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView codeLabel = new TextView(context);
        codeLabel.setText("Verification code");
        codeLabel.setTextColor(UiTheme.MUTED);
        codeLabel.setTypeface(bold);
        ScreenTypography.apply(codeLabel, 13);
        codeLabel.setGravity(Gravity.CENTER);
        codeLabel.setPadding(0, 0, 0, UiTheme.dp(context, 8));
        codeSection.addView(codeLabel);

        LinearLayout boxesRow = new LinearLayout(context);
        boxesRow.setOrientation(LinearLayout.HORIZONTAL);
        boxesRow.setGravity(Gravity.CENTER);

        int boxWidth = UiTheme.dp(context, 44);
        int boxHeight = UiTheme.dp(context, 48);
        int boxMargin = UiTheme.dp(context, 4);

        for (int i = 0; i < 6; i++) {
            final int index = i;
            EditText box = new EditText(context);
            box.setGravity(Gravity.CENTER);
            box.setTextSize(20);
            box.setTextColor(UiTheme.INK);
            box.setTypeface(bold);
            box.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
            box.setInputType(InputType.TYPE_CLASS_NUMBER);
            box.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(boxWidth, boxHeight);
            lp.setMargins(boxMargin, 0, boxMargin, 0);
            boxesRow.addView(box, lp);
            digitBoxes[i] = box;

            box.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) {
                        digitBoxes[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            box.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (box.getText().length() == 0 && index > 0) {
                        digitBoxes[index - 1].requestFocus();
                        digitBoxes[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }
        codeSection.addView(boxesRow);

        feedbackText = new TextView(context);
        feedbackText.setText("Enter the 6-digit code from the email.");
        feedbackText.setTextColor(UiTheme.MUTED);
        feedbackText.setTypeface(body);
        ScreenTypography.apply(feedbackText, 12);
        feedbackText.setGravity(Gravity.CENTER);
        feedbackText.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 16));
        codeSection.addView(feedbackText);

        verifyBtn = UiButton.primary(context, "Verify", this::attemptVerify);
        codeSection.addView(verifyBtn, new LinearLayout.LayoutParams(-1, -2));

        timerText = new TextView(context);
        timerText.setText("Resend available in 00:45");
        timerText.setTextColor(UiTheme.MUTED);
        timerText.setTypeface(body);
        ScreenTypography.apply(timerText, 13);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, UiTheme.dp(context, 12), 0, UiTheme.dp(context, 4));
        codeSection.addView(timerText);

        resendBtn = UiButton.text(context, "Resend email", UiTheme.NAVY, () -> {
            if (callbacks != null) callbacks.onResendVerificationCode(emailAddress);
            startCountdown(45);
        });
        resendBtn.setVisibility(View.GONE);
        codeSection.addView(resendBtn, new LinearLayout.LayoutParams(-1, -2));

        Button backBtn = UiButton.text(context, "Go back", UiTheme.INK, () -> {
            if (callbacks != null) callbacks.onBackFromVerification();
        });
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(-1, -2);
        backParams.topMargin = UiTheme.dp(context, 4);
        codeSection.addView(backBtn, backParams);

        form.addView(codeSection, new LinearLayout.LayoutParams(-1, -2));

        // Success state button (Continue to Sign In)
        successContinueBtn = UiButton.primary(context, "Continue to Sign In", () -> {
            if (callbacks != null) callbacks.onEmailVerified(emailAddress);
        });
        successContinueBtn.setVisibility(View.GONE);
        LinearLayout.LayoutParams continueParams = new LinearLayout.LayoutParams(-1, -2);
        continueParams.topMargin = UiTheme.dp(context, 16);
        form.addView(successContinueBtn, continueParams);

        ScrollView scroll = new ScrollView(context);
        scroll.setBackgroundColor(UiTheme.PAPER);
        scroll.setFillViewport(true);
        scroll.addView(form);

        LinearLayout screen = new LinearLayout(context);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(UiTheme.PAPER);
        screen.addView(BackControl.create(context, "Back", UiTheme.NAVY, () -> {
            if (callbacks != null) callbacks.onBackFromVerification();
        }));
        screen.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        root = screen;
        startCountdown(45);
    }

    private void startCountdown(int seconds) {
        if (countDownTimer != null) countDownTimer.cancel();
        timerText.setVisibility(View.VISIBLE);
        resendBtn.setVisibility(View.GONE);

        countDownTimer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                timerText.setText(String.format(Locale.ROOT, "Resend available in 00:%02d", sec));
            }

            @Override
            public void onFinish() {
                timerText.setVisibility(View.GONE);
                resendBtn.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void attemptVerify() {
        StringBuilder sb = new StringBuilder();
        for (EditText box : digitBoxes) {
            sb.append(box.getText().toString().trim());
        }
        String code = sb.toString();
        if (code.length() < 6) {
            showError("Enter the complete 6-digit verification code.");
            return;
        }

        if (callbacks != null) {
            callbacks.onVerifyEmail(emailAddress, code);
        }
    }

    public void showError(String message) {
        for (EditText box : digitBoxes) {
            box.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.ERROR_RED));
        }
        feedbackText.setText(message);
        feedbackText.setTextColor(UiTheme.ERROR_RED);
        timerText.setVisibility(View.GONE);
        resendBtn.setVisibility(View.VISIBLE);
    }

    public void showSuccess() {
        if (countDownTimer != null) countDownTimer.cancel();
        codeSection.setVisibility(View.GONE);
        successContinueBtn.setVisibility(View.VISIBLE);

        badge.setBackground(UiTheme.shape(context, UiTheme.SUCCESS_BG, 32, 0));
        Drawable check = context.getDrawable(R.drawable.ic_crm_success);
        if (check != null) {
            check = check.mutate();
            check.setColorFilter(UiTheme.SUCCESS_GREEN, PorterDuff.Mode.SRC_IN);
            badgeIcon.setImageDrawable(check);
        }

        headline.setText("Email verified");
        subtitle.setText("Your institutional email " + emailAddress + " has been verified. You may now sign in to the UMak Research Management and Lifecycle Information System.");
        subtitle.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 16));
    }

    public void configurePreviewState(String state) {
        if ("1.5".equals(state)) {
            // Default pending
            codeSection.setVisibility(View.VISIBLE);
            successContinueBtn.setVisibility(View.GONE);
            badge.setBackground(UiTheme.shape(context, UiTheme.BADGE_BLUE_BG, 32, 0));
            Drawable mail = context.getDrawable(R.drawable.ic_crm_email);
            if (mail != null) {
                mail = mail.mutate();
                mail.setColorFilter(UiTheme.BADGE_BLUE_ICON, PorterDuff.Mode.SRC_IN);
                badgeIcon.setImageDrawable(mail);
            }
            headline.setText("Verify your institutional email");
            subtitle.setText("We sent instructions to\n" + emailAddress);
            for (EditText box : digitBoxes) {
                box.setText("");
                box.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
            }
            feedbackText.setText("Enter the 6-digit code from the email.");
            feedbackText.setTextColor(UiTheme.MUTED);
            startCountdown(45);
        } else if ("1.5-error".equals(state)) {
            codeSection.setVisibility(View.VISIBLE);
            successContinueBtn.setVisibility(View.GONE);
            badge.setBackground(UiTheme.shape(context, UiTheme.BADGE_BLUE_BG, 32, 0));
            Drawable mail = context.getDrawable(R.drawable.ic_crm_email);
            if (mail != null) {
                mail = mail.mutate();
                mail.setColorFilter(UiTheme.BADGE_BLUE_ICON, PorterDuff.Mode.SRC_IN);
                badgeIcon.setImageDrawable(mail);
            }
            headline.setText("Verify your institutional email");
            subtitle.setText("We sent instructions to\n" + emailAddress);
            char[] digits = "482917".toCharArray();
            for (int i = 0; i < 6; i++) {
                digitBoxes[i].setText(String.valueOf(digits[i]));
            }
            showError("This verification link or code is invalid, expired or already used.");
        } else if ("1.5-success".equals(state)) {
            showSuccess();
        }
    }

    public View getView() {
        return root;
    }
}
