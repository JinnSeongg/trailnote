package com.example.trailnote.feature.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.databinding.ItemProjectSectionBinding
import com.example.trailnote.domain.model.Project

class ProjectSectionAdapter(
    private val onProjectClick: (Project) -> Unit,
    private val onCategoryClick: (String) -> Unit,
    private val onCategoryAddClick: (String) -> Unit
) : RecyclerView.Adapter<ProjectSectionAdapter.ViewHolder>() {
    private val items = mutableListOf<ProjectSection>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemProjectSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onProjectClick, onCategoryClick, onCategoryAddClick)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(sections: List<ProjectSection>) {
        items.clear()
        items.addAll(sections)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemProjectSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            section: ProjectSection,
            onProjectClick: (Project) -> Unit,
            onCategoryClick: (String) -> Unit,
            onCategoryAddClick: (String) -> Unit
        ) {
            binding.categoryText.text = section.category
            binding.categoryText.setOnClickListener { onCategoryClick(section.category) }
            binding.categoryAddButton.setOnClickListener { onCategoryAddClick(section.category) }
            binding.projectCardList.layoutManager = LinearLayoutManager(binding.root.context)
            binding.projectCardList.adapter = ProjectAdapter(section.projects, onProjectClick)
        }
    }
}

data class ProjectSection(
    val category: String,
    val projects: List<Project>
)
