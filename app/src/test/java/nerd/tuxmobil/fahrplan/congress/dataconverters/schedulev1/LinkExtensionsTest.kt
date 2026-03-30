package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev1

import com.google.common.truth.Truth.assertThat
import info.metadude.kotlin.library.schedule.v1.models.Link
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource

class LinkExtensionsTest {

    companion object {

        private fun scenarioOf(links: List<Link>, expectedMarkdown: String) =
            of(links, expectedMarkdown)

        @JvmStatic
        fun data() = listOf(
            scenarioOf(
                links = emptyList(),
                expectedMarkdown = "",
            ),
            scenarioOf(
                links = listOf(Link(url = "", title = "")),
                expectedMarkdown = "",
            ),
            scenarioOf(
                links = listOf(Link(url = "https://example.com", title = "")),
                expectedMarkdown = "[https://example.com](https://example.com)",
            ),
            scenarioOf(
                links = listOf(Link(url = "", title = "Hello World")),
                expectedMarkdown = "",
            ),
            scenarioOf(
                links = listOf(Link(url = " ", title = "Hello World")),
                expectedMarkdown = "",
            ),
            scenarioOf(
                links = listOf(Link(url = "https://example.com", title = "Hello World")),
                expectedMarkdown = "[Hello World](https://example.com)",
            ),
            scenarioOf(
                links = listOf(Link(url = "https://example.com", title = "https://example.com")),
                expectedMarkdown = "[https://example.com](https://example.com)",
            ),
            scenarioOf(
                links = listOf(
                    Link(url = "https://example.com", title = "Hello World"),
                    Link(url = "https://example.es", title = "Hola Mundo"),
                ),
                expectedMarkdown = "[Hello World](https://example.com),[Hola Mundo](https://example.es)",
            ),
        )
    }

    @ParameterizedTest(name = "{index}: links = {0} -> markdown = {1}")
    @MethodSource("data")
    fun toMarkdownLinks(links: List<Link>, expectedMarkdown: String) {
        assertThat(links.toMarkdownLinks()).isEqualTo(expectedMarkdown)
    }
}
