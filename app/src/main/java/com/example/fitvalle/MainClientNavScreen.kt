package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
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
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainClientNavScreen(rootNavController: NavHostController) {
    val navController = rememberNavController()

    val fondoPrincipal = Color(0xFF0D1525)
    val fondoBarra = Color(0xFF182235)
    val primario = Color(0xFFB1163A)

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

            // 👤 PERFIL
            composable("perfil") {
                ProfileScreen(
                    navController = rootNavController, // ✅ usa rootNavController aquí
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        rootNavController.navigate("login") {
                            popUpTo("mainClient") { inclusive = true }
                        }
                    }
                )
            }

            // 🧍 EDITAR AVATAR
            composable("editAvatar") {
                AvatarEditScreen(rootNavController)
            }

            // 🧑‍🏫 ENTRENADORES
            composable("coaches") {
                CoachesScreen(rootNavController)
            }

            // 📜 HISTORIAL
            composable("historial") {
                HistoryScreen(navController)
            }

            // 📋 DETALLE DE SESIÓN COMPLETADA
            composable("completedSessionDetail/{sessionId}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                CompletedSessionDetailScreen(navController, sessionId)
            }

            // 🏋️ ENTRENAMIENTO
            composable("entrenamiento") {
                TrainingScreen(navController = rootNavController) // ✅ usa rootNavController
            }

            // 🧩 EJERCICIOS
            composable("ejercicios") {
                ExercisesScreen(rootNavController)
            }

            // ➕ CREAR PLANTILLA
            composable("crearPlantilla") {
                CreateTemplateScreen(rootNavController)
            }

            // 🔍 DETALLE DE EJERCICIO
            composable("exerciseDetail/{exerciseId}") { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                ExerciseDetailScreen(navController = rootNavController, exerciseId = exerciseId)
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController, fondoBarra: Color, primario: Color) {
    val items = listOf(
        BottomNavItem("perfil", Icons.Default.Person, "Perfil"),
        BottomNavItem("historial", Icons.AutoMirrored.Filled.List, "Historial"),
        BottomNavItem("entrenamiento", Icons.Default.Add, "Entrenar"),
        BottomNavItem("ejercicios", Icons.Default.AddCircle, "Ejercicios")
    )

    NavigationBar(containerColor = fondoBarra, tonalElevation = 6.dp) {
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

data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
