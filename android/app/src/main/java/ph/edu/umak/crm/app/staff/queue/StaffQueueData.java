package ph.edu.umak.crm.app.staff.queue;

import java.util.ArrayList;
import java.util.List;

/**
 * Data models and mock dataset for Staff Review Queue and Request Workspace (Part 6).
 */
public final class StaffQueueData {

    private StaffQueueData() {}

    public static class AttachmentItem {
        public final String fileName;
        public final String fileSize;

        public AttachmentItem(String fileName, String fileSize) {
            this.fileName = fileName;
            this.fileSize = fileSize;
        }
    }

    public static class ChecklistItem {
        public final String title;
        public String status; // "Pending", "Verified", "Needs correction", "Not applicable"

        public ChecklistItem(String title, String status) {
            this.title = title;
            this.status = status;
        }
    }

    public static class ReviewLogItem {
        public final String status;
        public final String author;
        public final String timestamp;
        public final boolean isNew;

        public ReviewLogItem(String status, String author, String timestamp, boolean isNew) {
            this.status = status;
            this.author = author;
            this.timestamp = timestamp;
            this.isNew = isNew;
        }
    }

    public static class QueueRequestItem {
        public final String id;
        public String status; // "Submitted", "In review", "For correction", "Completed"
        public final String serviceType;
        public final String studyTitle;
        public final String submittedAt;
        public final String requesterName;
        public final String requesterRole; // "Student", "Faculty"
        public final String requesterCollege;
        public final String requesterEmail;
        public final String summary;
        public final List<AttachmentItem> attachments;
        public final List<ChecklistItem> checklist;
        public final List<ReviewLogItem> earlierReviews;

        // Decision fields
        public String targetStatus;
        public String feedbackToResearcher;
        public List<String> missingItems;
        public String nextAction;
        public String deadlineDate;
        public String deadlineTime;
        public String internalRemarks;
        public String checkpointName;
        public String checkpointDate;
        public String checkpointRef;

        // Completed correction fields
        public String correctionReason;

        public QueueRequestItem(String id, String status, String serviceType, String studyTitle,
                                String submittedAt, String requesterName, String requesterRole,
                                String requesterCollege, String requesterEmail, String summary,
                                List<AttachmentItem> attachments, List<ChecklistItem> checklist,
                                List<ReviewLogItem> earlierReviews) {
            this.id = id;
            this.status = status;
            this.serviceType = serviceType;
            this.studyTitle = studyTitle;
            this.submittedAt = submittedAt;
            this.requesterName = requesterName;
            this.requesterRole = requesterRole;
            this.requesterCollege = requesterCollege;
            this.requesterEmail = requesterEmail;
            this.summary = summary;
            this.attachments = attachments != null ? attachments : new ArrayList<>();
            this.checklist = checklist != null ? checklist : new ArrayList<>();
            this.earlierReviews = earlierReviews != null ? earlierReviews : new ArrayList<>();
            this.missingItems = new ArrayList<>();
        }
    }

    public static List<QueueRequestItem> getInitialQueue() {
        List<QueueRequestItem> list = new ArrayList<>();

        // REQ-2026-089 (In review)
        List<AttachmentItem> att89 = new ArrayList<>();
        att89.add(new AttachmentItem("RESEARCH_PROTOCOL_v2.pdf", "2.4 MB"));
        att89.add(new AttachmentItem("INFORMED_CONSENT_FORM.docx", "480 KB"));

        List<ChecklistItem> cl89 = new ArrayList<>();
        cl89.add(new ChecklistItem("Informed consent form included", "Verified"));
        cl89.add(new ChecklistItem("Research methodology is clear", "Verified"));
        cl89.add(new ChecklistItem("Participant data privacy addressed", "Needs correction"));
        cl89.add(new ChecklistItem("Signature of lead & co-authors", "Pending"));

        List<ReviewLogItem> rev89 = new ArrayList<>();
        rev89.add(new ReviewLogItem("In review", "Lebron James · CRM Staff", "Oct 31, 2026, 9:05 AM", false));
        rev89.add(new ReviewLogItem("Submitted", "LeBron R. James · Researcher", "Oct 29, 2026, 10:42 AM", false));

        QueueRequestItem req89 = new QueueRequestItem(
                "REQ-2026-089",
                "In review",
                "Ethics Clearance",
                "Impact of AI on Student Engagement",
                "Oct 29, 2026, 10:42 AM",
                "LeBron R. James",
                "Student",
                "College of Computing and Information Sciences",
                "ljames.A1234@umak.edu.ph",
                "Please review the consent form and research protocol before data collection…",
                att89, cl89, rev89
        );
        req89.targetStatus = "For correction";
        req89.feedbackToResearcher = "Please revise Section 3.2 (Consent Form) to specify participant data retention policies, as required by Section 4.1 of the research protocol.";
        req89.missingItems.add("Data retention period in the informed consent form (Section 3.2)");
        req89.missingItems.add("Signed data privacy notice for participants");
        req89.nextAction = "Add or replace the listed attachments, then resubmit.";
        req89.deadlineDate = "2026-11-04";
        req89.deadlineTime = "5:00 PM";
        req89.internalRemarks = "Second request from this adviser group this term; check against REG-2024-012 overlap.";
        req89.checkpointName = "Committee review completed";
        req89.checkpointDate = "2026-11-01";
        req89.checkpointRef = "UREC minutes no. 2026-41";
        list.add(req89);

        // REQ-2026-081 (In review)
        List<AttachmentItem> att81 = new ArrayList<>();
        att81.add(new AttachmentItem("SAMPLING_DESIGN_PROPOSAL.pdf", "1.2 MB"));
        List<ChecklistItem> cl81 = new ArrayList<>();
        cl81.add(new ChecklistItem("Sampling methodology described", "Verified"));
        cl81.add(new ChecklistItem("Target population defined", "Pending"));
        List<ReviewLogItem> rev81 = new ArrayList<>();
        rev81.add(new ReviewLogItem("In review", "Ana M. Reyes · CRM Staff", "Oct 21, 2026, 11:00 AM", false));
        rev81.add(new ReviewLogItem("Submitted", "Juan D. Inlwan · Researcher", "Oct 20, 2026, 2:15 PM", false));

        QueueRequestItem req81 = new QueueRequestItem(
                "REQ-2026-081",
                "In review",
                "Research Consultation",
                "Machine Learning on Agriculture",
                "Oct 20, 2026, 2:15 PM",
                "Juan D. Inlwan",
                "Student",
                "College of Technology",
                "jinlwan@umak.edu.ph",
                "Requesting guidance on sampling design for the field survey…",
                att81, cl81, rev81
        );
        list.add(req81);

        // REQ-2026-074 (Completed)
        List<AttachmentItem> att74 = new ArrayList<>();
        att74.add(new AttachmentItem("QUANTUM_PROPOSAL_FINAL.pdf", "3.1 MB"));
        att74.add(new AttachmentItem("ENDORSEMENT_LETTER.pdf", "890 KB"));
        List<ChecklistItem> cl74 = new ArrayList<>();
        cl74.add(new ChecklistItem("Complete manuscript attached", "Verified"));
        cl74.add(new ChecklistItem("Dean endorsement signed", "Verified"));
        List<ReviewLogItem> rev74 = new ArrayList<>();
        rev74.add(new ReviewLogItem("Completed", "Ana M. Reyes · CRM Staff", "Oct 14, 2026, 4:00 PM", false));
        rev74.add(new ReviewLogItem("In review", "Ana M. Reyes · CRM Staff", "Oct 13, 2026, 10:00 AM", false));
        rev74.add(new ReviewLogItem("Submitted", "Jonard L. Wala · Researcher", "Oct 12, 2026, 9:30 AM", false));

        QueueRequestItem req74 = new QueueRequestItem(
                "REQ-2026-074",
                "Completed",
                "Research Registration",
                "Quantum Computing Foundations",
                "Oct 12, 2026, 9:30 AM",
                "Jonard L. Wala",
                "Student",
                "College of Science",
                "jwala.K2210@umak.edu.ph",
                "Draft registration of academic study for database…",
                att74, cl74, rev74
        );
        req74.feedbackToResearcher = "Registration recorded under AY 2026-2027. Research ID: REG-2026-074.";
        req74.checkpointName = "Registration recorded";
        req74.checkpointDate = "2026-10-14";
        req74.checkpointRef = "CRM log 2026-10-118";
        list.add(req74);

        // REQ-2026-066 (Completed)
        List<AttachmentItem> att66 = new ArrayList<>();
        att66.add(new AttachmentItem("CLEARANCE_SUBMISSION.pdf", "1.8 MB"));
        List<ChecklistItem> cl66 = new ArrayList<>();
        cl66.add(new ChecklistItem("Defense clearance checklist satisfied", "Verified"));
        List<ReviewLogItem> rev66 = new ArrayList<>();
        rev66.add(new ReviewLogItem("Completed", "Lebron James · CRM Staff", "Oct 3, 2026, 2:00 PM", false));
        rev66.add(new ReviewLogItem("Submitted", "LeBron R. James · Researcher", "Oct 2, 2026, 4:05 PM", false));

        QueueRequestItem req66 = new QueueRequestItem(
                "REQ-2026-066",
                "Completed",
                "CRM Clearance Processing",
                "Gamified Learning Modules for Introductory Programming",
                "Oct 2, 2026, 4:05 PM",
                "LeBron R. James",
                "Student",
                "College of Computing and Information Sciences",
                "ljames.A1234@umak.edu.ph",
                "Clearance for final defense scheduling…",
                att66, cl66, rev66
        );
        list.add(req66);

        // Additional items for tabs: Submitted (3) and For correction (1)
        List<QueueRequestItem> extraSubmitted = createExtraSubmitted();
        list.addAll(extraSubmitted);

        List<QueueRequestItem> extraCorrection = createExtraCorrection();
        list.addAll(extraCorrection);

        return list;
    }

    private static List<QueueRequestItem> createExtraSubmitted() {
        List<QueueRequestItem> list = new ArrayList<>();
        list.add(new QueueRequestItem(
                "REQ-2026-092",
                "Submitted",
                "Research Registration",
                "Augmented Reality in Medical Training",
                "Nov 1, 2026, 8:15 AM",
                "Peter Parker",
                "Faculty",
                "College of Allied Health Studies",
                "pparker@umak.edu.ph",
                "New registration proposal for institutional funding review…",
                null, null, null
        ));
        list.add(new QueueRequestItem(
                "REQ-2026-090",
                "Submitted",
                "Ethics Clearance",
                "Biodegradable Plastics from Agricultural Waste",
                "Oct 30, 2026, 3:45 PM",
                "Maria S. Clara",
                "Faculty",
                "College of Science",
                "mclara@umak.edu.ph",
                "Protocol review for laboratory testing phase…",
                null, null, null
        ));
        list.add(new QueueRequestItem(
                "REQ-2026-088",
                "Submitted",
                "UNIVERSITY Journal Endorsement",
                "Cybersecurity Resilience in Local Banking",
                "Oct 28, 2026, 1:20 PM",
                "Andres B. Bonifacio",
                "Student",
                "College of Computing and Information Sciences",
                "abonifacio@umak.edu.ph",
                "Manuscript review for endorsement to university journal…",
                null, null, null
        ));
        return list;
    }

    private static List<QueueRequestItem> createExtraCorrection() {
        List<QueueRequestItem> list = new ArrayList<>();
        list.add(new QueueRequestItem(
                "REQ-2026-079",
                "For correction",
                "Statistical Report Generation",
                "Urban Transit Mobility Analysis in Makati City",
                "Oct 18, 2026, 11:30 AM",
                "Ramon S. Dizon",
                "Faculty",
                "College of Governance and Public Policy",
                "rdizon@umak.edu.ph",
                "Missing updated demographic survey dataset for regression analysis…",
                null, null, null
        ));
        return list;
    }
}
