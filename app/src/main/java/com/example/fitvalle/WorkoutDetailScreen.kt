package com.example.fitvalle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fitvalle.R
import com.example.fitvalle.SessionExercise
import com.example.fitvalle.SessionWithExercises
import com.example.fitvalle.data.dao.RoutineDao
import kotlinx.coroutines.launch



data class WorkoutExercise(
    val sets: Int,
    val reps: Int,
    val name: String,
    val muscle: String,
    val imageRes: Int
)

@Composable
fun WorkoutDetailScreen(
    routineId: String,
    coachName: String?,
    navController: NavController
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    val coroutineScope = rememberCoroutineScope()
    var sessions by remember { mutableStateOf<List<SessionWithExercises>>(emptyList()) }
    var exercises by remember { mutableStateOf<List<com.example.fitvalle.Exercise>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(routineId) {
        coroutineScope.launch {
            val dao = RoutineDao()
            //exercises = dao.getRoutineDetail(routineId)
            sessions = dao.getRoutineDetailBySessions(routineId)
            loading = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text(
                    text = "Rutina creada por ${coachName ?: "Desconocido"}",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    navController.navigate("routineSessions/$routineId")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
            ) {
                Text("INICAR ENTRENAMIENTO", color = Color.White, fontWeight = FontWeight.Bold)
            }

        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                sessions.isEmpty() -> Text(
                    text = "No hay ejercicios en esta rutina.",
                    color = Color(0xFFFFCDD2),
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sessions.forEachIndexed { index, session ->
                        item {
                            Text(
                                text = "Sesión ${index + 1}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(session.exercises) { exercise ->
                            ExerciseCard(exercise) {
                                navController.navigate("exerciseDetail/${exercise.id}")
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: com.example.fitvalle.Exercise, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "${exercise.series}× ${exercise.name}",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = exercise.muscleID,
                color = Color(0xFFFFCDD2),
                fontSize = 14.sp
            )
        }
    }
}

fun sampleExercises(): List<WorkoutExercise> = listOf(
    WorkoutExercise(5, 5, "Squat (Barbell)", "Piernas", R.drawable.height),
    WorkoutExercise(5, 5, "Overhead Press (Barbell)", "Hombros", R.drawable.gender),
    WorkoutExercise(1, 5, "Deadlift (Barbell)", "Espalda", R.drawable.activity)
)
