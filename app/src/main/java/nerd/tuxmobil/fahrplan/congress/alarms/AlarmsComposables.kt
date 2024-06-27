package nerd.tuxmobil.fahrplan.congress.alarms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale.Companion.FillBounds
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Loading
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Success
import nerd.tuxmobil.fahrplan.congress.commons.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.commons.Loading
import nerd.tuxmobil.fahrplan.congress.commons.NoData

@Composable
internal fun AlarmsScreen(
    state: AlarmsState,
) {
    EventFahrplanTheme {
        Scaffold { contentPadding ->
            Box(
                Modifier
                    .padding(contentPadding)
            ) {
                when (state) {
                    Loading -> Loading()
                    is Success -> {
                        val parameters = state.sessionAlarmParameters
                        if (parameters.isEmpty()) {
                            NoAlarms()
                        } else {
                            SessionAlarmsList(
                                parameters = parameters,
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
    onItemClick: (SessionAlarmParameter) -> Unit,
    onDeleteItemClick: (SessionAlarmParameter) -> Unit
) {
    LazyColumn(state = rememberLazyListState()) {
        itemsIndexed(parameters) { index, item ->
            SessionAlarmItem(
                parameter = item,
                onClick = onItemClick,
                onDeleteClick = onDeleteItemClick
            )
            if (index < parameters.size - 1) {
                HorizontalDivider(Modifier.padding(horizontal = 12.dp))
            }
        }
    }
}

@Composable
private fun SessionAlarmItem(
    parameter: SessionAlarmParameter,
    onClick: (SessionAlarmParameter) -> Unit,
    onDeleteClick: (SessionAlarmParameter) -> Unit
) {
    ListItem(
        modifier = Modifier
            .clickable(
                onClickLabel = stringResource(R.string.alarms_item_on_click_label),
                onClick = { onClick(parameter) }
            ),
        leadingContent = {
            AlarmIcon(parameter.alarmOffsetInMin, parameter.alarmOffsetContentDescription)
        },
        overlineContent = {
            Text(
                modifier = Modifier.semantics {
                    contentDescription = parameter.firesAtContentDescription
                },
                text = parameter.firesAtText,
            )
        },
        headlineContent = {
            Text(
                modifier = Modifier.semantics {
                    contentDescription = parameter.titleContentDescription
                },
                text = parameter.title,
                overflow = Ellipsis,
                maxLines = 1,
            )
        },
        supportingContent = {
            if (parameter.subtitle.isNotEmpty()) {
                Text(
                    modifier = Modifier.semantics {
                        contentDescription = parameter.subtitleContentDescription
                    },
                    text = parameter.subtitle,
                    overflow = Ellipsis,
                    maxLines = 1,
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
    Box(
        Modifier
            .semantics {
                contentDescription = alarmIconContentDescription
            }
            .size(36.dp)
            .paint(
                painter = painterResource(R.drawable.ic_bell_on_session_alarm),
                contentScale = FillBounds,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$alarmOffset",
            modifier = Modifier.padding(end = 2.dp), // to center text
            color = colorResource(R.color.session_alarm_item_bell_icon_text),
            textAlign = TextAlign.Center,
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
    IconButton(
        onClick = { onButtonClick(parameter) },
        modifier = Modifier.semantics {
            onClick(label) {
                onButtonClick(parameter)
                true
            }
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_delete),
            contentDescription = null
        )
    }
}

@Preview
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
                    firesAt = 0L,
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
                    firesAt = 0L,
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
                    firesAt = 0L,
                    firesAtText = "01.03.2023 17:00",
                    firesAtContentDescription = "",
                    dayIndex = 0,
                ),
            ),
            onItemClick = { _ -> },
            onDeleteItemClick = {},
        ),
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
        )
    )
}

@Preview
@Composable
private fun AlarmsScreenLoadingPreview() {
    AlarmsScreen(
        state = Loading,
    )
}
