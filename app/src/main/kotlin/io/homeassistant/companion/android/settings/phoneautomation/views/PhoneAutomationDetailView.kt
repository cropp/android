@file:OptIn(ExperimentalMaterial3Api::class)

package io.homeassistant.companion.android.settings.phoneautomation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.homeassistant.companion.android.common.R as commonR
import io.homeassistant.companion.android.common.compose.theme.HAThemeForPreview
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationAction
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationTrigger
import io.homeassistant.companion.android.database.server.Server
import io.homeassistant.companion.android.settings.phoneautomation.PhoneAutomationDetailViewModel

@Composable
fun PhoneAutomationDetailView(
    viewModel: PhoneAutomationDetailViewModel,
    onSaved: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PhoneAutomationDetailContent(
        name = state.name,
        isEnabled = state.isEnabled,
        selectedTrigger = state.trigger,
        selectedAction = state.action,
        servers = state.servers,
        onNameChanged = viewModel::onNameChanged,
        onEnabledChanged = viewModel::onEnabledChanged,
        onTriggerChanged = viewModel::onTriggerChanged,
        onActionChanged = viewModel::onActionChanged,
        onSave = { viewModel.save(onSuccess = onSaved) },
    )
}

@Suppress("LongParameterList")
@Composable
internal fun PhoneAutomationDetailContent(
    name: String,
    isEnabled: Boolean,
    selectedTrigger: PhoneAutomationTrigger?,
    selectedAction: PhoneAutomationAction?,
    servers: List<Server>,
    onNameChanged: (String) -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onTriggerChanged: (PhoneAutomationTrigger) -> Unit,
    onActionChanged: (PhoneAutomationAction) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(commonR.string.phone_automation_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(commonR.string.phone_automation_enabled),
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(checked = isEnabled, onCheckedChange = onEnabledChanged)
        }

        HorizontalDivider()

        Text(
            text = stringResource(commonR.string.phone_automation_trigger_section),
            style = MaterialTheme.typography.titleSmall,
        )

        TriggerPicker(
            selectedTrigger = selectedTrigger,
            onTriggerChanged = onTriggerChanged,
        )

        HorizontalDivider()

        Text(
            text = stringResource(commonR.string.phone_automation_action_section),
            style = MaterialTheme.typography.titleSmall,
        )

        ActionPicker(
            selectedAction = selectedAction,
            servers = servers,
            onActionChanged = onActionChanged,
        )

        Button(
            onClick = onSave,
            enabled = name.isNotBlank() && selectedTrigger != null && selectedAction != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(commonR.string.phone_automation_save))
        }
    }
}

@Composable
private fun TriggerPicker(
    selectedTrigger: PhoneAutomationTrigger?,
    onTriggerChanged: (PhoneAutomationTrigger) -> Unit,
) {
    val triggerOptions = listOf(
        PhoneAutomationTrigger.ScreenOn,
        PhoneAutomationTrigger.ScreenOff,
        PhoneAutomationTrigger.ChargingStarted,
        PhoneAutomationTrigger.ChargingStopped,
        PhoneAutomationTrigger.WifiConnected(),
        PhoneAutomationTrigger.WifiDisconnected,
        PhoneAutomationTrigger.BatteryLevel(threshold = 20, belowThreshold = true),
        PhoneAutomationTrigger.AppLaunched(packageName = ""),
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selectedTrigger?.baseDisplayName() ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(commonR.string.phone_automation_trigger)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            triggerOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.baseDisplayName()) },
                    onClick = {
                        onTriggerChanged(option)
                        expanded = false
                    },
                )
            }
        }
    }

    when (val trigger = selectedTrigger) {
        is PhoneAutomationTrigger.WifiConnected -> {
            OutlinedTextField(
                value = trigger.ssid ?: "",
                onValueChange = { ssid ->
                    onTriggerChanged(PhoneAutomationTrigger.WifiConnected(ssid = ssid.takeIf { it.isNotBlank() }))
                },
                label = { Text(stringResource(commonR.string.phone_automation_trigger_wifi_ssid)) },
                placeholder = { Text(stringResource(commonR.string.phone_automation_trigger_wifi_ssid_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        is PhoneAutomationTrigger.BatteryLevel -> {
            OutlinedTextField(
                value = trigger.threshold.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.coerceIn(0, 100)?.let { level ->
                        onTriggerChanged(trigger.copy(threshold = level))
                    }
                },
                label = { Text(stringResource(commonR.string.phone_automation_trigger_battery_threshold)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(commonR.string.phone_automation_trigger_battery_below_toggle),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = trigger.belowThreshold,
                    onCheckedChange = { onTriggerChanged(trigger.copy(belowThreshold = it)) },
                )
            }
        }
        is PhoneAutomationTrigger.AppLaunched -> {
            OutlinedTextField(
                value = trigger.packageName,
                onValueChange = { onTriggerChanged(PhoneAutomationTrigger.AppLaunched(packageName = it)) },
                label = { Text(stringResource(commonR.string.phone_automation_trigger_app_package)) },
                placeholder = { Text("com.example.app") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        else -> Unit
    }
}

@Composable
private fun ActionPicker(
    selectedAction: PhoneAutomationAction?,
    servers: List<Server>,
    onActionChanged: (PhoneAutomationAction) -> Unit,
) {
    val defaultServerId = servers.firstOrNull()?.id ?: 0
    val actionTypeOptions = listOf(
        stringResource(commonR.string.phone_automation_action_type_call_service),
        stringResource(commonR.string.phone_automation_action_type_fire_event),
    )
    val selectedActionTypeIndex = when (selectedAction) {
        is PhoneAutomationAction.CallService -> 0
        is PhoneAutomationAction.FireEvent -> 1
        null -> -1
    }

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = if (selectedActionTypeIndex >= 0) actionTypeOptions[selectedActionTypeIndex] else "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(commonR.string.phone_automation_action_type)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(actionTypeOptions[0]) },
                onClick = {
                    val existing = selectedAction as? PhoneAutomationAction.CallService
                    onActionChanged(
                        PhoneAutomationAction.CallService(
                            serverId = existing?.serverId ?: defaultServerId,
                            domain = existing?.domain ?: "",
                            service = existing?.service ?: "",
                        ),
                    )
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(actionTypeOptions[1]) },
                onClick = {
                    val existing = selectedAction as? PhoneAutomationAction.FireEvent
                    onActionChanged(
                        PhoneAutomationAction.FireEvent(
                            serverId = existing?.serverId ?: defaultServerId,
                            eventType = existing?.eventType ?: "",
                        ),
                    )
                    expanded = false
                },
            )
        }
    }

    when (val action = selectedAction) {
        is PhoneAutomationAction.CallService -> {
            if (servers.size > 1) {
                ServerDropdown(
                    servers = servers,
                    selectedServerId = action.serverId,
                    onServerSelected = { onActionChanged(action.copy(serverId = it)) },
                )
            }
            OutlinedTextField(
                value = action.domain,
                onValueChange = { onActionChanged(action.copy(domain = it)) },
                label = { Text(stringResource(commonR.string.phone_automation_action_domain)) },
                placeholder = { Text("light") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = action.service,
                onValueChange = { onActionChanged(action.copy(service = it)) },
                label = { Text(stringResource(commonR.string.phone_automation_action_service)) },
                placeholder = { Text("turn_on") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = action.entityId ?: "",
                onValueChange = { onActionChanged(action.copy(entityId = it.takeIf { s -> s.isNotBlank() })) },
                label = { Text(stringResource(commonR.string.phone_automation_action_entity_id)) },
                placeholder = { Text("light.living_room") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = action.serviceData ?: "",
                onValueChange = { onActionChanged(action.copy(serviceData = it.takeIf { s -> s.isNotBlank() })) },
                label = { Text(stringResource(commonR.string.phone_automation_action_service_data)) },
                placeholder = { Text("{\"brightness\": 128}") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        is PhoneAutomationAction.FireEvent -> {
            if (servers.size > 1) {
                ServerDropdown(
                    servers = servers,
                    selectedServerId = action.serverId,
                    onServerSelected = { onActionChanged(action.copy(serverId = it)) },
                )
            }
            OutlinedTextField(
                value = action.eventType,
                onValueChange = { onActionChanged(action.copy(eventType = it)) },
                label = { Text(stringResource(commonR.string.phone_automation_action_event_type)) },
                placeholder = { Text("custom_event") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = action.eventData ?: "",
                onValueChange = { onActionChanged(action.copy(eventData = it.takeIf { s -> s.isNotBlank() })) },
                label = { Text(stringResource(commonR.string.phone_automation_action_event_data)) },
                placeholder = { Text("{\"key\": \"value\"}") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        null -> Unit
    }
}

@Composable
private fun ServerDropdown(
    servers: List<Server>,
    selectedServerId: Int,
    onServerSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedServer = servers.find { it.id == selectedServerId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selectedServer?.friendlyName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(commonR.string.phone_automation_action_server)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            servers.forEach { server ->
                DropdownMenuItem(
                    text = { Text(server.friendlyName) },
                    onClick = {
                        onServerSelected(server.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PhoneAutomationTrigger.baseDisplayName(): String = when (this) {
    PhoneAutomationTrigger.ScreenOn -> stringResource(commonR.string.phone_automation_trigger_screen_on)
    PhoneAutomationTrigger.ScreenOff -> stringResource(commonR.string.phone_automation_trigger_screen_off)
    PhoneAutomationTrigger.ChargingStarted -> stringResource(commonR.string.phone_automation_trigger_charging_started)
    PhoneAutomationTrigger.ChargingStopped -> stringResource(commonR.string.phone_automation_trigger_charging_stopped)
    is PhoneAutomationTrigger.WifiConnected -> stringResource(commonR.string.phone_automation_trigger_wifi_connected)
    PhoneAutomationTrigger.WifiDisconnected -> stringResource(commonR.string.phone_automation_trigger_wifi_disconnected)
    is PhoneAutomationTrigger.BatteryLevel -> stringResource(commonR.string.phone_automation_trigger_battery_level)
    is PhoneAutomationTrigger.AppLaunched -> stringResource(commonR.string.phone_automation_trigger_app_launched_base)
}

@Preview
@Composable
private fun PhoneAutomationDetailContentPreview() {
    HAThemeForPreview {
        PhoneAutomationDetailContent(
            name = "Morning lights",
            isEnabled = true,
            selectedTrigger = PhoneAutomationTrigger.WifiConnected(ssid = "HomeNetwork"),
            selectedAction = PhoneAutomationAction.CallService(
                serverId = 1,
                domain = "light",
                service = "turn_on",
                entityId = "light.kitchen",
            ),
            servers = emptyList(),
            onNameChanged = {},
            onEnabledChanged = {},
            onTriggerChanged = {},
            onActionChanged = {},
            onSave = {},
        )
    }
}
