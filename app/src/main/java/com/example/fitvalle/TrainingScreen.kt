package com.example.fitvalle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.example.fitvalle.Routine
import com.example.fitvalle.data.dao.RoutineDao
//import com.example.fitvalle.data.model.Routine
import kotlinx.coroutines.launch



@Composable
fun TrainingScreen(navController: NavController) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    val coroutineScope = rememberCoroutineScope()
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val myTemplates = listOf("Piernas" to "Ab Wheel")
    val exampleTemplates = listOf(
        "Strong 5x5 - Workout B" to "Squat (Barbell), Press, Deadlift",
        "Legs" to "Squat (Barbell), Leg Extension, Leg Raise"
    )
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val dao = RoutineDao()
            routines = dao.getAssignedRoutines()
            loading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Entrenamiento",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 10.dp, bottom = 24.dp)
            )

            // when {
            //   loading -> CircularProgressIndicator(color = Color.White)

            // routines.isEmpty() -> Text(
            //   text = "No tienes rutinas asignadas.",
            // color = Color(0xFFFFCDD2),
            //fontSize = 14.sp
            //)

            //else -> LazyColumn(
            //  modifier = Modifier.fillMaxSize(),
            //verticalArrangement = Arrangement.spacedBy(20.dp)
            //) {
            //  items(routines) { routine ->
            //    TemplateCard(
            //      title = "Rutina ${routine.id.take(6)}",
            //    description = "Entrenador: ${routine.coachId.take(6)} • ${routine.sessions?.size ?: 0} sesiones"
            // ) {
            //   navController.navigate("workoutDetail/${routine.id}")
            //}
            //}
            //}
            //}
            when {
                loading -> CircularProgressIndicator(color = Color.White)

                routines.isEmpty() -> Text(
                    text = "No tienes rutinas asignadas.",
                    color = Color(0xFFFFCDD2),
                    fontSize = 14.sp
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        SectionTitle("Mis Plantillas")
                    }
                    items(myTemplates) { (title, desc) ->
                        TemplateCard(title, desc) {
                            navController.navigate("workoutDetail/$title")
                        }
                    }

                    item {
                        SectionTitle("Rutinas Asignadas")
                    }
                    items(routines) { routine ->
                        TemplateCard(
                            title = "Rutina",
                            description = "Entrenador: ${routine.coachName ?: "Desconocido"} • ${routine.sessions?.size ?: 0} sesiones"
                        ) {
                            navController.navigate("workoutDetail/${routine.id}/${routine.coachName}")
                        }
                    }

                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate("crearPlantilla") },
            containerColor = Color(0xFFD50000),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir plantilla")
        }
    }
}
@Composable fun SectionTitle(title: String) { Text( text = title, color = Color(0xFFFFCDD2), fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp) ) }
@Composable
fun TemplateCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color(0xFFFFCDD2),
                fontSize = 14.sp
            )
        }
    }
}

