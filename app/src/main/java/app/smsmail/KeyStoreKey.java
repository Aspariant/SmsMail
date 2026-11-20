package app.smsmail;

/* compiled from: KeyStoreKey.kt */
public final class KeyStoreKey {
    private static final java.lang.String ALIAS = "smsmail_aes_key";
    public static final app.smsmail.KeyStoreKey INSTANCE = new app.smsmail.KeyStoreKey();
    private static final java.lang.String PROVIDER = "AndroidKeyStore";

    private KeyStoreKey() {
    }

    public final synchronized javax.crypto.SecretKey get() {
        java.security.KeyStore ks = java.security.KeyStore.getInstance(PROVIDER);
        ks.load(null);
        java.security.Key key = ks.getKey(ALIAS, null);
        javax.crypto.SecretKey secretKey = key instanceof javax.crypto.SecretKey ? (javax.crypto.SecretKey) key : null;
        if (secretKey != null) {
            javax.crypto.SecretKey it = secretKey;
            return it;
        }
        android.security.keystore.KeyGenParameterSpec spec = new android.security.keystore.KeyGenParameterSpec.Builder(ALIAS, 3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setKeySize(256).build();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(spec, "build(...)");
        javax.crypto.KeyGenerator gen = javax.crypto.KeyGenerator.getInstance("AES", PROVIDER);
        gen.init(spec);
        javax.crypto.SecretKey generateKey = gen.generateKey();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(generateKey, "generateKey(...)");
        return generateKey;
    }
}
