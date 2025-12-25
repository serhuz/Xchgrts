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

package xyz.randomcode.xchgrts.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import strikt.assertions.withFirst
import xyz.randomcode.xchgrts.domain.RateDataUseCase
import xyz.randomcode.xchgrts.entities.ExchangeListItem
import xyz.randomcode.xchgrts.entities.Failure
import xyz.randomcode.xchgrts.entities.Success
import xyz.randomcode.xchgrts.util.ErrorLogger
import xyz.randomcode.xchgrts.util.Prefs

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeRatesViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var logger: ErrorLogger

    @MockK
    lateinit var prefs: Prefs

    @MockK
    lateinit var case: RateDataUseCase

    private lateinit var viewModel: ExchangeRatesViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)

        viewModel = ExchangeRatesViewModel(
            prefs, case, logger, dispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearMocks(logger, prefs, case)
    }

    @Test
    fun loadRates() = runTest {
        every { prefs.favCurrencies } returns emptySet()
        val item1 = ExchangeListItem(
            letterCode = "AAA",
            units = 1,
            amount = "1.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val item2 = ExchangeListItem(
            letterCode = "BBB",
            units = 1,
            amount = "2.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val currencyList = listOf(item1, item2)
        coEvery { case.getRatesForDate(any()) } returns currencyList

        viewModel.loadRates()
        advanceUntilIdle()

        coVerify { case.getRatesForDate(any()) }

        val actualItems = viewModel.items.value
        expectThat(actualItems)
            .isNotNull()
            .isA<Success<List<RateListItem>>>()
            .get { data }
            .hasSize(2)

        val actualDate = viewModel.date.first()
        expectThat(actualDate).isEqualTo("01.01.2025")
    }

    @Test
    fun sortFavorites() = runTest {
        every { prefs.favCurrencies } returns setOf("CCC")
        val item1 = ExchangeListItem(
            letterCode = "AAA",
            units = 1,
            amount = "1.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val item2 = ExchangeListItem(
            letterCode = "BBB",
            units = 1,
            amount = "2.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val item3 = ExchangeListItem(
            letterCode = "CCC",
            units = 1,
            amount = "2.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val currencyList = listOf(item1, item2, item3)
        coEvery { case.getRatesForDate(any()) } returns currencyList

        viewModel.loadRates()
        advanceUntilIdle()

        val actualItems = viewModel.items.value
        expectThat(actualItems)
            .isNotNull()
            .isA<Success<List<RateListItem>>>()
            .get { data }
            .hasSize(3)
            .withFirst {
                get { isFavorite }.isTrue()
                get { data }.isEqualTo(item3)
            }
    }

    @Test
    fun handleLoadError() = runTest {
        every { prefs.favCurrencies } returns emptySet()
        coEvery { case.getRatesForDate(any()) } throws RuntimeException("test")

        viewModel.loadRates()
        advanceUntilIdle()

        val actual = viewModel.items.value
        expectThat(actual)
            .isNotNull()
            .isA<Failure>()
    }

    @Test
    fun addToFavorites() = runTest {
        every { prefs.favCurrencies } returns emptySet()
        val favSlot = slot<Set<String>>()
        every { prefs.favCurrencies = capture(favSlot) } answers {}
        val item1 = ExchangeListItem(
            letterCode = "AAA",
            units = 1,
            amount = "1.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val item2 = ExchangeListItem(
            letterCode = "BBB",
            units = 1,
            amount = "2.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val item3 = ExchangeListItem(
            letterCode = "CCC",
            units = 1,
            amount = "2.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val currencyList = listOf(item1, item2, item3)
        coEvery { case.getRatesForDate(any()) } returns currencyList

        viewModel.loadRates()
        advanceUntilIdle()

        expectThat(viewModel.items.value)
            .isNotNull()
            .isA<Success<List<RateListItem>>>()
            .get { data }
            .hasSize(3)
            .withFirst {
                get { isFavorite }.isFalse()
                get { data }.isEqualTo(item1)
            }

        viewModel.updateFavorites("CCC")
        advanceUntilIdle()

        val actualItems = viewModel.items.value
        expectThat(actualItems)
            .isNotNull()
            .isA<Success<List<RateListItem>>>()
            .get { data }
            .hasSize(3)
            .withFirst {
                get { isFavorite }.isTrue()
                get { data }.isEqualTo(item3)
            }

        expectThat(favSlot.captured).hasSize(1).contains("CCC")
    }

    @Test
    fun removeFromFavorites() = runTest {
        every { prefs.favCurrencies } returns mutableSetOf("CCC")
        val favSlot = slot<Set<String>>()
        every { prefs.favCurrencies = capture(favSlot) } answers {}
        val item1 = ExchangeListItem(
            letterCode = "AAA",
            units = 1,
            amount = "1.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val item2 = ExchangeListItem(
            letterCode = "BBB",
            units = 1,
            amount = "2.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val item3 = ExchangeListItem(
            letterCode = "CCC",
            units = 1,
            amount = "2.00",
            flagRes = 0,
            date = "01.01.2025"
        )
        val currencyList = listOf(item1, item2, item3)
        coEvery { case.getRatesForDate(any()) } returns currencyList

        viewModel.loadRates()
        advanceUntilIdle()

        expectThat(viewModel.items.value)
            .isNotNull()
            .isA<Success<List<RateListItem>>>()
            .get { data }
            .hasSize(3)
            .withFirst {
                get { isFavorite }.isTrue()
                get { data }.isEqualTo(item3)
            }

        viewModel.updateFavorites("CCC")
        advanceUntilIdle()

        val actualItems = viewModel.items.value
        expectThat(actualItems)
            .isNotNull()
            .isA<Success<List<RateListItem>>>()
            .get { data }
            .hasSize(3)
            .withFirst {
                get { isFavorite }.isFalse()
                get { data }.isEqualTo(item1)
            }

        expectThat(favSlot.captured).isEmpty()
    }
}
