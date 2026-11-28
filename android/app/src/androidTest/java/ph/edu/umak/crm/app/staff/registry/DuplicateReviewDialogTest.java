package ph.edu.umak.crm.app.staff.registry;

import static org.junit.Assert.*;
import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import ph.edu.umak.crm.app.MainActivity;

public class DuplicateReviewDialogTest {
    private static final String RESPONSE = "{\"scored_count\":12,\"threshold\":0.85,"
            + "\"retrieval_scope\":\"all_authorized_records\",\"matches\":[{\"title\":\"Existing study\","
            + "\"tfidf_score\":0.85,\"alert\":false,\"abstract_unavailable\":true}]}";

    @Test public void exactThresholdUsesServerDecisionAndMissingAbstractIsVisible() {
        try (var scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                boolean[] continued = {false};
                AlertDialog dialog = DuplicateReviewDialog.show(activity, "Entered title", "Entered abstract",
                        result -> result.accept(RESPONSE, null), () -> true, reason -> continued[0] = true);
                View root = dialog.getWindow().getDecorView();
                assertNotNull(find(root, "Checked 12 accessible studies"));
                assertNotNull(find(root, "85.00% similarity · Does not exceed threshold"));
                assertNotNull(find(root, "Abstract unavailable"));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                assertTrue(continued[0]);
            });
        }
    }

    @Test public void failedReviewRetriesAndStaleInputCannotContinue() {
        try (var scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                int[] calls = {0};
                boolean[] current = {true};
                AlertDialog dialog = DuplicateReviewDialog.show(activity, "Title", "Abstract", result -> {
                    if (++calls[0] == 1) result.accept(null, "Unavailable");
                    else result.accept(RESPONSE, null);
                }, () -> current[0], reason -> fail("Stale input must not save"));
                assertFalse(dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled());
                find(dialog.getWindow().getDecorView(), "Retry").performClick();
                assertEquals(2, calls[0]);
                assertTrue(dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled());
                current[0] = false;
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                assertFalse(dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled());
                dialog.dismiss();
            });
        }
    }

    private static View find(View root, String text) {
        if (root instanceof TextView label && text.contentEquals(label.getText())) return root;
        if (root instanceof ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) {
            View found = find(group.getChildAt(i), text);
            if (found != null) return found;
        }
        return null;
    }
}
