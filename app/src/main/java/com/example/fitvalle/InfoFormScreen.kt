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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoFormScreen(navController: NavController, viewModel: UserFormViewModel) {
    val genderOptions = listOf("Hombre", "Mujer", "No Binario", "Otro")
    var expanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
            Column(  horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())) {
                Text(
                    "¡Empecemos! Necesitamos unos datos más para continuar",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))

                // Género
                Image(
                    painter = painterResource(id = R.drawable.gender),
                    contentDescription = "Género",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.gender.value,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecciona tu género") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.gender.value = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Altura
                Image(
                    painter = painterResource(id = R.drawable.height),
                    contentDescription = "Altura",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.height.value,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            viewModel.height.value = newValue
                        }
                    },
                    label = { Text("¿Cuanto mides? (cm)") },
                    suffix = { Text("cm") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // Peso
                Image(
                    painter = painterResource(id = R.drawable.goal),
                    contentDescription = "Peso",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.weight.value,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() || it == ',' }) {
                            viewModel.weight.value = newValue
                        }
                    },
                    label = { Text("¿Cuanto pesas? (kg)") },
                    suffix = { Text("kg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = {
                        val error = when {
                            viewModel.gender.value.isBlank() -> "Por favor selecciona tu género"
                            viewModel.height.value.isBlank() -> "Por favor ingresa tu altura"
                            viewModel.weight.value.isBlank() -> "Por favor ingresa tu peso"
                            viewModel.height.value.toIntOrNull() == null || viewModel.height.value.toInt() !in 100..250 ->
                                "La altura debe estar entre 100 y 250 cm"
                            viewModel.weight.value.replace(",", ".").toFloatOrNull() == null ||
                                    viewModel.weight.value.replace(",", ".").toFloat() !in 30f..300f ->
                                "El peso debe estar entre 30 y 300 kg"
                            else -> null
                        }

                        if (error != null) {
                            scope.launch { snackbarHostState.showSnackbar(error) }
                        } else {
                            navController.navigate("goalform")
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
