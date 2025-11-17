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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
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
    var selectedSharedAvatarId by remember { mutableStateOf<String?>(null) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var sharePublic by remember { mutableStateOf(false) }
    var sharedAvatars by remember { mutableStateOf<List<SharedAvatarItem>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) pickedUri = uri
    }

    // Cargar avatares compartidos al entrar a la pantalla
    LaunchedEffect(Unit) {
        SharedAvatarDao.getSharedAvatars(
            onSuccess = { avatars ->
                sharedAvatars = avatars
            },
            onFailure = { ex ->
                scope.launch {
                    snackbarHostState.showSnackbar("⚠️ No se pudieron cargar avatares compartidos")
                }
            }
        )
    }

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

                // Sección para subir avatar compartido desde el dispositivo
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Subir avatar desde el dispositivo (opcional)", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { launcher.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = primario)) {
                            Text("Seleccionar imagen", color = Color.White)
                        }
                        if (pickedUri != null) {
                            Button(onClick = { pickedUri = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                                Text("Quitar", color = Color.White)
                            }
                        }
                    }

                    pickedUri?.let { uri ->
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(model = uri, contentDescription = "Preview avatar", modifier = Modifier.size(80.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = sharePublic, onCheckedChange = { sharePublic = it })
                            Text("Compartir para todos", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            if (pickedUri != null) {
                                AvatarDao.uploadSharedAvatar(context, pickedUri!!, null, onSuccess = { id ->
                                    scope.launch { 
                                        snackbarHostState.showSnackbar("✅ Avatar compartido subido")
                                        // Recargar avatares después de subir
                                        SharedAvatarDao.getSharedAvatars(
                                            onSuccess = { avatars -> sharedAvatars = avatars },
                                            onFailure = { }
                                        )
                                    }
                                    pickedUri = null
                                    sharePublic = false
                                }, onFailure = { ex ->
                                    scope.launch { snackbarHostState.showSnackbar("❌ Error: ${ex.message}") }
                                })
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("⚠️ Selecciona una imagen primero") }
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = primario)) {
                            Text("SUBIR AVATAR COMPARTIDO", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sección de avatares compartidos (subidos por otros usuarios)
                if (sharedAvatars.isNotEmpty()) {
                    Text("Avatares compartidos", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                    ) {
                        items(sharedAvatars.size) { index ->
                            val item = sharedAvatars[index]
                            
                            // Decodificar Base64 fuera de la composable
                            val bitmap = remember(item.id) {
                                try {
                                    val decodedBytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                } catch (ex: Exception) {
                                    null
                                }
                            }
                            
                            Card(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clickable { selectedSharedAvatarId = item.id },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedSharedAvatarId == item.id)
                                        primario.copy(alpha = 0.6f)
                                    else
                                        Color(0xFF2E1A1A)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Avatar compartido: ${item.name}",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text("Error", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        val isLocalSelected = selectedAvatar != null
                        val isSharedSelected = selectedSharedAvatarId != null

                        if (isLocalSelected && userId != null) {
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
                        } else if (isSharedSelected && userId != null) {
                            // Guardar ID del avatar compartido (para recuperarlo desde Database después)
                            db.child(userId).child("avatar").setValue("shared_$selectedSharedAvatarId")
                            scope.launch {
                                snackbarHostState.showSnackbar("✅ Avatar compartido seleccionado")
                            }
                            navController.popBackStack()
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("⚠️ Selecciona un avatar antes de continuar")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primario),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("GUARDAR", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}
