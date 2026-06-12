package com.example.trailnote.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.R
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.FragmentAchievementListBinding
import com.example.trailnote.domain.model.Achievement
import kotlinx.coroutines.launch

class AchievementListFragment : Fragment() {
    private var binding: FragmentAchievementListBinding? = null
    private lateinit var adapter: AchievementListAdapter
    private var selectedGrade: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentAchievementListBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.root.setHeader(
            title = "\uC5C5\uC801",
            showBack = true,
            onBack = { findNavController().popBackStack() }
        )

        adapter = AchievementListAdapter { achievement ->
            AchievementDetailDialogFragment.newInstance(achievement.id)
                .show(childFragmentManager, "achievement_detail")
        }
        current.achievementRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        current.achievementRecyclerView.adapter = adapter
        childFragmentManager.setFragmentResultListener(AchievementDetailDialogFragment.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            renderAchievements()
        }

        bindGradeChips()
        renderAchievements()
    }

    private fun bindGradeChips() {
        val current = binding ?: return
        val chips = listOf(
            current.chipAll to null,
            current.chipCommon to "\uC77C\uBC18",
            current.chipRare to "\uD76C\uADC0",
            current.chipUnique to "\uC720\uB2C8\uD06C"
        )
        chips.forEach { (chip, grade) ->
            chip.setOnClickListener {
                selectedGrade = grade
                updateChipStyles(chips)
                renderAchievements()
            }
        }
        updateChipStyles(chips)
    }

    private fun updateChipStyles(chips: List<Pair<TextView, String?>>) {
        chips.forEach { (chip, grade) ->
            val isSelected = grade == selectedGrade
            chip.background = ContextCompat.getDrawable(
                requireContext(),
                if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
            )
            chip.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isSelected) R.color.white else R.color.trail_text_secondary
                )
            )
            chip.setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }
    }

    private fun renderAchievements() {
        viewLifecycleOwner.lifecycleScope.launch {
            val unlockResult = repository.refreshAchievementUnlocks()
            val achievements = repository.getUnlockedAchievementsForDisplay(unlockResult.achievements)
                .filter { achievement -> selectedGrade == null || achievement.grade == selectedGrade }
            val representativeAchievementId = repository.getProfileSummaryFromDb().featuredAchievementId
            adapter.submitList(toSectionItems(achievements, representativeAchievementId))
        }
    }

    private fun toSectionItems(
        achievements: List<Achievement>,
        representativeAchievementId: String?
    ): List<AchievementListItem> {
        return achievements
            .groupBy { it.category }
            .flatMap { (category, categoryAchievements) ->
                listOf(AchievementListItem.SectionHeader(category)) +
                    categoryAchievements.map { achievement ->
                        AchievementListItem.AchievementCard(
                            achievement = achievement,
                            isRepresentative = achievement.id == representativeAchievementId
                        )
                    }
            }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private val repository
        get() = RepositoryProvider.getRepository(requireContext())
}
