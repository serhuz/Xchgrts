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

package xyz.randomcode.xchgrts.entities

import arrow.core.identity
import arrow.core.left
import arrow.core.none
import arrow.core.right
import arrow.core.some
import arrow.optics.Prism
import arrow.optics.optics

@optics
data class CurrencyListItem(
    val numberCode: String,
    val letterCode: String,
    val displayName: String,
    val flagId: Int,
    val isSelected: Boolean = false
) {

    companion object {

        val itemSelected =
            Prism<CurrencyListItem, CurrencyListItem, CurrencyListItem, CurrencyListItem>(
                getOrModify = { if (it.isSelected) it.right() else it.left() },
            reverseGet = ::identity
        )

        fun codeNotEquals(letterCode: String) = Prism<CurrencyListItem, CurrencyListItem>(
            getOption = { if (letterCode != it.letterCode) it.some() else none() },
            reverseGet = ::identity
        )

        fun codeEquals(letterCode: String): Prism<CurrencyListItem, CurrencyListItem> = Prism(
            getOption = { if (letterCode == it.letterCode) it.some() else none() },
            reverseGet = ::identity
        )
    }
}
