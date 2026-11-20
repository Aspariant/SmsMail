package app.smsmail;

/* compiled from: SmsReceiver.kt */
public final class SmsReceiver extends android.content.BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(final android.content.Context context, android.content.Intent intent) {
        java.lang.Object answer$iv;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(context, "context");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(intent, "intent");
        if (kotlin.jvm.internal.Intrinsics.areEqual(intent.getAction(), "android.provider.Telephony.SMS_RECEIVED")) {
            android.telephony.SmsMessage[] parts = android.provider.Telephony.Sms.Intents.getMessagesFromIntent(intent);
            boolean z = true;
            if (parts != null) {
                if (!(parts.length == 0)) {
                    z = false;
                }
            }
            if (z) {
                return;
            }
            final java.util.LinkedHashMap bySender = new java.util.LinkedHashMap();
            java.util.Iterator it = kotlin.jvm.internal.ArrayIteratorKt.iterator(parts);
            while (it.hasNext()) {
                android.telephony.SmsMessage p = (android.telephony.SmsMessage) it.next();
                java.util.LinkedHashMap $this$getOrPut$iv = bySender;
                java.lang.String originatingAddress = p.getOriginatingAddress();
                if (originatingAddress == null) {
                    originatingAddress = androidx.core.os.EnvironmentCompat.MEDIA_UNKNOWN;
                }
                java.lang.Object value$iv = $this$getOrPut$iv.get(originatingAddress);
                if (value$iv == null) {
                    answer$iv = new java.lang.StringBuilder();
                    $this$getOrPut$iv.put(originatingAddress, answer$iv);
                } else {
                    answer$iv = value$iv;
                }
                java.lang.StringBuilder sb = (java.lang.StringBuilder) answer$iv;
                java.lang.String messageBody = p.getMessageBody();
                if (messageBody == null) {
                    messageBody = "";
                }
                sb.append(messageBody);
            }
            final long time = parts[0].getTimestampMillis();
            final android.content.BroadcastReceiver.PendingResult pending = goAsync();
            new java.lang.Thread(new java.lang.Runnable() { // from class: app.smsmail.SmsReceiver$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    app.smsmail.SmsReceiver.onReceive$lambda$1(bySender, context, time, pending);
                }
            }).start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onReceive$lambda$1(java.util.LinkedHashMap $bySender, android.content.Context $context, long $time, android.content.BroadcastReceiver.PendingResult $pending) {
        try {
            for (java.util.Map.Entry entry : $bySender.entrySet()) {
                java.lang.String number = (java.lang.String) entry.getKey();
                java.lang.StringBuilder text = (java.lang.StringBuilder) entry.getValue();
                try {
                    app.smsmail.Queue queue = app.smsmail.Queue.INSTANCE;
                    android.content.Context applicationContext = $context.getApplicationContext();
                    kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(applicationContext, "getApplicationContext(...)");
                    java.lang.String sb = text.toString();
                    kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(sb, "toString(...)");
                    queue.add(applicationContext, number, $time, sb);
                } catch (java.lang.Exception e) {
                    new app.smsmail.Store($context).fail("Could not queue an SMS: " + e.getClass().getSimpleName());
                }
            }
        } finally {
            $pending.finish();
        }
    }
}
