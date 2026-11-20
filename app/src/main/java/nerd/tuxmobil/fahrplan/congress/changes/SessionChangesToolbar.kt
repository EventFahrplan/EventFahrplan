package nerd.tuxmobil.fahrplan.congress.changes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnFavoritesFilterToggle
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.GenericFloatingToolbar
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconFilter
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
internal fun SessionChangesToolbar(
    visible: Boolean,
    favoritesFilterEnabled: Boolean,
    useVerticalToolbar: Boolean,
    modifier: Modifier = Modifier,
    onViewEvent: (SessionChangeViewEvent) -> Unit,
) {
    val favoritesFilterLabel = stringResource(R.string.schedule_changes_favorites_filter)
    GenericFloatingToolbar(
        visible = visible,
        useVerticalToolbar = useVerticalToolbar,
        modifier = modifier,
        toolbarContent = {
            toggleableItem(
                checked = favoritesFilterEnabled,
                onCheckedChange = { onViewEvent(OnFavoritesFilterToggle(enabled = it)) },
                icon = {
                    IconFilter(
                        enabled = favoritesFilterEnabled,
                        contentDescription = R.string.schedule_changes_favorites_filter,
                    )
                },
                label = favoritesFilterLabel,
            )
        },
    )
}

@PreviewLightDark
@Composable
private fun SessionChangesToolbarHorizontalPreview() {
    SessionChangesToolbar(useVerticalToolbar = false)
}

@PreviewLightDark
@Composable
private fun SessionChangesToolbarVerticalPreview() {
    SessionChangesToolbar(useVerticalToolbar = true)
}

@Composable
private fun SessionChangesToolbar(useVerticalToolbar: Boolean) {
    EventFahrplanTheme {
        SessionChangesToolbar(
            visible = true,
            favoritesFilterEnabled = true,
            useVerticalToolbar = useVerticalToolbar,
            onViewEvent = {},
        )
    }
}
