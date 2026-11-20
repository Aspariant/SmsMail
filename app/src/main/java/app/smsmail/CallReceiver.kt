package app.smsmail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

/**
 * Queues an incoming call alert immediately when the phone starts ringing.
 * Safely handles missing permissions and intent extras access to prevent crashes.
 */
class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

            val state = try {
                intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            } catch (_: Exception) {
                null
            }

            // Only process when the phone is ringing
            if (state != TelephonyManager.EXTRA_STATE_RINGING) return

            val appContext = context.applicationContext
            val store = Store(appContext)
            if (!store.isCallAlertsEnabled()) return

            @Suppress("DEPRECATION")
            val rawNumber = try {
                intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            } catch (_: Exception) {
                null
            }

            // With call-log permission Android sends RINGING twice: first without the
            // number extra, then with it ("" for hidden numbers). Wait for the second.
            val hasCallLog = appContext.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            @Suppress("DEPRECATION")
            if (hasCallLog && !intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) return

            val now = System.currentTimeMillis()
            val callerNumber = rawNumber?.takeIf { it.isNotBlank() } ?: "Unknown"

            // Android can announce the same ring event multiple times (e.g. per SIM or as caller ID updates).
            // Deduplicate within 5 seconds for the same caller/ring event.
            synchronized(LOCK) {
                if ((now - lastRing < 5000) && (callerNumber == lastNumber)) return
                // If previous ring was within 5s with "Unknown" and now we received a real number, update and proceed
                if ((now - lastRing < 5000) && (lastNumber == "Unknown") && (callerNumber != "Unknown")) {
                    // proceed to queue alert with the resolved caller number
                } else if (now - lastRing < 5000) {
                    return
                }
                lastRing = now
                lastNumber = callerNumber
            }

            val pending = goAsync()
            Thread {
                try {
                    Queue.add(appContext, "CALL", callerNumber, now, "")
                } catch (e: Exception) {
                    store.fail("Could not queue a call alert: ${e.javaClass.simpleName}")
                } finally {
                    pending.finish()
                }
            }.start()
        } catch (e: Throwable) {
            // Prevent any crash when phone state changes or call is declined/ended
            try {
                Store(context.applicationContext).fail("CallReceiver error: ${e.javaClass.simpleName}")
            } catch (_: Exception) {}
        }
    }

    companion object {
        private val LOCK = Any()
        private var lastRing = 0L
        private var lastNumber: String? = null
    }
}
