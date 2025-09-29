package com.example.fitvalle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPreferencesScreen(navController: NavController, viewModel: UserFormViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estados locales
    var exerciseExpanded by remember { mutableStateOf(false) }
    var experienceExpanded by remember { mutableStateOf(false) }
    var daysExpanded by remember { mutableStateOf(false) }

    val exerciseOptions = listOf("Musculación", "Aeróbicos", "Calisténicos")
    val experienceOptions = listOf("Principiante", "Experimentado", "Avanzado")
    val daysOptions = (2..6).map { "$it días/semana" }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("¿Como quieres entrenar?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(20.dp))

                // Tipo de ejercicio
                Image(
                    painter = painterResource(id = R.drawable.exercise),
                    contentDescription = "Ejercicio",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = exerciseExpanded,
                    onExpandedChange = { exerciseExpanded = !exerciseExpanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.exerciseType.value,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("¿Qué tipo de ejercicios prefieres?") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exerciseExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = exerciseExpanded,
                        onDismissRequest = { exerciseExpanded = false }
                    ) {
                        exerciseOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.exerciseType.value = option
                                    exerciseExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Experiencia
                Image(
                    painter = painterResource(id = R.drawable.experience),
                    contentDescription = "Experiencia",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = experienceExpanded,
                    onExpandedChange = { experienceExpanded = !experienceExpanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.experienceLevel.value,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("¿Cuál es tu experiencia?") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(experienceExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = experienceExpanded,
                        onDismissRequest = { experienceExpanded = false }
                    ) {
                        experienceOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.experienceLevel.value = option
                                    experienceExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Días por semana
                Image(
                    painter = painterResource(id = R.drawable.calendar),
                    contentDescription = "Días",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = daysExpanded,
                    onExpandedChange = { daysExpanded = !daysExpanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.trainingDays.value,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("¿Cuántos días quieres entrenar?") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(daysExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = daysExpanded,
                        onDismissRequest = { daysExpanded = false }
                    ) {
                        daysOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.trainingDays.value = option
                                    daysExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = {
                        val error = when {
                            viewModel.exerciseType.value.isBlank() -> "Selecciona el tipo de ejercicio"
                            viewModel.experienceLevel.value.isBlank() -> "Selecciona tu experiencia"
                            viewModel.trainingDays.value.isBlank() -> "Selecciona los días de entrenamiento"
                            else -> null
                        }
                        if (error != null) {
                            scope.launch { snackbarHostState.showSnackbar(error) }
                        } else {
                            // Construir descripción para el Request
                            val description = """
                                Tipo de ejercicio: ${viewModel.exerciseType.value}
                                Experiencia: ${viewModel.experienceLevel.value}
                                Días por semana: ${viewModel.trainingDays.value}
                                Velocidad de progreso: ${viewModel.gainSpeed.value}
                            """.trimIndent()

                            val request = Request(
                                id = UUID.randomUUID().toString(),
                                customerId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                description = description,
                                state = "pending"
                            )

                            val requestDao = RequestDao()
                            requestDao.saveRequest(request) { success, msg ->
                                if (success) {
                                    navController.navigate("mainClient")
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFFD50000)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CONTINUAR", color = Color.White)
                }
            }
        }
    }
}
