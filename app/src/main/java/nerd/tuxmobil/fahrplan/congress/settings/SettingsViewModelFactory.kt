package nerd.tuxmobil.fahrplan.congress.settings

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nerd.tuxmobil.fahrplan.congress.preferences.SettingsRepository
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

class SettingsViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            appRepository = AppRepository,
            settingsRepository = SettingsRepository.getInstance(context),
            scheduleNextFetchUpdater = ScheduleNextFetchUpdater.newInstance(context),
            isNotificationChannelSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
        ) as T
    }
}
