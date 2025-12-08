package nerd.tuxmobil.fahrplan.congress.utils

/**
 * Routines to convert from Markdown to HTML and vice versa.
 */
object MarkdownConverter : MarkdownConversion {

    // language=regex
    private val MARKDOWN_LINK_REGEX = """\[(.*?)\]\(([^ \)]+)[^\\)]*+\)""".toRegex()

    /**
     * Converts Markdown formatted links in the given [markdown] text
     * into HTML formatted links and returns the text as a string.
     */
    override fun markdownLinksToHtmlLinks(markdown: String): String {
        return MARKDOWN_LINK_REGEX.replace(markdown) { matchResult ->
            val title = matchResult.groupValues[1]
            val url = matchResult.groupValues[2]
            val displayText = title.ifEmpty { url }
            """<a href="$url">$displayText</a>"""
        }
    }

    /**
     * Converts Markdown formatted links in the given [markdown] text
     * into plain text links and return the text as a string.
     */
    override fun markdownLinksToPlainTextLinks(markdown: String): String {
        return MARKDOWN_LINK_REGEX.replace(markdown) { matchResult ->
            val title = matchResult.groupValues[1]
            val url = matchResult.groupValues[2]
            if (title.isEmpty()) url else "$title ($url)"
        }
    }

}

interface MarkdownConversion {

    fun markdownLinksToHtmlLinks(markdown: String): String
    fun markdownLinksToPlainTextLinks(markdown: String): String

}
