package io.homeassistant.companion.android.settings.phoneautomation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomation
import io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PhoneAutomationsViewModel @Inject constructor(
    private val repository: PhoneAutomationRepository,
) : ViewModel() {

    val automations: StateFlow<List<PhoneAutomation>> = repository.getAllFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    /**
     * Toggles the enabled state of the automation with the given [id].
     */
    fun toggleEnabled(id: Long, currentlyEnabled: Boolean) {
        viewModelScope.launch {
            val automation = repository.getById(id) ?: return@launch
            repository.update(automation.copy(isEnabled = !currentlyEnabled))
        }
    }

    /**
     * Permanently removes the automation with the given [id].
     */
    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
