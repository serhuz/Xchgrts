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

package xyz.randomcode.xchgrts.widgets.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import retrofit2.HttpException
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.entities.CurrencyListItem
import xyz.randomcode.xchgrts.entities.Failure
import xyz.randomcode.xchgrts.entities.Success
import xyz.randomcode.xchgrts.theme.AppTheme
import java.net.UnknownHostException

@Composable
fun CurrencySelectionScreen(
    viewModel: CurrencySelectionViewModel,
    closeAction: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { CurrencySelectionToolbar(closeAction) },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                val state = viewModel.currencies.observeAsState()
                when (state.value) {
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
                                            viewModel.loadCurrencyList()
                                        }

                                        else -> closeAction.invoke()
                                    }
                                }
                            }
                        }
                    }

                    is Success<List<CurrencyListItem>> -> {
                        val hasSelectedItem = viewModel.hasSelectedItem.observeAsState(false)
                        CurrencySelectionContent(
                            data = (state.value as Success<List<CurrencyListItem>>).data,
                            hasSelectedItem = hasSelectedItem.value,
                            buttonModifier = Modifier.align(Alignment.BottomCenter),
                            selectionAction = viewModel::updateItemSelection,
                            confirmAction = viewModel::confirmSelection
                        )
                    }

                    else -> CurrencySelectionLoader(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun CurrencySelectionToolbar(closeAction: () -> Unit = {}) {
    TopAppBar(
        modifier = Modifier,
        title = {
            Text(
                text = stringResource(R.string.currency_selection_title),
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = closeAction) {
                Image(
                    modifier = Modifier.padding(8.dp),
                    painter = painterResource(R.drawable.ic_close_32),
                    contentDescription = stringResource(R.string.desc_close),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                )
            }
        },
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
fun CurrencySelectionContent(
    data: List<CurrencyListItem>,
    hasSelectedItem: Boolean,
    buttonModifier: Modifier,
    selectionAction: (String) -> Unit = {},
    confirmAction: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 56.dp)
    ) {
        items(data) { CurrencyItem(it, selectionAction) }
    }

    if (hasSelectedItem) {
        Button(
            modifier = buttonModifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .fillMaxWidth()
                .height(48.dp),
            onClick = confirmAction,
            elevation = buttonElevation(defaultElevation = 8.dp)
        ) {
            Text(text = stringResource(R.string.ok), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun CurrencySelectionLoader(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier
            .width(48.dp)
            .height(48.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Preview(device = PIXEL_9)
@Composable
fun CurrencySelectionLoaderPreview() {
    AppTheme { CurrencySelectionLoader() }
}

@Composable
fun CurrencyItem(item: CurrencyListItem, selectionAction: (String) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = { selectionAction.invoke(item.letterCode) })
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(item.flagId),
            contentDescription = stringResource(R.string.desc_flag)
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .wrapContentSize(),
            text = item.letterCode,
            maxLines = 1,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1.0f),
            text = item.displayName,
            maxLines = 1,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        )

        if (item.isSelected) {
            Image(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(32.dp),
                painter = painterResource(R.drawable.ic_check),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                contentDescription = stringResource(R.string.desc_selection)
            )
        }
    }
}
