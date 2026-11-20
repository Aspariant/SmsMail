package app.smsmail

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Pending messages live as one encrypted file each in filesDir/queue.
 * WorkManager only ever sees the file NAME, because its own database is plain
 * SQLite and anything placed in a Work's input data would sit there unencrypted.
 *
 * The same queue feeds both delivery methods:
 *  - Email: one WorkManager job per file (SendWorker).
 *  - LAN: the iPhone Shortcut pulls files from LanServer, oldest first.
 * A file is deleted only once its message has been delivered.
 */
object Queue {
    const val KEY_FILE = "file"
    private const val EMAIL_WORK_TAG = "email"

    /** One queued SMS or call alert. */
    data class Item(val kind: String, val number: String, val time: Long, val text: String) {
        val isCall get() = kind == "CALL"

        /** Short notification title, e.g. "SMS from +966…" or "Incoming call: +966…". */
        fun title(): String =
            if (isCall) "Incoming call: ${number.ifBlank { "Unknown" }}" else "SMS from $number"

        /** Notification body: the SMS text, or the time the call came in. */
        fun body(): String =
            if (isCall) "Called at " + SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()).format(Date(time))
            else text
    }

    private fun dir(ctx: Context) = File(ctx.filesDir, "queue").apply { mkdirs() }

    fun file(ctx: Context, name: String) = File(dir(ctx), name)

    /** Oldest first: new names start with a zero-padded timestamp. */
    fun names(ctx: Context): List<String> =
        dir(ctx).list()?.filter { it.endsWith(".enc") }?.sorted() ?: emptyList()

    /** Encrypts first, then writes. Plaintext never reaches disk. */
    fun add(ctx: Context, kind: String, number: String, timeMillis: Long, text: String) {
        val payload = "$kind\n$number\n$timeMillis\n$text".toByteArray(Charsets.UTF_8)
        val name = "%013d-%s.enc".format(System.currentTimeMillis(), UUID.randomUUID())
        val tmp = File(dir(ctx), "$name.tmp")
        tmp.writeBytes(Crypto.encrypt(KeyStoreKey.get(), payload))
        // Rename so a half-written file is never picked up as a message.
        check(tmp.renameTo(File(dir(ctx), name))) { "rename failed" }
        dispatch(ctx, name)
    }

    /**
     * Decrypts one queued file. Returns null if it is missing or unreadable.
     * Understands both the current "kind\nnumber\ntime\ntext" layout and the
     * older "number\ntime\ntext" one.
     */
    fun read(ctx: Context, name: String): Item? {
        val f = file(ctx, name)
        if (!f.exists()) return null
        val parts = try {
            String(Crypto.decrypt(KeyStoreKey.get(), f.readBytes()), Charsets.UTF_8).split("\n")
        } catch (e: Exception) {
            return null
        }
        if (parts.size < 3) return null
        val hasKind = parts.size >= 4 && (parts[0] == "SMS" || parts[0] == "CALL")
        return if (hasKind) {
            Item(parts[0], parts[1], parts[2].toLongOrNull() ?: System.currentTimeMillis(),
                parts.subList(3, parts.size).joinToString("\n"))
        } else {
            Item("SMS", parts[0], parts[1].toLongOrNull() ?: System.currentTimeMillis(),
                parts.subList(2, parts.size).joinToString("\n"))
        }
    }

    fun delete(ctx: Context, name: String) {
        file(ctx, name).delete()
    }

    /** Hands a new message to whichever delivery method is selected. */
    private fun dispatch(ctx: Context, name: String) {
        if (Store(ctx).isLan()) {
            LanService.start(ctx)
            LanHub.signal()
        } else {
            enqueueEmail(ctx, name)
        }
    }

    /**
     * Re-applies the selected delivery method to everything still on disk
     * (after saving settings, on boot, or from "Retry queued messages").
     */
    fun resume(ctx: Context) {
        if (Store(ctx).isLan()) {
            // Email jobs that are still scheduled would just exit; drop them.
            WorkManager.getInstance(ctx.applicationContext).cancelAllWorkByTag(EMAIL_WORK_TAG)
            LanService.start(ctx)
            LanHub.signal()
        } else {
            LanService.stop(ctx)
            names(ctx).forEach { enqueueEmail(ctx, it) }
        }
    }

    private fun enqueueEmail(ctx: Context, name: String) {
        val req = OneTimeWorkRequestBuilder<SendWorker>()
            .setInputData(workDataOf(KEY_FILE to name))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(EMAIL_WORK_TAG)
            .build()
        // KEEP: if this message already has live work, don't duplicate it.
        WorkManager.getInstance(ctx.applicationContext)
            .enqueueUniqueWork(name, ExistingWorkPolicy.KEEP, req)
    }
}
