package com.example.fitvalle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fitvalle.R

@Composable
fun AvatarEditScreen(navController: NavController) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))

    val avatars = listOf(
        R.drawable.activity,
        R.drawable.activity,
        R.drawable.activity,
        R.drawable.activity,
        R.drawable.activity
    )

    var selectedAvatar by remember { mutableStateOf(avatars.first()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecciona tu avatar",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(vertical = 20.dp)
            )

            Image(
                painter = painterResource(id = selectedAvatar),
                contentDescription = "Avatar seleccionado",
                modifier = Modifier
                    .size(130.dp)
                    .padding(8.dp)
            )

            Text("Avatar actual", color = Color(0xFFFFCDD2), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(avatars) { avatar ->
                    Image(
                        painter = painterResource(id = avatar),
                        contentDescription = "Opción de avatar",
                        modifier = Modifier
                            .size(80.dp)
                            .clickable { selectedAvatar = avatar }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Aquí puedes guardar el avatar seleccionado en Firestore o Firebase Storage
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(Color(0xFFD50000))
            ) {
                Text("GUARDAR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
