package app.smsmail;

/* compiled from: Crypto.kt */
public final class Crypto {
    public static final app.smsmail.Crypto INSTANCE = new app.smsmail.Crypto();
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;
    private static final java.lang.String TRANSFORM = "AES/GCM/NoPadding";

    private Crypto() {
    }

    public final byte[] encrypt(javax.crypto.SecretKey key, byte[] plain) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(key, "key");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(plain, "plain");
        javax.crypto.Cipher c = javax.crypto.Cipher.getInstance(TRANSFORM);
        c.init(1, key);
        byte[] iv = c.getIV();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(iv, "getIV(...)");
        byte[] doFinal = c.doFinal(plain);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(doFinal, "doFinal(...)");
        return kotlin.collections.ArraysKt.plus(iv, doFinal);
    }

    public final byte[] decrypt(javax.crypto.SecretKey key, byte[] blob) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(key, "key");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(blob, "blob");
        if (!(blob.length > 12)) {
            throw new java.lang.IllegalArgumentException("ciphertext too short".toString());
        }
        javax.crypto.Cipher c = javax.crypto.Cipher.getInstance(TRANSFORM);
        c.init(2, key, new javax.crypto.spec.GCMParameterSpec(128, blob, 0, 12));
        byte[] doFinal = c.doFinal(blob, 12, blob.length - 12);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(doFinal, "doFinal(...)");
        return doFinal;
    }

    public final java.lang.String encryptString(javax.crypto.SecretKey key, java.lang.String s) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(key, "key");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(s, "s");
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        byte[] bytes = s.getBytes(kotlin.text.Charsets.UTF_8);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
        java.lang.String encodeToString = encoder.encodeToString(encrypt(key, bytes));
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(encodeToString, "encodeToString(...)");
        return encodeToString;
    }

    public final java.lang.String decryptString(javax.crypto.SecretKey key, java.lang.String s) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(key, "key");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(s, "s");
        byte[] decode = java.util.Base64.getDecoder().decode(s);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(decode, "decode(...)");
        return new java.lang.String(decrypt(key, decode), kotlin.text.Charsets.UTF_8);
    }
}
