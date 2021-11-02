package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestRule
import info.metadude.android.eventfahrplan.commons.testing.assertLiveData
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class StarredListViewModelTest {

    @get:Rule
    val mainDispatcherTestRule = MainDispatcherTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val simpleSessionFormat = mock<SimpleSessionFormat>()
    private val jsonSessionFormat = mock<JsonSessionFormat>()

    @Test
    fun `starredListParameter returns null`() {
        val repository = createRepository(sessionsFlow = emptyFlow())
        val viewModel = createViewModel(repository)
        assertLiveData(viewModel.starredListParameter).isEqualTo(null)
        verifyInvokedNever(repository).readMeta()
        verifyInvokedNever(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `starredListParameter returns zero sessions`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        val expected = StarredListParameter(emptyList(), 0, false)
        assertLiveData(viewModel.starredListParameter).isEqualTo(expected)
        verifyInvokedNever(repository).readMeta()
        verifyInvokedNever(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `starredListParameter returns a single session`() {
        val repository = createRepository(
            sessionsFlow = flowOf(listOf(Session("23"))),
            meta = Meta(numDays = 2),
            useDeviceTimeZoneEnabled = true
        )
        val viewModel = createViewModel(repository)
        val expectedSessions = listOf(Session("23"))
        val expected = StarredListParameter(expectedSessions, 2, true)
        assertLiveData(viewModel.starredListParameter).isEqualTo(expected)
        verifyInvokedOnce(repository).readMeta()
        verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `delete invokes repository functions`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.delete(Session("42"))
        verifyInvokedOnce(repository).updateHighlight(any())
        verifyInvokedOnce(repository).notifyHighlightsChanged()
    }

    @Test
    fun `deleteAll invokes repository functions`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.deleteAll()
        verifyInvokedOnce(repository).deleteAllHighlights()
        verifyInvokedOnce(repository).notifyHighlightsChanged()
    }

    @Test
    fun `initialization does not affect shareSimple property`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        assertLiveData(viewModel.shareSimple).isEqualTo(null)
    }

    @Test
    fun `share posts to shareSimple property when session is present`() {
        val repository = createRepository(
            sessionsFlow = flowOf(listOf(Session("23"))),
            meta = Meta(numDays = 0, timeZoneId = null)
        )
        val fakeSessionFormat = mock<SimpleSessionFormat> {
            on { format(any<List<Session>>(), anyOrNull()) } doReturn "session-23"
        }
        val viewModel = createViewModel(repository, simpleSessionFormat = fakeSessionFormat)
        viewModel.share()
        assertLiveData(viewModel.shareSimple).isEqualTo("session-23")
    }

    @Test
    fun `share never posts to shareSimple property when sessions is empty`() {
        val repository = createRepository(
            sessionsFlow = flowOf(emptyList()),
            meta = Meta(numDays = 0, timeZoneId = null)
        )
        val fakeSessionFormat = mock<SimpleSessionFormat> {
            on { format(any<List<Session>>(), anyOrNull()) } doReturn null // simulating empty list
        }
        val viewModel = createViewModel(repository, simpleSessionFormat = fakeSessionFormat)
        viewModel.share()
        assertLiveData(viewModel.shareSimple).isEqualTo(null)
    }

    @Test
    fun `initialization does not affect shareJson property`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        assertLiveData(viewModel.shareJson).isEqualTo(null)
    }

    @Test
    fun `shareToChaosflix posts to shareJson property when session is present`() {
        val repository = createRepository(sessionsFlow = flowOf(listOf(Session("17"))))
        val fakeSessionFormat = mock<JsonSessionFormat> {
            on { format(any<List<Session>>()) } doReturn "session-17"
        }
        val viewModel = createViewModel(repository, jsonSessionFormat = fakeSessionFormat)
        viewModel.shareToChaosflix()
        assertLiveData(viewModel.shareJson).isEqualTo("session-17")
    }

    @Test
    fun `shareToChaosflix never posts to shareJson property when sessions is empty`() {
        val repository = createRepository(sessionsFlow = flowOf(emptyList()))
        val fakeSessionFormat = mock<JsonSessionFormat> {
            on { format(any<List<Session>>()) } doReturn null // simulating empty list
        }
        val viewModel = createViewModel(repository, jsonSessionFormat = fakeSessionFormat)
        viewModel.shareToChaosflix()
        assertLiveData(viewModel.shareJson).isEqualTo(null)
    }

    private fun createRepository(
        sessionsFlow: Flow<List<Session>> = flowOf(emptyList()),
        meta: Meta = Meta(numDays = 0),
        useDeviceTimeZoneEnabled: Boolean = false
    ) = mock<AppRepository> {
        on { starredSessions } doReturn sessionsFlow
        on { readMeta() } doReturn meta
        on { readUseDeviceTimeZoneEnabled() } doReturn useDeviceTimeZoneEnabled
    }

    private fun createViewModel(
        repository: AppRepository,
        simpleSessionFormat: SimpleSessionFormat = this.simpleSessionFormat,
        jsonSessionFormat: JsonSessionFormat = this.jsonSessionFormat
    ) = StarredListViewModel(
        repository,
        TestExecutionContext,
        NoLogging,
        simpleSessionFormat,
        jsonSessionFormat
    )

}
