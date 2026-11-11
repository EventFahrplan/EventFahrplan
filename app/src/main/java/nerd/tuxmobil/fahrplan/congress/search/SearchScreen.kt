package nerd.tuxmobil.fahrplan.congress.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.search.SearchEffect.NavigateBack
import nerd.tuxmobil.fahrplan.congress.search.SearchEffect.NavigateToSession

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(context = LocalContext.current),
    ),
    onBack: () -> Unit,
    onSessionItemClick: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                NavigateBack -> onBack()
                is NavigateToSession -> onSessionItemClick(effect.sessionId)
            }
        }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SearchContent(
        state = state,
        onViewEvent = viewModel::onViewEvent,
    )
}
