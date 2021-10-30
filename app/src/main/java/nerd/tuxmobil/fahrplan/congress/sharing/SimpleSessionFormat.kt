package nerd.tuxmobil.fahrplan.congress.sharing

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposer
import nerd.tuxmobil.fahrplan.congress.wiki.containsWikiLink
import org.threeten.bp.ZoneId

object SimpleSessionFormat {

    private const val LINE_BREAK = "\n"
    private const val COMMA = ","
    private const val SPACE = " "
    private const val HORIZONTAL_DIVIDERS = "---"

    @JvmStatic
    fun format(session: Session, timeZoneId: ZoneId?): String {
        val builder = StringBuilder()
        builder.appendSession(session, timeZoneId)
        return builder.toString()
    }

    fun format(sessions: List<Session>, timeZoneId: ZoneId?): String? {
        if (sessions.isEmpty()) {
            return null
        }
        val sessionsSize = sessions.size
        if (sessionsSize == 1) {
            return format(sessions[0], timeZoneId)
        }
        val builder = StringBuilder()
        for (i in 0 until sessionsSize) {
            val session = sessions[i]
            builder.appendSession(session, timeZoneId)
            if (i < sessionsSize - 1) {
                builder.appendDivider()
            }
        }
        return builder.toString()
    }

    private fun StringBuilder.appendSession(session: Session, timeZoneId: ZoneId?) {
        val startTime = session.startTimeMilliseconds
        val useDeviceTimeZone = false // Always share in the original session time zone.
        val shareableStartTime = DateFormatter.newInstance(useDeviceTimeZone).getFormattedShareable(startTime, timeZoneId)
        append(session.title)
        append(LINE_BREAK)
        append(shareableStartTime)
        append(COMMA)
        append(SPACE)
        append(session.room)
        if (!session.getLinks().containsWikiLink()) {
            append(LINE_BREAK)
            append(LINE_BREAK)
            val sessionUrl = SessionUrlComposer().getSessionUrl(session)
            append(sessionUrl)
        }
    }

    private fun StringBuilder.appendDivider() {
        append(LINE_BREAK)
        append(LINE_BREAK)
        append(HORIZONTAL_DIVIDERS)
        append(LINE_BREAK)
        append(LINE_BREAK)
    }
}
