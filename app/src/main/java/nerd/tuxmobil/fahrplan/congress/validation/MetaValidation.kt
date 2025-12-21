package nerd.tuxmobil.fahrplan.congress.validation

import info.metadude.android.eventfahrplan.network.models.Meta
import org.threeten.bp.DateTimeException
import org.threeten.bp.ZoneId
import org.threeten.bp.zone.ZoneRulesException

/**
 * Validation routines to ensure that data received from the network contains values which can
 * actually processed further.
 */
object MetaValidation {

    /**
     * Returns a [Meta] object which has its [timeZoneName][Meta.timeZoneName] successfully
     * validated or set to `null`.
     */
    fun Meta.validate(): Meta {
        val timeZoneNameIsValid = try {
            ZoneId.of(timeZoneName)
            true
        } catch (_: NullPointerException) {
            false
        } catch (_: DateTimeException) {
            false
        } catch (_: ZoneRulesException) {
            false
        }
        return if (timeZoneNameIsValid) this else copy(timeZoneName = null)
    }

}
