package com.example.trailnote.feature.growth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.databinding.ItemGrowthAreaBinding
import com.example.trailnote.domain.model.GrowthArea

class GrowthAreaAdapter(
    items: List<GrowthArea>,
    private val onClick: (GrowthArea) -> Unit,
    private val onLongClick: (GrowthArea) -> Unit = {},
    private val isSelected: (GrowthArea) -> Boolean = { false }
) : RecyclerView.Adapter<GrowthAreaAdapter.ViewHolder>() {
    private val items = items.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGrowthAreaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onClick, onLongClick, isSelected)
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: GrowthArea) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    fun submitList(newItems: List<GrowthArea>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): GrowthArea? = items.getOrNull(position)

    class ViewHolder(private val binding: ItemGrowthAreaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: GrowthArea,
            onClick: (GrowthArea) -> Unit,
            onLongClick: (GrowthArea) -> Unit,
            isSelected: (GrowthArea) -> Boolean
        ) {
            binding.titleText.text = "${item.title}  Lv.${item.level}"
            binding.metaText.text = "${item.description} · ${item.exp}%"
            binding.progressBar.progress = item.exp
            binding.root.setBackgroundResource(if (isSelected(item)) R.drawable.bg_card_selected else R.drawable.bg_card)
            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }
}
