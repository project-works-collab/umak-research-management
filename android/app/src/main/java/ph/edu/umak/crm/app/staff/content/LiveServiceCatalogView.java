package ph.edu.umak.crm.app.staff.content;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.staff.StaffData;
import ph.edu.umak.crm.app.staff.common.StaffDesignPage;

/** Coordinates the reference catalog, authoring, forms and publication screens with CRM. */
public final class LiveServiceCatalogView {
    public interface Actions {
        void request(String path,JSONObject body,BiConsumer<String,String> complete);
        void replaceForm(int id,Runnable complete,java.util.function.Consumer<String> failure);
        void downloadForm(int id,String name,String mime,Runnable failure);
        void back();
    }
    private final Context context;
    private final Actions actions;
    private final LinearLayout layout;
    private LinearLayout body;
    private PendingStatus status;
    private boolean saving,loading,formsDirectory;
    private Runnable backAction;
    private String editorBaseline;
    private void guardEdits(){editorBaseline=ph.edu.umak.crm.app.staff.common.StaffDesignPage.fingerprint(body);}
    private void back(){
        if(saving||backAction==null)return;
        if(editorBaseline!=null&&!editorBaseline.equals(ph.edu.umak.crm.app.staff.common.StaffDesignPage.fingerprint(body)))
            ph.edu.umak.crm.app.staff.common.StaffDesignPage.confirmDiscard(context,()->{editorBaseline=null;backAction.run();});
        else backAction.run();
    }
    private JSONArray rows=new JSONArray();
    private ServiceCatalogView catalog;
    private ServiceAuthoringStep1View editor;
    private JSONObject editing;

    public LiveServiceCatalogView(Context context,Actions actions){this.context=context;this.actions=actions;layout=new LinearLayout(context);layout.setOrientation(LinearLayout.VERTICAL);}
    public LinearLayout view(){return layout;}
    public boolean isSaving(){return saving;}
    public boolean handleBack(){back();return true;}
    public void openFormsDirectory(){formsDirectory=true;if(!loading)load();}
    public void load(){load("");}
    private void load(String message){
        if(loading||saving)return;reset("Service Catalog",actions::back);loading=true;status.working("Loading service revisions…");
        PendingStatus current=status;
        actions.request("",null,(data,error)->{
            loading=false;if(status!=current)return;
            if(error!=null){current.result(error,true);body.addView(UiButton.outline(context,"Retry",UiTheme.BORDER,UiTheme.NAVY,this::load));return;}
            try{
                rows=new JSONArray(data);current.clear();
                if(formsDirectory){showFormsDirectory();return;}
                if(catalog==null)catalog=new ServiceCatalogView(context,new ServiceCatalogView.Listener(){
                    public void onBackToManage(){actions.back();}
                    public void onNewService(){edit(null,null);}
                    public void onEditService(StaffData.ServiceCatalogItem item){edit(find(item.revisionId),null);}
                    public void onWithdrawService(StaffData.ServiceCatalogItem item){withdraw(find(item.revisionId));}
                    public void onForms(StaffData.ServiceCatalogItem item){forms(find(item.revisionId),false);}
                    public void onHistory(StaffData.ServiceCatalogItem item){history(find(item.revisionId));}
                });
                List<StaffData.ServiceCatalogItem> models=new ArrayList<>();
                for(int i=0;i<rows.length();i++){
                    JSONObject row=rows.getJSONObject(i);String state=row.getString("state");
                    var item=new StaffData.ServiceCatalogItem(row.getString("code"),row.getJSONObject("details").optString("name","Untitled service"),
                            "Version "+row.getInt("version"),StaffDesignPage.timestamp(row.optString("updated_at","")),state.substring(0,1).toUpperCase(java.util.Locale.ROOT)+state.substring(1),
                            "published".equals(state)?"Visible to eligible researchers":"Not visible to researchers");
                    item.revisionId=row.getInt("id");models.add(item);
                }
                catalog.bindLive(models);body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(catalog.getContentLayout()));if(!message.isEmpty())current.result(message,false);
            }catch(JSONException invalid){current.result("CRM returned an invalid catalog. Try again.",true);}
        });
    }
    private JSONObject find(int id){for(int i=0;i<rows.length();i++)if(rows.optJSONObject(i).optInt("id")==id)return rows.optJSONObject(i);return null;}
    private void edit(JSONObject source,JSONObject errors){
        formsDirectory=false;editing=source;reset("Service Authoring",this::load);
        editor=new ServiceAuthoringStep1View(context,new ServiceAuthoringStep1View.Listener(){
            public void onBackToCatalog(){back();}
            public void onContinue(){saveDetails(true);}
            public void onSaveDraft(){saveDetails(false);}
        });
        editor.bindLive(source);if(errors!=null)editor.showLiveErrors(errors);body.addView(StaffDesignPage.inline(editor.getContentLayout()));guardEdits();
    }
    private void edit(JSONObject source){edit(source,null);}
    private void publish(JSONObject revision){
        try{call("/"+revision.optInt("id")+"/publish",new JSONObject().put("confirmed",true),(receipt,rejected)->{
            if(rejected==null)load("Service published.");else{
                JSONObject errors=new JSONObject();try{if(receipt!=null)errors=new JSONObject(receipt);}catch(JSONException ignored){}
                edit(revision,errors);status.result(rejected,true);
            }
        });}catch(JSONException invalid){status.result("Unable to prepare publishing.",true);}
    }
    private void saveDetails(boolean next){
        if(saving)return;
        String code=editor.liveCode();if(!code.matches("[A-Z0-9][A-Z0-9-]{1,31}")){status.result("Enter a service code using 2 to 32 letters, digits or hyphens.",true);return;}
        try{
            JSONObject details=editor.liveDetails();boolean existing=editing!=null&&"draft".equals(editing.optString("state"));
            JSONObject payload=existing?details:new JSONObject().put("code",code).put("details",details);
            call(existing?"/"+editing.optInt("id")+"/edit":"/drafts",payload,(data,error)->{
                if(error!=null){status.result(error,true);return;}
                try{
                    JSONObject receipt=new JSONObject(data);int id=receipt.getInt("id");
                    JSONObject saved=editing==null?new JSONObject():new JSONObject(editing.toString());
                    saved.put("id",id).put("code",code).put("details",details).put("state",receipt.getString("state")).put("version",receipt.getInt("version"));
                    editing=saved;if(next)forms(saved,true);else load("Draft saved to CRM.");
                }catch(JSONException invalid){status.result("CRM could not confirm the draft. Reload the catalog before retrying.",true);}
            });
        }catch(JSONException invalid){status.result("Check the service details.",true);}
    }
    private void showFormsDirectory(){
        reset("Approved Forms & Resources",()->{formsDirectory=false;actions.back();});
        if(rows.length()==0)StaffDesignPage.line(body,"No service revisions yet.",false);
        for(int i=0;i<rows.length();i++){
            JSONObject row=rows.optJSONObject(i);
            body.addView(UiButton.outline(context,row.optString("code")+" · Version "+row.optInt("version")+" · "+row.optString("state"),UiTheme.BORDER,UiTheme.NAVY,()->forms(row,false)));
        }
    }
    private void forms(JSONObject revision,boolean wizard){
        if(revision==null){load();return;}
        reset("Approved Forms & Resources",wizard?()->edit(revision,null):formsDirectory?this::showFormsDirectory:this::load);
        List<String> previous=new ArrayList<>();
        for(int i=0;i<rows.length();i++){
            JSONObject row=rows.optJSONObject(i);
            if(row.optString("code").equals(revision.optString("code"))&&row.optInt("id")!=revision.optInt("id"))previous.add("Version "+row.optInt("version")+" · "+row.optString("state")+" · "+row.optString("form_name","No form"));
        }
        JSONObject details=revision.optJSONObject("details");
        var model=new StaffData.ApprovedFormItem(revision.optString("code"),details==null?"":details.optString("name","Untitled service"),
                revision.isNull("form_name")||!revision.has("form_name")?"No blank form uploaded":revision.optString("form_name"),"Version "+revision.optInt("version"),previous);
        model.live=true;model.state=revision.optString("state");
        ApprovedFormsView design=new ApprovedFormsView(context,new ApprovedFormsView.Listener(){
            public void onBackToManage(){if(wizard)edit(revision,null);else load();}
            public void onReplaceVersion(){replace(revision,wizard);}
            public void onWithdraw(){withdraw(revision);}
            public void onDownload(){if(!revision.isNull("form_name")&&revision.has("form_name"))actions.downloadForm(revision.optInt("id"),revision.optString("form_name"),revision.optString("form_mime"),()->status.result("Unable to open the form. Try again.",true));else status.result("Upload a blank form to this draft first.",true);}
        },model);
        body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(design.getContentLayout()));
        if(wizard){StaffDesignPage.line(body,"Step 2 of 3: Forms & Resources",true);body.addView(UiButton.primary(context,"Continue to Access & Publishing",()->publishing(revision)));}
    }
    private void replace(JSONObject revision,boolean wizard){
        if(saving)return;
        if(!"draft".equals(revision.optString("state"))){
            confirm("Create a new form revision?","The published form stays available while you prepare the next draft.",()->{
                try{call("/drafts",new JSONObject().put("code",revision.getString("code")).put("details",revision.getJSONObject("details")),(data,error)->{
                    if(error!=null){status.result(error,true);return;}
                    try{JSONObject draft=new JSONObject(revision.toString()),receipt=new JSONObject(data);draft.put("id",receipt.getInt("id")).put("version",receipt.getInt("version")).put("state","draft");replace(draft,wizard);}catch(JSONException invalid){status.result("Reload the catalog to find the saved draft.",true);}
                });}catch(JSONException invalid){status.result("Unable to prepare a revision.",true);}
            });return;
        }
        confirm("Replace form version?","Upload a PDF or DOCX for this draft revision.",()->{saving=true;Runnable restore=StaffDesignPage.freeze(body);actions.replaceForm(revision.optInt("id"),()->{
            saving=false;restore.run();
            status.working("Loading the saved form…");PendingStatus current=status;
            actions.request("",null,(data,error)->{
                if(current!=status)return;if(error!=null){current.result(error,true);return;}
                try{rows=new JSONArray(data);JSONObject saved=find(revision.optInt("id"));if(saved==null)throw new JSONException("Draft missing");forms(saved,wizard);}catch(JSONException invalid){current.result("Reload the catalog to find the saved form.",true);}
            });
        },error->{saving=false;restore.run();status.result(error,true);});});
    }
    private void publishing(JSONObject revision){
        reset("Service Authoring",()->forms(revision,true));
        ServiceAuthoringStep3View[] holder=new ServiceAuthoringStep3View[1];
        holder[0]=new ServiceAuthoringStep3View(context,new ServiceAuthoringStep3View.Listener(){
            public void onBack(){back();}
            public void onEditStep1(){edit(revision,null);}
            public void onSaveDraft(){persist(false);}
            public void onPublish(){confirm("Publish service?","Eligible researchers will see this saved revision.",()->persist(true));}
            private void persist(boolean publish){
                try{JSONObject details=new JSONObject(revision.getJSONObject("details").toString());JSONArray applicants=holder[0].liveApplicants();if(applicants.length()==0&&publish){status.result("Select at least one applicant category.",true);return;}
                    if(applicants.length()==0)details.remove("applicants");else details.put("applicants",applicants);revision.put("details",details);
                    call("/"+revision.optInt("id")+"/edit",details,(data,error)->{
                        if(error!=null){status.result(error,true);return;}
                        if(!publish){load("Draft saved to CRM.");return;}
                        publish(revision);
                    });
                }catch(JSONException invalid){status.result("Check the publishing settings.",true);}
            }
        });
        holder[0].bindLive(revision);body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(holder[0].getContentLayout()));guardEdits();
    }
    private void withdraw(JSONObject revision){if(revision==null)return;confirm("Withdraw service?","This revision will be removed from the public directory.",()->{try{call("/"+revision.optInt("id")+"/withdraw",new JSONObject().put("confirmed",true),(data,error)->{if(error==null)load("Service withdrawn.");else status.result(error,true);});}catch(JSONException invalid){status.result("Unable to prepare withdrawal.",true);}});}
    private void history(JSONObject revision){
        reset("Publication History",this::load);status.working("Loading publication history…");PendingStatus current=status;
        actions.request("/"+revision.optInt("id")+"/history",null,(data,error)->{
            if(status!=current)return;if(error!=null){current.result(error,true);return;}
            try{JSONArray events=new JSONArray(data);List<StaffData.PublicationHistoryItem> models=new ArrayList<>();
                for(int i=0;i<events.length();i++){JSONObject event=events.getJSONObject(i);models.add(new StaffData.PublicationHistoryItem(revision.optString("code"),revision.optJSONObject("details").optString("name",""),event.getString("action").replace('_',' '),event.getString("action"),"Staff "+event.optString("actor_id"),event.getString("recorded_at")));}
                PublicationHistoryView design=new PublicationHistoryView(context,new PublicationHistoryView.Callbacks(){public void onBack(){load();}public void onNavigateToAccountAudit(){}});design.bindLive(models);current.clear();body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(design.getContentLayout()));
            }catch(JSONException invalid){current.result("CRM returned invalid publication history.",true);}
        });
    }
    private void call(String path,JSONObject body,BiConsumer<String,String> done){if(saving)return;saving=true;Runnable restore=StaffDesignPage.freeze(this.body);PendingStatus current=status;current.working("Saving service changes to CRM…");actions.request(path,body,(data,error)->{saving=false;restore.run();if(status==current)done.accept(data,error);});}
    private void reset(String title,Runnable back){layout.removeAllViews();backAction=back;editorBaseline=null;var page=new StaffDesignPage(context,title,"Back",this::back);layout.addView(page);status=page.status;body=page.section("");}
    private void confirm(String title,String message,Runnable proceed){new AlertDialog.Builder(context).setTitle(title).setMessage(message).setNegativeButton("Cancel",null).setPositiveButton("Confirm",(dialog,which)->proceed.run()).show();}
}
