package io.homeassistant.companion.android.common.phoneautomation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the action to execute when a [PhoneAutomationTrigger] fires.
 *
 * Actions communicate with the Home Assistant server. The serializable hierarchy allows actions to
 * be stored as JSON in the Room database.
 */
@Serializable
sealed interface PhoneAutomationAction {

    /**
     * Calls a Home Assistant service action.
     *
     * @param serverId The ID of the HA server to call the service on.
     * @param domain The service domain (e.g. "light", "switch", "script").
     * @param service The service name within the domain (e.g. "turn_on", "toggle").
     * @param entityId Optional entity ID to target. When null the service is called without a
     *   target entity and the [serviceData] may define one instead.
     * @param serviceData Optional JSON string with additional service data fields. Must be a valid
     *   JSON object string when non-null (e.g. `{"brightness": 128}`).
     */
    @Serializable
    @SerialName("call_service")
    data class CallService(
        val serverId: Int,
        val domain: String,
        val service: String,
        val entityId: String? = null,
        val serviceData: String? = null,
    ) : PhoneAutomationAction

    /**
     * Fires a custom Home Assistant event.
     *
     * @param serverId The ID of the HA server to fire the event on.
     * @param eventType The event type string (e.g. "custom_component.my_event").
     * @param eventData Optional JSON string with event payload data. Must be a valid JSON object
     *   string when non-null.
     */
    @Serializable
    @SerialName("fire_event")
    data class FireEvent(
        val serverId: Int,
        val eventType: String,
        val eventData: String? = null,
    ) : PhoneAutomationAction
}
