package app.smsmail;

/* compiled from: SendWorker.kt */
public final class SendWorker extends androidx.work.Worker {
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SendWorker(android.content.Context ctx, androidx.work.WorkerParameters params) {
        super(ctx, params);
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ctx, "ctx");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(params, "params");
    }

    /* JADX WARN: Code restructure failed: missing block: B:54:0x0184, code lost:
    
        if (r0 != null) goto L50;
     */
    @Override // androidx.work.Worker
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public androidx.work.ListenableWorker.Result doWork() {
        java.lang.String str;
        java.lang.String name = getInputData().getString(app.smsmail.Queue.KEY_FILE);
        if (name == null) {
            androidx.work.ListenableWorker.Result failure = androidx.work.ListenableWorker.Result.failure();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(failure, "failure(...)");
            return failure;
        }
        app.smsmail.Queue queue = app.smsmail.Queue.INSTANCE;
        android.content.Context applicationContext = getApplicationContext();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(applicationContext, "getApplicationContext(...)");
        java.io.File file = queue.file(applicationContext, name);
        if (!file.exists()) {
            androidx.work.ListenableWorker.Result success = androidx.work.ListenableWorker.Result.success();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(success, "success(...)");
            return success;
        }
        android.content.Context applicationContext2 = getApplicationContext();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(applicationContext2, "getApplicationContext(...)");
        app.smsmail.Store store = new app.smsmail.Store(applicationContext2);
        app.smsmail.Config cfg = store.config();
        if (cfg == null) {
            store.fail("Not configured (sender, password, recipient)");
            androidx.work.ListenableWorker.Result failure2 = androidx.work.ListenableWorker.Result.failure();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(failure2, "failure(...)");
            return failure2;
        }
        try {
            java.util.List parts = kotlin.text.StringsKt.split$default((java.lang.CharSequence) new java.lang.String(app.smsmail.Crypto.INSTANCE.decrypt(app.smsmail.KeyStoreKey.INSTANCE.get(), kotlin.io.FilesKt.readBytes(file)), kotlin.text.Charsets.UTF_8), new java.lang.String[]{"\n"}, false, 3, 2, (java.lang.Object) null);
            if (parts.size() < 3) {
                androidx.work.ListenableWorker.Result failure3 = androidx.work.ListenableWorker.Result.failure();
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(failure3, "failure(...)");
                return failure3;
            }
            java.lang.String number = (java.lang.String) parts.get(0);
            java.lang.Long longOrNull = kotlin.text.StringsKt.toLongOrNull((java.lang.String) parts.get(1));
            long time = longOrNull != null ? longOrNull.longValue() : java.lang.System.currentTimeMillis();
            java.lang.String text = (java.lang.String) parts.get(2);
            if (!store.tryAcquireSlot(java.lang.System.currentTimeMillis())) {
                androidx.work.ListenableWorker.Result retry = androidx.work.ListenableWorker.Result.retry();
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(retry, "retry(...)");
                return retry;
            }
            java.lang.String stamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", java.util.Locale.US).format(new java.util.Date(time));
            java.lang.String body = "From: " + number + "\nDate: " + stamp + "\n\n" + text;
            try {
                try {
                    try {
                        app.smsmail.Mailer.INSTANCE.send(cfg, "SMS from " + number, body);
                        file.delete();
                        store.success();
                        return androidx.work.ListenableWorker.Result.success();
                    } catch (javax.mail.AuthenticationFailedException e) {
                        store.fail("Login rejected by iCloud (check sender and app-specific password)");
                        return androidx.work.ListenableWorker.Result.failure();
                    }
                } catch (java.lang.Exception e2) {
                    java.lang.Throwable root = e2;
                    while (root.getCause() != null && root.getCause() != root) {
                        java.lang.Throwable cause = root.getCause();
                        kotlin.jvm.internal.Intrinsics.checkNotNull(cause);
                        root = cause;
                    }
                    java.lang.String simpleName = e2.getClass().getSimpleName();
                    java.lang.String simpleName2 = root.getClass().getSimpleName();
                    java.lang.String message = root.getMessage();
                    if (message == null) {
                        message = "";
                    }
                    java.lang.String it = app.smsmail.Mailer.INSTANCE.getLastChain();
                    if (it != null) {
                        str = " | chain: " + it;
                    }
                    str = "";
                    store.fail(simpleName + " | cause: " + simpleName2 + ": " + message + str);
                    return androidx.work.ListenableWorker.Result.retry();
                }
            } catch (javax.mail.AuthenticationFailedException e3) {
            }
        } catch (java.lang.Exception e4) {
            store.fail("Queued message unreadable: " + e4.getClass().getSimpleName());
            androidx.work.ListenableWorker.Result failure4 = androidx.work.ListenableWorker.Result.failure();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(failure4, "failure(...)");
            return failure4;
        }
    }
}
