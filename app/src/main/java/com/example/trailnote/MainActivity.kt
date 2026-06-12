package com.example.trailnote

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.trailnote.core.util.AchievementUnlockFeedback
import com.example.trailnote.core.selection.SelectionController
import com.example.trailnote.core.selection.SelectionState
import com.example.trailnote.data.local.db.DatabaseSeeder
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.ActivityMainBinding
import com.example.trailnote.feature.home.HomeFragment
import com.example.trailnote.feature.home.TodayGoalCountDialogFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var selectionController: SelectionController
        private set
    private var selectionDeleteHandler: (() -> Unit)? = null
    private var selectionMoveHandler: (() -> Unit)? = null
    private var isUpdatingBottomNavigation = false
    private var lastBackPressedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            DatabaseSeeder.seedIfNeeded(applicationContext)
            initializeUi()
            val repository = RepositoryProvider.getRepository(applicationContext)
            repository.recordAppVisitIfNeeded()
            AchievementUnlockFeedback.show(this@MainActivity, repository.refreshAchievementUnlocks().newlyUnlockedAchievements)
        }
    }

    private fun initializeUi() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectionController = SelectionController().also {
            it.addStateListener(::renderSelectionState)
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
        setupDrawerMenu()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                binding.drawerLayout.closeDrawer(Gravity.LEFT)
            }
        }.also { callback ->
            binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerOpened(drawerView: android.view.View) {
                    callback.isEnabled = true
                }

                override fun onDrawerClosed(drawerView: android.view.View) {
                    callback.isEnabled = false
                }
            })
        })
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    binding.drawerLayout.closeDrawer(Gravity.LEFT)
                    return
                }
                if (navController.currentDestination?.id.isRootTabDestination()) {
                    handleRootTabBackPressed()
                } else if (!navController.popBackStack()) {
                    handleRootTabBackPressed()
                }
            }
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            selectionController.exit()
            lastBackPressedAt = 0L
            val tabId = destination.id.toRootTabId() ?: return@addOnDestinationChangedListener
            if (binding.bottomNavigation.selectedItemId != tabId) {
                isUpdatingBottomNavigation = true
                binding.bottomNavigation.selectedItemId = tabId
                isUpdatingBottomNavigation = false
            }
        }
    }

    private fun handleRootTabBackPressed() {
        val now = System.currentTimeMillis()
        if (now - lastBackPressedAt <= BACK_PRESS_EXIT_INTERVAL_MS) {
            finish()
        } else {
            lastBackPressedAt = now
            Toast.makeText(this, "\uC571\uC744 \uC885\uB8CC\uD558\uB824\uBA74 \uD55C \uBC88 \uB354 \uB204\uB974\uC138\uC694", Toast.LENGTH_SHORT).show()
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

    fun openDrawer() {
        binding.drawerLayout.openDrawer(Gravity.LEFT)
    }

    private fun setupDrawerMenu() {
        binding.root.findViewById<TextView>(R.id.todayGoalCountMenu).setOnClickListener {
            binding.drawerLayout.closeDrawer(Gravity.LEFT)
            showTodayGoalCountDialog()
        }
        bindDrawerItem(R.id.themeMenu, "\uD14C\uB9C8 \uBCC0\uACBD \uC900\uBE44 \uC911")
        bindDrawerItem(R.id.notificationMenu, "\uC54C\uB9BC \uC900\uBE44 \uC911")
        bindDrawerItem(R.id.dataMenu, "\uB370\uC774\uD130 \uC900\uBE44 \uC911")
        bindDrawerItem(R.id.helpMenu, "\uB3C4\uC6C0\uB9D0 \uC900\uBE44 \uC911")
        bindDrawerItem(R.id.aboutMenu, "\uC815\uBCF4 \uC900\uBE44 \uC911")
    }

    private fun bindDrawerItem(viewId: Int, message: String) {
        binding.root.findViewById<TextView>(viewId).setOnClickListener {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(Gravity.LEFT)
        }
    }

    private fun showTodayGoalCountDialog() {
        lifecycleScope.launch {
            val repository = RepositoryProvider.getRepository(applicationContext)
            val currentCount = repository.getHomeGoalSettings().randomTodayGoalCount
            TodayGoalCountDialogFragment.newInstance(currentCount).apply {
                onSave = { count ->
                    lifecycleScope.launch {
                        repository.updateRandomTodayGoalCount(count)
                        currentHomeFragment()?.refreshHomeGoals()
                    }
                }
            }.show(supportFragmentManager, "today_goal_count")
        }
    }

    private fun currentHomeFragment(): HomeFragment? {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        return navHost?.childFragmentManager?.primaryNavigationFragment as? HomeFragment
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

    private fun Int?.isRootTabDestination(): Boolean {
        return when (this) {
            R.id.homeFragment,
            R.id.projectFragment,
            R.id.logFragment,
            R.id.growthFragment,
            R.id.profileFragment -> true
            else -> false
        }
    }

    companion object {
        private const val BACK_PRESS_EXIT_INTERVAL_MS = 2_000L
    }
}
