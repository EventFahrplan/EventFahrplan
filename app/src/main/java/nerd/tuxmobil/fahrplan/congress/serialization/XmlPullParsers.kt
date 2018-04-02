@file:JvmName("XmlPullParsers")

package nerd.tuxmobil.fahrplan.congress.serialization

import android.support.annotation.NonNull
import org.xmlpull.v1.XmlPullParser

@NonNull
fun XmlPullParser.getSanitizedText(): String = text?.trim() ?: ""
