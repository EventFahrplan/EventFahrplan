package nerd.tuxmobil.fahrplan.congress.changes

import android.content.Context
import android.graphics.Paint
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.SessionsAdapter
import nerd.tuxmobil.fahrplan.congress.extensions.textOrHide
import nerd.tuxmobil.fahrplan.congress.models.Session

class ChangeListAdapter internal constructor(

        context: Context,
        list: List<Session>,
        numDays: Int,
        useDeviceTimeZone: Boolean

) : SessionsAdapter(

        context,
        R.layout.session_list_item,
        list,
        numDays,
        useDeviceTimeZone

) {

    @ColorInt
    private val scheduleChangeTextColor = ContextCompat.getColor(context, R.color.schedule_change)

    @ColorInt
    private val scheduleChangeNewTextColor = ContextCompat.getColor(context, R.color.schedule_change_new)

    @ColorInt
    private val scheduleChangeCanceledTextColor = ContextCompat.getColor(context, R.color.schedule_change_canceled)

    override fun resetTextStyle(textView: TextView, style: Int) {
        super.resetTextStyle(textView, style)
        textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    override fun setItemContent(position: Int, viewHolder: ViewHolder) {
        resetItemStyles(viewHolder)

        val session = getSession(position)
        with(viewHolder) {
            title.textOrHide = session.title
            subtitle.textOrHide = session.subtitle
            subtitle.contentDescription = Session.getSubtitleContentDescription(subtitle.context, session.subtitle)

            speakers.textOrHide = session.formattedSpeakers
            speakers.contentDescription = Session.getSpeakersContentDescription(speakers.context, session.speakers.size, session.formattedSpeakers)
            lang.textOrHide = session.languageText
            lang.contentDescription = Session.getLanguageContentDescription(lang.context, session.languageText)

            val dayText = DateFormatter.newInstance(useDeviceTimeZone).getFormattedDate(session.dateUTC, session.timeZoneOffset)
            day.textOrHide = dayText
            val timeText = DateFormatter.newInstance(useDeviceTimeZone).getFormattedTime(session.dateUTC, session.timeZoneOffset)
            time.textOrHide = timeText
            time.contentDescription = Session.getStartTimeContentDescription(time.context, timeText)

            room.textOrHide = session.roomName
            room.contentDescription = Session.getRoomNameContentDescription(room.context, session.roomName)
            val durationText = duration.context.getString(R.string.session_list_item_duration_text, session.duration)
            duration.textOrHide = durationText
            duration.contentDescription = Session.getDurationContentDescription(duration.context, session.duration)

            video.isVisible = false
            noVideo.isVisible = false
            withoutVideoRecording.isVisible = false

            if (session.changedIsNew) {
                title.setTextStyleNew()
                subtitle.setTextStyleNew()
                speakers.setTextStyleNew()
                lang.setTextStyleNew()
                day.setTextStyleNew()
                time.setTextStyleNew()
                room.setTextStyleNew()
                duration.setTextStyleNew()
            } else if (session.changedIsCanceled) {
                title.setTextStyleCanceled()
                subtitle.setTextStyleCanceled()
                speakers.setTextStyleCanceled()
                lang.setTextStyleCanceled()
                day.setTextStyleCanceled()
                time.setTextStyleCanceled()
                room.setTextStyleCanceled()
                duration.setTextStyleCanceled()
            } else {
                if (session.changedTitle) {
                    title.setTextStyleChanged()
                    if (session.title.isEmpty()) {
                        title.text = title.context.getText(R.string.dash)
                    }
                }
                if (session.changedSubtitle) {
                    subtitle.setTextStyleChanged()
                    if (session.subtitle.isEmpty()) {
                        subtitle.text = subtitle.context.getText(R.string.dash)
                    }
                }
                if (session.changedSpeakers) {
                    speakers.setTextStyleChanged()
                    if (session.speakers.isEmpty()) {
                        speakers.text = speakers.context.getText(R.string.dash)
                    }
                }
                if (session.changedLanguage) {
                    lang.setTextStyleChanged()
                    if (session.language.isEmpty()) {
                        lang.text = lang.context.getText(R.string.dash)
                        lang.contentDescription = lang.context.getText(R.string.session_list_item_language_removed_content_description)
                    }
                }
                if (session.changedDayIndex) {
                    day.setTextStyleChanged()
                }
                if (session.changedStartTime) {
                    time.setTextStyleChanged()
                }
                if (session.changedRoomName) {
                    room.setTextStyleChanged()
                }
                if (session.changedDuration) {
                    duration.setTextStyleChanged()
                }
                if (session.changedRecordingOptOut) {
                    if (session.recordingOptOut) {
                        noVideo.isVisible = true
                    } else {
                        video.isVisible = true
                    }
                }
            }
        }
    }

    private fun TextView.setTextStyleNew() = setTextColor(scheduleChangeNewTextColor)

    private fun TextView.setTextStyleCanceled() {
        setTextColor(scheduleChangeCanceledTextColor)
        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    private fun TextView.setTextStyleChanged() = setTextColor(scheduleChangeTextColor)

}
