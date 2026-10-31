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

fun String?.sanitize() = this
    ?.replace(ZERO_WIDTH_NO_BREAK_SPACE, ' ')
    ?.trim()
    ?.replace("\r\n", "\n")
    .orEmpty()

/**
 * Prepares text which is going to be rendered as Markdown.
 * Must not be applied to plain text since it inserts Markdown syntax.
 *
 * Escapes asterisks within a word (e.g. "Entwickler*innen", "Nutzer*in") to avoid
 * unwanted italic formatting. Asterisks which are already escaped remain untouched.
 */
fun String.sanitizeMarkdown() = this
    .replace(INTRA_WORD_ASTERISK) { """\*""" }
