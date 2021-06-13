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

package xyz.randomcode.xchgrts.widgets.config

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import xyz.randomcode.xchgrts.entities.CurrencyListItem
import xyz.randomcode.xchgrts.databinding.ItemCurrencyBinding

class CurrencySelectionAdapter(private val viewModel: CurrencySelectionViewModel) :
    ListAdapter<CurrencyListItem, CurrencyListItemViewHolder>(DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).stableId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyListItemViewHolder =
        LayoutInflater.from(parent.context)
            .let { ItemCurrencyBinding.inflate(it, parent, false) }
            .let { CurrencyListItemViewHolder(it, viewModel) }


    override fun onBindViewHolder(holder: CurrencyListItemViewHolder, position: Int) {
        getItem(position).let(holder::bind)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CurrencyListItem>() {
            override fun areItemsTheSame(
                oldItem: CurrencyListItem,
                newItem: CurrencyListItem
            ): Boolean = oldItem.letterCode == newItem.letterCode

            override fun areContentsTheSame(
                oldItem: CurrencyListItem,
                newItem: CurrencyListItem
            ): Boolean = oldItem == newItem
        }
    }
}

class CurrencyListItemViewHolder(
    private val binding: ItemCurrencyBinding,
    private val viewModel: CurrencySelectionViewModel
) : RecyclerView.ViewHolder(binding.root), CurrencyListItemClickListener {

    fun bind(item: CurrencyListItem) {
        binding.item = item
        binding.listener = this
    }

    override fun updateItemSelection(letterCode: String) {
        viewModel.updateItemSelection(letterCode)
    }
}

interface CurrencyListItemClickListener {
    fun updateItemSelection(letterCode: String)
}
