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

import com.nhaarman.mockitokotlin2.*
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.equalToIgnoringCase
import org.junit.Assume.assumeThat
import org.junit.Test
import xyz.randomcode.xchgrts.domain.R
import xyz.randomcode.xchgrts.entities.CurrencyCode
import xyz.randomcode.xchgrts.entities.CurrencyListItem
import java.util.*

class CurrencyInfoProviderTest {

    private val flagProvider: FlagResourceProvider = mock {
        on { fallbackIcon() } doReturn R.drawable.globe
        on { getFlag(any()) } doReturn R.drawable.globe
    }

    private val provider = CurrencyInfoProvider(Locale.ENGLISH, flagProvider)

    @Test
    fun getFlagIcon() {
        val actual = provider.flagRes.get("EUR")

        verify(flagProvider).getFlag(eq("EU"))
        assumeThat(actual, equalTo(R.drawable.globe))
    }

    @Test
    fun getCurrencyName() {
        val actual = provider.currencyName.get("USD")

        assumeThat(actual, equalToIgnoringCase("US Dollar"))
    }

    @Test
    fun getListItem() {
        val expected = CurrencyListItem("826", "GBP", "British Pound", R.drawable.globe)

        val actual = provider.listItem.get(CurrencyCode("826", "GBP"))

        verify(flagProvider).getFlagResourceForCurrency(eq("GB"))
        assumeThat(actual, equalTo(expected))
    }
}
