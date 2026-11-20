package app.smsmail

import android.content.Context
import java.security.SecureRandom

data class Config(
    val sender: String,
    val password: String,
    val recipient: String,
    val receiver: String = "iphone",
    val theme: String = "light",
    val callAlertsEnabled: Boolean = false
) {
    fun smtpHost(): String = when (receiver) {
        "android" -> "smtp.gmail.com"
        else -> "smtp.mail.me.com"
    }

    fun smtpPort(): Int = 587
}

/**
 * Settings and status. Sender, recipient and password are stored encrypted with
 * the Keystore key. Status holds timestamps and short error names only, never
 * message text and never credentials.
 */
class Store(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("smsmail", Context.MODE_PRIVATE)

    // ---- credentials & preferences ----
    fun save(
        sender: String,
        password: String?,
        recipient: String,
        theme: String = theme(),
        receiver: String = receiver(),
        callAlertsEnabled: Boolean = isCallAlertsEnabled()
    ) {
        val key = KeyStoreKey.get()
        val e = prefs.edit()
            .putString("sender", Crypto.encryptString(key, sender))
            .putString("recipient", Crypto.encryptString(key, recipient))
            .putString("theme", if (theme == "dark") "dark" else "light")
            .putString("receiver", if (receiver == "android") "android" else "iphone")
            .putBoolean("call_alerts_enabled", callAlertsEnabled)
        // Empty password field means "keep the saved one".
        if (!password.isNullOrEmpty()) e.putString("password", Crypto.encryptString(key, password))
        e.apply()
    }

    private fun secret(name: String): String? = try {
        prefs.getString(name, null)?.let { Crypto.decryptString(KeyStoreKey.get(), it) }
    } catch (e: Exception) {
        null
    }

    /** Saves only the email account; used when the user also fills it in while on LAN mode. */
    fun saveEmail(sender: String, password: String?, recipient: String) =
        save(sender, password, recipient)

    fun savePreferences(delivery: String, theme: String, receiver: String, callAlertsEnabled: Boolean) {
        prefs.edit()
            .putString("delivery", if (delivery == DELIVERY_EMAIL) DELIVERY_EMAIL else DELIVERY_LAN)
            .putString("theme", if (theme == "dark") "dark" else "light")
            .putString("receiver", if (receiver == "android") "android" else "iphone")
            .putBoolean("call_alerts_enabled", callAlertsEnabled)
            .apply()
    }

    fun sender() = secret("sender")
    fun recipient() = secret("recipient")
    fun hasPassword() = prefs.contains("password")
    fun theme(): String = if (prefs.getString("theme", "light") == "dark") "dark" else "light"
    fun receiver(): String = if (prefs.getString("receiver", "iphone") == "android") "android" else "iphone"
    fun isCallAlertsEnabled(): Boolean = prefs.getBoolean("call_alerts_enabled", false)

    /**
     * How messages leave the phone: "lan" (recommended, iPhone Shortcut on the same
     * Wi-Fi) or "email". New installs default to LAN; installs that were already
     * set up for email before LAN existed keep email until the user changes it.
     */
    fun delivery(): String = when (prefs.getString("delivery", null)) {
        DELIVERY_EMAIL -> DELIVERY_EMAIL
        DELIVERY_LAN -> DELIVERY_LAN
        else -> if (hasPassword()) DELIVERY_EMAIL else DELIVERY_LAN
    }

    fun isLan(): Boolean = delivery() == DELIVERY_LAN

    /**
     * Secret the iPhone must send with every LAN request. Created on first use and
     * stored encrypted like the email password.
     */
    @Synchronized
    fun lanKey(): String = secret("lan_key") ?: regenerateLanKey()

    @Synchronized
    fun regenerateLanKey(): String {
        val rnd = SecureRandom()
        // No 0/O/1/I/L so it can be read and typed by hand if ever needed. 14 chars = 70 bits.
        val alphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
        val key = (1..14).map { alphabet[rnd.nextInt(alphabet.length)] }.joinToString("")
        prefs.edit().putString("lan_key", Crypto.encryptString(KeyStoreKey.get(), key)).commit()
        return key
    }

    fun config(): Config? {
        val s = sender() ?: return null
        val p = secret("password") ?: return null
        val r = recipient() ?: return null
        return Config(
            sender = s,
            password = p,
            recipient = r,
            receiver = receiver(),
            theme = theme(),
            callAlertsEnabled = isCallAlertsEnabled()
        )
    }

    // ---- status ----
    fun success() {
        prefs.edit().putLong("last_ok", System.currentTimeMillis()).apply()
    }

    fun fail(reason: String) {
        prefs.edit()
            .putLong("last_fail_time", System.currentTimeMillis())
            .putString("last_fail", reason.take(700))
            .apply()
    }

    fun lastSuccess(): Long = prefs.getLong("last_ok", 0L)
    fun lastFailure(): String? = prefs.getString("last_fail", null)
    fun lastFailureTime(): Long = prefs.getLong("last_fail_time", 0L)

    // ---- rate limit: max 30 sends per 10 minutes ----
    fun tryAcquireSlot(now: Long): Boolean = synchronized(LOCK) {
        val recent = (prefs.getString("sent_times", "") ?: "")
            .split(",").mapNotNull { it.toLongOrNull() }
            .filter { now - it < WINDOW_MS }
        if (recent.size >= MAX_PER_WINDOW) return false
        prefs.edit().putString("sent_times", (recent + now).joinToString(",")).apply()
        true
    }

    companion object {
        const val DELIVERY_LAN = "lan"
        const val DELIVERY_EMAIL = "email"
        private val LOCK = Any()
        const val MAX_PER_WINDOW = 30
        const val WINDOW_MS = 10 * 60 * 1000L
    }
}