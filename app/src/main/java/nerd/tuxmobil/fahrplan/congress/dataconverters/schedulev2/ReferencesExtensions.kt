package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev2

import info.metadude.android.eventfahrplan.commons.contracts.Delimiters.MARKDOWN_LINKS_DELIMITER
import info.metadude.android.eventfahrplan.commons.extensions.sanitize
import info.metadude.android.eventfahrplan.commons.schedule.markdownLinkSegment
import info.metadude.kotlin.library.schedule.v2.models.Reference

/**
 * Uses [markdownLinkSegment]; links with empty JSON URLs are skipped.
 */
internal fun List<Reference>.toMarkdownLinks(): String = this
    .map { link -> link to link.url.sanitize() }
    .filter { (_, url) -> url.isNotEmpty() }
    .joinToString(MARKDOWN_LINKS_DELIMITER) { (link, url) ->
        val urlName = link.title.sanitize()
        markdownLinkSegment(
            urlName = urlName.ifEmpty { url },
            href = url.takeIf { it.isNotBlank() },
        )
    }
