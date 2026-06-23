package io.homeassistant.companion.android.settings.phoneautomation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.homeassistant.companion.android.common.data.servers.ServerManager
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomation
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationAction
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationRepository
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationTrigger
import io.homeassistant.companion.android.database.server.Server
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val ARG_AUTOMATION_ID = "automation_id"
private const val NEW_AUTOMATION_ID = -1L

@HiltViewModel
class PhoneAutomationDetailViewModel @Inject constructor(
    private val repository: PhoneAutomationRepository,
    private val serverManager: ServerManager,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val automationId: Long = savedState[ARG_AUTOMATION_ID] ?: NEW_AUTOMATION_ID

    private val _uiState = MutableStateFlow(PhoneAutomationDetailUiState())
    val uiState: StateFlow<PhoneAutomationDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val servers = serverManager.servers()
            _uiState.update { it.copy(servers = servers) }

            if (automationId != NEW_AUTOMATION_ID) {
                repository.getById(automationId)?.let { automation ->
                    _uiState.update { state ->
                        state.copy(
                            name = automation.name,
                            isEnabled = automation.isEnabled,
                            trigger = automation.trigger,
                            action = automation.action,
                        )
                    }
                }
            } else if (servers.isNotEmpty()) {
                val defaultServerId = servers.first().id
                _uiState.update { state ->
                    state.copy(
                        action = PhoneAutomationAction.CallService(
                            serverId = defaultServerId,
                            domain = "",
                            service = "",
                        ),
                    )
                }
            }
        }
    }

    fun onNameChanged(name: String) = _uiState.update { it.copy(name = name) }

    fun onEnabledChanged(enabled: Boolean) = _uiState.update { it.copy(isEnabled = enabled) }

    fun onTriggerChanged(trigger: PhoneAutomationTrigger) = _uiState.update { it.copy(trigger = trigger) }

    fun onActionChanged(action: PhoneAutomationAction) = _uiState.update { it.copy(action = action) }

    /**
     * Persists the current state as a new automation or updates the existing one.
     *
     * Does nothing if the form is incomplete (missing name, trigger or action).
     *
     * @param onSuccess Callback invoked on the main thread after a successful save.
     */
    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank() || state.trigger == null || state.action == null) return
        viewModelScope.launch {
            try {
                val automation = PhoneAutomation(
                    id = if (automationId == NEW_AUTOMATION_ID) 0 else automationId,
                    name = state.name.trim(),
                    isEnabled = state.isEnabled,
                    trigger = state.trigger,
                    action = state.action,
                )
                if (automationId == NEW_AUTOMATION_ID) {
                    repository.insert(automation)
                } else {
                    repository.update(automation)
                }
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Timber.e(e, "Failed to save phone automation")
            }
        }
    }
}

data class PhoneAutomationDetailUiState(
    val name: String = "",
    val isEnabled: Boolean = true,
    val trigger: PhoneAutomationTrigger? = null,
    val action: PhoneAutomationAction? = null,
    val servers: List<Server> = emptyList(),
)
