package com.example.trailnote.core.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.setupTwoLineLimitedDescriptionEditText(onValidTextChanged: (String) -> Unit): TextWatcher {
    var lastValidText = text?.toString().orEmpty()
    var isReverting = false

    val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            if (isReverting) return
            post {
                if (isReverting) return@post
                val nextText = text?.toString().orEmpty()
                if (lineCount > MAX_DESCRIPTION_LINES) {
                    isReverting = true
                    setText(lastValidText)
                    setSelection(lastValidText.length)
                    isReverting = false
                } else {
                    lastValidText = nextText
                    onValidTextChanged(nextText)
                }
            }
        }
    }

    addTextChangedListener(watcher)
    return watcher
}

private const val MAX_DESCRIPTION_LINES = 2
