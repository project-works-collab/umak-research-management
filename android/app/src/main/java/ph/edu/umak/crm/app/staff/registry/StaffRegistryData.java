package ph.edu.umak.crm.app.staff.registry;

import java.util.ArrayList;
import java.util.List;

/**
 * Data models and mock datasets for Staff Research Registry and Corrections (Part 7, C-03).
 */
public final class StaffRegistryData {

    private StaffRegistryData() {}

    public static class TeamMember {
        public final String name;
        public final String role; // "Principal author", "Co-author"
        public final String category; // "Student", "Faculty"
        public final String initials;
        public final String college;

        public TeamMember(String name, String role, String category, String initials, String college) {
            this.name = name;
            this.role = role;
            this.category = category;
            this.initials = initials;
            this.college = college;
        }
    }

    public static class DocumentVersion {
        public final String fileName;
        public final String fileSize;
        public final String date;
        public final String uploader;
        public final boolean isCurrent;

        public DocumentVersion(String fileName, String fileSize, String date, String uploader, boolean isCurrent) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.date = date;
            this.uploader = uploader;
            this.isCurrent = isCurrent;
        }
    }

    public static class LinkedRequest {
        public final String requestId;
        public final String serviceTitle;
        public final String status; // "For correction", "Submitted", "Completed", etc.

        public LinkedRequest(String requestId, String serviceTitle, String status) {
            this.requestId = requestId;
            this.serviceTitle = serviceTitle;
            this.status = status;
        }
    }

    public static class HistoricalEntry {
        public String entryType;
        public String sourceReference;
        public String sourceInstitution;
        public String originalRegistrationDate;

        public HistoricalEntry(String entryType, String sourceReference, String sourceInstitution, String originalRegistrationDate) {
            this.entryType = entryType;
            this.sourceReference = sourceReference;
            this.sourceInstitution = sourceInstitution;
            this.originalRegistrationDate = originalRegistrationDate;
        }
    }

    public static class CorrectionLog {
        public final String fieldChanged;
        public final String previousValue;
        public final String newValue;
        public final String reason;
        public final String authorAndDate;

        public CorrectionLog(String fieldChanged, String previousValue, String newValue, String reason, String authorAndDate) {
            this.fieldChanged = fieldChanged;
            this.previousValue = previousValue;
            this.newValue = newValue;
            this.reason = reason;
            this.authorAndDate = authorAndDate;
        }
    }

    public static class RegistryRecordItem {
        public final String id;
        public String title;
        public String stage; // "In progress", "Registered", "For revision", "Completed"
        public final String registeredDate;
        public String academicPeriod;
        public String authorsDisplay;
        public String college;
        public String facultyAdviser;
        public String registrantCategory; // "Student", "Faculty"
        public String lastEditDate;
        public final List<TeamMember> teamMembers;
        public final List<DocumentVersion> documents;
        public final List<LinkedRequest> linkedRequests;
        public HistoricalEntry historicalEntry;
        public final List<CorrectionLog> correctionLogs;

        public RegistryRecordItem(String id, String title, String stage, String registeredDate,
                                  String academicPeriod, String authorsDisplay, String college,
                                  String facultyAdviser, String registrantCategory, String lastEditDate,
                                  List<TeamMember> teamMembers, List<DocumentVersion> documents,
                                  List<LinkedRequest> linkedRequests, HistoricalEntry historicalEntry,
                                  List<CorrectionLog> correctionLogs) {
            this.id = id;
            this.title = title;
            this.stage = stage;
            this.registeredDate = registeredDate;
            this.academicPeriod = academicPeriod;
            this.authorsDisplay = authorsDisplay;
            this.college = college;
            this.facultyAdviser = facultyAdviser;
            this.registrantCategory = registrantCategory;
            this.lastEditDate = lastEditDate;
            this.teamMembers = teamMembers != null ? teamMembers : new ArrayList<>();
            this.documents = documents != null ? documents : new ArrayList<>();
            this.linkedRequests = linkedRequests != null ? linkedRequests : new ArrayList<>();
            this.historicalEntry = historicalEntry;
            this.correctionLogs = correctionLogs != null ? correctionLogs : new ArrayList<>();
        }
    }

    public static List<RegistryRecordItem> getMockRecords() {
        List<RegistryRecordItem> list = new ArrayList<>();

        // 1. REG-2026-089 (In progress)
        List<TeamMember> team89 = new ArrayList<>();
        team89.add(new TeamMember("LeBron R. James", "Principal author", "Student", "LJ", "CCIS"));
        team89.add(new TeamMember("Juan D. Inlwan", "Co-author", "Student", "JI", "CCIS"));
        team89.add(new TeamMember("Peter Parker", "Co-author", "Faculty", "PP", "CCIS"));

        List<DocumentVersion> docs89 = new ArrayList<>();
        docs89.add(new DocumentVersion("manuscript-v2.pdf", "2 MB", "Oct 29, 2026", "LeBron James (CRM Staff)", true));
        docs89.add(new DocumentVersion("manuscript-v1.pdf", "2 MB", "Oct 15, 2026", "LeBron R. James", false));

        List<LinkedRequest> reqs89 = new ArrayList<>();
        reqs89.add(new LinkedRequest("REQ-2026-089", "Ethics Clearance", "For correction"));
        reqs89.add(new LinkedRequest("REQ-2026-092", "CRM Clearance Processing", "Submitted"));

        HistoricalEntry hist89 = new HistoricalEntry(
                "Current registration",
                "Enter the archival source reference",
                "University of Makati",
                "Unknown — not recorded"
        );

        List<CorrectionLog> logs89 = new ArrayList<>();
        logs89.add(new CorrectionLog(
                "Research title",
                "Impact of AI to Student Engagement",
                "Impact of AI on Student Engagement",
                "Typo in the submitted title.",
                "LeBron James (CRM Staff) · Oct 29, 2026, 4:12 PM"
        ));
        logs89.add(new CorrectionLog(
                "Co-authors",
                "Juan D. Inlwan",
                "Juan D. Inlwan, Peter Parker",
                "Faculty co-author omitted at registration.",
                "LeBron James (CRM Staff) · Oct 28, 2026, 11:03 AM"
        ));
        logs89.add(new CorrectionLog(
                "Academic period",
                "AY 2025-2026",
                "AY 2026-2027",
                "Registered under the wrong academic period.",
                "Ana M. Reyes (CRM Staff) · Oct 27, 2026, 9:40 AM"
        ));

        list.add(new RegistryRecordItem(
                "REG-2026-089",
                "Impact of AI on Student Engagement",
                "In progress",
                "Oct 29, 2026",
                "AY 2026-2027",
                "LeBron R. James, et al.",
                "College of Computing and Information Sciences",
                "Dr. Maria A. Santos",
                "Student",
                "Oct 29, 2026 by Staff",
                team89,
                docs89,
                reqs89,
                hist89,
                logs89
        ));

        // 2. REG-2026-071 (Registered)
        List<TeamMember> team71 = new ArrayList<>();
        team71.add(new TeamMember("Juan D. Inlwan", "Principal author", "Student", "JI", "CCIS"));

        List<DocumentVersion> docs71 = new ArrayList<>();
        docs71.add(new DocumentVersion("ml_agriculture_v1.pdf", "1.8 MB", "Oct 26, 2026", "Juan D. Inlwan", true));

        List<LinkedRequest> reqs71 = new ArrayList<>();
        reqs71.add(new LinkedRequest("REQ-2026-081", "Research Consultation", "In review"));

        list.add(new RegistryRecordItem(
                "REG-2026-071",
                "Machine Learning on Agriculture",
                "Registered",
                "Oct 26, 2026",
                "AY 2026-2027",
                "Juan D. Inlwan",
                "College of Computing and Information Sciences",
                "Dr. Maria A. Santos",
                "Student",
                "Oct 26, 2026 by Staff",
                team71,
                docs71,
                reqs71,
                new HistoricalEntry("Current registration", "", "University of Makati", "Unknown — not recorded"),
                new ArrayList<>()
        ));

        // 3. REG-2026-052 (For revision)
        List<TeamMember> team52 = new ArrayList<>();
        team52.add(new TeamMember("Reyes, J. B.", "Principal author", "Faculty", "JR", "CET"));
        team52.add(new TeamMember("Tan, L. M.", "Co-author", "Faculty", "LT", "CET"));

        List<DocumentVersion> docs52 = new ArrayList<>();
        docs52.add(new DocumentVersion("urban_drainage_proposal.pdf", "3.1 MB", "Sep 8, 2026", "Reyes, J. B.", true));

        list.add(new RegistryRecordItem(
                "REG-2026-052",
                "Sustainable Urban Drainage Solutions for Flood-Prone Barangays",
                "For revision",
                "Sep 8, 2026",
                "AY 2026-2027",
                "Reyes, J. B., Tan, L. M.",
                "College of Engineering and Technology",
                "Engr. Roberto Cruz",
                "Faculty",
                "Sep 8, 2026 by Staff",
                team52,
                docs52,
                new ArrayList<>(),
                new HistoricalEntry("Current registration", "", "University of Makati", "Unknown — not recorded"),
                new ArrayList<>()
        ));

        // 4. REG-2025-041 (Completed)
        List<TeamMember> team41 = new ArrayList<>();
        team41.add(new TeamMember("LeBron R. James", "Principal author", "Student", "LJ", "CCIS"));

        List<DocumentVersion> docs41 = new ArrayList<>();
        docs41.add(new DocumentVersion("gamified_modules_final.pdf", "4.2 MB", "Aug 14, 2025", "LeBron R. James", true));

        list.add(new RegistryRecordItem(
                "REG-2025-041",
                "Gamified Learning Modules for Introductory Programming",
                "Completed",
                "Aug 14, 2025",
                "AY 2025-2026",
                "LeBron R. James",
                "College of Computing and Information Sciences",
                "Dr. Maria A. Santos",
                "Student",
                "Aug 14, 2025 by Staff",
                team41,
                docs41,
                new ArrayList<>(),
                new HistoricalEntry("Current registration", "", "University of Makati", "Unknown — not recorded"),
                new ArrayList<>()
        ));

        return list;
    }
}
