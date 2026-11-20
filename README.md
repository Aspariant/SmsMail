# SmsMail

Android app that forwards incoming SMS messages to an email address over SMTP (iCloud, smtp.mail.me.com:587, STARTTLS).

## Notes
- Sender, recipient and app-specific password are entered in the app; the password is stored encrypted with an Android Keystore key.
- Incoming SMS are queued in a local database and sent by a WorkManager worker.
- Permissions: RECEIVE_SMS/BROADCAST_SMS, internet, boot completed, foreground service.

## About this source
This code was recovered by decompiling a debug APK (jadx), so it is Java rather than the original Kotlin, has no comments, and has no Gradle build files. See `app/src/main/java/app/smsmail`.
