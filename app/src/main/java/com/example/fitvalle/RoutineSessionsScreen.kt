package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitvalle.data.dao.RoutineDao
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineSessionsScreen(
    routineId: String,
    navController: NavController
) {
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF8E0E00), Color(0xFF1F1C18))
    )

    val scope = rememberCoroutineScope()
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var lastSessionTrained by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val dao = SessionDao()
        val customerId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        // 🔹 Cargar sesiones de la rutina
        val routineDao = RoutineDao()
        sessions = routineDao.getSessionsByRoutine(routineId)

        // 🔹 Obtener última sesión completada
        lastSessionTrained = dao.getLastSessionTrained(customerId)

        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sesiones de rutina",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
            Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )

                sessions.isEmpty() -> Text("No hay sesiones disponibles", color = Color.White)

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 👇 usamos itemsIndexed para numerarlas
                    itemsIndexed(sessions) { index, session ->
                        val completed = session.id == lastSessionTrained
                        SessionCard(
                            session = session,
                            completed = completed,
                            sessionNumber = index + 1
                        ) {
                            navController.navigate("activeSession/${session.id}/$routineId")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCard(
    session: Session,
    completed: Boolean,
    sessionNumber: Int,
    onClick: () -> Unit
) {
    val color = if (completed) Color(0xFF4CAF50) else Color(0xFFB71C1C)
    val textStatus = if (completed) "Completada" else "Pendiente"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            // 🔹 Mostramos "Sesión 1", "Sesión 2", etc.
            Text(
                text = "Sesión $sessionNumber",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Estado: $textStatus", color = color, fontWeight = FontWeight.Medium)
        }
    }
}
