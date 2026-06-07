package com.example.trailnote.feature.home

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.databinding.ItemHomeTaskBinding
import com.example.trailnote.domain.model.Task

class HomeTaskAdapter(
    tasks: List<Task>,
    private val onDoneChange: (Task, Boolean) -> Unit = { _, _ -> },
    private val onLongClick: (Task) -> Unit = {}
) : RecyclerView.Adapter<HomeTaskAdapter.ViewHolder>() {
    private val items = tasks.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHomeTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            item = items[position],
            onLongClick = onLongClick,
            onChecked = { checked ->
                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    items[adapterPosition] = items[adapterPosition].copy(isDone = checked)
                    onDoneChange(items[adapterPosition], checked)
                }
            }
        )
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: Task) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    fun submitList(newItems: List<Task>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemHomeTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        private val defaultTextColors: ColorStateList = binding.checkBox.textColors

        fun bind(item: Task, onLongClick: (Task) -> Unit, onChecked: (Boolean) -> Unit) {
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.text = item.title
            binding.checkBox.isChecked = item.isDone
            applyCompletionStyle(item.isDone)
            binding.checkBox.setOnCheckedChangeListener { _, checked ->
                onChecked(checked)
                applyCompletionStyle(checked)
            }
            binding.checkBox.setOnLongClickListener {
                onLongClick(item)
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
