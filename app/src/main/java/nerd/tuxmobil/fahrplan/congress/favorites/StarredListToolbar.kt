package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.BuildConfigProvider
import nerd.tuxmobil.fahrplan.congress.commons.BuildConfigProvision
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.FloatingToolbarDefaults.floatingToolbarColors
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.GenericFloatingToolbar
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonIcon
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDelete
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDeleteAll
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconShare
import nerd.tuxmobil.fahrplan.congress.designsystem.menues.DropdownMenu
import nerd.tuxmobil.fahrplan.congress.designsystem.menues.DropdownMenuItem
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.Multiselect.OnDeleteSelectedClick
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.OnDeleteAllWithConfirmationClick
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.OnShareClick
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.OnShareToChaosflixClick

@Composable
internal fun StarredListToolbar(
    visible: Boolean,
    multiselect: Boolean,
    hasStarredSessions: Boolean,
    useVerticalToolbar: Boolean,
    modifier: Modifier = Modifier,
    buildConfigProvision: BuildConfigProvision = BuildConfigProvider(),
    onViewEvent: (StarredListViewEvent) -> Unit,
) {
    @Composable
    fun colors() = when (multiselect) {
        true -> floatingToolbarColors(
            toolbarContainerColor = EventFahrplanTheme.colorScheme.multiChoiceBackground,
            fabContainerColor = EventFahrplanTheme.colorScheme.multiChoiceBackground,
        )

        false -> floatingToolbarColors()
    }

    val deleteSelectedLabel = stringResource(R.string.menu_item_title_delete_favorite)
    val deleteAllLabel = stringResource(R.string.menu_item_title_delete_all)
    val shareLabel = stringResource(R.string.menu_item_title_share_favorites)

    GenericFloatingToolbar(
        visible = visible,
        useVerticalToolbar = useVerticalToolbar,
        modifier = modifier,
        floatingActionButton = null,
        colors = colors(),
        toolbarContent = {
            if (multiselect) {
                clickableItem(
                    icon = { IconDelete() },
                    enabled = true,
                    label = deleteSelectedLabel,
                    onClick = { onViewEvent(OnDeleteSelectedClick) },
                )
            } else if (hasStarredSessions) {
                if (buildConfigProvision.enableChaosflixExport) {
                    customItem(
                        appbarContent = {
                            ShareFavoritesChaosflixButton(onViewEvent)
                        },
                        menuContent = { menuState ->
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_as_text)) },
                                onClick = {
                                    menuState.dismiss()
                                    onViewEvent(OnShareClick)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_as_json)) },
                                onClick = {
                                    menuState.dismiss()
                                    onViewEvent(OnShareToChaosflixClick)
                                },
                            )
                        },
                    )
                } else {
                    clickableItem(
                        icon = { IconShare() },
                        enabled = true,
                        label = shareLabel,
                        onClick = { onViewEvent(OnShareClick) },
                    )
                }
                clickableItem(
                    icon = { IconDeleteAll() },
                    enabled = true,
                    label = deleteAllLabel,
                    onClick = { onViewEvent(OnDeleteAllWithConfirmationClick) },
                )
            }
        },
    )
}

@Composable
private fun ShareFavoritesChaosflixButton(
    onViewEvent: (StarredListViewEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ButtonIcon(
            modifier = Modifier.size(48.dp),
            content = { IconShare() },
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.share_as_text)) },
                onClick = {
                    expanded = false
                    onViewEvent(OnShareClick)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.share_as_json)) },
                onClick = {
                    expanded = false
                    onViewEvent(OnShareToChaosflixClick)
                },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun StarredListToolbarHorizontalPreview() {
    StarredListToolbar(useVerticalToolbar = false, multiselect = false)
}

@PreviewLightDark
@Composable
private fun StarredListToolbarVerticalPreview() {
    StarredListToolbar(useVerticalToolbar = true, multiselect = false)
}

@PreviewLightDark
@Composable
private fun StarredListToolbarMultiselectPreview() {
    StarredListToolbar(useVerticalToolbar = false, multiselect = true)
}

@Composable
private fun StarredListToolbar(
    useVerticalToolbar: Boolean,
    multiselect: Boolean,
) {
    EventFahrplanTheme {
        StarredListToolbar(
            visible = true,
            multiselect = multiselect,
            hasStarredSessions = true,
            useVerticalToolbar = useVerticalToolbar,
            onViewEvent = {},
        )
    }
}
