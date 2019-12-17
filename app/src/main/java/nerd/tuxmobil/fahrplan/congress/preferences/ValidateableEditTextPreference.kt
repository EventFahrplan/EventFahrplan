package nerd.tuxmobil.fahrplan.congress.preferences

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.preference.EditTextPreference
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.utils.EngelsystemUrlValidator
import nerd.tuxmobil.fahrplan.congress.utils.UrlValidator

class ValidateableEditTextPreference : EditTextPreference {

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
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        applyAttributes(context, attrs)
    }

    private lateinit var validationType: ValidationType
    private lateinit var urlTypeFriendlyName: String

    private fun applyAttributes(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ValidateableEditTextPreference)
        try {
            val type = attributes.getInt(
                    R.styleable.ValidateableEditTextPreference_validationType,
                    ValidationType.Unknown.value
            )
            validationType = ValidationType.of(type)
            urlTypeFriendlyName = attributes.getString(
                    R.styleable.ValidateableEditTextPreference_urlTypeFriendlyName)
                    ?: ""
        } finally {
            attributes.recycle()
        }
    }

    override fun showDialog(state: Bundle?) {
        super.showDialog(state)
        if (dialog !is AlertDialog) {
            return
        }
        val button = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
        editText.error = null
        button.setOnClickListener(this::onConfirmed)
    }

    private fun onConfirmed(view: View) = with(editText) {
        val url = text.trim().toString()
        val validation = validationType.toValidationOf(url)
        // Allow users to wipe their URL
        if (url.isEmpty() || validation.isValid()) {
            error = null
            onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dialog.dismiss()
        } else {
            error = if (validation.getErrorMessage() == null) "General validation error"
            else view.resources.getString(validation.getErrorMessage()!!, urlTypeFriendlyName)
        }
    }

    /**
     * The [value] has to match one the enum values in attrs.xml -> validationType.
     */
    sealed class ValidationType(val value: Int) {

        object Unknown : ValidationType(0)
        object Url : ValidationType(1)
        object EngelsystemUrl : ValidationType(2)

        companion object {
            fun of(type: Int) = when (type) {
                Url.value -> Url
                EngelsystemUrl.value -> EngelsystemUrl
                // Fail at instantiation time not until when users input their data.
                else -> throw NotImplementedError("Unknown validation type: $type.")
            }
        }
    }

    private fun ValidationType.toValidationOf(url: String) = when (this) {
        ValidationType.Url -> UrlValidator(url)
        ValidationType.EngelsystemUrl -> EngelsystemUrlValidator(url)
        // Fails once the user submits their input. Late, but still good to know.
        else -> throw NotImplementedError("Unknown validation type: $this.")
    }

}
