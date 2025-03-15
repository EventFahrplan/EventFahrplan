package nerd.tuxmobil.fahrplan.congress.models

sealed interface MarkupLanguage {
    data object Html : MarkupLanguage
    data object Markdown : MarkupLanguage
}
