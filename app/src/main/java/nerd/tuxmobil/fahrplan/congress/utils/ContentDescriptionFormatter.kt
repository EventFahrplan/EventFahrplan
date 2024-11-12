package nerd.tuxmobil.fahrplan.congress.utils

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.models.Session

class ContentDescriptionFormatter(val resourceResolving: ResourceResolving) {

    fun getDurationContentDescription(duration: Int) =
        resourceResolving.getString(R.string.session_list_item_duration_content_description, duration)

    fun getTitleContentDescription(title: String) =
        if (title.isEmpty()) "" else resourceResolving.getString(
            R.string.session_list_item_title_content_description, title
        )

    fun getSubtitleContentDescription(subtitle: String): String =
        if (subtitle.isEmpty()) "" else resourceResolving.getString(
            R.string.session_list_item_subtitle_content_description, subtitle
        )

    fun getRoomNameContentDescription(roomName: String): String {
        return resourceResolving.getString(R.string.session_list_item_room_content_description, roomName)
    }

    fun getSpeakersContentDescription(speakersCount: Int, formattedSpeakerNames: String): String =
        if (speakersCount == 0 || formattedSpeakerNames.isEmpty()) {
            resourceResolving.getString(R.string.session_list_item_zero_speakers_content_description)
        } else {
            resourceResolving.getQuantityString(
                R.plurals.session_list_item_speakers_content_description,
                speakersCount,
                formattedSpeakerNames
            )
        }

    fun getFormattedTrackContentDescription(trackName: String, languageCode: String) =
        buildString {
            append(
                resourceResolving.getString(
                    R.string.session_list_item_track_content_description,
                    trackName
                )
            )
            if (languageCode.isNotEmpty()) {
                append("; ")
                append(getLanguageContentDescription(languageCode))
            }
        }

    fun getLanguageContentDescription(languageCode: String): String {
        if (languageCode.isEmpty()) {
            return resourceResolving.getString(R.string.session_list_item_language_unknown_content_description)
        }
        val languageName = when (languageCode) {
            "en" -> resourceResolving.getString(R.string.session_list_item_language_english_content_description)
            "de" -> resourceResolving.getString(R.string.session_list_item_language_german_content_description)
            "pt" -> resourceResolving.getString(R.string.session_list_item_language_portuguese_content_description)
            else -> languageCode
        }
        return resourceResolving.getString(
            R.string.session_list_item_language_content_description,
            languageName
        )
    }

    fun getStartTimeContentDescription(startTimeText: String) =
        resourceResolving.getString(R.string.session_list_item_start_time_content_description, startTimeText)

    private fun getHighlightContentDescription(isHighlighted: Boolean): String {
        val stringResource = if (isHighlighted)
            R.string.session_list_item_favored_content_description
        else
            R.string.session_list_item_not_favored_content_description
        return resourceResolving.getString(stringResource)
    }

    fun getStateContentDescription(session: Session, useDeviceTimeZone: Boolean): String {
        val roomNameContentDescription: String = getRoomNameContentDescription(session.roomName)
        val startsAtText = DateFormatter
            .newInstance(useDeviceTimeZone)
            .getFormattedTime(session.dateUTC, session.timeZoneOffset)
        val startsAtContentDescription = getStartTimeContentDescription(startsAtText)
        val isHighlightContentDescription = getHighlightContentDescription(session.isHighlight)
        return "$isHighlightContentDescription, $startsAtContentDescription, $roomNameContentDescription"
    }

}
