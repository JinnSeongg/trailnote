package com.example.trailnote.feature.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.R
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.ProgressCalculator
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentProjectDetailBinding

class ProjectDetailFragment : Fragment() {
    private var binding: FragmentProjectDetailBinding? = null
    private lateinit var milestoneAdapter: MilestoneAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentProjectDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val projectId = requireArguments().getString("projectId").orEmpty()
        val project = InMemoryDataStore.getProject(projectId) ?: return
        val milestones = InMemoryDataStore.getMilestonesByProject(projectId)
        val progress = ProgressCalculator.projectProgress(milestones.map { milestone ->
            ProgressCalculator.milestoneProgress(InMemoryDataStore.getShortTasksByMilestone(milestone.id))
        })
        val current = binding ?: return
        current.root.setHeader(project.title, action = "···", showBack = true, onBack = { findNavController().popBackStack() })
        current.descriptionText.text = project.description
        current.progressText.text = "진행률 $progress% · 목표 ${project.targetDate}"

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
            milestoneAdapter.addItem(InMemoryDataStore.addMilestone(projectId, title))
            current.emptyText.visibility = View.GONE
            current.milestoneList.visibility = View.VISIBLE
        }
        current.milestoneAddButton.setOnClickListener {
            current.milestoneFabButton.visibility = View.GONE
            InlineQuickAdd.show(current.milestoneQuickAdd.root, "새 중기목표 입력")
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

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
