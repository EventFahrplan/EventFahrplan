package nerd.tuxmobil.fahrplan.congress.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments

class SearchFragment : Fragment() {

    companion object {
        const val FRAGMENT_TAG = "SEARCH_FRAGMENT_TAG"

        fun replaceAtBackStack(fragmentManager: FragmentManager, @IdRes containerViewId: Int, sidePane: Boolean) {
            val fragment = SearchFragment().withArguments(
                BundleKeys.SIDEPANE to sidePane
            )
            fragmentManager.commit {
                fragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
            }
        }
    }

    private var sidePane = false
    private val viewModelFactory by lazy { SearchViewModelFactory(context = requireContext()) }
    private val viewModel: SearchViewModel by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        require(context is OnSessionListClick) { "$context must implement OnSessionListClick" }
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sidePane = it.getBoolean(BundleKeys.SIDEPANE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        with(viewModel) {
            SearchScreen(
                searchQuery = searchQuery,
                searchHistory = searchHistory.collectAsState(emptyList()).value,
                state = searchResultsState.collectAsState().value,
                onViewEvent = ::onViewEvent,
            )
        }
    }.also { it.isClickable = true }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.effects.observe(viewLifecycleOwner) { effect ->
            when (effect) {
                SearchEffect.NavigateBack -> navigateBack()
                is SearchEffect.NavigateToSession -> navigateToSession(effect.sessionId)
            }
        }
    }

    private fun navigateBack() {
        parentFragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        if (!sidePane) {
            requireActivity().finish()
        }
    }

    private fun navigateToSession(sessionId: String) {
        (requireContext() as OnSessionListClick).onSessionListClick(sessionId)
    }
}
