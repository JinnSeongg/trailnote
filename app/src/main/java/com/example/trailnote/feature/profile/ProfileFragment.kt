package com.example.trailnote.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.R
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentProfileBinding
import com.example.trailnote.databinding.ItemProfileActivityStatBinding

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentProfileBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.root.setHeader("\uD504\uB85C\uD544", action = "\u00B7\u00B7\u00B7")
        childFragmentManager.setFragmentResultListener(ProfileEditDialogFragment.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            bindProfileHeader()
        }

        bindProfileHeader()
        bindProfileStats()
        bindActivityStats()

        current.activityChart.setValues(InMemoryDataStore.getActivityRecords().map { it.value })
        current.achievementList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        current.achievementList.adapter = AchievementAdapter(InMemoryDataStore.getAchievements())
        current.editProfileButton.setOnClickListener {
            ProfileEditDialogFragment().show(childFragmentManager, "profile_edit")
        }
        current.achievementAllButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_achievementListFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        bindProfileHeader()
    }

    private fun bindProfileHeader() {
        val current = binding ?: return
        val summary = InMemoryDataStore.getProfileSummary()
        val featuredAchievement = InMemoryDataStore.getAchievements()
            .firstOrNull { it.id == summary.featuredAchievementId }
            ?: InMemoryDataStore.getAchievements().firstOrNull { it.isUnlocked }
            ?: InMemoryDataStore.getAchievements().firstOrNull()

        current.avatarText.text = if (summary.avatarVariant == 0) "\u25CF" else "\u25C6"
        current.userNameText.text = summary.name
        current.levelBadgeText.text = "Lv.${summary.level}"
        current.expBadgeText.text = featuredAchievement?.title ?: "\uB300\uD45C \uC5C5\uC801 \uC5C6\uC74C"
    }

    private fun bindProfileStats() {
        val current = binding ?: return
        val summary = InMemoryDataStore.getProfileSummary()

        current.profileStatOne.iconText.text = "\u2713"
        current.profileStatOne.titleText.text = "\uC644\uB8CC\uD55C \uD560 \uC77C"
        current.profileStatOne.valueText.text = summary.completedTaskCount.toString()

        current.profileStatTwo.iconText.text = "\u25A0"
        current.profileStatTwo.titleText.text = "\uC9C4\uD589 \uD504\uB85C\uC81D\uD2B8"
        current.profileStatTwo.valueText.text = summary.activeProjectCount.toString()

        current.profileStatThree.iconText.text = "\u25CF"
        current.profileStatThree.titleText.text = "\uC791\uC131\uD55C \uAE30\uB85D"
        current.profileStatThree.valueText.text = summary.logCount.toString()

        current.profileStatFour.iconText.text = "\u25C6"
        current.profileStatFour.titleText.text = "\uC644\uB8CC\uD55C \uB8E8\uD2F4"
        current.profileStatFour.valueText.text = summary.completedRoutineCount.toString()
    }

    private fun bindActivityStats() {
        val current = binding ?: return
        bindActivityStat(current.activityStatOne, "\uC77C\uC77C \uC791\uC131", "23\uAC1C", "\u25B2 18%", true)
        bindActivityStat(current.activityStatTwo, "\uAE30\uB85D \uC791\uC131", "16\uAC1C", "\u25B2 42%", true)
        bindActivityStat(current.activityStatThree, "\uB8E8\uD2F4 \uC644\uB8CC", "42\uAC1C", "\u25B2 26%", true)
        bindActivityStat(current.activityStatFour, "\uBC29\uBB38 \uC77C\uC218", "16\uC77C", "\u25BC 12%", false)
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
            ContextCompat.getColor(
                requireContext(),
                if (isUp) R.color.profile_success else R.color.profile_down
            )
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
