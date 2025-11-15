package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// 🔹 Cambiado 'weight' a 'load' para evitar conflicto con Modifier.weight()
data class ExerciseConfig(
    val name: String,
    var series: String = "",
    var reps: String = "",
    var load: String = "",
    var speed: String = "",
    var duration: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSetupScreen(
    navController: NavController,
    selectedExercises: List<String>
) {
    val fondo = Brush.verticalGradient(listOf(Color(0xFF0D1525), Color(0xFF1F1C18)))
    val rojo = Color(0xFFB1163A)

    var exercises by remember {
        mutableStateOf(selectedExercises.map { ExerciseConfig(it) })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Ejercicios", color = Color.White) },
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
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("configuredExercises", exercises)
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = rojo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "GUARDAR Y CONTINUAR",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(padding)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                itemsIndexed(exercises) { index, ex ->
                    ExerciseSetupCard(
                        index = index + 1,
                        exercise = ex,
                        onChange = { updated ->
                            exercises = exercises.mapIndexed { i, old ->
                                if (i == index) updated else old
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseSetupCard(index: Int, exercise: ExerciseConfig, onChange: (ExerciseConfig) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "$index. ${exercise.name}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            // 🔹 Fila 1: Series y Reps
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ConfigField(
                    label = "Series",
                    value = exercise.series,
                    modifier = Modifier.weight(1f)
                ) { onChange(exercise.copy(series = it)) }

                ConfigField(
                    label = "Reps",
                    value = exercise.reps,
                    modifier = Modifier.weight(1f)
                ) { onChange(exercise.copy(reps = it)) }
            }

            // 🔹 Fila 2: Peso y Velocidad
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ConfigField(
                    label = "Peso",
                    value = exercise.load,
                    modifier = Modifier.weight(1f)
                ) { onChange(exercise.copy(load = it)) }

                ConfigField(
                    label = "Velocidad",
                    value = exercise.speed,
                    modifier = Modifier.weight(1f)
                ) { onChange(exercise.copy(speed = it)) }
            }

            // 🔹 Fila 3: Duración
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ConfigField(
                    label = "Duración",
                    value = exercise.duration,
                    modifier = Modifier.weight(1f)
                ) { onChange(exercise.copy(duration = it)) }
            }
        }
    }
}

@Composable
fun ConfigField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFFFFCDD2)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFFCDD2),
            unfocusedBorderColor = Color(0xFFB71C1C),
            cursorColor = Color.White
        ),
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        modifier = modifier
    )
}
