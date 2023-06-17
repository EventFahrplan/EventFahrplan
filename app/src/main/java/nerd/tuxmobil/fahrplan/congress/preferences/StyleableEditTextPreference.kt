package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import androidx.annotation.CallSuper
import androidx.core.content.withStyledAttributes
import androidx.preference.EditTextPreference
import nerd.tuxmobil.fahrplan.congress.R

/**
 * A dialog based [EditTextPreference] that shows an [EditText] in the dialog.
 *
 * To compensate missing functionality in [EditTextPreference] a few additional
 * XML attributes can be configured. See corresponding `attrs.xml`.
 * See [https://issuetracker.google.com/issues/37060038]
 */
open class StyleableEditTextPreference : EditTextPreference {

    private companion object {

        const val HINT_DEFAULT_VALUE = ""
        const val MAX_LINES_DEFAULT_VALUE = 1
        const val SINGLE_LINE_DEFAULT_VALUE = true

    }

    private lateinit var hint: String
    private var maxLines: Int = MAX_LINES_DEFAULT_VALUE
    private var singleLine: Boolean = SINGLE_LINE_DEFAULT_VALUE

    @Suppress("unused")
    constructor(context: Context) : super(context)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        applyAttributes(context, attrs)
        applyCustomBehavior()
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        applyAttributes(context, attrs)
        applyCustomBehavior()
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        applyAttributes(context, attrs)
        applyCustomBehavior()
    }

    private fun applyAttributes(context: Context, attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.StyleableEditTextPreference) {
            hint = getString(R.styleable.StyleableEditTextPreference_android_hint)
                    ?: HINT_DEFAULT_VALUE
            maxLines = getInt(R.styleable.StyleableEditTextPreference_android_maxLines, MAX_LINES_DEFAULT_VALUE)
            singleLine = getBoolean(R.styleable.StyleableEditTextPreference_android_singleLine, SINGLE_LINE_DEFAULT_VALUE)
        }
    }

    private fun applyCustomBehavior() = setOnBindEditTextListener(::onBindEditText)

    @CallSuper
    protected open fun onBindEditText(editText: EditText) {
        editText.hint = hint
        editText.isSingleLine = singleLine
        editText.maxLines = maxLines
    }

}
