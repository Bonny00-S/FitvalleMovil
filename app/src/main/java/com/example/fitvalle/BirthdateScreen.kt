package com.example.fitvalle

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BirthdateScreen(navController: NavController, viewModel: UserFormViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Estado para mostrar/ocultar el DatePickerDialog
    var showDatePicker by remember { mutableStateOf(false) }

    // Estado para mostrar la edad
    var ageDisplay by remember { mutableStateOf("") }

    // DatePicker con validación de fecha máxima (hoy)
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)

            val today = Calendar.getInstance()

            // Validar que no sea una fecha futura
            if (selectedDate.after(today)) {
                scope.launch {
                    snackbarHostState.showSnackbar("La fecha no puede ser futura")
                }
                return@DatePickerDialog
            }

            // Calcular edad
            val age = today.get(Calendar.YEAR) - year -
                    if (today.get(Calendar.DAY_OF_YEAR) < selectedDate.get(Calendar.DAY_OF_YEAR)) 1 else 0

            if (age < 16 || age > 100) {
                scope.launch {
                    snackbarHostState.showSnackbar("La edad debe estar entre 16 y 100 años")
                }
                return@DatePickerDialog
            }

            // Guardar fecha válida
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            viewModel.birthdate.value = formatter.format(selectedDate.time)

            // Mostrar la edad automáticamente
            ageDisplay = "$age años"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        // Bloquear fechas futuras directamente en el calendario
        datePicker.maxDate = calendar.timeInMillis
    }

    if (showDatePicker) {
        datePickerDialog.show()
        showDatePicker = false
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
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "Tu fecha de nacimiento",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.calendar),
                    contentDescription = "Nacimiento",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(Modifier.height(20.dp))

                // Campo de fecha (no editable, solo abre el DatePicker)
                OutlinedTextField(
                    value = viewModel.birthdate.value,
                    onValueChange = {},
                    label = { Text("Selecciona tu fecha de nacimiento") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar),
                                contentDescription = "Abrir calendario",
                                tint = Color.White
                            )
                        }
                    }
                )

                // Mostrar la edad si está disponible
                if (ageDisplay.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Tienes $ageDisplay",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (viewModel.birthdate.value.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Por favor selecciona tu fecha de nacimiento")
                            }
                        } else {
                            navController.navigate("trainingChoice")
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
