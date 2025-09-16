package nerd.tuxmobil.fahrplan.congress.changes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Bottom
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDecoration.Companion.LineThrough
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeParameter.Separator
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeParameter.SessionChange
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.CANCELED
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.CHANGED
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.NEW
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeProperty.ChangeState.UNCHANGED
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Loading
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Success
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnSessionChangeItemClick
import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorProperty
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Available
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Unavailable
import nerd.tuxmobil.fahrplan.congress.designsystem.dividers.DividerHorizontal
import nerd.tuxmobil.fahrplan.congress.designsystem.headers.HeaderDayDate
import nerd.tuxmobil.fahrplan.congress.designsystem.headers.HeaderSessionList
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconVideoRecording
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.Loading
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.NoData
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextHeadlineContent
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.TextSupportingContent
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.safeContentHorizontalPadding

@Composable
internal fun SessionChangesScreen(
    state: SessionChangeState,
    showInSidePane: Boolean,
    onViewEvent: (SessionChangeViewEvent) -> Unit,
) {
    EventFahrplanTheme {
        Scaffold {
            Box {
                when (state) {
                    Loading -> Loading()
                    is Success -> {
                        val parameters = state.sessionChangeParameters
                        if (parameters.isEmpty()) {
                            NoScheduleChanges()
                        } else {
                            SessionChangesList(
                                parameters = parameters,
                                showInSidePane = showInSidePane,
                                onViewEvent = onViewEvent,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoScheduleChanges() {
    NoData(
        emptyContent = R.drawable.no_schedule_changes,
        title = stringResource(R.string.schedule_changes_no_changes_title),
        subtitle = stringResource(R.string.schedule_changes_no_changes_subtitle),
    )
}

@Composable
private fun SessionChangesList(
    parameters: List<SessionChangeParameter>,
    showInSidePane: Boolean,
    onViewEvent: (SessionChangeViewEvent) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = WindowInsets.navigationBars.only(Bottom).asPaddingValues(),
    ) {
        if (showInSidePane) {
            item {
                HeaderSessionList(stringResource(R.string.schedule_changes))
            }
        }
        itemsIndexed(parameters) { index, parameter ->
            when (parameter) {
                is Separator -> HeaderDayDate(
                    text = parameter.daySeparator.value,
                    contentDescription = parameter.daySeparator.contentDescription,
                )

                is SessionChange -> {
                    SessionChangeItem(
                        session = parameter,
                        modifier = Modifier
                            .safeContentHorizontalPadding()
                            .clickable {
                                if (!parameter.isCanceled) {
                                    onViewEvent(OnSessionChangeItemClick(parameter.id))
                                }
                            }
                    )
                    val next = parameters.getOrNull(index + 1)
                    if (index < parameters.size - 1 && (next != null && next !is Separator)) {
                        DividerHorizontal(Modifier.padding(horizontal = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SessionChangeItem(
    session: SessionChange,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            val titleColor = session.title.changeState.color()
            val textDecoration = textDecorationOf(session.title)
            TextHeadlineContent(
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = session.title.contentDescription
                    },
                text = session.title.value,
                fontSize = 16.sp,
                fontWeight = Bold,
                color = colorResource(titleColor),
                textDecoration = textDecoration,
            )
            val iconColor = session.videoRecordingState.changeState.color()
            IconVideoRecording(
                session.videoRecordingState.value,
                iconColor,
            )
        }
        SecondaryText(
            session.subtitle
        )
        SecondaryText(
            modifier = Modifier.padding(top = 4.dp),
            property = session.speakerNames,
        )
        Row {
            SecondaryText(
                modifier = Modifier.padding(end = 8.dp),
                property = session.dayText,
            )
            SecondaryText(
                modifier = Modifier.padding(end = 8.dp),
                property = session.startsAt,
            )
            SecondaryText(
                modifier = Modifier
                    .widthIn(55.dp)
                    .padding(end = 8.dp),
                property = session.duration,
            )
            SecondaryText(
                modifier = Modifier.weight(1f),
                property = session.roomName,
            )
            SecondaryText(
                modifier = Modifier.padding(start = 16.dp),
                property = session.languages,
            )
        }
    }
}

@Composable
private fun SecondaryText(
    property: SessionChangeProperty<String>,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        val color = property.changeState.color()
        val textDecoration = textDecorationOf(property)
        TextSupportingContent(
            modifier = modifier.semantics {
                contentDescription = property.contentDescription
            },
            text = property.value,
            fontSize = 13.sp,
            color = colorResource(color),
            textDecoration = textDecoration,
        )
    }
}

@Composable
private fun ChangeState.color() = with(this) {
    when (isSystemInDarkTheme()) { // TODO Move into theme
        true -> colorOnDark
        false -> colorOnLight
    }
}

@Composable
private fun textDecorationOf(property: SessionChangeProperty<String>) =
    if (property.changeState == CANCELED) LineThrough else TextDecoration.None

@MultiDevicePreview
@Composable
private fun SessionChangesScreenPreview() {
    SessionChangesScreen(
        Success(
            listOf(
                Separator(
                    DaySeparatorProperty(
                        value = "Day 1 - 31.02.2023",
                        contentDescription = "",
                    )
                ),
                SessionChange(
                    id = "changed",
                    title = SessionChangeProperty(stringResource(R.string.placeholder_session_title), "", UNCHANGED),
                    subtitle = SessionChangeProperty(stringResource(R.string.placeholder_session_subtitle), "", CHANGED),
                    videoRecordingState = SessionChangeProperty(Available, "", UNCHANGED),
                    speakerNames = SessionChangeProperty(stringResource(R.string.placeholder_session_speakers), "", UNCHANGED),
                    dayText = SessionChangeProperty(stringResource(R.string.placeholder_session_day), "", UNCHANGED),
                    startsAt = SessionChangeProperty(stringResource(R.string.placeholder_session_start_time), "", CHANGED),
                    duration = SessionChangeProperty(stringResource(R.string.placeholder_session_duration), "", UNCHANGED),
                    roomName = SessionChangeProperty(stringResource(R.string.placeholder_session_room), "", UNCHANGED),
                    languages = SessionChangeProperty(stringResource(R.string.placeholder_session_language), "", UNCHANGED),
                ),
                SessionChange(
                    id = "changed, now with video",
                    title = SessionChangeProperty(stringResource(R.string.placeholder_session_title), "", UNCHANGED),
                    subtitle = SessionChangeProperty(stringResource(R.string.placeholder_session_subtitle), "", CHANGED),
                    videoRecordingState = SessionChangeProperty(Available, "", CHANGED),
                    speakerNames = SessionChangeProperty(stringResource(R.string.placeholder_session_speakers), "", UNCHANGED),
                    dayText = SessionChangeProperty(stringResource(R.string.placeholder_session_day), "", UNCHANGED),
                    startsAt = SessionChangeProperty(stringResource(R.string.placeholder_session_start_time), "", CHANGED),
                    duration = SessionChangeProperty(stringResource(R.string.placeholder_session_duration), "", UNCHANGED),
                    roomName = SessionChangeProperty(stringResource(R.string.placeholder_session_room), "", UNCHANGED),
                    languages = SessionChangeProperty(stringResource(R.string.placeholder_session_language), "", UNCHANGED),
                ),
                SessionChange(
                    id = "changed, now with video",
                    title = SessionChangeProperty(stringResource(R.string.placeholder_session_title), "", UNCHANGED),
                    subtitle = SessionChangeProperty(stringResource(R.string.placeholder_session_subtitle), "", CHANGED),
                    videoRecordingState = SessionChangeProperty(Unavailable, "", CHANGED),
                    speakerNames = SessionChangeProperty(stringResource(R.string.placeholder_session_speakers), "", UNCHANGED),
                    dayText = SessionChangeProperty(stringResource(R.string.placeholder_session_day), "", UNCHANGED),
                    startsAt = SessionChangeProperty(stringResource(R.string.placeholder_session_start_time), "", CHANGED),
                    duration = SessionChangeProperty(stringResource(R.string.placeholder_session_duration), "", UNCHANGED),
                    roomName = SessionChangeProperty(stringResource(R.string.placeholder_session_room), "", UNCHANGED),
                    languages = SessionChangeProperty(stringResource(R.string.placeholder_session_language), "", UNCHANGED),
                ),
                Separator(
                    DaySeparatorProperty(
                        value = "Day 2 - 01.03.2023",
                        contentDescription = "",
                    )
                ),
                SessionChange(
                    id = "new with video",
                    title = SessionChangeProperty(stringResource(R.string.placeholder_session_title), "", NEW),
                    subtitle = SessionChangeProperty(stringResource(R.string.placeholder_session_subtitle), "", NEW),
                    videoRecordingState = SessionChangeProperty(Available, "", NEW),
                    speakerNames = SessionChangeProperty(stringResource(R.string.placeholder_session_speakers), "", NEW),
                    dayText = SessionChangeProperty(stringResource(R.string.placeholder_session_day), "", NEW),
                    startsAt = SessionChangeProperty(stringResource(R.string.placeholder_session_start_time), "", NEW),
                    duration = SessionChangeProperty(stringResource(R.string.placeholder_session_duration), "", NEW),
                    roomName = SessionChangeProperty(stringResource(R.string.placeholder_session_room), "", NEW),
                    languages = SessionChangeProperty(stringResource(R.string.placeholder_session_language), "", NEW),
                ),
                SessionChange(
                    id = "new without video",
                    title = SessionChangeProperty(stringResource(R.string.placeholder_session_title), "", NEW),
                    subtitle = SessionChangeProperty(stringResource(R.string.placeholder_session_subtitle), "", NEW),
                    videoRecordingState = SessionChangeProperty(Unavailable, "", NEW),
                    speakerNames = SessionChangeProperty(stringResource(R.string.placeholder_session_speakers), "", NEW),
                    dayText = SessionChangeProperty(stringResource(R.string.placeholder_session_day), "", NEW),
                    startsAt = SessionChangeProperty(stringResource(R.string.placeholder_session_start_time), "", NEW),
                    duration = SessionChangeProperty(stringResource(R.string.placeholder_session_duration), "", NEW),
                    roomName = SessionChangeProperty(stringResource(R.string.placeholder_session_room), "", NEW),
                    languages = SessionChangeProperty(stringResource(R.string.placeholder_session_language), "", NEW),
                ),
                SessionChange(
                    id = "canceled with video",
                    title = SessionChangeProperty(stringResource(R.string.placeholder_session_title), "", CANCELED),
                    subtitle = SessionChangeProperty(stringResource(R.string.placeholder_session_subtitle), "", CANCELED),
                    videoRecordingState = SessionChangeProperty(Available, "", CANCELED),
                    speakerNames = SessionChangeProperty(stringResource(R.string.placeholder_session_speakers), "", CANCELED),
                    dayText = SessionChangeProperty(stringResource(R.string.placeholder_session_day), "", CANCELED),
                    startsAt = SessionChangeProperty(stringResource(R.string.placeholder_session_start_time), "", CANCELED),
                    duration = SessionChangeProperty(stringResource(R.string.placeholder_session_duration), "", CANCELED),
                    roomName = SessionChangeProperty(stringResource(R.string.placeholder_session_room), "", CANCELED),
                    languages = SessionChangeProperty(stringResource(R.string.placeholder_session_language), "", CANCELED),
                ),
                SessionChange(
                    id = "canceled without video",
                    title = SessionChangeProperty(stringResource(R.string.placeholder_session_title), "", CANCELED),
                    subtitle = SessionChangeProperty(stringResource(R.string.placeholder_session_subtitle), "", CANCELED),
                    videoRecordingState = SessionChangeProperty(Unavailable, "", CANCELED),
                    speakerNames = SessionChangeProperty(stringResource(R.string.placeholder_session_speakers), "", CANCELED),
                    dayText = SessionChangeProperty(stringResource(R.string.placeholder_session_day), "", CANCELED),
                    startsAt = SessionChangeProperty(stringResource(R.string.placeholder_session_start_time), "", CANCELED),
                    duration = SessionChangeProperty(stringResource(R.string.placeholder_session_duration), "", CANCELED),
                    roomName = SessionChangeProperty(stringResource(R.string.placeholder_session_room), "", CANCELED),
                    languages = SessionChangeProperty(stringResource(R.string.placeholder_session_language), "", CANCELED),
                ),
            )
        ),
        showInSidePane = true,
        onViewEvent = {},
    )
}

@Preview
@Composable
private fun SessionChangesScreenEmptyPreview() {
    SessionChangesScreen(
        Success(emptyList()),
        showInSidePane = false,
        onViewEvent = {},
    )
}

@Preview
@Composable
private fun SessionChangesScreenLoadingPreview() {
    SessionChangesScreen(
        state = Loading,
        showInSidePane = false,
        onViewEvent = {},
    )
}
