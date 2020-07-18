package nerd.tuxmobil.fahrplan.congress.changes

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.SessionsAdapter
import nerd.tuxmobil.fahrplan.congress.extensions.textOrHide
import nerd.tuxmobil.fahrplan.congress.models.Session

class ChangeListAdapter internal constructor(

        context: Context,
        list: List<Session>,
        numDays: Int

) : SessionsAdapter(

        context,
        R.layout.session_list_item,
        list,
        numDays

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

    override fun initViewSetup() {
        // Nothing to do here
    }

    override fun setItemContent(position: Int, viewHolder: ViewHolder) {
        resetItemStyles(viewHolder)

        val session = getSession(position)
        with(viewHolder) {
            title.textOrHide = session.title
            subtitle.textOrHide = session.subtitle
            speakers.textOrHide = session.formattedSpeakers
            lang.textOrHide = session.lang
            lang.contentDescription = session.getLanguageContentDescription(context)
            val dayText = DateFormatter.newInstance().getFormattedDate(session.dateUTC)
            day.textOrHide = dayText
            val timeText = DateFormatter.newInstance().getFormattedTime(session.dateUTC)
            time.textOrHide = timeText
            room.textOrHide = session.room
            val durationText = context.getString(R.string.session_duration, session.duration)
            duration.textOrHide = durationText
            video.visibility = View.GONE
            noVideo.visibility = View.GONE
            withoutVideoRecording.visibility = View.GONE

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
                        title.text = context.getText(R.string.dash)
                    }
                }
                if (session.changedSubtitle) {
                    subtitle.setTextStyleChanged()
                    if (session.subtitle.isEmpty()) {
                        subtitle.text = context.getText(R.string.dash)
                    }
                }
                if (session.changedSpeakers) {
                    speakers.setTextStyleChanged()
                    if (session.speakers.isEmpty()) {
                        speakers.text = context.getText(R.string.dash)
                    }
                }
                if (session.changedLanguage) {
                    lang.setTextStyleChanged()
                    if (session.lang.isEmpty()) {
                        lang.text = context.getText(R.string.dash)
                        lang.contentDescription = context.getText(R.string.session_list_item_language_removed_content_description)
                    }
                }
                if (session.changedDay) {
                    day.setTextStyleChanged()
                }
                if (session.changedTime) {
                    time.setTextStyleChanged()
                }
                if (session.changedRoom) {
                    room.setTextStyleChanged()
                }
                if (session.changedDuration) {
                    duration.setTextStyleChanged()
                }
                if (session.changedRecordingOptOut) {
                    if (session.recordingOptOut) {
                        noVideo.visibility = View.VISIBLE
                    } else {
                        video.visibility = View.VISIBLE
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
