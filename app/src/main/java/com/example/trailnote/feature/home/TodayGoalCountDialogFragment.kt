package com.example.trailnote.feature.home

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TodayGoalCountDialogFragment : BottomSheetDialogFragment() {
    var onSave: ((Int) -> Unit)? = null
    private var currentCount = DEFAULT_COUNT
    private lateinit var countText: TextView
    private lateinit var decreaseButton: TextView
    private lateinit var increaseButton: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentCount = requireArguments().getInt(ARG_COUNT, DEFAULT_COUNT).coerceIn(MIN_COUNT, MAX_COUNT)
        val density = resources.displayMetrics.density

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22, density), dp(10, density), dp(22, density), dp(18, density))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadii = floatArrayOf(
                    dp(18, density).toFloat(), dp(18, density).toFloat(),
                    dp(18, density).toFloat(), dp(18, density).toFloat(),
                    0f, 0f,
                    0f, 0f
                )
            }
        }

        root.addView(
            View(requireContext()).apply {
                background = roundedDrawable("#E2E2E2", dp(2, density))
            },
            LinearLayout.LayoutParams(dp(36, density), dp(4, density)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(16, density)
            }
        )

        root.addView(
            TextView(requireContext()).apply {
                text = "\uC624\uB298 \uBAA9\uD45C \uC218"
                setTextColor(Color.parseColor("#111111"))
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        root.addView(
            TextView(requireContext()).apply {
                text = "\uB9E4\uC77C \uB79C\uB364\uC73C\uB85C \uD45C\uC2DC\uD560 \uC624\uB298 \uBAA9\uD45C \uAC1C\uC218\uC785\uB2C8\uB2E4."
                setTextColor(Color.parseColor("#777777"))
                textSize = 13f
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(6, density)
            }
        )

        val stepperRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        decreaseButton = createStepButton("-")
        countText = TextView(requireContext()).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#111111"))
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            background = roundedStrokeDrawable("#FFFFFF", "#E2E2E2", dp(10, density), dp(1, density))
        }
        increaseButton = createStepButton("+")
        stepperRow.addView(decreaseButton, LinearLayout.LayoutParams(dp(42, density), dp(40, density)))
        stepperRow.addView(
            countText,
            LinearLayout.LayoutParams(dp(86, density), dp(40, density)).apply {
                marginStart = dp(8, density)
                marginEnd = dp(8, density)
            }
        )
        stepperRow.addView(increaseButton, LinearLayout.LayoutParams(dp(42, density), dp(40, density)))
        root.addView(
            stepperRow,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(22, density)
            }
        )

        val actionRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }
        actionRow.addView(
            TextView(requireContext()).apply {
                text = "\uCDE8\uC18C"
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#555555"))
                textSize = 14f
                setOnClickListener { dismissAllowingStateLoss() }
            },
            LinearLayout.LayoutParams(dp(64, density), dp(40, density))
        )
        actionRow.addView(
            TextView(requireContext()).apply {
                text = "\uC800\uC7A5"
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                background = roundedDrawable("#111111", dp(10, density))
                setOnClickListener {
                    onSave?.invoke(currentCount.coerceIn(MIN_COUNT, MAX_COUNT))
                    dismissAllowingStateLoss()
                }
            },
            LinearLayout.LayoutParams(dp(76, density), dp(40, density)).apply {
                marginStart = dp(8, density)
            }
        )
        root.addView(
            actionRow,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(22, density)
            }
        )

        decreaseButton.setOnClickListener {
            currentCount = (currentCount - 1).coerceAtLeast(MIN_COUNT)
            renderCount()
        }
        increaseButton.setOnClickListener {
            currentCount = (currentCount + 1).coerceAtMost(MAX_COUNT)
            renderCount()
        }
        renderCount()
        return root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun createStepButton(label: String): TextView {
        val density = resources.displayMetrics.density
        return TextView(requireContext()).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#111111"))
            background = roundedStrokeDrawable("#FFFFFF", "#E2E2E2", dp(10, density), dp(1, density))
        }
    }

    private fun renderCount() {
        countText.text = "${currentCount}\uAC1C"
        decreaseButton.isEnabled = currentCount > MIN_COUNT
        increaseButton.isEnabled = currentCount < MAX_COUNT
        decreaseButton.alpha = if (decreaseButton.isEnabled) 1f else 0.35f
        increaseButton.alpha = if (increaseButton.isEnabled) 1f else 0.35f
    }

    private fun roundedDrawable(colorHex: String, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(colorHex))
            cornerRadius = radius.toFloat()
        }
    }

    private fun roundedStrokeDrawable(colorHex: String, strokeHex: String, radius: Int, strokeWidth: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(colorHex))
            cornerRadius = radius.toFloat()
            setStroke(strokeWidth, Color.parseColor(strokeHex))
        }
    }

    private fun dp(value: Int, density: Float): Int = (value * density).toInt()

    companion object {
        private const val ARG_COUNT = "count"
        private const val DEFAULT_COUNT = 3
        private const val MIN_COUNT = 1
        private const val MAX_COUNT = 20

        fun newInstance(count: Int): TodayGoalCountDialogFragment {
            return TodayGoalCountDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COUNT, count)
                }
            }
        }
    }
}
