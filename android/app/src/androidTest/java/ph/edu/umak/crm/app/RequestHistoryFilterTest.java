package ph.edu.umak.crm.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import ph.edu.umak.crm.app.requests.MyRequestsView;
import ph.edu.umak.crm.app.requests.RequestData;

public final class RequestHistoryFilterTest {
    @Test
    public void restoresSearchAndInclusiveDateRangeForLiveConsultationRows() throws JSONException {
        List<RequestData.RequestItem> rows = List.of(
                ticket(1, "Ethics consultation", "queued", "2026-10-29T09:00:00Z"),
                ticket(2, "Ethics consultation", "queued", "2026-10-28T09:00:00Z"),
                ticket(3, "Finance consultation", "queued", "2026-10-29T09:00:00Z"),
                ticket(4, "Ethics consultation", "resolved", "2026-10-29T09:00:00Z"));
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                MyRequestsView.Filters applied =
                        new MyRequestsView.Filters("Ethics", "Submitted", "2026-10-29", "2026-10-29");
                MyRequestsView view = new MyRequestsView(activity, null, false);
                view.restoreFilters(applied);
                view.showRequests(rows);
                assertEquals(applied, view.filters());
                View content = view.getContentLayout();
                assertNotNull(find(content, "#1"));
                assertNull(find(content, "#2"));
                assertNull(find(content, "#3"));
                assertNull(find(content, "#4"));

                MyRequestsView returned = new MyRequestsView(activity, null, false);
                returned.restoreFilters(view.filters());
                returned.showRequests(rows);
                View returnedContent = returned.getContentLayout();
                assertEquals(applied, returned.filters());
                assertNotNull(find(returnedContent, "#1"));
                assertNull(find(returnedContent, "#2"));
                returned.clearAllFilters();
                assertNotNull(find(returnedContent, "#2"));
                assertNotNull(find(returnedContent, "#3"));
                assertNotNull(find(returnedContent, "#4"));
            });
        }
    }

    @Test
    public void historyTargetsRemainLabeledAndReachableAtLargeText() throws JSONException {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                android.content.res.Configuration configuration = new android.content.res.Configuration(activity.getResources().getConfiguration());
                configuration.fontScale = 2f;
                configuration.screenWidthDp = 360;
                android.content.Context context = activity.createConfigurationContext(configuration);
                MyRequestsView history = new MyRequestsView(context, null, false);
                history.restoreFilters(new MyRequestsView.Filters("Ethics", "Submitted", "2026-10-29", "2026-10-29"));
                history.showRequests(List.of());
                View content = history.getContentLayout();
                int width = Math.round(360 * context.getResources().getDisplayMetrics().density);
                content.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                content.layout(0, 0, width, content.getMeasuredHeight());
                checkTargets(content, Math.round(48 * context.getResources().getDisplayMetrics().density), width);
                org.junit.Assert.assertNotNull(find(content, "Filter"));
            });
        }
    }

    private static void checkTargets(View view, int minimum, int pageWidth) {
        if (view.getVisibility() != View.VISIBLE) return;
        if (view.isClickable() && view.isEnabled()) {
            String label = String.valueOf(view.getContentDescription());
            if (view instanceof TextView text) {
                if (text.getText().length() > 0) label = text.getText().toString();
                else if (text.getHint() != null) label = text.getHint().toString();
            }
            org.junit.Assert.assertTrue("Missing accessible name", !label.equals("null") && !label.isBlank());
            org.junit.Assert.assertTrue(label + " height " + view.getMeasuredHeight(), view.getMeasuredHeight() >= minimum);
            org.junit.Assert.assertTrue(label + " width " + view.getMeasuredWidth(), view.getMeasuredWidth() >= minimum);
            int[] location = new int[2]; view.getLocationInWindow(location);
            org.junit.Assert.assertTrue(label + " exceeds page width", location[0] + view.getMeasuredWidth() <= pageWidth);
        }
        if (view instanceof ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) checkTargets(group.getChildAt(i), minimum, pageWidth);
    }

    private static RequestData.RequestItem ticket(int id, String service, String status, String submitted)
            throws JSONException {
        return RequestData.fromConsultation(new JSONObject().put("id", id)
                .put("service_type", service).put("status", status).put("submitted_at", submitted));
    }

    private static View find(View view, String label) {
        if (view instanceof TextView text && label.contentEquals(text.getText())) return view;
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = find(group.getChildAt(i), label);
                if (found != null) return found;
            }
        }
        return null;
    }
}
