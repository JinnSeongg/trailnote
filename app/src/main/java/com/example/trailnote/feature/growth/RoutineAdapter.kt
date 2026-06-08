package com.example.trailnote.feature.growth

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.databinding.ItemRoutineBinding
import com.example.trailnote.domain.model.GrowthColorPalette
import com.example.trailnote.domain.model.Routine

class RoutineAdapter(
    routines: List<Routine>,
    private val onClick: (Routine) -> Unit = {},
    private val onLongClick: (Routine) -> Unit = {},
    private val onDoneChange: (Routine, Boolean) -> Unit = { _, _ -> },
    private val onFixedStateChange: (Routine, Boolean) -> Unit = { _, _ -> },
    private val isSelectionMode: () -> Boolean = { false },
    private val isSelected: (Routine) -> Boolean = { false }
) : RecyclerView.Adapter<RoutineAdapter.ViewHolder>() {
    private val items = routines.toMutableList()
    private var colorHex: String = GrowthColorPalette.DEFAULT_COLOR

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            item = items[position],
            onItemClick = onClick,
            onItemLongClick = onLongClick,
            isSelectionMode = isSelectionMode,
            isSelected = isSelected,
            onChecked = { checked ->
                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val updated = items[adapterPosition].copy(isDoneToday = checked)
                    items[adapterPosition] = updated
                    onDoneChange(updated, checked)
                }
            },
            colorHex = colorHex,
            onFixedToggle = {
                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val nextIsFixed = !items[adapterPosition].isFixed
                    val updated = items[adapterPosition].copy(isFixed = nextIsFixed)
                    items[adapterPosition] = updated
                    onFixedStateChange(updated, nextIsFixed)
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

    fun submitList(newItems: List<Routine>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun submitList(newItems: List<Routine>, colorHex: String) {
        this.colorHex = colorHex
        submitList(newItems)
    }

    fun getItem(position: Int): Routine? = items.getOrNull(position)

    class ViewHolder(private val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root) {
        private val defaultTextColors: ColorStateList = binding.checkBox.textColors

        fun bind(
            item: Routine,
            onItemClick: (Routine) -> Unit,
            onItemLongClick: (Routine) -> Unit,
            isSelectionMode: () -> Boolean,
            isSelected: (Routine) -> Boolean,
            onChecked: (Boolean) -> Unit,
            colorHex: String,
            onFixedToggle: () -> Unit
        ) {
            binding.checkBox.setOnClickListener(null)
            binding.repeatTypeText.setOnClickListener(null)
            binding.checkBox.text = item.title
            binding.checkBox.isChecked = item.isDoneToday
            binding.repeatTypeText.text = item.fixedStateText()
            Log.d(TAG, "routineAdapter bind title=${item.title} parentColor=$colorHex")
            binding.checkBox.buttonTintList = ColorStateList.valueOf(parseColor(colorHex))
            binding.root.setBackgroundResource(if (isSelected(item)) R.drawable.bg_short_task_selected else android.R.color.transparent)
            applyCompletionStyle(item.isDoneToday)

            binding.root.setOnClickListener {
                if (isSelectionMode()) onItemClick(item)
            }
            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
            binding.checkBox.setOnClickListener {
                if (isSelectionMode()) {
                    binding.checkBox.isChecked = item.isDoneToday
                    onItemClick(item)
                } else {
                    val checked = binding.checkBox.isChecked
                    onChecked(checked)
                    applyCompletionStyle(checked)
                }
            }
            binding.repeatTypeText.setOnClickListener {
                if (isSelectionMode()) {
                    onItemClick(item)
                } else {
                    onFixedToggle()
                }
            }
        }

        private fun parseColor(colorHex: String): Int {
            return runCatching { Color.parseColor(colorHex) }.getOrDefault(Color.parseColor(GrowthColorPalette.DEFAULT_COLOR))
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
            const val TAG = "GrowthColorDebug"
            const val COMPLETED_TEXT_ALPHA = 110
        }
    }
}

private fun Routine.fixedStateText(): String = if (isFixed) "\uACE0\uC815" else "\uC77C\uBC18"
