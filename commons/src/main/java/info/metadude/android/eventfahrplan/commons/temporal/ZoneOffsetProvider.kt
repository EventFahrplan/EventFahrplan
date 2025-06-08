package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.Clock
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

internal class ZoneOffsetProvider(
    val clock: Clock,
    val useDeviceTimeZone: Boolean,
) {

    /**
     * Returns the available zone offset - either the given [sessionZoneOffset] or the zone offset
     * of the device. The user can overrule the logic by setting [useDeviceTimeZone].
     */
    fun getAvailableZoneOffset(sessionZoneOffset: ZoneOffset?): ZoneOffset {
        val deviceZoneOffset = OffsetDateTime.now(clock).offset
        val useDeviceZoneOffset = sessionZoneOffset == null || sessionZoneOffset == deviceZoneOffset
        return if (useDeviceTimeZone || useDeviceZoneOffset) deviceZoneOffset else sessionZoneOffset
    }

}
