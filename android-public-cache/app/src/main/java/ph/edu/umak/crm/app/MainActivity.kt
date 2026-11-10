package ph.edu.umak.crm.app

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.room.Room
import androidx.work.*
import org.json.JSONObject
import ph.edu.umak.crm.cache.PublicDatabase
import ph.edu.umak.crm.cache.PublicSync
import ph.edu.umak.crm.security.SessionVault
import java.net.URL
import java.net.HttpURLConnection
import java.util.Date
import java.util.concurrent.Executors
import java.io.File

/** Launchable integration for sign-in, Keystore sessions, and public offline resources. */
class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var layout: LinearLayout
    private lateinit var status: TextView
    private lateinit var resources: LinearLayout
    private lateinit var server: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private var refreshData: androidx.lifecycle.LiveData<List<WorkInfo>>? = null
    private var refreshObserver: androidx.lifecycle.Observer<List<WorkInfo>>? = null
    private lateinit var vault: SessionVault
    private val preferences by lazy { getSharedPreferences("crm-settings", MODE_PRIVATE) }
    private fun message(text: String) { runOnUiThread { if (!isDestroyed) status.text = text } }
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        vault = SessionVault(this)
        layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }
        setContentView(ScrollView(this).apply { addView(layout) })
        layout.addView(TextView(this).apply { text = "UMak Research Management"; textSize = 24f })
        server = field("CRM server (https://…)").apply { setText(preferences.getString("server", "")) }
        email = field("Email").apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS }
        password = field("Password").apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD; isSaveEnabled = false }
        status = TextView(this); layout.addView(status)
        button("Sign in") { signIn() }
        button("Sign out") { signOut() }
        button("Refresh public resources") { refresh() }
        resources = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; layout.addView(resources)
        loadCache()
        executor.execute {
            val saved = session()
            message(if (saved == null) "Sign in to access your account. Public resources remain available offline." else "Saved session available; expires ${Date(saved.getLong("expires_at"))}.")
        }
        val origin = preferences.getString("server", "") ?: ""
        if (origin.isNotEmpty()) { PublicSync.schedule(this, origin) }
    }
    private fun field(hintText: String) = EditText(this).apply {
        hint = hintText
        this@MainActivity.layout.addView(this)
    }
    private fun button(label: String, action: () -> Unit) { layout.addView(Button(this).apply { text = label; setOnClickListener { action() } }) }
    private fun origin(): String {
        val url = URL(server.text.toString().trim())
        require(url.protocol == "https" && url.userInfo == null && url.query == null && url.ref == null && url.path in listOf("", "/"))
        return url.toExternalForm().trimEnd('/')
    }
    private fun session(): JSONObject? = try {
        val saved = JSONObject(vault.read() ?: "{}")
        if (saved.getLong("expires_at") <= System.currentTimeMillis()) { vault.clear(); null } else saved
    } catch (_: Exception) { vault.clear(); null }
    private fun request(base: String, path: String, body: JSONObject, token: String? = null): JSONObject {
        val connection = URL(base + path).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 10000; connection.readTimeout = 15000
        connection.requestMethod = "POST"; connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
        try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            require(connection.responseCode in 200..299)
            if (connection.responseCode == 204) return JSONObject()
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                while (true) {
                    val count = input.read(buffer); if (count < 0) break
                    require(output.size() + count <= 16384); output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            return JSONObject(String(bytes, Charsets.UTF_8))
        } finally { connection.disconnect() }
    }
    private fun signIn() {
        val base = try { origin() } catch (_: Exception) { message("Enter a valid HTTPS server address."); return }
        val previous = preferences.getString("server", "") ?: ""
        if (previous.isNotEmpty() && previous != base) { message("Use the saved server address for this installation."); return }
        val credentials = JSONObject().put("email", email.text.toString().trim()).put("password", password.text.toString())
        password.text.clear()
        preferences.edit().putString("server", base).apply()
        message("Signing in…")
        executor.execute {
            try {
                val response = request(base, "/api/v1/auth/login", credentials)
                val lifetime = response.getLong("expires_in"); require(lifetime in 1..900)
                vault.save(JSONObject().put("token", response.getString("access_token")).put("server", base)
                    .put("expires_at", System.currentTimeMillis() + lifetime * 1000).toString())
                preferences.edit().putString("server", base).apply()
                PublicSync.schedule(this, base)
                message("Signed in. Session expires in ${lifetime / 60} minutes.")
            } catch (_: Exception) { message("Sign-in failed. Check your credentials, connection, and server address.") }
        }
    }
    private fun signOut() {
        executor.execute {
            val saved = session()
            // Queue behind any in-flight sign-in so logout always clears the latest token.
            vault.clear()
            if (saved != null) {
                try { request(saved.getString("server"), "/api/v1/auth/logout", JSONObject(), saved.getString("token")); message("Signed out.") }
                catch (_: Exception) { message("Signed out on this device. Server revocation unavailable; session expires within 15 minutes.") }
            } else message("Signed out.")
        }
    }
    private fun refresh() {
        val base = try { origin() } catch (_: Exception) { message("Enter a valid HTTPS server address."); return }
        // Cached content and credentials are bound to one server origin.
        val previous = preferences.getString("server", "") ?: ""
        if (previous.isNotEmpty() && previous != base) { message("Use the saved server for cached resources."); return }
        preferences.edit().putString("server", base).apply()
        PublicSync.schedule(this, base)
        val work = OneTimeWorkRequestBuilder<PublicSync>().setInputData(workDataOf("base_url" to base))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        val manager = WorkManager.getInstance(this)
        manager.enqueueUniqueWork("crm-public-refresh", ExistingWorkPolicy.REPLACE, work)
        message("Refresh queued. Saved resources remain available offline.")
        refreshObserver?.let { refreshData?.removeObserver(it) }
        val data = manager.getWorkInfosForUniqueWorkLiveData("crm-public-refresh")
        val observer = object : androidx.lifecycle.Observer<List<WorkInfo>> {
            override fun onChanged(value: List<WorkInfo>) {
                if (value.any { it.id == work.id && it.state.isFinished }) {
                    data.removeObserver(this)
                    if (!isDestroyed) loadCache()
                }
            }
        }
        refreshData = data; refreshObserver = observer; data.observeForever(observer)
    }
    private fun loadCache() {
        executor.execute {
            val db = Room.databaseBuilder(applicationContext, PublicDatabase::class.java, "crm-public.db").build()
            try {
                val rows = db.content().list()
                runOnUiThread {
                    if (!isDestroyed) {
                        resources.removeAllViews()
                        resources.addView(TextView(this).apply { text = if (rows.isEmpty()) "No saved public resources. Connect and refresh first." else "Saved public resources • last synced ${Date(rows.minOf { it.syncedAt })}. May be outdated; available offline." })
                        for (row in rows) {
                            resources.addView(TextView(this).apply { text = "\n${row.title}\n${row.text}" })
                            row.templatePath?.let { path -> resources.addView(Button(this).apply {
                                text = "Open saved blank form"
                                setOnClickListener {
                                    executor.execute {
                                        try {
                                            val file = File(path); require(file.length() <= 25_000_000)
                                            // Current server publishes a plain-text blank template.
                                            val text = file.readText(Charsets.UTF_8)
                                            runOnUiThread { if (!isDestroyed) android.app.AlertDialog.Builder(this@MainActivity).setTitle(row.title).setMessage(text).setPositiveButton("Close", null).show() }
                                        } catch (_: Exception) { message("Saved form unavailable. Refresh public resources.") }
                                    }
                                }
                            }) }
                        }
                    }
                }
            } finally { db.close() }
        }
    }
    override fun onDestroy() { refreshObserver?.let { refreshData?.removeObserver(it) }; executor.shutdown(); super.onDestroy() }
}
