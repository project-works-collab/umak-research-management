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
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.dialogs.BottomSheet;
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
    private LinearLayout chipsContainer;
    private HorizontalScrollView chipsScroll;
    private LinearLayout listContainer;
    private LinearLayout emptyContainer;
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

        // Title
        TextView title = new TextView(context);
        title.setText("Add Co-Author");
        title.setTextColor(0xff082d50);
        title.setTypeface(UiTheme.headingFont(context));
        ScreenTypography.apply(title, 20);
        layout.addView(title);

        addSpacing(context, layout, 12);

        // Search Input Container
        LinearLayout searchBox = new LinearLayout(context);
        searchBox.setOrientation(LinearLayout.HORIZONTAL);
        searchBox.setGravity(Gravity.CENTER_VERTICAL);
        searchBox.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
        searchBox.setMinimumHeight(UiTheme.dp(context, 46));
        int sPad = UiTheme.dp(context, 10);
        searchBox.setPadding(sPad, 0, sPad, 0);

        ImageView searchIcon = new ImageView(context);
        searchIcon.setImageResource(R.drawable.ic_researcher_search);
        searchIcon.setColorFilter(0xff64748b);
        int iconSize = UiTheme.dp(context, 18);
        searchBox.addView(searchIcon, new LinearLayout.LayoutParams(iconSize, iconSize));

        EditText searchInput = new EditText(context);
        searchInput.setHint("Search researcher name or email");
        searchInput.setHintTextColor(0xff8898aa);
        searchInput.setTextColor(0xff082d50);
        searchInput.setTypeface(regular);
        ScreenTypography.apply(searchInput, 14);
        searchInput.setBackground(null);
        searchInput.setPadding(UiTheme.dp(context, 8), sPad, UiTheme.dp(context, 8), sPad);
        searchInput.setSingleLine(true);
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        searchBox.addView(searchInput, inputLp);

        layout.addView(searchBox);

        addSpacing(context, layout, 10);

        // Selected Chips Row
        chipsScroll = new HorizontalScrollView(context);
        chipsScroll.setHorizontalScrollBarEnabled(false);
        chipsContainer = new LinearLayout(context);
        chipsContainer.setOrientation(LinearLayout.HORIZONTAL);
        chipsContainer.setGravity(Gravity.CENTER_VERTICAL);
        chipsScroll.addView(chipsContainer);
        layout.addView(chipsScroll);

        addSpacing(context, layout, 10);

        // Candidate results list
        listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(listContainer);

        // Inline Error Message: "⚠ This researcher is already a team member."
        errorBanner = new TextView(context);
        errorBanner.setText("⚠ This researcher is already a team member.");
        errorBanner.setTextColor(0xffc2410c);
        errorBanner.setTypeface(bold);
        ScreenTypography.apply(errorBanner, 13);
        errorBanner.setPadding(0, UiTheme.dp(context, 8), 0, 0);
        errorBanner.setVisibility(View.GONE);
        layout.addView(errorBanner);

        // Empty Search Container
        emptyContainer = new LinearLayout(context);
        emptyContainer.setOrientation(LinearLayout.VERTICAL);
        emptyContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyContainer.setPadding(0, UiTheme.dp(context, 24), 0, UiTheme.dp(context, 24));

        FrameLayout iconCircle = new FrameLayout(context);
        int cSize = UiTheme.dp(context, 44);
        iconCircle.setBackground(UiTheme.shape(context, 0xffe0f2fe, cSize / 2, 0));

        ImageView emptyIcon = new ImageView(context);
        emptyIcon.setImageResource(R.drawable.ic_researcher_search);
        emptyIcon.setColorFilter(0xff185d9d);
        int eIconSize = UiTheme.dp(context, 22);
        FrameLayout.LayoutParams eIconParams = new FrameLayout.LayoutParams(eIconSize, eIconSize, Gravity.CENTER);
        iconCircle.addView(emptyIcon, eIconParams);

        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(cSize, cSize);
        circleParams.gravity = Gravity.CENTER_HORIZONTAL;
        emptyContainer.addView(iconCircle, circleParams);

        addSpacing(context, emptyContainer, 10);

        TextView emptyText = new TextView(context);
        emptyText.setText("No matching researchers.");
        emptyText.setTextColor(0xff082d50);
        emptyText.setTypeface(bold);
        ScreenTypography.apply(emptyText, 15);
        emptyText.setGravity(Gravity.CENTER);
        emptyContainer.addView(emptyText);

        emptyContainer.setVisibility(View.GONE);
        layout.addView(emptyContainer);

        addSpacing(context, layout, 16);

        // Done Button (Dark Navy)
        Button doneBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
        doneBtn.setText("Done");
        doneBtn.setAllCaps(false);
        doneBtn.setTypeface(bold);
        doneBtn.setTextColor(0xffffffff);
        ScreenTypography.apply(doneBtn, 15);
        doneBtn.setBackground(UiTheme.ripple(UiTheme.shape(context, 0xff131c4b, 8, 0), 0x20ffffff));
        doneBtn.setMinHeight(UiTheme.dp(context, 46));
        doneBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onTeamUpdated(currentTeam);
                listener.onDismiss();
            }
        });
        layout.addView(doneBtn);

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

        renderChips();
        renderResults();

        BottomSheet.show(dialog, layout);
    }

    private void renderChips() {
        chipsContainer.removeAllViews();
        if (currentTeam.isEmpty()) {
            chipsScroll.setVisibility(View.GONE);
            return;
        }
        chipsScroll.setVisibility(View.VISIBLE);

        for (final ResearchData.CoAuthorItem member : currentTeam) {
            LinearLayout chip = new LinearLayout(context);
            chip.setOrientation(LinearLayout.HORIZONTAL);
            chip.setGravity(Gravity.CENTER_VERTICAL);
            chip.setBackground(UiTheme.shape(context, 0xffe0f2fe, 16, 0));
            int px = UiTheme.dp(context, 10);
            int py = UiTheme.dp(context, 4);
            chip.setPadding(px, py, px, py);

            TextView name = new TextView(context);
            name.setText(member.name);
            name.setTextColor(0xff185d9d);
            name.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(name, 12);
            chip.addView(name);

            TextView removeX = new TextView(context);
            removeX.setText("  ×");
            removeX.setTextColor(0xff185d9d);
            removeX.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(removeX, 14);
            removeX.setOnClickListener(v -> {
                currentTeam.remove(member);
                teamMemberNames.remove(member.name.toLowerCase());
                errorBanner.setVisibility(View.GONE);
                renderChips();
                renderResults();
            });
            chip.addView(removeX);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = UiTheme.dp(context, 6);
            chipsContainer.addView(chip, lp);
        }
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
            emptyContainer.setVisibility(View.VISIBLE);
            listContainer.setVisibility(View.GONE);
        } else {
            emptyContainer.setVisibility(View.GONE);
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
        row.setBackground(UiTheme.shape(context, 0xffffffff, 8, UiTheme.BORDER));
        int pad = UiTheme.dp(context, 10);
        row.setPadding(pad, pad, pad, pad);

        // Initials avatar
        TextView avatar = new TextView(context);
        avatar.setText(getInitials(candidate.name));
        avatar.setTextColor(0xff185d9d);
        avatar.setTypeface(UiTheme.boldFont(context));
        avatar.setGravity(Gravity.CENTER);
        ScreenTypography.apply(avatar, 13);
        int aSize = UiTheme.dp(context, 38);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(aSize, aSize);
        aLp.rightMargin = UiTheme.dp(context, 10);
        avatar.setLayoutParams(aLp);
        avatar.setBackground(UiTheme.shape(context, 0xffe0f2fe, aSize / 2, 0));
        row.addView(avatar);

        LinearLayout infoCol = new LinearLayout(context);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView name = new TextView(context);
        name.setText(candidate.name);
        name.setTextColor(0xff082d50);
        name.setTypeface(UiTheme.boldFont(context));
        ScreenTypography.apply(name, 14);
        infoCol.addView(name);

        TextView meta = new TextView(context);
        meta.setText(candidate.roleAndCollege);
        meta.setTextColor(0xff586b83);
        meta.setTypeface(UiTheme.bodyFont(context));
        ScreenTypography.apply(meta, 12);
        infoCol.addView(meta);

        row.addView(infoCol, infoLp);

        boolean isAlreadyAdded = teamMemberNames.contains(candidate.name.toLowerCase());
        if (isAlreadyAdded) {
            TextView addedTag = new TextView(context);
            addedTag.setText("✓ Added");
            addedTag.setTextColor(0xff185d9d);
            addedTag.setTypeface(UiTheme.boldFont(context));
            ScreenTypography.apply(addedTag, 13);
            int px = UiTheme.dp(context, 8);
            int py = UiTheme.dp(context, 4);
            addedTag.setPadding(px, py, px, py);
            addedTag.setOnClickListener(v -> {
                errorBanner.setText("⚠ This researcher is already a team member.");
                errorBanner.setVisibility(View.VISIBLE);
            });
            row.addView(addedTag);
        } else {
            Button addBtn = new Button(context, null, android.R.attr.borderlessButtonStyle);
            addBtn.setText("Add");
            addBtn.setAllCaps(false);
            addBtn.setTypeface(UiTheme.boldFont(context));
            addBtn.setTextColor(0xff082d50);
            ScreenTypography.apply(addBtn, 13);
            addBtn.setBackground(UiTheme.shape(context, 0xfff1f5f9, 6, 0xffcbd5e1));
            addBtn.setMinHeight(UiTheme.dp(context, 32));
            int px = UiTheme.dp(context, 14);
            addBtn.setPadding(px, 0, px, 0);
            addBtn.setOnClickListener(v -> {
                if (teamMemberNames.contains(candidate.name.toLowerCase())) {
                    errorBanner.setText("⚠ This researcher is already a team member.");
                    errorBanner.setVisibility(View.VISIBLE);
                } else {
                    currentTeam.add(candidate);
                    teamMemberNames.add(candidate.name.toLowerCase());
                    errorBanner.setVisibility(View.GONE);
                    renderChips();
                    renderResults();
                }
            });
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
