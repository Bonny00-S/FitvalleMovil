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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    sessionId: String,
    navController: NavController,
    routineId: String,
    editViewModel: EditSessionViewModel,
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

    // editViewModel is provided by the NavHost (shared between screens)

    var exercises by remember { mutableStateOf<List<SessionExercise>>(emptyList()) }
    var originalExercises by remember { mutableStateOf<Map<String, SessionExercise>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var showConfirm by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 🔹 Cargar ejercicios desde Firebase
    LaunchedEffect(sessionId) {
        val dao = SessionDao()
        try {
            exercises = dao.getSessionExercises(sessionId)
            // Guardar snapshot de los valores originales asignados por el coach
            originalExercises = exercises.associateBy { it.exerciseId }
        } catch (e: Exception) {
            Log.e("ActiveSession", "Error al cargar ejercicios: ${e.message}")
        } finally {
            loading = false
        }
    }

    // 🔹 Monitorear cambios en el ViewModel usando StateFlow
    val editedExercisesState by editViewModel.editedExercises.collectAsStateWithLifecycle()
    
    LaunchedEffect(editedExercisesState) {
        if (editedExercisesState.isNotEmpty()) {
            Log.d("ActiveSession", "📝 Cambios detectados en ViewModel: ${editedExercisesState.size} ejercicios")
            Log.d("ActiveSession", "🔎 edited keys=${editedExercisesState.keys}")
            Log.d("ActiveSession", "🔎 original keys=${originalExercises.keys}")
            exercises = exercises.map { exercise ->
                val edited = editedExercisesState[exercise.exerciseId]
                if (edited != null) {
                    Log.d("ActiveSession", "✅ Aplicando cambios a: ${exercise.exerciseName}")
                    Log.d("ActiveSession", "   Antes: Sets=${exercise.sets}, Reps=${exercise.reps}, Weight=${exercise.weight}")
                    Log.d("ActiveSession", "   Después: Sets=${edited.sets}, Reps=${edited.reps}, Weight=${edited.weight}")
                    // ✅ Preservar estado de 'completed' del original
                    edited.copy(completed = exercise.completed)
                } else {
                    exercise
                }
            }
            editViewModel.clearEdited()
        }
    }

    // 🔹 También comprobar si el detalle escribió directamente en savedStateHandle
    // Esto cubre casos donde la sincronización por ViewModel no fue recibida a tiempo.
    val currentEntry = navController.currentBackStackEntry
    val savedHandle = currentEntry?.savedStateHandle

    DisposableEffect(currentEntry) {
        val lifecycle = currentEntry?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                try {
                    val editedFromDetail = savedHandle?.get<SessionExercise>("editedExercise")
                    if (editedFromDetail != null) {
                        Log.d("ActiveSession", "🔁 Aplicando editedExercise desde savedStateHandle: ${editedFromDetail.exerciseName}")
                        exercises = exercises.map { ex ->
                            if (ex.exerciseId == editedFromDetail.exerciseId) {
                                // preservar 'completed' y 'sessionId'
                                editedFromDetail.copy(completed = ex.completed, sessionId = ex.sessionId)
                            } else ex
                        }
                        // limpiar la entrada para evitar re-aplicaciones
                        savedHandle.remove<SessionExercise>("editedExercise")
                    }
                } catch (e: Exception) {
                    Log.w("ActiveSession", "No pude leer editedExercise desde savedStateHandle: ${e.message}")
                }
            }
        }

        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
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
                            // Preparar la lista de ejercicios a guardar: incluir los completados
                            // y también aquellos cuyos parámetros fueron editados respecto al original
                            val exercisesToSave = exercises.filter { ex ->
                                val original = originalExercises[ex.exerciseId]
                                val changed = if (original != null) {
                                    original.sets != ex.sets || original.reps != ex.reps ||
                                            original.weight != ex.weight || original.speed != ex.speed ||
                                            original.duration != ex.duration
                                } else false
                                ex.completed || changed
                            }

                            if (exercisesToSave.isEmpty()) {
                                snackbarHostState.showSnackbar(
                                    "Debes completar o editar al menos un ejercicio antes de terminar."
                                )
                                return@launch
                            }

                            val dao = SessionDao()
                            val customerId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                            Log.d("ActiveSession", "📋 Antes de preparar finalExercisesToSave:")
                            Log.d("ActiveSession", "   editedExercises keys: ${editedExercisesState.keys}")
                            Log.d("ActiveSession", "   exercisesToSave size: ${exercisesToSave.size}")
                            
                            // Prefer edited values from the ViewModel when available
                            val finalExercisesToSave = exercisesToSave.map { ex ->
                                val edited = editViewModel.getEditedExercise(ex.exerciseId)
                                if (edited != null) {
                                    Log.d("ActiveSession", "💾 Using edited value for save: ${edited.exerciseName} Sets=${edited.sets}, Reps=${edited.reps}, Weight=${edited.weight}, Speed=${edited.speed}, Duration=${edited.duration}")
                                    // preserve completed flag from current ex
                                    edited.copy(completed = ex.completed, sessionId = ex.sessionId)
                                } else {
                                    Log.d("ActiveSession", "⚠️ NO edited value for ${ex.exerciseName}, using original: Sets=${ex.sets}, Reps=${ex.reps}")
                                    ex
                                }
                            }

                            Log.d("ActiveSession", "✅ finalExercisesToSave preparado con ${finalExercisesToSave.size} ejercicios")
                            finalExercisesToSave.forEachIndexed { idx, ex ->
                                Log.d("ActiveSession", "   [$idx] ${ex.exerciseName}: Sets=${ex.sets}, Reps=${ex.reps}, Weight=${ex.weight}, Speed=${ex.speed}, Duration=${ex.duration}")
                            }

                            val success = dao.saveCompletedSession(
                                customerId = customerId,
                                routineId = routineId,
                                sessionId = sessionId,
                                exercisesDone = finalExercisesToSave
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                    ) {
                        Text("No", color = Color.White)
                    }
                }
            }
        }
    }
}