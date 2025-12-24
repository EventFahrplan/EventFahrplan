package nerd.tuxmobil.fahrplan.congress.applinks

import android.net.Uri

class SlugFactory {

    fun getSlug(uri: Uri): Slug? {
        when {
            isPretalxUri(uri.pathSegments) -> {
                val slug = getSlug(uri, index = 4)
                if (!slug.isNullOrEmpty()) {
                    return Slug.PretalxSlug(slug)
                }
            }

            isHubUriWithoutLanguageSegment(uri.pathSegments) || isHubUriWithFahrplanSegment(uri.pathSegments) -> {
                val slug = getSlug(uri, index = 4)
                if (!slug.isNullOrEmpty()) {
                    return Slug.HubSlug(slug)
                }
            }

            isHubUriWithLanguageSegment(uri.pathSegments) || isHubUriWithoutLanguageSegmentWithDetailsSegment(uri.pathSegments) -> {
                val slug = getSlug(uri, index = 5)
                if (!slug.isNullOrEmpty()) {
                    return Slug.HubSlug(slug)
                }
            }

            isHubUriWithLanguageSegmentWithDetailsSegment(uri.pathSegments) -> {
                val slug = getSlug(uri, index = 6)
                if (!slug.isNullOrEmpty()) {
                    return Slug.HubSlug(slug)
                }
            }
        }
        return null
    }

    private fun getSlug(uri: Uri, index: Int): String? {
        return uri.pathSegments[index]?.trim()
    }

    private fun isPretalxUri(pathSegments: List<String>?): Boolean {
        return pathSegments != null && pathSegments.size == 5 && pathSegments[2] == "fahrplan" && pathSegments[3] == "talk"
    }

    private fun isHubUriWithLanguageSegment(pathSegments: List<String>?): Boolean {
        return pathSegments != null && pathSegments.size == 6 && pathSegments[2] == "hub" && pathSegments[4] == "event"
    }

    private fun isHubUriWithoutLanguageSegment(pathSegments: List<String>?): Boolean {
        return pathSegments != null && pathSegments.size == 5 && pathSegments[2] == "hub" && pathSegments[3] == "event"
    }

    private fun isHubUriWithLanguageSegmentWithDetailsSegment(pathSegments: List<String>?): Boolean {
        return pathSegments != null && pathSegments.size == 7 && pathSegments[2] == "hub" && pathSegments[4] == "event" && pathSegments[5] == "detail"
    }

    private fun isHubUriWithoutLanguageSegmentWithDetailsSegment(pathSegments: List<String>?): Boolean {
        return pathSegments != null && pathSegments.size == 6 && pathSegments[2] == "hub" && pathSegments[3] == "event" && pathSegments[4] == "detail"
    }

    private fun isHubUriWithFahrplanSegment(pathSegments: List<String>?): Boolean {
        return pathSegments != null && pathSegments.size == 5 && pathSegments[2] == "fahrplan" && pathSegments[3] == "event"
    }

}
