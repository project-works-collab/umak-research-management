package ph.edu.umak.crm.app.staff.reporting;

import java.util.ArrayList;
import java.util.List;

/**
 * Data models and sample datasets for Staff Reporting & Analytics (Part 9, C-05).
 * Zero emojis compliant.
 */
public final class StaffReportingData {
    private StaffReportingData() {}

    public static class RecordSummary {
        public final String id;
        public final String title;
        public final String status; // "In progress", "Completed", etc.

        public RecordSummary(String id, String title, String status) {
            this.id = id;
            this.title = title;
            this.status = status;
        }
    }

    public static class RequestSummary {
        public final String id;
        public final String serviceName;
        public final String status; // "Completed", "In review", etc.

        public RequestSummary(String id, String serviceName, String status) {
            this.id = id;
            this.serviceName = serviceName;
            this.status = status;
        }
    }

    public static class ServiceCountItem {
        public final String serviceName;
        public final int requestCount;

        public ServiceCountItem(String serviceName, int requestCount) {
            this.serviceName = serviceName;
            this.requestCount = requestCount;
        }
    }

    public static class ResearchReportModel {
        public final String collegeFilter;
        public final String registrantCategoryFilter;
        public final String stageFilter;
        public final String linkedServicesFilter;
        public final String academicPeriodFilter;
        public final String outcomeFilter;
        public final String generatedTimestamp;

        public final int distinctStudies;
        public final int presented;
        public final int distinctLinkedResearchers;
        public final int completed;
        public final int published;

        public final List<RecordSummary> underlyingRecords;

        public ResearchReportModel(
                String collegeFilter,
                String registrantCategoryFilter,
                String stageFilter,
                String linkedServicesFilter,
                String academicPeriodFilter,
                String outcomeFilter,
                String generatedTimestamp,
                int distinctStudies,
                int presented,
                int distinctLinkedResearchers,
                int completed,
                int published,
                List<RecordSummary> underlyingRecords) {
            this.collegeFilter = collegeFilter;
            this.registrantCategoryFilter = registrantCategoryFilter;
            this.stageFilter = stageFilter;
            this.linkedServicesFilter = linkedServicesFilter;
            this.academicPeriodFilter = academicPeriodFilter;
            this.outcomeFilter = outcomeFilter;
            this.generatedTimestamp = generatedTimestamp;
            this.distinctStudies = distinctStudies;
            this.presented = presented;
            this.distinctLinkedResearchers = distinctLinkedResearchers;
            this.completed = completed;
            this.published = published;
            this.underlyingRecords = underlyingRecords != null ? underlyingRecords : new ArrayList<>();
        }
    }

    public static class ServiceUtilizationModel {
        public final String requesterCollegeFilter;
        public final String submittedPeriodFilter;
        public final String serviceFilter;
        public final String statusFilter;
        public final String requesterCategoryFilter;
        public final String generatedTimestamp;

        public final int distinctRequests;
        public final String pendingValue; // "—"
        public final String pendingNote; // "Definition pending CRM confirmation"
        public final int completed;

        public final List<ServiceCountItem> requestsPerService;
        public final List<RequestSummary> underlyingRequests;

        public ServiceUtilizationModel(
                String requesterCollegeFilter,
                String submittedPeriodFilter,
                String serviceFilter,
                String statusFilter,
                String requesterCategoryFilter,
                String generatedTimestamp,
                int distinctRequests,
                String pendingValue,
                String pendingNote,
                int completed,
                List<ServiceCountItem> requestsPerService,
                List<RequestSummary> underlyingRequests) {
            this.requesterCollegeFilter = requesterCollegeFilter;
            this.submittedPeriodFilter = submittedPeriodFilter;
            this.serviceFilter = serviceFilter;
            this.statusFilter = statusFilter;
            this.requesterCategoryFilter = requesterCategoryFilter;
            this.generatedTimestamp = generatedTimestamp;
            this.distinctRequests = distinctRequests;
            this.pendingValue = pendingValue;
            this.pendingNote = pendingNote;
            this.completed = completed;
            this.requestsPerService = requestsPerService != null ? requestsPerService : new ArrayList<>();
            this.underlyingRequests = underlyingRequests != null ? underlyingRequests : new ArrayList<>();
        }
    }

    /**
     * 9.1 Research Report Default Result
     */
    public static ResearchReportModel getResearchReportResult() {
        List<RecordSummary> records = new ArrayList<>();
        records.add(new RecordSummary("REG-2026-089", "Impact of AI on Student Engagement", "In progress"));
        records.add(new RecordSummary("REG-2025-041", "Gamified Learning Modules for Introductory Programming", "Completed"));

        return new ResearchReportModel(
                "College: CCIS",
                "Registrant category: Student",
                "All stages",
                "All linked services",
                null,
                null,
                "Generated Nov 14, 2026, 10:30 AM",
                2, // Distinct studies
                1, // Presented
                3, // Distinct linked researchers
                1, // Completed
                0, // Published
                records
        );
    }

    /**
     * 9.3 Research Report Empty State
     */
    public static ResearchReportModel getResearchReportEmpty() {
        return new ResearchReportModel(
                "College: College of Health Sciences",
                null,
                null,
                null,
                "Academic period: AY 2023-2024",
                "Outcome: Published",
                "Generated Nov 14, 2026, 10:30 AM",
                0,
                0,
                0,
                0,
                0,
                new ArrayList<>()
        );
    }

    /**
     * 9.2 Service Utilization Result
     */
    public static ServiceUtilizationModel getServiceUtilizationResult() {
        List<ServiceCountItem> serviceCounts = new ArrayList<>();
        serviceCounts.add(new ServiceCountItem("Demo Service A", 2));

        List<RequestSummary> requests = new ArrayList<>();
        requests.add(new RequestSummary("REQ-2026-101", "Demo Service A", "Completed"));
        requests.add(new RequestSummary("REQ-2026-104", "Demo Service A", "In review"));

        return new ServiceUtilizationModel(
                "Requester college: CCIS",
                "Submitted: Jan–Oct 2026",
                "All services",
                "All statuses",
                "Requester category: Student",
                "Generated Nov 14, 2026, 10:30 AM",
                2,
                "—",
                "Definition pending CRM confirmation",
                1,
                serviceCounts,
                requests
        );
    }
}
