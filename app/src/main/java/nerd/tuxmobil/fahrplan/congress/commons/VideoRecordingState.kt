package nerd.tuxmobil.fahrplan.congress.commons

import nerd.tuxmobil.fahrplan.congress.R

sealed interface VideoRecordingState {

    data object None : VideoRecordingState

    sealed interface Drawable : VideoRecordingState {

        val drawable: Int
        val contentDescription: Int

        data object Available : Drawable {
            override val drawable: Int get() = R.drawable.ic_video_recording_available
            override val contentDescription: Int get() = R.string.session_list_item_video_content_description
        }

        data object Unavailable : Drawable {
            override val drawable: Int get() = R.drawable.ic_video_recording_unavailable
            override val contentDescription: Int get() = R.string.session_list_item_no_video_content_description
        }

    }

}
