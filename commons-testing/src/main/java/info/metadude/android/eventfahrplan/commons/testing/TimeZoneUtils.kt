// SPDX-FileCopyrightText: 2023 johnjohndoe <https://github.com/johnjohndoe>
//
// SPDX-License-Identifier: Apache-2.0

@file:JvmName("TimeZoneUtils")

package info.metadude.android.eventfahrplan.commons.testing

import java.util.TimeZone

fun withTimeZone(temporaryTimeZoneId: String, block: () -> Unit) {
    val systemTimezone = TimeZone.getDefault()
    TimeZone.setDefault(TimeZone.getTimeZone(temporaryTimeZoneId))
    block()
    TimeZone.setDefault(systemTimezone)
}
