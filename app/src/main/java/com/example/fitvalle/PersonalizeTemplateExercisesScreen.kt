package com.example.fitvalle

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun PersonalizeTemplateExercisesScreen(
    navController: NavController,
    templateName: String
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Recuperar ejercicios seleccionados desde savedStateHandle
    val selectedExerciseNames =
        navController.previousBackStackEntry?.savedStateHandle?.get<List<String>>("selectedExercises")
            ?: emptyList()

    // Estado mutable para los ejercicios con parámetros
    var templateExercises by remember {
        mutableStateOf(
            selectedExerciseNames.map { name ->
                TemplateExercise(
                    exerciseId = name, // Usar el nombre como ID temporal
                    exerciseName = name,
                    sets = 3,
                    reps = 10,
                    weight = 0,
                    speed = 0,
                    duration = 0
                )
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Personalizar ejercicios",
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
        bottomBar = {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val templateDao = TemplateDao()
                            val success = templateDao.saveTemplate(templateName, templateExercises)
                            if (success) {
                                snackbarHostState.showSnackbar("✅ Plantilla guardada correctamente")
                                // Navegar de vuelta a la pantalla principal
                                navController.navigate("training") {
                                    popUpTo("training") { inclusive = true }
                                }
                            } else {
                                snackbarHostState.showSnackbar("❌ Error al guardar la plantilla")
                            }
                        } catch (e: Exception) {
                            Log.e("PersonalizeTemplate", "Error: ${e.message}", e)
                            snackbarHostState.showSnackbar("❌ Error: ${e.message}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC41C3B))
            ) {
                Text("Guardar plantilla", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Plantilla: $templateName",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(templateExercises) { exercise ->
                    TemplateExerciseCard(
                        exercise = exercise,
                        onUpdate = { updated ->
                            templateExercises = templateExercises.map {
                                if (it.exerciseId == updated.exerciseId) updated else it
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateExerciseCard(
    exercise: TemplateExercise,
    onUpdate: (TemplateExercise) -> Unit
) {
    var sets by remember { mutableStateOf(exercise.sets.toString()) }
    var reps by remember { mutableStateOf(exercise.reps.toString()) }
    var weight by remember { mutableStateOf(exercise.weight.toString()) }
    var speed by remember { mutableStateOf(exercise.speed.toString()) }
    var duration by remember { mutableStateOf(exercise.duration.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                exercise.exerciseName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(12.dp))

            // Fila 1: Series y Reps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseInputField(
                    label = "Series",
                    value = sets,
                    onValueChange = { sets = it },
                    modifier = Modifier.weight(1f)
                )
                ExerciseInputField(
                    label = "Reps",
                    value = reps,
                    onValueChange = { reps = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Fila 2: Peso y Velocidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseInputField(
                    label = "Peso (kg)",
                    value = weight,
                    onValueChange = { weight = it },
                    modifier = Modifier.weight(1f)
                )
                ExerciseInputField(
                    label = "Velocidad",
                    value = speed,
                    onValueChange = { speed = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Fila 3: Duración
            ExerciseInputField(
                label = "Duración (min)",
                value = duration,
                onValueChange = { duration = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Botón de confirmación
            Button(
                onClick = {
                    val updated = exercise.copy(
                        sets = sets.toIntOrNull() ?: exercise.sets,
                        reps = reps.toIntOrNull() ?: exercise.reps,
                        weight = weight.toIntOrNull() ?: exercise.weight,
                        speed = speed.toIntOrNull() ?: exercise.speed,
                        duration = duration.toIntOrNull() ?: exercise.duration
                    )
                    onUpdate(updated)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC41C3B))
            ) {
                Text("Confirmar", color = Color.White)
            }
        }
    }
}

@Composable
fun ExerciseInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, color = Color(0xFFFFCDD2), fontSize = 12.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD12B56),
                unfocusedBorderColor = Color.White,
                cursorColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            singleLine = true
        )
    }
}
