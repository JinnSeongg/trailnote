package com.example.trailnote.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = TrailBlack,
    onPrimary = TrailWhite,
    secondary = TrailGray700,
    background = TrailWhite,
    onBackground = TrailBlack,
    surface = TrailWhite,
    onSurface = TrailBlack,
    surfaceVariant = TrailGray100,
    outline = TrailGray300
)

@Composable
fun TrailNoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = TrailNoteTypography,
        content = content
    )
}
