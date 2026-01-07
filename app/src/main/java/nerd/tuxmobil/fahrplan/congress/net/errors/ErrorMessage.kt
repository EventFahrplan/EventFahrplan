package nerd.tuxmobil.fahrplan.congress.net.errors

import android.content.Context
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.API_KEY_INVALID
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.HOST_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.PATH_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.QUERY_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.SCHEME_MISSING
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParsingResult.Error.Type.URL_MALFORMED
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.ParseShiftsResult

sealed interface ErrorMessage {

    data class TitledMessage(val title: String, val message: String) : ErrorMessage
    data class SimpleMessage(val message: String) : ErrorMessage

    /**
     * Produces an abstract [ErrorMessage] which can be displayed as a dialog or toast.
     * This class intentionally separates the text creation from how it will be displayed.
     */
    class Factory(private val context: Context) {

        /**
         * Returns an [ErrorMessage] which corresponds to given [httpStatus]. The [hostName]
         * is included in the error message text if suitable.
         */
        fun getMessageForHttpStatus(httpStatus: HttpStatus, hostName: String): ErrorMessage =
            when (httpStatus) {
                HttpStatus.HTTP_DNS_FAILURE -> TitledMessage(
                    context.getString(R.string.dlg_err_connection_failed),
                    context.getString(R.string.dlg_err_failed_unknown_host, hostName)
                )
                HttpStatus.HTTP_WRONG_HTTP_CREDENTIALS -> TitledMessage(
                    context.getString(R.string.dlg_err_connection_failed),
                    context.getString(R.string.dlg_err_failed_wrong_http_credentials)
                )
                HttpStatus.HTTP_CONNECT_TIMEOUT -> TitledMessage(
                    context.getString(R.string.dlg_err_connection_failed),
                    context.getString(R.string.dlg_err_failed_timeout)
                )
                HttpStatus.HTTP_COULD_NOT_CONNECT -> TitledMessage(
                    context.getString(R.string.dlg_err_connection_failed),
                    context.getString(R.string.dlg_err_failed_connect_failure)
                )
                HttpStatus.HTTP_CANNOT_PARSE_CONTENT -> TitledMessage(
                    context.getString(R.string.dlg_err_connection_failed),
                    context.getString(R.string.dlg_err_failed_parse_failure)
                )
                HttpStatus.HTTP_NOT_MODIFIED -> SimpleMessage(
                    context.getString(R.string.uptodate)
                )
                HttpStatus.HTTP_NOT_FOUND -> TitledMessage(
                    context.getString(R.string.dlg_err_connection_failed),
                    context.getString(R.string.dlg_err_failed_not_found)
                )
                HttpStatus.HTTP_CLEARTEXT_NOT_PERMITTED -> TitledMessage(
                    context.getString(R.string.dlg_err_connection_failed),
                    context.getString(R.string.dlg_err_failed_http_cleartext_not_permitted)
                )
                else -> error("Unknown HTTP status = $httpStatus, host name = $hostName")
            }

        /**
         * Returns a [TitledMessage] indicating that the loaded schedule does not contain any sessions
         * to be displayed. If present the [scheduleVersion] is included in the error message.
         */
        fun getMessageForEmptySchedule(scheduleVersion: String): TitledMessage = when {
            scheduleVersion.isEmpty() -> TitledMessage(
                context.getString(R.string.dlg_err_schedule_data),
                context.getString(R.string.dlg_err_schedule_data_empty_without_version),
            )
            else -> TitledMessage(
                context.getString(R.string.dlg_err_schedule_data),
                context.getString(R.string.dlg_err_schedule_data_empty, scheduleVersion),
            )
        }

        /**
         * Returns the [TitledMessage] based on the given [message].
         */
        fun getCertificateMessage(message: String): TitledMessage = TitledMessage(
            context.getString(R.string.certificate_error_title),
            context.getString(R.string.certificate_error_message, message),
        )

        /**
         * Returns an [ErrorMessage] derived from the information passed with the [parseResult].
         */
        fun getMessageForParsingResult(parseResult: ParseResult): ErrorMessage {
            val message = when (parseResult) {
                is ParseScheduleResult -> getMessageForScheduleVersion(parseResult.version)
                is ParseShiftsResult.Error -> getMessageForErrorResult(parseResult)
                is ParseShiftsResult.Exception -> context.getString(R.string.engelsystem_shifts_parsing_error_generic, context.getString(R.string.engelsystem_alias))
                else -> ""
            }
            check(message.isNotEmpty()) { "Unknown parsing result: $parseResult" }
            return SimpleMessage(message)
        }

        /**
         * Returns a [TitledMessage] based on the given [error].
         */
        fun getMessageForEngelsystemUrlError(error: EngelsystemUriParsingResult.Error): TitledMessage {
            val url = error.url
            val message = when (error.type) {
                URL_MALFORMED -> context.getString(R.string.engelsystem_url_error_url, url)
                SCHEME_MISSING -> context.getString(R.string.engelsystem_url_error_scheme, url)
                HOST_MISSING -> context.getString(R.string.engelsystem_url_error_host, url)
                PATH_MISSING -> context.getString(R.string.engelsystem_url_error_path, url)
                QUERY_MISSING -> context.getString(R.string.engelsystem_url_error_query, url)
                API_KEY_INVALID -> context.getString(R.string.engelsystem_url_error_api_key, url)
            }
            val title = context.getString(R.string.engelsystem_url_error_title, context.getString(R.string.engelsystem_alias))
            return TitledMessage(title, message)
        }

        private fun getMessageForScheduleVersion(scheduleVersion: String) = when {
            scheduleVersion.isEmpty() -> context.getString(R.string.schedule_parsing_error_generic)
            else -> context.getString(R.string.schedule_parsing_error_with_version, scheduleVersion)
        }

        private fun getMessageForErrorResult(errorResult: ParseShiftsResult.Error) = when {
            errorResult.isForbidden() -> context.getString(R.string.engelsystem_shifts_parsing_error_forbidden, context.getString(R.string.engelsystem_alias))
            errorResult.isNotFound() -> context.getString(R.string.engelsystem_shifts_parsing_error_not_found, context.getString(R.string.engelsystem_alias))
            else -> context.getString(R.string.engelsystem_shifts_parsing_error_generic, context.getString(R.string.engelsystem_alias))
        }
    }

}