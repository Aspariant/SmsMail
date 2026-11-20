package app.smsmail;

/* compiled from: Mailer.kt */
public final class Mailer {
    private static final java.lang.String HOST = "smtp.mail.me.com";
    public static final app.smsmail.Mailer INSTANCE = new app.smsmail.Mailer();
    private static final int PORT = 587;
    private static volatile java.lang.String lastChain;

    private Mailer() {
    }

    public final java.lang.String getLastChain() {
        return lastChain;
    }

    public final void setLastChain(java.lang.String str) {
        lastChain = str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: Mailer.kt */

    public static final class RecordingTrustManager implements javax.net.ssl.X509TrustManager {
        private final javax.net.ssl.X509TrustManager delegate;

        public RecordingTrustManager(javax.net.ssl.X509TrustManager delegate) {
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, java.lang.String authType) {
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(chain, "chain");
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(authType, "authType");
            this.delegate.checkClientTrusted(chain, authType);
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, java.lang.String authType) {
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(chain, "chain");
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(authType, "authType");
            try {
                this.delegate.checkServerTrusted(chain, authType);
            } catch (java.security.cert.CertificateException e) {
                app.smsmail.Mailer.INSTANCE.setLastChain(kotlin.collections.ArraysKt.joinToString$default(chain, " | ", (java.lang.CharSequence) null, (java.lang.CharSequence) null, 0, (java.lang.CharSequence) null, new kotlin.jvm.functions.Function1() { // from class: app.smsmail.Mailer$RecordingTrustManager$$ExternalSyntheticLambda0
                    @Override // kotlin.jvm.functions.Function1
                    public final java.lang.Object invoke(java.lang.Object obj) {
                        java.lang.CharSequence checkServerTrusted$lambda$0;
                        checkServerTrusted$lambda$0 = app.smsmail.Mailer.RecordingTrustManager.checkServerTrusted$lambda$0(app.smsmail.Mailer.RecordingTrustManager.this, (java.security.cert.X509Certificate) obj);
                        return checkServerTrusted$lambda$0;
                    }
                }, 30, (java.lang.Object) null));
                throw e;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static final java.lang.CharSequence checkServerTrusted$lambda$0(app.smsmail.Mailer.RecordingTrustManager this$0, java.security.cert.X509Certificate it) {
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(it, "it");
            java.lang.String name = it.getSubjectX500Principal().getName();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(name, "getName(...)");
            java.lang.String cn = this$0.cn(name);
            java.lang.String name2 = it.getIssuerX500Principal().getName();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(name2, "getName(...)");
            return cn + " (issuer " + this$0.cn(name2) + ")";
        }

        @Override // javax.net.ssl.X509TrustManager
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            java.security.cert.X509Certificate[] acceptedIssuers = this.delegate.getAcceptedIssuers();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(acceptedIssuers, "getAcceptedIssuers(...)");
            return acceptedIssuers;
        }

        private final java.lang.String cn(java.lang.String name) {
            java.util.List<java.lang.String> groupValues;
            java.lang.String str;
            kotlin.text.MatchResult find$default = kotlin.text.Regex.find$default(new kotlin.text.Regex("CN=([^,]+)"), name, 0, 2, null);
            return (find$default == null || (groupValues = find$default.getGroupValues()) == null || (str = groupValues.get(1)) == null) ? kotlin.text.StringsKt.take(name, 40) : str;
        }
    }

    private final javax.net.ssl.SSLSocketFactory socketFactory() {
        javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory.getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((java.security.KeyStore) null);
        javax.net.ssl.TrustManager[] trustManagers = tmf.getTrustManagers();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(trustManagers, "getTrustManagers(...)");
        java.util.Collection destination$iv$iv = new java.util.ArrayList();
        for (javax.net.ssl.TrustManager trustManager : trustManagers) {
            if (trustManager instanceof javax.net.ssl.X509TrustManager) {
                destination$iv$iv.add(trustManager);
            }
        }
        javax.net.ssl.X509TrustManager system = (javax.net.ssl.X509TrustManager) kotlin.collections.CollectionsKt.first((java.util.List) destination$iv$iv);
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
        ctx.init(null, new javax.net.ssl.TrustManager[]{new app.smsmail.Mailer.RecordingTrustManager(system)}, null);
        javax.net.ssl.SSLSocketFactory socketFactory = ctx.getSocketFactory();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(socketFactory, "getSocketFactory(...)");
        return socketFactory;
    }

    public final void send(app.smsmail.Config cfg, java.lang.String subject, java.lang.String body) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(cfg, "cfg");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(subject, "subject");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(body, "body");
        lastChain = null;
        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.checkserveridentity", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.socketFactory", INSTANCE.socketFactory());
        props.put("mail.smtp.connectiontimeout", "20000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");
        javax.mail.Session session = javax.mail.Session.getInstance(props);
        javax.mail.internet.MimeMessage msg = new javax.mail.internet.MimeMessage(session);
        msg.setFrom(new javax.mail.internet.InternetAddress(cfg.getSender()));
        msg.setRecipients(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress.parse(cfg.getRecipient(), true));
        msg.setSubject(new kotlin.text.Regex("\\p{Cntrl}").replace(subject, " "), "UTF-8");
        msg.setText(body, "UTF-8");
        msg.setSentDate(new java.util.Date());
        javax.mail.Transport transport = session.getTransport("smtp");
        try {
            transport.connect(HOST, PORT, cfg.getSender(), cfg.getPassword());
            transport.sendMessage(msg, msg.getAllRecipients());
            try {
                transport.close();
            } catch (java.lang.Exception e) {
            }
        } catch (java.lang.Throwable th) {
            try {
                transport.close();
            } catch (java.lang.Exception e2) {
            }
            throw th;
        }
    }
}
