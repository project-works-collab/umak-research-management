package ph.edu.umak.crm.app;

final class ResearcherCatalog {
    private ResearcherCatalog() {}

    static final String[][] SERVICES = {
            {"Research Registration", "Student · Faculty", "Register your undergraduate or graduate research study with CRM.",
                    "• Completed CRM form\n• Adviser endorsement letter\n• Research title and abstract\n• Proponent information sheet"},
            {"Ethics Review & Clearance", "Student · Faculty", "Request an ethics review for research involving human participants, sensitive data, or biological elements.",
                    "• Ethics application\n• Research protocol or methodology\n• Informed consent templates\n• Data collection instruments"},
            {"CRM Clearance Processing", "Student · Faculty", "Request CRM clearance for graduation, defense eligibility or external publication.",
                    "• Completed research manuscript\n• Final evaluation form\n• Similarity report\n• Final adviser approval"},
            {"UNIVERSITY Journal Endorsement", "Faculty · Graduate Students", "Submit your completed research for consideration in the UMak research journal.",
                    "• Camera-ready manuscript\n• Author declaration form\n• Conflict of interest disclosure\n• CRM clearance certificate"},
            {"Research Consultation", "Student · Faculty", "Request guidance on research methodology, statistical analysis or publication readiness.",
                    "• Concept paper or working draft\n• Specific questions or areas for review"},
            {"Statistical Report Generation", "Faculty · CRM Staff · Admin", "Request university-wide research output or service utilization reports.",
                    "• Formal request letter\n• Purpose of report\n• College or department scope"},
            {"Manuscript Revision Processing", "Student · Faculty", "Submit a revised manuscript after panel evaluation or CRM feedback.",
                    "• Revised manuscript\n• Revision response letter\n• Original panel evaluation sheet"},
            {"Repository Archiving", "Student · Faculty", "Submit finalized research for inclusion in the CRM digital repository.",
                    "• Final approved manuscript\n• Abstract in English\n• Keywords list\n• CRM clearance certificate"}
    };

    static final String[][] GUIDELINES = {
            {"Research Registration Guide", "Prepare the research title, abstract and supporting documents before registration."},
            {"Ethics Review Requirements", "Prepare the study protocol, participant consent forms and data collection materials."},
            {"CRM Clearance Checklist", "Prepare the completed manuscript, final evaluation and adviser approval."},
            {"UNIVERSITY Author Guidelines", "Review the journal requirements before submitting your manuscript."},
            {"Data Management Standards", "Secure collection, retention, privacy, sharing and disposal of research data."}
    };
}
