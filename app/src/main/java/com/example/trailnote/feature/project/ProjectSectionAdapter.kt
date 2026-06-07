package com.example.trailnote.feature.project

import android.view.LayoutInflater
import android.view.View
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
    private val onCategoryClick: (ProjectSection) -> Unit,
    private val onCategoryAddClick: (ProjectSection) -> Unit,
    private val onCategoryLongClick: (ProjectSection, View) -> Unit,
    private val projectProgressProvider: (Project) -> Int = { 0 }
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
            onCategoryAddClick,
            onCategoryLongClick,
            projectProgressProvider
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
            onCategoryClick: (ProjectSection) -> Unit,
            onCategoryAddClick: (ProjectSection) -> Unit,
            onCategoryLongClick: (ProjectSection, View) -> Unit,
            projectProgressProvider: (Project) -> Int
        ) {
            binding.categoryText.text = section.category
            binding.categoryText.setOnClickListener { onCategoryClick(section) }
            binding.categoryText.setOnLongClickListener {
                onCategoryLongClick(section, it)
                true
            }
            binding.categoryAddButton.setOnClickListener { onCategoryAddClick(section) }
            binding.projectCardList.layoutManager = LinearLayoutManager(binding.root.context)
            val adapter = ProjectAdapter(
                section.projects,
                onProjectClick,
                onProjectLongClick,
                isProjectSelected,
                projectProgressProvider
            )
            binding.projectCardList.adapter = adapter
            dragSelectionHelper?.let { binding.projectCardList.removeOnItemTouchListener(it) }
            dragSelectionHelper = RecyclerDragSelectionHelper(
                recyclerView = binding.projectCardList,
                selectionController = selectionController,
                getItemId = { position -> adapter.getItem(position)?.id },
                getItemScope = { position -> adapter.getItem(position)?.let { "project-category:${it.categoryId ?: "none"}" } },
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
    val categoryId: Long?,
    val category: String,
    val projects: List<Project>
)
