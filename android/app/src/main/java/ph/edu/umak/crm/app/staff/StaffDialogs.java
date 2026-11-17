package ph.edu.umak.crm.app.staff;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class StaffDialogs {
    private StaffDialogs() {}

    public static void showWithdrawServiceDialog(Context context, String serviceId, String serviceName, Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle("Withdraw service?")
                .setMessage("Hide " + serviceId + " " + serviceName + " from researchers. Existing requests keep their history. Cancel leaves the service unchanged.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void showReplaceFormVersionDialog(Context context, Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle("Replace form version?")
                .setMessage("Make CRM_Form_004_v4.pdf Version 4, the current CRM Form 004 – Ethics Application. Version 3 moves to superseded versions (staff only). Cancel leaves the current version unchanged.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void showDeactivateAccountDialog(Context context, String name, String role, Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle("Deactivate account?")
                .setMessage(name + " (" + role + ") will lose access to RMLIS until the account is activated again. Cancel leaves the account unchanged.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void showActivateAccountDialog(Context context, String name, String role, Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle("Activate account?")
                .setMessage(name + " (" + role + ") will be able to sign in to RMLIS again. Cancel leaves the account unchanged.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    public static void showTemporaryCredentialIssuedDialog(Context context, String credential) {
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

        TextView code = new TextView(context);
        code.setText(credential != null ? credential : "TMP-7Q4K-92LX");
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
