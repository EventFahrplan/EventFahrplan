package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage.Html
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage.Markdown

sealed class ServerBackendType(
    val name: String,
    val markupLanguage: MarkupLanguage,
) {

    data object PENTABARF : ServerBackendType("pentabarf", Html)
    data object FRAB : ServerBackendType("frab", Markdown)
    data object PRETALX : ServerBackendType("pretalx", Markdown)

    companion object {

        fun getMarkupLanguage(serverBackendTypeName: String) = when (serverBackendTypeName) {
            PENTABARF.name -> PENTABARF.markupLanguage
            FRAB.name -> FRAB.markupLanguage
            PRETALX.name -> PRETALX.markupLanguage
            else -> error("""Unknown server backend type: "$serverBackendTypeName".""")
        }

    }

}
