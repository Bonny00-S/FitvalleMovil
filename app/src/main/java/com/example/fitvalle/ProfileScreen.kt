package com.example.fitvalle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fitvalle.R
import com.example.fitvalle.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class Customer(
    val id: String = "",
    val birthdate: String = "",
    val registerDate: String = "",
    val weight: String = "",
    val goalWeight: String = "",
    val height: String = ""
)

@Composable
fun ProfileScreen(
    navController: NavController,
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF8E0E00), Color(0xFF1F1C18))
    )

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val dbRefUser = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("user")
    val dbRefCustomer = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("customer")

    var userData by remember { mutableStateOf<User?>(null) }
    var customerData by remember { mutableStateOf<Customer?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Obtener datos de usuario y cliente
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            dbRefUser.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userData = snapshot.getValue(User::class.java)

                    // Luego obtenemos los datos adicionales del customer
                    dbRefCustomer.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
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
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
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

                    // 👤 Imagen de perfil
                    Image(
                        painter = painterResource(id = R.drawable.profile_placeholder),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color(0x33FFFFFF), shape = MaterialTheme.shapes.large)
                            .clickable {

                            navController.navigate("editAvatar")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

                    // ℹ️ Datos básicos
                    //ProfileItem("Fecha de Registro", customer?.registerDate ?: "-")
                    ProfileItem("Fecha de Nacimiento", customer?.birthdate ?: "-")
                    ProfileItem("Altura (cm)", customer?.height ?: "-")
                    ProfileItem("Peso Actual (kg)", customer?.weight ?: "-")
                    ProfileItem("Peso Objetivo (kg)", customer?.goalWeight ?: "-")

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color.White.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(24.dp))

                    // ⚙️ Configuración
                    Text(
                        text = "Configuraciones",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileItem("Unidades de Medida", "kg / cm")
                    ProfileItem("Notificaciones", "Activadas")

                    Spacer(modifier = Modifier.height(32.dp))

                    // 🔴 Botón de Cerrar Sesión con confirmación
                    var showLogoutDialog by remember { mutableStateOf(false) }

                    if (showLogoutDialog) {
                        AlertDialog(
                            onDismissRequest = { showLogoutDialog = false },
                            title = {
                                Text(
                                    text = "Cerrar sesión",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Text(
                                    text = "¿Seguro que deseas cerrar sesión?",
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showLogoutDialog = false
                                        auth.signOut()
                                        onLogout()
                                    }
                                ) {
                                    Text("Cerrar sesión", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showLogoutDialog = false }) {
                                    Text("Cancelar", color = Color.White)
                                }
                            },
                            containerColor = Color(0xFF2B1A1A)
                        )
                    }

                    Button(
                        onClick = { showLogoutDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar sesión", color = Color.White, fontSize = 16.sp)
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
