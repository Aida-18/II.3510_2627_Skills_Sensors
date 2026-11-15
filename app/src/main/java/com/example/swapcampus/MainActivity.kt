package com.example.swapcampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * Point d'entrée Android : se contente d'afficher l'[App] partagée (commonMain).
 * Les données vivent dans AppContainer (singleton), elles survivent donc à la
 * recréation de l'activité (rotation, changement de langue...).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
