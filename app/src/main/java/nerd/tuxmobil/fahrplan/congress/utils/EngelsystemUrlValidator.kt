package nerd.tuxmobil.fahrplan.congress.utils

import android.net.Uri
import androidx.annotation.StringRes
import nerd.tuxmobil.fahrplan.congress.R

class EngelsystemUrlValidator(

        private val url: String

) : Validation {

    @StringRes
    private var errorMessage: Int? = null

    @StringRes
    override fun getErrorMessage(): Int? = errorMessage

    override fun isValid(): Boolean {
        val urlValidator = UrlValidator(url)
        if (!urlValidator.isValid()) {
            errorMessage = urlValidator.getErrorMessage()
            return false
        }
        val uri = Uri.parse(url)
        val query = uri.query
        if (query.isNullOrEmpty()) {
            errorMessage = R.string.validation_error_url_without_query
            return false
        }
        if (query.isNotEmpty()) {
            if (query.endsWith("=")) {
                errorMessage = R.string.validation_error_url_without_api_key
                return false
            }
            if (!query.contains("=")) {
                errorMessage = R.string.validation_error_url_with_incomplete_query
                return false
            }
        }
        return true
    }

}
