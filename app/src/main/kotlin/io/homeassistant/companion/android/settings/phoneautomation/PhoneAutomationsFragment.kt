package io.homeassistant.companion.android.settings.phoneautomation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.homeassistant.companion.android.R
import io.homeassistant.companion.android.common.R as commonR
import io.homeassistant.companion.android.common.compose.theme.HATheme
import io.homeassistant.companion.android.settings.phoneautomation.views.PhoneAutomationsView

/**
 * Displays the list of user-defined phone automations.
 *
 * Navigation to the create/edit screen is done by committing [PhoneAutomationDetailFragment] into
 * the parent activity's content frame.
 */
@AndroidEntryPoint
class PhoneAutomationsFragment : Fragment() {

    private val viewModel: PhoneAutomationsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            HATheme {
                PhoneAutomationsView(
                    viewModel = viewModel,
                    onAddAutomation = { openDetail(automationId = null) },
                    onEditAutomation = { openDetail(automationId = it) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(commonR.string.phone_automations)
    }

    private fun openDetail(automationId: Long?) {
        parentFragmentManager.commit {
            replace(R.id.content, PhoneAutomationDetailFragment.newInstance(automationId))
            addToBackStack(null)
        }
    }
}
