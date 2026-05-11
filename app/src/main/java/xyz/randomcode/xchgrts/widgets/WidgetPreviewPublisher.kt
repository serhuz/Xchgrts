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

package xyz.randomcode.xchgrts.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.collection.intSetOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetManager.Companion.SET_WIDGET_PREVIEWS_RESULT_RATE_LIMITED
import dagger.hilt.android.qualifiers.ApplicationContext
import xyz.randomcode.xchgrts.util.ErrorLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetPreviewPublisher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: ErrorLogger
) {

    suspend fun publishPreviewsIfMissing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return
        }

        if (hasPreview()) {
            return
        }

        publishPreviews()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun publishPreviews() {
        val result = GlanceAppWidgetManager(context)
            .setWidgetPreviews(
                WidgetReceiver::class,
                intSetOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN)
            )

        if (result == SET_WIDGET_PREVIEWS_RESULT_RATE_LIMITED) {
            logger.logError(IllegalStateException("Generated widget preview update was rate-limited"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun hasPreview(widgetCategory: Int = AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN) =
        context.getSystemService(AppWidgetManager::class.java)
            .installedProviders
            .firstOrNull { it.provider == ComponentName(context, WidgetReceiver::class.java) }
            ?.let { it.generatedPreviewCategories and widgetCategory != 0 }
            ?: false
}
