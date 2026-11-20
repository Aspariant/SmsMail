package app.smsmail

import java.net.Inet4Address
import java.net.NetworkInterface

object Net {
    /**
     * This phone's address that the iPhone can reach, e.g. "192.168.1.23".
     *
     * Order: this phone's own hotspot first (the iPhone usually joins it when the
     * two phones are carried together), then the Wi-Fi network it is connected
     * to. Mobile-data interfaces are ignored: their private 10.x addresses are
     * not reachable from the iPhone.
     */
    fun localIpv4(): String? {
        val candidates = try {
            NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { nif ->
                    nif.inetAddresses.toList()
                        .filterIsInstance<Inet4Address>()
                        .filter { it.isSiteLocalAddress }
                        .mapNotNull { addr -> addr.hostAddress?.let { nif.name to it } }
                }
        } catch (e: Exception) {
            emptyList()
        }
        return candidates
            .mapNotNull { (name, ip) -> rank(name, ip)?.let { it to ip } }
            .minByOrNull { it.first }?.second
    }

    /** Lower is better; null = not reachable from another device on the LAN. */
    private fun rank(name: String, ip: String): Int? = when {
        // Hotspot interfaces: ap0, ap_br_wlan2 (Pixel), swlan0 (Samsung), softap0.
        name.startsWith("ap") || name.startsWith("swlan") || name.startsWith("softap") -> 0
        // Some phones run the hotspot on a second Wi-Fi interface (wlan1, wlan2…).
        // As the hotspot owner this phone is the gateway, so its address ends in .1.
        name.startsWith("wlan") && name != "wlan0" && ip.endsWith(".1") -> 0
        name.startsWith("wlan") -> 1
        name.startsWith("eth") -> 2
        else -> null // rmnet/ccmni (mobile data), tun (VPN), dummy, …
    }

    fun baseUrl(): String? = localIpv4()?.let { "http://$it:${LanService.PORT}" }
}
