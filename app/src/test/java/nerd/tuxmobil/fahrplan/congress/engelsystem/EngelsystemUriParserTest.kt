package nerd.tuxmobil.fahrplan.congress.engelsystem

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.net.URISyntaxException

class EngelsystemUriParserTest {

    private val uriParser = EngelsystemUriParser()

    @Test
    fun `parseUri throws exception if url is empty`() {
        try {
            uriParser.parseUri("")
            failExpectingUriSyntaxException()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("Empty URL")
        }
    }

    @Test
    fun `parseUri throws exception if url lacks a scheme`() {
        try {
            uriParser.parseUri("example.com")
            failExpectingUriSyntaxException()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("Scheme is missing")
        }
    }

    @Test
    fun `parseUri throws exception if url lacks a host`() {
        try {
            uriParser.parseUri("https://?key=a1b2c3")
            failExpectingUriSyntaxException()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("Host is missing")
        }
    }

    @Test
    fun `parseUri throws exception if url lacks a path`() {
        try {
            uriParser.parseUri("https://example.com?key=a1b2c3")
            failExpectingUriSyntaxException()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("Path is missing")
        }
    }

    @Test
    fun `parseUri throws exception if url lacks a query`() {
        try {
            uriParser.parseUri("https://example.com/foo/file.json")
            failExpectingUriSyntaxException()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("Query is missing")
        }
    }

    @Test
    fun `parseUri throws exception if url lacks an API key`() {
        try {
            uriParser.parseUri("https://example.com/foo/file.json?key")
            failExpectingUriSyntaxException()
        } catch (e: URISyntaxException) {
            assertThat(e.message).contains("API key is missing")
        }
    }

    private fun failExpectingUriSyntaxException() {
        fail<Unit>("Expect a URISyntaxException to be thrown.")
    }

    @Test
    fun `parseUri returns valid EngelsystemUri if url is complete`() {
        val uri = EngelsystemUri("https://example.com", "foo/file.json", "a1b2c3")
        assertThat(uriParser.parseUri("https://example.com/foo/file.json?key=a1b2c3")).isEqualTo(uri)
    }

    @Test
    fun `parseUri returns valid EngelsystemUri if url is complete and port is present`() {
        val uri = EngelsystemUri("https://example.com:3000", "foo/file.json", "a1b2c3")
        assertThat(uriParser.parseUri("https://example.com:3000/foo/file.json?key=a1b2c3")).isEqualTo(uri)
    }

}
