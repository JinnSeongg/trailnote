package com.example.trailnote.feature.growth

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.databinding.ItemRoutineBinding
import com.example.trailnote.domain.model.Routine

class RoutineAdapter(
    routines: List<Routine>
) : RecyclerView.Adapter<RoutineAdapter.ViewHolder>() {
    private val items = routines.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position]) { checked ->
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                items[adapterPosition] = items[adapterPosition].copy(isDoneToday = checked)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: Routine) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    class ViewHolder(private val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root) {
        private val defaultTextColors: ColorStateList = binding.checkBox.textColors

        fun bind(item: Routine, onChecked: (Boolean) -> Unit) {
            val fixed = if (item.isFixed) "고정" else "오늘"
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.text = item.title
            binding.checkBox.isChecked = item.isDoneToday
            binding.badgeText.text = fixed
            applyCompletionStyle(item.isDoneToday)
            binding.checkBox.setOnCheckedChangeListener { _, checked ->
                onChecked(checked)
                applyCompletionStyle(checked)
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
