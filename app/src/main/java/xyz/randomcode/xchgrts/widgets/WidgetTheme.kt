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

import androidx.glance.color.ColorProvider
import androidx.glance.color.colorProviders
import xyz.randomcode.xchgrts.theme.backgroundDark
import xyz.randomcode.xchgrts.theme.backgroundLight
import xyz.randomcode.xchgrts.theme.errorContainerDark
import xyz.randomcode.xchgrts.theme.errorContainerLight
import xyz.randomcode.xchgrts.theme.errorDark
import xyz.randomcode.xchgrts.theme.errorLight
import xyz.randomcode.xchgrts.theme.inverseOnSurfaceDark
import xyz.randomcode.xchgrts.theme.inverseOnSurfaceLight
import xyz.randomcode.xchgrts.theme.inversePrimaryDark
import xyz.randomcode.xchgrts.theme.inversePrimaryLight
import xyz.randomcode.xchgrts.theme.inverseSurfaceDark
import xyz.randomcode.xchgrts.theme.inverseSurfaceLight
import xyz.randomcode.xchgrts.theme.onBackgroundDark
import xyz.randomcode.xchgrts.theme.onBackgroundLight
import xyz.randomcode.xchgrts.theme.onErrorContainerDark
import xyz.randomcode.xchgrts.theme.onErrorContainerLight
import xyz.randomcode.xchgrts.theme.onErrorDark
import xyz.randomcode.xchgrts.theme.onErrorLight
import xyz.randomcode.xchgrts.theme.onPrimaryContainerDark
import xyz.randomcode.xchgrts.theme.onPrimaryDark
import xyz.randomcode.xchgrts.theme.onPrimaryLight
import xyz.randomcode.xchgrts.theme.onSecondaryContainerDark
import xyz.randomcode.xchgrts.theme.onSecondaryContainerLight
import xyz.randomcode.xchgrts.theme.onSecondaryDark
import xyz.randomcode.xchgrts.theme.onSecondaryLight
import xyz.randomcode.xchgrts.theme.onSurfaceDark
import xyz.randomcode.xchgrts.theme.onSurfaceLight
import xyz.randomcode.xchgrts.theme.onSurfaceVariantDark
import xyz.randomcode.xchgrts.theme.onSurfaceVariantLight
import xyz.randomcode.xchgrts.theme.onTertiaryContainerDark
import xyz.randomcode.xchgrts.theme.onTertiaryContainerLight
import xyz.randomcode.xchgrts.theme.onTertiaryDark
import xyz.randomcode.xchgrts.theme.onTertiaryLight
import xyz.randomcode.xchgrts.theme.outlineDark
import xyz.randomcode.xchgrts.theme.outlineLight
import xyz.randomcode.xchgrts.theme.primaryContainerDark
import xyz.randomcode.xchgrts.theme.primaryContainerLight
import xyz.randomcode.xchgrts.theme.primaryDark
import xyz.randomcode.xchgrts.theme.primaryLight
import xyz.randomcode.xchgrts.theme.secondaryContainerDark
import xyz.randomcode.xchgrts.theme.secondaryContainerLight
import xyz.randomcode.xchgrts.theme.secondaryDark
import xyz.randomcode.xchgrts.theme.secondaryLight
import xyz.randomcode.xchgrts.theme.surfaceDark
import xyz.randomcode.xchgrts.theme.surfaceLight
import xyz.randomcode.xchgrts.theme.surfaceVariantDark
import xyz.randomcode.xchgrts.theme.surfaceVariantLight
import xyz.randomcode.xchgrts.theme.tertiaryContainerDark
import xyz.randomcode.xchgrts.theme.tertiaryContainerLight
import xyz.randomcode.xchgrts.theme.tertiaryDark
import xyz.randomcode.xchgrts.theme.tertiaryLight

object WidgetTheme {

    val colors = colorProviders(
        primary = ColorProvider(primaryLight, primaryDark),
        onPrimary = ColorProvider(onPrimaryLight, onPrimaryDark),
        primaryContainer = ColorProvider(primaryContainerLight, primaryContainerDark),
        onPrimaryContainer = ColorProvider(onPrimaryLight, onPrimaryContainerDark),
        secondary = ColorProvider(secondaryLight, secondaryDark),
        onSecondary = ColorProvider(onSecondaryLight, onSecondaryDark),
        secondaryContainer = ColorProvider(secondaryContainerLight, secondaryContainerDark),
        onSecondaryContainer = ColorProvider(onSecondaryContainerLight, onSecondaryContainerDark),
        tertiary = ColorProvider(tertiaryLight, tertiaryDark),
        onTertiary = ColorProvider(onTertiaryLight, onTertiaryDark),
        tertiaryContainer = ColorProvider(tertiaryContainerLight, tertiaryContainerDark),
        onTertiaryContainer = ColorProvider(onTertiaryContainerLight, onTertiaryContainerDark),
        error = ColorProvider(errorLight, errorDark),
        errorContainer = ColorProvider(errorContainerLight, errorContainerDark),
        onError = ColorProvider(onErrorLight, onErrorDark),
        onErrorContainer = ColorProvider(onErrorContainerLight, onErrorContainerDark),
        background = ColorProvider(backgroundLight, backgroundDark),
        onBackground = ColorProvider(onBackgroundLight, onBackgroundDark),
        surface = ColorProvider(surfaceLight, surfaceDark),
        onSurface = ColorProvider(onSurfaceLight, onSurfaceDark),
        surfaceVariant = ColorProvider(surfaceVariantLight, surfaceVariantDark),
        onSurfaceVariant = ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark),
        outline = ColorProvider(outlineLight, outlineDark),
        inverseOnSurface = ColorProvider(inverseOnSurfaceLight, inverseOnSurfaceDark),
        inverseSurface = ColorProvider(inverseSurfaceLight, inverseSurfaceDark),
        inversePrimary = ColorProvider(inversePrimaryLight, inversePrimaryDark),
        widgetBackground = ColorProvider(surfaceVariantLight, surfaceVariantDark)
    )
}
