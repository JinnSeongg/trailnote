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
import com.example.trailnote.domain.model.Project

class ProjectFragment : Fragment() {
    private var binding: FragmentProjectBinding? = null
    private lateinit var projectSectionAdapter: ProjectSectionAdapter
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var backCallback: OnBackPressedCallback
    private val knownCategories = mutableListOf<String>()
    private var selectedCategory: String? = null
    private var currentFilter = ProjectFilter.All
    private var quickAddMode: QuickAddMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentProjectBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        knownCategories.clear()
        knownCategories.addAll(InMemoryDataStore.getProjects().map { it.category }.distinct())
        projectSectionAdapter = ProjectSectionAdapter(
            onProjectClick = ::openProject,
            onCategoryClick = { category ->
                selectedCategory = category
                renderProjects()
            },
            onCategoryAddClick = { category ->
                openQuickInput(QuickAddMode.Project(category), "\uC0C8 \uD504\uB85C\uC81D\uD2B8 \uC785\uB825")
            }
        )
        projectAdapter = ProjectAdapter(emptyList(), ::openProject)
        current.projectList.layoutManager = LinearLayoutManager(requireContext())

        current.root.findViewById<TextView>(R.id.chipAll).setOnClickListener {
            currentFilter = ProjectFilter.All
            renderProjects()
        }
        current.root.findViewById<TextView>(R.id.chipActive).setOnClickListener {
            currentFilter = ProjectFilter.Active
            renderProjects()
        }
        current.root.findViewById<TextView>(R.id.chipDone).setOnClickListener {
            currentFilter = ProjectFilter.Done
            renderProjects()
        }

        InlineQuickAdd.bind(current.projectQuickAdd.root, onDismiss = {
            quickAddMode = null
            showFab()
            updateBackCallbackState()
        }) { title ->
            when (val mode = quickAddMode) {
                QuickAddMode.Category -> addCategory(title)
                is QuickAddMode.Project -> {
                    addCategory(mode.category)
                    InMemoryDataStore.addProject(title, mode.category)
                }
                null -> return@bind
            }
            renderProjects()
        }
        current.addButton.setOnClickListener {
            val category = selectedCategory
            if (category == null) {
                openQuickInput(QuickAddMode.Category, "\uC0C8 \uC8FC\uC81C \uC785\uB825")
            } else {
                openQuickInput(QuickAddMode.Project(category), "\uC0C8 \uD504\uB85C\uC81D\uD2B8 \uC785\uB825")
            }
        }

        backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (InlineQuickAdd.isVisible(current.projectQuickAdd.root)) {
                    closeQuickInput()
                } else {
                    selectedCategory = null
                    renderProjects()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        current.projectQuickAdd.root.addOnLayoutChangeListener { quickAdd, _, _, _, _, _, _, _, _ ->
            backCallback.isEnabled = InlineQuickAdd.isVisible(quickAdd) || selectedCategory != null
        }
        renderProjects()
    }

    private fun openProject(project: Project) {
        findNavController().navigate(
            R.id.action_projectFragment_to_projectDetailFragment,
            bundleOf("projectId" to project.id)
        )
    }

    private fun addCategory(category: String) {
        val normalized = category.trim()
        if (normalized.isNotEmpty() && knownCategories.none { it == normalized }) {
            knownCategories.add(normalized)
        }
    }

    private fun openQuickInput(mode: QuickAddMode, hint: String) {
        val current = binding ?: return
        quickAddMode = mode
        current.addButton.visibility = View.GONE
        InlineQuickAdd.show(current.projectQuickAdd.root, hint)
        updateBackCallbackState()
    }

    private fun closeQuickInput() {
        val current = binding ?: return
        quickAddMode = null
        InlineQuickAdd.hide(current.projectQuickAdd.root)
        showFab()
        updateBackCallbackState()
    }

    private fun showFab() {
        binding?.addButton?.visibility = View.VISIBLE
    }

    private fun renderProjects() {
        val current = binding ?: return
        current.root.setHeader(
            "\uD504\uB85C\uC81D\uD2B8",
            showBack = selectedCategory != null,
            onBack = {
                selectedCategory = null
                renderProjects()
            }
        )
        val category = selectedCategory
        val filteredProjects = InMemoryDataStore.getProjects().filter { project ->
            project.matchesFilter(currentFilter) && (category == null || project.category == category)
        }

        if (category == null) {
            current.selectedCategoryText.visibility = View.GONE
            current.projectList.adapter = projectSectionAdapter
            val sections = knownCategories.mapNotNull { knownCategory ->
                val projects = filteredProjects.filter { it.category == knownCategory }
                if (projects.isNotEmpty() || currentFilter == ProjectFilter.All) {
                    ProjectSection(knownCategory, projects)
                } else {
                    null
                }
            }
            projectSectionAdapter.submitList(sections)
        } else {
            current.selectedCategoryText.visibility = View.VISIBLE
            current.selectedCategoryText.text = category
            current.projectList.adapter = projectAdapter
            projectAdapter.submitList(filteredProjects)
        }

        current.root.findViewById<TextView>(R.id.chipAll).setSelectedStyle(currentFilter == ProjectFilter.All)
        current.root.findViewById<TextView>(R.id.chipActive).setSelectedStyle(currentFilter == ProjectFilter.Active)
        current.root.findViewById<TextView>(R.id.chipDone).setSelectedStyle(currentFilter == ProjectFilter.Done)
        updateBackCallbackState()
    }

    private fun updateBackCallbackState() {
        val current = binding ?: return
        if (::backCallback.isInitialized) {
            backCallback.isEnabled = InlineQuickAdd.isVisible(current.projectQuickAdd.root) || selectedCategory != null
        }
    }

    private fun Project.matchesFilter(filter: ProjectFilter): Boolean {
        return when (filter) {
            ProjectFilter.All -> true
            ProjectFilter.Active -> !status.isDoneStatus()
            ProjectFilter.Done -> status.isDoneStatus()
        }
    }

    private fun TextView.setSelectedStyle(isSelected: Boolean) {
        setTextColor(resources.getColor(if (isSelected) R.color.white else R.color.trail_text_secondary, null))
        setBackgroundResource(if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        setTypeface(typeface, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
    }

    private fun String.isDoneStatus(): Boolean {
        return trim().lowercase() in setOf("\uC644\uB8CC", "?꾨즺", "done", "completed")
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

    private sealed class QuickAddMode {
        object Category : QuickAddMode()
        data class Project(val category: String) : QuickAddMode()
    }
}
