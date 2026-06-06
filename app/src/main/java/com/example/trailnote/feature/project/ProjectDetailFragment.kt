package com.example.trailnote.feature.project

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.R
import com.example.trailnote.core.util.InlineQuickAdd
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

        milestoneAdapter = MilestoneAdapter(milestones) { milestone ->
            findNavController().navigate(
                R.id.action_projectDetailFragment_to_milestoneDetailFragment,
                bundleOf("projectId" to projectId, "milestoneId" to milestone.id)
            )
        }
        current.milestoneList.layoutManager = LinearLayoutManager(requireContext())
        current.milestoneList.adapter = milestoneAdapter
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

    private fun showProjectMenu() {
        val current = binding ?: return
        val anchor = current.root.findViewById<View>(R.id.headerAction)
        PopupMenu(requireContext(), anchor).apply {
            menu.add("\uC218\uC815")
            menu.add("\uC0AD\uC81C")
            setOnMenuItemClickListener { item ->
                when (item.title.toString()) {
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
        }.show()
    }

    private fun openTitleEdit() {
        val current = binding ?: return
        val project = InMemoryDataStore.getProject(projectId) ?: return
        quickAddMode = QuickAddMode.ProjectTitle
        current.milestoneFabButton.visibility = View.GONE
        InlineQuickAdd.show(current.milestoneQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", project.title)
    }

    private fun confirmDeleteProject() {
        AlertDialog.Builder(requireContext())
            .setMessage("\uC0AD\uC81C\uD560\uAE4C\uC694?")
            .setNegativeButton("\uCDE8\uC18C", null)
            .setPositiveButton("\uC0AD\uC81C") { _, _ ->
                InMemoryDataStore.deleteProject(projectId)
                findNavController().navigateUp()
            }
            .show()
    }

    override fun onDestroyView() {
        descriptionWatcher?.let { watcher ->
            binding?.descriptionText?.removeTextChangedListener(watcher)
        }
        descriptionWatcher = null
        binding = null
        super.onDestroyView()
    }

    private enum class QuickAddMode {
        Milestone,
        ProjectTitle
    }
}
