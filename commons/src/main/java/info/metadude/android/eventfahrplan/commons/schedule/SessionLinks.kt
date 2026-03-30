@file:JvmName("SessionLinks")

package info.metadude.android.eventfahrplan.commons.schedule

import info.metadude.android.eventfahrplan.commons.contracts.Delimiters.MARKDOWN_LINKS_DELIMITER

/**
 * Appends a Markdown link segment to [existingLinks], using [MARKDOWN_LINKS_DELIMITER] when
 * [existingLinks] is not empty.
 */
fun appendMarkdownLink(existingLinks: String, urlName: String, href: String?): String {
    val segment = markdownLinkSegment(urlName, href)
    return when (existingLinks.isEmpty()) {
        true -> segment
        false -> existingLinks + MARKDOWN_LINKS_DELIMITER + segment
    }
}

/**
 * Builds one `[urlName](url)` segment like XML schedule `<link>` parsing: if [href] is `null`
 * (attribute absent), [urlName] is used as the URL; if the URL has no `"://"`, `http://` is added.
 */
fun markdownLinkSegment(urlName: String, href: String?): String {
    var url = href ?: urlName
    if ("://" !in url) {
        url = "http://$url"
    }
    return "[$urlName]($url)"
}
