package io.homeassistant.companion.android.common.phoneautomation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Defines the conditions that can trigger a [PhoneAutomation].
 *
 * Each subtype maps to a specific system event that can be detected on-device. The serializable
 * hierarchy allows triggers to be stored as JSON in the Room database.
 */
@Serializable
sealed interface PhoneAutomationTrigger {

    /** Fires when the device screen turns on. */
    @Serializable
    @SerialName("screen_on")
    data object ScreenOn : PhoneAutomationTrigger

    /** Fires when the device screen turns off. */
    @Serializable
    @SerialName("screen_off")
    data object ScreenOff : PhoneAutomationTrigger

    /** Fires when the device begins charging. */
    @Serializable
    @SerialName("charging_started")
    data object ChargingStarted : PhoneAutomationTrigger

    /** Fires when the device stops charging (unplugged). */
    @Serializable
    @SerialName("charging_stopped")
    data object ChargingStopped : PhoneAutomationTrigger

    /**
     * Fires when the device connects to a Wi-Fi network.
     *
     * @param ssid Optional SSID filter. When non-null the trigger only fires when the connected
     *   network matches the given SSID. When null, any connection counts.
     */
    @Serializable
    @SerialName("wifi_connected")
    data class WifiConnected(val ssid: String? = null) : PhoneAutomationTrigger

    /** Fires when the device disconnects from any Wi-Fi network. */
    @Serializable
    @SerialName("wifi_disconnected")
    data object WifiDisconnected : PhoneAutomationTrigger

    /**
     * Fires when the device's battery level crosses a threshold.
     *
     * @param threshold The battery percentage (0–100) to compare against.
     * @param belowThreshold When true, fires when the battery drops below the threshold;
     *   when false, fires when the battery rises above it.
     */
    @Serializable
    @SerialName("battery_level")
    data class BatteryLevel(
        val threshold: Int,
        val belowThreshold: Boolean,
    ) : PhoneAutomationTrigger

    /**
     * Fires when a specific application comes to the foreground.
     *
     * Requires the PACKAGE_USAGE_STATS permission granted by the user.
     *
     * @param packageName The Android package name of the app to watch (e.g. "com.example.app").
     */
    @Serializable
    @SerialName("app_launched")
    data class AppLaunched(val packageName: String) : PhoneAutomationTrigger
}
