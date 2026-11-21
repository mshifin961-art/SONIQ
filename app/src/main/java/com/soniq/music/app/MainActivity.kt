package com.soniq.music.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.soniq.music.ui.navigation.SoniqAppShell
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for SONIQ's Jetpack Compose UI.
 * Playback lifecycle is strictly decoupled from this Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            SoniqAppShell()
        }
    }
}
