package com.example.trailnote.feature.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.core.util.ProgressCalculator
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.ItemProjectBinding
import com.example.trailnote.domain.model.Project

class ProjectAdapter(
    items: List<Project>,
    private val onClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {
    private val items = items.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position], onClick)

    override fun getItemCount(): Int = items.size

    fun submitList(projects: List<Project>) {
        items.clear()
        items.addAll(projects)
        notifyDataSetChanged()
    }

    fun addItem(item: Project) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    class ViewHolder(private val binding: ItemProjectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Project, onClick: (Project) -> Unit) {
            val milestoneProgresses = InMemoryDataStore.getMilestonesByProject(item.id)
                .map { milestone -> ProgressCalculator.milestoneProgress(InMemoryDataStore.getShortTasksByMilestone(milestone.id)) }
            val progress = ProgressCalculator.projectProgress(milestoneProgresses)
            binding.titleText.text = item.title
            binding.metaText.text = "${item.status} \u00B7 \uB9C8\uC9C0\uB9C9 \uC791\uC5C5 ${item.targetDate.replace('-', '.')}"
            binding.progressBar.progress = progress
            binding.progressText.text = "$progress%"
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
