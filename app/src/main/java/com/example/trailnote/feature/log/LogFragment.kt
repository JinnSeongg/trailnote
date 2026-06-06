package com.example.trailnote.feature.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.R
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentLogBinding
import com.example.trailnote.domain.model.LogTopic

class LogFragment : Fragment() {
    private var binding: FragmentLogBinding? = null
    private var selectedCategoryId: String? = null
    private var quickAddTargetTopic: LogTopic? = null
    private var topicSectionAdapter: LogTopicSectionAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentLogBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.root.setHeader("기록", action = "⌕")
        InlineQuickAdd.bind(current.categoryQuickAdd.root) { text ->
            val targetTopic = quickAddTargetTopic
            if (targetTopic == null) {
                val category = InMemoryDataStore.addLogCategory(text)
                selectedCategoryId = category.id
                renderChips()
            } else {
                InMemoryDataStore.addLogEntry(targetTopic.id, text)
            }
            quickAddTargetTopic = null
            renderList()
        }
        current.categoryAddButton.setOnClickListener {
            quickAddTargetTopic = null
            InlineQuickAdd.show(current.categoryQuickAdd.root, "새 카테고리 입력")
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                quickAddTargetTopic = null
                InlineQuickAdd.hide(current.categoryQuickAdd.root)
                isEnabled = false
            }
        }.also { callback ->
            current.categoryQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
        renderChips()
        renderList()
    }

    private fun renderChips() {
        val current = binding ?: return
        current.categoryChipContainer.removeAllViews()
        val chipEntries = listOf(null to "전체") + InMemoryDataStore.getLogCategories().map { it.id to it.name }
        chipEntries.forEach { (id, label) ->
            val chip = TextView(requireContext()).apply {
                text = label
                textSize = 12f
                setTextColor(resources.getColor(if (selectedCategoryId == id) R.color.white else R.color.trail_text_secondary, null))
                setBackgroundResource(if (selectedCategoryId == id) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
                setOnClickListener {
                    selectedCategoryId = id
                    quickAddTargetTopic = null
                    InlineQuickAdd.hide(current.categoryQuickAdd.root)
                    topicSectionAdapter?.closeOpen()
                    renderChips()
                    renderList()
                }
            }
            current.categoryChipContainer.addView(chip)
        }
    }

    private fun renderList() {
        val current = binding ?: return
        val visibleTopics = InMemoryDataStore.getLogTopicsByCategory(selectedCategoryId)
        topicSectionAdapter = LogTopicSectionAdapter(
            topics = visibleTopics,
            entriesForTopic = { topicId -> InMemoryDataStore.getLogEntriesByTopic(topicId) },
            onTopicClick = { topic ->
                findNavController().navigate(R.id.action_logFragment_to_logTopicDetailFragment, bundleOf("topicId" to topic.id))
            },
            onEntryClick = { entry ->
                findNavController().navigate(R.id.action_logFragment_to_logEntryDetailFragment, bundleOf("entryId" to entry.id))
            },
            onAddClick = { topic ->
                quickAddTargetTopic = topic
                InlineQuickAdd.show(current.categoryQuickAdd.root, "새 기록 제목 입력")
            }
        )
        current.topicSectionList.layoutManager = LinearLayoutManager(requireContext())
        current.topicSectionList.adapter = topicSectionAdapter
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}
