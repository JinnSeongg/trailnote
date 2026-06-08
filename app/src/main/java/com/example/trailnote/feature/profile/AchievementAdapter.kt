package com.example.trailnote.feature.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.R
import com.example.trailnote.databinding.ItemAchievementBinding
import com.example.trailnote.domain.model.Achievement

class AchievementAdapter(
    private val items: List<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Achievement) {
            val context = binding.root.context
            val (accentColorRes, chipBackgroundRes, chipTextColorRes) = when (item.grade) {
                "\uD76C\uADC0" -> Triple(
                    R.color.achievement_grade_rare_text,
                    R.drawable.bg_achievement_grade_rare,
                    R.color.achievement_grade_rare_text
                )
                "\uC720\uB2C8\uD06C" -> Triple(
                    R.color.achievement_grade_unique_text,
                    R.drawable.bg_achievement_grade_unique,
                    R.color.achievement_grade_unique_text
                )
                else -> Triple(
                    R.color.achievement_grade_common_text,
                    R.drawable.bg_achievement_grade_common,
                    R.color.achievement_grade_common_text
                )
            }

            binding.iconText.text = item.iconText
            binding.iconText.setTextColor(ContextCompat.getColor(context, accentColorRes))
            binding.titleText.text = item.title
            binding.gradeText.text = item.grade
            binding.gradeText.setBackgroundResource(chipBackgroundRes)
            binding.gradeText.setTextColor(ContextCompat.getColor(context, chipTextColorRes))
        }
    }
}
