package com.example.trailnote.feature.growth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.MainActivity
import com.example.trailnote.R
import com.example.trailnote.core.selection.RecyclerDragSelectionHelper
import com.example.trailnote.core.selection.SelectionState
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentGrowthBinding
import com.example.trailnote.domain.model.GrowthArea

class GrowthFragment : Fragment() {
    private var binding: FragmentGrowthBinding? = null
    private lateinit var growthAreaAdapter: GrowthAreaAdapter
    private var areaDragSelectionHelper: RecyclerDragSelectionHelper? = null
    private val selectionStateListener: (SelectionState) -> Unit = {
        if (::growthAreaAdapter.isInitialized) growthAreaAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentGrowthBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.root.setHeader("\uC131\uC7A5")
        selectionController.addStateListener(selectionStateListener)
        current.growthAreaList.layoutManager = LinearLayoutManager(requireContext())
        growthAreaAdapter = GrowthAreaAdapter(
            items = InMemoryDataStore.getGrowthAreas(),
            onClick = ::handleAreaClick,
            onLongClick = ::handleAreaLongClick,
            isSelected = ::isAreaSelected
        )
        current.growthAreaList.adapter = growthAreaAdapter
        attachAreaDragHelper()
        InlineQuickAdd.bind(current.growthQuickAdd.root, onDismiss = { showFab() }) { title ->
            growthAreaAdapter.addItem(InMemoryDataStore.addGrowthArea(title))
        }
        current.addButton.setOnClickListener {
            if (!selectionController.isInSelectionMode) {
                openQuickInput("\uC0C8 \uC131\uC7A5 \uBD84\uC57C \uC785\uB825")
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                closeQuickInput()
                isEnabled = false
            }
        }.also { callback ->
            current.growthQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
    }

    private fun handleAreaClick(area: GrowthArea) {
        if (selectionController.isInSelectionMode) {
            selectionController.toggle(area.id, areaSelectionScope())
        } else {
            findNavController().navigate(
                R.id.action_growthFragment_to_growthAreaDetailFragment,
                bundleOf("growthAreaId" to area.id)
            )
        }
    }

    private fun handleAreaLongClick(area: GrowthArea) {
        prepareAreaSelectionHandlers()
        selectionController.enter(areaSelectionScope(), listOf(area.id))
    }

    private fun prepareAreaSelectionHandlers() {
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(::confirmDeleteSelectedAreas)
            setSelectionMoveHandler(null)
        }
    }

    private fun isAreaSelected(area: GrowthArea): Boolean {
        return selectionController.isInSelectionMode && area.id in selectionController.selectedItemIds
    }

    private fun confirmDeleteSelectedAreas() {
        val ids = selectionController.selectedItemIds.toList()
        if (ids.isEmpty()) return
        AlertDialog.Builder(requireContext())
            .setMessage("\uC120\uD0DD\uD55C \uD56D\uBAA9\uC744 \uC0AD\uC81C\uD560\uAE4C\uC694?")
            .setNegativeButton("\uCDE8\uC18C", null)
            .setPositiveButton("\uC0AD\uC81C") { _, _ ->
                ids.forEach { InMemoryDataStore.deleteGrowthArea(it) }
                selectionController.exit()
                growthAreaAdapter.submitList(InMemoryDataStore.getGrowthAreas())
            }
            .show()
    }

    private fun openQuickInput(hint: String) {
        val current = binding ?: return
        current.addButton.visibility = View.GONE
        InlineQuickAdd.show(current.growthQuickAdd.root, hint)
    }

    private fun closeQuickInput() {
        val current = binding ?: return
        InlineQuickAdd.hide(current.growthQuickAdd.root)
        showFab()
    }

    private fun showFab() {
        binding?.addButton?.visibility = View.VISIBLE
    }

    private fun attachAreaDragHelper() {
        val current = binding ?: return
        if (areaDragSelectionHelper != null) return
        areaDragSelectionHelper = RecyclerDragSelectionHelper(
            recyclerView = current.growthAreaList,
            selectionController = selectionController,
            getItemId = { position -> growthAreaAdapter.getItem(position)?.id },
            getItemScope = { position -> growthAreaAdapter.getItem(position)?.let { areaSelectionScope() } },
            isItemSelected = { position -> growthAreaAdapter.getItem(position)?.let(::isAreaSelected) == true },
            onDragStarted = { prepareAreaSelectionHandlers() },
            onSelectionChanged = { growthAreaAdapter.notifyDataSetChanged() }
        ).also { current.growthAreaList.addOnItemTouchListener(it) }
    }

    override fun onDestroyView() {
        selectionController.removeStateListener(selectionStateListener)
        areaDragSelectionHelper?.let { binding?.growthAreaList?.removeOnItemTouchListener(it) }
        areaDragSelectionHelper = null
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(null)
            setSelectionMoveHandler(null)
        }
        binding = null
        super.onDestroyView()
    }

    private val selectionController
        get() = (requireActivity() as MainActivity).selectionController

    private fun areaSelectionScope(): String = "growth-areas"
}
