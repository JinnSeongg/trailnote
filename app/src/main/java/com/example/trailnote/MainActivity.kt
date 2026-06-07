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

    fun openDrawer() {
        binding.drawerLayout.openDrawer(Gravity.LEFT)
    }

    private fun setupDrawerMenu() {
        bindDrawerItem(R.id.todayGoalCountMenu, "오늘 목표 수 설정 준비 중")
        bindDrawerItem(R.id.themeMenu, "테마 변경 준비 중")
        bindDrawerItem(R.id.notificationMenu, "알림 준비 중")
        bindDrawerItem(R.id.dataMenu, "데이터 준비 중")
        bindDrawerItem(R.id.helpMenu, "도움말 준비 중")
        bindDrawerItem(R.id.aboutMenu, "정보 준비 중")
    }

    private fun bindDrawerItem(viewId: Int, message: String) {
        binding.root.findViewById<TextView>(viewId).setOnClickListener {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(Gravity.LEFT)
        }
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
