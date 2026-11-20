package app.smsmail

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Settings screen for delivery method, accounts, preferences, and permissions. */
class SettingsActivity : Activity() {

    private lateinit var store: Store
    private lateinit var etSender: EditText
    private lateinit var etPassword: EditText
    private lateinit var etRecipient: EditText
    private lateinit var rbLight: RadioButton
    private lateinit var rbDark: RadioButton
    private lateinit var rbIphone: RadioButton
    private lateinit var rbAndroid: RadioButton
    private lateinit var rbLan: RadioButton
    private lateinit var rbEmail: RadioButton
    private lateinit var cbCallAlerts: CheckBox
    private lateinit var lanSection: LinearLayout
    private lateinit var emailSection: LinearLayout
    private lateinit var lanUrl: TextView
    private lateinit var qrView: ImageView
    private var pad = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        store = Store(this)
        val isDark = store.theme() == "dark"
        setTheme(
            if (isDark) android.R.style.Theme_Material
            else android.R.style.Theme_Material_Light_DarkActionBar
        )

        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        root.addView(TextView(this).apply {
            text = "Settings"
            textSize = 22f
            setPadding(0, 0, 0, pad)
        })

        // ---- Delivery method ----
        heading(root, "Delivery method")
        rbLan = RadioButton(this).apply { text = "Local network → iPhone Shortcut (Recommended)" }
        rbEmail = RadioButton(this).apply { text = "Email" }
        root.addView(RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            addView(rbLan)
            addView(rbEmail)
            setOnCheckedChangeListener { _, _ -> showSectionForMode() }
        })
        if (store.isLan()) rbLan.isChecked = true else rbEmail.isChecked = true

        // ---- LAN section ----
        lanSection = section(root)
        hint(lanSection, "Instant and private: SMS and call alerts go straight from this phone to your " +
            "iPhone over Wi-Fi. No email account or password needed. Both phones must be on the same Wi-Fi, or the " +
            "iPhone can join this phone's hotspot.")
        lanSection.addView(TextView(this).apply {
            text = "Scan with the iPhone camera to open the setup guide there:"
            setPadding(0, pad / 2, 0, pad / 4)
        })
        val qrSize = (220 * resources.displayMetrics.density).toInt()
        qrView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(qrSize, qrSize)
        }
        lanSection.addView(qrView)
        lanSection.addView(TextView(this).apply {
            text = "Shortcut URL"
            setTypeface(null, Typeface.BOLD)
            setPadding(0, pad / 2, 0, 0)
        })
        lanUrl = TextView(this).apply {
            setTextIsSelectable(true)
            typeface = Typeface.MONOSPACE
        }
        lanSection.addView(lanUrl)
        button(lanSection, "Copy Shortcut URL") { copyUrl() }
        button(lanSection, "iPhone Shortcut setup guide") { showGuide() }
        button(lanSection, "New key (disconnects the current Shortcut)") { confirmNewKey() }

        // ---- Email section ----
        emailSection = section(root)
        hint(emailSection, "Forwards each SMS and call alert as an email through iCloud or Gmail. " +
            "Needs an app-specific password.")
        etSender = field(
            emailSection,
            "Sender email (iCloud or Gmail app account)",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        etPassword = field(
            emailSection,
            if (store.hasPassword()) "App-specific password (saved; leave empty to keep)"
            else "App-specific password",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        etRecipient = field(
            emailSection,
            "Recipient email (iPhone or Android mailbox)",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        etSender.setText(store.sender() ?: "")
        etRecipient.setText(store.recipient() ?: "")

        // Receiver selector: iPhone (iCloud) / Android (Gmail)
        emailSection.addView(TextView(this).apply {
            text = "Receiver"
            setPadding(0, pad / 2, 0, 0)
        })
        rbIphone = RadioButton(this).apply { text = "iPhone (iCloud)" }
        rbAndroid = RadioButton(this).apply { text = "Android (Gmail)" }
        emailSection.addView(RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            addView(rbIphone)
            addView(rbAndroid)
        })
        if (store.receiver() == "android") rbAndroid.isChecked = true else rbIphone.isChecked = true

        // ---- Common options ----
        heading(root, "Options")
        root.addView(TextView(this).apply { text = "Theme" })
        rbLight = RadioButton(this).apply { text = "Light" }
        rbDark = RadioButton(this).apply { text = "Dark" }
        root.addView(RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            addView(rbLight)
            addView(rbDark)
        })
        if (isDark) rbDark.isChecked = true else rbLight.isChecked = true

        cbCallAlerts = CheckBox(this).apply {
            text = "Receive incoming call notifications"
            isChecked = store.isCallAlertsEnabled()
            setPadding(0, pad / 2, 0, pad / 2)
            setOnCheckedChangeListener { _, checked ->
                if (checked && !hasCallPermissions()) {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG),
                        2
                    )
                }
            }
        }
        root.addView(cbCallAlerts)

        button(root, "Grant SMS permission") {
            requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS), 1)
        }
        button(root, "Grant Call permission") {
            requestPermissions(
                arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG),
                2
            )
        }
        button(root, "Battery optimization settings") {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }

        button(root, "Save settings") { save() }

        setContentView(ScrollView(this).apply { addView(root) })
        showSectionForMode()
    }

    override fun onResume() {
        super.onResume()
        updateLanInfo() // Wi-Fi may have changed while we were away
    }

    override fun onRequestPermissionsResult(code: Int, perms: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(code, perms, results)
        if (code == 2) {
            if (!hasCallPermissions()) {
                cbCallAlerts.isChecked = false
                toast("Call & Phone permissions are needed for call alerts")
            } else {
                cbCallAlerts.isChecked = true
                toast("Call permissions granted. Tap 'Save settings' to enable.")
            }
        } else if (code == 3) {
            finish()
        } else if (code == 1) {
            val smsOk = checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
            if (smsOk) toast("SMS permission granted")
        }
    }

    private fun showSectionForMode() {
        // The radio listener also fires while onCreate is still building the screen.
        if (!::lanSection.isInitialized || !::emailSection.isInitialized) return
        val lan = rbLan.isChecked
        lanSection.visibility = if (lan) View.VISIBLE else View.GONE
        emailSection.visibility = if (lan) View.GONE else View.VISIBLE
        if (lan) updateLanInfo()
    }

    private fun shortcutUrl(): String? =
        Net.baseUrl()?.let { ShortcutGuide.shortcutUrl(it, store.lanKey()) }

    private fun updateLanInfo() {
        if (!::lanUrl.isInitialized) return
        val base = Net.baseUrl()
        if (base == null) {
            lanUrl.text = "No local network. Connect this phone to the same Wi-Fi as your iPhone, or turn on its hotspot and join it from the iPhone."
            qrView.setImageDrawable(null)
            qrView.visibility = View.GONE
            return
        }
        val key = store.lanKey()
        lanUrl.text = ShortcutGuide.shortcutUrl(base, key)
        qrView.visibility = View.VISIBLE
        qrView.setImageBitmap(Qr.bitmap(ShortcutGuide.setupUrl(base, key), qrView.layoutParams.width))
    }

    private fun copyUrl() {
        val url = shortcutUrl() ?: return toast("Connect to Wi-Fi or turn on the hotspot first")
        getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Shortcut URL", url))
        toast("Copied")
    }

    private fun showGuide() {
        AlertDialog.Builder(this)
            .setTitle("iPhone Shortcut setup")
            .setMessage(ShortcutGuide.plainText(shortcutUrl()))
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmNewKey() {
        AlertDialog.Builder(this)
            .setTitle("Create a new key?")
            .setMessage("The current Shortcut URL will stop working. You'll need to paste the new URL into the iPhone Shortcut.")
            .setPositiveButton("New key") { _, _ ->
                store.regenerateLanKey()
                updateLanInfo()
                toast("New key created")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun hasCallPermissions(): Boolean {
        val phoneOk = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val logOk = checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        return phoneOk && logOk
    }

    private fun save() {
        val lan = rbLan.isChecked
        val s = etSender.text.toString().trim()
        val r = etRecipient.text.toString().trim()
        val p = etPassword.text.toString()
        val emailValid = s.contains("@") && r.contains("@") && (p.isNotEmpty() || store.hasPassword())

        if (!lan) {
            if (!s.contains("@") || !r.contains("@")) {
                toast("Enter valid sender and recipient addresses"); return
            }
            if (p.isEmpty() && !store.hasPassword()) {
                toast("Enter the app-specific password"); return
            }
        }

        val previousTheme = store.theme()
        val themeValue = if (rbDark.isChecked) "dark" else "light"
        val receiverValue = if (rbAndroid.isChecked) "android" else "iphone"

        try {
            // On LAN the email account is optional: keep it only if it's complete.
            if (emailValid) store.saveEmail(s, p, r)
            store.savePreferences(
                if (lan) Store.DELIVERY_LAN else Store.DELIVERY_EMAIL,
                themeValue, receiverValue, cbCallAlerts.isChecked
            )
        } catch (e: Exception) {
            toast("Could not save: ${e.javaClass.simpleName}"); return
        }
        etPassword.setText("")
        if (store.hasPassword()) etPassword.hint = "App-specific password (saved; leave empty to keep)"

        Queue.resume(this) // start/stop the LAN server and re-run anything that was stuck
        toast("Saved")

        // Android 13+: the LAN server's status notification needs this permission.
        // Close the screen once the user has answered.
        if (lan && Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 3)
            return
        }
        if (themeValue != previousTheme) {
            recreate()
        } else {
            finish()
        }
    }

    private fun heading(parent: LinearLayout, label: String) {
        parent.addView(TextView(this).apply {
            text = label
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, pad, 0, pad / 4)
        })
    }

    private fun hint(parent: LinearLayout, label: String) {
        parent.addView(TextView(this).apply {
            text = label
            alpha = 0.75f
            setPadding(0, pad / 4, 0, pad / 4)
        })
    }

    private fun section(parent: LinearLayout) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        parent.addView(this)
    }

    private fun field(parent: LinearLayout, hint: String, type: Int) = EditText(this).apply {
        this.hint = hint
        inputType = type
        importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        parent.addView(this)
    }

    private fun button(parent: LinearLayout, label: String, onClick: () -> Unit) {
        parent.addView(Button(this).apply {
            text = label
            setOnClickListener { onClick() }
        })
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
