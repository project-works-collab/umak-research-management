package ph.edu.umak.crm.app.staff.queue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Adapts the authorized request API to the same models used by the designed screens. */
public final class StaffRequestData {
    private StaffRequestData() {}
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");
    private static final List<String> STATUSES = List.of("Submitted", "In review", "For correction", "Completed");

    public static StaffQueueData.QueueRequestItem fromJson(JSONObject row) throws JSONException {
        int id = row.getInt("id");
        int version = row.getInt("version");
        String status = row.getString("status");
        if (id <= 0 || version <= 0 || !STATUSES.contains(status)) throw new JSONException("Invalid request identity");
        List<StaffQueueData.AttachmentItem> attachments = new ArrayList<>();
        JSONArray files = row.optJSONArray("attachments");
        if (files != null) for (int i = 0; i < files.length(); i++) {
            JSONObject file = files.getJSONObject(i);
            // File size is not supplied by this API; never substitute a sample size.
            var item = new StaffQueueData.AttachmentItem(file.getString("filename"), "");
            item.fileId = file.getString("id");
            item.mime = file.getString("mime");
            attachments.add(item);
        }
        JSONArray reviews = row.optJSONArray("reviews");
        JSONObject latest = reviews == null ? null : reviews.optJSONObject(0);
        JSONArray checked = latest == null ? null : latest.optJSONArray("checklist");
        List<StaffQueueData.ChecklistItem> checklist = new ArrayList<>();
        JSONArray requirements = row.optJSONArray("requirements");
        if (requirements != null) for (int i = 0; i < requirements.length(); i++) {
            checklist.add(new StaffQueueData.ChecklistItem(requirements.getJSONObject(i).getString("title"),
                    checked == null ? "Pending" : checked.optString(i, "Pending")));
        }
        List<StaffQueueData.ReviewLogItem> history = new ArrayList<>();
        if (reviews != null) for (int i = 0; i < reviews.length(); i++) {
            JSONObject event = reviews.getJSONObject(i);
            var entry = new StaffQueueData.ReviewLogItem(event.getString("to_status"),
                    "Staff " + event.getInt("reviewer_id"), displayDate(event.getString("reviewed_at")), false);
            StringBuilder detail = new StringBuilder();
            for (String field : new String[]{"feedback", "next_action", "internal_remark", "correction_reason"}) {
                String value = string(event, field);
                if (!value.isEmpty()) detail.append(switch (field) {
                    case "internal_remark" -> "Internal: ";
                    case "correction_reason" -> "Correction reason: ";
                    case "next_action" -> "Next action: ";
                    default -> "Feedback: ";
                }).append(value).append('\n');
            }
            String deadline = string(event, "deadline_at");
            if (!deadline.isEmpty()) detail.append("Deadline (Philippine time): ").append(displayDate(deadline)).append('\n');
            JSONObject checkpoint = event.optJSONObject("checkpoint");
            if (checkpoint != null) detail.append("Checkpoint: ").append(string(checkpoint, "name"))
                    .append(" · ").append(string(checkpoint, "date")).append(" · ").append(string(checkpoint, "reference")).append('\n');
            entry.details = detail.toString().trim();
            history.add(entry);
        }
        history.add(new StaffQueueData.ReviewLogItem("Submitted", "Researcher", displayDate(row.getString("submitted_at")), false));
        String requester = string(row, "requester_name");
        if (requester.isEmpty()) requester = string(row, "requester_email");
        var item = new StaffQueueData.QueueRequestItem("REQ-" + id, status, row.getString("service"),
                row.getString("subject"), displayDate(row.getString("submitted_at")), requester,
                "faculty".equals(string(row, "requester_category")) ? "Faculty" : "Student",
                string(row, "requester_college"), string(row, "requester_email"), string(row, "description"),
                attachments, checklist, history);
        item.serverId = id;
        item.version = version;
        item.submittedDate = Instant.parse(row.getString("submitted_at")).atZone(MANILA).toLocalDate().toString();
        item.targetStatus = "Submitted".equals(status) ? "In review" : status;
        item.feedbackToResearcher = latest == null ? "" : string(latest, "feedback");
        item.internalRemarks = latest == null ? "" : string(latest, "internal_remark");
        item.nextAction = latest == null ? "" : string(latest, "next_action");
        item.deadlineDate = item.deadlineTime = "";
        if (latest != null && !string(latest, "deadline_at").isEmpty()) {
            var deadline = Instant.parse(latest.getString("deadline_at")).atZone(MANILA);
            item.deadlineDate = deadline.toLocalDate().toString();
            item.deadlineTime = deadline.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        JSONArray missing = latest == null ? null : latest.optJSONArray("missing_items");
        if (missing != null) for (int i = 0; i < missing.length(); i++) {
            int index = missing.getInt(i);
            if (index < 0 || index >= checklist.size()) throw new JSONException("Invalid missing requirement");
            item.missingRequirementIndices.add(index);
        }
        JSONObject checkpoint = latest == null ? null : latest.optJSONObject("checkpoint");
        item.checkpointName = checkpoint == null ? "" : string(checkpoint, "name");
        item.checkpointDate = checkpoint == null ? "" : string(checkpoint, "date");
        item.checkpointRef = checkpoint == null ? "" : string(checkpoint, "reference");
        return item;
    }

    public static JSONObject review(StaffQueueData.QueueRequestItem item) throws JSONException {
        if (!allowedStatuses(item.status).contains(item.targetStatus)) throw new IllegalArgumentException("This status change is not allowed.");
        if ("For correction".equals(item.targetStatus)
                && (item.missingRequirementIndices.isEmpty() || item.nextAction.trim().isEmpty()))
            throw new IllegalArgumentException("Select the missing requirements and enter the next action.");
        if ("Completed".equals(item.status) && (item.correctionReason == null || item.correctionReason.trim().isEmpty()))
            throw new IllegalArgumentException("Enter the correction reason.");
        JSONArray checks = new JSONArray();
        for (var entry : item.checklist) checks.put(entry.status);
        JSONObject body = new JSONObject().put("expected_version", item.version).put("status", item.targetStatus)
                .put("checklist", checks).put("missing_items", new JSONArray(item.missingRequirementIndices))
                .put("feedback", item.feedbackToResearcher).put("internal_remark", item.internalRemarks)
                .put("next_action", item.nextAction).put("deadline_at", JSONObject.NULL);
        String date = item.deadlineDate.trim(), time = item.deadlineTime.trim();
        if (!date.isEmpty() || !time.isEmpty()) {
            if (date.isEmpty() || time.isEmpty()) throw new IllegalArgumentException("Enter both a deadline date and time, or leave both blank.");
            try {
                body.put("deadline_at", LocalDate.parse(date).atTime(LocalTime.parse(time)).atZone(MANILA).toInstant().toString());
            } catch (java.time.DateTimeException invalid) {
                throw new IllegalArgumentException("Use YYYY-MM-DD and HH:mm for the deadline (Philippine time).");
            }
        }
        boolean checkpoint = !item.checkpointName.trim().isEmpty() || !item.checkpointDate.trim().isEmpty() || !item.checkpointRef.trim().isEmpty();
        if (checkpoint) {
            if (item.checkpointName.trim().isEmpty()) throw new IllegalArgumentException("Enter the checkpoint name.");
            try { LocalDate.parse(item.checkpointDate.trim()); }
            catch (java.time.DateTimeException invalid) { throw new IllegalArgumentException("Enter a checkpoint date in YYYY-MM-DD format."); }
            body.put("checkpoint", new JSONObject().put("name", item.checkpointName.trim())
                    .put("date", item.checkpointDate.trim()).put("reference", item.checkpointRef.trim()));
        }
        if ("Completed".equals(item.status)) body.put("correction_reason", item.correctionReason.trim());
        return body;
    }

    public static List<String> allowedStatuses(String status) {
        return switch (status) {
            case "Submitted" -> List.of("In review");
            case "In review" -> List.of("In review", "For correction", "Completed");
            case "Completed" -> List.of("Completed");
            default -> List.of();
        };
    }

    private static String string(JSONObject row, String key) { return row.isNull(key) ? "" : row.optString(key, ""); }
    private static String displayDate(String value) {
        return DateTimeFormatter.ofPattern("MMM d, uuuu, h:mm a", Locale.US).withZone(MANILA).format(Instant.parse(value));
    }
}
