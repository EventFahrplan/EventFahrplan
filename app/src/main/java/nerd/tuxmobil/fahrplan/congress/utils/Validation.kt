package nerd.tuxmobil.fahrplan.congress.utils

import android.support.annotation.StringRes

interface Validation {

    fun isValid(): Boolean

    @StringRes
    fun getErrorMessage(): Int?

}
