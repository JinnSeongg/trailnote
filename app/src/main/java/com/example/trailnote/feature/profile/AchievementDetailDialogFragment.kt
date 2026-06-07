package com.example.trailnote.feature.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.AchievementDetailDialogBinding

class AchievementDetailDialogFragment : DialogFragment() {
    private var binding: AchievementDetailDialogBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = AchievementDetailDialogBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val achievementId = requireArguments().getString(ARG_ACHIEVEMENT_ID).orEmpty()
        val achievement = InMemoryDataStore.getAchievements().firstOrNull { it.id == achievementId }
        val current = binding ?: return
        if (achievement == null) {
            dismiss()
            return
        }

        current.iconText.text = achievement.iconText
        current.titleText.text = achievement.title
        current.gradeText.text = achievement.grade
        current.unlockedAtText.text = achievement.unlockedAt ?: "\uBBF8\uD68D\uB4DD"
        renderRepresentativeButton(achievement.id)
        current.representativeButton.setOnClickListener {
            if (InMemoryDataStore.getProfileSummary().featuredAchievementId != achievement.id) {
                InMemoryDataStore.updateFeaturedAchievement(achievement.id)
                renderRepresentativeButton(achievement.id)
                setFragmentResult(REQUEST_KEY, Bundle.EMPTY)
                Toast.makeText(requireContext(), "\uB300\uD45C \uC5C5\uC801\uC73C\uB85C \uC124\uC815\uD588\uC5B4\uC694", Toast.LENGTH_SHORT).show()
            }
        }
        current.closeButton.setOnClickListener { dismiss() }
    }

    private fun renderRepresentativeButton(achievementId: String) {
        val current = binding ?: return
        val isRepresentative = InMemoryDataStore.getProfileSummary().featuredAchievementId == achievementId
        current.representativeButton.text = if (isRepresentative) {
            "\uB300\uD45C \uC5C5\uC801"
        } else {
            "\uB300\uD45C \uC5C5\uC801\uC73C\uB85C \uC124\uC815"
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * DIALOG_WIDTH_RATIO).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_KEY = "achievement_detail_result"
        private const val ARG_ACHIEVEMENT_ID = "achievementId"
        private const val DIALOG_WIDTH_RATIO = 0.86f

        fun newInstance(achievementId: String): AchievementDetailDialogFragment {
            return AchievementDetailDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACHIEVEMENT_ID, achievementId)
                }
            }
        }
    }
}
