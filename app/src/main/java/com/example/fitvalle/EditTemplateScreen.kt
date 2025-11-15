package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.Normalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTemplateScreen(
    navController: NavController,
    templateId: String
) {
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF8E0E00), Color(0xFF1F1C18))
    )

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    val db = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("templates")

    val exercisesDb = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("exercise")

    var template by remember { mutableStateOf<Template?>(null) }
    var newName by remember { mutableStateOf("") }

    // lista de ejercicios dentro de la plantilla
    val exercises = remember { mutableStateListOf<String>() }

    var loading by remember { mutableStateOf(true) }

    // diálogo para agregar ejercicio
    var showAddDialog by remember { mutableStateOf(false) }
    var allExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    // índice seleccionado
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // 1. cargar plantilla
    LaunchedEffect(templateId) {
        if (userId != null) {
            db.child(userId).child(templateId).get().addOnSuccessListener { snap ->
                val data = snap.getValue(Template::class.java)
                template = data
                newName = data?.name ?: ""
                exercises.clear()
                exercises.addAll(data?.exercises ?: emptyList())
                loading = false
            }.addOnFailureListener {
                loading = false
            }
        }
    }

    // 2. cargar ejercicios disponibles
    LaunchedEffect(Unit) {
        exercisesDb.get().addOnSuccessListener { snap ->
            val list = snap.children.mapNotNull { it.getValue(Exercise::class.java) }
            allExercises = list.sortedBy { it.name }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar plantilla", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFB1163A)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (template == null) {
                Text(
                    "No se encontró la plantilla",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre de la plantilla", color = Color.White) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Ejercicios",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    // ✅ lista con scroll
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(exercises) { index, name ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedIndex == index)
                                        Color(0xFF7A1515) else Color(0xFF2E1A1A)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { /* nada */ },
                                        onLongClick = { selectedIndex = index }
                                    )
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}. $name",
                                        color = Color.White
                                    )

                                    Row {
                                        if (selectedIndex == index) {
                                            IconButton(
                                                onClick = {
                                                    if (index > 0) {
                                                        exercises.swap(index, index - 1)
                                                        selectedIndex = index - 1
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.KeyboardArrowUp,
                                                    contentDescription = "Subir",
                                                    tint = Color.White
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (index < exercises.size - 1) {
                                                        exercises.swap(index, index + 1)
                                                        selectedIndex = index + 1
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Bajar",
                                                    tint = Color.White
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                exercises.removeAt(index)
                                                if (selectedIndex == index) selectedIndex = null
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (userId != null) {
                                val updated = template!!.copy(
                                    name = newName,
                                    exercises = exercises.toList()
                                )
                                db.child(userId).child(templateId).setValue(updated)
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB1163A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GUARDAR CAMBIOS", color = Color.White)
                    }
                }
            }
        }
    }

    // 🔻 diálogo para agregar ejercicio (BUSCADOR MEJORADO)
    if (showAddDialog) {
        val musclesDb = FirebaseDatabase
            .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
            .getReference("targetMuscles")
        val typesDb = FirebaseDatabase
            .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
            .getReference("exerciseTypes")

        var muscleMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
        var typeMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

        LaunchedEffect(Unit) {
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

        val stopwords = listOf(
            "de", "la", "el", "en", "y", "para", "del", "los", "las",
            "con", "por", "un", "una", "al", "lo"
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

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Agregar ejercicio", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar...", color = Color.White) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color.White
                        )
                    )
                    Spacer(Modifier.height(8.dp))

                    val searchWords = cleanQuery(searchQuery)
                    val filtered = if (searchWords.isEmpty()) {
                        allExercises
                    } else {
                        allExercises.filter { ex ->
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

                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        itemsIndexed(filtered) { _, ex ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!exercises.contains(ex.name)) {
                                            exercises.add(ex.name)
                                        }
                                        showAddDialog = false
                                        searchQuery = ""
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    ex.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Músculo: ${muscleMap[ex.muscleID] ?: "Desconocido"}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                                Text(
                                    "Tipo: ${typeMap[ex.typeID] ?: "Desconocido"}",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF2B1A1A)
        )
    }
}

/**
 * pequeña extensión para intercambiar 2 posiciones
 */
private fun <T> MutableList<T>.swap(from: Int, to: Int) {
    val tmp = this[from]
    this[from] = this[to]
    this[to] = tmp
}
