package com.example.trailnote.feature.log

import android.os.Bundle
import android.view.LayoutInflater
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
import com.example.trailnote.core.selection.RecyclerDragSelectionHelper
import com.example.trailnote.core.selection.SelectionState
import com.example.trailnote.core.util.AchievementUnlockFeedback
import com.example.trailnote.core.util.DeleteConfirmDialogHelper
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.PopupMenuHelper
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.FragmentLogTopicDetailBinding
import com.example.trailnote.domain.model.LogCategory
import com.example.trailnote.domain.model.LogEntry
import com.example.trailnote.domain.model.LogTopic
import kotlinx.coroutines.launch

class LogTopicDetailFragment : Fragment() {
    private var binding: FragmentLogTopicDetailBinding? = null
    private lateinit var entryAdapter: LogEntryAdapter
    private var topicId: String = ""
    private var quickAddMode: QuickAddMode = QuickAddMode.Entry
    private var entryDragSelectionHelper: RecyclerDragSelectionHelper? = null
    private var topic: LogTopic? = null
    private var category: LogCategory? = null
    private var topics: List<LogTopic> = emptyList()
    private var entries: List<LogEntry> = emptyList()
    private val selectionStateListener: (SelectionState) -> Unit = {
        if (::entryAdapter.isInitialized) entryAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentLogTopicDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        topicId = requireArguments().getString("topicId").orEmpty()
        val current = binding ?: return
        current.entryList.layoutManager = LinearLayoutManager(requireContext())
        entryAdapter = LogEntryAdapter(
            items = emptyList(),
            onClick = ::handleEntryClick,
            onLongClick = ::handleEntryLongClick,
            isSelected = ::isEntrySelected
        )
        selectionController.addStateListener(selectionStateListener)
        current.entryList.adapter = entryAdapter
        attachEntryDragHelper()

        InlineQuickAdd.bind(current.entryQuickAdd.root, onDismiss = { showFab() }) { title ->
            viewLifecycleOwner.lifecycleScope.launch {
                when (quickAddMode) {
                    QuickAddMode.Entry -> {
                        repository.addLogEntry(topicId, title)
                        refreshAchievementsAndShowFeedback()
                    }
                    QuickAddMode.TopicTitle -> {
                        repository.updateLogTopicTitle(topicId, title)?.let { updated ->
                            current.root.findViewById<TextView>(R.id.headerTitle)?.text = updated.title
                        }
                    }
                }
                quickAddMode = QuickAddMode.Entry
                reloadTopic()
            }
        }
        current.entryAddButton.setOnClickListener {
            quickAddMode = QuickAddMode.Entry
            openQuickInput("\uC0C8 \uAE30\uB85D \uC81C\uBAA9 \uC785\uB825")
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                closeQuickInput()
                isEnabled = false
            }
        }.also { callback ->
            current.entryQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
        reloadTopic()
    }

    override fun onResume() {
        super.onResume()
        if (binding != null && ::entryAdapter.isInitialized) {
            reloadTopic()
        }
    }

    private fun openQuickInput(hint: String) {
        val current = binding ?: return
        current.entryAddButton.visibility = View.GONE
        InlineQuickAdd.show(current.entryQuickAdd.root, hint)
    }

    private suspend fun refreshAchievementsAndShowFeedback() {
        val unlockResult = repository.refreshAchievementUnlocks()
        AchievementUnlockFeedback.show(requireContext(), unlockResult.newlyUnlockedAchievements)
    }

    private fun openTitleEdit() {
        val current = binding ?: return
        val topic = topic ?: return
        quickAddMode = QuickAddMode.TopicTitle
        current.entryAddButton.visibility = View.GONE
        InlineQuickAdd.show(current.entryQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", topic.title)
    }

    private fun closeQuickInput() {
        val current = binding ?: return
        InlineQuickAdd.hide(current.entryQuickAdd.root)
        quickAddMode = QuickAddMode.Entry
        showFab()
    }

    private fun handleEntryClick(entry: com.example.trailnote.domain.model.LogEntry) {
        if (selectionController.isInSelectionMode) {
            selectionController.toggle(entry.id, entrySelectionScope())
        } else {
            findNavController().navigate(
                R.id.action_logTopicDetailFragment_to_logEntryDetailFragment,
                bundleOf("entryId" to entry.id)
            )
        }
    }

    private fun handleEntryLongClick(entry: com.example.trailnote.domain.model.LogEntry) {
        prepareEntrySelectionHandlers()
        selectionController.enter(entrySelectionScope(), listOf(entry.id))
    }

    private fun prepareEntrySelectionHandlers() {
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(::confirmDeleteSelectedEntries)
            setSelectionMoveHandler(::showMoveEntryDialog)
        }
    }

    private fun isEntrySelected(entry: com.example.trailnote.domain.model.LogEntry): Boolean {
        return selectionController.isInSelectionMode && entry.id in selectionController.selectedItemIds
    }

    private fun confirmDeleteSelectedEntries() {
        val ids = selectionController.selectedItemIds.toList()
        if (ids.isEmpty()) return
        DeleteConfirmDialogHelper.showMultiple(requireContext(), ids.size) {
            viewLifecycleOwner.lifecycleScope.launch {
                ids.forEach { repository.deleteLogEntry(it) }
                selectionController.exit()
                reloadTopic()
            }
        }
    }

    private fun showMoveEntryDialog() {
        val categoryId = topic?.categoryId ?: return
        MoveTargetDialogFragment(
            title = "\uC774\uB3D9\uD560 \uC8FC\uC81C",
            addHint = "\uC0C8 \uC8FC\uC81C \uC785\uB825",
            loadTargets = { topics.map { MoveTarget(it.id, it.title) } },
            onAddTarget = { title ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.addLogTopic(categoryId, title)
                    reloadTopic()
                }
            },
            onTargetSelected = { target ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.moveLogEntriesToTopic(selectionController.selectedItemIds, target.id)
                    selectionController.exit()
                    reloadTopic()
                }
            }
        ).show(childFragmentManager, "move_log_topic_entries")
    }

    private fun renderEntries() {
        entryAdapter.submitList(entries)
        binding?.emptyText?.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        binding?.entryList?.visibility = if (entries.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun attachEntryDragHelper() {
        val current = binding ?: return
        if (entryDragSelectionHelper != null) return
        entryDragSelectionHelper = RecyclerDragSelectionHelper(
            recyclerView = current.entryList,
            selectionController = selectionController,
            getItemId = { position -> entryAdapter.getItem(position)?.id },
            getItemScope = { position -> entryAdapter.getItem(position)?.let { entrySelectionScope() } },
            isItemSelected = { position -> entryAdapter.getItem(position)?.let(::isEntrySelected) == true },
            onDragStarted = { prepareEntrySelectionHandlers() },
            onSelectionChanged = { entryAdapter.notifyDataSetChanged() }
        ).also { current.entryList.addOnItemTouchListener(it) }
    }

    private fun showTopicMenu(anchor: View) {
        PopupMenuHelper.show(requireContext(), anchor, listOf("\uC218\uC815", "\uC0AD\uC81C")) { title ->
            when (title) {
                "\uC218\uC815" -> {
                    openTitleEdit()
                    true
                }
                "\uC0AD\uC81C" -> {
                    confirmDeleteTopic()
                    true
                }
                else -> false
            }
        }
    }

    private fun confirmDeleteTopic() {
        val topicTitle = topic?.title
        DeleteConfirmDialogHelper.showSingle(requireContext(), topicTitle) {
            viewLifecycleOwner.lifecycleScope.launch {
                repository.deleteLogTopic(topicId)
                findNavController().navigateUp()
            }
        }
    }

    private fun reloadTopic() {
        viewLifecycleOwner.lifecycleScope.launch {
            topic = repository.getLogTopicById(topicId)
            if (topic == null) {
                findNavController().navigateUp()
                return@launch
            }
            category = repository.getLogCategoryById(topic?.categoryId.orEmpty())
            topics = repository.getLogTopics()
            entries = repository.getLogEntriesByTopicId(topicId)
            renderTopic()
            renderEntries()
        }
    }

    private fun renderTopic() {
        val current = binding ?: return
        val currentTopic = topic ?: return
        current.root.setHeader(
            title = currentTopic.title,
            action = "\u2315",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = {
                // TODO: Connect log entry search.
            }
        )
        current.root.findViewById<TextView>(R.id.headerMore)?.apply {
            visibility = View.VISIBLE
            setOnClickListener { showTopicMenu(this) }
        }
        current.categoryText.text = category?.name.orEmpty()
    }

    private fun showFab() {
        binding?.entryAddButton?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        selectionController.removeStateListener(selectionStateListener)
        entryDragSelectionHelper?.let { binding?.entryList?.removeOnItemTouchListener(it) }
        entryDragSelectionHelper = null
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

    private fun entrySelectionScope(): String = "log-entries:$topicId"

    private enum class QuickAddMode {
        Entry,
        TopicTitle
    }
}
