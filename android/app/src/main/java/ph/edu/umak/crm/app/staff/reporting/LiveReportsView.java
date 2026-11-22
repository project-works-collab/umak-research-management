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
        default void openRecord(String id) {}
        default void openRequest(String id) {}
    }
    private final Actions actions;
    private boolean research = true;
    private final Map<String, String> filters = new LinkedHashMap<>();
    private PendingStatus status;
    private boolean loading;
    private boolean showingReport;
    private JSONObject snapshot;
    private LinearLayout filterBody;
    public LiveReportsView(Context context, Actions actions) {
        super(context); this.actions = actions; setOrientation(VERTICAL);
    }

    public boolean handleBack() {
        if (!showingReport) return false;
        load(); return true;
    }

    public void load() {
        showingReport = false;
        removeAllViews();
        var page=new ph.edu.umak.crm.app.staff.common.StaffDesignPage(getContext(),"Analytics & Reporting","Back to Manage",actions::back);
        addView(page);filterBody=page.section("Report filters");
        button("Back to Manage", actions::back);
        button(research ? "Service utilization report" : "Research report", () -> {
            if (loading) return;
            research = !research; filters.clear(); load();
        });
        RegisteredProgressView.line(filterBody, research ? "Research Report" : "Service Utilization", true);
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
        status = new PendingStatus(getContext()); filterBody.addView(status);
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
                    if (current != status) return;
                    generate.setEnabled(true);
                    if (error != null) {
                        if(error.toLowerCase(java.util.Locale.ROOT).contains("access denied")){removeAllViews();addView(new AccessDeniedReportingView((android.app.Activity)getContext(),actions::back).getContentLayout());showingReport=true;}
                        else current.result(error,true);return;
                    }
                    try {
                        JSONObject report = new JSONObject(data); current.clear();
                        showReport(results, report, request);
                    } catch (JSONException invalid) { current.result("CRM returned invalid report data. Try again.", true); }
                });
            } catch (JSONException invalid) { status.result("Review the report filters.", true); }
        });
        button("Clear filters", () -> { if (!loading) { filters.clear(); load(); } });
        filterBody.addView(results);
    }

    private void showReport(LinearLayout panel, JSONObject report, JSONObject request) throws JSONException {
        var researchModel=research?LiveReportData.research(report):null;
        var serviceModel=research?null:LiveReportData.service(report);
        snapshot = new JSONObject(report.toString());
        removeAllViews();
        showingReport = true;
        if (research) {
            ResearchReportView design = new ResearchReportView((android.app.Activity)getContext(), new ResearchReportView.Callbacks() {
                public void onBackToFilters() { load(); }
                public void onOpenRecord(String id) { actions.openRecord(id); }
                public boolean onExport() { exportDisplayed(request); return true; }
            });
            design.setModel(researchModel);
            addView(design.getContentLayout());
        } else {
            ServiceUtilizationView design = new ServiceUtilizationView((android.app.Activity)getContext(), new ServiceUtilizationView.Callbacks() {
                public void onBackToFilters() { load(); }
                public void onOpenRequest(StaffReportingData.RequestSummary request) { actions.openRequest(request.id); }
                public boolean onExport() { exportDisplayed(request); return true; }
            });
            design.setModel(serviceModel);
            addView(design.getContentLayout());
        }
    }

    private void exportDisplayed(JSONObject request){
        if(loading)return;loading=true;JSONObject displayed=snapshot;
        // Recheck current report permission without replacing the displayed snapshot.
        actions.request(request,(data,error)->{
            loading=false;if(!showingReport||snapshot!=displayed)return;
            if(error==null)ExportReportDialog.showLiveReport(getContext(),displayed);
            else new android.app.AlertDialog.Builder(getContext()).setTitle("Export unavailable").setMessage(error).setPositiveButton("Close",null).show();
        });
    }

    private void input(Map<String, EditText> fields, String key, String title) {
        RegisteredProgressView.line(filterBody, title, false); EditText field = new EditText(getContext());
        field.setContentDescription(title); field.setText(filters.getOrDefault(key, "")); ph.edu.umak.crm.app.staff.common.StaffDesignPage.style(field,false); filterBody.addView(field); fields.put(key, field);
    }
    private void choice(Map<String, Spinner> fields, String key, String title, String[] options) {
        RegisteredProgressView.line(filterBody, title, false); Spinner field = new Spinner(getContext());
        field.setContentDescription(title); field.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, options));
        String value = filters.getOrDefault(key, "");
        if ("In-Progress".equals(value)) value = "In progress";
        if ("UnderReview".equals(value)) value = "For revision";
        for (int i = 0; i < options.length; i++) if (options[i].equalsIgnoreCase(value)) field.setSelection(i);
        field.setMinimumHeight(UiTheme.dp(getContext(),48));filterBody.addView(field); fields.put(key, field);
    }
    private Button button(String title, Runnable action) {
        Button button = UiButton.outline(getContext(), title, UiTheme.BORDER, UiTheme.NAVY, action); filterBody.addView(button); return button;
    }
}
