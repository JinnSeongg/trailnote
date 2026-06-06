package com.example.trailnote.core.util

import android.view.View
import android.widget.TextView
import com.example.trailnote.R

fun View.setHeader(
    title: String,
    action: String = "",
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    onAction: (() -> Unit)? = null
) {
    findViewById<TextView>(R.id.headerTitle)?.text = title
    findViewById<TextView>(R.id.headerAction)?.apply {
        text = action
        setOnClickListener { onAction?.invoke() }
    }
    findViewById<TextView>(R.id.headerMore)?.visibility = View.GONE
    findViewById<TextView>(R.id.menuText)?.apply {
        text = if (showBack) "‹" else "☰"
        setOnClickListener { onBack?.invoke() }
    }
}
