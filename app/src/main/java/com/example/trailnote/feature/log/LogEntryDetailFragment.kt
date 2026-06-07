package com.example.trailnote.feature.log

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.trailnote.R
import com.example.trailnote.core.util.DeleteConfirmDialogHelper
import com.example.trailnote.core.util.PopupMenuHelper
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.repository.RepositoryProvider
import com.example.trailnote.databinding.FragmentLogEntryDetailBinding
import com.example.trailnote.domain.model.LogEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogEntryDetailFragment : Fragment() {
    private var binding: FragmentLogEntryDetailBinding? = null
    private var entryId: String = ""
    private var entry: LogEntry? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentLogEntryDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        entryId = requireArguments().getString("entryId").orEmpty()
        val current = binding ?: return

        current.root.setHeader(
            "\uAE30\uB85D",
            action = "\u00B7\u00B7\u00B7",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = { showEntryMenu() }
        )
        reloadEntry()
    }

    private val editWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            persistCurrentText()
        }

        override fun afterTextChanged(s: Editable?) = Unit
    }

    private fun persistCurrentText() {
        val current = binding ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val updatedEntry = repository.updateLogEntry(
                entryId = entryId,
                title = current.entryTitleEditText.text.toString(),
                content = current.contentEditText.text.toString(),
                updatedAt = currentDate()
            ) ?: return@launch
            entry = updatedEntry
            updateModifiedDateText(updatedEntry)
        }
    }

    private fun showEntryMenu() {
        val current = binding ?: return
        val anchor = current.root.findViewById<View>(R.id.headerAction)
        PopupMenuHelper.show(requireContext(), anchor, listOf("\uC0AD\uC81C")) {
            confirmDeleteEntry()
            true
        }
    }

    private fun confirmDeleteEntry() {
        val entryTitle = entry?.title
        DeleteConfirmDialogHelper.showSingle(requireContext(), entryTitle) {
            viewLifecycleOwner.lifecycleScope.launch {
                repository.deleteLogEntry(entryId)
                findNavController().navigateUp()
            }
        }
    }

    private fun reloadEntry() {
        viewLifecycleOwner.lifecycleScope.launch {
            val loadedEntry = repository.getLogEntryById(entryId)
            if (loadedEntry == null) {
                findNavController().navigateUp()
                return@launch
            }
            entry = loadedEntry
            renderEntry(loadedEntry)
        }
    }

    private fun renderEntry(entry: LogEntry) {
        val current = binding ?: return
        current.entryTitleEditText.removeTextChangedListener(editWatcher)
        current.contentEditText.removeTextChangedListener(editWatcher)
        current.entryTitleEditText.setText(entry.title)
        current.contentEditText.setText(entry.content)
        updateModifiedDateText(entry)
        current.entryTitleEditText.addTextChangedListener(editWatcher)
        current.contentEditText.addTextChangedListener(editWatcher)
    }

    private fun updateModifiedDateText(entry: LogEntry) {
        val current = binding ?: return
        val sourceDate = entry.updatedAt.ifBlank { entry.createdAt }
        current.modifiedDateText.text = "\uCD5C\uC885 \uC218\uC815\uC77C ${formatDisplayDate(sourceDate)}"
    }

    private fun formatDisplayDate(sourceDate: String): String {
        val normalized = sourceDate.trim()
        if (normalized.isEmpty()) return currentDate()
        return when {
            DOT_DATE_REGEX.matches(normalized) -> normalized
            DASH_DATE_REGEX.matches(normalized) -> normalized.replace('-', '.')
            ISO_DATE_TIME_REGEX.matches(normalized) -> normalized.take(10).replace('-', '.')
            else -> currentDate()
        }
    }

    private fun currentDate(): String = DATE_FORMAT.format(Date())

    private val repository
        get() = RepositoryProvider.getRepository(requireContext())

    override fun onDestroyView() {
        val current = binding
        if (current != null) {
            current.entryTitleEditText.removeTextChangedListener(editWatcher)
            current.contentEditText.removeTextChangedListener(editWatcher)
        }
        binding = null
        super.onDestroyView()
    }

    private companion object {
        val DATE_FORMAT = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        val DOT_DATE_REGEX = Regex("""\d{4}\.\d{2}\.\d{2}""")
        val DASH_DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")
        val ISO_DATE_TIME_REGEX = Regex("""\d{4}-\d{2}-\d{2}T.*""")
    }
}
