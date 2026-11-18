package ph.edu.umak.crm.app;

import android.view.View;
import android.widget.EditText;
import ph.edu.umak.crm.app.auth.ActivationView;
import ph.edu.umak.crm.app.auth.AuthCallbacks;
import ph.edu.umak.crm.app.auth.CreateAccountView;
import ph.edu.umak.crm.app.auth.EmailVerifyView;
import ph.edu.umak.crm.app.auth.PasswordUpdatedView;
import ph.edu.umak.crm.app.auth.SignInView;
import ph.edu.umak.crm.app.auth.WelcomeView;
import ph.edu.umak.crm.app.common.dialogs.UiDialog;

final class SharedAccess implements AuthCallbacks {
    private final MainActivity activity;
    private SignInView currentSignIn;
    private ActivationView currentActivation;
    private CreateAccountView currentCreateAccount;
    private EmailVerifyView currentEmailVerify;

    SharedAccess(MainActivity activity) {
        this.activity = activity;
    }

    View welcome() {
        return WelcomeView.create(activity, this);
    }

    View signIn(String emailAddress) {
        currentSignIn = new SignInView(activity, emailAddress, this);
        return currentSignIn.getView();
    }

    View activate() {
        currentActivation = new ActivationView(activity, this);
        return currentActivation.getView();
    }

    View createAccount() {
        currentCreateAccount = new CreateAccountView(activity, this);
        return currentCreateAccount.getView();
    }

    View emailVerify(String email) {
        currentEmailVerify = new EmailVerifyView(activity, email, this);
        return currentEmailVerify.getView();
    }

    View passwordUpdated() {
        return new PasswordUpdatedView(activity, this).getView();
    }

    SignInView getCurrentSignIn() {
        return currentSignIn;
    }

    ActivationView getCurrentActivation() {
        return currentActivation;
    }

    CreateAccountView getCurrentCreateAccount() {
        return currentCreateAccount;
    }

    EmailVerifyView getCurrentEmailVerify() {
        return currentEmailVerify;
    }

    void feedback(String message) {
        if (currentSignIn != null) currentSignIn.setFeedback(message);
        if (currentActivation != null) currentActivation.setFeedback(message);
    }

    void information(String title, String message) {
        UiDialog.alert(activity, title, message);
    }

    void dialog(String title, String message, String action, Runnable callback, String cancel) {
        UiDialog.show(activity, title, message, action, callback, cancel, null);
    }

    @Override
    public void onStartAccess() {
        activity.startAccess();
    }

    @Override
    public void onSignIn(String email, EditText passwordInput) {
        activity.signIn(email, passwordInput);
    }

    @Override
    public void onShowAccessHelp() {
        activity.showAccessHelp();
    }

    @Override
    public void onContinueAsGuest() {
        activity.showGuest();
    }

    @Override
    public void onBackFromSignIn() {
        activity.backFromSignIn();
    }

    @Override
    public void onActivate(EditText codeInput, EditText passwordInput, EditText confirmInput) {
        activity.activate(codeInput, passwordInput, confirmInput);
    }

    @Override
    public void onShowSignIn() {
        activity.showSignIn();
    }

    @Override
    public void onShowForgotPassword() {
        information("Forgot password?",
                "Contact the Center for Research Management to recover access to your account.");
    }

    @Override
    public void onShowCreateAccount() {
        activity.showCreateAccount();
    }

    @Override
    public void onBackFromCreateAccount() {
        activity.showSignIn();
    }

    @Override
    public void onAccountCreated(String email, String fullName, String college, String category, String password, String confirmation) {
        activity.onAccountCreated(email, fullName, college, category, password, confirmation);
    }

    @Override
    public void onShowEmailVerification(String email) {
        activity.showEmailVerification(email);
    }

    @Override
    public void onBackFromVerification() {
        activity.showSignIn();
    }

    @Override
    public void onVerifyEmail(String email, String code) {
        activity.verifyEmail(email, code);
    }

    @Override
    public void onResendVerificationCode(String email) {
        activity.resendVerificationCode(email);
    }

    @Override
    public void onEmailVerified(String email) {
        activity.onEmailVerified(email);
    }
}
