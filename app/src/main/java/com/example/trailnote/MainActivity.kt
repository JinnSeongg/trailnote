package com.example.trailnote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.trailnote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isUpdatingBottomNavigation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val tabId = destination.id.toRootTabId() ?: return@addOnDestinationChangedListener
            if (binding.bottomNavigation.selectedItemId != tabId) {
                isUpdatingBottomNavigation = true
                binding.bottomNavigation.selectedItemId = tabId
                isUpdatingBottomNavigation = false
            }
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
