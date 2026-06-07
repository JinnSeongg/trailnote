package com.example.trailnote.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.core.util.DeleteConfirmDialogHelper
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.local.db.DatabaseSeeder
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.FragmentHomeBinding
import com.example.trailnote.databinding.ItemStatCardBinding
import com.example.trailnote.domain.model.Routine
import com.example.trailnote.domain.model.Task
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private lateinit var todayWorkAdapter: HomeTaskAdapter
    private lateinit var fixedGoalAdapter: HomeGoalAdapter
    private lateinit var todayGoalAdapter: HomeGoalAdapter
    private var todayWorks: List<Task> = emptyList()
    private var fixedRoutines: List<Routine> = emptyList()
    private var todayRoutines: List<Routine> = emptyList()
    private val todayDate: String
        get() = LocalDate.now().toString()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.root.setHeader("홈")
        current.dateText.text = todayDate

        current.statOne.iconText.text = "✓"
        current.statOne.titleText.text = "오늘 할 일"
        current.statTwo.iconText.text = "■"
        current.statTwo.titleText.text = "고정 목표"
        current.statThree.iconText.text = "●"
        current.statThree.titleText.text = "오늘 목표"

        todayWorkAdapter = HomeTaskAdapter(
            tasks = emptyList(),
            onDoneChange = { task, checked ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.updateHomeTaskDoneState(task.id, checked)
                    reloadHome()
                }
            },
            onLongClick = { task ->
                DeleteConfirmDialogHelper.showSingle(requireContext(), task.title) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        repository.deleteHomeTask(task.id)
                        reloadHome()
                    }
                }
            }
        )
        current.todayWorkList.layoutManager = LinearLayoutManager(requireContext())
        current.todayWorkList.adapter = todayWorkAdapter

        fixedGoalAdapter = HomeGoalAdapter(emptyList()) { task, checked ->
            viewLifecycleOwner.lifecycleScope.launch {
                repository.updateRoutineDoneState(task.id, checked)
                reloadHome()
            }
        }
        current.fixedGoalList.layoutManager = LinearLayoutManager(requireContext())
        current.fixedGoalList.adapter = fixedGoalAdapter

        todayGoalAdapter = HomeGoalAdapter(emptyList()) { task, checked ->
            viewLifecycleOwner.lifecycleScope.launch {
                repository.updateRoutineDoneState(task.id, checked)
                reloadHome()
            }
        }
        current.todayGoalList.layoutManager = LinearLayoutManager(requireContext())
        current.todayGoalList.adapter = todayGoalAdapter

        InlineQuickAdd.bind(current.todayWorkQuickAdd.root) { title ->
            viewLifecycleOwner.lifecycleScope.launch {
                repository.addHomeTask(title, todayDate)
                reloadHome()
            }
        }
        current.todayWorkAddButton.setOnClickListener {
            InlineQuickAdd.show(current.todayWorkQuickAdd.root, "오늘 할 일 입력")
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
        reloadHome()
    }

    override fun onResume() {
        super.onResume()
        if (binding != null && ::todayWorkAdapter.isInitialized) {
            reloadHome()
        }
    }

    private fun reloadHome() {
        viewLifecycleOwner.lifecycleScope.launch {
            DatabaseSeeder.seedIfNeeded(requireContext().applicationContext)
            val date = todayDate
            repository.runDailyResetIfNeeded(date)
            todayWorks = repository.getTodayHomeTasks(date)
            fixedRoutines = repository.getFixedRoutinesForHome()
            todayRoutines = repository.getTodayRandomRoutines(date)
            renderHome()
        }
    }

    private fun renderHome() {
        val current = binding ?: return
        val fixedGoals = fixedRoutines.map { it.toTask() }
        val todayGoals = todayRoutines.map { it.toTask() }
        todayWorkAdapter.submitList(todayWorks)
        fixedGoalAdapter.submitList(fixedGoals)
        todayGoalAdapter.submitList(todayGoals)
        current.dateText.text = todayDate
        renderStat(current.statOne, todayWorks.count { it.isDone }, todayWorks.size)
        renderStat(current.statTwo, fixedGoals.count { it.isDone }, fixedGoals.size)
        renderStat(current.statThree, todayGoals.count { it.isDone }, todayGoals.size)
    }

    private fun renderStat(stat: ItemStatCardBinding, doneCount: Int, totalCount: Int) {
        val percent = if (totalCount == 0) 0 else doneCount * 100 / totalCount
        stat.valueText.text = "$percent%"
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun Routine.toTask(): Task = Task(id, title, isDoneToday)

    private val repository
        get() = RepositoryProvider.getRepository(requireContext())
}
