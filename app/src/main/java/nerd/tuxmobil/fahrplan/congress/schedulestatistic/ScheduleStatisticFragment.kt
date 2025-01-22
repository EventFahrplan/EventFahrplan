package nerd.tuxmobil.fahrplan.congress.schedulestatistic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.navigateBack.observe(viewLifecycleOwner) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

}
