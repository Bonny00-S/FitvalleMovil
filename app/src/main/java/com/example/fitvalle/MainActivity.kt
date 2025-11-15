package com.example.fitvalle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.fitvalle.ui.theme.FitvalleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            // 🌈 Aplica tu tema principal (colores, tipografía, etc.)
            FitvalleTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    // 🧭 Crea el controlador de navegación principal
                    val navController = rememberNavController()

                    // 🚀 Llama al NavigationController general
                    NavigationController(navController)
                }
            }
        }
    }
}
