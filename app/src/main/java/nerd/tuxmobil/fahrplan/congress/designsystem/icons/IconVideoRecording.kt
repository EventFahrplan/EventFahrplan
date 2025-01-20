package nerd.tuxmobil.fahrplan.congress.designsystem.icons

import androidx.annotation.ColorRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Available
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState.Drawable.Unavailable

@Composable
fun IconVideoRecording(videoRecordingState: VideoRecordingState, @ColorRes tintColor: Int?) {
    if (videoRecordingState is VideoRecordingState.Drawable) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.15.dp),
        ) {
            val color = if (tintColor == null) Color.White else colorResource(tintColor)
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
private fun IconVideoRecordingPreview() {
    Row {
        IconVideoRecording(Available, tintColor = null)
        IconVideoRecording(Available, R.color.schedule_change_new_on_dark)
        IconVideoRecording(Available, R.color.schedule_change_canceled_on_dark)
        IconVideoRecording(Available, R.color.schedule_change_on_dark)
        IconVideoRecording(Unavailable, tintColor = null)
        IconVideoRecording(Unavailable, R.color.schedule_change_new_on_dark)
        IconVideoRecording(Unavailable, R.color.schedule_change_canceled_on_dark)
        IconVideoRecording(Unavailable, R.color.schedule_change_on_dark)
    }
}
