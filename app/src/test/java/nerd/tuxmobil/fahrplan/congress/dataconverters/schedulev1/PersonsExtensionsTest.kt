@file:OptIn(ExperimentalUuidApi::class)

package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev1

import com.google.common.truth.Truth.assertThat
import info.metadude.kotlin.library.schedule.v1.models.Person
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PersonsExtensionsTest {

    companion object {

        private fun scenarioOf(persons: List<Person>, expectedSpeakers: String) =
            of(persons, expectedSpeakers)

        private fun personOf(
            name: String,
            publicName: String?,
        ) = Person(
            guid = Uuid.parse("00000000-0000-0000-0000-000000000007"),
            name = name,
            publicName = publicName,
        )

        @JvmStatic
        fun data() = listOf(
            scenarioOf(
                persons = emptyList(),
                expectedSpeakers = "",
            ),
            scenarioOf(
                persons = listOf(personOf(name = "", publicName = null)),
                expectedSpeakers = "",
            ),
            scenarioOf(
                persons = listOf(personOf(name = "Ada Lovelace", publicName = null)),
                expectedSpeakers = "Ada Lovelace",
            ),
            scenarioOf(
                persons = listOf(personOf(name = " Ada Lovelace ", publicName = null)),
                expectedSpeakers = "Ada Lovelace",
            ),
            scenarioOf(
                persons = listOf(personOf(
                    name = "Ada Lovelace",
                    publicName = "Countess Ada",
                )),
                expectedSpeakers = "Countess Ada",
            ),
            scenarioOf(
                persons = listOf(personOf(
                    name = "Ada Lovelace",
                    publicName = " Countess Ada ",
                )),
                expectedSpeakers = "Countess Ada",
            ),
            scenarioOf(
                persons = listOf(personOf(
                    name = "Ada Lovelace",
                    publicName = "   ",
                )),
                expectedSpeakers = "Ada Lovelace",
            ),
            scenarioOf(
                persons = listOf(
                    personOf(
                        name = "Ada Lovelace",
                        publicName = null,
                    ),
                    personOf(
                        name = "Grace Hopper",
                        publicName = "Rear Admiral Grace Hopper",
                    ),
                ),
                expectedSpeakers = "Ada Lovelace;Rear Admiral Grace Hopper",
            ),
            scenarioOf(
                persons = listOf(
                    personOf(
                        name = "",
                        publicName = null,
                    ),
                    personOf(
                        name = "Grace Hopper",
                        publicName = "Rear Admiral Grace Hopper",
                    ),
                ),
                expectedSpeakers = "Rear Admiral Grace Hopper",
            ),
        )
    }

    @ParameterizedTest(name = "{index}: persons = {0} -> speakers = {1}")
    @MethodSource("data")
    fun toDelimitedSpeakersString(
        persons: List<Person>,
        expectedSpeakers: String
    ) {
        assertThat(persons.toDelimitedSpeakersString()).isEqualTo(expectedSpeakers)
    }

}
