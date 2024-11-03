package nerd.tuxmobil.fahrplan.congress.search

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

private const val SOME_DATE = "8/13/15"
private const val SOME_TIME = "5:15 PM"

class SearchResultParameterFactoryTest {

    @Test
    fun `createSearchResults returns empty list when sessions is empty`() {
        val factory = createFactory()
        val sessions = emptyList<Session>()
        val results = factory.createSearchResults(sessions, useDeviceTimeZone = false)
        assertThat(results).isEmpty()
    }

    @Test
    fun `createSearchResults returns sessions when sessions is not empty`() {
        val factory = createFactory()
        val sessions = listOf(
            Session(
                sessionId = "123",
                title = "Session 123",
                speakers = listOf("Jane Doe", "John Doe"),
                dateUTC = 1683981000000,
            )
        )
        val results = factory.createSearchResults(sessions, useDeviceTimeZone = false)
        assertThat(results).isEqualTo(
            listOf(
                SearchResultParameter.SearchResult(
                    id = "123",
                    title = SearchResultProperty(value = "Session 123", contentDescription = "Title: Session 123"),
                    speakerNames = SearchResultProperty(value = "Jane Doe; John Doe", contentDescription = "Speakers: Jane Doe; John Doe"),
                    startsAt = SearchResultProperty(value = "$SOME_DATE, $SOME_TIME", contentDescription = "Start time: $SOME_DATE, $SOME_TIME"),
                )
            )
        )
    }

    @Test
    fun `createSearchResults returns sessions with dashes when sessions is not empty and contains empty properties`() {
        val factory = createFactory()
        val sessions = listOf(
            Session(
                sessionId = "123",
                title = "",
                speakers = emptyList(),
                dateUTC = 1683981000000,
            )
        )
        val results = factory.createSearchResults(sessions, useDeviceTimeZone = false)
        assertThat(results).isEqualTo(
            listOf(
                SearchResultParameter.SearchResult(
                    id = "123",
                    title = SearchResultProperty(value = "-", contentDescription = ""),
                    speakerNames = SearchResultProperty(value = "-", contentDescription = ""),
                    startsAt = SearchResultProperty(value = "$SOME_DATE, $SOME_TIME", contentDescription = "Start time: $SOME_DATE, $SOME_TIME"),
                )
            )
        )
    }

    private fun createFactory(): SearchResultParameterFactory {
        return SearchResultParameterFactory(
            resourceResolving = CompleteResourceResolver,
            sessionPropertiesFormatter = sessionPropertiesFormatter,
            contentDescriptionFormatter = ContentDescriptionFormatter(CompleteResourceResolver),
            formattingDelegate = formattingDelegate,
        )
    }

    private val sessionPropertiesFormatter = mock<SessionPropertiesFormatter> {
        on { getFormattedSpeakers(anyOrNull()) } doReturn "Jane Doe; John Doe"
    }

    private val formattingDelegate = mock<FormattingDelegate> {
        on { getFormattedDateTimeLong(anyOrNull(), anyOrNull(), anyOrNull()) } doReturn "$SOME_DATE, $SOME_TIME"
    }
}

private object CompleteResourceResolver : ResourceResolving {
    override fun getString(id: Int, vararg formatArgs: Any) = when (id) {
        R.string.dash -> "-"
        R.string.session_list_item_title_content_description -> "Title: Session 123"
        R.string.session_list_item_start_time_content_description -> "Start time: $SOME_DATE, $SOME_TIME"
        R.plurals.session_list_item_speakers_content_description -> "Speakers: Jane Doe; John Doe"
        else -> fail("Unknown string id : $id")
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String =
        when (id) {
            R.plurals.session_list_item_speakers_content_description ->
                when (quantity) {
                    0 -> ""
                    else -> "Speakers: Jane Doe; John Doe"
                }

            else -> fail("Unknown quantity string id : $id")
        }
}
