package com.example.fitvalle

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationController(navController: NavHostController) {

    // 🧠 ViewModel compartido entre los formularios
    val formViewModel: UserFormViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login" // 👈 Cambia a "mainClient" si quieres saltar el login
    ) {
        // 🟢 LOGIN
        composable("login") { LoginScreen(navController) }

        // 🟣 REGISTRO
        composable("register") { RegisterScreen(navController) }

        // 🟥 PANTALLA DE BIENVENIDA (inicio del flujo de formulario)
        composable("welcome") { WelcomeScreen(navController) }

        // 🧾 FORMULARIO 1: Datos iniciales (género, altura, peso)
        composable("cuestionario1") { InfoFormScreen(navController, formViewModel) }

        // 🎯 FORMULARIO 2: Objetivos del usuario
        composable("goalform") { GoalFormScreen(navController, formViewModel) }

        // 🎂 FORMULARIO 3: Fecha de nacimiento
        composable("birthdate") { BirthdateScreen(navController, formViewModel) }

        // 💪 FORMULARIO 4: Tipo de entrenamiento preferido
        composable("trainingChoice") { TrainingChoiceScreen(navController, formViewModel) }

        // 🏋️ FORMULARIO 5: Preferencias específicas
        composable("trainingPreferences") { TrainingPreferencesScreen(navController, formViewModel) }

        // 🧭 MENÚ PRINCIPAL CLIENTE (pasa rootNavController)
        composable("mainClient") {
            MainClientNavScreen(rootNavController = navController)
        }

        // 👤 PERFIL (fuera del bottom nav)
        composable("profile") {
            ProfileScreen(
                navController = navController,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("mainClient") { inclusive = true }
                    }
                }
            )
        }

        // 🏋️ ENTRENAMIENTO
        composable("training") { TrainingScreen(navController) }

        // 🏋️ DETALLE DE EJERCICIO
        composable(
            route = "exerciseDetail/{exerciseId}",
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            ExerciseDetailScreen(navController = navController, exerciseId = exerciseId)
        }

        // ➕ CREAR PLANTILLA
        composable("crearPlantilla") { CreateTemplateScreen(navController) }

        // 📋 DETALLE DE PLANTILLA
        composable(
            route = "templateDetail/{templateId}",
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            TemplateDetailScreen(navController = navController, templateId = templateId)
        }

        // 🏋️ SELECCIONAR EJERCICIOS
        composable("selectExercises") {
            SelectExercisesScreen(navController)
        }

        // ✏️ EDITAR PLANTILLA
        composable(
            route = "editTemplate/{templateId}",
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            EditTemplateScreen(navController = navController, templateId = templateId)
        }

        // 🧩 SESIONES DE RUTINA (para rutinas asignadas por coach)
        composable(
            route = "routineSessions/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
            RoutineSessionsScreen(routineId = routineId, navController = navController)
        }

        // 🧩 SESIÓN ACTIVA (ejercicios dentro de la sesión)
        composable(
            route = "activeSession/{sessionId}/{routineId}",
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("routineId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
            ActiveSessionScreen(navController = navController, sessionId = sessionId, routineId = routineId)
        }

        // ✳️ EDITAR AVATAR
        composable("editAvatar") { AvatarEditScreen(navController) }

        // 📆 HISTORIAL
        composable("history") { HistoryScreen(navController = navController) }

        // 🎯 FORMULARIO DE OBJETIVOS (de versión anterior, mantenido por compatibilidad)

        composable("goalform") { GoalFormScreen(navController, formViewModel) }

        // 🧑‍🏫 LISTA DE ENTRENADORES
        composable("coaches") { CoachesScreen(navController) }

        // 🏋️ SESIÓN DE ENTRENAMIENTO DESDE PLANTILLA
        composable("training/{templateId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("templateId") ?: ""
            TrainingSessionScreen(navController, id)
        }

        // ⚙️ CONFIGURACIÓN DE EJERCICIOS SELECCIONADOS
        composable("exerciseSetup") { backStackEntry ->
            val selectedExercises =
                backStackEntry.savedStateHandle.get<List<String>>("selectedExercises") ?: emptyList()
            ExerciseSetupScreen(navController, selectedExercises)
        }

        //  EJERCIOS DE ENTRENAMIENTO DE MI COACH DETALLADO
        composable("exerciseSessionDetail") {
            val exercise = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<SessionExercise>("exerciseDetail")

            if (exercise != null) {
                ExerciseSessionDetailScreen(navController, exercise)
            }
        }

    }
}
