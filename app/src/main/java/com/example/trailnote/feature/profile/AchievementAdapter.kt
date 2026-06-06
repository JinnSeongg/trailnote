package com.example.trailnote.feature.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
            binding.iconText.text = item.iconText
            binding.titleText.text = item.title
        }
    }
}
