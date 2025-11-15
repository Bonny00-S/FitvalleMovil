package com.example.fitvalle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachesScreen(navController: NavController) {

    // 🎨 Paleta institucional
    val fondoPrincipal = Color(0xFF0D1525)
    val fondoSecundario = Color(0xFF182235)
    val primario = Color(0xFFB1163A)
    val textoSecundario = Color(0xFFAAB2C5)

    val db = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("user")

    var coaches by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // 🔹 Cargar entrenadores (usuarios con role = coach)
    LaunchedEffect(Unit) {
        db.get().addOnSuccessListener { snap ->
            val list = snap.children.mapNotNull { it.getValue(User::class.java) }
            coaches = list.filter { it.role?.lowercase() == "coach" }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrenadores", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = fondoPrincipal)
            )
        },
        containerColor = fondoPrincipal
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(fondoPrincipal)
                .padding(padding)
        ) {
            when {
                loading -> CircularProgressIndicator(
                    color = primario,
                    modifier = Modifier.align(Alignment.Center)
                )
                coaches.isEmpty() -> Text(
                    "No hay entrenadores disponibles.",
                    color = textoSecundario,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(coaches) { coach ->
                        CoachCard(
                            coach = coach,
                            fondoSecundario = fondoSecundario,
                            primario = primario,
                            textoSecundario = textoSecundario
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoachCard(
    coach: User,
    fondoSecundario: Color,
    primario: Color,
    textoSecundario: Color
) {
    // 🔹 Detecta y corrige rutas incompletas automáticamente
    val imageUrl = when {
        coach.photoUrl.isBlank() -> "https://cdn-icons-png.flaticon.com/512/3135/3135715.png"
        coach.photoUrl.startsWith("/uploads") ->
            "https://fitvalle-fced7-default-rtdb.firebaseio.com${coach.photoUrl}"
        coach.photoUrl.startsWith("gs://") ->
            coach.photoUrl.replace(
                "gs://fitvalle-fced7.appspot.com/",
                "https://firebasestorage.googleapis.com/v0/b/fitvalle-fced7.appspot.com/o/"
            ) + "?alt=media"
        else -> coach.photoUrl
    }


    Card(
        colors = CardDefaults.cardColors(containerColor = fondoSecundario),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 📷 Foto del entrenador
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = coach.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D1525))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    coach.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (coach.specialty.isNotEmpty()) {
                    Text(coach.specialty, color = primario, fontSize = 13.sp)
                }

                if (coach.description.isNotEmpty()) {
                    Text(
                        coach.description,
                        color = textoSecundario,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Alumnos: ${coach.students}",
                        color = textoSecundario,
                        fontSize = 12.sp
                    )
                    Text(
                        "Activos: ${coach.activeStudents}",
                        color = primario,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
