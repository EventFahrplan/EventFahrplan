package nerd.tuxmobil.fahrplan.congress

import android.app.Application
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ConferenceTimeFrame
import org.ligi.tracedroid.TraceDroid
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime


class MyApp : Application() {

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

        @VisibleForTesting
        fun getMilliseconds(timeZoneId: String, year: Int, month: Int, day: Int): Long {
            val zoneId = ZoneId.of(timeZoneId)
            val localDate = LocalDate.of(year, month, day)
            val zonedDateTime = ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, zoneId)
            return zonedDateTime.toInstant().toEpochMilli()
        }

        val conferenceTimeFrame = ConferenceTimeFrame(
            Moment.ofEpochMilli(FIRST_DAY_START),
            Moment.ofEpochMilli(LAST_DAY_END)
        )

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
