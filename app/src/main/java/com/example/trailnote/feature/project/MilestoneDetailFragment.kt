package com.example.trailnote.feature.project

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.trailnote.R
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.MainActivity
import com.example.trailnote.core.selection.MoveTarget
import com.example.trailnote.core.selection.MoveTargetDialogFragment
import com.example.trailnote.core.selection.RecyclerDragSelectionHelper
import com.example.trailnote.core.selection.SelectionState
import com.example.trailnote.core.util.DeleteConfirmDialogHelper
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.PopupMenuHelper
import com.example.trailnote.core.util.setupTwoLineLimitedDescriptionEditText
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentMilestoneDetailBinding

class MilestoneDetailFragment : Fragment() {
    private var binding: FragmentMilestoneDetailBinding? = null
    private lateinit var shortTaskAdapter: ShortTaskAdapter
    private var milestoneId: String = ""
    private var projectId: String = ""
    private var descriptionWatcher: TextWatcher? = null
    private var quickAddMode: QuickAddMode = QuickAddMode.ShortTask
    private var shortTaskDragSelectionHelper: RecyclerDragSelectionHelper? = null
    private val selectionStateListener: (SelectionState) -> Unit = {
        if (::shortTaskAdapter.isInitialized) shortTaskAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentMilestoneDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        projectId = requireArguments().getString("projectId").orEmpty()
        milestoneId = requireArguments().getString("milestoneId").orEmpty()
        val milestone = InMemoryDataStore.getMilestone(milestoneId) ?: return
        val tasks = InMemoryDataStore.getShortTasksByMilestone(milestoneId)
        val current = binding ?: return
        current.root.setHeader(
            milestone.title,
            action = "\u00B7\u00B7\u00B7",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = { showMilestoneMenu() }
        )
        current.descriptionText.setText(milestone.description)
        descriptionWatcher = current.descriptionText.setupTwoLineLimitedDescriptionEditText { text ->
            InMemoryDataStore.updateMilestoneDescription(milestoneId, text)
        }

        shortTaskAdapter = ShortTaskAdapter(
            tasks = tasks,
            onClick = ::handleShortTaskClick,
            onLongClick = ::handleShortTaskLongClick,
            isSelectionMode = { selectionController.isInSelectionMode },
            isSelected = ::isShortTaskSelected
        )
        selectionController.addStateListener(selectionStateListener)
        current.shortTaskList.layoutManager = LinearLayoutManager(requireContext())
        current.shortTaskList.adapter = shortTaskAdapter
        attachShortTaskDragHelper()
        current.emptyText.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        current.shortTaskList.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE

        InlineQuickAdd.bind(current.shortTaskQuickAdd.root, onDismiss = { showShortTaskFab() }) { title ->
            when (quickAddMode) {
                QuickAddMode.ShortTask -> {
                    shortTaskAdapter.addItem(InMemoryDataStore.addShortTask(milestoneId, title))
                    current.emptyText.visibility = View.GONE
                    current.shortTaskList.visibility = View.VISIBLE
                }
                QuickAddMode.MilestoneTitle -> {
                    InMemoryDataStore.updateMilestoneTitle(milestoneId, title)?.let { updated ->
                        current.root.findViewById<TextView>(R.id.headerTitle)?.text = updated.title
                    }
                }
            }
            quickAddMode = QuickAddMode.ShortTask
        }
        current.shortTaskAddButton.setOnClickListener {
            quickAddMode = QuickAddMode.ShortTask
            current.shortTaskFabButton.visibility = View.GONE
            InlineQuickAdd.show(current.shortTaskQuickAdd.root, "\uC0C8 \uB2E8\uAE30\uBAA9\uD45C \uC785\uB825")
        }
        current.shortTaskFabButton.setOnClickListener {
            current.shortTaskAddButton.performClick()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                InlineQuickAdd.hide(current.shortTaskQuickAdd.root)
                showShortTaskFab()
                isEnabled = false
            }
        }.also { callback ->
            current.shortTaskQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
    }

    private fun showShortTaskFab() {
        binding?.shortTaskFabButton?.visibility = View.VISIBLE
    }

    private fun handleShortTaskClick(shortTask: com.example.trailnote.domain.model.ShortTask) {
        if (selectionController.isInSelectionMode) {
            selectionController.toggle(shortTask.id, shortTaskSelectionScope())
        }
    }

    private fun handleShortTaskLongClick(shortTask: com.example.trailnote.domain.model.ShortTask) {
        prepareShortTaskSelectionHandlers()
    }

    private fun prepareShortTaskSelectionHandlers() {
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(::confirmDeleteSelectedShortTasks)
            setSelectionMoveHandler(::showMoveShortTaskDialog)
        }
    }

    private fun isShortTaskSelected(shortTask: com.example.trailnote.domain.model.ShortTask): Boolean {
        return selectionController.isInSelectionMode && shortTask.id in selectionController.selectedItemIds
    }

    private fun confirmDeleteSelectedShortTasks() {
        val ids = selectionController.selectedItemIds.toList()
        if (ids.isEmpty()) return
        DeleteConfirmDialogHelper.showMultiple(requireContext(), ids.size) {
            ids.forEach { InMemoryDataStore.deleteShortTask(it) }
            selectionController.exit()
            val tasks = InMemoryDataStore.getShortTasksByMilestone(milestoneId)
            shortTaskAdapter.submitList(tasks)
            binding?.emptyText?.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
            binding?.shortTaskList?.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun attachShortTaskDragHelper() {
        val current = binding ?: return
        if (shortTaskDragSelectionHelper != null) return
        shortTaskDragSelectionHelper = RecyclerDragSelectionHelper(
            recyclerView = current.shortTaskList,
            selectionController = selectionController,
            getItemId = { position -> shortTaskAdapter.getItem(position)?.id },
            getItemScope = { position -> shortTaskAdapter.getItem(position)?.let { shortTaskSelectionScope() } },
            isItemSelected = { position -> shortTaskAdapter.getItem(position)?.let(::isShortTaskSelected) == true },
            onDragStarted = { prepareShortTaskSelectionHandlers() },
            onSelectionChanged = { shortTaskAdapter.notifyDataSetChanged() }
        ).also { current.shortTaskList.addOnItemTouchListener(it) }
    }

    private fun showMoveShortTaskDialog() {
        MoveTargetDialogFragment(
            title = "\uC774\uB3D9\uD560 \uC911\uAE30\uBAA9\uD45C",
            addHint = "\uC0C8 \uC911\uAE30\uBAA9\uD45C \uC785\uB825",
            loadTargets = {
                InMemoryDataStore.getMilestonesByProject(projectId).map { milestone ->
                    MoveTarget(milestone.id, milestone.title)
                }
            },
            onAddTarget = { title -> InMemoryDataStore.addMilestone(projectId, title) },
            onTargetSelected = { target ->
                val ids = selectionController.selectedItemIds.toList()
                InMemoryDataStore.moveShortTasksToMilestone(ids, target.id)
                selectionController.exit()
                val tasks = InMemoryDataStore.getShortTasksByMilestone(milestoneId)
                shortTaskAdapter.submitList(tasks)
                binding?.emptyText?.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
                binding?.shortTaskList?.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
            }
        ).show(childFragmentManager, "move_short_tasks")
    }

    private fun showMilestoneMenu() {
        val current = binding ?: return
        val anchor = current.root.findViewById<View>(R.id.headerAction)
        PopupMenuHelper.show(requireContext(), anchor, listOf("\uC218\uC815", "\uC0AD\uC81C")) { title ->
            when (title) {
                "\uC218\uC815" -> {
                    openTitleEdit()
                    true
                }
                "\uC0AD\uC81C" -> {
                    confirmDeleteMilestone()
                    true
                }
                else -> false
            }
        }
    }

    private fun openTitleEdit() {
        val current = binding ?: return
        val milestone = InMemoryDataStore.getMilestone(milestoneId) ?: return
        quickAddMode = QuickAddMode.MilestoneTitle
        current.shortTaskFabButton.visibility = View.GONE
        InlineQuickAdd.show(current.shortTaskQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", milestone.title)
    }

    private fun confirmDeleteMilestone() {
        val milestoneTitle = InMemoryDataStore.getMilestone(milestoneId)?.title
        DeleteConfirmDialogHelper.showSingle(requireContext(), milestoneTitle) {
            InMemoryDataStore.deleteMilestone(milestoneId)
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        descriptionWatcher?.let { watcher ->
            binding?.descriptionText?.removeTextChangedListener(watcher)
        }
        descriptionWatcher = null
        selectionController.removeStateListener(selectionStateListener)
        shortTaskDragSelectionHelper?.let { binding?.shortTaskList?.removeOnItemTouchListener(it) }
        shortTaskDragSelectionHelper = null
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(null)
            setSelectionMoveHandler(null)
        }
        binding = null
        super.onDestroyView()
    }

    private val selectionController
        get() = (requireActivity() as MainActivity).selectionController

    private fun shortTaskSelectionScope(): String = "milestone-short-tasks:$milestoneId"

    private enum class QuickAddMode {
        ShortTask,
        MilestoneTitle
    }
}
