package ph.edu.umak.crm.app.staff.accounts;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class LiveAccountsView extends LinearLayout {
    public interface Actions {
        void request(String path, JSONObject body, BiConsumer<String, String> result);
        void back();
    }
    private static final String[] PERMISSIONS = {"content_management", "request_processing", "registry_management",
            "progress_updates", "reporting", "account_administration"};
    private final Actions actions;
    private PendingStatus status;
    private boolean saving;

    public LiveAccountsView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        setOrientation(VERTICAL);
        int padding = UiTheme.dp(context, 16);
        setPadding(padding, padding, padding, padding);
    }

    public void load() {
        reset("User accounts", "Back to Manage", actions::back);
        PendingStatus current = status;
        current.working("Loading accounts…");
        actions.request("", null, (data, error) -> {
            if (!isAttachedToWindow() || status != current) return;
            if (error != null) { current.result(error, true); button("Retry", this::load); return; }
            try {
                JSONArray users = new JSONArray(data);
                current.clear();
                button("Create staff account", this::create);
                for (int i = 0; i < users.length(); i++) {
                    JSONObject account = users.getJSONObject(i);
                    button(account.optString("display_name", account.optString("email")) + " · "
                            + account.optString("role") + " · " + (account.optBoolean("active") ? "Active" : "Inactive"),
                            () -> detail(account));
                }
            } catch (JSONException invalid) { current.result("CRM returned invalid accounts.", true); }
        });
    }

    private void create() {
        reset("Create staff account", "Back to accounts", this::load);
        EditText name = input("Name", "");
        EditText email = input("Institutional email", "");
        EditText college = input("College or unit", "");
        List<CheckBox> choices = permissionChoices(new JSONArray());
        Button save = button("Create account", null);
        save.setOnClickListener(view -> {
            if (!required(name, email, college)) return;
            try {
                post("", new JSONObject().put("display_name", value(name)).put("email", value(email))
                        .put("college", value(college)).put("permissions", selected(choices)), save);
            } catch (JSONException invalid) { status.result("Complete the highlighted account details.", true); }
        });
    }

    private void detail(JSONObject account) {
        int id = account.optInt("id");
        reset("Account details", "Back to accounts", this::load);
        line(account.optString("display_name"));
        line(account.optString("email"));
        line(account.optString("college"));
        line("Account ID: " + id);
        line(account.optString("role") + " · " + (account.optBoolean("active") ? "Active" : "Inactive"));
        if ("staff".equals(account.optString("role"))) {
            List<CheckBox> checks = permissionChoices(account.optJSONArray("permissions"));
            Button save = button("Save permissions", null);
            save.setOnClickListener(view -> {
                try { post("/" + id + "/permissions", new JSONObject().put("permissions", selected(checks)), save); }
                catch (JSONException invalid) { status.result("Review the permission choices.", true); }
            });
        } else {
            button("Set category to Student", () -> category(id, "student"));
            button("Set category to Faculty", () -> category(id, "faculty"));
        }
        boolean next = !account.optBoolean("active");
        button(next ? "Activate account" : "Deactivate account", () -> {
            String action = next ? "Activate" : "Deactivate";
            new AlertDialog.Builder(getContext()).setTitle(action + " " + account.optString("email") + "?")
                    .setNegativeButton("Cancel", null).setPositiveButton("Confirm", (dialog, which) -> {
                        try { post("/" + id + "/state", new JSONObject().put("active", next).put("confirmed", true), null); }
                        catch (JSONException invalid) { status.result("Unable to prepare this account change.", true); }
                    }).show();
        });
        button("Issue temporary credential for approved recovery", () -> recovery(id, account.optString("email")));
        button("Account audit history", () -> history(id));
    }

    private void category(int id, String role) {
        try { post("/" + id + "/category", new JSONObject().put("category", role), null); }
        catch (JSONException invalid) { status.result("Select Student or Faculty.", true); }
    }

    private void recovery(int id, String email) {
        EditText reason = new EditText(getContext());
        reason.setHint("Approved recovery reason");
        var dialog = new AlertDialog.Builder(getContext()).setTitle("Issue temporary credential for " + email + "?")
                .setView(reason).setNegativeButton("Cancel", null).setPositiveButton("Confirm", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (value(reason).isEmpty()) { reason.setError("Enter the approved recovery reason."); return; }
            try {
                JSONObject payload = new JSONObject().put("reason", value(reason)).put("confirmed", true);
                dialog.dismiss();
                post("/" + id + "/recovery", payload, null);
            } catch (JSONException invalid) { reason.setError("Check the recovery reason."); }
        }));
        dialog.show();
    }

    private void history(int id) {
        reset("Account audit history", "Back to accounts", this::load);
        PendingStatus current = status;
        current.working("Loading account history…");
        actions.request("/" + id + "/history", null, (data, error) -> {
            if (!isAttachedToWindow() || current != status) return;
            if (error != null) { current.result(error, true); return; }
            try {
                JSONArray events = new JSONArray(data);
                current.clear();
                if (events.length() == 0) line("No account changes recorded.");
                for (int i = 0; i < events.length(); i++) {
                    JSONObject event = events.getJSONObject(i);
                    line(event.optString("action") + " · Staff " + event.optString("actor_id") + " · " + event.optString("recorded_at"));
                    line("Previous: " + event.optString("before_value"));
                    line("Saved: " + event.optString("after_value"));
                    line(event.optString("reason"));
                }
            } catch (JSONException invalid) { current.result("CRM returned invalid account history.", true); }
        });
    }

    private void post(String path, JSONObject payload, Button button) {
        if (saving) return;
        saving = true;
        if (button != null) button.setEnabled(false);
        PendingStatus current = status;
        current.working("Saving account changes…");
        actions.request(path, payload, (data, error) -> {
            saving = false;
            if (!isAttachedToWindow() || current != status) return;
            if (button != null) button.setEnabled(true);
            if (error != null) { current.result(error, true); return; }
            try {
                JSONObject result = new JSONObject(data);
                if (result.has("temporary_credential")) {
                    reset("Temporary credential issued", "Back to accounts", this::load);
                    line(result.getJSONObject("account").optString("email"));
                    TextView credential = line(result.getString("temporary_credential"));
                    credential.setTextIsSelectable(true);
                    line("The account holder must replace this temporary credential before accessing protected functions.");
                } else load();
            } catch (JSONException invalid) { current.result("CRM could not confirm the account change.", true); }
        });
    }

    private List<CheckBox> permissionChoices(JSONArray permissions) {
        List<CheckBox> choices = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            CheckBox choice = new CheckBox(getContext());
            choice.setText(permission.replace('_', ' '));
            choice.setTag(permission);
            if (permissions != null) for (int i = 0; i < permissions.length(); i++)
                if (permission.equals(permissions.optString(i))) choice.setChecked(true);
            addView(choice);
            choices.add(choice);
        }
        return choices;
    }
    private static JSONArray selected(List<CheckBox> checks) {
        JSONArray values = new JSONArray();
        for (CheckBox check : checks) if (check.isChecked()) values.put(check.getTag().toString());
        return values;
    }
    private boolean required(EditText... fields) {
        boolean valid = true;
        for (EditText field : fields) if (value(field).isEmpty()) { field.setError("Required"); valid = false; }
        if (!valid) status.result("Complete the highlighted account details.", true);
        return valid;
    }
    private void reset(String title, String back, Runnable action) {
        removeAllViews(); button(back, action); line(title);
        status = new PendingStatus(getContext()); addView(status);
    }
    private TextView line(String content) {
        TextView text = new TextView(getContext()); text.setText(content); text.setTextColor(UiTheme.NAVY);
        addView(text); return text;
    }
    private EditText input(String label, String value) {
        line(label); EditText field = new EditText(getContext()); field.setText(value); field.setContentDescription(label);
        addView(field); return field;
    }
    private Button button(String label, Runnable action) {
        Button button = UiButton.outline(getContext(), label, UiTheme.BORDER, UiTheme.NAVY, action); addView(button); return button;
    }
    private static String value(EditText field) { return field.getText().toString().trim(); }
}
