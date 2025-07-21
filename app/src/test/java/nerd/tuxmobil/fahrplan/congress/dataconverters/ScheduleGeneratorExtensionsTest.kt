package nerd.tuxmobil.fahrplan.congress.dataconverters

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import info.metadude.android.eventfahrplan.database.models.ScheduleGenerator as ScheduleGeneratorDatabaseModel
import info.metadude.android.eventfahrplan.network.models.ScheduleGenerator as ScheduleGeneratorNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.ScheduleGenerator as ScheduleGeneratorAppModel

class ScheduleGeneratorExtensionsTest {

    @Nested
    inner class AppToNetwork {

        @Test
        fun `toScheduleGeneratorNetworkModel returns model with both non-null properties`() {
            val actual = ScheduleGeneratorAppModel(
                name = "pretalx",
                version = "1.0.0",
            ).toScheduleGeneratorNetworkModel()
            val expected = ScheduleGeneratorNetworkModel(
                name = "pretalx",
                version = "1.0.0",
            )
            assertThat(actual).isEqualTo(expected)
        }

        @Test
        fun `toScheduleGeneratorNetworkModel returns model with both properties empty`() {
            val actual = ScheduleGeneratorAppModel(
                name = "",
                version = "",
            ).toScheduleGeneratorNetworkModel()
            val expected = ScheduleGeneratorNetworkModel(
                name = "",
                version = "",
            )
            assertThat(actual).isEqualTo(expected)
        }

    }

    @Nested
    inner class DatabaseToApp {

        @Test
        fun `toScheduleGeneratorAppModel returns model with both non-null properties`() {
            val actual = ScheduleGeneratorDatabaseModel(
                name = "pretalx",
                version = "1.0.0",
            ).toScheduleGeneratorAppModel()
            val expected = ScheduleGeneratorAppModel(
                name = "pretalx",
                version = "1.0.0",
            )
            assertThat(actual).isEqualTo(expected)
        }

        @Test
        fun `toScheduleGeneratorAppModel returns null if name is null`() {
            val actual = ScheduleGeneratorDatabaseModel(
                name = null,
                version = "1.0.0",
            ).toScheduleGeneratorAppModel()
            assertThat(actual).isEqualTo(null)
        }

        @Test
        fun `toScheduleGeneratorAppModel returns null if version is null`() {
            val actual = ScheduleGeneratorDatabaseModel(
                name = "pretalx",
                version = null,
            ).toScheduleGeneratorAppModel()
            assertThat(actual).isEqualTo(null)
        }

    }

    @Nested
    inner class NetworkToDatabase {

        @Test
        fun `toScheduleGeneratorDatabaseModel returns model with both properties`() {
            val actual = ScheduleGeneratorNetworkModel(
                name = "pretalx",
                version = "1.0.0",
            ).toScheduleGeneratorDatabaseModel()
            val expected = ScheduleGeneratorDatabaseModel(
                name = "pretalx",
                version = "1.0.0",
            )
            assertThat(actual).isEqualTo(expected)
        }

        @Test
        fun `toScheduleGeneratorDatabaseModel returns model with both properties empty`() {
            val actual = ScheduleGeneratorNetworkModel(
                name = "",
                version = "",
            ).toScheduleGeneratorDatabaseModel()
            val expected = ScheduleGeneratorDatabaseModel(
                name = "",
                version = "",
            )
            assertThat(actual).isEqualTo(expected)
        }

        @Test
        fun `toScheduleGeneratorDatabaseModel returns model with both null properties`() {
            val actual = ScheduleGeneratorNetworkModel(
                name = null,
                version = null,
            ).toScheduleGeneratorDatabaseModel()
            val expected = ScheduleGeneratorDatabaseModel(
                name = null,
                version = null,
            )
            assertThat(actual).isEqualTo(expected)
        }

    }

}