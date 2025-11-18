package nerd.tuxmobil.fahrplan.congress.alarms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Bottom
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Loading
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Success
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonIcon
import nerd.tuxmobil.fahrplan.congress.designsystem.dividers.DividerHorizontal
import nerd.tuxmobil.fahrplan.congress.designsystem.headers.HeaderSessionList
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconBoxed
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorative
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.Loading
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.NoData
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.ListItem
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextHeadlineContent
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextOverline
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextSupportingContent
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.safeContentHorizontalPadding

@Composable
internal fun AlarmsScreen(
    state: AlarmsState,
    showInSidePane: Boolean,
) {
    EventFahrplanTheme {
        Scaffold {
            Box {
                when (state) {
                    Loading -> Loading()
                    is Success -> {
                        val parameters = state.sessionAlarmParameters
                        if (parameters.isEmpty()) {
                            NoAlarms()
                        } else {
                            SessionAlarmsList(
                                parameters = parameters,
                                showInSidePane = showInSidePane,
                                onItemClick = state.onItemClick,
                                onDeleteItemClick = state.onDeleteItemClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoAlarms() {
    NoData(
        emptyContent = R.drawable.no_alarms,
        title = stringResource(R.string.alarms_no_alarms_title),
        subtitle = stringResource(R.string.alarms_no_alarms_subtitle),
    )
}

@Composable
private fun SessionAlarmsList(
    parameters: List<SessionAlarmParameter>,
    showInSidePane: Boolean,
    onItemClick: (SessionAlarmParameter) -> Unit,
    onDeleteItemClick: (SessionAlarmParameter) -> Unit
) {
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = WindowInsets.navigationBars.only(Bottom).asPaddingValues(),
    ) {
        if (showInSidePane) {
            item {
                HeaderSessionList(stringResource(R.string.reminders))
            }
        }
        itemsIndexed(parameters) { index, item ->
            SessionAlarmItem(
                modifier = Modifier.safeContentHorizontalPadding(),
                parameter = item,
                onClick = onItemClick,
                onDeleteClick = onDeleteItemClick
            )
            if (index < parameters.size - 1) {
                DividerHorizontal(Modifier.padding(horizontal = 12.dp))
            }
        }
    }
}

@Composable
private fun SessionAlarmItem(
    modifier: Modifier = Modifier,
    parameter: SessionAlarmParameter,
    onClick: (SessionAlarmParameter) -> Unit,
    onDeleteClick: (SessionAlarmParameter) -> Unit
) {
    ListItem(
        modifier = modifier
            .clickable(
                onClickLabel = stringResource(R.string.alarms_item_on_click_label),
                onClick = { onClick(parameter) }
            ),
        leadingContent = {
            AlarmIcon(parameter.alarmOffsetInMin, parameter.alarmOffsetContentDescription)
        },
        overlineContent = {
            TextOverline(
                modifier = Modifier.semantics {
                    contentDescription = parameter.firesAtContentDescription
                },
                text = parameter.firesAtText,
            )
        },
        headlineContent = {
            TextHeadlineContent(
                modifier = Modifier.semantics {
                    contentDescription = parameter.titleContentDescription
                },
                text = parameter.title,
            )
        },
        supportingContent = {
            if (parameter.subtitle.isNotEmpty()) {
                TextSupportingContent(
                    modifier = Modifier.semantics {
                        contentDescription = parameter.subtitleContentDescription
                    },
                    text = parameter.subtitle,
                )
            }
        },
        trailingContent = {
            DeleteIcon(parameter, onDeleteClick)
        },
    )
}

@Composable
private fun AlarmIcon(alarmOffset: Int, alarmIconContentDescription: String) {
    IconBoxed(
        icon = R.drawable.ic_bell_on_session_alarm,
        modifier = Modifier.semantics {
            contentDescription = alarmIconContentDescription
        }
    ) {
        Text(
            text = "$alarmOffset",
            textAlign = TextAlign.Center,
            color = EventFahrplanTheme.colorScheme.sessionAlarmItemBellIconText,
            fontWeight = Bold,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun DeleteIcon(
    parameter: SessionAlarmParameter,
    onButtonClick: (SessionAlarmParameter) -> Unit,
) {
    val label = stringResource(R.string.alarms_item_delete_icon_on_click_label)
    ButtonIcon(
        onClick = { onButtonClick(parameter) },
        modifier = Modifier.semantics {
            onClick(label) {
                onButtonClick(parameter)
                true
            }
        }
    ) {
        IconDecorative(
            icon = R.drawable.ic_delete,
        )
    }
}

@MultiDevicePreview
@Composable
private fun AlarmsScreenPreview() {
    AlarmsScreen(
        Success(
            listOf(
                SessionAlarmParameter(
                    sessionId = "s1",
                    title = "Some random title",
                    titleContentDescription = "",
                    subtitle = "A longer subtitle to be displayed",
                    subtitleContentDescription = "",
                    alarmOffsetInMin = 45,
                    alarmOffsetContentDescription = "",
                    firesAt = Moment.ofEpochMilli(0),
                    firesAtText = "28.02.2023 14:00",
                    firesAtContentDescription = "",
                    dayIndex = 0,
                ),
                SessionAlarmParameter(
                    sessionId = "s2",
                    title = "Second title",
                    titleContentDescription = "",
                    subtitle = "A longer subtitle to be displayed lorem ipsum",
                    subtitleContentDescription = "",
                    alarmOffsetInMin = 10,
                    alarmOffsetContentDescription = "",
                    firesAt = Moment.ofEpochMilli(0),
                    firesAtText = "01.03.2023 09:00",
                    firesAtContentDescription = "",
                    dayIndex = 0,
                ),
                SessionAlarmParameter(
                    sessionId = "s3",
                    title = "No subtitle present for this item",
                    titleContentDescription = "",
                    subtitle = "",
                    subtitleContentDescription = "",
                    alarmOffsetInMin = 0,
                    alarmOffsetContentDescription = "",
                    firesAt = Moment.ofEpochMilli(0),
                    firesAtText = "01.03.2023 17:00",
                    firesAtContentDescription = "",
                    dayIndex = 0,
                ),
            ),
            onItemClick = { _ -> },
            onDeleteItemClick = {},
        ),
        showInSidePane = true,
    )
}

@Preview
@Composable
private fun AlarmsScreenEmptyPreview() {
    AlarmsScreen(
        state = Success(
            emptyList(),
            onItemClick = { _ -> },
            onDeleteItemClick = {},
        ),
        showInSidePane = false,
    )
}

@Preview
@Composable
private fun AlarmsScreenLoadingPreview() {
    AlarmsScreen(
        state = Loading,
        showInSidePane = false,
    )
}
