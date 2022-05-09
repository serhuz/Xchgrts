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

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.databinding.ActivityCurrencySelectionBinding
import xyz.randomcode.xchgrts.entities.Failure
import xyz.randomcode.xchgrts.entities.Loading
import xyz.randomcode.xchgrts.entities.Success
import xyz.randomcode.xchgrts.updater.UpdateWorker
import xyz.randomcode.xchgrts.widgets.WidgetProvider
import java.net.UnknownHostException

@AndroidEntryPoint
class CurrencySelectionActivity : AppCompatActivity() {

    private val viewModel: CurrencySelectionViewModel by viewModels()
    private val adapter: CurrencySelectionAdapter by lazy { CurrencySelectionAdapter(viewModel) }
    private lateinit var binding: ActivityCurrencySelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.widgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        setResult(Activity.RESULT_CANCELED)

        DataBindingUtil.setContentView<ActivityCurrencySelectionBinding>(this, R.layout.activity_currency_selection)
            .also { binding = it }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setSupportActionBar(binding.currencySelectionToolbar)

        setupRecycler()
        observeItems()

        UpdateWorker.scheduleRateUpdate(this)
    }

    private fun setupRecycler() {
        binding.currencySelectionList.apply {
            adapter = this@CurrencySelectionActivity.adapter
        }
    }

    private fun observeItems() {
        viewModel.currencies.observe(this) {
            when (it) {
                is Loading -> {
                    binding.apply {
                        currencyListProgress.isVisible = true
                        currencySelectionList.isVisible = false
                    }
                }
                is Failure -> {
                    binding.apply {
                        currencyListProgress.isVisible = false
                        currencySelectionList.isVisible = true
                    }
                    showRetrySnackbar(it.reason)
                }
                is Success -> {
                    binding.apply {
                        currencyListProgress.isVisible = false
                        currencySelectionList.isVisible = true
                    }
                    adapter.submitList(it.data)
                }
            }
        }
        viewModel.confirmSelection.observe(this) { updateWidget() }
    }

    private fun showRetrySnackbar(reason: Throwable) {
        when (reason) {
            is HttpException, is UnknownHostException -> {
                Snackbar.make(binding.coordinator, R.string.loading_error_http, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) { viewModel.loadCurrencyList() }
                    .show()
            }
            else -> {
                Snackbar.make(binding.coordinator, R.string.loading_error, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.finish) { finish() }
                    .show()
            }
        }
    }

    private fun updateWidget() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                WidgetProvider.updateWidgets(
                    this@CurrencySelectionActivity,
                    AppWidgetManager.getInstance(this@CurrencySelectionActivity),
                    viewModel.prefs,
                    viewModel.case,
                    viewModel.widgetId
                )
            }
            setResult(
                Activity.RESULT_OK,
                Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, viewModel.widgetId) }
            )
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
