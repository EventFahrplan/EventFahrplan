package nerd.tuxmobil.fahrplan.congress.navigation

object RoomForC3NavConverter {

    private const val EMPTY_STRING = ""

    private val ROOM_TO_C3NAV_MAPPING = mapOf(
            "ADA" to "hall-a",
            "BORG" to "hall-b",
            "CLARKE" to "hall-c",
            "DIJKSTRA" to "hall-d",
            "ELIZA" to "hall-e",

            // From everything.schedule.xml
            "ASSEMBLY:BLIND NAVIGATION WORKSHOP" to "blindnavigation",
            "ASSEMBLY:CHAOS WEST" to "chaos-west",
            "ASSEMBLY:CRYPTO CURRENCY ASSEMBLY / EMBASSY" to "crypto-currency",
            "ASSEMBLY:CURRY CLUB AUGSBURG" to "openlab-augsburg",
            "ASSEMBLY:FOODHACKINGBASE" to "foodhacking",
            "ASSEMBLY:FOSSASIA" to "fossasia",
            "ASSEMBLY:HARDWARE HACKING AREA" to "hardware-hacking",
            "ASSEMBLY:MOIN - MEHRERE ORTE IM NORDEN" to "moin",
            "BÜHNE" to null,
            "C-BASE" to "c-base",
            "CCL HALL 3" to "dlf-sendezentrum",
            "CCL SAAL 3" to "dlf-sendezentrum",
            "CHAOSZONE" to "chaoszone",
            "CHAOS WEST BÜHNE" to "chaos-west-stage",
            "CHAOS-WEST BÜHNE" to "chaos-west-stage",
            "CHAOS WEST STAGE" to "chaos-west-stage",
            "COMPEILER" to "compeiler",
            "DISCODRAMA" to "discodrama",
            "KIDSPACE" to "kidspace",
            "LECTURE ROOM 11" to "self-organized-sessions-11",
            "LECTURE ROOM M1" to "self-organized-sessions-m1",
            "LECTURE ROOM M2" to "self-organized-sessions-m2",
            "LECTURE ROOM M3" to "self-organized-sessions-m3",
            "OIO LECTURE ARENA" to "oio-arena",
            "OIO LÖTAREA" to "oio-soldering",
            "OIO THEMENTISCH 1" to "oio-table1",
            "OIO THEMENTISCH 2" to "oio-table2",
            "OIO THEMENTISCH 3" to "oio-table3",
            "OIO THEMENTISCH 4" to "oio-table4",
            "OIO THEMENTISCH 6" to "oio-table6",
            "OIO Stage" to "oio-arena",
            "OIO VORTRAGS-ARENA" to "oio-arena",
            "OIO WORKSHOP" to "oio-workshop-dome",
            "OIO WORKSHOP-DOMO" to "oio-workshop-dome",
            "SEMINAR ROOM 13" to "self-organized-sessions-13",
            "SEMINAR ROOM 14-15" to "self-organized-sessions-14-15",
            "SENDETISCH" to "sendetisch",
            "SHUTTER ISLAND" to "shutter-island",
            "VINTAGE COMPUTING CLUSTER" to "vintage-computing",
            "WIKIPAKAWG ESSZIMMER" to "wikipaka-dining",
            "WIKIPAKAWG WG: ESSZIMMER" to "wikipaka-dining",
            "WIKIPAKAWG KÜCHE" to "wikipaka-kitchen",
            "UPTIME BAR" to "uptime-bar"
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
