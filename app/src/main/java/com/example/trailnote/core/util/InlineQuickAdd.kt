package com.example.trailnote.core.util

import android.content.Context
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.example.trailnote.R

object InlineQuickAdd {
    fun isVisible(container: View): Boolean = container.visibility == View.VISIBLE

    fun show(container: View, hint: String) {
        val editText = container.findViewById<EditText>(R.id.quickAddEditText)
        container.visibility = View.VISIBLE
        editText.hint = hint
        editText.setText("")
        editText.requestFocus()
        editText.post {
            val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hide(container: View) {
        val editText = container.findViewById<EditText>(R.id.quickAddEditText)
        editText.setText("")
        container.visibility = View.GONE
        val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun bind(container: View, onDismiss: () -> Unit = {}, onSubmit: (String) -> Unit) {
        val editText = container.findViewById<EditText>(R.id.quickAddEditText)
        val doneButton = container.findViewById<TextView>(R.id.quickAddConfirmButton)
        fun submit() {
            val text = editText.text.toString().trim()
            if (text.isNotEmpty()) onSubmit(text)
            hide(container)
            onDismiss()
        }
        doneButton.setOnClickListener { submit() }
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && editText.text.toString().trim().isEmpty()) {
                hide(container)
                onDismiss()
            }
        }
    }
}
