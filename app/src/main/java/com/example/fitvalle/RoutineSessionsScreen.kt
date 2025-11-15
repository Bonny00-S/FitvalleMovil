package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun RoutineSessionsScreen(
    routineId: String,
    navController: NavController
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF0D1525), Color(0xFF182235)))
    val primary = Color(0xFFB1163A)

    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(routineId) {
        loading = true
        val dao = RoutineDao()
        try {
            // 🧠 Obtiene las rutinas asignadas y busca la correspondiente
            val routines = dao.getAssignedRoutines()
            val routine = routines.find { it.id == routineId }

            if (routine != null) {
                sessions = routine.sessions.values.toList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sesiones de la rutina", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
        ) {
            when {
                loading -> CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                sessions.isEmpty() -> Text(
                    "No hay sesiones disponibles para esta rutina.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sessions) { session ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("activeSession/${session.id}/$routineId")
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Sesión ${session.id.take(6)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Registrada: ${session.registerDate}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )

                                val exerciseCount = session.sessionExercises?.size ?: 0
                                Text(
                                    text = "Ejercicios: $exerciseCount",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )

                                if (exerciseCount > 0) {
                                    Spacer(Modifier.height(6.dp))
                                    session.sessionExercises!!.values.take(3).forEach {
                                        Text("• ${it.exerciseName ?: "Ejercicio"}",
                                            color = Color(0xFFAAB2C5),
                                            fontSize = 12.sp)
                                    }
                                    if (exerciseCount > 3) {
                                        Text("+${exerciseCount - 3} más...",
                                            color = Color(0xFFAAB2C5),
                                            fontSize = 12.sp)
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        navController.navigate("activeSession/${session.id}/$routineId")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Iniciar sesión", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
