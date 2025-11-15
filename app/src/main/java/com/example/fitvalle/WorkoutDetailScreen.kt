package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    navController: NavController,
    routineId: String,
    coachName: String
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    val scope = rememberCoroutineScope()

    // 🔹 Datos simulados o cargados desde Firebase
    var exercises by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(routineId) {
        scope.launch {
            // Aquí puedes cargar desde Firebase, por ahora ejemplo estático
            exercises = listOf("Sentadillas", "Press de banca", "Peso muerto", "Curl de bíceps")
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rutina de $coachName",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                loading -> CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
                exercises.isEmpty() -> Text(
                    text = "No se encontraron ejercicios.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> {
                    Column {
                        Text(
                            text = "Ejercicios (${exercises.size})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(exercises) { ejercicio ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = ejercicio,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
