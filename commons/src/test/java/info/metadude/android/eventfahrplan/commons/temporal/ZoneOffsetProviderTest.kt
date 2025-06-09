package info.metadude.android.eventfahrplan.commons.temporal

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZoneOffset.UTC

class ZoneOffsetProviderTest {

    @Nested
    inner class DeviceZoneOffset {

        @Test
        fun `getAvailableZoneOffset returns the device zone offset when useDeviceTimeZone = true regardless if sessionZoneOffset = null`() {
            val zoneOffset = getAvailableZoneOffset(
                deviceZoneOffset = UTC,
                sessionZoneOffset = null,
                useDeviceTimeZone = true,
            )
            assertThat(zoneOffset).isEqualTo(UTC)
        }

        @Test
        fun `getAvailableZoneOffset returns the device zone offset when useDeviceTimeZone = true regardless if sessionZoneOffset is set`() {
            val zoneOffset = getAvailableZoneOffset(
                deviceZoneOffset = UTC,
                sessionZoneOffset = ZoneOffset.ofHours(2),
                useDeviceTimeZone = true,
            )
            assertThat(zoneOffset).isEqualTo(UTC)
        }

        @Test
        fun `getAvailableZoneOffset returns the device zone offset as a fallback when sessionZoneOffset = null, although useDeviceTimeZone = false`() {
            val zoneOffset = getAvailableZoneOffset(
                deviceZoneOffset = UTC,
                sessionZoneOffset = null,
                useDeviceTimeZone = false,
            )
            assertThat(zoneOffset).isEqualTo(UTC)
        }

        @Test
        fun `getAvailableZoneOffset returns the device zone offset when useDeviceTimeZone = false and deviceZoneOffset == sessionZoneOffset`() {
            val zoneOffset = getAvailableZoneOffset(
                deviceZoneOffset = ZoneOffset.ofHours(-7),
                sessionZoneOffset = ZoneOffset.ofHours(-7),
                useDeviceTimeZone = false,
            )
            assertThat(zoneOffset).isEqualTo(ZoneOffset.ofHours(-7))
        }

    }

    @Nested
    inner class SessionZoneOffset {

        @Test
        fun `getAvailableZoneOffset returns the session zone offset when useDeviceTimeZone = false and sessionZoneOffset is set`() {
            val zoneOffset = getAvailableZoneOffset(
                deviceZoneOffset = UTC,
                sessionZoneOffset = ZoneOffset.ofHours(2),
                useDeviceTimeZone = false,
            )
            assertThat(zoneOffset).isEqualTo(ZoneOffset.ofHours(2))
        }

    }

}

private fun getAvailableZoneOffset(
    deviceZoneOffset: ZoneOffset,
    sessionZoneOffset: ZoneOffset?,
    useDeviceTimeZone: Boolean,
): ZoneOffset {
    // The instant is not relevant for these tests, we care about the zone offset.
    val clock = Clock.fixed(Instant.EPOCH, deviceZoneOffset)
    return ZoneOffsetProvider(clock, useDeviceTimeZone).getAvailableZoneOffset(sessionZoneOffset)
}
