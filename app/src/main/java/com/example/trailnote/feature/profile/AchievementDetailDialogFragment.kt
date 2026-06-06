package com.example.trailnote.feature.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
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
        current.closeButton.setOnClickListener { dismiss() }
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
