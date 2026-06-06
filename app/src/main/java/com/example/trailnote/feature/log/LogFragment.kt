package com.example.trailnote.feature.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.MainActivity
import com.example.trailnote.R
import com.example.trailnote.core.selection.MoveTarget
import com.example.trailnote.core.selection.MoveTargetDialogFragment
import com.example.trailnote.core.selection.SelectionState
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentLogBinding
import com.example.trailnote.domain.model.LogEntry
import com.example.trailnote.domain.model.LogTopic

class LogFragment : Fragment() {
    private var binding: FragmentLogBinding? = null
    private var selectedCategoryId: String? = null
    private var quickAddTargetTopic: LogTopic? = null
    private var topicSectionAdapter: LogTopicSectionAdapter? = null
    private val selectionStateListener: (SelectionState) -> Unit = {
        topicSectionAdapter?.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentLogBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.root.setHeader("\uAE30\uB85D", action = "\u2315")
        selectionController.addStateListener(selectionStateListener)
        InlineQuickAdd.bind(current.categoryQuickAdd.root) { text ->
            val targetTopic = quickAddTargetTopic
            if (targetTopic == null) {
                val category = InMemoryDataStore.addLogCategory(text)
                selectedCategoryId = category.id
                renderChips()
            } else {
                InMemoryDataStore.addLogEntry(targetTopic.id, text)
            }
            quickAddTargetTopic = null
            renderList()
        }
        current.categoryAddButton.setOnClickListener {
            if (!selectionController.isInSelectionMode) {
                quickAddTargetTopic = null
                InlineQuickAdd.show(current.categoryQuickAdd.root, "\uC0C8 \uCE74\uD14C\uACE0\uB9AC \uC785\uB825")
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                quickAddTargetTopic = null
                InlineQuickAdd.hide(current.categoryQuickAdd.root)
                isEnabled = false
            }
        }.also { callback ->
            current.categoryQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
        renderChips()
        renderList()
    }

    private fun renderChips() {
        val current = binding ?: return
        current.categoryChipContainer.removeAllViews()
        val chipEntries = listOf(null to "\uC804\uCCB4") + InMemoryDataStore.getLogCategories().map { it.id to it.name }
        chipEntries.forEach { (id, label) ->
            val chip = TextView(requireContext()).apply {
                text = label
                textSize = 12f
                setTextColor(resources.getColor(if (selectedCategoryId == id) R.color.white else R.color.trail_text_secondary, null))
                setBackgroundResource(if (selectedCategoryId == id) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
                setOnClickListener {
                    if (!selectionController.isInSelectionMode) {
                        selectedCategoryId = id
                        quickAddTargetTopic = null
                        InlineQuickAdd.hide(current.categoryQuickAdd.root)
                        topicSectionAdapter?.closeOpen()
                        renderChips()
                        renderList()
                    }
                }
            }
            current.categoryChipContainer.addView(chip)
        }
    }

    private fun renderList() {
        val current = binding ?: return
        val visibleTopics = InMemoryDataStore.getLogTopicsByCategory(selectedCategoryId)
        topicSectionAdapter = LogTopicSectionAdapter(
            topics = visibleTopics,
            entriesForTopic = { topicId -> InMemoryDataStore.getLogEntriesByTopic(topicId) },
            onTopicClick = ::handleTopicClick,
            onEntryClick = ::handleEntryClick,
            onEntryLongClick = ::handleEntryLongClick,
            isEntrySelected = ::isEntrySelected,
            selectionController = selectionController,
            onEntryDragStarted = ::prepareEntrySelectionHandlers,
            onAddClick = { topic ->
                if (!selectionController.isInSelectionMode) {
                    quickAddTargetTopic = topic
                    InlineQuickAdd.show(current.categoryQuickAdd.root, "\uC0C8 \uAE30\uB85D \uC81C\uBAA9 \uC785\uB825")
                }
            }
        )
        current.topicSectionList.layoutManager = LinearLayoutManager(requireContext())
        current.topicSectionList.adapter = topicSectionAdapter
    }

    private fun handleTopicClick(topic: LogTopic) {
        if (!selectionController.isInSelectionMode) {
            findNavController().navigate(R.id.action_logFragment_to_logTopicDetailFragment, bundleOf("topicId" to topic.id))
        }
    }

    private fun handleEntryClick(entry: LogEntry) {
        if (selectionController.isInSelectionMode) {
            selectionController.toggle(entry.id, entry.selectionScope())
        } else {
            findNavController().navigate(R.id.action_logFragment_to_logEntryDetailFragment, bundleOf("entryId" to entry.id))
        }
    }

    private fun handleEntryLongClick(entry: LogEntry) {
        prepareEntrySelectionHandlers(entry)
        selectionController.enter(entry.selectionScope(), listOf(entry.id))
    }

    private fun prepareEntrySelectionHandlers(entry: LogEntry) {
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(::confirmDeleteSelectedLogEntries)
            setSelectionMoveHandler(::showMoveEntryDialog)
        }
    }

    private fun isEntrySelected(entry: LogEntry): Boolean {
        return selectionController.isInSelectionMode && entry.id in selectionController.selectedItemIds
    }

    private fun confirmDeleteSelectedLogEntries() {
        val ids = selectionController.selectedItemIds.toList()
        if (ids.isEmpty()) return
        AlertDialog.Builder(requireContext())
            .setMessage("\uC120\uD0DD\uD55C \uD56D\uBAA9\uC744 \uC0AD\uC81C\uD560\uAE4C\uC694?")
            .setNegativeButton("\uCDE8\uC18C", null)
            .setPositiveButton("\uC0AD\uC81C") { _, _ ->
                ids.forEach { InMemoryDataStore.deleteLogEntry(it) }
                selectionController.exit()
                renderList()
            }
            .show()
    }

    private fun showMoveEntryDialog() {
        MoveTargetDialogFragment(
            title = "\uC774\uB3D9\uD560 \uC8FC\uC81C",
            addHint = "\uC0C8 \uC8FC\uC81C \uC785\uB825",
            loadTargets = { InMemoryDataStore.getLogTopics().map { MoveTarget(it.id, it.title) } },
            onAddTarget = { title ->
                val categoryId = currentEntryCategoryId() ?: InMemoryDataStore.getLogCategories().firstOrNull()?.id
                if (categoryId != null) InMemoryDataStore.addLogTopic(categoryId, title)
            },
            onTargetSelected = { target ->
                InMemoryDataStore.moveLogEntriesToTopic(selectionController.selectedItemIds, target.id)
                selectionController.exit()
                renderList()
            }
        ).show(childFragmentManager, "move_log_entries")
    }

    private fun currentEntryCategoryId(): String? {
        val firstEntryId = selectionController.selectedItemIds.firstOrNull() ?: return selectedCategoryId
        val topicId = InMemoryDataStore.getLogEntry(firstEntryId)?.topicId ?: return selectedCategoryId
        return InMemoryDataStore.getLogTopic(topicId)?.categoryId ?: selectedCategoryId
    }

    override fun onDestroyView() {
        selectionController.removeStateListener(selectionStateListener)
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(null)
            setSelectionMoveHandler(null)
        }
        binding = null
        super.onDestroyView()
    }

    private val selectionController
        get() = (requireActivity() as MainActivity).selectionController

    private fun LogEntry.selectionScope(): String = "log-entries:$topicId"
}
