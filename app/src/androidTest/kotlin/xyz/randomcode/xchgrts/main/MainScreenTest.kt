/*
 * Copyright 2026 Sergei Munovarov
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

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.MutableLiveData
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.entities.ExchangeListItem
import xyz.randomcode.xchgrts.entities.Failure
import xyz.randomcode.xchgrts.entities.Loading
import xyz.randomcode.xchgrts.entities.Success
import xyz.randomcode.xchgrts.theme.AppTheme
import java.net.UnknownHostException

class MainScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadRatesShouldBeCalled() {
        val viewModel: ExchangeRatesViewModel = mockk(relaxed = true)
        every { viewModel.items } returns MutableLiveData(Loading)
        every { viewModel.isRefreshing } returns MutableLiveData(false)
        every { viewModel.date } returns flowOf("")

        composeTestRule.setContent { MainScreen(viewModel, {}, {}) }

        verify { viewModel.loadRates() }
    }

    @Test
    fun loadingIndicatorShouldBeDisplayed() {
        val viewModel: ExchangeRatesViewModel = mockk(relaxed = true)
        every { viewModel.items } returns MutableLiveData(Loading)
        every { viewModel.isRefreshing } returns MutableLiveData(false)
        every { viewModel.date } returns flowOf("")

        composeTestRule.setContent { MainScreen(viewModel, {}, {}) }

        composeTestRule.onNodeWithTag(TAG_LOADING_INDICATOR).assertIsDisplayed()
    }

    @Test
    fun displayContentWhenLoadedSuccessfully() {
        val viewModel: ExchangeRatesViewModel = mockk(relaxed = true)
        val items = listOf(
            RateListItem(
                true,
                ExchangeListItem(
                    "AAA",
                    1,
                    "1.00",
                    com.blongho.country_data.R.drawable.eu,
                    "01.01.2025"
                )
            ),
            RateListItem(
                false,
                ExchangeListItem(
                    "BBB",
                    1,
                    "2.00",
                    com.blongho.country_data.R.drawable.globe,
                    "01.01.2025"
                )
            )
        )
        every { viewModel.items } returns MutableLiveData(Success(items))
        every { viewModel.isRefreshing } returns MutableLiveData(false)
        every { viewModel.date } returns flowOf("01.01.2025")

        composeTestRule.setContent { MainScreen(viewModel, {}, {}) }

        composeTestRule.onNodeWithText("1 AAA").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.00").assertIsDisplayed()
        composeTestRule
            .onNode(hasTestTag("${TAG_FLAG}_AAA") and hasAnySibling(hasText("1 AAA")))
            .assertIsDisplayed()
        composeTestRule
            .onNode(
                hasTestTag("${TAG_FAVORITE}_AAA") and
                        SemanticsMatcher.expectValue(
                            SemanticsProperties.StateDescription,
                            SEMANTICS_TAG_FAVORITE
                        )
            )
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("1 BBB").assertIsDisplayed()
        composeTestRule.onNodeWithText("2.00").assertIsDisplayed()
        composeTestRule
            .onNode(hasTestTag("${TAG_FLAG}_BBB") and hasAnySibling(hasText("1 BBB")))
            .assertIsDisplayed()
        composeTestRule
            .onNode(
                hasTestTag("${TAG_FAVORITE}_BBB") and
                        SemanticsMatcher.expectValue(
                            SemanticsProperties.StateDescription,
                            SEMANTICS_TAG_NOT_FAVORITE
                        )
            )
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(TAG_LOADING_INDICATOR).assertIsNotDisplayed()
    }

    @Test
    fun listItemShouldDisplayContentAndTriggerFavoriteAction() {
        val updateFavAction: (String) -> Unit = mockk(relaxed = true)
        val item = RateListItem(
            true,
            ExchangeListItem(
                "AAA",
                10,
                "42.00",
                com.blongho.country_data.R.drawable.eu,
                "01.01.2025",
                "Currency"
            )
        )

        composeTestRule.setContent {
            AppTheme {
                ListItem(
                    item = item,
                    updateFavAction = updateFavAction
                )
            }
        }

        composeTestRule.onNodeWithText("10 AAA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Currency").assertIsDisplayed()
        composeTestRule.onNodeWithText("42.00").assertIsDisplayed()
        composeTestRule
            .onNode(
                hasTestTag("${TAG_FAVORITE}_AAA") and
                        SemanticsMatcher.expectValue(
                            SemanticsProperties.StateDescription,
                            SEMANTICS_TAG_FAVORITE
                        )
            )
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("${TAG_FAVORITE}_AAA").performClick()

        verify { updateFavAction.invoke("AAA") }
    }

    @Test
    fun displayDefaultAppTitleWhenNoDateExtracted() {
        val viewModel: ExchangeRatesViewModel = mockk(relaxed = true)
        every { viewModel.items } returns MutableLiveData(Success(emptyList()))
        every { viewModel.isRefreshing } returns MutableLiveData(false)
        every { viewModel.date } returns flowOf("")

        composeTestRule.setContent { MainScreen(viewModel, {}, {}) }

        composeTestRule.onNode(hasTestTag(TAG_APP_TITLE))
            .assert(hasText(getInstrumentation().targetContext.getString(R.string.app_name)))
    }

    @Test
    fun displayDefaultAppTitleWhenDatePresent() {
        val date = "01.01.2025"

        val viewModel: ExchangeRatesViewModel = mockk(relaxed = true)
        every { viewModel.items } returns MutableLiveData(Success(emptyList()))
        every { viewModel.isRefreshing } returns MutableLiveData(false)
        every { viewModel.date } returns flowOf(date)

        composeTestRule.setContent { MainScreen(viewModel, {}, {}) }

        composeTestRule.onNode(hasTestTag(TAG_APP_TITLE)).assert(hasText(date))
    }

    @Test
    fun favoriteActionShouldBeTriggeredWhenFavoriteButtonPressed() {
        val viewModel: ExchangeRatesViewModel = mockk(relaxed = true)
        val items = listOf(
            RateListItem(
                false,
                ExchangeListItem(
                    "AAA",
                    1,
                    "1.00",
                    com.blongho.country_data.R.drawable.eu,
                    "01.01.2025"
                )
            ),
            RateListItem(
                false,
                ExchangeListItem(
                    "BBB",
                    1,
                    "2.00",
                    com.blongho.country_data.R.drawable.globe,
                    "01.01.2025"
                )
            )
        )
        every { viewModel.items } returns MutableLiveData(Success(items))
        every { viewModel.isRefreshing } returns MutableLiveData(false)
        every { viewModel.date } returns flowOf("")

        composeTestRule.setContent { MainScreen(viewModel, {}, {}) }

        composeTestRule.onNodeWithTag("${TAG_FAVORITE}_AAA").performClick()

        verify { viewModel.updateFavorites("AAA") }
    }

    @Test
    fun errorSnackbarShouldBeDismissedAfterRetry() {
        val viewModel: ExchangeRatesViewModel = mockk(relaxed = true)
        every { viewModel.items } returns MutableLiveData(Failure(UnknownHostException("test")))
        every { viewModel.isRefreshing } returns MutableLiveData(false)
        every { viewModel.date } returns flowOf("")

        composeTestRule.setContent { MainScreen(viewModel, {}, {}) }

        composeTestRule.onNodeWithTag(TAG_SNACKBAR).assertIsDisplayed()

        composeTestRule.onNodeWithText(getInstrumentation().targetContext.getString(R.string.retry))
            .performClick()

        composeTestRule.onNodeWithTag(TAG_SNACKBAR).assertIsNotDisplayed()
    }

    @Test
    fun closeActionShouldBeTriggeredAfterFinishPressed() {
        val viewModel: ExchangeRatesViewModel = mockk(relaxed = true)
        every { viewModel.items } returns MutableLiveData(Failure(IllegalStateException("test")))
        every { viewModel.isRefreshing } returns MutableLiveData(false)
        every { viewModel.date } returns flowOf("")

        val closeAction: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent { MainScreen(viewModel, {}, closeAction) }

        composeTestRule.onNodeWithTag(TAG_SNACKBAR).assertIsDisplayed()

        composeTestRule.onNodeWithText(getInstrumentation().targetContext.getString(R.string.finish))
            .performClick()

        verify { closeAction.invoke() }
    }

    @Test
    fun licenseActionShouldBeTriggeredWhenLicenseButtonPressed() {
        val viewModel: ExchangeRatesViewModel = mockk()
        every { viewModel.items } returns MutableLiveData(Success(emptyList()))
        every { viewModel.isRefreshing } returns MutableLiveData(false)
        every { viewModel.date } returns flowOf("")
        every { viewModel.loadRates() } returns Unit

        val licenseAction: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent { MainScreen(viewModel, licenseAction, {}) }

        composeTestRule.onNodeWithTag(TAG_LICENSE).performClick()

        verify { licenseAction.invoke() }
    }
}
