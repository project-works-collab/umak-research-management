package ph.edu.umak.crm.app.staff;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.common.dialogs.CenteredActionDialog;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class StaffDialogs {
    private StaffDialogs() {}

    public static void showWithdrawServiceDialog(Context context, String serviceId, String serviceName, Runnable onConfirm) {
        CenteredActionDialog.show(context, "Withdraw service?",
                "Hide " + serviceId + " " + serviceName + " from researchers. Existing requests keep their history. Cancel leaves the service unchanged.",
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
        new AlertDialog.Builder(context)
                .setTitle("Can’t deactivate account")
                .setMessage("You cannot deactivate your own account.")
                .setPositiveButton("Go Back", null)
                .show();
    }

    public static void showCantDeactivateLastAdminDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Can’t deactivate account")
                .setMessage("Keep at least one active account administrator.")
                .setPositiveButton("Go Back", null)
                .show();
    }

    public static void showTemporaryCredentialIssuedDialog(Context context, String temporaryCode) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 20);
        layout.setPadding(pad, pad, pad, pad);

        TextView sub = new TextView(context);
        sub.setText("Share it through the agreed handover process. The account holder must replace it before protected access is granted.");
        sub.setTextColor(UiTheme.MUTED);
        sub.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(sub, 13);
        layout.addView(sub);

        String displayCode = (temporaryCode != null && !temporaryCode.trim().isEmpty())
                ? temporaryCode
                : "TMP-0000-0000";

        TextView code = new TextView(context);
        code.setText(displayCode);
        code.setTextColor(UiTheme.NAVY);
        code.setTypeface(UiTheme.boldFont(context));
        code.setGravity(Gravity.CENTER);
        ScreenTypography.apply(code, 18);
        int cPad = UiTheme.dp(context, 14);
        code.setPadding(cPad, cPad, cPad, cPad);
        code.setBackground(UiTheme.shape(context, UiTheme.PALE, 8, UiTheme.BORDER));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = UiTheme.dp(context, 16);
        code.setLayoutParams(lp);
        layout.addView(code);

        new AlertDialog.Builder(context)
                .setTitle("Temporary credential issued")
                .setView(layout)
                .setPositiveButton("Done", null)
                .show();
    }
}
