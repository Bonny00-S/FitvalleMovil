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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedSessionDetailScreen(navController: NavController, sessionId: String) {
    val fondoPrincipal = Brush.verticalGradient(listOf(Color(0xFF0D1525), Color(0xFF182235)))
    
    var sessionDetail by remember { mutableStateOf<Map<String, Any>?>(null) }
    var exercises by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    
    LaunchedEffect(sessionId) {
        try {
            val dbRoot = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/").reference
            val snapshot = dbRoot.child("completedSessions").get().await()
            
            for (child in snapshot.children) {
                val id = child.child("id").getValue(String::class.java)
                if (id == sessionId) {
                    val data = mutableMapOf<String, Any>()
                    data["id"] = child.child("id").getValue(String::class.java) ?: ""
                    data["routineId"] = child.child("routineId").getValue(String::class.java) ?: ""
                    data["dateFinished"] = child.child("dateFinished").getValue(String::class.java) ?: ""
                    
                    sessionDetail = data
                    
                    // Cargar ejercicios
                    val exercisesList = mutableListOf<Map<String, Any>>()
                    val exercisesNode = child.child("exercisesDone")
                    for (exChild in exercisesNode.children) {
                        val exercise = mutableMapOf<String, Any>()
                        exercise["exerciseId"] = exChild.child("exerciseId").getValue(String::class.java) ?: ""
                        exercise["exerciseName"] = exChild.child("exerciseName").getValue(String::class.java) ?: "Sin nombre"
                        exercise["sets"] = exChild.child("sets").getValue(Int::class.java) ?: 0
                        exercise["reps"] = exChild.child("reps").getValue(Int::class.java) ?: 0
                        exercise["weight"] = exChild.child("weight").getValue(Int::class.java) ?: 0
                        exercise["speed"] = exChild.child("speed").getValue(Int::class.java) ?: 0
                        exercise["duration"] = exChild.child("duration").getValue(Int::class.java) ?: 0
                        exercisesList.add(exercise)
                    }
                    exercises = exercisesList
                    break
                }
            }
        } catch (e: Exception) {
            Log.e("CompletedSessionDetail", "Error loading session: ${e.message}", e)
        } finally {
            loading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Entrenamiento", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .background(fondoPrincipal)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                loading -> CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
                
                sessionDetail == null -> Text(
                    "Sesión no encontrada",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Encabezado con información de la sesión
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "Fecha: ${formatDate(sessionDetail?.get("dateFinished")?.toString() ?: "")}",
                                    color = Color(0xFFFFCDD2),
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Total de ejercicios: ${exercises.size}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    
                    // Lista de ejercicios
                    items(exercises) { exercise ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    exercise["exerciseName"]?.toString() ?: "Ejercicio",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                
                                Spacer(Modifier.height(12.dp))
                                
                                // Parámetros en 2 columnas
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        ParameterRow("Series", exercise["sets"]?.toString() ?: "0")
                                        ParameterRow("Reps", exercise["reps"]?.toString() ?: "0")
                                        ParameterRow("Peso", "${exercise["weight"]?.toString() ?: "0"} kg")
                                    }
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        ParameterRow("Velocidad", exercise["speed"]?.toString() ?: "0")
                                        ParameterRow("Duración", "${exercise["duration"]?.toString() ?: "0"} min")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParameterRow(label: String, value: String) {
    Column {
        Text(label, color = Color(0xFFFFCDD2), fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
    }
}
