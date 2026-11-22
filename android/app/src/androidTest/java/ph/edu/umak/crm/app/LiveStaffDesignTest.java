package ph.edu.umak.crm.app;

import static org.junit.Assert.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import ph.edu.umak.crm.app.staff.accounts.LiveAccountsView;
import ph.edu.umak.crm.app.staff.common.StaffDesignPage;
import ph.edu.umak.crm.app.staff.registry.LiveRegistryData;
import ph.edu.umak.crm.app.staff.registry.LiveRegistryDirectoryView;
import ph.edu.umak.crm.app.staff.reporting.LiveReportsView;

public final class LiveStaffDesignTest {
    @Test public void accountSaveFreezesInputsAndRetainsRejectedValues() {
        try(var scenario=ActivityScenario.launch(MainActivity.class)){
            scenario.onActivity(activity->{
                class Api implements LiveAccountsView.Actions {
                    BiConsumer<String,String> pending;JSONObject saved;
                    public void back(){}
                    public void request(String path,JSONObject body,BiConsumer<String,String> result){if(body==null)result.accept("[]",null);else{saved=body;pending=result;}}
                }
                Api api=new Api();LiveAccountsView view=new LiveAccountsView(activity,api);activity.setContentView(view);view.load();click(view,"Create Staff Account");
                EditText name=(EditText)find(view,"Name");assertEquals("",name.getText().toString());name.setText("Actual invited staff");
                ((EditText)find(view,"College or unit")).setText("Actual College");
                ((EditText)find(view,"Institutional email")).setText("actual@umak.edu.ph");((android.widget.CheckBox)find(view,"reporting")).setChecked(true);click(view,"Create Account");
                assertTrue(view.isSaving());assertFalse(name.isEnabled());assertEquals("Actual invited staff",api.saved.optString("display_name"));assertEquals("Actual College",api.saved.optString("college"));assertEquals("reporting",api.saved.optJSONArray("permissions").optString(0));
                api.pending.accept(null,"This email already exists.");assertFalse(view.isSaving());assertTrue(name.isEnabled());assertEquals("Actual invited staff",name.getText().toString());assertNotNull(find(view,"This email already exists."));
            });
        }
    }
    @Test public void invalidReportKeepsFiltersAndPermissionDenialUsesDesignedState(){
        try(var scenario=ActivityScenario.launch(MainActivity.class)){
            scenario.onActivity(activity->{
                class Api implements LiveReportsView.Actions { boolean denied;public void back(){} public void request(JSONObject filters,BiConsumer<String,String> result){result.accept("{}",denied?"Access denied.":null);} }
                Api api=new Api();LiveReportsView view=new LiveReportsView(activity,api);activity.setContentView(view);view.load();
                editor(view,"Adviser").setText("Actual adviser");click(view,"Generate report");assertNotNull(find(view,"CRM returned invalid report data. Try again."));assertEquals("Actual adviser",editor(view,"Adviser").getText().toString());
                api.denied=true;click(view,"Generate report");assertNull(find(view,"Generate report"));assertTrue(view.handleBack());assertEquals("Actual adviser",editor(view,"Adviser").getText().toString());
            });
        }
    }
    @Test public void registryLoadsOlderPagesAndRetryRetainsCursor() {
        try(var scenario=ActivityScenario.launch(MainActivity.class)){
            scenario.onActivity(activity->{
                class Api implements LiveRegistryDirectoryView.Actions {
                    int calls;String query;boolean fail=true;
                    public void open(int id){}public void create(){}public void back(){}
                    public void request(String q,BiConsumer<String,String> result){query=q;calls++;if(calls==1){JSONArray rows=new JSONArray();try{for(int id=200;id>100;id--)rows.put(new JSONObject().put("id",id).put("title","Actual study "+id));}catch(Exception e){throw new AssertionError(e);}result.accept(rows.toString(),null);}else if(fail){fail=false;result.accept(null,"Offline");}else result.accept("[]",null);}
                }
                Api api=new Api();LiveRegistryDirectoryView view=new LiveRegistryDirectoryView(activity,api);activity.setContentView(view);view.load();assertNotNull(find(view,"Actual study 101"));click(view,"Load more research");assertTrue(api.query.contains("before=101"));click(view,"Retry loading research");assertEquals(3,api.calls);assertTrue(api.query.contains("before=101"));assertNotNull(find(view,"Actual study 200"));
            });
        }
    }
    @Test public void documentSeriesUseRealFileIdsAndFreezePreservesImmutableFields() throws Exception {
        var record=LiveRegistryData.record(new JSONObject().put("id",44).put("title","Actual study"));
        JSONArray docs=new JSONArray();for(int version=2;version>=1;version--)docs.put(new JSONObject().put("id","file-"+version).put("filename","protocol.pdf").put("mime","application/pdf").put("series_id","series-a").put("version",version));
        LiveRegistryData.documents(record,docs);assertTrue(record.documents.get(0).isCurrent);assertFalse(record.documents.get(1).isCurrent);assertEquals("file-2",record.documents.get(0).fileId);
        try(var scenario=ActivityScenario.launch(MainActivity.class)){scenario.onActivity(activity->{android.widget.LinearLayout group=new android.widget.LinearLayout(activity);EditText fixed=new EditText(activity),editable=new EditText(activity);fixed.setEnabled(false);group.addView(fixed);group.addView(editable);Runnable restore=StaffDesignPage.freeze(group);assertFalse(editable.isEnabled());restore.run();assertFalse(fixed.isEnabled());assertTrue(editable.isEnabled());});}
    }
    private static EditText editor(View root,String text){if(root instanceof EditText field&&text.equals(field.getContentDescription()))return field;if(root instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){EditText found=editor(group.getChildAt(i),text);if(found!=null)return found;}return null;}
    private static EditText findHint(View root,String hint){if(root instanceof EditText field&&hint.contentEquals(field.getHint()))return field;if(root instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){EditText found=findHint(group.getChildAt(i),hint);if(found!=null)return found;}return null;}
    private static View find(View root,String text){if(text.equals(root.getContentDescription())||root instanceof TextView label&&text.contentEquals(label.getText()))return root;if(root instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){View found=find(group.getChildAt(i),text);if(found!=null)return found;}return null;}
    private static void click(View root,String text){View view=find(root,text);assertNotNull(text,view);while(!view.isClickable()&&view.getParent() instanceof View parent)view=parent;assertTrue(text,view.performClick());}
}
