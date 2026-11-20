package nerd.tuxmobil.fahrplan.congress.settings

import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.models.NextFetch
import nerd.tuxmobil.fahrplan.congress.preferences.Settings
import nerd.tuxmobil.fahrplan.congress.preferences.SettingsRepository
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.LaunchSessionAlarmChannelSettingsScreen
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.PickAlarmTone
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.AlarmToneClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ScheduleChangesFavoritesFilterClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.SetAlarmTone
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class SettingsViewModelTest {

    @Test
    fun `onViewEvent(AlarmToneClicked) emits LaunchSessionAlarmChannelSettingsScreen if notification channels are supported`() =
        runTest {
            val viewModel = createViewModel(isNotificationChannelSupported = true)
            viewModel.effects.test {
                viewModel.onViewEvent(AlarmToneClicked)
                assertThat(awaitItem()).isEqualTo(LaunchSessionAlarmChannelSettingsScreen)
            }
        }

    @Test
    fun `onViewEvent(AlarmToneClicked) emits PickAlarmTone with the current alarm tone if notification channels are not supported`() =
        runTest {
            val alarmTone = mock<Uri>()
            val viewModel = createViewModel(
                isNotificationChannelSupported = false,
                settingsStream = flowOf(Settings(alarmTone = alarmTone)),
            )
            viewModel.uiState.test {
                // Wait until the settings have been collected.
                assertThat(expectMostRecentItem().settings.alarmTone).isEqualTo(alarmTone)
                viewModel.effects.test {
                    viewModel.onViewEvent(AlarmToneClicked)
                    assertThat(awaitItem()).isEqualTo(PickAlarmTone(alarmTone))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onViewEvent(SetAlarmTone) persists the alarm tone`() = runTest {
        val alarmTone = mock<Uri>()
        val settingsRepository = createSettingsRepository()
        val viewModel = createViewModel(settingsRepository = settingsRepository)
        viewModel.onViewEvent(SetAlarmTone(alarmTone))
        verifyInvokedOnce(settingsRepository).setAlarmTone(alarmTone)
    }

    @Test
    fun `onViewEvent(ScheduleChangesFavoritesFilterClicked) persists the inverted value`() = runTest {
        val settingsRepository = createSettingsRepository(
            settingsStream = flowOf(Settings(isScheduleChangesFavoritesFilterEnabled = true)),
        )
        val viewModel = createViewModel(settingsRepository = settingsRepository)
        viewModel.uiState.test {
            // Wait until the settings have been collected.
            assertThat(expectMostRecentItem().settings.isScheduleChangesFavoritesFilterEnabled).isTrue()
            viewModel.onViewEvent(ScheduleChangesFavoritesFilterClicked)
            verifyInvokedOnce(settingsRepository).setScheduleChangesFavoritesFilter(false)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(
        isNotificationChannelSupported: Boolean = true,
        settingsStream: Flow<Settings> = emptyFlow(),
        settingsRepository: SettingsRepository = createSettingsRepository(settingsStream),
    ) = SettingsViewModel(
        appRepository = createAppRepository(),
        settingsRepository = settingsRepository,
        scheduleNextFetchUpdater = mock(),
        isNotificationChannelSupported = isNotificationChannelSupported,
    )

    private fun createAppRepository() = mock<AppRepository> {
        on { scheduleNextFetch } doReturn flowOf(NextFetch.INVALID)
    }

    private fun createSettingsRepository(
        settingsStream: Flow<Settings> = emptyFlow(),
    ) = mock<SettingsRepository> {
        on { this.settingsStream } doReturn settingsStream
    }

}
