package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestRule
import info.metadude.android.eventfahrplan.commons.testing.assertLiveData
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.DateInfos
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.schedule.observables.FahrplanEmptyParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.FahrplanParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.ScrollToCurrentSessionParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.ScrollToSessionParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.TimeTextViewParameter
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.threeten.bp.ZoneOffset

class FahrplanViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherTestRule = MainDispatcherTestRule()

    private val simpleSessionFormat = mock<SimpleSessionFormat>()
    private val jsonSessionFormat = mock<JsonSessionFormat>()
    private val scrollAmountCalculator = mock<ScrollAmountCalculator>()

    @Test
    fun `fahrplanParameter neither posts to fahrplanParameter nor fahrplanEmptyParameter properties`() {
        val repository = createRepository(uncanceledSessionsForDayIndexFlow = emptyFlow())
        val viewModel = createViewModel(repository)
        assertLiveData(viewModel.fahrplanParameter).isNull()
        assertLiveData(viewModel.fahrplanEmptyParameter).isNull()
        verifyInvokedNever(repository).readMeta()
        verifyInvokedNever(repository).readDisplayDayIndex()
        verifyInvokedNever(repository).readDateInfos()
    }

    @Test
    fun `updateUncanceledSessions posts FahrplanEmptyParameter to fahrplanEmptyParameter property`() {
        val scheduleData = ScheduleData(0, emptyList())
        val repository = createRepository(
            uncanceledSessionsForDayIndexFlow = flowOf(scheduleData),
            meta = Meta(version = "test-version")
        )
        val viewModel = createViewModel(repository)
        val expected = FahrplanEmptyParameter("test-version")
        assertLiveData(viewModel.fahrplanEmptyParameter).isEqualTo(expected)
        assertThat(viewModel.fahrplanParameter.value).isNull()
        verifyInvokedOnce(repository).readMeta()
        verifyInvokedNever(repository).readDisplayDayIndex()
        verifyInvokedNever(repository).readDateInfos()
    }

    @Test
    fun `updateUncanceledSessions never posts to fahrplanEmptyParameter property when schedule has never been loaded`() {
        val scheduleData = ScheduleData(0, emptyList())
        val repository = createRepository(
            uncanceledSessionsForDayIndexFlow = flowOf(scheduleData),
            meta = Meta(version = "")
        )
        val viewModel = createViewModel(repository)
        assertLiveData(viewModel.fahrplanEmptyParameter).isNull()
        assertLiveData(viewModel.fahrplanParameter).isNull()
        verifyInvokedOnce(repository).readMeta()
        verifyInvokedNever(repository).readDisplayDayIndex()
        verifyInvokedNever(repository).readDateInfos()
    }

    @Test
    fun `updateUncanceledSessions posts FahrplanParameter to fahrplanParameter property`() {
        val repository = createRepository(
            uncanceledSessionsForDayIndexFlow = flowOf(createScheduleData("session-01")),
            meta = Meta(numDays = 1),
            displayDayIndex = 2
        )
        val menuEntriesGenerator = mock<NavigationMenuEntriesGenerator>()
        val viewModel = createViewModel(repository, navigationMenuEntriesGenerator = menuEntriesGenerator)
        val expected = FahrplanParameter(
            scheduleData = createScheduleData("session-01"),
            numDays = 1,
            dayIndex = 2,
            dayMenuEntries = null
        )
        assertLiveData(viewModel.fahrplanParameter).isEqualTo(expected)
        assertThat(viewModel.fahrplanEmptyParameter.value).isNull()
        verifyInvokedNever(menuEntriesGenerator).getDayMenuEntries(any(), anyOrNull(), any())
        verifyInvokedOnce(repository).readDisplayDayIndex()
        verifyInvokedOnce(repository).readMeta()
        verifyInvokedOnce(repository).readDateInfos()
    }

    @Test
    fun `updateUncanceledSessions generates navigation menu entries`() {
        val repository = createRepository(
            uncanceledSessionsForDayIndexFlow = flowOf(createScheduleData("session-01")),
            meta = Meta(numDays = 2),
            displayDayIndex = 0
        )
        val menuEntriesGenerator = mock<NavigationMenuEntriesGenerator> {
            on { getDayMenuEntries(any(), anyOrNull(), any()) } doReturn listOf("Day 1", "Day 2")
        }
        val viewModel = createViewModel(repository, navigationMenuEntriesGenerator = menuEntriesGenerator)
        val expected = FahrplanParameter(
            scheduleData = createScheduleData("session-01"),
            numDays = 2,
            dayIndex = 0,
            dayMenuEntries = listOf("Day 1", "Day 2")
        )
        assertLiveData(viewModel.fahrplanParameter).isEqualTo(expected)
        assertThat(viewModel.fahrplanEmptyParameter.value).isNull()
        verifyInvokedOnce(menuEntriesGenerator).getDayMenuEntries(any(), anyOrNull(), any())
        verifyInvokedOnce(repository).readDisplayDayIndex()
        verifyInvokedOnce(repository).readMeta()
        verifyInvokedOnce(repository).readDateInfos()
    }

    @Test
    fun `requestScheduleUpdate invokes repository function`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.requestScheduleUpdate(isUserRequest = true)
        verifyInvokedOnce(repository).loadSchedule(isUserRequest = true, onFetchingDone = {}, onParsingDone = {}, onLoadingShiftsDone = {})
    }

    @Test
    fun `requestScheduleAutoUpdate invokes repository function`() {
        val repository = createRepository(isAutoUpdateEnabled = true)
        val viewModel = createViewModel(repository)
        viewModel.requestScheduleAutoUpdate()
        verifyInvokedOnce(repository).loadSchedule(isUserRequest = false, onFetchingDone = {}, onParsingDone = {}, onLoadingShiftsDone = {})
    }

    @Test
    fun `requestScheduleAutoUpdate never invokes repository function`() {
        val repository = createRepository(isAutoUpdateEnabled = false)
        val viewModel = createViewModel(repository)
        viewModel.requestScheduleAutoUpdate()
        verifyInvokedNever(repository).loadSchedule(isUserRequest = false, onFetchingDone = {}, onParsingDone = {}, onLoadingShiftsDone = {})
    }

    @Test
    fun `fillTimes posts TimeTextViewParameter to timeTextViewParameters property`() {
        val startsAt = 1582963200000L // February 29, 2020 08:00:00 AM GMT
        val earliestSession = mock<Session> {
            on { startTimeMoment } doReturn Moment.ofEpochMilli(startsAt)
        }
        val session = Session("session-01").apply {
            dateUTC = startsAt
            duration = 30
            timeZoneOffset = ZoneOffset.UTC
        }
        val repository = createRepository(
            uncanceledSessionsForDayIndexFlow = flowOf(createScheduleData(session)),
            earliestSession = earliestSession
        )
        val viewModel = createViewModel(repository)
        viewModel.fillTimes(nowMoment = mock(), normalizedBoxHeight = 42)
        val expected = listOf(
            TimeTextViewParameter(R.layout.time_layout, height = 126, titleText = "08:00"),
            TimeTextViewParameter(R.layout.time_layout, height = 126, titleText = "08:15")
        )
        assertLiveData(viewModel.timeTextViewParameters).isEqualTo(expected)
        verifyInvokedOnce(repository).loadEarliestSession()
        verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
        verify(repository, times(2)).readDisplayDayIndex()
    }

    @Test
    fun `updateFavorStatus invokes repository function`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.updateFavorStatus(Session("session-52"))
        verifyInvokedOnce(repository).updateHighlight(Session("session-52"))
    }

    @Test
    fun `addAlarm invokes alarmService function`() {
        val repository = createRepository()
        val alarmServices = mock<AlarmServices>()
        val viewModel = createViewModel(repository, alarmServices)
        val session = Session("session-97")
        viewModel.addAlarm(session, alarmTimesIndex = 0)
        verifyInvokedOnce(alarmServices).addSessionAlarm(session, alarmTimesIndex = 0)
    }

    @Test
    fun `deleteAlarm invokes alarmService function`() {
        val repository = createRepository()
        val alarmServices = mock<AlarmServices>()
        val viewModel = createViewModel(repository, alarmServices)
        val session = Session("session-97")
        viewModel.deleteAlarm(session)
        verifyInvokedOnce(alarmServices).deleteSessionAlarm(session)
    }

    @Test
    fun `share posts to shareSimple property`() {
        val repository = createRepository()
        val fakeSessionFormat = mock<SimpleSessionFormat> {
            on { format(any(), anyOrNull(), any()) } doReturn "session-61"
        }
        val viewModel = createViewModel(repository, simpleSessionFormat = fakeSessionFormat)
        viewModel.share(Session("61"))
        assertLiveData(viewModel.shareSimple).isEqualTo("session-61")
        verifyInvokedOnce(repository).readMeta()
    }

    @Test
    fun `shareToChaosflix posts to shareJson property`() {
        val repository = createRepository()
        val fakeSessionFormat = mock<JsonSessionFormat> {
            on { format(any<Session>()) } doReturn "session-62"
        }
        val viewModel = createViewModel(repository, jsonSessionFormat = fakeSessionFormat)
        viewModel.shareToChaosflix(Session("62"))
        assertLiveData(viewModel.shareJson).isEqualTo("session-62")
    }

    @Test
    fun `selectDay updates preserveVerticalScrollPosition property when displayDayIndex changes`() {
        val repository = createRepository(displayDayIndex = 1)
        val viewModel = createViewModel(repository)
        viewModel.preserveVerticalScrollPosition = true
        viewModel.selectDay(dayItemPosition = 3)
        assertThat(viewModel.preserveVerticalScrollPosition).isFalse()
        verifyInvokedOnce(repository).readDisplayDayIndex()
        verifyInvokedOnce(repository).updateDisplayDayIndex(displayDayIndex = 4)
    }

    @Test
    fun `selectDay never updates preserveVerticalScrollPosition property when displayDayIndex is the same`() {
        val repository = createRepository(displayDayIndex = 4)
        val viewModel = createViewModel(repository)
        viewModel.preserveVerticalScrollPosition = true
        viewModel.selectDay(dayItemPosition = 3)
        assertThat(viewModel.preserveVerticalScrollPosition).isTrue()
        verifyInvokedOnce(repository).readDisplayDayIndex()
        verifyInvokedNever(repository).updateDisplayDayIndex(displayDayIndex = any())
    }

    @Test
    fun `saveSelectedDayIndex invokes repository function`() {
        val repository = createRepository()
        val viewModel = createViewModel(repository)
        viewModel.saveSelectedDayIndex(dayIndex = 2)
        verifyInvokedOnce(repository).updateDisplayDayIndex(displayDayIndex = 2)
    }

    @Test
    fun `scrollToCurrentSession never posts to scrollToCurrentSessionParameter property when sessions is empty`() {
        val scheduleData = ScheduleData(0, emptyList())
        val repository = createRepository(uncanceledSessionsForDayIndexFlow = flowOf(scheduleData))
        val viewModel = createViewModel(repository)
        viewModel.scrollToCurrentSession()
        verifyInvokedNever(repository).readDisplayDayIndex()
        assertLiveData(viewModel.scrollToCurrentSessionParameter).isNull()
    }

    @Test
    fun `scrollToCurrentSession posts to scrollToCurrentSessionParameter property when session is present and day indices match`() {
        val scheduleData = createScheduleData(Session("session-31"), dayIndex = 3)
        val nowMoment = Moment.now().startOfDay() // depends DateInfos.getIndexOfToday
        val repository = createRepository(
            loadUncanceledSessionsForDayIndex = scheduleData,
            dateInfos = DateInfos().apply { add(DateInfo(3, nowMoment)) }
        )
        val viewModel = createViewModel(repository)
        viewModel.scrollToCurrentSession()
        val expected = ScrollToCurrentSessionParameter(
            scheduleData = createScheduleData(Session("session-31"), dayIndex = 3),
            dateInfos = DateInfos().apply { add(DateInfo(3, nowMoment)) }
        )
        assertLiveData(viewModel.scrollToCurrentSessionParameter).isEqualTo(expected)
    }

    @Test
    fun `scrollToCurrentSession never posts to scrollToCurrentSessionParameter property when day indices mismatch`() {
        val scheduleData = createScheduleData(Session("session-21"), dayIndex = 2)
        val nowMoment = Moment.now().startOfDay() // depends DateInfos.getIndexOfToday
        val repository = createRepository(
            loadUncanceledSessionsForDayIndex = scheduleData,
            dateInfos = DateInfos().apply { add(DateInfo(3, nowMoment)) }
        )
        val viewModel = createViewModel(repository)
        viewModel.scrollToCurrentSession()
        assertLiveData(viewModel.scrollToCurrentSessionParameter).isNull()
    }

    @Test
    fun `scrollToSession never posts to scrollToSessionParameter property when sessions is empty`() {
        val scheduleData = ScheduleData(0, emptyList())
        val repository = createRepository(uncanceledSessionsForDayIndexFlow = flowOf(scheduleData))
        val viewModel = createViewModel(repository)
        viewModel.scrollToSession("session-27", boxHeight = 42)
        assertLiveData(viewModel.scrollToSessionParameter).isNull()
    }

    @Test
    fun `scrollToSession never posts to scrollToSessionParameter property when session can not be found`() {
        val scheduleData = createScheduleData("session-01")
        val repository = createRepository(uncanceledSessionsForDayIndexFlow = flowOf(scheduleData))
        val viewModel = createViewModel(repository)
        viewModel.scrollToSession("session-27", boxHeight = 42)
        assertLiveData(viewModel.scrollToSessionParameter).isNull()
        verifyInvokedOnce(repository).readDisplayDayIndex()
    }

    @Test
    fun `scrollToSession posts to scrollToSessionParameter property when session is present and matched`() {
        val scheduleData = createScheduleData("session-02")
        val repository = createRepository(loadUncanceledSessionsForDayIndex = scheduleData)
        val viewModel = createViewModel(repository)
        viewModel.scrollToSession("session-02", boxHeight = 42)
        val expected = ScrollToSessionParameter(sessionId = "session-02", verticalPosition = 0, roomIndex = 0)
        assertLiveData(viewModel.scrollToSessionParameter).isEqualTo(expected)
    }

    private fun createRepository(
        uncanceledSessionsForDayIndexFlow: Flow<ScheduleData> = emptyFlow(),
        loadUncanceledSessionsForDayIndex: ScheduleData = mock(),
        meta: Meta = Meta(numDays = 0, version = "test-version"),
        isAutoUpdateEnabled: Boolean = true,
        displayDayIndex: Int = 0,
        earliestSession: Session = Session(""),
        dateInfos: DateInfos = DateInfos()
    ) = mock<AppRepository> {
        on { uncanceledSessionsForDayIndex } doReturn uncanceledSessionsForDayIndexFlow
        on { loadUncanceledSessionsForDayIndex() } doReturn loadUncanceledSessionsForDayIndex
        on { readMeta() } doReturn meta
        on { readAutoUpdateEnabled() } doReturn isAutoUpdateEnabled
        on { readDisplayDayIndex() } doReturn displayDayIndex
        on { loadEarliestSession() } doReturn earliestSession
        on { readDateInfos() } doReturn dateInfos
    }

    private fun createScheduleData(sessionId: String? = null): ScheduleData {
        val session = if (sessionId == null) null else Session(sessionId)
        return createScheduleData(session)
    }

    private fun createScheduleData(session: Session? = null, dayIndex: Int = 0): ScheduleData {
        val sessions = if (session == null) emptyList() else listOf(session)
        val roomDataList = listOf(RoomData(roomName = "", sessions))
        return ScheduleData(dayIndex = dayIndex, roomDataList)
    }

    private fun createViewModel(
        repository: AppRepository,
        alarmServices: AlarmServices = mock(),
        navigationMenuEntriesGenerator: NavigationMenuEntriesGenerator = mock(),
        simpleSessionFormat: SimpleSessionFormat = this.simpleSessionFormat,
        jsonSessionFormat: JsonSessionFormat = this.jsonSessionFormat
    ) = FahrplanViewModel(
        repository = repository,
        executionContext = TestExecutionContext,
        logging = NoLogging,
        alarmServices = alarmServices,
        navigationMenuEntriesGenerator = navigationMenuEntriesGenerator,
        simpleSessionFormat = simpleSessionFormat,
        jsonSessionFormat = jsonSessionFormat,
        scrollAmountCalculator = scrollAmountCalculator
    )

}
