package com.example.trailnote.core.util

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.trailnote.R

object DeleteConfirmDialogHelper {
    fun showSingle(
        context: Context,
        targetName: String?,
        onDelete: () -> Unit
    ) {
        val name = targetName?.trim().orEmpty()
        show(
            context = context,
            title = "삭제할까요?",
            message = if (name.isNotEmpty()) {
                "${name}을(를) 삭제합니다. 이 작업은 되돌릴 수 없습니다."
            } else {
                "선택한 항목을 삭제합니다. 이 작업은 되돌릴 수 없습니다."
            },
            onDelete = onDelete
        )
    }

    fun showMultiple(
        context: Context,
        count: Int,
        onDelete: () -> Unit
    ) {
        show(
            context = context,
            title = "선택 항목을 삭제할까요?",
            message = "선택한 ${count}개 항목을 삭제합니다. 이 작업은 되돌릴 수 없습니다.",
            onDelete = onDelete
        )
    }

    fun showCustom(
        context: Context,
        title: String,
        message: String,
        onDelete: () -> Unit
    ) {
        show(context, title, message, onDelete)
    }

    private fun show(
        context: Context,
        title: String,
        message: String,
        onDelete: () -> Unit
    ) {
        val dialog = AlertDialog.Builder(context).create()
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_dialog_rounded)
            setPadding(20.dp(context), 20.dp(context), 20.dp(context), 16.dp(context))
        }
        content.addView(TextView(context).apply {
            text = title
            setTextColor(context.getColor(R.color.trail_ink))
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        })
        content.addView(TextView(context).apply {
            text = message
            setTextColor(context.getColor(R.color.trail_text_secondary))
            textSize = 14f
            setLineSpacing(2.dp(context).toFloat(), 1f)
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 10.dp(context)
        })
        content.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            addView(dialogButton(context, "취소", R.drawable.bg_button_outline_rounded, R.color.trail_ink) {
                dialog.dismiss()
            }, LinearLayout.LayoutParams(0, 46.dp(context), 1f))
            addView(dialogButton(context, "삭제", R.drawable.bg_button_black_rounded, R.color.white) {
                dialog.dismiss()
                onDelete()
            }, LinearLayout.LayoutParams(0, 46.dp(context), 1f).apply {
                leftMargin = 10.dp(context)
            })
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 18.dp(context)
        })

        dialog.setView(content)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.show()
    }

    private fun dialogButton(
        context: Context,
        label: String,
        backgroundResId: Int,
        textColorResId: Int,
        onClick: () -> Unit
    ): TextView {
        return TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(context.getColor(textColorResId))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setBackgroundResource(backgroundResId)
            setOnClickListener { onClick() }
        }
    }

    private fun Int.dp(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
