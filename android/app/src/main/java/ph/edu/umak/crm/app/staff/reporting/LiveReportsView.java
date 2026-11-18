package ph.edu.umak.crm.app.staff.reporting;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.research.RegisteredProgressView;

public final class LiveReportsView extends LinearLayout {
    public interface Actions {
        void request(JSONObject filters, BiConsumer<String, String> result);
        void back();
    }
    private final Actions actions;
    private boolean research = true;
    private final Map<String, String> filters = new LinkedHashMap<>();
    private PendingStatus status;
    private boolean loading;
    public LiveReportsView(Context context, Actions actions) {
        super(context); this.actions = actions; setOrientation(VERTICAL);
        int padding = UiTheme.dp(context, 16); setPadding(padding, padding, padding, padding);
    }

    public void load() {
        removeAllViews();
        button("Back to Manage", actions::back);
        button(research ? "Service utilization report" : "Research report", () -> {
            if (loading) return;
            research = !research; filters.clear(); load();
        });
        RegisteredProgressView.line(this, research ? "Research Report" : "Service Utilization", true);
        Map<String, EditText> inputs = new LinkedHashMap<>();
        Map<String, Spinner> choices = new LinkedHashMap<>();
        input(inputs, "college", "College or institute");
        if (research) {
            input(inputs, "academic_period", "Academic period (AY 2026-2027)");
            choice(choices, "stage", "Current stage", new String[]{"All", "Registered", "In progress", "For revision", "Completed"});
            choice(choices, "outcome", "Outcome", new String[]{"All", "completed", "presented", "published"});
            input(inputs, "researcher", "Researcher name or account ID");
            input(inputs, "adviser", "Adviser");
        } else {
            input(inputs, "from", "Submitted from (YYYY-MM-DD)");
            input(inputs, "to", "Submitted to (YYYY-MM-DD)");
            choice(choices, "status", "Request status", new String[]{"All", "Submitted", "In review", "For correction", "Completed"});
        }
        input(inputs, "service", research ? "Linked service code" : "Service code");
        choice(choices, "category", research ? "Registrant category" : "Requester category", new String[]{"All", "Student", "Faculty"});
        status = new PendingStatus(getContext()); addView(status);
        LinearLayout results = new LinearLayout(getContext()); results.setOrientation(VERTICAL);
        Button generate = button("Generate report", null);
        generate.setOnClickListener(view -> {
            if (loading) return;
            try {
                JSONObject request = new JSONObject().put("type", research ? "research" : "service");
                filters.clear();
                for (var field : inputs.entrySet()) {
                    String value = field.getValue().getText().toString().trim();
                    if (!value.isEmpty()) { request.put(field.getKey(), value); filters.put(field.getKey(), value); }
                }
                for (var field : choices.entrySet()) {
                    String value = field.getValue().getSelectedItemPosition() == 0 ? "" : field.getValue().getSelectedItem().toString();
                    if ("category".equals(field.getKey())) value = value.toLowerCase(java.util.Locale.ROOT);
                    if ("In progress".equals(value)) value = "In-Progress";
                    if ("For revision".equals(value)) value = "UnderReview";
                    if (!value.isEmpty()) { request.put(field.getKey(), value); filters.put(field.getKey(), value); }
                }
                loading = true; generate.setEnabled(false); results.removeAllViews();
                PendingStatus current = status; current.working("Generating report…");
                actions.request(request, (data, error) -> {
                    loading = false;
                    if (!isAttachedToWindow() || current != status) return;
                    generate.setEnabled(true);
                    if (error != null) { current.result(error, true); return; }
                    try {
                        JSONObject report = new JSONObject(data); current.clear();
                        showReport(results, report, request);
                    } catch (JSONException invalid) { current.result("CRM returned invalid report data. Try again.", true); }
                });
            } catch (JSONException invalid) { status.result("Review the report filters.", true); }
        });
        button("Clear filters", () -> { if (!loading) { filters.clear(); load(); } });
        addView(results);
    }

    private void showReport(LinearLayout panel, JSONObject report, JSONObject request) throws JSONException {
        RegisteredProgressView.line(panel, "Generated " + report.getString("generated_at"), false);
        JSONObject applied = report.getJSONObject("filters");
        for (var keys = applied.keys(); keys.hasNext();) {
            String key = keys.next();
            RegisteredProgressView.line(panel, key.replace('_', ' ') + ": " + applied.getString(key), false);
        }
        JSONArray metrics = report.getJSONArray("metrics");
        for (int i = 0; i < metrics.length(); i++) {
            JSONArray metric = metrics.getJSONArray(i);
            RegisteredProgressView.line(panel, metric.getString(0) + ": " + metric.getInt(1), true);
        }
        if (report.getBoolean("empty")) RegisteredProgressView.line(panel, "No matching records", true);
        JSONArray definitions = report.getJSONArray("definitions");
        RegisteredProgressView.line(panel, "Metric definitions", true);
        for (int i = 0; i < definitions.length(); i++) RegisteredProgressView.line(panel, definitions.getString(i), false);
        JSONArray counts = report.getJSONArray("service_counts");
        for (int i = 0; i < counts.length(); i++) {
            JSONArray row = counts.getJSONArray(i);
            RegisteredProgressView.line(panel, row.getString(0) + " · " + row.getInt(1) + " requests", false);
        }
        Button export = UiButton.outline(getContext(), "Export report", UiTheme.BORDER, UiTheme.NAVY,
                () -> actions.request(request, (fresh, error) -> {
                    if (!panel.isAttachedToWindow()) return;
                    if (error != null) { status.result(error, true); return; }
                    ExportReportDialog.showLiveReport(getContext(), report);
                }));
        panel.addView(export);
        JSONArray rows = report.getJSONArray("rows");
        RegisteredProgressView.line(panel, "Underlying records", true);
        for (int i = 0; i < rows.length(); i++) {
            JSONArray row = rows.getJSONArray(i);
            RegisteredProgressView.line(panel, row.getString(0) + " · " + row.getString(1) + " · " + row.getString(2), false);
        }
    }

    private void input(Map<String, EditText> fields, String key, String title) {
        RegisteredProgressView.line(this, title, false); EditText field = new EditText(getContext());
        field.setContentDescription(title); field.setText(filters.getOrDefault(key, "")); addView(field); fields.put(key, field);
    }
    private void choice(Map<String, Spinner> fields, String key, String title, String[] options) {
        RegisteredProgressView.line(this, title, false); Spinner field = new Spinner(getContext());
        field.setContentDescription(title); field.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, options));
        String value = filters.getOrDefault(key, "");
        if ("In-Progress".equals(value)) value = "In progress";
        if ("UnderReview".equals(value)) value = "For revision";
        for (int i = 0; i < options.length; i++) if (options[i].equalsIgnoreCase(value)) field.setSelection(i);
        addView(field); fields.put(key, field);
    }
    private Button button(String title, Runnable action) {
        Button button = UiButton.outline(getContext(), title, UiTheme.BORDER, UiTheme.NAVY, action); addView(button); return button;
    }
}
