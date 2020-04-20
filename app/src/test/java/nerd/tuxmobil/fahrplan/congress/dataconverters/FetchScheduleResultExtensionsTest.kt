package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.net.HttpStatus as AppHttpStatus
import info.metadude.android.eventfahrplan.network.fetching.HttpStatus as NetworkHttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import info.metadude.android.eventfahrplan.network.fetching.FetchScheduleResult as NetworkFetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult as AppFetchScheduleResult

class FetchScheduleResultExtensionsTest {

    @Test
    fun networkFetchScheduleResult_toAppFetchScheduleResult_toNetworkFetchScheduleResult() {
        val fetchScheduleResult = NetworkFetchScheduleResult(
                httpStatus = NetworkHttpStatus.HTTP_NOT_MODIFIED,
                scheduleXml = "",
                eTag = "mno456",
                hostName = "example.com",
                exceptionMessage = "SSLException"
        )
        assertThat(fetchScheduleResult.toAppFetchScheduleResult().toNetworkFetchScheduleResult()).isEqualTo(fetchScheduleResult)
    }

    @Test
    fun networkFetchScheduleResult_toAppFetchScheduleResult() {
        val networkFetchScheduleResult = NetworkFetchScheduleResult(
                httpStatus = NetworkHttpStatus.HTTP_NOT_MODIFIED,
                scheduleXml = "<xml></xml>",
                eTag = "mno456",
                hostName = "example.com",
                exceptionMessage = "SSLException"
        )
        val appFetchScheduleResult = AppFetchScheduleResult(
                httpStatus = AppHttpStatus.HTTP_NOT_MODIFIED,
                eTag = "mno456",
                hostName = "example.com",
                exceptionMessage = "SSLException"
        )
        assertThat(networkFetchScheduleResult.toAppFetchScheduleResult()).isEqualTo(appFetchScheduleResult)
    }

}
