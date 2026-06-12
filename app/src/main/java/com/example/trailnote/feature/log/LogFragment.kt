package com.example.trailnote.feature.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.MainActivity
import com.example.trailnote.R
import com.example.trailnote.core.selection.MoveTarget
import com.example.trailnote.core.selection.MoveTargetDialogFragment
import com.example.trailnote.core.selection.SelectionState
import com.example.trailnote.core.util.AchievementUnlockFeedback
import com.example.trailnote.core.util.DeleteConfirmDialogHelper
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.PopupMenuHelper
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.local.db.DatabaseSeeder
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.FragmentLogBinding
import com.example.trailnote.domain.model.LogCategory
import com.example.trailnote.domain.model.LogEntry
import com.example.trailnote.domain.model.LogTopic
import kotlinx.coroutines.launch

class LogFragment : Fragment() {
    private var binding: FragmentLogBinding? = null
    private var selectedCategoryId: String? = null
    private var quickAddMode: QuickAddMode? = null
    private var topicSectionAdapter: LogTopicSectionAdapter? = null
    private var categories: List<LogCategory> = emptyList()
    private var topics: List<LogTopic> = emptyList()
    private var entries: List<LogEntry> = emptyList()
    private val selectionStateListener: (SelectionState) -> Unit = {
        topicSectionAdapter?.notifyDataSetChanged()
        renderHeader()
        updateTopicFabVisibility()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentLogBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        renderHeader()
        selectionController.addStateListener(selectionStateListener)
        InlineQuickAdd.bind(current.categoryQuickAdd.root, onDismiss = {
            quickAddMode = null
            updateTopicFabVisibility()
        }) { text ->
            val modeAtSubmit = quickAddMode
            viewLifecycleOwner.lifecycleScope.launch {
                when (val mode = modeAtSubmit) {
                    QuickAddMode.Category -> {
                        val category = repository.addLogCategory(text)
                        selectedCategoryId = category.id
                    }
                    is QuickAddMode.Topic -> repository.addLogTopic(mode.categoryId, text)
                    is QuickAddMode.Entry -> {
                        repository.addLogEntry(mode.topicId, text)
                        refreshAchievementsAndShowFeedback()
                    }
                    is QuickAddMode.EditCategory -> repository.updateLogCategoryName(mode.categoryId, text)
                    is QuickAddMode.EditTopic -> repository.updateLogTopicTitle(mode.topicId, text)
                    null -> return@launch
                }
                quickAddMode = null
                reloadLogs()
            }
        }
        current.categoryAddButton.setOnClickListener {
            if (!selectionController.isInSelectionMode) {
                openQuickAdd(QuickAddMode.Category, "\uC0C8 \uCE74\uD14C\uACE0\uB9AC \uC785\uB825")
            }
        }
        current.logTopicAddButton.setOnClickListener {
            val categoryId = selectedCategoryId ?: return@setOnClickListener
            if (!selectionController.isInSelectionMode) {
                openQuickAdd(QuickAddMode.Topic(categoryId), "\uC0C8 \uC8FC\uC81C \uC785\uB825")
            }
        }
        current.topicSectionList.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && quickAddMode is QuickAddMode.Topic) {
                closeQuickAdd()
            }
            false
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                closeQuickAdd()
                isEnabled = false
            }
        }.also { callback ->
            current.categoryQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
        reloadLogs()
    }

    override fun onResume() {
        super.onResume()
        if (binding != null) {
            reloadLogs()
        }
    }

    private fun renderChips() {
        val current = binding ?: return
        current.categoryChipContainer.removeAllViews()
        val chipEntries = listOf(null to "\uC804\uCCB4") + categories.map { it.id to it.name }
        chipEntries.forEach { (id, label) ->
            val chip = TextView(requireContext()).apply {
                text = label
                textSize = 12f
                setTextColor(resources.getColor(if (selectedCategoryId == id) R.color.white else R.color.trail_text_secondary, null))
                setBackgroundResource(if (selectedCategoryId == id) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
                setOnClickListener {
                    if (!selectionController.isInSelectionMode) {
                        selectedCategoryId = id
                        closeQuickAdd()
                        topicSectionAdapter?.closeOpen()
                        renderHeader()
                        renderChips()
                        renderList()
                    }
                }
            }
            current.categoryChipContainer.addView(chip)
        }
    }

    private fun renderHeader() {
        val current = binding ?: return
        current.root.setHeader(
            "\uAE30\uB85D",
            action = "\u2315",
            showMore = selectedCategoryId != null && !selectionController.isInSelectionMode,
            onMore = {
                current.root.findViewById<TextView>(R.id.headerMore)?.let(::showSelectedCategoryMenu)
            }
        )
    }

    private fun renderList() {
        val current = binding ?: return
        val visibleTopics = if (selectedCategoryId == null) {
            topics
        } else {
            topics.filter { it.categoryId == selectedCategoryId }
        }
        topicSectionAdapter = LogTopicSectionAdapter(
            topics = visibleTopics,
            entriesForTopic = { topicId -> entries.filter { it.topicId == topicId } },
            onTopicClick = ::handleTopicClick,
            onTopicLongClick = ::showTopicMenu,
            onEntryClick = ::handleEntryClick,
            onEntryLongClick = ::handleEntryLongClick,
            isEntrySelected = ::isEntrySelected,
            selectionController = selectionController,
            onEntryDragStarted = ::prepareEntrySelectionHandlers,
            onAddClick = { topic ->
                if (!selectionController.isInSelectionMode) {
                    openQuickAdd(QuickAddMode.Entry(topic.id), "\uC0C8 \uAE30\uB85D \uC81C\uBAA9 \uC785\uB825")
                }
            }
        )
        current.topicSectionList.layoutManager = LinearLayoutManager(requireContext())
        current.topicSectionList.adapter = topicSectionAdapter
        updateTopicFabVisibility()
    }

    private fun updateTopicFabVisibility() {
        val current = binding ?: return
        current.logTopicAddButton.visibility =
            if (
                selectedCategoryId != null &&
                !selectionController.isInSelectionMode &&
                !isAddTopicInputVisible()
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun isAddTopicInputVisible(): Boolean {
        val current = binding ?: return false
        return quickAddMode is QuickAddMode.Topic && InlineQuickAdd.isVisible(current.categoryQuickAdd.root)
    }

    private fun openQuickAdd(mode: QuickAddMode, hint: String, initialText: String = "") {
        val current = binding ?: return
        quickAddMode = mode
        InlineQuickAdd.show(current.categoryQuickAdd.root, hint, initialText)
        updateTopicFabVisibility()
    }

    private fun closeQuickAdd() {
        val current = binding ?: return
        quickAddMode = null
        InlineQuickAdd.hide(current.categoryQuickAdd.root)
        updateTopicFabVisibility()
    }

    private suspend fun refreshAchievementsAndShowFeedback() {
        val unlockResult = repository.refreshAchievementUnlocks()
        AchievementUnlockFeedback.show(requireContext(), unlockResult.newlyUnlockedAchievements)
    }

    private fun showSelectedCategoryMenu(anchor: View) {
        val category = categories.firstOrNull { it.id == selectedCategoryId } ?: return
        PopupMenuHelper.show(requireContext(), anchor, listOf("\uC218\uC815", "\uC0AD\uC81C")) { title ->
            when (title) {
                "\uC218\uC815" -> {
                    openQuickAdd(QuickAddMode.EditCategory(category.id), "\uCE74\uD14C\uACE0\uB9AC \uC785\uB825", category.name)
                    true
                }
                "\uC0AD\uC81C" -> {
                    DeleteConfirmDialogHelper.showCustom(
                        context = requireContext(),
                        title = "\uC0AD\uC81C\uD560\uAE4C\uC694?",
                        message = "${category.name}\uC744(\uB97C) \uC0AD\uC81C\uD569\uB2C8\uB2E4. \uC774 \uCE74\uD14C\uACE0\uB9AC\uC758 \uC8FC\uC81C\uC640 \uAE30\uB85D\uB3C4 \uD568\uAED8 \uC0AD\uC81C\uB429\uB2C8\uB2E4. \uC774 \uC791\uC5C5\uC740 \uB418\uB3CC\uB9B4 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.",
                        onDelete = {
                            viewLifecycleOwner.lifecycleScope.launch {
                                repository.deleteLogCategory(category.id)
                                selectedCategoryId = null
                                quickAddMode = null
                                reloadLogs()
                            }
                        }
                    )
                    true
                }
                else -> false
            }
        }
    }

    private fun showTopicMenu(topic: LogTopic, anchor: View) {
        if (selectionController.isInSelectionMode) return
        PopupMenuHelper.show(requireContext(), anchor, listOf("\uC218\uC815", "\uC0AD\uC81C")) { title ->
            when (title) {
                "\uC218\uC815" -> {
                    openQuickAdd(QuickAddMode.EditTopic(topic.id), "\uC8FC\uC81C \uC785\uB825", topic.title)
                    true
                }
                "\uC0AD\uC81C" -> {
                    DeleteConfirmDialogHelper.showCustom(
                        context = requireContext(),
                        title = "\uC774 \uC8FC\uC81C\uB97C \uC0AD\uC81C\uD560\uAE4C\uC694?",
                        message = "\uC774 \uC8FC\uC81C\uC5D0 \uD3EC\uD568\uB41C \uAE30\uB85D\uB3C4 \uD568\uAED8 \uC0AD\uC81C\uB429\uB2C8\uB2E4. \uC774 \uC791\uC5C5\uC740 \uB418\uB3CC\uB9B4 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.",
                        onDelete = {
                            viewLifecycleOwner.lifecycleScope.launch {
                                repository.deleteLogTopic(topic.id)
                                quickAddMode = null
                                reloadLogs()
                            }
                        }
                    )
                    true
                }
                else -> false
            }
        }
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
        DeleteConfirmDialogHelper.showMultiple(requireContext(), ids.size) {
            viewLifecycleOwner.lifecycleScope.launch {
                ids.forEach { repository.deleteLogEntry(it) }
                selectionController.exit()
                reloadLogs()
            }
        }
    }

    private fun showMoveEntryDialog() {
        MoveTargetDialogFragment(
            title = "\uC774\uB3D9\uD560 \uC8FC\uC81C",
            addHint = "\uC0C8 \uC8FC\uC81C \uC785\uB825",
            loadTargets = { topics.map { MoveTarget(it.id, it.title) } },
            onAddTarget = { title ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val categoryId = currentEntryCategoryId() ?: categories.firstOrNull()?.id
                    if (categoryId != null) {
                        repository.addLogTopic(categoryId, title)
                        reloadLogs()
                    }
                }
            },
            onTargetSelected = { target ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.moveLogEntriesToTopic(selectionController.selectedItemIds, target.id)
                    selectionController.exit()
                    reloadLogs()
                }
            }
        ).show(childFragmentManager, "move_log_entries")
    }

    private fun currentEntryCategoryId(): String? {
        val firstEntryId = selectionController.selectedItemIds.firstOrNull() ?: return selectedCategoryId
        val topicId = entries.firstOrNull { it.id == firstEntryId }?.topicId ?: return selectedCategoryId
        return topics.firstOrNull { it.id == topicId }?.categoryId ?: selectedCategoryId
    }

    private fun reloadLogs() {
        viewLifecycleOwner.lifecycleScope.launch {
            DatabaseSeeder.seedIfNeeded(requireContext().applicationContext)
            categories = repository.getLogCategories()
            topics = repository.getLogTopics()
            entries = repository.getLogEntries()
            if (selectedCategoryId != null && categories.none { it.id == selectedCategoryId }) {
                selectedCategoryId = null
            }
            renderHeader()
            renderChips()
            renderList()
        }
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

    private val repository
        get() = RepositoryProvider.getRepository(requireContext())

    private fun LogEntry.selectionScope(): String = "log-entries:$topicId"

    private sealed class QuickAddMode {
        object Category : QuickAddMode()
        data class Topic(val categoryId: String) : QuickAddMode()
        data class Entry(val topicId: String) : QuickAddMode()
        data class EditCategory(val categoryId: String) : QuickAddMode()
        data class EditTopic(val topicId: String) : QuickAddMode()
    }
}
