package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingSessionScreen(navController: NavController, templateId: String) {
    val fondo = Brush.verticalGradient(listOf(Color(0xFF0D1525), Color(0xFF1F1C18)))
    val rojo = Color(0xFFB1163A)

    // 🔹 Cargar la plantilla desde Firebase
    var template by remember { mutableStateOf<Template?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(templateId) {
        val db = com.google.firebase.database.FirebaseDatabase
            .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
            .getReference("templates")

        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.child(userId).child(templateId).get().addOnSuccessListener { snap ->
                template = snap.getValue(Template::class.java)
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutina de hoy", color = Color.White) },
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
        bottomBar = {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = rojo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("FINALIZAR RUTINA", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(padding)
                .padding(16.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                template?.let {
                    Column {
                        Text(
                            text = it.name,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${it.exercises.size} ejercicios incluidos",
                            color = Color(0xFFFFCDD2)
                        )
                        Spacer(Modifier.height(16.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            itemsIndexed(it.exercises) { index, name ->
                                ExerciseItemCard(index + 1, name)
                            }
                        }
                    }
                } ?: Text("No se encontró la rutina", color = Color.White)
            }
        }
    }
}

@Composable
fun ExerciseItemCard(index: Int, name: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A1A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("$index. $name", color = Color.White, fontSize = 18.sp)
                Text("4 series x 10 repeticiones", color = Color(0xFFFFCDD2), fontSize = 14.sp)
            }
            Checkbox(
                checked = false,
                onCheckedChange = {},
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFD50000),
                    uncheckedColor = Color.White
                )
            )
        }
    }
}
