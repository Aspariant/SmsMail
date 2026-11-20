package app.smsmail

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM helpers. Used ONLY for data at rest on the phone (the queue and
 * the saved credentials). Emails themselves are plain text, by design.
 *
 * Pure javax.crypto, so it can be unit-tested on a normal JVM with any AES key.
 * Output layout: 12-byte IV, then ciphertext + 16-byte GCM tag.
 */
object Crypto {
    private const val IV_LEN = 12
    private const val TAG_BITS = 128
    private const val TRANSFORM = "AES/GCM/NoPadding"

    fun encrypt(key: SecretKey, plain: ByteArray): ByteArray {
        val c = Cipher.getInstance(TRANSFORM)
        // No IV is passed in: the cipher generates a fresh random one itself.
        // (Android Keystore refuses caller-supplied IVs for encryption.)
        c.init(Cipher.ENCRYPT_MODE, key)
        return c.iv + c.doFinal(plain)
    }

    fun decrypt(key: SecretKey, blob: ByteArray): ByteArray {
        require(blob.size > IV_LEN) { "ciphertext too short" }
        val c = Cipher.getInstance(TRANSFORM)
        c.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, blob, 0, IV_LEN))
        // Throws if the data was modified or the key is wrong.
        return c.doFinal(blob, IV_LEN, blob.size - IV_LEN)
    }

    fun encryptString(key: SecretKey, s: String): String =
        Base64.getEncoder().encodeToString(encrypt(key, s.toByteArray(Charsets.UTF_8)))

    fun decryptString(key: SecretKey, s: String): String =
        String(decrypt(key, Base64.getDecoder().decode(s)), Charsets.UTF_8)
}
