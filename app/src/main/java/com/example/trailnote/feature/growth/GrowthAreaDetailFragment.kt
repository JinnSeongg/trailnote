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
import com.example.trailnote.MainActivity
import com.example.trailnote.R
import com.example.trailnote.core.selection.MoveTarget
import com.example.trailnote.core.selection.MoveTargetDialogFragment
import com.example.trailnote.core.selection.RecyclerDragSelectionHelper
import com.example.trailnote.core.selection.SelectionState
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
    private var topicDragSelectionHelper: RecyclerDragSelectionHelper? = null
    private val selectionStateListener: (SelectionState) -> Unit = {
        if (::growthTopicAdapter.isInitialized) growthTopicAdapter.notifyDataSetChanged()
    }

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

        growthTopicAdapter = GrowthTopicAdapter(
            sourceItems = topics,
            onClick = ::handleTopicClick,
            onLongClick = ::handleTopicLongClick,
            isSelected = ::isTopicSelected
        )
        selectionController.addStateListener(selectionStateListener)
        current.growthTopicList.layoutManager = LinearLayoutManager(requireContext())
        current.growthTopicList.adapter = growthTopicAdapter
        attachTopicDragHelper()
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

    private fun handleTopicClick(topic: com.example.trailnote.domain.model.GrowthTopic) {
        if (selectionController.isInSelectionMode) {
            selectionController.toggle(topic.id, topicSelectionScope())
        } else {
            findNavController().navigate(
                R.id.action_growthAreaDetailFragment_to_growthTopicDetailFragment,
                bundleOf("growthAreaId" to areaId, "topicId" to topic.id)
            )
        }
    }

    private fun handleTopicLongClick(topic: com.example.trailnote.domain.model.GrowthTopic) {
        prepareTopicSelectionHandlers()
        selectionController.enter(topicSelectionScope(), listOf(topic.id))
    }

    private fun prepareTopicSelectionHandlers() {
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(::confirmDeleteSelectedTopics)
            setSelectionMoveHandler(::showMoveTopicDialog)
        }
    }

    private fun isTopicSelected(topic: com.example.trailnote.domain.model.GrowthTopic): Boolean {
        return selectionController.isInSelectionMode && topic.id in selectionController.selectedItemIds
    }

    private fun confirmDeleteSelectedTopics() {
        val ids = selectionController.selectedItemIds.toList()
        if (ids.isEmpty()) return
        AlertDialog.Builder(requireContext())
            .setMessage("\uC120\uD0DD\uD55C \uD56D\uBAA9\uC744 \uC0AD\uC81C\uD560\uAE4C\uC694?")
            .setNegativeButton("\uCDE8\uC18C", null)
            .setPositiveButton("\uC0AD\uC81C") { _, _ ->
                ids.forEach { InMemoryDataStore.deleteGrowthTopic(it) }
                selectionController.exit()
                renderTopics()
            }
            .show()
    }

    private fun showMoveTopicDialog() {
        MoveTargetDialogFragment(
            title = "\uC774\uB3D9\uD560 \uC131\uC7A5 \uBD84\uC57C",
            addHint = "\uC0C8 \uC131\uC7A5 \uBD84\uC57C \uC785\uB825",
            loadTargets = { InMemoryDataStore.getGrowthAreas().map { MoveTarget(it.id, it.title) } },
            onAddTarget = { title -> InMemoryDataStore.addGrowthArea(title) },
            onTargetSelected = { target ->
                InMemoryDataStore.moveGrowthTopicsToArea(selectionController.selectedItemIds, target.id)
                selectionController.exit()
                renderTopics()
            }
        ).show(childFragmentManager, "move_growth_topics")
    }

    private fun renderTopics() {
        val topics = InMemoryDataStore.getGrowthTopicsByArea(areaId)
        growthTopicAdapter.submitList(topics)
        binding?.emptyText?.visibility = if (topics.isEmpty()) View.VISIBLE else View.GONE
        binding?.growthTopicList?.visibility = if (topics.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun attachTopicDragHelper() {
        val current = binding ?: return
        if (topicDragSelectionHelper != null) return
        topicDragSelectionHelper = RecyclerDragSelectionHelper(
            recyclerView = current.growthTopicList,
            selectionController = selectionController,
            getItemId = { position -> growthTopicAdapter.getItem(position)?.id },
            getItemScope = { position -> growthTopicAdapter.getItem(position)?.let { topicSelectionScope() } },
            isItemSelected = { position -> growthTopicAdapter.getItem(position)?.let(::isTopicSelected) == true },
            onDragStarted = { prepareTopicSelectionHandlers() },
            onSelectionChanged = { growthTopicAdapter.notifyDataSetChanged() }
        ).also { current.growthTopicList.addOnItemTouchListener(it) }
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
        selectionController.removeStateListener(selectionStateListener)
        topicDragSelectionHelper?.let { binding?.growthTopicList?.removeOnItemTouchListener(it) }
        topicDragSelectionHelper = null
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(null)
            setSelectionMoveHandler(null)
        }
        binding = null
        super.onDestroyView()
    }

    private val selectionController
        get() = (requireActivity() as MainActivity).selectionController

    private fun topicSelectionScope(): String = "growth-topics:$areaId"

    private enum class QuickAddMode {
        GrowthTopic,
        AreaTitle
    }
}
