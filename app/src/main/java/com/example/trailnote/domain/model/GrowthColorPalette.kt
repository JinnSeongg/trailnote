package com.example.trailnote.domain.model

object GrowthColorPalette {
    const val DEFAULT_COLOR = "#8B7CFF"

    val colors = listOf(
        "#8B7CFF",
        "#F9A8D4",
        "#FF8A65",
        "#FCD34D",
        "#A3E635",
        "#4ADE80",
        "#5EEAD4",
        "#67E8F9",
        "#60A5FA",
        "#818CF8"
    )

    fun normalize(colorHex: String?): String {
        val color = colorHex.orEmpty().trim().uppercase()
        return if (color in colors) color else DEFAULT_COLOR
    }
}
