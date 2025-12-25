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

package xyz.randomcode.xchgrts.widgets.config

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import xyz.randomcode.xchgrts.domain.RateDataUseCase
import xyz.randomcode.xchgrts.entities.CurrencyListItem
import xyz.randomcode.xchgrts.entities.Failure
import xyz.randomcode.xchgrts.entities.Success
import xyz.randomcode.xchgrts.entities.WidgetSettings
import xyz.randomcode.xchgrts.util.ErrorLogger
import xyz.randomcode.xchgrts.util.Prefs

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencySelectionViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var logger: ErrorLogger

    @MockK
    lateinit var prefs: Prefs

    @MockK
    lateinit var case: RateDataUseCase

    @MockK
    lateinit var state: SavedStateHandle

    private lateinit var viewModel: CurrencySelectionViewModel


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        viewModel = CurrencySelectionViewModel(
            state,
            case,
            prefs,
            logger,
            testDispatcher,
            testDispatcher
        )
    }

    @After
    fun tearDown() {
        clearMocks(logger, prefs, case, state)
        Dispatchers.resetMain()
    }

    @Test
    fun loadCurrencies() = runTest {
        every { state.get<List<CurrencyListItem>>(CurrencySelectionViewModel.ITEMS) } returns emptyList()

        val item = CurrencyListItem(
            numberCode = "100",
            letterCode = "AAA",
            displayName = "Currency name",
            flagId = 0
        )
        val currencyList = listOf(item)
        coEvery { case.getCurrencyList() } returns currencyList

        viewModel.loadCurrencyList()
        runCurrent()

        coVerify { case.getCurrencyList() }

        val actual = viewModel.currencies.value
        expectThat(actual)
            .isNotNull()
            .isA<Success<List<CurrencyListItem>>>()
            .get { data }
            .hasSize(1)

        verify { state[CurrencySelectionViewModel.ITEMS] = currencyList }
    }

    @Test
    fun getCurrenciesFromSavedState() = runTest {
        val item = CurrencyListItem(
            numberCode = "100",
            letterCode = "AAA",
            displayName = "Currency name",
            flagId = 0
        )
        every { state.get<List<CurrencyListItem>>(CurrencySelectionViewModel.ITEMS) } returns
                listOf(item)

        viewModel.loadCurrencyList()

        coVerify(exactly = 0) { case.getCurrencyList() }
        advanceUntilIdle()

        val actual = viewModel.currencies.value
        expectThat(actual)
            .isNotNull()
            .isA<Success<List<CurrencyListItem>>>()
            .get { data }
            .hasSize(1)
    }

    @Test
    fun handleCurrencyLoadError() = runTest {
        every { state.get<List<CurrencyListItem>>(CurrencySelectionViewModel.ITEMS) } returns emptyList()
        coEvery { case.getCurrencyList() } throws RuntimeException("test")

        viewModel.loadCurrencyList()
        advanceUntilIdle()

        coVerify { case.getCurrencyList() }

        val actual = viewModel.currencies.value
        expectThat(actual)
            .isNotNull()
            .isA<Failure>()
        verify { logger.logError(any()) }
    }

    @Test
    fun selectCurrency() = runTest {
        val item = CurrencyListItem(
            numberCode = "100",
            letterCode = "AAA",
            displayName = "Currency name",
            flagId = 0
        )
        every { state.get<List<CurrencyListItem>>(CurrencySelectionViewModel.ITEMS) } returns
                listOf(item)

        viewModel.loadCurrencyList()
        advanceUntilIdle()

        viewModel.updateItemSelection("AAA")
        advanceUntilIdle()

        val hasSelection = viewModel.hasSelectedItem.value
        expectThat(hasSelection).isTrue()

        val actual = viewModel.currencies.value
        expectThat(actual)
            .isA<Success<List<CurrencyListItem>>>()
            .get { data }
            .get { first().isSelected }
            .isTrue()
    }

    @Test
    fun confirmSelection() = runTest {
        val item = CurrencyListItem(
            numberCode = "100",
            letterCode = "AAA",
            displayName = "Currency name",
            flagId = 0
        )
        every { state.get<List<CurrencyListItem>>(CurrencySelectionViewModel.ITEMS) } returns
                listOf(item)

        viewModel.loadCurrencyList()
        advanceUntilIdle()
        viewModel.updateItemSelection("AAA")
        advanceUntilIdle()
        viewModel.confirmSelection()
        advanceUntilIdle()

        coVerify { prefs.storeWidgetSettings(WidgetSettings(Int.MIN_VALUE, "AAA")) }
        expectThat(viewModel.confirmSelection.value).isNull()
    }
}
