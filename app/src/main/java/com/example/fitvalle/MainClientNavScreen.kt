package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitvalle.ui.screens.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainClientNavScreen(rootNavController: NavHostController) {
    val navController = rememberNavController()

    val fondoPrincipal = Color(0xFF0D1525)
    val fondoBarra = Color(0xFF182235)
    val primario = Color(0xFFB1163A)
    val textoPrincipal = Color.White
    val textoSecundario = Color(0xFFAAB2C5)

    Scaffold(
        bottomBar = { BottomNavBar(navController, fondoBarra, primario) },
        containerColor = fondoPrincipal
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "entrenamiento",
            modifier = Modifier
                .padding(innerPadding)
                .background(fondoPrincipal)
        ) {
            composable("perfil") {
                ProfileScreen(
                    navController,
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        rootNavController.navigate("login") {
                            popUpTo("mainClientNav") { inclusive = true }
                        }
                    }
                )
            }
            composable("editAvatar") { AvatarEditScreen(navController) }
            composable("historial") { HistoryScreen(onBack = { navController.popBackStack() }) }
            composable("entrenamiento") { TrainingScreen(navController) }
            composable("ejercicios") { ExercisesScreen(navController) }
            composable("crearPlantilla") { CreateTemplateScreen(navController) }
            composable("exerciseDetail/{exerciseId}") { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                ExerciseDetailScreen(
                    exerciseId = exerciseId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("selectExercises") { SelectExercisesScreen(navController) }
            composable("workoutDetail/{routineId}/{coachName}") { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
                val coachName = backStackEntry.arguments?.getString("coachName")
                WorkoutDetailScreen(routineId, coachName, navController)
            }
            composable("routineSessions/{routineId}") { backStack ->
                val routineId = backStack.arguments?.getString("routineId") ?: ""
                RoutineSessionsScreen(routineId, navController)
            }
            composable("activeSession/{sessionId}/{routineId}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
                ActiveSessionScreen(
                    sessionId = sessionId,
                    routineId = routineId,
                    navController = navController,
                    onFinish = {
                        navController.navigate("entrenamiento") {
                            popUpTo("entrenamiento") { inclusive = true }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController, fondoBarra: Color, primario: Color) {
    val items = listOf(
        BottomNavItem("perfil", Icons.Default.Person, "Perfil"),
        BottomNavItem("historial", Icons.Default.List, "Historial"),
        BottomNavItem("entrenamiento", Icons.Default.Add, "Entrenar"),
        BottomNavItem("ejercicios", Icons.Default.AddCircle, "Ejercicios")
    )

    NavigationBar(
        containerColor = fondoBarra,
        tonalElevation = 6.dp
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) primario else Color.White
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (selected) primario else Color.White,
                        fontSize = 12.sp
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}

data class BottomNavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)


// 🔹 Pantallas base (placeholder, luego se reemplazan por los diseños del Figma)

//@Composable
//fun PerfilScreen() {
//    Box(
//        Modifier
//            .fillMaxSize()
//            .background(Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))),
//        contentAlignment = Alignment.Center
//    ) {
//        Text("Pantalla de Perfil", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
//    }
//}
//
//@Composable
//fun HistorialScreen() {
//    Box(
//        Modifier
//            .fillMaxSize()
//            .background(Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))),
//        contentAlignment = Alignment.Center
//    ) {
//        Text("Pantalla de Historial", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
//    }
//}
//
//@Composable
//fun EntrenamientoScreen() {
//    Box(
//        Modifier
//            .fillMaxSize()
//            .background(Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))),
//        contentAlignment = Alignment.Center
//    ) {
//        Text("Pantalla de Entrenamiento", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
//    }
//}
//
//@Composable
//fun EjerciciosScreen() {
//    Box(
//        Modifier
//            .fillMaxSize()
//            .background(Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))),
//        contentAlignment = Alignment.Center
//    ) {
//        Text("Pantalla de Ejercicios", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
//    }
//}
