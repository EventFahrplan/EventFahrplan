package nerd.tuxmobil.fahrplan.congress.alarms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Bottom
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Loading
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Success
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnDeleteItemClick
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnItemClick
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.commons.ScreenMetrics
import nerd.tuxmobil.fahrplan.congress.commons.ToolbarMetrics
import nerd.tuxmobil.fahrplan.congress.commons.useVerticalFloatingToolbar
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonIcon
import nerd.tuxmobil.fahrplan.congress.designsystem.dividers.DividerHorizontal
import nerd.tuxmobil.fahrplan.congress.designsystem.headers.HeaderSessionList
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconBoxed
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDelete
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.Loading
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.NoData
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.ListItem
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.NavigationSection
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.NavigationSectionWithContent
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextHeadlineContent
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextOverline
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextSupportingContent
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.safeContentHorizontalPadding
import nerd.tuxmobil.fahrplan.congress.utils.compose.ScrollPosition
import nerd.tuxmobil.fahrplan.congress.utils.compose.rememberAutoHideOnScrollDown

@Composable
internal fun AlarmsContent(
    state: AlarmsState,
    showInSidePane: Boolean,
    onBack: () -> Unit,
    onViewEvent: (AlarmsViewEvent) -> Unit,
) {
    val title = stringResource(R.string.reminders)
    Scaffold { _ ->
        Box(
            Modifier
                .fillMaxSize()
                .semantics { paneTitle = title },
        ) {
            when (state) {
                Loading -> {
                    Column(Modifier.fillMaxSize()) {
                        NavigationSection(
                            showInSidePane = showInSidePane,
                            onNavClick = onBack,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            Loading()
                        }
                    }
                }

                is Success -> {
                    val parameters = state.sessionAlarmParameters
                    if (parameters.isEmpty()) {
                        Column(Modifier.fillMaxSize()) {
                            NavigationSection(
                                showInSidePane = showInSidePane,
                                onNavClick = onBack,
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            ) {
                                NoAlarms()
                            }
                        }
                    } else {
                        SessionAlarmsList(
                            parameters = parameters,
                            showInSidePane = showInSidePane,
                            title = title,
                            onBack = onBack,
                            onViewEvent = onViewEvent,
                        )
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
    title: String,
    onBack: () -> Unit,
    onViewEvent: (AlarmsViewEvent) -> Unit,
) {
    val useVerticalToolbar = useVerticalFloatingToolbar(showInSidePane)
    val listState = rememberLazyListState()
    val showToolbar by rememberAutoHideOnScrollDown(
        source = ScrollPosition.LazyList(listState),
        enabled = !useVerticalToolbar,
    )

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.navigationBars.union(WindowInsets.ime).only(Bottom).asPaddingValues(),
        ) {
            item {
                NavigationSection(
                    showInSidePane = showInSidePane,
                    title = title,
                    onNavClick = onBack,
                )
            }
            itemsIndexed(parameters) { index, item ->
                SessionAlarmItem(
                    useVerticalToolbar = useVerticalToolbar,
                    parameter = item,
                    onViewEvent = onViewEvent,
                )
                if (index < parameters.size - 1) {
                    DividerHorizontal()
                }
            }
        }
        AlarmsToolbar(
            modifier = Modifier
                .align(if (useVerticalToolbar) CenterEnd else BottomCenter),
            visible = showToolbar,
            useVerticalToolbar = useVerticalToolbar,
            onViewEvent = onViewEvent,
        )
    }
}

@Composable
private fun NavigationSection(
    showInSidePane: Boolean,
    title: String,
    onNavClick: () -> Unit,
) {
    NavigationSectionWithContent(
        showInSidePane = showInSidePane,
        contentLeftOfCloseButton = {
            HeaderSessionList(
                modifier = Modifier.widthIn(max = maxWidth),
                text = title,
                includeDefaultPadding = false,
            )
        },
        contentRightOfBackButton = {
            HeaderSessionList(
                text = title,
                includeDefaultPadding = false,
            )
        },
        onNavClick = onNavClick,
    )
}

@Composable
private fun SessionAlarmItem(
    useVerticalToolbar: Boolean,
    parameter: SessionAlarmParameter,
    onViewEvent: (AlarmsViewEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(R.string.alarms_item_on_click_label),
                onClick = { onViewEvent(OnItemClick(parameter.sessionId)) },
            )
            .safeContentHorizontalPadding()
            .padding(ToolbarMetrics.searchResultItemPaddingValues(useVerticalToolbar)),
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScreenMetrics.listItemPaddingValues()),
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
                DeleteIcon(parameter, onViewEvent)
            },
        )
    }
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
    onViewEvent: (AlarmsViewEvent) -> Unit,
) {
    val label = stringResource(R.string.alarms_item_delete_icon_on_click_label)
    val deleteContentDescription = stringResource(R.string.menu_item_title_delete_alarm)
    ButtonIcon(
        onClick = {
            onViewEvent(
                OnDeleteItemClick(
                    sessionId = parameter.sessionId,
                    dayIndex = parameter.dayIndex,
                    title = parameter.title,
                    firesAt = parameter.firesAt,
                )
            )
        },
        modifier = Modifier.semantics {
            contentDescription = deleteContentDescription
            onClick(label) {
                onViewEvent(
                    OnDeleteItemClick(
                        sessionId = parameter.sessionId,
                        dayIndex = parameter.dayIndex,
                        title = parameter.title,
                        firesAt = parameter.firesAt,
                    )
                )
                true
            }
        }
    ) {
        IconDelete()
    }
}

@MultiDevicePreview
@Composable
private fun AlarmsContentPreview() {
    EventFahrplanTheme {
        AlarmsContent(
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
            ),
            showInSidePane = true,
            onBack = {},
            onViewEvent = {},
        )
    }
}

@Preview
@Composable
private fun AlarmsContentEmptyPreview() {
    EventFahrplanTheme {
        AlarmsContent(
            state = Success(
                emptyList(),
            ),
            showInSidePane = false,
            onBack = {},
            onViewEvent = {},
        )
    }
}

@Preview
@Composable
private fun AlarmsContentLoadingPreview() {
    EventFahrplanTheme {
        AlarmsContent(
            state = Loading,
            showInSidePane = false,
            onBack = {},
            onViewEvent = {},
        )
    }
}
