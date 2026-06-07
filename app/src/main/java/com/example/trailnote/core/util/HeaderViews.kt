package com.example.trailnote.core.util

import android.view.View
import android.widget.TextView
import com.example.trailnote.MainActivity
import com.example.trailnote.R

fun View.setHeader(
    title: String,
    action: String = "",
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    onAction: (() -> Unit)? = null,
    showMore: Boolean = false,
    onMore: (() -> Unit)? = null
) {
    findViewById<TextView>(R.id.headerTitle)?.text = title
    findViewById<TextView>(R.id.headerAction)?.apply {
        text = action
        setOnClickListener { onAction?.invoke() }
    }
    findViewById<TextView>(R.id.headerMore)?.apply {
        visibility = if (showMore) View.VISIBLE else View.GONE
        setOnClickListener { onMore?.invoke() }
    }
    findViewById<TextView>(R.id.menuText)?.apply {
        text = if (showBack) "\u2039" else "\u2630"
        setOnClickListener {
            if (showBack) {
                onBack?.invoke()
            } else {
                (context as? MainActivity)?.openDrawer() ?: onBack?.invoke()
            }
        }
    }
}
