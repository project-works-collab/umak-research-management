package ph.edu.umak.crm.app;

import static org.junit.Assert.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.json.JSONObject;
import org.junit.Test;
import ph.edu.umak.crm.app.requests.LiveRequestForm;
import ph.edu.umak.crm.app.services.ServiceData;
import ph.edu.umak.crm.app.staff.content.LiveServiceCatalogView;

public class AcceptanceFeedbackTest {
    @Test public void invalidRequestHighlightsFieldsAndRetainsValuesAndToken() {
        try (var scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                AtomicInteger calls = new AtomicInteger();
                String[] token = {null};
                var service = new ServiceData.ServiceItem(1,"TEST","Test",new String[0],"","",new String[0],
                        "Submit in app","","",new String[0],"","",new String[0],new ServiceData.DocumentItem[0],"","");
                var form = new LiveRequestForm(activity, service, new LiveRequestForm.Actions() {
                    public void request(String path, JSONObject body, BiConsumer<String,String> result) {
                        calls.incrementAndGet();
                        if (token[0] == null) token[0] = body.optString("submission_token");
                        else assertEquals(token[0], body.optString("submission_token"));
                        result.accept("{\"description\":\"Check this field.\"}","Invalid request");
                    }
                    public void choose(Runnable checking, BiConsumer<MainActivity.SelectedAttachment,String> result) { fail(); }
                    public void upload(MainActivity.SelectedAttachment file,int requirement,String attempt,BiConsumer<String,String> result) { fail(); }
                }, saved -> fail("No receipt expected"));
                activity.setContentView(form.view());
                EditText subject = (EditText) described(form.view(),"Request subject");
                EditText description = (EditText) described(form.view(),"Request details");
                subject.setText("x".repeat(161)); description.setText("Preserved details");
                find(form.view(),"Submit request").performClick();
                assertEquals(0,calls.get()); assertNotNull(subject.getError());
                assertEquals("Preserved details",description.getText().toString());
                assertNotNull(find(form.view(),"Correct the highlighted information."));
                subject.setText("Valid subject"); description.setText("x".repeat(4001));
                find(form.view(),"Submit request").performClick();
                assertEquals(0,calls.get()); assertNotNull(description.getError());
                description.setText("Valid details"); find(form.view(),"Submit request").performClick();
                assertEquals(1,calls.get()); assertNotNull(description.getError());
                assertEquals("Valid subject",subject.getText().toString());
                find(form.view(),"Retry submission").performClick();
                assertEquals(2,calls.get()); assertFalse(token[0].isEmpty());
            });
        }
    }

    @Test public void rejectedPublicationReopensSavedDraftAndHighlightsMissingFields() {
        try (var scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                var view = new LiveServiceCatalogView(activity,new LiveServiceCatalogView.Actions() {
                    public void request(String path,JSONObject body,BiConsumer<String,String> complete) {
                        assertTrue(path.endsWith("/publish")); assertTrue(body.optBoolean("confirmed"));
                        complete.accept("{\"purpose\":\"Check this field.\",\"contact_email\":\"Check this field.\"}",
                                "Complete the highlighted service information before publishing.");
                    }
                    public void replaceForm(int id,Runnable done,java.util.function.Consumer<String> failure) { fail(); }
                    public void downloadForm(int id,String name,String mime,Runnable failure) { fail(); }
                    public void back() { }
                });
                activity.setContentView(view.view());
                try {
                    JSONObject revision=new JSONObject("{\"id\":1,\"state\":\"draft\",\"code\":\"TEST\",\"details\":{\"name\":\"Saved service name\"}}");
                    // Confirmed-publication callback, independently of the unchanged confirmation dialog.
                    var method=LiveServiceCatalogView.class.getDeclaredMethod("publish",JSONObject.class);
                    method.setAccessible(true);
                    // Initialize the existing view/status without issuing a list request.
                    var editor=LiveServiceCatalogView.class.getDeclaredMethod("edit",JSONObject.class);
                    editor.setAccessible(true); editor.invoke(view,revision);
                    method.invoke(view,revision);
                    assertNotNull(find(view.view(),"Complete the highlighted service information before publishing."));
                    assertNotNull(((EditText)described(view.view(),"Purpose")).getError());
                    assertNotNull(((EditText)described(view.view(),"Contact email")).getError());
                    assertEquals("Saved service name",((EditText)described(view.view(),"Service name")).getText().toString());
                } catch (Exception error) { throw new AssertionError(error); }
            });
        }
    }

    @Test public void connectionDialogUsesExactMessageAndRetriesOnce() {
        AtomicInteger retried=new AtomicInteger();
        try (var scenario=ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                try {
                    var method=MainActivity.class.getDeclaredMethod("connectionRetry",Runnable.class,Runnable.class);
                    method.setAccessible(true);
                    android.app.AlertDialog dialog=(android.app.AlertDialog)method.invoke(activity,
                            (Runnable)retried::incrementAndGet,(Runnable)()->fail("Retry selected"));
                    TextView message=dialog.findViewById(android.R.id.message);
                    assertEquals("Unable to connect. Try again",message.getText().toString());
                    assertEquals("Retry",dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).getText().toString());
                    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick();
                } catch(Exception error) { throw new AssertionError(error); }
            });
            var instrumentation=InstrumentationRegistry.getInstrumentation();
            instrumentation.waitForIdleSync();
            assertEquals(1,retried.get());
        }
    }
    private static View described(View v,String label) {
        if(label.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;
        if(v instanceof ViewGroup g)for(int i=0;i<g.getChildCount();i++){View x=described(g.getChildAt(i),label);if(x!=null)return x;}
        return null;
    }
    private static View find(View v,String text) {
        if(v instanceof TextView t&&text.contentEquals(t.getText()))return v;
        if(v instanceof ViewGroup g)for(int i=0;i<g.getChildCount();i++){View x=find(g.getChildAt(i),text);if(x!=null)return x;}
        return null;
    }
}
