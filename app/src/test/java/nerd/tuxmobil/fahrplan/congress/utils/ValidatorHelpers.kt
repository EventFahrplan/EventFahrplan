package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult

fun ValidationResult.isValid() = this is ValidationResult.Success

fun createFakeResourceResolver(): ResourceResolving = object : ResourceResolving {
    override fun getString(id: Int, vararg formatArgs: Any): String {
        return "irrelevant"
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String {
        return "irrelevant"
    }
}
