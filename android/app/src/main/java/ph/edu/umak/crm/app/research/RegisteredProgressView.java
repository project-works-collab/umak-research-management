package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class RegisteredProgressView {
    private RegisteredProgressView() {}

    public static String stageLabel(String stage) {
        return switch (stage) {
            case "In-Progress" -> "In progress";
            case "UnderReview" -> "For revision";
            default -> stage;
        };
    }

    public static void append(LinearLayout parent, JSONObject record, boolean staff) {
        line(parent, "Research progress", true);
        line(parent, "Current stage: " + stageLabel(record.optString("stage")), true);
        String next = record.optString("next_action", "");
        line(parent, next.isEmpty() ? "No next action recorded" : "Next action: " + next, false);
        line(parent, "Responsible contact: " + record.optString("responsible_contact"), false);
        if (!record.isNull("deadline")) line(parent, "Deadline: " + record.optString("deadline"), false);

        line(parent, "Milestones and requirements", true);
        JSONArray milestones = record.optJSONArray("milestones");
        if (milestones != null) for (int i = 0; i < milestones.length(); i++) {
            JSONObject item = milestones.optJSONObject(i);
            if (item == null) continue;
            String date = item.isNull("effective_date") ? "" : " · " + item.optString("effective_date");
            line(parent, item.optString("title") + " · " + item.optString("status") + date, false);
            if (staff) line(parent, item.optString("evidence"), false);
        }
        outcome(parent, "Completion", record.optJSONObject("completion"), "details");
        outcome(parent, "Presentation", record.optJSONObject("presentation"), "event");
        outcome(parent, "Publication", record.optJSONObject("publication"), "citation");

        line(parent, "Stage and outcome history", true);
        line(parent, "Registered · " + record.optString("registered_at").split("T", 2)[0], false);
        JSONArray entries = record.optJSONArray("history");
        if (entries == null) return;
        for (int i = entries.length() - 1; i >= 0; i--) {
            JSONObject event = entries.optJSONObject(i);
            if (event == null) continue;
            JSONObject before = event.optJSONObject("before");
            JSONObject after = event.optJSONObject("after");
            if (before != null && after != null && before.has("stage")) {
                line(parent, stageLabel(before.optString("stage")) + " → " + stageLabel(after.optString("stage")),
                        false);
            } else {
                line(parent, switch (event.optString("kind")) {
                    case "milestone" -> "Milestone updated";
                    case "presentation" -> "Presentation recorded";
                    case "publication" -> "Publication recorded";
                    case "completion_correction" -> "Completion corrected";
                    default -> "Progress updated";
                }, false);
            }
            String when = event.isNull("effective_date") ? event.optString("recorded_at").split("T", 2)[0]
                    : event.optString("effective_date");
            line(parent, "Effective: " + when + (staff ? " · Staff " + event.optString("actor_id") : ""), false);
            if (staff) {
                line(parent, "Recorded: " + event.optString("recorded_at"), false);
                line(parent, event.optString("supporting_details"), false);
                line(parent, event.optString("reference"), false);
                line(parent, event.optString("reason"), false);
                String note = event.optString("internal_note");
                if (!note.isEmpty()) line(parent, "Internal: " + note, false);
            }
        }
    }

    public static void appendPlanning(LinearLayout parent, JSONObject plan) {
        line(parent, "Next tasks and work estimate", true);
        if (!plan.optBoolean("configured")) {
            line(parent, "A staff member can configure milestone prerequisites and estimated work minutes.", false);
            return;
        }
        if (plan.has("error")) {
            line(parent, plan.optString("error"), false);
            return;
        }
        if (plan.optBoolean("complete")) line(parent, "All workflow tasks are completed or not applicable.", false);
        JSONArray ready = plan.optJSONArray("ready");
        if (ready != null && ready.length() > 0) {
            line(parent, "Ready to start", true);
            for (int i = 0; i < ready.length(); i++) line(parent, ready.optJSONObject(i).optString("title"), false);
        }
        JSONArray blocked = plan.optJSONArray("blocked");
        if (blocked != null && blocked.length() > 0) {
            line(parent, "Blocked tasks", true);
            for (int i = 0; i < blocked.length(); i++) {
                JSONObject task = blocked.optJSONObject(i);
                line(parent, task.optString("title"), true);
                JSONArray reasons = task.optJSONArray("reasons");
                if (reasons != null) for (int j = 0; j < reasons.length(); j++) line(parent, reasons.optString(j), false);
            }
        }
        line(parent, "Estimated remaining work: " + plan.optLong("remaining_duration_minutes") + " minutes", true);
        JSONArray path = plan.optJSONArray("critical_path");
        if (path != null && path.length() > 0) {
            java.util.StringJoiner chain = new java.util.StringJoiner(" → ");
            for (int i = 0; i < path.length(); i++) chain.add(path.optJSONObject(i).optString("title"));
            line(parent, "Critical path: " + chain, false);
        }
        line(parent, "Work estimates follow the longest remaining prerequisite chain and assume parallel tasks can run together. Evidence waits, staffing, and calendar delays are excluded.", false);
    }

    private static void outcome(LinearLayout parent, String title, JSONObject data, String detail) {
        line(parent, title, true);
        if (data == null || data.isNull("date")) {
            line(parent, "Not recorded", false);
            return;
        }
        line(parent, data.optString("date"), false);
        line(parent, data.optString(detail), false);
    }

    public static void line(LinearLayout parent, String content, boolean heading) {
        if (content == null || content.isEmpty()) return;
        Context context = parent.getContext();
        TextView view = new TextView(context);
        view.setText(content);
        view.setTypeface(heading ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        view.setTextColor(heading ? UiTheme.NAVY : UiTheme.MUTED);
        ScreenTypography.apply(view, heading ? 16 : 14);
        view.setPadding(0, UiTheme.dp(context, heading ? 12 : 3), 0, UiTheme.dp(context, 4));
        parent.addView(view);
    }
}
