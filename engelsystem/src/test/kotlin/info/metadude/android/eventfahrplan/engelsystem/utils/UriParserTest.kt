package info.metadude.android.eventfahrplan.engelsystem.utils

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.engelsystem.models.EngelsystemUri
import org.junit.Assert.fail
import org.junit.Test
import java.net.URISyntaxException

class UriParserTest {

    private val uriParser = UriParser()

    @Test
    fun parseUriWithEmptyString() {
        try {
            uriParser.parseUri("")
            fail()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("Empty URL")
        }
    }

    @Test
    fun parseUriWithoutScheme() {
        try {
            uriParser.parseUri("example.com")
            fail()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("Scheme is missing")
        }
    }

    @Test
    fun parseUriWithoutHost() {
        try {
            uriParser.parseUri("https://?key=a1b2c3")
            fail()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("Host is missing")
        }
    }

    @Test
    fun parseUriWithoutPath() {
        try {
            uriParser.parseUri("https://example.com?key=a1b2c3")
            fail()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("Path is missing")
        }
    }

    @Test
    fun parseUriWithoutQuery() {
        try {
            uriParser.parseUri("https://example.com/foo/file.json")
            fail()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("Query is missing")
        }
    }

    @Test
    fun parseUriWithoutApiKey() {
        try {
            uriParser.parseUri("https://example.com/foo/file.json?key")
            fail()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("API key is missing")
        }
    }

    @Test
    fun parseUriWithCompleteUrl() {
        val uri = EngelsystemUri("https://example.com", "foo/file.json", "a1b2c3")
        assertThat(uriParser.parseUri("https://example.com/foo/file.json?key=a1b2c3")).isEqualTo(uri)
    }

    @Test
    fun parseUriWithCompleteUrlWithPort() {
        val uri = EngelsystemUri("https://example.com:3000", "foo/file.json", "a1b2c3")
        assertThat(uriParser.parseUri("https://example.com:3000/foo/file.json?key=a1b2c3")).isEqualTo(uri)
    }

}
