package com.example.trailnote.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailnote.R
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.sample.SampleProfile
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
        val summary = SampleProfile.summary
        val current = binding ?: return
        current.root.setHeader("프로필", action = "···")

        current.userNameText.text = summary.name
        current.levelBadgeText.text = "Lv.${summary.level}"
        current.expBadgeText.text = "대단한 열정 ${summary.exp}%"

        current.profileStatOne.iconText.text = "☆"
        current.profileStatOne.titleText.text = "완료한 할 일"
        current.profileStatOne.valueText.text = summary.completedTaskCount.toString()

        current.profileStatTwo.iconText.text = "□"
        current.profileStatTwo.titleText.text = "완료한 프로젝트"
        current.profileStatTwo.valueText.text = summary.activeProjectCount.toString()

        current.profileStatThree.iconText.text = "▣"
        current.profileStatThree.titleText.text = "작성한 기록"
        current.profileStatThree.valueText.text = summary.logCount.toString()

        current.profileStatFour.iconText.text = "⌁"
        current.profileStatFour.titleText.text = "완료한 루틴"
        current.profileStatFour.valueText.text = summary.completedRoutineCount.toString()

        bindActivityStat(current.activityStatOne, "할일 작성", "23개", "▲ 18%", true)
        bindActivityStat(current.activityStatTwo, "기록 작성", "16개", "▲ 42%", true)
        bindActivityStat(current.activityStatThree, "루틴 완료", "42개", "▲ 26%", true)
        bindActivityStat(current.activityStatFour, "방문 일수", "16일", "▼ 12%", false)

        current.activityChart.setValues(SampleProfile.activityRecords.map { it.value })
        current.achievementList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        current.achievementList.adapter = AchievementAdapter(SampleProfile.achievements)
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
