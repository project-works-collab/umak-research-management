package ph.edu.umak.crm.app.staff;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StaffData {

    public static class ServiceCatalogItem {
        public int revisionId;
        public final String id;
        public final String title;
        public final String version;
        public final String updatedDate;
        public final String status; // "Draft", "Published", "Withdrawn"
        public final String visibilityNote; // e.g. "Not visible to researchers"

        public ServiceCatalogItem(String id, String title, String version, String updatedDate, String status, String visibilityNote) {
            this.id = id;
            this.title = title;
            this.version = version;
            this.updatedDate = updatedDate;
            this.status = status;
            this.visibilityNote = visibilityNote;
        }
    }

    public static class ApprovedFormItem {
        public String state="published";
        public boolean live;
        public final String serviceId;
        public final String serviceName;
        public final String formTitle;
        public final String currentVersion;
        public final List<String> supersededVersions;

        public ApprovedFormItem(String serviceId, String serviceName, String formTitle, String currentVersion, List<String> supersededVersions) {
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.formTitle = formTitle;
            this.currentVersion = currentVersion;
            this.supersededVersions = supersededVersions != null ? supersededVersions : new ArrayList<>();
        }
    }

    public static class PublicationHistoryItem {
        public final String serviceId;
        public final String title;
        public final String change;
        public final String badge; // "Replaced", "Published", "Withdrawn"
        public final String author;
        public final String timestamp;

        public PublicationHistoryItem(String serviceId, String title, String change, String badge, String author, String timestamp) {
            this.serviceId = serviceId;
            this.title = title;
            this.change = change;
            this.badge = badge;
            this.author = author;
            this.timestamp = timestamp;
        }
    }

    public static class StaffAccountItem {
        public int serverId;
        public final String initials;
        public final String name;
        public final String role; // "CRM Staff", "Researcher"
        public final String category; // "Student", "Faculty", or null
        public final String status; // "Active", "Inactive"
        public final String email;
        public final String identifier;
        public final Map<String, Boolean> permissions;

        public StaffAccountItem(String initials, String name, String role, String category, String status, String email, String identifier, Map<String, Boolean> permissions) {
            this.initials = initials;
            this.name = name;
            this.role = role;
            this.category = category;
            this.status = status;
            this.email = email;
            this.identifier = identifier;
            this.permissions = permissions != null ? permissions : new LinkedHashMap<>();
        }
    }

    public static class AccountAuditLogItem {
        public final String action;
        public final String previous;
        public final String next;
        public final String reason;
        public final String actor;
        public final String timestamp;

        public AccountAuditLogItem(String action, String previous, String next, String reason, String actor, String timestamp) {
            this.action = action;
            this.previous = previous;
            this.next = next;
            this.reason = reason;
            this.actor = actor;
            this.timestamp = timestamp;
        }
    }

    public static List<ServiceCatalogItem> getServiceCatalog() {
        List<ServiceCatalogItem> list = new ArrayList<>();
        list.add(new ServiceCatalogItem("SVC-09", "Plagiarism Checker", "Version 1", "Updated Oct 2026", "Draft", "Not visible to researchers"));
        list.add(new ServiceCatalogItem("SVC-01", "Research Registration", "Version 4", "Updated Oct 2026", "Published", null));
        list.add(new ServiceCatalogItem("SVC-02", "Ethics Review & Clearance", "Version 3", "Updated Sep 2026", "Published", null));
        list.add(new ServiceCatalogItem("SVC-03", "CRM Clearance Processing", "Version 2", "Updated Aug 2026", "Published", null));
        list.add(new ServiceCatalogItem("SVC-04", "UNIVERSITY Journal Endorsement", "Version 2", "Updated Jul 2026", "Published", null));
        list.add(new ServiceCatalogItem("SVC-06", "Statistical Report Generation", "Version 2", "Updated Jun 2026", "Withdrawn", null));
        return list;
    }

    public static ApprovedFormItem getApprovedForms() {
        List<String> superseded = new ArrayList<>();
        superseded.add("Version 2 · Updated Jan 2026 · Superseded Sep 2026");
        superseded.add("Version 1 · Updated Aug 2025 · Superseded Jan 2026");
        return new ApprovedFormItem(
                "SVC-02",
                "Ethics Review & Clearance",
                "CRM Form 004 – Ethics Application",
                "Version 3 · Updated Sep 2026",
                superseded
        );
    }

    public static List<PublicationHistoryItem> getPublicationHistory() {
        List<PublicationHistoryItem> list = new ArrayList<>();
        list.add(new PublicationHistoryItem("SVC-02", "Ethics Review & Clearance", "Form v3 -> v4", "Replaced", "Lebron James", "Oct 29, 2026, 4:05 PM"));
        list.add(new PublicationHistoryItem("SVC-01", "Research Registration", "v4", "Published", "Ana M. Reyes", "Oct 12, 2026, 10:20 AM"));
        list.add(new PublicationHistoryItem("SVC-02", "Ethics Review & Clearance", "v3", "Published", "Lebron James", "Sep 18, 2026, 2:47 PM"));
        list.add(new PublicationHistoryItem("SVC-06", "Statistical Report Generation", "v2", "Withdrawn", "Ana M. Reyes", "Jun 30, 2026, 9:02 AM"));
        return list;
    }

    public static List<StaffAccountItem> getUserAccounts() {
        List<StaffAccountItem> list = new ArrayList<>();

        Map<String, Boolean> staffPerms = new LinkedHashMap<>();
        staffPerms.put("Content management", true);
        staffPerms.put("Request processing", true);
        staffPerms.put("Registry management", true);
        staffPerms.put("Progress updates", true);
        staffPerms.put("Reporting", false);
        staffPerms.put("Account administration", false);

        list.add(new StaffAccountItem("LJ", "Lebron James (you)", "CRM Staff", null, "Active", "lebron.james@umak.edu.ph", "STF-0007", staffPerms));
        list.add(new StaffAccountItem("AR", "Ana M. Reyes", "CRM Staff", null, "Active", "areyes@umak.edu.ph", "STF-0042", staffPerms));
        list.add(new StaffAccountItem("RD", "Ramon S. Dizon", "CRM Staff", null, "Inactive", "rdizon@umak.edu.ph", "STF-0015", staffPerms));
        list.add(new StaffAccountItem("LJ", "LeBron R. James", "Researcher", "Student", "Active", "ljames.A1234@umak.edu.ph", "A1234", null));
        list.add(new StaffAccountItem("PP", "Peter Parker", "Researcher", "Faculty", "Active", "pparker@umak.edu.ph", "F-9021", null));
        list.add(new StaffAccountItem("JW", "Jonard L. Wala", "Researcher", "Student", "Inactive", "jwala.K2210@umak.edu.ph", "K2210", null));

        return list;
    }

    public static List<AccountAuditLogItem> getAccountAuditLogs() {
        List<AccountAuditLogItem> list = new ArrayList<>();
        list.add(new AccountAuditLogItem(
                "Researcher category corrected",
                "Faculty",
                "Student",
                "Registered as BS CS student; Faculty was selected in error.",
                "Lebron James (CRM Staff)",
                "Oct 30, 2026, 9:14 AM"
        ));
        list.add(new AccountAuditLogItem(
                "Temporary credential issued",
                "—",
                "Must be replaced at sign-in",
                null,
                "Lebron James (CRM Staff)",
                "Oct 22, 2026, 1:40 PM"
        ));
        list.add(new AccountAuditLogItem(
                "Account activated",
                "Inactive",
                "Active",
                null,
                "Ana M. Reyes (CRM Staff)",
                "Aug 4, 2026, 8:55 AM"
        ));
        list.add(new AccountAuditLogItem(
                "Account deactivated",
                "Active",
                "Inactive",
                null,
                "Ana M. Reyes (CRM Staff)",
                "Jun 1, 2026, 5:10 PM"
        ));
        return list;
    }
}
