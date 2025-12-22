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

package xyz.randomcode.xchgrts.widgets

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter.Companion.tint
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.EntryPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.domain.util.DateProvider
import xyz.randomcode.xchgrts.entities.ExchangeListItem
import xyz.randomcode.xchgrts.main.MainActivity

class ExchangeRateWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPoints.get<WidgetEntryPoint>(context, WidgetEntryPoint::class.java)
        val useCase = entryPoint.useCase()
        val prefs = entryPoint.prefs()

        val appWidgetManager = GlanceAppWidgetManager(context)
        val widgetId = appWidgetManager.getAppWidgetId(id)

        val data = withContext(Dispatchers.IO) {
            runCatching {
                prefs.loadWidgetSettings(widgetId).filterNotNull().first().let {
                    useCase.getRateForDate(DateProvider().currentDate, it.letterCode)
                }
            }
                .onFailure(FirebaseCrashlytics.getInstance()::recordException)
                .getOrNull()
        }

        provideContent {
            GlanceTheme(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    GlanceTheme.colors
                else
                    WidgetTheme.colors
            ) {
                data?.let { WidgetUi(it) } ?: ErrorUi()
            }
        }
    }
}

@Composable
fun WidgetUi(item: ExchangeListItem) {
    val action = actionStartActivity<MainActivity>()
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = action)
            .background(GlanceTheme.colors.widgetBackground)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.shortDate,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onBackground
            )
        )

        Image(
            modifier = GlanceModifier.padding(start = 8.dp).width(32.dp).height(32.dp),
            provider = ImageProvider(item.flagRes),
            colorFilter = tint(ColorProvider(Color.Unspecified)),
            contentDescription = LocalContext.current.getString(R.string.desc_flag)
        )

        Text(
            modifier = GlanceModifier.padding(start = 8.dp),
            text = "${item.units} ${item.letterCode}",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onBackground
            )
        )

        Text(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 8.dp),
            text = item.amount,
            style = TextStyle(
                textAlign = TextAlign.End,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onBackground
            )
        )
    }
}

@Composable
fun ErrorUi() {
    val action = actionStartActivity<MainActivity>()
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(48.dp)
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(action),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = GlanceModifier,
            text = LocalContext.current.getString(R.string.loading_error),
            maxLines = 1,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onBackground
            )
        )
    }
}
