package app.smsmail

import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.Date
import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Sends one plain-text email through the configured SMTP server (iCloud or Gmail).
 * Security settings here must not be loosened:
 *  - STARTTLS is REQUIRED (the send fails rather than going out unencrypted)
 *  - the server certificate chain AND hostname are verified
 */
object Mailer {
    private const val DEFAULT_PORT = 587

    /** Names in the certificate chain Android rejected, for diagnosis only. */
    @Volatile
    var lastChain: String? = null

    /**
     * Wraps the system trust manager. It NEVER accepts anything the system
     * rejects: it only notes the certificate names, then rethrows.
     */
    private class RecordingTrustManager(private val delegate: X509TrustManager) : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) =
            delegate.checkClientTrusted(chain, authType)

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            try {
                delegate.checkServerTrusted(chain, authType)
            } catch (e: CertificateException) {
                lastChain = chain.joinToString(" | ") {
                    "${cn(it.subjectX500Principal.name)} (issuer ${cn(it.issuerX500Principal.name)})"
                }
                throw e
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = delegate.acceptedIssuers

        private fun cn(name: String): String =
            Regex("CN=([^,]+)").find(name)?.groupValues?.get(1) ?: name.take(40)
    }

    private fun socketFactory(): javax.net.ssl.SSLSocketFactory {
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(null as KeyStore?)
        val system = tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
        val ctx = SSLContext.getInstance("TLS")
        ctx.init(null, arrayOf<TrustManager>(RecordingTrustManager(system)), null)
        return ctx.socketFactory
    }

    fun send(cfg: Config, subject: String, body: String) {
        val host = cfg.smtpHost()
        val port = cfg.smtpPort()

        lastChain = null
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port.toString())
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.starttls.required", "true")
            put("mail.smtp.ssl.checkserveridentity", "true")
            put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
            put("mail.smtp.ssl.socketFactory", socketFactory())
            put("mail.smtp.connectiontimeout", "20000")
            put("mail.smtp.timeout", "30000")
            put("mail.smtp.writetimeout", "30000")
        }
        val session = Session.getInstance(props)
        val msg = MimeMessage(session).apply {
            setFrom(InternetAddress(cfg.sender))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(cfg.recipient, true))
            // Strip control characters so a hostile sender name can't inject headers.
            setSubject(subject.replace(Regex("\\p{Cntrl}"), " "), "UTF-8")
            setText(body, "UTF-8")
            sentDate = Date()
        }
        val transport = session.getTransport("smtp")
        try {
            transport.connect(host, port, cfg.sender, cfg.password)
            transport.sendMessage(msg, msg.allRecipients)
        } finally {
            try { transport.close() } catch (e: Exception) { }
        }
    }
}