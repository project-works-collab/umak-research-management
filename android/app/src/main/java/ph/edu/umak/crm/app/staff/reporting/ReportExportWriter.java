package ph.edu.umak.crm.app.staff.reporting;

import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class ReportExportWriter {
    private static final String SHEET_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String OFFICE_RELS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    private static final String PACKAGE_RELS = "http://schemas.openxmlformats.org/package/2006/relationships";
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 36;

    private ReportExportWriter() {}

    static File write(File cache, ReportExportData report, boolean pdf) throws IOException {
        File directory = Files.createDirectories(new File(cache, "report-exports").toPath()).toFile();
        File job = Files.createTempDirectory(directory.toPath(), "export-").toFile();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"));
        File file = new File(job, report.type + "_" + timestamp + (pdf ? ".pdf" : ".xlsx"));
        try (FileOutputStream output = new FileOutputStream(file)) {
            if (pdf) writePdf(report, output);
            else writeWorkbook(report, output);
        } catch (IOException | RuntimeException failure) {
            file.delete();
            job.delete();
            throw failure;
        }
        return file;
    }

    private static void writePdf(ReportExportData report, FileOutputStream output) throws IOException {
        TextPaint paint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(11);
        PdfDocument document = new PdfDocument();
        PdfDocument.Page page = null;
        try {
            int number = 1;
            page = startPage(document, number);
            int y = MARGIN;
            for (List<Object> row : report.rows) {
                StringBuilder line = new StringBuilder();
                for (Object value : row) {
                    if (line.length() > 0) line.append("   |   ");
                    line.append(value);
                }
                StaticLayout text = StaticLayout.Builder.obtain(line, 0, line.length(), paint, PAGE_WIDTH - 2 * MARGIN)
                        .setIncludePad(false).setAlignment(Layout.Alignment.ALIGN_NORMAL).build();
                int firstLine = 0;
                while (firstLine < text.getLineCount()) {
                    int endLine = firstLine;
                    int origin = text.getLineTop(firstLine);
                    while (endLine < text.getLineCount()
                            && y + text.getLineTop(endLine + 1) - origin <= PAGE_HEIGHT - MARGIN) {
                        endLine++;
                    }
                    if (endLine == firstLine) {
                        document.finishPage(page);
                        page = null;
                        page = startPage(document, ++number);
                        y = MARGIN;
                        continue;
                    }
                    int height = text.getLineTop(endLine) - origin;
                    page.getCanvas().save();
                    page.getCanvas().clipRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + height);
                    page.getCanvas().translate(MARGIN, y - origin);
                    text.draw(page.getCanvas());
                    page.getCanvas().restore();
                    y += height + 4;
                    firstLine = endLine;
                }
            }
            document.finishPage(page);
            page = null;
            document.writeTo(output);
        } finally {
            if (page != null) document.finishPage(page);
            document.close();
        }
    }

    private static PdfDocument.Page startPage(PdfDocument document, int pageNumber) {
        return document.startPage(new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create());
    }

    private static void writeWorkbook(ReportExportData report, FileOutputStream output) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            put(zip, "[Content_Types].xml",
                    "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                            + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                            + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                            + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                            + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                            + "</Types>");
            put(zip, "_rels/.rels", relationship("officeDocument", "xl/workbook.xml"));
            put(zip, "xl/_rels/workbook.xml.rels", relationship("worksheet", "worksheets/sheet1.xml"));
            put(zip, "xl/workbook.xml", "<workbook xmlns=\"" + SHEET_NS + "\" xmlns:r=\"" + OFFICE_RELS
                    + "\"><sheets><sheet name=\"Report\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            put(zip, "xl/worksheets/sheet1.xml", worksheet(report.rows));
        }
    }

    private static String relationship(String type, String target) {
        return "<Relationships xmlns=\"" + PACKAGE_RELS
                + "\"><Relationship Id=\"rId1\" Type=\"" + OFFICE_RELS + "/" + type
                + "\" Target=\"" + target + "\"/></Relationships>";
    }

    private static String worksheet(List<List<Object>> rows) throws IOException {
        StringWriter output = new StringWriter();
        XmlSerializer xml = Xml.newSerializer();
        xml.setOutput(output);
        xml.setPrefix("", SHEET_NS);
        xml.startTag(SHEET_NS, "worksheet");
        xml.startTag(SHEET_NS, "cols");
        xml.startTag(SHEET_NS, "col").attribute(null, "min", "1").attribute(null, "max", "5")
                .attribute(null, "width", "48").attribute(null, "customWidth", "1").endTag(SHEET_NS, "col");
        xml.endTag(SHEET_NS, "cols");
        xml.startTag(SHEET_NS, "sheetData");
        for (int index = 0; index < rows.size(); index++) {
            xml.startTag(SHEET_NS, "row").attribute(null, "r", Integer.toString(index + 1));
            for (Object value : rows.get(index)) {
                xml.startTag(SHEET_NS, "c");
                if (value instanceof Number) {
                    xml.startTag(SHEET_NS, "v").text(value.toString()).endTag(SHEET_NS, "v");
                } else {
                    xml.attribute(null, "t", "inlineStr").startTag(SHEET_NS, "is");
                    xml.startTag(SHEET_NS, "t").attribute("http://www.w3.org/XML/1998/namespace", "space", "preserve")
                            .text(value.toString().replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\uFFFE\\uFFFF]", ""))
                            .endTag(SHEET_NS, "t").endTag(SHEET_NS, "is");
                }
                xml.endTag(SHEET_NS, "c");
            }
            xml.endTag(SHEET_NS, "row");
        }
        xml.endTag(SHEET_NS, "sheetData").endTag(SHEET_NS, "worksheet");
        xml.flush();
        return output.toString();
    }

    private static void put(ZipOutputStream zip, String path, String value) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(value.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
