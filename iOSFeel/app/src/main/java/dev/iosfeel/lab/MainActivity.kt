package dev.iosfeel.lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * Single Activity for the iOSFeel Interaction Laboratory.
 *
 * Uses edge-to-edge rendering so the framework can control
 * its own inset handling in Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaboratoryApp()
        }
    }
}
