package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemplateScreen(navController: NavController) {
    val fondo = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 🔹 Estados persistentes
    var templateName by rememberSaveable { mutableStateOf("") }
    var exercises by rememberSaveable { mutableStateOf(emptyList<String>()) }

    // 🔹 Recuperar ejercicios seleccionados desde SelectExercisesScreen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val savedExercises =
        navBackStackEntry?.savedStateHandle
            ?.getStateFlow<List<String>>("selectedExercises", emptyList())
            ?.collectAsState()

    // 🔹 Si llegan nuevos ejercicios, actualiza la lista
    LaunchedEffect(savedExercises?.value) {
        savedExercises?.value?.let {
            if (it.isNotEmpty()) exercises = it
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            Button(
                onClick = {
                    scope.launch {
                        if (templateName.isBlank()) {
                            snackbarHostState.showSnackbar("⚠️ Escribe un nombre para la plantilla")
                            return@launch
                        }
                        if (exercises.isEmpty()) {
                            snackbarHostState.showSnackbar("⚠️ Añade al menos un ejercicio")
                            return@launch
                        }
                        // Navegar a personalización pasando el nombre en la ruta
                        val encodedName = java.net.URLEncoder.encode(templateName, "UTF-8")
                        navController.navigate("personalizeTemplateExercises/$encodedName")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC41C3B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Guardar cambios",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // 🏷️ Campo editable para el nombre
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
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
                    onClick = {
                        if (templateName.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("⚠️ Escribe un nombre para la plantilla primero")
                            }
                        } else {
                            // Pasar el nombre de plantilla antes de navegar
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("templateName", templateName)
                            navController.navigate("selectExercises")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "AÑADIR EJERCICIOS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 📋 Lista de ejercicios agregados
                if (exercises.isEmpty()) {
                    Text(
                        text = "Aún no has añadido ejercicios.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(exercises) { exercise ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = exercise,
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


