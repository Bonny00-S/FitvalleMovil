package com.example.fitvalle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSessionDetailScreen(
    navController: NavController,
    exercise: SessionExercise
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF0D1525), Color(0xFF182235)))

    // BASE
    var baseExercise by remember { mutableStateOf<Exercise?>(null) }

    // EXTRA INFO
    var typeName by remember { mutableStateOf<String>("-") }
    var muscleName by remember { mutableStateOf<String>("-") }

    var loading by remember { mutableStateOf(true) }

    // EDITABLES TEMPORALES
    var sets by remember { mutableStateOf(exercise.sets.toString()) }
    var reps by remember { mutableStateOf(exercise.reps.toString()) }
    var weight by remember { mutableStateOf(exercise.weight.toString()) }
    var speed by remember { mutableStateOf(exercise.speed.toString()) }
    var duration by remember { mutableStateOf(exercise.duration.toString()) }

    val db = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .reference

    // ==========================
    // HELPERS
    // ==========================

    fun fetchTypeName(typeId: String) {
        db.child("exerciseTypes").child(typeId).child("name").get()
            .addOnSuccessListener {
                val name = it.getValue(String::class.java)
                if (!name.isNullOrBlank()) {
                    typeName = name
                } else {
                    db.child("types").child(typeId).child("name").get()
                        .addOnSuccessListener { snap2 ->
                            typeName = snap2.getValue(String::class.java) ?: "-"
                        }
                }
            }
    }

    fun fetchMuscleName(muscleId: String) {
        db.child("targetMuscles").child(muscleId).child("name").get()
            .addOnSuccessListener {
                muscleName = it.getValue(String::class.java) ?: "-"
            }
    }

    // ==========================
    // LOAD BASE EXERCISE
    // ==========================

    LaunchedEffect(exercise.exerciseId) {
        try {
            val snap = db.child("exercise").child(exercise.exerciseId ?: "").get().await()
            val ex = snap.getValue(Exercise::class.java)
            baseExercise = ex

            ex?.typeID?.takeIf { it.isNotBlank() }?.let { fetchTypeName(it) }
            ex?.muscleID?.takeIf { it.isNotBlank() }?.let { fetchMuscleName(it) }

        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del ejercicio", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        if (loading) {
            Box(
                Modifier.fillMaxSize().background(gradient),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color.White) }
            return@Scaffold
        }

        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(gradient)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val ex = baseExercise
            val mediaUrl = ex?.imageUrl ?: ""
            val context = LocalContext.current

            // ==========================
            // VIDEO O IMAGEN
            // ==========================

            if (mediaUrl.endsWith(".mp4")) {

                val player = remember(mediaUrl) {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(mediaUrl.toUri()))
                        prepare()
                        playWhenReady = false
                    }
                }

                DisposableEffect(Unit) { onDispose { player.release() } }

                AndroidView(
                    factory = { PlayerView(it).apply { this.player = player } },
                    modifier = Modifier.fillMaxWidth().height(230.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

            } else {
                Image(
                    painter = rememberAsyncImagePainter(
                        mediaUrl.ifEmpty {
                            "https://cdn-icons-png.flaticon.com/512/9022/9022314.png"
                        }
                    ),
                    contentDescription = ex?.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(230.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(Modifier.height(16.dp))

            // ==========================
            // TITULOS E INFO BASE
            // ==========================

            Text(
                ex?.name ?: "Ejercicio",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text("Tipo: $typeName", color = Color(0xFFFFCDD2))
            Text("Grupo muscular: $muscleName", color = Color(0xFFFFCDD2))

            if (!ex?.description.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("Descripción", color = Color.White, fontWeight = FontWeight.Bold)
                Text(ex!!.description, color = Color.White.copy(alpha = 0.9f))
            }

            Spacer(Modifier.height(20.dp))

            // ==========================
            // PARÁMETROS DEL COACH
            // ==========================

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Parámetros asignados por tu coach", color = Color.White, fontSize = 18.sp)

                    Spacer(Modifier.height(12.dp))

                    InfoRow("Series", exercise.sets.toString(), "Reps", exercise.reps.toString())
                    InfoRow("Peso (kg)", exercise.weight.toString(), "Velocidad", exercise.speed.toString())
                    InfoRow("Duración (min)", exercise.duration.toString(), "", "")
                }
            }

            Spacer(Modifier.height(20.dp))

            // ==========================
            // PANEL EDITABLE
            // ==========================

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Personalizar tu sesión", color = Color.White, fontSize = 18.sp)

                    Spacer(Modifier.height(12.dp))

                    EditableRow("Series", sets, { sets = it }, "Reps", reps, { reps = it })
                    EditableRow("Peso (kg)", weight, { weight = it }, "Velocidad", speed, { speed = it })

                    Spacer(Modifier.height(12.dp))

                    InputField("Duración (min)", duration) { duration = it }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ==========================
            // GUARDAR CAMBIOS
            // ==========================

            Button(
                onClick = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            "exerciseEdited",
                            exercise.copy(
                                sets = sets.toIntOrNull() ?: exercise.sets,
                                reps = reps.toIntOrNull() ?: exercise.reps,
                                weight = weight.toIntOrNull() ?: exercise.weight,
                                speed = speed.toIntOrNull() ?: exercise.speed,
                                duration = duration.toIntOrNull() ?: exercise.duration
                            )
                        )
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB1163A))
            ) {
                Text("Guardar cambios", color = Color.White)
            }
        }
    }
}

// ==========================
// COMPONENTES REUTILIZABLES
// ==========================

@Composable
fun InfoRow(label1: String, v1: String, label2: String, v2: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text("$label1: $v1", color = Color.White)
        if (label2.isNotBlank()) Text("$label2: $v2", color = Color.White)
    }
}

@Composable
fun EditableRow(
    label1: String, v1: String, on1: (String) -> Unit,
    label2: String, v2: String, on2: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        InputField(label1, v1, on1)
        InputField(label2, v2, on2)
    }
}

@Composable
fun InputField(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        Text(label, color = Color(0xFFFFCDD2))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.width(150.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD12B56),
                unfocusedBorderColor = Color.White,
                cursorColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(color = Color.White)
        )
    }
}
