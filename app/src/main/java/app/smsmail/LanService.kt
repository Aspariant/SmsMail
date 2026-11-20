package app.smsmail

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder

/**
 * Keeps the LAN server running in the background. Android requires a visible
 * notification for this; it shows the address the iPhone connects to.
 * Runs only while the delivery method is LAN.
 */
class LanService : Service() {

    companion object {
        const val PORT = 8765
        private const val CHANNEL = "lan_server"
        private const val NOTIFICATION_ID = 1

        /** Shown on the status screen. */
        @Volatile var state: String = "stopped"
            private set

        fun start(ctx: Context) {
            try {
                ctx.startForegroundService(Intent(ctx, LanService::class.java))
                if (state == "stopped") state = "starting"
            } catch (e: Exception) {
                // Android blocks starting it from the background unless the app is
                // exempt from battery optimization.
                Store(ctx).fail("Could not start LAN server (${e.javaClass.simpleName}). Open the app and allow battery optimization exemption.")
            }
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, LanService::class.java))
        }
    }

    private var server: LanServer? = null
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "LAN server", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shown while SmsMail waits for your iPhone on the local network"
            }
        )
        goForeground()

        // Keeps Wi-Fi from sleeping, so the iPhone can always reach this phone.
        @Suppress("DEPRECATION")
        wifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "smsmail:lan")
            .apply { setReferenceCounted(false); acquire() }

        server = try {
            LanServer(applicationContext, PORT).also { it.start() }
        } catch (e: Exception) {
            state = "error: port $PORT unavailable (${e.javaClass.simpleName})"
            Store(this).fail("LAN server could not listen on port $PORT: ${e.javaClass.simpleName}")
            null
        }
        if (server != null) state = "running"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Store(this).isLan()) {
            stopSelf()
            return START_NOT_STICKY
        }
        // The IP may have changed since the service started.
        goForeground()
        return START_STICKY
    }

    override fun onDestroy() {
        server?.stop()
        server = null
        wifiLock?.let { if (it.isHeld) it.release() }
        state = "stopped"
        super.onDestroy()
    }

    private fun goForeground() {
        val n = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, n)
        }
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val where = Net.baseUrl()?.let { "Listening on $it" } ?: "Waiting for Wi-Fi or hotspot"
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Forwarding to iPhone over local network")
            .setContentText(where)
            .setContentIntent(open)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
    }
}
