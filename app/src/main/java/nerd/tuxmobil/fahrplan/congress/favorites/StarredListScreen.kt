package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.designsystem.dialogs.ConfirmationDialog
import nerd.tuxmobil.fahrplan.congress.extensions.showToast
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListDestination.ConfirmDeleteAll
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListDestination.StarredList
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListEffect.ShareJson
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListEffect.ShareSimple
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListUiState.Success
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.OnDeleteAllClick
import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter.SearchResult
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer

@Composable
fun StarredListScreen(
    viewModel: StarredListViewModel,
    showInSidePane: Boolean,
    onTitleTextChanged: (String) -> Unit,
    onMultiSelectChanged: (Boolean) -> Unit,
    onNavigateToSession: (String) -> Unit,
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current

    val headerTitle = remember { mutableStateOf("") }
    val titleTextChangedState = rememberUpdatedState(onTitleTextChanged)
    val multiSelectChangedState = rememberUpdatedState(onMultiSelectChanged)
    val multiSelectState = remember(context) {
        MultiSelectState(
            resourceResolving = ResourceResolver(context),
            onUpdateTitleText = { text ->
                headerTitle.value = text
                titleTextChangedState.value(text)
            },
            multiSelectTitle = R.string.choose_to_delete,
            defaultTitle = R.string.favorites_screen_default_title,
            sessionsCountPlurals = R.plurals.favorites_screen_title,
        )
    }

    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                is NavigateTo -> navController.navigate(effect.destination.route)
                is NavigateToSession -> onNavigateToSession(effect.sessionId)
                is ShareSimple -> SessionSharer.shareSimple(context, effect.formattedSessions)
                is ShareJson -> if (!SessionSharer.shareJson(context, effect.formattedSessions)) {
                    context.showActivityNotFoundError()
                }
            }
        }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()

    LaunchedEffect(state, selectedIds) {
        if (state is Success && selectedIds.isEmpty()) {
            multiSelectState.updateTitleText((state as Success).parameters.filterIsInstance<SearchResult>().size)
        }
    }
    LaunchedEffect(selectedIds) {
        multiSelectState.onMultiSelectChanged(selectedIds.size) {
            multiSelectChangedState.value(it)
        }
    }

    NavHost(
        navController = navController,
        startDestination = StarredList.route,
    ) {
        composable(StarredList.route) {
            StarredListContent(
                headerTitle = headerTitle.value,
                uiState = state,
                selectedIds = selectedIds,
                showInSidePane = showInSidePane,
                onViewEvent = viewModel::onViewEvent,
            )
        }
        dialog(ConfirmDeleteAll.route) {
            ConfirmationDialog(
                title = stringResource(R.string.dlg_delete_all_favorites),
                confirmationButtonText = stringResource(R.string.dlg_delete_all_favorites_delete_all),
                onConfirm = {
                    viewModel.onViewEvent(OnDeleteAllClick)
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() },
            )
        }
    }
}

private fun Context.showActivityNotFoundError() {
    showToast(R.string.share_error_activity_not_found, showShort = true)
}

