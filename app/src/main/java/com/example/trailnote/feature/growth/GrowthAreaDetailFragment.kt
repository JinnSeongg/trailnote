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
import com.example.trailnote.databinding.FragmentGrowthAreaDetailBinding

class GrowthAreaDetailFragment : Fragment() {
    private var binding: FragmentGrowthAreaDetailBinding? = null
    private lateinit var growthTopicAdapter: GrowthTopicAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentGrowthAreaDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val areaId = requireArguments().getString("growthAreaId").orEmpty()
        val area = InMemoryDataStore.getGrowthArea(areaId) ?: return
        val topics = InMemoryDataStore.getGrowthTopicsByArea(areaId)
        val current = binding ?: return
        current.root.setHeader(area.title, action = "···", showBack = true, onBack = { findNavController().popBackStack() })
        current.descriptionText.text = area.description

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
            growthTopicAdapter.addItem(InMemoryDataStore.addGrowthTopic(areaId, title))
            current.emptyText.visibility = View.GONE
            current.growthTopicList.visibility = View.VISIBLE
        }
        current.growthTopicAddButton.setOnClickListener {
            current.growthTopicFabButton.visibility = View.GONE
            InlineQuickAdd.show(current.growthTopicQuickAdd.root, "새 세부 분야 입력")
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

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
