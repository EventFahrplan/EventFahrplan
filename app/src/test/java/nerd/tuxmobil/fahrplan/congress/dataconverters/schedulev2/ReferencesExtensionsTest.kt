package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev2

import com.google.common.truth.Truth.assertThat
import info.metadude.kotlin.library.schedule.v2.models.Reference
import info.metadude.kotlin.library.schedule.v2.models.ReferenceService
import info.metadude.kotlin.library.schedule.v2.models.ReferenceType
import org.junit.jupiter.api.Test

class ReferencesExtensionsTest {

    @Test
    fun `toMarkdownLinks returns empty string when references are missing`() {
        assertThat(emptyList<Reference>().toMarkdownLinks()).isEmpty()
    }

    @Test
    fun `toMarkdownLinks returns empty string when title and url are empty`() {
        val reference = createReference(title = "", url = "")
        assertThat(listOf(reference).toMarkdownLinks()).isEmpty()
    }

    @Test
    fun `toMarkdownLinks returns markdown link`() {
        val reference = createReference(title = "Title", url = "https://example.com")
        val expected = "[Title](https://example.com)"
        assertThat(listOf(reference).toMarkdownLinks()).isEqualTo(expected)
    }

    @Test
    fun `toMarkdownLinks returns empty string when url is empty`() {
        val reference = createReference(title = "Title", url = "")
        assertThat(listOf(reference).toMarkdownLinks()).isEmpty()
    }

    @Test
    fun `toMarkdownLinks returns empty string when title is empty`() {
        val reference = createReference(title = "", url = "https://example.com")
        assertThat(listOf(reference).toMarkdownLinks()).isEqualTo("[https://example.com](https://example.com)")
    }

    @Test
    fun `toMarkdownLinks returns markdown links`() {
        val reference1 = createReference(title = "Title 1", url = "https://example1.com")
        val reference2 = createReference(title = "Title 2", url = "https://example2.com")
        val expected = "[Title 1](https://example1.com),[Title 2](https://example2.com)"
        assertThat(listOf(reference1, reference2).toMarkdownLinks()).isEqualTo(expected)
    }

    private fun createReference(title: String, url: String) = Reference(
        service = ReferenceService.UNKNOWN,
        title = title,
        type = ReferenceType.UNKNOWN,
        url = url,
    )

}
