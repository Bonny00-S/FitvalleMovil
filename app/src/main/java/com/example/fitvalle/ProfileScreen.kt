package com.example.fitvalle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
    val primario = Color(0xFFB1163A)

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid

    // 🔹 Referencias a la base de datos
    val dbUsers = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("user")
    val dbCustomers = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("customer")
    val dbAvatars = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("users")

    var userData by remember { mutableStateOf<User?>(null) }
    var customerData by remember { mutableStateOf<Customer?>(null) }
    var avatarName by remember { mutableStateOf("avatar1") }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Cargar datos
    LaunchedEffect(userId) {
        if (userId != null) {
            // Cargar usuario
            dbUsers.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userData = snapshot.getValue(User::class.java)

                    // Cargar datos de cliente
                    dbCustomers.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(customerSnap: DataSnapshot) {
                            customerData = customerSnap.getValue(Customer::class.java)
                            isLoading = false
                        }

                        override fun onCancelled(error: DatabaseError) {
                            isLoading = false
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })

            // 🔄 Escuchar avatar en tiempo real
            dbAvatars.child(userId).child("avatar")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        avatarName = snapshot.getValue(String::class.java) ?: "avatar1"
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    // 🔹 Selección de imagen del avatar
    val avatarRes = when (avatarName) {
        "avatar1" -> R.drawable.avartar1
        "avatar2p" -> R.drawable.avatar2p
        "avatar3p" -> R.drawable.avartar3p
        "avatar4p" -> R.drawable.avatar4p
        "avatar5p" -> R.drawable.avatar5p
        "avatar6p" -> R.drawable.avatar6p
        else -> R.drawable.avartar1
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
        } else {
            if (userData != null) {
                val user = userData!!
                val customer = customerData

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Perfil del Usuario",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 👤 Avatar
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = "Avatar actual",
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E1A1A))
                            .clickable { navController.navigate("editAvatar") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Avatar actual", color = Color.White, fontSize = 15.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔹 Nombre y correo
                    Text(
                        text = user.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        color = Color(0xFFFFCDD2),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color.White.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔹 Datos del cliente
                    ProfileItem("Fecha de nacimiento", customer?.birthdate ?: "-")
                    ProfileItem("Altura (cm)", customer?.height ?: "-")
                    ProfileItem("Peso actual (kg)", customer?.weight ?: "-")
                    ProfileItem("Peso objetivo (kg)", customer?.goalWeight ?: "-")

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color.White.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // ⚙️ Configuraciones
                    Text(
                        text = "Configuraciones",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileItem("Unidades de medida", "kg / cm")
                    ProfileItem("Notificaciones", "Activadas")

                    Spacer(modifier = Modifier.height(40.dp))

                    // 🧑‍🏫 Botón para ver entrenadores
                    Button(
                        onClick = { navController.navigate("coaches") },
                        colors = ButtonDefaults.buttonColors(containerColor = primario),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Ver Entrenadores", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔴 Cerrar sesión
                    OutlinedButton(
                        onClick = { onLogout() },
                        border = BorderStroke(2.dp, SolidColor(primario)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primario),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            } else {
                Text(
                    text = "No se encontraron datos del usuario.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFFFFCDD2),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value.ifEmpty { "-" },
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
