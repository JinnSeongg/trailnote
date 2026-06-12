package com.example.trailnote.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.R
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.FragmentProfileBinding
import com.example.trailnote.databinding.ItemProfileActivityStatBinding
import com.example.trailnote.domain.model.Achievement
import com.example.trailnote.domain.model.ActivityStatsSummary
import com.example.trailnote.domain.model.ProfileSummary
import com.example.trailnote.domain.model.StatsPeriod
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private var selectedPeriod: StatsPeriod = StatsPeriod.MONTHLY

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentProfileBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.root.setHeader("\uD504\uB85C\uD544")
        childFragmentManager.setFragmentResultListener(ProfileEditDialogFragment.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            loadProfile()
        }

        current.achievementList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        current.editProfileButton.setOnClickListener {
            ProfileEditDialogFragment().show(childFragmentManager, "profile_edit")
        }
        current.achievementAllButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_achievementListFragment)
        }
        setupStatsPeriodControls()
        loadProfile()
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            val summary = repository.getProfileSummaryFromDb()
            val unlockResult = repository.refreshAchievementUnlocks()
            val achievements = unlockResult.achievements
            val previewAchievements = repository.getUnlockedAchievementsForDisplay(achievements, limit = 4)
            val activityTrend = repository.getRecentActivityTrend()
            bindProfileHeader(summary, achievements)
            bindProfileStats(summary)
            binding?.activityChart?.setValues(activityTrend.map { it.averageCompletionRate })
            bindAchievementPreview(previewAchievements)
            loadActivityStats(selectedPeriod)
        }
    }

    private fun bindAchievementPreview(previewAchievements: List<Achievement>) {
        val current = binding ?: return
        current.achievementEmptyText.visibility = if (previewAchievements.isEmpty()) View.VISIBLE else View.GONE
        current.achievementList.visibility = if (previewAchievements.isEmpty()) View.GONE else View.VISIBLE
        current.achievementList.adapter = AchievementAdapter(previewAchievements)
    }

    private fun setupStatsPeriodControls() {
        val current = binding ?: return
        current.statsPeriodButton.setOnClickListener {
            selectStatsPeriod(selectedPeriod.next())
        }
        updateStatsPeriodButton(selectedPeriod)
    }

    private fun selectStatsPeriod(period: StatsPeriod) {
        if (selectedPeriod == period) return
        selectedPeriod = period
        updateStatsPeriodButton(period)
        loadActivityStats(period)
    }

    private fun loadActivityStats(period: StatsPeriod) {
        viewLifecycleOwner.lifecycleScope.launch {
            val stats = repository.getActivityStats(period)
            bindActivityStats(stats)
        }
    }

    private fun updateStatsPeriodButton(period: StatsPeriod) {
        val current = binding ?: return
        current.statsPeriodButton.text = period.displayLabel()
    }

    private fun bindProfileHeader(summary: ProfileSummary, achievements: List<Achievement>) {
        val current = binding ?: return
        val featuredAchievement = achievements
            .firstOrNull { it.id == summary.featuredAchievementId }
            ?: achievements.firstOrNull { it.isUnlocked }
            ?: achievements.firstOrNull()

        current.avatarImage.setImageResource(R.drawable.ic_default_profile)
        current.userNameText.text = summary.name
        current.levelBadgeText.text = "Lv.${summary.level}"
        current.expBadgeText.text = featuredAchievement?.title ?: "\uB300\uD45C \uC5C5\uC801 \uC5C6\uC74C"
    }

    private fun bindProfileStats(summary: ProfileSummary) {
        val current = binding ?: return

        current.profileStatOne.iconText.text = "\u2713"
        current.profileStatOne.titleText.text = "\uC644\uB8CC\uD55C \uD560 \uC77C"
        current.profileStatOne.valueText.text = summary.completedTaskCount.toString()

        current.profileStatTwo.iconText.text = "\u25A0"
        current.profileStatTwo.titleText.text = "\uC644\uB8CC\uD55C \uD504\uB85C\uC81D\uD2B8"
        current.profileStatTwo.valueText.text = summary.activeProjectCount.toString()

        current.profileStatThree.iconText.text = "\u25CF"
        current.profileStatThree.titleText.text = "\uC791\uC131\uD55C \uAE30\uB85D"
        current.profileStatThree.valueText.text = summary.logCount.toString()

        current.profileStatFour.iconText.text = "\u25C6"
        current.profileStatFour.titleText.text = "\uC644\uB8CC\uD55C \uB8E8\uD2F4"
        current.profileStatFour.valueText.text = summary.completedRoutineCount.toString()
    }

    private fun bindActivityStats(stats: ActivityStatsSummary) {
        val current = binding ?: return
        bindActivityStat(current.activityStatOne, "\uD560 \uC77C \uC791\uC131", "${stats.todoCreatedCount}\uAC1C", "0%", true)
        bindActivityStat(current.activityStatTwo, "\uAE30\uB85D \uC791\uC131", "${stats.logCreatedCount}\uAC1C", "0%", true)
        bindActivityStat(current.activityStatThree, "\uB8E8\uD2F4 \uC644\uB8CC", "${stats.normalRoutineCompletedCount}\uAC1C", "0%", true)
        bindActivityStat(current.activityStatFour, "\uACE0\uC815 \uB8E8\uD2F4", "${stats.fixedRoutineAverageCompletionRate}%", "0%", true)
    }

    private fun bindActivityStat(
        binding: ItemProfileActivityStatBinding,
        title: String,
        value: String,
        change: String,
        isUp: Boolean
    ) {
        binding.titleText.text = title
        binding.valueText.text = value
        binding.changeText.text = change
        binding.changeText.setTextColor(
            androidx.core.content.ContextCompat.getColor(
                requireContext(),
                if (isUp) R.color.profile_success else R.color.profile_down
            )
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private val repository
        get() = RepositoryProvider.getRepository(requireContext())

    private fun StatsPeriod.next(): StatsPeriod {
        return when (this) {
            StatsPeriod.MONTHLY -> StatsPeriod.DAILY
            StatsPeriod.DAILY -> StatsPeriod.WEEKLY
            StatsPeriod.WEEKLY -> StatsPeriod.MONTHLY
        }
    }

    private fun StatsPeriod.displayLabel(): String {
        return when (this) {
            StatsPeriod.DAILY -> "\uC624\uB298"
            StatsPeriod.WEEKLY -> "\uC774\uBC88 \uC8FC"
            StatsPeriod.MONTHLY -> "\uC774\uBC88 \uB2EC"
        }
    }
}
