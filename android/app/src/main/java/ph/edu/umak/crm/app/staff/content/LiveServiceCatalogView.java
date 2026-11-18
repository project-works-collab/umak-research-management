package ph.edu.umak.crm.app.staff.content;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.BiConsumer;

import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class LiveServiceCatalogView {
    public interface Actions {
        void request(String path, JSONObject body, BiConsumer<String, String> complete);
        void replaceForm(int id, Runnable complete, java.util.function.Consumer<String> failure);
        void downloadForm(int id, String name, String mime, Runnable failure);
        void back();
    }

    private final Context context;
    private final Actions actions;
    private final LinearLayout layout;
    private PendingStatus status;
    private boolean saving;

    public LiveServiceCatalogView(Context context, Actions actions) {
        this.context = context;
        this.actions = actions;
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = UiTheme.dp(context, 16);
        layout.setPadding(padding, padding, padding, padding);
    }

    public LinearLayout view() { return layout; }

    public void load() {
        reset("Service catalog", "Back to Manage", actions::back);
        status.working("Loading service revisions…");
        PendingStatus loading = status;
        actions.request("", null, (data, error) -> {
            if (!layout.isAttachedToWindow() || status != loading) return;
            if (error != null) {
                status.result(error, true);
                button("Retry", this::load);
                return;
            }
            try {
                JSONArray rows = new JSONArray(data);
                status.clear();
                button("New service", () -> edit(null));
                if (rows.length() == 0) label("No service revisions yet.");
                for (int i = 0; i < rows.length(); i++) row(rows.getJSONObject(i));
            } catch (JSONException invalid) {
                status.result("CRM returned an invalid catalog. Try again.", true);
                button("Retry", this::load);
            }
        });
    }

    private void row(JSONObject revision) throws JSONException {
        int id = revision.getInt("id");
        String state = revision.getString("state");
        String title = revision.getJSONObject("details").optString("name", "Untitled service");
        label(title + " · " + revision.getString("code") + " · Version "
                + revision.getInt("version") + " · " + state);
        if ("draft".equals(state)) {
            button("Edit draft", () -> edit(revision));
            button(revision.isNull("form_name") ? "Upload blank form" : "Replace blank form", () ->
                    confirm("Upload form?", "This file will be saved on the draft revision.",
                            () -> actions.replaceForm(id, this::load, error -> status.result(error, true))));
            button("Publish", () -> confirm("Publish " + title + "?",
                    "This revision and its blank form will become public.", () -> change(id, "publish")));
        } else if ("published".equals(state) || "withdrawn".equals(state)) {
            button("Create revision", () -> edit(revision));
            if ("published".equals(state)) button("Withdraw", () -> confirm("Withdraw " + title + "?",
                    "This service and its form will be removed from the public directory.",
                    () -> change(id, "withdraw")));
        }
        if (!revision.isNull("form_name")) {
            button("Download " + revision.getString("form_name"), () ->
                    actions.downloadForm(id, revision.optString("form_name"), revision.optString("form_mime"),
                            () -> status.result("Unable to open the form. Try again.", true)));
        }
        button("Publication history", () -> history(id));
    }

    private void history(int id) {
        reset("Publication history", "Back to catalog", this::load);
        status.working("Loading publication history…");
        PendingStatus loading = status;
        actions.request("/" + id + "/history", null, (data, error) -> {
            if (!layout.isAttachedToWindow() || status != loading) return;
            if (error != null) {
                status.result(error, true);
                return;
            }
            try {
                JSONArray history = new JSONArray(data);
                status.clear();
                for (int i = 0; i < history.length(); i++) {
                    JSONObject event = history.getJSONObject(i);
                    label(event.getString("action").replace('_', ' ') + " · Staff "
                            + event.optString("actor_id", "—") + " · Revision "
                            + event.getInt("revision_id") + " · " + event.getString("recorded_at"));
                }
            } catch (JSONException invalid) {
                status.result("CRM returned invalid publication history. Try again.", true);
            }
        });
    }

    private void edit(JSONObject source) {
        boolean existingDraft = source != null && "draft".equals(source.optString("state"));
        JSONObject fields = source == null ? new JSONObject() : source.optJSONObject("details");
        reset(existingDraft ? "Edit service draft" : "New service draft", "Back to catalog", this::load);
        EditText code = input("Service code", source == null ? "" : source.optString("code"), false);
        code.setEnabled(source == null);
        EditText name = input("Service name", fields.optString("name"), false);
        EditText description = input("Brief description", fields.optString("description"), true);
        EditText purpose = input("Purpose", fields.optString("purpose"), true);
        EditText eligibility = input("Who can apply", fields.optString("eligibility"), true);
        String applicants = fields.optString("applicants");
        CheckBox student = check("Student researchers", applicants.contains("Student"));
        CheckBox faculty = check("Faculty researchers", applicants.contains("Faculty"));
        EditText contact = input("Responsible contact", fields.optString("contact_name"), false);
        EditText email = input("Contact email", fields.optString("contact_email"), false);
        EditText turnaround = input("Turnaround (optional)", fields.optString("turnaround"), false);
        CheckBox external = check("Submit through an external channel", "external".equals(fields.optString("channel")));
        EditText instructions = input("Instructions, one step per line", lines(fields.optJSONArray("instructions")), true);
        EditText requirements = input("Requirements, one per line. Prefix optional items with Optional:", requirementLines(fields.optJSONArray("requirements")), true);
        Button save = button("Save draft", null);
        save.setOnClickListener(view -> {
            if (!value(code).toUpperCase(java.util.Locale.ROOT).matches("[A-Z0-9][A-Z0-9-]{1,31}")) {
                code.setError("Enter a service code using 2 to 32 letters, digits or hyphens.");
                return;
            }
            try {
                JSONArray categories = new JSONArray();
                if (student.isChecked()) categories.put("Student");
                if (faculty.isChecked()) categories.put("Faculty");
                JSONArray steps = new JSONArray();
                for (String line : value(instructions).split("\n")) {
                    if (!line.isBlank()) steps.put(line.trim());
                }
                JSONArray checklist = new JSONArray();
                for (String line : value(requirements).split("\n")) {
                    String item = line.trim();
                    if (item.isEmpty()) continue;
                    boolean optional = item.regionMatches(true, 0, "Optional:", 0, 9);
                    String title = optional ? item.substring(9).trim() : item;
                    if (title.isEmpty()) continue;
                    checklist.put(new JSONObject().put("title", title)
                            .put("required", !optional));
                }
                JSONObject details = new JSONObject()
                        .put("channel", external.isChecked() ? "external" : "in_app")
                        .put("requirements", checklist);
                putInput(details, "name", name);
                putInput(details, "description", description);
                putInput(details, "purpose", purpose);
                putInput(details, "eligibility", eligibility);
                putInput(details, "contact_name", contact);
                putInput(details, "contact_email", email);
                putInput(details, "turnaround", turnaround);
                if (categories.length() > 0) details.put("applicants", categories);
                if (steps.length() > 0) details.put("instructions", steps);
                if (existingDraft) save("/" + source.optInt("id") + "/edit", details, save);
                else save("/drafts", new JSONObject().put("code", value(code).toUpperCase(java.util.Locale.ROOT))
                        .put("details", details), save);
            } catch (JSONException invalid) {
                status.result("Complete the highlighted service information before publishing.", true);
            }
        });
    }

    private void change(int id, String action) {
        try {
            save("/" + id + "/" + action, new JSONObject().put("confirmed", true), null);
        } catch (JSONException invalid) {
            status.result("Unable to prepare this change. Try again.", true);
        }
    }

    private void save(String path, JSONObject body, Button saveButton) {
        if (saving) return;
        saving = true;
        if (saveButton != null) saveButton.setEnabled(false);
        status.working("Saving service changes to CRM…");
        PendingStatus savingStatus = status;
        actions.request(path, body, (data, error) -> {
            saving = false;
            if (!layout.isAttachedToWindow() || status != savingStatus) return;
            if (saveButton != null) saveButton.setEnabled(true);
            if (error == null) load();
            else status.result(error, true);
        });
    }

    private void confirm(String title, String message, Runnable proceed) {
        new AlertDialog.Builder(context).setTitle(title).setMessage(message)
                .setNegativeButton("Cancel", null).setPositiveButton("Confirm", (dialog, which) -> proceed.run()).show();
    }

    private void reset(String heading, String backLabel, Runnable back) {
        layout.removeAllViews();
        button(backLabel, back);
        label(heading);
        status = new PendingStatus(context);
        layout.addView(status);
    }

    private void label(String caption) {
        TextView text = new TextView(context);
        text.setText(caption);
        text.setTextColor(UiTheme.NAVY);
        text.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(text, 16);
        text.setPadding(0, UiTheme.dp(context, 12), 0, UiTheme.dp(context, 6));
        layout.addView(text);
    }

    private Button button(String text, Runnable click) {
        Button button = UiButton.outline(context, text, UiTheme.BORDER, UiTheme.NAVY, click);
        layout.addView(button);
        return button;
    }

    private EditText input(String caption, String initial, boolean multiline) {
        label(caption);
        EditText field = new EditText(context);
        field.setContentDescription(caption);
        field.setSingleLine(!multiline);
        field.setText(initial);
        layout.addView(field);
        return field;
    }

    private CheckBox check(String label, boolean checked) {
        CheckBox check = new CheckBox(context);
        check.setText(label);
        check.setChecked(checked);
        layout.addView(check);
        return check;
    }

    private static String value(EditText field) { return field.getText().toString().trim(); }

    private static void putInput(JSONObject details, String key, EditText field) throws JSONException {
        String input = value(field);
        if (!input.isEmpty()) details.put(key, input);
    }

    private static String lines(JSONArray items) {
        java.util.StringJoiner text = new java.util.StringJoiner("\n");
        if (items != null) for (int i = 0; i < items.length(); i++) text.add(items.optString(i));
        return text.toString();
    }

    private static String requirementLines(JSONArray items) {
        java.util.StringJoiner text = new java.util.StringJoiner("\n");
        if (items != null) for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item != null) text.add((item.optBoolean("required") ? "" : "Optional: ") + item.optString("title"));
        }
        return text.toString();
    }
}
