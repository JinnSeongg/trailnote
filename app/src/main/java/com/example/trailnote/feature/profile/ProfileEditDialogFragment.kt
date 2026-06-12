package com.example.trailnote.feature.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.example.trailnote.R
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.DialogEditProfileBinding
import kotlinx.coroutines.launch

class ProfileEditDialogFragment : DialogFragment() {
    private var binding: DialogEditProfileBinding? = null

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

        current.avatarPreviewImage.setImageResource(R.drawable.ic_default_profile)
        viewLifecycleOwner.lifecycleScope.launch {
            val summary = repository.getProfileSummaryFromDb()
            binding?.nameEditText?.setText(summary.name)
        }

        current.cancelButton.setOnClickListener {
            dismiss()
        }
        current.saveButton.setOnClickListener {
            val name = current.nameEditText.text.toString()
            viewLifecycleOwner.lifecycleScope.launch {
                repository.updateUserProfileName(name)
                setFragmentResult(REQUEST_KEY, Bundle.EMPTY)
                dismiss()
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
        const val REQUEST_KEY = "profile_edit_result"
        private const val DIALOG_WIDTH_RATIO = 0.9f
    }

    private val repository
        get() = RepositoryProvider.getRepository(requireContext())
}
