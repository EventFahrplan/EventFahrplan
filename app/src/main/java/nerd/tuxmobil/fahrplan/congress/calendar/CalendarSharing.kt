package nerd.tuxmobil.fahrplan.congress.calendar

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_MINUTE
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.startActivity
import nerd.tuxmobil.fahrplan.congress.extensions.withExtras
import nerd.tuxmobil.fahrplan.congress.models.Session

class CalendarSharing @JvmOverloads constructor(

    val context: Context,
    val session: Session,
    private val calendarDescriptionComposition: CalendarDescriptionComposition = CalendarDescriptionComposer(
        context.getString(R.string.session_details_section_title_session_online)
    ),
    val onFailure: () -> Unit = {
        Toast.makeText(context, R.string.add_to_calendar_failed, Toast.LENGTH_LONG).show()
    }

) {

    fun addToCalendar() {
        val intent = session.toCalendarInsertIntent()
        context.startActivity(intent) {
            // TODO Updating a calendar event is broken. Always creates new entries.
            // See https://developer.android.com/guide/topics/providers/calendar-provider#intent-edit
            intent.transformToCalendarEditIntent()
            context.startActivity(intent) { onFailure() }
        }
    }

    private fun Session.toCalendarInsertIntent(): Intent {
        val title = this.title
        val description = calendarDescriptionComposition.getCalendarDescription(this)
        val location = this.room
        val startTime = startTimeMilliseconds
        val endTime = startTime + this.duration * MILLISECONDS_OF_ONE_MINUTE
        return Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).withExtras(
            CalendarContract.Events.TITLE to title,
            CalendarContract.Events.DESCRIPTION to description,
            CalendarContract.Events.EVENT_LOCATION to location,
            CalendarContract.EXTRA_EVENT_BEGIN_TIME to startTime,
            CalendarContract.EXTRA_EVENT_END_TIME to endTime
        )
    }

    private fun Intent.transformToCalendarEditIntent() {
        action = Intent.ACTION_EDIT
    }

}
