package com.example.trailnote.feature.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.core.selection.RecyclerDragSelectionHelper
import com.example.trailnote.core.selection.SelectionController
import com.example.trailnote.databinding.ItemLogTopicSectionBinding
import com.example.trailnote.domain.model.LogEntry
import com.example.trailnote.domain.model.LogTopic

class LogTopicSectionAdapter(
    private val topics: List<LogTopic>,
    private val entriesForTopic: (String) -> List<LogEntry>,
    private val onTopicClick: (LogTopic) -> Unit,
    private val onTopicLongClick: (LogTopic, View) -> Unit = { _, _ -> },
    private val onEntryClick: (LogEntry) -> Unit,
    private val onEntryLongClick: (LogEntry) -> Unit = {},
    private val isEntrySelected: (LogEntry) -> Boolean = { false },
    private val selectionController: SelectionController,
    private val onEntryDragStarted: (LogEntry) -> Unit = {},
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
            onTopicLongClick = onTopicLongClick,
            onEntryClick = onEntryClick,
            onEntryLongClick = onEntryLongClick,
            isEntrySelected = isEntrySelected,
            selectionController = selectionController,
            onEntryDragStarted = onEntryDragStarted,
            onAddClick = onAddClick
        )
    }

    override fun getItemCount(): Int = topics.size

    fun closeOpen() = Unit

    class ViewHolder(private val binding: ItemLogTopicSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        private var dragSelectionHelper: RecyclerDragSelectionHelper? = null

        fun bind(
            topic: LogTopic,
            entries: List<LogEntry>,
            onTopicClick: (LogTopic) -> Unit,
            onTopicLongClick: (LogTopic, View) -> Unit,
            onEntryClick: (LogEntry) -> Unit,
            onEntryLongClick: (LogEntry) -> Unit,
            isEntrySelected: (LogEntry) -> Boolean,
            selectionController: SelectionController,
            onEntryDragStarted: (LogEntry) -> Unit,
            onAddClick: (LogTopic) -> Unit
        ) {
            binding.topicTitleText.text = topic.title
            binding.topicTitleText.setBackgroundResource(android.R.color.transparent)
            binding.topicTitleText.setOnClickListener { onTopicClick(topic) }
            binding.topicTitleText.setOnLongClickListener {
                onTopicLongClick(topic, it)
                true
            }
            binding.entryPreviewList.layoutManager = LinearLayoutManager(binding.root.context)
            val adapter = LogEntryPreviewAdapter(entries.take(6), onEntryClick, onEntryLongClick, isEntrySelected)
            binding.entryPreviewList.adapter = adapter
            dragSelectionHelper?.let { binding.entryPreviewList.removeOnItemTouchListener(it) }
            dragSelectionHelper = RecyclerDragSelectionHelper(
                recyclerView = binding.entryPreviewList,
                selectionController = selectionController,
                getItemId = { position -> adapter.getItem(position)?.id },
                getItemScope = { position -> adapter.getItem(position)?.let { "log-entries:${it.topicId}" } },
                isItemSelected = { position -> adapter.getItem(position)?.let(isEntrySelected) == true },
                onDragStarted = { position -> adapter.getItem(position)?.let(onEntryDragStarted) },
                onSelectionChanged = { adapter.notifyDataSetChanged() }
            ).also { binding.entryPreviewList.addOnItemTouchListener(it) }
            binding.topicAddButton.setOnClickListener { onAddClick(topic) }
        }
    }
}
