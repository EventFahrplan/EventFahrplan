package nerd.tuxmobil.fahrplan.congress.schedule

import android.annotation.SuppressLint
import android.content.Context
import nerd.tuxmobil.fahrplan.congress.R
import org.xmlpull.v1.XmlPullParser

object TrackBackgrounds {

    private fun getHashMapResource(context: Context, hashMapResId: Int): Map<String?, String?>? {
        var map: MutableMap<String?, String?>? = null
        val parser = context.resources.getXml(hashMapResId)
        var key: String? = null
        var value: String? = null
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    // XmlPullParser can also be START_DOCUMENT
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "map") {
                            val isLinked = parser.getAttributeBooleanValue(null, "linked", false)
                            map = if (isLinked) linkedMapOf() else hashMapOf()
                        } else if (parser.name == "entry") {
                            key = parser.getAttributeValue(null, "key")
                            if (null == key) {
                                parser.close()

                                return null
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "entry") {
                            map!![key] = value
                            key = null
                            value = null
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (key != null) {
                            value = parser.text.trim()
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return map
    }

    @SuppressLint("DiscouragedApi")
    private fun buildTrackBackgroundHashMap(
        trackNamesMap: Map<String?, String?>,
        prefix: String,
        resourceType: String,
        context: Context
    ) = trackNamesMap.mapValues {
        var name = prefix
        // Handle empty track names
        // key can have the value: ""
        // See track_resource_names.xml
        if (!it.key.isNullOrEmpty()) {
            name += "_${it.value}"
        }
        context.resources.getIdentifier(name, resourceType, context.packageName)
    }

    fun getTrackNameBackgroundColorDefaultPairs(context: Context) = buildTrackBackgroundHashMap(
        getHashMapResource(context, R.xml.track_resource_names)!!,
        "track_background_default",
        "color",
        context
    )

    fun getTrackNameBackgroundColorHighlightPairs(context: Context) = buildTrackBackgroundHashMap(
        getHashMapResource(context, R.xml.track_resource_names)!!,
        "track_background_highlight",
        "color",
        context
    )

}
