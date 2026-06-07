package com.example.trailnote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.trailnote.core.selection.SelectionController
import com.example.trailnote.core.selection.SelectionState
import com.example.trailnote.data.local.db.DatabaseSeeder
import com.example.trailnote.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var selectionController: SelectionController
        private set
    private var selectionDeleteHandler: (() -> Unit)? = null
    private var selectionMoveHandler: (() -> Unit)? = null
    private var isUpdatingBottomNavigation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectionController = SelectionController().also {
            it.addStateListener(::renderSelectionState)
        }
        lifecycleScope.launch {
            DatabaseSeeder.seedIfNeeded(applicationContext)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (isUpdatingBottomNavigation) {
                true
            } else {
                navigateToRootTab(navController, item.itemId)
                true
            }
        }
        binding.bottomNavigation.setOnItemReselectedListener { item ->
            navigateToRootTab(navController, item.itemId)
        }
        binding.selectionActionBar.cancelSelectionButton.setOnClickListener {
            selectionController.exit()
        }
        binding.selectionActionBar.moveSelectionButton.setOnClickListener {
            selectionMoveHandler?.invoke()
        }
        binding.selectionActionBar.deleteSelectionButton.setOnClickListener {
            selectionDeleteHandler?.invoke()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            selectionController.exit()
            val tabId = destination.id.toRootTabId() ?: return@addOnDestinationChangedListener
            if (binding.bottomNavigation.selectedItemId != tabId) {
                isUpdatingBottomNavigation = true
                binding.bottomNavigation.selectedItemId = tabId
                isUpdatingBottomNavigation = false
            }
        }
    }

    fun enterSelectionMode(selectionScope: String?, selectedItemIds: Collection<String> = emptyList()) {
        selectionController.enter(selectionScope, selectedItemIds)
    }

    fun setSelectionDeleteHandler(handler: (() -> Unit)?) {
        selectionDeleteHandler = handler
    }

    fun setSelectionMoveHandler(handler: (() -> Unit)?) {
        selectionMoveHandler = handler
        binding.selectionActionBar.moveSelectionButton.isVisible = handler != null && selectionController.isInSelectionMode
    }

    private fun renderSelectionState(state: SelectionState) {
        binding.bottomNavigation.isVisible = !state.isInSelectionMode
        binding.selectionActionBar.root.isVisible = state.isInSelectionMode
        binding.selectionActionBar.moveSelectionButton.isVisible = state.isInSelectionMode && selectionMoveHandler != null
        if (!state.isInSelectionMode) {
            selectionDeleteHandler = null
            selectionMoveHandler = null
        }
    }

    private fun navigateToRootTab(navController: NavController, rootDestinationId: Int) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(navController.graph.startDestinationId, false, false)
            .setLaunchSingleTop(true)
            .setRestoreState(false)
            .build()
        navController.navigate(rootDestinationId, null, navOptions)
    }

    private fun Int.toRootTabId(): Int? {
        return when (this) {
            R.id.homeFragment -> R.id.homeFragment
            R.id.projectFragment,
            R.id.projectDetailFragment,
            R.id.milestoneDetailFragment -> R.id.projectFragment
            R.id.logFragment,
            R.id.logTopicDetailFragment,
            R.id.logEntryDetailFragment -> R.id.logFragment
            R.id.growthFragment,
            R.id.growthAreaDetailFragment,
            R.id.growthTopicDetailFragment -> R.id.growthFragment
            R.id.profileFragment,
            R.id.achievementListFragment -> R.id.profileFragment
            else -> null
        }
    }
}
