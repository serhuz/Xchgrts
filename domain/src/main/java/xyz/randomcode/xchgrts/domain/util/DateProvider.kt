/*
 * Copyright 2021 Sergei Munovarov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.randomcode.xchgrts.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DateProvider(private val initialInstant: Instant = Clock.System.now()) {

    val currentDate: String
        get() = initialInstant.toLocalDateTime(ZONE_UA)
            .let {
                val day = "${it.dayOfMonth}".padStart(2, '0')
                val month = "${it.monthNumber}".padStart(2, '0')
                val year = "${it.year}"
                "$day.$month.$year"
            }

    val minutesToMidnight: Int
        get() = initialInstant.toLocalDateTime(ZONE_UA)
            .let { (24 * 60) - (it.hour * 60 + it.minute) }


    companion object {
        private const val UA = "Europe/Kiev"
        private val ZONE_UA = TimeZone.of(UA)
    }
}
