/*
 * Copyright 2025 Sergei Munovarov
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

import org.junit.Assert.assertEquals
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.time.Instant

class DateProviderTest {

    @Test
    fun currentDateIsCorrect() {
        val instant = Instant.parse("2020-01-01T01:00:00+02:00")
        val provider = DateProvider(instant)

        val date = provider.currentDate

        assertEquals(date.requestFormat, "01.01.2020")
        assertEquals(date.entityFormat, "01.01.2020")
    }

    @Test
    fun calculateMinutesToMidnight() {
        val instant = Instant.parse("2020-01-01T01:00:00+02:00")
        val provider = DateProvider(instant)

        val actual = provider.minutesToMidnight

        expectThat(actual).isEqualTo(23 * 60 /* 23 hours */)
    }
}
