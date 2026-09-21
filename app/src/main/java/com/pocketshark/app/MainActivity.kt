package com.pocketshark.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.pocketshark.app.ui.navigation.PocketSharkNavigation
import com.pocketshark.app.ui.theme.PocketSharkTheme

/**
 * Single Activity hosting the Jetpack Compose UI.
 * Renders edge-to-edge with the PocketShark dark theme.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PocketSharkTheme {
                PocketSharkNavigation()
            }
        }
    }
}
