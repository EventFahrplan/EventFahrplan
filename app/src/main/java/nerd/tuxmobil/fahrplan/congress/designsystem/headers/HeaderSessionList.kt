package nerd.tuxmobil.fahrplan.congress.designsystem.headers

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun HeaderSessionList(
    text: String,
    modifier: Modifier = Modifier,
    includeDefaultPadding: Boolean = true,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    Text(
        modifier = modifier.then(
            if (includeDefaultPadding) {
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            } else {
                Modifier
            },
        ),
        text = text,
        fontWeight = Bold,
        fontSize = 22.sp,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@PreviewLightDark
@Composable
private fun HeaderSessionListScheduleChangesPreview() {
    EventFahrplanTheme {
        HeaderSessionList(stringResource(R.string.schedule_changes))
    }
}
