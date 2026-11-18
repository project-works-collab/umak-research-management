package ph.edu.umak.crm.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ph.edu.umak.crm.app.auth.ActivationView;

@RunWith(AndroidJUnit4.class)
public final class PendingFlowTest {
    @Test
    public void queueRefreshShowsActualRowsAndKeepsThemOnFailure() throws JSONException {
        JSONArray tickets = new JSONArray().put(new JSONObject()
                .put("id", 17).put("status", "queued").put("service_type", "Consultation topic")
                .put("submitted_at", "2026-11-19T00:00:00.000Z"));
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                StaffScreen root = new StaffScreen(activity, "staff@example.test", "Requests");
                assertTrue(root.beginConsultationsRefresh());
                assertFalse(root.beginConsultationsRefresh());
                assertFalse(find(root, "Refreshing…").isEnabled());
                assertNotNull(find(root, "Loading the review queue…"));
                assertTrue(hasVisibleSpinner(root));

                root.showConsultations(tickets, null);
                assertTrue(find(root, "Refresh").isEnabled());
                assertNotNull(find(root, "Review queue updated."));
                assertNotNull(find(root, "Submitted (1)"));
                assertNotNull(find(root, "#17"));
                assertNull(find(root, "REQ-2026-089"));
                assertFalse(hasVisibleSpinner(root));

                assertTrue(root.beginConsultationsRefresh());
                root.showConsultations(null, "Unable to load consultations.");
                assertNotNull(find(root, "#17"));
                assertNotNull(find(root, "Unable to load consultations. Showing the last loaded requests."));
                assertTrue(find(root, "Refresh").isEnabled());
                assertFalse(hasVisibleSpinner(root));
            });
        }
    }

    @Test
    public void passwordUpdateRestoresControlsAfterPendingState() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                ActivationView activation = new ActivationView(activity, null);
                View root = activation.getView();
                activation.setUpdating(true);
                Button pending = (Button) find(root, "Updating password…");
                assertNotNull(pending);
                assertFalse(pending.isEnabled());
                assertFalse(activation.getPasswordInput().isEnabled());
                assertTrue(hasVisibleSpinner(root));

                activation.setFeedback("The activation code is invalid or expired.");
                activation.setUpdating(false);
                assertTrue(find(root, "Update Password").isEnabled());
                assertTrue(activation.getPasswordInput().isEnabled());
                assertNotNull(find(root, "The activation code is invalid or expired."));
                assertFalse(hasVisibleSpinner(root));
            });
        }
    }

    private static View find(View root, String label) {
        if (root.getVisibility() != View.VISIBLE) return null;
        if (root instanceof TextView text && label.contentEquals(text.getText())) return root;
        if (root instanceof ViewGroup group) {
            for (int index = 0; index < group.getChildCount(); index++) {
                View match = find(group.getChildAt(index), label);
                if (match != null) return match;
            }
        }
        return null;
    }

    private static boolean hasVisibleSpinner(View root) {
        if (root.getVisibility() != View.VISIBLE) return false;
        if (root instanceof ProgressBar) return true;
        if (root instanceof ViewGroup group) {
            for (int index = 0; index < group.getChildCount(); index++) {
                if (hasVisibleSpinner(group.getChildAt(index))) return true;
            }
        }
        return false;
    }
}
