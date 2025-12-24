package nerd.tuxmobil.fahrplan.congress.applinks

import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SlugFactoryTest {

    private val factory = SlugFactory()

    @Test
    fun `getSlug returns Hub slug when Hub uri has language segment`() {
        val slug = factory.getSlug("https://events.ccc.de/congress/2024/hub/de/event/correctiv-recherche-geheimplan-gegen-deutschland-1-jahr-danach/".toUri())
        assertThat(slug).isEqualTo(Slug.HubSlug("correctiv-recherche-geheimplan-gegen-deutschland-1-jahr-danach"))
    }

    @Test
    fun `getSlug returns Hub slug when Hub uri lacks language segment`() {
        val slug = factory.getSlug("https://events.ccc.de/congress/2024/hub/event/correctiv-recherche-geheimplan-gegen-deutschland-1-jahr-danach/".toUri())
        assertThat(slug).isEqualTo(Slug.HubSlug("correctiv-recherche-geheimplan-gegen-deutschland-1-jahr-danach"))
    }

    @Test
    fun `getSlug returns Hub slug when Hub uri contains detail segment and has language segment`() {
        val slug = factory.getSlug("https://events.ccc.de/congress/2025/hub/en/event/detail/opening-ceremony".toUri())
        assertThat(slug).isEqualTo(Slug.HubSlug("opening-ceremony"))
    }

    @Test
    fun `getSlug returns Hub slug when Hub uri contains detail segment but lacks language segment`() {
        val slug = factory.getSlug("https://events.ccc.de/congress/2025/hub/event/detail/opening-ceremony".toUri())
        assertThat(slug).isEqualTo(Slug.HubSlug("opening-ceremony"))
    }

    @Test
    fun `getSlug returns Hub slug when Hub uri contains fahrplan segment`() {
        val slug = factory.getSlug("https://fahrplan.events.ccc.de/congress/2025/fahrplan/event/opening-ceremony".toUri())
        assertThat(slug).isEqualTo(Slug.HubSlug("opening-ceremony"))
    }

    @Test
    fun `getSlug returns Pretalx slug`() {
        val slug = factory.getSlug("https://fahrplan.events.ccc.de/congress/2024/fahrplan/talk/HQCCYH/".toUri())
        assertThat(slug).isEqualTo(Slug.PretalxSlug("HQCCYH"))
    }

    @Test
    fun `getSlug returns null when uri is neither Hub nor Pretalx`() {
        val slug = factory.getSlug("https://ccc.de".toUri())
        assertThat(slug).isNull()
    }

    @Test
    fun `getSlug returns null when uri string is empty`() {
        val slug = factory.getSlug("".toUri())
        assertThat(slug).isNull()
    }

}
