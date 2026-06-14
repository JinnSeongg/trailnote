package com.example.trailnote.feature.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.databinding.ItemMilestoneBinding
import com.example.trailnote.domain.model.Milestone

class MilestoneAdapter(
    sourceItems: List<MilestoneUiModel>,
    private val onClick: (Milestone) -> Unit,
    private val onLongClick: (Milestone) -> Unit = {},
    private val isSelected: (Milestone) -> Boolean = { false }
) : RecyclerView.Adapter<MilestoneAdapter.ViewHolder>() {
    private val items = sourceItems.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMilestoneBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onClick, onLongClick, isSelected)
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: MilestoneUiModel) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    fun submitList(newItems: List<MilestoneUiModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Milestone? = items.getOrNull(position)?.milestone

    class ViewHolder(private val binding: ItemMilestoneBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: MilestoneUiModel,
            onClick: (Milestone) -> Unit,
            onLongClick: (Milestone) -> Unit,
            isSelected: (Milestone) -> Boolean
        ) {
            val milestone = item.milestone
            binding.titleText.text = milestone.title
            binding.metaText.text = "${item.statusText} \u00B7 ${item.lastWorkText}"
            binding.progressBar.progress = item.progress
            binding.progressText.text = "${item.progress}%"
            binding.root.setBackgroundResource(if (isSelected(milestone)) R.drawable.bg_card_selected else R.drawable.bg_card)
            binding.root.setOnClickListener { onClick(milestone) }
            binding.root.setOnLongClickListener {
                onLongClick(milestone)
                true
            }
        }
    }
}

data class MilestoneUiModel(
    val milestone: Milestone,
    val doneCount: Int,
    val totalCount: Int,
    val progress: Int,
    val isCompleted: Boolean,
    val lastWorkedAt: Long?,
    val statusText: String,
    val lastWorkText: String
)
