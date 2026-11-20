package app.smsmail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * After a reboot or an app update: restart the LAN server (LAN mode) or
 * reschedule anything still waiting to be emailed (email mode).
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                try {
                    Queue.resume(context.applicationContext)
                } catch (e: Exception) {
                    Store(context).fail("Could not resume after restart: ${e.javaClass.simpleName}")
                }
            }
        }
    }
}
