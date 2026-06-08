package com.example.trailnote.feature.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.databinding.ItemHomeGoalBinding
import com.example.trailnote.domain.model.GrowthColorPalette

class HomeGoalAdapter(
    goals: List<HomeGoalItem>,
    private val onDoneChange: (HomeGoalItem, Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<HomeGoalAdapter.ViewHolder>() {
    private val items = goals.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHomeGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position]) { checked ->
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                items[adapterPosition] = items[adapterPosition].copy(isDone = checked)
                onDoneChange(items[adapterPosition], checked)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<HomeGoalItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemHomeGoalBinding) : RecyclerView.ViewHolder(binding.root) {
        private val defaultTextColors: ColorStateList = binding.checkBox.textColors

        fun bind(item: HomeGoalItem, onChecked: (Boolean) -> Unit) {
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.text = item.title
            binding.checkBox.isChecked = item.isDone
            Log.d(TAG, "homeGoalAdapter bind title=${item.title} parentColor=${item.colorHex}")
            binding.checkBox.buttonTintList = ColorStateList.valueOf(parseColor(item.colorHex))
            applyCompletionStyle(item.isDone)
            binding.checkBox.setOnCheckedChangeListener { _, checked ->
                onChecked(checked)
                applyCompletionStyle(checked)
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
