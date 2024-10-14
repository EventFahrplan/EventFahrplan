package nerd.tuxmobil.fahrplan.congress.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter

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
    private var onSessionListClickListener: OnSessionListClick? = null
    private val viewModelFactory by lazy {
        val resourceResolving = ResourceResolver(requireContext())
        SearchViewModelFactory(
            appRepository = AppRepository,
            resourceResolving = resourceResolving,
            sessionPropertiesFormatter = SessionPropertiesFormatter(),
            contentDescriptionFormatter = ContentDescriptionFormatter(resourceResolving),
        )

    }
    private val viewModel: SearchViewModel by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.screenNavigation = ScreenNavigation { sessionId ->
            onSessionListClickListener?.onSessionListClick(sessionId)
        }
        if (context is OnSessionListClick) {
            onSessionListClickListener = context
        } else {
            error("$context must implement OnSessionListClick")
        }
    }

    override fun onDetach() {
        onSessionListClickListener = null
        viewModel.screenNavigation = null
        super.onDetach()
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
    ): View = inflater.inflate(R.layout.fragment_search, container, false).apply {
        findViewById<ComposeView>(R.id.search_view).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                with(viewModel) {
                    SearchScreen(
                        searchQuery = searchQuery,
                        state = searchResultsState.collectAsState().value,
                        onViewEvent = ::onViewEvent,
                    )
                }
            }
            isClickable = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.navigateBack.observe(viewLifecycleOwner) {
            parentFragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            if (!sidePane) {
                requireActivity().finish()
            }
        }
    }

}
