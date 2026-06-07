package com.example.trailnote.feature.project

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.MainActivity
import com.example.trailnote.R
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
import com.example.trailnote.databinding.FragmentProjectDetailBinding

class ProjectDetailFragment : Fragment() {
    private var binding: FragmentProjectDetailBinding? = null
    private lateinit var milestoneAdapter: MilestoneAdapter
    private var projectId: String = ""
    private var descriptionWatcher: TextWatcher? = null
    private var quickAddMode: QuickAddMode = QuickAddMode.Milestone
    private var milestoneDragSelectionHelper: RecyclerDragSelectionHelper? = null
    private val selectionStateListener: (SelectionState) -> Unit = {
        if (::milestoneAdapter.isInitialized) milestoneAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentProjectDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        projectId = requireArguments().getString("projectId").orEmpty()
        val project = InMemoryDataStore.getProject(projectId) ?: return
        val milestones = InMemoryDataStore.getMilestonesByProject(projectId)
        val current = binding ?: return
        current.root.setHeader(
            project.title,
            action = "\u00B7\u00B7\u00B7",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = { showProjectMenu() }
        )
        current.descriptionText.setText(project.description)
        descriptionWatcher = current.descriptionText.setupTwoLineLimitedDescriptionEditText { text ->
            InMemoryDataStore.updateProjectDescription(projectId, text)
        }

        milestoneAdapter = MilestoneAdapter(
            milestones,
            onClick = ::handleMilestoneClick,
            onLongClick = ::handleMilestoneLongClick,
            isSelected = ::isMilestoneSelected
        )
        selectionController.addStateListener(selectionStateListener)
        current.milestoneList.layoutManager = LinearLayoutManager(requireContext())
        current.milestoneList.adapter = milestoneAdapter
        attachMilestoneDragHelper()
        current.emptyText.visibility = if (milestones.isEmpty()) View.VISIBLE else View.GONE
        current.milestoneList.visibility = if (milestones.isEmpty()) View.GONE else View.VISIBLE

        InlineQuickAdd.bind(current.milestoneQuickAdd.root, onDismiss = { showMilestoneFab() }) { title ->
            when (quickAddMode) {
                QuickAddMode.Milestone -> {
                    milestoneAdapter.addItem(InMemoryDataStore.addMilestone(projectId, title))
                    current.emptyText.visibility = View.GONE
                    current.milestoneList.visibility = View.VISIBLE
                }
                QuickAddMode.ProjectTitle -> {
                    InMemoryDataStore.updateProjectTitle(projectId, title)?.let { updated ->
                        current.root.findViewById<TextView>(R.id.headerTitle)?.text = updated.title
                    }
                }
            }
            quickAddMode = QuickAddMode.Milestone
        }
        current.milestoneAddButton.setOnClickListener {
            quickAddMode = QuickAddMode.Milestone
            current.milestoneFabButton.visibility = View.GONE
            InlineQuickAdd.show(current.milestoneQuickAdd.root, "\uC0C8 \uC911\uAE30\uBAA9\uD45C \uC785\uB825")
        }
        current.milestoneFabButton.setOnClickListener {
            current.milestoneAddButton.performClick()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                InlineQuickAdd.hide(current.milestoneQuickAdd.root)
                showMilestoneFab()
                isEnabled = false
            }
        }.also { callback ->
            current.milestoneQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
    }

    private fun showMilestoneFab() {
        binding?.milestoneFabButton?.visibility = View.VISIBLE
    }

    private fun handleMilestoneClick(milestone: com.example.trailnote.domain.model.Milestone) {
        if (selectionController.isInSelectionMode) {
            selectionController.toggle(milestone.id, milestoneSelectionScope())
        } else {
            findNavController().navigate(
                R.id.action_projectDetailFragment_to_milestoneDetailFragment,
                bundleOf("projectId" to projectId, "milestoneId" to milestone.id)
            )
        }
    }

    private fun handleMilestoneLongClick(milestone: com.example.trailnote.domain.model.Milestone) {
        prepareMilestoneSelectionHandlers()
    }

    private fun prepareMilestoneSelectionHandlers() {
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(::confirmDeleteSelectedMilestones)
            setSelectionMoveHandler(::showMoveMilestoneDialog)
        }
    }

    private fun isMilestoneSelected(milestone: com.example.trailnote.domain.model.Milestone): Boolean {
        return selectionController.isInSelectionMode && milestone.id in selectionController.selectedItemIds
    }

    private fun confirmDeleteSelectedMilestones() {
        val ids = selectionController.selectedItemIds.toList()
        if (ids.isEmpty()) return
        DeleteConfirmDialogHelper.showMultiple(requireContext(), ids.size) {
            ids.forEach { InMemoryDataStore.deleteMilestone(it) }
            selectionController.exit()
            val milestones = InMemoryDataStore.getMilestonesByProject(projectId)
            milestoneAdapter.submitList(milestones)
            binding?.emptyText?.visibility = if (milestones.isEmpty()) View.VISIBLE else View.GONE
            binding?.milestoneList?.visibility = if (milestones.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun attachMilestoneDragHelper() {
        val current = binding ?: return
        if (milestoneDragSelectionHelper != null) return
        milestoneDragSelectionHelper = RecyclerDragSelectionHelper(
            recyclerView = current.milestoneList,
            selectionController = selectionController,
            getItemId = { position -> milestoneAdapter.getItem(position)?.id },
            getItemScope = { position -> milestoneAdapter.getItem(position)?.let { milestoneSelectionScope() } },
            isItemSelected = { position -> milestoneAdapter.getItem(position)?.let(::isMilestoneSelected) == true },
            onDragStarted = { prepareMilestoneSelectionHandlers() },
            onSelectionChanged = { milestoneAdapter.notifyDataSetChanged() }
        ).also { current.milestoneList.addOnItemTouchListener(it) }
    }

    private fun showMoveMilestoneDialog() {
        MoveTargetDialogFragment(
            title = "\uC774\uB3D9\uD560 \uD504\uB85C\uC81D\uD2B8",
            addHint = "\uC0C8 \uD504\uB85C\uC81D\uD2B8 \uC785\uB825",
            loadTargets = {
                InMemoryDataStore.getProjects().map { project ->
                    MoveTarget(project.id, project.title)
                }
            },
            onAddTarget = { title -> InMemoryDataStore.addProject(title) },
            onTargetSelected = { target ->
                val ids = selectionController.selectedItemIds.toList()
                InMemoryDataStore.moveMilestonesToProject(ids, target.id)
                selectionController.exit()
                val milestones = InMemoryDataStore.getMilestonesByProject(projectId)
                milestoneAdapter.submitList(milestones)
                binding?.emptyText?.visibility = if (milestones.isEmpty()) View.VISIBLE else View.GONE
                binding?.milestoneList?.visibility = if (milestones.isEmpty()) View.GONE else View.VISIBLE
            }
        ).show(childFragmentManager, "move_milestones")
    }

    private fun showProjectMenu() {
        val current = binding ?: return
        val anchor = current.root.findViewById<View>(R.id.headerAction)
        PopupMenuHelper.show(requireContext(), anchor, listOf("\uC218\uC815", "\uC0AD\uC81C")) { title ->
            when (title) {
                "\uC218\uC815" -> {
                    openTitleEdit()
                    true
                }
                "\uC0AD\uC81C" -> {
                    confirmDeleteProject()
                    true
                }
                else -> false
            }
        }
    }

    private fun openTitleEdit() {
        val current = binding ?: return
        val project = InMemoryDataStore.getProject(projectId) ?: return
        quickAddMode = QuickAddMode.ProjectTitle
        current.milestoneFabButton.visibility = View.GONE
        InlineQuickAdd.show(current.milestoneQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", project.title)
    }

    private fun confirmDeleteProject() {
        val projectTitle = InMemoryDataStore.getProject(projectId)?.title
        DeleteConfirmDialogHelper.showSingle(requireContext(), projectTitle) {
            InMemoryDataStore.deleteProject(projectId)
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        descriptionWatcher?.let { watcher ->
            binding?.descriptionText?.removeTextChangedListener(watcher)
        }
        descriptionWatcher = null
        selectionController.removeStateListener(selectionStateListener)
        milestoneDragSelectionHelper?.let { binding?.milestoneList?.removeOnItemTouchListener(it) }
        milestoneDragSelectionHelper = null
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(null)
            setSelectionMoveHandler(null)
        }
        binding = null
        super.onDestroyView()
    }

    private val selectionController
        get() = (requireActivity() as MainActivity).selectionController

    private fun milestoneSelectionScope(): String = "project-milestones:$projectId"

    private enum class QuickAddMode {
        Milestone,
        ProjectTitle
    }
}
