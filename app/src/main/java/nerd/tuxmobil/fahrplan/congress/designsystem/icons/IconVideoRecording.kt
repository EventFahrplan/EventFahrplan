package nerd.tuxmobil.fahrplan.congress.designsystem.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Available
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Unavailable
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun IconVideoRecording(videoRecordingState: VideoRecordingState, tintColor: Color?) {
    if (videoRecordingState is VideoRecordingState.Drawable) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.15.dp),
        ) {
            val color = tintColor ?: Color.White
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

@PreviewLightDark
@Composable
private fun IconVideoRecordingPreview() {
    EventFahrplanTheme {
        Row(Modifier.background(EventFahrplanTheme.colorScheme.background)) {

            IconVideoRecording(Available, tintColor = null)
            IconVideoRecording(Available, EventFahrplanTheme.colorScheme.scheduleChangeUnchangedText)
            IconVideoRecording(Available, EventFahrplanTheme.colorScheme.scheduleChangeNew)
            IconVideoRecording(Available, EventFahrplanTheme.colorScheme.scheduleChangeCanceled)
            IconVideoRecording(Available, EventFahrplanTheme.colorScheme.scheduleChangeChanged)

            IconVideoRecording(Unavailable, tintColor = null)
            IconVideoRecording(Unavailable, EventFahrplanTheme.colorScheme.scheduleChangeUnchangedText)
            IconVideoRecording(Unavailable, EventFahrplanTheme.colorScheme.scheduleChangeNew)
            IconVideoRecording(Unavailable, EventFahrplanTheme.colorScheme.scheduleChangeCanceled)
            IconVideoRecording(Unavailable, EventFahrplanTheme.colorScheme.scheduleChangeChanged)
        }
    }
}
