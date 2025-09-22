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

package xyz.randomcode.xchgrts.main

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import retrofit2.HttpException
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.databinding.FragmentExchangeRatesBinding
import xyz.randomcode.xchgrts.entities.Failure
import xyz.randomcode.xchgrts.entities.Loading
import xyz.randomcode.xchgrts.entities.Success
import xyz.randomcode.xchgrts.util.Prefs
import xyz.randomcode.xchgrts.widgets.WidgetProvider
import java.net.UnknownHostException

class ExchangeRatesFragment : Fragment() {

    private val viewModel: ExchangeRatesViewModel by stateViewModel()
    private val adapter: ExchangeRatesAdapter by lazy { ExchangeRatesAdapter() }
    private val prefs: Prefs by inject()
    private lateinit var binding: FragmentExchangeRatesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentExchangeRatesBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.ratesList.adapter = this.adapter

        binding.ratesRefreshLayout.setOnRefreshListener {
            viewModel.loadRates()
            binding.ratesRefreshLayout.isRefreshing = true
        }

        binding.exchangeRateToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.refresh -> {
                    viewModel.loadRates()
                }

                R.id.showLicenses -> {
                    OssLicensesMenuActivity.setActivityTitle(getString(R.string.menu_oss_licenses));
                    startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
                }

                else -> error("Illegal itemId")
            }
            true
        }

        adapter.favUpdate.observe(viewLifecycleOwner, viewModel::updateFavorites)

        viewModel.items.observe(viewLifecycleOwner, {
            when (it) {
                is Loading -> {
                    binding.exchangeRateProgress.isVisible = true
                }

                is Failure -> {
                    binding.ratesRefreshLayout.isRefreshing = false
                    binding.exchangeRateProgress.isVisible = false
                    showRetrySnackbar(it.reason)
                }

                is Success -> {
                    it.data.fold(HashSet<String>(), { acc, item ->
                        acc.apply { add(item.data.date) }
                    })
                        .single()
                        .let(binding.exchangeRateToolbar::setTitle)

                    adapter.submitList(it.data)

                    binding.ratesRefreshLayout.isRefreshing = false
                    binding.exchangeRateProgress.isVisible = false

                    updateWidgets()
                }
            }
        })
    }

    private fun showRetrySnackbar(reason: Throwable) {
        when (reason) {
            is HttpException, is UnknownHostException -> {
                Snackbar.make(
                    binding.coordinator,
                    R.string.loading_error_http,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.retry) { viewModel.loadRates() }
                    .show()
            }

            else -> {
                Snackbar.make(
                    binding.coordinator,
                    R.string.loading_error,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.finish) { requireActivity().finish() }
                    .show()
            }
        }
    }

    private fun updateWidgets() {
        lifecycleScope.launch {
            val manager = AppWidgetManager.getInstance(requireActivity())
            val applicationContext = requireContext().applicationContext

            withContext(Dispatchers.IO) {
                ComponentName(applicationContext, WidgetProvider::class.java)
                    .let(manager::getAppWidgetIds)
                    .forEach { id ->
                        prefs.loadWidgetSettings(id)
                            ?.let {
                                WidgetProvider.updateWidgets(
                                    applicationContext,
                                    manager,
                                    prefs,
                                    viewModel.case,
                                    it.id
                                )
                            }
                            ?: FirebaseCrashlytics.getInstance()
                                .recordException(IllegalStateException("No settings found for widget $id"))
                    }
            }
        }
    }
}
