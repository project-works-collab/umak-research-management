package ph.edu.umak.crm.app.staff.reporting;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.research.RegisteredProgressView;

/** A single server snapshot supplies the designed report and its exported files. */
public final class LiveReportData {
    private LiveReportData() {}
    public static StaffReportingData.ResearchReportModel research(JSONObject report) throws JSONException {
        if (!"research".equals(report.getString("type"))) throw new JSONException("Wrong report type");
        JSONObject filters = report.getJSONObject("filters");
        List<StaffReportingData.RecordSummary> records = new ArrayList<>();
        JSONArray rows = report.getJSONArray("rows");
        for (int i=0;i<rows.length();i++) {
            JSONArray row=rows.getJSONArray(i);
            records.add(new StaffReportingData.RecordSummary(row.getString(0), row.getString(1), RegisteredProgressView.stageLabel(row.getString(2))));
        }
        var model = new StaffReportingData.ResearchReportModel(filter(filters,"college"), filter(filters,"category"),
                filter(filters,"stage"), filter(filters,"service"), filter(filters,"academic_period"), filter(filters,"outcome"),
                "Generated " + ph.edu.umak.crm.app.staff.common.StaffDesignPage.timestamp(report.getString("generated_at")), metric(report,"Distinct studies"), metric(report,"Presented"),
                metric(report,"Distinct linked researchers"), metric(report,"Completed"), metric(report,"Published"), records);
        model.definitions=definitions(report);
        model.extraFilters=new ArrayList<>();for(String key:new String[]{"researcher","adviser"})if(!"All".equals(filters.optString(key,"All")))model.extraFilters.add(filter(filters,key));
        return model;
    }
    public static StaffReportingData.ServiceUtilizationModel service(JSONObject report) throws JSONException {
        if (!"service".equals(report.getString("type"))) throw new JSONException("Wrong report type");
        JSONObject filters=report.getJSONObject("filters");
        List<StaffReportingData.RequestSummary> requests=new ArrayList<>();
        JSONArray rows=report.getJSONArray("rows");
        for(int i=0;i<rows.length();i++) {
            JSONArray row=rows.getJSONArray(i);
            requests.add(new StaffReportingData.RequestSummary(row.getString(0),row.getString(1),row.getString(2)));
        }
        List<StaffReportingData.ServiceCountItem> counts=new ArrayList<>();
        JSONArray services=report.getJSONArray("service_counts");
        for(int i=0;i<services.length();i++) counts.add(new StaffReportingData.ServiceCountItem(services.getJSONArray(i).getString(0),services.getJSONArray(i).getInt(1)));
        var model=new StaffReportingData.ServiceUtilizationModel(filter(filters,"college"),
                "Submitted: " + filters.optString("from","All") + " – " + filters.optString("to","All"),
                filter(filters,"service"),filter(filters,"status"),filter(filters,"category"),"Generated " + ph.edu.umak.crm.app.staff.common.StaffDesignPage.timestamp(report.getString("generated_at")),
                metric(report,"Distinct requests"),String.valueOf(metric(report,"Pending")),
                "Submitted, In review and For correction",metric(report,"Completed"),counts,requests);
        model.definitions=definitions(report);
        return model;
    }
    private static int metric(JSONObject report,String label)throws JSONException {
        JSONArray metrics=report.getJSONArray("metrics");
        for(int i=0;i<metrics.length();i++) if(label.equals(metrics.getJSONArray(i).getString(0)))return metrics.getJSONArray(i).getInt(1);
        throw new JSONException("Missing metric: " + label);
    }
    private static List<String> definitions(JSONObject report)throws JSONException {
        List<String> result=new ArrayList<>();JSONArray values=report.getJSONArray("definitions");
        for(int i=0;i<values.length();i++)result.add(values.getString(i));return result;
    }
    private static String filter(JSONObject filters,String key) { return key.replace('_',' ') + ": " + filters.optString(key,"All"); }
}
