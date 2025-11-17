package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    navController: NavController,
    templateId: String
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    val rojo = Color(0xFFB1163A)

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    val db = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("templates")

    var template by remember { mutableStateOf<Template?>(null) }
    var loading by remember { mutableStateOf(true) }

    // 🔥 Cargar plantilla del usuario actual
    LaunchedEffect(templateId) {
        if (userId != null) {
            db.child(userId).child(templateId).get()
                .addOnSuccessListener { snapshot ->
                    template = snapshot.getValue(Template::class.java)
                    loading = false
                }
                .addOnFailureListener {
                    loading = false
                }
        } else {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Plantilla", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // ✏️ Botón de editar
                    IconButton(onClick = {
                        navController.navigate("editTemplate/${templateId}")
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        bottomBar = {
            // 🏋️‍♂️ Botón para iniciar entrenamiento (solo si hay ejercicios)
            if (template != null && template!!.exercises.isNotEmpty()) {
                Button(
                    onClick = {
                        navController.navigate("training/${templateId}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = rojo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "INICIAR ENTRENAMIENTO",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            } else if (template != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = template!!.name,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Divider(color = Color.White.copy(alpha = 0.3f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ejercicios incluidos:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (template!!.exercises.isEmpty()) {
                        Text(
                            text = "No hay ejercicios registrados en esta plantilla.",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(template!!.exercises) { index, ex ->
                                // ex es TemplateExercise
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            text = "${index + 1}. ${ex.exerciseName}",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            text = "Series: ${ex.sets}  •  Reps: ${ex.reps}  •  Peso: ${ex.weight}kg",
                                            color = Color(0xFFFFCDD2),
                                            fontSize = 13.sp
                                        )

                                        Text(
                                            text = "Velocidad: ${ex.speed}  •  Duración: ${ex.duration} min",
                                            color = Color(0xFFFFCDD2),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    "No se encontró la plantilla.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
