package io.homeassistant.companion.android.settings.phoneautomation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.homeassistant.companion.android.common.R as commonR
import io.homeassistant.companion.android.common.compose.theme.HATheme
import io.homeassistant.companion.android.settings.phoneautomation.views.PhoneAutomationDetailView

private const val ARG_AUTOMATION_ID = "automation_id"

/**
 * Create or edit a single phone automation.
 *
 * Pass a non-null [automationId] to edit an existing one; pass null to create a new one.
 */
@AndroidEntryPoint
class PhoneAutomationDetailFragment : Fragment() {

    companion object {
        /** Creates an instance for creating a new automation (when [automationId] is null) or editing one. */
        fun newInstance(automationId: Long?): PhoneAutomationDetailFragment =
            PhoneAutomationDetailFragment().apply {
                arguments = bundleOf(ARG_AUTOMATION_ID to (automationId ?: -1L))
            }
    }

    private val viewModel: PhoneAutomationDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            HATheme {
                PhoneAutomationDetailView(
                    viewModel = viewModel,
                    onSaved = { parentFragmentManager.popBackStack() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val isEditing = (arguments?.getLong(ARG_AUTOMATION_ID, -1L) ?: -1L) != -1L
        activity?.title = if (isEditing) {
            getString(commonR.string.phone_automation_edit)
        } else {
            getString(commonR.string.phone_automation_add)
        }
    }
}
