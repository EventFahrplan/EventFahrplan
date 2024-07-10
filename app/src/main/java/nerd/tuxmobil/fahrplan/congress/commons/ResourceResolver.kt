package nerd.tuxmobil.fahrplan.congress.commons

import android.content.Context

class ResourceResolver(private val context: Context) : ResourceResolving {

    override fun getString(id: Int, vararg formatArgs: Any) = context.getString(id, *formatArgs)

}
