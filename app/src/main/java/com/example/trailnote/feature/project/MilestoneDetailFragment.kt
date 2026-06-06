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
import androidx.fragment.app.Fragment
import com.example.trailnote.R
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.setupTwoLineLimitedDescriptionEditText
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentMilestoneDetailBinding

class MilestoneDetailFragment : Fragment() {
    private var binding: FragmentMilestoneDetailBinding? = null
    private lateinit var shortTaskAdapter: ShortTaskAdapter
    private var milestoneId: String = ""
    private var descriptionWatcher: TextWatcher? = null
    private var quickAddMode: QuickAddMode = QuickAddMode.ShortTask

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentMilestoneDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        shortTaskAdapter = ShortTaskAdapter(tasks)
        current.shortTaskList.layoutManager = LinearLayoutManager(requireContext())
        current.shortTaskList.adapter = shortTaskAdapter
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

    private fun showMilestoneMenu() {
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
                        confirmDeleteMilestone()
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    private fun openTitleEdit() {
        val current = binding ?: return
        val milestone = InMemoryDataStore.getMilestone(milestoneId) ?: return
        quickAddMode = QuickAddMode.MilestoneTitle
        current.shortTaskFabButton.visibility = View.GONE
        InlineQuickAdd.show(current.shortTaskQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", milestone.title)
    }

    private fun confirmDeleteMilestone() {
        AlertDialog.Builder(requireContext())
            .setMessage("\uC0AD\uC81C\uD560\uAE4C\uC694?")
            .setNegativeButton("\uCDE8\uC18C", null)
            .setPositiveButton("\uC0AD\uC81C") { _, _ ->
                InMemoryDataStore.deleteMilestone(milestoneId)
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
        ShortTask,
        MilestoneTitle
    }
}
