package nerd.tuxmobil.fahrplan.congress.details

data class SessionDetailsProperty<T>(
    val value: T,
    val contentDescription: String,
) {
    sealed interface MarkupLanguage {
        val text: String
        data class Html(override val text: String) : MarkupLanguage
        data class Markdown(override val text: String) : MarkupLanguage
    }
}
