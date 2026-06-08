package com.example.trailnote.feature.growth

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.domain.model.GrowthColorPalette
import com.example.trailnote.databinding.ItemGrowthTopicBinding
import com.example.trailnote.domain.model.GrowthTopic

class GrowthTopicAdapter(
    sourceItems: List<GrowthTopic>,
    private val onClick: (GrowthTopic) -> Unit,
    private val onLongClick: (GrowthTopic) -> Unit = {},
    private val isSelected: (GrowthTopic) -> Boolean = { false }
) : RecyclerView.Adapter<GrowthTopicAdapter.ViewHolder>() {
    private val items = sourceItems.toMutableList()
    private var colorHex: String = GrowthColorPalette.DEFAULT_COLOR

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGrowthTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], colorHex, onClick, onLongClick, isSelected)
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: GrowthTopic) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    fun submitList(newItems: List<GrowthTopic>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun submitList(newItems: List<GrowthTopic>, colorHex: String) {
        this.colorHex = colorHex
        submitList(newItems)
    }

    fun getItem(position: Int): GrowthTopic? = items.getOrNull(position)

    class ViewHolder(private val binding: ItemGrowthTopicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: GrowthTopic,
            colorHex: String,
            onClick: (GrowthTopic) -> Unit,
            onLongClick: (GrowthTopic) -> Unit,
            isSelected: (GrowthTopic) -> Boolean
        ) {
            val color = parseColor(colorHex)
            Log.d(TAG, "topicAdapter bind title=${item.title} parentColor=$colorHex")
            binding.titleText.text = levelTitle(item.title, item.level, color)
            binding.metaText.text = "${item.description} · ${item.progressPercent}%"
            binding.progressBar.progress = item.progressPercent
            binding.progressBar.progressTintList = ColorStateList.valueOf(color)
            binding.root.setBackgroundResource(if (isSelected(item)) R.drawable.bg_card_selected else R.drawable.bg_card)
            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }

        private fun levelTitle(title: String, level: Int, color: Int): SpannableString {
            val levelText = "Lv.$level"
            val text = "$title  $levelText"
            return SpannableString(text).apply {
                setSpan(
                    ForegroundColorSpan(color),
                    text.indexOf(levelText),
                    text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        private fun parseColor(colorHex: String): Int {
            return runCatching { Color.parseColor(colorHex) }.getOrDefault(Color.parseColor(GrowthColorPalette.DEFAULT_COLOR))
        }

        private companion object {
            const val TAG = "GrowthColorDebug"
        }
    }
}
