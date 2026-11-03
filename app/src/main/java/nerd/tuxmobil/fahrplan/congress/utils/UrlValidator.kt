package nerd.tuxmobil.fahrplan.congress.utils

import android.util.Patterns.WEB_URL
import androidx.annotation.StringRes
import androidx.core.net.toUri
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Error
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Success
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class UrlValidator(
    private val resourceResolver: ResourceResolving,
    private val urlTypeName: String,
) : Validation {

    private companion object {
        const val SCHEME_HTTP = "http"
        const val SCHEME_HTTPS = "https"
        val SCHEME_AND_AUTHORITY = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://[^/?#]*")
    }

    private val invalidUrlError = validationError(R.string.validation_error_invalid_url)
    private val missingHostError = validationError(R.string.validation_error_url_without_host)
    private val httpsRequiredError = validationError(R.string.validation_error_url_without_https)

    override fun validate(input: String): ValidationResult {
        // Scheme and host are case-insensitive (RFC 3986, sections 3.1 and 3.2.2)
        // but older versions of WEB_URL are not.
        val url = SCHEME_AND_AUTHORITY.replace(input) { it.value.lowercase() }
        if (!WEB_URL.matcher(url).matches()) {
            return invalidUrlError
        }
        // WEB_URL treats the scheme as optional. Without a scheme, there is no host.
        val uri = url.toUri()
        if (uri.host.isNullOrEmpty()) {
            return missingHostError
        }
        val scheme = uri.scheme
        if (scheme == SCHEME_HTTP) {
            return httpsRequiredError
        }
        if (scheme != SCHEME_HTTPS || url.toHttpUrlOrNull() == null) {
            return invalidUrlError
        }
        return Success
    }

    private fun validationError(@StringRes errorResId: Int): Error {
        return Error(resourceResolver.getString(errorResId, urlTypeName))
    }
}
