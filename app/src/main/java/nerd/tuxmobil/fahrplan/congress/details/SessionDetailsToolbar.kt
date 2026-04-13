package nerd.tuxmobil.fahrplan.congress.details

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.AppBarMenuState
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.AppBarScope
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.GenericFloatingToolbar
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonIcon
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.FloatingActionButtonIcon
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconActionable
import nerd.tuxmobil.fahrplan.congress.designsystem.menues.DropdownMenu
import nerd.tuxmobil.fahrplan.congress.designsystem.menues.DropdownMenuItem
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.AddToCalendar
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Alarm
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Favorite
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Feedback
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Navigate
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Share
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnShareClick
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnShareToChaosflixClick

@Composable
internal fun SessionDetailsToolbar(
    visible: Boolean,
    actions: List<SessionDetailsToolbarAction>,
    useVerticalToolbar: Boolean,
    onViewEvent: (SessionDetailsViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val floatingAction = actions.find { it is Favorite }
    val toolbarActions = actions.filterNot { it is Favorite }
    val menuItems = ToolbarActionMenuItem.of(toolbarActions)
    val floatingActionButton = floatingAction?.let {
        @Composable {
            FloatingActionButtonIcon(
                icon = it.icon,
                contentDescription = it.contentDescription,
                onClick = { it.viewEvent?.let { event -> onViewEvent(event) } },
            )
        }
    }

    GenericFloatingToolbar(
        modifier = modifier,
        visible = visible,
        useVerticalToolbar = useVerticalToolbar,
        floatingActionButton = floatingActionButton,
        toolbarContent = {
            menuItems.forEach { item ->
                toolbarActionItem(
                    action = item.action,
                    label = item.label,
                    onViewEvent = onViewEvent,
                )
            }
        },
    )
}

private fun AppBarScope.toolbarActionItem(
    action: SessionDetailsToolbarAction,
    label: String,
    onViewEvent: (SessionDetailsViewEvent) -> Unit,
) {
    when (action) {
        is Share.ChaosflixSubmenu -> {
            customItem(
                appbarContent = {
                    ShareChaosflixActionButton(
                        action = action,
                        onViewEvent = onViewEvent,
                    )
                },
                menuContent = { menuState ->
                    ShareChaosflixOverflowMenuItems(
                        menuState = menuState,
                        onViewEvent = onViewEvent,
                    )
                },
            )
        }

        else -> {
            val viewEvent = action.viewEvent
            clickableItem(
                onClick = {
                    if (viewEvent != null) {
                        onViewEvent(viewEvent)
                    }
                },
                enabled = viewEvent != null,
                label = label,
                icon = { IconActionable(action) },
            )
        }
    }
}

@Composable
private fun ShareChaosflixActionButton(
    action: Share.ChaosflixSubmenu,
    onViewEvent: (SessionDetailsViewEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ButtonIcon(
            onClick = { expanded = true },
            modifier = Modifier.size(48.dp),
            content = { IconActionable(action) },
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

@Composable
private fun ShareChaosflixOverflowMenuItems(
    menuState: AppBarMenuState,
    onViewEvent: (SessionDetailsViewEvent) -> Unit,
) {
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
}

@Composable
private fun IconActionable(action: SessionDetailsToolbarAction) {
    IconActionable(
        modifier = Modifier.size(EventFahrplanTheme.dimensions.iconSize),
        icon = action.icon,
        contentDescription = action.contentDescription,
    )
}

private data class ToolbarActionMenuItem(
    val action: SessionDetailsToolbarAction,
    val label: String,
) {
    companion object {
        @Composable
        fun of(actions: List<SessionDetailsToolbarAction>) = actions.map {
            ToolbarActionMenuItem(
                action = it,
                label = stringResource(it.contentDescription),
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = UI_MODE_NIGHT_NO,
    name = "Floating toolbar — horizontal",
)
@Composable
private fun SessionDetailsToolbarHorizontalPreview() {
    SessionDetailsToolbar(false)
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = UI_MODE_NIGHT_YES,
    name = "Floating toolbar — vertical",
)
@Composable
private fun SessionDetailsToolbarVerticalPreview() {
    SessionDetailsToolbar(true)
}

@Composable
private fun SessionDetailsToolbar(useVerticalToolbar: Boolean) {
    EventFahrplanTheme {
        SessionDetailsToolbar(
            actions = listOf(
                Favorite(isFavored = false),
                Alarm(hasAlarm = true),
                Feedback,
                AddToCalendar,
                Share.Direct,
                Navigate,
            ),
            onViewEvent = {},
            useVerticalToolbar = useVerticalToolbar,
            visible = true,
        )
    }
}

