package io.homeassistant.companion.android.phoneautomation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationTrigger
import javax.inject.Inject
import timber.log.Timber

/**
 * Listens for system-level broadcast events and forwards them to [PhoneAutomationEngine] for
 * evaluation against the user's configured automation rules.
 *
 * Charging and battery events are received statically (registered in the manifest). Screen and
 * Wi-Fi events must be registered dynamically at runtime since they are not deliverable to static
 * receivers on API 26+.
 */
@AndroidEntryPoint
class PhoneAutomationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var engine: PhoneAutomationEngine

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("PhoneAutomationReceiver: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_SCREEN_ON ->
                engine.onTrigger(trigger = PhoneAutomationTrigger.ScreenOn)

            Intent.ACTION_SCREEN_OFF ->
                engine.onTrigger(trigger = PhoneAutomationTrigger.ScreenOff)

            Intent.ACTION_POWER_CONNECTED ->
                engine.onTrigger(trigger = PhoneAutomationTrigger.ChargingStarted)

            Intent.ACTION_POWER_DISCONNECTED ->
                engine.onTrigger(trigger = PhoneAutomationTrigger.ChargingStopped)

            Intent.ACTION_BATTERY_LOW ->
                engine.onTrigger(
                    trigger = PhoneAutomationTrigger.BatteryLevel(threshold = 0, belowThreshold = true),
                    batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
                )

            Intent.ACTION_BATTERY_OKAY ->
                engine.onTrigger(
                    trigger = PhoneAutomationTrigger.BatteryLevel(threshold = 100, belowThreshold = false),
                    batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
                )

            WifiManager.NETWORK_STATE_CHANGED_ACTION -> handleWifiChange(intent)
        }
    }

    @Suppress("DEPRECATION")
    private fun handleWifiChange(intent: Intent) {
        val networkInfo: NetworkInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO, NetworkInfo::class.java)
        } else {
            intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
        }
        networkInfo ?: return

        if (networkInfo.isConnected) {
            // The SSID is embedded in NetworkInfo.extraInfo as a quoted string, e.g. "\"MyWifi\""
            val ssid = networkInfo.extraInfo?.trim('"')?.takeIf { it.isNotBlank() }
            engine.onTrigger(
                trigger = PhoneAutomationTrigger.WifiConnected(),
                wifiSsid = ssid,
            )
        } else {
            engine.onTrigger(trigger = PhoneAutomationTrigger.WifiDisconnected)
        }
    }
}
