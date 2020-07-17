package nerd.tuxmobil.fahrplan.congress.utils

import androidx.annotation.StringRes

interface Validation {

    fun isValid(): Boolean

    @StringRes
    fun getErrorMessage(): Int?

}
