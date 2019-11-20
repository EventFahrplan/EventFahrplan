package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.network.fetching.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import info.metadude.android.eventfahrplan.network.fetching.FetchScheduleResult as NetworkFetchScheduleResult

class FetchScheduleResultExtensionsTest {

    @Test
    fun networkFetchScheduleResult_toAppFetchScheduleResult_toNetworkFetchScheduleResult() {
        val fetchScheduleResult = NetworkFetchScheduleResult(
                httpStatus = HttpStatus.HTTP_NOT_MODIFIED,
                scheduleXml = "<xml></xml>",
                eTag = "mno456",
                hostName = "example.com",
                exceptionMessage = "SSLException"
        )
        assertThat(fetchScheduleResult.toAppFetchScheduleResult().toNetworkFetchScheduleResult()).isEqualTo(fetchScheduleResult)
    }

}
