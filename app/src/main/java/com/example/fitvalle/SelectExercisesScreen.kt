package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.google.firebase.database.FirebaseDatabase
import java.text.Normalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectExercisesScreen(navController: NavController) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var selected by remember { mutableStateOf(setOf<Exercise>()) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    // Recuperar nombre de plantilla desde el backstack previo
    val templateName = navController.previousBackStackEntry?.savedStateHandle?.get<String>("templateName") ?: ""

    // Mapas para músculos y tipos
    var muscleMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var typeMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val db = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
    val musclesDb = db.getReference("targetMuscles")
    val typesDb = db.getReference("exerciseTypes")

    // 🔥 Cargar ejercicios desde DAO + cargar nombres de músculos y tipos
    LaunchedEffect(Unit) {
        ExerciseDao().getAllExercises { list ->
            exercises = list.map { it.first } // Ignoramos tipo y músculo en el DAO
            loading = false
        }

        musclesDb.get().addOnSuccessListener { snap ->
            val map = mutableMapOf<String, String>()
            snap.children.forEach {
                val id = it.child("id").getValue(String::class.java)
                val name = it.child("name").getValue(String::class.java)
                if (id != null && name != null) map[id] = name
            }
            muscleMap = map
        }

        typesDb.get().addOnSuccessListener { snap ->
            val map = mutableMapOf<String, String>()
            snap.children.forEach {
                val id = it.child("id").getValue(String::class.java)
                val name = it.child("name").getValue(String::class.java)
                if (id != null && name != null) map[id] = name
            }
            typeMap = map
        }
    }

    // 🧠 Stopwords + funciones de limpieza
    val stopwords = listOf(
        "de", "la", "el", "en", "y", "para", "del", "los", "las", "con", "por", "un", "una", "al", "lo"
    )

    fun normalize(text: String): String {
        return Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    fun cleanQuery(text: String): List<String> {
        return normalize(text)
            .split(" ")
            .filter { it.isNotBlank() && it !in stopwords }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar ejercicios", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val selectedExerciseNames = selected.map { it.name }

                        if (selectedExerciseNames.isNotEmpty()) {
                            // ✅ Guardamos los ejercicios seleccionados en el backstack actual
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedExercises", selectedExerciseNames)

                            // ✅ Navegamos hacia la pantalla de personalización con el nombre de plantilla
                            if (templateName.isNotBlank()) {
                                val encodedName = java.net.URLEncoder.encode(templateName, "UTF-8")
                                navController.navigate("personalizeTemplateExercises/$encodedName")
                            } else {
                                navController.navigate("personalizeTemplateExercises/Mi%20plantilla")
                            }
                        } else {
                            // Si no seleccionaron nada, mostrar advertencia (opcional)
                            println("⚠️ No se seleccionaron ejercicios")
                        }
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Continuar", tint = Color.White)
                    }

                }
                ,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(16.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 🔍 Campo de búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar por nombre, músculo o tipo...", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🧠 Filtro avanzado
                    val searchWords = cleanQuery(searchQuery.text)
                    val filtered = if (searchWords.isEmpty()) {
                        exercises
                    } else {
                        exercises.filter { ex ->
                            val exerciseName = normalize(ex.name)
                            val muscleName = normalize(muscleMap[ex.muscleID] ?: "")
                            val typeName = normalize(typeMap[ex.typeID] ?: "")
                            searchWords.any { word ->
                                exerciseName.contains(word) ||
                                        muscleName.contains(word) ||
                                        typeName.contains(word)
                            }
                        }
                    }

                    if (filtered.isEmpty()) {
                        Text(
                            "No se encontraron ejercicios.",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(filtered) { ex ->
                                SelectableExerciseCard(
                                    exercise = ex,
                                    selected = selected.contains(ex),
                                    onSelect = {
                                        selected = if (selected.contains(ex)) {
                                            selected - ex
                                        } else {
                                            selected + ex
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableExerciseCard(exercise: Exercise, selected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF4CAF50) else Color(0xFF2E1A1A)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(exercise.name, color = Color.White, fontWeight = FontWeight.Medium)
            if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
        }
    }
}
