package com.example.trailnote.feature.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.databinding.ItemLogEntryPreviewBinding
import com.example.trailnote.domain.model.LogEntry

class LogEntryPreviewAdapter(
    private val items: List<LogEntry>,
    private val onClick: (LogEntry) -> Unit
) : RecyclerView.Adapter<LogEntryPreviewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLogEntryPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position], onClick)

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemLogEntryPreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LogEntry, onClick: (LogEntry) -> Unit) {
            binding.titleText.text = "\u00B7 ${item.title.ifBlank { "\uC81C\uBAA9 \uC5C6\uC74C" }}"
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
