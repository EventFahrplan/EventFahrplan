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
            "ART-AND-PLAY INSTALLATION" to "artandplay",
            "ART-AND-PLAY STAGE" to "ap-stage",
            "ASSEMBLY:ANARCHIST VILLAGE" to "anarchist-assembly",
            "ASSEMBLY:BACKSPACE" to "backspace",
            "ASSEMBLY:BLIND NAVIGATION WORKSHOP" to "blindnavigation",
            "ASSEMBLY:C-BASE / CCC-B / XHAIN" to "c-base-workshop",
            "ASSEMBLY:C3BLIND" to "c3blind",
            "ASSEMBLY:CHAOS WEST" to "cw-stage",
            "ASSEMBLY:CHAOSZONE" to "chaoszone",
            "ASSEMBLY:CRYPTO CURRENCY ASSEMBLY / EMBASSY" to "crypto-currency",
            "ASSEMBLY:CURRY CLUB AUGSBURG" to "openlab-augsburg",
            "ASSEMBLY:FOODHACKINGBASE" to "fhb",
            "ASSEMBLY:FOSSASIA" to "fossasia",
            "ASSEMBLY:FREE SOFTWARE FOUNDATION EUROPE" to "fsfe",
            "ASSEMBLY:HAECKSEN" to "haecksen",
            "ASSEMBLY:HARDWARE HACKING AREA" to "hardwarehackingarea",
            "ASSEMBLY:HSBE" to "hsbe",
            "ASSEMBLY:ICMP" to "icmp",
            "ASSEMBLY:MILLIWAYS" to "milliways",
            "ASSEMBLY:MOIN - MEHRERE ORTE IM NORDEN" to "moin",
            "ASSEMBLY:NIBBLE AREA" to "nibble",
            "ASSEMBLY:NIXOS" to "nixos",
            "ASSEMBLY:OPEN INFRASTRUCTURE ORBIT" to "oio",
            "ASSEMBLY:PSEUDOROOM" to "pseudoroom",
            "ASSEMBLY:SPACE IN FRONT OF M1" to "mh-hall-m1-m2-foyer",
            "ASSEMBLY:VINTAGE COMPUTING" to "vintage",
            "ASSEMBLY:WIKIPAKAWG" to "wikipaka-wg",
            "BÜHNE" to null,
            "C-BASE" to "c-base",
            "CCL HALL 3" to "dlf-sendezentrum",
            "CCL SAAL 3" to "ccl-hall-3",
            "CCL SAAL 3" to "dlf-sendezentrum",
            "CCL TERRACE" to "ccl-terrace",
            "CDC - STAGE" to "cdc-stage",
            "CDC - WORKSHOP AREA" to "cdc",
            "CHAOS-WEST BÜHNE" to "cw-stage",
            "CHAOSZONE BÜHNE" to "chaoszone-stage",
            "CHAOSZONE WORKSHOP" to "cz-workshop",
            "CHAOSZONE" to "chaoszone",
            "COMPEILER" to "compeiler",
            "DEZENTRALE*" to "dezentrale",
            "DISCODRAMA" to "discodrama",
            "DLF- UND PODCAST-BÜHNE" to "dlf-sendezentrum",
            "FOSSASIA WORKSHOPS - CDC" to "fossasia",
            "HACKERS BEAUTY PALACE" to "hbp",
            "HEADNUT" to "komona-headnut",
            "KIDSPACE" to "kidspace",
            "LECTURE ROOM 11" to "self-organized-sessions-11",
            "LECTURE ROOM M1" to "self-organized-sessions-m1",
            "LECTURE ROOM M2" to "self-organized-sessions-m2",
            "LECTURE ROOM M3" to "self-organized-sessions-m3",
            "MONIPILAMI" to "monipilami",
            "NOKINGDOME" to "komona-nokingdome",
            "OIO LECTURE ARENA" to "oio-arena",
            "OIO LÖTAREA" to "oio-soldering",
            "OIO SOLDER AREA" to "oio-workshop",
            "OIO STAGE" to "oio-stage",
            "OIO THEMENTISCH 1" to "oio-table1",
            "OIO THEMENTISCH 2" to "oio-table2",
            "OIO THEMENTISCH 3" to "oio-table3",
            "OIO THEMENTISCH 4" to "oio-table4",
            "OIO THEMENTISCH 6" to "oio-table6",
            "OIO VORTRAGS-ARENA" to "oio-arena",
            "OIO WORKSHOP" to "oio-workshop-dome",
            "OIO WORKSHOP-DOMO" to "oio-workshop-dome",
            "SEMINAR ROOM 13" to "self-organized-sessions-13",
            "SEMINAR ROOM 14-15" to "self-organized-sessions-14-15",
            "SENDETISCH" to "sendetisch",
            "SHUTTER ISLAND" to "shutter-island",
            "SOUPWORX" to "komona-soupworx",
            "STUDIO DATSCHE" to "studio-datscha",
            "UPTIME BAR" to "uptime-bar",
            "VINTAGE COMPUTING CLUSTER" to "vintage",
            "WIKIPAKA WG: BIBLIOTHEK" to "wikipaka-library",
            "WIKIPAKA WG: ESSZIMMER" to "wikipaka-dining"
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
