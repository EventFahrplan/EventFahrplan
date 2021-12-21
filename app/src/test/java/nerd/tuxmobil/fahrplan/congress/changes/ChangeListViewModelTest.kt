package nerd.tuxmobil.fahrplan.congress.changes

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
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ChangeListViewModelTest {

    @get:Rule
    val mainDispatcherTestRule = MainDispatcherTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `changeListParameter emits null`() {
        val repository = createRepository(sessionsFlow = emptyFlow())
        val viewModel = createViewModel(repository)
        assertLiveData(viewModel.changeListParameter).isNull()
        verifyInvokedNever(repository).readMeta()
        verifyInvokedNever(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `changeListParameter emits zero sessions`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        val expected = ChangeListParameter(emptyList(), 0, false)
        assertLiveData(viewModel.changeListParameter).isEqualTo(expected)
        verifyInvokedNever(repository).readMeta()
        verifyInvokedNever(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `changeListParameter emits a single session`() {
        val repository = createRepository(
            sessionsFlow = flowOf(listOf(Session("18"))),
            meta = Meta(numDays = 3),
            useDeviceTimeZoneEnabled = true
        )
        val viewModel = createViewModel(repository)
        val expectedSessions = listOf(Session("18"))
        val expected = ChangeListParameter(expectedSessions, 3, true)
        assertLiveData(viewModel.changeListParameter).isEqualTo(expected)
        verifyInvokedOnce(repository).readMeta()
        verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `updateScheduleChangesSeen invokes repository function`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.updateScheduleChangesSeen(changesSeen = true)
        assertLiveData(viewModel.scheduleChangesSeen).isEqualTo(Unit)
        verifyInvokedOnce(repository).updateScheduleChangesSeen(changesSeen = true)
    }

    private fun createRepository(
        sessionsFlow: Flow<List<Session>> = flowOf(emptyList()),
        meta: Meta = Meta(numDays = 0),
        useDeviceTimeZoneEnabled: Boolean = false
    ) = mock<AppRepository> {
        on { changedSessions } doReturn sessionsFlow
        on { readMeta() } doReturn meta
        on { readUseDeviceTimeZoneEnabled() } doReturn useDeviceTimeZoneEnabled
    }

    private fun createViewModel(
        repository: AppRepository,
    ) = ChangeListViewModel(
        repository,
        TestExecutionContext,
        NoLogging
    )

}
