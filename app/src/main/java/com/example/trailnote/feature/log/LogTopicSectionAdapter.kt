package com.example.trailnote.feature.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.databinding.ItemLogTopicSectionBinding
import com.example.trailnote.domain.model.LogEntry
import com.example.trailnote.domain.model.LogTopic

class LogTopicSectionAdapter(
    private val topics: List<LogTopic>,
    private val entriesForTopic: (String) -> List<LogEntry>,
    private val onTopicClick: (LogTopic) -> Unit,
    private val onEntryClick: (LogEntry) -> Unit,
    private val onAddClick: (LogTopic) -> Unit
) : RecyclerView.Adapter<LogTopicSectionAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLogTopicSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            topic = topics[position],
            entries = entriesForTopic(topics[position].id),
            onTopicClick = onTopicClick,
            onEntryClick = onEntryClick,
            onAddClick = onAddClick
        )
    }

    override fun getItemCount(): Int = topics.size

    fun closeOpen() = Unit

    class ViewHolder(private val binding: ItemLogTopicSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            topic: LogTopic,
            entries: List<LogEntry>,
            onTopicClick: (LogTopic) -> Unit,
            onEntryClick: (LogEntry) -> Unit,
            onAddClick: (LogTopic) -> Unit
        ) {
            binding.topicTitleText.text = topic.title
            binding.topicTitleText.setOnClickListener { onTopicClick(topic) }
            binding.entryPreviewList.layoutManager = LinearLayoutManager(binding.root.context)
            binding.entryPreviewList.adapter = LogEntryPreviewAdapter(entries.take(6), onEntryClick)
            binding.topicAddButton.setOnClickListener { onAddClick(topic) }
        }
    }
}
