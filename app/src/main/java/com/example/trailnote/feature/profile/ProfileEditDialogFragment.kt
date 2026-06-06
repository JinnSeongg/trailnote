package com.example.trailnote.feature.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.DialogEditProfileBinding

class ProfileEditDialogFragment : DialogFragment() {
    private var binding: DialogEditProfileBinding? = null
    private var avatarVariant: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = DialogEditProfileBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        val summary = InMemoryDataStore.getProfileSummary()
        avatarVariant = summary.avatarVariant

        current.nameEditText.setText(summary.name)
        renderAvatar()
        current.avatarPreviewText.setOnClickListener {
            avatarVariant = if (avatarVariant == 0) 1 else 0
            renderAvatar()
        }

        current.cancelButton.setOnClickListener {
            dismiss()
        }
        current.saveButton.setOnClickListener {
            InMemoryDataStore.updateProfile(
                name = current.nameEditText.text.toString().trim().ifBlank { summary.name },
                featuredAchievementId = summary.featuredAchievementId,
                avatarVariant = avatarVariant
            )
            setFragmentResult(REQUEST_KEY, Bundle.EMPTY)
            dismiss()
        }
    }

    private fun renderAvatar() {
        binding?.avatarPreviewText?.text = if (avatarVariant == 0) "\u25CF" else "\u25C6"
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
        const val REQUEST_KEY = "profile_edit_result"
        private const val DIALOG_WIDTH_RATIO = 0.9f
    }
}
