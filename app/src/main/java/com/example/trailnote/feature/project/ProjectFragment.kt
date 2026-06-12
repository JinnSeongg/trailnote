package com.example.trailnote.feature.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.example.trailnote.core.util.ProgressCalculator
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.local.db.DatabaseSeeder
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.FragmentProjectBinding
import com.example.trailnote.domain.model.Milestone
import com.example.trailnote.domain.model.Project
import com.example.trailnote.domain.model.ProjectCategory
import com.example.trailnote.domain.model.ShortTask
import kotlinx.coroutines.launch

class ProjectFragment : Fragment() {
    private var binding: FragmentProjectBinding? = null
    private lateinit var projectSectionAdapter: ProjectSectionAdapter
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var backCallback: OnBackPressedCallback
    private var projectCategories: List<ProjectCategory> = emptyList()
    private var selectedCategory: ProjectCategory? = null
    private var currentFilter = ProjectFilter.All
    private var quickAddMode: QuickAddMode? = null
    private var projectDragSelectionHelper: RecyclerDragSelectionHelper? = null
    private var projects: List<Project> = emptyList()
    private var milestones: List<Milestone> = emptyList()
    private var shortTasks: List<ShortTask> = emptyList()
    private val selectionStateListener: (SelectionState) -> Unit = {
        projectAdapter.notifyDataSetChanged()
        projectSectionAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentProjectBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        projectSectionAdapter = ProjectSectionAdapter(
            onProjectClick = ::handleProjectClick,
            onProjectLongClick = ::handleProjectLongClick,
            isProjectSelected = ::isProjectSelected,
            selectionController = selectionController,
            onProjectDragStarted = ::prepareProjectSelectionHandlers,
            onCategoryClick = { section ->
                if (!selectionController.isInSelectionMode) {
                    selectedCategory = section.toProjectCategory()
                    renderProjects()
                }
            },
            onCategoryAddClick = { section ->
                if (!selectionController.isInSelectionMode) {
                    openQuickInput(QuickAddMode.Project(section.categoryId), "\uC0C8 \uD504\uB85C\uC81D\uD2B8 \uC785\uB825")
                }
            },
            onCategoryLongClick = ::showCategoryMenu,
            projectProgressProvider = ::projectProgress
        )
        projectAdapter = ProjectAdapter(
            emptyList(),
            ::handleProjectClick,
            ::handleProjectLongClick,
            ::isProjectSelected,
            ::projectProgress
        )
        selectionController.addStateListener(selectionStateListener)
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
            viewLifecycleOwner.lifecycleScope.launch {
                when (val mode = quickAddMode) {
                    QuickAddMode.Category -> {
                        repository.addProjectCategory(title)
                        reloadProjects()
                    }
                    is QuickAddMode.Project -> {
                        repository.addProject(title, mode.categoryId)
                        reloadProjects()
                    }
                    is QuickAddMode.EditCategory -> {
                        repository.updateProjectCategoryTitle(mode.categoryId, title)
                        reloadProjects()
                    }
                    null -> return@launch
                }
            }
        }
        current.addButton.setOnClickListener {
            val category = selectedCategory
            if (category == null) {
                openQuickInput(QuickAddMode.Category, "\uC0C8 \uC8FC\uC81C \uC785\uB825")
            } else {
                openQuickInput(QuickAddMode.Project(category.id), "\uC0C8 \uD504\uB85C\uC81D\uD2B8 \uC785\uB825")
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
        reloadProjects()
    }

    override fun onResume() {
        super.onResume()
        if (binding != null && ::projectAdapter.isInitialized) {
            reloadProjects()
        }
    }

    private fun openProject(project: Project) {
        findNavController().navigate(
            R.id.action_projectFragment_to_projectDetailFragment,
            bundleOf("projectId" to project.id)
        )
    }

    private fun handleProjectClick(project: Project) {
        if (selectionController.isInSelectionMode) {
            selectionController.toggle(project.id, project.selectionScope())
        } else {
            openProject(project)
        }
    }

    private fun handleProjectLongClick(project: Project) {
        prepareProjectSelectionHandlers(project)
    }

    private fun prepareProjectSelectionHandlers(project: Project) {
        (requireActivity() as MainActivity).apply {
            setSelectionDeleteHandler(::confirmDeleteSelectedProjects)
            setSelectionMoveHandler(::showMoveProjectDialog)
        }
    }

    private fun isProjectSelected(project: Project): Boolean {
        return selectionController.isInSelectionMode && project.id in selectionController.selectedItemIds
    }

    private fun confirmDeleteSelectedProjects() {
        val ids = selectionController.selectedItemIds.toList()
        if (ids.isEmpty()) return
        DeleteConfirmDialogHelper.showMultiple(requireContext(), ids.size) {
            viewLifecycleOwner.lifecycleScope.launch {
                ids.forEach { repository.deleteProject(it) }
                selectionController.exit()
                reloadProjects()
            }
        }
    }

    private fun showMoveProjectDialog() {
        MoveTargetDialogFragment(
            title = "\uC774\uB3D9\uD560 \uC8FC\uC81C",
            addHint = "\uC0C8 \uC8FC\uC81C \uC785\uB825",
            loadTargets = { projectCategories.map { MoveTarget(it.id.toString(), it.title) } },
            onAddTarget = { title ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.addProjectCategory(title)
                    reloadProjects()
                }
            },
            onTargetSelected = { target ->
                val ids = selectionController.selectedItemIds.toList()
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.moveProjectsToCategory(ids, target.id.toLongOrNull())
                    selectionController.exit()
                    reloadProjects()
                }
            }
        ).show(childFragmentManager, "move_projects")
    }

    private fun openQuickInput(mode: QuickAddMode, hint: String) {
        val current = binding ?: return
        quickAddMode = mode
        current.addButton.visibility = View.GONE
        InlineQuickAdd.show(current.projectQuickAdd.root, hint)
        updateBackCallbackState()
    }

    private fun openQuickInput(mode: QuickAddMode, hint: String, initialText: String) {
        val current = binding ?: return
        quickAddMode = mode
        current.addButton.visibility = View.GONE
        InlineQuickAdd.show(current.projectQuickAdd.root, hint, initialText)
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
            },
            showMore = selectedCategory != null,
            onMore = {
                selectedCategory?.let { category ->
                    showProjectCategoryMenu(
                        ProjectSection(category.id, category.title, emptyList()),
                        current.root.findViewById(R.id.headerMore)
                    )
                }
            }
        )
        val category = selectedCategory
        val filteredProjects = projects.filter { project ->
            project.matchesFilter(currentFilter) && (category == null || project.categoryId == category.id)
        }

        if (category == null) {
            current.selectedCategoryText.visibility = View.GONE
            current.projectList.adapter = projectSectionAdapter
            removeFlatProjectDragHelper()
            val projectsByCategoryId = filteredProjects
                .sortedByDescending { it.updatedSortKey() }
                .groupBy { it.categoryId }
            val categorySections = projectCategories.sortedByStableCreationOrder().mapNotNull { projectCategory ->
                val projects = projectsByCategoryId[projectCategory.id].orEmpty()
                if (projects.isNotEmpty() || currentFilter == ProjectFilter.All) {
                    ProjectSection(projectCategory.id, projectCategory.title, projects)
                } else {
                    null
                }
            }
            val uncategorizedProjects = projectsByCategoryId[null].orEmpty()
            val sections = if (uncategorizedProjects.isNotEmpty()) {
                categorySections + ProjectSection(null, "\uBBF8\uBD84\uB958", uncategorizedProjects)
            } else {
                categorySections
            }
            projectSectionAdapter.submitList(sections)
        } else {
            current.selectedCategoryText.visibility = View.VISIBLE
            current.selectedCategoryText.text = category.title
            current.projectList.adapter = projectAdapter
            projectAdapter.submitList(filteredProjects.sortedByDescending { it.updatedSortKey() })
            attachFlatProjectDragHelper()
        }

        current.root.findViewById<TextView>(R.id.chipAll).setSelectedStyle(currentFilter == ProjectFilter.All)
        current.root.findViewById<TextView>(R.id.chipActive).setSelectedStyle(currentFilter == ProjectFilter.Active)
        current.root.findViewById<TextView>(R.id.chipDone).setSelectedStyle(currentFilter == ProjectFilter.Done)
        updateBackCallbackState()
    }

    private fun reloadProjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            DatabaseSeeder.seedIfNeeded(requireContext().applicationContext)
            projectCategories = repository.getProjectCategories()
                .distinctBy { it.title.trim() }
                .sortedByStableCreationOrder()
            projects = repository.getProjects().sortedByDescending { it.updatedSortKey() }
            milestones = repository.getMilestones()
            shortTasks = repository.getShortTasks()
            selectedCategory = selectedCategory?.let { selected ->
                projectCategories.firstOrNull { it.id == selected.id }
            }
            renderProjects()
        }
    }

    private fun showCategoryMenu(section: ProjectSection, anchor: View) {
        showProjectCategoryMenu(section, anchor)
    }

    private fun showProjectCategoryMenu(section: ProjectSection, anchor: View) {
        val categoryId = section.categoryId ?: return
        if (selectionController.isInSelectionMode) return
        PopupMenuHelper.show(requireContext(), anchor, listOf("\uC218\uC815", "\uC0AD\uC81C")) { title ->
            when (title) {
                "\uC218\uC815" -> {
                    openQuickInput(QuickAddMode.EditCategory(categoryId), "\uC8FC\uC81C \uC785\uB825", section.category)
                    true
                }
                "\uC0AD\uC81C" -> {
                    DeleteConfirmDialogHelper.showSingle(requireContext(), section.category) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            repository.deleteProjectCategory(categoryId)
                            if (selectedCategory?.id == categoryId) selectedCategory = null
                            reloadProjects()
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun projectProgress(project: Project): Int {
        val milestoneProgresses = milestones
            .filter { it.projectId == project.id }
            .map { milestone ->
                ProgressCalculator.milestoneProgress(shortTasks.filter { it.milestoneId == milestone.id })
            }
        return ProgressCalculator.projectProgress(milestoneProgresses)
    }

    private fun updateBackCallbackState() {
        val current = binding ?: return
        if (::backCallback.isInitialized) {
            backCallback.isEnabled = InlineQuickAdd.isVisible(current.projectQuickAdd.root) || selectedCategory != null
        }
    }

    private fun attachFlatProjectDragHelper() {
        val current = binding ?: return
        if (projectDragSelectionHelper != null) return
        projectDragSelectionHelper = RecyclerDragSelectionHelper(
            recyclerView = current.projectList,
            selectionController = selectionController,
            getItemId = { position -> projectAdapter.getItem(position)?.id },
            getItemScope = { position -> projectAdapter.getItem(position)?.selectionScope() },
            isItemSelected = { position -> projectAdapter.getItem(position)?.let(::isProjectSelected) == true },
            onDragStarted = { position ->
                projectAdapter.getItem(position)?.let(::prepareProjectSelectionHandlers)
            },
            onSelectionChanged = { projectAdapter.notifyDataSetChanged() }
        ).also { current.projectList.addOnItemTouchListener(it) }
    }

    private fun removeFlatProjectDragHelper() {
        val current = binding ?: return
        projectDragSelectionHelper?.let { current.projectList.removeOnItemTouchListener(it) }
        projectDragSelectionHelper = null
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
        selectionController.removeStateListener(selectionStateListener)
        removeFlatProjectDragHelper()
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

    private fun Project.selectionScope(): String = "project-category:${categoryId ?: "none"}"

    private fun Project.updatedSortKey(): String = updatedAt

    private fun List<ProjectCategory>.sortedByStableCreationOrder(): List<ProjectCategory> {
        return sortedWith(
            compareBy<ProjectCategory> { it.orderIndex }
                .thenBy { it.createdAt }
                .thenBy { it.id }
        )
    }

    private fun ProjectSection.toProjectCategory(): ProjectCategory? {
        val id = categoryId ?: return null
        return ProjectCategory(id, category, 0)
    }

    private enum class ProjectFilter {
        All,
        Active,
        Done
    }

    private sealed class QuickAddMode {
        object Category : QuickAddMode()
        data class Project(val categoryId: Long?) : QuickAddMode()
        data class EditCategory(val categoryId: Long) : QuickAddMode()
    }
}
