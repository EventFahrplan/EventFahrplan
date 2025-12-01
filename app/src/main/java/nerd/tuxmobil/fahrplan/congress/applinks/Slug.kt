package nerd.tuxmobil.fahrplan.congress.applinks

sealed interface Slug {

    val value: String

    data class PretalxSlug(override val value: String) : Slug
    data class HubSlug(override val value: String) : Slug

}
