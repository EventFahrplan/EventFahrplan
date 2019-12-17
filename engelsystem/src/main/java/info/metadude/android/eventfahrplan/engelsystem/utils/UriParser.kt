package info.metadude.android.eventfahrplan.engelsystem.utils

import info.metadude.android.eventfahrplan.engelsystem.models.EngelsystemUri
import java.net.URI
import java.net.URISyntaxException

class UriParser {

    private companion object {
        const val NO_PORT_DEFINED = -1
    }

    /**
     * Returns the corresponding [EngelsystemUri] for the given [url].
     * @throws URISyntaxException
     */
    fun parseUri(url: String): EngelsystemUri {
        if (url.isEmpty()) {
            throw ParsingException("Empty URL")
        }
        val uri = URI.create(url)
        return EngelsystemUri(
                parseBaseUrl(uri),
                parsePathPart(uri.path),
                parseApiKey(uri.query))
    }

    /**
     * The base URL, e.g. https://example.com or https://example.com:3000
     */
    private fun parseBaseUrl(uri: URI): String {
        val schemePart = if (uri.scheme != null) "${uri.scheme}://" else throw ParsingException("Scheme is missing: $uri")
        val hostPart = uri.host ?: throw ParsingException("Host is missing: $uri")
        val portPart = if (NO_PORT_DEFINED == uri.port) "" else ":${uri.port}"
        return "$schemePart$hostPart$portPart"
    }

    /**
     * The URL path, e.g. /some/folder/file.json
     */
    private fun parsePathPart(path: String?) =
            if (path.isNullOrEmpty()) throw ParsingException("Path is missing")
            else path.trimStart('/')

    /**
     * The API key provides as a query parameter value, as in: ?key=a1b2c3
     */
    private fun parseApiKey(query: String?) = try {
        val queryPart = query ?: throw ParsingException("Query is missing")
        val keyValuePairs = queryPart.split("=")
        if (keyValuePairs.size % 2 != 0) {
            throw ParsingException("API key is missing: $query")
        }
        keyValuePairs.last()
    } catch (e: NoSuchElementException) {
        throw ParsingException("API key is missing: $query")
    } catch (e: IllegalArgumentException) {
        throw ParsingException("API key is missing: $query")
    }

    internal class ParsingException(messageSuffix: String)
        : URISyntaxException(messageSuffix, "Parsing failure")

}
