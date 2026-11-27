package ph.edu.umak.crm.app.staff.registry;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.MainActivity;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.research.RegisteredProgressView;

public final class LiveRegistryRecordView extends LinearLayout {
    public interface Actions {
        void request(String path, JSONObject body, BiConsumer<String, String> result);
        void reviewDuplicates(JSONObject body, BiConsumer<String, String> result);
        void choose(Runnable checking, BiConsumer<MainActivity.SelectedAttachment, String> result);
        void upload(MainActivity.SelectedAttachment file, int study, String previous, String token, BiConsumer<String, String> result);
        void download(String path, String name, String mime, Runnable failure);
        void back();
        default void openRequest(String id) {}
        default void openProgress(int id) {}
        default boolean canProgress() { return true; }
    }
    private final Actions actions;
    private PendingStatus status;
    private boolean saving;
    private LinearLayout body;
    private Runnable backAction;
    private String editorBaseline;
    private void guardEdits(){editorBaseline=ph.edu.umak.crm.app.staff.common.StaffDesignPage.fingerprint(body);}
    private void back(){
        if(saving||backAction==null)return;
        if(editorBaseline!=null&&!editorBaseline.equals(ph.edu.umak.crm.app.staff.common.StaffDesignPage.fingerprint(body)))
            ph.edu.umak.crm.app.staff.common.StaffDesignPage.confirmDiscard(getContext(),()->{editorBaseline=null;backAction.run();});
        else backAction.run();
    }
    private int currentRecordId;
    private boolean creating;
    public void showRecord(int id){if(currentRecordId!=id||getChildCount()==0)load(id);}
    public LiveRegistryRecordView(Context context, Actions actions) {
        super(context); this.actions = actions; setOrientation(VERTICAL);
    }

    public void load(int id) {
        currentRecordId=id;creating=false;
        reset("Research registry record", actions::back);
        PendingStatus current = status; current.working("Loading record…");
        actions.request("/" + id, null, (data, error) -> {
            if (status != current) return;
            if (error != null) { current.result(error, true); return; }
            try {
                JSONObject record=new JSONObject(data);
                actions.request("/"+id+"/documents",null,(files,fileError)->{
                    if(status!=current)return;
                    try{if(fileError==null)record.put("documents",new JSONArray(files));current.clear();show(record);if(fileError!=null)current.result("Document list could not be loaded. Open Manage under Documents to retry.",true);}
                    catch(JSONException invalid){current.result("CRM returned invalid research details.",true);}
                });
            }catch(JSONException invalid){current.result("CRM returned invalid research details.",true);}
        });
    }

    private void show(JSONObject record) throws JSONException {
        int id=record.getInt("id");
        var model=LiveRegistryData.record(record);
        if(record.optJSONArray("documents")!=null)LiveRegistryData.documents(model,record.getJSONArray("documents"));
        ResearchRecordDetailView design=new ResearchRecordDetailView((android.app.Activity)getContext(),new ResearchRecordDetailView.Callbacks() {
            public void onBackToRegistry(){actions.back();}
            public void onEditRecord(StaffRegistryData.RegistryRecordItem item){edit(record);}
            public void onCorrectionHistory(StaffRegistryData.RegistryRecordItem item){history(record);}
            public void onManageDocuments(StaffRegistryData.RegistryRecordItem item){documents(id);}
            public void onOpenLinkedRequest(String request){actions.openRequest(request);}
            public void onMilestonesAndOutcomes(StaffRegistryData.RegistryRecordItem item){actions.openProgress(id);}
            public boolean canProgress(){return actions.canProgress();}
        });
        design.setRecord(model);body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(design.getContentLayout()));
    }

    public void create() { if(creating&&getChildCount()>0)return;edit(null);creating=true;currentRecordId=0; }

    private void edit(JSONObject record) {
        reset(record == null ? "Add research entry" : "Correct research record", record == null ? actions::back : () -> load(record.optInt("id")));
        EditText title = input("Research title", record == null ? "" : record.optString("title"));
        EditText college = input("College or institute", record == null ? "" : knownInput(record, "college"));
        EditText period = input("Academic period (AY 2026-2027)", record == null ? "" : knownInput(record, "academic_period"));
        EditText adviser = input("Adviser", record == null ? "" : knownInput(record, "adviser"));
        EditText abstractText = input("Abstract", record == null ? "" : record.optString("abstract"));
        title.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(200)});
        abstractText.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(4000)});
        line("Review similar titles and abstracts before creating a record. Scores are advisory; the alert threshold is a synthetic pilot.", false);
        Button review = button("Review duplicates", null);
        review.setOnClickListener(view -> {
            if (saving) return;
            if (value(title).isEmpty()) { title.setError("Enter a research title."); return; }
            if (value(abstractText).isEmpty()) { abstractText.setError("Enter an abstract for duplicate review."); return; }
            try {
                JSONObject query = new JSONObject().put("title", value(title)).put("abstract", value(abstractText));
                if (record != null) query.put("exclude_id", record.optInt("id"));
                saving = true;
                Runnable restore = ph.edu.umak.crm.app.staff.common.StaffDesignPage.freeze(this.body);
                PendingStatus current = status; current.working("Reviewing titles and abstracts…");
                actions.reviewDuplicates(query, (data, error) -> {
                    saving = false; restore.run();
                    if (current != status) return;
                    if (error != null) { current.result("Duplicate review unavailable. Please try again.", true); return; }
                    try {
                        JSONArray matches = new JSONObject(data).getJSONArray("matches");
                        StringBuilder message = new StringBuilder();
                        if (matches.length() == 0) message.append("No matches found in the current title shortlist. This does not prove originality.\n");
                        for (int i = 0; i < matches.length(); i++) {
                            JSONObject match = matches.getJSONObject(i);
                            message.append(i + 1).append(". ").append(match.getString("title"))
                                .append("\nTF-IDF similarity: ").append(String.format(java.util.Locale.ROOT, "%.3f", match.getDouble("tfidf_score")))
                                .append(match.optBoolean("alert") ? " — review suggested" : " — below pilot threshold");
                            if (match.optBoolean("abstract_unavailable")) message.append("\nArchive abstract unavailable");
                            message.append("\n\n");
                        }
                        message.append("Synthetic pilot threshold. Staff decide whether records are duplicates; scores do not determine plagiarism.");
                        current.clear();
                        new AlertDialog.Builder(getContext()).setTitle("Duplicate review")
                            .setMessage(message.toString()).setPositiveButton("Back to entry", null).show();
                    } catch (JSONException invalid) { current.result("CRM returned invalid duplicate review results. Please retry.", true); }
                });
            } catch (JSONException invalid) { status.result("Review the title and abstract.", true); }
        });
        CheckBox historical = new CheckBox(getContext()); historical.setText("Historical entry");
        historical.setChecked(record != null && record.optBoolean("historical")); body.addView(historical);
        EditText source = input("Source reference", record == null ? "" : knownInput(record, "source_reference"));
        EditText originalDate = input("Original registration date (leave blank if unknown)", record == null ? "" : knownInput(record, "original_registration_date"));
        EditText reason = input("Correction or entry reason", "");
        CheckBox duplicate = new CheckBox(getContext()); duplicate.setText("Create a separate record despite a confirmed possible duplicate"); body.addView(duplicate);
        EditText duplicateReason = input("Reason for creating a separate record", "");
        List<JSONObject> members = new ArrayList<>();
        JSONArray existing = record == null ? new JSONArray() : record.optJSONArray("members");
        if (existing != null) for (int i = 0; i < existing.length(); i++) members.add(existing.optJSONObject(i));
        LinearLayout team = new LinearLayout(getContext()); team.setOrientation(VERTICAL); body.addView(team);
        Spinner registrant = new Spinner(getContext()); registrant.setContentDescription("Registrant");
        if (record == null) { line("Registrant", true); body.addView(registrant); }
        Runnable refresh = () -> {
            team.removeAllViews();
            ArrayList<String> names = new ArrayList<>();
            for (JSONObject member : members) {
                names.add(member.optString("name"));
                Button remove = UiButton.outline(getContext(), member.optString("name") + " · Remove", UiTheme.BORDER, UiTheme.NAVY, () -> {});
                remove.setOnClickListener(view -> { members.remove(member); refreshTeam(team, members, registrant, record == null ? 0 : record.optInt("registrant_id")); });
                if (record != null && member.optInt("id") == record.optInt("registrant_id")) remove.setEnabled(false);
                team.addView(remove);
            }
            registrant.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names));
        };
        refresh.run();
        button("Add existing researcher", () -> {
            EditText email = new EditText(getContext()); email.setHint("Institutional email");
            var dialog = new AlertDialog.Builder(getContext()).setTitle("Find researcher").setView(email)
                    .setNegativeButton("Cancel", null).setPositiveButton("Find", null).create();
            dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                Button find = dialog.getButton(AlertDialog.BUTTON_POSITIVE); find.setEnabled(false);
                String query = new android.net.Uri.Builder().appendQueryParameter("email", value(email)).build().toString();
                actions.request("/candidate" + query, null, (data, error) -> {
                    if (!dialog.isShowing()) return;
                    find.setEnabled(true);
                    if (error != null) { email.setError(error); return; }
                    try {
                        JSONObject selected = new JSONObject(data);
                        if (members.stream().anyMatch(person -> person.optInt("id") == selected.optInt("id"))) {
                            email.setError("This researcher is already a team member."); return;
                        }
                        members.add(selected); dialog.dismiss(); refresh.run();
                    } catch (JSONException invalid) { email.setError("CRM returned invalid account details."); }
                });
            }));
            dialog.show();
        });
        String token = UUID.randomUUID().toString();
        Button save = button(record == null ? "Create record" : "Save correction", null);
        save.setOnClickListener(view -> {
            if (saving) return;
            if (members.isEmpty()) { status.result("Select existing Researcher accounts.", true); return; }
            if (value(title).isEmpty()) { title.setError("Please enter the research title."); return; }
            try {
                JSONArray ids = new JSONArray(); for (JSONObject member : members) ids.put(member.optInt("id"));
                int owner = record == null ? members.get(registrant.getSelectedItemPosition()).optInt("id") : record.optInt("registrant_id");
                JSONObject body = new JSONObject().put("title", value(title)).put("college", value(college))
                        .put("academic_period", value(period)).put("adviser", value(adviser)).put("abstract", value(abstractText))
                        .put("registrant_id", owner).put("member_ids", ids).put("historical", historical.isChecked())
                        .put("source_reference", value(source)).put("original_registration_date", value(originalDate).isEmpty() ? JSONObject.NULL : value(originalDate))
                        .put("reason", value(reason)).put("duplicate_confirmed", duplicate.isChecked())
                        .put("duplicate_reason", value(duplicateReason)).put("submission_token", token);
                if (record != null) body.put("expected_version", record.optInt("version"));
                saving = true; Runnable restore=ph.edu.umak.crm.app.staff.common.StaffDesignPage.freeze(this.body); PendingStatus current = status; current.working("Saving registry record…");
                actions.request(record == null ? "" : "/" + record.optInt("id") + "/corrections", body, (data, error) -> {
                    saving = false;restore.run();
                    if (current != status) return;
                    if (error != null) {
                        current.result(error, true);
                        if (error.contains("Reload before saving")) {save.setEnabled(false);button("Reload record", () -> load(record.optInt("id")));}
                        else save.setEnabled(true);
                        return;
                    }
                    try { load(new JSONObject(data).getInt("id")); }
                    catch (JSONException invalid) { save.setEnabled(true); current.result("CRM could not confirm the registry change.", true); }
                });
            } catch (JSONException invalid) { status.result("Review the registry information.", true); }
        });
        guardEdits();
    }

    private void refreshTeam(LinearLayout team, List<JSONObject> members, Spinner registrant, int owner) {
        team.removeAllViews();
        ArrayList<String> names = new ArrayList<>();
        for (JSONObject member : members) {
            names.add(member.optString("name"));
            Button remove = UiButton.outline(getContext(), member.optString("name") + " · Remove", UiTheme.BORDER, UiTheme.NAVY, () -> {
                members.remove(member); refreshTeam(team, members, registrant, owner);
            });
            if (member.optInt("id") == owner) remove.setEnabled(false);
            team.addView(remove);
        }
        registrant.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void documents(int id) {
        reset("Documents", () -> load(id));
        PendingStatus current=status;current.working("Loading document versions…");
        actions.request("/"+id+"/documents",null,(data,error)->{
            if(status!=current)return;
            if(error!=null){current.result(error,true);button("Retry",()->documents(id));return;}
            try {
                var model=new StaffRegistryData.RegistryRecordItem("REG-"+id,"Research documents","","","","","","","","",null,null,null,null,null);
                LiveRegistryData.documents(model,new JSONArray(data));
                RegistryDocumentsView design=new RegistryDocumentsView((android.app.Activity)getContext(),new RegistryDocumentsView.Callbacks(){
                    public void onBackToRecord(){load(id);}
                    public boolean isLive(){return true;}
                    public void onChooseAttachment(Runnable checking,BiConsumer<MainActivity.SelectedAttachment,String> result){actions.choose(checking,result);}
                    public void onUpload(String previous){upload(id,previous);}
                    public void onOpenDocument(StaffRegistryData.DocumentVersion file){actions.download("/"+id+"/documents/"+file.fileId,file.fileName,file.mime,()->status.result("Unable to open the document. Try again.",true));}
                });
                design.setRecord(model);current.clear();body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(design.getContentLayout()));
            }catch(JSONException invalid){current.result("CRM returned invalid document versions.",true);}
        });
    }

    private void upload(int study, String previous) {
        actions.choose(() -> status.working("Checking document…"), (file, error) -> {
            if (error != null) { status.result(error, true); return; }
            String token = UUID.randomUUID().toString();
            performUpload(file, study, previous, token);
        });
    }
    private void performUpload(MainActivity.SelectedAttachment file, int study, String previous, String token) {
        if (saving) return;
        saving = true; PendingStatus current = status; current.working("Uploading research document…");
        actions.upload(file, study, previous, token, (data, error) -> {
            saving = false;
            if (status != current) return;
            if (error == null) documents(study);
            else { current.result(error, true); button("Retry upload", () -> performUpload(file, study, previous, token)); }
        });
    }
    private void history(JSONObject record) {
        reset("Correction History",()->load(record.optInt("id")));
        try {
            RegistryCorrectionHistoryView design=new RegistryCorrectionHistoryView((android.app.Activity)getContext(),()->load(record.optInt("id")));
            design.setRecord(LiveRegistryData.record(record));body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(design.getContentLayout()));
        }catch(JSONException invalid){status.result("CRM returned invalid correction history.",true);}
    }
    public boolean isSaving(){return saving;}
    public boolean handleBack(){back();return true;}
    private void reset(String title,Runnable back){
        removeAllViews();backAction=back;editorBaseline=null;
        var page=new ph.edu.umak.crm.app.staff.common.StaffDesignPage(getContext(),title,"Back",this::back);
        addView(page);status=page.status;body=page.section("");
    }
    private void line(String content,boolean heading){ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(body,content,heading);}
    private EditText input(String title,String initial){
        line(title,false);EditText field=new EditText(getContext());field.setText(initial);field.setContentDescription(title);
        ph.edu.umak.crm.app.staff.common.StaffDesignPage.style(field,title.equals("Abstract"));body.addView(field);return field;
    }
    private Button button(String title, Runnable action) {
        Button button = UiButton.outline(getContext(), title, UiTheme.BORDER, UiTheme.NAVY, action); body.addView(button); return button;
    }
    private static String value(EditText field) { return field.getText().toString().trim(); }
    private static String knownInput(JSONObject record, String key) { return record.isNull(key) ? "" : record.optString(key); }
    private static String known(JSONObject record, String key) { String value = knownInput(record, key); return value.isEmpty() ? "Unknown" : value; }
}
