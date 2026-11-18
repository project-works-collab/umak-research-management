package ph.edu.umak.crm.app.staff.registry;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.MainActivity;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.research.RegisteredProgressView;

public final class LiveRegistryRecordView extends LinearLayout {
    public interface Actions {
        void request(String path, JSONObject body, BiConsumer<String, String> result);
        void choose(Runnable checking, BiConsumer<MainActivity.SelectedAttachment, String> result);
        void upload(MainActivity.SelectedAttachment file, int study, String previous, String token, BiConsumer<String, String> result);
        void download(String path, String name, String mime, Runnable failure);
        void back();
    }
    private final Actions actions;
    private PendingStatus status;
    private boolean saving;
    public LiveRegistryRecordView(Context context, Actions actions) {
        super(context); this.actions = actions; setOrientation(VERTICAL);
        int padding = UiTheme.dp(context, 16); setPadding(padding, padding, padding, padding);
    }

    public void load(int id) {
        reset("Research registry record", actions::back);
        PendingStatus current = status; current.working("Loading record…");
        actions.request("/" + id, null, (data, error) -> {
            if (!isAttachedToWindow() || status != current) return;
            if (error != null) { current.result(error, true); return; }
            try { current.clear(); show(new JSONObject(data)); }
            catch (JSONException invalid) { current.result("CRM returned invalid research details.", true); }
        });
    }

    private void show(JSONObject record) throws JSONException {
        int id = record.getInt("id");
        line(record.getString("research_id") + " · " + record.getString("title"), true);
        line("College: " + known(record, "college"), false);
        line("Academic period: " + known(record, "academic_period"), false);
        line("Adviser: " + known(record, "adviser"), false);
        line("Recorded registrant category: " + record.optString("registrant_category"), false);
        line("Historical entry: " + record.optBoolean("historical"), false);
        if (record.optBoolean("historical")) {
            line("Source reference: " + known(record, "source_reference"), false);
            line("Original registration date: " + known(record, "original_registration_date"), false);
        }
        line("Encoded on: " + record.optString("encoded_at"), false);
        JSONArray team = record.getJSONArray("members");
        line("Research team", true);
        for (int i = 0; i < team.length(); i++) line(team.getJSONObject(i).getString("name"), false);
        button("Correct record", () -> edit(record));
        button("Documents and versions", () -> documents(id));
        button("Correction history", () -> history(record));
        line("Linked service requests", true);
        JSONArray requests = record.getJSONArray("linked_requests");
        if (requests.length() == 0) line("No linked service requests.", false);
        for (int i = 0; i < requests.length(); i++) {
            JSONObject request = requests.getJSONObject(i);
            line("REQ-" + request.getInt("id") + " · " + request.getString("service") + " · " + request.getString("status"), false);
        }
    }

    public void create() { edit(null); }

    private void edit(JSONObject record) {
        reset(record == null ? "Add research entry" : "Correct research record", record == null ? actions::back : () -> load(record.optInt("id")));
        EditText title = input("Research title", record == null ? "" : record.optString("title"));
        EditText college = input("College or institute", record == null ? "" : knownInput(record, "college"));
        EditText period = input("Academic period (AY 2026-2027)", record == null ? "" : knownInput(record, "academic_period"));
        EditText adviser = input("Adviser", record == null ? "" : knownInput(record, "adviser"));
        EditText abstractText = input("Abstract", record == null ? "" : record.optString("abstract"));
        CheckBox historical = new CheckBox(getContext()); historical.setText("Historical entry");
        historical.setChecked(record != null && record.optBoolean("historical")); addView(historical);
        EditText source = input("Source reference", record == null ? "" : knownInput(record, "source_reference"));
        EditText originalDate = input("Original registration date (leave blank if unknown)", record == null ? "" : knownInput(record, "original_registration_date"));
        EditText reason = input("Correction or entry reason", "");
        CheckBox duplicate = new CheckBox(getContext()); duplicate.setText("Create a separate record despite a confirmed possible duplicate"); addView(duplicate);
        EditText duplicateReason = input("Reason for creating a separate record", "");
        List<JSONObject> members = new ArrayList<>();
        JSONArray existing = record == null ? new JSONArray() : record.optJSONArray("members");
        if (existing != null) for (int i = 0; i < existing.length(); i++) members.add(existing.optJSONObject(i));
        LinearLayout team = new LinearLayout(getContext()); team.setOrientation(VERTICAL); addView(team);
        Spinner registrant = new Spinner(getContext()); registrant.setContentDescription("Registrant");
        if (record == null) { line("Registrant", true); addView(registrant); }
        Runnable refresh = () -> {
            team.removeAllViews();
            ArrayList<String> names = new ArrayList<>();
            for (JSONObject member : members) {
                names.add(member.optString("name"));
                Button remove = UiButton.outline(getContext(), member.optString("name") + " · Remove", UiTheme.BORDER, UiTheme.NAVY, () -> {});
                remove.setOnClickListener(view -> { members.remove(member); refreshTeam(team, members, registrant, record == null ? 0 : record.optInt("registrant_id")); });
                if (record != null && member.optInt("id") == record.optInt("registrant_id")) remove.setEnabled(false);
                team.addView(remove);
            }
            registrant.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names));
        };
        refresh.run();
        button("Add existing researcher", () -> {
            EditText email = new EditText(getContext()); email.setHint("Institutional email");
            var dialog = new AlertDialog.Builder(getContext()).setTitle("Find researcher").setView(email)
                    .setNegativeButton("Cancel", null).setPositiveButton("Find", null).create();
            dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                Button find = dialog.getButton(AlertDialog.BUTTON_POSITIVE); find.setEnabled(false);
                String query = new android.net.Uri.Builder().appendQueryParameter("email", value(email)).build().toString();
                actions.request("/candidate" + query, null, (data, error) -> {
                    if (!dialog.isShowing()) return;
                    find.setEnabled(true);
                    if (error != null) { email.setError(error); return; }
                    try {
                        JSONObject selected = new JSONObject(data);
                        if (members.stream().anyMatch(person -> person.optInt("id") == selected.optInt("id"))) {
                            email.setError("This researcher is already a team member."); return;
                        }
                        members.add(selected); dialog.dismiss(); refresh.run();
                    } catch (JSONException invalid) { email.setError("CRM returned invalid account details."); }
                });
            }));
            dialog.show();
        });
        String token = UUID.randomUUID().toString();
        Button save = button(record == null ? "Create record" : "Save correction", null);
        save.setOnClickListener(view -> {
            if (saving) return;
            if (members.isEmpty()) { status.result("Select existing Researcher accounts.", true); return; }
            if (value(title).isEmpty()) { title.setError("Please enter the research title."); return; }
            try {
                JSONArray ids = new JSONArray(); for (JSONObject member : members) ids.put(member.optInt("id"));
                int owner = record == null ? members.get(registrant.getSelectedItemPosition()).optInt("id") : record.optInt("registrant_id");
                JSONObject body = new JSONObject().put("title", value(title)).put("college", value(college))
                        .put("academic_period", value(period)).put("adviser", value(adviser)).put("abstract", value(abstractText))
                        .put("registrant_id", owner).put("member_ids", ids).put("historical", historical.isChecked())
                        .put("source_reference", value(source)).put("original_registration_date", value(originalDate).isEmpty() ? JSONObject.NULL : value(originalDate))
                        .put("reason", value(reason)).put("duplicate_confirmed", duplicate.isChecked())
                        .put("duplicate_reason", value(duplicateReason)).put("submission_token", token);
                if (record != null) body.put("expected_version", record.optInt("version"));
                saving = true; save.setEnabled(false); PendingStatus current = status; current.working("Saving registry record…");
                actions.request(record == null ? "" : "/" + record.optInt("id") + "/corrections", body, (data, error) -> {
                    saving = false;
                    if (!isAttachedToWindow() || current != status) return;
                    if (error != null) {
                        current.result(error, true);
                        if (error.contains("Reload before saving")) button("Reload record", () -> load(record.optInt("id")));
                        else save.setEnabled(true);
                        return;
                    }
                    try { load(new JSONObject(data).getInt("id")); }
                    catch (JSONException invalid) { save.setEnabled(true); current.result("CRM could not confirm the registry change.", true); }
                });
            } catch (JSONException invalid) { status.result("Review the registry information.", true); }
        });
    }

    private void refreshTeam(LinearLayout team, List<JSONObject> members, Spinner registrant, int owner) {
        team.removeAllViews();
        ArrayList<String> names = new ArrayList<>();
        for (JSONObject member : members) {
            names.add(member.optString("name"));
            Button remove = UiButton.outline(getContext(), member.optString("name") + " · Remove", UiTheme.BORDER, UiTheme.NAVY, () -> {
                members.remove(member); refreshTeam(team, members, registrant, owner);
            });
            if (member.optInt("id") == owner) remove.setEnabled(false);
            team.addView(remove);
        }
        registrant.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void documents(int id) {
        reset("Documents and versions", () -> load(id));
        button("Attach research document", () -> upload(id, null));
        PendingStatus current = status; current.working("Loading document versions…");
        actions.request("/" + id + "/documents", null, (data, error) -> {
            if (!isAttachedToWindow() || status != current) return;
            if (error != null) { current.result(error, true); return; }
            try {
                JSONArray files = new JSONArray(data); current.clear();
                if (files.length() == 0) line("No research documents attached.", false);
                for (int i = 0; i < files.length(); i++) {
                    JSONObject file = files.getJSONObject(i);
                    String fileId = file.getString("id");
                    button(file.getString("filename") + " · Version " + file.getInt("version"), () -> actions.download(
                            "/" + id + "/documents/" + fileId, file.optString("filename"), file.optString("mime"),
                            () -> status.result("Unable to open the document. Try again.", true)));
                    button("Upload next version", () -> upload(id, fileId));
                }
            } catch (JSONException invalid) { current.result("CRM returned invalid document versions.", true); }
        });
    }

    private void upload(int study, String previous) {
        actions.choose(() -> status.working("Checking document…"), (file, error) -> {
            if (error != null) { status.result(error, true); return; }
            String token = UUID.randomUUID().toString();
            performUpload(file, study, previous, token);
        });
    }
    private void performUpload(MainActivity.SelectedAttachment file, int study, String previous, String token) {
        if (saving) return;
        saving = true; PendingStatus current = status; current.working("Uploading research document…");
        actions.upload(file, study, previous, token, (data, error) -> {
            saving = false;
            if (!isAttachedToWindow() || status != current) return;
            if (error == null) documents(study);
            else { current.result(error, true); button("Retry upload", () -> performUpload(file, study, previous, token)); }
        });
    }
    private void history(JSONObject record) {
        reset("Registry correction history", () -> load(record.optInt("id")));
        JSONArray events = record.optJSONArray("history");
        if (events == null || events.length() == 0) line("No corrections recorded.", false);
        else for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            line("Staff " + event.optString("actor_id") + " · " + event.optString("recorded_at"), true);
            line(event.optString("reason"), false); line("Previous: " + event.optString("before_value"), false);
            line("Saved: " + event.optString("after_value"), false);
        }
    }
    private void reset(String title, Runnable back) {
        removeAllViews(); button("Back", back); line(title, true); status = new PendingStatus(getContext()); addView(status);
    }
    private void line(String content, boolean heading) { RegisteredProgressView.line(this, content, heading); }
    private EditText input(String title, String initial) {
        line(title, false); EditText field = new EditText(getContext()); field.setText(initial); field.setContentDescription(title); addView(field); return field;
    }
    private Button button(String title, Runnable action) {
        Button button = UiButton.outline(getContext(), title, UiTheme.BORDER, UiTheme.NAVY, action); addView(button); return button;
    }
    private static String value(EditText field) { return field.getText().toString().trim(); }
    private static String knownInput(JSONObject record, String key) { return record.isNull(key) ? "" : record.optString(key); }
    private static String known(JSONObject record, String key) { String value = knownInput(record, key); return value.isEmpty() ? "Unknown" : value; }
}
