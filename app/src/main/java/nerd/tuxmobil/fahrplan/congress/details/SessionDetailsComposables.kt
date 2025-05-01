package nerd.tuxmobil.fahrplan.congress.details

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign.Companion.End
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.HtmlStyle
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import be.digitalia.compose.htmlconverter.htmlToString
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownListItems
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.DefaultMarkdownAnimation
import com.mikepenz.markdown.model.markdownPadding
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.R.color.text_link_on_light
import nerd.tuxmobil.fahrplan.congress.R.color.text_link_pressed_background_on_light
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorative
import nerd.tuxmobil.fahrplan.congress.designsystem.resources.floatResource
import nerd.tuxmobil.fahrplan.congress.designsystem.screenstates.Loading
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsParameter.SessionDetails
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsProperty.MarkupLanguage
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsProperty.MarkupLanguage.Html
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsProperty.MarkupLanguage.Markdown
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsState.Loading
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsState.Success
import nerd.tuxmobil.fahrplan.congress.extensions.toTextUnit

@Composable
internal fun SessionDetailsScreen(
    sessionDetailsState: SessionDetailsState,
    showRoomState: Boolean,
    roomStateMessage: String,
) {
    EventFahrplanTheme {
        Scaffold { contentPadding ->
            val contentAlignment = if (sessionDetailsState is Loading) Alignment.Center else Alignment.TopStart
            Box(
                Modifier
                    .padding(contentPadding)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = contentAlignment,
            ) {
                when (sessionDetailsState) {
                    Loading -> Loading()
                    is Success -> {
                        val parameter = sessionDetailsState.sessionDetailsParameter as SessionDetails
                        SessionDetails(parameter, showRoomState, roomStateMessage)
                    }

                }
            }
        }
    }
}

private val textLinkStyles: TextLinkStyles
    @Composable
    get() = TextLinkStyles(
        style = TextStyle(
            color = colorResource(text_link_on_light),
            textDecoration = TextDecoration.Underline,
        ).toSpanStyle(),
        pressedStyle = TextStyle(
            background = colorResource(text_link_pressed_background_on_light),
        ).toSpanStyle(),
    )

@Composable
fun SessionDetails(session: SessionDetails, showRoomState: Boolean, roomStateMessage: String) {
    val htmlStyle = HtmlStyle(
        textLinkStyles = textLinkStyles,
    )

    SelectionContainer {
        Column(
            modifier = Modifier,
        ) {
            with(session) {
                DetailBar(this)
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.session_details_common_space_between_sections)),
                ) {
                    RoomState(showRoomState, roomStateMessage)
                    Title(title)
                    Subtitle(subtitle)
                    SpeakerNames(speakerNames, Modifier.padding(top = dimensionResource(R.dimen.session_details_extra_space_above_speaker_names)))
                    Abstract(abstract, htmlStyle)
                    Description(description, htmlStyle)
                    Links(links, htmlStyle, Modifier.padding(top = dimensionResource(R.dimen.session_details_extra_space_above_section_header)))
                    TrackName(trackName, Modifier.padding(top = dimensionResource(R.dimen.session_details_extra_space_above_section_header)))
                    SessionLink(sessionLink, htmlStyle, Modifier.padding(top = dimensionResource(R.dimen.session_details_extra_space_above_section_header)))
                }
            }
        }
    }
}

@Composable
private fun DetailBar(
    sessionDetails: SessionDetails,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(colorResource(R.color.session_detailbar_background))
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextLeadingIcon(
            property = sessionDetails.startsAt,
            icon = R.drawable.ic_access_time,
        )
        TextLeadingIcon(
            property = sessionDetails.roomName,
            icon = R.drawable.ic_room,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = sessionDetails.id.contentDescription
                },
            text = sessionDetails.id.value.uppercase(),
            fontSize = dimensionResource(R.dimen.session_detailbar_text).toTextUnit(),
            color = colorResource(R.color.session_detailbar_text),
            overflow = Ellipsis,
            maxLines = 1,
            textAlign = End,
        )
    }
}

@Composable
private fun RoomState(showRoomState: Boolean, roomStateMessage: String) {
    if (showRoomState) {
        Box(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .background(
                    colorResource(R.color.session_details_room_state_info_background),
                    shape = RoundedCornerShape(6.dp),
                )
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                text = roomStateMessage,
                color = colorResource(R.color.session_details_room_state_info_text),
            )
        }
    }
}

@Composable
private fun Title(
    property: SessionDetailsProperty<String>,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        val fontSize = dimensionResource(R.dimen.session_details_title).toTextUnit()
        val multiplier = floatResource(R.dimen.session_details_title_line_spacing_multiplier)
        val lineHeight = fontSize.times(multiplier)
        TextSectionHeader(
            modifier = modifier
                .semantics {
                    contentDescription = property.contentDescription
                },
            text = property.value,
            fontSize = fontSize,
            lineHeight = lineHeight,
        )
    }
}

@Composable
private fun Subtitle(
    property: SessionDetailsProperty<String>,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        TextSection(
            modifier = modifier
                .semantics {
                    contentDescription = property.contentDescription
                },
            text = property.value,
            fontSize = dimensionResource(R.dimen.session_details_subtitle).toTextUnit(),
        )
    }
}

@Composable
private fun SpeakerNames(
    property: SessionDetailsProperty<String>,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        TextSection(
            modifier = modifier
                .semantics {
                    contentDescription = property.contentDescription
                },
            text = property.value.uppercase(),
            fontSize = dimensionResource(R.dimen.session_details_speakers).toTextUnit(),
        )
    }
}

@Composable
private fun Abstract(
    property: SessionDetailsProperty<MarkupLanguage>,
    htmlStyle: HtmlStyle,
    modifier: Modifier = Modifier,
) {
    if (property.value.text.isNotEmpty()) {
        Box(modifier = modifier) {
            when (val markupLanguage = property.value) {
                is Html -> AbstractHtml(
                    text = markupLanguage.text,
                    contentDescription = htmlToString(property.contentDescription),
                    htmlStyle = htmlStyle,
                )

                is Markdown -> TextMarkdown(
                    text = markupLanguage.text,
                    contentDescription = property.contentDescription,
                    isAbstract = true,
                )
            }
        }
    }
}

@Composable
private fun AbstractHtml(
    text: String,
    contentDescription: String,
    htmlStyle: HtmlStyle,
) {
    TextSectionHeader(
        modifier = Modifier
            .semantics {
                this.contentDescription = contentDescription
            },
        text = remember(text) { getAnnotatedString(text, htmlStyle) },
    )
}


@Composable
private fun Description(
    property: SessionDetailsProperty<MarkupLanguage>,
    htmlStyle: HtmlStyle,
    modifier: Modifier = Modifier,
) {
    if (property.value.text.isNotEmpty()) {
        Box(modifier = modifier) {
            when (val markupLanguage = property.value) {
                is Html -> DescriptionHtml(
                    text = markupLanguage.text,
                    contentDescription = htmlToString(property.contentDescription),
                    htmlStyle = htmlStyle,
                )

                is Markdown -> TextMarkdown(
                    text = markupLanguage.text,
                    contentDescription = property.contentDescription,
                    isAbstract = false,
                )
            }
        }
    }
}

@Composable
private fun DescriptionHtml(
    text: String,
    contentDescription: String,
    htmlStyle: HtmlStyle,
) {
    TextSection(
        modifier = Modifier
            .semantics {
                this.contentDescription = contentDescription
            },
        text = remember(text) { getAnnotatedString(text, htmlStyle) },
    )
}

@Composable
private fun TextMarkdown(
    text: String,
    contentDescription: String,
    isAbstract: Boolean,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalMarkdownColors provides markdownColor(
        linkText = colorResource(R.color.text_link_on_light),
    )) {
        Markdown(
            modifier = modifier
                .semantics {
                    this.contentDescription = contentDescription
                },
            content = text,
            colors = LocalMarkdownColors.current,
            typography = markdownTypography(
                h1 = TextStyle(
                    fontSize = dimensionResource(R.dimen.session_details_headline1).toTextUnit(),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                ),
                h2 = TextStyle(
                    fontSize = dimensionResource(R.dimen.session_details_headline2).toTextUnit(),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                ),
                h3 = TextStyle(
                    fontSize = dimensionResource(R.dimen.session_details_headline3).toTextUnit(),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.None,
                ),
                paragraph = EventFahrplanTheme.typography.bodyLarge.copy(
                    fontWeight = if (isAbstract) FontWeight.Bold else FontWeight.Normal,
                ),
                textLink = textLinkStyles,
            ),
            padding = markdownPadding(
                listItemTop = 0.dp,
                listItemBottom = 0.dp,
            ),
            components = markdownComponents(
                unorderedList = unorderedList,
                orderedList = orderedList,
            ),
            // Bug fix to patch broken rendering in tablet details view. Disables animations.
            // See https://github.com/mikepenz/multiplatform-markdown-renderer/issues/330
            animations = DefaultMarkdownAnimation(fun Modifier.(): Modifier = this),
        )
    }
}


@Composable
private fun Links(
    property: SessionDetailsProperty<String>,
    htmlStyle: HtmlStyle,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        Column(modifier = modifier) {
            TextSectionHeader(
                text = stringResource(R.string.session_details_section_title_links),
            )
            TextSection(
                modifier = Modifier
                    .semantics {
                        contentDescription = property.contentDescription
                    },
                text = remember(property.value) { getAnnotatedString(property.value, htmlStyle) },
            )
        }
    }
}

@Composable
private fun TrackName(
    property: SessionDetailsProperty<String>,
    modifier: Modifier = Modifier,
) {
    if (property.value.isNotEmpty()) {
        val headerText = stringResource(R.string.session_details_section_title_track)
        Column(modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "$headerText: ${property.contentDescription}"
            }
        ) {
            TextSectionHeader(
                text = headerText,
            )
            TextSection(
                text = property.value,
            )
        }
    }
}

@Composable
private fun SessionLink(
    sessionLink: String,
    htmlStyle: HtmlStyle,
    modifier: Modifier = Modifier,
) {
    if (sessionLink.isNotEmpty()) {
        val headerText = stringResource(R.string.session_details_section_title_session_online)
        val plainSessionLink = htmlToString(sessionLink)
        Column(modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "$headerText: $plainSessionLink"
            }
        ) {
            TextSectionHeader(
                text = headerText,
            )
            TextSection(
                text = remember(sessionLink) { getAnnotatedString(sessionLink, htmlStyle) },
            )
        }
    }
}

@Composable
private fun TextLeadingIcon(
    property: SessionDetailsProperty<String>,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = CenterVertically
    ) {
        val iconPadding = dimensionResource(R.dimen.session_detailbar_icon_padding)
        IconDecorative(
            modifier = Modifier
                .padding(start = iconPadding, end = iconPadding, bottom = iconPadding),
            icon = icon,
            tint = colorResource(R.color.session_detailbar_icon),
        )
        Text(
            modifier = Modifier
                .semantics {
                    contentDescription = property.contentDescription
                },
            text = property.value.uppercase(),
            fontSize = dimensionResource(R.dimen.session_detailbar_text).toTextUnit(),
            color = colorResource(R.color.session_detailbar_text),
            overflow = Ellipsis,
            maxLines = 1,
        )
    }
}

@Composable
private fun TextSectionHeader(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    TextSection(
        modifier = modifier,
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        lineHeight = lineHeight,
    )
}

@Composable
private fun TextSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    TextSection(
        modifier = modifier,
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        lineHeight = lineHeight,
    )
}

@Composable
private fun TextSection(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = if (fontSize == TextUnit.Unspecified) dimensionResource(R.dimen.session_details_text).toTextUnit() else fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
    )
}

@Composable
private fun TextSection(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = if (fontSize == TextUnit.Unspecified) dimensionResource(R.dimen.session_details_text).toTextUnit() else fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
    )
}

private val unorderedList: @Composable ColumnScope.(MarkdownComponentModel) -> Unit = { model ->
    val style = LocalMarkdownTypography.current.bullet
    MarkdownListItems(model.content, model.node, style, level = 0) { _, _ ->
        Text(
            text = "⦁ ",
            color = colorResource(R.color.session_details_list_item),
            modifier = Modifier.size(dimensionResource(R.dimen.session_details_text_bullet)),
        )
    }
}

private val orderedList: @Composable ColumnScope.(MarkdownComponentModel) -> Unit = {
    val style = LocalMarkdownTypography.current.ordered
    MarkdownListItems(it.content, it.node, style, level = 0) { index, _ ->
        Text(
            text = "${index + 1}. ",
            color = colorResource(R.color.session_details_list_item),
        )
    }
}

private fun getAnnotatedString(html: String, htmlStyle: HtmlStyle) =
    htmlToAnnotatedString(html, compactMode = true, htmlStyle)

@MultiDevicePreview
@Composable
private fun SessionDetailsScreenPreview() {
    SessionDetailsScreen(
        sessionDetailsState = Success(
            SessionDetails(
                id = SessionDetailsProperty(stringResource(R.string.placeholder_session_id), ""),
                title = SessionDetailsProperty(stringResource(R.string.placeholder_session_title), ""),
                subtitle = SessionDetailsProperty(stringResource(R.string.placeholder_session_subtitle), ""),
                speakerNames = SessionDetailsProperty(stringResource(R.string.placeholder_session_speakers), ""),
                abstract = SessionDetailsProperty(Markdown(stringResource(R.string.placeholder_session_abstract)), ""),
                description = SessionDetailsProperty(Markdown(stringResource(R.string.placeholder_session_description)), ""),
                trackName = SessionDetailsProperty(stringResource(R.string.placeholder_session_track), ""),
                links = SessionDetailsProperty(stringResource(R.string.placeholder_session_links), ""),
                startsAt = SessionDetailsProperty(stringResource(R.string.placeholder_session_date), ""),
                roomName = SessionDetailsProperty(stringResource(R.string.placeholder_session_location), ""),
                sessionLink = stringResource(R.string.placeholder_session_online),
            )
        ),
        showRoomState = true,
        roomStateMessage = stringResource(R.string.room_state_text),
    )
}

private const val EXAMPLE_MARKDOWN = """# About
 - In this session you learn how to build the [38C3 Fahrplan app for Android](https://eventfahrplan.eu) yourself.
 - You learn how to customize colors and graphics, enable or disable certain features, translate the app or even prepare the app for your own event.

 # Language
 - I will talk in English to reach most people.
 - Ich werde auf Englisch sprechen, um die Mehrzahl der Menschen zu erreichen.

 # Requirements
 1. Some experience with Android, Kotlin, Git is helpful.
 2. Bring your own Android smartphone or tablet (minimum Android 5, Lollipop).
 3. Bring your own USB cable fitting with your Android device & computer.
 4. Bring your own computer with [Android Studio (latest stable)](https://developer.android.com/studio) installed.
 5. Have the project **already cloned** to your machine. ⚠️ Here is the [source code](https://github.com/EventFahrplan/EventFahrplan)
 6. Build the project at least once to download the Android SDK and libraries **before** you come. ⚠️ This will take some time!

 # Your ideas
 - I am looking forward to getting to know your ideas shared with everyone. Let them become reality!"""

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TextMarkdownPreview() {
    TextMarkdown(
        text = EXAMPLE_MARKDOWN,
        contentDescription = "",
        isAbstract = false,
    )
}

private const val EXAMPLE_HTML = """<h1>About</h1>
<ul>
    <li>In this session, you will learn how to build the <a href="https://eventfahrplan.eu">38C3 Fahrplan app for Android</a> yourself.</li>
    <li>You will learn how to customize colors and graphics, enable or disable certain features, translate the app, or even prepare the app for your own event.</li>
</ul>

<h1>Language</h1>
<ul>
    <li>I will talk in English to reach most people.</li>
    <li>Ich werde auf Englisch sprechen, um die Mehrzahl der Menschen zu erreichen.</li>
</ul>

<h1>Requirements</h1>
<ol>
    <li>Some experience with Android, Kotlin, and Git is helpful.</li>
    <li>Bring your own Android smartphone or tablet (minimum Android 5, Lollipop).</li>
    <li>Bring your own USB cable fitting with your Android device & computer.</li>
    <li>Bring your own computer with <a href="https://developer.android.com/studio">Android Studio (latest stable)</a> installed.</li>
    <li>Have the project <strong>already cloned</strong> to your machine. ⚠️ Here is the <a href="https://github.com/EventFahrplan/EventFahrplan">source code</a>.</li>
    <li>Build the project at least once to download the Android SDK and libraries <strong>before</strong> you come. ⚠️ This will take some time!</li>
</ol>

<h1>Your Ideas</h1>
<ul>
    <li>I am looking forward to getting to know your ideas shared with everyone. Let them become reality!</li>
</ul>"""

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun DescriptionHtmlPreview() {
    DescriptionHtml(
        text = EXAMPLE_HTML,
        contentDescription = "",
        htmlStyle = HtmlStyle(
            textLinkStyles = textLinkStyles,
        ),
    )
}

@Preview
@Composable
private fun SessionDetailsScreenLoadingPreview() {
    SessionDetailsScreen(
        sessionDetailsState = Loading,
        showRoomState = false,
        roomStateMessage = stringResource(R.string.room_state_text),
    )
}
