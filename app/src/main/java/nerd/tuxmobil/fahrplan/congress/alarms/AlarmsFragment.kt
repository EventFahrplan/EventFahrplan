package nerd.tuxmobil.fahrplan.congress.alarms

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.OnSessionItemClickListener
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

class AlarmsFragment : Fragment() {

    companion object {
        const val FRAGMENT_TAG = "ALARMS_FRAGMENT_TAG"

        fun replace(
            fragmentManager: FragmentManager,
            @IdRes containerViewId: Int,
            sidePane: Boolean
        ) {
            val fragment = AlarmsFragment().withArguments(
                BundleKeys.SIDEPANE to sidePane
            )
            fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
        }
    }

    private lateinit var appRepository: AppRepository
    private lateinit var resourceResolving: ResourceResolving
    private lateinit var alarmServices: AlarmServices
    private lateinit var screenNavigation: ScreenNavigation
    private val viewModel: AlarmsViewModel by viewModels {
        AlarmsViewModelFactory(appRepository, resourceResolving, alarmServices, screenNavigation)
    }
    private var sidePane = false
    private var onSessionItemClickListener: OnSessionItemClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appRepository = AppRepository
        resourceResolving = ResourceResolver(context)
        alarmServices = AlarmServices.newInstance(context, appRepository)
        screenNavigation = ScreenNavigation { sessionId ->
            onSessionItemClickListener?.onSessionItemClick(sessionId)
        }
        onSessionItemClickListener = try {
            context as OnSessionItemClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement ClassCastException")
        }
    }

    override fun onDetach() {
        onSessionItemClickListener = null
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sidePane = it.getBoolean(BundleKeys.SIDEPANE)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_alarms, container, false).apply {
        findViewById<ComposeView>(R.id.alarms_view).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AlarmsScreen(
                    state = viewModel.alarmsState.collectAsState().value,
                    showInSidePane = sidePane,
                )
            }
            isClickable = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.alarms_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_item_delete_all_alarms -> {
            viewModel.onDeleteAllClick()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

}

