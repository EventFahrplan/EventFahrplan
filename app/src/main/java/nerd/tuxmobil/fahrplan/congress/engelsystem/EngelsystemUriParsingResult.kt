package nerd.tuxmobil.fahrplan.congress.engelsystem

sealed interface EngelsystemUriParsingResult {

    data object Empty : EngelsystemUriParsingResult

    data class Parsed(val uri: EngelsystemUri) : EngelsystemUriParsingResult

    data class Error(val type: Type, val url: String) : EngelsystemUriParsingResult {

        enum class Type {
            URL_MALFORMED,
            SCHEME_MISSING,
            HOST_MISSING,
            PATH_MISSING,
            QUERY_MISSING,
            API_KEY_INVALID,
        }

    }
}
