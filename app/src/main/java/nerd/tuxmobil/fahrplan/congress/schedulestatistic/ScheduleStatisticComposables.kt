package nerd.tuxmobil.fahrplan.congress.schedulestatistic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.text.style.TextAlign.Companion.End
import androidx.compose.ui.text.style.TextAlign.Companion.Start
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.metadude.android.eventfahrplan.database.models.ColumnStatistic
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.TopBar
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonIcon
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconActionable
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.Loading
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.NoData
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextTableHeader
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.safeContentHorizontalPadding
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticState.Loading
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticState.Success
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticViewEvent.OnBackClick
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticViewEvent.OnToggleSorting

@Composable
internal fun ScheduleStatisticScreen(
    state: ScheduleStatisticState,
    onViewEvent: (ScheduleStatisticViewEvent) -> Unit,
) {
    EventFahrplanTheme {
        Scaffold(
            topBar = {
                TopBar(
                    showActions = state is Success && state.scheduleStatistic.isNotEmpty(),
                    onViewEvent = onViewEvent,
                )
            },
            content = { contentPadding ->
                Box(
                    Modifier
                        .padding(top = contentPadding.calculateTopPadding())
                ) {
                    when (state) {
                        Loading -> Loading()
                        is Success -> {
                            val scheduleStatistic = state.scheduleStatistic
                            if (scheduleStatistic.isEmpty()) {
                                NoScheduleStatistic()
                            } else {
                                ScheduleStatisticList(scheduleStatistic)
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun TopBar(showActions: Boolean, onViewEvent: (ScheduleStatisticViewEvent) -> Unit) {
    TopBar(
        title = stringResource(R.string.schedule_statistic_title),
        onBack = { onViewEvent(OnBackClick) },
        actions = {
            if (showActions) {
                ButtonIcon(
                    onClick = { onViewEvent(OnToggleSorting) },
                ) {
                    IconActionable(
                        icon = R.drawable.ic_sort,
                        tint = EventFahrplanTheme.colorScheme.appBarActionIcon,
                        contentDescription = R.string.schedule_statistic_toggle_sorting,
                    )
                }
            }
        },
    )
}

@Composable
private fun NoScheduleStatistic() {
    NoData(
        emptyContent = R.drawable.no_schedule,
        title = stringResource(R.string.schedule_statistic_no_statistic_data_title),
        subtitle = stringResource(R.string.schedule_statistic_no_statistic_data_subtitle),
    )
}


@Composable
private fun ScheduleStatisticList(scheduleStatistic: List<ColumnStatistic>) {
    val safeContentPadding = WindowInsets.safeContent.asPaddingValues()
    LazyColumn(
        modifier = Modifier.safeContentHorizontalPadding(),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 16.dp + safeContentPadding.calculateBottomPadding(),
        ),
        horizontalAlignment = CenterHorizontally,
    ) {
        item {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.schedule_statistic_description),
                textAlign = Center,
                fontStyle = Italic,
            )
            ColumnStatisticHeader()
        }
        items(scheduleStatistic, key = { it.name }) {
            ColumnStatisticItem(it, Modifier.animateItem())
        }
    }
}

@Composable
private fun ColumnStatisticHeader() {
    Row(
        modifier = Modifier
            .padding(bottom = 4.dp)
            .clearAndSetSemantics { },
        verticalAlignment = CenterVertically,
    ) {
        TextTableHeader(
            modifier = Modifier.defaultMinSize(minWidth = 140.dp),
            text = stringResource(R.string.schedule_statistic_column_name),
        )
        TextTableHeader(
            text = stringResource(R.string.schedule_statistic_null_empty),
            textAlign = End,
            modifier = Modifier
                .defaultMinSize(minWidth = 50.dp)
                .padding(horizontal = 8.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        TextTableHeader(
            text = stringResource(R.string.schedule_statistic_non_empty),
            textAlign = Start,
            modifier = Modifier
                .defaultMinSize(minWidth = 50.dp)
                .padding(start = 8.dp)
        )
    }
}

@Composable
private fun ColumnStatisticItem(
    statistic: ColumnStatistic,
    modifier: Modifier,
) {
    val rowContentDescription = buildString {
        append(stringResource(R.string.schedule_statistic_column_name))
        append(": ${statistic.name},")
        append(stringResource(R.string.schedule_statistic_null_empty_content_description))
        append(": ${statistic.countNone},")
        append(stringResource(R.string.schedule_statistic_non_empty))
        append(": ${statistic.countPresent}")
    }
    Row(
        verticalAlignment = CenterVertically,
        modifier = modifier
            .padding(vertical = 4.dp)
            .clearAndSetSemantics {
                contentDescription = rowContentDescription
            }
    ) {
        Text(
            modifier = Modifier.defaultMinSize(minWidth = 140.dp),
            text = "${statistic.name}:",
        )
        StackedHorizontalBar(
            value1 = statistic.countNone,
            value2 = statistic.countPresent,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
    }
}

@Composable
private fun StackedHorizontalBar(
    value1: Int,
    value2: Int,
    modifier: Modifier
) {
    val totalValue = value1 + value2
    val value1Fraction = if (totalValue == 0) 0f else value1.toFloat() / totalValue
    val value2Fraction = if (totalValue == 0) 0f else value2.toFloat() / totalValue
    val value1Percentage = value1Fraction * 100

    Row(
        modifier = modifier,
        verticalAlignment = CenterVertically,
    ) {
        Text(
            text = "$value1",
            textAlign = End,
            modifier = Modifier
                .defaultMinSize(minWidth = 50.dp)
                .padding(end = 8.dp),
        )
        if (value1 > 0) {
            Box(
                modifier = Modifier
                    .weight(value1Fraction)
                    .height(20.dp)
                    .background(colorOf(value1Percentage))
            )
        }
        if (value2 > 0) {
            Box(
                modifier = Modifier
                    .weight(value2Fraction)
                    .height(20.dp)
                    .background(EventFahrplanTheme.colorScheme.scheduleStatisticBarNoWarningBackground)
            )
        }
        Text(
            text = "$value2",
            textAlign = Start,
            modifier = Modifier
                .defaultMinSize(minWidth = 50.dp)
                .padding(start = 8.dp)
        )
    }
}

@Composable
private fun colorOf(percentage: Float) = when {
    percentage < 34 -> EventFahrplanTheme.colorScheme.scheduleStatisticBarWarningLevel1Background
    percentage < 67 -> EventFahrplanTheme.colorScheme.scheduleStatisticBarWarningLevel2Background
    else -> EventFahrplanTheme.colorScheme.scheduleStatisticBarWarningLevel3Background
}

@MultiDevicePreview
@Composable
private fun ScheduleStatisticScreenPreview() {
    ScheduleStatisticScreen(
        state = Success(
            listOf(
                ColumnStatistic("Links", countNone = 583, countPresent = 6),
                ColumnStatistic("Language", countNone = 387, countPresent = 202),
                ColumnStatistic("Room", countNone = 70, countPresent = 519),
                ColumnStatistic("Track", countNone = 0, countPresent = 589),
            )
        ),
        onViewEvent = {},
    )
}

@Preview
@Composable
private fun ScheduleStatisticScreenEmptyPreview() {
    ScheduleStatisticScreen(
        state = Success(emptyList()),
        onViewEvent = {},
    )
}

@Preview
@Composable
private fun ScheduleStatisticScreenLoadingPreview() {
    ScheduleStatisticScreen(
        state = Loading,
        onViewEvent = {},
    )
}
