package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.google.firebase.database.*
import java.text.Normalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(navController: NavController) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))

    val db = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
    val exercisesRef = db.getReference("exercise")
    val musclesRef = db.getReference("targetMuscles")
    val typesRef = db.getReference("exerciseTypes")

    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var muscleMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var typeMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

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

    // Cargar datos
    LaunchedEffect(Unit) {
        // Músculos
        musclesRef.get().addOnSuccessListener { snap ->
            val map = mutableMapOf<String, String>()
            snap.children.forEach {
                val id = it.child("id").getValue(String::class.java)
                val name = it.child("name").getValue(String::class.java)
                if (id != null && name != null) map[id] = name
            }
            muscleMap = map
        }

        // Tipos
        typesRef.get().addOnSuccessListener { snap ->
            val map = mutableMapOf<String, String>()
            snap.children.forEach {
                val id = it.child("id").getValue(String::class.java)
                val name = it.child("name").getValue(String::class.java)
                if (id != null && name != null) map[id] = name
            }
            typeMap = map
        }

        // Ejercicios
        exercisesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Exercise::class.java) }
                exercises = list.sortedBy { it.name }
                loading = false
            }

            override fun onCancelled(error: DatabaseError) {
                loading = false
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Ejercicios", color = Color.White, fontWeight = FontWeight.Bold)
                },
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
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                        },
                        placeholder = { Text("Buscar ejercicio...", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33FFFFFF), shape = MaterialTheme.shapes.medium),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val searchWords = cleanQuery(searchQuery.text)

                    val filtered = if (searchWords.isEmpty()) {
                        exercises
                    } else {
                        exercises.filter { ex ->
                            val muscleName = normalize(muscleMap[ex.muscleID] ?: "")
                            val typeName = normalize(typeMap[ex.typeID] ?: "")
                            val exerciseName = normalize(ex.name)

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
                        val grouped = filtered.groupBy { it.name.first().uppercaseChar() }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            grouped.forEach { (letter, group) ->
                                item {
                                    Text(
                                        letter.toString(),
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                                items(group) { ex ->
                                    ExerciseItem(
                                        exercise = ex,
                                        muscleName = muscleMap[ex.muscleID] ?: "Sin grupo",
                                        onClick = {
                                            navController.navigate("exerciseDetail/${ex.id}")
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
}
@Composable
fun ExerciseItem(exercise: Exercise, muscleName: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(exercise.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(muscleName, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}

