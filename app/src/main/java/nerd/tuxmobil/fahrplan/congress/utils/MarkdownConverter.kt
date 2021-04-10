package nerd.tuxmobil.fahrplan.congress.utils

/**
 * Routines to convert from Markdown to HTML and vice versa.
 */
object MarkdownConverter : MarkdownConversion {

    // language=regex
    private const val MARKDOWN_LINK_REGEX = """\[(.*?)\]\(([^ \)]+).*?\)"""
    private const val HTML_LINK_TEMPLATE = """<a href="$2">$1</a>"""

    /**
     * Converts Markdown formatted links in the given [markdown] text
     * into HTML formatted links and returns the text as a string.
     */
    override fun markdownLinksToHtmlLinks(markdown: String): String {
        return markdown.replace(MARKDOWN_LINK_REGEX.toRegex(), HTML_LINK_TEMPLATE)
    }

}

interface MarkdownConversion {

    fun markdownLinksToHtmlLinks(markdown: String): String

}
