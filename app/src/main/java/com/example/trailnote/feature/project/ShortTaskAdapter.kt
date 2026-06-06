package com.example.trailnote.feature.project

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.databinding.ItemShortTaskBinding
import com.example.trailnote.domain.model.ShortTask

class ShortTaskAdapter(
    tasks: List<ShortTask>,
    private val onClick: (ShortTask) -> Unit = {},
    private val onLongClick: (ShortTask) -> Unit = {},
    private val isSelectionMode: () -> Boolean = { false },
    private val isSelected: (ShortTask) -> Boolean = { false }
) : RecyclerView.Adapter<ShortTaskAdapter.ViewHolder>() {
    private val items = tasks.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemShortTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            item = items[position],
            onItemClick = onClick,
            onItemLongClick = onLongClick,
            isSelectionMode = isSelectionMode,
            isSelected = isSelected
        ) { checked ->
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                items[adapterPosition] = items[adapterPosition].copy(isDone = checked)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: ShortTask) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    fun submitList(newItems: List<ShortTask>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): ShortTask? = items.getOrNull(position)

    class ViewHolder(private val binding: ItemShortTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        private val defaultTextColors: ColorStateList = binding.checkBox.textColors

        fun bind(
            item: ShortTask,
            onItemClick: (ShortTask) -> Unit,
            onItemLongClick: (ShortTask) -> Unit,
            isSelectionMode: () -> Boolean,
            isSelected: (ShortTask) -> Boolean,
            onChecked: (Boolean) -> Unit
        ) {
            binding.checkBox.setOnClickListener(null)
            binding.checkBox.text = item.title
            binding.checkBox.isChecked = item.isDone
            applyCompletionStyle(item.isDone)
            binding.checkBox.setBackgroundResource(if (isSelected(item)) R.drawable.bg_short_task_selected else android.R.color.transparent)
            binding.checkBox.setOnClickListener {
                if (isSelectionMode()) {
                    binding.checkBox.isChecked = item.isDone
                    onItemClick(item)
                } else {
                    val checked = binding.checkBox.isChecked
                    onChecked(checked)
                    applyCompletionStyle(checked)
                }
            }
            binding.checkBox.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }

        private fun applyCompletionStyle(isDone: Boolean) {
            binding.checkBox.paintFlags = if (isDone) {
                binding.checkBox.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.checkBox.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            binding.checkBox.setTextColor(
                if (isDone) defaultTextColors.withAlpha(COMPLETED_TEXT_ALPHA) else defaultTextColors
            )
        }

        private companion object {
            const val COMPLETED_TEXT_ALPHA = 110
        }
    }
}
