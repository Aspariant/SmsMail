package app.smsmail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.security.GeneralSecurityException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/** Runs on a normal JVM: the Keystore is swapped for an ordinary AES key. */
class CryptoTest {
    private fun newKey(): SecretKey =
        KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    @Test
    fun roundTrip() {
        val key = newKey()
        val text = "Your code is 123456 \u00e9\u4e2d\n(line two)"
        assertEquals(text, Crypto.decryptString(key, Crypto.encryptString(key, text)))
    }

    @Test
    fun ivIsFreshEachTime() {
        val key = newKey()
        val a = Crypto.encryptString(key, "same")
        val b = Crypto.encryptString(key, "same")
        assertFalse(a == b)
    }

    @Test(expected = GeneralSecurityException::class)
    fun wrongKeyFails() {
        val blob = Crypto.encrypt(newKey(), "secret".toByteArray())
        Crypto.decrypt(newKey(), blob)
    }

    @Test(expected = GeneralSecurityException::class)
    fun tamperedDataFails() {
        val key = newKey()
        val blob = Crypto.encrypt(key, "secret".toByteArray())
        blob[blob.size - 1] = (blob[blob.size - 1].toInt() xor 1).toByte()
        Crypto.decrypt(key, blob)
    }
}
