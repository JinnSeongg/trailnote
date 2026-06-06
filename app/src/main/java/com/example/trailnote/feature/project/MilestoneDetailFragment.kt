package com.example.trailnote.feature.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.ProgressCalculator
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentMilestoneDetailBinding

class MilestoneDetailFragment : Fragment() {
    private var binding: FragmentMilestoneDetailBinding? = null
    private lateinit var shortTaskAdapter: ShortTaskAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentMilestoneDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val milestoneId = requireArguments().getString("milestoneId").orEmpty()
        val milestone = InMemoryDataStore.getMilestone(milestoneId) ?: return
        val tasks = InMemoryDataStore.getShortTasksByMilestone(milestoneId)
        val current = binding ?: return
        current.root.setHeader(milestone.title, action = "···", showBack = true, onBack = { findNavController().popBackStack() })
        current.descriptionText.text = milestone.description
        current.progressText.text = "진행률 ${ProgressCalculator.milestoneProgress(tasks)}% · 목표 ${milestone.targetDate}"

        shortTaskAdapter = ShortTaskAdapter(tasks)
        current.shortTaskList.layoutManager = LinearLayoutManager(requireContext())
        current.shortTaskList.adapter = shortTaskAdapter
        current.emptyText.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        current.shortTaskList.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE

        InlineQuickAdd.bind(current.shortTaskQuickAdd.root, onDismiss = { showShortTaskFab() }) { title ->
            shortTaskAdapter.addItem(InMemoryDataStore.addShortTask(milestoneId, title))
            current.emptyText.visibility = View.GONE
            current.shortTaskList.visibility = View.VISIBLE
        }
        current.shortTaskAddButton.setOnClickListener {
            current.shortTaskFabButton.visibility = View.GONE
            InlineQuickAdd.show(current.shortTaskQuickAdd.root, "새 단기목표 입력")
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

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
