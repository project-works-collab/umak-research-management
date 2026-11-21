package ph.edu.umak.crm.app;

import static org.junit.Assert.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import ph.edu.umak.crm.app.staff.queue.LiveStaffRequestsView;
import ph.edu.umak.crm.app.staff.queue.StaffRequestData;

public final class LiveRequestDesignTest {
    private static JSONObject record(String status, int version) throws Exception {
        return new JSONObject().put("id", 71).put("version", version).put("status", status)
                .put("service", "Real custom service").put("subject", "Live request subject")
                .put("description", "Actual submitted description").put("submitted_at", "2026-11-20T18:30:00Z")
                .put("requester_category", "faculty").put("requester_college", "Actual College")
                .put("requester_name", "Actual Researcher").put("requester_email", "actual@example.test")
                .put("requirements", new JSONArray().put(new JSONObject().put("title", "Consent"))
                        .put(new JSONObject().put("title", "Protocol")))
                .put("attachments", new JSONArray().put(new JSONObject().put("id", "real-file-id")
                        .put("filename", "real.pdf").put("mime", "application/pdf")))
                .put("reviews", new JSONArray());
    }

    @Test public void adapterPreservesLatestReviewAndUsesPhilippineDeadlineTime() throws Exception {
        JSONObject row = record("In review", 8);
        row.getJSONArray("reviews").put(new JSONObject().put("to_status", "In review").put("reviewer_id", 13)
                .put("reviewed_at", "2026-11-20T04:00:00Z").put("feedback", "Existing feedback")
                .put("internal_remark", "Private note").put("next_action", "Replace protocol")
                .put("checklist", new JSONArray().put("Verified").put("Needs correction"))
                .put("missing_items", new JSONArray().put(1)).put("deadline_at", "2026-11-21T09:00:00Z")
                .put("checkpoint", new JSONObject().put("name", "Actual checkpoint").put("date", "2026-11-20").put("reference", "REF")));
        var item = StaffRequestData.fromJson(row);
        assertEquals("2026-11-21", item.submittedDate);
        assertEquals("17:00", item.deadlineTime);
        assertEquals("Existing feedback", item.feedbackToResearcher);
        assertEquals("Private note", item.internalRemarks);
        assertEquals("real-file-id", item.attachments.get(0).fileId);
        assertEquals("", item.attachments.get(0).fileSize);
        item.targetStatus = "For correction";
        JSONObject body = StaffRequestData.review(item);
        assertEquals(8, body.getInt("expected_version"));
        assertEquals(1, body.getJSONArray("missing_items").getInt(0));
        assertEquals("2026-11-21T09:00:00Z", body.getString("deadline_at"));
        assertEquals("Actual checkpoint", body.getJSONObject("checkpoint").getString("name"));
        item.deadlineDate = "2026-02-30";
        assertThrows(IllegalArgumentException.class, () -> StaffRequestData.review(item));
        item.deadlineDate = item.deadlineTime = "";
        item.missingRequirementIndices.clear();
        assertThrows(IllegalArgumentException.class, () -> StaffRequestData.review(item));
    }

    @Test public void designedWorkspaceSavesActualFieldsAndKeepsConflictEdits() throws Exception {
        FakeApi api = new FakeApi(record("Submitted", 1));
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                LiveStaffRequestsView view = new LiveStaffRequestsView(activity, api);
                activity.setContentView(view); view.load();
                assertNotNull(find(view, "Review Queue"));
                assertNull(find(view, "REQ-2026-089"));
                click(view, "REQ-71");
                assertNotNull(find(view, "Requester profile"));
                assertNotNull(find(view, "Actual Researcher"));
                assertNotNull(find(view, "Actual submitted description"));
                assertEquals("", editor(view, "Processing checkpoint").getText().toString());
                click(view, "Open real.pdf");
                assertEquals("/71/files/real-file-id", api.download);
                editor(view, "Researcher-visible feedback").setText("Start real review");
                click(view, "Save Review");
                assertEquals("In review", api.saved.optString("status"));
                assertEquals(1, api.saved.optInt("expected_version"));
                assertNotNull(find(view, "Review saved."));
                assertNotNull(find(view, "Current status: In review"));
                api.conflict = true;
                editor(view, "Researcher-visible feedback").setText("Do not lose this edit");
                click(view, "For correction");
                // The missing-items card maps requirement selection to API indices.
                check(view, "Protocol").setChecked(true);
                editor(view, "Next action").setText("Replace the protocol");
                click(view, "Save Review");
                assertEquals(1, api.saved.optJSONArray("missing_items").optInt(0));
                assertEquals("Do not lose this edit", editor(view, "Researcher-visible feedback").getText().toString());
                assertFalse(find(view, "Save Review").isEnabled());
                assertNotNull(find(view, "Reload request"));
                assertEquals(1, api.successfulWrites);
            });
        }
    }

    @Test public void sessionDraftResumesAndCompletedCorrectionRequiresReason() throws Exception {
        FakeApi api = new FakeApi(record("In review", 4));
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                LiveStaffRequestsView view = new LiveStaffRequestsView(activity, api);
                activity.setContentView(view); view.load(); click(view, "REQ-71");
                editor(view, "Researcher-visible feedback").setText("Session-only edit");
                click(view, "Keep draft for this session");
                assertEquals(0, api.successfulWrites);
                click(view, "REQ-71");
                assertEquals("Session-only edit", editor(view, "Researcher-visible feedback").getText().toString());
                click(view, "Completed"); click(view, "Save Review");
                assertNotNull(find(view, "Save Correction"));
                int written = api.successfulWrites;
                click(view, "Save Correction");
                assertEquals(written, api.successfulWrites);
                assertNotNull(find(view, "Enter the correction reason."));
                editor(view, "Reason for correcting this completed request").setText("Correct final feedback");
                editor(view, "Researcher-visible feedback").setText("Corrected final text");
                click(view, "Save Correction");
                assertEquals("Completed", api.saved.optString("status"));
                assertEquals("Correct final feedback", api.saved.optString("correction_reason"));
                assertEquals("Corrected final text", api.saved.optString("feedback"));
                assertTrue(view.handleBack());
                assertNotNull(find(view, "Review Queue"));
                assertFalse(view.handleBack());
            });
        }
    }

    @Test public void correctionWaitingStateRemainsReadOnlyAndPermissionHidesRequests() throws Exception {
        FakeApi api = new FakeApi(record("For correction", 3));
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                LiveStaffRequestsView view = new LiveStaffRequestsView(activity, api);
                activity.setContentView(view); view.load();
                click(view, "For correction (1)"); click(view, "REQ-71");
                assertNull(find(view, "Save Review")); assertNull(find(view, "Save Correction"));
                assertNotNull(find(view, "Open real.pdf"));
                assertNotNull(find(view, "Waiting for the researcher to resubmit. This request cannot be reviewed yet."));
                StaffScreen restricted = new StaffScreen(activity, "staff@example.test", "Dashboard", true, new JSONArray());
                activity.setContentView(restricted);
                assertNull(find(restricted, "Requests"));
                restricted.openRequestsSubPage("queue");
                assertNotNull(find(restricted, "Access denied"));
                assertNull(find(restricted, "Review Queue"));
            });
        }
    }

    @Test public void queueFiltersUseSubmissionDatesAndAllTabsFitAtLargeText() throws Exception {
        JSONObject older = record("Submitted", 1).put("id", 72).put("submitted_at", "2026-11-20T00:30:00Z");
        var today = StaffRequestData.fromJson(record("Submitted", 1));
        var yesterday = StaffRequestData.fromJson(older);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                android.content.res.Configuration configuration = new android.content.res.Configuration(activity.getResources().getConfiguration());
                configuration.fontScale = 2f;
                android.content.Context context = activity.createConfigurationContext(configuration);
                var queue = new ph.edu.umak.crm.app.staff.queue.ReviewQueueView(context, null);
                queue.showLiveRequests(java.util.List.of(today, yesterday), null);
                queue.applyFilters("Real custom service", "Submitted", "2026-11-21", "2026-11-21");
                View root = queue.getContentLayout();
                assertNotNull(find(root, "REQ-71"));
                assertNull(find(root, "REQ-72"));
                int width = Math.round(360 * context.getResources().getDisplayMetrics().density);
                root.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                root.layout(0, 0, width, root.getMeasuredHeight());
                for (String label : new String[]{"In review (0)", "Submitted (2)", "For correction (0)", "Completed (0)"}) {
                    View tab = find(root, label);
                    assertNotNull(tab);
                    assertTrue(label, tab.getWidth() > 0);
                    assertTrue(label, tab.getRight() <= ((View) tab.getParent()).getWidth());
                }
                queue.applyFilters("All Services", "Submitted", "", "");
                assertNotNull(find(root, "REQ-72"));
            });
        }
    }

    @Test public void referenceSelectorWorksAfterLiveStaffWorkspaceWasCreated() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                StaffScreen staff = new StaffScreen(activity, "staff@example.test", "Dashboard", true,
                        new JSONArray().put("request_processing"));
                activity.setContentView(staff);
                staff.showPreview6_1_InReviewTab();
                assertNotNull(find(staff, "REQ-2026-089"));
                assertNotNull(find(staff, "Review Queue"));
                assertNull(find(staff, "Service request review queue"));
                assertTrue(staff.handleBack());
                assertNotNull(find(staff, "Staff workspace"));
                assertNull(find(staff, "REQ-2026-089"));
            });
        }
    }

    private static final class FakeApi implements LiveStaffRequestsView.Actions {
        JSONObject record, saved;
        boolean conflict;
        int successfulWrites;
        String download;
        FakeApi(JSONObject record) { this.record = record; }
        public void request(String path, JSONObject body, BiConsumer<String, String> callback) {
            try {
                if (body == null) {
                    callback.accept(path.equals("/71") ? record.toString() : new JSONArray().put(record).toString(), null);
                    return;
                }
                saved = new JSONObject(body.toString());
                if (conflict) { callback.accept(null, "This request was updated. Reload before saving"); return; }
                assertEquals("/71/reviews", path);
                assertEquals(record.getInt("version"), body.getInt("expected_version"));
                successfulWrites++;
                record.put("status", body.getString("status")).put("version", record.getInt("version") + 1);
                JSONObject review = new JSONObject(body.toString()).put("reviewer_id", 13)
                        .put("reviewed_at", "2026-11-20T10:00:00Z").put("to_status", body.getString("status"));
                record.put("reviews", new JSONArray().put(review));
                callback.accept(record.toString(), null);
            } catch (Exception error) { throw new AssertionError(error); }
        }
        public void download(String path, String name, String mime, Runnable failure) { download = path; }
        public void back() {}
    }

    private static EditText editor(View view, String label) {
        if (view instanceof EditText input && label.contentEquals(input.getContentDescription())) return input;
        if (view instanceof ViewGroup group) for (int i=0;i<group.getChildCount();i++) {
            EditText found=editor(group.getChildAt(i),label); if(found!=null)return found;
        }
        return null;
    }
    private static CheckBox check(View view, String label) {
        if(view instanceof CheckBox box && label.contentEquals(box.getText()))return box;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){CheckBox result=check(group.getChildAt(i),label);if(result!=null)return result;}
        return null;
    }
    private static View find(View view, String label) {
        if(view instanceof TextView text && label.contentEquals(text.getText()))return view;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){View result=find(group.getChildAt(i),label);if(result!=null)return result;}
        return null;
    }
    private static void click(View view,String label){View button=find(view,label);assertNotNull(label,button);if(button instanceof android.widget.CompoundButton){button.performClick();}else assertTrue(label,button.performClick());}
}
