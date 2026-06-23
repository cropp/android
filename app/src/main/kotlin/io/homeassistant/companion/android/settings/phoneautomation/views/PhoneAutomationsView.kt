package io.homeassistant.companion.android.settings.phoneautomation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.homeassistant.companion.android.common.R as commonR
import io.homeassistant.companion.android.common.compose.theme.HAThemeForPreview
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomation
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationAction
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationTrigger
import io.homeassistant.companion.android.settings.phoneautomation.PhoneAutomationsViewModel

@Composable
fun PhoneAutomationsView(
    viewModel: PhoneAutomationsViewModel,
    onAddAutomation: () -> Unit,
    onEditAutomation: (Long) -> Unit,
) {
    val automations by viewModel.automations.collectAsStateWithLifecycle()
    PhoneAutomationsContent(
        automations = automations,
        onAddAutomation = onAddAutomation,
        onEditAutomation = onEditAutomation,
        onToggleEnabled = { id, enabled -> viewModel.toggleEnabled(id, enabled) },
        onDelete = { viewModel.delete(it) },
    )
}

@Composable
internal fun PhoneAutomationsContent(
    automations: List<PhoneAutomation>,
    onAddAutomation: () -> Unit,
    onEditAutomation: (Long) -> Unit,
    onToggleEnabled: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAutomation) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(commonR.string.phone_automation_add),
                )
            }
        },
    ) { innerPadding ->
        if (automations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(commonR.string.phone_automations_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = automations, key = { it.id }) { automation ->
                    PhoneAutomationItem(
                        automation = automation,
                        onToggleEnabled = { onToggleEnabled(automation.id, automation.isEnabled) },
                        onEdit = { onEditAutomation(automation.id) },
                        onDelete = { onDelete(automation.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneAutomationItem(
    automation: PhoneAutomation,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit,
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = automation.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = automation.isEnabled,
                    onCheckedChange = { onToggleEnabled() },
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(commonR.string.phone_automation_delete),
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = stringResource(
                    commonR.string.phone_automation_trigger_label,
                    automation.trigger.displayName(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    commonR.string.phone_automation_action_label,
                    automation.action.displayName(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
internal fun PhoneAutomationTrigger.displayName(): String = when (this) {
    PhoneAutomationTrigger.ScreenOn -> stringResource(commonR.string.phone_automation_trigger_screen_on)
    PhoneAutomationTrigger.ScreenOff -> stringResource(commonR.string.phone_automation_trigger_screen_off)
    PhoneAutomationTrigger.ChargingStarted -> stringResource(commonR.string.phone_automation_trigger_charging_started)
    PhoneAutomationTrigger.ChargingStopped -> stringResource(commonR.string.phone_automation_trigger_charging_stopped)
    is PhoneAutomationTrigger.WifiConnected -> if (this.ssid != null) {
        stringResource(commonR.string.phone_automation_trigger_wifi_connected_ssid, this.ssid)
    } else {
        stringResource(commonR.string.phone_automation_trigger_wifi_connected)
    }
    PhoneAutomationTrigger.WifiDisconnected -> stringResource(commonR.string.phone_automation_trigger_wifi_disconnected)
    is PhoneAutomationTrigger.BatteryLevel -> if (this.belowThreshold) {
        stringResource(commonR.string.phone_automation_trigger_battery_below, this.threshold)
    } else {
        stringResource(commonR.string.phone_automation_trigger_battery_above, this.threshold)
    }
    is PhoneAutomationTrigger.AppLaunched -> stringResource(
        commonR.string.phone_automation_trigger_app_launched,
        this.packageName,
    )
}

@Composable
internal fun PhoneAutomationAction.displayName(): String = when (this) {
    is PhoneAutomationAction.CallService ->
        stringResource(commonR.string.phone_automation_action_call_service, this.domain, this.service)
    is PhoneAutomationAction.FireEvent ->
        stringResource(commonR.string.phone_automation_action_fire_event, this.eventType)
}

@Preview
@Composable
private fun PhoneAutomationsContentPreview() {
    HAThemeForPreview {
        PhoneAutomationsContent(
            automations = listOf(
                PhoneAutomation(
                    id = 1,
                    name = "Lights on when screen on",
                    isEnabled = true,
                    trigger = PhoneAutomationTrigger.ScreenOn,
                    action = PhoneAutomationAction.CallService(
                        serverId = 1,
                        domain = "light",
                        service = "turn_on",
                        entityId = "light.living_room",
                    ),
                ),
                PhoneAutomation(
                    id = 2,
                    name = "Notify when charging",
                    isEnabled = false,
                    trigger = PhoneAutomationTrigger.ChargingStarted,
                    action = PhoneAutomationAction.FireEvent(
                        serverId = 1,
                        eventType = "charging_started",
                    ),
                ),
            ),
            onAddAutomation = {},
            onEditAutomation = {},
            onToggleEnabled = { _, _ -> },
            onDelete = {},
        )
    }
}
