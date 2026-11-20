package app.smsmail

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.URLDecoder
import java.security.MessageDigest
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * A tiny HTTP server for the local network. The iPhone Shortcut long-polls it:
 * each request waits up to [DEFAULT_WAIT_S] seconds and returns as soon as a
 * message is queued, which makes it feel like a push.
 *
 * Endpoints (all except "/" need ?key=… or an X-Key header):
 *   GET /next        JSON: {"text":"title\nbody", …} for a message, {} if nothing
 *                    came in, {"stop":"yes"} if a newer copy of the Shortcut took over
 *   GET /next.txt    plain text "title\nbody", or an empty reply (for curl/scripts;
 *                    iOS Shortcuts treats an empty reply as "has a value")
 *   GET /ping        {"ok":true,"waiting":N}, for testing from a browser
 *   GET /setup       the setup page the QR code opens on the iPhone
 */
class LanServer(private val ctx: Context, private val port: Int) {

    companion object {
        private const val DEFAULT_WAIT_S = 25 // stays under the Shortcuts request timeout
        private const val MAX_WAIT_S = 50
        private const val MAX_LINE = 4096
        private const val MAX_HEADERS = 50
    }

    private class Request(val path: String, val query: Map<String, String>, val headers: Map<String, String>)

    @Volatile private var server: ServerSocket? = null
    private val pool = ThreadPoolExecutor(0, 16, 30, TimeUnit.SECONDS, SynchronousQueue())

    /** Throws (for example BindException when the port is taken) if it cannot listen. */
    fun start() {
        val s = ServerSocket()
        s.reuseAddress = true
        s.bind(InetSocketAddress(port), 32)
        server = s
        Thread({ acceptLoop(s) }, "smsmail-lan-accept").apply { isDaemon = true }.start()
    }

    fun stop() {
        try { server?.close() } catch (_: IOException) { }
        server = null
        pool.shutdownNow()
    }

    private fun acceptLoop(s: ServerSocket) {
        while (!s.isClosed) {
            val client = try { s.accept() } catch (_: IOException) { break }
            try {
                pool.execute { handle(client) }
            } catch (_: RejectedExecutionException) {
                try { client.close() } catch (_: IOException) { }
            }
        }
    }

    private fun handle(sock: Socket) {
        try {
            sock.use {
                it.soTimeout = 10_000
                val req = readRequest(it.getInputStream()) ?: return
                route(it, req)
            }
        } catch (_: IOException) {
            // Client went away; nothing to do.
        } catch (e: Exception) {
            Store(ctx).fail("LAN server: ${e.javaClass.simpleName}")
        }
    }

    private fun route(sock: Socket, req: Request) {
        if (req.path == "/") {
            return reply(sock, 200, "text/plain", "SmsMail is running. Scan the QR code in the app's Settings to set up your iPhone.\n")
        }
        if (req.path !in setOf("/next", "/next.json", "/next.txt", "/ping", "/setup")) {
            return reply(sock, 404, "text/plain", "Not found\n")
        }
        val store = Store(ctx)
        if (!keyMatches(req.query["key"] ?: req.headers["x-key"], store.lanKey())) {
            Thread.sleep(1000) // slows down guessing
            return reply(sock, 401, "text/plain", "Wrong or missing key. Copy the URL again from SmsMail.\n")
        }

        when (req.path) {
            "/ping" -> reply(sock, 200, "application/json",
                JSONObject().put("ok", true).put("waiting", Queue.names(ctx).size).toString())

            "/setup" -> {
                val base = hostBase(req.headers["host"]) ?: Net.baseUrl() ?: "http://<phone-ip>:${LanService.PORT}"
                reply(sock, 200, "text/html",
                    ShortcutGuide.html(ShortcutGuide.shortcutUrl(base, store.lanKey()), Queue.names(ctx).size))
            }

            else -> next(sock, req, json = req.path != "/next.txt", lan = store.isLan())
        }
    }

    private fun next(sock: Socket, req: Request, json: Boolean, lan: Boolean) {
        val type = if (json) "application/json" else "text/plain"
        val empty = if (json) "{}" else ""
        if (!lan) return reply(sock, 200, type, empty) // email mode: the iPhone gets nothing

        val gen = LanHub.newPoll()
        LanHub.confirmDelivered(ctx)
        val waitS = (req.query["wait"]?.toIntOrNull() ?: DEFAULT_WAIT_S).coerceIn(0, MAX_WAIT_S)
        val claim = when (val outcome = LanHub.await(ctx, gen, waitS * 1000L) { isConnected(sock) }) {
            is LanHub.Outcome.Message -> outcome.claim
            LanHub.Outcome.Nothing -> return reply(sock, 200, type, empty)
            LanHub.Outcome.Superseded ->
                return reply(sock, 200, type, if (json) JSONObject().put("stop", "yes").toString() else empty)
        }

        val item = claim.item
        val text = item.title() + "\n" + item.body()
        val body = if (json) {
            JSONObject()
                .put("text", text)
                .put("id", claim.name.removeSuffix(".enc"))
                .put("kind", if (item.isCall) "call" else "sms")
                .put("from", item.number)
                .put("time", item.time)
                .put("title", item.title())
                .put("body", item.body())
                .toString()
        } else {
            text
        }
        try {
            reply(sock, 200, type, body)
            LanHub.sent(claim)
        } catch (e: IOException) {
            LanHub.failed(claim)
            throw e
        }
    }

    /** True while the client keeps the connection open (it sends nothing more after the request). */
    private fun isConnected(sock: Socket): Boolean {
        val old = sock.soTimeout
        return try {
            sock.soTimeout = 1
            sock.getInputStream().read() != -1
        } catch (_: SocketTimeoutException) {
            true
        } catch (_: IOException) {
            false
        } finally {
            try { sock.soTimeout = old } catch (_: IOException) { }
        }
    }

    private fun keyMatches(given: String?, expected: String): Boolean =
        given != null && MessageDigest.isEqual(given.trim().toByteArray(), expected.toByteArray())

    /** Builds "http://host:port" from the Host header the iPhone used, if it looks sane. */
    private fun hostBase(host: String?): String? =
        host?.takeIf { it.matches(Regex("^[A-Za-z0-9.\\-]+(:[0-9]{1,5})?$")) }?.let { "http://$it" }

    private fun reply(sock: Socket, code: Int, type: String, body: String) {
        val status = when (code) { 200 -> "OK"; 401 -> "Unauthorized"; 404 -> "Not Found"; else -> "Error" }
        val bytes = body.toByteArray(Charsets.UTF_8)
        val head = "HTTP/1.1 $code $status\r\n" +
            "Content-Type: $type; charset=utf-8\r\n" +
            "Content-Length: ${bytes.size}\r\n" +
            "Cache-Control: no-store\r\n" +
            "Connection: close\r\n\r\n"
        val out = sock.getOutputStream()
        out.write(head.toByteArray(Charsets.ISO_8859_1))
        out.write(bytes)
        out.flush()
    }

    private fun readRequest(input: InputStream): Request? {
        val requestLine = readLine(input) ?: return null
        val parts = requestLine.split(" ")
        if (parts.size < 2) return null
        val headers = HashMap<String, String>()
        repeat(MAX_HEADERS) {
            val line = readLine(input) ?: return null
            if (line.isEmpty()) {
                val target = parts[1]
                val path = target.substringBefore('?')
                val query = if ('?' in target) parseQuery(target.substringAfter('?')) else emptyMap()
                return Request(path, query, headers)
            }
            val colon = line.indexOf(':')
            if (colon > 0) headers[line.substring(0, colon).trim().lowercase()] = line.substring(colon + 1).trim()
        }
        return null
    }

    private fun parseQuery(q: String): Map<String, String> =
        q.split('&').filter { it.isNotEmpty() }.associate {
            val k = it.substringBefore('=')
            val v = it.substringAfter('=', "")
            dec(k) to dec(v)
        }

    private fun dec(s: String) = try { URLDecoder.decode(s, "UTF-8") } catch (_: Exception) { s }

    private fun readLine(input: InputStream): String? {
        val sb = StringBuilder()
        while (true) {
            val b = input.read()
            if (b < 0) return if (sb.isEmpty()) null else sb.toString()
            if (b == '\n'.code) return sb.toString().trimEnd('\r')
            if (sb.length >= MAX_LINE) throw IOException("line too long")
            sb.append(b.toChar())
        }
    }
}
