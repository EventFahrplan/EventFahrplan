package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.network.models.HttpHeader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import info.metadude.android.eventfahrplan.network.fetching.FetchScheduleResult as NetworkFetchScheduleResult
import info.metadude.android.eventfahrplan.network.fetching.HttpStatus as NetworkHttpStatus
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult as AppFetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus as AppHttpStatus

class FetchScheduleResultExtensionsTest {

    @Test
    fun networkFetchScheduleResult_toAppFetchScheduleResult() {
        val networkFetchScheduleResult = NetworkFetchScheduleResult(
                httpStatus = NetworkHttpStatus.HTTP_NOT_MODIFIED,
                scheduleXml = "<xml></xml>",
                httpHeader = HttpHeader(
                    eTag = "mno456",
                    lastModified = "2023-12-31T23:59:59+01:00",
                ),
                hostName = "example.com",
                exceptionMessage = "SSLException"
        )
        val appFetchScheduleResult = AppFetchScheduleResult(
                httpStatus = AppHttpStatus.HTTP_NOT_MODIFIED,
                hostName = "example.com",
                exceptionMessage = "SSLException"
        )
        assertThat(networkFetchScheduleResult
                .toAppFetchScheduleResult())
                .isEqualTo(appFetchScheduleResult)
    }

}
