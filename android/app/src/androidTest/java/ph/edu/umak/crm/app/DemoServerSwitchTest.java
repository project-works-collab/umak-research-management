package ph.edu.umak.crm.app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public final class DemoServerSwitchTest {
    @Test public void switchPersistsAcrossRecreationAndReturnsToLocal() throws Exception {
        assumeTrue(BuildConfig.SERVER_SWITCH);
        try(var scenario=ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                activity.getSharedPreferences("crm-settings",0).edit().putString("server",BuildConfig.LOCAL_SERVER_URL).commit();
                activity.recreate();
            });
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            for(String destination:new String[]{BuildConfig.CLOUD_SERVER_URL,BuildConfig.LOCAL_SERVER_URL}) {
                scenario.onActivity(activity -> {
                    View toggle=find(activity.getWindow().getDecorView(),"Switch demo server");
                    assertNotNull(toggle);toggle.performClick();
                });
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                scenario.onActivity(activity -> {
                    var windows=android.view.inspector.WindowInspector.getGlobalWindowViews();
                    View dialog=windows.get(windows.size()-1);
                    View confirm=find(dialog,"Switch");assertNotNull(confirm);confirm.performClick();
                });
                boolean[] ready={false};
                for(int i=0;i<100&&!ready[0];i++) {
                    Thread.sleep(100);
                    scenario.onActivity(activity -> ready[0]=destination.equals(activity.getSharedPreferences("crm-settings",0).getString("server",""))&&find(activity.getWindow().getDecorView(),"Switch demo server")!=null);
                }
                assertTrue("Server switched and recreated",ready[0]);
            }
        }
    }
    private static View find(View root,String text) {
        if(text.equals(root.getContentDescription())||(root instanceof TextView label&&text.contentEquals(label.getText())))return root;
        if(root instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){View found=find(group.getChildAt(i),text);if(found!=null)return found;}
        return null;
    }
}
