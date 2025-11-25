package nerd.tuxmobil.fahrplan.congress.changes.statistic

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType.CANCELED
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType.CHANGED
import nerd.tuxmobil.fahrplan.congress.changes.ChangeType.NEW
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonText
import nerd.tuxmobil.fahrplan.congress.designsystem.dialogs.AlertDialog
import nerd.tuxmobil.fahrplan.congress.designsystem.graphs.StackedHorizontalBar
import nerd.tuxmobil.fahrplan.congress.designsystem.graphs.StackedHorizontalBar.Colors
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
internal fun ChangeStatisticScreen(
    uiState: ChangeStatisticsUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(R.string.schedule_changes_statistic_title)) },
        text = {
            Column {
                Text(stringResource(R.string.schedule_changes_statistic_update_to, uiState.scheduleVersion))
                Spacer(Modifier.padding(8.dp))
                HorizontalBars(uiState.statistics, uiState.allSessionsCount)
                Spacer(Modifier.padding(8.dp))
                Text(stringResource(R.string.schedule_changes_statistic_total_sessions, uiState.allSessionsCount))
            }
        },
        confirmButton = {
            ButtonText(onClick = onConfirm) {
                Text(stringResource(R.string.schedule_changes_statistic_browse))
            }
        },
        dismissButton = {
            ButtonText(onClick = onDismiss) {
                Text(stringResource(R.string.schedule_changes_statistic_later))
            }
        },
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.safeContentPadding(),
    )
}

@Composable
private fun HorizontalBars(properties: List<ChangeStatisticProperty>, totalSessionsCount: Int) {
    LazyColumn(verticalArrangement = spacedBy(8.dp)) {
        items(properties) {
            StackedHorizontalBar(
                item = StackedHorizontalBar.Item(
                    value1 = it.value,
                    value2 = totalSessionsCount - it.value,
                    totalValue = totalSessionsCount,
                    text1 = "${stringResource(nameOf(it.changeType))} (${it.value}):",
                    text2 = "",
                ),
                contentDescription = contentDescriptionOf(it),
                colors = Colors(
                    value1 = colorOf(it.changeType),
                    value2 = EventFahrplanTheme.colorScheme.scheduleChangeBarBackground,
                ),
                textMinWidth = 120.dp,
            )
        }
    }
}

@Composable
private fun contentDescriptionOf(property: ChangeStatisticProperty) =
    when (val changeType = property.changeType) {
        CHANGED -> stringResource(R.string.schedule_changes_statistic_changed_content_description, property.value)
        NEW -> stringResource(R.string.schedule_changes_statistic_new_content_description, property.value)
        CANCELED -> stringResource(R.string.schedule_changes_statistic_canceled_content_description, property.value)
        else -> error("Unsupported change state type: $changeType")
    }

@Composable
private fun nameOf(changeType: ChangeType) = when (changeType) {
    CHANGED -> R.string.schedule_changes_statistic_changed
    NEW -> R.string.schedule_changes_statistic_new
    CANCELED -> R.string.schedule_changes_statistic_canceled
    else -> error("Unsupported change state type: $changeType")
}

@Composable
private fun colorOf(changeType: ChangeType) = when (changeType) {
    CHANGED -> EventFahrplanTheme.colorScheme.scheduleChangeChanged
    NEW -> EventFahrplanTheme.colorScheme.scheduleChangeNew
    CANCELED -> EventFahrplanTheme.colorScheme.scheduleChangeCanceled
    else -> error("Unsupported change state type: $changeType")
}

@PreviewLightDark
@Composable
private fun ChangeStatisticScreenPreview() {
    EventFahrplanTheme {
        ChangeStatisticScreen(
            uiState = createUiState(),
            onConfirm = {},
            onDismiss = {},
        )
    }
}

private fun createUiState(): ChangeStatisticsUiState {
    val list = buildList {
        add(ChangeStatisticProperty(20, CHANGED))
        add(ChangeStatisticProperty(60, NEW))
        add(ChangeStatisticProperty(10, CANCELED))
    }
    return ChangeStatisticsUiState(
        scheduleVersion = "1.42.0",
        statistics = list,
        allSessionsCount = 100,
    )
}
