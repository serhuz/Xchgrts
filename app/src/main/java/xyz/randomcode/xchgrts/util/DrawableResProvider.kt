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

package xyz.randomcode.xchgrts.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.domain.util.FlagResourceProvider
import javax.inject.Inject

class DrawableResProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : FlagResourceProvider() {

    override fun getFlag(alpha2Code: String): Int =
        if (alpha2Code == "do") {
            R.drawable.dominican
        } else {
            val resource = "drawable/${alpha2Code.lowercase()}"
            context.resources
                .getIdentifier(resource, null, context.packageName)
                .takeIf { it != 0 }
                ?: fallbackIcon()
        }

    override fun fallbackIcon(): Int = R.drawable.globe
}
