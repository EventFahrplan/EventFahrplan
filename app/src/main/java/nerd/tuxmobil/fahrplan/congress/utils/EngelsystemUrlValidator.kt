package nerd.tuxmobil.fahrplan.congress.utils

import androidx.annotation.StringRes
import androidx.core.net.toUri
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Error
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Success

class EngelsystemUrlValidator(
    private val resourceResolver: ResourceResolving,
    private val urlTypeName: String,
) : Validation {
    private val urlValidator = UrlValidator(resourceResolver, urlTypeName)

    private val noQueryError = validationError(R.string.validation_error_url_without_query)
    private val noApiKeyError = validationError(R.string.validation_error_url_without_api_key)
    private val incompleteQueryPartError = validationError(R.string.validation_error_url_with_incomplete_query)

    override fun validate(input: String): ValidationResult {
        val urlValidationResult = urlValidator.validate(input)
        if (urlValidationResult !== Success) {
            return urlValidationResult
        }

        val uri = input.toUri()
        val query = uri.query
        if (query.isNullOrEmpty()) {
            return noQueryError
        }

        if (query.isNotEmpty()) {
            if (query.endsWith("=")) {
                return noApiKeyError
            }
            if (!query.contains("=")) {
                return incompleteQueryPartError
            }
        }

        return Success
    }

    private fun validationError(@StringRes errorResId: Int): Error {
        return Error(errorMessage = resourceResolver.getString(errorResId, urlTypeName))
    }
}
