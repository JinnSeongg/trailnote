package com.example.trailnote.feature.growth

import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.FragmentGrowthAreaDetailBinding
import com.example.trailnote.domain.model.GrowthArea
import com.example.trailnote.domain.model.GrowthColorPalette
import com.example.trailnote.domain.model.GrowthTopic
import kotlinx.coroutines.launch

class GrowthAreaDetailFragment : Fragment() {
    private var binding: FragmentGrowthAreaDetailBinding? = null
    private lateinit var growthTopicAdapter: GrowthTopicAdapter
    private var descriptionWatcher: TextWatcher? = null
    private var areaId: String = ""
    private var quickAddMode: QuickAddMode = QuickAddMode.GrowthTopic
    private var topicDragSelectionHelper: RecyclerDragSelectionHelper? = null
    private var area: GrowthArea? = null
    private var areas: List<GrowthArea> = emptyList()
    private var topics: List<GrowthTopic> = emptyList()
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
        val current = binding ?: return
        descriptionWatcher = current.descriptionText.setupTwoLineLimitedDescriptionEditText { text ->
            viewLifecycleOwner.lifecycleScope.launch {
                repository.updateGrowthAreaDescription(areaId, text)
            }
        }

        growthTopicAdapter = GrowthTopicAdapter(
            sourceItems = emptyList(),
            onClick = ::handleTopicClick,
            onLongClick = ::handleTopicLongClick,
            isSelected = ::isTopicSelected
        )
        selectionController.addStateListener(selectionStateListener)
        current.growthTopicList.layoutManager = LinearLayoutManager(requireContext())
        current.growthTopicList.adapter = growthTopicAdapter
        attachTopicDragHelper()

        InlineQuickAdd.bind(current.growthTopicQuickAdd.root, onDismiss = { showGrowthTopicFab() }) { title ->
            viewLifecycleOwner.lifecycleScope.launch {
                when (quickAddMode) {
                    QuickAddMode.GrowthTopic -> repository.addGrowthTopic(areaId, title)
                    QuickAddMode.AreaTitle -> {
                        repository.updateGrowthAreaTitle(areaId, title)?.let { updated ->
                            current.root.findViewById<TextView>(R.id.headerTitle)?.text = updated.title
                        }
                    }
                }
                quickAddMode = QuickAddMode.GrowthTopic
                reloadArea()
            }
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
        reloadArea()
    }

    override fun onResume() {
        super.onResume()
        if (binding != null && ::growthTopicAdapter.isInitialized) {
            reloadArea()
        }
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
        DeleteConfirmDialogHelper.showMultiple(requireContext(), ids.size) {
            viewLifecycleOwner.lifecycleScope.launch {
                ids.forEach { repository.deleteGrowthTopic(it) }
                selectionController.exit()
                reloadArea()
            }
        }
    }

    private fun showMoveTopicDialog() {
        MoveTargetDialogFragment(
            title = "\uC774\uB3D9\uD560 \uC131\uC7A5 \uBD84\uC57C",
            addHint = "\uC0C8 \uC131\uC7A5 \uBD84\uC57C \uC785\uB825",
            loadTargets = { areas.map { MoveTarget(it.id, it.title) } },
            onAddTarget = { title ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.addGrowthArea(title)
                    reloadArea()
                }
            },
            onTargetSelected = { target ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.moveGrowthTopicsToArea(selectionController.selectedItemIds, target.id)
                    selectionController.exit()
                    reloadArea()
                }
            }
        ).show(childFragmentManager, "move_growth_topics")
    }

    private fun renderTopics() {
        growthTopicAdapter.submitList(topics, area?.colorHex ?: GrowthColorPalette.DEFAULT_COLOR)
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
        PopupMenuHelper.show(requireContext(), anchor, listOf("\uC218\uC815", "\uC0C9\uC0C1", "\uC0AD\uC81C")) { title ->
            when (title) {
                "\uC218\uC815" -> {
                    openTitleEdit()
                    true
                }
                "\uC0C9\uC0C1" -> {
                    showColorDialog()
                    true
                }
                "\uC0AD\uC81C" -> {
                    confirmDeleteArea()
                    true
                }
                else -> false
            }
        }
    }

    private fun showColorDialog() {
        val currentArea = area ?: return
        GrowthAreaColorDialogFragment.newInstance(currentArea.colorHex).apply {
            onColorSelected = { colorHex ->
                Log.d(TAG, "dialog callback areaId=$areaId selected=$colorHex")
                if (this@GrowthAreaDetailFragment.isAdded && binding != null) {
                    this@GrowthAreaDetailFragment.viewLifecycleOwner.lifecycleScope.launch {
                        runCatching {
                            Log.d(TAG, "call repo update areaId=$areaId requested=$colorHex")
                            repository.updateGrowthAreaColor(areaId, colorHex)
                        }.onSuccess {
                            if (this@GrowthAreaDetailFragment.isAdded && binding != null) reloadArea()
                        }.onFailure { throwable ->
                            Log.e(TAG, "Failed to update growth area color", throwable)
                            if (this@GrowthAreaDetailFragment.isAdded) {
                                Toast.makeText(requireContext(), "\uC0C9\uC0C1 \uBCC0\uACBD\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }.show(childFragmentManager, "growth_area_color")
    }

    private fun openTitleEdit() {
        val current = binding ?: return
        val area = area ?: return
        quickAddMode = QuickAddMode.AreaTitle
        current.growthTopicFabButton.visibility = View.GONE
        InlineQuickAdd.show(current.growthTopicQuickAdd.root, "\uC81C\uBAA9 \uC785\uB825", area.title)
    }

    private fun confirmDeleteArea() {
        val areaTitle = area?.title
        DeleteConfirmDialogHelper.showSingle(requireContext(), areaTitle) {
            viewLifecycleOwner.lifecycleScope.launch {
                repository.deleteGrowthArea(areaId)
                findNavController().navigateUp()
            }
        }
    }

    private fun reloadArea() {
        viewLifecycleOwner.lifecycleScope.launch {
            area = repository.getGrowthAreaById(areaId)
            if (area == null) {
                findNavController().navigateUp()
                return@launch
            }
            Log.d(TAG, "areaDetail loaded areaId=$areaId color=${area?.colorHex}")
            areas = repository.getGrowthAreas()
            topics = repository.getGrowthTopicsByAreaId(areaId)
            renderArea()
            renderTopics()
        }
    }

    private fun renderArea() {
        val current = binding ?: return
        val currentArea = area ?: return
        current.root.setHeader(
            currentArea.title,
            action = "\u00B7\u00B7\u00B7",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = { showAreaMenu() }
        )
        if (current.descriptionText.text.toString() != currentArea.description) {
            current.descriptionText.setText(currentArea.description)
        }
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

    private val repository
        get() = RepositoryProvider.getRepository(requireContext())

    private fun topicSelectionScope(): String = "growth-topics:$areaId"

    private enum class QuickAddMode {
        GrowthTopic,
        AreaTitle
    }

    private companion object {
        const val TAG = "GrowthColorDebug"
    }
}
