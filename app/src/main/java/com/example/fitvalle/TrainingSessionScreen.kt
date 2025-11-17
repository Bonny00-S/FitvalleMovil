package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingSessionScreen(navController: NavController, templateId: String) {
    val fondo = Brush.verticalGradient(listOf(Color(0xFF0D1525), Color(0xFF1F1C18)))
    val rojo = Color(0xFFB1163A)

    // 🔹 Cargar la plantilla desde Firebase
    var template by remember { mutableStateOf<Template?>(null) }
    var loading by remember { mutableStateOf(true) }

    // 🔹 Estado local: ejercicios con valores planificados y realizados
    val trainingExercises = remember { mutableStateListOf<TrainingExercise>() }

    // 🔹 Diálogo de edición
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var tmpSets by remember { mutableStateOf("") }
    var tmpReps by remember { mutableStateOf("") }
    var tmpWeight by remember { mutableStateOf("") }
    var tmpDuration by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(templateId) {
        val db = FirebaseDatabase
            .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
            .getReference("templates")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.child(userId).child(templateId).get().addOnSuccessListener { snap ->
                template = snap.getValue(Template::class.java)
                loading = false
                // Inicializar estado de ejercicios
                trainingExercises.clear()
                template?.exercises?.forEach { ex ->
                    trainingExercises.add(ex.toTrainingExercise())
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Rutina de hoy", color = Color.White) },
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
        bottomBar = {
            Button(
                onClick = {
                    scope.launch {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId == null) {
                            snackbarHostState.showSnackbar("❌ Debes iniciar sesión")
                            return@launch
                        }

                        // Filtrar solo ejercicios completados
                        val completedExercises = trainingExercises.filter { it.completed }
                        if (completedExercises.isEmpty()) {
                            snackbarHostState.showSnackbar("⚠️ Marca al menos un ejercicio como completado")
                            return@launch
                        }

                        // Guardar sesión
                        val sessionId = UUID.randomUUID().toString()
                        val exercisesDone = completedExercises.map { it.toSessionExercise().copy(sessionId = sessionId) }

                        val dao = SessionDao()
                        val success = dao.saveCompletedSession(
                            customerId = userId,
                            routineId = templateId,
                            sessionId = sessionId,
                            exercisesDone = exercisesDone
                        )

                        if (success) {
                            dao.updateLastSessionTrained(userId, sessionId)
                            snackbarHostState.showSnackbar("✅ Rutina guardada en historial")
                            navController.popBackStack()
                        } else {
                            snackbarHostState.showSnackbar("❌ Error al guardar")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = rojo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("FINALIZAR RUTINA", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(padding)
                .padding(16.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                template?.let {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = it.name,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${trainingExercises.size} ejercicios incluidos",
                            color = Color(0xFFFFCDD2)
                        )
                        Spacer(Modifier.height(16.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            itemsIndexed(trainingExercises) { index, ex ->
                                TrainingExerciseCard(
                                    index = index + 1,
                                    exercise = ex,
                                    onEdit = {
                                        selectedIndex = index
                                        tmpSets = ex.performedSets.toString()
                                        tmpReps = ex.performedReps.toString()
                                        tmpWeight = ex.performedWeight.toString()
                                        tmpDuration = ex.performedDuration.toString()
                                        showEditDialog = true
                                    },
                                    onCheckChanged = { isChecked ->
                                        val updated = ex.copy(completed = isChecked)
                                        trainingExercises[index] = updated
                                    }
                                )
                            }
                        }
                    }
                } ?: Text("No se encontró la rutina", color = Color.White)
            }
        }
    }

    // 🔹 Diálogo para editar valores realizados
    if (showEditDialog && selectedIndex != null) {
        val idx = selectedIndex!!
        val ex = trainingExercises[idx]

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Registrar lo que realizaste", color = Color.White) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(ex.exerciseName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    
                    // Comparación plan vs realizado
                    Text("Plan: ${ex.plannedSets}s x ${ex.plannedReps}r | ${ex.plannedWeight}kg", color = Color(0xFFFFCDD2), fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))

                    TrainingInputField("Series", tmpSets) { tmpSets = it }
                    TrainingInputField("Reps", tmpReps) { tmpReps = it }
                    TrainingInputField("Peso (kg)", tmpWeight) { tmpWeight = it }
                    TrainingInputField("Duración (min)", tmpDuration) { tmpDuration = it }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val sets = tmpSets.toIntOrNull() ?: 0
                    val reps = tmpReps.toIntOrNull() ?: 0
                    val weight = tmpWeight.toIntOrNull() ?: 0
                    val duration = tmpDuration.toIntOrNull() ?: 0

                    val updated = ex.copy(
                        performedSets = sets,
                        performedReps = reps,
                        performedWeight = weight,
                        performedDuration = duration
                    )
                    trainingExercises[idx] = updated
                    showEditDialog = false
                }) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = Color(0xFF2B1A1A)
        )
    }
}

@Composable
fun TrainingInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color(0xFFFFCDD2), fontSize = 12.sp)
        OutlinedTextField(
            value = value,
            onValueChange = { newVal ->
                // Validar: solo dígitos
                if (newVal.isEmpty() || newVal.all { it.isDigit() }) {
                    onValueChange(newVal)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                cursorColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun TrainingExerciseCard(
    index: Int,
    exercise: TrainingExercise,
    onEdit: () -> Unit,
    onCheckChanged: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (exercise.completed) Color(0xFF4A1515) else Color(0xFF2E1A1A)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("$index. ${exercise.exerciseName}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                // Comparación plan vs realizado
                Text(
                    "${exercise.plannedSets}s x ${exercise.plannedReps}r → ${exercise.performedSets}s x ${exercise.performedReps}r | ${exercise.performedWeight}kg",
                    color = Color(0xFFFFCDD2),
                    fontSize = 13.sp
                )
            }
            
            Checkbox(
                checked = exercise.completed,
                onCheckedChange = onCheckChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFD50000),
                    uncheckedColor = Color.White
                )
            )
        }
    }
}
