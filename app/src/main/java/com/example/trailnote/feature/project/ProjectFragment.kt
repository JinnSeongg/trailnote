package com.example.trailnote.feature.project

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
import com.example.trailnote.databinding.FragmentProjectBinding

class ProjectFragment : Fragment() {
    private var binding: FragmentProjectBinding? = null
    private lateinit var projectAdapter: ProjectAdapter
    private var currentFilter = ProjectFilter.All

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentProjectBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.root.setHeader("프로젝트")
        current.projectList.layoutManager = LinearLayoutManager(requireContext())
        projectAdapter = ProjectAdapter(InMemoryDataStore.getProjects()) { project ->
            findNavController().navigate(
                R.id.action_projectFragment_to_projectDetailFragment,
                bundleOf("projectId" to project.id)
            )
        }
        current.projectList.adapter = projectAdapter
        val chipAll = current.root.findViewById<TextView>(R.id.chipAll)
        val chipActive = current.root.findViewById<TextView>(R.id.chipActive)
        val chipDone = current.root.findViewById<TextView>(R.id.chipDone)
        chipAll.setOnClickListener {
            currentFilter = ProjectFilter.All
            renderProjects()
        }
        chipActive.setOnClickListener {
            currentFilter = ProjectFilter.Active
            renderProjects()
        }
        chipDone.setOnClickListener {
            currentFilter = ProjectFilter.Done
            renderProjects()
        }
        InlineQuickAdd.bind(current.projectQuickAdd.root, onDismiss = { showFab() }) { title ->
            InMemoryDataStore.addProject(title)
            renderProjects()
        }
        current.addButton.setOnClickListener {
            openQuickInput("새 프로젝트 입력")
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                closeQuickInput()
                isEnabled = false
            }
        }.also { callback ->
            current.projectQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
                callback.isEnabled = InlineQuickAdd.isVisible(quickAdd)
            }
        })
        renderProjects()
    }

    private fun openQuickInput(hint: String) {
        val current = binding ?: return
        current.addButton.visibility = View.GONE
        InlineQuickAdd.show(current.projectQuickAdd.root, hint)
    }

    private fun closeQuickInput() {
        val current = binding ?: return
        InlineQuickAdd.hide(current.projectQuickAdd.root)
        showFab()
    }

    private fun showFab() {
        binding?.addButton?.visibility = View.VISIBLE
    }

    private fun renderProjects() {
        val current = binding ?: return
        val visibleProjects = InMemoryDataStore.getProjects().filter { project ->
            when (currentFilter) {
                ProjectFilter.All -> true
                ProjectFilter.Active -> project.status.isActiveStatus()
                ProjectFilter.Done -> project.status.isDoneStatus()
            }
        }
        projectAdapter.submitList(visibleProjects)
        current.root.findViewById<TextView>(R.id.chipAll).setSelectedStyle(currentFilter == ProjectFilter.All)
        current.root.findViewById<TextView>(R.id.chipActive).setSelectedStyle(currentFilter == ProjectFilter.Active)
        current.root.findViewById<TextView>(R.id.chipDone).setSelectedStyle(currentFilter == ProjectFilter.Done)
    }

    private fun TextView.setSelectedStyle(isSelected: Boolean) {
        setTextColor(resources.getColor(if (isSelected) R.color.white else R.color.trail_text_secondary, null))
        setBackgroundResource(if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        setTypeface(typeface, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
    }

    private fun String.isActiveStatus(): Boolean {
        return trim().lowercase() in setOf("진행중", "in_progress", "active", "ongoing")
    }

    private fun String.isDoneStatus(): Boolean {
        return trim().lowercase() in setOf("완료", "done", "completed")
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private enum class ProjectFilter {
        All,
        Active,
        Done
    }
}
