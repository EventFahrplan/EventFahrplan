package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import androidx.core.content.withStyledAttributes
import androidx.core.widget.doAfterTextChanged
import androidx.preference.EditTextPreference
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.extensions.enableContentSensitivitySensitive
import nerd.tuxmobil.fahrplan.congress.preferences.ValidateableEditTextPreference.ValidationType.EngelsystemUrl
import nerd.tuxmobil.fahrplan.congress.preferences.ValidateableEditTextPreference.ValidationType.Url
import nerd.tuxmobil.fahrplan.congress.utils.EngelsystemUrlValidator
import nerd.tuxmobil.fahrplan.congress.utils.UrlValidator
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Error
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Success
import nerd.tuxmobil.fahrplan.congress.utils.Validation

/**
 * A dialog based [EditTextPreference] that shows an [EditText] in the dialog.
 *
 * Input text is validated via the configured [ValidationType].
 */
class ValidateableEditTextPreference : StyleableEditTextPreference {

    private companion object {
        const val URL_TYPE_FRIENDLY_NAME_DEFAULT_VALUE = ""
        const val UNKNOWN_VALIDATION_TYPE = 0
    }

    private lateinit var validator: Validation

    @Suppress("unused")
    constructor(context: Context) : super(context)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        applyAttributes(context, attrs)
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        applyAttributes(context, attrs)
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        applyAttributes(context, attrs)
    }

    private fun applyAttributes(context: Context, attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.ValidateableEditTextPreference) {
            val type = getInt(
                R.styleable.ValidateableEditTextPreference_validationType,
                UNKNOWN_VALIDATION_TYPE
            )
            val validationType = ValidationType.of(type)
            val urlTypeFriendlyName = getString(
                R.styleable.ValidateableEditTextPreference_urlTypeFriendlyName
            ) ?: URL_TYPE_FRIENDLY_NAME_DEFAULT_VALUE

            validator = validationType.getValidator(urlTypeFriendlyName)
        }
    }

    override fun onBindEditText(editText: EditText) {
        super.onBindEditText(editText)
        editText.enableContentSensitivitySensitive()
        editText.doAfterTextChanged { editable ->
            requireNotNull(editable)
            val url = editable.toString().trim()
            // Allow users to wipe their URL
            editText.error = if (url.isEmpty()) {
                null
            } else {
                // TODO Disable the "OK" button if the URL is invalid.
                when (val validationResult = validator.validate(url)) {
                    Success -> null
                    is Error -> validationResult.errorMessage
                }
            }
        }
    }

    /**
     * The [value] has to match one the enum values in attrs.xml -> validationType.
     */
    private sealed class ValidationType(val value: Int) {

        data object Url : ValidationType(1)
        data object EngelsystemUrl : ValidationType(2)

        companion object {
            fun of(type: Int) = when (type) {
                Url.value -> Url
                EngelsystemUrl.value -> EngelsystemUrl
                // Fail at instantiation time not until when users input their data.
                else -> throw UnknownUrlTypeException(type)
            }
        }
    }

    private fun ValidationType.getValidator(urlTypeFriendlyName: String): Validation {
        val resourceResolver = ResourceResolver(context)

        return when (this) {
            Url -> UrlValidator(resourceResolver, urlTypeFriendlyName)
            EngelsystemUrl -> EngelsystemUrlValidator(resourceResolver, urlTypeFriendlyName)
        }
    }
}

private class UnknownUrlTypeException(type: Int) : IllegalArgumentException(
    "Unknown url type: $type."
)
