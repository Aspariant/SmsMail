package app.smsmail

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * One AES-256 key that lives inside the Android Keystore. The key material is
 * non-exportable: other apps (and a file-level copy of this app's data) cannot
 * read it. Code running inside this app can still ask the Keystore to use it.
 */
object KeyStoreKey {
    private const val ALIAS = "smsmail_aes_key"
    private const val PROVIDER = "AndroidKeyStore"

    @Synchronized
    fun get(): SecretKey {
        val ks = KeyStore.getInstance(PROVIDER).apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }

        val spec = KeyGenParameterSpec.Builder(
            ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        gen.init(spec)
        return gen.generateKey()
    }
}
