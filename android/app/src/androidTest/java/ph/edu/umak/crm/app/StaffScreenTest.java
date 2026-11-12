package ph.edu.umak.crm.app;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class StaffScreenTest {
    @Test
    public void queueTabsRenderTheMatchingLiveStatus() throws JSONException {
        JSONArray tickets = new JSONArray("["
                + "{\"id\":1,\"service_type\":\"Ethics Review\",\"status\":\"queued\"},"
                + "{\"id\":2,\"service_type\":\"Manuscript Review\",\"status\":\"claimed\"},"
                + "{\"id\":3,\"service_type\":\"Research Registration\",\"status\":\"resolved\"}]");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                StaffScreen screen = new StaffScreen(activity, "staff@example.test", "Requests");
                activity.setContentView(screen);
                screen.showConsultations(tickets, null);

                assertNotNull(find(screen, "Review Queue"));
                assertNotNull(find(screen, "Ethics Review"));
                assertNull(find(screen, "Manuscript Review"));

                find(screen, "Under Review").performClick();
                assertNotNull(find(screen, "Manuscript Review"));
                assertNull(find(screen, "Ethics Review"));

                find(screen, "Completed").performClick();
                assertNotNull(find(screen, "Research Registration"));
                assertNull(find(screen, "Manuscript Review"));

                find(screen, "Profile").performClick();
                assertNotNull(find(screen, "staff@example.test"));
                assertNotNull(find(screen, "Account Settings"));
            });
        }
    }

    private static View find(View root, String label) {
        if (root instanceof TextView && label.contentEquals(((TextView) root).getText())) return root;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int index = 0; index < group.getChildCount(); index++) {
                View child = find(group.getChildAt(index), label);
                if (child != null) return child;
            }
        }
        return null;
    }
}
