package ph.edu.umak.crm.security;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.AtomicFile;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Ciphertext is excluded from Auto Backup by residing in noBackupFilesDir. */
public final class SessionVault {
    private final AtomicFile file;
    private static final String ALIAS = "crm.session.aes256.v1";

    public SessionVault(Context context) {
        file = new AtomicFile(new File(context.getNoBackupFilesDir(), "crm-session-v1"));
    }

    @Nullable
    private SecretKey key(boolean create) throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        java.security.Key existing = store.getKey(ALIAS, null);
        if (existing instanceof SecretKey) return (SecretKey) existing;
        if (!create) return null;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).setKeySize(256).build());
        return generator.generateKey();
    }

    public synchronized void save(String secret) throws Exception {
        byte[] plaintext = secret.getBytes(StandardCharsets.UTF_8);
        if (plaintext.length < 1 || plaintext.length > 4096) throw new IllegalArgumentException("Invalid session size");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key(true)); // Provider creates a fresh random IV.
        cipher.updateAAD(ALIAS.getBytes(StandardCharsets.UTF_8));
        byte[] encrypted = cipher.doFinal(plaintext);
        FileOutputStream stream = file.startWrite();
        try {
            stream.write(ByteBuffer.allocate(4 + cipher.getIV().length + encrypted.length)
                    .putInt(1).put(cipher.getIV()).put(encrypted).array());
            file.finishWrite(stream);
        } catch (Exception error) {
            file.failWrite(stream);
            throw error;
        }
    }

    @Nullable
    public synchronized String read() throws Exception {
        try {
            byte[] bytes = file.readFully();
            if (bytes.length < 33 || bytes.length > 8192) throw new IllegalArgumentException("Invalid ciphertext size");
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            if (buffer.getInt() != 1) throw new IllegalArgumentException("Invalid ciphertext version");
            byte[] iv = new byte[12];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            SecretKey secretKey = key(false);
            if (secretKey == null) throw new IllegalStateException("Key unavailable");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
            cipher.updateAAD(ALIAS.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception error) {
            clear();
            return null; // Lost/invalid key or modified ciphertext requires sign-in.
        }
    }

    public synchronized void clear() throws Exception {
        file.delete();
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        store.deleteEntry(ALIAS);
    }
}
