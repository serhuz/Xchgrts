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

@file:OptIn(ExperimentalMaterial3Api::class)

package xyz.randomcode.xchgrts.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices.PIXEL_8
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import retrofit2.HttpException
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.entities.ExchangeListItem
import xyz.randomcode.xchgrts.entities.Failure
import xyz.randomcode.xchgrts.entities.Loading
import xyz.randomcode.xchgrts.entities.Success
import xyz.randomcode.xchgrts.theme.AppTheme
import java.net.UnknownHostException

@Composable
fun MainScreen(
    viewModel: ExchangeRatesViewModel,
    licenseAction: () -> Unit = {},
    closeAction: () -> Unit = {}
) {
    AppTheme {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                val title =
                    viewModel.date.collectAsState(stringResource(R.string.app_name)).value
                MainScreenToolbar(
                    scrollBehavior = scrollBehavior,
                    title = title,
                    licenseAction = licenseAction
                )
            }
        ) { contentPadding ->
            val state = viewModel.items.observeAsState()
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                isRefreshing = (state.value != null && state.value is Loading),
                onRefresh = viewModel::loadRates
            ) {
                when (val resource = state.value) {
                    is Success<List<RateListItem>> -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(resource.data, key = { it.data.letterCode }) {
                                ListItem(
                                    modifier = Modifier.animateItem(),
                                    item = it,
                                    updateFavAction = viewModel::updateFavorites
                                )
                            }
                        }
                    }

                    is Failure -> {
                        val reason = (state.value as Failure).reason
                        val message: String
                        val action: String
                        when (reason) {
                            is HttpException, is UnknownHostException -> {
                                message = stringResource(R.string.loading_error_http)
                                action = stringResource(R.string.retry)
                            }

                            else -> {
                                message = stringResource(R.string.loading_error)
                                action = stringResource(R.string.finish)
                            }
                        }

                        LaunchedEffect("show error") {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = message,
                                    actionLabel = action,
                                    duration = SnackbarDuration.Indefinite
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    when (reason) {
                                        is HttpException, is UnknownHostException -> {
                                            viewModel.loadRates()
                                        }

                                        else -> closeAction.invoke()
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(48.dp)
                                    .align(Alignment.Center),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreenToolbar(
    scrollBehavior: TopAppBarScrollBehavior,
    licenseAction: () -> Unit = {},
    title: String = ""
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title.ifEmpty { stringResource(R.string.app_name) },
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        actions = {
            IconButton(onClick = licenseAction::invoke) {
                Icon(
                    painter = painterResource(R.drawable.ic_description_32),
                    contentDescription = stringResource(R.string.desc_license)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            subtitleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    item: RateListItem,
    updateFavAction: (letterCode: String) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .width(32.dp)
                .height(32.dp),
            painter = (painterResource(item.data.flagRes)),
            contentDescription = stringResource(R.string.desc_flag),
            tint = Color.Unspecified
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .wrapContentSize(),
            text = "${item.data.units} ${item.data.letterCode}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
                .wrapContentHeight(),
            text = item.data.fullName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .wrapContentSize(),
            text = item.data.amount,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )

        val favIconId =
            if (item.isFavorite) R.drawable.ic_filled_star else R.drawable.ic_outline_star
        IconButton(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(32.dp)
                .height(32.dp),
            onClick = { updateFavAction.invoke(item.data.letterCode) }
        ) {
            Icon(
                painter = painterResource(favIconId),
                contentDescription = stringResource(R.string.desc_flag),
                tint = colorResource(R.color.favorite)
            )
        }
    }
}

@Preview(device = PIXEL_8)
@Composable
fun MainScreenToolbarPreview() {
    AppTheme {
        MainScreenToolbar(TopAppBarDefaults.enterAlwaysScrollBehavior())
    }
}

@Preview(device = PIXEL_8)
@Composable
fun ListItemPreview() {
    AppTheme {
        ListItem(
            item = RateListItem(
                isFavorite = true,
                data = ExchangeListItem(
                    letterCode = "AAA",
                    units = 1,
                    amount = "100.00",
                    flagRes = com.blongho.country_data.R.drawable.eu,
                    date = "01.01.2025",
                    fullName = "Currency"
                )
            )
        )
    }
}
