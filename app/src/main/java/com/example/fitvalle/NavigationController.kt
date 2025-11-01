package com.example.fitvalle

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitvalle.ui.screens.*

@Composable
fun NavigationController() {
    val navController: NavHostController = rememberNavController()
    val formViewModel: UserFormViewModel = viewModel()

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
        composable("mainClientNav") { MainClientNavScreen(navController) }

    }
}
