@file:JvmName("XmlPullParsers")

package info.metadude.android.eventfahrplan.network.serialization

import info.metadude.android.eventfahrplan.commons.extensions.sanitize
import org.xmlpull.v1.XmlPullParser

fun XmlPullParser.getSanitizedText(): String =
    text.sanitize()

fun XmlPullParser.getSanitizedAttributeNullableValue(name: String): String? {
    val sanitized = getAttributeValue(null, name).sanitize()
    return sanitized.ifEmpty { null }
}
