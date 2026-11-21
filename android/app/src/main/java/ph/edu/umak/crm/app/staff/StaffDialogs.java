package ph.edu.umak.crm.app.staff;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.dialogs.CenteredActionDialog;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class StaffDialogs {
    private StaffDialogs() {}

    public static void showWithdrawServiceDialog(Context context, String serviceId, String serviceName, Runnable onConfirm) {
        CenteredActionDialog.show(context, "Withdraw service?",
                "Hide " + serviceId.toLowerCase() + " " + serviceName + " from researchers. Existing requests keep their history. Cancel leaves the service unchanged.",
                "Confirm", "Cancel", onConfirm, null);
    }

    public static void showReplaceFormVersionDialog(Context context, Runnable onConfirm) {
        CenteredActionDialog.show(context, "Replace form version?",
                "Make CRM_Form_004_v4.pdf Version 4, the current CRM Form 004 – Ethics Application. Version 3 moves to superseded versions (staff only). Cancel leaves the current version unchanged.",
                "Confirm", "Cancel", onConfirm, null);
    }

    public static void showDeactivateAccountDialog(Context context, String name, String role, Runnable onConfirm) {
        CenteredActionDialog.show(context, "Deactivate account?",
                name + " (" + role + ") will lose access to RMLIS until the account is activated again. Cancel leaves the account unchanged.",
                "Confirm", "Cancel", onConfirm, null);
    }

    public static void showActivateAccountDialog(Context context, String name, String role, Runnable onConfirm) {
        CenteredActionDialog.show(context, "Activate account?",
                name + " (" + role + ") will be able to sign in to RMLIS again. Cancel leaves the account unchanged.",
                "Confirm", "Cancel", onConfirm, null);
    }

    public static void showCantDeactivateOwnAccountDialog(Context context) {
        CenteredActionDialog.show(context, "Can’t deactivate account",
                "You cannot deactivate your own account.",
                "Go Back", null, null, null);
    }

    public static void showCantDeactivateLastAdminDialog(Context context) {
        CenteredActionDialog.show(context, "Can’t deactivate account",
                "Keep at least one active account administrator.",
                "Go Back", null, null, null);
    }

    public static void showTemporaryCredentialIssuedDialog(Context context, String temporaryCode) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 24);
        content.setPadding(pad, pad, pad, pad);
        content.setBackground(UiTheme.shape(context, UiTheme.WHITE, 12, 0));

        // Title: Temporary credential issued
        TextView heading = new TextView(context);
        heading.setText("Temporary credential issued");
        heading.setTextColor(0xff082d50);
        heading.setTypeface(UiTheme.headingFont(context));
        heading.setGravity(Gravity.CENTER);
        ScreenTypography.apply(heading, 18);
        ScreenTypography.heading(heading);
        content.addView(heading);

        // Subtitle
        TextView copy = new TextView(context);
        copy.setText("Share it through the agreed handover process. The account holder must replace it before protected access is granted.");
        copy.setTextColor(0xff586b83);
        copy.setTypeface(UiTheme.bodyFont(context));
        copy.setGravity(Gravity.CENTER);
        ScreenTypography.apply(copy, 13);
        copy.setPadding(0, UiTheme.dp(context, 8), 0, UiTheme.dp(context, 16));
        content.addView(copy);

        // Credential code container box
        LinearLayout codeBox = new LinearLayout(context);
        codeBox.setOrientation(LinearLayout.HORIZONTAL);
        codeBox.setGravity(Gravity.CENTER);
        codeBox.setBackground(UiTheme.shape(context, 0xfff8fafc, 8, UiTheme.BORDER));
        int bPad = UiTheme.dp(context, 12);
        codeBox.setPadding(bPad, bPad, bPad, bPad);

        ImageView keyIcon = new ImageView(context);
        keyIcon.setImageResource(R.drawable.ic_crm_key);
        keyIcon.setColorFilter(0xff082d50);
        int kSize = UiTheme.dp(context, 18);
        LinearLayout.LayoutParams kLp = new LinearLayout.LayoutParams(kSize, kSize);
        kLp.rightMargin = UiTheme.dp(context, 8);
        codeBox.addView(keyIcon, kLp);

        String displayCode = (temporaryCode != null && !temporaryCode.trim().isEmpty() && !"TMP-0000-0000".equals(temporaryCode))
                ? temporaryCode
                : "TMP - 7Q4K - 92LX";

        TextView codeText = new TextView(context);
        codeText.setText(displayCode);
        codeText.setTextColor(0xff082d50);
        codeText.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(codeText, 15);
        codeBox.addView(codeText);

        content.addView(codeBox);

        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(1, UiTheme.dp(context, 18)));
        content.addView(spacer);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(content)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Done button
        Button doneBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        doneBtn.setText("Done");
        doneBtn.setAllCaps(false);
        doneBtn.setTextColor(0xffffffff);
        doneBtn.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(doneBtn, 15);
        doneBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        doneBtn.setMinHeight(UiTheme.dp(context, 44));
        doneBtn.setOnClickListener(v -> dialog.dismiss());
        content.addView(doneBtn);

        dialog.show();
    }
}
