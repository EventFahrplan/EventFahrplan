package nerd.tuxmobil.fahrplan.congress.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.compose.content
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment

class SearchFragment : Fragment() {

    companion object {
        const val FRAGMENT_TAG = "SEARCH_FRAGMENT_TAG"

        fun replaceAtBackStack(fragmentManager: FragmentManager, @IdRes containerViewId: Int) {
            val fragment = SearchFragment()
            fragmentManager.commit {
                fragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
            }
        }
    }

    override fun onAttach(context: Context) {
        require(context is OnSessionListClick) { "$context must implement OnSessionListClick" }
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        EventFahrplanTheme {
            SearchScreen(
                onBack = ::navigateBack,
                onSessionListClick = ::navigateToSession,
            )
        }
    }.also { it.isClickable = true }

    private fun navigateBack() {
        parentFragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun navigateToSession(sessionId: String) {
        (requireContext() as OnSessionListClick).onSessionListClick(sessionId)
    }
}
