package ph.edu.umak.crm.app.staff.lifecycle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import ph.edu.umak.crm.app.research.RegisteredProgressView;

public final class LiveLifecycleData {
    private LiveLifecycleData() {}
    public static StaffLifecycleData.ResearchLifecycleState state(JSONObject record)throws JSONException {
        var history=new ArrayList<StaffLifecycleData.StageHistoryEntry>();
        JSONArray entries=record.getJSONArray("history");
        for(int i=entries.length()-1;i>=0;i--) {
            JSONObject event=entries.getJSONObject(i),before=event.optJSONObject("before"),after=event.optJSONObject("after");
            String transition=before!=null&&after!=null&&before.has("stage")?
                    RegisteredProgressView.stageLabel(before.optString("stage"))+" → "+RegisteredProgressView.stageLabel(after.optString("stage")):
                    event.getString("kind").replace('_',' ');
            history.add(new StaffLifecycleData.StageHistoryEntry(transition,text(event,"effective_date").isEmpty()?text(event,"recorded_at"):text(event,"effective_date"),
                    text(event,"supporting_details"),text(event,"reason"),"Staff "+event.optInt("actor_id")+" · "+text(event,"recorded_at"),"completion_correction".equals(event.optString("kind"))));
        }
        return new StaffLifecycleData.ResearchLifecycleState("REG-"+record.getInt("id"),record.getString("title"),RegisteredProgressView.stageLabel(record.getString("stage")),
                null,null,"","","",text(record,"next_action"),text(record,"next_action").isEmpty(),null,
                new StaffLifecycleData.OutcomeCompletion("Not completed","",false),new StaffLifecycleData.OutcomePresentation("Not recorded","",""),
                new StaffLifecycleData.OutcomePublication("Not recorded","",""),history);
    }
    private static String text(JSONObject object,String key){return object.isNull(key)?"":object.optString(key,"");}
}
