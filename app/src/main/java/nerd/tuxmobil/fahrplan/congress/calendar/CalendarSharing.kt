@file:JvmName("CalendarSharing")

package nerd.tuxmobil.fahrplan.congress.calendar

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.startActivity
import nerd.tuxmobil.fahrplan.congress.utils.EventUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc
import nerd.tuxmobil.fahrplan.congress.utils.StringUtils
import nerd.tuxmobil.fahrplan.congress.wiki.containsWikiLink
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

fun Event.addToCalendar(context: Context) {
    val intent = this.toCalendarInsertIntent(context)
    context.startActivity(intent) {
        intent.transformToCalendarEditIntent()
        context.startActivity(intent) {
            Toast.makeText(context, R.string.add_to_calendar_failed, Toast.LENGTH_LONG).show()
        }
    }
}

private fun Event.toCalendarInsertIntent(context: Context): Intent {
    val title = this.title
    val description = this.getCalendarDescription(context)
    val location = this.room
    val startTime = FahrplanMisc.getLectureStartTime(this)
    val endTime = startTime + this.duration * 60000
    return Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.Events.DESCRIPTION, description)
        putExtra(CalendarContract.Events.EVENT_LOCATION, location)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
    }
}

private fun Intent.transformToCalendarEditIntent() {
    action = Intent.ACTION_EDIT
}

private fun Event.getCalendarDescription(context: Context): String = with(StringBuilder()) {
    append(this@getCalendarDescription.description)
    append("\n\n")
    var links = this@getCalendarDescription.getLinks()
    if (links.containsWikiLink()) {
        links = links.separateByHtmlLineBreaks()
        links = StringUtils.getHtmlLinkFromMarkdown(links)
        append(links)
    } else {
        val eventOnline = context.getString(R.string.event_online)
        append(eventOnline)
        append(": ")
        val eventUrl = EventUrlComposer(this@getCalendarDescription).getEventUrl()
        append(eventUrl)
    }
    return toString()
}

private fun String.separateByHtmlLineBreaks() = replace("\\),".toRegex(), ")<br>")
