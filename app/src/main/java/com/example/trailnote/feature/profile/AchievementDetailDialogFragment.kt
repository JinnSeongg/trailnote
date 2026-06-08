package com.example.trailnote.feature.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.AchievementDetailDialogBinding
import kotlinx.coroutines.launch

class AchievementDetailDialogFragment : DialogFragment() {
    private var binding: AchievementDetailDialogBinding? = null
    private var achievementId: String = ""

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
        val current = binding ?: return
        achievementId = requireArguments().getString(ARG_ACHIEVEMENT_ID).orEmpty()

        viewLifecycleOwner.lifecycleScope.launch {
            val achievement = repository.getAchievementById(achievementId)
            if (achievement == null) {
                dismiss()
                return@launch
            }

            current.iconText.text = achievement.iconText
            current.titleText.text = achievement.title
            current.gradeText.text = achievement.grade
            current.descriptionText.text = achievement.description
            current.unlockedAtText.text = achievement.unlockedAt ?: "\uBBF8\uD68D\uB4DD"
            renderRepresentativeButton()
            current.representativeButton.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    if (repository.getProfileSummaryFromDb().featuredAchievementId != achievement.id) {
                        repository.updateRepresentativeAchievement(achievement.id)
                        renderRepresentativeButton()
                        setFragmentResult(REQUEST_KEY, Bundle.EMPTY)
                        Toast.makeText(requireContext(), "\uB300\uD45C \uC5C5\uC801\uC73C\uB85C \uC124\uC815\uD588\uC5B4\uC694", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            current.closeButton.setOnClickListener { dismiss() }
        }
    }

    private fun renderRepresentativeButton() {
        val current = binding ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val isRepresentative = repository.getProfileSummaryFromDb().featuredAchievementId == achievementId
            current.representativeButton.text = if (isRepresentative) {
                "\uB300\uD45C \uC5C5\uC801"
            } else {
                "\uB300\uD45C \uC5C5\uC801\uC73C\uB85C \uC124\uC815"
            }
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

    private val repository
        get() = RepositoryProvider.getRepository(requireContext())
}
