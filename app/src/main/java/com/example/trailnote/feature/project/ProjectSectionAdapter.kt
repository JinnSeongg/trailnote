package com.example.trailnote.feature.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.core.selection.RecyclerDragSelectionHelper
import com.example.trailnote.core.selection.SelectionController
import com.example.trailnote.databinding.ItemProjectSectionBinding
import com.example.trailnote.domain.model.Project

class ProjectSectionAdapter(
    private val onProjectClick: (Project) -> Unit,
    private val onProjectLongClick: (Project) -> Unit,
    private val isProjectSelected: (Project) -> Boolean,
    private val selectionController: SelectionController,
    private val onProjectDragStarted: (Project) -> Unit,
    private val onCategoryClick: (String) -> Unit,
    private val onCategoryAddClick: (String) -> Unit
) : RecyclerView.Adapter<ProjectSectionAdapter.ViewHolder>() {
    private val items = mutableListOf<ProjectSection>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemProjectSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            items[position],
            onProjectClick,
            onProjectLongClick,
            isProjectSelected,
            selectionController,
            onProjectDragStarted,
            onCategoryClick,
            onCategoryAddClick
        )
    }

    override fun getItemCount(): Int = items.size

    fun submitList(sections: List<ProjectSection>) {
        items.clear()
        items.addAll(sections)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemProjectSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        private var dragSelectionHelper: RecyclerDragSelectionHelper? = null

        fun bind(
            section: ProjectSection,
            onProjectClick: (Project) -> Unit,
            onProjectLongClick: (Project) -> Unit,
            isProjectSelected: (Project) -> Boolean,
            selectionController: SelectionController,
            onProjectDragStarted: (Project) -> Unit,
            onCategoryClick: (String) -> Unit,
            onCategoryAddClick: (String) -> Unit
        ) {
            binding.categoryText.text = section.category
            binding.categoryText.setOnClickListener { onCategoryClick(section.category) }
            binding.categoryAddButton.setOnClickListener { onCategoryAddClick(section.category) }
            binding.projectCardList.layoutManager = LinearLayoutManager(binding.root.context)
            val adapter = ProjectAdapter(section.projects, onProjectClick, onProjectLongClick, isProjectSelected)
            binding.projectCardList.adapter = adapter
            dragSelectionHelper?.let { binding.projectCardList.removeOnItemTouchListener(it) }
            dragSelectionHelper = RecyclerDragSelectionHelper(
                recyclerView = binding.projectCardList,
                selectionController = selectionController,
                getItemId = { position -> adapter.getItem(position)?.id },
                getItemScope = { position -> adapter.getItem(position)?.let { "project-category:${it.category}" } },
                isItemSelected = { position -> adapter.getItem(position)?.let(isProjectSelected) == true },
                onDragStarted = { position ->
                    adapter.getItem(position)?.let(onProjectDragStarted)
                },
                onSelectionChanged = { adapter.notifyDataSetChanged() }
            ).also { binding.projectCardList.addOnItemTouchListener(it) }
        }
    }
}

data class ProjectSection(
    val category: String,
    val projects: List<Project>
)
