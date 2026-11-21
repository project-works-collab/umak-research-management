package ph.edu.umak.crm.app.staff.registry;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
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

/** Searches and paginates the entire authorized registry, retaining the reference filters. */
public final class LiveRegistryDirectoryView extends LinearLayout {
    public interface Actions {
        void request(String query,BiConsumer<String,String> result);
        void open(int id); void create(); void back();
    }
    private final Actions actions;
    private final ResearchRegistryView design;
    private final PendingStatus status;
    private final android.widget.Button more;
    private final android.widget.TextView scope;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final List<StaffRegistryData.RegistryRecordItem> rows=new ArrayList<>();
    private String keyword="",college="",period="",key="";
    private int generation;
    private boolean loading,loaded;
    private Runnable search;
    public LiveRegistryDirectoryView(Activity activity,Actions actions){
        super(activity);this.actions=actions;setOrientation(VERTICAL);
        design=new ResearchRegistryView(activity,new ResearchRegistryView.Callbacks(){
            public void onOpenRecord(StaffRegistryData.RegistryRecordItem row){actions.open(row.serverId);}
            public void onAddResearchEntry(){actions.create();}
            public void onFiltersChanged(String term,String unit,String academic){
                String next=term+"\n"+unit+"\n"+academic;if(next.equals(key))return;
                key=next;keyword=term;college=unit;period=academic;generation++;loading=false;
                if(search!=null)handler.removeCallbacks(search);
                search=()->page(false);handler.postDelayed(search,250);
            }
        });
        design.bindLive(List.of());addView(design.getContentLayout());
        status=new PendingStatus(activity);addView(status);
        scope=new android.widget.TextView(activity);scope.setTextColor(UiTheme.MUTED);addView(scope);
        more=UiButton.outline(activity,"Load more research",UiTheme.BORDER,UiTheme.NAVY,()->page(true));more.setVisibility(GONE);addView(more);
        key="\n\n";
    }
    public void load(){if(!loaded&&!loading)page(false);}
    private void page(boolean append){
        if(loading)return;loading=true;more.setEnabled(false);int current=++generation;
        android.net.Uri.Builder query=new android.net.Uri.Builder().appendQueryParameter("keyword",keyword)
            .appendQueryParameter("college",college).appendQueryParameter("academic_period",period);
        if(append&&!rows.isEmpty())query.appendQueryParameter("before",String.valueOf(rows.get(rows.size()-1).serverId));
        status.working("Loading registered research…");
        actions.request(query.build().toString(),(data,error)->{
            if(generation!=current)return;loading=false;more.setEnabled(true);
            if(error!=null){status.result(error,true);more.setText("Retry loading research");more.setVisibility(VISIBLE);more.setOnClickListener(v->page(append));return;}
            try{
                JSONArray response=new JSONArray(data);List<StaffRegistryData.RegistryRecordItem> parsed=new ArrayList<>();
                for(int i=0;i<response.length();i++){
                    JSONObject row=response.getJSONObject(i);row.put("college",row.optString("department",""))
                        .put("stage",row.optString("lifecycle_state","Registered")).put("authors",row.optString("registrant_name",""));
                    parsed.add(LiveRegistryData.record(row));
                }
                if(!append)rows.clear();for(var row:parsed){rows.removeIf(existing->existing.serverId==row.serverId);rows.add(row);}
                loaded=true;more.setText("Load more research");more.setOnClickListener(v->page(true));design.bindLive(rows);status.clear();more.setVisibility(response.length()==100?VISIBLE:GONE);
                scope.setText(rows.size()+" matching records loaded. Directory export includes these loaded records.");
            }catch(JSONException invalid){status.result("CRM returned invalid registry data. Try again.",true);}
        });
    }
    public void refresh(){loaded=false;if(!loading)page(false);}
}
