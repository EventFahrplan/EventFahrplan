@file:JvmName("XmlPullParsers")

package info.metadude.android.eventfahrplan.network.serialization

import org.xmlpull.v1.XmlPullParser

fun XmlPullParser.getSanitizedText(): String = text?.trim() ?: ""
