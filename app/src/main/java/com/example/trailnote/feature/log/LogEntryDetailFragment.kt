package com.example.trailnote.feature.log

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.trailnote.R
import com.example.trailnote.core.util.setHeader
import com.example.trailnote.data.InMemoryDataStore
import com.example.trailnote.databinding.FragmentLogEntryDetailBinding
import com.example.trailnote.domain.model.LogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogEntryDetailFragment : Fragment() {
    private var binding: FragmentLogEntryDetailBinding? = null
    private var entryId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentLogEntryDetailBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        entryId = requireArguments().getString("entryId").orEmpty()
        val entry = InMemoryDataStore.getLogEntry(entryId) ?: return
        val current = binding ?: return

        current.root.setHeader(
            "\uAE30\uB85D",
            action = "\u00B7\u00B7\u00B7",
            showBack = true,
            onBack = { findNavController().popBackStack() },
            onAction = { showEntryMenu() }
        )
        current.entryTitleEditText.setText(entry.title)
        current.contentEditText.setText(entry.content)
        updateModifiedDateText(entry)

        current.entryTitleEditText.addTextChangedListener(editWatcher)
        current.contentEditText.addTextChangedListener(editWatcher)
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
        val updatedEntry = InMemoryDataStore.updateLogEntry(
            entryId = entryId,
            title = current.entryTitleEditText.text.toString(),
            content = current.contentEditText.text.toString(),
            updatedAt = currentDate()
        ) ?: return
        updateModifiedDateText(updatedEntry)
    }

    private fun showEntryMenu() {
        val current = binding ?: return
        val anchor = current.root.findViewById<View>(R.id.headerAction)
        PopupMenu(requireContext(), anchor).apply {
            menu.add("\uC0AD\uC81C")
            setOnMenuItemClickListener {
                confirmDeleteEntry()
                true
            }
        }.show()
    }

    private fun confirmDeleteEntry() {
        AlertDialog.Builder(requireContext())
            .setMessage("\uC0AD\uC81C\uD560\uAE4C\uC694?")
            .setNegativeButton("\uCDE8\uC18C", null)
            .setPositiveButton("\uC0AD\uC81C") { _, _ ->
                InMemoryDataStore.deleteLogEntry(entryId)
                findNavController().navigateUp()
            }
            .show()
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
            else -> currentDate()
        }
    }

    private fun currentDate(): String = DATE_FORMAT.format(Date())

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
    }
}
