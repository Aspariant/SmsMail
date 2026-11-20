package app.smsmail;

/* compiled from: MainActivity.kt */
public final class MainActivity extends android.app.Activity {
    private android.widget.EditText etPassword;
    private android.widget.EditText etRecipient;
    private android.widget.EditText etSender;
    private android.widget.TextView status;
    private app.smsmail.Store store;

    @Override // android.app.Activity
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(8192, 8192);
        this.store = new app.smsmail.Store(this);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(pad, pad, pad, pad);
        this.etSender = field(root, "Sender iCloud address (dedicated account)", 33);
        app.smsmail.Store store = this.store;
        android.widget.TextView textView = null;
        if (store == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("store");
            store = null;
        }
        this.etPassword = field(root, store.hasPassword() ? "App-specific password (saved; leave empty to keep)" : "App-specific password", 129);
        this.etRecipient = field(root, "Recipient address (your iCloud mailbox)", 33);
        android.widget.EditText editText = this.etSender;
        if (editText == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("etSender");
            editText = null;
        }
        app.smsmail.Store store2 = this.store;
        if (store2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("store");
            store2 = null;
        }
        java.lang.String sender = store2.sender();
        if (sender == null) {
            sender = "";
        }
        editText.setText(sender);
        android.widget.EditText editText2 = this.etRecipient;
        if (editText2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("etRecipient");
            editText2 = null;
        }
        app.smsmail.Store store3 = this.store;
        if (store3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("store");
            store3 = null;
        }
        java.lang.String recipient = store3.recipient();
        editText2.setText(recipient != null ? recipient : "");
        button(root, "Save settings", new kotlin.jvm.functions.Function0() { // from class: app.smsmail.MainActivity$$ExternalSyntheticLambda1
            @Override // kotlin.jvm.functions.Function0
            public final java.lang.Object invoke() {
                kotlin.Unit onCreate$lambda$1;
                onCreate$lambda$1 = app.smsmail.MainActivity.onCreate$lambda$1(app.smsmail.MainActivity.this);
                return onCreate$lambda$1;
            }
        });
        button(root, "Send test message", new kotlin.jvm.functions.Function0() { // from class: app.smsmail.MainActivity$$ExternalSyntheticLambda2
            @Override // kotlin.jvm.functions.Function0
            public final java.lang.Object invoke() {
                kotlin.Unit onCreate$lambda$2;
                onCreate$lambda$2 = app.smsmail.MainActivity.onCreate$lambda$2(app.smsmail.MainActivity.this);
                return onCreate$lambda$2;
            }
        });
        button(root, "Grant SMS permission", new kotlin.jvm.functions.Function0() { // from class: app.smsmail.MainActivity$$ExternalSyntheticLambda3
            @Override // kotlin.jvm.functions.Function0
            public final java.lang.Object invoke() {
                kotlin.Unit onCreate$lambda$3;
                onCreate$lambda$3 = app.smsmail.MainActivity.onCreate$lambda$3(app.smsmail.MainActivity.this);
                return onCreate$lambda$3;
            }
        });
        button(root, "Battery optimization settings", new kotlin.jvm.functions.Function0() { // from class: app.smsmail.MainActivity$$ExternalSyntheticLambda4
            @Override // kotlin.jvm.functions.Function0
            public final java.lang.Object invoke() {
                kotlin.Unit onCreate$lambda$4;
                onCreate$lambda$4 = app.smsmail.MainActivity.onCreate$lambda$4(app.smsmail.MainActivity.this);
                return onCreate$lambda$4;
            }
        });
        button(root, "Retry queued messages", new kotlin.jvm.functions.Function0() { // from class: app.smsmail.MainActivity$$ExternalSyntheticLambda5
            @Override // kotlin.jvm.functions.Function0
            public final java.lang.Object invoke() {
                kotlin.Unit onCreate$lambda$5;
                onCreate$lambda$5 = app.smsmail.MainActivity.onCreate$lambda$5(app.smsmail.MainActivity.this);
                return onCreate$lambda$5;
            }
        });
        button(root, "Refresh status", new kotlin.jvm.functions.Function0() { // from class: app.smsmail.MainActivity$$ExternalSyntheticLambda6
            @Override // kotlin.jvm.functions.Function0
            public final java.lang.Object invoke() {
                kotlin.Unit onCreate$lambda$6;
                onCreate$lambda$6 = app.smsmail.MainActivity.onCreate$lambda$6(app.smsmail.MainActivity.this);
                return onCreate$lambda$6;
            }
        });
        android.widget.TextView $this$onCreate_u24lambda_u247 = new android.widget.TextView(this);
        $this$onCreate_u24lambda_u247.setPadding(0, pad, 0, 0);
        this.status = $this$onCreate_u24lambda_u247;
        android.widget.TextView textView2 = this.status;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(androidx.core.app.NotificationCompat.CATEGORY_STATUS);
        } else {
            textView = textView2;
        }
        root.addView(textView);
        android.widget.ScrollView $this$onCreate_u24lambda_u248 = new android.widget.ScrollView(this);
        $this$onCreate_u24lambda_u248.addView(root);
        setContentView($this$onCreate_u24lambda_u248);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$1(app.smsmail.MainActivity this$0) {
        this$0.save();
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$2(app.smsmail.MainActivity this$0) {
        this$0.sendTest();
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$3(app.smsmail.MainActivity this$0) {
        this$0.requestPermissions(new java.lang.String[]{"android.permission.RECEIVE_SMS"}, 1);
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$4(app.smsmail.MainActivity this$0) {
        this$0.startActivity(new android.content.Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS"));
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$5(app.smsmail.MainActivity this$0) {
        app.smsmail.Queue.INSTANCE.enqueueAll(this$0);
        this$0.refresh();
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$6(app.smsmail.MainActivity this$0) {
        this$0.refresh();
        return kotlin.Unit.INSTANCE;
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int code, java.lang.String[] perms, int[] results) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(perms, "perms");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(results, "results");
        super.onRequestPermissionsResult(code, perms, results);
        refresh();
    }

    private final void save() {
        android.widget.EditText editText = this.etSender;
        android.widget.EditText editText2 = null;
        if (editText == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("etSender");
            editText = null;
        }
        java.lang.String s = kotlin.text.StringsKt.trim((java.lang.CharSequence) editText.getText().toString()).toString();
        android.widget.EditText editText3 = this.etRecipient;
        if (editText3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("etRecipient");
            editText3 = null;
        }
        java.lang.String r = kotlin.text.StringsKt.trim((java.lang.CharSequence) editText3.getText().toString()).toString();
        android.widget.EditText editText4 = this.etPassword;
        if (editText4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("etPassword");
            editText4 = null;
        }
        java.lang.String p = editText4.getText().toString();
        if (!kotlin.text.StringsKt.contains$default((java.lang.CharSequence) s, (java.lang.CharSequence) "@", false, 2, (java.lang.Object) null) || !kotlin.text.StringsKt.contains$default((java.lang.CharSequence) r, (java.lang.CharSequence) "@", false, 2, (java.lang.Object) null)) {
            toast("Enter valid sender and recipient addresses");
            return;
        }
        if (p.length() == 0) {
            app.smsmail.Store store = this.store;
            if (store == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("store");
                store = null;
            }
            if (!store.hasPassword()) {
                toast("Enter the app-specific password");
                return;
            }
        }
        try {
            app.smsmail.Store store2 = this.store;
            if (store2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("store");
                store2 = null;
            }
            store2.save(s, p, r);
            android.widget.EditText editText5 = this.etPassword;
            if (editText5 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("etPassword");
                editText5 = null;
            }
            editText5.setText("");
            android.widget.EditText editText6 = this.etPassword;
            if (editText6 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("etPassword");
            } else {
                editText2 = editText6;
            }
            editText2.setHint("App-specific password (saved; leave empty to keep)");
            app.smsmail.Queue.INSTANCE.enqueueAll(this);
            toast("Saved");
            refresh();
        } catch (java.lang.Exception e) {
            toast("Could not save: " + e.getClass().getSimpleName());
        }
    }

    private final void sendTest() {
        try {
            app.smsmail.Queue.INSTANCE.add(this, "SmsMail test", java.lang.System.currentTimeMillis(), "If you can read this, forwarding works.");
            toast("Test message queued");
        } catch (java.lang.Exception e) {
            toast("Could not queue: " + e.getClass().getSimpleName());
        }
        refresh();
    }

    /* JADX WARN: Code restructure failed: missing block: B:34:0x0149, code lost:
    
        if (r2 == null) goto L42;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private final void refresh() {
        app.smsmail.Store store;
        java.lang.String it;
        java.text.DateFormat fmt = java.text.DateFormat.getDateTimeInstance();
        boolean perm = checkSelfPermission("android.permission.RECEIVE_SMS") == 0;
        java.lang.Object systemService = getSystemService("power");
        kotlin.jvm.internal.Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.os.PowerManager");
        boolean ignoring = ((android.os.PowerManager) systemService).isIgnoringBatteryOptimizations(getPackageName());
        app.smsmail.Store store2 = this.store;
        if (store2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("store");
            store2 = null;
        }
        long ok = store2.lastSuccess();
        app.smsmail.Store store3 = this.store;
        if (store3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("store");
            store3 = null;
        }
        long failTime = store3.lastFailureTime();
        android.widget.TextView textView = this.status;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(androidx.core.app.NotificationCompat.CATEGORY_STATUS);
            textView = null;
        }
        java.lang.StringBuilder $this$refresh_u24lambda_u2410 = new java.lang.StringBuilder();
        java.lang.StringBuilder append = $this$refresh_u24lambda_u2410.append("SMS permission: " + (perm ? "granted" : "NOT granted"));
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append, "append(...)");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append.append('\n'), "append(...)");
        app.smsmail.Store store4 = this.store;
        if (store4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("store");
            store4 = null;
        }
        java.lang.StringBuilder append2 = $this$refresh_u24lambda_u2410.append("Configured: " + (store4.config() != null ? "yes" : "no"));
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append2, "append(...)");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append2.append('\n'), "append(...)");
        java.lang.StringBuilder append3 = $this$refresh_u24lambda_u2410.append("Battery optimization exempt: " + (ignoring ? "yes" : "no"));
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append3, "append(...)");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append3.append('\n'), "append(...)");
        java.lang.StringBuilder append4 = $this$refresh_u24lambda_u2410.append("Last success: " + (ok > 0 ? fmt.format(new java.util.Date(ok)) : "never"));
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append4, "append(...)");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append4.append('\n'), "append(...)");
        app.smsmail.Store store5 = this.store;
        if (store5 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("store");
            store = null;
        } else {
            store = store5;
        }
        java.lang.String it2 = store.lastFailure();
        if (it2 != null) {
            it = fmt.format(new java.util.Date(failTime)) + "\n  " + it2;
        }
        it = "none";
        java.lang.StringBuilder append5 = $this$refresh_u24lambda_u2410.append("Last failure: " + it);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append5, "append(...)");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append5.append('\n'), "append(...)");
        $this$refresh_u24lambda_u2410.append("Queued messages: " + app.smsmail.Queue.INSTANCE.names(this).size());
        java.lang.String sb = $this$refresh_u24lambda_u2410.toString();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(sb, "toString(...)");
        textView.setText(sb);
    }

    private final android.widget.EditText field(android.widget.LinearLayout parent, java.lang.String hint, int type) {
        android.widget.EditText $this$field_u24lambda_u2411 = new android.widget.EditText(this);
        $this$field_u24lambda_u2411.setHint(hint);
        $this$field_u24lambda_u2411.setInputType(type);
        $this$field_u24lambda_u2411.setImportantForAutofill(2);
        parent.addView($this$field_u24lambda_u2411);
        return $this$field_u24lambda_u2411;
    }

    private final void button(android.widget.LinearLayout parent, java.lang.String label, final kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
        android.widget.Button $this$button_u24lambda_u2413 = new android.widget.Button(this);
        $this$button_u24lambda_u2413.setText(label);
        $this$button_u24lambda_u2413.setOnClickListener(new android.view.View.OnClickListener() { // from class: app.smsmail.MainActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                kotlin.jvm.functions.Function0.this.invoke();
            }
        });
        parent.addView($this$button_u24lambda_u2413);
    }

    private final void toast(java.lang.String msg) {
        android.widget.Toast.makeText(this, msg, 0).show();
    }
}
