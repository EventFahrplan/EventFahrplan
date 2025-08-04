package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.Start
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.designsystem.cards.Card
import nerd.tuxmobil.fahrplan.congress.designsystem.cards.CardDefaults
import nerd.tuxmobil.fahrplan.congress.designsystem.colors.LocalColorScheme
import nerd.tuxmobil.fahrplan.congress.designsystem.dividers.DividerHorizontal
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorative
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorativeVector
import nerd.tuxmobil.fahrplan.congress.designsystem.indicators.ripple
import nerd.tuxmobil.fahrplan.congress.designsystem.menues.DropdownMenu
import nerd.tuxmobil.fahrplan.congress.designsystem.menues.DropdownMenuItem
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.toTextUnit
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.ADD_TO_CALENDAR
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.SHARE
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.SHARE_JSON
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.SHARE_TEXT
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.TOGGLE_ALARM
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.TOGGLE_FAVORITE

@Composable
fun RoomColumn(
    columnData: RoomColumnData,
    onSessionClick: (String) -> Unit,
    onSessionInteraction: (String, SessionInteractionType) -> Unit
) {
    EventFahrplanTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            // Add spacings and sessions in the right order
            columnData.sessionData.forEachIndexed { index, sessionData ->
                // Add spacing before session if needed
                if (index < columnData.spacings.size && columnData.spacings[index] > 0) {
                    Spacer(Modifier.height(columnData.spacings[index].dp))
                }

                SessionCard(
                    data = sessionData,
                    sessionCardLayout = { SessionCardLayout(sessionData) },
                    onClick = { onSessionClick(sessionData.sessionId) },
                    onMenuItemClick = { onSessionInteraction(sessionData.sessionId, it) },
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionCard(
    data: SessionCardData,
    contextMenuTextColor: Color = LocalColorScheme.current.inverseOnSurface,
    sessionCardLayout: @Composable ColumnScope.() -> Unit,
    onClick: () -> Unit,
    onMenuItemClick: (SessionInteractionType) -> Unit,
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var pressPosition by remember { mutableStateOf(Offset.Zero) }
    var cardPosition by remember { mutableStateOf(Offset.Zero) }
    var cardWidth by remember { mutableFloatStateOf(0f) }
    val interactionSource = remember { MutableInteractionSource() }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(data.cardHeight.dp)
                .onGloballyPositioned { coordinates ->
                    cardPosition = coordinates.positionInRoot()
                    cardWidth = coordinates.size.width.toFloat()
                }
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onClick,
                    onLongClick = {
                        pressPosition = Offset(cardPosition.x + (cardWidth / 2), cardPosition.y)
                        showContextMenu = true
                    }
                )
                .padding(
                    start = dimensionResource(R.dimen.session_drawable_inset_start),
                    bottom = dimensionResource(R.dimen.session_drawable_inset_bottom),
                    top = dimensionResource(R.dimen.session_drawable_inset_top),
                    end = dimensionResource(R.dimen.session_drawable_inset_end)
                )
                .semantics {
                    if (data.stateContentDescription.isNotEmpty()) {
                        contentDescription = data.stateContentDescription
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = colorResource(data.backgroundColor),
                contentColor = colorResource(data.textColor),
            ),
            border = if (data.showBorder) {
                BorderStroke(
                    dimensionResource(R.dimen.session_drawable_selection_stroke_width),
                    colorResource(R.color.session_drawable_selection_stroke),
                )
            } else {
                null
            },
            shape = RoundedCornerShape(dimensionResource(R.dimen.session_drawable_corner_radius)),
            content = { sessionCardLayout() },
        )
        ContextMenu(
            expanded = showContextMenu,
            isFavored = data.isFavored,
            hasAlarm = data.hasAlarm,
            shouldShowShareSubMenu = data.shouldShowShareSubMenu,
            textColor = contextMenuTextColor,
            cardPosition = cardPosition,
            pressPosition = pressPosition,
            onMenuItemClick = onMenuItemClick,
            onCollapseRequest = { showContextMenu = false },
        )
    }
}

@Suppress("kotlin:S107")
@Composable
private fun SessionCardLayout(data: SessionCardData) {
    Column(
        modifier = Modifier
            .padding(horizontal = dimensionResource(R.dimen.session_drawable_inner_padding))
            .fillMaxWidth()
    ) {

        val currentDensity = LocalDensity.current
        val cardHeightPx = (currentDensity.density * data.cardHeight).toInt()
        val innerPaddingDp = dimensionResource(R.dimen.session_drawable_inner_padding)
        val innerPaddingPx = (currentDensity.density * innerPaddingDp.value).toInt()

        SubcomposeLayout { constraints ->

            // Title
            val titlePlaceable = subcompose("title") {
                Row {
                    Title(
                        modifier = Modifier.weight(1f),
                        property = data.title,
                        textColor = colorResource(data.textColor),
                    )
                    if (data.recordingOptOut?.value == true || data.hasAlarm) {
                        Spacer(Modifier.width(8.dp))
                    }
                    // Icons
                    Row(
                        verticalAlignment = CenterVertically,
                    ) {
                        if (data.recordingOptOut?.value == true) {
                            VideoRecordingIcon(
                                property = data.recordingOptOut,
                            )
                        }
                        if (data.hasAlarm) {
                            AlarmIcon(
                                modifier = Modifier.padding(start = 4.dp),
                            )
                        }
                    }
                }
            }.firstOrNull()?.measure(constraints)

            // Subtitle
            val subtitlePlaceable = subcompose("subtitle") {
                data.subtitle?.let {
                    Subtitle(
                        property = it,
                        textColor = colorResource(data.textColor),
                    )
                }
            }.firstOrNull()?.measure(constraints)

            // Speaker names and languages
            val speakerNamesAndLanguagesPlaceable = subcompose("speakerNamesAndLanguages") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Start,
                    verticalAlignment = CenterVertically,
                ) {
                    data.speakerNames?.let {
                        SpeakerNames(
                            modifier = Modifier.weight(1f, fill = false),
                            property = it,
                            textColor = colorResource(data.textColor),
                        )
                    }
                    val startPadding = if (data.speakerNames?.value.isNullOrEmpty()) 0.dp else 4.dp
                    data.languages?.let {
                        Languages(
                            modifier = Modifier.padding(start = startPadding),
                            property = it,
                            textColor = colorResource(data.textColor),
                        )
                    }
                }
            }.firstOrNull()?.measure(constraints)

            // Track name
            val trackNamePlaceable = subcompose("trackName") {
                data.trackName?.let {
                    TrackName(
                        property = it,
                    )
                }
            }.firstOrNull()?.measure(constraints)

            layout(constraints.maxWidth, cardHeightPx) {
                var yPosition = 0
                yPosition += innerPaddingPx

                // Title
                var showTitle = true
                titlePlaceable?.let {
                    showTitle = yPosition + it.height + innerPaddingPx < cardHeightPx
                    if (showTitle) {
                        it.placeRelative(0, yPosition)
                        yPosition += it.height
                    }
                }

                // Subtitle
                val minWhiteSpaceAboveSubtitle = 8.dp.toPx().toInt()
                var showSubtitle = true
                subtitlePlaceable?.let {
                    showSubtitle = yPosition + minWhiteSpaceAboveSubtitle + it.height + innerPaddingPx < cardHeightPx
                    if (showTitle && showSubtitle) {
                        it.placeRelative(0, yPosition)
                        yPosition += it.height
                    }
                }

                // Speaker names and languages
                val minWhiteSpaceAboveSpeakerNames = 12.dp.toPx().toInt()
                var showSpeakerNamesAndLanguages = true
                speakerNamesAndLanguagesPlaceable?.let {
                    showSpeakerNamesAndLanguages = yPosition + minWhiteSpaceAboveSpeakerNames + it.height + innerPaddingPx < cardHeightPx
                    if (showTitle && showSubtitle && showSpeakerNamesAndLanguages) {
                        it.placeRelative(0, yPosition)
                        yPosition += it.height
                    }
                }

                // Track name
                val minWhiteSpaceAboveTrackName = 4.dp.toPx().toInt()
                trackNamePlaceable?.let {
                    val showTrackName = yPosition + minWhiteSpaceAboveTrackName + it.height + innerPaddingPx < cardHeightPx
                    if (showTitle && showSubtitle && showSpeakerNamesAndLanguages && showTrackName) {
                        val trackNameYPos = cardHeightPx - innerPaddingPx - it.height
                        it.placeRelative(0, trackNameYPos)
                    }
                }
            }
        }
    }
}

@Composable
private fun Title(
    property: SessionProperty<String>,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        Text(
            modifier = modifier
                .semantics {
                    contentDescription = property.contentDescription
                },
            text = property.value,
            fontFamily = FontFamily(Font(R.font.roboto_condensed_medium)),
            fontSize = dimensionResource(R.dimen.session_drawable_title).toTextUnit(),
            color = textColor,
            maxLines = property.maxLines,
            overflow = Ellipsis,
        )
    }
}

@Composable
private fun Subtitle(
    property: SessionProperty<String>,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = property.contentDescription
                },
            text = property.value,
            fontFamily = FontFamily(Font(R.font.roboto_condensed_regular)),
            fontSize = dimensionResource(R.dimen.session_drawable_subtitle).toTextUnit(),
            color = textColor,
            maxLines = 2,
            overflow = Ellipsis,
        )
    }
}

@Composable
private fun SpeakerNames(
    property: SessionProperty<String>,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        Row(
            verticalAlignment = CenterVertically
        ) {
            IconDecorative(
                modifier = Modifier.padding(bottom = 1.dp),
                icon = R.drawable.speaker,
                tint = textColor,
            )
            Text(
                modifier = modifier
                    .padding(start = 4.dp)
                    .semantics {
                        contentDescription = property.contentDescription
                    },
                text = property.value,
                fontFamily = FontFamily(Font(R.font.roboto_condensed_regular)),
                fontSize = dimensionResource(R.dimen.session_drawable_speakers).toTextUnit(),
                color = textColor,
                maxLines = 1,
                overflow = Ellipsis,
            )
        }
    }
}

@Composable
private fun Languages(
    property: SessionProperty<String>,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        Text(
            modifier = modifier
                .semantics {
                    contentDescription = property.contentDescription
                },
            text = "[${property.value}]",
            fontFamily = FontFamily(Font(R.font.roboto_condensed_regular)),
            fontSize = dimensionResource(R.dimen.session_drawable_languages).toTextUnit(),
            color = textColor,
            maxLines = 1,
            overflow = Ellipsis,
        )
    }
}


@Composable
private fun TrackName(
    property: SessionProperty<String>,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        Box(
            modifier = modifier
                .background(
                    color = colorResource(R.color.session_item_track_name_background),
                    shape = RoundedCornerShape(12.dp),
                )
                .defaultMinSize(minHeight = 20.dp)
                .padding(horizontal = 8.dp)
                .semantics {
                    contentDescription = property.contentDescription
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = property.value,
                fontFamily = FontFamily(Font(R.font.roboto_condensed_medium)),
                fontSize = dimensionResource(R.dimen.session_drawable_track).toTextUnit(),
                color = colorResource(R.color.session_item_track_name_text),
                maxLines = 1,
                overflow = Ellipsis,
            )
        }
    }
}

@Composable
private fun VideoRecordingIcon(
    property: SessionProperty<Boolean>,
    modifier: Modifier = Modifier,
) {
    IconDecorative(
        modifier = modifier.size(dimensionResource(R.dimen.session_drawable_icon_size)),
        icon = R.drawable.ic_novideo,
        contentDescription = property.contentDescription,
        tint = Color.Unspecified,
    )
}

@Composable
private fun AlarmIcon(
    modifier: Modifier = Modifier,
) {
    IconDecorative(
        modifier = modifier
            .size(dimensionResource(R.dimen.session_drawable_icon_size))
            .padding(dimensionResource(R.dimen.session_drawable_icon_padding)),
        icon = R.drawable.ic_bell_on_session,
        tint = colorResource(R.color.session_item_alarm_icon),
        contentDescription = stringResource(R.string.session_item_has_alarm_content_description),
    )
}

@Suppress("kotlin:S107")
@Composable
private fun ContextMenu(
    expanded: Boolean,
    isFavored: Boolean,
    hasAlarm: Boolean,
    shouldShowShareSubMenu: Boolean,
    textColor: Color,
    cardPosition: Offset,
    pressPosition: Offset,
    onMenuItemClick: (SessionInteractionType) -> Unit,
    onCollapseRequest: () -> Unit,
) {
    var showShareSubMenu by remember { mutableStateOf(false) }

    // The UI stack above the schedule content includes:
    // 1. System status bar (~24dp)
    // 2. App toolbar (~56dp)
    // 3. Progress bar (variable) or horizontal scrolling line
    // 4. Room names header
    val systemUiOffset = 132.dp // Combined height of all UI elements above schedule

    // Times layout column width
    val timeColumnWidth = dimensionResource(R.dimen.schedule_time_column_layout_width)

    val density = LocalDensity.current
    val relativeX = with(density) { pressPosition.x.toDp() }
    val relativeY = with(density) { pressPosition.y.toDp() }
    val cardPositionX = with(density) { cardPosition.x.toDp() }
    val cardPositionY = with(density) { cardPosition.y.toDp() }

    val menuXOffset = cardPositionX + relativeX - timeColumnWidth
    val menuYOffset = cardPositionY + relativeY - systemUiOffset
    val menuOffset = DpOffset(menuXOffset, menuYOffset)

    if (expanded && !showShareSubMenu) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = { onCollapseRequest() },
            offset = menuOffset
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(if (isFavored) R.string.menu_item_title_unflag_as_favorite else R.string.menu_item_title_flag_as_favorite), color = textColor) },
                onClick = {
                    onMenuItemClick(TOGGLE_FAVORITE)
                    onCollapseRequest()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(if (hasAlarm) R.string.menu_item_title_delete_alarm else R.string.menu_item_title_set_alarm), color = textColor) },
                onClick = {
                    onMenuItemClick(TOGGLE_ALARM)
                    onCollapseRequest()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_item_title_add_to_calendar), color = textColor) },
                onClick = {
                    onMenuItemClick(ADD_TO_CALENDAR)
                    onCollapseRequest()
                },
            )

            if (shouldShowShareSubMenu) {
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = SpaceBetween,
                            verticalAlignment = CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.menu_item_title_share_session), color = textColor)
                            IconDecorativeVector(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                tint = textColor,
                            )
                        }
                    },
                    onClick = {
                        showShareSubMenu = true
                    },
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_item_title_share_session), color = textColor) },
                    onClick = {
                        onMenuItemClick(SHARE)
                        onCollapseRequest()
                    },
                )
            }
        }
    }

    if (showShareSubMenu) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = {
                showShareSubMenu = false
                onCollapseRequest()
            },
            offset = menuOffset,
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = stringResource(R.string.menu_item_title_share_session),
                color = colorResource(R.color.colorAccent),
            )
            DividerHorizontal(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                thickness = 1.dp,
                color = colorResource(R.color.colorAccent),
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_item_title_share_session_text), color = textColor) },
                onClick = {
                    onMenuItemClick(SHARE_TEXT)
                    showShareSubMenu = false
                    onCollapseRequest()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_item_title_share_session_json), color = textColor) },
                onClick = {
                    onMenuItemClick(SHARE_JSON)
                    showShareSubMenu = false
                    onCollapseRequest()
                },
            )
        }
    }
}

@MultiDevicePreview
@Composable
private fun RoomColumnPreview() {
    RoomColumn(
        columnData = createRoomColumnData(),
        onSessionClick = {},
        onSessionInteraction = { _, _ -> },
    )
}

@Composable
private fun createRoomColumnData() = RoomColumnData(
    sessionData = listOf(
        createSessionCardData(height = 50, isFavored = false, hasAlarm = true, isRecorded = true),
        createSessionCardData(height = 100, isFavored = true, hasAlarm = true, isRecorded = false),
        createSessionCardData(height = 70, isFavored = false, hasAlarm = true, isRecorded = true),
        createSessionCardData(height = 200, isFavored = true, hasAlarm = false, isRecorded = true),
        createSessionCardData(height = 35, isFavored = false, hasAlarm = true, isRecorded = false),
    ),
    spacings = listOf(20, 80, 0, 8, 0),
)

@Preview()
@Composable
private fun SessionCardPreview() {
    val data = createSessionCardData(height = 200, isFavored = true, hasAlarm = true, isRecorded = false)
    SessionCard(
        data = data,
        sessionCardLayout = { SessionCardLayout(data) },
        contextMenuTextColor = Color.White,
        onClick = {},
        onMenuItemClick = {},
    )
}

@Composable
private fun createSessionCardData(
    height: Int = 100,
    isFavored: Boolean = false,
    hasAlarm: Boolean = false,
    isRecorded: Boolean = true,
) = SessionCardData(
    sessionId = stringResource(R.string.placeholder_session_id),
    title = SessionProperty(stringResource(R.string.placeholder_session_title), ""),
    subtitle = SessionProperty(stringResource(R.string.placeholder_session_subtitle), ""),
    speakerNames = SessionProperty(stringResource(R.string.placeholder_session_speakers), ""),
    languages = SessionProperty(stringResource(R.string.placeholder_session_language), ""),
    trackName = SessionProperty(stringResource(R.string.placeholder_session_track), ""),
    recordingOptOut = SessionProperty(!isRecorded, ""),
    stateContentDescription = "",
    innerHorizontalPadding = 8f,
    innerVerticalPadding = 4f,
    cardHeight = height,
    isFavored = isFavored,
    hasAlarm = hasAlarm,
    showBorder = isFavored, // omitting the app setting here for simplicity
    shouldShowShareSubMenu = false,
    backgroundColor = R.color.track_background_default,
    textColor = R.color.text_primary,
)

@Preview("Enough whitespace between speakers and track", "Scenarios")
@Composable
private fun SessionCardPreview01() {
    createSessionCard(
        title = stringResource(R.string.placeholder_session_title),
        subtitle = "",
        speakerNames = stringResource(R.string.placeholder_session_speakers),
        languages = stringResource(R.string.placeholder_session_language),
        trackName = stringResource(R.string.placeholder_session_track),
        cardHeight = 120,
        recordingOptOut = true,
        isFavored = true,
        hasAlarm = true,
        showBorder = true,
    )
}

@Preview("Not enough whitespace between speakers and track", "Scenarios")
@Composable
private fun SessionCardPreview02() {
    createSessionCard(
        title = stringResource(R.string.placeholder_session_title),
        subtitle = "",
        speakerNames = stringResource(R.string.placeholder_session_speakers),
        languages = stringResource(R.string.placeholder_session_language),
        trackName = stringResource(R.string.placeholder_session_track),
        cardHeight = 90,
        recordingOptOut = true,
        isFavored = true,
        hasAlarm = true,
        showBorder = true,
    )
}

@Preview("Enough whitespace between title and track", "Scenarios")
@Composable
private fun SessionCardPreview03() {
    createSessionCard(
        title = stringResource(R.string.placeholder_session_title),
        subtitle = "",
        speakerNames = "",
        languages = "",
        trackName = stringResource(R.string.placeholder_session_track),
        cardHeight = 90,
        recordingOptOut = true,
        isFavored = true,
        hasAlarm = true,
        showBorder = true,
    )
}

@Preview("Not enough whitespace between title and track", "Scenarios")
@Composable
private fun SessionCardPreview04() {
    createSessionCard(
        title = stringResource(R.string.placeholder_session_title),
        subtitle = "",
        speakerNames = "",
        languages = "",
        trackName = stringResource(R.string.placeholder_session_track),
        cardHeight = 70,
        recordingOptOut = true,
        isFavored = true,
        hasAlarm = true,
        showBorder = true,
    )
}

@Preview("Enough whitespace under title - two lines", "Scenarios")
@Composable
private fun SessionCardPreview05() {
    createSessionCard(
        title = stringResource(R.string.placeholder_session_title),
        subtitle = "",
        speakerNames = "",
        languages = "",
        trackName = stringResource(R.string.placeholder_session_track),
        cardHeight = 55,
        recordingOptOut = true,
        isFavored = true,
        hasAlarm = true,
        showBorder = true,
    )
}

@Preview("Not enough whitespace under title", "Scenarios")
@Composable
private fun SessionCardPreview06() {
    createSessionCard(
        title = stringResource(R.string.placeholder_session_title),
        subtitle = "",
        speakerNames = "",
        languages = "",
        trackName = stringResource(R.string.placeholder_session_track),
        cardHeight = 50,
        recordingOptOut = true,
        isFavored = true,
        hasAlarm = true,
        showBorder = true,
    )
}

@Preview("Ellipsize title", "Scenarios")
@Composable
private fun SessionCardPreview07() {
    createSessionCard(
        title = "Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Very loooooooooooooooooooooooooooooooooooong title",
        subtitle = "",
        speakerNames = "",
        languages = "",
        trackName = "",
        cardHeight = 60,
        recordingOptOut = true,
        isFavored = true,
        hasAlarm = true,
        showBorder = true,
    )
}

@Preview("Ellipsize subtitle", "Scenarios")
@Composable
private fun SessionCardPreview08() {
    createSessionCard(
        title = stringResource(R.string.placeholder_session_title),
        subtitle = "Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Very loooooooooooooooooooooooooooooooooooong subtitle",
        speakerNames = "",
        languages = "",
        trackName = "",
        cardHeight = 95,
        recordingOptOut = true,
        isFavored = true,
        hasAlarm = true,
        showBorder = true,
    )
}

@Preview("Ellipsize speakers", "Scenarios")
@Composable
private fun SessionCardPreview09() {
    createSessionCard(
        title = stringResource(R.string.placeholder_session_title),
        subtitle = stringResource(R.string.placeholder_session_subtitle),
        speakerNames = "Stephen A. Ridley, Ernest Ridley, Conan Ridley, Bridget Ridley, Frank Ridley, Barbara Ridley",
        languages = stringResource(R.string.placeholder_session_language),
        trackName = "",
        cardHeight = 110,
        recordingOptOut = true,
        isFavored = true,
        hasAlarm = true,
        showBorder = true,
    )
}

@Preview("Ellipsize track name", "Scenarios")
@Composable
private fun SessionCardPreview10() {
    createSessionCard(
        title = stringResource(R.string.placeholder_session_title),
        subtitle = stringResource(R.string.placeholder_session_subtitle),
        speakerNames = stringResource(R.string.placeholder_session_speakers),
        languages = stringResource(R.string.placeholder_session_language),
        trackName = "Very looooooooooooooooooooooooooooooooooooooooooooooong track name",
        cardHeight = 160,
        recordingOptOut = true,
        isFavored = true,
        hasAlarm = true,
        showBorder = true,
    )
}

@Suppress("kotlin:S107")
@Composable
private fun createSessionCard(
    title: String,
    subtitle: String = "",
    speakerNames: String = "",
    languages: String = "",
    trackName: String = "",
    cardHeight: Int,
    recordingOptOut: Boolean = false,
    isFavored: Boolean = false,
    hasAlarm: Boolean = false,
    showBorder: Boolean = false,
    shouldShowShareSubMenu: Boolean = false,
    backgroundColor: Int = R.color.track_background_default,
    textColor: Int = R.color.text_primary,
) {
    SessionCard(
        SessionCardData(
            sessionId = stringResource(R.string.placeholder_session_id),
            title = SessionProperty(title, ""),
            subtitle = SessionProperty(subtitle, ""),
            speakerNames = SessionProperty(speakerNames, ""),
            languages = SessionProperty(languages, ""),
            trackName = SessionProperty(trackName, ""),
            recordingOptOut = SessionProperty(recordingOptOut, ""),
            stateContentDescription = "",
            innerHorizontalPadding = 8f,
            innerVerticalPadding = 4f,
            cardHeight = cardHeight,
            isFavored = isFavored,
            hasAlarm = hasAlarm,
            showBorder = showBorder,
            shouldShowShareSubMenu = shouldShowShareSubMenu,
            backgroundColor = backgroundColor,
            textColor = textColor,
        )
    )
}

@Composable
private fun SessionCard(data: SessionCardData) {
    SessionCard(
        data = data,
        sessionCardLayout = { SessionCardLayout(data) },
        contextMenuTextColor = Color.White,
        onClick = {},
        onMenuItemClick = {},
    )
}
