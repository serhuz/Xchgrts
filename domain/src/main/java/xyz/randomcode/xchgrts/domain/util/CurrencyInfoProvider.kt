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

import android.icu.util.Currency
import arrow.core.Option
import arrow.core.getOrElse
import xyz.randomcode.xchgrts.entities.CurrencyCode
import xyz.randomcode.xchgrts.entities.CurrencyListItem
import java.util.Locale

class CurrencyInfoProvider(private val locale: Locale, private val provider: FlagResourceProvider) {

    private val available = Currency.getAvailableCurrencies()

    fun getFlagRes(letterCode: String): Int = provider.getFlagResourceForCurrency(letterCode)

    fun getCurrencyName(letterCode: String): String =
        Option.fromNullable(available.singleOrNull { it.currencyCode == letterCode })
            .map { it.displayName }
            .getOrElse { "" }

    fun getCurrencyListItem(code: CurrencyCode): CurrencyListItem =
        Option.fromNullable(available.singleOrNull { it.currencyCode == code.letterCode })
            .map {
                CurrencyListItem(
                    it.numericCode.toString(),
                    code.letterCode,
                    it.getDisplayName(locale),
                    getFlagRes(code.letterCode)
                )
            }
            .getOrElse {
                CurrencyListItem(
                    code.numberCode,
                    code.letterCode,
                    "",
                    provider.fallbackIcon()
                )
            }
}
