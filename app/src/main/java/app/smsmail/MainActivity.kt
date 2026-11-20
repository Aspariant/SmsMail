package app.smsmail

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.PowerManager
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.DateFormat
import java.util.Date

/** Main dashboard for daily use. Contains status overview, quick actions, and settings gear. */
class MainActivity : Activity() {

    private lateinit var store: Store
    private lateinit var status: TextView
    private var currentTheme: String = "light"

    override fun onCreate(savedInstanceState: Bundle?) {
        store = Store(this)
        currentTheme = store.theme()
        val isDark = currentTheme == "dark"
        setTheme(
            if (isDark) android.R.style.Theme_Material
            else android.R.style.Theme_Material_Light_DarkActionBar
        )

        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        // Top Header with Title and Gear icon button
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, pad)
        }
        val titleTv = TextView(this).apply {
            text = "SmsMail"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val gearBtn = Button(this).apply {
            text = "⚙ Settings"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }
        headerLayout.addView(titleTv)
        headerLayout.addView(gearBtn)
        root.addView(headerLayout)

        // Daily action buttons
        button(root, "Send test message") { sendTest() }
        button(root, "Retry queued messages") { Queue.resume(this); refresh() }
        button(root, "Refresh status") { refresh() }

        status = TextView(this).apply { setPadding(0, pad, 0, 0) }
        root.addView(status)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        if (store.theme() != currentTheme) {
            recreate()
        } else {
            // Opening the app is a good moment to make sure the LAN server is up.
            if (store.isLan()) LanService.start(this)
            refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val item = menu.add(0, 1, 0, "Settings")
        item.setIcon(android.R.drawable.ic_menu_preferences)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendTest() {
        try {
            Queue.add(
                this, "SMS", "SmsMail test", System.currentTimeMillis(),
                "If you can read this, forwarding works."
            )
            toast("Test message queued")
        } catch (e: Exception) {
            toast("Could not queue: ${e.javaClass.simpleName}")
        }
        refresh()
    }

    private fun refresh() {
        val fmt = DateFormat.getDateTimeInstance()
        val smsPerm = checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val phoneOk = checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val logOk = checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        val callPerm = phoneOk && logOk
        val ignoring = (getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)
        val ok = store.lastSuccess()
        val failTime = store.lastFailureTime()

        val lan = store.isLan()
        status.text = buildString {
            if (lan) {
                appendLine("Delivery: Local network → iPhone Shortcut (recommended)")
                appendLine("LAN server: ${LanService.state}")
                appendLine("Address: ${Net.baseUrl() ?: "no Wi-Fi or hotspot"}")
                appendLine("Set up the iPhone from Settings → Shortcut setup guide")
            } else {
                appendLine("Delivery: Email")
                appendLine("Receiver: ${if (store.receiver() == "android") "Android (Gmail)" else "iPhone (iCloud)"}")
                appendLine("Configured: ${if (store.config() != null) "yes" else "no"}")
            }
            appendLine()
            appendLine("SMS permission: ${if (smsPerm) "granted" else "NOT granted"}")
            appendLine("Call permission: ${if (callPerm) "granted" else "NOT granted"}")
            appendLine("Call alerts: ${if (store.isCallAlertsEnabled()) "enabled" else "disabled"}")
            appendLine("Battery optimization exempt: ${if (ignoring) "yes" else "no"}")
            appendLine("Last delivered: ${if (ok > 0) fmt.format(Date(ok)) else "never"}")
            appendLine("Last failure: ${store.lastFailure()?.let { "${fmt.format(Date(failTime))}\n  $it" } ?: "none"}")
            append(
                if (lan) "Waiting for the iPhone to fetch: ${Queue.names(this@MainActivity).size}"
                else "Queued messages: ${Queue.names(this@MainActivity).size}"
            )
        }
    }

    private fun button(parent: LinearLayout, label: String, onClick: () -> Unit) {
        parent.addView(Button(this).apply {
            text = label
            setOnClickListener { onClick() }
        })
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
