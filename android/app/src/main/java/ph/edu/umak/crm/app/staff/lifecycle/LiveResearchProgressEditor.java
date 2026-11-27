package ph.edu.umak.crm.app.staff.lifecycle;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.research.RegisteredProgressView;

public final class LiveResearchProgressEditor extends LinearLayout {
    public interface Actions {
        void request(String path, JSONObject body, BiConsumer<String, String> result);
        void back();
    }

    private final Actions actions;
    private PendingStatus feedback;
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

    public LiveResearchProgressEditor(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        setOrientation(VERTICAL);
        int padding = UiTheme.dp(context, 16);
        setPadding(padding, padding, padding, padding);
    }

    public void ensureLoaded(){if(getChildCount()==0)load();}
    public void load(){reset("Research progress","Back to Registry",actions::back);loadPage(null);}
    private void loadPage(Integer before){
        String path=before==null?"":"?before="+before;
        request(path,payload->{
            JSONArray studies=new JSONArray(payload);
            if(studies.length()==0&&before==null)line("No registered research yet.");
            for(int i=0;i<studies.length();i++){
                JSONObject study=studies.getJSONObject(i);int id=study.getInt("id");
                button("REG-"+id+" · "+study.getString("title"),()->detail(id));
            }
            if(studies.length()==100){int last=studies.getJSONObject(99).getInt("id");Button more=button("Load more research",null);more.setOnClickListener(view->{body.removeView(more);loadPage(last);});}
        },()->loadPage(before));
    }

    private void detail(int id) {
        reset("Research progress · REG-" + id, "Back to progress list", this::load);
        request("/" + id, payload -> showRecord(new JSONObject(payload)), () -> detail(id));
    }

    private void showRecord(JSONObject record) throws JSONException {
        int id = record.getInt("id");
        int version = record.getInt("version");
        String current = record.getString("stage");
        MilestonesAndOutcomesView design=new MilestonesAndOutcomesView((android.app.Activity)getContext(),null);
        design.bindLive(record,new MilestonesAndOutcomesView.LiveActions(){
            public void milestones(){LiveResearchProgressEditor.this.milestones(id,version,record.optJSONArray("milestones"));}
            public void stage(){editStage(id,version,current,record);}
            public void milestone(int index){editMilestone(id,version,record.optJSONArray("milestones"),record.optJSONArray("milestones").optJSONObject(index));}
            public void presentation(){editOutcome(id,version,false,record.optJSONObject("presentation"));}
            public void publication(){editOutcome(id,version,true,record.optJSONObject("publication"));}
            public void completion(){editCompletion(id,version,record.optJSONObject("completion"));}
            public void history(){try{showHistory(record);}catch(JSONException invalid){feedback.result("CRM returned invalid stage history.",true);}}
            public void close(){load();}
        });
        body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(design.getContentLayout()));
        loadPlanning(id,version,record);
    }

    public void openRecord(int id){detail(id);}
    public boolean isSaving(){return saving;}
    public boolean handleBack(){back();return true;}
    private void showHistory(JSONObject record)throws JSONException {
        int id=record.getInt("id");reset("Stage History","Back to milestones",()->detail(id));
        StageHistoryView design=new StageHistoryView((android.app.Activity)getContext(),()->detail(id));
        design.setState(LiveLifecycleData.state(record));body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(design.getContentLayout()));
    }

    private void loadPlanning(int id, int version, JSONObject record) {
        LinearLayout planning = new LinearLayout(getContext());
        planning.setOrientation(VERTICAL);
        body.addView(planning);
        PendingStatus status = new PendingStatus(getContext());
        planning.addView(status);
        status.working("Calculating workflow readiness…");
        actions.request("/" + id + "/planning", null, (data, error) -> {
            if (!planning.isAttachedToWindow()) return;
            if (error != null) {
                status.result(error, true);
                Button retry = UiButton.outline(getContext(), "Retry planning", UiTheme.BORDER, UiTheme.NAVY, () -> {
                    removeView(planning);
                    loadPlanning(id, version, record);
                });
                planning.addView(retry);
                return;
            }
            try {
                JSONObject plan = new JSONObject(data);
                status.clear();
                if (plan.optInt("version") != version) {
                    status.result("This research record was updated. Reload before saving.", true);
                    return;
                }
                RegisteredProgressView.appendPlanning(planning, plan);
                Button configure = UiButton.outline(getContext(), "Configure research workflow", UiTheme.BORDER, UiTheme.NAVY,
                        () -> configureWorkflow(id, version, record.optJSONArray("milestones"), plan.optJSONObject("definition")));
                planning.addView(configure);
            } catch (JSONException invalid) {
                status.result("CRM returned invalid workflow results. Try again.", true);
            }
        });
    }

    private void configureWorkflow(int id, int version, JSONArray milestones, JSONObject previous) {
        reset("Configure research workflow", "Back to research progress", () -> detail(id));
        line("Set prerequisites and estimated work minutes for each milestone. Evidence requirements refer to the saved evidence on the selected milestone. Staff review determines whether that evidence is received or verified.");
        if (milestones == null) return;
        java.util.List<EditText> durations = new java.util.ArrayList<>();
        java.util.List<EditText> prerequisites = new java.util.ArrayList<>();
        java.util.List<EditText> gates = new java.util.ArrayList<>();
        JSONArray existing = previous == null ? new JSONArray() : previous.optJSONArray("tasks");
        for (int i = 0; i < milestones.length(); i++) {
            JSONObject milestone = milestones.optJSONObject(i);
            String code = milestone.optString("code");
            RegisteredProgressView.line(this, milestone.optString("title") + " (" + code + ")", true);
            JSONObject prior = null;
            if (existing != null) for (int j = 0; j < existing.length(); j++) {
                JSONObject candidate = existing.optJSONObject(j);
                if (candidate != null && code.equals(candidate.optString("code"))) prior = candidate;
            }
            EditText duration = input("Estimated work minutes · " + code,
                    prior == null ? "" : prior.optString("duration_minutes"));
            duration.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            durations.add(duration);
            JSONArray parents = prior == null ? null : prior.optJSONArray("prerequisites");
            java.util.StringJoiner codes = new java.util.StringJoiner(", ");
            if (parents != null) for (int j = 0; j < parents.length(); j++) codes.add(parents.optString(j));
            prerequisites.add(input("Prerequisite milestone codes, comma separated · " + code, codes.toString()));
            JSONArray requirements = prior == null ? null : prior.optJSONArray("requirements");
            java.util.StringJoiner evidence = new java.util.StringJoiner(", ");
            if (requirements != null) for (int j = 0; j < requirements.length(); j++) {
                JSONObject requirement = requirements.optJSONObject(j);
                if (requirement != null) evidence.add(requirement.optString("id") + ":" + requirement.optString("gate"));
            }
            gates.add(input("Evidence gates (code:received or code:verified), comma separated · " + code, evidence.toString()));
        }
        Button save = button("Save workflow", null);
        save.setOnClickListener(view -> {
            if (saving) return;
            try {
                JSONArray tasks = new JSONArray();
                for (int i = 0; i < milestones.length(); i++) {
                    String code = milestones.optJSONObject(i).optString("code");
                    int minutes;
                    try {
                        minutes = Integer.parseInt(value(durations.get(i)));
                        if (minutes < 0 || minutes > 525600) throw new NumberFormatException();
                    } catch (NumberFormatException invalid) {
                        durations.get(i).setError("Enter a whole number from 0 to 525600.");
                        return;
                    }
                    JSONArray parents = new JSONArray();
                    for (String parent : value(prerequisites.get(i)).split(",")) {
                        if (!parent.isBlank()) parents.put(parent.trim());
                    }
                    JSONArray requirements = new JSONArray();
                    for (String entry : value(gates.get(i)).split(",")) {
                        if (entry.isBlank()) continue;
                        String[] gate = entry.trim().split(":", -1);
                        if (gate.length != 2 || !java.util.List.of("received", "verified").contains(gate[1].trim())) {
                            gates.get(i).setError("Use milestone-code:received or milestone-code:verified.");
                            return;
                        }
                        requirements.put(new JSONObject().put("id", gate[0].trim()).put("gate", gate[1].trim()));
                    }
                    tasks.put(new JSONObject().put("code", code).put("duration_minutes", minutes)
                            .put("prerequisites", parents).put("requirements", requirements));
                }
                JSONObject payload = new JSONObject().put("expected_version", version)
                        .put("definition", new JSONObject().put("tasks", tasks));
                saveWorkflow(id, payload, save);
            } catch (JSONException invalid) {
                feedback.result("Check the workflow details and try again.", true);
            }
        });
        guardEdits();
    }

    private void saveWorkflow(int id, JSONObject payload, Button save) {
        saving = true;
        Runnable restore=ph.edu.umak.crm.app.staff.common.StaffDesignPage.freeze(this.body);
        PendingStatus current = feedback;
        current.working("Validating prerequisites and saving workflow…");
        actions.request("/" + id + "/workflow", payload, (data, error) -> {
            saving = false;restore.run();
            if (feedback != current) return;
            if (error == null) detail(id);
            else {
                current.result(error, true);
                if (error.contains("Reload before saving")) {save.setEnabled(false);button("Reload research progress", () -> detail(id));}
                else save.setEnabled(true);
            }
        });
    }

    private void editStage(int id, int version, String current, JSONObject record) {
        reset("Update overall stage", "Back to research progress", () -> detail(id));
        String[] values = switch (current) {
            case "Registered" -> new String[]{"In-Progress"};
            case "UnderReview" -> new String[]{"In-Progress", "Completed"};
            case "In-Progress" -> new String[]{"UnderReview", "Completed"};
            default -> new String[0];
        };
        if (values.length == 0) {
            line("Use Correct completion to amend this completed study.");
            return;
        }
        String[] labels = java.util.Arrays.stream(values).map(RegisteredProgressView::stageLabel).toArray(String[]::new);
        Spinner stage = options("New stage", labels);
        EditText effective = input("Effective date (YYYY-MM-DD)", "");
        EditText supporting = input("Supporting details", "");
        EditText reference = input("Supporting reference", "");
        EditText nextAction = input("Next action", record.optString("next_action", ""));
        CheckBox none = check("No next action recorded");
        EditText contact = input("Responsible contact", record.optString("responsible_contact", ""));
        EditText deadline = input("Deadline (YYYY-MM-DD)", dateValue(record, "deadline"));
        EditText completionDate = input("Completion date (when completing)", "");
        EditText completion = input("Completion details", "");
        EditText note = input("Internal staff note", "");
        saveButton(id, "Save stage update", () -> event("stage", version)
                .put("stage", values[stage.getSelectedItemPosition()]).put("effective_date", date(effective))
                .put("supporting_details", value(supporting)).put("reference", value(reference))
                .put("next_action", value(nextAction)).put("no_next_action", none.isChecked())
                .put("responsible_contact", value(contact)).put("deadline", date(deadline))
                .put("completion_date", date(completionDate)).put("completion_details", value(completion))
                .put("internal_note", value(note)));
    }

    private void milestones(int id, int version, JSONArray milestones) {
        reset("Research milestones", "Back to research progress", () -> detail(id));
        if (milestones == null) return;
        for (int i = 0; i < milestones.length(); i++) {
            JSONObject item = milestones.optJSONObject(i);
            if (item != null) button(item.optString("title") + " · " + item.optString("status"),
                    () -> editMilestone(id, version, milestones, item));
        }
    }

    private void editMilestone(int id,int version,JSONArray milestones,JSONObject item){
        MilestoneEditorDialog.showLive((android.app.Activity)getContext(),item,(fields,result)->{
            if(saving)return;
            try{fields.put("kind","milestone").put("expected_version",version);saving=true;
                actions.request("/"+id+"/events",fields,(data,error)->{saving=false;result.accept(data,error);});
            }catch(JSONException invalid){result.accept(null,"Check the milestone details.");}
        },()->detail(id),()->detail(id));
    }

    private void editOutcome(int id, int version, boolean publication, JSONObject previous) {
        String kind = publication ? "publication" : "presentation";
        reset(publication ? "Record publication" : "Record presentation", "Back to research progress", () -> detail(id));
        EditText effective = input(publication ? "Publication date (YYYY-MM-DD)" : "Presentation date (YYYY-MM-DD)",
                dateValue(previous, "date"));
        String key = publication ? "citation" : "event";
        EditText information = input(publication ? "Publication citation" : "Presentation event",
                previous == null ? "" : previous.optString(key));
        EditText reason = input("Change reason (optional)", "");
        saveButton(id, "Save " + kind, () -> event(kind, version)
                .put("effective_date", date(effective)).put(key, value(information)).put("reason", value(reason)));
    }

    private void editCompletion(int id,int version,JSONObject previous){
        reset("Completion correction","Back to research progress",()->detail(id));
        JSONObject record=new JSONObject();try{record.put("id",id).put("title","REG-"+id).put("completion",previous==null?JSONObject.NULL:previous);}catch(JSONException impossible){throw new IllegalStateException(impossible);}
        body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(CompletionCorrectionView.createLive((android.app.Activity)getContext(),record,(fields,result)->{
            if(saving)return;
            try{fields.put("kind","completion_correction").put("expected_version",version);saving=true;
                actions.request("/"+id+"/events",fields,(data,error)->{saving=false;result.accept(data,error);});
            }catch(JSONException invalid){result.accept(null,"Check the completion details.");}
        },()->detail(id),()->detail(id))));guardEdits();
    }

    private interface Response { void parse(String data) throws JSONException; }
    private interface Payload { JSONObject make() throws JSONException; }

    private void request(String path, Response response, Runnable retry) {
        PendingStatus current = feedback;
        current.working("Loading saved progress…");
        actions.request(path, null, (data, error) -> {
            if (feedback != current) return;
            if (error != null) {
                current.result(error, true);
                button("Retry", retry);
                return;
            }
            try {
                current.clear();
                response.parse(data);
            } catch (JSONException invalid) {
                current.result("CRM returned invalid research progress. Try again.", true);
            }
        });
    }

    private void saveButton(int id, String title, Payload payload) {
        Button save = button(title, null);
        save.setOnClickListener(view -> {
            if (saving) return;
            try {
                JSONObject body = payload.make();
                saving = true;
                Runnable restore=ph.edu.umak.crm.app.staff.common.StaffDesignPage.freeze(this.body);
                PendingStatus current = feedback;
                current.working("Saving research progress…");
                actions.request("/" + id + "/events", body, (data, error) -> {
                    saving = false;restore.run();
                    if (feedback != current) return;
                    if (error == null) detail(id);
                    else {
                        current.result(error, true);
                        if (error.contains("Reload before saving")) {save.setEnabled(false);button("Reload research progress", () -> detail(id));}
                        else save.setEnabled(true);
                    }
                });
            } catch (JSONException invalid) {
                feedback.result("Check the progress details and try again.", true);
            }
        });
        guardEdits();
    }

    private void reset(String title,String back,Runnable onBack){
        removeAllViews();backAction=onBack;editorBaseline=null;
        var page=new ph.edu.umak.crm.app.staff.common.StaffDesignPage(getContext(),title,back,this::back);
        addView(page);feedback=page.status;body=page.section("");
    }
    private void line(String text){ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(body,text,true);}
    private Button button(String title,Runnable action){Button button=UiButton.outline(getContext(),title,UiTheme.BORDER,UiTheme.NAVY,action);body.addView(button);return button;}
    private EditText input(String title,String initial){line(title);EditText field=new EditText(getContext());field.setText(initial);field.setContentDescription(title);ph.edu.umak.crm.app.staff.common.StaffDesignPage.style(field,false);body.addView(field);return field;}
    private Spinner options(String title,String[] values){line(title);Spinner spinner=new Spinner(getContext());spinner.setContentDescription(title);spinner.setAdapter(new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_dropdown_item,values));spinner.setMinimumHeight(UiTheme.dp(getContext(),48));body.addView(spinner);return spinner;}
    private CheckBox check(String title){CheckBox box=new CheckBox(getContext());box.setText(title);body.addView(box);return box;}

    private static JSONObject event(String type, int version) throws JSONException {
        return new JSONObject().put("kind", type).put("expected_version", version);
    }

    private static String value(EditText field) { return field.getText().toString().trim(); }
    private static Object date(EditText field) { return value(field).isEmpty() ? JSONObject.NULL : value(field); }

    private static String dateValue(JSONObject value, String key) {
        return value == null || value.isNull(key) ? "" : value.optString(key);
    }
}
