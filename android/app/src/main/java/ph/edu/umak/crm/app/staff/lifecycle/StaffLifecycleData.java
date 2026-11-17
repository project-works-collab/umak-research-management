package ph.edu.umak.crm.app.staff.lifecycle;

import java.util.ArrayList;
import java.util.List;

/**
 * Data models and mock datasets for Staff Stages, Milestones, and Outcomes (Part 8, C-04).
 */
public final class StaffLifecycleData {

    private StaffLifecycleData() {}

    public static class MilestoneItem {
        public String name;
        public String status; // "Pending", "In progress", "Completed", "Not applicable"
        public String meta; // e.g. "Approved · Jun 15, 2026"
        public String date; // "2026-11-15"
        public String evidenceRef; // "Field logbook, survey batches 1 – 3"

        public MilestoneItem(String name, String status, String meta, String date, String evidenceRef) {
            this.name = name;
            this.status = status;
            this.meta = meta;
            this.date = date;
            this.evidenceRef = evidenceRef;
        }
    }

    public static class StageHistoryEntry {
        public final String transition;
        public final String effectiveDate;
        public final String supportingDetails;
        public final String explanation;
        public final String authorAndTimestamp;
        public final boolean isCorrection;

        public StageHistoryEntry(String transition, String effectiveDate, String supportingDetails,
                                 String explanation, String authorAndTimestamp, boolean isCorrection) {
            this.transition = transition;
            this.effectiveDate = effectiveDate;
            this.supportingDetails = supportingDetails;
            this.explanation = explanation;
            this.authorAndTimestamp = authorAndTimestamp;
            this.isCorrection = isCorrection;
        }
    }

    public static class OutcomeCompletion {
        public String status; // "Not completed", "Completed"
        public String completionDate; // "2027-03-15"
        public boolean isSaved;

        public OutcomeCompletion(String status, String completionDate, boolean isSaved) {
            this.status = status;
            this.completionDate = completionDate;
            this.isSaved = isSaved;
        }
    }

    public static class OutcomePresentation {
        public String status; // "Not recorded", "Presented"
        public String presentationDate; // "YYYY-MM-DD"
        public String eventName; // ""

        public OutcomePresentation(String status, String presentationDate, String eventName) {
            this.status = status;
            this.presentationDate = presentationDate;
            this.eventName = eventName;
        }
    }

    public static class OutcomePublication {
        public String status; // "Recorded month: Oct 2026", "Not recorded", "Published"
        public String publicationDate; // "Exact date not recorded"
        public String journalCitation; // "Citation not recorded"

        public OutcomePublication(String status, String publicationDate, String journalCitation) {
            this.status = status;
            this.publicationDate = publicationDate;
            this.journalCitation = journalCitation;
        }
    }

    public static class ResearchLifecycleState {
        public String recordId;
        public String title;
        public String currentStage; // "In progress", "For revision", "Completed"
        public String savedBannerMessage; // "Stage updated.", "Outcome recorded.", null
        public String savedTimestamp; // "Saved Nov 6, 2026, 2:40 PM by LeBron James"

        // Form fields
        public String newStage;
        public String effectiveDate;
        public String supportingRef;
        public String nextAction;
        public boolean noNextActionRecorded;

        // Sections
        public List<MilestoneItem> milestones;
        public OutcomeCompletion completion;
        public OutcomePresentation presentation;
        public OutcomePublication publication;
        public List<StageHistoryEntry> stageHistory;

        public ResearchLifecycleState(String recordId, String title, String currentStage,
                                      String savedBannerMessage, String savedTimestamp,
                                      String newStage, String effectiveDate, String supportingRef,
                                      String nextAction, boolean noNextActionRecorded,
                                      List<MilestoneItem> milestones,
                                      OutcomeCompletion completion,
                                      OutcomePresentation presentation,
                                      OutcomePublication publication,
                                      List<StageHistoryEntry> stageHistory) {
            this.recordId = recordId;
            this.title = title;
            this.currentStage = currentStage;
            this.savedBannerMessage = savedBannerMessage;
            this.savedTimestamp = savedTimestamp;
            this.newStage = newStage;
            this.effectiveDate = effectiveDate;
            this.supportingRef = supportingRef;
            this.nextAction = nextAction;
            this.noNextActionRecorded = noNextActionRecorded;
            this.milestones = milestones != null ? milestones : new ArrayList<>();
            this.completion = completion != null ? completion : new OutcomeCompletion("Not completed", "", false);
            this.presentation = presentation != null ? presentation : new OutcomePresentation("Not recorded", "", "");
            this.publication = publication != null ? publication : new OutcomePublication("Recorded month: Oct 2026", "Exact date not recorded", "Citation not recorded");
            this.stageHistory = stageHistory != null ? stageHistory : new ArrayList<>();
        }
    }

    public static ResearchLifecycleState getDefaultLifecycleState() {
        List<MilestoneItem> milestones = new ArrayList<>();
        milestones.add(new MilestoneItem("Proposal Submission & Defense", "Completed", "Approved · Jun 15, 2026", "2026-06-15", "Approved memo 2026-081"));
        milestones.add(new MilestoneItem("Ethics & Consent Clearance", "Completed", "Cleared · Jul 22, 2026", "2026-07-22", "UREC clearance certificate"));
        milestones.add(new MilestoneItem("Data Collection & Analysis", "In progress", "Target · Nov 15, 2026", "2026-11-15", "Field logbook, survey batches 1 – 3"));
        milestones.add(new MilestoneItem("Manuscript Drafting & Peer Review", "Pending", "No effective date recorded", "", ""));
        milestones.add(new MilestoneItem("Final Output & Registration", "Pending", "No effective date recorded", "", ""));

        List<StageHistoryEntry> history = new ArrayList<>();
        history.add(new StageHistoryEntry(
                "Registered → In progress",
                "Nov 5, 2026",
                "Ethics clearance memo dated Nov 5, 2026.",
                "Corrects the effective date of the Nov 3, 2026 entry (was Nov 3, 2026).",
                "Ana M. Reyes (CRM Staff) · Recorded Nov 6, 2026, 10:15 AM",
                true
        ));
        history.add(new StageHistoryEntry(
                "Registered → In progress",
                "Nov 3, 2026",
                "Proposal defense result, CRM memo 2026-118.",
                null,
                "Lebron James (CRM Staff) · Recorded Nov 3, 2026, 4:30 PM",
                false
        ));
        history.add(new StageHistoryEntry(
                "New record → Registered",
                "Oct 29, 2026",
                "Research registration approved.",
                null,
                "Lebron James (CRM Staff) · Recorded Oct 29, 2026, 10:42 AM",
                false
        ));

        return new ResearchLifecycleState(
                "REG-2026-089",
                "Impact of AI on Student Engagement",
                "In progress",
                null,
                null,
                "",
                "",
                "",
                "",
                false,
                milestones,
                new OutcomeCompletion("Not completed", "", false),
                new OutcomePresentation("Not recorded", "", ""),
                new OutcomePublication("Recorded month: Oct 2026", "Exact date not recorded", "Citation not recorded"),
                history
        );
    }
}
