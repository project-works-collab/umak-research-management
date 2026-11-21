package ph.edu.umak.crm.app.staff.lifecycle;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import ph.edu.umak.crm.app.common.dialogs.BottomSheet;
import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Milestone Editor Bottom Sheet (Wireframe 8.2).
 * Allows staff to update status, date, and evidence reference for an individual milestone.
 * Zero emojis compliant.
 */
public final class MilestoneEditorDialog {

    public interface Callbacks {
        void onMilestoneSaved(StaffLifecycleData.MilestoneItem updatedItem);
    }

    private MilestoneEditorDialog() {}

    public interface LiveSave { void save(org.json.JSONObject fields,java.util.function.BiConsumer<String,String> result); }
    public static void showLive(Activity activity,org.json.JSONObject item,LiveSave save,Runnable saved,Runnable reload){
        Dialog dialog=new Dialog(activity);
        var page=new ph.edu.umak.crm.app.staff.common.StaffDesignPage(activity,"Edit milestone",null,null);
        var card=page.section(item.optString("title"));
        String[] statuses={"Pending","In progress","Completed","Not applicable"};
        android.widget.Spinner status=new android.widget.Spinner(activity);status.setContentDescription("Milestone status");status.setAdapter(new android.widget.ArrayAdapter<>(activity,android.R.layout.simple_spinner_dropdown_item,statuses));status.setSelection(Math.max(0,java.util.Arrays.asList(statuses).indexOf(item.optString("status"))));card.addView(status);
        EditText date=new EditText(activity);date.setContentDescription("Milestone date (YYYY-MM-DD)");date.setHint("YYYY-MM-DD");date.setText(item.isNull("effective_date")?"":item.optString("effective_date"));ph.edu.umak.crm.app.staff.common.StaffDesignPage.style(date,false);card.addView(date);
        EditText evidence=new EditText(activity);evidence.setContentDescription("Evidence reference");evidence.setHint("Evidence reference");evidence.setText(item.optString("evidence",""));ph.edu.umak.crm.app.staff.common.StaffDesignPage.style(evidence,true);card.addView(evidence);
        android.widget.Spinner review=new android.widget.Spinner(activity);review.setContentDescription("Evidence review state");review.setAdapter(new android.widget.ArrayAdapter<>(activity,android.R.layout.simple_spinner_dropdown_item,new String[]{"No evidence recorded","Received","Verified"}));review.setSelection(item.isNull("evidence_state")?0:"verified".equals(item.optString("evidence_state"))?2:1);card.addView(review);
        Button submit=ph.edu.umak.crm.app.common.components.UiButton.primary(activity,"Save milestone",null);card.addView(submit);
        card.addView(ph.edu.umak.crm.app.common.components.UiButton.outline(activity,"Cancel",UiTheme.BORDER,UiTheme.NAVY,dialog::dismiss));
        submit.setOnClickListener(view->{
            try{
                String day=date.getText().toString().trim();if(!day.isEmpty())java.time.LocalDate.parse(day);
                org.json.JSONObject fields=new org.json.JSONObject().put("milestone_code",item.getString("code")).put("status",status.getSelectedItem().toString())
                    .put("effective_date",day.isEmpty()?org.json.JSONObject.NULL:day).put("evidence",evidence.getText().toString().trim())
                    .put("evidence_state",review.getSelectedItemPosition()==0?org.json.JSONObject.NULL:review.getSelectedItemPosition()==2?"verified":"received");
                Runnable restore=ph.edu.umak.crm.app.staff.common.StaffDesignPage.freeze(card);dialog.setCancelable(false);page.status.working("Saving milestone…");
                save.save(fields,(data,error)->{
                    restore.run();dialog.setCancelable(true);if(!dialog.isShowing())return;
                    if(error==null){dialog.dismiss();saved.run();}else{
                        page.status.result(error,true);
                        if(error.toLowerCase(java.util.Locale.ROOT).contains("reload")){submit.setEnabled(false);card.addView(ph.edu.umak.crm.app.common.components.UiButton.outline(activity,"Reload research progress",UiTheme.BORDER,UiTheme.NAVY,()->{dialog.dismiss();reload.run();}));}else submit.setEnabled(true);
                    }
                });
            }catch(org.json.JSONException|java.time.DateTimeException invalid){page.status.result("Check the milestone date and evidence.",true);}
        });
        android.widget.ScrollView scroll=new android.widget.ScrollView(activity);scroll.addView(page);ph.edu.umak.crm.app.common.dialogs.BottomSheet.show(dialog,scroll);
    }

    private static int dp(Activity activity, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    public static void show(Activity activity,
                            StaffLifecycleData.MilestoneItem item, Callbacks callbacks) {
        Dialog dialog = new Dialog(activity);

        ScrollView scroll = new ScrollView(activity);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiTheme.WHITE);
        root.setPadding(dp(activity, 20), dp(activity, 12), dp(activity, 20), dp(activity, 24));
        scroll.addView(root);

        // Top drag handle bar
        View handle = new View(activity);
        GradientDrawable handleBg = new GradientDrawable();
        handleBg.setColor(0xffcbd5e1);
        handleBg.setCornerRadius(dp(activity, 2));
        handle.setBackground(handleBg);
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(
                dp(activity, 36), dp(activity, 4));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.bottomMargin = dp(activity, 16);
        root.addView(handle, handleParams);

        // Title
        TextView cardTitle = new TextView(activity);
        cardTitle.setText("Edit milestone");
        cardTitle.setTextColor(UiTheme.NAVY);
        cardTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        cardTitle.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(cardTitle);

        // Milestone name
        TextView nameLabel = new TextView(activity);
        nameLabel.setText("Milestone name");
        nameLabel.setTextColor(UiTheme.NAVY);
        nameLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        nameLabel.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams nameLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameLabelParams.topMargin = dp(activity, 14);
        root.addView(nameLabel, nameLabelParams);

        TextView nameBox = new TextView(activity);
        nameBox.setText(item.name);
        nameBox.setTextColor(UiTheme.NAVY);
        nameBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        nameBox.setPadding(dp(activity, 12), dp(activity, 10), dp(activity, 12), dp(activity, 10));
        GradientDrawable nameBg = new GradientDrawable();
        nameBg.setColor(0xfff8fafc);
        nameBg.setCornerRadius(dp(activity, 8));
        nameBg.setStroke(dp(activity, 1), UiTheme.BORDER);
        nameBox.setBackground(nameBg);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameParams.topMargin = dp(activity, 4);
        nameParams.bottomMargin = dp(activity, 12);
        root.addView(nameBox, nameParams);

        // Status Chips (Pending, In progress, Completed, Not applicable)
        TextView statusLabel = new TextView(activity);
        statusLabel.setText("Status");
        statusLabel.setTextColor(UiTheme.NAVY);
        statusLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        statusLabel.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(statusLabel);

        final String[] statusOptions = {"Pending", "In progress", "Completed", "Not applicable"};
        final String[] selectedStatus = {item.status};

        LinearLayout statusGrid = new LinearLayout(activity);
        statusGrid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gridParams.topMargin = dp(activity, 6);
        gridParams.bottomMargin = dp(activity, 12);
        root.addView(statusGrid, gridParams);

        LinearLayout statusRow = null;
        final Button[] statusBtns = new Button[statusOptions.length];
        for (int i = 0; i < statusOptions.length; i++) {
            if (i % 2 == 0) {
                statusRow = new LinearLayout(activity);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i > 0) rowParams.topMargin = dp(activity, 8);
                statusGrid.addView(statusRow, rowParams);
            }
            final String st = statusOptions[i];
            Button btn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
            btn.setMinHeight(dp(activity, 44));
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            btn.setTypeface(Typeface.DEFAULT_BOLD);
            btn.setAllCaps(false);
            btn.setPadding(dp(activity, 12), dp(activity, 6), dp(activity, 12), dp(activity, 6));

            statusBtns[i] = btn;
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            if (i % 2 != 0) btnParams.setMarginStart(dp(activity, 8));
            statusRow.addView(btn, btnParams);

            final int idx = i;
            btn.setOnClickListener(v -> {
                selectedStatus[0] = st;
                for (int j = 0; j < statusOptions.length; j++) {
                    updateStatusBtnStyle(activity, statusBtns[j], statusOptions[j], j == idx);
                }
            });

            updateStatusBtnStyle(activity, btn, st, st.equalsIgnoreCase(selectedStatus[0]));
        }

        // Date input
        TextView dateLabel = new TextView(activity);
        dateLabel.setText("Date");
        dateLabel.setTextColor(UiTheme.NAVY);
        dateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        dateLabel.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(dateLabel);

        EditText dateInput = new EditText(activity);
        dateInput.setText(item.date != null ? item.date : "2026-11-15");
        dateInput.setHint("YYYY-MM-DD");
        dateInput.setTextColor(UiTheme.INK);
        dateInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        dateInput.setPadding(dp(activity, 12), dp(activity, 10), dp(activity, 12), dp(activity, 10));
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(UiTheme.WHITE);
        inputBg.setCornerRadius(dp(activity, 8));
        inputBg.setStroke(dp(activity, 1), UiTheme.BORDER);
        dateInput.setBackground(inputBg);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = dp(activity, 4);
        dateParams.bottomMargin = dp(activity, 12);
        root.addView(dateInput, dateParams);

        // Evidence reference input
        TextView refLabel = new TextView(activity);
        refLabel.setText("Evidence reference");
        refLabel.setTextColor(UiTheme.NAVY);
        refLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        refLabel.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(refLabel);

        EditText refInput = new EditText(activity);
        refInput.setText(item.evidenceRef != null ? item.evidenceRef : "Field logbook, survey batches 1 – 3");
        refInput.setHint("Enter supporting reference");
        refInput.setTextColor(UiTheme.INK);
        refInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        refInput.setPadding(dp(activity, 12), dp(activity, 10), dp(activity, 12), dp(activity, 10));
        GradientDrawable refBg = new GradientDrawable();
        refBg.setColor(UiTheme.WHITE);
        refBg.setCornerRadius(dp(activity, 8));
        refBg.setStroke(dp(activity, 1), UiTheme.BORDER);
        refInput.setBackground(refBg);
        LinearLayout.LayoutParams refParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        refParams.topMargin = dp(activity, 4);
        refParams.bottomMargin = dp(activity, 12);
        root.addView(refInput, refParams);

        TextView helpNote = new TextView(activity);
        helpNote.setText("Milestones are tracked separately from the overall stage.");
        helpNote.setTextColor(0xff64748b);
        helpNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams helpParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        helpParams.bottomMargin = dp(activity, 16);
        root.addView(helpNote, helpParams);

        // Actions
        Button saveBtn = new Button(activity);
        saveBtn.setText("Save Milestone");
        saveBtn.setTextColor(UiTheme.WHITE);
        saveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        saveBtn.setTypeface(Typeface.DEFAULT_BOLD);
        saveBtn.setAllCaps(false);
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(UiTheme.NAVY);
        saveBg.setCornerRadius(dp(activity, 8));
        saveBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), saveBg, null));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 48));
        saveParams.bottomMargin = dp(activity, 10);
        root.addView(saveBtn, saveParams);

        Button cancelBtn = new Button(activity);
        cancelBtn.setText("Cancel");
        cancelBtn.setTextColor(UiTheme.NAVY);
        cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        cancelBtn.setTypeface(Typeface.DEFAULT_BOLD);
        cancelBtn.setAllCaps(false);
        GradientDrawable cancelBg = new GradientDrawable();
        cancelBg.setColor(UiTheme.WHITE);
        cancelBg.setCornerRadius(dp(activity, 8));
        cancelBg.setStroke(dp(activity, 1), UiTheme.BORDER);
        cancelBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22131c4b), cancelBg, null));
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 48));
        root.addView(cancelBtn, cancelParams);

        saveBtn.setOnClickListener(v -> {
            item.status = selectedStatus[0];
            item.date = dateInput.getText().toString().trim();
            item.evidenceRef = refInput.getText().toString().trim();
            item.meta = item.status + " · " + item.date;
            dialog.dismiss();
            Toast.makeText(activity, "Milestone updated.", Toast.LENGTH_SHORT).show();
            if (callbacks != null) {
                callbacks.onMilestoneSaved(item);
            }
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        BottomSheet.show(dialog, scroll);
    }

    private static void updateStatusBtnStyle(Activity activity, Button btn, String statusText, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(activity, 8));
        if (selected) {
            bg.setColor(0xffeff6ff);
            bg.setStroke(dp(activity, 2), 0xff2563eb);
            btn.setTextColor(0xff1d4ed8);
            btn.setText("✓ " + statusText);
        } else {
            bg.setColor(UiTheme.WHITE);
            bg.setStroke(dp(activity, 1), UiTheme.BORDER);
            btn.setTextColor(UiTheme.NAVY);
            btn.setText(statusText);
        }
        btn.setBackground(bg);
    }
}
