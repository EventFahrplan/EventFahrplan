package nerd.tuxmobil.fahrplan.congress.calendar

import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConversion
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConverter
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposition
import nerd.tuxmobil.fahrplan.congress.wiki.containsWikiLink

/**
 * Composes the description text to be sent to the calendar app.
 * Here, multiple session information are concatenated if available such as
 * subtitle, speakers, abstract, description, links, session online block.
 */
class CalendarDescriptionComposer(

    private val session: Session,
    private val sessionOnlineText: String,
    private val markdownConversion: MarkdownConversion = MarkdownConverter,
    private val sessionUrlComposition: SessionUrlComposition = SessionUrlComposer(session)

) {

    /**
     * Returns the composed description text.
     */
    fun getCalendarDescription() = buildString {
        appendSubtitle()
        appendSpeakers()
        appendAbstract()
        appendDescription()
        if (session.getLinks().containsWikiLink()) {
            appendWikiLinks()
        } else {
            appendLinks()
            appendSessionOnline()
        }
    }

    private fun StringBuilder.appendSubtitle() {
        appendParagraphIfNotEmpty(session.subtitle.orEmpty())
    }

    private fun StringBuilder.appendSpeakers() {
        appendParagraphIfNotEmpty(session.formattedSpeakers)
    }

    private fun StringBuilder.appendAbstract() {
        appendMarkdownParagraphIfNotEmpty(session.abstractt.orEmpty())
    }

    private fun StringBuilder.appendDescription() {
        appendMarkdownParagraphIfNotEmpty(session.description.orEmpty())
    }

    private fun StringBuilder.appendWikiLinks() {
        val links = session.getLinks().separateByHtmlLineBreaks()
        append(markdownConversion.markdownLinksToHtmlLinks(links))
    }

    private fun StringBuilder.appendLinks() {
        val links = session.getLinks().separateByHtmlLineBreaks()
        appendMarkdownParagraphIfNotEmpty(markdownConversion.markdownLinksToHtmlLinks(links))
    }

    private fun StringBuilder.appendSessionOnline() {
        val sessionUrl = sessionUrlComposition.getSessionUrl()
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
