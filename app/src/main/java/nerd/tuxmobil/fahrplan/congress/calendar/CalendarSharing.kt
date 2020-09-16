@file:JvmName("CalendarSharing")

package nerd.tuxmobil.fahrplan.congress.calendar

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.startActivity
import nerd.tuxmobil.fahrplan.congress.extensions.withExtras
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.StringUtils
import nerd.tuxmobil.fahrplan.congress.wiki.containsWikiLink

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
    val description = this.getCalendarDescription(context)
    val location = this.room
    val startTime = startTimeMilliseconds
    val endTime = startTime + this.duration * 60000
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

private fun Session.getCalendarDescription(context: Context): String = with(StringBuilder()) {
    append(this@getCalendarDescription.description)
    append("\n\n")
    var links = this@getCalendarDescription.getLinks()
    if (links.containsWikiLink()) {
        links = links.separateByHtmlLineBreaks()
        links = StringUtils.getHtmlLinkFromMarkdown(links)
        append(links)
    } else {
        val sessionUrl = SessionUrlComposer(this@getCalendarDescription).getSessionUrl()
        if (sessionUrl.isNotEmpty()) {
            val sessionOnlineText = context.getString(R.string.session_details_section_title_session_online)
            append(sessionOnlineText)
            append(": ")
            append(sessionUrl)
        }
    }
    return toString()
}

private fun String.separateByHtmlLineBreaks() =
// language=regex
        replace("\\),".toRegex(), ")<br>")
