package nerd.tuxmobil.fahrplan.congress.changes

import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorProperty
import nerd.tuxmobil.fahrplan.congress.commons.VideoRecordingState

sealed interface SessionChangeParameter {

    data class Separator(
        val daySeparator: DaySeparatorProperty<String>,
    ) : SessionChangeParameter

    data class SessionChange(
        val id: String,
        val title: SessionChangeProperty<String>,
        val subtitle: SessionChangeProperty<String>,
        val videoRecordingState: SessionChangeProperty<VideoRecordingState>,
        val speakerNames: SessionChangeProperty<String>,
        val dayText: SessionChangeProperty<String>,
        val startsAt: SessionChangeProperty<String>,
        val duration: SessionChangeProperty<String>,
        val roomName: SessionChangeProperty<String>,
        val languages: SessionChangeProperty<String>,
        val isCanceled: Boolean = false,
    ) : SessionChangeParameter

}
