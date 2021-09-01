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

package xyz.randomcode.xchgrts.domain.util

import androidx.annotation.VisibleForTesting

abstract class FlagResourceProvider {

    fun getFlagResourceForCurrency(letterCode: String): Int =
        getFlag(letterCode.substring(0..1))

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun getFlag(alpha2Code: String): Int

    abstract fun fallbackIcon(): Int
}
