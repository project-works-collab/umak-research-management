package ph.edu.umak.crm.app.research;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ph.edu.umak.crm.app.common.dialogs.BottomSheet;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class AddCoAuthorDialog {

    public interface Listener {
        void onTeamUpdated(List<ResearchData.CoAuthorItem> currentTeam);
        void onDismiss();
    }

    private final Context context;
    private final Listener listener;
    private final List<ResearchData.CoAuthorItem> currentTeam = new ArrayList<>();
    private final Set<String> teamMemberNames = new HashSet<>();

    private final List<ResearchData.CoAuthorItem> allCandidates = new ArrayList<>();
    private String currentQuery = "";
    private LinearLayout listContainer;
    private TextView emptyText;
    private TextView errorBanner;

    public AddCoAuthorDialog(Context context, List<ResearchData.CoAuthorItem> initialTeam, Listener listener) {
        this.context = context;
        this.listener = listener;
        if (initialTeam != null) {
            this.currentTeam.addAll(initialTeam);
            for (ResearchData.CoAuthorItem item : initialTeam) {
                teamMemberNames.add(item.name.toLowerCase());
            }
        }
        this.allCandidates.addAll(ResearchData.getAvailableResearchers());
    }

    public void show() {
        final Dialog dialog = new Dialog(context);

        Typeface regular = UiTheme.bodyFont(context);
        Typeface bold = UiTheme.boldFont(context);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = UiTheme.dp(context, 20);
        layout.setPadding(pad, pad, pad, pad);

        // Sheet title
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleCol = new LinearLayout(context);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView title = new TextView(context);
        title.setText("Add Co-Author");
        title.setTextColor(UiTheme.NAVY);
        title.setTypeface(bold);
        ScreenTypography.apply(title, 18);
        titleCol.addView(title);

        header.addView(titleCol, tLp);

        Button doneBtn = UiButton.primary(context, "Done", () -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onTeamUpdated(currentTeam);
                listener.onDismiss();
            }
        });
        doneBtn.setPadding(UiTheme.dp(context, 16), 0, UiTheme.dp(context, 16), 0);

        layout.addView(header);

        addSpacing(context, layout, 14);

        // Error Banner
        errorBanner = new TextView(context);
        errorBanner.setText("This researcher is already a team member.");
        errorBanner.setTextColor(UiTheme.PEACH_TEXT);
        errorBanner.setTypeface(bold);
        ScreenTypography.apply(errorBanner, 12);
        errorBanner.setBackground(UiTheme.shape(context, UiTheme.PEACH_BG, 6, UiTheme.PEACH_BORDER));
        int bPad = UiTheme.dp(context, 8);
        errorBanner.setPadding(bPad, bPad, bPad, bPad);
        errorBanner.setVisibility(View.GONE);
        layout.addView(errorBanner);

        addSpacing(context, layout, 6);

        // Search Input
        EditText searchInput = new EditText(context);
        searchInput.setHint("Search researcher name or email");
        searchInput.setHintTextColor(UiTheme.MUTED);
        searchInput.setTextColor(UiTheme.NAVY);
        searchInput.setTypeface(regular);
        ScreenTypography.apply(searchInput, 14);
        searchInput.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        int sPad = UiTheme.dp(context, 10);
        searchInput.setPadding(sPad, sPad, sPad, sPad);
        searchInput.setSingleLine(true);
        layout.addView(searchInput);

        addSpacing(context, layout, 12);

        // Candidate results
        listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(listContainer);

        // Empty Search text
        emptyText = new TextView(context);
        emptyText.setText("No matching researchers.");
        emptyText.setTextColor(UiTheme.MUTED);
        emptyText.setTypeface(regular);
        ScreenTypography.apply(emptyText, 14);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(0, UiTheme.dp(context, 24), 0, UiTheme.dp(context, 24));
        emptyText.setVisibility(View.GONE);
        layout.addView(emptyText);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s != null ? s.toString().trim() : "";
                errorBanner.setVisibility(View.GONE);
                renderResults();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        renderResults();

        addSpacing(context, layout, 12);
        layout.addView(doneBtn);
        BottomSheet.show(dialog, layout);
    }

    private void renderResults() {
        listContainer.removeAllViews();
        List<ResearchData.CoAuthorItem> filtered = new ArrayList<>();
        for (ResearchData.CoAuthorItem c : allCandidates) {
            if (currentQuery.isEmpty() || c.name.toLowerCase().contains(currentQuery.toLowerCase())
                    || c.email.toLowerCase().contains(currentQuery.toLowerCase())) {
                filtered.add(c);
            }
        }

        if (filtered.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            listContainer.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            listContainer.setVisibility(View.VISIBLE);

            for (ResearchData.CoAuthorItem candidate : filtered) {
                View row = createCandidateRow(candidate);
                listContainer.addView(row);
                addSpacing(context, listContainer, 8);
            }
        }
    }

    private View createCandidateRow(ResearchData.CoAuthorItem candidate) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(UiTheme.shape(context, 0xFFF8FAFC, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 10);
        row.setPadding(pad, pad, pad, pad);

        // Initials avatar
        TextView avatar = new TextView(context);
        avatar.setText(getInitials(candidate.name));
        avatar.setTextColor(UiTheme.NAVY);
        avatar.setTypeface(UiTheme.boldFont(context));
        avatar.setGravity(Gravity.CENTER);
        ScreenTypography.apply(avatar, 13);
        int aSize = UiTheme.dp(context, 36);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(aSize, aSize);
        aLp.rightMargin = UiTheme.dp(context, 10);
        avatar.setLayoutParams(aLp);
        avatar.setBackground(UiTheme.shape(context, UiTheme.TAG_BG, 18, 0));
        row.addView(avatar);

        LinearLayout infoCol = new LinearLayout(context);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView name = new TextView(context);
        name.setText(candidate.name);
        name.setTextColor(UiTheme.NAVY);
        name.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(name, 14);
        infoCol.addView(name);

        TextView meta = new TextView(context);
        meta.setText(candidate.roleAndCollege);
        meta.setTextColor(UiTheme.MUTED);
        meta.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(meta, 12);
        infoCol.addView(meta);

        row.addView(infoCol, infoLp);

        boolean isAlreadyAdded = teamMemberNames.contains(candidate.name.toLowerCase());
        if (isAlreadyAdded) {
            TextView addedTag = new TextView(context);
            addedTag.setText("Added");
            addedTag.setTextColor(UiTheme.MUTED);
            addedTag.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(addedTag, 12);
            addedTag.setBackground(UiTheme.shape(context, UiTheme.TAG_BG, 4, 0));
            int px = UiTheme.dp(context, 10);
            int py = UiTheme.dp(context, 4);
            addedTag.setPadding(px, py, px, py);
            addedTag.setOnClickListener(v -> {
                errorBanner.setText("This researcher is already a team member.");
                errorBanner.setVisibility(View.VISIBLE);
            });
            row.addView(addedTag);
        } else {
            Button addBtn = UiButton.outline(context, "Add", UiTheme.ACCENT, UiTheme.ACCENT, () -> {
                if (teamMemberNames.contains(candidate.name.toLowerCase())) {
                    errorBanner.setText("This researcher is already a team member.");
                    errorBanner.setVisibility(View.VISIBLE);
                } else {
                    currentTeam.add(candidate);
                    teamMemberNames.add(candidate.name.toLowerCase());
                    errorBanner.setVisibility(View.GONE);
                    renderResults();
                }
            });
            addBtn.setPadding(UiTheme.dp(context, 12), 0, UiTheme.dp(context, 12), 0);
            row.addView(addBtn);
        }

        return row;
    }

    private static String getInitials(String name) {
        if (name == null || name.isEmpty()) return "R";
        String[] parts = name.split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private static void addSpacing(Context context, LinearLayout parent, int dp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiTheme.dp(context, dp)));
        parent.addView(space);
    }
}
