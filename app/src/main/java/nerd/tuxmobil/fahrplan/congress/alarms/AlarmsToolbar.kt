package nerd.tuxmobil.fahrplan.congress.alarms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnDeleteAllWithConfirmationClick
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.GenericFloatingToolbar
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDeleteAll
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
internal fun AlarmsToolbar(
    visible: Boolean,
    useVerticalToolbar: Boolean,
    modifier: Modifier = Modifier,
    onViewEvent: (AlarmsViewEvent) -> Unit,
) {
    val deleteAllLabel = stringResource(R.string.menu_item_title_delete_all)
    GenericFloatingToolbar(
        visible = visible,
        useVerticalToolbar = useVerticalToolbar,
        modifier = modifier,
        toolbarContent = {
            clickableItem(
                onClick = { onViewEvent(OnDeleteAllWithConfirmationClick) },
                enabled = true,
                label = deleteAllLabel,
                icon = { IconDeleteAll() },
            )
        },
    )
}

@PreviewLightDark
@Composable
private fun AlarmsToolbarHorizontalPreview() {
    AlarmsToolbar(useVerticalToolbar = false)
}

@PreviewLightDark
@Composable
private fun AlarmsToolbarVerticalPreview() {
    AlarmsToolbar(useVerticalToolbar = true)
}

@Composable
private fun AlarmsToolbar(useVerticalToolbar: Boolean) {
    EventFahrplanTheme {
        AlarmsToolbar(
            visible = true,
            useVerticalToolbar = useVerticalToolbar,
            onViewEvent = {},
        )
    }
}
