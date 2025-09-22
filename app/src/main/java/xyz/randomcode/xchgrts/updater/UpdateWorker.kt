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

package xyz.randomcode.xchgrts.updater

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.HttpException
import xyz.randomcode.xchgrts.BuildConfig
import xyz.randomcode.xchgrts.domain.RateDataUseCase
import xyz.randomcode.xchgrts.domain.util.DateProvider
import xyz.randomcode.xchgrts.util.Prefs
import xyz.randomcode.xchgrts.widgets.WidgetProvider
import java.util.concurrent.TimeUnit

class UpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context.applicationContext, params), KoinComponent {

    private val case: RateDataUseCase by inject()
    private val prefs: Prefs by inject()

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            FirebaseAnalytics.getInstance(context).logEvent("currency_update_started", Bundle.EMPTY)
            try {
                case.updateRates(DateProvider().currentDate)
                updateWidgets()
                FirebaseAnalytics.getInstance(context)
                    .logEvent("currency_update_done", Bundle.EMPTY)
                Result.success()
            } catch (e: Exception) {
                FirebaseAnalytics.getInstance(context)
                    .logEvent("currency_update_failed", Bundle.EMPTY)
                if (e is HttpException) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }

    private suspend fun updateWidgets() {
        val widgetManager = AppWidgetManager.getInstance(applicationContext)
        ComponentName(applicationContext, WidgetProvider::class.java)
            .let(widgetManager::getAppWidgetIds)
            .forEach { id ->
                prefs.loadWidgetSettings(id)
                    ?.let {
                        WidgetProvider.updateWidgets(
                            applicationContext,
                            widgetManager,
                            prefs,
                            case,
                            it.id
                        )
                    }
                    ?: FirebaseCrashlytics.getInstance()
                        .recordException(IllegalStateException("No settings found for widget $id"))
            }

    }

    companion object {

        fun scheduleRateUpdate(context: Context) {
            val ratesUpdateRequest = PeriodicWorkRequestBuilder<UpdateWorker>(12, TimeUnit.HOURS)
                .addTag(BuildConfig.UPDATE_WORK_TAG)
                .setInitialDelay(DateProvider().minutesToMidnight + 60L, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    BuildConfig.UPDATE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    ratesUpdateRequest
                )
        }
    }
}
