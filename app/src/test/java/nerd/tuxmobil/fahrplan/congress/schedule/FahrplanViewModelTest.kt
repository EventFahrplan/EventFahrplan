package nerd.tuxmobil.fahrplan.congress.schedule

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame.Known
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame.Unknown
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.DateInfos
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus.HTTP_NOT_FOUND
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Fetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialFetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialParsing
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Parsing
import nerd.tuxmobil.fahrplan.congress.schedule.observables.FahrplanEmptyParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.FahrplanParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.ScrollToCurrentSessionParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.ScrollToSessionParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.TimeTextViewParameter
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.threeten.bp.ZoneOffset

@ExtendWith(MainDispatcherTestExtension::class)
class FahrplanViewModelTest {

    companion object {
        private val fetchFailure = FetchFailure(
            httpStatus = HTTP_NOT_FOUND,
            hostName = "example.com",
            exceptionMessage = "",
            isUserRequest = false,
        )
        private val parseFailure = ParseFailure(
            ParseScheduleResult(isSuccess = true, version = "")
        )

        @JvmStatic
        fun showByState() = listOf(
            arrayOf(InitialFetching, false),
            arrayOf(Fetching, false),
            arrayOf(InitialParsing, false),
            arrayOf(Parsing, false),
            arrayOf(FetchSuccess, true),
            arrayOf(fetchFailure, true),
            arrayOf(ParseSuccess, true),
            arrayOf(parseFailure, true),
        )
    }

    private val simpleSessionFormat = mock<SimpleSessionFormat>()
    private val jsonSessionFormat = mock<JsonSessionFormat>()
    private val scrollAmountCalculator = mock<ScrollAmountCalculator>()

    @ParameterizedTest(name = "{index}: showHorizontalScrollingProgressLine property emits {1} when loadScheduleState is {0}")
    @MethodSource("showByState")
    fun `HorizontalProgressLine #`(
        state: LoadScheduleState,
        shouldShow: Boolean
    ) = runTest {
        val repository = createRepository(loadScheduleStateFlow = flowOf(state))
        val viewModel = createViewModel(repository)
        viewModel.showHorizontalScrollingProgressLine.test {
            assertThat(awaitItem()).isEqualTo(shouldShow)
            expectNoEvents()
        }
    }

    @Nested
    inner class ScheduleUpdateAlarm {

        @Test
        fun `requestScheduleUpdateAlarm never posts to activateScheduleUpdateAlarm property when no sessions are emitted`() =
            runTest {
                val repository = createRepository(sessionsFlow = emptyFlow())
                val viewModel = createViewModel(repository)
                viewModel.activateScheduleUpdateAlarm.test {
                    expectNoEvents()
                }
            }

        @Test
        fun `requestScheduleUpdateAlarm posts to activateScheduleUpdateAlarm property when sessions are empty`() =
            runTest {
                val repository = createRepository(sessionsFlow = flowOf(emptyList()))
                val viewModel = createViewModel(repository)
                viewModel.activateScheduleUpdateAlarm.test {
                    assertThat(awaitItem()).isEqualTo(Unknown)
                }
            }

        @Test
        fun `requestScheduleUpdateAlarm posts to activateScheduleUpdateAlarm property when sessions are present`() =
            runTest {
                val session1 = Session("1", dateUTC = Moment.ofEpochMilli(0).toMilliseconds())
                val session2 = Session("2", dateUTC = Moment.ofEpochMilli(1).toMilliseconds())
                val repository = createRepository(sessionsFlow = flowOf(listOf(session1, session2)))
                val viewModel = createViewModel(repository)
                viewModel.activateScheduleUpdateAlarm.test {
                    val expected = Known(Moment.ofEpochMilli(0), Moment.ofEpochMilli(1))
                    assertThat(awaitItem()).isEqualTo(expected)
                }
            }

        @Test
        fun `requestScheduleUpdateAlarm posts once to activateScheduleUpdateAlarm property when sessions do not change`() =
            runTest {
                val sessionsFlow = MutableStateFlow<List<Session>>(emptyList())
                val repository = createRepository(sessionsFlow = sessionsFlow)
                val viewModel = createViewModel(repository)
                viewModel.activateScheduleUpdateAlarm.test {
                    assertThat(awaitItem()).isEqualTo(Unknown)
                    sessionsFlow.emit(emptyList())
                    expectNoEvents()
                }
            }

        @Test
        fun `requestScheduleUpdateAlarm posts repetitive to activateScheduleUpdateAlarm property when sessions change`() =
            runTest {
                val sessionsFlow = MutableStateFlow<List<Session>>(emptyList())
                val repository = createRepository(sessionsFlow = sessionsFlow)
                val viewModel = createViewModel(repository)
                viewModel.activateScheduleUpdateAlarm.test {
                    assertThat(awaitItem()).isEqualTo(Unknown)
                    val session1 = Session("1", dateUTC = Moment.ofEpochMilli(0).toMilliseconds())
                    val session2 = Session("2", dateUTC = Moment.ofEpochMilli(1).toMilliseconds())
                    sessionsFlow.emit(listOf(session1, session2))
                    val expected = Known(Moment.ofEpochMilli(0), Moment.ofEpochMilli(1))
                    assertThat(awaitItem()).isEqualTo(expected)
                }
            }

    }

    @Nested
    inner class SessionData {

        @Test
        fun `updateUncanceledSessions neither posts to fahrplanParameter nor fahrplanEmptyParameter properties`() =
            runTest {
                val repository = createRepository(uncanceledSessionsForDayIndexFlow = emptyFlow())
                val viewModel = createViewModel(repository)
                viewModel.fahrplanParameter.test {
                    awaitComplete()
                }
                viewModel.fahrplanEmptyParameter.test {
                    expectNoEvents()
                }
                verifyInvokedNever(repository).readMeta()
                verifyInvokedNever(repository).readDisplayDayIndex()
            }

        @Test
        fun `updateUncanceledSessions posts FahrplanEmptyParameter to fahrplanEmptyParameter property`() =
            runTest {
                val scheduleData = ScheduleData(0, emptyList())
                val repository = createRepository(
                    uncanceledSessionsForDayIndexFlow = flowOf(scheduleData),
                    meta = Meta(version = "test-version")

                )
                val viewModel = createViewModel(repository)
                val expected = FahrplanEmptyParameter("test-version")
                viewModel.fahrplanEmptyParameter.test {
                    assertThat(awaitItem()).isEqualTo(expected)
                }
                viewModel.fahrplanParameter.test {
                    awaitComplete()
                }
                verifyInvokedOnce(repository).readMeta()
                verifyInvokedNever(repository).readDisplayDayIndex()
            }

        @Test
        fun `updateUncanceledSessions never posts to fahrplanEmptyParameter property when schedule has never been loaded`() =
            runTest {
                val scheduleData = ScheduleData(0, emptyList())
                val repository = createRepository(
                    uncanceledSessionsForDayIndexFlow = flowOf(scheduleData),
                    meta = Meta(version = "")
                )
                val viewModel = createViewModel(repository)
                viewModel.fahrplanEmptyParameter.test {
                    expectNoEvents()
                }
                viewModel.fahrplanParameter.test {
                    awaitComplete()
                }
                verifyInvokedOnce(repository).readMeta()
                verifyInvokedNever(repository).readDisplayDayIndex()
            }

        @Test
        fun `fahrplanParameter property emits FahrplanParameter`() = runTest {
            val repository = createRepository(
                uncanceledSessionsForDayIndexFlow = flowOf(createScheduleData("session-01")),
                sessionsWithoutShiftsFlow = flowOf(listOf(Session("not relevant"))),
                meta = Meta(numDays = 1),
                displayDayIndex = 2
            )
            val menuEntriesGenerator = mock<NavigationMenuEntriesGenerator>()
            val viewModel = createViewModel(repository, navigationMenuEntriesGenerator = menuEntriesGenerator)
            val expected = FahrplanParameter(
                scheduleData = createScheduleData("session-01"),
                useDeviceTimeZone = false,
                numDays = 1,
                dayIndex = 2,
                dayMenuEntries = emptyList()
            )
            viewModel.fahrplanParameter.test {
                assertThat(awaitItem()).isEqualTo(expected)
                awaitComplete()
            }
            viewModel.fahrplanEmptyParameter.test {
                expectNoEvents()
            }
            verifyInvokedNever(menuEntriesGenerator).getDayMenuEntries(any(), anyOrNull(), any())
            verifyInvokedOnce(repository).readDisplayDayIndex()
            verifyInvokedOnce(repository).readMeta()
        }

        @Test
        fun `fahrplanParameter property emits FahrplanParameter containing session with alarm flag`() =
            runTest {
                val repository = createRepository(
                    uncanceledSessionsForDayIndexFlow = flowOf(createScheduleData("session-01")),
                    sessionsWithoutShiftsFlow = flowOf(listOf(Session("not relevant"))),
                    meta = Meta(numDays = 1),
                    alarmsFlow = flowOf(listOf(createAlarm("session-01"))),
                    displayDayIndex = 2
                )
                val menuEntriesGenerator = mock<NavigationMenuEntriesGenerator>()
                val viewModel = createViewModel(repository, navigationMenuEntriesGenerator = menuEntriesGenerator)
                val expected = FahrplanParameter(
                    scheduleData = createScheduleData("session-01", hasAlarm = true),
                    useDeviceTimeZone = false,
                    numDays = 1,
                    dayIndex = 2,
                    dayMenuEntries = emptyList()
                )
                viewModel.fahrplanParameter.test {
                    val actual = awaitItem()
                    assertThat(actual).isEqualTo(expected)
                    // Obsolete once Session#hasAlarm is part of isEqual and hashCode
                    assertThat(actual.scheduleData.allSessions.first().hasAlarm).isEqualTo(true)
                    awaitComplete()
                }
                viewModel.fahrplanEmptyParameter.test {
                    expectNoEvents()
                }
                verifyInvokedNever(menuEntriesGenerator).getDayMenuEntries(any(), anyOrNull(), any())
                verifyInvokedOnce(repository).readDisplayDayIndex()
                verifyInvokedOnce(repository).readMeta()
            }

    }

    @Nested
    inner class NavigationMenu {

        @Test
        fun `fahrplanParameter property emits and generates navigation menu entries`() = runTest {
            val repository = createRepository(
                uncanceledSessionsForDayIndexFlow = flowOf(createScheduleData("session-01")),
                sessionsWithoutShiftsFlow = flowOf(listOf(Session("not relevant"))),
                meta = Meta(numDays = 2),
                displayDayIndex = 0
            )
            val menuEntriesGenerator = mock<NavigationMenuEntriesGenerator> {
                on { getDayMenuEntries(any(), anyOrNull(), any()) } doReturn listOf("Day 1", "Day 2")
            }
            val viewModel = createViewModel(repository, navigationMenuEntriesGenerator = menuEntriesGenerator)
            val expected = FahrplanParameter(
                scheduleData = createScheduleData("session-01"),
                useDeviceTimeZone = false,
                numDays = 2,
                dayIndex = 0,
                dayMenuEntries = listOf("Day 1", "Day 2")
            )
            viewModel.fahrplanParameter.test {
                assertThat(awaitItem()).isEqualTo(expected)
                awaitComplete()
            }
            viewModel.fahrplanEmptyParameter.test {
                expectNoEvents()
            }
            verifyInvokedOnce(menuEntriesGenerator).getDayMenuEntries(any(), anyOrNull(), any())
            verifyInvokedOnce(repository).readDisplayDayIndex()
            verifyInvokedOnce(repository).readMeta()
        }

    }

    @Nested
    inner class RequestScheduleUpdate {

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

    }

    @Nested
    inner class TimeText {

        @Test
        fun `fillTimes posts TimeTextViewParameter to timeTextViewParameters property`() = runTest {
            val startsAt = 1582963200000L // February 29, 2020 08:00:00 AM GMT
            val session = Session(
                guid = "11111111-1111-1111-1111-111111111101",
                dateUTC = startsAt,
                duration = 30,
                timeZoneOffset = ZoneOffset.UTC,
            )
            val repository = createRepository(
                uncanceledSessionsForDayIndexFlow = flowOf(createScheduleData(session)),
            )
            val viewModel = createViewModel(repository)
            viewModel.fillTimes(nowMoment = mock(), normalizedBoxHeight = 42)
            val expected = listOf(
                TimeTextViewParameter(R.layout.time_layout, height = 126, titleText = "08:00"),
                TimeTextViewParameter(R.layout.time_layout, height = 126, titleText = "08:15")
            )
            viewModel.timeTextViewParameters.test {
                assertThat(awaitItem()).isEqualTo(expected)
            }
            verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
        }

    }

    @Nested
    inner class Favoring {

        @Test
        fun `updateFavorStatus invokes repository function`() {
            val repository = createRepository()
            val viewModel = createViewModel(repository)
            viewModel.updateFavorStatus(Session("session-52"))
            verifyInvokedOnce(repository).updateHighlight(Session("session-52"))
        }

    }

    @Nested
    inner class Alarms {

        @Test
        fun `canAddAlarms invokes canScheduleExactAlarms property`() {
            val repository = createRepository()
            val alarmServices = mock<AlarmServices>()
            val viewModel = createViewModel(repository = repository, alarmServices = alarmServices)
            viewModel.canAddAlarms()
            verifyInvokedOnce(alarmServices).canScheduleExactAlarms
        }

        @Test
        fun `addAlarmWithChecks() posts to showAlarmTimePicker`() = runTest {
            val notificationHelper = mock<NotificationHelper> {
                on { notificationsEnabled } doReturn true
            }
            val alarmServices = mock<AlarmServices> {
                on { canScheduleExactAlarms } doReturn true
            }
            val repository = createRepository()
            val viewModel = createViewModel(
                repository = repository,
                notificationHelper = notificationHelper,
                alarmServices = alarmServices,
                runsAtLeastOnAndroidTiramisu = true, // not relevant
            )
            viewModel.addAlarmWithChecks()
            viewModel.showAlarmTimePicker.test {
                assertThat(awaitItem()).isEqualTo(Unit)
            }
            verifyInvokedOnce(notificationHelper).notificationsEnabled
            verifyInvokedOnce(alarmServices).canScheduleExactAlarms
        }

        @Test
        fun `addAlarmWithChecks() posts to requestScheduleExactAlarmsPermission`() = runTest {
            val notificationHelper = mock<NotificationHelper> {
                on { notificationsEnabled } doReturn true
            }
            val alarmServices = mock<AlarmServices> {
                on { canScheduleExactAlarms } doReturn false
            }
            val repository = createRepository()
            val viewModel = createViewModel(
                repository = repository,
                notificationHelper = notificationHelper,
                alarmServices = alarmServices,
                runsAtLeastOnAndroidTiramisu = true, // not relevant
            )
            viewModel.addAlarmWithChecks()
            viewModel.requestScheduleExactAlarmsPermission.test {
                assertThat(awaitItem()).isEqualTo(Unit)
            }
            verifyInvokedOnce(notificationHelper).notificationsEnabled
            verifyInvokedOnce(alarmServices).canScheduleExactAlarms
        }

        @Test
        fun `addAlarmWithChecks() posts to requestPostNotificationsPermission as of Android 13`() =
            runTest {
                val notificationHelper = mock<NotificationHelper> {
                    on { notificationsEnabled } doReturn false
                }
                val repository = createRepository()
                val viewModel = createViewModel(
                    repository = repository,
                    notificationHelper = notificationHelper,
                    runsAtLeastOnAndroidTiramisu = true,
                )
                viewModel.addAlarmWithChecks()
                viewModel.requestPostNotificationsPermission.test {
                    assertThat(awaitItem()).isEqualTo(Unit)
                }
                verifyInvokedOnce(notificationHelper).notificationsEnabled
            }

        @Test
        fun `addAlarmWithChecks() posts to notificationsDisabled before Android 13`() = runTest {
            val notificationHelper = mock<NotificationHelper> {
                on { notificationsEnabled } doReturn false
            }
            val repository = createRepository()
            val viewModel = createViewModel(
                repository = repository,
                notificationHelper = notificationHelper,
                runsAtLeastOnAndroidTiramisu = false,
            )
            viewModel.addAlarmWithChecks()
            viewModel.notificationsDisabled.test {
                assertThat(awaitItem()).isEqualTo(Unit)
            }
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

    }

    @Nested
    inner class Sharing {

        @Test
        fun `share posts to shareSimple property`() = runTest {
            val repository = createRepository()
            val fakeSessionFormat = mock<SimpleSessionFormat> {
                on { format(any(), anyOrNull(), any()) } doReturn "session-61"
            }
            val viewModel = createViewModel(repository, simpleSessionFormat = fakeSessionFormat)
            viewModel.share(Session("61"))
            viewModel.shareSimple.test {
                assertThat(awaitItem()).isEqualTo("session-61")
            }
            verifyInvokedOnce(repository).readMeta()
        }

        @Test
        fun `shareToChaosflix posts to shareJson property`() = runTest {
            val repository = createRepository()
            val fakeSessionFormat = mock<JsonSessionFormat> {
                on { format(any<Session>()) } doReturn "session-62"
            }
            val viewModel = createViewModel(repository, jsonSessionFormat = fakeSessionFormat)
            viewModel.shareToChaosflix(Session("62"))
            viewModel.shareJson.test {
                assertThat(awaitItem()).isEqualTo("session-62")
            }
        }

    }

    @Nested
    inner class DaySelection {


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

    }

    @Nested
    inner class Scrolling {

        @Test
        fun `scrollToCurrentSession never posts to scrollToCurrentSessionParameter property when sessions is empty`() =
            runTest {
                val scheduleData = ScheduleData(0, emptyList())
                val repository = createRepository(uncanceledSessionsForDayIndexFlow = flowOf(scheduleData))
                val viewModel = createViewModel(repository)
                viewModel.scrollToCurrentSession()
                verifyInvokedNever(repository).readDisplayDayIndex()
                viewModel.scrollToCurrentSessionParameter.test {
                    expectNoEvents()
                }
            }

        @Disabled("Flaky, see https://github.com/EventFahrplan/EventFahrplan/issues/526")
        @Test
        fun `scrollToCurrentSession posts to scrollToCurrentSessionParameter property when session is present and day indices match`() =
            runTest {
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
                viewModel.scrollToCurrentSessionParameter.test {
                    assertThat(awaitItem()).isEqualTo(expected)
                }
            }

        @Test
        fun `scrollToCurrentSession never posts to scrollToCurrentSessionParameter property when day indices mismatch`() =
            runTest {
                val scheduleData = createScheduleData(Session("session-21"), dayIndex = 2)
                val repository = createRepository(
                    loadUncanceledSessionsForDayIndex = scheduleData,
                )
                val viewModel = createViewModel(repository)
                viewModel.scrollToCurrentSession()
                viewModel.scrollToCurrentSessionParameter.test {
                    expectNoEvents()
                }
            }

        @Test
        fun `scrollToSession never posts to scrollToSessionParameter property when sessions is empty`() =
            runTest {
                val scheduleData = ScheduleData(0, emptyList())
                val repository = createRepository(uncanceledSessionsForDayIndexFlow = flowOf(scheduleData))
                val viewModel = createViewModel(repository)
                viewModel.scrollToSession("session-27", boxHeight = 42)
                viewModel.scrollToSessionParameter.test {
                    expectNoEvents()
                }
            }

        @Test
        fun `scrollToSession never posts to scrollToSessionParameter property when session can not be found`() =
            runTest {
                val scheduleData = createScheduleData("session-01")
                val repository = createRepository(uncanceledSessionsForDayIndexFlow = flowOf(scheduleData))
                val viewModel = createViewModel(repository)
                viewModel.scrollToSession("session-27", boxHeight = 42)
                viewModel.scrollToSessionParameter.test {
                    expectNoEvents()
                }
            }

        @Test
        fun `scrollToSession posts to scrollToSessionParameter property when session is present and matched`() =
            runTest {
                val scheduleData = createScheduleData("11111111-1111-1111-1111-111111111102")
                val repository = createRepository(loadUncanceledSessionsForDayIndex = scheduleData)
                val viewModel = createViewModel(repository)
                viewModel.scrollToSession("session-02", boxHeight = 42)
                val expected = ScrollToSessionParameter(guid = "11111111-1111-1111-1111-111111111102", verticalPosition = 0, roomIndex = 0)
                viewModel.scrollToSessionParameter.test {
                    assertThat(awaitItem()).isEqualTo(expected)
                }
            }

    }

    private fun createRepository(
        sessionsFlow: Flow<List<Session>> = emptyFlow(),
        uncanceledSessionsForDayIndexFlow: Flow<ScheduleData> = emptyFlow(),
        sessionsWithoutShiftsFlow: Flow<List<Session>> = emptyFlow(),
        loadUncanceledSessionsForDayIndex: ScheduleData = mock(),
        loadScheduleStateFlow: Flow<LoadScheduleState> = emptyFlow(),
        alarmsFlow: Flow<List<Alarm>> = flowOf(emptyList()),
        meta: Meta = Meta(numDays = 0, version = "test-version"),
        isAutoUpdateEnabled: Boolean = true,
        displayDayIndex: Int = 0,
        dateInfos: DateInfos = DateInfos(),
    ) = mock<AppRepository> {
        on { sessions } doReturn sessionsFlow
        on { uncanceledSessionsForDayIndex } doReturn uncanceledSessionsForDayIndexFlow
        on { sessionsWithoutShifts } doReturn sessionsWithoutShiftsFlow
        on { loadUncanceledSessionsForDayIndex() } doReturn loadUncanceledSessionsForDayIndex
        on { loadScheduleState } doReturn loadScheduleStateFlow
        on { alarms } doReturn alarmsFlow
        on { readMeta() } doReturn meta
        on { readAutoUpdateEnabled() } doReturn isAutoUpdateEnabled
        on { readDisplayDayIndex() } doReturn displayDayIndex
        on { readDateInfos() } doReturn dateInfos
    }

    private fun createScheduleData(guid: String? = null, hasAlarm: Boolean = false): ScheduleData {
        val session = if (guid == null) null else Session(guid = guid, hasAlarm = hasAlarm)
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
        notificationHelper: NotificationHelper = mock(),
        navigationMenuEntriesGenerator: NavigationMenuEntriesGenerator = mock(),
        simpleSessionFormat: SimpleSessionFormat = this.simpleSessionFormat,
        jsonSessionFormat: JsonSessionFormat = this.jsonSessionFormat,
        runsAtLeastOnAndroidTiramisu: Boolean = false
    ) = FahrplanViewModel(
        repository = repository,
        executionContext = TestExecutionContext,
        logging = NoLogging,
        alarmServices = alarmServices,
        notificationHelper = notificationHelper,
        navigationMenuEntriesGenerator = navigationMenuEntriesGenerator,
        simpleSessionFormat = simpleSessionFormat,
        jsonSessionFormat = jsonSessionFormat,
        scrollAmountCalculator = scrollAmountCalculator,
        defaultEngelsystemRoomName = "Engelshifts",
        customEngelsystemRoomName = "Trollshifts",
        runsAtLeastOnAndroidTiramisu = runsAtLeastOnAndroidTiramisu
    )

    private fun createAlarm(@Suppress("SameParameterValue") guid: String) = Alarm(
        alarmTimeInMin = 10,
        day = 2,
        displayTime = 0,
        guid = guid,
        sessionTitle = "Title",
        startTime = 1536332400000L,
        timeText = "Lorem ipsum"
    )

}
