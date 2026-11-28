package ph.edu.umak.crm.app.staff.registry;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.common.util.UiTheme;

/** Shared, advisory duplicate review. No results survive an edit or a new review. */
final class DuplicateReviewDialog {
    interface Request { void run(BiConsumer<String, String> result); }

    static AlertDialog show(Context context, String title, String abstractText, Request request,
                     BooleanSupplier current, java.util.function.Consumer<String> continueAction) {
        LinearLayout content = column(context);
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.addView(content);
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("Duplicate review")
                .setView(scroll).setNegativeButton("Back to entry", null);
        if (continueAction != null) builder.setPositiveButton("Continue", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        // Initialize controls immediately after show so Continue is disabled before
        // asynchronous results arrive, without waiting for the posted OnShow event.
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (context.getResources().getDisplayMetrics().heightPixels * .85));
        load(context, dialog, content, title, abstractText, request, current, continueAction);
        return dialog;
    }

    private static void load(Context context, AlertDialog dialog, LinearLayout content,
                             String title, String abstractText, Request request,
                             BooleanSupplier current, java.util.function.Consumer<String> continueAction) {
        content.removeAllViews();
        text(content, "Checking authorized studies…", false);
        Button proceed = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (proceed != null) proceed.setEnabled(false);
        request.run((data, error) -> {
            if (!dialog.isShowing()) return;
            content.removeAllViews();
            if (!current.getAsBoolean()) {
                text(content, "The entry changed. Return to the form and review it again.", false);
                return;
            }
            try {
                if (error != null || data == null) throw new JSONException("Unavailable");
                JSONObject response = new JSONObject(data);
                JSONArray matches = response.getJSONArray("matches");
                int checked = response.getInt("scored_count");
                double threshold = response.getDouble("threshold");
                if (!"all_authorized_records".equals(response.getString("retrieval_scope"))
                        || checked < 0 || !Double.isFinite(threshold) || threshold < 0 || threshold > 1)
                    throw new JSONException("Invalid review scope");
                text(content, "Checked " + checked + " accessible studies", true);
                text(content, "Review suggested above " + percent(threshold)
                        + ". Staff decide whether records are duplicates; scores do not establish plagiarism.", false);
                if ("policy_requires_institutional_calibration".equals(response.optString("threshold_status")))
                    text(content, "Policy threshold; institutional calibration pending.", false);
                boolean alert = false;
                boolean positive = false;
                for (int i = 0; i < matches.length(); i++) {
                    JSONObject match = matches.getJSONObject(i);
                    double score = match.getDouble("tfidf_score");
                    if (!Double.isFinite(score) || score < 0 || score > 1) throw new JSONException("Invalid score");
                    alert |= match.getBoolean("alert");
                    positive |= score > 0;
                }
                if (checked == 0) text(content, "No accessible studies to compare. This does not prove originality.", false);
                else if (matches.length() == 0) throw new JSONException("Missing results");
                else if (!positive) text(content, "No shared indexed terms were found. Zero similarity does not prove originality.", false);
                else if (!alert) text(content, "No displayed match exceeds the review threshold.", false);
                text(content, "Top " + Math.min(5, matches.length()) + " results", true);
                for (int i = 0; i < Math.min(5, matches.length()); i++) {
                    JSONObject match = matches.getJSONObject(i);
                    LinearLayout card = column(context);
                    card.setBackground(UiTheme.shape(context, UiTheme.WHITE, 8, UiTheme.BORDER));
                    content.addView(card);
                    text(card, match.getString("title"), true);
                    text(card, percent(match.getDouble("tfidf_score")) + " similarity · "
                            + (match.getBoolean("alert") ? "Review suggested" : "Does not exceed threshold"), true);
                    text(card, field(match, "department") + " · " + field(match, "research_year"), false);
                    String archiveAbstract = archiveAbstract(match);
                    text(card, archiveAbstract.length() > 240 ? archiveAbstract.substring(0, 240) + "…" : archiveAbstract, false);
                    Button compare = new Button(context);
                    compare.setText("Compare");
                    compare.setOnClickListener(v -> {
                        LinearLayout comparison = column(context);
                        text(comparison, "Entered study", true);
                        text(comparison, title + "\n\n" + abstractText, false);
                        text(comparison, "Existing study", true);
                        text(comparison, match.optString("title") + "\n\n" + archiveAbstract, false);
                        ScrollView comparisonScroll = new ScrollView(context);
                        comparisonScroll.addView(comparison);
                        new AlertDialog.Builder(context).setTitle("Compare studies").setView(comparisonScroll)
                                .setPositiveButton("Back to results", null).show();
                    });
                    card.addView(compare);
                }
                android.widget.CheckBox confirmation = new android.widget.CheckBox(context);
                android.widget.EditText reason = new android.widget.EditText(context);
                if (proceed != null && alert) {
                    confirmation.setText("I confirm this is a separate record");
                    reason.setHint("Reason for creating a separate record");
                    content.addView(confirmation);
                    content.addView(reason);
                }
                final boolean needsConfirmation = alert;
                if (proceed != null) {
                    proceed.setEnabled(true);
                    proceed.setOnClickListener(v -> {
                        if (!current.getAsBoolean()) {
                            proceed.setEnabled(false);
                            text(content, "The entry changed. Return and review again.", true);
                            return;
                        }
                        if (needsConfirmation && (!confirmation.isChecked() || reason.getText().toString().trim().isEmpty())) {
                            reason.setError("Confirm a separate record and enter a reason.");
                            return;
                        }
                        dialog.dismiss();
                        continueAction.accept(needsConfirmation ? reason.getText().toString().trim() : "");
                    });
                }
            } catch (JSONException invalid) {
                content.removeAllViews();
                text(content, "Duplicate review could not be completed. Your entry is preserved.", false);
                Button retry = new Button(context);
                retry.setText("Retry");
                retry.setOnClickListener(v -> load(context, dialog, content, title, abstractText, request, current, continueAction));
                content.addView(retry);
            }
        });
    }

    private static String field(JSONObject object, String key) {
        String value = object.isNull(key) ? "" : object.optString(key).trim();
        return value.isEmpty() ? "Not recorded" : value;
    }
    private static String archiveAbstract(JSONObject match) {
        return match.optBoolean("abstract_unavailable") || match.isNull("abstract")
                || match.optString("abstract").trim().isEmpty() ? "Abstract unavailable" : match.optString("abstract");
    }
    private static String percent(double value) { return String.format(Locale.ROOT, "%.2f%%", value * 100); }
    private static LinearLayout column(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = UiTheme.dp(context, 12);
        layout.setPadding(padding, padding, padding, padding);
        return layout;
    }
    private static void text(LinearLayout parent, String value, boolean bold) {
        TextView view = new TextView(parent.getContext());
        view.setText(value);
        view.setTextColor(UiTheme.NAVY);
        view.setTextSize(14);
        view.setTypeface(bold ? UiTheme.boldFont(parent.getContext()) : UiTheme.bodyFont(parent.getContext()));
        int gap = UiTheme.dp(parent.getContext(), 6);
        view.setPadding(0, gap, 0, gap);
        parent.addView(view);
    }
}
