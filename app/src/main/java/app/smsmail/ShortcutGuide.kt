package app.smsmail

/**
 * The iPhone setup steps. Shown in the app and on the setup page the iPhone
 * opens from the QR code, so both always say the same thing.
 */
object ShortcutGuide {

    /** Ready-made SmsMail shortcut. On import it asks for the Shortcut URL and the Wi-Fi name. */
    const val ICLOUD_SHORTCUT = "https://www.icloud.com/shortcuts/ffa3ff1e25aa48df8899342de6cf1d65"

    /** The URL the Shortcut polls. Replies with JSON: {"text":…} for a message, {} for nothing. */
    fun shortcutUrl(base: String, key: String) = "$base/next?key=$key"

    fun setupUrl(base: String, key: String) = "$base/setup?key=$key"

    val steps = listOf(
        "Copy the Shortcut URL.",
        "Get the SmsMail shortcut and tap \"Add Shortcut\".",
        "When it asks, paste the Shortcut URL, then type the exact name of the Wi-Fi network both phones use " +
            "(this phone's hotspot name, if the iPhone joins its hotspot).",
        "Run the shortcut once and allow the network and notification prompts.",
    )

    val automationIntro =
        "iOS can't keep a shortcut running forever, so let automations restart it. Starting it while it's " +
            "already running is harmless: the new copy takes over and the old one stops."

    val automations = listOf(
        "Shortcuts → Automation → + → Wi-Fi → the same network (or this phone's hotspot) → Run Immediately → turn off Notify When Run → SmsMail.",
        "+ → App → apps you open often (Messages, WhatsApp, Safari…) → Is Opened → Run Immediately → SmsMail.",
        "+ → Charger → Is Connected → Run Immediately → SmsMail.",
    )

    val notes = listOf(
        "Nothing is lost when iOS stops the shortcut: messages wait on this phone until it runs again.",
        "SmsMail forwards SMS and normal phone calls to this phone's SIM. WhatsApp and other internet calls are not detected.",
        "If the URL changes (new IP or new key), run the setup again and paste the new URL.",
    )

    fun plainText(shortcutUrl: String?): String = buildString {
        appendLine("Easiest: scan the QR code in Settings with the iPhone camera. It opens a page with the URL to copy and the shortcut link.")
        appendLine()
        appendLine("Shortcut URL:")
        appendLine(shortcutUrl ?: "(connect this phone to Wi-Fi or turn on its hotspot first)")
        appendLine()
        appendLine("SmsMail shortcut:")
        appendLine(ICLOUD_SHORTCUT)
        appendLine()
        steps.forEachIndexed { i, s -> appendLine("${i + 1}. $s") }
        appendLine()
        appendLine("RUN IT AUTOMATICALLY")
        appendLine(automationIntro)
        automations.forEach { appendLine("• $it") }
        appendLine()
        notes.forEach { appendLine("• $it") }
    }

    fun html(shortcutUrl: String, waiting: Int): String {
        fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
        fun list(tag: String, items: List<String>) =
            "<$tag>" + items.joinToString("") { "<li>${esc(it)}</li>" } + "</$tag>"
        val url = esc(shortcutUrl)
        return """<!doctype html>
<html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>SmsMail setup</title>
<style>
:root{color-scheme:light dark}
body{font:17px/1.45 -apple-system,system-ui,sans-serif;margin:0 auto;padding:20px;max-width:640px}
.ok{background:#e7f6ec;color:#14532d;padding:10px 12px;border-radius:10px}
@media (prefers-color-scheme:dark){.ok{background:#173a24;color:#c9f0d6}}
input{width:100%;box-sizing:border-box;font:15px ui-monospace,Menlo,monospace;padding:12px;border-radius:10px;border:1px solid #888}
.btn{display:inline-block;margin-top:8px;font:inherit;padding:12px 18px;border-radius:12px;border:0;background:#0a84ff;color:#fff;text-decoration:none}
li{margin:8px 0} h2{margin-top:28px}
</style></head><body>
<h1>SmsMail → iPhone</h1>
<p class="ok">✅ This iPhone can reach SmsMail. Messages waiting: $waiting</p>
<h2>1. Copy the Shortcut URL</h2>
<input id="u" readonly value="$url" onclick="this.setSelectionRange(0,this.value.length)">
<button class="btn" onclick="var u=document.getElementById('u');u.focus();u.setSelectionRange(0,u.value.length);document.execCommand('copy');this.textContent='Copied ✓'">Copy URL</button>
<h2>2. Add the shortcut</h2>
<a class="btn" href="$ICLOUD_SHORTCUT">Get the SmsMail shortcut</a>
${list("ol", steps.drop(1))}
<h2>3. Run it automatically</h2>
<p>${esc(automationIntro)}</p>
${list("ul", automations)}
<h2>Good to know</h2>
${list("ul", notes)}
</body></html>"""
    }
}
