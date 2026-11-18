package ph.edu.umak.crm.app.staff.reporting;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.staff.reporting.StaffReportingData.ResearchReportModel;
import ph.edu.umak.crm.app.staff.reporting.StaffReportingData.ServiceUtilizationModel;

final class ReportExportData {
    final String type;
    final String timestamp;
    final List<List<Object>> rows = new ArrayList<>();

    private ReportExportData(String type, String title, String timestamp) {
        this.type = type;
        this.timestamp = timestamp;
        add(title);
        if (timestamp != null) add(timestamp);
    }

    static ReportExportData fromLive(org.json.JSONObject report) throws org.json.JSONException {
        ReportExportData data = new ReportExportData(report.getString("type") + "-report",
                report.getString("title"), report.getString("generated_at"));
        data.add("Applied filters");
        org.json.JSONObject filters = report.getJSONObject("filters");
        for (var keys = filters.keys(); keys.hasNext();) {
            String key = keys.next(); data.add(key, filters.getString(key));
        }
        for (String section : new String[]{"metrics", "service_counts"}) {
            data.add();
            data.add(section.equals("metrics") ? "Metric" : "Service", "Count");
            org.json.JSONArray items = report.getJSONArray(section);
            for (int i = 0; i < items.length(); i++) {
                org.json.JSONArray item = items.getJSONArray(i); data.add(item.getString(0), item.getInt(1));
            }
        }
        data.add(); data.add("Metric definitions");
        org.json.JSONArray definitions = report.getJSONArray("definitions");
        for (int i = 0; i < definitions.length(); i++) data.add(definitions.getString(i));
        org.json.JSONArray columns = report.getJSONArray("columns");
        data.add(); data.add(columns.getString(0), columns.getString(1), columns.getString(2));
        org.json.JSONArray rows = report.getJSONArray("rows");
        for (int i = 0; i < rows.length(); i++) {
            org.json.JSONArray row = rows.getJSONArray(i); data.add(row.getString(0), row.getString(1), row.getString(2));
        }
        return data;
    }

    static ReportExportData from(ResearchReportModel model) {
        ReportExportData data = new ReportExportData("research-report", "Research Report", model.generatedTimestamp);
        data.addFilters(model.collegeFilter, model.registrantCategoryFilter, model.stageFilter,
                model.linkedServicesFilter, model.academicPeriodFilter, model.outcomeFilter);
        data.add();
        data.add("Metric", "Count");
        data.add("Distinct studies", model.distinctStudies);
        data.add("Completed", model.completed);
        data.add("Presented", model.presented);
        data.add("Published", model.published);
        data.add("Distinct linked researchers", model.distinctLinkedResearchers);
        data.add();
        data.add("Metric definitions");
        data.add("Each research record is counted once.");
        data.add("Outcome counts are independent. One study can count in more than one.");
        data.add("Each linked researcher or adviser is counted once across all studies.");
        data.add();
        data.add("Record ID", "Title", "Status");
        for (StaffReportingData.RecordSummary record : model.underlyingRecords) {
            data.add(record.id, record.title, record.status);
        }
        return data;
    }

    static ReportExportData from(ServiceUtilizationModel model) {
        ReportExportData data = new ReportExportData("service-utilization", "Service Utilization", model.generatedTimestamp);
        data.addFilters(model.requesterCollegeFilter, model.submittedPeriodFilter, model.serviceFilter,
                model.statusFilter, model.requesterCategoryFilter);
        data.add();
        data.add("Metric", "Count");
        data.add("Distinct requests", model.distinctRequests);
        data.add("Completed", model.completed);
        data.add("Pending", model.pendingValue, model.pendingNote);
        data.add();
        data.add("Service", "Request count");
        for (StaffReportingData.ServiceCountItem item : model.requestsPerService) {
            data.add(item.serviceName, item.requestCount);
        }
        data.add();
        data.add("Request ID", "Service", "Status");
        for (StaffReportingData.RequestSummary request : model.underlyingRequests) {
            data.add(request.id, request.serviceName, request.status);
        }
        return data;
    }

    static ReportExportData directory(List<ph.edu.umak.crm.app.staff.registry.StaffRegistryData.RegistryRecordItem> records,
                                      String college, String period) {
        ReportExportData data = new ReportExportData("research-directory", "Research Directory", null);
        data.addFilters(college, period);
        data.add();
        data.add("Research ID", "Title", "Stage", "College", "Academic period");
        for (var record : records) {
            data.add(record.id, record.title, record.stage, record.college, record.academicPeriod);
        }
        return data;
    }

    private void addFilters(String... filters) {
        add();
        add("Applied filters");
        for (String filter : filters) {
            if (filter != null) add(filter);
        }
    }

    private void add(Object... values) {
        List<Object> row = new ArrayList<>();
        for (Object value : values) row.add(value == null ? "" : value);
        rows.add(row);
    }
}
