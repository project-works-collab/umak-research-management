package ph.edu.umak.crm.app;

import android.os.Bundle;
import java.util.Map;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONObject;

/** Renders the researcher workspace against local fixtures without issuing a session. */
public final class ResearcherPreviewActivity extends MainActivity {
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(null);
        showResearcher(new JSONObject(Map.of(
                "display_name", "Lebron James",
                "email", "student1@example.test",
                "role", "student")));
    }

    @Override
    void researcherRequest(ResearcherScreen screen, String path, JSONObject body, boolean authenticated,
            BiConsumer<String, String> result) {
        if (body != null && !"/api/v1/research/search".equals(path)) {
            result.accept(null, "Preview mode. Changes are not saved.");
            return;
        }
        switch (path) {
            case "/api/v1/researcher/studies":
                result.accept(new JSONArray(java.util.List.of(Map.of(
                        "id", 142, "title", "ML-Based Predictive Analytics for Student Academic Performance",
                        "department", "CCIS", "status", "Draft", "lifecycle_state", "In-Progress"))).toString(), null);
                break;
            case "/api/v1/consultations":
                result.accept("[{\"id\":89,\"service_type\":\"Research Consultation\",\"status\":\"claimed\"}]", null);
                break;
            case "/api/v1/research/142/feedback":
                result.accept("{\"researcher_visible_feedback\":\"Revise the consent form to clarify participant data retention.\"}", null);
                break;
            case "/api/v1/public/content":
                result.accept("{\"items\":[]}", null);
                break;
            default:
                result.accept("[]", null);
        }
    }

    @Override
    boolean isResearcherPreview() { return true; }

    @Override
    void signOut() {
        finish();
    }
}
