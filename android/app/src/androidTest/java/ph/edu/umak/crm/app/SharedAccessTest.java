package ph.edu.umak.crm.app;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class SharedAccessTest {
    @Test
    public void startupShowsWelcomeThenSharedSignInAndGuestPortal() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(find(activity.getWindow().getDecorView(), "UNIVERSITY OF MAKATI"));
                click(activity, "Get Started");
                assertNotNull(find(activity.getWindow().getDecorView(), "Username or Institutional Email"));
                assertNotNull(find(activity.getWindow().getDecorView(), "Password"));
                assertNull(find(activity.getWindow().getDecorView(), "CRM server (https://…)"));
                click(activity, "Continue as Guest");
                assertNotNull(find(activity.getWindow().getDecorView(), "Center for Research Management"));
                click(activity, "Services");
                assertNotNull(find(activity.getWindow().getDecorView(), "Research Services &\nProcedures"));
                click(activity, "Requests");
                assertNotNull(find(activity.getWindow().getDecorView(), "Username or Institutional Email"));
            });
        }
    }

    @Test
    public void verifiedResearcherRolesOpenTheResearcherWorkspace() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                for (String role : new String[]{"student", "faculty"}) {
                    org.json.JSONObject account = new org.json.JSONObject(
                            java.util.Map.of("id", role.equals("student") ? 91823 : 91824,
                                    "email", "access-test-" + role + "@example.test", "role", role));
                    assertTrue(activity.displayAccount(account));
                    assertNotNull(find(activity.getWindow().getDecorView(), "Research Progress Tracker"));
                    click(activity, "Profile");
                    assertNotNull(find(activity.getWindow().getDecorView(), "Account Settings"));
                    View resume = find(activity.getWindow().getDecorView(), "Resume local draft");
                    click(activity, resume == null ? "Start a draft" : "Resume local draft");
                    assertNotNull(find(activity.getWindow().getDecorView(), "Step 1 of 3: General Information"));
                    clearTitle(activity.getWindow().getDecorView());
                    click(activity, "Continue");
                    assertNotNull(find(activity.getWindow().getDecorView(), "Please enter the research title."));
                    click(activity, "Back to Profile");
                    assertNotNull(find(activity.getWindow().getDecorView(), "Account Settings"));
                }
            });
        }
    }

    private static void click(MainActivity activity, String label) {
        View target = find(activity.getWindow().getDecorView(), label);
        assertNotNull(label, target);
        if (!target.isClickable() && target.getParent() instanceof View) target = (View) target.getParent();
        target.performClick();
    }

    private static void clearTitle(View root) {
        if (root instanceof android.widget.EditText field && "Enter the complete research title".contentEquals(field.getHint())) field.setText("");
        if (root instanceof ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) clearTitle(group.getChildAt(i));
    }

    private static View find(View root, String label) {
        if (root.isShown() && label.contentEquals(root.getContentDescription() == null ? "" : root.getContentDescription())) return root;
        if (root instanceof TextView && label.contentEquals(((TextView) root).getText())) return root;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int index = 0; index < group.getChildCount(); index++) {
                View match = find(group.getChildAt(index), label);
                if (match != null) return match;
            }
        }
        return null;
    }
}
