# SmsMail

**SmsMail** is a tool that allows you to receive your sms on email, why? i have 2 phones, my sim is in one and the other has no sim, i daily drive the one with no sim, and it is annoying to go search for the other phone whenever i need a verification code, and when it is in my bag i don't hear the calls usually. so with this tool i'll get all the notifications on my main phone without the hassle of searching for my phone.
the tool is completely vibe coded, I am not a programmer and know barely anything about programming.

---

## Two ways to forward

| | **Local network → iPhone Shortcut** (Recommended) | **Email** |
|---|---|---|
| Speed | Instant (1–2 s) while the Shortcut runs | Depends on the mail server |
| Accounts / passwords | None | Gmail or iCloud app-specific password |
| Where it works | Same Wi‑Fi, or iPhone on the Android phone's hotspot | Anywhere with internet |
| Receiver | iPhone (Shortcuts app) | Any mailbox |

Pick the method in **Settings → Delivery method**. You can switch at any time; messages waiting in the queue go out through whichever method is selected.

---

## Requirements

- **Android:** Minimum API 29 (Android 10+)
- **Permissions:** SMS and phone/call permissions granted in the app, plus battery optimization exemption for reliable background delivery
- **LAN mode:** an iPhone with the Shortcuts app, on the same Wi‑Fi as the Android phone or on its hotspot
- **Email mode:** A Gmail or iCloud (`@icloud.com`) account with **2-Step Verification / Two-Factor Authentication** enabled and an **App-Specific Password** (your regular account password will not work)

---

## Local network → iPhone Shortcut (Recommended)

The Android phone runs a small server on the local network (port `8765`). An iPhone Shortcut keeps asking it "anything new?". Each request waits up to 25 seconds and answers the moment an SMS or call comes in, so it behaves like a push. The Shortcut shows each message as an iOS notification.

Both phones must be on the same Wi‑Fi, or the iPhone can join the Android phone's **hotspot**.

### On the Android phone

1. Open **SmsMail → Settings** and choose **Local network → iPhone Shortcut (Recommended)**.
2. Grant **SMS** (and **Call**, if you want call alerts) permission and turn on the **battery optimization exemption**.
3. Tap **Save settings**. A persistent notification shows the address the server listens on.

### On the iPhone

1. In **Settings** on the Android phone, scan the **QR code** with the iPhone camera. A setup page opens in Safari. Tap **Copy URL**.
2. Tap **Get the SmsMail shortcut** on that page (or open [the SmsMail shortcut](https://www.icloud.com/shortcuts/ffa3ff1e25aa48df8899342de6cf1d65)) and tap **Add Shortcut**. When asked, paste the URL and type the exact name of the Wi‑Fi network (or hotspot) both phones use.
3. iOS can't keep a shortcut running forever, so add automations that restart it (**Shortcuts → Automation → +**, each **Run Immediately**):
   - **Wi‑Fi**: when joining that network or hotspot
   - **App**: when you open apps you use often (Messages, WhatsApp, Safari…)
   - **Charger**: when connected

   Starting it while it's already running is fine: the newest copy takes over and the old one stops.

More detail is in [docs/IPHONE_SHORTCUT.md](docs/IPHONE_SHORTCUT.md).

### Good to know

- **Nothing gets lost.** Messages stay encrypted on the Android phone until the iPhone confirms it received them. If iOS stops the Shortcut, they arrive the next time it runs. The worst case is a duplicate notification.
- **Fixed IP:** on a shared Wi‑Fi, give the Android phone a DHCP reservation in your router so the URL doesn't change.
- **Security:** local traffic is plain HTTP, protected by a random key in the URL. Only use it on a network you trust. Tap **New key** in Settings if the URL ever leaks.
- SmsMail forwards SMS and normal phone calls to the SIM. WhatsApp and other internet calls are not detected.
- Test from any browser on the same network: `http://<phone-ip>:8765/ping?key=<your key>` should return `{"ok":true,...}`.

---

## Email

### How to Get an App-Specific Password

#### 1. Gmail (Android / Gmail Forwarding)

Google requires **2-Step Verification** to be active on your Google Account before you can generate an App Password.

1. Sign in to your [Google Account](https://myaccount.google.com/) and open **Security**.
2. Under **How you sign in to Google**, make sure **2-Step Verification** is turned **On**.
3. Go to **App passwords** (or open `https://myaccount.google.com/apppasswords` directly) and verify your identity if prompted.
4. Enter a label name (such as `SmsMail`) and select **Create**.
5. Copy the **16-character app password** shown on screen—Google only displays it once.

> Note: Changing your main Google Account password revokes existing app passwords, so you will need to generate a new one if your main password changes.

---

#### 2. iCloud (iPhone / iCloud Forwarding)

Apple requires **Two-Factor Authentication** to be enabled on your Apple Account before App-Specific Passwords appear.

1. Sign in to [account.apple.com](https://account.apple.com/) with your Apple Account.
2. Open the **Sign-In and Security** section and select **App-Specific Passwords**.
3. Select **Generate an app-specific password** (or **+**), enter a label such as `SmsMail`, and confirm with your Apple Account password if prompted.
4. Copy the generated app-specific password immediately—Apple only shows it once when created.

> Note: Resetting or changing your Apple Account password revokes all active app-specific passwords.

---

### Email setup

1. In **Settings**, choose **Email** as the delivery method.
2. Enter your sender/recipient email details and paste the **16-character app-specific password** (not your normal account password).
3. Select your **Receiver** (**iPhone (iCloud)** or **Android (Gmail)**).

---

## Setup & Usage

1. Install `app-debug.apk` (from **Releases** or built with `./gradlew :app:assembleDebug`) and open **SmsMail**.
2. Open **Settings**, pick a **Delivery method** (LAN is recommended), choose your **Theme**, and toggle **Receive incoming call notifications** if desired.
3. Grant **SMS** and **Call** permissions, configure battery optimization exemption, and tap **SAVE SETTINGS**.
4. Tap **SEND TEST MESSAGE** and **REFRESH STATUS** to verify delivery and confirm the queue returns to `0`.

> Android 13+ may block SMS and call-log permissions for sideloaded apps. If the permission buttons do nothing, open **App info → ⋮ → Allow restricted settings**, then try again.
