package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class EditProfileView {

    public interface Listener {
        void onSave(String name, String college);
        void onCancel();
    }

    private final Context context;
    private final LinearLayout layout;
    private final Listener listener;
    private final ResearchData.UserProfile userProfile;

    private final EditText nameInput;
    private final TextView nameError;
    private final Spinner collegeSpinner;
    private final TextView collegeError;
    private final Button saveBtn;
    private final PendingStatus saveStatus;

    private static final String[] COLLEGES = {
            "Select your college or institute",
            "College of Computing and Information Sciences",
            "College of Arts and Letters",
            "College of Science",
            "College of Business and Financial Science",
            "College of Education",
            "College of Technology",
            "College of Tourism and Hospitality Management",
            "Center for Governance and Public Policy",
            "Institute of Imaging Health Sciences",
            "Institute of Nursing",
            "Institute of Pharmacy",
            "School of Law"
    };

    public EditProfileView(Context context, ResearchData.UserProfile profile, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.userProfile = profile != null ? profile : ResearchData.getDefaultProfile();

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);
        int pad = UiTheme.dp(context, 16);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(UiTheme.PAPER);
        layout.setPadding(pad, pad, pad, pad);

        addSpacing(context, layout, 16);

        // Personal Details Card
        LinearLayout personalCard = new LinearLayout(context);
        personalCard.setOrientation(LinearLayout.VERTICAL);
        personalCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        personalCard.setPadding(pad, pad, pad, pad);

        TextView pTitle = new TextView(context);
        pTitle.setText("Personal details");
        pTitle.setTextColor(UiTheme.NAVY);
        pTitle.setTypeface(bold);
        ScreenTypography.apply(pTitle, 16);
        personalCard.addView(pTitle);

        addSpacing(context, personalCard, 12);

        // Name field
        TextView nameLabel = new TextView(context);
        nameLabel.setText("Name *");
        nameLabel.setTextColor(UiTheme.NAVY);
        nameLabel.setTypeface(bold);
        ScreenTypography.apply(nameLabel, 14);
        personalCard.addView(nameLabel);

        addSpacing(context, personalCard, 6);

        nameInput = new EditText(context);
        nameInput.setText(userProfile.name);
        nameInput.setHint("Enter your full name");
        nameInput.setTextColor(UiTheme.NAVY);
        nameInput.setTypeface(regular);
        ScreenTypography.apply(nameInput, 14);
        nameInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int inpPad = UiTheme.dp(context, 10);
        nameInput.setPadding(inpPad, inpPad, inpPad, inpPad);
        nameInput.setSingleLine(true);
        personalCard.addView(nameInput);

        nameError = new TextView(context);
        nameError.setTextColor(UiTheme.ERROR_RED);
        nameError.setTypeface(regular);
        ScreenTypography.apply(nameError, 12);
        nameError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        nameError.setVisibility(View.GONE);
        personalCard.addView(nameError);

        addSpacing(context, personalCard, 14);

        // College field
        TextView collegeLabel = new TextView(context);
        collegeLabel.setText("College / Institute *");
        collegeLabel.setTextColor(UiTheme.NAVY);
        collegeLabel.setTypeface(bold);
        ScreenTypography.apply(collegeLabel, 14);
        personalCard.addView(collegeLabel);

        addSpacing(context, personalCard, 6);

        collegeSpinner = new Spinner(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, COLLEGES);
        collegeSpinner.setAdapter(adapter);
        collegeSpinner.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int collegeIdx = 0;
        for (int i = 0; i < COLLEGES.length; i++) {
            if (COLLEGES[i].equalsIgnoreCase(userProfile.college)) {
                collegeIdx = i;
                break;
            }
        }
        collegeSpinner.setSelection(collegeIdx);
        personalCard.addView(collegeSpinner);

        collegeError = new TextView(context);
        collegeError.setTextColor(UiTheme.ERROR_RED);
        collegeError.setTypeface(regular);
        ScreenTypography.apply(collegeError, 12);
        collegeError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        collegeError.setVisibility(View.GONE);
        personalCard.addView(collegeError);

        layout.addView(personalCard);

        addSpacing(context, layout, 16);

        // Account Details Card
        LinearLayout accountCard = new LinearLayout(context);
        accountCard.setOrientation(LinearLayout.VERTICAL);
        accountCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        accountCard.setPadding(pad, pad, pad, pad);

        TextView accTitle = new TextView(context);
        accTitle.setText("Account details");
        accTitle.setTextColor(UiTheme.NAVY);
        accTitle.setTypeface(bold);
        ScreenTypography.apply(accTitle, 16);
        accountCard.addView(accTitle);

        addSpacing(context, accountCard, 4);

        TextView accNote = new TextView(context);
        accNote.setText("These details are managed by CRM and can’t be edited here.");
        accNote.setTextColor(UiTheme.MUTED);
        accNote.setTypeface(regular);
        ScreenTypography.apply(accNote, 13);
        accountCard.addView(accNote);

        addSpacing(context, accountCard, 12);

        addAccountRow(accountCard, "Institutional email", userProfile.email);
        addAccountRow(accountCard, "Account identifier", userProfile.accountId);
        addAccountRow(accountCard, "Role", "Researcher");
        addAccountRow(accountCard, "Researcher category", userProfile.category);

        layout.addView(accountCard);

        addSpacing(context, layout, 24);

        // Buttons: Save & Cancel
        saveStatus = new PendingStatus(context);
        layout.addView(saveStatus);
        saveBtn = UiButton.primary(context, "Save", this::handleSave);
        layout.addView(saveBtn);

        addSpacing(context, layout, 12);

        Button cancelBtn = UiButton.outline(context, "Cancel", UiTheme.BORDER, UiTheme.NAVY, () -> {
            if (listener != null) listener.onCancel();
        });
        layout.addView(cancelBtn);
    }

    private void addAccountRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));

        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(UiTheme.MUTED);
        lbl.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(lbl, 13);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(lbl, lp);

        TextView val = new TextView(context);
        val.setText(value != null ? value : "—");
        val.setTextColor(UiTheme.NAVY);
        val.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(val, 13);
        row.addView(val);

        parent.addView(row);
    }

    private void handleSave() {
        String name = nameInput.getText().toString().trim();
        int collegePos = collegeSpinner.getSelectedItemPosition();

        boolean hasError = false;

        if (name.isEmpty()) {
            nameError.setText("Please enter your name.");
            nameError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            nameError.setVisibility(View.GONE);
        }

        if (collegePos <= 0) {
            collegeError.setText("Please select your college or institute.");
            collegeError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            collegeError.setVisibility(View.GONE);
        }

        if (hasError) return;

        String selectedCollege = COLLEGES[collegePos];
        if (listener != null) {
            listener.onSave(name, selectedCollege);
        }
    }

    public void showSaving() {
        saveBtn.setEnabled(false);
        nameInput.setEnabled(false);
        collegeSpinner.setEnabled(false);
        saveStatus.working("Saving profile to CRM…");
    }

    public void showSaveError(String error) {
        saveBtn.setEnabled(true);
        nameInput.setEnabled(true);
        collegeSpinner.setEnabled(true);
        saveStatus.result(error, true);
    }

    public void setPreviewState(String state) {
        nameError.setVisibility(View.GONE);
        collegeError.setVisibility(View.GONE);

        if ("4.1_error_name".equals(state)) {
            nameInput.setText("");
            nameError.setText("Please enter your name.");
            nameError.setVisibility(View.VISIBLE);
            collegeSpinner.setSelection(1);
        } else if ("4.1_error_college".equals(state)) {
            nameInput.setText(userProfile.name);
            collegeSpinner.setSelection(0);
            collegeError.setText("Please select your college or institute.");
            collegeError.setVisibility(View.VISIBLE);
        } else {
            nameInput.setText(userProfile.name);
            collegeSpinner.setSelection(1);
        }
    }

    private static void addSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(space);
    }

    public View getContentLayout() {
        if (layout.getParent() instanceof ViewGroup) {
            ((ViewGroup) layout.getParent()).removeView(layout);
        }
        return PageTitle.wrap(layout, "Edit Profile", "Update your name and college or institute.", false);
    }
}
