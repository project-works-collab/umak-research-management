package ph.edu.umak.crm.app.auth;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiBanner;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.components.UiInput;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class CreateAccountView {
    private final Context context;
    private final View root;
    private final LinearLayout bannerContainer;
    private final UiInput.FormField nameField;
    private final UiInput.FormField emailField;
    private final Spinner collegeSpinner;
    private final LinearLayout collegeBox;
    private final TextView collegeError;
    private final Button studentBtn;
    private final Button facultyBtn;
    private final UiInput.FormField passwordField;
    private final UiInput.FormField confirmField;
    private final Button submitBtn;
    private final AuthCallbacks callbacks;
    private boolean isStudent = true;

    private static final String[] COLLEGES = new String[]{
            "Select College / Institute",
            "CCIS - College of Computing and Information Sciences",
            "CBFS - College of Business and Financial Sciences",
            "COED - College of Education",
            "CAL - College of Arts and Letters",
            "COS - College of Science",
            "CTHTM - College of Tourism and Hospitality Management",
            "CGPP - Center for Governance and Public Policy",
            "IIHS - Institute of Imaging Health Sciences",
            "ION - Institute of Nursing",
            "IOP - Institute of Pharmacy",
            "SOL - School of Law"
    };

    public CreateAccountView(Context context, AuthCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
        Typeface heading = UiTheme.headingFont(context);
        Typeface body = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        LinearLayout form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 24);
        form.setPadding(pad, pad, pad, UiTheme.dp(context, 32));
        form.setFocusableInTouchMode(true);

        TextView headline = new TextView(context);
        headline.setText("Create Researcher Account");
        headline.setTextColor(UiTheme.INK);
        headline.setTypeface(heading);
        ScreenTypography.apply(headline, 28);
        ScreenTypography.heading(headline);
        form.addView(headline);

        TextView description = new TextView(context);
        description.setText("Use your UMak institutional email.");
        description.setTextColor(UiTheme.MUTED);
        description.setTypeface(body);
        ScreenTypography.apply(description, 14);
        description.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 16));
        form.addView(description);

        bannerContainer = new LinearLayout(context);
        bannerContainer.setOrientation(LinearLayout.VERTICAL);
        bannerContainer.setVisibility(View.GONE);
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(-1, -2);
        bannerParams.bottomMargin = UiTheme.dp(context, 16);
        form.addView(bannerContainer, bannerParams);

        nameField = UiInput.field(context, form, "Full name *", "e.g. Juan Dela Cruz", false);

        emailField = UiInput.field(context, form, "Institutional email *", "e.g. jdelacruz.k1234@umak.edu.ph", false);
        emailField.getInput().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // College / Institute *
        TextView collegeLabel = new TextView(context);
        collegeLabel.setText(UiInput.formatLabel("College / Institute *"));
        collegeLabel.setTextColor(UiTheme.MUTED);
        collegeLabel.setTypeface(bold);
        ScreenTypography.apply(collegeLabel, 13);
        collegeLabel.setPadding(0, UiTheme.dp(context, 10), 0, UiTheme.dp(context, 4));
        form.addView(collegeLabel);

        collegeBox = new LinearLayout(context);
        collegeBox.setGravity(Gravity.CENTER_VERTICAL);
        collegeBox.setBackground(UiTheme.shape(context, UiTheme.PAPER, 8, UiTheme.BORDER));
        collegeBox.setMinimumHeight(UiTheme.dp(context, 48));
        collegeBox.setPadding(UiTheme.dp(context, 6), UiTheme.dp(context, 4), UiTheme.dp(context, 6), UiTheme.dp(context, 4));

        collegeSpinner = new Spinner(context, Spinner.MODE_DIALOG);
        collegeSpinner.setPrompt("Select College / Institute");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, COLLEGES) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTypeface(UiTheme.bodyFont(context));
                    ScreenTypography.apply(tv, 14);
                    tv.setTextColor(position == 0 ? UiTheme.MUTED : UiTheme.INK);
                    tv.setSingleLine(false);
                    tv.setMaxLines(4);
                    tv.setEllipsize(null);
                }
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTypeface(UiTheme.bodyFont(context));
                    ScreenTypography.apply(tv, 14);
                    tv.setTextColor(position == 0 ? UiTheme.MUTED : UiTheme.INK);
                    tv.setSingleLine(false);
                    tv.setMaxLines(4);
                    tv.setEllipsize(null);
                    int pad = UiTheme.dp(context, 14);
                    tv.setPadding(pad, pad, pad, pad);
                }
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        collegeSpinner.setAdapter(adapter);
        collegeBox.addView(collegeSpinner, new LinearLayout.LayoutParams(-1, -2));
        form.addView(collegeBox);

        collegeError = new TextView(context);
        collegeError.setTextColor(UiTheme.ERROR_RED);
        collegeError.setTypeface(body);
        ScreenTypography.apply(collegeError, 12);
        collegeError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        collegeError.setVisibility(View.GONE);
        form.addView(collegeError);

        // Researcher category toggle
        TextView categoryLabel = new TextView(context);
        categoryLabel.setText("Researcher category");
        categoryLabel.setTextColor(UiTheme.MUTED);
        categoryLabel.setTypeface(bold);
        ScreenTypography.apply(categoryLabel, 13);
        categoryLabel.setPadding(0, UiTheme.dp(context, 12), 0, UiTheme.dp(context, 6));
        form.addView(categoryLabel);

        LinearLayout toggleRow = new LinearLayout(context);
        toggleRow.setOrientation(LinearLayout.HORIZONTAL);

        studentBtn = UiButton.primary(context, "Student", () -> setCategory(true));
        studentBtn.setMinHeight(UiTheme.dp(context, 40));
        facultyBtn = UiButton.secondary(context, "Faculty", () -> setCategory(false));
        facultyBtn.setMinHeight(UiTheme.dp(context, 40));

        LinearLayout.LayoutParams toggleParams1 = new LinearLayout.LayoutParams(0, -2, 1);
        toggleParams1.rightMargin = UiTheme.dp(context, 8);
        toggleRow.addView(studentBtn, toggleParams1);
        toggleRow.addView(facultyBtn, new LinearLayout.LayoutParams(0, -2, 1));
        form.addView(toggleRow);

        passwordField = UiInput.field(context, form, "Password *", "Create a password", true);
        passwordField.setHelper("At least 12 characters");

        confirmField = UiInput.field(context, form, "Confirm password *", "Re-enter your password", true);

        submitBtn = UiButton.primary(context, "Create Account", this::attemptCreateAccount);
        LinearLayout.LayoutParams submitParams = new LinearLayout.LayoutParams(-1, -2);
        submitParams.topMargin = UiTheme.dp(context, 18);
        form.addView(submitBtn, submitParams);

        Button backBtn = UiButton.text(context, "Go back", UiTheme.INK, () -> {
            if (callbacks != null) callbacks.onBackFromCreateAccount();
        });
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(-1, -2);
        backParams.topMargin = UiTheme.dp(context, 8);
        form.addView(backBtn, backParams);

        ScrollView scroll = new ScrollView(context);
        scroll.setBackgroundColor(UiTheme.PAPER);
        scroll.setFillViewport(true);
        scroll.addView(form);

        root = scroll;
    }

    private void setCategory(boolean student) {
        isStudent = student;
        if (student) {
            studentBtn.setText("Student");
            studentBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.NAVY, 8, 0), 0x20395072));
            studentBtn.setTextColor(UiTheme.WHITE);

            facultyBtn.setText("Faculty");
            facultyBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.PALE, 8, 0), 0x20395072));
            facultyBtn.setTextColor(UiTheme.NAVY);
        } else {
            studentBtn.setText("Student");
            studentBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.PALE, 8, 0), 0x20395072));
            studentBtn.setTextColor(UiTheme.NAVY);

            facultyBtn.setText("Faculty");
            facultyBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.NAVY, 8, 0), 0x20395072));
            facultyBtn.setTextColor(UiTheme.WHITE);
        }
    }

    private void attemptCreateAccount() {
        clearErrors();
        String name = nameField.getInput().getText().toString().trim();
        String email = emailField.getInput().getText().toString().trim();
        int collegeIndex = collegeSpinner.getSelectedItemPosition();
        String password = passwordField.getInput().getText().toString();
        String confirm = confirmField.getInput().getText().toString();

        boolean missingRequired = false;
        if (name.isEmpty()) {
            nameField.setError("Required");
            missingRequired = true;
        }
        if (email.isEmpty()) {
            emailField.setError("Required");
            missingRequired = true;
        }
        if (collegeIndex == 0) {
            collegeError.setText("Required");
            collegeError.setVisibility(View.VISIBLE);
            collegeBox.setBackground(UiTheme.shape(context, UiTheme.PAPER, 8, UiTheme.ERROR_RED));
            missingRequired = true;
        }
        if (password.isEmpty()) {
            passwordField.setError("Required");
            missingRequired = true;
        }
        if (confirm.isEmpty()) {
            confirmField.setError("Required");
            missingRequired = true;
        }

        if (missingRequired) {
            showBanner("Complete the highlighted account details.");
            return;
        }

        if (password.length() < 12) {
            passwordField.setError("Password must be at least 12 characters.");
            return;
        }

        if (!password.equals(confirm)) {
            confirmField.setError("Passwords do not match.");
            return;
        }

        if (callbacks != null) {
            String college = COLLEGES[collegeIndex].split(" - ", 2)[1];
            String category = isStudent ? "student" : "faculty";
            callbacks.onAccountCreated(email, name, college, category, password, confirm);
        }
    }

    public void setSubmitting(boolean submitting) {
        submitBtn.setEnabled(!submitting);
        submitBtn.setText(submitting ? "Creating account…" : "Create Account");
        nameField.setEnabled(!submitting);
        emailField.setEnabled(!submitting);
        passwordField.setEnabled(!submitting);
        confirmField.setEnabled(!submitting);
        collegeSpinner.setEnabled(!submitting);
        studentBtn.setEnabled(!submitting);
        facultyBtn.setEnabled(!submitting);
    }

    public void clearPasswords() {
        passwordField.getInput().getText().clear();
        confirmField.getInput().getText().clear();
    }

    public void showEmailError(String error) {
        emailField.setError(error);
    }

    public void clearErrors() {
        bannerContainer.removeAllViews();
        bannerContainer.setVisibility(View.GONE);
        nameField.clearError();
        emailField.clearError();
        collegeError.setVisibility(View.GONE);
        collegeBox.setBackground(UiTheme.shape(context, UiTheme.PAPER, 8, UiTheme.BORDER));
        passwordField.clearError();
        passwordField.setHelper("At least 12 characters");
        confirmField.clearError();
    }

    public void showBanner(String message) {
        bannerContainer.removeAllViews();
        bannerContainer.addView(UiBanner.create(context, message, null, null));
        bannerContainer.setVisibility(View.VISIBLE);
    }

    public void configurePreviewState(String state) {
        clearErrors();
        if ("1.4".equals(state)) {
            nameField.getInput().setText("");
            emailField.getInput().setText("");
            collegeSpinner.setSelection(0);
            passwordField.getInput().setText("");
            confirmField.getInput().setText("");
        } else if ("1.4-required".equals(state)) {
            showBanner("Complete the highlighted account details.");
            nameField.setError("Required");
            emailField.setError("Required");
            collegeError.setText("Required");
            collegeError.setVisibility(View.VISIBLE);
            collegeBox.setBackground(UiTheme.shape(context, UiTheme.PAPER, 8, UiTheme.ERROR_RED));
            passwordField.setError("Required");
            confirmField.setError("Required");
        } else if ("1.4-duplicate".equals(state)) {
            nameField.getInput().setText("Juan Dela Cruz");
            emailField.getInput().setText("jdelacruz.k1234@umak.edu.ph");
            collegeSpinner.setSelection(1);
            passwordField.getInput().setText("SecurePass123456!");
            confirmField.getInput().setText("SecurePass123456!");
            emailField.setError("This institutional email is already registered.");
        } else if ("1.4-short-pass".equals(state)) {
            nameField.getInput().setText("Juan Dela Cruz");
            emailField.getInput().setText("jdelacruz.k1234@umak.edu.ph");
            collegeSpinner.setSelection(1);
            passwordField.getInput().setText("short123");
            confirmField.getInput().setText("short123");
            passwordField.setError("Password must be at least 12 characters.");
        } else if ("1.4-mismatch".equals(state)) {
            nameField.getInput().setText("Juan Dela Cruz");
            emailField.getInput().setText("jdelacruz.k1234@umak.edu.ph");
            collegeSpinner.setSelection(1);
            passwordField.getInput().setText("SecurePass123456!");
            confirmField.getInput().setText("DifferentPass123!");
            confirmField.setError("Passwords do not match.");
        }
    }

    public View getView() {
        return root;
    }
}
