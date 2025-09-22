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

package xyz.randomcode.xchgrts.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.domain.RateDataUseCase
import xyz.randomcode.xchgrts.domain.util.DateProvider
import xyz.randomcode.xchgrts.main.MainActivity
import xyz.randomcode.xchgrts.util.Prefs

class WidgetProvider : AppWidgetProvider(), KoinComponent {

    private val components: ProviderComponents by lazy { ProviderComponents() }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        try {
            CoroutineScope(Dispatchers.Default + Job()).launch {
                updateWidgets(context, manager, components.prefs, components.case, *ids)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach(components.prefs::removeSettings)
    }

    companion object {

        suspend fun updateWidgets(
            context: Context,
            manager: AppWidgetManager,
            prefs: Prefs,
            case: RateDataUseCase,
            vararg ids: Int
        ) {
            ids.forEach { id ->
                prefs.loadWidgetSettings(id)?.let {
                    val intent = Intent(context, MainActivity::class.java).let {
                        PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
                    }

                    val exchangeRate = case.getRateForDate(DateProvider().currentDate, it.letterCode)

                    val views = RemoteViews(context.packageName, R.layout.widget_single_wide).apply {
                        setOnClickPendingIntent(R.id.widgetLayout, intent)
                        setTextViewText(R.id.widgetDate, exchangeRate.shortDate)
                        setTextViewText(R.id.widgetCurrencyUnit, "${exchangeRate.units} ${exchangeRate.letterCode}")
                        setTextViewText(R.id.widgetRateText, exchangeRate.amount)
                        setImageViewResource(R.id.widgetFlag, exchangeRate.flagRes)
                    }

                    manager.updateAppWidget(id, views)
                }
            }
        }
    }
}

class ProviderComponents : KoinComponent {
    val prefs: Prefs by inject()
    val case: RateDataUseCase by inject()
}
