package app.smsmail

import android.content.Context

/**
 * Hands queued messages to iPhone Shortcut requests, one at a time, without
 * losing any of them.
 *
 * Delivery is confirmed implicitly: the Shortcut runs in a loop, so when it
 * comes back for the next message, everything it was given before must have
 * arrived and is deleted. If the Shortcut never comes back (iOS stopped it, or
 * Wi-Fi dropped mid-reply), the message is offered again after ACK_TIMEOUT_MS.
 * Worst case is a duplicate notification, never a lost one.
 */
object LanHub {
    private const val ACK_TIMEOUT_MS = 2 * 60 * 1000L

    private val lock = Object()
    /** Being written to a client right now. */
    private val inFlight = HashSet<String>()
    /** Written to a client, waiting for that client's next poll. Name -> time sent. */
    private val awaitingAck = LinkedHashMap<String, Long>()

    class Claim(val name: String, val item: Queue.Item)

    /** What a waiting /next request ends with. */
    sealed class Outcome {
        class Message(val claim: Claim) : Outcome()
        object Nothing : Outcome()
        /** A newer copy of the Shortcut started polling; this one should stop. */
        object Superseded : Outcome()
    }

    /**
     * Increases every time a /next request comes in. One running Shortcut only
     * ever has one request open, so a request that is still waiting when a new
     * one arrives belongs to an older copy (started by another automation).
     * The newest copy wins and the older one is told to stop, so copies don't
     * pile up on the iPhone.
     */
    private var generation = 0L

    /** Registers a new /next request and returns its generation. */
    fun newPoll(): Long = synchronized(lock) {
        generation++
        lock.notifyAll() // wake older waiting requests so they notice
        generation
    }

    /** Wakes any request that is waiting for a new message. */
    fun signal() = synchronized(lock) { lock.notifyAll() }

    /** The Shortcut came back for more, so it received everything sent before. */
    fun confirmDelivered(ctx: Context) {
        val confirmed = synchronized(lock) {
            val names = awaitingAck.keys.toList()
            awaitingAck.clear()
            names
        }
        if (confirmed.isEmpty()) return
        confirmed.forEach { Queue.delete(ctx, it) }
        Store(ctx).success()
    }

    /**
     * Takes the oldest undelivered message, waiting up to [waitMs] for one to
     * arrive. Gives up early when [stillConnected] says the client left, and
     * when a newer request (generation [gen] is no longer current) takes over.
     */
    fun await(ctx: Context, gen: Long, waitMs: Long, stillConnected: () -> Boolean): Outcome {
        val deadline = System.currentTimeMillis() + waitMs
        while (true) {
            synchronized(lock) {
                if (gen != generation) return Outcome.Superseded
                val now = System.currentTimeMillis()
                awaitingAck.entries.removeAll { now - it.value > ACK_TIMEOUT_MS }

                for (name in Queue.names(ctx)) {
                    if (name in inFlight || name in awaitingAck) continue
                    val item = Queue.read(ctx, name)
                    if (item == null) {
                        Queue.delete(ctx, name)
                        Store(ctx).fail("Dropped an unreadable queued message")
                        continue
                    }
                    inFlight += name
                    return Outcome.Message(Claim(name, item))
                }

                val left = deadline - now
                if (left <= 0) return Outcome.Nothing
                lock.wait(minOf(left, 1000L))
            }
            if (!stillConnected()) return Outcome.Nothing
        }
    }

    /** The reply was written; wait for the client's next poll to confirm it. */
    fun sent(claim: Claim) = synchronized(lock) {
        inFlight -= claim.name
        awaitingAck[claim.name] = System.currentTimeMillis()
    }

    /** The reply could not be written; offer the message again. */
    fun failed(claim: Claim) = synchronized(lock) {
        inFlight -= claim.name
        lock.notifyAll()
    }
}
