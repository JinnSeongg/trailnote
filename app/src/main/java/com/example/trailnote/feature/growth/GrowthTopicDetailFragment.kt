package com.example.trailnote.feature.growth

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
import com.example.trailnote.databinding.FragmentGrowthTopicDetailBinding

class GrowthTopicDetailFragment : Fragment() {
    private var binding: FragmentGrowthTopicDetailBinding? = null
    private lateinit var routineAdapter: RoutineAdapter
    private var descriptionWatcher: TextWatcher? = null
    private var topicId: String = ""
    private var quickAddMode: QuickAddMode = QuickAddMode.Routine

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentGrowthTopicDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        routineAdapter = RoutineAdapter(routines)
        current.routineList.adapter = routineAdapter
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

    private fun showTopicMenu() {
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
                        confirmDeleteTopic()
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    private fun openTitleEdit() {
        val current = binding ?: return
        val topic = InMemoryDataStore.getGrowthTopic(topicId) ?: return
        quickAddMode = QuickAddMode.TopicTitle
        current.routineFabButton.visibility = View.GONE
        InlineQuickAdd.show(current.routineQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", topic.title)
    }

    private fun confirmDeleteTopic() {
        AlertDialog.Builder(requireContext())
            .setMessage("\uC0AD\uC81C\uD560\uAE4C\uC694?")
            .setNegativeButton("\uCDE8\uC18C", null)
            .setPositiveButton("\uC0AD\uC81C") { _, _ ->
                InMemoryDataStore.deleteGrowthTopic(topicId)
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
        Routine,
        TopicTitle
    }
}
