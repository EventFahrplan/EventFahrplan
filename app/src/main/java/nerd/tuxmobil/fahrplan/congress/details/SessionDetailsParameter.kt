package nerd.tuxmobil.fahrplan.congress.details

import nerd.tuxmobil.fahrplan.congress.BuildConfig

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
        @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
        val showTitleBoxed: Boolean = BuildConfig.FLAVOR == "ccc39c3"
    ) : SessionDetailsParameter
}
