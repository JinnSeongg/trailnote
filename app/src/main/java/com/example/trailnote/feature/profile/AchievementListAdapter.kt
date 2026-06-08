package com.example.trailnote.feature.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.databinding.ItemAchievementFullBinding
import com.example.trailnote.databinding.ItemAchievementSectionHeaderBinding
import com.example.trailnote.domain.model.Achievement

class AchievementListAdapter(
    private val onAchievementClick: (Achievement) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<AchievementListItem>()

    fun submitList(newItems: List<AchievementListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is AchievementListItem.SectionHeader -> VIEW_TYPE_HEADER
            is AchievementListItem.AchievementCard -> VIEW_TYPE_ACHIEVEMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                ItemAchievementSectionHeaderBinding.inflate(inflater, parent, false)
            )
            else -> AchievementViewHolder(
                ItemAchievementFullBinding.inflate(inflater, parent, false),
                onAchievementClick
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is AchievementListItem.SectionHeader -> (holder as HeaderViewHolder).bind(item.category)
            is AchievementListItem.AchievementCard -> (holder as AchievementViewHolder).bind(
                item.achievement,
                item.isRepresentative
            )
        }
    }

    override fun getItemCount(): Int = items.size

    private class HeaderViewHolder(
        private val binding: ItemAchievementSectionHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String) {
            binding.categoryText.text = category
        }
    }

    private class AchievementViewHolder(
        private val binding: ItemAchievementFullBinding,
        private val onAchievementClick: (Achievement) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(achievement: Achievement, isRepresentative: Boolean) {
            binding.iconText.text = achievement.iconText
            binding.titleText.text = achievement.title
            binding.descriptionText.text = achievement.description
            bindGrade(achievement.grade)
            binding.representativeText.visibility = if (isRepresentative) View.VISIBLE else View.GONE
            binding.root.setOnClickListener { onAchievementClick(achievement) }
        }

        private fun bindGrade(grade: String) {
            val context = binding.root.context
            val (backgroundRes, textColorRes) = when (grade) {
                "\uD76C\uADC0" -> R.drawable.bg_achievement_grade_rare to R.color.achievement_grade_rare_text
                "\uC720\uB2C8\uD06C" -> R.drawable.bg_achievement_grade_unique to R.color.achievement_grade_unique_text
                else -> R.drawable.bg_achievement_grade_common to R.color.achievement_grade_common_text
            }
            binding.gradeText.text = grade
            binding.gradeText.setBackgroundResource(backgroundRes)
            binding.gradeText.setTextColor(ContextCompat.getColor(context, textColorRes))
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ACHIEVEMENT = 1
    }
}

sealed class AchievementListItem {
    data class SectionHeader(val category: String) : AchievementListItem()
    data class AchievementCard(val achievement: Achievement, val isRepresentative: Boolean) : AchievementListItem()
}
