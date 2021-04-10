@file:JvmName("CalendarSharing")

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

fun Session.addToCalendar(context: Context) {
    val intent = this.toCalendarInsertIntent(context)
    context.startActivity(intent) {
        intent.transformToCalendarEditIntent()
        context.startActivity(intent) {
            Toast.makeText(context, R.string.add_to_calendar_failed, Toast.LENGTH_LONG).show()
        }
    }
}

private fun Session.toCalendarInsertIntent(context: Context): Intent {
    val title = this.title
    val description = CalendarDescriptionComposer(this, context.getString(R.string.session_details_section_title_session_online)).getCalendarDescription()
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
