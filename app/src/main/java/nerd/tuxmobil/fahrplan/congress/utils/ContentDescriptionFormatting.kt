package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.models.Session

interface ContentDescriptionFormatting {

    fun getSessionIdContentDescription(sessionId: String): String

    fun getDurationContentDescription(duration: Int): String

    fun getTitleContentDescription(title: String): String

    fun getSubtitleContentDescription(subtitle: String): String

    fun getRoomNameContentDescription(roomName: String): String

    fun getSpeakersContentDescription(speakersCount: Int, formattedSpeakerNames: String): String

    fun getTrackNameContentDescription(trackName: String): String

    fun getLanguageContentDescription(languageCode: String): String

    fun getStartTimeContentDescription(startTimeText: String): String

    fun getStateContentDescription(session: Session, useDeviceTimeZone: Boolean): String

}
