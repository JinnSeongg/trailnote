package com.example.trailnote.feature.growth

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
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentGrowthBinding

class GrowthFragment : Fragment() {
    private var binding: FragmentGrowthBinding? = null
    private lateinit var growthAreaAdapter: GrowthAreaAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentGrowthBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.root.setHeader("성장")
        current.growthAreaList.layoutManager = LinearLayoutManager(requireContext())
        growthAreaAdapter = GrowthAreaAdapter(InMemoryDataStore.getGrowthAreas()) { area ->
            findNavController().navigate(
                R.id.action_growthFragment_to_growthAreaDetailFragment,
                bundleOf("growthAreaId" to area.id)
            )
        }
        current.growthAreaList.adapter = growthAreaAdapter
        InlineQuickAdd.bind(current.growthQuickAdd.root, onDismiss = { showFab() }) { title ->
            growthAreaAdapter.addItem(InMemoryDataStore.addGrowthArea(title))
        }
        current.addButton.setOnClickListener {
            openQuickInput("새 성장 분야 입력")
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

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
