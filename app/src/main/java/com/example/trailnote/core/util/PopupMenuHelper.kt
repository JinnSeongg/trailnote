package com.example.trailnote.core.util

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.PopupMenu
import com.example.trailnote.R

object PopupMenuHelper {
    fun show(
        context: Context,
        anchor: View,
        items: List<String>,
        onItemClick: (String) -> Boolean
    ) {
        PopupMenu(ContextThemeWrapper(context, R.style.TrailNote_PopupMenuTheme), anchor).apply {
            items.forEach { menu.add(it) }
            setOnMenuItemClickListener { item -> onItemClick(item.title.toString()) }
        }.show()
    }
}
