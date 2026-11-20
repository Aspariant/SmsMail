# SmsMail

**SmsMail** is a programmatic Kotlin Android app (no XML layouts) that forwards SMS messages and optional incoming call notifications via email using `WorkManager` (`SendWorker`), `javax.mail` over TLS, `AndroidKeyStore`, AES-256-GCM encryption, and an encrypted local queue (`Queue` / `Store`)[cite: 3, 4].

---

## Requirements

- **Android:** Minimum API 29 (Android 10+)[cite: 5]
- **Permissions:** SMS and phone/call permissions granted in the app, plus battery optimization exemption for reliable background delivery
- **Email Account:** A Gmail or iCloud (`@icloud.com`) account with **2-Step Verification / Two-Factor Authentication** enabled and an **App-Specific Password** (your regular account password will not work)

---

## How to Get an App-Specific Password

### 1. Gmail (Android / Gmail Forwarding)

Google requires **2-Step Verification** to be active on your Google Account before you can generate an App Password.

1. Sign in to your [Google Account](https://myaccount.google.com/) and open **Security**.
2. Under **How you sign in to Google**, make sure **2-Step Verification** is turned **On**.
3. Go to **App passwords** (or open `https://myaccount.google.com/apppasswords` directly) and verify your identity if prompted.
4. Enter a label name (such as `SmsMail`) and select **Create**.
5. Copy the **16-character app password** shown on screen—Google only displays it once.

> Note: Changing your main Google Account password revokes existing app passwords, so you will need to generate a new one if your main password changes.

---

### 2. iCloud (iPhone / iCloud Forwarding)

Apple requires **Two-Factor Authentication** to be enabled on your Apple Account before App-Specific Passwords appear.

1. Sign in to [account.apple.com](https://account.apple.com/) with your Apple Account.
2. Open the **Sign-In and Security** section and select **App-Specific Passwords**.
3. Select **Generate an app-specific password** (or **+**), enter a label such as `SmsMail`, and confirm with your Apple Account password if prompted.
4. Copy the generated app-specific password immediately—Apple only shows it once when created.

> Note: Resetting or changing your Apple Account password revokes all active app-specific passwords.

---

## Setup & Usage

1. Install `app-debug.apk` (from **Releases** or built via `:app:assembleDebug`) and open **SmsMail**[cite: 3, 4, 5].
2. Enter your sender/recipient email details and paste the **16-character app-specific password** (not your normal account password).
3. Choose your **Theme** (**Light** or **Dark**), select your **Receiver** mode (**iPhone (iCloud)** or **Android (Gmail)**), and toggle **Receive incoming call notifications via email** if desired.
4. Grant **SMS** and **Call** permissions, configure battery optimization exemption if needed, and tap **SAVE SETTINGS**.
5. Tap **SEND TEST MESSAGE** and **REFRESH STATUS** to verify delivery and confirm `Queued messages` returns to `0`.
