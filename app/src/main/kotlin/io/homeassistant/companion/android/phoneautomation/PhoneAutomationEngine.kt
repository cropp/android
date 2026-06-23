package io.homeassistant.companion.android.phoneautomation

import io.homeassistant.companion.android.common.data.servers.ServerManager
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationAction
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationRepository
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationTrigger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber

/**
 * Evaluates triggered [PhoneAutomationTrigger] events and executes the corresponding
 * [PhoneAutomationAction] on the Home Assistant server.
 *
 * This engine is invoked by [PhoneAutomationReceiver] whenever a relevant system event occurs.
 * Each evaluation runs off the main thread to avoid blocking the receiver.
 */
@Singleton
class PhoneAutomationEngine @Inject constructor(
    private val repository: PhoneAutomationRepository,
    private val serverManager: ServerManager,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val actionJson = Json { ignoreUnknownKeys = true }

    /**
     * Evaluates all enabled automations against the given [trigger] and executes matching actions.
     *
     * @param trigger The trigger event that occurred.
     * @param batteryLevel Current battery level (0–100), required for [PhoneAutomationTrigger.BatteryLevel].
     * @param wifiSsid Connected SSID, required for [PhoneAutomationTrigger.WifiConnected] matching.
     */
    fun onTrigger(
        trigger: PhoneAutomationTrigger,
        batteryLevel: Int? = null,
        wifiSsid: String? = null,
    ) {
        scope.launch {
            val automations = repository.getEnabled()
            for (automation in automations) {
                val matches = matchesTrigger(
                    configured = automation.trigger,
                    received = trigger,
                    batteryLevel = batteryLevel,
                    wifiSsid = wifiSsid,
                )
                if (matches) {
                    Timber.d("Phone automation '${automation.name}' matched trigger $trigger")
                    executeAction(automation.action)
                }
            }
        }
    }

    private fun matchesTrigger(
        configured: PhoneAutomationTrigger,
        received: PhoneAutomationTrigger,
        batteryLevel: Int?,
        wifiSsid: String?,
    ): Boolean = when (configured) {
        PhoneAutomationTrigger.ScreenOn -> received is PhoneAutomationTrigger.ScreenOn
        PhoneAutomationTrigger.ScreenOff -> received is PhoneAutomationTrigger.ScreenOff
        PhoneAutomationTrigger.ChargingStarted -> received is PhoneAutomationTrigger.ChargingStarted
        PhoneAutomationTrigger.ChargingStopped -> received is PhoneAutomationTrigger.ChargingStopped
        PhoneAutomationTrigger.WifiDisconnected -> received is PhoneAutomationTrigger.WifiDisconnected
        is PhoneAutomationTrigger.WifiConnected ->
            received is PhoneAutomationTrigger.WifiConnected &&
                (configured.ssid == null || configured.ssid == wifiSsid)
        is PhoneAutomationTrigger.BatteryLevel ->
            received is PhoneAutomationTrigger.BatteryLevel &&
                batteryLevel != null &&
                if (configured.belowThreshold) {
                    batteryLevel < configured.threshold
                } else {
                    batteryLevel >= configured.threshold
                }
        is PhoneAutomationTrigger.AppLaunched ->
            received is PhoneAutomationTrigger.AppLaunched &&
                configured.packageName == received.packageName
    }

    private suspend fun executeAction(action: PhoneAutomationAction) {
        try {
            when (action) {
                is PhoneAutomationAction.CallService -> {
                    val integration = serverManager.integrationRepository(action.serverId)
                    integration.callAction(
                        domain = action.domain,
                        action = action.service,
                        actionData = buildServiceData(entityId = action.entityId, serviceDataJson = action.serviceData),
                    )
                    Timber.d("Phone automation executed call_service ${action.domain}.${action.service}")
                }
                is PhoneAutomationAction.FireEvent -> {
                    val integration = serverManager.integrationRepository(action.serverId)
                    integration.fireEvent(
                        eventType = action.eventType,
                        eventData = buildEventData(eventDataJson = action.eventData),
                    )
                    Timber.d("Phone automation executed fire_event ${action.eventType}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute phone automation action")
        }
    }

    private fun buildServiceData(entityId: String?, serviceDataJson: String?): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        if (entityId != null) {
            result["entity_id"] = entityId
        }
        if (serviceDataJson != null) {
            try {
                actionJson.parseToJsonElement(serviceDataJson).jsonObject
                    .forEach { (key, value) -> result[key] = value.jsonPrimitive.content }
            } catch (e: Exception) {
                Timber.w(e, "Could not parse service data JSON")
            }
        }
        return result
    }

    private fun buildEventData(eventDataJson: String?): Map<String, Any> {
        eventDataJson ?: return emptyMap()
        return try {
            actionJson.parseToJsonElement(eventDataJson).jsonObject
                .entries.associate { (key, value) -> key to value.jsonPrimitive.content }
        } catch (e: Exception) {
            Timber.w(e, "Could not parse event data JSON")
            emptyMap()
        }
    }
}
