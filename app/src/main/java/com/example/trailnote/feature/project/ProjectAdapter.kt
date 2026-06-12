package com.example.trailnote.feature.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.databinding.ItemProjectBinding
import com.example.trailnote.domain.model.Project

class ProjectAdapter(
    items: List<Project>,
    private val onClick: (Project) -> Unit,
    private val onLongClick: (Project) -> Unit = {},
    private val isSelected: (Project) -> Boolean = { false },
    private val uiStateProvider: (Project) -> ProjectUiState = { ProjectUiState() }
) : RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {
    private val items = items.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onClick, onLongClick, isSelected, uiStateProvider)
    }

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

    fun getItem(position: Int): Project? = items.getOrNull(position)

    class ViewHolder(private val binding: ItemProjectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Project,
            onClick: (Project) -> Unit,
            onLongClick: (Project) -> Unit,
            isSelected: (Project) -> Boolean,
            uiStateProvider: (Project) -> ProjectUiState
        ) {
            val uiState = uiStateProvider(item)
            binding.titleText.text = item.title
            binding.metaText.text = "${uiState.statusText} \u00B7 ${uiState.lastWorkText}"
            binding.progressBar.progress = uiState.progress
            binding.progressText.text = "${uiState.progress}%"
            binding.root.setBackgroundResource(if (isSelected(item)) R.drawable.bg_card_selected else R.drawable.bg_card)
            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }
}

data class ProjectUiState(
    val progress: Int = 0,
    val statusText: String = "\uC9C4\uD589\uC911",
    val lastWorkText: String = "\uB9C8\uC9C0\uB9C9 \uC791\uC5C5 \uBBF8\uC815",
    val lastWorkedAt: Long? = null
)
