package ph.edu.umak.crm.app.research;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class RegisteredProgressView {
    private RegisteredProgressView() {}

    public static final String[] STAGE_NAMES = {
            "Registered",
            "In progress",
            "Under review",
            "Completed"
    };

    public static String stageLabel(String stage) {
        return switch (stage) {
            case "In-Progress" -> "In progress";
            case "UnderReview" -> "For revision";
            default -> stage;
        };
    }

    public static int stageToIndex(String stage) {
        if (stage == null) return 0;
        return switch (stage.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "in-progress", "in progress" -> 1;
            case "underreview", "for revision", "under review" -> 2;
            case "completed" -> 3;
            default -> 0; // "Registered"
        };
    }

    public static void append(LinearLayout parent, JSONObject record, boolean staff) {
        // 1. Interactive Lifecycle Timeline with numbered steps (1 to 4)
        parent.addView(createLifecycleTimeline(parent.getContext(), record, staff));

        line(parent, "Research progress details", true);
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

    public static View createLifecycleTimeline(Context context, JSONObject record, boolean staff) {
        String stage = record.optString("stage", "Registered");
        String registeredAt = record.optString("registered_at", "").split("T", 2)[0];
        List<String> completedMilestones = new ArrayList<>();
        List<String> activeMilestones = new ArrayList<>();
        JSONArray milestones = record.optJSONArray("milestones");
        if (milestones != null) {
            for (int i = 0; i < milestones.length(); i++) {
                JSONObject item = milestones.optJSONObject(i);
                if (item == null) continue;
                String title = item.optString("title", "");
                String status = item.optString("status", "");
                String date = item.isNull("effective_date") ? "" : " · " + item.optString("effective_date");
                if ("Completed".equalsIgnoreCase(status)) {
                    completedMilestones.add(title + date);
                } else if ("In progress".equalsIgnoreCase(status)) {
                    activeMilestones.add(title + " (" + status + ")" + date);
                }
            }
        }
        String nextAction = record.optString("next_action", "");
        String responsibleContact = record.optString("responsible_contact", "");
        String deadline = record.optString("deadline", "");
        return createLifecycleTimelineCard(context, stage, registeredAt, completedMilestones, activeMilestones,
                nextAction, responsibleContact, deadline);
    }

    public static View createLifecycleTimeline(Context context, ResearchData.StudyProgress progress) {
        if (progress == null) return new View(context);
        String stage = progress.latestStage != null ? progress.latestStage : "Registered";
        String registeredAt = "";
        if (progress.stageHistory != null && !progress.stageHistory.isEmpty()) {
            registeredAt = progress.stageHistory.get(0).date;
        }
        List<String> completedMilestones = new ArrayList<>();
        List<String> activeMilestones = new ArrayList<>();
        if (progress.requirements != null) {
            for (ResearchData.RequirementItem item : progress.requirements) {
                if ("Completed".equalsIgnoreCase(item.status)) {
                    completedMilestones.add(item.title);
                } else {
                    activeMilestones.add(item.title + " (" + item.status + ")");
                }
            }
        }
        return createLifecycleTimelineCard(context, stage, registeredAt, completedMilestones, activeMilestones,
                progress.nextAction, progress.responsibleContact, progress.deadline);
    }

    public static View createLifecycleTimelineCard(Context context, String currentStageStr,
            String registeredAt, List<String> completedMilestones, List<String> activeMilestones,
            String nextAction, String responsibleContact, String deadline) {
        int currentStep = stageToIndex(currentStageStr);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 10, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 14);
        card.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = UiTheme.dp(context, 14);
        card.setLayoutParams(cardLp);

        // Overline
        TextView overline = new TextView(context);
        overline.setText("RESEARCH LIFECYCLE PROGRESS");
        overline.setTextColor(0xff586b83);
        overline.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(overline, 11);
        card.addView(overline);

        LinearLayout statusRow = new LinearLayout(context);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams sRowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sRowLp.topMargin = UiTheme.dp(context, 4);
        sRowLp.bottomMargin = UiTheme.dp(context, 4);
        statusRow.setLayoutParams(sRowLp);

        TextView currentLabel = new TextView(context);
        currentLabel.setText("Step " + (currentStep + 1) + " of 4: " + STAGE_NAMES[currentStep]);
        currentLabel.setTextColor(0xff082d50);
        currentLabel.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(currentLabel, 16);
        LinearLayout.LayoutParams cLabelLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        statusRow.addView(currentLabel, cLabelLp);

        TextView stateBadge = new TextView(context);
        stateBadge.setText(currentStep == 3 ? "COMPLETED" : "IN LIFECYCLE");
        stateBadge.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(stateBadge, 11);
        stateBadge.setTextColor(currentStep == 3 ? 0xff16a34a : 0xff185d9d);
        stateBadge.setBackground(UiTheme.shape(context, currentStep == 3 ? 0xffdcfce7 : 0xffe5edf7, 4, 0));
        int bpx = UiTheme.dp(context, 8);
        int bpy = UiTheme.dp(context, 3);
        stateBadge.setPadding(bpx, bpy, bpx, bpy);
        statusRow.addView(stateBadge);
        card.addView(statusRow);

        TextView instructionText = new TextView(context);
        instructionText.setText("Tap any numbered step to inspect previous works, progress, and upcoming requirements.");
        instructionText.setTextColor(0xff586b83);
        instructionText.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(instructionText, 12);
        instructionText.setPadding(0, 0, 0, UiTheme.dp(context, 12));
        card.addView(instructionText);

        // Stepper Bar: Circular steps connected by lines
        LinearLayout stepperRow = new LinearLayout(context);
        stepperRow.setOrientation(LinearLayout.HORIZONTAL);
        stepperRow.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams stepperLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stepperLp.bottomMargin = UiTheme.dp(context, 10);
        stepperRow.setLayoutParams(stepperLp);

        final TextView[] circleViews = new TextView[4];
        final TextView[] labelViews = new TextView[4];
        final int[] selectedStepHolder = new int[]{currentStep};
        final LinearLayout detailContainer = new LinearLayout(context);
        detailContainer.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < 4; i++) {
            final int stepIndex = i;
            LinearLayout stepCol = new LinearLayout(context);
            stepCol.setOrientation(LinearLayout.VERTICAL);
            stepCol.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            stepCol.setLayoutParams(colLp);
            stepCol.setClickable(true);
            stepCol.setFocusable(true);

            FrameLayout circleFrame = new FrameLayout(context);
            LinearLayout.LayoutParams frameLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 36));
            circleFrame.setLayoutParams(frameLp);

            // Left connecting line
            if (i > 0) {
                View leftLine = new View(context);
                int lineColor = (i <= currentStep) ? 0xff16a34a : 0xffcbd5e1;
                leftLine.setBackgroundColor(lineColor);
                FrameLayout.LayoutParams leftLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 3));
                leftLp.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                leftLp.rightMargin = UiTheme.dp(context, 18);
                circleFrame.addView(leftLine, leftLp);
            }

            // Right connecting line
            if (i < 3) {
                View rightLine = new View(context);
                int lineColor = (i < currentStep) ? 0xff16a34a : 0xffcbd5e1;
                rightLine.setBackgroundColor(lineColor);
                FrameLayout.LayoutParams rightLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, 3));
                rightLp.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                rightLp.leftMargin = UiTheme.dp(context, 18);
                circleFrame.addView(rightLine, rightLp);
            }

            // Circle Node with Number (1, 2, 3, 4)
            TextView circle = new TextView(context);
            circle.setText(i < currentStep ? "✓" : String.valueOf(i + 1));
            circle.setGravity(Gravity.CENTER);
            circle.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(circle, 13);
            int circleSize = UiTheme.dp(context, 34);
            FrameLayout.LayoutParams cLp = new FrameLayout.LayoutParams(circleSize, circleSize, Gravity.CENTER);
            circleFrame.addView(circle, cLp);
            circleViews[i] = circle;

            stepCol.addView(circleFrame);

            // Step Name
            TextView nameText = new TextView(context);
            nameText.setText(STAGE_NAMES[i]);
            nameText.setGravity(Gravity.CENTER);
            nameText.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(nameText, 11);
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            nameLp.topMargin = UiTheme.dp(context, 4);
            stepCol.addView(nameText, nameLp);
            labelViews[i] = nameText;

            // Status Pill
            TextView pill = new TextView(context);
            pill.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(pill, 9);
            pill.setGravity(Gravity.CENTER);
            int ppx = UiTheme.dp(context, 6);
            int ppy = UiTheme.dp(context, 2);
            pill.setPadding(ppx, ppy, ppx, ppy);
            if (i < currentStep) {
                pill.setText("Done");
                pill.setTextColor(0xff16a34a);
                pill.setBackground(UiTheme.shape(context, 0xffdcfce7, 4, 0));
            } else if (i == currentStep) {
                pill.setText("Current");
                pill.setTextColor(0xff185d9d);
                pill.setBackground(UiTheme.shape(context, 0xffe5edf7, 4, 0));
            } else {
                pill.setText("Upcoming");
                pill.setTextColor(0xff64748b);
                pill.setBackground(UiTheme.shape(context, 0xfff1f5f9, 4, 0));
            }
            LinearLayout.LayoutParams pillLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            pillLp.topMargin = UiTheme.dp(context, 2);
            stepCol.addView(pill, pillLp);

            stepCol.setOnClickListener(v -> {
                selectedStepHolder[0] = stepIndex;
                updateStepperSelection(context, circleViews, labelViews, currentStep, selectedStepHolder[0]);
                renderStageDetails(context, detailContainer, stepIndex, currentStep, registeredAt,
                        completedMilestones, activeMilestones, nextAction, responsibleContact, deadline);
            });
            stepperRow.addView(stepCol);
        }

        card.addView(stepperRow);

        updateStepperSelection(context, circleViews, labelViews, currentStep, selectedStepHolder[0]);
        renderStageDetails(context, detailContainer, selectedStepHolder[0], currentStep, registeredAt,
                completedMilestones, activeMilestones, nextAction, responsibleContact, deadline);
        card.addView(detailContainer);

        return card;
    }

    private static void updateStepperSelection(Context context, TextView[] circleViews, TextView[] labelViews,
            int currentStep, int selectedStep) {
        for (int i = 0; i < 4; i++) {
            boolean isSelected = (i == selectedStep);
            if (i < currentStep) {
                circleViews[i].setTextColor(0xffffffff);
                circleViews[i].setBackground(UiTheme.shape(context, 0xff16a34a, 17, isSelected ? 0xff082d50 : 0));
            } else if (i == currentStep) {
                circleViews[i].setTextColor(0xffffffff);
                circleViews[i].setBackground(UiTheme.shape(context, 0xff185d9d, 17, isSelected ? 0xfff59e0b : 0));
            } else {
                circleViews[i].setTextColor(isSelected ? 0xff082d50 : 0xff64748b);
                circleViews[i].setBackground(UiTheme.shape(context, UiTheme.WHITE, 17, isSelected ? 0xff082d50 : 0xffcbd5e1));
            }
            labelViews[i].setTextColor(isSelected ? 0xff082d50 : 0xff586b83);
            labelViews[i].setTypeface(isSelected ? UiTheme.boldFont(context) : UiTheme.bodyFont(context));
        }
    }

    private static void renderStageDetails(Context context, LinearLayout container, int stepIndex, int currentStep,
            String registeredAt, List<String> completedMilestones, List<String> activeMilestones,
            String nextAction, String responsibleContact, String deadline) {
        container.removeAllViews();

        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(UiTheme.shape(context, 0xfff8fafc, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 12);
        box.setPadding(pad, pad, pad, pad);

        // Stage Title & Step Badge
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(context);
        title.setText("Step " + (stepIndex + 1) + ": " + STAGE_NAMES[stepIndex]);
        title.setTextColor(0xff082d50);
        title.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(title, 15);
        headerRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView badge = new TextView(context);
        badge.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(badge, 10);
        int px = UiTheme.dp(context, 6);
        int py = UiTheme.dp(context, 2);
        badge.setPadding(px, py, px, py);
        if (stepIndex < currentStep) {
            badge.setText("COMPLETED");
            badge.setTextColor(0xff16a34a);
            badge.setBackground(UiTheme.shape(context, 0xffdcfce7, 4, 0));
        } else if (stepIndex == currentStep) {
            badge.setText("CURRENT STAGE");
            badge.setTextColor(0xff185d9d);
            badge.setBackground(UiTheme.shape(context, 0xffe5edf7, 4, 0));
        } else {
            badge.setText("UPCOMING REQUIREMENT");
            badge.setTextColor(0xff64748b);
            badge.setBackground(UiTheme.shape(context, 0xfff1f5f9, 4, 0));
        }
        headerRow.addView(badge);
        box.addView(headerRow);

        // Section 1: Previous Works
        addSectionHeader(context, box, "PREVIOUS WORKS & DELIVERABLES");
        if (stepIndex == 0) {
            addBullet(context, box, "Completed CRM Form 001 – Research Registration");
            addBullet(context, box, "Faculty adviser endorsement letter verified");
            addBullet(context, box, "Approved research proposal title & abstract recorded");
            if (!registeredAt.isEmpty()) addBullet(context, box, "Registered date: " + registeredAt);
        } else if (stepIndex <= currentStep) {
            if (!completedMilestones.isEmpty()) {
                for (String m : completedMilestones) addBullet(context, box, m);
            } else {
                addBullet(context, box, "Work deliverables submitted and recorded for this stage.");
            }
        } else {
            addText(context, box, "No previous works recorded for this stage yet.");
        }

        // Section 2: Current Progress
        addSectionHeader(context, box, "PROGRESS & ACTIVE TASKS");
        if (stepIndex == currentStep) {
            String action = (nextAction != null && !nextAction.isEmpty()) ? nextAction : "Executing research milestones and tasks.";
            addBullet(context, box, "Next Action: " + action);
            if (responsibleContact != null && !responsibleContact.isEmpty()) {
                addBullet(context, box, "Responsible Contact: " + responsibleContact);
            }
            if (deadline != null && !deadline.isEmpty()) {
                addBullet(context, box, "Scheduled Deadline: " + deadline);
            }
            if (!activeMilestones.isEmpty()) {
                for (String m : activeMilestones) addBullet(context, box, m);
            }
        } else if (stepIndex < currentStep) {
            addText(context, box, "Stage completed and validated by the Center for Research Management.");
        } else {
            addText(context, box, "Awaiting completion of earlier lifecycle stages.");
        }

        // Section 3: Future Requirements
        addSectionHeader(context, box, "FUTURE REQUIREMENTS FOR UPCOMING STAGE");
        if (stepIndex == 0) {
            addBullet(context, box, "Maintain adviser consultation and prepare methodology protocol");
            addBullet(context, box, "Apply for Ethics Clearance if study involves human/animal subjects");
        } else if (stepIndex == 1) {
            addBullet(context, box, "Submit completed research manuscript for panel evaluation");
            addBullet(context, box, "Obtain ethics clearance certificate");
            addBullet(context, box, "Generate originality / plagiarism similarity check report");
        } else if (stepIndex == 2) {
            addBullet(context, box, "Submit manuscript revision matrix addressing panel comments");
            addBullet(context, box, "Apply for CRM Clearance Certificate for oral defense or graduation");
            addBullet(context, box, "Prepare camera-ready approved final copy");
        } else {
            addBullet(context, box, "Official deposit in the CRM Digital Repository");
            addBullet(context, box, "Submission for consideration in the UNIVERSITY Research Journal");
        }

        container.addView(box);
    }

    private static void addSectionHeader(Context context, LinearLayout parent, String title) {
        TextView heading = new TextView(context);
        heading.setText(title);
        heading.setTextColor(0xff185d9d);
        heading.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(heading, 11);
        heading.setLetterSpacing(0.04f);
        heading.setPadding(0, UiTheme.dp(context, 10), 0, UiTheme.dp(context, 4));
        parent.addView(heading);
    }

    private static void addBullet(Context context, LinearLayout parent, String text) {
        TextView item = new TextView(context);
        item.setText("• " + text);
        item.setTextColor(0xff082d50);
        item.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(item, 12);
        item.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 2));
        parent.addView(item);
    }

    private static void addText(Context context, LinearLayout parent, String text) {
        TextView item = new TextView(context);
        item.setText(text);
        item.setTextColor(0xff64748b);
        item.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(item, 12);
        item.setPadding(0, UiTheme.dp(context, 2), 0, UiTheme.dp(context, 2));
        parent.addView(item);
    }
}

