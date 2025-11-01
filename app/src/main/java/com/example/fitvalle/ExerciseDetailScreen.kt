package com.example.fitvalle.ui.screens

import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.fitvalle.Exercise
import com.example.fitvalle.ExerciseDao
import com.example.fitvalle.ExerciseType
import com.google.firebase.database.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun ExerciseDetailScreen(exerciseId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))

    // Loader para GIFs
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (android.os.Build.VERSION.SDK_INT >= 28)
                add(ImageDecoderDecoder.Factory())
            else
                add(GifDecoder.Factory())
        }
        .build()

    var exercise by remember { mutableStateOf<Exercise?>(null) }
    var type by remember { mutableStateOf<ExerciseType?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Cargar datos de Firebase
    LaunchedEffect(exerciseId) {
        val dao = ExerciseDao()
        dao.getExerciseById(exerciseId) { ex ->
            exercise = ex
            if (ex != null) {
                FirebaseDatabase.getInstance()
                    .getReference("exerciseTypes")
                    .child(ex.typeID.trim())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            type = snapshot.getValue(ExerciseType::class.java)
                            loading = false
                        }
                        override fun onCancelled(error: DatabaseError) {
                            loading = false
                        }
                    })
            } else loading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        when {
            loading -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            exercise == null -> {
                Text(
                    text = "Ejercicio no encontrado",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                val ex = exercise!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }

                    Text(
                        text = ex.name,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    if (ex.imageUrl.endsWith(".mp4")) {
                        val exoPlayer = remember {
                            ExoPlayer.Builder(context).build().apply {
                                val mediaItem = MediaItem.fromUri(ex.imageUrl)
                                setMediaItem(mediaItem)
                                prepare()
                                playWhenReady = true
                                repeatMode = Player.REPEAT_MODE_ALL
                                volume = 0f
                            }
                        }

                        // 🔁 Asegurar que el player se libere correctamente
                        DisposableEffect(Unit) {
                            onDispose {
                                exoPlayer.release()
                            }
                        }

                        // 🔹 AndroidView directo, sin layouts intermedios ni RoundedCornerShape (eso puede tapar el video)
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false
                                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM // 🔹 Llenar todo
                                    layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                    )
                                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                                }

                            },
                            update = { view ->
                                view.player = exoPlayer
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(Color.Black)
                        )
                    }
                    else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(ex.imageUrl)
                                .crossfade(true)
                                .build(),
                            imageLoader = imageLoader,
                            contentDescription = ex.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Tipo: ${type?.name ?: "Desconocido"}",
                                color = Color(0xFFFFCDD2),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Descripción:",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = ex.description.ifBlank { "Sin descripción disponible." },
                                color = Color.White,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
