package ph.edu.umak.crm.app.staff.queue;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ph.edu.umak.crm.app.common.components.PageTitle;
import ph.edu.umak.crm.app.common.components.PendingStatus;
import ph.edu.umak.crm.app.common.components.UiButton;
import ph.edu.umak.crm.app.common.util.UiTheme;

/** Live data coordinator for the designed Part 6 queue and request workspace. */
public final class LiveStaffRequestsView extends LinearLayout {
    public interface Actions {
        void request(String path, JSONObject body, BiConsumer<String, String> result);
        void download(String path, String filename, String mime, Runnable failure);
        void back();
    }

    private final Actions actions;
    private final ReviewQueueView queue;
    private final RequestWorkspaceView workspace;
    private final List<StaffQueueData.QueueRequestItem> rows = new ArrayList<>();
    private boolean loaded;
    private boolean loading;
    private boolean more;
    private boolean saving;
    private boolean refreshOnReturn;
    private boolean detailReady;
    private int detailId;
    private int generation;
    private int queueGeneration;
    private int version;
    private PendingStatus detailStatus;

    public LiveStaffRequestsView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        setOrientation(VERTICAL);
        queue = new ReviewQueueView(context, new ReviewQueueView.Callbacks() {
            @Override public void onOpenRequest(StaffQueueData.QueueRequestItem request) { detail(request.serverId, ""); }
            @Override public void onRefreshQueue() { loadQueue(false); }
            @Override public void onLoadMore() { loadQueue(true); }
        });
        workspace = new RequestWorkspaceView(context, new RequestWorkspaceView.Callbacks() {
            @Override public void onBackToQueue() { workspace.confirmLeave(LiveStaffRequestsView.this::returnToQueue); }
            @Override public void onSessionDraftKept() {
                returnToQueue();
                queue.showMessage("Draft kept for this session. It has not been sent to CRM.", false);
            }
        }, new RequestWorkspaceView.LiveActions() {
            @Override public void save(JSONObject body) { saveReview(body); }
            @Override public void download(StaffQueueData.AttachmentItem attachment) {
                int current = detailId;
                actions.download("/" + current + "/files/" + attachment.fileId, attachment.fileName, attachment.mime,
                        () -> { if (detailId == current) workspace.showResult("Unable to open the file. Try again.", true, false); });
            }
            @Override public void reload() { detail(detailId, ""); }
        });
    }

    public void load() {
        // The instance is retained while changing staff tabs, preserving edits and queue filters.
        if (getChildCount() == 0) showQueue();
        if (detailId == 0 && !loaded && !loading) loadQueue(false);
    }

    public void openRecord(int id){detail(id, "");}

    public boolean isSaving() { return saving; }

    public boolean handleBack() {
        if (saving) return true;
        if (detailId == 0) return false;
        if (detailReady) workspace.confirmLeave(this::returnToQueue);
        else returnToQueue();
        return true;
    }

    private void showQueue() {
        removeAllViews();
        addView(queue.getContentLayout());
    }

    private void returnToQueue() {
        generation++;
        detailId = 0;
        showQueue();
        if (refreshOnReturn) { refreshOnReturn = false; loadQueue(false); }
    }

    private void loadQueue(boolean append) {
        if (loading || saving || detailId != 0) return;
        loading = true;
        int current = ++queueGeneration;
        String path = append && !rows.isEmpty() ? "?before=" + rows.get(rows.size() - 1).serverId : "";
        queue.showLoading();
        queue.showPagination(more, rows.size(), true);
        actions.request(path, null, (data, error) -> {
            if (current != queueGeneration) return;
            loading = false;
            if (error == null) {
                try {
                    JSONArray response = new JSONArray(data);
                    List<StaffQueueData.QueueRequestItem> parsed = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) parsed.add(StaffRequestData.fromJson(response.getJSONObject(i)));
                    if (!append) rows.clear();
                    for (var row : parsed) {
                        rows.removeIf(existing -> existing.serverId == row.serverId);
                        rows.add(row);
                    }
                    more = response.length() == 100;
                    loaded = true;
                } catch (JSONException | java.time.DateTimeException invalid) {
                    error = "CRM returned invalid requests. Try refreshing again.";
                }
            }
            queue.showLiveRequests(rows, error);
            queue.showPagination(more, rows.size(), false);
        });
    }

    private void detail(int id, String savedMessage) {
        if (id <= 0 || saving) return;
        detailId = id;
        detailReady = false;
        int current = ++generation;
        removeAllViews();
        LinearLayout pending = new LinearLayout(getContext());
        pending.setOrientation(VERTICAL);
        int padding = UiTheme.dp(getContext(), 16);
        pending.setPadding(padding, padding, padding, padding);
        Button back = UiButton.outline(getContext(), "Back to queue", UiTheme.BORDER, UiTheme.NAVY, this::returnToQueue);
        pending.addView(back);
        detailStatus = new PendingStatus(getContext());
        pending.addView(detailStatus);
        addView(PageTitle.wrap(pending, "Request Workspace", "Review and process service requests.", false));
        detailStatus.working("Loading request…");
        actions.request("/" + id, null, (data, error) -> {
            if (generation != current || detailId != id) return;
            if (error == null) {
                try {
                    JSONObject response = new JSONObject(data);
                    // A detail response must include the requirement/file/history collections.
                    response.getJSONArray("requirements"); response.getJSONArray("attachments"); response.getJSONArray("reviews");
                    var record = StaffRequestData.fromJson(response);
                    if (record.serverId != id) throw new JSONException("Unexpected request");
                    version = record.version;
                    workspace.setRequest(record);
                    detailReady = true;
                    removeAllViews();
                    addView(workspace.getContentLayout());
                    if (!savedMessage.isEmpty()) workspace.showResult(savedMessage, false, false);
                    return;
                } catch (JSONException | java.time.DateTimeException invalid) {
                    error = "CRM returned invalid request details. Try again.";
                }
            }
            detailStatus.result(savedMessage.isEmpty() ? error : savedMessage + " Reload the request to see the saved review. " + error, true);
            pending.addView(UiButton.outline(getContext(), "Retry", UiTheme.BORDER, UiTheme.NAVY, () -> detail(id, savedMessage)));
        });
    }

    private void saveReview(JSONObject body) {
        if (saving || detailId == 0) return;
        saving = true;
        workspace.setSaving(true);
        int id = detailId, current = generation;
        actions.request("/" + id + "/reviews", body, (data, error) -> {
            saving = false;
            if (generation != current || detailId != id) return;
            workspace.setSaving(false);
            if (error == null) {
                try {
                    JSONObject saved = new JSONObject(data);
                    if (saved.getInt("id") != id || saved.getInt("version") <= version
                            || !saved.getString("status").equals(body.getString("status"))) throw new JSONException("Invalid review receipt");
                    workspace.discardSessionDraft();
                    refreshOnReturn = true;
                    detail(id, "Completed".equals(body.getString("status")) ? "Review saved. The request is completed." : "Review saved.");
                    return;
                } catch (JSONException invalid) {
                    error = "CRM could not confirm the saved review. Reload before saving again.";
                }
            }
            workspace.showResult(error, true, error.toLowerCase(java.util.Locale.ROOT).contains("reload"));
        });
    }
}
