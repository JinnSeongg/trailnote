package com.example.trailnote.feature.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.databinding.ItemLogEntryBinding
import com.example.trailnote.domain.model.LogEntry

class LogEntryAdapter(
    items: List<LogEntry>,
    private val onClick: (LogEntry) -> Unit
) : RecyclerView.Adapter<LogEntryAdapter.ViewHolder>() {
    private val items = items.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLogEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position], onClick)

    override fun getItemCount(): Int = items.size

    fun addItem(item: LogEntry) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    class ViewHolder(private val binding: ItemLogEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LogEntry, onClick: (LogEntry) -> Unit) {
            binding.titleText.text = item.title.ifBlank { "\uC81C\uBAA9 \uC5C6\uC74C" }
            binding.bodyText.text = item.content
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
