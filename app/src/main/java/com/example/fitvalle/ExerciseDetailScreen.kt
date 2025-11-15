package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.firebase.database.*
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)

//@OptIn(ExperimentalMaterial3Api::class, UnstableApi::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseId: String
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))

    val db = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
    val exerciseRef = db.getReference("exercise").child(exerciseId)
    val musclesRef = db.getReference("targetMuscles")

    var exercise by remember { mutableStateOf<Exercise?>(null) }
    var muscleName by remember { mutableStateOf<String?>(null) }
    var typeName by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    // --- helpers -----------------------------------------------------------------
    fun fetchTypeNameById(typeId: String, onDone: (String?) -> Unit) {
        // 1) intenta /exerciseTypes/{id}/name
        db.getReference("exerciseTypes").child(typeId).child("name").get()
            .addOnSuccessListener { snap ->
                val n = snap.getValue(String::class.java)
                if (!n.isNullOrBlank()) {
                    onDone(n); return@addOnSuccessListener
                }
                // 2) intenta /types/{id}/name
                db.getReference("types").child(typeId).child("name").get()
                    .addOnSuccessListener { snap2 ->
                        onDone(snap2.getValue(String::class.java))
                    }
                    .addOnFailureListener { onDone(null) }
            }
            .addOnFailureListener { onDone(null) }
    }

    fun fetchMuscleNameById(muscleId: String, onDone: (String?) -> Unit) {
        musclesRef.child(muscleId).child("name").get()
            .addOnSuccessListener { onDone(it.getValue(String::class.java)) }
            .addOnFailureListener { onDone(null) }
    }
    // -----------------------------------------------------------------------------

    // Cargar datos
    LaunchedEffect(exerciseId) {
        exerciseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ex = snapshot.getValue(Exercise::class.java)
                exercise = ex

                ex?.muscleID?.takeIf { it.isNotBlank() }?.let { id ->
                    fetchMuscleNameById(id) { muscleName = it }
                }
                ex?.typeID?.takeIf { it.isNotBlank() }?.let { id ->
                    fetchTypeNameById(id) { typeName = it }
                }

                loading = false
            }
            override fun onCancelled(error: DatabaseError) { loading = false }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
        Box(
            Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(16.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            } else {
                exercise?.let { ex ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(ex.name, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        // Video (si imageUrl es mp4)
                        val isVideo = ex.imageUrl.endsWith(".mp4", ignoreCase = true)
                        if (isVideo) {
                            val context = LocalContext.current
                            val player = remember(ex.imageUrl) {
                                ExoPlayer.Builder(context).build().apply {
                                    setMediaItem(MediaItem.fromUri(ex.imageUrl.toUri()))
                                    prepare()
                                    playWhenReady = false
                                }
                            }
                            DisposableEffect(Unit) { onDispose { player.release() } }

                            Card(Modifier.fillMaxWidth().height(220.dp)) {
                                AndroidView(factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        this.player = player
                                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                                        setControllerShowTimeoutMs(2000)
                                    }
                                })
                            }
                        } else {
                            Card(
                                Modifier.fillMaxWidth().height(200.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF))
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Video no disponible", color = Color.White.copy(alpha = 0.8f))
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Ficha
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                // 🔹 SIEMPRE mostramos la fila de Tipo (con fallback “-”)
                                Text(
                                    "Tipo: ${typeName ?: "-"}",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )

                                // Grupo muscular (si se pudo resolver)
                                Text(
                                    "Grupo Muscular: ${muscleName ?: "-"}",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (ex.description.isNotBlank()) {
                                    Text("Descripción:", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(ex.description, color = Color.White.copy(alpha = 0.95f))
                                }

                                if (ex.series > 0 || ex.repetitions.isNotBlank() || ex.restTime > 0) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Parámetros sugeridos", color = Color.White, fontWeight = FontWeight.Bold)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Series: ${if (ex.series > 0) ex.series else "-"}", color = Color.White)
                                        Text("Reps: ${if (ex.repetitions.isNotBlank()) ex.repetitions else "-"}", color = Color.White)
                                        Text("Descanso: ${if (ex.restTime > 0) "${ex.restTime}s" else "-"}", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                } ?: Text("No se encontró el ejercicio.", color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
