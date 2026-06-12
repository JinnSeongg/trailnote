package com.example.trailnote.feature.growth

import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
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
import com.example.trailnote.databinding.FragmentGrowthTopicDetailBinding
import com.example.trailnote.domain.model.GrowthArea
import com.example.trailnote.domain.model.GrowthColorPalette
import com.example.trailnote.domain.model.GrowthTopic
import com.example.trailnote.domain.model.Routine
import kotlinx.coroutines.launch

class GrowthTopicDetailFragment : Fragment() {
    private var binding: FragmentGrowthTopicDetailBinding? = null
    private lateinit var routineAdapter: RoutineAdapter
    private var descriptionWatcher: TextWatcher? = null
    private var topicId: String = ""
    private var growthAreaId: String = ""
    private var quickAddMode: QuickAddMode = QuickAddMode.Routine
    private var routineDragSelectionHelper: RecyclerDragSelectionHelper? = null
    private var topic: GrowthTopic? = null
    private var area: GrowthArea? = null
    private var topics: List<GrowthTopic> = emptyList()
    private var routines: List<Routine> = emptyList()
    private val selectionStateListener: (SelectionState) -> Unit = {
        if (::routineAdapter.isInitialized) routineAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentGrowthTopicDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        growthAreaId = requireArguments().getString("growthAreaId").orEmpty()
        topicId = requireArguments().getString("topicId").orEmpty()
        val current = binding ?: return
        descriptionWatcher = current.descriptionText.setupTwoLineLimitedDescriptionEditText { text ->
            viewLifecycleOwner.lifecycleScope.launch {
                repository.updateGrowthTopicDescription(topicId, text)
            }
        }
        current.routineList.layoutManager = LinearLayoutManager(requireContext())
        routineAdapter = RoutineAdapter(
            routines = emptyList(),
            onClick = ::handleRoutineClick,
            onLongClick = ::handleRoutineLongClick,
            onDoneChange = { routine, checked ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (repository.updateRoutineDoneState(routine.id, checked) != null) {
                        refreshAchievementsAndShowFeedback()
                    }
                    reloadTopic()
                }
            },
            onFixedStateChange = { routine, isFixed ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.updateRoutineFixedState(routine.id, isFixed)
                    reloadTopic()
                }
            },
            isSelectionMode = { selectionController.isInSelectionMode },
            isSelected = ::isRoutineSelected
        )
        selectionController.addStateListener(selectionStateListener)
        current.routineList.adapter = routineAdapter
        attachRoutineDragHelper()

        InlineQuickAdd.bind(current.routineQuickAdd.root, onDismiss = { showRoutineFab() }) { title ->
            viewLifecycleOwner.lifecycleScope.launch {
                when (quickAddMode) {
                    QuickAddMode.Routine -> repository.addRoutine(topicId, title)
                    QuickAddMode.TopicTitle -> {
                        repository.updateGrowthTopicTitle(topicId, title)?.let { updated ->
                            current.root.findViewById<TextView>(R.id.headerTitle)?.text = updated.title
                        }
                    }
                }
                quickAddMode = QuickAddMode.Routine
                reloadTopic()
            }
        }
        current.routineFabButton.setOnClickListener {
            quickAddMode = QuickAddMode.Routine
            current.routineFabButton.visibility = View.GONE
            InlineQuickAdd.show(current.routineQuickAdd.root, "\uC0C8 \uB8E8\uD2F4 \uC785\uB825")
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                InlineQuickAdd.hide(current.routineQuickAdd.root)
                showRoutineFab()
                isEnabled = false
            }
        }.also { callback ->
            current.routineQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
        reloadTopic()
    }

    override fun onResume() {
        super.onResume()
        if (binding != null && ::routineAdapter.isInitialized) {
            reloadTopic()
        }
    }

    private fun showRoutineFab() {
        binding?.routineFabButton?.visibility = View.VISIBLE
    }

    private suspend fun refreshAchievementsAndShowFeedback() {
        val unlockResult = repository.refreshAchievementUnlocks()
        AchievementUnlockFeedback.show(requireContext(), unlockResult.newlyUnlockedAchievements)
    }

    private fun handleRoutineClick(routine: com.example.trailnote.domain.model.Routine) {
        if (selectionController.isInSelectionMode) {
            selectionController.toggle(routine.id, routineSelectionScope())
        }
    }

    private fun handleRoutineLongClick(routine: com.example.trailnote.domain.model.Routine) {
        prepareRoutineSelectionHandlers()
        selectionController.enter(routineSelectionScope(), listOf(routine.id))
    }

    private fun prepareRoutineSelectionHandlers() {
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(::confirmDeleteSelectedRoutines)
            setSelectionMoveHandler(::showMoveRoutineDialog)
        }
    }

    private fun isRoutineSelected(routine: com.example.trailnote.domain.model.Routine): Boolean {
        return selectionController.isInSelectionMode && routine.id in selectionController.selectedItemIds
    }

    private fun confirmDeleteSelectedRoutines() {
        val ids = selectionController.selectedItemIds.toList()
        if (ids.isEmpty()) return
        DeleteConfirmDialogHelper.showMultiple(requireContext(), ids.size) {
            viewLifecycleOwner.lifecycleScope.launch {
                ids.forEach { repository.deleteRoutine(it) }
                selectionController.exit()
                reloadTopic()
            }
        }
    }

    private fun showMoveRoutineDialog() {
        MoveTargetDialogFragment(
            title = "\uC774\uB3D9\uD560 \uC138\uBD80 \uBD84\uC57C",
            addHint = "\uC0C8 \uC138\uBD80 \uBD84\uC57C \uC785\uB825",
            loadTargets = { topics.map { MoveTarget(it.id, it.title) } },
            onAddTarget = { title ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.addGrowthTopic(growthAreaId, title)
                    reloadTopic()
                }
            },
            onTargetSelected = { target ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.moveRoutinesToTopic(selectionController.selectedItemIds, target.id)
                    selectionController.exit()
                    reloadTopic()
                }
            }
        ).show(childFragmentManager, "move_routines")
    }

    private fun renderRoutines() {
        routineAdapter.submitList(routines, area?.colorHex ?: GrowthColorPalette.DEFAULT_COLOR)
        binding?.emptyText?.visibility = if (routines.isEmpty()) View.VISIBLE else View.GONE
        binding?.routineList?.visibility = if (routines.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun attachRoutineDragHelper() {
        val current = binding ?: return
        if (routineDragSelectionHelper != null) return
        routineDragSelectionHelper = RecyclerDragSelectionHelper(
            recyclerView = current.routineList,
            selectionController = selectionController,
            getItemId = { position -> routineAdapter.getItem(position)?.id },
            getItemScope = { position -> routineAdapter.getItem(position)?.let { routineSelectionScope() } },
            isItemSelected = { position -> routineAdapter.getItem(position)?.let(::isRoutineSelected) == true },
            onDragStarted = { prepareRoutineSelectionHandlers() },
            onSelectionChanged = { routineAdapter.notifyDataSetChanged() }
        ).also { current.routineList.addOnItemTouchListener(it) }
    }

    private fun showTopicMenu() {
        val current = binding ?: return
        val anchor = current.root.findViewById<View>(R.id.headerAction)
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

    private fun openTitleEdit() {
        val current = binding ?: return
        val topic = topic ?: return
        quickAddMode = QuickAddMode.TopicTitle
        current.routineFabButton.visibility = View.GONE
        InlineQuickAdd.show(current.routineQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", topic.title)
    }

    private fun confirmDeleteTopic() {
        val topicTitle = topic?.title
        DeleteConfirmDialogHelper.showSingle(requireContext(), topicTitle) {
            viewLifecycleOwner.lifecycleScope.launch {
                repository.deleteGrowthTopic(topicId)
                findNavController().navigateUp()
            }
        }
    }

    private fun reloadTopic() {
        viewLifecycleOwner.lifecycleScope.launch {
            topic = repository.getGrowthTopicById(topicId)
            if (topic == null) {
                findNavController().navigateUp()
                return@launch
            }
            area = repository.getGrowthAreaById(growthAreaId)
            Log.d(TAG, "topicDetail loaded areaId=$growthAreaId color=${area?.colorHex}")
            topics = repository.getGrowthTopicsByAreaId(growthAreaId)
            routines = repository.getRoutinesByTopicId(topicId)
            renderTopic()
            renderRoutines()
        }
    }

    private fun renderTopic() {
        val current = binding ?: return
        val currentTopic = topic ?: return
        current.root.setHeader(
            currentTopic.title,
            action = "\u00B7\u00B7\u00B7",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = { showTopicMenu() }
        )
        if (current.descriptionText.text.toString() != currentTopic.description) {
            current.descriptionText.setText(currentTopic.description)
        }
    }

    override fun onDestroyView() {
        descriptionWatcher?.let { watcher ->
            binding?.descriptionText?.removeTextChangedListener(watcher)
        }
        descriptionWatcher = null
        selectionController.removeStateListener(selectionStateListener)
        routineDragSelectionHelper?.let { binding?.routineList?.removeOnItemTouchListener(it) }
        routineDragSelectionHelper = null
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

    private fun routineSelectionScope(): String = "growth-routines:$topicId"

    private enum class QuickAddMode {
        Routine,
        TopicTitle
    }

    private companion object {
        const val TAG = "GrowthColorDebug"
    }
}
