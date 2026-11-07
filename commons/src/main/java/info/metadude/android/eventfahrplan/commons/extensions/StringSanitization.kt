package info.metadude.android.eventfahrplan.commons.extensions

import androidx.annotation.VisibleForTesting

/**
 * Zero-width no-break space often appears in schedule.xml.
 */
@VisibleForTesting
const val ZERO_WIDTH_NO_BREAK_SPACE: Char = '\uFEFF'

/**
 * Matches an asterisk enclosed by letters or digits, e.g. the gender star in "Entwickler*innen".
 */
private val INTRA_WORD_ASTERISK = Regex("""(?<=[\p{L}\p{N}])\*(?=[\p{L}\p{N}])""")

/**
 * Matches a "•" bullet point at the beginning of a line, including its indentation
 * and the whitespace (space, tab or no-break space) which follows the bullet point.
 */
private val BULLET_POINT = Regex("""^([ \t]*)•[ \t\u00A0]+""", RegexOption.MULTILINE)

/**
 * Maximum indentation of a Markdown list item which still allows it to interrupt a paragraph.
 * Larger indentation would turn the item into a paragraph continuation.
 */
private const val MAX_LIST_ITEM_INDENTATION = 3

fun String?.sanitize() = this
    ?.replace(ZERO_WIDTH_NO_BREAK_SPACE, ' ')
    ?.trim()
    ?.replace("\r\n", "\n")
    .orEmpty()

/**
 * Prepares text which is going to be rendered as Markdown.
 * Must not be applied to plain text since it inserts Markdown syntax.
 *
 * - Escapes asterisks within a word (e.g. "Entwickler*innen", "Nutzer*in") to avoid
 *   unwanted italic formatting. Asterisks which are already escaped remain untouched.
 * - Converts "•" bullet points at the beginning of a line (e.g. "•\tItem", "    • Item")
 *   into Markdown list items. Indentation is limited to [MAX_LIST_ITEM_INDENTATION] spaces
 *   so that the list is still recognized after a paragraph while nesting below an ordered
 *   list item (e.g. "1. Item\n   •\tSub item") is retained.
 */
fun String.sanitizeMarkdown() = this
    .replace(INTRA_WORD_ASTERISK) { """\*""" }
    .replace(BULLET_POINT) { match ->
        val indentation = match.groupValues[1].length.coerceAtMost(MAX_LIST_ITEM_INDENTATION)
        " ".repeat(indentation) + "* "
    }
