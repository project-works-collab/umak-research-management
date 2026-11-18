package ph.edu.umak.crm.app.staff.registry;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.common.util.UiTheme;

/**
 * Staff Research Registry View (Wireframes 7.1a, 7.1b, 7.1c).
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class ResearchRegistryView {

    public interface Callbacks {
        void onOpenRecord(StaffRegistryData.RegistryRecordItem record);
        void onAddResearchEntry();
        void onExportDirectory();
    }

    private final Activity activity;
    private final Callbacks callbacks;

    private LinearLayout rootLayout;
    private EditText searchInput;
    private Spinner collegeSpinner;
    private Spinner periodSpinner;
    private LinearLayout activeFiltersContainer;
    private TextView recordCountText;
    private LinearLayout recordsContainer;
    private LinearLayout emptyStateContainer;

    private final List<StaffRegistryData.RegistryRecordItem> allRecords = new ArrayList<>();
    private String selectedCollege = "All colleges";
    private String selectedPeriod = "All academic periods";
    private String searchQuery = "";

    private static final String[] COLLEGES = {
            "All colleges",
            "College of Computing and Information Sciences",
            "College of Engineering and Technology",
            "College of Health Sciences"
    };

    private static final String[] PERIODS = {
            "All academic periods",
            "AY 2026-2027",
            "AY 2025-2026",
            "AY 2024-2025",
            "AY 2023-2024"
    };

    public ResearchRegistryView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        this.allRecords.addAll(StaffRegistryData.getMockRecords());
        buildUi();
    }

    public ViewGroup getContentLayout() {
        if (rootLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) rootLayout.getParent()).removeView(rootLayout);
        }
        return rootLayout;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    private void buildUi() {
        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));

        // 1. Header
        TextView topHeader = new TextView(activity);
        topHeader.setText("Center for Research Management");
        topHeader.setTextColor(UiTheme.NAVY);
        topHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        topHeader.setTypeface(Typeface.DEFAULT_BOLD);
        rootLayout.addView(topHeader);

        TextView title = new TextView(activity);
        title.setText("Research Registry");
        title.setTextColor(UiTheme.NAVY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(4);
        titleParams.bottomMargin = dp(16);
        rootLayout.addView(title, titleParams);

        // 2. Search Field
        searchInput = new EditText(activity);
        searchInput.setHint("Search ID, title, researcher or adviser");
        searchInput.setHintTextColor(UiTheme.MUTED);
        searchInput.setTextColor(UiTheme.INK);
        searchInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        searchInput.setSingleLine(true);
        searchInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(150)});
        searchInput.setPadding(dp(14), dp(12), dp(14), dp(12));

        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(UiTheme.WHITE);
        searchBg.setCornerRadius(dp(8));
        searchBg.setStroke(dp(1), UiTheme.BORDER);
        searchInput.setBackground(searchBg);

        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        searchParams.bottomMargin = dp(12);
        rootLayout.addView(searchInput, searchParams);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilterAndRender();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 3. Dropdowns Row (College or institute / Academic period)
        TextView collegeLabel = new TextView(activity);
        collegeLabel.setText("College or institute");
        collegeLabel.setTextColor(UiTheme.MUTED);
        collegeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        collegeLabel.setTypeface(Typeface.DEFAULT_BOLD);
        rootLayout.addView(collegeLabel);

        collegeSpinner = createSpinner(COLLEGES);
        LinearLayout.LayoutParams collegeSpinnerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        collegeSpinnerParams.topMargin = dp(4);
        collegeSpinnerParams.bottomMargin = dp(12);
        rootLayout.addView(collegeSpinner, collegeSpinnerParams);

        TextView periodLabel = new TextView(activity);
        periodLabel.setText("Academic period");
        periodLabel.setTextColor(UiTheme.MUTED);
        periodLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        periodLabel.setTypeface(Typeface.DEFAULT_BOLD);
        rootLayout.addView(periodLabel);

        periodSpinner = createSpinner(PERIODS);
        LinearLayout.LayoutParams periodSpinnerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        periodSpinnerParams.topMargin = dp(4);
        periodSpinnerParams.bottomMargin = dp(14);
        rootLayout.addView(periodSpinner, periodSpinnerParams);

        collegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCollege = COLLEGES[position];
                applyFilterAndRender();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPeriod = PERIODS[position];
                applyFilterAndRender();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 4. Active Filters Container
        activeFiltersContainer = new LinearLayout(activity);
        activeFiltersContainer.setOrientation(LinearLayout.HORIZONTAL);
        activeFiltersContainer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams activeFilterParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        activeFilterParams.bottomMargin = dp(12);
        rootLayout.addView(activeFiltersContainer, activeFilterParams);

        // 5. Record Count Header
        recordCountText = new TextView(activity);
        recordCountText.setText("4 research records");
        recordCountText.setTextColor(UiTheme.MUTED);
        recordCountText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        recordCountText.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        countParams.bottomMargin = dp(12);
        rootLayout.addView(recordCountText, countParams);

        // 6. Records Container
        recordsContainer = new LinearLayout(activity);
        recordsContainer.setOrientation(LinearLayout.VERTICAL);
        rootLayout.addView(recordsContainer);

        // 7. Empty State Container
        emptyStateContainer = new LinearLayout(activity);
        emptyStateContainer.setOrientation(LinearLayout.VERTICAL);
        emptyStateContainer.setGravity(Gravity.CENTER);
        emptyStateContainer.setPadding(dp(24), dp(36), dp(24), dp(36));

        GradientDrawable emptyBg = new GradientDrawable();
        emptyBg.setColor(UiTheme.WHITE);
        emptyBg.setCornerRadius(dp(12));
        emptyBg.setStroke(dp(1), UiTheme.BORDER);
        emptyStateContainer.setBackground(emptyBg);

        TextView emptyTitle = new TextView(activity);
        emptyTitle.setText("No matching research records");
        emptyTitle.setTextColor(UiTheme.NAVY);
        emptyTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        emptyTitle.setTypeface(Typeface.DEFAULT_BOLD);
        emptyTitle.setGravity(Gravity.CENTER);
        emptyStateContainer.addView(emptyTitle);

        Button clearFiltersBtn = new Button(activity);
        clearFiltersBtn.setText("Clear filters");
        clearFiltersBtn.setTextColor(UiTheme.WHITE);
        clearFiltersBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        clearFiltersBtn.setTypeface(Typeface.DEFAULT_BOLD);
        clearFiltersBtn.setAllCaps(false);
        GradientDrawable clearBtnBg = new GradientDrawable();
        clearBtnBg.setColor(UiTheme.NAVY);
        clearBtnBg.setCornerRadius(dp(8));
        clearFiltersBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), clearBtnBg, null));
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
        clearParams.topMargin = dp(16);
        emptyStateContainer.addView(clearFiltersBtn, clearParams);

        clearFiltersBtn.setOnClickListener(v -> resetFilters());
        emptyStateContainer.setVisibility(View.GONE);
        rootLayout.addView(emptyStateContainer);

        // 8. Bottom Action Buttons: "Add Research Entry" & "Export Directory"
        View spacer = new View(activity);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(20));
        rootLayout.addView(spacer, spacerParams);

        Button addEntryBtn = new Button(activity);
        addEntryBtn.setText("Add Research Entry");
        addEntryBtn.setTextColor(UiTheme.WHITE);
        addEntryBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        addEntryBtn.setTypeface(Typeface.DEFAULT_BOLD);
        addEntryBtn.setAllCaps(false);
        GradientDrawable addBtnBg = new GradientDrawable();
        addBtnBg.setColor(UiTheme.NAVY);
        addBtnBg.setCornerRadius(dp(8));
        addEntryBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), addBtnBg, null));
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        addParams.bottomMargin = dp(12);
        rootLayout.addView(addEntryBtn, addParams);

        addEntryBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onAddResearchEntry();
            }
        });

        Button exportBtn = new Button(activity);
        exportBtn.setText("Export Directory");
        exportBtn.setTextColor(UiTheme.NAVY);
        exportBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        exportBtn.setTypeface(Typeface.DEFAULT_BOLD);
        exportBtn.setAllCaps(false);
        GradientDrawable exportBtnBg = new GradientDrawable();
        exportBtnBg.setColor(UiTheme.WHITE);
        exportBtnBg.setCornerRadius(dp(8));
        exportBtnBg.setStroke(dp(1), UiTheme.BORDER);
        exportBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22131c4b), exportBtnBg, null));
        LinearLayout.LayoutParams exportParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        rootLayout.addView(exportBtn, exportParams);

        exportBtn.setOnClickListener(v -> {
            Toast.makeText(activity, "Research directory exported successfully.", Toast.LENGTH_SHORT).show();
            if (callbacks != null) {
                callbacks.onExportDirectory();
            }
        });

        applyFilterAndRender();
    }

    private Spinner createSpinner(String[] items) {
        Spinner spinner = new Spinner(activity);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        GradientDrawable spinnerBg = new GradientDrawable();
        spinnerBg.setColor(UiTheme.WHITE);
        spinnerBg.setCornerRadius(dp(8));
        spinnerBg.setStroke(dp(1), UiTheme.BORDER);
        spinner.setBackground(spinnerBg);
        spinner.setPadding(dp(12), dp(10), dp(12), dp(10));
        return spinner;
    }

    public void applyFilterAndRender() {
        List<StaffRegistryData.RegistryRecordItem> filtered = new ArrayList<>();
        boolean collegeFilterActive = !"All colleges".equalsIgnoreCase(selectedCollege);
        boolean periodFilterActive = !"All academic periods".equalsIgnoreCase(selectedPeriod);
        boolean hasActiveFilter = collegeFilterActive || periodFilterActive;

        for (StaffRegistryData.RegistryRecordItem record : allRecords) {
            // Check college
            if (collegeFilterActive && !record.college.equalsIgnoreCase(selectedCollege)) {
                continue;
            }
            // Check period
            if (periodFilterActive && !record.academicPeriod.equalsIgnoreCase(selectedPeriod)) {
                continue;
            }
            // Check search query
            if (!TextUtils.isEmpty(searchQuery)) {
                String q = searchQuery.toLowerCase();
                boolean matches = record.id.toLowerCase().contains(q)
                        || record.title.toLowerCase().contains(q)
                        || record.authorsDisplay.toLowerCase().contains(q)
                        || (record.facultyAdviser != null && record.facultyAdviser.toLowerCase().contains(q));
                if (!matches) {
                    continue;
                }
            }
            filtered.add(record);
        }

        renderActiveFilterPills(hasActiveFilter);

        recordCountText.setText(filtered.size() + " research records");

        recordsContainer.removeAllViews();
        if (filtered.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            for (StaffRegistryData.RegistryRecordItem record : filtered) {
                recordsContainer.addView(createRecordCard(record));
            }
        }
    }

    private void renderActiveFilterPills(boolean hasActiveFilter) {
        activeFiltersContainer.removeAllViews();
        if (!hasActiveFilter) {
            activeFiltersContainer.setVisibility(View.GONE);
            return;
        }
        activeFiltersContainer.setVisibility(View.VISIBLE);

        HorizontalScrollView scroll = new HorizontalScrollView(activity);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout pillsRow = new LinearLayout(activity);
        pillsRow.setOrientation(LinearLayout.HORIZONTAL);
        pillsRow.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(pillsRow);
        activeFiltersContainer.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (!"All colleges".equalsIgnoreCase(selectedCollege)) {
            pillsRow.addView(createFilterPill(selectedCollege, () -> {
                collegeSpinner.setSelection(0);
            }));
        }

        if (!"All academic periods".equalsIgnoreCase(selectedPeriod)) {
            pillsRow.addView(createFilterPill(selectedPeriod, () -> {
                periodSpinner.setSelection(0);
            }));
        }

        // Clear filters action text
        TextView clearText = new TextView(activity);
        clearText.setText("Clear filters");
        clearText.setTextColor(UiTheme.ACCENT);
        clearText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        clearText.setTypeface(Typeface.DEFAULT_BOLD);
        clearText.setPadding(dp(8), dp(4), dp(8), dp(4));
        clearText.setClickable(true);
        clearText.setFocusable(true);
        clearText.setOnClickListener(v -> resetFilters());
        pillsRow.addView(clearText);
    }

    private View createFilterPill(String text, Runnable onRemove) {
        LinearLayout pill = new LinearLayout(activity);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        pill.setPadding(dp(10), dp(4), dp(10), dp(4));

        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(UiTheme.BADGE_BLUE_BG);
        pillBg.setCornerRadius(dp(16));
        pillBg.setStroke(dp(1), UiTheme.BORDER);
        pill.setBackground(pillBg);

        TextView label = new TextView(activity);
        label.setText(text);
        label.setTextColor(UiTheme.NAVY);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        pill.addView(label);

        TextView remove = new TextView(activity);
        remove.setText(" ×");
        remove.setTextColor(UiTheme.MUTED);
        remove.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        remove.setTypeface(Typeface.DEFAULT_BOLD);
        remove.setPadding(dp(4), 0, 0, 0);
        pill.addView(remove);

        pill.setOnClickListener(v -> onRemove.run());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.rightMargin = dp(8);
        pill.setLayoutParams(params);
        return pill;
    }

    private View createRecordCard(StaffRegistryData.RegistryRecordItem record) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(UiTheme.WHITE);
        cardBg.setCornerRadius(dp(10));
        cardBg.setStroke(dp(1), UiTheme.BORDER);
        card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x15131c4b), cardBg, null));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        // Top Row: ID & Stage Badge
        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView idText = new TextView(activity);
        idText.setText(record.id);
        idText.setTextColor(UiTheme.NAVY);
        idText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        idText.setTypeface(Typeface.DEFAULT_BOLD);
        topRow.addView(idText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView stageBadge = createStageBadge(record.stage);
        topRow.addView(stageBadge);
        card.addView(topRow);

        // Title
        TextView titleText = new TextView(activity);
        titleText.setText(record.title);
        titleText.setTextColor(UiTheme.NAVY);
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleText.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(6);
        titleParams.bottomMargin = dp(6);
        card.addView(titleText, titleParams);

        // Registration & AY
        TextView metaText = new TextView(activity);
        metaText.setText("Registered: " + record.registeredDate + " · " + record.academicPeriod);
        metaText.setTextColor(UiTheme.MUTED);
        metaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        card.addView(metaText);

        // Authors
        TextView authorsText = new TextView(activity);
        authorsText.setText("Authors: " + record.authorsDisplay);
        authorsText.setTextColor(UiTheme.INK);
        authorsText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams authorsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        authorsParams.topMargin = dp(4);
        authorsParams.bottomMargin = dp(4);
        card.addView(authorsText, authorsParams);

        // College
        TextView collegeText = new TextView(activity);
        collegeText.setText(record.college);
        collegeText.setTextColor(UiTheme.MUTED);
        collegeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        card.addView(collegeText);

        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onOpenRecord(record);
            }
        });

        return card;
    }

    private TextView createStageBadge(String stage) {
        TextView badge = new TextView(activity);
        badge.setText("Current stage " + stage);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));

        if ("In progress".equalsIgnoreCase(stage)) {
            bg.setColor(UiTheme.STATUS_IN_REVIEW_BG);
            badge.setTextColor(UiTheme.STATUS_IN_REVIEW_TEXT);
        } else if ("Registered".equalsIgnoreCase(stage)) {
            bg.setColor(UiTheme.STATUS_SUBMITTED_BG);
            badge.setTextColor(UiTheme.STATUS_SUBMITTED_TEXT);
        } else if ("For revision".equalsIgnoreCase(stage)) {
            bg.setColor(UiTheme.STATUS_CORRECTION_BG);
            badge.setTextColor(UiTheme.STATUS_CORRECTION_TEXT);
        } else if ("Completed".equalsIgnoreCase(stage)) {
            bg.setColor(UiTheme.STATUS_COMPLETED_BG);
            badge.setTextColor(UiTheme.STATUS_COMPLETED_TEXT);
        } else {
            bg.setColor(UiTheme.TAG_BG);
            badge.setTextColor(UiTheme.TAG_TEXT);
        }

        badge.setBackground(bg);
        return badge;
    }

    public void resetFilters() {
        searchQuery = "";
        searchInput.setText("");
        collegeSpinner.setSelection(0);
        periodSpinner.setSelection(0);
        applyFilterAndRender();
    }

    // Public preview setters for Wireframes 7.1a, 7.1b, 7.1c
    public void showDefaultState() {
        resetFilters();
    }

    public void showFiltersAppliedState() {
        searchInput.setText("");
        // Select CCIS (index 1) and AY 2026-2027 (index 1)
        collegeSpinner.setSelection(1);
        periodSpinner.setSelection(1);
    }

    public void showEmptyState() {
        searchInput.setText("");
        // Select College of Health Sciences (index 3) and AY 2023-2024 (index 4)
        collegeSpinner.setSelection(3);
        periodSpinner.setSelection(4);
    }
}
