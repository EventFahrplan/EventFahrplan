package nerd.tuxmobil.fahrplan.congress.utils

object StringUtils {

    // language=regex
    private const val MARKDOWN_LINK_REGEX = """\[(.*?)\]\(([^ \)]+).*?\)"""
    private const val HTML_LINK_TEMPLATE = """<a href="$2">$1</a>"""

    fun getHtmlLinkFromMarkdown(markdown: String): String {
        return markdown.replace(MARKDOWN_LINK_REGEX.toRegex(), HTML_LINK_TEMPLATE)
    }

}
