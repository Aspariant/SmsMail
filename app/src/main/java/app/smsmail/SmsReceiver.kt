package app.smsmail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

/**
 * Receives SMS from the system, encrypts each message into the on-disk queue
 * and schedules sending. Multipart messages arrive as several parts in one
 * intent; they are joined per sender here.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (parts.isNullOrEmpty()) return

        val bySender = LinkedHashMap<String, StringBuilder>()
        for (p in parts) {
            bySender.getOrPut(p.originatingAddress ?: "unknown") { StringBuilder() }
                .append(p.messageBody ?: "")
        }
        val time = parts[0].timestampMillis

        // Keystore + file write can take a moment; keep the receiver alive for it.
        val pending = goAsync()
        Thread {
            try {
                for ((number, text) in bySender) {
                    try {
                        Queue.add(context.applicationContext, "SMS", number, time, text.toString())
                    } catch (e: Exception) {
                        Store(context).fail("Could not queue an SMS: ${e.javaClass.simpleName}")
                    }
                }
            } finally {
                pending.finish()
            }
        }.start()
    }
}
