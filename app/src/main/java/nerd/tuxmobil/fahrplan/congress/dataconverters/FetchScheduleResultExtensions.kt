package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.network.fetching.FetchScheduleResult as NetworkFetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult as AppFetchScheduleResult

fun NetworkFetchScheduleResult.toAppFetchScheduleResult() = AppFetchScheduleResult(
        httpStatus = httpStatus.toAppHttpStatus(),
        eTag = eTag,
        hostName = hostName,
        exceptionMessage = exceptionMessage
)

fun AppFetchScheduleResult.toNetworkFetchScheduleResult() = NetworkFetchScheduleResult(
        httpStatus = httpStatus.toNetworkHttpStatus(),
        eTag = eTag,
        hostName = hostName,
        exceptionMessage = exceptionMessage
)
