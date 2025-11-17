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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val fondoPrincipal = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))

    // 🔹 Cargar historial desde Firebase
    var historyList by remember { mutableStateOf<List<CompletedSessionItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val customerId = currentUser.uid
                val dbRoot = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/").reference
                val snapshot = dbRoot.child("completedSessions").get().await()
                
                val sessions = mutableListOf<CompletedSessionItem>()
                for (child in snapshot.children) {
                    val id = child.child("id").getValue(String::class.java) ?: continue
                    val userId = child.child("customerId").getValue(String::class.java) ?: ""
                    val routineId = child.child("routineId").getValue(String::class.java) ?: ""
                    val sessionId = child.child("sessionId").getValue(String::class.java) ?: ""
                    val dateFinished = child.child("dateFinished").getValue(String::class.java) ?: ""
                    val exerciseCount = child.child("exercisesDone").childrenCount.toInt()
                    
                    // Filtrar solo las sesiones del usuario actual
                    if (userId == customerId) {
                        sessions.add(
                            CompletedSessionItem(
                                id = id,
                                routineId = routineId,
                                sessionId = sessionId,
                                exerciseCount = exerciseCount,
                                dateFinished = dateFinished
                            )
                        )
                    }
                }
                historyList = sessions.sortedByDescending { it.dateFinished }
            }
        } catch (e: Exception) {
            Log.e("HistoryScreen", "Error loading completed sessions: ${e.message}", e)
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial de Entrenamientos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                
                historyList.isEmpty() -> Text(
                    "Aún no tienes entrenamientos registrados.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyList) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clickable {
                                    navController.navigate("completedSessionDetail/${item.id}")
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column {
                                    Text(
                                        text = "Rutina - ${item.exerciseCount} ejercicios completados",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Fecha: ${formatDate(item.dateFinished)}",
                                        color = Color(0xFFFFCDD2),
                                        fontSize = 12.sp
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

// 📋 Data class para las sesiones completadas
data class CompletedSessionItem(
    val id: String,
    val routineId: String,
    val sessionId: String,
    val exerciseCount: Int,
    val dateFinished: String
)