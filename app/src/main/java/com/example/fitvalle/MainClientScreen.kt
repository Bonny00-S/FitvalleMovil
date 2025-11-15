package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MainClientScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Panel del cliente",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            // Botón ejemplo: ir a tu plan (ajusta la ruta si ya tienes esa pantalla)
            Button(
                onClick = { /* navController.navigate("tuRutaDePlan") */ },
                colors = ButtonDefaults.buttonColors(Color(0xFFD50000)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver mi plan", color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            // Otro botón opcional, deja el onClick para cuando tengas la ruta
            Button(
                onClick = { /* navController.navigate("tuRutaDeEntrenamientos") */ },
                colors = ButtonDefaults.buttonColors(Color.Gray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrenamientos", color = Color.White)
            }
        }
    }
}

