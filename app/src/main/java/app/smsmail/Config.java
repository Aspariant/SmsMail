package app.smsmail;

/* compiled from: Store.kt */
public final /* data */ class Config {
    private final java.lang.String password;
    private final java.lang.String recipient;
    private final java.lang.String sender;

    public static /* synthetic */ app.smsmail.Config copy$default(app.smsmail.Config config, java.lang.String str, java.lang.String str2, java.lang.String str3, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = config.sender;
        }
        if ((i & 2) != 0) {
            str2 = config.password;
        }
        if ((i & 4) != 0) {
            str3 = config.recipient;
        }
        return config.copy(str, str2, str3);
    }

    /* renamed from: component1, reason: from getter */
    public final java.lang.String getSender() {
        return this.sender;
    }

    /* renamed from: component2, reason: from getter */
    public final java.lang.String getPassword() {
        return this.password;
    }

    /* renamed from: component3, reason: from getter */
    public final java.lang.String getRecipient() {
        return this.recipient;
    }

    public final app.smsmail.Config copy(java.lang.String sender, java.lang.String password, java.lang.String recipient) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(sender, "sender");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(password, "password");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(recipient, "recipient");
        return new app.smsmail.Config(sender, password, recipient);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof app.smsmail.Config)) {
            return false;
        }
        app.smsmail.Config config = (app.smsmail.Config) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.sender, config.sender) && kotlin.jvm.internal.Intrinsics.areEqual(this.password, config.password) && kotlin.jvm.internal.Intrinsics.areEqual(this.recipient, config.recipient);
    }

    public int hashCode() {
        return (((this.sender.hashCode() * 31) + this.password.hashCode()) * 31) + this.recipient.hashCode();
    }

    public java.lang.String toString() {
        return "Config(sender=" + this.sender + ", password=" + this.password + ", recipient=" + this.recipient + ")";
    }

    public Config(java.lang.String sender, java.lang.String password, java.lang.String recipient) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(sender, "sender");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(password, "password");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(recipient, "recipient");
        this.sender = sender;
        this.password = password;
        this.recipient = recipient;
    }

    public final java.lang.String getPassword() {
        return this.password;
    }

    public final java.lang.String getRecipient() {
        return this.recipient;
    }

    public final java.lang.String getSender() {
        return this.sender;
    }
}
