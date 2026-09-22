package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev2

import com.google.common.truth.Truth.assertThat
import info.metadude.kotlin.library.schedule.v2.models.Participant
import info.metadude.kotlin.library.schedule.v2.models.ParticipantRole.UNKNOWN
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

class ParticipantsExtensionsTest {

    @Test
    fun `toDelimitedSpeakersString returns empty string when participants are missing`() {
        assertThat(emptyList<Participant>().toDelimitedSpeakersString()).isEmpty()
    }

    @Test
    fun `toDelimitedSpeakersString returns empty string when name is empty`() {
        val participant = createParticipant("")
        assertThat(listOf(participant).toDelimitedSpeakersString()).isEmpty()
    }

    @Test
    fun `toDelimitedSpeakersString returns sanitized name`() {
        val participant = createParticipant(" John Doe ")
        assertThat(listOf(participant).toDelimitedSpeakersString()).isEqualTo("John Doe")
    }

    @Test
    fun `toDelimitedSpeakersString returns sanitized names`() {
        val participant1 = createParticipant(" John Doe ")
        val participant2 = createParticipant("  Mia Moe ")
        val participants = listOf(participant1, participant2)
        assertThat(participants.toDelimitedSpeakersString()).isEqualTo("John Doe;Mia Moe")
    }

    private fun createParticipant(name: String) = Participant(
        guid = Uuid.parse("00000000-1111-1111-1111-000000000000"),
        name = name,
        role = UNKNOWN,
    )

}
