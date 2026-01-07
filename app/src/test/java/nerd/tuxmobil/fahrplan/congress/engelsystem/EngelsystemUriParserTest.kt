package nerd.tuxmobil.fahrplan.congress.engelsystem

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Empty
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.API_KEY_INVALID
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.HOST_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.PATH_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.QUERY_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.SCHEME_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.URL_MALFORMED
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Parsed
import org.junit.jupiter.api.Test

class EngelsystemUriParserTest {

    private val uriParser = EngelsystemUriParser()

    @Test
    fun `parseUri returns Empty if url is empty`() {
        assertThat(uriParser.parseUri("")).isEqualTo(Empty)
    }

    @Test
    fun `parseUri returns Error if url is malformed`() {
        assertThat(uriParser.parseUri("https://example.com/path with spaces"))
            .isEqualTo(Error(URL_MALFORMED, "https://example.com/path with spaces"))
    }

    @Test
    fun `parseUri returns Error if url lacks a scheme`() {
        assertThat(uriParser.parseUri("example.com"))
            .isEqualTo(Error(SCHEME_MISSING, "example.com"))
    }

    @Test
    fun `parseUri returns Error if url lacks a host`() {
        assertThat(uriParser.parseUri("https://?key=a1b2c3"))
            .isEqualTo(Error(HOST_MISSING, "https://?key=a1b2c3"))
    }

    @Test
    fun `parseUri returns Error if url lacks a path`() {
        assertThat(uriParser.parseUri("https://example.com?key=a1b2c3"))
            .isEqualTo(Error(PATH_MISSING, "https://example.com?key=a1b2c3"))
    }

    @Test
    fun `parseUri returns Error if url lacks a query`() {
        assertThat(uriParser.parseUri("https://example.com/foo/file.json"))
            .isEqualTo(Error(QUERY_MISSING, "https://example.com/foo/file.json"))
    }

    @Test
    fun `parseUri returns Error if url lacks an API key`() {
        assertThat(uriParser.parseUri("https://example.com/foo/file.json?key"))
            .isEqualTo(Error(API_KEY_INVALID, "https://example.com/foo/file.json?key"))
    }

    @Test
    fun `parseUri returns Error if url has been pasted twice`() {
        assertThat(uriParser.parseUri("https://example.com/foo/file.json?key=a1b2c3https://example.com/foo/file.json?key=a1b2c3"))
            .isEqualTo(Error(API_KEY_INVALID, "https://example.com/foo/file.json?key=a1b2c3https://example.com/foo/file.json?key=a1b2c3"))
    }

    @Test
    fun `parseUri returns Error if url has different query parameter`() {
        assertThat(uriParser.parseUri("https://example.com/foo/file.json?foo=bar"))
            .isEqualTo(Error(API_KEY_INVALID, "https://example.com/foo/file.json?foo=bar"))
    }

    @Test
    fun `parseUri returns Error if url has key parameter with empty value`() {
        assertThat(uriParser.parseUri("https://example.com/foo/file.json?key="))
            .isEqualTo(Error(API_KEY_INVALID, "https://example.com/foo/file.json?key="))
    }

    @Test
    fun `parseUri returns Parsed if url is complete`() {
        val uri = EngelsystemUri("https://example.com", "foo/file.json", "a1b2c3")
        assertThat(uriParser.parseUri("https://example.com/foo/file.json?key=a1b2c3"))
            .isEqualTo(Parsed(uri))
    }

    @Test
    fun `parseUri returns Parsed if url is complete and port is present`() {
        val uri = EngelsystemUri("https://example.com:3000", "foo/file.json", "a1b2c3")
        assertThat(uriParser.parseUri("https://example.com:3000/foo/file.json?key=a1b2c3"))
            .isEqualTo(Parsed(uri))
    }

    @Test
    fun `parseUri returns Parsed if url has multiple query parameters including key`() {
        val uri = EngelsystemUri("https://example.com", "foo/file.json", "a1b2c3")
        assertThat(uriParser.parseUri("https://example.com/foo/file.json?foo=bar&key=a1b2c3"))
            .isEqualTo(Parsed(uri))
    }

}
