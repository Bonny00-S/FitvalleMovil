package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(navController: NavController) {
    val fondoPrincipal = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    val primario = Color(0xFFB1163A)

    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mis plantillas", "Asignadas por mi coach")

    var myTemplates by remember { mutableStateOf<List<Template>>(emptyList()) }
    var assignedRoutines by remember { mutableStateOf<List<Routine>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }

    // 🔥 Cargar datos desde los DAOs
    LaunchedEffect(Unit) {
        loading = true
        val templateDao = TemplateDao()
        val routineDao = RoutineDao()

        scope.launch {
            myTemplates = templateDao.getUserTemplates()
        }
        scope.launch {
            assignedRoutines = routineDao.getAssignedRoutines()
        }
        loading = false
    }

    Scaffold(
        floatingActionButton = {
            // solo tiene sentido crear plantillas propias
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("crearPlantilla") },
                    containerColor = primario
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar Plantilla",
                        tint = Color.White
                    )
                }
            }
        },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Plantillas",
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondoPrincipal)
                .padding(innerPadding)
        ) {
            Column(Modifier.fillMaxSize()) {

                // 🔹 Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF182235)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, color = Color.White) }
                        )
                    }
                }

                when {
                    loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    selectedTab == 0 -> {
                        // ==========================
                        //   MIS PLANTILLAS
                        // ==========================
                        if (myTemplates.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No tienes plantillas creadas aún.", color = Color.White)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                items(myTemplates) { template ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(
                                                0xFF2E1A1A
                                            )
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .clickable {
                                                navController.navigate("templateDetail/${template.id}")
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                template.name,
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "${template.exercises.size} ejercicios",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // ==========================
                        //   ASIGNADAS POR MI COACH
                        // ==========================
                        val fondoCard = Color(0xFF2E1A1A)
                        val textoSecundario = Color(0xFFAAB2C5)
                        val primario = Color(0xFFB1163A)
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                        var sessions by remember {
                            mutableStateOf<List<Pair<String, Session>>>(
                                emptyList()
                            )
                        }
                        var loadingSessions by remember { mutableStateOf(true) }

                        // 🔹 Cargar sesiones desde las rutinas asignadas al usuario actual
                        LaunchedEffect(assignedRoutines) {
                            loadingSessions = true
                            val list = mutableListOf<Pair<String, Session>>()

                            assignedRoutines
                                .filter { it.customerId == currentUserId }
                                .forEach { routine ->
                                    routine.sessions.forEach { (sessionId, session) ->
                                        list.add(sessionId to session.copy(routineId = routine.id))
                                    }
                                }

                            sessions = list
                            loadingSessions = false
                        }

                        when {
                            loadingSessions -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }

                            sessions.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Tu entrenador todavía no te asignó sesiones.",
                                        color = Color.White
                                    )
                                }
                            }

                            else -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    items(sessions) { (sessionId, session) ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = fondoCard),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    // Ir directamente a la sesión activa
                                                    navController.navigate("activeSession/${sessionId}/${session.routineId}")
                                                }
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            ) {
                                                Text(
                                                    text = "Sesión ${sessionId.take(6)}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                                if (session.registerDate.isNotEmpty()) {
                                                    Text(
                                                        "Registrada: ${session.registerDate}",
                                                        color = textoSecundario,
                                                        fontSize = 13.sp
                                                    )
                                                }

                                                Text(
                                                    "Ejercicios: ${session.sessionExercises?.size ?: 0}",
                                                    color = textoSecundario,
                                                    fontSize = 13.sp
                                                )

                                                Spacer(Modifier.height(8.dp))

                                                Button(
                                                    onClick = {
                                                        navController.navigate("activeSession/${sessionId}/${session.routineId}")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = primario
                                                    ),
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
            }
        }
    }
}
