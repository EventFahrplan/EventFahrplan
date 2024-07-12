package nerd.tuxmobil.fahrplan.congress.commons

import android.view.Gravity.CENTER
import android.view.Gravity.START
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons.AutoMirrored.Default
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.Empty
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.Html
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.PostalAddress
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Available
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Unavailable
import nerd.tuxmobil.fahrplan.congress.extensions.toSpanned
import nerd.tuxmobil.fahrplan.congress.extensions.toTextUnit
import nerd.tuxmobil.fahrplan.congress.utils.LinkMovementMethodCompat

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(R.color.colorPrimary),
        ),
        title = {
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = SemiBold,
                color = colorResource(R.color.text_primary),
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { onBack() },
            ) {
                Icon(
                    imageVector = Default.ArrowBack,
                    tint = colorResource(R.color.text_primary),
                    contentDescription = stringResource(R.string.navigate_back_content_description),
                )
            }
        },
        actions = {
            actions()
        },
    )
}

@Preview
@Composable
private fun TopBarPreview() {
    TopBar(
        title = "TopBar Title",
        onBack = {},
        actions = {},
    )
}

@Composable
fun Loading() {
    Box(
        Modifier.fillMaxSize(),
        Alignment.Center
    ) {
        CircularProgressIndicator(Modifier.size(48.dp))
    }
}

@Composable
fun NoData(
    @DrawableRes emptyContent: Int,
    title: String,
    subtitle: String,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.empty_screen_padding)),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Center,
    ) {
        Image(
            modifier = Modifier.padding(dimensionResource(R.dimen.empty_screen_drawable_padding)),
            painter = painterResource(emptyContent),
            contentDescription = null,
        )
        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontWeight = Bold,
            fontSize = dimensionResource(R.dimen.empty_screen_text).toTextUnit(),
            lineHeight = TextUnit(1.5f, TextUnitType.Em),
        )
        Text(
            modifier = Modifier.padding(top = dimensionResource(R.dimen.empty_screen_subtitle_padding_top)),
            text = subtitle,
            textAlign = TextAlign.Center,
            fontSize = dimensionResource(R.dimen.empty_screen_text).toTextUnit(),
            lineHeight = TextUnit(1.5f, TextUnitType.Em),
        )
    }
}

@Preview
@Composable
private fun NoDataNoSchedulePreview() {
    NoData(
        emptyContent = R.drawable.no_schedule,
        title = stringResource(R.string.schedule_no_schedule_data_title),
        subtitle = stringResource(R.string.schedule_no_schedule_data_subtitle),
    )
}

@Composable
fun ClickableText(
    textResource: TextResource,
    fontSize: TextUnit,
    textAlign: TextAlign,
    @ColorRes textColor: Int,
    @ColorRes textLinkColor: Int,
    onClick: (String) -> Unit = {},
) {
    var vertical = 0.dp
    if (textResource is PostalAddress && textResource.text.isNotEmpty()) {
        vertical = 4.dp
    }
    if (textResource is Html && textResource.html.isNotEmpty()) {
        vertical = 4.dp
    }
    Box(Modifier.padding(horizontal = 16.dp, vertical = vertical)) {
        when (textResource) {
            Empty -> Unit
            is PostalAddress -> {
                GenericClickableText(
                    text = textResource.text,
                    plainUrl = textResource.text,
                    fontSize = fontSize,
                    textAlign = textAlign,
                    textLinkColor = colorResource(textLinkColor),
                    onClick = { onClick(textResource.text) },
                )
            }

            is Html -> {
                AndroidView(
                    factory = { context ->
                        TextView(context).apply {
                            movementMethod = LinkMovementMethodCompat.getInstance()
                            setTextColor(ContextCompat.getColor(context, textColor))
                            setLinkTextColor(ContextCompat.getColor(context, textLinkColor))
                            gravity = if (textAlign == TextAlign.Center) CENTER else START
                            textSize = fontSize.value
                            text = textResource.html.toSpanned()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun GenericClickableText(
    text: String,
    plainUrl: String,
    fontSize: TextUnit,
    textAlign: TextAlign,
    textLinkColor: Color,
    onClick: (String) -> Unit,
) {
    if (text.isNotEmpty()) {
        val tag = "URL"
        val annotatedString = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = textLinkColor,
                    textDecoration = Underline,
                    fontSize = fontSize,
                )
            ) {
                append(text)
            }
            addStringAnnotation(
                tag = tag,
                annotation = plainUrl,
                start = 0,
                end = text.length,
            )
        }

        ClickableText(
            text = annotatedString,
            style = TextStyle(
                textAlign = textAlign,
            ),
            onClick = {
                annotatedString
                    .getStringAnnotations(tag = tag, start = it, end = it)
                    .firstOrNull()
                    ?.let { range -> onClick(range.item) }
            }
        )
    }
}

@Preview
@Composable
private fun ClickableTextPostalAddressPreview() {
    ClickableText(
        textResource = PostalAddress("Congressplatz 1, 20355 Hamburg"),
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        textColor = -1,
        textLinkColor = android.R.color.holo_blue_light,
        onClick = {},
    )
}

@Preview
@Composable
private fun ClickableTextHtmlPreview() {
    ClickableText(
        textResource = Html("""Design by <a href="https://eventfahrplan.eu">eventfahrplan.eu</a>"""),
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        textColor = R.color.colorPrimary,
        textLinkColor = android.R.color.holo_purple,
        onClick = {},
    )
}

@Composable
fun VideoRecordingIcon(videoRecordingState: VideoRecordingState, @ColorRes tintColor: Int?) {
    if (videoRecordingState is VideoRecordingState.Drawable) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.15.dp),
        ) {
            val color = if (tintColor == null) White else colorResource(tintColor)
            Image(
                painter = painterResource(videoRecordingState.drawable),
                colorFilter = ColorFilter.tint(color),
                contentDescription = stringResource(videoRecordingState.contentDescription),
            )
            if (videoRecordingState == Unavailable) {
                Image(
                    painter = painterResource(R.drawable.ic_video_recording_unavailable_overlay),
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
private fun VideoRecordingIconPreview() {
    Row {
        VideoRecordingIcon(Available, tintColor = null)
        VideoRecordingIcon(Available, R.color.schedule_change_new)
        VideoRecordingIcon(Available, R.color.schedule_change_canceled)
        VideoRecordingIcon(Available, R.color.schedule_change)
        VideoRecordingIcon(Unavailable, tintColor = null)
        VideoRecordingIcon(Unavailable, R.color.schedule_change_new)
        VideoRecordingIcon(Unavailable, R.color.schedule_change_canceled)
        VideoRecordingIcon(Unavailable, R.color.schedule_change)
    }
}

@Composable
fun DayDateSeparatorItem(text: String) {
    Column(
        Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
    ) {
        val color = colorResource(R.color.text_link_on_light)
        Text(
            color = color,
            text = text.uppercase(),
            fontSize = 13.sp,
            fontWeight = Bold,
        )
        HorizontalDivider(thickness = 1.dp, color = color)
    }
}

@Preview
@Composable
private fun DayDateSeparatorItemPreview() {
    DayDateSeparatorItem("Day 1 - 31.02.2023")
}

@Composable
fun SessionListHeader(text: String) {
    Text(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        text = text,
        fontWeight = Bold,
        fontSize = 22.sp,
        color = colorResource(R.color.session_list_header_text),
    )
}

@Preview
@Composable
private fun SessionListHeaderScheduleChangesPreview() {
    SessionListHeader(stringResource(R.string.schedule_changes))
}
