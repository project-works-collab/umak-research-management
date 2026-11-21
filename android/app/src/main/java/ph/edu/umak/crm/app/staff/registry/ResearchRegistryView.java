package ph.edu.umak.crm.app.staff.registry;

import android.app.Activity;
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
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;
import ph.edu.umak.crm.app.staff.reporting.ExportReportDialog;

/**
 * Staff Research Registry View (Wireframes 7.1a, 7.1b, 7.1c).
 * Zero emojis compliant. Defensive view detachment on getContentLayout().
 */
public class ResearchRegistryView {

    public interface Callbacks {
        void onOpenRecord(StaffRegistryData.RegistryRecordItem record);
        void onAddResearchEntry();
        default void onFiltersChanged(String keyword,String college,String period) {}
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
    private List<StaffRegistryData.RegistryRecordItem> displayedRecords = List.of();
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

    private boolean live;
    private boolean binding;
    private String[] colleges = COLLEGES;
    private String[] periods = PERIODS;

    public ResearchRegistryView(Activity activity, Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        this.allRecords.addAll(StaffRegistryData.getMockRecords());
        buildUi();
    }

    public void bindLive(java.util.List<StaffRegistryData.RegistryRecordItem> records) {
        binding=true;live=true;allRecords.clear();allRecords.addAll(records);
        java.util.Set<String> cs=new java.util.TreeSet<>(),ps=new java.util.TreeSet<>();
        for(var record:records){if(!record.college.isBlank())cs.add(record.college);if(!record.academicPeriod.isBlank())ps.add(record.academicPeriod);}
        if(!selectedCollege.equals("All colleges"))cs.add(selectedCollege);
        if(!selectedPeriod.equals("All academic periods"))ps.add(selectedPeriod);
        colleges=java.util.stream.Stream.concat(java.util.stream.Stream.of("All colleges"),cs.stream()).toArray(String[]::new);
        periods=java.util.stream.Stream.concat(java.util.stream.Stream.of("All academic periods"),ps.stream()).toArray(String[]::new);
        String currentCollege=selectedCollege,currentPeriod=selectedPeriod;
        collegeSpinner.setAdapter(new ArrayAdapter<>(activity,android.R.layout.simple_spinner_dropdown_item,colleges));
        periodSpinner.setAdapter(new ArrayAdapter<>(activity,android.R.layout.simple_spinner_dropdown_item,periods));
        collegeSpinner.setSelection(Math.max(0,java.util.Arrays.asList(colleges).indexOf(currentCollege)));
        periodSpinner.setSelection(Math.max(0,java.util.Arrays.asList(periods).indexOf(currentPeriod)));
        applyFilterAndRender();binding=false;
    }

    public ViewGroup getContentLayout() {
        if (pageWrapper != null && pageWrapper.getParent() instanceof ViewGroup) {
            ((ViewGroup) pageWrapper.getParent()).removeView(pageWrapper);
        }
        return pageWrapper;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                activity.getResources().getDisplayMetrics()
        );
    }

    private LinearLayout pageWrapper;

    private void buildUi() {
        pageWrapper = new LinearLayout(activity);
        pageWrapper.setOrientation(LinearLayout.VERTICAL);

        // Dark Navy Header Band (#082d50)
        LinearLayout band = new LinearLayout(activity);
        band.setOrientation(LinearLayout.VERTICAL);
        band.setBackgroundColor(0xff082d50);
        int pad = dp(16);
        band.setPadding(pad, pad, pad, pad);

        TextView heading = new TextView(activity);
        heading.setText("Research Registry");
        heading.setTextColor(0xffffffff);
        heading.setTypeface(UiTheme.headingFont(activity));
        ScreenTypography.apply(heading, 24);
        ScreenTypography.heading(heading);
        band.addView(heading);

        pageWrapper.addView(band);

        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(UiTheme.PAPER);
        rootLayout.setPadding(dp(16), dp(16), dp(16), dp(32));
        pageWrapper.addView(rootLayout);

        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        // 2. Search & Filter Bar Row (Single rounded box with search icon and filter icon)
        FrameLayout searchContainer = new FrameLayout(activity);
        searchContainer.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER));
        LinearLayout.LayoutParams searchBoxParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        searchBoxParams.bottomMargin = dp(14);
        searchContainer.setLayoutParams(searchBoxParams);

        ImageView searchIcon = new ImageView(activity);
        searchIcon.setImageResource(R.drawable.ic_researcher_search);
        searchIcon.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams sIconLp = new FrameLayout.LayoutParams(dp(18), dp(18));
        sIconLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        sIconLp.leftMargin = dp(12);
        searchContainer.addView(searchIcon, sIconLp);

        searchInput = new EditText(activity);
        searchInput.setHint("Search ID, title, researcher or ad...");
        searchInput.setHintTextColor(0xff94a3b8);
        searchInput.setTextColor(0xff082d50);
        searchInput.setTypeface(regular);
        searchInput.setBackground(null);
        ScreenTypography.apply(searchInput, 13);
        searchInput.setPadding(dp(38), 0, dp(40), 0);
        searchInput.setSingleLine(true);
        searchInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(150)});
        FrameLayout.LayoutParams sInputLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        searchContainer.addView(searchInput, sInputLp);

        ImageView filterIcon = new ImageView(activity);
        filterIcon.setImageResource(R.drawable.ic_crm_filter);
        filterIcon.setColorFilter(0xff64748b);
        FrameLayout.LayoutParams fIconLp = new FrameLayout.LayoutParams(dp(18), dp(18));
        fIconLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        fIconLp.rightMargin = dp(12);
        searchContainer.addView(filterIcon, fIconLp);

        rootLayout.addView(searchContainer);

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

        // 3. Dropdowns (College or institute / Academic period)
        TextView collegeLabel = new TextView(activity);
        collegeLabel.setText("College or institute");
        collegeLabel.setTextColor(0xff586b83);
        collegeLabel.setTypeface(bold);
        ScreenTypography.apply(collegeLabel, 12);
        rootLayout.addView(collegeLabel);

        collegeSpinner = createSpinner(colleges);
        LinearLayout.LayoutParams collegeSpinnerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        collegeSpinnerParams.topMargin = dp(4);
        collegeSpinnerParams.bottomMargin = dp(12);
        rootLayout.addView(collegeSpinner, collegeSpinnerParams);

        TextView periodLabel = new TextView(activity);
        periodLabel.setText("Academic period");
        periodLabel.setTextColor(0xff586b83);
        periodLabel.setTypeface(bold);
        ScreenTypography.apply(periodLabel, 12);
        rootLayout.addView(periodLabel);

        periodSpinner = createSpinner(periods);
        LinearLayout.LayoutParams periodSpinnerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        periodSpinnerParams.topMargin = dp(4);
        periodSpinnerParams.bottomMargin = dp(14);
        rootLayout.addView(periodSpinner, periodSpinnerParams);

        collegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCollege = colleges[position];
                applyFilterAndRender();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPeriod = periods[position];
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
        recordCountText.setTextColor(0xff64748b);
        recordCountText.setTypeface(regular);
        ScreenTypography.apply(recordCountText, 13);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        countParams.bottomMargin = dp(12);
        rootLayout.addView(recordCountText, countParams);

        // 6. Records Container
        recordsContainer = new LinearLayout(activity);
        recordsContainer.setOrientation(LinearLayout.VERTICAL);
        rootLayout.addView(recordsContainer);

        // 7. Empty State Container (Wireframe 7.1c)
        emptyStateContainer = new LinearLayout(activity);
        emptyStateContainer.setOrientation(LinearLayout.VERTICAL);
        emptyStateContainer.setGravity(Gravity.CENTER);
        emptyStateContainer.setPadding(dp(24), dp(36), dp(24), dp(36));
        emptyStateContainer.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 12, UiTheme.BORDER));

        // Circular search icon badge
        TextView iconCircle = new TextView(activity);
        iconCircle.setGravity(Gravity.CENTER);
        int cSize = dp(48);
        LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(cSize, cSize);
        cParams.bottomMargin = dp(14);
        iconCircle.setLayoutParams(cParams);
        GradientDrawable cBg = new GradientDrawable();
        cBg.setShape(GradientDrawable.OVAL);
        cBg.setColor(Color.parseColor("#F1F5F9"));
        iconCircle.setBackground(cBg);
        iconCircle.setText("⚲");
        iconCircle.setTextColor(0xff64748b);
        iconCircle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        emptyStateContainer.addView(iconCircle);

        TextView emptyTitle = new TextView(activity);
        emptyTitle.setText("No matching research records");
        emptyTitle.setTextColor(0xff082d50);
        emptyTitle.setTypeface(bold);
        ScreenTypography.apply(emptyTitle, 16);
        emptyTitle.setGravity(Gravity.CENTER);
        emptyStateContainer.addView(emptyTitle);

        Button clearFiltersBtn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        clearFiltersBtn.setText("Clear filters");
        clearFiltersBtn.setAllCaps(false);
        clearFiltersBtn.setTextColor(0xff082d50);
        clearFiltersBtn.setTypeface(bold);
        ScreenTypography.apply(clearFiltersBtn, 14);
        clearFiltersBtn.setBackground(UiTheme.ripple(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER), 0x20082d50));
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(
                dp(160), dp(40));
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

        Button addEntryBtn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        addEntryBtn.setText("Add Research Entry");
        addEntryBtn.setAllCaps(false);
        addEntryBtn.setTextColor(0xffffffff);
        addEntryBtn.setTypeface(bold);
        ScreenTypography.apply(addEntryBtn, 15);
        addEntryBtn.setBackground(UiTheme.ripple(UiTheme.shape(activity, 0xff131c4b, 8, 0), 0x20ffffff));
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        addParams.bottomMargin = dp(10);
        rootLayout.addView(addEntryBtn, addParams);

        addEntryBtn.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onAddResearchEntry();
            }
        });

        Button exportBtn = new Button(activity, null, android.R.attr.borderlessButtonStyle);
        exportBtn.setText("Export Directory");
        exportBtn.setAllCaps(false);
        exportBtn.setTextColor(0xff082d50);
        exportBtn.setTypeface(bold);
        ScreenTypography.apply(exportBtn, 15);
        exportBtn.setBackground(UiTheme.ripple(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER), 0x20082d50));
        LinearLayout.LayoutParams exportParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        rootLayout.addView(exportBtn, exportParams);

        exportBtn.setOnClickListener(v -> ExportReportDialog.showDirectory(activity, displayedRecords,
                selectedCollege, selectedPeriod));

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
        if(live&&!binding&&callbacks!=null)callbacks.onFiltersChanged(searchQuery,"All colleges".equals(selectedCollege)?"":selectedCollege,"All academic periods".equals(selectedPeriod)?"":selectedPeriod);
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
            if (!live && !TextUtils.isEmpty(searchQuery)) {
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

        displayedRecords = filtered;
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
            pillsRow.addView(createFilterPill(selectedCollege, () -> collegeSpinner.setSelection(0)));
        }

        if (!"All academic periods".equalsIgnoreCase(selectedPeriod)) {
            pillsRow.addView(createFilterPill(selectedPeriod, () -> periodSpinner.setSelection(0)));
        }

        // Clear filters action text
        TextView clearText = new TextView(activity);
        clearText.setText("Clear filters");
        clearText.setTextColor(0xff185d9d);
        clearText.setTypeface(UiTheme.boldFont(activity));
        ScreenTypography.apply(clearText, 13);
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
        pillBg.setColor(Color.parseColor("#E0F2FE"));
        pillBg.setCornerRadius(dp(16));
        pillBg.setStroke(dp(1), Color.parseColor("#BAE6FD"));
        pill.setBackground(pillBg);

        TextView label = new TextView(activity);
        label.setText(text);
        label.setTextColor(0xff0284c7);
        ScreenTypography.apply(label, 12);
        label.setTypeface(UiTheme.boldFont(activity));
        pill.addView(label);

        TextView remove = new TextView(activity);
        remove.setText(" ✕");
        remove.setTextColor(0xff0284c7);
        ScreenTypography.apply(remove, 12);
        remove.setTypeface(UiTheme.boldFont(activity));
        remove.setPadding(dp(2), 0, 0, 0);
        pill.addView(remove);

        pill.setOnClickListener(v -> onRemove.run());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.rightMargin = dp(8);
        pill.setLayoutParams(params);
        return pill;
    }

    private View createRecordCard(StaffRegistryData.RegistryRecordItem record) {
        Typeface bold = UiTheme.boldFont(activity);
        Typeface regular = UiTheme.bodyFont(activity);

        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(UiTheme.shape(activity, UiTheme.WHITE, 8, UiTheme.BORDER));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        // Top Row: ID & Right Chevron
        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView idText = new TextView(activity);
        idText.setText(record.id);
        idText.setTextColor(0xff185d9d);
        idText.setTypeface(bold);
        ScreenTypography.apply(idText, 13);
        topRow.addView(idText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView chevron = new TextView(activity);
        chevron.setText("›");
        chevron.setTextColor(0xff94a3b8);
        chevron.setTypeface(bold);
        ScreenTypography.apply(chevron, 16);
        topRow.addView(chevron);

        card.addView(topRow);

        // Title
        TextView titleText = new TextView(activity);
        titleText.setText(record.title);
        titleText.setTextColor(0xff082d50);
        titleText.setTypeface(bold);
        ScreenTypography.apply(titleText, 15);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(4);
        titleParams.bottomMargin = dp(4);
        card.addView(titleText, titleParams);

        // Stage Row: Current stage [Badge]
        LinearLayout stageRow = new LinearLayout(activity);
        stageRow.setOrientation(LinearLayout.HORIZONTAL);
        stageRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView csLabel = new TextView(activity);
        csLabel.setText("Current stage ");
        csLabel.setTextColor(0xff586b83);
        csLabel.setTypeface(regular);
        ScreenTypography.apply(csLabel, 12);
        stageRow.addView(csLabel);

        TextView stageBadge = createStageBadge(record.stage);
        stageRow.addView(stageBadge);

        LinearLayout.LayoutParams stageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stageParams.bottomMargin = dp(6);
        card.addView(stageRow, stageParams);

        // Registered & AY
        TextView metaText = new TextView(activity);
        metaText.setText("Registered: " + record.registeredDate + " · " + record.academicPeriod);
        metaText.setTextColor(0xff64748b);
        metaText.setTypeface(regular);
        ScreenTypography.apply(metaText, 12);
        card.addView(metaText);

        // Authors
        TextView authorsText = new TextView(activity);
        authorsText.setText("Authors: " + record.authorsDisplay);
        authorsText.setTextColor(0xff64748b);
        authorsText.setTypeface(regular);
        ScreenTypography.apply(authorsText, 12);
        LinearLayout.LayoutParams authorsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        authorsParams.topMargin = dp(2);
        authorsParams.bottomMargin = dp(2);
        card.addView(authorsText, authorsParams);

        // College
        TextView collegeText = new TextView(activity);
        collegeText.setText(record.college);
        collegeText.setTextColor(0xff64748b);
        collegeText.setTypeface(regular);
        ScreenTypography.apply(collegeText, 12);
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
        Typeface bold = UiTheme.boldFont(activity);
        TextView badge = new TextView(activity);
        badge.setText(stage);
        ScreenTypography.apply(badge, 11);
        badge.setTypeface(bold);
        badge.setPadding(dp(8), dp(2), dp(8), dp(2));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(4));

        if ("In progress".equalsIgnoreCase(stage)) {
            bg.setColor(Color.parseColor("#FEF3C7"));
            badge.setTextColor(Color.parseColor("#D97706"));
        } else if ("Registered".equalsIgnoreCase(stage)) {
            bg.setColor(Color.parseColor("#E0F2FE"));
            badge.setTextColor(Color.parseColor("#0284c7"));
        } else if ("For revision".equalsIgnoreCase(stage)) {
            bg.setColor(Color.parseColor("#FEF3C7"));
            badge.setTextColor(Color.parseColor("#D97706"));
        } else if ("Completed".equalsIgnoreCase(stage)) {
            bg.setColor(Color.parseColor("#DCFCE7"));
            badge.setTextColor(Color.parseColor("#16A34A"));
        } else {
            bg.setColor(Color.parseColor("#F1F5F9"));
            badge.setTextColor(Color.parseColor("#64748B"));
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
        collegeSpinner.setSelection(1);
        periodSpinner.setSelection(1);
    }

    public void showEmptyState() {
        searchInput.setText("");
        collegeSpinner.setSelection(3);
        periodSpinner.setSelection(4);
    }
}
