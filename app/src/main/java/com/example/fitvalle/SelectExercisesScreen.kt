package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitvalle.Exercise
import com.example.fitvalle.ExerciseDao
import com.example.fitvalle.ExerciseType
import androidx.navigation.NavController
import com.example.fitvalle.TargetMuscle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectExercisesScreen(navController: NavController) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var selected by remember { mutableStateOf(setOf<Exercise>()) }
    var loading by remember { mutableStateOf(true) }

    // Cargar ejercicios desde Firebase
    LaunchedEffect(Unit) {
        ExerciseDao().getAllExercises { list ->
            exercises = list.map { it.first } // Ignoramos tipo y músculo por ahora
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar ejercicios", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Enviar lista seleccionada al CreateTemplateScreen
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedExercises", selected.map { it.name })
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Guardar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(16.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(exercises) { ex ->
                        SelectableExerciseCard(
                            exercise = ex,
                            selected = selected.contains(ex),
                            onSelect = {
                                selected = if (selected.contains(ex)) {
                                    selected - ex
                                } else {
                                    selected + ex
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableExerciseCard(exercise: Exercise, selected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF4CAF50) else Color(0xFF2E1A1A)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(exercise.name, color = Color.White, fontWeight = FontWeight.Medium)
            if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
        }
    }
}