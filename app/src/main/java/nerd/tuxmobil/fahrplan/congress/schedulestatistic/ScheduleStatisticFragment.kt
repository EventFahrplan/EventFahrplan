package nerd.tuxmobil.fahrplan.congress.schedulestatistic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

class ScheduleStatisticFragment : Fragment() {

    companion object {
        const val FRAGMENT_TAG = "SCHEDULE_STATISTIC_FRAGMENT_TAG"
    }

    private val viewModel = ScheduleStatisticViewModel(AppRepository)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        ScheduleStatisticScreen(
            state = viewModel.scheduleStatisticState.collectAsState().value,
            onViewEvent = viewModel::onViewEvent,
        )
    }.also { it.isClickable = true }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val typeMask = systemBars() or displayCutout() or ime()
            val insets = windowInsets.getInsets(typeMask)
            WindowInsetsCompat.Builder()
                .setInsets(typeMask, insets)
                .build()
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.navigateBack.observe(viewLifecycleOwner) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

}
