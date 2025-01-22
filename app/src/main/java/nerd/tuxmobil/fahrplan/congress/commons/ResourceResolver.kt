package nerd.tuxmobil.fahrplan.congress.commons

import android.content.Context

class ResourceResolver(private val context: Context) : ResourceResolving {

    override fun getString(id: Int, vararg formatArgs: Any) =
        context.getString(id, *formatArgs)

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any) =
        context.resources.getQuantityString(id, quantity, *formatArgs)

}
