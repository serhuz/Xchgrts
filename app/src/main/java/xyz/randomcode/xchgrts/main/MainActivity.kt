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

package xyz.randomcode.xchgrts.main

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.domain.RateDataUseCase
import xyz.randomcode.xchgrts.entities.Success
import xyz.randomcode.xchgrts.updater.UpdateWorker
import xyz.randomcode.xchgrts.util.Prefs
import xyz.randomcode.xchgrts.widgets.WidgetProvider
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: ExchangeRatesViewModel by viewModels()

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var rateDataUseCase: RateDataUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen(
                viewModel = viewModel,
                licenseAction = {
                    OssLicensesMenuActivity.setActivityTitle(getString(R.string.menu_oss_licenses));
                    startActivity(Intent(applicationContext, OssLicensesMenuActivity::class.java))
                },
                closeAction = { finish() }
            )
        }

        viewModel.items.observe(this) {
            if (it is Success) {
                updateWidgets()
            }
        }

        UpdateWorker.scheduleRateUpdate(this)
    }

    private fun updateWidgets() {
        lifecycleScope.launch {
            val manager = AppWidgetManager.getInstance(applicationContext)

            withContext(Dispatchers.IO) {
                ComponentName(applicationContext, WidgetProvider::class.java)
                    .let(manager::getAppWidgetIds)
                    .forEach { id ->
                        prefs.loadWidgetSettings(id)
                            ?.let {
                                WidgetProvider.updateWidgets(
                                    applicationContext,
                                    manager,
                                    prefs,
                                    rateDataUseCase,
                                    it.id
                                )
                            }
                            ?: FirebaseCrashlytics.getInstance()
                                .recordException(IllegalStateException("No settings found for widget $id"))
                    }
            }
        }
    }
}
