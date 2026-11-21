package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.components.PendingStatus;
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
    private final FrameLayout collegeContainer;
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

        addSpacing(context, layout, 12);

        // 1. Personal Details Card
        LinearLayout personalCard = new LinearLayout(context);
        personalCard.setOrientation(LinearLayout.VERTICAL);
        personalCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        personalCard.setPadding(pad, pad, pad, pad);

        TextView pTitle = new TextView(context);
        pTitle.setText("Personal details");
        pTitle.setTextColor(0xff082d50);
        pTitle.setTypeface(bold);
        ScreenTypography.apply(pTitle, 16);
        personalCard.addView(pTitle);

        addSpacing(context, personalCard, 14);

        // Name field
        TextView nameLabel = new TextView(context);
        nameLabel.setText("Name *");
        nameLabel.setTextColor(0xff082d50);
        nameLabel.setTypeface(bold);
        ScreenTypography.apply(nameLabel, 13);
        personalCard.addView(nameLabel);

        addSpacing(context, personalCard, 6);

        nameInput = new EditText(context);
        nameInput.setText(userProfile.name);
        nameInput.setHint("Enter your full name");
        nameInput.setHintTextColor(0xff8898aa);
        nameInput.setTextColor(0xff082d50);
        nameInput.setTypeface(regular);
        ScreenTypography.apply(nameInput, 14);
        nameInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int inpPadH = UiTheme.dp(context, 12);
        int inpPadV = UiTheme.dp(context, 10);
        nameInput.setPadding(inpPadH, inpPadV, inpPadH, inpPadV);
        nameInput.setMinHeight(UiTheme.dp(context, 46));
        nameInput.setSingleLine(true);
        personalCard.addView(nameInput);

        nameError = new TextView(context);
        nameError.setTextColor(0xffdc2626);
        nameError.setTypeface(regular);
        ScreenTypography.apply(nameError, 12);
        nameError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        nameError.setVisibility(View.GONE);
        personalCard.addView(nameError);

        addSpacing(context, personalCard, 14);

        // College field
        TextView collegeLabel = new TextView(context);
        collegeLabel.setText("College / Institute *");
        collegeLabel.setTextColor(0xff082d50);
        collegeLabel.setTypeface(bold);
        ScreenTypography.apply(collegeLabel, 13);
        personalCard.addView(collegeLabel);

        addSpacing(context, personalCard, 6);

        collegeContainer = new FrameLayout(context);
        collegeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        collegeSpinner = new Spinner(context);
        collegeSpinner.setBackground(null);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, COLLEGES) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTypeface(UiTheme.bodyFont(context));
                    ScreenTypography.apply(tv, 14);
                    tv.setTextColor(position == 0 ? 0xff8898aa : 0xff082d50);
                    tv.setPadding(UiTheme.dp(context, 12), 0, UiTheme.dp(context, 32), 0);
                }
                return v;
            }
        };
        collegeSpinner.setAdapter(adapter);
        FrameLayout.LayoutParams spinLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 46));
        collegeContainer.addView(collegeSpinner, spinLp);

        ImageView chevron = new ImageView(context);
        chevron.setImageResource(R.drawable.ic_crm_chevron_down);
        chevron.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams chevLp = new FrameLayout.LayoutParams(UiTheme.dp(context, 20), UiTheme.dp(context, 20));
        chevLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        chevLp.rightMargin = UiTheme.dp(context, 12);
        collegeContainer.addView(chevron, chevLp);

        int collegeIdx = 0;
        for (int i = 0; i < COLLEGES.length; i++) {
            if (COLLEGES[i].equalsIgnoreCase(userProfile.college)) {
                collegeIdx = i;
                break;
            }
        }
        collegeSpinner.setSelection(collegeIdx);
        personalCard.addView(collegeContainer);

        collegeError = new TextView(context);
        collegeError.setTextColor(0xffdc2626);
        collegeError.setTypeface(regular);
        ScreenTypography.apply(collegeError, 12);
        collegeError.setPadding(0, UiTheme.dp(context, 4), 0, 0);
        collegeError.setVisibility(View.GONE);
        personalCard.addView(collegeError);

        layout.addView(personalCard);

        addSpacing(context, layout, 14);

        // 2. Account Details Card
        LinearLayout accountCard = new LinearLayout(context);
        accountCard.setOrientation(LinearLayout.VERTICAL);
        accountCard.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        accountCard.setPadding(pad, pad, pad, pad);

        TextView accTitle = new TextView(context);
        accTitle.setText("Account details");
        accTitle.setTextColor(0xff082d50);
        accTitle.setTypeface(bold);
        ScreenTypography.apply(accTitle, 16);
        accountCard.addView(accTitle);

        addSpacing(context, accountCard, 4);

        TextView accNote = new TextView(context);
        accNote.setText("These details are managed by CRM and can't be edited here.");
        accNote.setTextColor(0xff586b83);
        accNote.setTypeface(regular);
        ScreenTypography.apply(accNote, 13);
        accountCard.addView(accNote);

        addSpacing(context, accountCard, 14);

        addAccountRow(accountCard, "Institutional email", userProfile.email);
        addAccountRow(accountCard, "Account identifier", userProfile.accountId);
        addAccountRow(accountCard, "Role", "Researcher");
        addAccountRow(accountCard, "Researcher category", userProfile.category);

        layout.addView(accountCard);

        addSpacing(context, layout, 20);

        // Buttons: Save & Cancel
        saveStatus = new PendingStatus(context);
        layout.addView(saveStatus);

        saveBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        saveBtn.setText("Save");
        saveBtn.setAllCaps(false);
        saveBtn.setTextColor(0xffffffff);
        saveBtn.setTypeface(bold);
        ScreenTypography.apply(saveBtn, 15);
        saveBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        saveBtn.setOnClickListener(v -> handleSave());
        layout.addView(saveBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));

        addSpacing(context, layout, 10);

        Button cancelBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        cancelBtn.setText("Cancel");
        cancelBtn.setAllCaps(false);
        cancelBtn.setTextColor(0xff082d50);
        cancelBtn.setTypeface(bold);
        ScreenTypography.apply(cancelBtn, 15);
        cancelBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffcbd5e1), 0x20082d50));
        cancelBtn.setOnClickListener(v -> {
            if (listener != null) listener.onCancel();
        });
        layout.addView(cancelBtn, new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 46)));
    }

    private void addAccountRow(LinearLayout parent, String label, String value) {
        TextView lbl = new TextView(context);
        lbl.setText(label);
        lbl.setTextColor(0xff082d50);
        lbl.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(lbl, 13);
        lbl.setPadding(0, 0, 0, UiTheme.dp(context, 4));
        parent.addView(lbl);

        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setBackground(UiTheme.shape(context, 0xfff8fafc, 8, 0xffe2e8f0));
        int padH = UiTheme.dp(context, 12);
        box.setPadding(padH, 0, padH, 0);
        box.setMinimumHeight(UiTheme.dp(context, 44));

        TextView val = new TextView(context);
        val.setText(value != null ? value : "—");
        val.setTextColor(0xff586b83);
        val.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(val, 13);
        LinearLayout.LayoutParams valLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        box.addView(val, valLp);

        ImageView lockIcon = new ImageView(context);
        lockIcon.setImageResource(R.drawable.ic_crm_lock);
        lockIcon.setColorFilter(0xff94a3b8);
        int lockSize = UiTheme.dp(context, 16);
        box.addView(lockIcon, new LinearLayout.LayoutParams(lockSize, lockSize));

        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(-1, UiTheme.dp(context, 44));
        boxLp.bottomMargin = UiTheme.dp(context, 12);
        parent.addView(box, boxLp);
    }

    private void handleSave() {
        String name = nameInput.getText().toString().trim();
        int collegePos = collegeSpinner.getSelectedItemPosition();

        boolean hasError = false;

        if (name.isEmpty()) {
            nameInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            nameError.setText("⚠ Please enter your name.");
            nameError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            nameInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
            nameError.setVisibility(View.GONE);
        }

        if (collegePos <= 0) {
            collegeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            collegeError.setText("⚠ Please select your college or institute.");
            collegeError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            collegeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
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
        nameInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        collegeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));

        if ("4.1_error_name".equals(state)) {
            nameInput.setText("");
            nameInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            nameError.setText("⚠ Please enter your name.");
            nameError.setVisibility(View.VISIBLE);
            collegeSpinner.setSelection(1);
        } else if ("4.1_error_college".equals(state)) {
            nameInput.setText(userProfile.name);
            collegeSpinner.setSelection(0);
            collegeContainer.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, 0xffdc2626));
            collegeError.setText("⚠ Please select your college or institute.");
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
