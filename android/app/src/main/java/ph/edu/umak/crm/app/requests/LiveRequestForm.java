package ph.edu.umak.crm.app.requests;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.MainActivity;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.services.ServiceData;

public final class LiveRequestForm {
    public interface Actions {
        void request(String path, JSONObject body, BiConsumer<String, String> result);
        void choose(Runnable checking, BiConsumer<MainActivity.SelectedAttachment, String> result);
        void upload(MainActivity.SelectedAttachment attachment, int requirement, String attempt,
                BiConsumer<String, String> result);
    }

    private record Study(int id, String title) {
        @Override public String toString() { return title; }
    }

    private final LinearLayout layout;
    private final Context context;
    private final Actions actions;
    private final int revisionId;
    private final int requestId;
    private final int requestVersion;
    private final ServiceData.DocumentItem[] documents;
    private final Consumer<RequestData.RequestItem> saved;
    private final Map<Integer, String> attachments = new LinkedHashMap<>();
    private final String token = UUID.randomUUID().toString();
    private final PendingStatus status;
    private final EditText subject;
    private final EditText description;
    private final Spinner linkedStudy;
    private final Button submit;
    private final Button retryStudies;
    private boolean uploading;
    private boolean submitting;

    public LiveRequestForm(Context context, ServiceData.ServiceItem service, Actions actions,
            Consumer<RequestData.RequestItem> saved) {
        this(context, service.revisionId, 0, 0, service.documents, actions, saved);
    }

    public LiveRequestForm(Context context, int requestId, int requestVersion,
            ServiceData.DocumentItem[] documents, Actions actions, Consumer<RequestData.RequestItem> saved) {
        this(context, 0, requestId, requestVersion, documents, actions, saved);
    }

    private LiveRequestForm(Context context, int revisionId, int requestId, int requestVersion,
            ServiceData.DocumentItem[] documents, Actions actions, Consumer<RequestData.RequestItem> saved) {
        this.context = context;
        this.revisionId = revisionId;
        this.requestId = requestId;
        this.requestVersion = requestVersion;
        this.documents = documents;
        this.actions = actions;
        this.saved = saved;
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 16);
        layout.setPadding(pad, pad, pad, pad);

        subject = requestId == 0 ? input("Request subject", true) : new EditText(context);
        description = requestId == 0 ? input("Request details", false) : new EditText(context);
        if (requestId == 0) label("Linked research study (optional)");
        linkedStudy = new Spinner(context);
        linkedStudy.setContentDescription("Linked research study");
        if (requestId == 0) layout.addView(linkedStudy);
        retryStudies = UiButton.outline(context, "Retry study lookup", UiTheme.BORDER, UiTheme.NAVY, this::loadStudies);
        retryStudies.setVisibility(android.view.View.GONE);
        if (requestId == 0) layout.addView(retryStudies);

        label("Attachments · PDF or DOCX · up to 10 MB per file · up to 5 files");
        for (int i = 0; i < documents.length; i++) {
            var requirement = documents[i];
            addAttachment(i, requirement.title + (requirement.required ? " · Required" : " · Optional"));
        }
        if (documents.length == 0) {
            for (int i = 0; i < 5; i++) addAttachment(i, "Supporting document " + (i + 1));
        }
        status = new PendingStatus(context);
        layout.addView(status);
        submit = UiButton.primary(context, requestId == 0 ? "Submit request" : "Resubmit request", this::submit);
        layout.addView(submit);
        showStudyChoices(new ArrayList<>());
    }

    public LinearLayout view() { return layout; }
    public boolean isBusy() { return uploading || submitting; }

    public void loadStudies() {
        retryStudies.setVisibility(android.view.View.GONE);
        actions.request("/linked-studies", null, (data, error) -> {
            if (!layout.isAttachedToWindow()) return;
            if (error == null) {
                try {
                    JSONArray rows = new JSONArray(data);
                    ArrayList<Study> studies = new ArrayList<>();
                    for (int i = 0; i < rows.length(); i++) {
                        JSONObject row = rows.getJSONObject(i);
                        studies.add(new Study(row.getInt("id"), row.getString("title")));
                    }
                    showStudyChoices(studies);
                    return;
                } catch (JSONException invalid) {
                    // Keep no linked study available while the lookup is retried.
                }
            }
            retryStudies.setVisibility(android.view.View.VISIBLE);
        });
    }

    private void showStudyChoices(ArrayList<Study> studies) {
        studies.add(0, new Study(0, "No linked study"));
        var adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, studies);
        linkedStudy.setAdapter(adapter);
    }

    private void addAttachment(int index, String caption) {
        label(caption);
        PendingStatus attachmentStatus = new PendingStatus(context);
        layout.addView(attachmentStatus);
        Button choose = UiButton.outline(context, "Choose file", UiTheme.BORDER, UiTheme.NAVY, null);
        choose.setContentDescription("Choose file for " + caption);
        layout.addView(choose);
        Button retry = UiButton.text(context, "Retry upload", UiTheme.NAVY, null);
        retry.setVisibility(android.view.View.GONE);
        layout.addView(retry);
        choose.setOnClickListener(ignored -> {
            if (uploading || submitting) return;
            if (attachments.size() >= 5 && !attachments.containsKey(index)) {
                attachmentStatus.result("You can attach up to 5 files.", true);
                return;
            }
            actions.choose(() -> attachmentStatus.working("Checking file…"), (attachment, error) -> {
                if (!layout.isAttachedToWindow()) return;
                if (error != null) {
                    attachmentStatus.result(error, true);
                    return;
                }
                String attempt = UUID.randomUUID().toString();
                Runnable send = () -> upload(index, attachment, attempt, attachmentStatus, retry);
                retry.setOnClickListener(view -> send.run());
                send.run();
            });
        });
    }

    private void upload(int index, MainActivity.SelectedAttachment file, String attempt,
            PendingStatus feedback, Button retry) {
        if (uploading || submitting) return;
        if (attachments.size() >= 5 && !attachments.containsKey(index)) {
            feedback.result("You can attach up to 5 files.", true);
            return;
        }
        uploading = true;
        submit.setEnabled(false);
        retry.setVisibility(android.view.View.GONE);
        feedback.working("Uploading " + file.name() + "…");
        actions.upload(file, index, attempt, (data, error) -> {
            uploading = false;
            if (!layout.isAttachedToWindow()) return;
            submit.setEnabled(true);
            if (error == null) {
                try {
                    attachments.put(index, new JSONObject(data).getString("id"));
                    feedback.result("Attached: " + file.name(), false);
                    return;
                } catch (JSONException invalid) {
                    error = "Upload failed. Try again.";
                }
            }
            feedback.result(error, true);
            retry.setVisibility(android.view.View.VISIBLE);
        });
    }

    private void submit() {
        if (submitting || uploading) return;
        subject.setError(null);
        description.setError(null);
        String title = subject.getText().toString().trim();
        if (requestId == 0 && title.isEmpty()) {
            subject.setError("Required");
            status.result("Complete the highlighted required fields.", true);
            return;
        }
        if (requestId == 0 && (title.length() > 160 || description.getText().toString().trim().length() > 4000)) {
            if (title.length() > 160) subject.setError("Use 160 characters or fewer.");
            if (description.getText().toString().trim().length() > 4000) description.setError("Use 4,000 characters or fewer.");
            status.result("Correct the highlighted information.", true);
            return;
        }
        for (int i = 0; i < documents.length; i++) {
            if (documents[i].required && !attachments.containsKey(i)) {
                status.result("Complete the highlighted required fields. Attach " + documents[i].title + ".", true);
                return;
            }
        }
        if (requestId != 0 && attachments.isEmpty()) {
            status.result("Attach at least one replacement or supporting file.", true);
            return;
        }
        try {
            JSONObject request = new JSONObject().put("files", new JSONArray(attachments.values()));
            String path = "";
            if (requestId == 0) {
                Study study = (Study) linkedStudy.getSelectedItem();
                request.put("revision_id", revisionId)
                        .put("study_id", study == null || study.id == 0 ? JSONObject.NULL : study.id)
                        .put("subject", title).put("description", description.getText().toString().trim())
                        .put("submission_token", token);
            } else {
                path = "/" + requestId + "/resubmit";
                request.put("expected_version", requestVersion);
            }
            submitting = true;
            submit.setEnabled(false);
            status.working("Submitting your request to CRM…");
            actions.request(path, request, (data, error) -> {
                submitting = false;
                if (!layout.isAttachedToWindow()) return;
                submit.setEnabled(true);
                if (error == null) {
                    try {
                        saved.accept(RequestData.fromServiceRequest(new JSONObject(data)));
                        return;
                    } catch (JSONException invalid) {
                        error = "CRM could not confirm your request. Try again.";
                    }
                }
                if (data != null && error != null) {
                    try {
                        JSONObject fields = new JSONObject(data);
                        if (fields.has("subject")) subject.setError("Check this field.");
                        if (fields.has("description")) description.setError("Check this field.");
                        if (fields.length() > 0) error = "Correct the highlighted information.";
                    } catch (JSONException ignored) { /* Retain the ordinary error. */ }
                }
                status.result(error, true);
                submit.setText("Retry submission");
            });
        } catch (JSONException invalid) {
            status.result("Correct the highlighted information.", true);
        }
    }

    private EditText input(String title, boolean singleLine) {
        label(title);
        EditText input = new EditText(context);
        input.setContentDescription(title);
        input.setSingleLine(singleLine);
        layout.addView(input);
        return input;
    }

    private void label(String title) {
        TextView label = new TextView(context);
        label.setText(title);
        label.setTextColor(UiTheme.NAVY);
        layout.addView(label);
    }
}
