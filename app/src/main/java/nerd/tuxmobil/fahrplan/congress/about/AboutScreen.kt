package nerd.tuxmobil.fahrplan.congress.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun AboutScreen() {
    val context = LocalContext.current
    val viewModel = viewModel<AboutViewModel>(factory = AboutViewModelFactory(context))
    val state by viewModel.aboutParameter.collectAsStateWithLifecycle()

    AboutContent(
        parameter = state,
        onViewEvent = viewModel::onViewEvent,
    )
}
