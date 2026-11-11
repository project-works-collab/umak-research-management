package ph.edu.umak.crm;

import android.content.Context;
import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import ph.edu.umak.crm.cache.PublicDatabase;
import ph.edu.umak.crm.cache.PublicItem;
import ph.edu.umak.crm.security.SessionVault;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class SecurityCacheTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void keystoreCiphertextTamperAndLogoutRequireSignIn() throws Exception {
        SessionVault vault = new SessionVault(context);
        vault.clear();
        vault.save("synthetic-token");
        assertEquals("synthetic-token", vault.read());
        File file = new File(context.getNoBackupFilesDir(), "crm-session-v1");
        byte[] bytes = Files.readAllBytes(file.toPath());
        assertFalse(new String(bytes, StandardCharsets.ISO_8859_1).contains("synthetic-token"));
        bytes[bytes.length - 1] ^= 1;
        Files.write(file.toPath(), bytes);
        assertNull(vault.read());
        vault.save("synthetic-token");
        vault.clear();
        assertNull(vault.read());
    }

    @Test
    public void roomPublicCacheSurvivesDatabaseReopenWithoutNetwork() {
        String name = "crm-instrumented-cache.db";
        context.deleteDatabase(name);
        PublicDatabase first = Room.databaseBuilder(context, PublicDatabase.class, name).build();
        try {
            first.content().replace(Collections.singletonList(
                    new PublicItem("directory", "directory", "CRM", "Public demo", "v1", 1, null)));
        } finally {
            first.close();
        }
        PublicDatabase reopened = Room.databaseBuilder(context, PublicDatabase.class, name).build();
        try {
            assertEquals(1, reopened.content().list().size());
            assertEquals("Public demo", reopened.content().list().get(0).text);
        } finally {
            reopened.close();
            context.deleteDatabase(name);
        }
    }
}
