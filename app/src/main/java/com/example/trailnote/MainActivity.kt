package com.example.trailnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.trailnote.app.TrailNoteApp
import com.example.trailnote.core.design.theme.TrailNoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrailNoteTheme {
                TrailNoteApp()
            }
        }
    }
}
