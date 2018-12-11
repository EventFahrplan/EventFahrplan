package nerd.tuxmobil.fahrplan.congress.navigation

object RoomForC3NavConverter {

    private const val EMPTY_STRING = ""

    private val ROOM_TO_C3NAV_MAPPING = mapOf(
            "SAAL ADAMS" to "hall-a",
            "SAAL BORG" to "hall-b",
            "SAAL CLARKE" to "hall-c",
            "SAAL DIJKSTRA" to "hall-d",

            // From everything.schedule.xml
            "ASSEMBLY:CHAOS WEST" to "chaos-west-stage",
            "ASSEMBLY:BOGONAUTEN" to null,
            "ASSEMBLY:HAECKSEN" to null,
            "ASSEMBLY:HARDWAREHACKINGAREA" to "hardware-hacking-area",
            "ASSEMBLY:JUGEND HACKT" to "jugend-hackt",
            "ASSEMBLY:MILLIWAYS" to "milliways",
            "ASSEMBLY:OPEN KNOWLEDGE ASSEMBLY" to null,
            "ASSEMBLY:PHYSIKFACHSCHAFT ROSTOCK" to null,
            "ASSEMBLY:TEAHOUSE" to null,
            "CCL HALL 3" to "ccl-hall-3",
            "CHAOS WEST STAGE" to "chaos-west-stage",
            "HALL 3" to "ccl-hall-3",
            "HIVE STAGE" to "the-hive-stage",
            "KIDSPACE" to "kidspace",
            "KOMONA AQUARIUS" to "komona-aquarius",
            "KOMONA BLUE PRINCESS" to "komona-blue-princess",
            "KOMONA CORAL REEF" to "komona-coral-reef",
            "KOMONA D.RESSROSA" to "komona-d-ressrosa",
            "LECTURE ROOM 11" to "ccl-lecture-room-11",
            "LECTURE ROOM 12" to "ccl-lecture-room-12",
            "SEMINAR ROOM 13" to "ccl-seminar-room-13",
            "SEMINAR ROOM 14-15" to "ccl-seminar-room-13"
    )

    @JvmStatic
    fun convert(room: String?) = when {
        room != null && EMPTY_STRING != room -> {
            val c3navName = ROOM_TO_C3NAV_MAPPING[room.toUpperCase()]
            c3navName ?: EMPTY_STRING
        }
        else -> EMPTY_STRING
    }

}
