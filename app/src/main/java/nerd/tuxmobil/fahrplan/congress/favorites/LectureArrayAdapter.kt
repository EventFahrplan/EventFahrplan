package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.text.format.Time
import android.view.View
import android.widget.TextView

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.LecturesAdapter
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper

class LectureArrayAdapter internal constructor(

        context: Context,
        list: List<Lecture>, numDays: Int

) : LecturesAdapter(

        context,
        R.layout.lecture_change_row,
        list,
        numDays

) {

    private val now: Time = Time()

    @ColorInt
    private val pastEventTextColor = ContextCompat.getColor(context, R.color.favorites_past_event_text)

    override fun initViewSetup() {
        now.setToNow()
    }

    override fun setItemContent(position: Int, viewHolder: ViewHolder) {
        resetItemStyles(viewHolder)

        val lecture = getLecture(position)
        with(viewHolder) {
            if (lecture.tookPlace) {
                title.setPastEventTextColor()
                subtitle.setPastEventTextColor()
                speakers.setPastEventTextColor()
                lang.setPastEventTextColor()
                day.setPastEventTextColor()
                time.setPastEventTextColor()
                room.setPastEventTextColor()
                duration.setPastEventTextColor()
            }

            title.text = lecture.title
            subtitle.text = lecture.subtitle
            speakers.text = lecture.formattedSpeakers
            lang.text = lecture.lang
            day.visibility = View.GONE
            val timeText = DateHelper.getFormattedTime(lecture.dateUTC)
            time.text = timeText
            room.text = lecture.room
            val durationText = context.getString(R.string.event_duration, lecture.duration)
            duration.text = durationText
            video.visibility = View.GONE
            noVideo.visibility = View.GONE
        }
    }

    private val Lecture.tookPlace
        get() = dateUTC + duration * 60000 < now.toMillis(true)

    private fun TextView.setPastEventTextColor() = setTextColor(pastEventTextColor)

}
