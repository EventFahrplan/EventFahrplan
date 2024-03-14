package nerd.tuxmobil.fahrplan.congress.utils

/**
 * Routines to convert from Markdown to HTML and vice versa.
 */
object MarkdownConverter : MarkdownConversion {

    // language=regex
    private const val MARKDOWN_LINK_REGEX = """\[(.*?)\]\(([^ \)]+).*?\)"""
    private const val HTML_LINK_TEMPLATE = """<a href="$2">$1</a>"""
    private const val PLAIN_LINK_TEMPLATE = """$1 ($2)"""

    /**
     * Converts Markdown formatted links in the given [markdown] text
     * into HTML formatted links and returns the text as a string.
     */
    override fun markdownLinksToHtmlLinks(markdown: String): String {
        return markdown.replace(MARKDOWN_LINK_REGEX.toRegex(), HTML_LINK_TEMPLATE)
    }

    /**
     * Converts Markdown formatted links in the given [markdown] text
     * into plain text links and return the text as a string.
     */
    override fun markdownLinksToPlainTextLinks(markdown: String): String {
        return markdown.replace(MARKDOWN_LINK_REGEX.toRegex(), PLAIN_LINK_TEMPLATE)
    }

}

interface MarkdownConversion {

    fun markdownLinksToHtmlLinks(markdown: String): String
    fun markdownLinksToPlainTextLinks(markdown: String): String

}
