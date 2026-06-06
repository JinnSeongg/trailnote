package com.example.trailnote.feature.growth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.databinding.ItemGrowthTopicBinding
import com.example.trailnote.domain.model.GrowthTopic

class GrowthTopicAdapter(
    sourceItems: List<GrowthTopic>,
    private val onClick: (GrowthTopic) -> Unit,
    private val onLongClick: (GrowthTopic) -> Unit = {},
    private val isSelected: (GrowthTopic) -> Boolean = { false }
) : RecyclerView.Adapter<GrowthTopicAdapter.ViewHolder>() {
    private val items = sourceItems.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGrowthTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onClick, onLongClick, isSelected)
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: GrowthTopic) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    fun submitList(newItems: List<GrowthTopic>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): GrowthTopic? = items.getOrNull(position)

    class ViewHolder(private val binding: ItemGrowthTopicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: GrowthTopic,
            onClick: (GrowthTopic) -> Unit,
            onLongClick: (GrowthTopic) -> Unit,
            isSelected: (GrowthTopic) -> Boolean
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
