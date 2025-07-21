@file:JvmName("XmlPullParsers")

package info.metadude.android.eventfahrplan.network.serialization

import androidx.annotation.VisibleForTesting
import org.xmlpull.v1.XmlPullParser

@VisibleForTesting
const val ZERO_WIDTH_NO_BREAK_SPACE = '\uFEFF'

fun XmlPullParser.getSanitizedText(): String = text
    ?.replace(ZERO_WIDTH_NO_BREAK_SPACE, ' ')
    ?.trim()
    ?.replace("\r\n", "\n")
    ?: ""
