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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrencyData(
    @Json(name = "StartDate") val date: String,
    @Json(name = "TimeSign") val time: String,
    @Json(name = "CurrencyCode") val numberCode: String,
    @Json(name = "CurrencyCodeL") val letterCode: String,
    @Json(name = "Units") val units: Int,
    @Json(name = "Amount") val amount: Float
)
