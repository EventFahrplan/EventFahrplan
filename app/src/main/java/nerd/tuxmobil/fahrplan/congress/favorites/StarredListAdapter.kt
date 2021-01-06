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

class StarredListAdapter internal constructor(

        context: Context,
        list: List<Session>, numDays: Int

) : SessionsAdapter(

        context,
        R.layout.session_list_item,
        list,
        numDays

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
            speakers.textOrHide = session.formattedSpeakers
            lang.textOrHide = session.lang
            lang.contentDescription = session.getLanguageContentDescription(context)
            day.isVisible = false
            val timeText = DateFormatter.newInstance().getFormattedTime(session.dateUTC)
            time.textOrHide = timeText
            room.textOrHide = session.room
            val durationText = context.getString(R.string.session_duration, session.duration)
            duration.textOrHide = durationText
            video.isVisible = false
            noVideo.isVisible = false
            withoutVideoRecording.isVisible = session.recordingOptOut
        }
    }

    private val Session.tookPlace
        get() = endsAtDateUtc < Moment.now().toMilliseconds()

    private fun TextView.setPastSessionTextColor() = setTextColor(pastSessionTextColor)

}
