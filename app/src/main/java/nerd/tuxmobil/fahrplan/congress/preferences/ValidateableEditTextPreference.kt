package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import androidx.core.content.withStyledAttributes
import androidx.core.widget.doAfterTextChanged
import androidx.preference.EditTextPreference
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.preferences.ValidateableEditTextPreference.ValidationType
import nerd.tuxmobil.fahrplan.congress.utils.EngelsystemUrlValidator
import nerd.tuxmobil.fahrplan.congress.utils.UrlValidator

/**
 * A dialog based [EditTextPreference] that shows an [EditText] in the dialog.
 *
 * Input text is validated via the configured [ValidationType].
 */
class ValidateableEditTextPreference : StyleableEditTextPreference {

    private companion object {

        const val URL_TYPE_FRIENDLY_NAME_DEFAULT_VALUE = ""

    }

    private lateinit var validationType: ValidationType
    private lateinit var urlTypeFriendlyName: String

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
                    ValidationType.Unknown.value
            )
            validationType = ValidationType.of(type)
            urlTypeFriendlyName = getString(
                    R.styleable.ValidateableEditTextPreference_urlTypeFriendlyName)
                    ?: URL_TYPE_FRIENDLY_NAME_DEFAULT_VALUE
        }
    }

    override fun onBindEditText(editText: EditText) {
        super.onBindEditText(editText)
        editText.doAfterTextChanged { editable ->
            requireNotNull(editable)
            val url = editable.toString().trim()
            val validation = validationType.toValidation(url)
            // Allow users to wipe their URL
            editText.error = if (url.isEmpty() || validation.isValid()) {
                null
            } else {
                if (validation.getErrorMessage() == null) "General validation error"
                else editText.resources.getString(validation.getErrorMessage()!!, urlTypeFriendlyName)
            }
        }
    }

    /**
     * The [value] has to match one the enum values in attrs.xml -> validationType.
     */
    sealed class ValidationType(val value: Int) {

        data object Unknown : ValidationType(0)
        data object Url : ValidationType(1)
        data object EngelsystemUrl : ValidationType(2)

        companion object {
            fun of(type: Int) = when (type) {
                Url.value -> Url
                EngelsystemUrl.value -> EngelsystemUrl
                // Fail at instantiation time not until when users input their data.
                else -> throw NotImplementedError("Unknown validation type: $type.")
            }
        }
    }

    private fun ValidationType.toValidation(url: String) = when (this) {
        ValidationType.Url -> UrlValidator(url)
        ValidationType.EngelsystemUrl -> EngelsystemUrlValidator(url)
        // Fails once the user submits their input. Late, but still good to know.
        else -> throw NotImplementedError("Unknown validation type: $this.")
    }

}
