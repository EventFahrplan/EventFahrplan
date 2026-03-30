package nerd.tuxmobil.fahrplan.congress.utils

import androidx.annotation.VisibleForTesting
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage.Html
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage.Markdown

enum class ServerBackendType(
    val value: String,
    val markupLanguage: MarkupLanguage,
) {

    PENTABARF("pentabarf", Html),
    FRAB("frab", Markdown),
    PRETALX("pretalx", Markdown);

    companion object {
        fun of(name: String) = entries.firstOrNull { it.value == name }
            ?: throw UnknownServerBackendTypeException(name)
    }

}

@VisibleForTesting
internal class UnknownServerBackendTypeException(name: String) : IllegalArgumentException("""Unknown server backend type: "$name".""")
