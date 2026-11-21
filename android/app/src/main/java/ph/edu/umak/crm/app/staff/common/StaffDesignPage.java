package ph.edu.umak.crm.app.staff.common;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

/** The banner, card and field treatment shared by the staff reference screens and live editors. */
public final class StaffDesignPage extends LinearLayout {
    public final LinearLayout content;
    public final PendingStatus status;
    public StaffDesignPage(Context context,String title,String backLabel,Runnable back) {
        super(context); setOrientation(VERTICAL); setBackgroundColor(UiTheme.PAPER);
        content=new LinearLayout(context);content.setOrientation(VERTICAL);
        int p=UiTheme.dp(context,16);content.setPadding(p,p,p,p);
        if(back!=null)content.addView(UiButton.text(context,backLabel,UiTheme.NAVY,back));
        status=new PendingStatus(context);content.addView(status);
        addView(PageTitle.wrap(content,title,null,false));
    }
    public LinearLayout section(String title) {
        LinearLayout card=new LinearLayout(getContext());card.setOrientation(VERTICAL);
        int p=UiTheme.dp(getContext(),16);card.setPadding(p,p,p,p);
        card.setBackground(UiTheme.shape(getContext(),UiTheme.WHITE,10,UiTheme.BORDER));
        LayoutParams params=new LayoutParams(-1,-2);params.bottomMargin=UiTheme.dp(getContext(),12);
        content.addView(card,params);
        if(title!=null&&!title.isEmpty())line(card,title,true);
        return card;
    }
    public static TextView line(LinearLayout parent,String text,boolean heading) {
        TextView label=new TextView(parent.getContext());label.setText(text);
        label.setTypeface(heading?UiTheme.boldFont(parent.getContext()):UiTheme.bodyFont(parent.getContext()));
        label.setTextColor(heading?UiTheme.NAVY:UiTheme.MUTED);ScreenTypography.apply(label,heading?16:14);
        int gap=UiTheme.dp(parent.getContext(),8);label.setPadding(0,gap,0,gap);parent.addView(label);return label;
    }
    public static void style(EditText field,boolean multiline) {
        field.setTextColor(UiTheme.NAVY);field.setHintTextColor(UiTheme.MUTED);
        field.setTypeface(UiTheme.bodyFont(field.getContext()));ScreenTypography.apply(field,14);
        int p=UiTheme.dp(field.getContext(),12);field.setPadding(p,p,p,p);
        field.setMinHeight(UiTheme.dp(field.getContext(),multiline?96:48));
        field.setBackground(UiTheme.shape(field.getContext(),UiTheme.WHITE,8,UiTheme.BORDER));
    }
    /** Embed reference content in an existing page without repeating its title banner. */
    public static View inline(View view){
        if(view instanceof LinearLayout wrapper && wrapper.getChildCount()==2 && wrapper.getChildAt(1) instanceof LinearLayout content){
            wrapper.removeView(content);stripLeadingBack(content);return content;
        }
        stripLeadingBack(view);return view;
    }
    private static void stripLeadingBack(View view){
        if(view instanceof android.view.ViewGroup group&&group.getChildCount()>0){
            View first=group.getChildAt(0);
            if(first instanceof TextView text&&text.getText().toString().matches("(?i)^(?:< )?Back(?: to .*)?$"))group.removeView(first);
            else if(first instanceof android.view.ViewGroup)stripLeadingBack(first);
        }
    }
    public static String timestamp(String value){
        if(value==null||value.isBlank()||value.equals("null"))return "";
        try{return java.time.OffsetDateTime.parse(value.replace(' ','T')).atZoneSameInstant(java.time.ZoneId.of("Asia/Manila")).format(java.time.format.DateTimeFormatter.ofPattern("MMM d, uuuu · h:mm a",java.util.Locale.ENGLISH));}
        catch(java.time.DateTimeException ignored){return value;}
    }
    public static String fingerprint(View view){
        StringBuilder result=new StringBuilder();
        if(view instanceof EditText field)result.append(field.getText()).append('\u0000');
        else if(view instanceof android.widget.CompoundButton box)result.append(box.isChecked()).append('\u0000');
        else if(view instanceof android.widget.Spinner choice)result.append(choice.getSelectedItemPosition()).append('\u0000');
        if(view instanceof android.view.ViewGroup group)for(int i=0;i<group.getChildCount();i++)result.append(fingerprint(group.getChildAt(i)));
        return result.toString();
    }
    public static void confirmDiscard(Context context,Runnable leave){
        new android.app.AlertDialog.Builder(context).setTitle("Discard unsaved changes?")
            .setMessage("These edits have not been saved to CRM.").setPositiveButton("Discard",(dialog,which)->leave.run())
            .setNegativeButton("Keep editing",null).show();
    }
    /** Disable an in-flight editor while preserving immutable field states on retry. */
    public static Runnable freeze(View root) {
        java.util.Map<View,Boolean> enabled=new java.util.IdentityHashMap<>();
        freezeInto(root,enabled);
        return () -> { for(var entry:enabled.entrySet())entry.getKey().setEnabled(entry.getValue()); };
    }
    private static void freezeInto(View view,java.util.Map<View,Boolean> enabled){
        enabled.put(view,view.isEnabled());view.setEnabled(false);
        if(view instanceof android.view.ViewGroup group)for(int i=0;i<group.getChildCount();i++)freezeInto(group.getChildAt(i),enabled);
    }
    public static void detach(View view) { if(view.getParent() instanceof android.view.ViewGroup parent)parent.removeView(view); }
}
