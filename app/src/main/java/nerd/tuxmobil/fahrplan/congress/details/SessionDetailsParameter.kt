package nerd.tuxmobil.fahrplan.congress.details

sealed interface SessionDetailsParameter {
    data class SessionDetails(
        val id: SessionDetailsProperty<String>,
        val title: SessionDetailsProperty<String>,
        val subtitle: SessionDetailsProperty<String>,
        val speakerNames: SessionDetailsProperty<String>,
        val abstract: SessionDetailsProperty<SessionDetailsProperty.MarkupLanguage>,
        val description: SessionDetailsProperty<SessionDetailsProperty.MarkupLanguage>,
        val trackName: SessionDetailsProperty<String>,
        val links: SessionDetailsProperty<String>,
        val startsAt: SessionDetailsProperty<String>,
        val roomName: SessionDetailsProperty<String>,
        val sessionLink: String,
    ) : SessionDetailsParameter
}
