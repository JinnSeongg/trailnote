package com.example.trailnote.feature.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.core.util.ProgressCalculator
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.ItemMilestoneBinding
import com.example.trailnote.domain.model.Milestone

class MilestoneAdapter(
    sourceItems: List<Milestone>,
    private val onClick: (Milestone) -> Unit
) : RecyclerView.Adapter<MilestoneAdapter.ViewHolder>() {
    private val items = sourceItems.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMilestoneBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position], onClick)

    override fun getItemCount(): Int = items.size

    fun addItem(item: Milestone) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    class ViewHolder(private val binding: ItemMilestoneBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Milestone, onClick: (Milestone) -> Unit) {
            val tasks = InMemoryDataStore.getShortTasksByMilestone(item.id)
            val progress = ProgressCalculator.milestoneProgress(tasks)
            binding.titleText.text = item.title
            binding.metaText.text = "\uBAA9\uD45C ${tasks.count { it.isDone }}/${tasks.size} \u00B7 \uB9C8\uC9C0\uB9C9 \uC791\uC5C5 ${item.targetDate}"
            binding.progressBar.progress = progress
            binding.progressText.text = "$progress%"
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
