package ph.edu.umak.crm.app;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
    public void startupShowsWelcomeThenSharedSignInAndGuestResources() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(find(activity.getWindow().getDecorView(), "UNIVERSITY OF MAKATI"));
                click(activity, "Get Started");
                assertNotNull(find(activity.getWindow().getDecorView(), "Institutional Email"));
                assertNotNull(find(activity.getWindow().getDecorView(), "Password"));
                assertNull(find(activity.getWindow().getDecorView(), "CRM server (https://…)"));
                click(activity, "Continue as Guest →");
                assertNotNull(find(activity.getWindow().getDecorView(), "Public Resources"));
                click(activity, "Sign In");
                assertNotNull(find(activity.getWindow().getDecorView(), "Institutional Email"));
            });
        }
    }

    private static void click(MainActivity activity, String label) {
        View target = find(activity.getWindow().getDecorView(), label);
        assertNotNull(label, target);
        target.performClick();
    }

    private static View find(View root, String label) {
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
