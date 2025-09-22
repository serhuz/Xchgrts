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

package xyz.randomcode.xchgrts

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.randomcode.xchgrts.client.JsonParamInterceptor
import xyz.randomcode.xchgrts.domain.ExchangeRateApi
import xyz.randomcode.xchgrts.domain.RateDataUseCase
import xyz.randomcode.xchgrts.domain.XchgrtsDb
import xyz.randomcode.xchgrts.domain.util.CurrencyInfoProvider
import xyz.randomcode.xchgrts.domain.util.FlagResourceProvider
import xyz.randomcode.xchgrts.entities.CurrencyData
import xyz.randomcode.xchgrts.entities.CurrencyDataJsonAdapter
import xyz.randomcode.xchgrts.entities.WidgetSettings
import xyz.randomcode.xchgrts.main.ExchangeRatesViewModel
import xyz.randomcode.xchgrts.util.DrawableResProvider
import xyz.randomcode.xchgrts.util.Prefs
import xyz.randomcode.xchgrts.widgets.config.CurrencySelectionViewModel
import java.util.Locale
import java.util.concurrent.TimeUnit

@Suppress("unused")
class Xchgrts : Application() {

    private val networkModule = module {
        single<Moshi> {
            Moshi.Builder()
                .build()
                .run {
                    newBuilder()
                        .add(CurrencyData::class.java, CurrencyDataJsonAdapter(this))
                        .build()
                }
        }
        single {
            OkHttpClient.Builder()
                .connectTimeout(10L, TimeUnit.SECONDS)
                .readTimeout(20L, TimeUnit.SECONDS)
                .writeTimeout(20L, TimeUnit.SECONDS)
                .addInterceptor(JsonParamInterceptor())
                .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
                .build()
        }
        single<Retrofit> {
            Retrofit.Builder()
                .baseUrl(NBU_ADDRESS)
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .build()
        }
        single {
            get<Retrofit>().create(ExchangeRateApi::class.java)
        }
    }

    private val dbModule = module {
        single<XchgrtsDb> {
            Room.databaseBuilder(get(), XchgrtsDb::class.java, DB_NAME).build()
        }
        single {
            get<XchgrtsDb>().exchangeRateDao()
        }
    }

    private val appModule = module {
        single<Locale> {
            get<Context>().resources.configuration.locales.get(0) ?: Locale.getDefault()
        }
        single<JsonAdapter<WidgetSettings>> { get<Moshi>().adapter(WidgetSettings::class.java) }
        single<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(get()) }
        single { Prefs(get(), get()) }
        single<FlagResourceProvider> { DrawableResProvider(get()) }
        single { CurrencyInfoProvider(get(), get()) }
        single { RateDataUseCase(get(), get(), get()) }
        viewModel { ExchangeRatesViewModel(get(), get(), get()) }
        viewModel { CurrencySelectionViewModel(get(), get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(applicationContext)
            modules(networkModule, dbModule, appModule)
        }
    }

    companion object {
        private const val NBU_ADDRESS = "https://bank.gov.ua"
        private const val DB_NAME = "xchgrts.db"
    }
}
