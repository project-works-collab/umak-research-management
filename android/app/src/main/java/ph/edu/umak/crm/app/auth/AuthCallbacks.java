package ph.edu.umak.crm.app.auth;

import android.widget.EditText;

public interface AuthCallbacks {
    void onStartAccess();
    void onSignIn(String email, EditText passwordInput);
    void onShowAccessHelp();
    void onContinueAsGuest();
    void onBackFromSignIn();
    void onActivate(EditText codeInput, EditText passwordInput, EditText confirmInput);
    void onShowSignIn();
    void onShowForgotPassword();

    void onShowCreateAccount();
    void onBackFromCreateAccount();
    void onAccountCreated(String email, String fullName, String college, String category, String password, String confirmation);
    void onShowEmailVerification(String email);
    void onBackFromVerification();
    void onVerifyEmail(String email, String code);
    void onResendVerificationCode(String email);
    void onEmailVerified(String email);
}
