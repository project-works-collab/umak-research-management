package ph.edu.umak.crm.app;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class LocalRegistrationDraftTest {
    @Test
    public void incompleteLocalDraftReopensAndIsScopedToItsAccount() throws Exception {
        JSONObject owner = new JSONObject().put("id", 91821).put("role", "student");
        JSONObject other = new JSONObject().put("id", 91822).put("role", "student");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                var preferences = activity.getSharedPreferences("crm-registration-drafts", Context.MODE_PRIVATE);
                preferences.edit().remove("91821").remove("91822").commit();
                try {
                    ResearcherScreen first = new ResearcherScreen(activity, owner, "Profile");
                    first.openInitialTab();
                    find(first, "Start a draft").performClick();
                    EditText title = firstEditor(first);
                    assertNotNull(title);
                    title.setText("Locally saved, unfinished draft");
                    assertNotNull(find(first, "Saved on this device"));
                    assertNotNull(find(first, "Save to CRM"));

                    ResearcherScreen reopened = new ResearcherScreen(activity, owner, "Profile");
                    reopened.openInitialTab();
                    find(reopened, "Resume local draft").performClick();
                    assertNotNull(find(reopened, "Locally saved, unfinished draft"));

                    ResearcherScreen separate = new ResearcherScreen(activity, other, "Profile");
                    separate.openInitialTab();
                    assertNotNull(find(separate, "Start a draft"));
                    assertNull(find(separate, "Resume local draft"));
                } finally {
                    preferences.edit().remove("91821").remove("91822").commit();
                }
            });
        }
    }

    private static View find(View root, String text) {
        if (root instanceof TextView label && text.contentEquals(label.getText())) return root;
        if (root instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                View match = find(group.getChildAt(i), text);
                if (match != null) return match;
            }
        }
        return null;
    }

    private static EditText firstEditor(View root) {
        if (root instanceof EditText editor) return editor;
        if (root instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                EditText match = firstEditor(group.getChildAt(i));
                if (match != null) return match;
            }
        }
        return null;
    }
}
