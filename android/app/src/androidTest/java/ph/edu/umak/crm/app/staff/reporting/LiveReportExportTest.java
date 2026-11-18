package ph.edu.umak.crm.app.staff.reporting;

import static org.junit.Assert.*;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipFile;
import org.json.JSONObject;
import org.junit.Test;

public class LiveReportExportTest {
    @Test public void liveSnapshotProducesReadablePdfAndMatchingWorkbook() throws Exception {
        JSONObject snapshot = new JSONObject("{\"type\":\"research\",\"title\":\"Research Report\","
                + "\"generated_at\":\"2026-11-19T01:23:45Z\",\"filters\":{\"category\":\"student\"},"
                + "\"metrics\":[[\"Distinct studies\",1],[\"Distinct linked researchers\",2]],"
                + "\"service_counts\":[],\"definitions\":[\"Each study is counted once.\"],"
                + "\"columns\":[\"Research ID\",\"Title\",\"Current stage\"],"
                + "\"rows\":[[\"REG-42\",\"Stored research title\",\"Completed\"]],"
                + "\"internal_remarks\":\"DO-NOT-EXPORT\",\"email\":\"private@example.test\"}");
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File cache = Files.createTempDirectory(context.getCacheDir().toPath(), "report-test-").toFile();
        try {
            ReportExportData report = ReportExportData.fromLive(snapshot);
            File workbook = ReportExportWriter.write(cache, report, false);
            try (ZipFile zip = new ZipFile(workbook)) {
                String sheet = new String(zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")).readAllBytes(), StandardCharsets.UTF_8);
                for (String expected : new String[]{"2026-11-19T01:23:45Z", "student", "Distinct studies", "Stored research title", "Each study is counted once.", "REG-42"})
                    assertTrue(expected, sheet.contains(expected));
                assertFalse(sheet.contains("private@example.test"));
                assertFalse(sheet.contains("DO-NOT-EXPORT"));
                assertTrue(sheet.contains("<v>2</v>"));
            }
            File pdf = ReportExportWriter.write(cache, report, true);
            try (ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY);
                 PdfRenderer renderer = new PdfRenderer(descriptor)) {
                assertTrue(renderer.getPageCount() > 0);
                try (PdfRenderer.Page page = renderer.openPage(0)) { assertTrue(page.getWidth() > 0); }
            }
        } finally { remove(cache); }
    }

    @Test public void failedFileCreationDoesNotClaimAnExport() throws Exception {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File blocked = File.createTempFile("blocked-report-", ".tmp", context.getCacheDir());
        try {
            JSONObject snapshot = new JSONObject("{\"type\":\"service\",\"title\":\"Service Utilization\",\"generated_at\":\"now\","
                    + "\"filters\":{},\"metrics\":[],\"service_counts\":[],\"definitions\":[],"
                    + "\"columns\":[\"ID\",\"Service\",\"Status\"],\"rows\":[]}");
            try { ReportExportWriter.write(blocked, ReportExportData.fromLive(snapshot), false); fail("Expected file creation to fail"); }
            catch (java.io.IOException expected) { assertTrue(blocked.isFile()); }
        } finally { blocked.delete(); }
    }
    private static void remove(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) remove(child);
        file.delete();
    }
}
