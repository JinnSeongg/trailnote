package com.example.trailnote.feature.growth

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.trailnote.domain.model.GrowthColorPalette

class GrowthAreaColorDialogFragment : DialogFragment() {
    var onColorSelected: ((String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val selectedColor = GrowthColorPalette.normalize(requireArguments().getString(ARG_SELECTED_COLOR))
        val density = resources.displayMetrics.density

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20, density), dp(18, density), dp(20, density), dp(18, density))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(8, density).toFloat()
            }
        }

        root.addView(
            TextView(requireContext()).apply {
                text = "\uC0C9\uC0C1 \uC120\uD0DD"
                setTextColor(Color.parseColor("#111111"))
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        val paletteRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(12, density), 0, 0)
        }

        GrowthColorPalette.colors.take(PALETTE_SIZE).forEachIndexed { index, colorHex ->
            paletteRow.addView(
                TextView(requireContext()).apply {
                    gravity = Gravity.CENTER
                    text = if (colorHex == selectedColor) "\u2713" else ""
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor("#111111"))
                    background = swatchDrawable(colorHex, colorHex == selectedColor, density)
                    setOnClickListener {
                        Log.d(TAG, "swatch click selected=$colorHex")
                        onColorSelected?.invoke(colorHex)
                        dismissAllowingStateLoss()
                    }
                },
                LinearLayout.LayoutParams(dp(SWATCH_WIDTH_DP, density), dp(SWATCH_HEIGHT_DP, density)).apply {
                    setMargins(dp(SWATCH_GAP_DP, density), dp(SWATCH_GAP_DP, density), dp(SWATCH_GAP_DP, density), dp(SWATCH_GAP_DP, density))
                }
            )
        }

        root.addView(
            HorizontalScrollView(requireContext()).apply {
                setFillViewport(true)
                isHorizontalScrollBarEnabled = false
                overScrollMode = HorizontalScrollView.OVER_SCROLL_NEVER
                addView(
                    LinearLayout(requireContext()).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                        addView(
                            paletteRow,
                            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        )
                    },
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                )
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        return Dialog(requireContext()).apply {
            setContentView(root)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * DIALOG_WIDTH_RATIO).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun swatchDrawable(colorHex: String, isSelected: Boolean, density: Float): GradientDrawable {
        val normalizedColor = GrowthColorPalette.normalize(colorHex)
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(12, density).toFloat()
            setColor(Color.parseColor(normalizedColor))
            setStroke(
                dp(if (isSelected) 3 else 1, density),
                Color.parseColor(if (isSelected) "#111111" else "#E2E2E2")
            )
        }
    }

    private fun dp(value: Int, density: Float): Int = (value * density).toInt()

    companion object {
        private const val ARG_SELECTED_COLOR = "selectedColor"
        private const val PALETTE_SIZE = 10
        private const val SWATCH_WIDTH_DP = 28
        private const val SWATCH_HEIGHT_DP = 68
        private const val SWATCH_GAP_DP = 2
        private const val DIALOG_WIDTH_RATIO = 0.96f
        private const val TAG = "GrowthColorDebug"

        fun newInstance(selectedColor: String): GrowthAreaColorDialogFragment {
            return GrowthAreaColorDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SELECTED_COLOR, selectedColor)
                }
            }
        }
    }
}
