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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import xyz.randomcode.xchgrts.R
import xyz.randomcode.xchgrts.databinding.ItemExchangeRateBinding

class ExchangeRatesAdapter : ListAdapter<RateListItem, ExchangeRateViewHolder>(DIFF_CALLBACK) {

    val favUpdate = MutableLiveData<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeRateViewHolder =
        ItemExchangeRateBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { ExchangeRateViewHolder(it, favUpdate) }


    override fun onBindViewHolder(holder: ExchangeRateViewHolder, position: Int) {
        getItem(position).let(holder::bind)
    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RateListItem>() {
            override fun areItemsTheSame(oldItem: RateListItem, newItem: RateListItem): Boolean =
                oldItem.isFavorite == newItem.isFavorite && oldItem.data.letterCode == newItem.data.letterCode

            override fun areContentsTheSame(oldItem: RateListItem, newItem: RateListItem): Boolean =
                oldItem == newItem
        }
    }
}

class ExchangeRateViewHolder(
    private val binding: ItemExchangeRateBinding,
    private val favUpdate: MutableLiveData<String>
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: RateListItem) {
        binding.currencyCode.text = "${item.data.units} ${item.data.letterCode}"
        binding.exchangeRate.text = item.data.amount
        binding.currencyName.text = item.data.fullName
        binding.currencyFlag.setImageResource(item.data.flagRes)
        binding.favIcon.setImageResource(if (item.isFavorite) R.drawable.ic_filled_star else R.drawable.ic_outline_star)
        binding.favIcon.setOnClickListener { favUpdate.value = item.data.letterCode }
    }
}
