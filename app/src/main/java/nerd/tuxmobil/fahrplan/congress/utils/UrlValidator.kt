package nerd.tuxmobil.fahrplan.congress.utils

import android.util.Patterns.WEB_URL
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Error
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Success

class UrlValidator(
    resourceResolver: ResourceResolving,
    urlTypeName: String,
) : Validation {
    private val error = Error(
        errorMessage = resourceResolver.getString(
            R.string.validation_error_invalid_url,
            urlTypeName,
        )
    )

    override fun validate(input: String): ValidationResult {
        return if (WEB_URL.matcher(input).matches()) Success else error
    }
}
