package com.example.trailnote.feature.growth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentGrowthTopicDetailBinding

class GrowthTopicDetailFragment : Fragment() {
    private var binding: FragmentGrowthTopicDetailBinding? = null
    private lateinit var routineAdapter: RoutineAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentGrowthTopicDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val topicId = requireArguments().getString("topicId").orEmpty()
        val topic = InMemoryDataStore.getGrowthTopic(topicId) ?: return
        val routines = InMemoryDataStore.getRoutinesByTopic(topicId)
        val current = binding ?: return
        current.root.setHeader(topic.title, action = "···", showBack = true, onBack = { findNavController().popBackStack() })
        current.descriptionText.text = topic.description
        current.routineList.layoutManager = LinearLayoutManager(requireContext())
        routineAdapter = RoutineAdapter(routines)
        current.routineList.adapter = routineAdapter
        current.emptyText.visibility = if (routines.isEmpty()) View.VISIBLE else View.GONE
        current.routineList.visibility = if (routines.isEmpty()) View.GONE else View.VISIBLE

        InlineQuickAdd.bind(current.routineQuickAdd.root, onDismiss = { showRoutineFab() }) { title ->
            routineAdapter.addItem(InMemoryDataStore.addRoutine(topicId, title))
            current.emptyText.visibility = View.GONE
            current.routineList.visibility = View.VISIBLE
        }
        current.routineFabButton.setOnClickListener {
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

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
