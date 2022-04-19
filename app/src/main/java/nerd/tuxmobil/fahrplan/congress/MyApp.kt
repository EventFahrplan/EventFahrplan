package nerd.tuxmobil.fahrplan.congress

import androidx.annotation.CallSuper
import androidx.multidex.MultiDexApplication
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ConferenceTimeFrame
import org.ligi.tracedroid.TraceDroid
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class MyApp : MultiDexApplication() {

    enum class TASKS {
        NONE, FETCH, PARSE
    }

    companion object {

        private val FIRST_DAY_START = getMilliseconds(
            "Europe/Paris",
            BuildConfig.SCHEDULE_FIRST_DAY_START_YEAR,
            BuildConfig.SCHEDULE_FIRST_DAY_START_MONTH,
            BuildConfig.SCHEDULE_FIRST_DAY_START_DAY
        )

        private val LAST_DAY_END = getMilliseconds(
            "Europe/Paris",
            BuildConfig.SCHEDULE_LAST_DAY_END_YEAR,
            BuildConfig.SCHEDULE_LAST_DAY_END_MONTH,
            BuildConfig.SCHEDULE_LAST_DAY_END_DAY
        )

        private fun getMilliseconds(timeZoneId: String, year: Int, month: Int, day: Int): Long {
            val zeroBasedMonth = month - 1
            val zone = TimeZone.getTimeZone(timeZoneId)
            return GregorianCalendar(zone).apply {
                set(year, zeroBasedMonth, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val conferenceTimeFrame = ConferenceTimeFrame(FIRST_DAY_START, LAST_DAY_END)

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
