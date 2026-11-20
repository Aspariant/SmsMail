package app.smsmail

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.mail.AuthenticationFailedException

/** Sends one queued message. The file is deleted only after a successful send. */
class SendWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val name = inputData.getString(Queue.KEY_FILE) ?: return Result.failure()
        val file = Queue.file(applicationContext, name)
        if (!file.exists()) return Result.success() // already sent

        val store = Store(applicationContext)
        // LAN mode was selected after this job was scheduled: leave the file for
        // the iPhone to pick up.
        if (store.isLan()) return Result.success()

        val cfg = store.config()
        if (cfg == null) {
            // Permanent for now: message stays queued, "Retry queue" re-runs it.
            store.fail("Not configured (sender, password, recipient)")
            return Result.failure()
        }

        val item = Queue.read(applicationContext, name)
        if (item == null) {
            store.fail("Queued message unreadable")
            return Result.failure()
        }
        val (kind, number, time, text) = item

        // Over the limit: try again later with backoff. Never dropped.
        if (!store.tryAcquireSlot(System.currentTimeMillis())) return Result.retry()

        val dateObj = Date(time)
        val (subject, body) = if (kind == "CALL") {
            val timeOnly = SimpleDateFormat("HH:mm", Locale.getDefault()).format(dateObj)
            val dateTimeShort = SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()).format(dateObj)
            val caller = number.ifBlank { "Unknown" }

            val callSubject = "Incoming Call: $caller"
            val callBody = buildString {
                appendLine("Incoming Call:")
                appendLine("Number: $caller")
                appendLine("Date and time: $dateTimeShort")
                appendLine()
                appendLine("Number $caller")
                append("Called on $timeOnly")
            }
            callSubject to callBody
        } else {
            val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(dateObj)
            val smsBody = "From: $number\nDate: $stamp\n\n$text"
            "SMS from $number" to smsBody
        }

        return try {
            Mailer.send(cfg, subject, body)
            file.delete()
            store.success()
            Result.success()
        } catch (e: AuthenticationFailedException) {
            val providerName = if (cfg.receiver == "android") "Gmail" else "iCloud"
            // Wrong app-specific password: retrying would just lock things up.
            store.fail("Login rejected by $providerName (check sender and app-specific password)")
            Result.failure()
        } catch (e: Exception) {
            // Class name plus a short server message; never message text or credentials.
            store.fail("${e.javaClass.simpleName}: ${e.message ?: ""}")
            Result.retry()
        }
    }
}
