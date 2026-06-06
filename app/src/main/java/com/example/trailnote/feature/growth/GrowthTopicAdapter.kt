package com.example.trailnote.feature.growth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.databinding.ItemGrowthTopicBinding
import com.example.trailnote.domain.model.GrowthTopic

class GrowthTopicAdapter(
    sourceItems: List<GrowthTopic>,
    private val onClick: (GrowthTopic) -> Unit
) : RecyclerView.Adapter<GrowthTopicAdapter.ViewHolder>() {
    private val items = sourceItems.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGrowthTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position], onClick)

    override fun getItemCount(): Int = items.size

    fun addItem(item: GrowthTopic) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    class ViewHolder(private val binding: ItemGrowthTopicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GrowthTopic, onClick: (GrowthTopic) -> Unit) {
            binding.titleText.text = "${item.title}  Lv.${item.level}"
            binding.metaText.text = "${item.description} · ${item.exp}%"
            binding.progressBar.progress = item.exp
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
