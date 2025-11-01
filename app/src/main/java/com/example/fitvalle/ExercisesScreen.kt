package com.example.fitvalle.ui.screens

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

@Composable
fun ExercisesScreen(navController: NavController) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))

    var searchQuery by remember { mutableStateOf("") }
    var ascending by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(true) }

    var exercises by remember { mutableStateOf<List<Triple<Exercise, ExerciseType?, TargetMuscle?>>>(emptyList()) }

    LaunchedEffect(Unit) {
        ExerciseDao().getAllExercises { list ->
            exercises = list
            loading = false
        }
    }

    val filtered = exercises.filter { (ex, type, muscle) ->
        ex.name.contains(searchQuery, true) ||
                (type?.name?.contains(searchQuery, true) ?: false) ||
                (muscle?.name?.contains(searchQuery, true) ?: false)
    }

    val grouped = filtered.groupBy { it.first.name.firstOrNull()?.uppercaseChar() ?: '#' }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ejercicios",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { ascending = !ascending }) {
                    Icon(
                        imageVector = if (ascending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Cambiar orden",
                        tint = Color.White
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar ejercicio...", color = Color.LightGray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0x33FFFFFF),
                    focusedContainerColor = Color(0x55FFFFFF),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) }
            )

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    grouped.toSortedMap().forEach { (letra, lista) ->
                        item {
                            Text(
                                text = letra.toString(),
                                color = Color(0xFFFFCDD2),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(lista) { (ex, _, muscle) ->
                            EjercicioCard(
                                nombre = ex.name,
                                tipo = muscle?.name ?: "Sin músculo",
                                onClick = { navController.navigate("exerciseDetail/${ex.id}") }
                            )
                        }
                    }

                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                "No se encontraron ejercicios",
                                color = Color.LightGray,
                                modifier = Modifier.padding(top = 50.dp),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EjercicioCard(
    nombre: String,
    tipo: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(nombre, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(tipo, color = Color(0xFFFFCDD2), fontSize = 14.sp)
        }
    }
}
