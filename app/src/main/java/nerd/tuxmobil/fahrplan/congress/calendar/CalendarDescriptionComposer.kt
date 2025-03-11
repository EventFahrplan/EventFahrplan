package nerd.tuxmobil.fahrplan.congress.calendar

import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConversion
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConverter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatting
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposition
import nerd.tuxmobil.fahrplan.congress.wiki.containsWikiLink

/**
 * Composes the description text to be sent to the calendar app.
 * Here, multiple session information are concatenated if available such as
 * subtitle, speakers, abstract, description, links, session online block.
 */
class CalendarDescriptionComposer(

    private val sessionOnlineText: String,
    private val resourceResolving: ResourceResolving,
    private val sessionPropertiesFormatting: SessionPropertiesFormatting =
        SessionPropertiesFormatter(resourceResolving),
    private val markdownConversion: MarkdownConversion = MarkdownConverter,
    private val sessionUrlComposition: SessionUrlComposition = SessionUrlComposer()

) : CalendarDescriptionComposition {

    /**
     * Returns the composed description text.
     */
    override fun getCalendarDescription(session: Session) = buildString {
        appendSubtitle(session)
        appendSpeakers(session)
        appendAbstract(session)
        appendDescription(session)
        if (session.links.containsWikiLink()) {
            // TODO: It seems that wiki links are no longer added to the links XML attribute.
            // Therefore, this code path might be removed in the future.
            // To be verified with VOC wiki scripts.
            appendWikiLinks(session)
        } else {
            appendLinks(session)
            appendSessionOnline(session)
        }
    }

    private fun StringBuilder.appendSubtitle(session: Session) {
        appendParagraphIfNotEmpty(session.subtitle)
    }

    private fun StringBuilder.appendSpeakers(session: Session) {
        appendParagraphIfNotEmpty(sessionPropertiesFormatting.getFormattedSpeakers(session))
    }

    private fun StringBuilder.appendAbstract(session: Session) {
        appendMarkdownParagraphIfNotEmpty(session.abstractt)
    }

    private fun StringBuilder.appendDescription(session: Session) {
        appendMarkdownParagraphIfNotEmpty(session.description)
    }

    private fun StringBuilder.appendWikiLinks(session: Session) {
        val links = session.links.separateByHtmlLineBreaks()
        append(markdownConversion.markdownLinksToHtmlLinks(links))
    }

    private fun StringBuilder.appendLinks(session: Session) {
        val links = session.links.separateByHtmlLineBreaks()
        appendMarkdownParagraphIfNotEmpty(markdownConversion.markdownLinksToHtmlLinks(links))
    }

    private fun StringBuilder.appendSessionOnline(session: Session) {
        val sessionUrl = sessionUrlComposition.getSessionUrl(session)
        if (sessionUrl.isNotEmpty()) {
            append("$sessionOnlineText: $sessionUrl")
        }
    }

    private fun StringBuilder.appendMarkdownParagraphIfNotEmpty(markdown: String) {
        if (markdown.isNotEmpty()) {
            appendLine(markdownConversion.markdownLinksToPlainTextLinks(markdown))
            appendLine()
        }
    }

    private fun StringBuilder.appendParagraphIfNotEmpty(text: String) {
        if (text.isNotEmpty()) {
            appendLine(text)
            appendLine()
        }
    }

    /**
     * Detects a trailing comma at the end of a Markdown formatted link
     * and replaces it with a HTML line break. See ParserTask#parseFahrplan
     * which adds the Markdown initially.
     */
    private fun String.separateByHtmlLineBreaks(): String {
        // language=regex
        return replace("\\),".toRegex(), ")<br>")
    }

}

fun interface CalendarDescriptionComposition {

    fun getCalendarDescription(session: Session): String

}
