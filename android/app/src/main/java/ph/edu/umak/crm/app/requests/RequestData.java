package ph.edu.umak.crm.app.requests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public final class RequestData {
    private RequestData() {}

    public static class FileItem {
        public final String name;
        public final String size;
        public final String status; // "Attached" or "Not attached"
        public final boolean isError;

        public FileItem(String name, String size, String status, boolean isError) {
            this.name = name;
            this.size = size;
            this.status = status;
            this.isError = isError;
        }
    }

    public static class RequestItem {
        public final String id;
        public final String service;
        public final String submittedDate;
        public int serviceRequestId;
        String previewTitle;
        String previewDetails;
        public String status; // "Submitted", "In review", "For correction", "Completed"
        public final String linkedStudyCode;
        public final String linkedStudyTitle;
        public final String linkedStudyStage;
        public String processingStep;
        public List<FileItem> submittedFiles;
        public String feedbackFromCrm;
        public List<String> missingItems;
        public String nextAction;
        public String deadline;
        public List<String> reviewHistory;
        public String actionRequiredNotice;

        public RequestItem(String id, String service, String submittedDate, String status,
                String linkedStudyCode, String linkedStudyTitle, String linkedStudyStage,
                String processingStep, List<FileItem> submittedFiles, String feedbackFromCrm,
                List<String> missingItems, String nextAction, String deadline,
                List<String> reviewHistory, String actionRequiredNotice) {
            this.id = id;
            this.service = service;
            this.submittedDate = submittedDate;
            this.status = status;
            this.linkedStudyCode = linkedStudyCode;
            this.linkedStudyTitle = linkedStudyTitle;
            this.linkedStudyStage = linkedStudyStage;
            this.processingStep = processingStep;
            this.submittedFiles = submittedFiles != null ? new ArrayList<>(submittedFiles) : new ArrayList<>();
            this.feedbackFromCrm = feedbackFromCrm;
            this.missingItems = missingItems != null ? new ArrayList<>(missingItems) : new ArrayList<>();
            this.nextAction = nextAction;
            this.deadline = deadline;
            this.reviewHistory = reviewHistory != null ? new ArrayList<>(reviewHistory) : new ArrayList<>();
            this.actionRequiredNotice = actionRequiredNotice;
        }

        public boolean isPreviewSubmission() {
            return id.startsWith("PREVIEW-");
        }
    }

    public static class NotificationItem {
        public final String title;
        public final String requestCode;
        public final String date;
        public boolean unread;

        public NotificationItem(String title, String requestCode, String date, boolean unread) {
            this.title = title;
            this.requestCode = requestCode;
            this.date = date;
            this.unread = unread;
        }
    }

    static RequestItem previewSubmission(String service, String title, String details, List<FileItem> files) {
        List<FileItem> attached = new ArrayList<>();
        for (FileItem file : files) {
            if (!file.isError) attached.add(file);
        }
        String id = "PREVIEW-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        RequestItem item = new RequestItem(id, service, displayTimestamp(java.time.Instant.now().toString()), "Submitted",
                null, null, null, null, attached, null, null, null, null, null, null);
        item.previewTitle = title;
        item.previewDetails = details;
        return item;
    }

    public static RequestItem serviceRequestReference(int id, String service) {
        RequestItem item = new RequestItem("REQ-" + id, service, "", "", null, null, null,
                "No processing step recorded", null, null, null, null, null, null, null);
        item.serviceRequestId = id;
        return item;
    }

    public static RequestItem fromServiceRequest(JSONObject request) throws JSONException {
        int serviceRequest = request.getInt("id");
        RequestItem item = new RequestItem("REQ-" + serviceRequest, request.getString("service"),
                displayTimestamp(request.has("resubmitted_at") ? request.getString("resubmitted_at")
                        : request.getString("submitted_at")), request.getString("status"),
                request.isNull("study_id") ? null : "#" + request.getInt("study_id"), null, null,
                "No processing step recorded", null, null, null, null, null, null, null);
        item.serviceRequestId = serviceRequest;
        return item;
    }

    public static RequestItem fromConsultation(JSONObject ticket) throws JSONException {
        int id = ticket.getInt("id");
        if (id <= 0) throw new JSONException("Invalid consultation ID");
        String status = switch (ticket.getString("status")) {
            case "queued" -> "Submitted";
            case "claimed" -> "In review";
            case "resolved" -> "Completed";
            case "cancelled" -> "Cancelled";
            default -> ticket.getString("status");
        };
        String submitted = displayTimestamp(ticket.optString("submitted_at", ""));
        String deadline = ticket.isNull("deadline_at") ? null : displayTimestamp(ticket.getString("deadline_at"));
        return new RequestItem("#" + id, ticket.getString("service_type"), submitted, status,
                null, null, null, null, null, null, null, null, deadline, null, null);
    }

    private static String displayTimestamp(String timestamp) {
        try {
            return java.time.format.DateTimeFormatter.ofPattern("MMM d, uuuu, h:mm a", java.util.Locale.ENGLISH)
                    .withZone(java.time.ZoneId.systemDefault())
                    .format(java.time.Instant.parse(timestamp));
        } catch (java.time.format.DateTimeParseException invalid) {
            return timestamp;
        }
    }

    public static RequestItem createReq089(String status) {
        if ("Submitted".equalsIgnoreCase(status)) {
            return new RequestItem(
                    "REQ-2026-089",
                    "Ethics Clearance",
                    "Oct 29, 2026",
                    "Submitted",
                    "REG-2026-089",
                    "Impact of AI on Student Engagement",
                    "In progress",
                    "No processing step recorded",
                    Arrays.asList(
                            new FileItem("RESEARCH_PROTOCOL_v2.pdf", "2.4 MB", "Attached", false),
                            new FileItem("INFORMED_CONSENT_FORM.docx", "480 KB", "Attached", false)
                    ),
                    "No feedback yet",
                    Collections.emptyList(),
                    "No next action recorded",
                    null,
                    Collections.emptyList(),
                    null
            );
        } else if ("In review".equalsIgnoreCase(status)) {
            return new RequestItem(
                    "REQ-2026-089",
                    "Ethics Clearance",
                    "Oct 29, 2026",
                    "In review",
                    "REG-2026-089",
                    "Impact of AI on Student Engagement",
                    "In progress",
                    "Ethics committee review · Started Oct 31, 2026",
                    Arrays.asList(
                            new FileItem("RESEARCH_PROTOCOL_v2.pdf", "2.4 MB", "Attached", false),
                            new FileItem("INFORMED_CONSENT_FORM.docx", "480 KB", "Attached", false)
                    ),
                    "No feedback yet",
                    Collections.emptyList(),
                    "No action needed while CRM reviews your request.",
                    null,
                    Collections.singletonList("Review started Oct 31, 2026"),
                    null
            );
        } else if ("Completed".equalsIgnoreCase(status)) {
            return new RequestItem(
                    "REQ-2026-089",
                    "Ethics Clearance",
                    "Oct 29, 2026",
                    "Completed",
                    "REG-2026-089",
                    "Impact of AI on Student Engagement",
                    "In progress",
                    "Ethics clearance issued · Nov 12, 2026",
                    Arrays.asList(
                            new FileItem("RESEARCH_PROTOCOL_v3.pdf", "2.4 MB", "Attached", false),
                            new FileItem("INFORMED_CONSENT_FORM_v2.docx", "512 KB", "Attached", false),
                            new FileItem("DATA_PRIVACY_NOTICE.pdf", "220 KB", "Attached", false)
                    ),
                    "Your study meets the ethics requirements. The clearance applies to the protocol version submitted on Nov 3, 2026.",
                    Collections.emptyList(),
                    "No next action recorded",
                    null,
                    Arrays.asList(
                            "Returned for correction · Nov 1, 2026",
                            "Correction submitted · Nov 3, 2026",
                            "Completed · Nov 12, 2026"
                    ),
                    null
            );
        } else {
            // Default: "For correction" (3.7c)
            return new RequestItem(
                    "REQ-2026-089",
                    "Ethics Clearance",
                    "Oct 29, 2026",
                    "For correction",
                    "REG-2026-089",
                    "Impact of AI on Student Engagement",
                    "In progress",
                    "Returned for correction · Nov 1, 2026",
                    Arrays.asList(
                            new FileItem("RESEARCH_PROTOCOL_v2.pdf", "2.4 MB", "Attached", false),
                            new FileItem("INFORMED_CONSENT_FORM.docx", "480 KB", "Attached", false)
                    ),
                    "Please revise Section 3.2 (Consent Form) to specify participant data retention policies. The current draft lacks compliance with the data privacy guidelines in Section 4.1 of the research protocol.",
                    Arrays.asList(
                            "Data retention period in the informed consent form (Section 3.2)",
                            "Signed data privacy notice for participants"
                    ),
                    "Add or replace the listed attachments, then resubmit.",
                    "Nov 4, 2026, 5:00 PM",
                    Collections.singletonList("Returned for correction · Nov 1, 2026"),
                    "Correct the missing items below and resubmit by Nov 4, 2026, 5:00 PM."
            );
        }
    }

    public static List<RequestItem> getInitialRequests() {
        List<RequestItem> list = new ArrayList<>();
        list.add(new RequestItem(
                "REQ-2026-092",
                "CRM Clearance Processing",
                "Nov 2, 2026",
                "Submitted",
                "REG-2026-092",
                "Clearance for Graduate Thesis",
                "In progress",
                "No processing step recorded",
                Collections.emptyList(),
                "No feedback yet",
                Collections.emptyList(),
                "No next action recorded",
                null,
                Collections.emptyList(),
                null
        ));
        list.add(createReq089("For correction"));
        list.add(new RequestItem(
                "REQ-2026-081",
                "Research Consultation",
                "Oct 20, 2026",
                "In review",
                "REG-2026-081",
                "Methodology Consultation on Survey Design",
                "In progress",
                "Consultation scheduled",
                Collections.emptyList(),
                "No feedback yet",
                Collections.emptyList(),
                "No next action recorded",
                null,
                Collections.emptyList(),
                null
        ));
        list.add(new RequestItem(
                "REQ-2026-074",
                "Research Registration",
                "Oct 12, 2026",
                "Completed",
                "REG-2026-074",
                "Urban Sustainability in Makati",
                "Registered",
                "Registration finalized · Oct 20, 2026",
                Collections.emptyList(),
                "Study officially registered.",
                Collections.emptyList(),
                "No next action recorded",
                null,
                Collections.emptyList(),
                null
        ));
        return list;
    }

    public static List<NotificationItem> getInitialNotifications() {
        return Arrays.asList(
                new NotificationItem("Deadline tomorrow, 5:00 PM", "REQ-2026-089", "Nov 3, 9:00 AM", true),
                new NotificationItem("Correction needed", "REQ-2026-089", "Nov 1, 3:20 PM", true),
                new NotificationItem("Request received", "REQ-2026-089", "Oct 29, 10:42 AM", false),
                new NotificationItem("Request completed", "REQ-2026-074", "Oct 20, 11:05 AM", false)
        );
    }
}
