package com.example.fitvalle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarEditScreen(navController: NavController) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    val primario = Color(0xFFB1163A)
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/").getReference("users")

    val avatars = listOf(
        R.drawable.avartar1,
        R.drawable.avatar2p,
        R.drawable.avartar3p,
        R.drawable.avatar4p,
        R.drawable.avatar5p,
        R.drawable.avatar6p
    )

    var selectedAvatar by remember { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecciona tu avatar", color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Elige tu imagen de perfil", color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(avatars) { avatar ->
                        Card(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { selectedAvatar = avatar },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedAvatar == avatar)
                                    primario.copy(alpha = 0.6f)
                                else
                                    Color(0xFF2E1A1A)
                            )
                        ) {
                            Image(
                                painter = painterResource(id = avatar),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (selectedAvatar != null && userId != null) {
                            val avatarName = when (selectedAvatar) {
                                R.drawable.avartar1 -> "avatar1"
                                R.drawable.avatar2p -> "avatar2p"
                                R.drawable.avartar3p -> "avatar3p"
                                R.drawable.avatar4p -> "avatar4p"
                                R.drawable.avatar5p -> "avatar5p"
                                R.drawable.avatar6p -> "avatar6p"
                                else -> "avatar1"
                            }
                            db.child(userId).child("avatar").setValue(avatarName)
                            scope.launch {
                                snackbarHostState.showSnackbar("✅ Avatar actualizado correctamente")
                            }
                            navController.popBackStack()
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("⚠️ Selecciona un avatar antes de continuar")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primario),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("GUARDAR", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}
