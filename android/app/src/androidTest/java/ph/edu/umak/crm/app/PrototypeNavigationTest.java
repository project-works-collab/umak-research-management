package ph.edu.umak.crm.app;

import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class PrototypeNavigationTest {
    @Test
    public void allFiveBoardsAreReachableWithoutSignIn() {
        try (ActivityScenario<PrototypeActivity> scenario = ActivityScenario.launch(PrototypeActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(find(activity.getWindow().getDecorView(), "Service Request Queue"));
                click(activity, "Generate Monthly Report");
                assertNotNull(find(activity.getWindow().getDecorView(), "Export Report"));

                click(activity, "Manage");
                assertNotNull(find(activity.getWindow().getDecorView(), "Form Fields"));
                click(activity, "Continue");
                assertNotNull(find(activity.getWindow().getDecorView(), "Attachment Validation"));
                click(activity, "Continue");
                assertNotNull(find(activity.getWindow().getDecorView(), "Access Control"));
                click(activity, "Profile");
                assertNotNull(find(activity.getWindow().getDecorView(), "Role & System Permissions"));

                click(activity, "Requests");
                click(activity, "View Assigned Request");
                assertNotNull(find(activity.getWindow().getDecorView(), "Evaluation Checklist"));

                click(activity, "Registry");
                click(activity, "Open Record");
                assertNotNull(find(activity.getWindow().getDecorView(), "Potential Duplicate"));
                click(activity, "Milestones and Outcomes");
                assertNotNull(find(activity.getWindow().getDecorView(), "Target Deliverable & Outcomes"));
            });
        }
    }

    private static void click(PrototypeActivity activity, String label) {
        View target = find(activity.getWindow().getDecorView(), label);
        assertNotNull(label, target);
        target.performClick();
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
