package br.com.autombot.ecogestor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.autombot.ecogestor.ui.EcoGestorApp
import br.com.autombot.ecogestor.ui.theme.EcoGestorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoGestorTheme {
                EcoGestorApp()
            }
        }
    }
}
