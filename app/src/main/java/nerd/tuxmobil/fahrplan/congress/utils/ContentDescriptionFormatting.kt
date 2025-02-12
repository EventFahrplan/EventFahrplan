package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.models.Session

interface ContentDescriptionFormatting {

    fun getDurationContentDescription(duration: Int): String

    fun getTitleContentDescription(title: String): String

    fun getSubtitleContentDescription(subtitle: String): String

    fun getRoomNameContentDescription(roomName: String): String

    fun getSpeakersContentDescription(speakersCount: Int, formattedSpeakerNames: String): String

    fun getTrackNameAndLanguageContentDescription(trackName: String, languageCode: String): String

    fun getLanguageContentDescription(languageCode: String): String

    fun getStartTimeContentDescription(startTimeText: String): String

    fun getStateContentDescription(session: Session, useDeviceTimeZone: Boolean): String
}
