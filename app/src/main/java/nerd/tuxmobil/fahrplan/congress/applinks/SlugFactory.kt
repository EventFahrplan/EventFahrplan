package nerd.tuxmobil.fahrplan.congress.applinks

import android.net.Uri
import info.metadude.android.eventfahrplan.commons.logging.Logging

class SlugFactory(private val logging: Logging) {

    private companion object {
        const val LOG_TAG = "SlugFactory"
    }

    fun getSlug(uri: Uri): Slug? {
        logging.d(LOG_TAG, "deepLink -> uri: $uri")
        if (isPretalxUri(uri)) {
            val slug = getSlug(uri, index = 4)
            if (!slug.isNullOrEmpty()) {
                return Slug.PretalxSlug(slug)
            }
        } else if (isHubUriWithLanguageSegment(uri)) {
            val slug = getSlug(uri, index = 5)
            if (!slug.isNullOrEmpty()) {
                return Slug.HubSlug(slug)
            }
        } else if (isHubUriWithoutLanguageSegment(uri)) {
            val slug = getSlug(uri, index = 4)
            if (!slug.isNullOrEmpty()) {
                return Slug.HubSlug(slug)
            }
        } else if (isHubUriWithFahrplanSegment(uri)) {
            val slug = getSlug(uri, index = 4)
            if (!slug.isNullOrEmpty()) {
                return Slug.HubSlug(slug)
            }
        }
        return null
    }

    private fun getSlug(uri: Uri, index: Int): String? {
        val slug = uri.pathSegments[index]?.trim()
        logging.d(LOG_TAG, "deepLink -> Hub slug: $slug")
        return slug
    }

    private fun isPretalxUri(uri: Uri): Boolean {
        val pathSegments = uri.pathSegments
        return pathSegments != null && pathSegments.size == 5 && pathSegments[2] == "fahrplan" && pathSegments[3] == "talk"
    }

    private fun isHubUriWithLanguageSegment(uri: Uri): Boolean {
        val pathSegments = uri.pathSegments
        return pathSegments != null && pathSegments.size == 6 && pathSegments[2] == "hub" && pathSegments[4] == "event"
    }

    private fun isHubUriWithoutLanguageSegment(uri: Uri): Boolean {
        val pathSegments = uri.pathSegments
        return pathSegments != null && pathSegments.size == 5 && pathSegments[2] == "hub" && pathSegments[3] == "event"
    }

    private fun isHubUriWithFahrplanSegment(uri: Uri): Boolean {
        val pathSegments = uri.pathSegments
        return pathSegments != null && pathSegments.size == 5 && pathSegments[2] == "fahrplan" && pathSegments[3] == "event"
    }

}
