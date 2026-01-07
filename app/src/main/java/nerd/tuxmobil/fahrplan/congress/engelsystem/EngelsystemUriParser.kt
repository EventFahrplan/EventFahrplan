package nerd.tuxmobil.fahrplan.congress.engelsystem

import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Empty
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.API_KEY_INVALID
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.HOST_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.PATH_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.QUERY_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.SCHEME_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.URL_MALFORMED
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Parsed
import nerd.tuxmobil.fahrplan.congress.engelsystem.PartialResult.Failure
import nerd.tuxmobil.fahrplan.congress.engelsystem.PartialResult.Success
import java.net.URI

class EngelsystemUriParser {

    private companion object {
        const val NO_PORT_DEFINED = -1
    }

    /**
     * Returns the corresponding [EngelsystemUri] wrapped in an [EngelsystemUriParsingResult]
     * for the given [url].
     */
    fun parseUri(url: String): EngelsystemUriParsingResult {
        if (url.isEmpty()) {
            return Empty
        }
        val uri = try {
            URI.create(url)
        } catch (_: IllegalArgumentException) {
            return Error(URL_MALFORMED, url)
        }
        return when (val baseUrl = parseBaseUrl(uri)) {
            is Failure -> Error(baseUrl.type, url)
            is Success -> when (val pathPart = parsePathPart(uri.path)) {
                is Failure -> Error(pathPart.type, url)
                is Success -> when (val apiKey = parseApiKey(uri.query)) {
                    is Failure -> Error(apiKey.type, url)
                    is Success -> Parsed(EngelsystemUri(baseUrl.value, pathPart.value, apiKey.value))
                }
            }
        }
    }

    /**
     * The base URL, e.g. https://example.com or https://example.com:3000
     */
    private fun parseBaseUrl(uri: URI): PartialResult {
        val schemePart = if (uri.scheme != null) "${uri.scheme}://" else return Failure(SCHEME_MISSING)
        val hostPart = uri.host ?: return Failure(HOST_MISSING)
        val portPart = if (NO_PORT_DEFINED == uri.port) "" else ":${uri.port}"
        return Success("$schemePart$hostPart$portPart")
    }

    /**
     * The URL path, e.g. /some/folder/file.json
     */
    private fun parsePathPart(path: String?): PartialResult =
        if (path.isNullOrEmpty()) Failure(PATH_MISSING)
        else Success(path.trimStart('/'))

    /**
     * The API key provided as a query parameter value, as in: ?key=a1b2c3
     */
    private fun parseApiKey(query: String?): PartialResult {
        val queryPart = query ?: return Failure(QUERY_MISSING)
        val valuesByKeys = queryPart.split("&").associate { param ->
            val valueByKey = param.split("=", limit = 2)
            if (valueByKey.size == 2) {
                valueByKey[0] to valueByKey[1]
            } else {
                valueByKey[0] to ""
            }
        }
        val apiKey = valuesByKeys["key"]
        // Valid API keys should be alphanumeric (and possibly contain some special chars like - or _)
        // but definitely should not contain protocol separators, slashes, or query parameter syntax.
        return when (apiKey.isNullOrEmpty() || !isValidApiKey(apiKey)) {
            true -> Failure(API_KEY_INVALID)
            false -> Success(apiKey)
        }
    }

    /**
     * Validates that the API key contains only allowed characters.
     * Rejects keys that contain URL-like patterns which might indicate a malformed/doubled URL.
     */
    private fun isValidApiKey(apiKey: String) = "://" !in apiKey && "/" !in apiKey && "?" !in apiKey
}

private sealed interface PartialResult {
    data class Success(val value: String) : PartialResult
    data class Failure(val type: Type) : PartialResult
}
