package nerd.tuxmobil.fahrplan.congress.about

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ExternalNavigation
import nerd.tuxmobil.fahrplan.congress.commons.ExternalNavigator
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving

class AboutDialog : DialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "AboutDialog"
    }

    private lateinit var resourceResolving: ResourceResolving
    private lateinit var externalNavigation: ExternalNavigation
    private val viewModel: AboutViewModel by viewModels {
        AboutViewModelFactory(resourceResolving, externalNavigation)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        resourceResolving = ResourceResolver(context)
        externalNavigation = ExternalNavigator(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.about_dialog, container, false).apply {
        findViewById<ComposeView>(R.id.about_view).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AboutScreen(
                    parameter = viewModel.aboutParameter.collectAsState().value,
                    onViewEvent = viewModel::onViewEvent,
                )
            }
            isClickable = true
        }
    }

}
