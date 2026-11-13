package ph.edu.umak.crm.app;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

final class SharedAccess {
    private static final int NAVY = 0xff131c4b;
    private static final int WHITE = 0xffffffff;
    private static final int PAPER = 0xfff5f6f8;
    private static final int INK = 0xff16191c;
    private static final int MUTED = 0xff586b83;
    private static final int PALE = 0xffe5edf6;
    private static final int YELLOW = 0xfff8eb37;

    private final MainActivity activity;
    private final Typeface heading;
    private final Typeface body;
    private final Typeface bold;
    private TextView feedback;

    SharedAccess(MainActivity activity) {
        this.activity = activity;
        heading = activity.getResources().getFont(R.font.marcellus_regular);
        body = activity.getResources().getFont(R.font.metropolis_regular);
        bold = activity.getResources().getFont(R.font.metropolis_semibold);
    }

    View welcome() {
        FrameLayout screen = new FrameLayout(activity);
        screen.setBackgroundColor(0xff09154d);
        LinearLayout brand = column();
        brand.setGravity(Gravity.CENTER);
        brand.setTranslationY(-dp(34));
        brand.setPadding(dp(16), 0, dp(16), 0);
        ImageView emblems = new ImageView(activity);
        emblems.setImageResource(R.drawable.shared_access_emblems);
        emblems.setContentDescription("University of Makati and Center for Research Management emblems");
        emblems.setScaleType(ImageView.ScaleType.FIT_CENTER);
        brand.addView(emblems, new LinearLayout.LayoutParams(dp(188), dp(91)));
        centered(brand, "UNIVERSITY OF MAKATI", 19, WHITE, heading);
        centered(brand, "Center for Research Management - RMLIS", 13, WHITE, bold);
        screen.addView(brand, new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER));
        Button start = button("Get Started", YELLOW, NAVY, activity::startAccess);
        FrameLayout.LayoutParams startParams = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        startParams.setMargins(dp(46), 0, dp(46), dp(52));
        screen.addView(start, startParams);
        return screen;
    }

    View signIn(String emailAddress) {
        LinearLayout form = form("Sign In", "Enter your given institutional email and password to access all CRM services.");
        EditText email = field(form, "Institutional Email", "e.g. jdelacruz.K1234@umak.edu.ph", false);
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setText(emailAddress);
        EditText password = field(form, "Password", "Enter your password", true);
        Button forgot = button("Forgot password?", PAPER, MUTED, () -> information(
                "Forgot password?", "Contact the Center for Research Management to recover access to your account."));
        forgot.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        form.addView(forgot);
        feedback = text("", 13, MUTED, body);
        feedback.setVisibility(View.GONE);
        form.addView(feedback);
        action(form, "Sign In", NAVY, WHITE, () -> activity.signIn(email.getText().toString().trim(), password));
        action(form, "Need Help? Click here", PALE, NAVY, activity::showAccessHelp);
        action(form, "Continue as Guest →", PAPER, INK, activity::showGuest);
        return scroll(form);
    }

    View activate() {
        LinearLayout form = form("Change Temporary Password",
                "For security purposes, please set a new password before continuing. Enter the activation code provided by CRM.");
        EditText code = field(form, "Activation Code", "Enter your activation code", true);
        EditText password = field(form, "New Password", "Enter your new password", true);
        EditText confirm = field(form, "Confirm Password", "Re-enter your new password", true);
        feedback = text("", 13, MUTED, body);
        feedback.setVisibility(View.GONE);
        form.addView(feedback);
        action(form, "Update Password", NAVY, WHITE, () -> activity.activate(code, password, confirm));
        action(form, "← Go back", PAPER, INK, activity::showSignIn);
        return scroll(form);
    }

    View passwordUpdated() {
        LinearLayout form = form("Password Update Successful",
                "Your password has been updated. You may now sign in to the UMak Research Management and Lifecycle Information System.");
        View space = new View(activity);
        form.addView(space, new LinearLayout.LayoutParams(-1, 0, 1));
        action(form, "Continue", NAVY, WHITE, activity::showSignIn);
        form.setPadding(dp(22), dp(96), dp(22), dp(160));
        return scroll(form);
    }

    void feedback(String message) {
        if (feedback == null) return;
        feedback.setText(message);
        feedback.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
    }

    void information(String title, String message) {
        dialog(title, message, "Close", null, null);
    }

    void dialog(String title, String message, String action, Runnable callback, String cancel) {
        LinearLayout card = column();
        card.setPadding(dp(22), dp(22), dp(22), dp(10));
        card.setBackground(shape(WHITE, 14, 0));
        card.addView(text(title, 23, NAVY, heading));
        TextView description = text(message, 14, MUTED, body);
        description.setPadding(0, dp(12), 0, dp(10));
        card.addView(description);
        AlertDialog dialog = new AlertDialog.Builder(activity).setView(card).create();
        LinearLayout actions = new LinearLayout(activity);
        actions.setGravity(cancel == null ? Gravity.CENTER : Gravity.END);
        card.addView(actions);
        if (cancel != null) actions.addView(button(cancel, WHITE, 0xffb72a39, dialog::dismiss));
        actions.addView(button(action, WHITE, NAVY, () -> {
            dialog.dismiss();
            if (callback != null) callback.run();
        }));
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private LinearLayout form(String title, String subtitle) {
        LinearLayout form = column();
        form.setPadding(dp(22), dp(96), dp(22), dp(30));
        form.setFocusableInTouchMode(true);
        form.addView(text(title, 32, INK, heading));
        TextView description = text(subtitle, 14, MUTED, body);
        description.setPadding(0, dp(12), 0, dp(30));
        form.addView(description);
        return form;
    }

    private View scroll(LinearLayout form) {
        ScrollView scroll = new ScrollView(activity);
        scroll.setBackgroundColor(PAPER);
        scroll.setFillViewport(true);
        scroll.addView(form);
        return scroll;
    }

    private EditText field(LinearLayout form, String label, String hint, boolean secret) {
        TextView caption = text(label, 13, MUTED, bold);
        caption.setPadding(0, dp(10), 0, dp(5));
        form.addView(caption);
        LinearLayout row = new LinearLayout(activity);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(shape(PAPER, 8, 0xffdce4ee));
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setHintTextColor(MUTED);
        input.setTextColor(INK);
        input.setTextSize(14);
        input.setSingleLine(true);
        input.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        input.setPadding(dp(12), dp(10), dp(10), dp(10));
        input.setMinHeight(dp(52));
        input.setSaveEnabled(false);
        if (secret) input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setTypeface(body);
        row.addView(input, new LinearLayout.LayoutParams(0, -2, 1));
        if (secret) {
            Button eye = button("", android.graphics.Color.TRANSPARENT, MUTED, () -> {});
            eye.setContentDescription("Show " + label.toLowerCase(java.util.Locale.ROOT));
            eye.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shared_visibility, 0, 0, 0);
            eye.setPadding(dp(12), 0, 0, 0);
            eye.setOnClickListener(view -> {
                boolean reveal = input.getTransformationMethod() instanceof PasswordTransformationMethod;
                input.setTransformationMethod(reveal ? null : PasswordTransformationMethod.getInstance());
                input.setSelection(input.length());
                eye.setContentDescription((reveal ? "Hide " : "Show ") + label.toLowerCase(java.util.Locale.ROOT));
            });
            row.addView(eye, new LinearLayout.LayoutParams(dp(48), dp(48)));
        }
        form.addView(row);
        return input;
    }

    private void action(LinearLayout parent, String title, int fill, int color, Runnable callback) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(10);
        parent.addView(button(title, fill, color, callback), params);
    }

    private Button button(String title, int fill, int color, Runnable callback) {
        Button button = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        button.setText(title);
        button.setAllCaps(false);
        button.setTypeface(bold);
        button.setTextColor(color);
        button.setMinHeight(dp(46));
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x20395072), shape(fill, 7, 0), null));
        button.setOnClickListener(view -> callback.run());
        return button;
    }

    private LinearLayout column() {
        LinearLayout view = new LinearLayout(activity);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    private void centered(LinearLayout parent, String value, int size, int color, Typeface font) {
        TextView view = text(value, size, color, font);
        view.setGravity(Gravity.CENTER);
        view.setPadding(0, dp(7), 0, 0);
        parent.addView(view, new LinearLayout.LayoutParams(-1, -2));
    }

    private TextView text(String value, int size, int color, Typeface font) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(font);
        return view;
    }

    private GradientDrawable shape(int fill, int radius, int stroke) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(dp(radius));
        if (stroke != 0) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
