package nerd.tuxmobil.fahrplan.congress.commons

sealed interface TextResource {

    /**
     * No text at all.
     */
    data object Empty : TextResource

    /**
     * Examples:
     * - <a href="https://example.com">example.com</a>
     * - Some text without a link.
     */
    data class Html(
        val html: String,
    ) : TextResource {

        companion object {
            fun of(url: String, text: String? = null): Html {
                require(url.isNotEmpty())
                return if ("http" in url) {
                    val title = text ?: url
                    Html("""<a href="$url">$title</a>""")
                } else {
                    require(text == null)
                    Html(url)
                }
            }
        }

    }

    /**
     * Example: Congressplatz 1, 20355 Hamburg
     */
    data class PostalAddress(
        val text: String,
    ) : TextResource

}
