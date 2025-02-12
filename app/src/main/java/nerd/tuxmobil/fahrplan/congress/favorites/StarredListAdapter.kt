package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.SessionsAdapter
import nerd.tuxmobil.fahrplan.congress.extensions.textOrHide
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter

class StarredListAdapter internal constructor(

        context: Context,
        list: List<Session>,
        numDays: Int,
        useDeviceTimeZone: Boolean,
        private val sessionPropertiesFormatter: SessionPropertiesFormatter,
        private val contentDescriptionFormatting: ContentDescriptionFormatting,

) : SessionsAdapter(

        context,
        R.layout.session_list_item,
        list,
        numDays,
        useDeviceTimeZone

) {

    @ColorInt
    private val pastSessionTextColor = ContextCompat.getColor(context, R.color.favorites_past_session_text)

    override fun setItemContent(position: Int, viewHolder: ViewHolder) {
        resetItemStyles(viewHolder)

        val session = getSession(position)
        with(viewHolder) {
            if (session.tookPlace) {
                title.setPastSessionTextColor()
                subtitle.setPastSessionTextColor()
                speakers.setPastSessionTextColor()
                lang.setPastSessionTextColor()
                day.setPastSessionTextColor()
                time.setPastSessionTextColor()
                room.setPastSessionTextColor()
                duration.setPastSessionTextColor()
                withoutVideoRecording.setImageResource(R.drawable.ic_without_video_recording_took_place)
            }

            title.textOrHide = session.title
            subtitle.textOrHide = session.subtitle
            subtitle.contentDescription = contentDescriptionFormatting
                .getSubtitleContentDescription(session.subtitle)

            val speakerNames = sessionPropertiesFormatter.getFormattedSpeakers(session)
            speakers.textOrHide = speakerNames
            speakers.contentDescription = contentDescriptionFormatting
                .getSpeakersContentDescription(session.speakers.size, speakerNames)
            val languageText = sessionPropertiesFormatter.getLanguageText(session)
            lang.textOrHide = languageText
            lang.contentDescription = contentDescriptionFormatting
                .getLanguageContentDescription(languageText)

            day.isVisible = false
            val timeText = DateFormatter.newInstance(useDeviceTimeZone).getFormattedTime(session.dateUTC, session.timeZoneOffset)
            time.textOrHide = timeText
            time.contentDescription = contentDescriptionFormatting
                .getStartTimeContentDescription(timeText)

            room.textOrHide = session.roomName
            room.contentDescription = contentDescriptionFormatting
                .getRoomNameContentDescription(session.roomName)
            val durationText = duration.context.getString(R.string.session_list_item_duration_text, session.duration)
            duration.textOrHide = durationText
            duration.contentDescription = contentDescriptionFormatting
                .getDurationContentDescription(session.duration)

            withoutVideoRecording.isVisible = session.recordingOptOut
        }
    }

    private val Session.tookPlace
        get() = endsAt.isBefore(Moment.now())

    private fun TextView.setPastSessionTextColor() = setTextColor(pastSessionTextColor)

}
