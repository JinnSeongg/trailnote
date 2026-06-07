package com.example.trailnote.feature.growth

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
import com.example.trailnote.databinding.FragmentGrowthTopicDetailBinding

class GrowthTopicDetailFragment : Fragment() {
    private var binding: FragmentGrowthTopicDetailBinding? = null
    private lateinit var routineAdapter: RoutineAdapter
    private var descriptionWatcher: TextWatcher? = null
    private var topicId: String = ""
    private var growthAreaId: String = ""
    private var quickAddMode: QuickAddMode = QuickAddMode.Routine
    private var routineDragSelectionHelper: RecyclerDragSelectionHelper? = null
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
        val topic = InMemoryDataStore.getGrowthTopic(topicId) ?: return
        val routines = InMemoryDataStore.getRoutinesByTopic(topicId)
        val current = binding ?: return
        current.root.setHeader(
            topic.title,
            action = "\u00B7\u00B7\u00B7",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = { showTopicMenu() }
        )
        current.descriptionText.setText(topic.description)
        descriptionWatcher = current.descriptionText.setupTwoLineLimitedDescriptionEditText { text ->
            InMemoryDataStore.updateGrowthTopicDescription(topicId, text)
        }
        current.routineList.layoutManager = LinearLayoutManager(requireContext())
        routineAdapter = RoutineAdapter(
            routines = routines,
            onClick = ::handleRoutineClick,
            onLongClick = ::handleRoutineLongClick,
            isSelectionMode = { selectionController.isInSelectionMode },
            isSelected = ::isRoutineSelected
        )
        selectionController.addStateListener(selectionStateListener)
        current.routineList.adapter = routineAdapter
        attachRoutineDragHelper()
        current.emptyText.visibility = if (routines.isEmpty()) View.VISIBLE else View.GONE
        current.routineList.visibility = if (routines.isEmpty()) View.GONE else View.VISIBLE

        InlineQuickAdd.bind(current.routineQuickAdd.root, onDismiss = { showRoutineFab() }) { title ->
            when (quickAddMode) {
                QuickAddMode.Routine -> {
                    routineAdapter.addItem(InMemoryDataStore.addRoutine(topicId, title))
                    current.emptyText.visibility = View.GONE
                    current.routineList.visibility = View.VISIBLE
                }
                QuickAddMode.TopicTitle -> {
                    InMemoryDataStore.updateGrowthTopicTitle(topicId, title)?.let { updated ->
                        current.root.findViewById<TextView>(R.id.headerTitle)?.text = updated.title
                    }
                }
            }
            quickAddMode = QuickAddMode.Routine
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
    }

    private fun showRoutineFab() {
        binding?.routineFabButton?.visibility = View.VISIBLE
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
            ids.forEach { InMemoryDataStore.deleteRoutine(it) }
            selectionController.exit()
            renderRoutines()
        }
    }

    private fun showMoveRoutineDialog() {
        MoveTargetDialogFragment(
            title = "\uC774\uB3D9\uD560 \uC138\uBD80 \uBD84\uC57C",
            addHint = "\uC0C8 \uC138\uBD80 \uBD84\uC57C \uC785\uB825",
            loadTargets = { InMemoryDataStore.getGrowthTopicsByArea(growthAreaId).map { MoveTarget(it.id, it.title) } },
            onAddTarget = { title -> InMemoryDataStore.addGrowthTopic(growthAreaId, title) },
            onTargetSelected = { target ->
                InMemoryDataStore.moveRoutinesToTopic(selectionController.selectedItemIds, target.id)
                selectionController.exit()
                renderRoutines()
            }
        ).show(childFragmentManager, "move_routines")
    }

    private fun renderRoutines() {
        val routines = InMemoryDataStore.getRoutinesByTopic(topicId)
        routineAdapter.submitList(routines)
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
        val topic = InMemoryDataStore.getGrowthTopic(topicId) ?: return
        quickAddMode = QuickAddMode.TopicTitle
        current.routineFabButton.visibility = View.GONE
        InlineQuickAdd.show(current.routineQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", topic.title)
    }

    private fun confirmDeleteTopic() {
        val topicTitle = InMemoryDataStore.getGrowthTopic(topicId)?.title
        DeleteConfirmDialogHelper.showSingle(requireContext(), topicTitle) {
            InMemoryDataStore.deleteGrowthTopic(topicId)
            findNavController().navigateUp()
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

    private fun routineSelectionScope(): String = "growth-routines:$topicId"

    private enum class QuickAddMode {
        Routine,
        TopicTitle
    }
}
