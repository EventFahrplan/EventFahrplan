package nerd.tuxmobil.fahrplan.congress.changes

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.LecturesAdapter
import nerd.tuxmobil.fahrplan.congress.extensions.textOrHide
import nerd.tuxmobil.fahrplan.congress.models.Lecture

class LectureChangesArrayAdapter internal constructor(

        context: Context,
        list: List<Lecture>,
        numDays: Int

) : LecturesAdapter(

        context,
        R.layout.lecture_list_item,
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

        val lecture = getLecture(position)
        with(viewHolder) {
            title.textOrHide = lecture.title
            subtitle.textOrHide = lecture.subtitle
            speakers.textOrHide = lecture.formattedSpeakers
            lang.textOrHide = lecture.lang
            lang.contentDescription = lecture.getLanguageContentDescription(context)
            val dayText = DateFormatter.newInstance().getFormattedDate(lecture.dateUTC)
            day.textOrHide = dayText
            val timeText = DateFormatter.newInstance().getFormattedTime(lecture.dateUTC)
            time.textOrHide = timeText
            room.textOrHide = lecture.room
            val durationText = context.getString(R.string.event_duration, lecture.duration)
            duration.textOrHide = durationText
            video.visibility = View.GONE
            noVideo.visibility = View.GONE
            withoutVideoRecording.visibility = View.GONE

            if (lecture.changedIsNew) {
                title.setTextStyleNew()
                subtitle.setTextStyleNew()
                speakers.setTextStyleNew()
                lang.setTextStyleNew()
                day.setTextStyleNew()
                time.setTextStyleNew()
                room.setTextStyleNew()
                duration.setTextStyleNew()
            } else if (lecture.changedIsCanceled) {
                title.setTextStyleCanceled()
                subtitle.setTextStyleCanceled()
                speakers.setTextStyleCanceled()
                lang.setTextStyleCanceled()
                day.setTextStyleCanceled()
                time.setTextStyleCanceled()
                room.setTextStyleCanceled()
                duration.setTextStyleCanceled()
            } else {
                if (lecture.changedTitle) {
                    title.setTextStyleChanged()
                    if (lecture.title.isEmpty()) {
                        title.text = context.getText(R.string.dash)
                    }
                }
                if (lecture.changedSubtitle) {
                    subtitle.setTextStyleChanged()
                    if (lecture.subtitle.isEmpty()) {
                        subtitle.text = context.getText(R.string.dash)
                    }
                }
                if (lecture.changedSpeakers) {
                    speakers.setTextStyleChanged()
                    if (lecture.speakers.isEmpty()) {
                        speakers.text = context.getText(R.string.dash)
                    }
                }
                if (lecture.changedLanguage) {
                    lang.setTextStyleChanged()
                    if (lecture.lang.isEmpty()) {
                        lang.text = context.getText(R.string.dash)
                        lang.contentDescription = context.getText(R.string.lecture_list_item_language_removed_content_description)
                    }
                }
                if (lecture.changedDay) {
                    day.setTextStyleChanged()
                }
                if (lecture.changedTime) {
                    time.setTextStyleChanged()
                }
                if (lecture.changedRoom) {
                    room.setTextStyleChanged()
                }
                if (lecture.changedDuration) {
                    duration.setTextStyleChanged()
                }
                if (lecture.changedRecordingOptOut) {
                    if (lecture.recordingOptOut) {
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
