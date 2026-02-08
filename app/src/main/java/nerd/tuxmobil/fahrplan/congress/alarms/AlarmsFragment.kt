package nerd.tuxmobil.fahrplan.congress.alarms

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.Lifecycle.State.RESUMED
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.OnSessionItemClickListener
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments

class AlarmsFragment : Fragment(), MenuProvider {

    companion object {
        const val FRAGMENT_TAG = "ALARMS_FRAGMENT_TAG"

        fun replaceAtBackStack(
            fragmentManager: FragmentManager,
            @IdRes containerViewId: Int,
            sidePane: Boolean
        ) {
            val fragment = AlarmsFragment().withArguments(
                BundleKeys.SIDEPANE to sidePane
            )
            fragmentManager.commit {
                fragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
            }
        }
    }

    private val viewModel: AlarmsViewModel by viewModels { AlarmsViewModelFactory(requireContext()) }
    private var sidePane = false
    private var onSessionItemClickListener: OnSessionItemClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.screenNavigation = ScreenNavigation { sessionId ->
            onSessionItemClickListener?.onSessionItemClick(sessionId)
        }
        onSessionItemClickListener = try {
            context as OnSessionItemClickListener
        } catch (_: ClassCastException) {
            error("$context must implement OnSessionItemClickListener")
        }
    }

    override fun onDetach() {
        onSessionItemClickListener = null
        viewModel.screenNavigation = null
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sidePane = it.getBoolean(BundleKeys.SIDEPANE)
        }
        requireActivity().addMenuProvider(this, this, RESUMED)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        EventFahrplanTheme {
            AlarmsScreen(
                viewModel = viewModel,
                showInSidePane = sidePane,
            )
        }
    }.also { it.isClickable = true }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.alarms_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_item_delete_all_alarms -> viewModel.onDeleteAllClick()
            else -> return false
        }
        return true
    }

}

