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

package xyz.randomcode.xchgrts.widgets.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import xyz.randomcode.xchgrts.updater.UpdateWorker
import xyz.randomcode.xchgrts.widgets.ExchangeRateWidget

@AndroidEntryPoint
class CurrencySelectionActivity : AppCompatActivity() {

    private val viewModel: CurrencySelectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.widgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        setResult(RESULT_CANCELED)

        viewModel.confirmSelection.observe(this) { updateWidget() }
        viewModel.loadCurrencyList()

        setContent {
            CurrencySelectionScreen(viewModel) { finish() }
        }

        UpdateWorker.scheduleRateUpdate(this)
    }

    private fun updateWidget() {
        lifecycleScope.launch {
            ExchangeRateWidget().updateAll(applicationContext)
            setResult(
                RESULT_OK,
                Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, viewModel.widgetId) }
            )
            finish()
        }
    }
}
