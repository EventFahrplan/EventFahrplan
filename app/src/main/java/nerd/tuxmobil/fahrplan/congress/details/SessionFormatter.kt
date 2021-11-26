package nerd.tuxmobil.fahrplan.congress.details

/**
 * Session details related formatting logic.
 */
class SessionFormatter {

    /**
     * Returns the given [links] separated by HTML `br` entities.
     * The original string is returned if no link separator is detected.
     */
    fun getFormattedLinks(links: String): String {
        // language=regex
        return links.replace("\\),".toRegex(), ")<br>")
    }

    /**
     * Returns the given [url] formatted as an HTML weblink.
     * An empty string is returned if the [url] is empty itself.
     */
    fun getFormattedUrl(url: String): String {
        return if (url.isEmpty()) "" else "<a href=\"$url\">$url</a>"
    }

}
