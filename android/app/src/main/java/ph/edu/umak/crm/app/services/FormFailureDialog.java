package ph.edu.umak.crm.app.services;

import android.content.Context;

import ph.edu.umak.crm.app.common.dialogs.CenteredActionDialog;

public final class FormFailureDialog {
    private FormFailureDialog() {}

    public static void show(Context context, Runnable onRetry, Runnable onCancel) {
        CenteredActionDialog.show(context, "Unable to open the form. Try again.", null,
                "Retry", "Cancel", onRetry, onCancel);
    }
}
