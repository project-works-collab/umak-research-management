package ph.edu.umak.crm.app.research;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ResearchData {
    private ResearchData() {}

    public static class UserProfile {
        public String name;
        public String role; // "Student Researcher", "Faculty Researcher"
        public String category; // "Student", "Faculty"
        public String program;
        public String yearLevel;
        public String college;
        public String email;
        public String accountId;
        public String accountStatus; // "Active"
        public String lastLogin;

        public UserProfile(String name, String role, String category, String program,
                String yearLevel, String college, String email, String accountId,
                String accountStatus, String lastLogin) {
            this.name = name;
            this.role = role;
            this.category = category;
            this.program = program;
            this.yearLevel = yearLevel;
            this.college = college;
            this.email = email;
            this.accountId = accountId;
            this.accountStatus = accountStatus;
            this.lastLogin = lastLogin;
        }
    }

    public static class CoAuthorItem {
        public final String id;
        public final String name;
        public final String roleAndCollege; // e.g. "Student · CCIS", "Faculty · CCSS"
        public final String email;

        public CoAuthorItem(String id, String name, String roleAndCollege, String email) {
            this.id = id;
            this.name = name;
            this.roleAndCollege = roleAndCollege;
            this.email = email;
        }
    }

    public static class StageHistoryItem {
        public final String stage;
        public final String date;

        public StageHistoryItem(String stage, String date) {
            this.stage = stage;
            this.date = date;
        }
    }

    public static class RequirementItem {
        public final String title;
        public final String status; // "Completed", "Pending"

        public RequirementItem(String title, String status) {
            this.title = title;
            this.status = status;
        }
    }

    public static class OutcomeItem {
        public final String category; // "Completion", "Presentation", "Publication"
        public final String status; // "Completed", "Presented", "Published", "Not recorded"
        public final String date; // "Mar 15, 2027", "Not recorded"
        public final String detailLabel; // "Details", "Event", "Journal or citation"
        public final String detailValue;

        public OutcomeItem(String category, String status, String date, String detailLabel, String detailValue) {
            this.category = category;
            this.status = status;
            this.date = date;
            this.detailLabel = detailLabel;
            this.detailValue = detailValue;
        }
    }

    public static class StudyProgress {
        public final String id;
        public final String title;
        public final String latestStage; // "Completed", "In progress", "Registered"
        public final List<StageHistoryItem> stageHistory;
        public final List<RequirementItem> requirements;
        public final String nextAction;
        public final String responsibleContact;
        public final String deadline;
        public final List<OutcomeItem> outcomes;

        public StudyProgress(String id, String title, String latestStage,
                List<StageHistoryItem> stageHistory,
                List<RequirementItem> requirements,
                String nextAction, String responsibleContact, String deadline,
                List<OutcomeItem> outcomes) {
            this.id = id;
            this.title = title;
            this.latestStage = latestStage;
            this.stageHistory = stageHistory != null ? stageHistory : Collections.emptyList();
            this.requirements = requirements != null ? requirements : Collections.emptyList();
            this.nextAction = nextAction;
            this.responsibleContact = responsibleContact;
            this.deadline = deadline;
            this.outcomes = outcomes != null ? outcomes : Collections.emptyList();
        }
    }

    public static UserProfile getDefaultProfile() {
        return new UserProfile(
                "LeBron R. James",
                "Student Researcher",
                "Student",
                "BS CS",
                "3rd Year",
                "College of Computing and Information Sciences",
                "ljames.A1234@umak.edu.ph",
                "A1234",
                "Active",
                "This session"
        );
    }

    public static List<CoAuthorItem> getAvailableResearchers() {
        List<CoAuthorItem> list = new ArrayList<>();
        list.add(new CoAuthorItem("1", "Juan D. Inlwan", "Student · CCIS", "jinlwan.A1235@umak.edu.ph"));
        list.add(new CoAuthorItem("4", "Peter Parker", "Faculty · CCIS", "pparker@umak.edu.ph"));
        list.add(new CoAuthorItem("2", "Jonard L. Wala", "Student · CET", "jwala.A1236@umak.edu.ph"));
        list.add(new CoAuthorItem("3", "Maria A. Santos", "Faculty · CCSS", "msantos@umak.edu.ph"));
        return list;
    }

    public static StudyProgress getStudyProgress(String state) {
        if ("4.6a".equals(state) || "all_outcomes".equals(state)) {
            return new StudyProgress(
                    "REG-2026-089",
                    "Impact of AI on Student Engagement",
                    "Completed",
                    Arrays.asList(
                            new StageHistoryItem("Registered", "Oct 29, 2026"),
                            new StageHistoryItem("In progress", "Nov 3, 2026"),
                            new StageHistoryItem("Completed", "Mar 15, 2027")
                    ),
                    Arrays.asList(
                            new RequirementItem("Proposal Submission & Defense", "Completed"),
                            new RequirementItem("Ethics & Consent Clearance", "Completed"),
                            new RequirementItem("Data Collection & Analysis", "Completed"),
                            new RequirementItem("Manuscript Drafting & Peer Review", "Completed"),
                            new RequirementItem("Final Output & Registration", "Completed")
                    ),
                    "Submit the final manuscript for Repository Archiving (SVC-08).",
                    "Center for Research Management · research@umak.edu.ph",
                    null, // No deadline
                    Arrays.asList(
                            new OutcomeItem("Completion", "Completed", "Mar 15, 2027",
                                    "Details", "Final defense passed; manuscript approved by panel."),
                            new OutcomeItem("Presentation", "Presented", "Apr 22, 2027",
                                    "Event", "UMak Research Congress 2027"),
                            new OutcomeItem("Publication", "Published", "Jul 2027",
                                    "Journal or citation", "James, L. R., Inlwan, J. D., & Parker, P. (2027). Impact of AI on student engagement. UNIVERSITY, 15(1), 45–62.")
                    )
            );
        } else if ("4.6b".equals(state) || "completed_only".equals(state)) {
            return new StudyProgress(
                    "REG-2026-089",
                    "Impact of AI on Student Engagement",
                    "Completed",
                    Arrays.asList(
                            new StageHistoryItem("Registered", "Oct 29, 2026"),
                            new StageHistoryItem("In progress", "Nov 3, 2026"),
                            new StageHistoryItem("Completed", "Mar 15, 2027")
                    ),
                    Arrays.asList(
                            new RequirementItem("Proposal Submission & Defense", "Completed"),
                            new RequirementItem("Ethics & Consent Clearance", "Completed"),
                            new RequirementItem("Data Collection & Analysis", "Completed"),
                            new RequirementItem("Manuscript Drafting & Peer Review", "Completed"),
                            new RequirementItem("Final Output & Registration", "Completed")
                    ),
                    "Submit the final manuscript for Repository Archiving (SVC-08).",
                    "Center for Research Management · research@umak.edu.ph",
                    "Apr 30, 2027, 5:00 PM",
                    Arrays.asList(
                            new OutcomeItem("Completion", "Completed", "Mar 15, 2027",
                                    "Details", "Final defense passed; manuscript approved by panel."),
                            new OutcomeItem("Presentation", "Not recorded", "Not recorded",
                                    "Event", "Not recorded"),
                            new OutcomeItem("Publication", "Not recorded", "Not recorded",
                                    "Journal or citation", "Not recorded")
                    )
            );
        } else if ("4.6c".equals(state) || "in_progress".equals(state)) {
            return new StudyProgress(
                    "REG-2026-089",
                    "Impact of AI on Student Engagement",
                    "In progress",
                    Arrays.asList(
                            new StageHistoryItem("Registered", "Oct 29, 2026"),
                            new StageHistoryItem("In progress", "Nov 3, 2026")
                    ),
                    Arrays.asList(
                            new RequirementItem("Proposal Submission & Defense", "Completed"),
                            new RequirementItem("Ethics & Consent Clearance", "Completed"),
                            new RequirementItem("Data Collection & Analysis", "Pending"),
                            new RequirementItem("Manuscript Drafting & Peer Review", "Pending"),
                            new RequirementItem("Final Output & Registration", "Pending")
                    ),
                    "No next action recorded",
                    "Center for Research Management · research@umak.edu.ph",
                    null,
                    Arrays.asList(
                            new OutcomeItem("Completion", "Not recorded", "Not recorded",
                                    "Details", "Not recorded"),
                            new OutcomeItem("Presentation", "Not recorded", "Not recorded",
                                    "Event", "Not recorded"),
                            new OutcomeItem("Publication", "Not recorded", "Not recorded",
                                    "Journal or citation", "Not recorded")
                    )
            );
        } else {
            // 4.6d Empty
            return null;
        }
    }
}
