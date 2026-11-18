package ph.edu.umak.crm.app.staff.queue;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

public final class LiveStaffRequestsView extends LinearLayout {
    public interface Actions {
        void request(String path, JSONObject body, BiConsumer<String, String> result);
        void download(String path, String filename, String mime, Runnable failure);
        void back();
    }

    private final Actions actions;
    private PendingStatus feedback;
    private String statusFilter = "";
    private String serviceFilter = "";
    private String fromFilter = "";
    private String toFilter = "";

    public LiveStaffRequestsView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        setOrientation(VERTICAL);
        int spacing = UiTheme.dp(context, 16);
        setPadding(spacing, spacing, spacing, spacing);
    }

    public void load() {
        reset("Service request review queue", "Back to staff workspace", actions::back);
        String[] statuses = {"All statuses", "Submitted", "In review", "For correction", "Completed"};
        Spinner status = choices("Status filter", statuses, statusFilter);
        EditText service = input("Service code", serviceFilter);
        EditText from = input("From date (YYYY-MM-DD)", fromFilter);
        EditText to = input("To date (YYYY-MM-DD)", toFilter);
        button("Apply filters", () -> {
            statusFilter = status.getSelectedItemPosition() == 0 ? "" : status.getSelectedItem().toString();
            serviceFilter = value(service);
            fromFilter = value(from);
            toFilter = value(to);
            load();
        });
        button("Clear filters", () -> {
            statusFilter = serviceFilter = fromFilter = toFilter = "";
            load();
        });
        loadPage(null);
    }

    private void loadPage(Integer before) {
        android.net.Uri.Builder query = new android.net.Uri.Builder();
        if (!statusFilter.isEmpty()) query.appendQueryParameter("status", statusFilter);
        if (!serviceFilter.isEmpty()) query.appendQueryParameter("service", serviceFilter);
        if (!fromFilter.isEmpty()) query.appendQueryParameter("from", fromFilter);
        if (!toFilter.isEmpty()) query.appendQueryParameter("to", toFilter);
        if (before != null) query.appendQueryParameter("before", before.toString());
        PendingStatus current = feedback;
        feedback.working("Loading requests…");
        actions.request(query.build().toString(), null, (data, error) -> {
            if (!isAttachedToWindow() || feedback != current) return;
            if (error != null) {
                current.result(error, true);
                return;
            }
            try {
                JSONArray rows = new JSONArray(data);
                current.clear();
                if (rows.length() == 0 && before == null) label("No matching requests");
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);
                    int id = row.getInt("id");
                    String requester = row.isNull("requester_name") ? row.optString("requester_email")
                            : row.optString("requester_name");
                    button("REQ-" + id + " · " + row.getString("service") + " · " + requester + " · "
                            + row.getString("status") + " · " + row.getString("submitted_at"), () -> detail(id));
                }
                if (rows.length() == 100) {
                    int last = rows.getJSONObject(99).getInt("id");
                    Button more = button("Load more", null);
                    more.setOnClickListener(view -> {
                        removeView(more);
                        loadPage(last);
                    });
                }
            } catch (JSONException invalid) {
                current.result("CRM returned invalid requests. Try again.", true);
            }
        });
    }

    private void detail(int id) {
        reset("Review request · REQ-" + id, "Back to queue", this::load);
        PendingStatus current = feedback;
        current.working("Loading request…");
        actions.request("/" + id, null, (data, error) -> {
            if (!isAttachedToWindow() || feedback != current) return;
            if (error != null) {
                current.result(error, true);
                button("Retry", () -> detail(id));
                return;
            }
            try {
                current.clear();
                showRequest(new JSONObject(data));
            } catch (JSONException invalid) {
                current.result("CRM returned invalid request details. Try again.", true);
            }
        });
    }

    private void showRequest(JSONObject request) throws JSONException {
        int id = request.getInt("id");
        String currentStatus = request.getString("status");
        label(request.getString("service") + " · " + currentStatus);
        label(request.getString("subject"));
        label(request.optString("description"));
        label("Requester · " + request.optString("requester_name", "—") + " · "
                + request.optString("requester_email") + " · " + request.optString("requester_college", "—"));
        JSONArray files = request.getJSONArray("attachments");
        label("Attached documents");
        for (int i = 0; i < files.length(); i++) {
            JSONObject file = files.getJSONObject(i);
            button(file.getString("filename"), () -> actions.download("/" + id + "/files/" + file.optString("id"),
                    file.optString("filename"), file.optString("mime"),
                    () -> feedback.result("Unable to open the file. Try again.", true)));
        }
        JSONArray reviews = request.getJSONArray("reviews");
        label("Review history");
        if (reviews.length() == 0) label("No previous reviews.");
        for (int i = 0; i < reviews.length(); i++) {
            JSONObject event = reviews.getJSONObject(i);
            label(event.optString("from_status") + " → " + event.optString("to_status")
                    + " · Staff " + event.optString("reviewer_id") + " · " + event.optString("reviewed_at"));
            if (!event.optString("feedback").isEmpty()) label(event.optString("feedback"));
            if (!event.optString("internal_remark").isEmpty()) label("Internal: " + event.optString("internal_remark"));
        }

        String[] nextStatuses = switch (currentStatus) {
            case "Submitted" -> new String[]{"In review"};
            case "In review" -> new String[]{"In review", "For correction", "Completed"};
            case "Completed" -> new String[]{"Completed"};
            default -> new String[0];
        };
        if (nextStatuses.length == 0) {
            label("Waiting for the researcher to resubmit.");
            return;
        }
        Spinner decision = choices("Decision", nextStatuses, nextStatuses[0]);
        JSONArray requirements = request.getJSONArray("requirements");
        JSONObject latestReview = reviews.optJSONObject(0);
        JSONArray latestChecklist = latestReview == null ? null : latestReview.optJSONArray("checklist");
        List<Spinner> checklist = new ArrayList<>();
        List<CheckBox> missing = new ArrayList<>();
        for (int i = 0; i < requirements.length(); i++) {
            String name = requirements.getJSONObject(i).getString("title");
            String previous = latestChecklist == null ? "Pending" : latestChecklist.optString(i, "Pending");
            checklist.add(choices(name, new String[]{"Pending", "Verified", "Needs correction", "Not applicable"}, previous));
            CheckBox item = new CheckBox(getContext());
            item.setText("Request a correction for " + name);
            addView(item);
            missing.add(item);
        }
        EditText publicFeedback = input("Researcher-visible feedback", "");
        EditText remarks = input("Internal staff remarks", "");
        EditText next = input("Next action", "");
        EditText deadline = input("Deadline, UTC (YYYY-MM-DDThh:mm:ssZ)", "");
        EditText checkpoint = input("Processing checkpoint", "");
        EditText checkpointDate = input("Checkpoint date (YYYY-MM-DD)", "");
        EditText reference = input("Supporting reference", "");
        EditText reason = "Completed".equals(currentStatus) ? input("Reason for correcting this completed request", "") : null;
        Button save = button("Save review", null);
        save.setOnClickListener(view -> {
            try {
                JSONArray checks = new JSONArray();
                for (Spinner item : checklist) checks.put(item.getSelectedItem().toString());
                JSONArray missingItems = new JSONArray();
                for (int i = 0; i < missing.size(); i++) if (missing.get(i).isChecked()) missingItems.put(i);
                JSONObject body = new JSONObject().put("expected_version", request.getInt("version"))
                        .put("status", decision.getSelectedItem().toString()).put("feedback", value(publicFeedback))
                        .put("internal_remark", value(remarks)).put("next_action", value(next))
                        .put("checklist", checks).put("missing_items", missingItems)
                        .put("deadline_at", value(deadline).isEmpty() ? JSONObject.NULL : value(deadline));
                if (reason != null) body.put("correction_reason", value(reason));
                if (!value(checkpoint).isEmpty()) body.put("checkpoint", new JSONObject().put("name", value(checkpoint))
                        .put("date", value(checkpointDate)).put("reference", value(reference)));
                save.setEnabled(false);
                PendingStatus current = feedback;
                current.working("Saving review…");
                actions.request("/" + id + "/reviews", body, (data, error) -> {
                    if (!isAttachedToWindow() || feedback != current) return;
                    save.setEnabled(true);
                    if (error == null) detail(id);
                    else {
                        current.result(error, true);
                        if (error.contains("Reload before saving")) {
                            save.setEnabled(false);
                            button("Reload request", () -> detail(id));
                        }
                    }
                });
            } catch (JSONException invalid) {
                feedback.result("Check the review information and try again.", true);
            }
        });
    }

    private Spinner choices(String label, String[] options, String selection) {
        label(label);
        Spinner spinner = new Spinner(getContext());
        spinner.setContentDescription(label);
        spinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, options));
        spinner.setSelection(Math.max(0, java.util.Arrays.asList(options).indexOf(selection)));
        addView(spinner);
        return spinner;
    }

    private void reset(String title, String back, Runnable callback) {
        removeAllViews();
        button(back, callback);
        label(title);
        feedback = new PendingStatus(getContext());
        addView(feedback);
    }

    private Button button(String text, Runnable click) {
        Button button = UiButton.outline(getContext(), text, UiTheme.BORDER, UiTheme.NAVY, click);
        addView(button);
        return button;
    }

    private void label(String text) {
        if (text.isEmpty()) return;
        TextView label = new TextView(getContext());
        label.setText(text);
        label.setTextColor(UiTheme.NAVY);
        addView(label);
    }

    private EditText input(String title, String initial) {
        label(title);
        EditText input = new EditText(getContext());
        input.setContentDescription(title);
        input.setText(initial);
        addView(input);
        return input;
    }

    private static String value(EditText field) { return field.getText().toString().trim(); }
}
