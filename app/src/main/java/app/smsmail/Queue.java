package app.smsmail;

/* compiled from: Queue.kt */
public final class Queue {
    public static final app.smsmail.Queue INSTANCE = new app.smsmail.Queue();
    public static final java.lang.String KEY_FILE = "file";

    private Queue() {
    }

    private final java.io.File dir(android.content.Context ctx) {
        java.io.File $this$dir_u24lambda_u240 = new java.io.File(ctx.getFilesDir(), "queue");
        $this$dir_u24lambda_u240.mkdirs();
        return $this$dir_u24lambda_u240;
    }

    public final java.io.File file(android.content.Context ctx, java.lang.String name) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ctx, "ctx");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        return new java.io.File(dir(ctx), name);
    }

    public final java.util.List<java.lang.String> names(android.content.Context ctx) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ctx, "ctx");
        java.lang.String[] list = dir(ctx).list();
        if (list != null) {
            java.util.Collection destination$iv$iv = new java.util.ArrayList();
            for (java.lang.String str : list) {
                kotlin.jvm.internal.Intrinsics.checkNotNull(str);
                if (kotlin.text.StringsKt.endsWith$default(str, ".enc", false, 2, (java.lang.Object) null)) {
                    destination$iv$iv.add(str);
                }
            }
            java.util.List<java.lang.String> sorted = kotlin.collections.CollectionsKt.sorted((java.util.List) destination$iv$iv);
            if (sorted != null) {
                return sorted;
            }
        }
        return kotlin.collections.CollectionsKt.emptyList();
    }

    public final void add(android.content.Context ctx, java.lang.String number, long timeMillis, java.lang.String text) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ctx, "ctx");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(number, "number");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(text, "text");
        byte[] payload = (number + "\n" + timeMillis + "\n" + text).getBytes(kotlin.text.Charsets.UTF_8);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(payload, "getBytes(...)");
        java.lang.String name = java.util.UUID.randomUUID() + ".enc";
        java.io.File tmp = new java.io.File(dir(ctx), name + ".tmp");
        kotlin.io.FilesKt.writeBytes(tmp, app.smsmail.Crypto.INSTANCE.encrypt(app.smsmail.KeyStoreKey.INSTANCE.get(), payload));
        if (!tmp.renameTo(new java.io.File(dir(ctx), name))) {
            throw new java.lang.IllegalStateException("rename failed".toString());
        }
        enqueue(ctx, name);
    }

    public final void enqueueAll(android.content.Context ctx) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ctx, "ctx");
        java.lang.Iterable $this$forEach$iv = names(ctx);
        for (java.lang.Object element$iv : $this$forEach$iv) {
            java.lang.String it = (java.lang.String) element$iv;
            INSTANCE.enqueue(ctx, it);
        }
    }

    private final void enqueue(android.content.Context ctx, java.lang.String name) {
        androidx.work.OneTimeWorkRequest.Builder builder = new androidx.work.OneTimeWorkRequest.Builder(app.smsmail.SendWorker.class);
        kotlin.Pair[] pairs$iv = {kotlin.TuplesKt.to(KEY_FILE, name)};
        androidx.work.Data.Builder dataBuilder$iv = new androidx.work.Data.Builder();
        for (kotlin.Pair pair$iv : pairs$iv) {
            dataBuilder$iv.put((java.lang.String) pair$iv.getFirst(), pair$iv.getSecond());
        }
        androidx.work.Data build = dataBuilder$iv.build();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(build, "dataBuilder.build()");
        androidx.work.OneTimeWorkRequest req = builder.setInputData(build).setConstraints(new androidx.work.Constraints.Builder().setRequiredNetworkType(androidx.work.NetworkType.CONNECTED).build()).setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 30L, java.util.concurrent.TimeUnit.SECONDS).build();
        androidx.work.WorkManager.getInstance(ctx.getApplicationContext()).enqueueUniqueWork(name, androidx.work.ExistingWorkPolicy.KEEP, req);
    }
}
