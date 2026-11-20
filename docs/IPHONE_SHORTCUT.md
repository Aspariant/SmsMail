# iPhone Shortcut setup (LAN mode)

The Android phone running SmsMail acts as a small server on the local network. A ready-made iPhone Shortcut keeps asking it for new messages and shows each one as a notification.

The two phones must be on the same network: the same Wi‑Fi, or the iPhone joined to the Android phone's **hotspot**.

## 1. Copy the Shortcut URL

On the Android phone: **SmsMail → Settings → Local network → iPhone Shortcut (Recommended) → Save settings**.

Then, in Settings, scan the **QR code** with the iPhone camera. It opens a setup page in Safari. Tap **Copy URL**. The URL looks like:

```
http://192.168.43.1:8765/next?key=ABCDEFGHJKMNPQ
```

If Safari can't open the page, the phones are not on the same network, or the router blocks devices from talking to each other ("client/AP isolation", common on guest networks).

## 2. Add the shortcut

On the setup page, tap **Get the SmsMail shortcut**, or open this link on the iPhone:

**https://www.icloud.com/shortcuts/ffa3ff1e25aa48df8899342de6cf1d65**

Tap **Add Shortcut**. It asks two things:

1. **The URL**: paste the Shortcut URL you copied.
2. **The Wi‑Fi name**: type the exact name of the network both phones use (the Android phone's hotspot name, if the iPhone joins its hotspot). When the iPhone is on any other network, the shortcut quietly stops instead of showing errors.

Run it once by hand and allow the network and notification prompts.

## 3. Make it run automatically

iOS can't keep a shortcut running forever; it may end it in the background after a while. Automations start it again. Starting it while it's already running is harmless: the new copy takes over and the old one stops.

In **Shortcuts → Automation → +**, create these. Set each to **Run Immediately**, turn off **Notify When Run**, and pick the **SmsMail** shortcut:

- **Wi‑Fi**: when joining the same network (or the Android phone's hotspot).
- **App**: apps you open many times a day (Messages, WhatsApp, Safari, Instagram…), **Is Opened**.
- **Charger**: Is Connected.
- Optional **Time of Day**: a few fixed times (e.g. 7:00, 12:00, 18:00, 22:00), one automation each.

## Good to know

- **Nothing is lost** when iOS stops the shortcut: messages wait (encrypted) on the Android phone until the iPhone confirms it received them. The worst case is a duplicate notification.
- SmsMail forwards **SMS and normal phone calls** to the Android phone's SIM. WhatsApp, FaceTime and other internet calls are not detected.
- If the URL changes (different IP, or **Settings → New key**), open the setup page again and re-add the shortcut with the new URL.
- On a shared Wi‑Fi, give the Android phone a fixed IP (DHCP reservation in the router) so the URL keeps working.
- The connection is plain HTTP on the local network, protected by the key. Use a network you trust.

## What the shortcut does

```
If the Wi-Fi name is not <your network>: stop
Repeat 100000 times
    Get contents of <Shortcut URL>
    If it has "text": show it as a notification
    If it has "stop": stop (a newer copy took over)
End Repeat
```

## For power users

| Endpoint | Reply |
|---|---|
| `GET /next?key=K` | `{"text","id","kind":"sms"\|"call","from","time","title","body"}`, `{}` after 25 s, or `{"stop":"yes"}` |
| `GET /next.txt?key=K` | `title\nbody` as plain text, or empty (handy for `curl`) |
| `GET /ping?key=K` | `{"ok":true,"waiting":N}` |
| `GET /setup?key=K` | the setup page |

- `wait=0..50` changes how long `/next` waits (default 25 seconds).
- The key can also be sent as an `X-Key` header.
- A message counts as delivered when the Shortcut asks for the next one. Messages that are never confirmed are offered again after 2 minutes.
- A `/next` request that is still waiting when a newer one arrives gets `{"stop":"yes"}`: only the newest copy of the Shortcut keeps polling.
