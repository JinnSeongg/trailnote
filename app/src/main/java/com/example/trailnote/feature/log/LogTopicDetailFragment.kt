package com.example.trailnote.feature.log

import android.os.Bundle
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
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentLogTopicDetailBinding

class LogTopicDetailFragment : Fragment() {
    private var binding: FragmentLogTopicDetailBinding? = null
    private lateinit var entryAdapter: LogEntryAdapter
    private var topicId: String = ""
    private var quickAddMode: QuickAddMode = QuickAddMode.Entry

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentLogTopicDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        topicId = requireArguments().getString("topicId").orEmpty()
        val topic = InMemoryDataStore.getLogTopic(topicId) ?: return
        val category = InMemoryDataStore.getLogCategories().firstOrNull { it.id == topic.categoryId }
        val entries = InMemoryDataStore.getLogEntriesByTopic(topicId)
        val current = binding ?: return

        current.root.setHeader(
            title = topic.title,
            action = "\u2315",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = {
                // TODO: Connect log entry search.
            }
        )
        current.root.findViewById<TextView>(R.id.headerMore)?.apply {
            visibility = View.VISIBLE
            setOnClickListener { showTopicMenu(this) }
        }
        current.categoryText.text = category?.name.orEmpty()
        current.entryList.layoutManager = LinearLayoutManager(requireContext())
        entryAdapter = LogEntryAdapter(entries) { entry ->
            findNavController().navigate(
                R.id.action_logTopicDetailFragment_to_logEntryDetailFragment,
                bundleOf("entryId" to entry.id)
            )
        }
        current.entryList.adapter = entryAdapter
        current.emptyText.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        current.entryList.visibility = if (entries.isEmpty()) View.GONE else View.VISIBLE

        InlineQuickAdd.bind(current.entryQuickAdd.root, onDismiss = { showFab() }) { title ->
            when (quickAddMode) {
                QuickAddMode.Entry -> {
                    entryAdapter.addItem(InMemoryDataStore.addLogEntry(topicId, title))
                    current.emptyText.visibility = View.GONE
                    current.entryList.visibility = View.VISIBLE
                }
                QuickAddMode.TopicTitle -> {
                    InMemoryDataStore.updateLogTopicTitle(topicId, title)?.let { updated ->
                        current.root.findViewById<TextView>(R.id.headerTitle)?.text = updated.title
                    }
                }
            }
            quickAddMode = QuickAddMode.Entry
        }
        current.entryAddButton.setOnClickListener {
            quickAddMode = QuickAddMode.Entry
            openQuickInput("\uC0C8 \uAE30\uB85D \uC81C\uBAA9 \uC785\uB825")
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                closeQuickInput()
                isEnabled = false
            }
        }.also { callback ->
            current.entryQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
    }

    private fun openQuickInput(hint: String) {
        val current = binding ?: return
        current.entryAddButton.visibility = View.GONE
        InlineQuickAdd.show(current.entryQuickAdd.root, hint)
    }

    private fun openTitleEdit() {
        val current = binding ?: return
        val topic = InMemoryDataStore.getLogTopic(topicId) ?: return
        quickAddMode = QuickAddMode.TopicTitle
        current.entryAddButton.visibility = View.GONE
        InlineQuickAdd.show(current.entryQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", topic.title)
    }

    private fun closeQuickInput() {
        val current = binding ?: return
        InlineQuickAdd.hide(current.entryQuickAdd.root)
        quickAddMode = QuickAddMode.Entry
        showFab()
    }

    private fun showTopicMenu(anchor: View) {
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

    private fun confirmDeleteTopic() {
        AlertDialog.Builder(requireContext())
            .setMessage("\uC0AD\uC81C\uD560\uAE4C\uC694?")
            .setNegativeButton("\uCDE8\uC18C", null)
            .setPositiveButton("\uC0AD\uC81C") { _, _ ->
                InMemoryDataStore.deleteLogTopic(topicId)
                findNavController().navigateUp()
            }
            .show()
    }

    private fun showFab() {
        binding?.entryAddButton?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private enum class QuickAddMode {
        Entry,
        TopicTitle
    }
}
