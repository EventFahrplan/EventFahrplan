package info.metadude.android.eventfahrplan.commons.extensions

import androidx.annotation.VisibleForTesting

/**
 * Zero-width no-break space often appears in schedule.xml.
 */
@VisibleForTesting
const val ZERO_WIDTH_NO_BREAK_SPACE: Char = '\uFEFF'

fun String?.sanitize() = this
    ?.replace(ZERO_WIDTH_NO_BREAK_SPACE, ' ')
    ?.trim()
    ?.replace("\r\n", "\n")
    .orEmpty()
