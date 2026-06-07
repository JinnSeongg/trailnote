package com.example.trailnote.feature.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.databinding.ItemMilestoneBinding
import com.example.trailnote.domain.model.Milestone

class MilestoneAdapter(
    sourceItems: List<Milestone>,
    private val onClick: (Milestone) -> Unit,
    private val onLongClick: (Milestone) -> Unit = {},
    private val isSelected: (Milestone) -> Boolean = { false },
    private val taskSummaryProvider: (Milestone) -> TaskSummary = { TaskSummary(0, 0, 0) }
) : RecyclerView.Adapter<MilestoneAdapter.ViewHolder>() {
    private val items = sourceItems.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMilestoneBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onClick, onLongClick, isSelected, taskSummaryProvider)
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: Milestone) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    fun submitList(newItems: List<Milestone>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Milestone? = items.getOrNull(position)

    class ViewHolder(private val binding: ItemMilestoneBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Milestone,
            onClick: (Milestone) -> Unit,
            onLongClick: (Milestone) -> Unit,
            isSelected: (Milestone) -> Boolean,
            taskSummaryProvider: (Milestone) -> TaskSummary
        ) {
            val taskSummary = taskSummaryProvider(item)
            binding.titleText.text = item.title
            binding.metaText.text = "\uBAA9\uD45C ${taskSummary.doneCount}/${taskSummary.totalCount} \u00B7 \uB9C8\uC9C0\uB9C9 \uC791\uC5C5 ${item.targetDate}"
            binding.progressBar.progress = taskSummary.progress
            binding.progressText.text = "${taskSummary.progress}%"
            binding.root.setBackgroundResource(if (isSelected(item)) R.drawable.bg_card_selected else R.drawable.bg_card)
            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }
}

data class TaskSummary(
    val doneCount: Int,
    val totalCount: Int,
    val progress: Int
)
