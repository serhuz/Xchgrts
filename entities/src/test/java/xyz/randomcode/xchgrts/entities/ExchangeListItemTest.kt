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

package xyz.randomcode.xchgrts.entities

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExchangeListItemTest {

    private lateinit var item: ExchangeListItem


    @Before
    fun setUp() {
        item = ExchangeListItem(
            letterCode = "AAA",
            units = 0,
            amount = "0.00",
            flagRes = 0,
            date = "01.01.2050"
        )
    }

    @Test
    fun shortDateIsCorrect() {
        val actual = item.shortDate

        assertEquals(actual, "01.01")
    }
}
