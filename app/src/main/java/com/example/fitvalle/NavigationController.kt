package com.example.fitvalle

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavigationController() {
    val navController: NavHostController = rememberNavController()
    val formViewModel: UserFormViewModel = viewModel() // ✅ se crea una sola vez y se comparte

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("registro") { RegisterScreen(navController) }
        composable("welcome") { WelcomeScreen(navController) }
        composable("cuestionario1") { InfoFormScreen(navController, formViewModel) }
        composable("goalform") { GoalFormScreen(navController, formViewModel) }
        composable("birthdate") { BirthdateScreen(navController, formViewModel) }
        composable("trainingChoice") { TrainingChoiceScreen(navController, formViewModel) }
        composable("trainingPreferences") { TrainingPreferencesScreen(navController, formViewModel) }
        composable("mainClient") { MainClientScreen(navController) }
    }
}


