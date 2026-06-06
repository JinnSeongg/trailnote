package com.example.trailnote.feature.growth

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.ItemRoutineBinding
import com.example.trailnote.domain.model.RepeatType
import com.example.trailnote.domain.model.Routine

class RoutineAdapter(
    routines: List<Routine>
) : RecyclerView.Adapter<RoutineAdapter.ViewHolder>() {
    private val items = routines.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            item = items[position],
            onChecked = { checked ->
                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val updated = items[adapterPosition].copy(isDoneToday = checked)
                    items[adapterPosition] = updated
                    InMemoryDataStore.updateRoutineDone(updated.id, checked)
                }
            },
            onRepeatToggle = {
                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val nextRepeatType = items[adapterPosition].repeatType.nextToggle()
                    val updated = items[adapterPosition].copy(repeatType = nextRepeatType)
                    items[adapterPosition] = updated
                    InMemoryDataStore.updateRoutineRepeatType(updated.id, nextRepeatType)
                    notifyItemChanged(adapterPosition)
                }
            }
        )
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: Routine) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    class ViewHolder(private val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root) {
        private val defaultTextColors: ColorStateList = binding.checkBox.textColors

        fun bind(item: Routine, onChecked: (Boolean) -> Unit, onRepeatToggle: () -> Unit) {
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.text = item.title
            binding.checkBox.isChecked = item.isDoneToday
            binding.repeatTypeText.text = item.repeatType.displayText()
            binding.repeatTypeText.setOnClickListener { onRepeatToggle() }
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

private fun RepeatType.displayText(): String {
    return when (this) {
        RepeatType.Daily -> "\uB9E4\uC77C"
        RepeatType.Weekly,
        RepeatType.Monthly -> "\uB9E4\uC8FC"
    }
}

private fun RepeatType.nextToggle(): RepeatType {
    return when (this) {
        RepeatType.Daily -> RepeatType.Weekly
        RepeatType.Weekly,
        RepeatType.Monthly -> RepeatType.Daily
    }
}
