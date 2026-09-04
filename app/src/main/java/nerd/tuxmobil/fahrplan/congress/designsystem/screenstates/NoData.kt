package nerd.tuxmobil.fahrplan.congress.designsystem.screenstates

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.toTextUnit

@Composable
fun NoData(
    @DrawableRes emptyContent: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onContentSizeChanged: (IntSize) -> Unit = {},
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.empty_screen_padding)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier.onSizeChanged(onContentSizeChanged),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = Modifier.padding(dimensionResource(R.dimen.empty_screen_drawable_padding)),
                painter = painterResource(emptyContent),
                contentDescription = null,
            )
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = dimensionResource(R.dimen.empty_screen_text).toTextUnit(),
                lineHeight = TextUnit(1.5f, TextUnitType.Em),
            )
            Text(
                modifier = Modifier
                    .widthIn(max = dimensionResource(R.dimen.empty_screen_subtitle_max_width))
                    .padding(top = dimensionResource(R.dimen.empty_screen_subtitle_padding_top)),
                text = subtitle,
                textAlign = TextAlign.Center,
                fontSize = dimensionResource(R.dimen.empty_screen_text).toTextUnit(),
                lineHeight = TextUnit(1.5f, TextUnitType.Em),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NoDataNoSchedulePreview() {
    EventFahrplanTheme {
        NoData(
            emptyContent = R.drawable.no_schedule,
            title = stringResource(R.string.schedule_no_schedule_data_title),
            subtitle = stringResource(R.string.schedule_no_schedule_data_subtitle),
        )
    }
}
