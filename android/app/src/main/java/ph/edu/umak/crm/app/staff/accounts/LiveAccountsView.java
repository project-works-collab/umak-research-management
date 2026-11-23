package ph.edu.umak.crm.app.staff.accounts;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class LiveAccountsView extends LinearLayout {
    public interface Actions {
        void request(String path, JSONObject body, BiConsumer<String, String> result);
        void back();
    }
    private static final String[] PERMISSIONS = {"content_management", "request_processing", "registry_management",
            "progress_updates", "reporting", "account_administration"};
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
    private UserAccountsView directory;

    public LiveAccountsView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        setOrientation(VERTICAL);
    }

    public void load() {
        reset("User accounts", "Back to Manage", actions::back);
        PendingStatus current = status;
        current.working("Loading accounts…");
        actions.request("", null, (data, error) -> {
            if (status != current) return;
            if (error != null) { current.result(error, true); button("Retry", this::load); return; }
            try {
                JSONArray users = new JSONArray(data);
                current.clear();
                List<ph.edu.umak.crm.app.staff.StaffData.StaffAccountItem> models=new ArrayList<>();
                java.util.Map<Integer,JSONObject> accounts=new java.util.HashMap<>();
                for(int i=0;i<users.length();i++){JSONObject row=users.getJSONObject(i);var model=model(row);models.add(model);accounts.put(model.serverId,row);}
                if(directory==null)directory=new UserAccountsView(getContext(),new UserAccountsView.Callbacks(){
                    public void onBack(){actions.back();}
                    public void onCreateStaffAccount(){create();}
                    public void onSelectAccount(ph.edu.umak.crm.app.staff.StaffData.StaffAccountItem account){detail(accounts.get(account.serverId));}
                });
                else directory=new UserAccountsView(getContext(),new UserAccountsView.Callbacks(){
                    public void onBack(){actions.back();}public void onCreateStaffAccount(){create();}
                    public void onSelectAccount(ph.edu.umak.crm.app.staff.StaffData.StaffAccountItem account){detail(accounts.get(account.serverId));}
                });
                directory.bindLive(models);body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(directory.getContentLayout()));
            } catch (JSONException invalid) { current.result("CRM returned invalid accounts.", true); }
        });
    }

    private static ph.edu.umak.crm.app.staff.StaffData.StaffAccountItem model(JSONObject row)throws JSONException {
        var permissions=new java.util.LinkedHashMap<String,Boolean>();JSONArray current=row.optJSONArray("permissions");
        for(String permission:PERMISSIONS){boolean allowed=false;if(current!=null)for(int i=0;i<current.length();i++)if(permission.equals(current.optString(i)))allowed=true;permissions.put(permission,allowed);}
        String name=row.isNull("display_name")?row.optString("email"):row.optString("display_name");
        boolean staff="staff".equals(row.getString("role"));
        var result=new ph.edu.umak.crm.app.staff.StaffData.StaffAccountItem(name.isEmpty()?"?":name.substring(0,1),name,staff?"CRM Staff":"Researcher",staff?null:"faculty".equals(row.optString("role"))?"Faculty":"Student",row.optBoolean("active")?"Active":"Inactive",row.getString("email"),String.valueOf(row.getInt("id")),permissions);
        result.serverId=row.getInt("id");return result;
    }
    private void create(){
        reset("Create Staff Account","Back to accounts",this::load);
        List<CheckBox> choices=permissionChoices(new JSONArray());
        CreateStaffAccountView design=new CreateStaffAccountView(getContext(),new CreateStaffAccountView.Callbacks(){
            public void onBack(){back();}
            public void onAccountCreated(String name,String college,String email){
                if(name.trim().isEmpty()||college.trim().isEmpty()||email.trim().isEmpty()){status.result("Complete the highlighted account details.",true);return;}
                try{post("",new JSONObject().put("display_name",name.trim()).put("email",email.trim()).put("college",college.trim()).put("permissions",selected(choices)),null);}catch(JSONException invalid){status.result("Check the account details.",true);}
            }
        });
        design.bindLive();design.setPermissionOptions(choices);body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(design.getContentLayout()),0);guardEdits();
    }
    private void detail(JSONObject account){
        if(account==null){load();return;}
        int id=account.optInt("id");reset("Account Details","Back to accounts",this::load);
        try{
            AccountDetailsView design=new AccountDetailsView(getContext(),null);
            design.bindLive(model(account),new AccountDetailsView.LiveActions(){
                public void back(){LiveAccountsView.this.back();}
                public void permissions(JSONArray permissions){try{post("/"+id+"/permissions",new JSONObject().put("permissions",permissions),null);}catch(JSONException invalid){status.result("Check permissions.",true);}}
                public void category(String value){LiveAccountsView.this.category(id,value);}
                public void state(boolean active){new AlertDialog.Builder(getContext()).setTitle((active?"Activate":"Deactivate")+" "+account.optString("email")+"?").setNegativeButton("Cancel",null).setPositiveButton("Confirm",(dialog,which)->{try{post("/"+id+"/state",new JSONObject().put("active",active).put("confirmed",true),null);}catch(JSONException invalid){status.result("Check the account change.",true);}}).show();}
                public void recovery(){LiveAccountsView.this.recovery(id,account.optString("email"));}
                public void history(){LiveAccountsView.this.history(id);}
            });body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(design.getContentLayout()));guardEdits();
        }catch(JSONException invalid){status.result("CRM returned invalid account details.",true);}
    }

    private void category(int id, String role) {
        try { post("/" + id + "/category", new JSONObject().put("category", role), null); }
        catch (JSONException invalid) { status.result("Select Student or Faculty.", true); }
    }

    private void recovery(int id, String email) {
        EditText reason = new EditText(getContext());
        reason.setHint("Approved recovery reason");
        var dialog = new AlertDialog.Builder(getContext()).setTitle("Issue temporary credential for " + email + "?")
                .setView(reason).setNegativeButton("Cancel", null).setPositiveButton("Confirm", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (value(reason).isEmpty()) { reason.setError("Enter the approved recovery reason."); return; }
            try {
                JSONObject payload = new JSONObject().put("reason", value(reason)).put("confirmed", true);
                dialog.dismiss();
                post("/" + id + "/recovery", payload, null);
            } catch (JSONException invalid) { reason.setError("Check the recovery reason."); }
        }));
        dialog.show();
    }

    private void history(int id) {
        reset("Account audit history", "Back to accounts", this::load);
        PendingStatus current = status;
        current.working("Loading account history…");
        actions.request("/" + id + "/history", null, (data, error) -> {
            if (current != status) return;
            if (error != null) { current.result(error, true); return; }
            try {
                JSONArray events = new JSONArray(data);
                current.clear();
                List<ph.edu.umak.crm.app.staff.StaffData.AccountAuditLogItem> logs=new ArrayList<>();
                for(int i=0;i<events.length();i++){JSONObject event=events.getJSONObject(i);logs.add(new ph.edu.umak.crm.app.staff.StaffData.AccountAuditLogItem(event.optString("action"),event.optString("before_value"),event.optString("after_value"),event.optString("reason"),"Staff "+event.optString("actor_id"),event.optString("recorded_at")));}
                AccountAuditHistoryView design=new AccountAuditHistoryView(getContext(),()->load());design.bindLive("Account",String.valueOf(id),logs);body.addView(ph.edu.umak.crm.app.staff.common.StaffDesignPage.inline(design.getContentLayout()));
            } catch (JSONException invalid) { current.result("CRM returned invalid account history.", true); }
        });
    }

    private void post(String path, JSONObject payload, Button button) {
        if (saving) return;
        saving = true;
        Runnable restore=ph.edu.umak.crm.app.staff.common.StaffDesignPage.freeze(body);
        PendingStatus current = status;
        current.working("Saving account changes…");
        actions.request(path, payload, (data, error) -> {
            saving = false;restore.run();
            if (current != status) return;
            if (button != null) button.setEnabled(true);
            if (error != null) { current.result(error, true); return; }
            try {
                JSONObject result = new JSONObject(data);
                if (result.has("temporary_credential")) {
                    reset("Temporary credential issued", "Back to accounts", this::load);
                    line(result.getJSONObject("account").optString("email"));
                    TextView credential = line(result.getString("temporary_credential"));
                    credential.setTextIsSelectable(true);
                    line("The account holder must replace this temporary credential before accessing protected functions.");
                } else load();
            } catch (JSONException invalid) { current.result("CRM could not confirm the account change.", true); }
        });
    }

    private List<CheckBox> permissionChoices(JSONArray permissions) {
        List<CheckBox> choices = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            CheckBox choice = new CheckBox(getContext());
            choice.setText(permission.replace('_', ' '));
            choice.setTag(permission);
            if (permissions != null) for (int i = 0; i < permissions.length(); i++)
                if (permission.equals(permissions.optString(i))) choice.setChecked(true);
            body.addView(choice);
            choices.add(choice);
        }
        return choices;
    }
    private static JSONArray selected(List<CheckBox> checks) {
        JSONArray values = new JSONArray();
        for (CheckBox check : checks) if (check.isChecked()) values.put(check.getTag().toString());
        return values;
    }
    private boolean required(EditText... fields) {
        boolean valid = true;
        for (EditText field : fields) if (value(field).isEmpty()) { field.setError("Required"); valid = false; }
        if (!valid) status.result("Complete the highlighted account details.", true);
        return valid;
    }
    public boolean isSaving(){return saving;}
    public boolean handleBack(){back();return true;}
    private void reset(String title,String back,Runnable action){removeAllViews();backAction=action;editorBaseline=null;var page=new ph.edu.umak.crm.app.staff.common.StaffDesignPage(getContext(),title,back,this::back);addView(page);status=page.status;body=page.section("");}
    private TextView line(String content){return ph.edu.umak.crm.app.staff.common.StaffDesignPage.line(body,content,true);}
    private EditText input(String title,String value){line(title);EditText field=new EditText(getContext());field.setText(value);field.setContentDescription(title);ph.edu.umak.crm.app.staff.common.StaffDesignPage.style(field,false);body.addView(field);return field;}
    private Button button(String label,Runnable action){Button button=UiButton.outline(getContext(),label,UiTheme.BORDER,UiTheme.NAVY,action);body.addView(button);return button;}
    private static String value(EditText field) { return field.getText().toString().trim(); }
}
