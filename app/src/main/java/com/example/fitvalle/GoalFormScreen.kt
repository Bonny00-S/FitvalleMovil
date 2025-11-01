package com.example.fitvalle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalFormScreen(navController: NavController, viewModel: UserFormViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gainOptions = listOf("0.25 kg/semana", "0.5 kg/semana", "0.75 kg/semana")
    var expanded by remember { mutableStateOf(false) }

    // --- Calcular si el objetivo es subir o bajar ---
    val currentWeight = viewModel.weight.value.replace(",", ".").toFloatOrNull()
    val goalWeight = viewModel.goalWeight.value.replace(",", ".").toFloatOrNull()
    val isGain = if (currentWeight != null && goalWeight != null) {
        goalWeight > currentWeight
    } else null

    val speedLabel = when (isGain) {
        true -> "Velocidad de subida de peso"
        false -> "Velocidad de bajada de peso"
        else -> "Velocidad (elige según tu objetivo)"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF8E0E00), Color(0xFF1F1C18))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "¿Cuáles son tus objetivos?",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(20.dp))

                // Meta de peso
                Image(
                    painter = painterResource(id = R.drawable.goal),
                    contentDescription = "Meta de peso",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.goalWeight.value,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                            viewModel.goalWeight.value = newValue.replace(',', '.')
                        }
                    },
                    label = { Text("¿Cuánto quieres llegar a pesar? (kg)") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // Nivel de actividad
                Image(
                    painter = painterResource(id = R.drawable.activity),
                    contentDescription = "Actividad",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))

                val activityLevels = listOf(
                    "Sedentario",
                    "Ligeramente activo",
                    "Moderadamente activo",
                    "Muy activo"
                )

                var expandedActivity by remember { mutableStateOf(false) }
                var expandedSpeed by remember { mutableStateOf(false) }
                val selectedLevel = viewModel.activityLevel.value

                ExposedDropdownMenuBox(
                    expanded = expandedActivity,
                    onExpandedChange = { expandedActivity = !expandedActivity },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedLevel.ifEmpty { "Selecciona tu nivel" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nivel de actividad") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD5CDCD),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFFB2BBBA)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedActivity,
                        onDismissRequest = { expandedActivity = false }
                    ) {
                        activityLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    viewModel.activityLevel.value = level
                                    expandedActivity = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))

                // Velocidad (según objetivo)
                Image(
                    painter = painterResource(id = R.drawable.goal),
                    contentDescription = "Velocidad",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedSpeed,
                    onExpandedChange = { expandedSpeed = !expandedSpeed }
                ) {
                    OutlinedTextField(
                        value = viewModel.gainSpeed.value,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(speedLabel) }, // 👈 texto dinámico
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSpeed,
                        onDismissRequest = { expandedSpeed = false }
                    ) {
                        gainOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.gainSpeed.value = option
                                    expandedSpeed = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(30.dp))

                // Botones de navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { navController.popBackStack() }, // volver atrás
                        colors = ButtonDefaults.buttonColors(Color.Gray)
                    ) {
                        Text("ATRÁS", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val error = when {
                                viewModel.goalWeight.value.isBlank() -> "Ingresa tu meta de peso"
                                goalWeight == null -> "Ingresa un peso válido"
                                goalWeight < 50 || goalWeight > 150 -> "La meta de peso debe estar entre 50 y 150 kg"
                                viewModel.activityLevel.value.isBlank() -> "Selecciona tu nivel de actividad"
                                viewModel.gainSpeed.value.isBlank() -> "Selecciona la velocidad de tu objetivo"
                                else -> null
                            }
                            if (error != null) {
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            } else {
                                navController.navigate("birthdate")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFFD50000))
                    ) {
                        Text("CONTINUAR", color = Color.White)
                    }
                }
            }
        }
    }
}
