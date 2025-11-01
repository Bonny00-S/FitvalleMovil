package com.example.fitvalle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemplateScreen(
    navController: NavController,
    onSave: (String, List<String>) -> Unit = { _, _ -> }
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF8E0E00), Color(0xFF1F1C18))
    )

    var plantillaName by remember { mutableStateOf(TextFieldValue("Nueva plantilla")) }

    // ✅ Escuchar los ejercicios seleccionados desde SelectExercisesScreen
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val selectedExercisesFlow =
        navBackStackEntry?.savedStateHandle?.getStateFlow<List<String>>("selectedExercises", emptyList())

    val ejercicios by selectedExercisesFlow?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva plantilla de entrenamiento",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onSave(plantillaName.text, ejercicios)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Guardar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                // 🏷️ Nombre de la plantilla editable
                OutlinedTextField(
                    value = plantillaName,
                    onValueChange = { plantillaName = it },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    label = { Text("Nombre de la plantilla", color = Color(0xFFFFCDD2)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFCDD2),
                        unfocusedBorderColor = Color(0xFFB71C1C),
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 🔴 Botón para añadir ejercicios
                Button(
                    onClick = { navController.navigate("selectExercises") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "AÑADIR EJERCICIO",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 📋 Lista de ejercicios agregados
                if (ejercicios.isEmpty()) {
                    Text(
                        text = "Aún no has añadido ejercicios.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(ejercicios.size) { index ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = ejercicios[index],
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
