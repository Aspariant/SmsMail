package app.smsmail;

/* compiled from: Store.kt */
public final class Store {
    private static final java.lang.Object LOCK = new java.lang.Object();
    public static final int MAX_PER_WINDOW = 30;
    public static final long WINDOW_MS = 600000;
    private final android.content.SharedPreferences prefs;

    public Store(android.content.Context context) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(context, "context");
        this.prefs = context.getApplicationContext().getSharedPreferences("smsmail", 0);
    }

    public final void save(java.lang.String sender, java.lang.String password, java.lang.String recipient) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(sender, "sender");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(recipient, "recipient");
        javax.crypto.SecretKey key = app.smsmail.KeyStoreKey.INSTANCE.get();
        android.content.SharedPreferences.Editor e = this.prefs.edit().putString("sender", app.smsmail.Crypto.INSTANCE.encryptString(key, sender)).putString("recipient", app.smsmail.Crypto.INSTANCE.encryptString(key, recipient));
        java.lang.String str = password;
        if (!(str == null || str.length() == 0)) {
            e.putString("password", app.smsmail.Crypto.INSTANCE.encryptString(key, password));
        }
        e.apply();
    }

    private final java.lang.String secret(java.lang.String name) {
        try {
            java.lang.String it = this.prefs.getString(name, null);
            if (it != null) {
                return app.smsmail.Crypto.INSTANCE.decryptString(app.smsmail.KeyStoreKey.INSTANCE.get(), it);
            }
            return null;
        } catch (java.lang.Exception e) {
            return null;
        }
    }

    public final java.lang.String sender() {
        return secret("sender");
    }

    public final java.lang.String recipient() {
        return secret("recipient");
    }

    public final boolean hasPassword() {
        return this.prefs.contains("password");
    }

    public final app.smsmail.Config config() {
        java.lang.String p;
        java.lang.String r;
        java.lang.String s = sender();
        if (s == null || (p = secret("password")) == null || (r = recipient()) == null) {
            return null;
        }
        return new app.smsmail.Config(s, p, r);
    }

    public final void success() {
        this.prefs.edit().putLong("last_ok", java.lang.System.currentTimeMillis()).apply();
    }

    public final void fail(java.lang.String reason) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(reason, "reason");
        this.prefs.edit().putLong("last_fail_time", java.lang.System.currentTimeMillis()).putString("last_fail", kotlin.text.StringsKt.take(reason, 700)).apply();
    }

    public final long lastSuccess() {
        return this.prefs.getLong("last_ok", 0L);
    }

    public final java.lang.String lastFailure() {
        return this.prefs.getString("last_fail", null);
    }

    public final long lastFailureTime() {
        return this.prefs.getLong("last_fail_time", 0L);
    }

    public final boolean tryAcquireSlot(long now) {
        synchronized (LOCK) {
            java.lang.String string = this.prefs.getString("sent_times", "");
            if (string == null) {
                string = "";
            }
            java.lang.Iterable $this$mapNotNull$iv = kotlin.text.StringsKt.split$default((java.lang.CharSequence) string, new java.lang.String[]{","}, false, 0, 6, (java.lang.Object) null);
            java.util.Collection destination$iv$iv = new java.util.ArrayList();
            for (java.lang.Object element$iv$iv$iv : $this$mapNotNull$iv) {
                java.lang.String it = (java.lang.String) element$iv$iv$iv;
                java.lang.Long longOrNull = kotlin.text.StringsKt.toLongOrNull(it);
                if (longOrNull != null) {
                    destination$iv$iv.add(longOrNull);
                }
            }
            java.lang.Iterable $this$filter$iv = (java.util.List) destination$iv$iv;
            java.util.Collection destination$iv$iv2 = new java.util.ArrayList();
            for (java.lang.Object element$iv$iv : $this$filter$iv) {
                long it2 = ((java.lang.Number) element$iv$iv).longValue();
                if (now - it2 < WINDOW_MS) {
                    destination$iv$iv2.add(element$iv$iv);
                }
            }
            java.util.List recent = (java.util.List) destination$iv$iv2;
            if (recent.size() >= 30) {
                return false;
            }
            this.prefs.edit().putString("sent_times", kotlin.collections.CollectionsKt.joinToString$default(kotlin.collections.CollectionsKt.plus((java.util.Collection<? extends java.lang.Long>) recent, java.lang.Long.valueOf(now)), ",", null, null, 0, null, null, 62, null)).apply();
            return true;
        }
    }
}
