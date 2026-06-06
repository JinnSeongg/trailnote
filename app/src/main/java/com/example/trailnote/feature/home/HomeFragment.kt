package com.example.trailnote.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private lateinit var todayWorkAdapter: HomeTaskAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.root.setHeader("홈")
        current.dateText.text = InMemoryDataStore.getTodayLabel()

        current.statOne.iconText.text = "☆"
        current.statOne.valueText.text = InMemoryDataStore.getTodayWorks().count { it.isDone }.toString()
        current.statOne.titleText.text = "연속 기록"
        current.statTwo.iconText.text = "□"
        current.statTwo.valueText.text = InMemoryDataStore.getProjects().count { it.status == "진행중" }.toString()
        current.statTwo.titleText.text = "진행 프로젝트"
        current.statThree.iconText.text = "✓"
        current.statThree.valueText.text = InMemoryDataStore.getGoalSettings().randomTodayGoalCount.toString()
        current.statThree.titleText.text = "오늘 목표"

        todayWorkAdapter = HomeTaskAdapter(InMemoryDataStore.getTodayWorks())
        current.todayWorkList.layoutManager = LinearLayoutManager(requireContext())
        current.todayWorkList.adapter = todayWorkAdapter
        current.fixedGoalList.layoutManager = LinearLayoutManager(requireContext())
        current.fixedGoalList.adapter = HomeGoalAdapter(InMemoryDataStore.getFixedGoals())
        current.todayGoalList.layoutManager = LinearLayoutManager(requireContext())
        current.todayGoalList.adapter = HomeGoalAdapter(InMemoryDataStore.getTodayGoals())

        InlineQuickAdd.bind(current.todayWorkQuickAdd.root) { title ->
            todayWorkAdapter.addItem(InMemoryDataStore.addTodayWork(title))
        }
        current.todayWorkAddButton.setOnClickListener {
            InlineQuickAdd.show(current.todayWorkQuickAdd.root, "새 할 일 입력")
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                InlineQuickAdd.hide(current.todayWorkQuickAdd.root)
                isEnabled = false
            }
        }.also { callback ->
            current.todayWorkQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
