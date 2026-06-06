package com.example.trailnote.feature.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.databinding.ItemLogEntryPreviewBinding
import com.example.trailnote.domain.model.LogEntry

class LogEntryPreviewAdapter(
    private val items: List<LogEntry>,
    private val onClick: (LogEntry) -> Unit,
    private val onLongClick: (LogEntry) -> Unit = {},
    private val isSelected: (LogEntry) -> Boolean = { false }
) : RecyclerView.Adapter<LogEntryPreviewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLogEntryPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position], onClick, onLongClick, isSelected)

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): LogEntry? = items.getOrNull(position)

    class ViewHolder(private val binding: ItemLogEntryPreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: LogEntry,
            onClick: (LogEntry) -> Unit,
            onLongClick: (LogEntry) -> Unit,
            isSelected: (LogEntry) -> Boolean
        ) {
            binding.titleText.text = "\u00B7 ${item.title.ifBlank { "\uC81C\uBAA9 \uC5C6\uC74C" }}"
            binding.titleText.setBackgroundResource(if (isSelected(item)) R.drawable.bg_short_task_selected else android.R.color.transparent)
            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }
}
