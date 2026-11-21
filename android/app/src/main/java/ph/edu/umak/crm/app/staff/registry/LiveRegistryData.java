package ph.edu.umak.crm.app.staff.registry;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.research.RegisteredProgressView;

public final class LiveRegistryData {
    private LiveRegistryData() {}
    public static String text(JSONObject value,String key) { return value==null||value.isNull(key)?"":value.optString(key,""); }
    private static String time(JSONObject row,String key){return ph.edu.umak.crm.app.staff.common.StaffDesignPage.timestamp(text(row,key));}
    public static StaffRegistryData.RegistryRecordItem record(JSONObject row) throws JSONException {
        var team=new ArrayList<StaffRegistryData.TeamMember>();
        JSONArray members=row.optJSONArray("members");
        StringBuilder authors=new StringBuilder();
        if(members!=null)for(int i=0;i<members.length();i++) {
            JSONObject member=members.getJSONObject(i);String name=member.getString("name");
            if(authors.length()>0)authors.append(", ");authors.append(name);
            team.add(new StaffRegistryData.TeamMember(name,member.optInt("id")==row.optInt("registrant_id")?"Registrant":"Co-author",
                    "faculty".equals(member.optString("role"))?"Faculty":"Student",name.isEmpty()?"?":name.substring(0,1),text(row,"college")));
        }
        if(authors.length()==0)authors.append(text(row,"authors"));
        var linked=new ArrayList<StaffRegistryData.LinkedRequest>();
        JSONArray requests=row.optJSONArray("linked_requests");
        if(requests!=null)for(int i=0;i<requests.length();i++) {
            JSONObject request=requests.getJSONObject(i);
            linked.add(new StaffRegistryData.LinkedRequest("REQ-"+request.getInt("id"),request.getString("service"),request.getString("status")));
        }
        var logs=new ArrayList<StaffRegistryData.CorrectionLog>();
        JSONArray events=row.optJSONArray("history");
        if(events!=null)for(int i=0;i<events.length();i++) {
            JSONObject event=events.getJSONObject(i);
            JSONObject before=event.optJSONObject("before_value"),after=event.optJSONObject("after_value");
            if(after!=null)for(var keys=after.keys();keys.hasNext();) {
                String key=keys.next();
                if(!java.util.List.of("title","college","academic_period","adviser","members","historical","source_reference","original_registration_date","abstract").contains(key))continue;
                String old=before==null?"":text(before,key),next=text(after,key);
                if(!old.equals(next))logs.add(new StaffRegistryData.CorrectionLog(key.replace('_',' '),old,next,text(event,"reason"),
                        "Staff "+event.optInt("actor_id")+" · "+text(event,"recorded_at")));
            }
        }
        var result=new StaffRegistryData.RegistryRecordItem(row.optString("research_id","REG-"+row.getInt("id")),row.getString("title"),
                RegisteredProgressView.stageLabel(row.optString("stage","Registered")),time(row,"encoded_at").isEmpty()?time(row,"registered_at"):time(row,"encoded_at"),
                text(row,"academic_period"),authors.toString(),text(row,"college"),text(row,"adviser"),text(row,"registrant_category"),time(row,"encoded_at"),
                team,null,linked,row.optBoolean("historical")?new StaffRegistryData.HistoricalEntry("Historical entry",text(row,"source_reference"),"",text(row,"original_registration_date")):null,logs);
        result.serverId=row.getInt("id");result.version=row.optInt("version");
        if(events!=null&&events.length()>0)result.lastEditDate=time(events.getJSONObject(0),"recorded_at");
        return result;
    }
    public static void documents(StaffRegistryData.RegistryRecordItem record,JSONArray rows)throws JSONException {
        record.documents.clear();
        java.util.Set<String> series=new java.util.HashSet<>();
        for(int i=0;i<rows.length();i++) {
            JSONObject row=rows.getJSONObject(i);
            boolean current=series.add(row.optString("series_id",row.getString("id")));
            var document=new StaffRegistryData.DocumentVersion(row.getString("filename"),"Version "+row.getInt("version"),time(row,"created_at"),
                    "",current);
            document.fileId=row.getString("id");document.mime=row.getString("mime");record.documents.add(document);
        }
    }
}
