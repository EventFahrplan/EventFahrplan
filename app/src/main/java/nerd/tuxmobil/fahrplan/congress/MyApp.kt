package nerd.tuxmobil.fahrplan.congress

import android.app.Application
import androidx.annotation.CallSuper
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.ligi.tracedroid.TraceDroid


class MyApp : Application() {

    enum class TASKS {
        NONE, FETCH, PARSE
    }

    companion object {

        @JvmField
        var taskRunning = TASKS.NONE

    }

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        TraceDroid.init(this)
        taskRunning = TASKS.NONE
        AppRepository.initialize(
            context = applicationContext,
            logging = Logging.get()
        )
    }

}
