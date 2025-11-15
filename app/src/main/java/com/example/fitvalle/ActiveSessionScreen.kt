package com.example.fitvalle

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ActiveSessionScreen(
    sessionId: String,
    navController: NavController,
    routineId: String,
    onFinish: () -> Unit = {
        navController.navigate("training") {
            popUpTo("training") { inclusive = true }
        }
    },
    onCancel: () -> Unit = { navController.popBackStack() }
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D1525), Color(0xFF182235))
    )

    var exercises by remember { mutableStateOf<List<SessionExercise>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showConfirm by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 🔹 Cargar ejercicios desde Firebase
    LaunchedEffect(sessionId) {
        val dao = SessionDao()
        try {
            exercises = dao.getSessionExercises(sessionId)
        } catch (e: Exception) {
            Log.e("ActiveSession", "Error al cargar ejercicios: ${e.message}")
        } finally {
            loading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Entrenamiento", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = {
                        scope.launch {
                            val completedExercises = exercises.filter { it.completed }
                            if (completedExercises.isEmpty()) {
                                snackbarHostState.showSnackbar(
                                    "Debes completar al menos un ejercicio antes de terminar."
                                )
                                return@launch
                            }

                            val dao = SessionDao()
                            val customerId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                            val success = dao.saveCompletedSession(
                                customerId = customerId,
                                routineId = routineId,
                                sessionId = sessionId,
                                exercisesDone = completedExercises
                            )

                            if (success) {
                                dao.updateLastSessionTrained(customerId, sessionId)
                                snackbarHostState.showSnackbar("✅ Sesión completada correctamente.")
                                onFinish()
                            } else {
                                snackbarHostState.showSnackbar("❌ Error al guardar sesión completada.")
                            }
                        }
                    }) {
                        Text("TERMINAR", color = Color(0xFFFFCDD2), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Button(
                onClick = { showConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB1163A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("CANCELAR ENTRENAMIENTO", color = Color.White)
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when {
                loading -> CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))

                exercises.isEmpty() -> Text(
                    "No hay ejercicios asignados para esta sesión.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(exercises) { exercise ->
                        ExerciseProgressCard(
                            exercise = exercise,
                            navController = navController
                        ) { checked ->
                            exercises = exercises.map {
                                if (it.exerciseId == exercise.exerciseId) it.copy(completed = checked)
                                else it
                            }
                        }
                    }
                }
            }

            if (showConfirm) {
                CancelTrainingDialog(
                    onConfirm = {
                        showConfirm = false
                        onCancel()
                    },
                    onDismiss = { showConfirm = false }
                )
            }
        }
    }
}

@Composable
fun ExerciseProgressCard(
    exercise: SessionExercise,
    navController: NavController,
    onCheckedChange: (Boolean) -> Unit
) {
    var completed by remember { mutableStateOf(exercise.completed) }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Navegar y mandar el ejercicio completo
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("exerciseDetail", exercise)

                navController.navigate("exerciseSessionDetail")
            }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = exercise.exerciseName ?: "Ejercicio sin nombre",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Series: ${exercise.sets}", color = Color(0xFFFFCDD2))
                    Text("Reps: ${exercise.reps}", color = Color(0xFFFFCDD2))
                    Text("Peso: ${exercise.weight} kg", color = Color(0xFFFFCDD2))
                    Text("Velocidad: ${exercise.speed}", color = Color(0xFFFFCDD2))
                    Text("Duración: ${exercise.duration} min", color = Color(0xFFFFCDD2))
                }

                Checkbox(
                    checked = completed,
                    onCheckedChange = {
                        completed = it
                        onCheckedChange(it)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFD12B56),
                        uncheckedColor = Color.White
                    )
                )
            }
        }
    }
}


@Composable
fun CancelTrainingDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF182235)),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp)
        ) {
            Column(
                Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Cancelar entrenamiento", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                Text("¿Seguro que deseas cancelar la sesión actual?", color = Color(0xFFFFCDD2), fontSize = 16.sp)
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB1163A))
                    ) {
                        Text("Sí, cancelar", color = Color.White)
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("No", color = Color.White)
                    }
                }
            }
        }
    }
}
