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
import android.os.Bundle
import android.widget.RemoteViews
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.domain.RateDataUseCase
import xyz.randomcode.xchgrts.domain.util.DateProvider
import xyz.randomcode.xchgrts.main.MainActivity
import xyz.randomcode.xchgrts.util.Prefs
import javax.inject.Inject

@AndroidEntryPoint
class WidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var useCase: RateDataUseCase

    @Inject
    lateinit var prefs: Prefs

    private val scope: CoroutineScope by lazy(LazyThreadSafetyMode.NONE) { MainScope() }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        scope.launch {
            runCatching { updateWidgets(context, manager, prefs, useCase, *ids) }
                .onSuccess {
                    FirebaseAnalytics
                        .getInstance(context)
                        .logEvent("Widget_update", Bundle.EMPTY)
                }
                .onFailure(FirebaseCrashlytics.getInstance()::recordException)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach(prefs::removeSettings)
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

                    val exchangeRate =
                        case.getRateForDate(DateProvider().currentDate, it.letterCode)

                    val views =
                        RemoteViews(context.packageName, R.layout.widget_single_wide).apply {
                            setOnClickPendingIntent(R.id.widgetLayout, intent)
                            setTextViewText(R.id.widgetDate, exchangeRate.shortDate)
                            setTextViewText(
                                R.id.widgetCurrencyUnit,
                                "${exchangeRate.units} ${exchangeRate.letterCode}"
                            )
                            setTextViewText(R.id.widgetRateText, exchangeRate.amount)
                            setImageViewResource(R.id.widgetFlag, exchangeRate.flagRes)
                        }

                    manager.updateAppWidget(id, views)
                }
            }
        }
    }
}
