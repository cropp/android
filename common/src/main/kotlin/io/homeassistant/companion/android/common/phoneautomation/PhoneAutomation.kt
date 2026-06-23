package io.homeassistant.companion.android.common.phoneautomation

/**
 * Represents a user-defined automation rule that monitors a [PhoneAutomationTrigger] and executes
 * a [PhoneAutomationAction] when the trigger condition is met.
 */
data class PhoneAutomation(
    val id: Long = 0,
    val name: String,
    val isEnabled: Boolean,
    val trigger: PhoneAutomationTrigger,
    val action: PhoneAutomationAction,
)
