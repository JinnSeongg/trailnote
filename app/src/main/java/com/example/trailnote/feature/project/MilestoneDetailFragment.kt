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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.MainActivity
import com.example.trailnote.core.selection.MoveTarget
import com.example.trailnote.core.selection.MoveTargetDialogFragment
import com.example.trailnote.core.selection.RecyclerDragSelectionHelper
import com.example.trailnote.core.selection.SelectionState
import com.example.trailnote.core.util.AchievementUnlockFeedback
import com.example.trailnote.core.util.DeleteConfirmDialogHelper
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.PopupMenuHelper
import com.example.trailnote.core.util.setupTwoLineLimitedDescriptionEditText
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.FragmentMilestoneDetailBinding
import com.example.trailnote.domain.model.Milestone
import com.example.trailnote.domain.model.ShortTask
import kotlinx.coroutines.launch

class MilestoneDetailFragment : Fragment() {
    private var binding: FragmentMilestoneDetailBinding? = null
    private lateinit var shortTaskAdapter: ShortTaskAdapter
    private var milestoneId: String = ""
    private var projectId: String = ""
    private var descriptionWatcher: TextWatcher? = null
    private var quickAddMode: QuickAddMode = QuickAddMode.ShortTask
    private var shortTaskDragSelectionHelper: RecyclerDragSelectionHelper? = null
    private var milestone: Milestone? = null
    private var milestones: List<Milestone> = emptyList()
    private var shortTasks: List<ShortTask> = emptyList()
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
        val current = binding ?: return
        descriptionWatcher = current.descriptionText.setupTwoLineLimitedDescriptionEditText { text ->
            viewLifecycleOwner.lifecycleScope.launch {
                repository.updateMilestoneDescription(milestoneId, text)
            }
        }

        shortTaskAdapter = ShortTaskAdapter(
            tasks = emptyList(),
            onClick = ::handleShortTaskClick,
            onLongClick = ::handleShortTaskLongClick,
            onDoneChange = { task, checked ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (repository.updateShortTaskDone(task.id, checked) != null) {
                        refreshAchievementsAndShowFeedback()
                    }
                    reloadMilestone()
                }
            },
            isSelectionMode = { selectionController.isInSelectionMode },
            isSelected = ::isShortTaskSelected
        )
        selectionController.addStateListener(selectionStateListener)
        current.shortTaskList.layoutManager = LinearLayoutManager(requireContext())
        current.shortTaskList.adapter = shortTaskAdapter
        attachShortTaskDragHelper()

        InlineQuickAdd.bind(current.shortTaskQuickAdd.root, onDismiss = { showShortTaskFab() }) { title ->
            viewLifecycleOwner.lifecycleScope.launch {
                when (quickAddMode) {
                    QuickAddMode.ShortTask -> repository.addShortTask(milestoneId, title)
                    QuickAddMode.MilestoneTitle -> {
                        repository.updateMilestoneTitle(milestoneId, title)?.let { updated ->
                            current.root.findViewById<TextView>(R.id.headerTitle)?.text = updated.title
                        }
                    }
                }
                quickAddMode = QuickAddMode.ShortTask
                reloadMilestone()
            }
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
        reloadMilestone()
    }

    override fun onResume() {
        super.onResume()
        if (binding != null && ::shortTaskAdapter.isInitialized) {
            reloadMilestone()
        }
    }

    private fun showShortTaskFab() {
        binding?.shortTaskFabButton?.visibility = View.VISIBLE
    }

    private suspend fun refreshAchievementsAndShowFeedback() {
        val unlockResult = repository.refreshAchievementUnlocks()
        AchievementUnlockFeedback.show(requireContext(), unlockResult.newlyUnlockedAchievements)
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
            viewLifecycleOwner.lifecycleScope.launch {
                ids.forEach { repository.deleteShortTask(it) }
                selectionController.exit()
                reloadMilestone()
            }
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
                milestones.map { milestone ->
                    MoveTarget(milestone.id, milestone.title)
                }
            },
            onAddTarget = { title ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.addMilestone(projectId, title)
                    reloadMilestone()
                }
            },
            onTargetSelected = { target ->
                val ids = selectionController.selectedItemIds.toList()
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.moveShortTasksToMilestone(ids, target.id)
                    selectionController.exit()
                    reloadMilestone()
                }
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
        val milestone = milestone ?: return
        quickAddMode = QuickAddMode.MilestoneTitle
        current.shortTaskFabButton.visibility = View.GONE
        InlineQuickAdd.show(current.shortTaskQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", milestone.title)
    }

    private fun confirmDeleteMilestone() {
        val milestoneTitle = milestone?.title
        DeleteConfirmDialogHelper.showSingle(requireContext(), milestoneTitle) {
            viewLifecycleOwner.lifecycleScope.launch {
                repository.deleteMilestone(milestoneId)
                findNavController().navigateUp()
            }
        }
    }

    private fun reloadMilestone() {
        viewLifecycleOwner.lifecycleScope.launch {
            milestone = repository.getMilestoneById(milestoneId)
            if (milestone == null) {
                findNavController().navigateUp()
                return@launch
            }
            milestones = repository.getMilestonesByProjectId(projectId)
            shortTasks = repository.getShortTasksByMilestoneId(milestoneId)
            renderMilestone()
        }
    }

    private fun renderMilestone() {
        val current = binding ?: return
        val currentMilestone = milestone ?: return
        current.root.setHeader(
            currentMilestone.title,
            action = "\u00B7\u00B7\u00B7",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = { showMilestoneMenu() }
        )
        if (current.descriptionText.text.toString() != currentMilestone.description) {
            current.descriptionText.setText(currentMilestone.description)
        }
        shortTaskAdapter.submitList(shortTasks)
        current.emptyText.visibility = if (shortTasks.isEmpty()) View.VISIBLE else View.GONE
        current.shortTaskList.visibility = if (shortTasks.isEmpty()) View.GONE else View.VISIBLE
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

    private val repository
        get() = RepositoryProvider.getRepository(requireContext())

    private fun shortTaskSelectionScope(): String = "milestone-short-tasks:$milestoneId"

    private enum class QuickAddMode {
        ShortTask,
        MilestoneTitle
    }
}
