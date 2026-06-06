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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.R
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.setupTwoLineLimitedDescriptionEditText
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentGrowthAreaDetailBinding

class GrowthAreaDetailFragment : Fragment() {
    private var binding: FragmentGrowthAreaDetailBinding? = null
    private lateinit var growthTopicAdapter: GrowthTopicAdapter
    private var descriptionWatcher: TextWatcher? = null
    private var areaId: String = ""
    private var quickAddMode: QuickAddMode = QuickAddMode.GrowthTopic

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentGrowthAreaDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        areaId = requireArguments().getString("growthAreaId").orEmpty()
        val area = InMemoryDataStore.getGrowthArea(areaId) ?: return
        val topics = InMemoryDataStore.getGrowthTopicsByArea(areaId)
        val current = binding ?: return
        current.root.setHeader(
            area.title,
            action = "\u00B7\u00B7\u00B7",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = { showAreaMenu() }
        )
        current.descriptionText.setText(area.description)
        descriptionWatcher = current.descriptionText.setupTwoLineLimitedDescriptionEditText { text ->
            InMemoryDataStore.updateGrowthAreaDescription(areaId, text)
        }

        growthTopicAdapter = GrowthTopicAdapter(topics) { topic ->
            findNavController().navigate(
                R.id.action_growthAreaDetailFragment_to_growthTopicDetailFragment,
                bundleOf("growthAreaId" to areaId, "topicId" to topic.id)
            )
        }
        current.growthTopicList.layoutManager = LinearLayoutManager(requireContext())
        current.growthTopicList.adapter = growthTopicAdapter
        current.emptyText.visibility = if (topics.isEmpty()) View.VISIBLE else View.GONE
        current.growthTopicList.visibility = if (topics.isEmpty()) View.GONE else View.VISIBLE

        InlineQuickAdd.bind(current.growthTopicQuickAdd.root, onDismiss = { showGrowthTopicFab() }) { title ->
            when (quickAddMode) {
                QuickAddMode.GrowthTopic -> {
                    growthTopicAdapter.addItem(InMemoryDataStore.addGrowthTopic(areaId, title))
                    current.emptyText.visibility = View.GONE
                    current.growthTopicList.visibility = View.VISIBLE
                }
                QuickAddMode.AreaTitle -> {
                    InMemoryDataStore.updateGrowthAreaTitle(areaId, title)?.let { updated ->
                        current.root.findViewById<TextView>(R.id.headerTitle)?.text = updated.title
                    }
                }
            }
            quickAddMode = QuickAddMode.GrowthTopic
        }
        current.growthTopicAddButton.setOnClickListener {
            quickAddMode = QuickAddMode.GrowthTopic
            current.growthTopicFabButton.visibility = View.GONE
            InlineQuickAdd.show(current.growthTopicQuickAdd.root, "\uC0C8 \uC138\uBD80 \uBD84\uC57C \uC785\uB825")
        }
        current.growthTopicFabButton.setOnClickListener {
            current.growthTopicAddButton.performClick()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                InlineQuickAdd.hide(current.growthTopicQuickAdd.root)
                showGrowthTopicFab()
                isEnabled = false
            }
        }.also { callback ->
            current.growthTopicQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
    }

    private fun showGrowthTopicFab() {
        binding?.growthTopicFabButton?.visibility = View.VISIBLE
    }

    private fun showAreaMenu() {
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
                        confirmDeleteArea()
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    private fun openTitleEdit() {
        val current = binding ?: return
        val area = InMemoryDataStore.getGrowthArea(areaId) ?: return
        quickAddMode = QuickAddMode.AreaTitle
        current.growthTopicFabButton.visibility = View.GONE
        InlineQuickAdd.show(current.growthTopicQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", area.title)
    }

    private fun confirmDeleteArea() {
        AlertDialog.Builder(requireContext())
            .setMessage("\uC0AD\uC81C\uD560\uAE4C\uC694?")
            .setNegativeButton("\uCDE8\uC18C", null)
            .setPositiveButton("\uC0AD\uC81C") { _, _ ->
                InMemoryDataStore.deleteGrowthArea(areaId)
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
        GrowthTopic,
        AreaTitle
    }
}
