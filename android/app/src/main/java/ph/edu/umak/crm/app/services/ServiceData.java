package ph.edu.umak.crm.app.services;

import java.util.ArrayList;
import java.util.List;

public final class ServiceData {
    public static class DocumentItem {
        public final String title;
        public final boolean required;

        public DocumentItem(String title, boolean required) {
            this.title = title;
            this.required = required;
        }
    }

    public static class ServiceItem {
        public final int id;
        public final String code;
        public final String name;
        public final String[] tags;
        public final String turnaround;
        public final String cardDescription;
        public final String[] cardRequirements;
        public final String channel; // "Submit in app" or "External channel"
        public final String purpose;
        public final String whoCanApplyText;
        public final String[] whoCanApplyTags;
        public final String responsibleContact;
        public final String contactEmail;
        public final String[] instructions;
        public final DocumentItem[] documents;
        public final String formTitle;
        public final String formVersion;

        public ServiceItem(int id, String code, String name, String[] tags, String turnaround,
                String cardDescription, String[] cardRequirements, String channel,
                String purpose, String whoCanApplyText, String[] whoCanApplyTags,
                String responsibleContact, String contactEmail,
                String[] instructions, DocumentItem[] documents,
                String formTitle, String formVersion) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.tags = tags;
            this.turnaround = turnaround;
            this.cardDescription = cardDescription;
            this.cardRequirements = cardRequirements;
            this.channel = channel;
            this.purpose = purpose;
            this.whoCanApplyText = whoCanApplyText;
            this.whoCanApplyTags = whoCanApplyTags;
            this.responsibleContact = responsibleContact;
            this.contactEmail = contactEmail;
            this.instructions = instructions;
            this.documents = documents;
            this.formTitle = formTitle;
            this.formVersion = formVersion;
        }
    }

    public static final List<ServiceItem> ALL_SERVICES = new ArrayList<>();

    static {
        ALL_SERVICES.add(new ServiceItem(
                1, "SVC-01", "Research Registration",
                new String[]{"Student", "Faculty"}, "3–5 business days",
                "Officially register your undergraduate or graduate research study with the Center for Research Management.",
                new String[]{"Completed CRM Form 001", "Research proposal", "Adviser endorsement"},
                "Submit in app",
                "Officially register your undergraduate or graduate research study with the Center for Research Management to establish institutional record and access university research facilities.",
                "Undergraduate and graduate researchers of the University of Makati embarking on academic studies.",
                new String[]{"Student", "Faculty"},
                "Center for Research Management · Registration Unit",
                "research@umak.edu.ph",
                new String[]{
                        "Sign in to your Researcher account.",
                        "Download and complete CRM Form 001 – Research Registration.",
                        "Prepare proposal manuscript and endorsement letter.",
                        "Tap Submit Request and attach your files.",
                        "Track the review status in Requests."
                },
                new DocumentItem[]{
                        new DocumentItem("Completed CRM Form 001", true),
                        new DocumentItem("Research proposal", true),
                        new DocumentItem("Adviser endorsement letter", true)
                },
                "CRM Form 001 – Research Registration",
                "Version 2 · Updated Jan 2026"
        ));

        ALL_SERVICES.add(new ServiceItem(
                2, "SVC-02", "Ethics Review & Clearance",
                new String[]{"Student", "Faculty"}, "10–15 business days",
                "Secure ethics approval for studies involving human participants, sensitive data or biological materials.",
                new String[]{"CRM Form 004 – Ethics Application", "Research protocol", "Informed consent form"},
                "Submit in app",
                "Secure ethics approval for studies involving human participants, sensitive data or biological materials before data collection begins.",
                "Registered researchers whose study involves human participants or personal data.",
                new String[]{"Student", "Faculty"},
                "University Research Ethics Committee · Center for Research Management",
                "research@umak.edu.ph",
                new String[]{
                        "Sign in to your Researcher account.",
                        "Download and complete CRM Form 004 – Ethics Application.",
                        "Prepare the documents listed below as PDF or DOCX.",
                        "Tap Submit Request and attach your files.",
                        "Track the review status in Requests."
                },
                new DocumentItem[]{
                        new DocumentItem("CRM Form 004 – Ethics Application", true),
                        new DocumentItem("Research protocol", true),
                        new DocumentItem("Informed consent form", true),
                        new DocumentItem("Data collection instruments", true),
                        new DocumentItem("Adviser endorsement letter", false)
                },
                "CRM Form 004 – Ethics Application",
                "Version 3 · Updated Sep 2026"
        ));

        ALL_SERVICES.add(new ServiceItem(
                3, "SVC-03", "CRM Clearance Processing",
                new String[]{"Student", "Faculty"}, "5–7 business days",
                "Secure official CRM clearance for graduation, thesis defense or external publication.",
                new String[]{"Completed research manuscript", "Ethics clearance", "Plagiarism report"},
                "Submit in app",
                "Secure official CRM clearance required for graduation, thesis oral defense eligibility or external journal publication.",
                "Researchers preparing for oral defense, final manuscript binding, or graduation clearance.",
                new String[]{"Student", "Faculty"},
                "Center for Research Management · Clearance Unit",
                "research@umak.edu.ph",
                new String[]{
                        "Sign in to your Researcher account.",
                        "Download and complete CRM Form 007 – Clearance Request.",
                        "Upload complete manuscript, ethics clearance, and similarity report.",
                        "Tap Submit Request and track approval.",
                        "Receive official clearance certificate upon completion."
                },
                new DocumentItem[]{
                        new DocumentItem("Completed research manuscript", true),
                        new DocumentItem("Ethics clearance certificate", true),
                        new DocumentItem("Plagiarism similarity report (< 15%)", true),
                        new DocumentItem("Adviser approval sheet", true)
                },
                "CRM Form 007 – Clearance Request",
                "Version 2 · Updated Mar 2026"
        ));

        ALL_SERVICES.add(new ServiceItem(
                4, "SVC-04", "UNIVERSITY Journal Endorsement",
                new String[]{"Graduate", "Faculty"}, "20–30 business days",
                "Submit your completed research for consideration in UNIVERSITY, the official peer-reviewed journal of UMak.",
                new String[]{"Camera-ready manuscript", "Author declaration form", "CRM clearance certificate"},
                "External channel",
                "Submit your completed research for consideration in UNIVERSITY, the official peer-reviewed journal of the University of Makati.",
                "Authors with a completed study and a CRM clearance certificate.",
                new String[]{"Graduate", "Faculty"},
                "UNIVERSITY Editorial Office · Center for Research Management",
                "research@umak.edu.ph",
                new String[]{
                        "Download CRM Form 011 – Journal Endorsement Request.",
                        "Format your manuscript using the UNIVERSITY author guidelines.",
                        "Prepare the documents listed below.",
                        "Send everything through the external channel in the submission instructions.",
                        "Wait for the editorial office to confirm receipt by email."
                },
                new DocumentItem[]{
                        new DocumentItem("Camera-ready manuscript (UNIVERSITY format)", true),
                        new DocumentItem("Author declaration form", true),
                        new DocumentItem("Conflict of interest disclosure", true),
                        new DocumentItem("CRM clearance certificate", true),
                        new DocumentItem("Cover letter", false)
                },
                "CRM Form 011 – Journal Endorsement Request",
                "Version 2 · Updated Aug 2026"
        ));

        ALL_SERVICES.add(new ServiceItem(
                5, "SVC-05", "Research Consultation",
                new String[]{"Student", "Faculty"}, "2–3 business days (scheduling)",
                "Schedule a one-on-one consultation with a CRM adviser on methodology, statistical analysis or publication.",
                new String[]{"Concept paper or working draft", "Specific questions or areas for review"},
                "Submit in app",
                "Schedule an in-depth consultation session with CRM research fellows on study design, sampling, statistical treatment, or instrument validation.",
                "Students and faculty members needing technical guidance during any phase of their research.",
                new String[]{"Student", "Faculty"},
                "Center for Research Management · Consultation Desk",
                "research@umak.edu.ph",
                new String[]{
                        "Sign in to your Researcher account.",
                        "Prepare your concept paper or draft manuscript.",
                        "Specify your preferred consultation focus and questions.",
                        "Submit request and select an available consultation schedule.",
                        "Meet with the CRM technical adviser."
                },
                new DocumentItem[]{
                        new DocumentItem("Concept paper or working draft", true),
                        new DocumentItem("Specific questions or areas for review", true)
                },
                "CRM Form 005 – Consultation Intake",
                "Version 1 · Updated Jan 2026"
        ));

        ALL_SERVICES.add(new ServiceItem(
                6, "SVC-06", "Statistical Report Generation",
                new String[]{"Faculty", "CRM Staff"}, "3 business days",
                "Request official university-wide statistical summaries on research output, service utilization and trends.",
                new String[]{"Formal request letter", "Purpose of report", "College or department scope"},
                "Submit in app",
                "Request university-level statistical digests, analytics on institutional research productivity, and accreditation support data.",
                "Faculty researchers, department heads, and academic administrators.",
                new String[]{"Faculty", "CRM Staff"},
                "Center for Research Management · Data & Statistics Unit",
                "research@umak.edu.ph",
                new String[]{
                        "Sign in to your Researcher account.",
                        "Complete the statistical inquiry form.",
                        "Attach formal request letter signed by Dean or Department Head.",
                        "Submit request and await data generation.",
                        "Receive verified statistical report."
                },
                new DocumentItem[]{
                        new DocumentItem("Formal request letter", true),
                        new DocumentItem("Statement of purpose and data scope", true)
                },
                "CRM Form 009 – Statistical Request",
                "Version 1 · Updated Feb 2026"
        ));

        ALL_SERVICES.add(new ServiceItem(
                7, "SVC-07", "Manuscript Revision Processing",
                new String[]{"Student", "Faculty"}, "5–7 business days",
                "Submit revised manuscripts after panel evaluation or CRM review so staff can track the revision history.",
                new String[]{"Revised manuscript", "Revision response matrix", "Original panel evaluation"},
                "Submit in app",
                "Submit post-defense revisions and compliance matrices to ensure all panel recommendations are properly addressed.",
                "Researchers with conditional pass or revision requirements from their oral evaluation panel.",
                new String[]{"Student", "Faculty"},
                "Center for Research Management · Evaluation Unit",
                "research@umak.edu.ph",
                new String[]{
                        "Sign in to your Researcher account.",
                        "Compile the point-by-point revision matrix.",
                        "Highlight modifications in the revised manuscript.",
                        "Attach adviser verification of revisions.",
                        "Submit for final compliance verification."
                },
                new DocumentItem[]{
                        new DocumentItem("Revised manuscript with tracked changes", true),
                        new DocumentItem("Revision response matrix", true),
                        new DocumentItem("Original panel evaluation rubric", true),
                        new DocumentItem("Adviser confirmation of revisions", true)
                },
                "CRM Form 008 – Revision Matrix",
                "Version 2 · Updated May 2026"
        ));

        ALL_SERVICES.add(new ServiceItem(
                8, "SVC-08", "Repository Archiving",
                new String[]{"Student", "Faculty"}, "3 business days",
                "Submit your finalized research for inclusion in the CRM digital repository and Research Archive.",
                new String[]{"Final approved manuscript", "Abstract in English", "CRM clearance certificate"},
                "Submit in app",
                "Submit finalized manuscripts and metadata for permanent preservation and open access citation in the UMak Research Repository.",
                "Graduating students and faculty authors of completed and approved studies.",
                new String[]{"Student", "Faculty"},
                "Center for Research Management · Repository & Library Services",
                "research@umak.edu.ph",
                new String[]{
                        "Sign in to your Researcher account.",
                        "Prepare final PDF conforming to university archival standards.",
                        "Provide metadata: abstract, keywords, and citations.",
                        "Attach institutional clearance and copyright consent.",
                        "Submit for archiving and obtain permanent accession identifier."
                },
                new DocumentItem[]{
                        new DocumentItem("Final approved manuscript (PDF/A format)", true),
                        new DocumentItem("Structured abstract in English", true),
                        new DocumentItem("Institutional clearance certificate", true),
                        new DocumentItem("Repository deposit agreement", true)
                },
                "CRM Form 012 – Repository Archival Deposit",
                "Version 2 · Updated Apr 2026"
        ));
    }

    public static ServiceItem getById(int id) {
        for (ServiceItem item : ALL_SERVICES) {
            if (item.id == id) return item;
        }
        return ALL_SERVICES.get(1); // default SVC-02
    }

    public static ServiceItem getByCode(String code) {
        for (ServiceItem item : ALL_SERVICES) {
            if (item.code.equalsIgnoreCase(code)) return item;
        }
        return ALL_SERVICES.get(1);
    }
}
